package thesis.icpep.helmata;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;


import java.io.File;

public class Player extends AppCompatActivity {

    VideoView videoView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_player);

        //get video file name
        Intent intent = getIntent();
        String message = intent.getStringExtra("video");

        //get directory path
        File f = new File(Environment.getExternalStorageDirectory(), "Helmata");

        //exo player
//        playerView = (SimpleExoPlayerView)findViewById(R.id.playerView);
//        try {
//            BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
//            TrackSelector trackSelector = new DefaultTrackSelector(new AdaptiveTrackSelection.Factory(bandwidthMeter));
//            player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
//
//            Uri uri = Uri.parse(f + "/" + message);
//
//            DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory("exoplayer_video");
//            ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
//            MediaSource mediaSource = new ExtractorMediaSource(uri, dataSourceFactory, extractorsFactory, null, null);
//
//            playerView.setPlayer(player);
//            player.prepare(mediaSource);
//            player.setPlayWhenReady(true);
//        } catch (Exception e) {
//
//        }


//        Toast.makeText(getApplicationContext(),
//                        message, Toast.LENGTH_SHORT).show();
        videoView = this.findViewById(R.id.videoView);
        MediaController mc = new MediaController(this);
        videoView.setMediaController(mc);
//

        Uri uri = Uri.parse(message);
        videoView.setVideoURI(uri);
//
//        //Set the focus
        videoView.requestFocus();
//
//        //play stream
        videoView.start();
    }
}
