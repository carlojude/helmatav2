package thesis.icpep.helmata;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;

import java.io.File;

//import io.vov.vitamio.widget.MediaController;
//import io.vov.vitamio.widget.VideoView;

public class Player extends AppCompatActivity {

    VideoView videoView;
    private SimpleExoPlayer player;
    private SimpleExoPlayerView playerView;



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

        Uri uri = Uri.parse(f + "/" + message);
        videoView.setVideoURI(uri);
//
//        //Set the focus
        videoView.requestFocus();
//
//        //play stream
        videoView.start();
    }
}
