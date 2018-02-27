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
    private List<String> listValues;
    private String trim;


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
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(Gallery.this, android.R.layout.simple_list_item_1,
                myList);
        setListAdapter(adapter);


        listView = getListView();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                item = String.valueOf(parent.getItemAtPosition(position));
                trim = item.substring(item.lastIndexOf("/")+1);

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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(Gallery.this,MainActivity.class));
        finish();
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
