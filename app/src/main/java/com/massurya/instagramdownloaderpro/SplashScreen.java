package com.massurya.instagramdownloaderpro;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Timer;

public class SplashScreen extends AppCompatActivity {
    TextView tv;
    ImageView iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_screen);
        tv = findViewById(R.id.tv);
        iv = findViewById(R.id.iv);
        Animation animation = AnimationUtils.loadAnimation(this,R.anim.transitions);
        tv.startAnimation(animation);
        iv.startAnimation(animation);
        final Intent intent = new Intent(this,MainActivity.class);
        Thread timer = new Thread() {
            public void run() {
                try{
                    sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    startActivity(intent);
                    finish();
                }
            }
        };

            timer.start();
    }
}
