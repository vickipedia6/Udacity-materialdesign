package com.example.xyzreader.ui;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;

import com.example.xyzreader.R;

import pl.droidsonroids.gif.GifImageView;

public class SplashScreen extends AppCompatActivity {
ProgressBar mProgressbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        mProgressbar=(ProgressBar)findViewById(R.id.progressBar);



        Thread thread=new Thread(){
            @Override
            public void run() {
                try {
                    mProgressbar.setProgress(66);
                    sleep(5000);
                    Intent articleDetailActivity=new Intent(getApplicationContext(),ArticleListActivity.class);
                    startActivity(articleDetailActivity);
                }
                catch (Exception e)
                {

                }
            }
        };
        thread.start();
     }
}

