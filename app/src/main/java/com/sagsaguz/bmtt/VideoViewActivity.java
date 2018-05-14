package com.sagsaguz.bmtt;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.sagsaguz.bmtt.utils.Config;

import java.net.URL;

import static com.sagsaguz.bmtt.HomePageActivity.homePageActivity;

public class VideoViewActivity extends AppCompatActivity {

    private VideoView vvFullScreen;
    private SharedPreferences preferences;
    private ProgressBar pbVideoView;
    String video = "null", email, phone;
    private static String subKey;

    private static Context context;
    private VideoViewActivity videoViewActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.video_view_layout);

        videoViewActivity = VideoViewActivity.this;
        context = VideoViewActivity.this;

        vvFullScreen = (VideoView) findViewById(R.id.vvFullScreen);
        pbVideoView = findViewById(R.id.pbVideoView);
        pbVideoView.setVisibility(View.VISIBLE);
        /*preferences = getSharedPreferences("VIDEO", MODE_PRIVATE);
        video = preferences.getString("VIDEOFILE", null);*/
        Intent intent = getIntent();
        video = intent.getStringExtra("VIDEOFILE");
        email = intent.getStringExtra("EMAIL");
        phone = intent.getStringExtra("PHONE");
        if(video.equals("null")){
            Toast.makeText(getBaseContext(), "Please Refresh The Page", Toast.LENGTH_SHORT).show();
        } else {
            subKey = video.substring(video.indexOf("_")+1, video.indexOf("."));
            playVideo(video);
        }

        vvFullScreen.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                Intent mcqIntent = new Intent(getBaseContext(), MCQActivity.class);
                mcqIntent.putExtra("FILENAME", subKey);
                mcqIntent.putExtra("EMAIL", email);
                mcqIntent.putExtra("PHONE", phone);
                startActivity(mcqIntent);
                finish();
            }
        });
    }

    public void showSnackBar(String message){
        Snackbar snackbar = Snackbar.make(vvFullScreen, message, Snackbar.LENGTH_SHORT)
                .setAction("Try Again", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        playVideo(video);
                    }
                });
        snackbar.setActionTextColor(ContextCompat.getColor(context, R.color.colorAccent));

        View sbView = snackbar.getView();
        sbView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
        TextView textView = sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
        snackbar.setDuration(BaseTransientBottomBar.LENGTH_INDEFINITE);
        snackbar.show();
    }

    public void playVideo(String objectName){

        AWSCredentials myCredentials = new BasicAWSCredentials(Config.ACCESSKEY, Config.SECRETKEY);
        AmazonS3 s3client = new AmazonS3Client(myCredentials);
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(Config.BUCKETNAME, objectName);
        URL objectURL = s3client.generatePresignedUrl(request);

        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        MediaController mediaCtrl = new MediaController(this);
        mediaCtrl.setMediaPlayer(vvFullScreen);
        mediaCtrl.setAnchorView(vvFullScreen);
        mediaCtrl.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        vvFullScreen.setMediaController(mediaCtrl);
        Uri clip = Uri.parse(objectURL.toString());
        vvFullScreen.setVideoURI(clip);
        vvFullScreen.requestFocus();
        vvFullScreen.start();
        vvFullScreen.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                pbVideoView.setVisibility(View.GONE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        homePageActivity.updateCValues(video);
                    }
                }, 5000);
            }
        });

    }

}
