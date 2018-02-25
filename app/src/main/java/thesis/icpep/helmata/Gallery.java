package thesis.icpep.helmata;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.cognito.CognitoSyncManager;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class Gallery extends ListActivity {

    CognitoCachingCredentialsProvider credentialsProvider;
    ListView listView;
    ArrayList<String> arrayList;
    ArrayAdapter<String> mAdapter;
    private static  final int MY_PERMISSION_REQUEST = 1;
    private String item;

    private Context context;
    private File theFile;
    private String username;
    List<String> listing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_gallery);

        SharedPreferences userPrefs = getSharedPreferences("user", MODE_PRIVATE);
        username = userPrefs.getString("user", null);

        AWSMobileClient.getInstance().initialize(this).execute();

        credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "us-east-1:f56a94ef-db6a-4c04-89d2-ec75a9ccb023", // Identity pool ID
                Regions.US_EAST_1); // Region

        CognitoSyncManager syncClient = new CognitoSyncManager(
                getApplicationContext(),
                Regions.US_EAST_1, // Region
                credentialsProvider);

        if(ContextCompat.checkSelfPermission(Gallery.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(Gallery.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)){
                ActivityCompat.requestPermissions(Gallery.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSION_REQUEST);
            } else {
                ActivityCompat.requestPermissions(Gallery.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSION_REQUEST);
            }
        } else {
            fetchFileFromS3();
        }

        listView = getListView();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                item = String.valueOf(parent.getItemAtPosition(position));

                //dialog to download
                final AlertDialog.Builder builder = new AlertDialog.Builder(Gallery.this);
                builder.setTitle("Download");

//                final EditText input = new EditText(Gallery.this);
//                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
//                        LinearLayout.LayoutParams.MATCH_PARENT,
//                        LinearLayout.LayoutParams.MATCH_PARENT);
//                input.setLayoutParams(lp);
//                builder.setView(input); // uncomment this line
//                builder.setSingleChoiceItems(items, -1,
//                        new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int item) {
//                                Toast.makeText(getApplicationContext(), items[item],
//                                        Toast.LENGTH_SHORT).show();
//                            }
//                        });

                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        download();
                    }
                });

//                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                    }
//                });

                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    public void fetchFileFromS3(){

        // Get List of files from S3 Bucket
        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {
                AmazonS3 s3 = new AmazonS3Client(credentialsProvider);
                try {
                    Looper.prepare();
                    listing = getObjectNamesForBucket("helmata", s3);

                    for (int i=0; i< listing.size(); i++){
                        Toast.makeText(Gallery.this, listing.get(i),Toast.LENGTH_LONG).show();
                    }
                    Looper.loop();
                    // Log.e("tag", "listing "+ listing);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    Log.e("tag", "Exception found while listing "+ e);
                }

            }
        });
        thread.start();
    }

    private List<String> getObjectNamesForBucket(String bucket, AmazonS3 s3Client) {
        ObjectListing objects=s3Client.listObjects(bucket);
        List<String> objectNames=new ArrayList<String>(objects.getObjectSummaries().size());
        Iterator<S3ObjectSummary> iterator=objects.getObjectSummaries().iterator();
        while (iterator.hasNext()) {
            objectNames.add(iterator.next().getKey());
        }
        while (objects.isTruncated()) {
            objects=s3Client.listNextBatchOfObjects(objects);
            iterator=objects.getObjectSummaries().iterator();
            while (iterator.hasNext()) {
                objectNames.add(iterator.next().getKey());
            }
        }
        return objectNames;
    }

    public void files(){
        AmazonS3 s3 = new AmazonS3Client(credentialsProvider);

        ListObjectsRequest listObjectRequest = new ListObjectsRequest().
                withBucketName("helmata").
                withPrefix("Helmata").
                withDelimiter("/");

        ObjectListing current = (ObjectListing) s3.listObjects(listObjectRequest).getCommonPrefixes();

        List<S3ObjectSummary> keyList = current.getObjectSummaries();
        current = s3.listNextBatchOfObjects(current);

        while (current.isTruncated()){
            keyList.addAll(current.getObjectSummaries());
            current = s3.listNextBatchOfObjects(current);
        }
        keyList.addAll(current.getObjectSummaries());

        Toasty.info(Gallery.this, keyList.toString(), Toast.LENGTH_LONG);
    }

    //get files in amazon s3 bucket
    public void getFiles(){
        arrayList = new ArrayList<>();
        SharedPreferences prefs = getSharedPreferences("filename", MODE_PRIVATE);
        String filename = prefs.getString("filename", null);

        if(filename == null){
            Toast.makeText(this, "Empty", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, arrayList.toString(), Toast.LENGTH_SHORT).show();
            for(int i = 0; i <= arrayList.size(); i++){
                if(arrayList.get(i) != ""){
                    int x = i + 1;

                    arrayList.add(x,filename);
                } else {
                    arrayList.add(i, filename);
                }

            }
            mAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, arrayList);
            getListView().setAdapter(mAdapter);
        }
    }

    //download video from amazon bucket
    public void download(){
        AmazonS3 s3 = new AmazonS3Client(credentialsProvider);
        TransferUtility transferUtility = new TransferUtility(s3, getApplicationContext());

        theFile = new File(Environment.getExternalStorageDirectory(), "Helmata/" + item);

        TransferObserver transferObserver = transferUtility.download(
                "helmata",     /* The bucket to download from */
                username + "/" + item,    /* The key for the object to download */
                theFile        /* The file to download the object to */
        );

        transferObserver.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (state.equals(TransferState.COMPLETED)) {
                    Toasty.success(getApplicationContext(), "Download Complete", Toast.LENGTH_SHORT).show();
                } else if (state.equals(TransferState.FAILED)) {
                    Toasty.error(getApplicationContext(), "Unable to Download Video", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                float percentDonef = ((float)bytesCurrent/(float)bytesTotal) * 100;
                int percentDone = (int)percentDonef;

                Toasty.info(getApplicationContext(), percentDone + "% Downloaded",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(int id, Exception ex) {
                Toasty.error(getApplicationContext(), "Please Try Again", Toast.LENGTH_SHORT).show();
            }
        });

    }
}
