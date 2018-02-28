package thesis.icpep.helmata;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
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
    private List<String> listValues;
    private String trim;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };


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

        ArrayList<String> myList = (ArrayList<String>) getIntent().getSerializableExtra("mylist");
        if(myList == null || myList.size() < 1){
            Toasty.error(Gallery.this, "Check your Connection or Username", Toast.LENGTH_LONG).show();
        } else {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(Gallery.this, android.R.layout.simple_list_item_1,
                    myList);
            setListAdapter(adapter);
        }



        listView = getListView();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                item = String.valueOf(parent.getItemAtPosition(position));
                trim = item.substring(item.lastIndexOf("/")+1);
                final String editedItem = item.replaceAll(":", "%");
//                Toasty.info(Gallery.this, editedItem, 300).show();

                //dialog to download
                final AlertDialog.Builder builder = new AlertDialog.Builder(Gallery.this);
                builder.setTitle("Download?");

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
                        Toasty.info(Gallery.this, "Downloading..", 300).show();
                        download();
                    }
                });


                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
//                        Intent intent;
//
//
//
//                        verifyStoragePermissions(Gallery.this);
//                        Uri uri = Uri.parse(editedItem);
//
//                        intent = new Intent(Gallery.this, Player.class);
//                        intent.putExtra(Player.VIDEO_TYPE, Player.SIMPLE_VIDEO);
//                        intent.putExtra(Player.VIDEO_URI, "https://s3.amazonaws.com/helmata/verrell/02-27-2018_15%3A45%3A37.mp4" + uri);
//                        startActivity(intent);
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        startActivity(new Intent(Gallery.this,MainActivity.class));
    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    //download video from amazon bucket
    public void download(){
        AmazonS3 s3 = new AmazonS3Client(credentialsProvider);
        TransferUtility transferUtility = new TransferUtility(s3, getApplicationContext());

        theFile = new File(Environment.getExternalStorageDirectory(), "Helmata/" + trim);

        TransferObserver transferObserver = transferUtility.download(
                "helmata",     /* The bucket to download from */
                username + "/" + trim,    /* The key for the object to download */
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
                Toasty.error(getApplicationContext(), "Set your username.", Toast.LENGTH_SHORT).show();
            }
        });

    }
}
