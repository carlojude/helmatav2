package thesis.icpep.helmata;

import android.accessibilityservice.AccessibilityService;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.PowerManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ToggleButton;

import es.dmoral.toasty.Toasty;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;
import android.Manifest;
import android.annotation.TargetApi;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;

import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Offline extends AppCompatActivity {

    VideoView videoView;
    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE = 1000;
    private int mScreenDensity;
    private MediaProjectionManager mProjectionManager;
    private static final int DISPLAY_WIDTH = 720;
    private static final int DISPLAY_HEIGHT = 1280;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private MediaProjectionCallback mMediaProjectionCallback;
    private ToggleButton mToggleButton;
    private MediaRecorder mMediaRecorder;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private static final int REQUEST_PERMISSIONS = 10;
    private String formattedDate;
    private String formattedTime;
    private String ip;
    private String curIp;

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_online);

        final int orientation = this.getResources().getConfiguration().orientation;

        final Button orientationBtn = (Button)findViewById(R.id.fabOrientation);
        final Button portrait = (Button)findViewById(R.id.fabOrientation2) ;
        portrait.setVisibility(View.GONE);

        orientationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (orientation == Configuration.ORIENTATION_PORTRAIT){
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

                }
                orientationBtn.setVisibility(View.GONE);
                portrait.setVisibility(View.VISIBLE);
            }
        });

        portrait.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (orientation == Configuration.ORIENTATION_LANDSCAPE){
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

                }
                portrait.setVisibility(View.GONE);
                orientationBtn.setVisibility(View.VISIBLE);
            }
        });
//

//        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)videoView.getLayoutParams();
//            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//            videoView.setLayoutParams(params); //causes layout update
//        } else {
//            //code for portrait mode
//        }

        //get date and time
        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        //set format of date and time
        SimpleDateFormat df = new SimpleDateFormat("MM-dd-yyyy");
        formattedDate = df.format(c);

        SimpleDateFormat tf = new SimpleDateFormat("HH:mm:ss");
        formattedTime = tf.format(c);

        //pass data from mainactivity
        SharedPreferences prefs = getSharedPreferences("IPADD", MODE_PRIVATE);
        ip = prefs.getString("ip", null);
        curIp = ip;

        //display ip cam stream
        videoView = this.findViewById(R.id.videoView);

        //set height of videoView
        int width = getResources().getDisplayMetrics().widthPixels;
        int hei=getResources().getDisplayMetrics().heightPixels;

//        videoView.setLayoutParams(new RelativeLayout.LayoutParams(width, hei));

        //add controls to a MediaPlayer like play, pause.
        MediaController mc = new MediaController(this);
        videoView.setMediaController(mc);

        //Set the path of Video or URI rtsp://184.72.239.149/vod/mp4:BigBuckBunny_175k.mov
        videoView.setVideoURI(Uri.parse("rtsp://" + curIp + "/ch0_1.h264"));
//        videoView.setVideoURI(Uri.parse("rtsp://184.72.239.149/vod/mp4:BigBuckBunny_175k.mov"));

        //Set the focus
        videoView.requestFocus();

        //play stream
        videoView.start();

        //Record Screen
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;

        mMediaRecorder = new MediaRecorder();

        mProjectionManager = (MediaProjectionManager) getSystemService
                (Context.MEDIA_PROJECTION_SERVICE);

        //record/stop buttons
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        final FloatingActionButton fabStop = (FloatingActionButton) findViewById(R.id.fabStop);

        fabStop.setVisibility(View.GONE);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        final FloatingActionButton finalFab = fab;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(Offline.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) + ContextCompat
                        .checkSelfPermission(Offline.this,
                                Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale
                            (Offline.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                            ActivityCompat.shouldShowRequestPermissionRationale
                                    (Offline.this, Manifest.permission.RECORD_AUDIO)) {
                        mToggleButton.setChecked(false);
                        Snackbar.make(findViewById(android.R.id.content), "Permission",
                                Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        ActivityCompat.requestPermissions(Offline.this,
                                                new String[]{Manifest.permission
                                                        .WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
                                                REQUEST_PERMISSIONS);
                                    }
                                }).show();
                    } else {
                        ActivityCompat.requestPermissions(Offline.this,
                                new String[]{Manifest.permission
                                        .WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
                                REQUEST_PERMISSIONS);
                    }
                } else {
                    Toasty.info(Offline.this, "Recording..", 300).show();
                    initRecorder();
                    shareScreen();
                    finalFab.setVisibility(View.GONE);
                    orientationBtn.setVisibility(View.GONE);
                    portrait.setVisibility(View.GONE);
//                    fabStop.setVisibility(View.VISIBLE);

                }
            }
        });


        fabStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMediaRecorder.stop();
                mMediaRecorder.reset();
                Log.v(TAG, "Stopping Recording");
                stopScreenSharing();
                fabStop.setVisibility(View.GONE);
                finalFab.setVisibility(View.VISIBLE);
                if (orientation == Configuration.ORIENTATION_PORTRAIT){
                    orientationBtn.setVisibility(View.VISIBLE);
                }
                if (orientation == Configuration.ORIENTATION_LANDSCAPE){
                    portrait.setVisibility(View.VISIBLE);
                }
                Toasty.success(Offline.this, "Recording has been saved.", Toast.LENGTH_LONG).show();
            }
        });

        videoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                FloatingActionButton fab1 = (FloatingActionButton) findViewById(R.id.fab);
                FloatingActionButton fabStop1 = (FloatingActionButton) findViewById(R.id.fabStop);

                if(fabStop1.getVisibility() == View.VISIBLE && fab1.getVisibility() == View.GONE){
                    fabStop1.setVisibility(View.GONE);

                } else if(fab1.getVisibility() == View.GONE && fab1.getVisibility() == View.GONE){
                    fabStop1.setVisibility(View.VISIBLE);
                }
                return false;
            }
        });

        //stop recording if screen is locked
