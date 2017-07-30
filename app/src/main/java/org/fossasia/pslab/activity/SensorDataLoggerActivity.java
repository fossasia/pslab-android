package org.fossasia.pslab.activity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.fossasia.pslab.R;
import org.fossasia.pslab.communication.ScienceLab;
import org.fossasia.pslab.communication.peripherals.I2C;
import org.fossasia.pslab.others.ScienceLabCommon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by viveksb007 on 23/7/17.
 */

public class SensorDataLoggerActivity extends AppCompatActivity {

    private static final int WRITE_EXTERNAL_STORAGE_REQUEST = 1;
    private static boolean hasPermission = false;
    private LinkedHashMap<Integer, String> sensorAddress = new LinkedHashMap<>();
    private ScienceLab scienceLab = ScienceLabCommon.scienceLab;
    private I2C i2c = scienceLab.i2c;
    private Context context;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.fab)
    FloatingActionButton scanFab;
    @BindView(R.id.coordinator_layout)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.layout_container)
    FrameLayout container;


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
        context = this;
        scanFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (scienceLab.isConnected()) {
                    Runnable detectSensors = new Runnable() {
                        @Override
                        public void run() {
                            try {
                                ArrayList<Integer> scanResult = i2c.scan(null);
                                final ArrayList<String> listData = new ArrayList<String>();
                                if (scanResult != null) {
                                    for (Integer temp : scanResult) {
                                        if (sensorAddress.get(temp) != null) {
                                            listData.add(sensorAddress.get(temp) + " : " + temp);
                                        }
                                    }
                                }
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        ListView sensorList = new ListView(context);
                                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, listData);
                                        sensorList.setAdapter(adapter);
                                        container.addView(sensorList);
                                    }
                                });
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    new Thread(detectSensors).start();
                } else {
                    Snackbar snackbar = Snackbar.make(coordinatorLayout, "Device not connected", Snackbar.LENGTH_SHORT);
                    View snackBarView = snackbar.getView();
                    TextView snackbarTextView = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
                    snackbarTextView.setTextColor(Color.YELLOW);
                    snackbar.show();
                }
            }
        });
        sensorAddress.put(0x60, "MCP4728");
        sensorAddress.put(0x48, "ADS1115");
        sensorAddress.put(0x23, "BH1750");
        sensorAddress.put(0x77, "BMP180");
        sensorAddress.put(0x5A, "MLX90614");
        sensorAddress.put(0x1E, "HMC5883L");
        sensorAddress.put(0x68, "MPU6050");
        sensorAddress.put(0x40, "SHT21");
        sensorAddress.put(0x39, "TSL2561");
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
