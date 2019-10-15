package io.pslab.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import butterknife.ButterKnife;
import io.pslab.R;
import io.pslab.others.PSLabPermission;

/**
 * Created by viveksb007 on 11/3/17.
 */

public class SplashActivity extends AppCompatActivity {

    private Handler handler;
    private Runnable runnable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        ButterKnife.bind(this);
        ImageView logo = findViewById(R.id.imageView);
        ImageView text = findViewById(R.id.PSLabText);
        logo.animate().alpha(1f).setDuration(2500);
        text.animate().alpha(1f).setDuration(2500);
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
        handler.postDelayed(runnable = () -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }, SPLASH_TIME_OUT);
    }
}