//        KeyguardManager myKM = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
//        if( myKM.inKeyguardRestrictedInputMode()) {
//            mMediaRecorder.stop();
//            mMediaRecorder.reset();
//            Log.v(TAG, "Stopping Recording");
//            stopScreenSharing();
//            fabStop.setVisibility(View.GONE);
//            finalFab.setVisibility(View.VISIBLE);
//        } else {
//            //it is not locked
//        }

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isInteractive();
        if(isScreenOn){

        } else {
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            Log.v(TAG, "Stopping Recording");
            stopScreenSharing();
            fabStop.setVisibility(View.GONE);
            finalFab.setVisibility(View.VISIBLE);
        }
    }

    //recorder permission
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_CODE) {
            Log.e(TAG, "Unknown request code: " + requestCode);
            return;
        }
        if (resultCode != RESULT_OK) {
            Toast.makeText(this,
                    "Screen Cast Permission Denied", Toast.LENGTH_SHORT).show();
            mToggleButton.setChecked(false);
            return;
        }
        mMediaProjectionCallback = new MediaProjectionCallback();
        mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
        mMediaProjection.registerCallback(mMediaProjectionCallback, null);
        mVirtualDisplay = createVirtualDisplay();
        mMediaRecorder.start();
    }

    //start recording
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void shareScreen() {
        if (mMediaProjection == null) {
            startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
            return;
        }
        mVirtualDisplay = createVirtualDisplay();
        mMediaRecorder.start();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private VirtualDisplay createVirtualDisplay() {
        Display screenOrientation = getWindowManager().getDefaultDisplay();
        int orientation = Configuration.ORIENTATION_UNDEFINED;

        if(screenOrientation.getWidth() < screenOrientation.getHeight()){
            return mMediaProjection.createVirtualDisplay("MainActivity",
                    DISPLAY_WIDTH, DISPLAY_HEIGHT, mScreenDensity,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    mMediaRecorder.getSurface(), null /*Callbacks*/, null
                /*Handler*/);

        }else {
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            int screenWidth = 1280;
            int screenHeight = 720;
            int screenDensity = metrics.densityDpi;

            return mMediaProjection.createVirtualDisplay("MainActivity",
                    screenWidth, screenHeight, screenDensity,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    mMediaRecorder.getSurface(), null /*Callbacks*/, null
                /*Handler*/);

        }

    }

    //initialize recorder
    private void initRecorder() {
        try {
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mMediaRecorder.setOutputFile(Environment
                    .getExternalStoragePublicDirectory("Helmata") + "/" + formattedDate + "_" + formattedTime + ".mp4");
            mMediaRecorder.setVideoSize(getResources().getDisplayMetrics().widthPixels,
                    getResources().getDisplayMetrics().heightPixels);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mMediaRecorder.setVideoEncodingBitRate(720 * 1280);
            mMediaRecorder.setVideoFrameRate(30);
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            int orientation = ORIENTATIONS.get(180);
            mMediaRecorder.setOrientationHint(orientation);
            mMediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private class MediaProjectionCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
            if (mToggleButton.isChecked()) {
                mToggleButton.setChecked(false);
                mMediaRecorder.stop();
                mMediaRecorder.reset();
                Log.v(TAG, "Recording Stopped");
            }
            mMediaProjection = null;
            stopScreenSharing();
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void stopScreenSharing() {
        if (mVirtualDisplay == null) {
            return;
        }
        mVirtualDisplay.release();
        //mMediaRecorder.release(); //If used: mMediaRecorder object cannot
        // be reused again
        destroyMediaProjection();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        destroyMediaProjection();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void destroyMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection.unregisterCallback(mMediaProjectionCallback);
            mMediaProjection.stop();
            mMediaProjection = null;
        }
        Log.i(TAG, "MediaProjection Stopped");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS: {
                if ((grantResults.length > 0) && (grantResults[0] +
                        grantResults[1]) == PackageManager.PERMISSION_GRANTED) {
//                    onToggleScreenShare(mToggleButton);
                } else {
                    mToggleButton.setChecked(false);
                    Snackbar.make(findViewById(android.R.id.content), "Permission",
                            Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent();
                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                                    intent.setData(Uri.parse("package:" + getPackageName()));
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                    startActivity(intent);
                                }
                            }).show();
                }
                return;
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        startActivity(new Intent(Offline.this,MainActivity.class));
    }

    //    if(getResources().getDisplayMetrics().widthPixels>getResources().getDisplayMetrics().
//    heightPixels)
//    {
//        Toast.makeText(this,"Screen switched to Landscape mode",Toast.LENGTH_SHORT).show();
//    }
//        else
//    {
//        Toast.makeText(this,"Screen switched to Portrait mode",Toast.LENGTH_SHORT).show();
//    }


}
