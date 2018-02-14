package thesis.icpep.helmata;

import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;

public class Online extends AppCompatActivity {

    VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_online);

        videoView = this.findViewById(R.id.videoView);

        //add controls to a MediaPlayer like play, pause.
        MediaController mc = new MediaController(this);
        videoView.setMediaController(mc);

        //Set the path of Video or URI
        videoView.setVideoURI(Uri.parse("rtsp://192.168.100.8/unicast"));


        //Set the focus
        videoView.requestFocus();
        videoView.start();

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        final FloatingActionButton fabStop = (FloatingActionButton) findViewById(R.id.fabStop);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                if(fab.getVisibility() == View.VISIBLE){
                    fab.setVisibility(View.GONE);
                    fabStop.setVisibility(View.VISIBLE);
                }

            }
        });


        fabStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fabStop.getVisibility() == View.VISIBLE){
                    fabStop.setVisibility(View.GONE);
                    fab.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}
