package io.pslab.models;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.snackbar.Snackbar;

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
import io.pslab.activity.guide.GuideActivity;
import io.pslab.communication.ScienceLab;
import io.pslab.fragment.SoundMeterDataFragment;
import io.pslab.interfaces.OperationCallback;
import io.pslab.others.CSVLogger;
import io.pslab.others.CustomSnackBar;
import io.pslab.others.GPSLogger;
import io.pslab.others.LocalDataLog;
import io.pslab.others.PSLabPermission;
import io.pslab.others.ScienceLabCommon;
import io.realm.Realm;
import io.realm.RealmObject;

public abstract class PSLabSensor extends GuideActivity {

    public boolean isRecording = false;
    public boolean locationEnabled = true;
    public boolean addLocation = true;
    public boolean checkGPSOnResume = false;
    public boolean writeHeaderToFile = true;
    public boolean playingData = false;
    public boolean viewingData = false;
    public boolean startedPlay = false;

    public CoordinatorLayout sensorParentView;
    public ScienceLab scienceLab;

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
    public static final String BAROMETER_DATA_FORMAT = "%.2f";
    public static final String GYROSCOPE = "Gyroscope";
    public static final String GYROSCOPE_DATA_FORMAT = "%.2f";
    public static final String GYROSCOPE_CONFIGURATIONS = "Gyroscope Configurations";
    public static final String COMPASS = "Compass";
    public static final String COMPASS_CONFIGURATIONS = "Compass Configurations";
    public static final String ACCELEROMETER = "Accelerometer";
    public static final String ACCELEROMETER_CONFIGURATIONS = "Accelerometer Configurations";
    public static final String THERMOMETER = "Thermometer";
    public static final String THERMOMETER_CONFIGURATIONS = "Thermometer Configurations";
    public static final String THERMOMETER_DATA_FORMAT = "%.2f";
    public static final String DUSTSENSOR_CONFIGURATIONS = "Dust Sensor Configurations";
    public static final String ROBOTIC_ARM = "Robotic Arm";
    public static final String WAVE_GENERATOR = "Wave Generator";
    public static final String OSCILLOSCOPE = "Oscilloscope";
    public static final String POWER_SOURCE = "Power Source";
    public static final String MULTIMETER = "Multimeter";
    public static final String LOGIC_ANALYZER = "Logic Analyzer";
    public static final String GAS_SENSOR = "Gas Sensor";
    public static final String SOUND_METER = "Sound Meter";
    public static final String SOUNDMETER_CONFIGURATIONS = "Sound Meter Configurations";
    public static final String SOUNDMETER_DATA_FORMAT = "%.2f";

    @BindView(R.id.sensor_toolbar)
    Toolbar sensorToolBar;
    @BindView(R.id.sensor_cl)
    CoordinatorLayout coordinatorLayout;

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

    public PSLabSensor() {
        super(R.layout.activity_generic_sensor);
    }

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

