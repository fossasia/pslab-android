package io.pslab.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import io.pslab.databinding.SplashScreenBinding;
import io.pslab.others.PSLabPermission;

/**
 * Created by viveksb007 on 11/3/17.
 */

public class SplashActivity extends AppCompatActivity {

    private SplashScreenBinding binding;

    private Handler handler;
    private Runnable runnable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = SplashScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.imageView.animate().alpha(1f).setDuration(2500);
        binding.PSLabText.animate().alpha(1f).setDuration(2500);
        PSLabPermission psLabPermission = PSLabPermission.getInstance();
        if (psLabPermission.checkPermissions(SplashActivity.this,
                PSLabPermission.ALL_PERMISSION)) {
            exitSplashScreen();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        handler.removeCallbacks(runnable);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        exitSplashScreen();
    }

    private void exitSplashScreen() {
        handler = new Handler();
        int SPLASH_TIME_OUT = 2000;
        handler.postDelayed(runnable = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}
