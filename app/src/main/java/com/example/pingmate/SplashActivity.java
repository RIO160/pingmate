package com.example.pingmate;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.media.MediaPlayer;


import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 3000;
    private MediaPlayer splashSound;// Duration in milliseconds (3 seconds)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView splashLogo = findViewById(R.id.splashLogo);
        ImageView text = findViewById(R.id.pingMateText);


        // Load animations
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation scale = AnimationUtils.loadAnimation(this, R.anim.scale);

        // Apply animations
        splashLogo.startAnimation(scale);
        splashLogo.startAnimation(fadeIn);
        text.startAnimation(scale);

        splashSound = MediaPlayer.create(this, R.raw.splash_sound);
        splashSound.start();



        // Navigate to MainActivity after SPLASH_DURATION
        new Handler().postDelayed(() -> {
            if (splashSound.isPlaying()) {
                splashSound.stop();
                splashSound.release();
            }
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);  // Change to your main activity
            startActivity(intent);
            finish();  // Close SplashActivity so it's not in the back stack
        }, SPLASH_DURATION);
    }
}
