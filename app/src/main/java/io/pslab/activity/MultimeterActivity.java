package io.pslab.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.snackbar.Snackbar;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.pslab.DataFormatter;
import io.pslab.R;
import io.pslab.activity.guide.GuideActivity;
import io.pslab.communication.ScienceLab;
import io.pslab.fragment.MultimeterSettingsFragment;
import io.pslab.models.MultimeterData;
import io.pslab.models.SensorDataBlock;
import io.pslab.others.CSVDataLine;
import io.pslab.others.CSVLogger;
import io.pslab.others.CustomSnackBar;
import io.pslab.others.GPSLogger;
import io.pslab.others.LocalDataLog;
import io.pslab.others.ScienceLabCommon;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;
import it.beppi.knoblibrary.Knob;

/**
 * Created by Abhinav Raj on 26/5/18.
 */

public class MultimeterActivity extends GuideActivity {

    public static final String NAME = "savingData";
    private static final int MY_PERMISSIONS_REQUEST_STORAGE_FOR_DATA = 101;
    private static final CSVDataLine CSV_HEADER =
            new CSVDataLine()
                    .add("Timestamp")
                    .add("DateTime")
                    .add("Data")
                    .add("Value")
                    .add("Latitude")
                    .add("Longitude");
    private final String KEY_LOG = "has_log";
    private final String DATA_BLOCK = "data_block";
    public boolean recordData = false;
    public CSVLogger multimeterLogger = null;
    @BindView(R.id.multimeter_toolbar)
    Toolbar mToolbar;
    @BindView(R.id.quantity)
    TextView quantity;
    @BindView(R.id.unit)
    TextView unit;
    @BindView(R.id.knobs)
    Knob knob;
    @BindView(R.id.selector)
    SwitchCompat aSwitch;
    @BindView(R.id.multimeter_coordinator_layout)
    CoordinatorLayout coordinatorLayout;

    SharedPreferences multimeter_data;
    private ScienceLab scienceLab;
    private int knobState;
    private CSVDataLine dataRecorded;
    private String defaultValue;
    private Menu menu;
    private Boolean switchIsChecked;
    private String[] knobMarker;
    private boolean isRecordingStarted = false;
    private boolean isDataRecorded = false;
    private Timer recordTimer;
    private boolean locationEnabled = true;
    private long recordPeriod;
    private double lat = 0, lon = 0;
    private GPSLogger gpsLogger;
    private Realm realm;
    private long block;
    private RealmResults<MultimeterData> recordedMultimeterData;
    private MenuItem playMenu;
    private MenuItem stopMenu;
    private boolean isPlayingBack = false;
    private boolean playClicked = false;
    private Timer playBackTimer;
    private int currentPosition = 0;

