package io.pslab.models;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.pslab.R;
import io.pslab.activity.DataLoggerActivity;
import io.pslab.activity.MapsActivity;
import io.pslab.activity.SettingsActivity;
import io.pslab.fragment.BaroMeterDataFragment;
import io.pslab.fragment.LuxMeterDataFragment;
import io.pslab.others.CSVLogger;
import io.pslab.others.CustomSnackBar;
import io.pslab.others.GPSLogger;
import io.pslab.others.LocalDataLog;
import io.pslab.others.MathUtils;
import io.pslab.others.PSLabPermission;
import io.pslab.others.SwipeGestureDetector;
import io.realm.Realm;
import io.realm.RealmObject;

/**
 * Created by Padmal on 10/20/18.
 */

public abstract class PSLabSensor extends AppCompatActivity {

    public boolean isRecording = false;
    public boolean locationEnabled = true;
    public boolean addLocation = true;
    public boolean checkGPSOnResume = false;
    public boolean writeHeaderToFile = true;
    public boolean playingData = false;
    public boolean viewingData = false;
    public boolean startedPlay = false;

    public CoordinatorLayout sensorParentView;
    public BottomSheetBehavior bottomSheetBehavior;
    public GestureDetector gestureDetector;

    public JSONArray markers;

    public Fragment sensorFragment;
    public PSLabPermission psLabPermission;
    public GPSLogger gpsLogger;
    public CSVLogger csvLogger;
    public Realm realm;
    private Intent map;

    public SimpleDateFormat titleFormat;
    public final String KEY_LOG = "has_log";
    public final String DATA_BLOCK = "data_block";

    public static final String LUXMETER = "Lux Meter";
    public static final String LUXMETER_CONFIGURATIONS = "Lux Meter Configurations";
    public static final String LUXMETER_DATA_FORMAT = "%.2f";
    public static final String BAROMETER = "Barometer";
    public static final String BAROMETER_CONFIGURATIONS = "Barometer Configurations";
    public static final String BAROMETER_DATA_FORMAT = "%.5f";

    @BindView(R.id.sensor_toolbar)
    Toolbar sensorToolBar;
    @BindView(R.id.sensor_cl)
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
    @BindView(R.id.custom_dialog_additional_content)
    LinearLayout bottomSheetAdditionalContent;

    /**
     * Getting menu layout distinct to each sensor
     *
     * @return Menu resource file in 'R.menu.id' format
     */
    public abstract int getMenu();

    /**
     * Getting saved setting configurations for dialogs
     *
     * @return SharedPreferences in Private mode
     */
    public abstract SharedPreferences getStateSettings();

    /**
     * Getting ID to fetch first time usage of each sensor
     *
     * @return String ID of the first time usage ID of sensor
     */
    public abstract String getFirstTimeSettingID();

    /**
     * Sensor ID
     *
     * @return String ID of the sensor
     */
    public abstract String getSensorName();

    /**
     * Title of the sensor guide
     *
     * @return Sensor name as a String resource
     */
    public abstract int getGuideTitle();

    /**
     * Abstract of the sensor guide
     *
     * @return Sensor abstract as a String resource
     */
    public abstract int getGuideAbstract();

    /**
     * Circuit diagrams and pin settings for the sensor
     *
     * @return Schematics as a drawable resource
     */
    public abstract int getGuideSchematics();

    /**
     * Description of the sensor guide
     *
     * @return Sensor guide description as a String resource
     */
    public abstract int getGuideDescription();

    /**
     * Extra content for a specific sensor if it is not a generic one
     *
     * @return Layout id of the content file
     */
    public abstract int getGuideExtraContent();

    /**
     * This method will create a new entry in Realm database with a new block
     *
     * @param block Start timestamp of the recording
     */
    public abstract void recordSensorDataBlockID(SensorDataBlock block);

