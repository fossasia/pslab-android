package io.pslab.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import butterknife.ButterKnife;
import io.pslab.R;
import io.pslab.others.PSLabPermission;

/**
 * Created by viveksb007 on 11/3/17.
 */

public class SplashActivity extends AppCompatActivity {

    private Handler handler;
    private Runnable runnable;
    private ImageView logo;
    private ImageView text;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        ButterKnife.bind(this);
        logo = findViewById(R.id.imageView);
        text = findViewById(R.id.PSLabText);
        logo.animate().alpha(1f).setDuration(2500);
        text.animate().alpha(1f).setDuration(2500);
        PSLabPermission psLabPermission = PSLabPermission.getInstance();
        if (psLabPermission.checkPermissions(SplashActivity.this,
                PSLabPermission.ALL_PERMISSION)) {
            exitSplashScreen();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PSLabPermission.PERMISSIONS_REQUIRED) {
            exitSplashScreen();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        handler.removeCallbacks(runnable);
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
