package org.fossasia.pslab.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.fossasia.pslab.R;
import org.fossasia.pslab.communication.ScienceLab;
import org.fossasia.pslab.communication.peripherals.I2C;
import org.fossasia.pslab.communication.sensors.MPU6050;
import org.fossasia.pslab.models.DataMPU6050;
import org.fossasia.pslab.models.SensorLogged;
import org.fossasia.pslab.others.ScienceLabCommon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by viveksb007 on 23/7/17.
 */

public class SensorDataLoggerActivity extends AppCompatActivity {

    private static final int WRITE_EXTERNAL_STORAGE_REQUEST = 1;
    private static boolean hasPermission = false;
    private static boolean isLogging = false;
    private LinkedHashMap<Integer, String> sensorAddress = new LinkedHashMap<>();
    private ScienceLab scienceLab = ScienceLabCommon.scienceLab;
    private I2C i2c = scienceLab.i2c;
    private ArrayList<String> sensorList = new ArrayList<>();
    private ArrayList<DataMPU6050> mpu6050DataList = new ArrayList<>();
    private Context context;
    private Thread loggingThread;
    private volatile boolean loggingThreadRunning = false;
    private final Object lock = new Object();
    private View customView;
    private Realm realm;
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
        realm = Realm.getDefaultInstance();
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
                                            sensorList.add(sensorAddress.get(temp));
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
                                        sensorList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                            @Override
                                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                handleClick(position);
                                            }
                                        });
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
                    TextView snackBarTextView = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
                    snackBarTextView.setTextColor(Color.YELLOW);
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

    private void handleClick(int position) {
        String sensor = sensorList.get(position);
        Toast.makeText(context, sensor, Toast.LENGTH_SHORT).show();
        switch (sensor) {
            case "MPU6050":
                MaterialDialog dialog = new MaterialDialog.Builder(context)
                        .customView(R.layout.sensor_mpu6050_data_card, true)
                        .positiveText(getResources().getString(R.string.start_logging))
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull final DialogAction which) {
                                if (!isLogging) {
                                    isLogging = true;
                                    loggingThreadRunning = true;
                                    dialog.getActionButton(DialogAction.POSITIVE).setText(getResources().getString(R.string.stop_logging));
                                    Runnable loggingRunnable = new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                MPU6050 sensorMPU6050 = new MPU6050(i2c);
                                                while (loggingThreadRunning) {
                                                    TaskMPU6050 taskMPU6050 = new TaskMPU6050(sensorMPU6050);
                                                    taskMPU6050.execute();
                                                    synchronized (lock) {
                                                        try {
                                                            lock.wait();
                                                        } catch (InterruptedException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                    Thread.sleep(500);
                                                }
                                            } catch (IOException | InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    };
                                    loggingThread = new Thread(loggingRunnable);
                                    loggingThread.start();
                                } else {
                                    isLogging = false;
                                    dialog.getActionButton(DialogAction.POSITIVE).setText(getResources().getString(R.string.start_logging));
                                    loggingThreadRunning = false;
                                }
                            }
                        })
                        .negativeText(getResources().getString(R.string.cancel))
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                if (isLogging) {
                                    // stop and discard logging gracefully
                                }
                                dialog.dismiss();
                            }
                        })
                        .neutralText(getResources().getString(R.string.save_data))
                        .onNeutral(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                realm.beginTransaction();
                                long trial;
                                Number trialNumber = realm.where(DataMPU6050.class).max("trial");
                                if (trialNumber == null) {
                                    trial = 0;
                                } else {
                                    trial = (long) trialNumber + 1;
                                }
                                for (int i = 0; i < mpu6050DataList.size(); i++) {
                                    DataMPU6050 tempObject = mpu6050DataList.get(i);
                                    tempObject.setTrial(trial);
                                    tempObject.setId(i);
                                    realm.copyToRealm(tempObject);
                                }
                                RealmResults<SensorLogged> results = realm.where(SensorLogged.class).equalTo("sensor", "MPU6050").findAll();
                                if (results.size() == 0) {
                                    SensorLogged sensorLogged = new SensorLogged("MPU6050");
                                    realm.copyToRealm(sensorLogged);
                                }
                                realm.commitTransaction();
                                Toast.makeText(SensorDataLoggerActivity.this, "Data Logged Successfully", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                /*
                                Log.v("Realm Transaction", "Successful");
                                RealmResults<DataMPU6050> results = realm.where(DataMPU6050.class).findAll();
                                for (int i = 0; i < results.size(); i++) {
                                    Log.v("Realm Saved Data ", results.get(i).toString());
                                }
                                */
                            }
                        })
                        .autoDismiss(false)
                        .build();
                dialog.show();
                customView = dialog.getCustomView();
                break;
        }
    }

    private class TaskMPU6050 extends AsyncTask<Void, Void, Void> {

        private MPU6050 sensorMPU6050;
        private ArrayList<Double> dataMPU6050 = new ArrayList<>();

        TaskMPU6050(MPU6050 mpu6050) {
            this.sensorMPU6050 = mpu6050;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                dataMPU6050 = sensorMPU6050.getRaw();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            TextView tvAx = (TextView) customView.findViewById(R.id.tv_sensor_mpu6050_ax);
            TextView tvAy = (TextView) customView.findViewById(R.id.tv_sensor_mpu6050_ay);
            TextView tvAz = (TextView) customView.findViewById(R.id.tv_sensor_mpu6050_az);
            TextView tvGx = (TextView) customView.findViewById(R.id.tv_sensor_mpu6050_gx);
            TextView tvGy = (TextView) customView.findViewById(R.id.tv_sensor_mpu6050_gy);
            TextView tvGz = (TextView) customView.findViewById(R.id.tv_sensor_mpu6050_gz);
            TextView tvTemp = (TextView) customView.findViewById(R.id.tv_sensor_mpu6050_temp);
            tvAx.setText(String.valueOf(dataMPU6050.get(0)));
            tvAy.setText(String.valueOf(dataMPU6050.get(1)));
            tvAz.setText(String.valueOf(dataMPU6050.get(2)));
            tvGx.setText(String.valueOf(dataMPU6050.get(4)));
            tvGy.setText(String.valueOf(dataMPU6050.get(5)));
            tvGz.setText(String.valueOf(dataMPU6050.get(6)));
            tvTemp.setText(String.valueOf(dataMPU6050.get(3)));
            DataMPU6050 tempObject = new DataMPU6050(dataMPU6050.get(0), dataMPU6050.get(1), dataMPU6050.get(2),
                    dataMPU6050.get(4), dataMPU6050.get(5), dataMPU6050.get(6), dataMPU6050.get(3));
            mpu6050DataList.add(tempObject);
            Log.v("MPU6050", mpu6050DataList.size() + "");
            synchronized (lock) {
                lock.notify();
            }
        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_sensor_data_logger, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_logged_data:
                Intent activityLoggedData = new Intent(this, ShowLoggedData.class);
                startActivity(activityLoggedData);
                break;
            default:
                //
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
       finish();
    }
}
