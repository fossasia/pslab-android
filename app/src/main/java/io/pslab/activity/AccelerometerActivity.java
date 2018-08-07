package io.pslab.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;

import io.pslab.R;
import io.pslab.adapters.AccelerometerAdapter;
import io.pslab.others.CSVLogger;
import io.pslab.others.CustomSnackBar;
import io.pslab.others.GPSLogger;

public class AccelerometerActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_STORAGE_FOR_DATA = 101;
    private static final int MY_PERMISSIONS_REQUEST_STORAGE_FOR_MAPS = 102;

    public boolean recordData = false;
    public boolean locationPref;
    public GPSLogger gpsLogger;
    public CSVLogger accLogger;
    Toolbar mToolbar;
    AccelerometerAdapter adapter;
    private boolean checkGpsOnResume = false;
    private boolean isRecordingStarted = false;
    private boolean isDataRecorded = false;
    private Menu menu;
    private CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometer);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mToolbar = (Toolbar) findViewById(R.id.accel_toolbar);
        setSupportActionBar(mToolbar);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.accel_coordinator_layout);

        adapter = new AccelerometerAdapter(new String[]{"X axis", "Y axis", "Z axis"}, getApplicationContext());
        RecyclerView recyclerView = this.findViewById(R.id.accelerometer_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.data_log_menu, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.record_pause_data:
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_STORAGE_FOR_DATA);
                    return true;
                }
                if (recordData) {
                    item.setIcon(R.drawable.record_icon);
                    adapter.setRecordingStatus(false);
                    recordData = false;
                    CustomSnackBar.showSnackBar(coordinatorLayout, getString(R.string.data_recording_paused), null, null);
                } else {
                    isDataRecorded = true;
                    item.setIcon(R.drawable.pause_icon);
                    adapter.setRecordingStatus(true);
                    if (!isRecordingStarted) {
                        accLogger = new CSVLogger(getString(R.string.accelerometer));
                        accLogger.writeCSVFile("Timestamp,X,Y,Z\n");
                        isRecordingStarted = true;
                        recordData = true;
                    }
                    if (locationPref) {
                        gpsLogger = new GPSLogger(this, (LocationManager) getSystemService(Context.LOCATION_SERVICE));
                        if (gpsLogger.isGPSEnabled()) {
                            recordData = true;
                            CustomSnackBar.showSnackBar(coordinatorLayout, getString(R.string.data_recording_start) + "\n" + getString(R.string.location_enabled), null, null);
                        } else {
                            checkGpsOnResume = true;
                        }
                        gpsLogger.startFetchingLocation();
                    } else {
                        recordData = true;
                        CustomSnackBar.showSnackBar(coordinatorLayout, getString(R.string.data_recording_start) + "\n" + getString(R.string.location_disabled), null, null);
                    }
                }
                break;
            case R.id.record_csv_data:
                if (isDataRecorded) {
                    MenuItem item1 = menu.findItem(R.id.record_pause_data);
                    item1.setIcon(R.drawable.record_icon);

                    // Export Data
                    ArrayList<Entry> dataX = adapter.getEntries(0);
                    ArrayList<Entry> dataY = adapter.getEntries(1);
                    ArrayList<Entry> dataZ = adapter.getEntries(2);
                    int length = Math.min(Math.min(dataX.size(), dataY.size()), dataZ.size());
                    for (int i = 0; i < length; i++) {
                        accLogger.writeCSVFile(dataX.get(i).getX() + "," + dataX.get(i).getY() + ","
                                + dataY.get(i).getY() + "," + dataZ.get(i).getY() + "\n");
                    }
                    if (locationPref && gpsLogger != null) {
                        String data;
                        Location location = gpsLogger.getBestLocation();
                        if (location != null) {
                            data = "\nLocation" + "," + String.valueOf(location.getLatitude()) + "," + String.valueOf(location.getLongitude() + "\n");
                        } else {
                            data = "\nLocation" + "," + "null" + "," + "null";
                        }
                        accLogger.writeCSVFile(data);
                        gpsLogger.removeUpdate();
                    }
                    CustomSnackBar.showSnackBar(coordinatorLayout,
                            getString(R.string.csv_store_text) + " " + accLogger.getCurrentFilePath()
                            , getString(R.string.delete_capital), new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    new AlertDialog.Builder(AccelerometerActivity.this, R.style.AlertDialogStyle)
                                            .setTitle(R.string.delete_file)
                                            .setMessage(R.string.delete_warning)
                                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    accLogger.deleteFile();
                                                }
                                            })
                                            .setNegativeButton(R.string.cancel, null)
                                            .create()
                                            .show();
                                }
                            });
                    adapter.setRecordingStatus(false);
                    isRecordingStarted = false;
                    recordData = false;
                } else {
                    CustomSnackBar.showSnackBar(coordinatorLayout, getString(R.string.nothing_to_export), null, null);
                }
                break;
            case R.id.delete_csv_data:
                if (isDataRecorded) {
                    MenuItem item1 = menu.findItem(R.id.record_pause_data);
                    item1.setIcon(R.drawable.record_icon);
                    adapter.setRecordingStatus(false);
                    recordData = false;
                    isRecordingStarted = false;
                    isDataRecorded = false;
                    accLogger.deleteFile();
                    CustomSnackBar.showSnackBar(coordinatorLayout, getString(R.string.data_deleted), null, null);
                } else
                    CustomSnackBar.showSnackBar(coordinatorLayout, getString(R.string.nothing_to_delete), null, null);
                break;
            case R.id.show_map:
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_STORAGE_FOR_MAPS);
                    return true;
                }
                Intent MAP = new Intent(getApplicationContext(), MapsActivity.class);
                startActivity(MAP);
                break;
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(isRecordingStarted) {
            accLogger.deleteFile();
            isRecordingStarted = false;
        }
    }
}