    /**
     * This method will be called upon when menu button for recording data has been clicked
     */
    public abstract void recordSensorData(RealmObject sensorData);

    /**
     * This method will be called upon when menu button for stop recording data has been clicked
     */
    public abstract void stopRecordSensorData();

    /**
     * Fragment implementation of each individual sensor
     *
     * @return Custom fragment instance of the sensor
     */
    public abstract Fragment getSensorFragment();

    /**
     * This method will fetch logged data information from the data logger activity
     */
    public abstract void getDataFromDataLogger();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generic_sensor);
        ButterKnife.bind(this);
        setSupportActionBar(sensorToolBar);
        getSupportActionBar().setTitle(getSensorName());
        markers = new JSONArray();
        psLabPermission = PSLabPermission.getInstance();
        gpsLogger = new GPSLogger(this,
                (LocationManager) getSystemService(Context.LOCATION_SERVICE));
        map = new Intent(this, MapsActivity.class);
        csvLogger = new CSVLogger(getSensorName());
        realm = LocalDataLog.with().getRealm();
        titleFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        sensorParentView = coordinatorLayout;
        setUpBottomSheet();
        fillUpFragment();
        invalidateOptionsMenu();
    }

    /**
     * Fill up the frame with the individual sensor fragment layout
     */
    private void fillUpFragment() {
        try {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            sensorFragment = getSensorFragment();
            transaction.replace(R.id.sensor_frame, sensorFragment, getSensorName());
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUpMenu(Menu menu) {
        if (playingData || viewingData) {
            for (int i = 0; i < menu.size(); i++) {
                menu.getItem(i).setVisible(false);
            }
        }
        menu.findItem(R.id.save_graph).setVisible(viewingData || playingData);
        menu.findItem(R.id.play_data).setVisible(viewingData || playingData);
        menu.findItem(R.id.settings).setTitle(getSensorName() + " Configurations");
        menu.findItem(R.id.stop_data).setVisible(viewingData).setEnabled(startedPlay);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(getMenu(), menu);
        setUpMenu(menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem record = menu.findItem(R.id.record_data);
        record.setIcon(isRecording ? R.drawable.ic_record_stop_white : R.drawable.ic_record_white);
        MenuItem play = menu.findItem(R.id.play_data);
        play.setIcon(playingData ? R.drawable.ic_pause_white_24dp : R.drawable.ic_play_arrow_white_24dp);
        MenuItem stop = menu.findItem(R.id.stop_data);
        stop.setVisible(startedPlay);
        return super.onPrepareOptionsMenu(menu);
    }

    private void prepareMarkers() {
        if (markers.length() > 0) {
            map.putExtra("hasMarkers", true);
            map.putExtra("markers", markers.toString());
        } else {
            map.putExtra("hasMarkers", false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            /*
              When record data button has been pressed, check if the device has write permission
              to log and access to location. checkPermission method will prompt user with a dialog
              box to allow app to use those features. Upon allowing, onRequestPermissionsResult
              will fire up. If user declines to give permission, don't do anything.
             */
            case R.id.record_data:
                if (!isRecording) {
                    dataRecordingCycle();
                } else {
                    stopRecordSensorData();
                    displayLogLocationOnSnackBar();
                    isRecording = false;
                    prepareMarkers();
                }
                invalidateOptionsMenu();
                break;
            case R.id.play_data:
                playingData = !playingData;
                if (!startedPlay) {
                    if (getSensorFragment() instanceof LuxMeterDataFragment) {
                        ((LuxMeterDataFragment) getSupportFragmentManager()
                                .findFragmentByTag(getSensorName())).playData();
                    } else if (getSensorFragment() instanceof BaroMeterDataFragment) {
                        ((BaroMeterDataFragment) getSupportFragmentManager()
                                .findFragmentByTag(getSensorName())).playData();
                    }
                }
                invalidateOptionsMenu();
                break;
            case R.id.stop_data:
                if (getSensorFragment() instanceof LuxMeterDataFragment) {
                    ((LuxMeterDataFragment) getSupportFragmentManager()
                            .findFragmentByTag(getSensorName())).stopData();
                } else if (getSensorFragment() instanceof BaroMeterDataFragment) {
                    ((BaroMeterDataFragment) getSupportFragmentManager()
                            .findFragmentByTag(getSensorName())).stopData();
                }
                break;
            case R.id.show_map:
                if (psLabPermission.checkPermissions(PSLabSensor.this,
                        PSLabPermission.MAP_PERMISSION)) {
                    startActivity(map);
                }
                break;
            case R.id.settings:
                Intent settingIntent = new Intent(this, SettingsActivity.class);
                settingIntent.putExtra("title", getSensorName() + " Configurations");
                startActivity(settingIntent);
                break;
            case R.id.show_logged_data:
                if (psLabPermission.checkPermissions(PSLabSensor.this,
                        PSLabPermission.CSV_PERMISSION)) {
                    Intent intent = new Intent(this, DataLoggerActivity.class);
                    intent.putExtra(DataLoggerActivity.CALLER_ACTIVITY, getSensorName());
                    startActivity(intent);
                }
                break;
            case R.id.show_guide:
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                break;
            case R.id.save_graph:
                displayLogLocationOnSnackBar();
                if (getSensorFragment() instanceof LuxMeterDataFragment) {
                    ((LuxMeterDataFragment) getSupportFragmentManager()
                            .findFragmentByTag(getSensorName())).saveGraph();
                } else if (getSensorFragment() instanceof BaroMeterDataFragment) {
                    ((BaroMeterDataFragment) getSupportFragmentManager()
                            .findFragmentByTag(getSensorName())).saveGraph();
                }
                break;
            default:
                break;
        }
        return true;
    }

    private void dataRecordingCycle() {
        if (psLabPermission.checkPermissions(PSLabSensor.this, PSLabPermission.LOG_PERMISSION)) {
            if (locationEnabled) {
                if (psLabPermission.checkPermissions(PSLabSensor.this, PSLabPermission.GPS_PERMISSION)) {
                    gpsRecordingCycle();
                }
            } else {
                CustomSnackBar.showSnackBar(sensorParentView,
                        getString(R.string.data_recording_without_location), null, null, Snackbar.LENGTH_LONG);
                isRecording = true;
            }
        }
    }

    private void gpsRecordingCycle() {
        addLocation = true;
        gpsLogger.startCaptureLocation();
        if (gpsLogger.isGPSEnabled()) {
            CustomSnackBar.showSnackBar(sensorParentView,
                    getString(R.string.data_recording_with_location), null, null, Snackbar.LENGTH_LONG);
            isRecording = true;
        } else {
            gpsLogger.gpsAlert.show();
        }
    }

    public void displayLogLocationOnSnackBar() {
        final File logDirectory = new File(
                Environment.getExternalStorageDirectory().getAbsolutePath() +
                        File.separator + CSVLogger.CSV_DIRECTORY + File.separator + getSensorName());
        String logLocation;
        try {
            logLocation = getString(R.string.log_saved_directory) + logDirectory.getCanonicalPath();
        } catch (IOException e) {
            // This message wouldn't appear in usual cases. Added in order to handle ex:
            logLocation = getString(R.string.log_saved_failed);
        }
        CustomSnackBar.showSnackBar(sensorParentView, logLocation, getString(R.string.open),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Uri selectedUri = Uri.parse(logDirectory.getAbsolutePath());
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(selectedUri, "resource/folder");
                        if (intent.resolveActivityInfo(getPackageManager(), 0) != null) {
                            startActivity(intent);
                        }
                    }
                }, Snackbar.LENGTH_INDEFINITE);
    }

    private void nogpsRecordingCycle() {
        CustomSnackBar.showSnackBar(sensorParentView,
                getString(R.string.data_recording_without_location), null, null, Snackbar.LENGTH_LONG);
        addLocation = false;
        isRecording = true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PSLabPermission.MAP_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent map = new Intent(getApplicationContext(), MapsActivity.class);
                    startActivity(map);
                } else {
                    Toast.makeText(getApplicationContext(),
                            getResources().getString(R.string.no_permission_for_maps),
                            Toast.LENGTH_LONG).show();
                }
                break;
            case PSLabPermission.LOG_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    dataRecordingCycle();
                    invalidateOptionsMenu();
                }
                break;
            case PSLabPermission.GPS_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    gpsRecordingCycle();
                    invalidateOptionsMenu();
                } else {
                    nogpsRecordingCycle();
                    invalidateOptionsMenu();
                }
                break;
            case PSLabPermission.CSV_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(this, DataLoggerActivity.class);
                    intent.putExtra(DataLoggerActivity.CALLER_ACTIVITY, getSensorName());
                    startActivity(intent);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    /**
     * Configure the sensor guide with content and settings
     */
    private void setUpBottomSheet() {
        setupGuideLayout();
        handleFirstTimeUsage();
        handleBottomSheetBehavior();
        handleShadowClicks();
        gestureDetector = new GestureDetector(this,
                new SwipeGestureDetector(bottomSheetBehavior));
    }

    /**
     * Closes the guide when user clicks on dark background area
     */
    private void handleShadowClicks() {
        tvShadow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                tvShadow.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Handle sliding up and down behaviors and proper handling in closure.
     */
    private void handleBottomSheetBehavior() {
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            private Handler handler = new Handler();
            private Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                    tvShadow.setVisibility(View.GONE);
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
                        tvShadow.setVisibility(View.GONE);
                        handler.removeCallbacks(runnable);
                        bottomSheetSlideText.setText(R.string.show_guide_text);
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                Float value = (float) MathUtils.map((double) slideOffset,
                        0.0, 1.0, 0.0, 0.8);
                tvShadow.setVisibility(View.VISIBLE);
                tvShadow.setAlpha(value);
                arrowUpDown.setRotation(slideOffset * 180);
            }
        });
    }

    /**
     * Inflate each individual view with content to fill up the sensor guide
     */
    private void setupGuideLayout() {
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        bottomSheetGuideTitle.setText(getGuideTitle());
        bottomSheetText.setText(getGuideAbstract());
        bottomSheetSchematic.setImageResource(getGuideSchematics());
        bottomSheetDesc.setText(getGuideDescription());
        // If a sensor has extra content than provided in the standard layout, create a new layout
        // and attach the layout id with getGuideExtraContent()
        if (getGuideExtraContent() != 0) {
            LayoutInflater I = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
            assert I != null;
            View childLayout = I.inflate(getGuideExtraContent(), null);
            bottomSheetAdditionalContent.addView(childLayout);
        }
    }

    /**
     * Handle first time usage of sensor to show the guide at startup
     */
    private void handleFirstTimeUsage() {
        if (getStateSettings().getBoolean(getFirstTimeSettingID(), true)) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            tvShadow.setVisibility(View.VISIBLE);
            tvShadow.setAlpha(0.8f);
            arrowUpDown.setRotation(180);
            bottomSheetSlideText.setText(R.string.hide_guide_text);
            SharedPreferences.Editor editor = getStateSettings().edit();
            editor.putBoolean(getFirstTimeSettingID(), false);
            editor.apply();
        } else {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            tvShadow.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            getDataFromDataLogger();
        } catch (ArrayIndexOutOfBoundsException e) {
            Toast.makeText(getApplicationContext(), getString(R.string.no_data_fetched), Toast.LENGTH_LONG).show();
        }
        if (checkGPSOnResume) {
            isRecording = true;
            checkGPSOnResume = false;
            invalidateOptionsMenu();
        }
    }
}