    /**
     * This method will check whether the device has in-built sensor or not
     **/
    public abstract boolean sensorFound();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        setSupportActionBar(sensorToolBar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getSensorName());
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        markers = new JSONArray();
        psLabPermission = PSLabPermission.getInstance();
        gpsLogger = new GPSLogger(this,
                (LocationManager) getSystemService(Context.LOCATION_SERVICE));
        map = new Intent(this, MapsActivity.class);
        csvLogger = new CSVLogger(getSensorName());
        realm = LocalDataLog.with().getRealm();
        titleFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT);
        sensorParentView = coordinatorLayout;
        setupGuideLayout();
        fillUpFragment();
        invalidateOptionsMenu();
        scienceLab = ScienceLabCommon.scienceLab;
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

        Fragment fragment;

        switch (item.getItemId()) {
            /*
              When record data button has been pressed, check if the device has write permission
              to log and access to location. checkPermission method will prompt user with a dialog
              box to allow app to use those features. Upon allowing, onRequestPermissionsResult
              will fire up. If user declines to give permission, don't do anything.
             */
            case R.id.record_data:
                if (!isRecording && (sensorFound() || scienceLab.isConnected())) {
                    dataRecordingCycle();
                } else if (!isRecording && !sensorFound() && !scienceLab.isConnected()) {
                    CustomSnackBar.showSnackBar(sensorParentView, getString(R.string.device_not_connected), null, null, Snackbar.LENGTH_SHORT);
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
                    fragment = getSupportFragmentManager()
                            .findFragmentByTag(getSensorName());

                    if (fragment instanceof OperationCallback) {
                        ((OperationCallback) fragment).playData();
                    }
                } else {
                    if (getSensorFragment() instanceof SoundMeterDataFragment) {
                        if (!playingData) {
                            ((SoundMeterDataFragment) getSupportFragmentManager()
                                    .findFragmentByTag(getSensorName())).pause();
                        } else {
                            ((SoundMeterDataFragment) getSupportFragmentManager()
                                    .findFragmentByTag(getSensorName())).resume();
                        }
                    }
                }
                invalidateOptionsMenu();
                break;
            case R.id.stop_data:
                fragment = getSupportFragmentManager()
                        .findFragmentByTag(getSensorName());

                if (fragment instanceof OperationCallback) {
                    ((OperationCallback) fragment).stopData();
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
                toggleGuide();
                break;
            case R.id.save_graph:
                displayLogLocationOnSnackBar();
                fragment = getSupportFragmentManager()
                        .findFragmentByTag(getSensorName());

                if (fragment instanceof OperationCallback) {
                    ((OperationCallback) fragment).saveGraph();
                }
                break;
            case android.R.id.home:
                this.finish();
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
                        Intent intent = new Intent(PSLabSensor.this, DataLoggerActivity.class);
                        intent.putExtra(DataLoggerActivity.CALLER_ACTIVITY, getSensorName());
                        startActivity(intent);
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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PSLabPermission.MAP_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent map = new Intent(getApplicationContext(), MapsActivity.class);
                    startActivity(map);
                } else {
                    CustomSnackBar.showSnackBar(findViewById(android.R.id.content),
                            getString(R.string.no_permission_for_maps), null, null, Snackbar.LENGTH_LONG);
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
                } else {
                    nogpsRecordingCycle();
                }
                invalidateOptionsMenu();
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

    /**
     * Inflate each individual view with content to fill up the sensor guide
     */
    private void setupGuideLayout() {
        bottomSheetGuideTitle.setText(getGuideTitle());
        bottomSheetText.setText(getGuideAbstract());
        bottomSheetSchematic.setImageResource(getGuideSchematics());
        bottomSheetDesc.setText(getGuideDescription());
        // if sensor doesn't image in it's guide and hence returns 0 for getGuideSchematics(), hide the visibility of bottomSheetSchematic
        if (getGuideSchematics() != 0) {
            bottomSheetSchematic.setImageResource(getGuideSchematics());
        } else {
            bottomSheetSchematic.setVisibility(View.GONE);
        }
        // If a sensor has extra content than provided in the standard layout, create a new layout
        // and attach the layout id with getGuideExtraContent()
        if (getGuideExtraContent() != 0) {
            LayoutInflater I = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
            assert I != null;
            View childLayout = I.inflate(getGuideExtraContent(), null);
            bottomSheetAdditionalContent.addView(childLayout);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            getDataFromDataLogger();
        } catch (ArrayIndexOutOfBoundsException e) {
            CustomSnackBar.showSnackBar(findViewById(android.R.id.content),
                    getString(R.string.no_data_fetched), null, null, Snackbar.LENGTH_LONG);
        }
        if (checkGPSOnResume) {
            isRecording = true;
            checkGPSOnResume = false;
            invalidateOptionsMenu();
        }
    }
}
