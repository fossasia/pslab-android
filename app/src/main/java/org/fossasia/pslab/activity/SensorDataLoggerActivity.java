package org.fossasia.pslab.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import org.fossasia.pslab.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by viveksb007 on 23/7/17.
 */

public class SensorDataLoggerActivity extends AppCompatActivity {

    private static final int WRITE_EXTERNAL_STORAGE_REQUEST = 1;
    private static boolean hasPermission = false;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.fab)
    FloatingActionButton scanFab;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_sensor_logger);
        ButterKnife.bind(this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_REQUEST);
        } else {
            hasPermission = true;
        }
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Sensor Data Logger");
        }
        scanFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // scan for sensors and display a list of sensors connected
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                hasPermission = true;
            } else {
                hasPermission = false;
                Toast.makeText(this, "Can't log data", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
