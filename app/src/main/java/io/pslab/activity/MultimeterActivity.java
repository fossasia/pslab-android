package io.pslab.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
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
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.pslab.R;
import io.pslab.communication.ScienceLab;
import io.pslab.fragment.MultimeterSettingsFragment;
import io.pslab.others.CSVLogger;
import io.pslab.others.CustomSnackBar;
import io.pslab.others.GPSLogger;
import io.pslab.others.MathUtils;

import android.support.v7.preference.PreferenceManager;

import io.pslab.others.ScienceLabCommon;
import io.pslab.others.SwipeGestureDetector;
import it.beppi.knoblibrary.Knob;
import io.pslab.DataFormatter;

/**
 * Created by Abhinav Raj on 26/5/18.
 */

public class MultimeterActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "customDialogPreference";
    public static final String NAME = "savingData";
    private static final int MY_PERMISSIONS_REQUEST_STORAGE_FOR_DATA = 101;
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
    //bottomSheet
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
    BottomSheetBehavior bottomSheetBehavior;
    GestureDetector gestureDetector;
    SharedPreferences multimeter_data;
    private ScienceLab scienceLab;
    private int knobState;
    private String dataRecorded;
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
    private String multimeterCSVheader = "Data,Value,Latitude,Longitude\n";
    private GPSLogger gpsLogger;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multimeter_main);
        defaultValue = getString(R.string.multimeter_default_value);
        ButterKnife.bind(this);
        scienceLab = ScienceLabCommon.scienceLab;
        knobMarker = getResources().getStringArray(io.pslab.R.array.multimeter_knob_states);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        checkConfig();
        logTimer();

        gpsLogger = new GPSLogger(this, (LocationManager) getSystemService(Context.LOCATION_SERVICE));
        setUpBottomSheet();
        tvShadow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                tvShadow.setVisibility(View.GONE);
            }
        });

        multimeter_data = this.getSharedPreferences(NAME, MODE_PRIVATE);
        dataRecorded = multimeterCSVheader;
        knobState = multimeter_data.getInt("KnobState", 2);
        switchIsChecked = multimeter_data.getBoolean("SwitchState", false);
        aSwitch.setChecked(switchIsChecked);

        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/digital-7 (italic).ttf");
        quantity.setTypeface(tf);

        String text_quantity = multimeter_data.getString("TextBox", defaultValue);
        String text_unit = multimeter_data.getString("TextBoxUnit", null);
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
                    String recordUnit = "";
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
                            resistanceUnit = "";
                        }
                    }
                    saveAndSetData(Resistance, resistanceUnit);
                    if (recordData)
                        record("Resistance", Resistance + " " + recordUnit);
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
                        capacitanceUnit = "";
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
                        record("Capacitance", Capacitance + " " + capacitanceUnit);
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
            saveAndSetData("Cannot measure!", "");
            record(knobMarker[knobState], "null");
        }
    }

    private void checkConfig() {
        SharedPreferences multimeterConfigs = PreferenceManager.getDefaultSharedPreferences(this);
        recordPeriod = Long.valueOf(multimeterConfigs.getString(MultimeterSettingsFragment.KEY_UPDATE_PERIOD, getResources().getString(R.string.multimeter_default_1000)));
        locationEnabled = multimeterConfigs.getBoolean(MultimeterSettingsFragment.KEY_INCLUDE_LOCATION, true);
    }

    private void setUpBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        final SharedPreferences settings = this.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Boolean isFirstTime = settings.getBoolean("MultimeterFirstTime", true);

        bottomSheetGuideTitle.setText(R.string.multimeter_dialog_heading);
        bottomSheetText.setText(R.string.multimeter_dialog_text);
        bottomSheetSchematic.setImageResource(R.drawable.multimeter_circuit);
        bottomSheetDesc.setText(R.string.multimeter_dialog_description);

        if (isFirstTime) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            tvShadow.setVisibility(View.VISIBLE);
            tvShadow.setAlpha(0.8f);
            arrowUpDown.setRotation(180);
            bottomSheetSlideText.setText(R.string.hide_guide_text);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("MultimeterFirstTime", false);
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);                 //Gesture detector need this to transfer touch event to the gesture detector.
        return super.onTouchEvent(event);
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
            lat = location.getLatitude();
            lon = location.getLongitude();
        } else {
            lat = lon = 0;
        }
        dataRecorded += data + "," + value + "," + lat + "," + lon + "\n";
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
                            multimeterLogger.writeCSVFile(dataRecorded);
                            dataRecorded = multimeterCSVheader;
                            // Export Data
                            CustomSnackBar.showSnackBar(coordinatorLayout,
                                    getString(R.string.csv_store_text) + " " + multimeterLogger.getCurrentFilePath()
                                    , getString(R.string.delete_capital), new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            new AlertDialog.Builder(MultimeterActivity.this, R.style.AlertDialogStyle)
                                                    .setTitle(R.string.delete_file)
                                                    .setMessage(R.string.delete_warning)
                                                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            multimeterLogger.deleteFile();
                                                        }
                                                    })
                                                    .setNegativeButton(R.string.cancel, null)
                                                    .create()
                                                    .show();
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
                            isRecordingStarted = true;
                            recordData = true;
                            CustomSnackBar.showSnackBar(coordinatorLayout, getString(R.string.data_recording_start), null, null, Snackbar.LENGTH_SHORT);
                        }
                    }
                } else {
                    CustomSnackBar.showSnackBar(coordinatorLayout, getString(R.string.device_not_found), null, null, Snackbar.LENGTH_SHORT);
                }
                break;
            case R.id.delete_csv_data:
                if (isDataRecorded) {
                    MenuItem item1 = menu.findItem(R.id.record_pause_data);
                    item1.setIcon(R.drawable.ic_record_white);
                    recordData = false;
                    isRecordingStarted = false;
                    isDataRecorded = false;
                    multimeterLogger.deleteFile();
                    CustomSnackBar.showSnackBar(coordinatorLayout, getString(R.string.data_deleted), null, null, Snackbar.LENGTH_SHORT);
                } else
                    CustomSnackBar.showSnackBar(coordinatorLayout, getString(R.string.nothing_to_delete), null, null, Snackbar.LENGTH_SHORT);
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
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (recordTimer != null) {
            recordTimer.cancel();
            recordTimer = null;
        }
        if (isRecordingStarted) {
            if (multimeterLogger != null)
                multimeterLogger.deleteFile();
            isRecordingStarted = false;
        }
    }
}