    public MultimeterActivity() {
        super(R.layout.activity_multimeter_main);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        defaultValue = getString(R.string.multimeter_default_value);
        ButterKnife.bind(this);
        scienceLab = ScienceLabCommon.scienceLab;
        knobMarker = getResources().getStringArray(io.pslab.R.array.multimeter_knob_states);
        setSupportActionBar(mToolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        gpsLogger = new GPSLogger(this, (LocationManager) getSystemService(Context.LOCATION_SERVICE));

        multimeter_data = this.getSharedPreferences(NAME, MODE_PRIVATE);
        dataRecorded = CSV_HEADER;
        knobState = multimeter_data.getInt("KnobState", 2);
        switchIsChecked = multimeter_data.getBoolean("SwitchState", false);
        aSwitch.setChecked(switchIsChecked);

        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/digital-7 (italic).ttf");
        quantity.setTypeface(tf);

        String text_quantity = multimeter_data.getString("TextBox", defaultValue);
        String text_unit = multimeter_data.getString("TextBoxUnit", null);

        realm = LocalDataLog.with().getRealm();

        if (getIntent().getExtras() != null && getIntent().getExtras().getBoolean(KEY_LOG)) {
            recordedMultimeterData = LocalDataLog.with()
                    .getBlockOfMultimeterRecords(getIntent().getExtras().getLong(DATA_BLOCK));
            isPlayingBack = true;
        } else {
            knob.setState(knobState);
            quantity.setText(text_quantity);
            unit.setText(text_unit);
            knob.setOnStateChanged(new Knob.OnStateChanged() {
                @Override
                public void onState(int state) {
                    knobState = state;
                    saveKnobState(knobState);
                }
            });
            aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    switchIsChecked = isChecked;
                    SharedPreferences.Editor editor = multimeter_data.edit();
                    editor.putBoolean("SwitchState", switchIsChecked);
                    editor.apply();
                }
            });
            isPlayingBack = false;
            checkConfig();
            logTimer();
        }
        if (getResources().getBoolean(R.bool.isTablet)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkConfig();
    }

    private void logData() {
        switch (knobState) {
            case 3: // Resistor
                if (scienceLab.isConnected()) {
                    DecimalFormat resistanceFormat = new DecimalFormat("#.##");
                    Double resistance;
                    Double avgResistance = 0.0;
                    int loops = 20;
                    for (int i = 0; i < loops; i++) {
                        resistance = scienceLab.getResistance();
                        if (resistance == null) {
                            avgResistance = null;
                            break;
                        } else {
                            avgResistance = avgResistance + resistance / loops;
                        }
                    }
                    String resistanceUnit;
                    String recordUnit = "Ohms";
                    String Resistance = "";
                    if (avgResistance == null) {
                        Resistance = "Infinity";
                        resistanceUnit = "\u2126";
                        recordUnit = "Ohms";
                    } else {
                        if (avgResistance > 10e5) {
                            Resistance = resistanceFormat.format((avgResistance / 10e5));
                            resistanceUnit = "M" + "\u2126";
                            recordUnit = "MOhms";
                        } else if (avgResistance > 10e2) {
                            Resistance = resistanceFormat.format((avgResistance / 10e2));
                            resistanceUnit = "k" + "\u2126";
                            recordUnit = "kOhms";
                        } else if (avgResistance > 1) {
                            Resistance = resistanceFormat.format(avgResistance);
                            resistanceUnit = "\u2126";
                            recordUnit = "Ohms";
                        } else {
                            Resistance = "Cannot measure!";
                            resistanceUnit = "Ohms";
                        }
                    }
                    saveAndSetData(Resistance, resistanceUnit);
                    if (recordData)
                        record(knobMarker[knobState], Resistance + " " + recordUnit);
                }
                break;
            case 4: //Capacitor
                if (scienceLab.isConnected()) {
                    Double capacitance = scienceLab.getCapacitance();
                    DecimalFormat capacitanceFormat = new DecimalFormat("#.##");
                    String Capacitance;
                    String capacitanceUnit;
                    if (capacitance == null) {
                        Capacitance = "Cannot measure!";
                        capacitanceUnit = "pF";
                    } else {
                        if (capacitance < 1e-9) {
                            Capacitance = capacitanceFormat.format((capacitance / 1e-12));
                            capacitanceUnit = "pF";
                        } else if (capacitance < 1e-6) {
                            Capacitance = capacitanceFormat.format((capacitance / 1e-9));
                            capacitanceUnit = "nF";
                        } else if (capacitance < 1e-3) {
                            Capacitance = capacitanceFormat.format((capacitance / 1e-6));
                            capacitanceUnit = "\u00B5" + "F";
                        } else if (capacitance < 1e-1) {
                            Capacitance = capacitanceFormat.format((capacitance / 1e-3));
                            capacitanceUnit = "mF";
                        } else {
                            Capacitance = capacitanceFormat.format(capacitance);
                            capacitanceUnit = getString(R.string.capacitance_unit);
                        }
                    }
                    saveAndSetData(Capacitance, capacitanceUnit);
                    if (recordData)
                        record(knobMarker[knobState], Capacitance + " " + capacitanceUnit);
                }
                break;
            case 5:
                getIDdata();
                break;
            case 6:
                getIDdata();
                break;
            case 7:
                getIDdata();
                break;
            case 8:
                getIDdata();
                break;
            default:
                if (scienceLab.isConnected()) {
                    saveAndSetData(DataFormatter.formatDouble(scienceLab.getVoltage(knobMarker[knobState], 1), DataFormatter.LOW_PRECISION_FORMAT), getString(R.string.multimeter_voltage_unit));
                    if (recordData)
                        record(knobMarker[knobState], DataFormatter.formatDouble(scienceLab.getVoltage(knobMarker[knobState], 1), DataFormatter.LOW_PRECISION_FORMAT) + " " + getString(R.string.multimeter_voltage_unit));
                }
                break;
        }
    }

    private void logTimer() {
        if (recordTimer == null) {
            recordTimer = new Timer();
        }
        recordTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logData();
                    }
                });
            }
        }, 0, recordPeriod);
    }

    private void getIDdata() {
        try {
            if (!switchIsChecked) {
                if (scienceLab.isConnected()) {
                    Double frequency = scienceLab.getFrequency(knobMarker[knobState], null);
                    saveAndSetData(DataFormatter.formatDouble(frequency, DataFormatter.LOW_PRECISION_FORMAT), getString(R.string.frequency_unit));
                    if (recordData)
                        record(knobMarker[knobState], DataFormatter.formatDouble(frequency, DataFormatter.LOW_PRECISION_FORMAT) + getString(R.string.frequency_unit));
                }
            } else {
                if (scienceLab.isConnected()) {
                    scienceLab.countPulses(knobMarker[knobState]);
                    double pulseCount = scienceLab.readPulseCount();
                    saveAndSetData(DataFormatter.formatDouble(pulseCount, DataFormatter.LOW_PRECISION_FORMAT), "");
                    if (recordData)
                        record(knobMarker[knobState], String.valueOf(pulseCount));
                }
            }
        } catch (Exception e) {
            saveAndSetData("Cannot measure!", "null");
            if (recordData) {
                record(knobMarker[knobState], "null");
            }
        }
    }

    private void checkConfig() {
        SharedPreferences multimeterConfigs = PreferenceManager.getDefaultSharedPreferences(this);
        recordPeriod = Long.valueOf(multimeterConfigs.getString(MultimeterSettingsFragment.KEY_UPDATE_PERIOD, getResources().getString(R.string.multimeter_default_1000)));
        locationEnabled = multimeterConfigs.getBoolean(MultimeterSettingsFragment.KEY_INCLUDE_LOCATION, true);
    }

    private void saveAndSetData(String Quantity, String Unit) {
        SharedPreferences.Editor editor = multimeter_data.edit();
        editor.putString("TextBox", Quantity);
        editor.putString("TextBoxUnit", Unit);
        editor.apply();
        quantity.setText(Quantity);
        unit.setText(Unit);
    }

    private void record(String data, String value) {
        if (locationEnabled && gpsLogger.isGPSEnabled()) {
            Location location = gpsLogger.getDeviceLocation();
            if (location != null) {
                lat = location.getLatitude();
                lon = location.getLongitude();
            } else {
                lat = 0.0;
                lon = 0.0;
            }
        } else {
            lat = 0.0;
            lon = 0.0;
        }
        long timestamp = System.currentTimeMillis();
        dataRecorded = new CSVDataLine()
                .add(timestamp)
                .add(CSVLogger.FILE_NAME_FORMAT.format(new Date(timestamp)))
                .add(data)
                .add(value)
                .add(lat)
                .add(lon);
        multimeterLogger.writeCSVFile(dataRecorded);
        recordSensorData(new MultimeterData(timestamp, block, data, value, lat, lon));
    }

    private void saveKnobState(int state) {
        SharedPreferences.Editor editor = multimeter_data.edit();
        editor.putInt("KnobState", state);
        editor.apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.multimeter_log_menu, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        playMenu = menu.findItem(R.id.play_data);
        stopMenu = menu.findItem(R.id.stop_data);
        playMenu.setVisible(isPlayingBack);
        menu.findItem(R.id.record_pause_data).setVisible(!isPlayingBack);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.record_pause_data:
                if (scienceLab.isConnected()) {
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_STORAGE_FOR_DATA);
                        return true;
                    }
                    if (recordData) {
                        item.setIcon(R.drawable.ic_record_white);
                        recordData = false;
                        if (isDataRecorded) {
                            MenuItem item1 = menu.findItem(R.id.record_pause_data);
                            item1.setIcon(R.drawable.ic_record_white);
                            dataRecorded = CSV_HEADER;
                            // Export Data
                            CustomSnackBar.showSnackBar(coordinatorLayout,
                                    getString(R.string.csv_store_text) + " " + multimeterLogger.getCurrentFilePath()
                                    , getString(R.string.open), new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            startActivity(new Intent(MultimeterActivity.this, DataLoggerActivity.class));
                                        }
                                    }, Snackbar.LENGTH_SHORT);
                            isRecordingStarted = false;
                            recordData = false;
                        } else {
                            CustomSnackBar.showSnackBar(coordinatorLayout, getString(R.string.nothing_to_export), null, null, Snackbar.LENGTH_SHORT);
                        }
                    } else {
                        isDataRecorded = true;
                        item.setIcon(R.drawable.ic_record_stop_white);
                        if (!isRecordingStarted) {
                            multimeterLogger = new CSVLogger(getString(R.string.multimeter));
                            multimeterLogger.prepareLogFile();
                            multimeterLogger.writeMetaData(getResources().getString(R.string.multimeter));
                            multimeterLogger.writeCSVFile(CSV_HEADER);
                            block = System.currentTimeMillis();
                            recordSensorDataBlockID(new SensorDataBlock(block, getResources().getString(R.string.multimeter)));
                            isRecordingStarted = true;
                            recordData = true;
                            CustomSnackBar.showSnackBar(coordinatorLayout, getString(R.string.data_recording_start), null, null, Snackbar.LENGTH_SHORT);
                        }
                    }
                } else {
                    CustomSnackBar.showSnackBar(coordinatorLayout, getString(R.string.device_not_found), null, null, Snackbar.LENGTH_SHORT);
                }
                break;
            case R.id.settings:
                Intent settingIntent = new Intent(this, SettingsActivity.class);
                settingIntent.putExtra("title", getResources().getString(R.string.multimeter_configurations));
                startActivity(settingIntent);
                break;
            case android.R.id.home:
                this.finish();
                break;
            case R.id.multimeter_show_data:
                Intent intent = new Intent(this, DataLoggerActivity.class);
                intent.putExtra(DataLoggerActivity.CALLER_ACTIVITY, getResources().getString(R.string.multimeter));
                startActivity(intent);
                break;
            case R.id.show_guide:
                toggleGuide();
                break;
            case R.id.play_data:
                if (playClicked) {
                    playClicked = false;
                    stopMenu.setVisible(true);
                    item.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_play_arrow_white_24dp, null));
                    if (playBackTimer != null) {
                        playBackTimer.cancel();
                    }
                } else {
                    playClicked = true;
                    item.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_pause_white_24dp, null));
                    stopMenu.setVisible(true);
                    if (playBackTimer != null) {
                        playBackTimer.cancel();
                    }
                    playBackTimer = new Timer();
                    final Handler handler = new Handler();
                    playBackTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (currentPosition < recordedMultimeterData.size()) {
                                        setLoggedData(recordedMultimeterData.get(currentPosition));
                                    } else {
                                        playBackTimer.cancel();
                                        currentPosition = 0;
                                        stopMenu.setVisible(false);
                                        item.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_play_arrow_white_24dp, null));
                                    }
                                }
                            });
                        }
                    }, 0, recordPeriod);
                }
                break;
            case R.id.stop_data:
                stopMenu.setVisible(false);
                if (playBackTimer != null) {
                    playBackTimer.cancel();
                    playBackTimer = null;
                }
                currentPosition = 0;
                playClicked = false;
                playMenu.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_play_arrow_white_24dp, null));
            default:
                break;
        }
        return true;
    }

    private void setLoggedData(MultimeterData multimeterData) {
        String data = multimeterData.getData();
        String value = multimeterData.getValue();
        knob.setEnabled(false);
        knob.setState(Arrays.asList(knobMarker).indexOf(data));
        String quantityString = "";
        String unitString = "";
        try {
            if (value.split(" ")[0].equals("Cannot")) {
                quantityString = value.split(" ")[0] + " " + value.split(" ")[1];
                unitString = value.split(" ")[2];
            } else {
                quantityString = value.split(" ")[0];
                unitString = value.split(" ")[1];
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        unit.setText(unitString);
        quantity.setText(quantityString);
        currentPosition++;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (recordTimer != null) {
            recordTimer.cancel();
            recordTimer = null;
        }
        if (playBackTimer != null) {
            playBackTimer.cancel();
            playBackTimer = null;
        }
        if (isRecordingStarted) {
            if (multimeterLogger != null)
                multimeterLogger.deleteFile();
            isRecordingStarted = false;
        }
    }

    public void recordSensorDataBlockID(SensorDataBlock block) {
        realm.beginTransaction();
        realm.copyToRealm(block);
        realm.commitTransaction();
    }

    public void recordSensorData(RealmObject sensorData) {
        realm.beginTransaction();
        realm.copyToRealm((MultimeterData) sensorData);
        realm.commitTransaction();
    }
}
