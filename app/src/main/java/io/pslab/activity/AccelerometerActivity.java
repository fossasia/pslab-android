package io.pslab.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.pslab.R;
import io.pslab.adapters.AccelerometerAdapter;
import io.pslab.others.CSVLogger;
import io.pslab.others.CustomSnackBar;
import io.pslab.others.GPSLogger;
import io.pslab.others.MathUtils;
import io.pslab.others.SwipeGestureDetector;

public class AccelerometerActivity extends AppCompatActivity {

    private static final String PREF_NAME = "AccelerometerPreferences";

    private static final int MY_PERMISSIONS_REQUEST_STORAGE_FOR_DATA = 101;
    private static final int MY_PERMISSIONS_REQUEST_STORAGE_FOR_MAPS = 102;

    public boolean recordData = false;
    public boolean locationPref;
    public GPSLogger gpsLogger;
    public CSVLogger accLogger;
    AccelerometerAdapter adapter;
    BottomSheetBehavior bottomSheetBehavior;
    GestureDetector gestureDetector;
    @BindView(R.id.accel_toolbar)
    Toolbar mToolbar;
    @BindView(R.id.accel_coordinator_layout)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.bottom_sheet)
    LinearLayout bottomSheet;
    @BindView(R.id.shadow)
    View tvShadow;
    @BindView(R.id.img_arrow)
    ImageView arrowUpDown;
    @BindView(R.id.sheet_slide_text)
    TextView bottomSheetSlideText;
    @BindView(R.id.guide_title)
    TextView bottomSheetGuideTitle;
    @BindView(R.id.custom_dialog_text)
    TextView bottomSheetText;
    @BindView(R.id.custom_dialog_schematic)
    ImageView bottomSheetSchematic;
    @BindView(R.id.custom_dialog_desc)
    TextView bottomSheetDesc;
    private boolean checkGpsOnResume = false;
    private boolean isRecordingStarted = false;
    private boolean isDataRecorded = false;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometer_main);
        ButterKnife.bind(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setUpBottomSheet();
        tvShadow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                tvShadow.setVisibility(View.GONE);
            }
        });
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

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
                    item.setIcon(R.drawable.ic_record_white);
                    adapter.setRecordingStatus(false);
                    recordData = false;
                    CustomSnackBar.showSnackBar(coordinatorLayout, getString(R.string.data_recording_paused), null, null, Snackbar.LENGTH_LONG);
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
                            CustomSnackBar.showSnackBar(coordinatorLayout, getString(R.string.data_recording_start) + "\n" + getString(R.string.location_enabled), null, null, Snackbar.LENGTH_LONG);
                        } else {
                            checkGpsOnResume = true;
                        }
                        gpsLogger.startCaptureLocation();
                    } else {
                        recordData = true;
                        CustomSnackBar.showSnackBar(coordinatorLayout, getString(R.string.data_recording_start) + "\n" + getString(R.string.location_disabled), null, null, Snackbar.LENGTH_LONG);
                    }
                }
                break;
            case R.id.record_csv_data:
                if (isDataRecorded) {
                    MenuItem item1 = menu.findItem(R.id.record_pause_data);
                    item1.setIcon(R.drawable.ic_record_white);

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
                        Location location = gpsLogger.getDeviceLocation();
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
                            }, Snackbar.LENGTH_LONG);
                    adapter.setRecordingStatus(false);
                    isRecordingStarted = false;
                    recordData = false;
                } else {
                    CustomSnackBar.showSnackBar(coordinatorLayout, getString(R.string.nothing_to_export), null, null, Snackbar.LENGTH_LONG);
                }
                break;
            case R.id.delete_csv_data:
                if (isDataRecorded) {
                    MenuItem item1 = menu.findItem(R.id.record_pause_data);
                    item1.setIcon(R.drawable.ic_record_white);
                    adapter.setRecordingStatus(false);
                    recordData = false;
                    isRecordingStarted = false;
                    isDataRecorded = false;
                    accLogger.deleteFile();
                    CustomSnackBar.showSnackBar(coordinatorLayout, getString(R.string.data_deleted), null, null, Snackbar.LENGTH_LONG);
                } else
                    CustomSnackBar.showSnackBar(coordinatorLayout, getString(R.string.nothing_to_delete), null, null, Snackbar.LENGTH_LONG);
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
                Intent settingIntent = new Intent(this, SettingsActivity.class);
                settingIntent.putExtra("title", getResources().getString(R.string.accelerometer_configurations));
                startActivity(settingIntent);
                break;
            case android.R.id.home:
                this.finish();
                break;
            case R.id.show_guide:
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            default:
                break;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isRecordingStarted) {
            accLogger.deleteFile();
            isRecordingStarted = false;
        }
    }

    private void setUpBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        final SharedPreferences settings = this.getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        Boolean isFirstTime = settings.getBoolean("AccelerometerFirstTime", true);

        bottomSheetGuideTitle.setText(R.string.accelerometer);
        bottomSheetText.setText(R.string.accelerometer_intro);
        bottomSheetSchematic.setImageResource(R.drawable.find_mobile_axis);
        bottomSheetDesc.setText(R.string.accelerometer_description_text);

        if (isFirstTime) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            tvShadow.setVisibility(View.VISIBLE);
            tvShadow.setAlpha(0.8f);
            arrowUpDown.setRotation(180);
            bottomSheetSlideText.setText(R.string.hide_guide_text);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("AccelerometerFirstTime", false);
            editor.apply();
        } else {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            private Handler handler = new Handler();
            private Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }
            };

            @Override
            public void onStateChanged(@NonNull final View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_EXPANDED:
                        handler.removeCallbacks(runnable);
                        bottomSheetSlideText.setText(R.string.hide_guide_text);
                        break;

                    case BottomSheetBehavior.STATE_COLLAPSED:
                        handler.postDelayed(runnable, 2000);
                        break;

                    default:
                        handler.removeCallbacks(runnable);
                        bottomSheetSlideText.setText(R.string.show_guide_text);
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                Float value = (float) MathUtils.map((double) slideOffset, 0.0, 1.0, 0.0, 0.8);
                tvShadow.setVisibility(View.VISIBLE);
                tvShadow.setAlpha(value);
                arrowUpDown.setRotation(slideOffset * 180);
            }
        });
        gestureDetector = new GestureDetector(this, new SwipeGestureDetector(bottomSheetBehavior));
    }
}
