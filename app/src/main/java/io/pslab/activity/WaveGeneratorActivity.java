package io.pslab.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.warkiz.widget.IndicatorSeekBar;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.pslab.R;
import io.pslab.communication.ScienceLab;
import io.pslab.models.SensorDataBlock;
import io.pslab.models.WaveGeneratorData;
import io.pslab.others.CSVDataLine;
import io.pslab.others.CSVLogger;
import io.pslab.others.CustomSnackBar;
import io.pslab.others.GPSLogger;
import io.pslab.others.LocalDataLog;
import io.pslab.others.MathUtils;
import io.pslab.others.ScienceLabCommon;
import io.pslab.others.SwipeGestureDetector;
import io.pslab.others.WaveGeneratorCommon;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;

public class WaveGeneratorActivity extends AppCompatActivity {

    //const values
    public static final int SIN = 1;
    public static final int TRIANGULAR = 2;
    public static final int PWM = 3;
    public static final String PREFS_NAME = "customDialogPreference";
    private static final CSVDataLine CSV_HEADER = new CSVDataLine()
            .add("Timestamp")
            .add("DateTime")
            .add("Mode")
            .add("Wave")
            .add("Shape")
            .add("Freq")
            .add("Phase")
            .add("Duty")
            .add("lat")
            .add("lon");
    private static boolean waveMonSelected;
    private final long LONG_CLICK_DELAY = 100;
    private final String KEY_LOG = "has_log";
    private final String DATA_BLOCK = "data_block";
    private final String MODE_SQUARE = "Square";
    private final String MODE_PWM = "PWM";
    //waveform monitor
    @BindView(R.id.wave_ic_img)
    ImageView selectedWaveImg;
    @BindView(R.id.wave_mon_select_wave)
    TextView selectedWaveText;
    @BindView(R.id.wave_freq_value)
    TextView waveFreqValue;
    @BindView(R.id.wave_phase_value)
    TextView wavePhaseValue;
    @BindView(R.id.wave_mon_select_prop)
    TextView waveMonPropSelect;
    @BindView(R.id.wave_mon_select_prop_value)
    TextView waveMonPropValueSelect;
    //pwm monitor
    @BindView(R.id.pwm_ic_img)
    ImageView pwmSelectedModeImg;
    @BindView(R.id.pwm_mon_mode_select)
    TextView pwmMonSelectMode;
    @BindView(R.id.pwm_freq_value)
    TextView pwmFreqValue;
    @BindView(R.id.pwm_phase_value)
    TextView pwmPhaseValue;
    @BindView(R.id.pwm_duty_value)
    TextView pwmDutyValue;
    @BindView(R.id.pwm_mon_select_prop)
    TextView pwmMonPropSelect;
    @BindView(R.id.pwm_mon_select_prop_value)
    TextView pwmMonPropSelectValue;
    //buttons on waveform panel
    @BindView(R.id.ctrl_btn_wave1)
    Button btnCtrlWave1;
    @BindView(R.id.ctrl_btn_wave2)
    Button btnCtrlWave2;
    @BindView(R.id.ctrl_btn_freq)
    Button btnCtrlFreq;
    @BindView(R.id.ctrl_btn_phase)
    Button btnCtrlPhase;
    @BindView(R.id.ctrl_img_btn_sin)
    ImageButton imgBtnSin;
    @BindView(R.id.ctrl_img_btn_tri)
    ImageButton imgBtnTri;
    //buttons on PWM panel
    @BindView(R.id.pwm_btn_sq1)
    Button btnPwmSq1;
    @BindView(R.id.pwm_btn_sq2)
    Button btnPwmSq2;
    @BindView(R.id.pwm_btn_sq3)
    Button btnPwmSq3;
    @BindView(R.id.pwm_btn_sq4)
    Button btnPwmSq4;
    @BindView(R.id.analog_mode_btn)
    Button btnAnalogMode;
    @BindView(R.id.digital_mode_btn)
    Button btnDigitalMode;
    @BindView(R.id.pwm_btn_freq)
    Button pwmBtnFreq;
    @BindView(R.id.pwm_btn_duty)
    Button pwmBtnDuty;
    @BindView(R.id.pwm_btn_phase)
    Button pwmBtnPhase;
    //seek bar controls
    @BindView(R.id.img_btn_up)
    ImageButton imgBtnUp;
    @BindView(R.id.img_btn_down)
    ImageButton imgBtnDown;
    @BindView(R.id.seek_bar_wave_gen)
    IndicatorSeekBar seekBar;
    //bottomSheet
    @BindView(R.id.bottom_sheet)
    LinearLayout bottomSheet;
    @BindView(R.id.shadow)
    View tvShadow;
    @BindView(R.id.img_arrow)
    ImageView arrowUpDown;
    @BindView(R.id.sheet_slide_text)
    TextView bottomSheetSlideText;
    @BindView(R.id.wave_phase)
    TextView wavePhaseTitle;
    @BindView(R.id.btn_produce_sound)
    Button btnProduceSound;
    ScienceLab scienceLab;
    BottomSheetBehavior bottomSheetBehavior;
    GestureDetector gestureDetector;
    private int leastCount, seekMax, seekMin;
    private String unit;
    private Timer waveGenCounter;
    private Handler wavegenHandler = new Handler();
    private AlertDialog waveDialog;
    private CSVLogger csvLogger;
    private WaveConst waveBtnActive, pwmBtnActive, prop_active, digital_mode;
    private TextView activePropTv = null;
    private CoordinatorLayout coordinatorLayout;
    private Realm realm;
    private GPSLogger gpsLogger;
    private RealmResults<WaveGeneratorData> recordedWaveData;
    private ConstraintLayout pwmModeLayout;
    private ConstraintLayout squareModeLayout;
    private RelativeLayout pwmModeControls;
    private RelativeLayout squareModeControls;
    private LineChart previewChart;
    private boolean isPlayingSound = false;
    private ProduceSoundTask produceSoundTask;

    private AudioTrack track;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wave_generator_main);
        ButterKnife.bind(this);

        realm = LocalDataLog.with().getRealm();
        gpsLogger = new GPSLogger(this,
                (LocationManager) getSystemService(Context.LOCATION_SERVICE));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.wave_generator));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        coordinatorLayout = findViewById(R.id.wave_generator_coordinator_layout);
        squareModeLayout = findViewById(R.id.square_mode_layout);
        pwmModeLayout = findViewById(R.id.pwm_mode_layout);
        previewChart = findViewById(R.id.chart_preview);

        waveBtnActive = WaveConst.WAVE1;
        pwmBtnActive = WaveConst.SQR1;
        squareModeControls = findViewById(R.id.square_mode_controls);
        pwmModeControls = findViewById(R.id.pwm_mode_controls);
        csvLogger = new CSVLogger(getString(R.string.wave_generator));
        scienceLab = ScienceLabCommon.scienceLab;
        if (!WaveGeneratorCommon.isInitialized) {
            new WaveGeneratorCommon(true);
        }

        setUpBottomSheet();
        tvShadow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                tvShadow.setVisibility(View.GONE);
            }
        });

        enableInitialState();
        waveDialog = createIntentDialog();

        //wave panel
        btnCtrlWave1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!waveBtnActive.equals(WaveConst.WAVE1)) {
                    waveMonSelected = true;
                    selectBtn(WaveConst.WAVE1);
                }
            }
        });
        btnCtrlWave2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!waveBtnActive.equals(WaveConst.WAVE2)) {
                    waveMonSelected = true;
                    selectBtn(WaveConst.WAVE2);
                }
            }
        });
        imgBtnSin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imgBtnSin.setBackground(getResources().getDrawable(R.drawable.btn_back_rounded));
                imgBtnTri.setBackground(getResources().getDrawable(R.drawable.btn_back_rounded_light));
                WaveGeneratorCommon.wave.get(waveBtnActive).put(WaveConst.WAVETYPE, SIN);
                selectWaveform(SIN);
            }
        });

        imgBtnTri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imgBtnSin.setBackground(getResources().getDrawable(R.drawable.btn_back_rounded_light));
                imgBtnTri.setBackground(getResources().getDrawable(R.drawable.btn_back_rounded));
                WaveGeneratorCommon.wave.get(waveBtnActive).put(WaveConst.WAVETYPE, TRIANGULAR);
                selectWaveform(TRIANGULAR);
            }
        });

        btnCtrlFreq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                waveMonSelected = true;
                prop_active = WaveConst.FREQUENCY;
                unit = getString(R.string.unit_hz);
                activePropTv = waveFreqValue;
                waveMonPropSelect.setText(getString(R.string.wave_frequency));
                setSeekBar(seekBar);
                btnCtrlFreq.setBackground(getResources().getDrawable(R.drawable.btn_back_rounded));
                btnCtrlPhase.setBackground(getResources().getDrawable(R.drawable.btn_back_rounded_light));
            }
        });

        btnCtrlPhase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                waveMonSelected = true;
                prop_active = WaveConst.PHASE;
                unit = getString(R.string.deg_text);
                activePropTv = wavePhaseValue;
                waveMonPropSelect.setText(getString(R.string.phase_offset));
                setSeekBar(seekBar);
                btnCtrlFreq.setBackground(getResources().getDrawable(R.drawable.btn_back_rounded_light));
                btnCtrlPhase.setBackground(getResources().getDrawable(R.drawable.btn_back_rounded));
            }
        });

        btnAnalogMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pwmModeLayout.setVisibility(View.GONE);
                pwmModeControls.setVisibility(View.GONE);
                squareModeLayout.setVisibility(View.VISIBLE);
                squareModeControls.setVisibility(View.VISIBLE);
                imgBtnSin.setEnabled(true);
                imgBtnTri.setEnabled(true);
                toggleDigitalMode(WaveConst.SQUARE);
                btnDigitalMode.setBackground(getResources().getDrawable(R.drawable.btn_back_rounded_light));
                btnAnalogMode.setBackground(getResources().getDrawable(R.drawable.btn_back_rounded));
            }
        });

        btnDigitalMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pwmModeLayout.setVisibility(View.VISIBLE);
                pwmModeControls.setVisibility(View.VISIBLE);
                squareModeLayout.setVisibility(View.GONE);
                squareModeControls.setVisibility(View.GONE);
                toggleDigitalMode(WaveConst.PWM);
                imgBtnSin.setEnabled(false);
                imgBtnTri.setEnabled(false);
                pwmBtnActive = WaveConst.SQR1;
                selectBtn(WaveConst.SQR1);
                btnDigitalMode.setBackground(getResources().getDrawable(R.drawable.btn_back_rounded));
                btnAnalogMode.setBackground(getResources().getDrawable(R.drawable.btn_back_rounded_light));
            }
        });

        btnPwmSq1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!pwmBtnActive.equals(WaveConst.SQR1)) {
                    waveMonSelected = false;
                    selectBtn(WaveConst.SQR1);
                }
            }
        });

        btnPwmSq2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!pwmBtnActive.equals(WaveConst.SQR2)) {
                    waveMonSelected = false;
                    selectBtn(WaveConst.SQR2);
                }
            }
        });

        btnPwmSq3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!pwmBtnActive.equals(WaveConst.SQR3)) {
                    waveMonSelected = false;
                    selectBtn(WaveConst.SQR3);
                }
            }
        });

        btnPwmSq4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!pwmBtnActive.equals(WaveConst.SQR4)) {
                    waveMonSelected = false;
                    selectBtn(WaveConst.SQR4);
                }
            }
        });

        pwmBtnFreq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                waveMonSelected = false;
                prop_active = WaveConst.FREQUENCY;
                unit = getString(R.string.unit_hz);
                activePropTv = pwmFreqValue;
                pwmMonPropSelect.setText(getString(R.string.frequecy_colon));
                setSeekBar(seekBar);
                pwmBtnFreq.setBackground(getResources().getDrawable(R.drawable.btn_back_rounded));
                pwmBtnPhase.setBackground(getResources().getDrawable(R.drawable.btn_back_rounded_light));
                pwmBtnDuty.setBackground(getResources().getDrawable(R.drawable.btn_back_rounded_light));
            }
        });

        pwmBtnPhase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                waveMonSelected = false;
                prop_active = WaveConst.PHASE;
                unit = getString(R.string.deg_text);
                activePropTv = pwmPhaseValue;
                pwmMonPropSelect.setText(getString(R.string.pwm_phase));
                setSeekBar(seekBar);
                pwmBtnFreq.setBackground(getResources().getDrawable(R.drawable.btn_back_rounded_light));
                pwmBtnPhase.setBackground(getResources().getDrawable(R.drawable.btn_back_rounded));
                pwmBtnDuty.setBackground(getResources().getDrawable(R.drawable.btn_back_rounded_light));
            }
        });

        pwmBtnDuty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                waveMonSelected = false;
                prop_active = WaveConst.DUTY;
                unit = getString(R.string.unit_percent);
                activePropTv = pwmDutyValue;
                pwmMonPropSelect.setText(getString(R.string.duty_cycle));
                setSeekBar(seekBar);
                pwmBtnFreq.setBackground(getResources().getDrawable(R.drawable.btn_back_rounded_light));
                pwmBtnPhase.setBackground(getResources().getDrawable(R.drawable.btn_back_rounded_light));
                pwmBtnDuty.setBackground(getResources().getDrawable(R.drawable.btn_back_rounded));
            }
        });

        monitorVariations(imgBtnUp, imgBtnDown);

        monitorLongClicks(imgBtnUp, imgBtnDown);

        seekBar.setOnSeekChangeListener(new IndicatorSeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(IndicatorSeekBar seekBar, int progress, float progressFloat, boolean fromUserTouch) {
                String valueText;
                switch (unit) {
                    case "\u00b0":
                        valueText = progress + unit;
                        break;
                    default:
                        valueText = progress + " " + unit;
                }

                if (waveMonSelected) {
                    waveMonPropValueSelect.setText(valueText);
                } else {
                    pwmMonPropSelectValue.setText(valueText);
                }
                setValue();
            }

            @Override
            public void onSectionChanged(IndicatorSeekBar seekBar, int thumbPosOnTick, String textBelowTick, boolean fromUserTouch) {
                //do nothing
            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar, int thumbPosOnTick) {
                //do nothing
            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
                //do nothing
            }
        });

        if (getIntent().getExtras() != null && getIntent().getExtras().getBoolean(KEY_LOG)) {
            recordedWaveData = LocalDataLog.with()
                    .getBlockOfWaveRecords(getIntent().getExtras().getLong(DATA_BLOCK));
            setReceivedData();
        }
        chartInit();

        btnProduceSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlayingSound) {
                    btnProduceSound.setText(getResources().getString(R.string.produce_sound_text));
                    produceSoundTask.cancel(true);
                    produceSoundTask = null;
                    isPlayingSound = false;
                } else {
                    btnProduceSound.setText(getResources().getString(R.string.stop_sound_text));
                    produceSoundTask = new ProduceSoundTask();
                    produceSoundTask.execute();
                    isPlayingSound = true;
                }
            }
        });

        if (getResources().getBoolean(R.bool.isTablet)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
        }
    }

    public void saveWaveConfig() {
        long block = System.currentTimeMillis();
        csvLogger.prepareLogFile();
        csvLogger.writeMetaData(getResources().getString(R.string.wave_generator));
        long timestamp;
        double lat, lon;
        csvLogger.writeCSVFile(CSV_HEADER);
        recordSensorDataBlockID(new SensorDataBlock(block, getResources().getString(R.string.wave_generator)));
        double freq1 = (double) (WaveGeneratorCommon.wave.get(WaveConst.WAVE1).get(WaveConst.FREQUENCY));
        double freq2 = (double) WaveGeneratorCommon.wave.get(WaveConst.WAVE2).get(WaveConst.FREQUENCY);
        double phase = (double) WaveGeneratorCommon.wave.get(WaveConst.WAVE2).get(WaveConst.PHASE);

        String waveType1 = WaveGeneratorCommon.wave.get(WaveConst.WAVE1).get(WaveConst.WAVETYPE) == SIN ? "sine" : "tria";
        String waveType2 = WaveGeneratorCommon.wave.get(WaveConst.WAVE2).get(WaveConst.WAVETYPE) == SIN ? "sine" : "tria";

        if (gpsLogger.isGPSEnabled()) {
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

        timestamp = System.currentTimeMillis();
        String dateTime = CSVLogger.FILE_NAME_FORMAT.format(new Date(timestamp));
        if (scienceLab.isConnected()) {
            if (digital_mode == WaveConst.SQUARE) {
                csvLogger.writeCSVFile(new CSVDataLine().add(timestamp).add(dateTime).add("Square").add("Wave1").add(waveType1).add(freq1).add(0).add(0).add(lat).add(lon)); //wave1
                recordSensorData(new WaveGeneratorData(timestamp, block, "Square", "Wave1", waveType1, String.valueOf(freq1), "0", "0", lat, lon));
                csvLogger.writeCSVFile(new CSVDataLine().add(timestamp).add(dateTime).add("Square").add("Wave2").add(waveType2).add(freq2).add(phase).add(0).add(lat).add(lon)); //wave2
                recordSensorData(new WaveGeneratorData(timestamp + 1, block, "Square", "Wave2", waveType2, String.valueOf(freq2), String.valueOf(phase), "0", lat, lon));
            } else {
                double freqSqr1 = (double) WaveGeneratorCommon.wave.get(WaveConst.SQR1).get(WaveConst.FREQUENCY);
                double dutySqr1 = (double) WaveGeneratorCommon.wave.get(WaveConst.SQR1).get(WaveConst.DUTY) / 100;
                double dutySqr2 = ((double) WaveGeneratorCommon.wave.get(WaveConst.SQR2).get(WaveConst.DUTY)) / 100;
                double phaseSqr2 = (double) WaveGeneratorCommon.wave.get(WaveConst.SQR2).get(WaveConst.PHASE) / 360;
                double dutySqr3 = ((double) WaveGeneratorCommon.wave.get(WaveConst.SQR3).get(WaveConst.DUTY)) / 100;
                double phaseSqr3 = (double) WaveGeneratorCommon.wave.get(WaveConst.SQR3).get(WaveConst.PHASE) / 360;
                double dutySqr4 = ((double) WaveGeneratorCommon.wave.get(WaveConst.SQR4).get(WaveConst.DUTY)) / 100;
                double phaseSqr4 = (double) WaveGeneratorCommon.wave.get(WaveConst.SQR4).get(WaveConst.PHASE) / 360;

                csvLogger.writeCSVFile(new CSVDataLine().add(timestamp).add(dateTime).add("PWM").add("Sq1").add("PWM").add(freqSqr1).add(0).add(dutySqr1).add(lat).add(lon));
                recordSensorData(new WaveGeneratorData(timestamp, block, "PWM", "Sq1", "PWM", String.valueOf(freqSqr1), "0", String.valueOf(dutySqr1), lat, lon));
                csvLogger.writeCSVFile(new CSVDataLine().add(timestamp).add(dateTime).add("PWM").add("Sq2").add("PWM").add(freqSqr1).add(phaseSqr2).add(dutySqr2).add(lat).add(lon));
                recordSensorData(new WaveGeneratorData(timestamp + 1, block, "PWM", "Sq2", "PWM", String.valueOf(freqSqr1), String.valueOf(phaseSqr2), String.valueOf(dutySqr2), lat, lon));
                csvLogger.writeCSVFile(new CSVDataLine().add(timestamp).add(dateTime).add("PWM").add("Sq3").add("PWM").add(freqSqr1).add(phaseSqr3).add(dutySqr3).add(lat).add(lon));
                recordSensorData(new WaveGeneratorData(timestamp + 2, block, "PWM", "Sq3", "PWM", String.valueOf(freqSqr1), String.valueOf(phaseSqr3), String.valueOf(dutySqr3), lat, lon));
                csvLogger.writeCSVFile(new CSVDataLine().add(timestamp).add(dateTime).add("PWM").add("Sq4").add("PWM").add(freqSqr1).add(phaseSqr4).add(dutySqr4).add(lat).add(lon));
                recordSensorData(new WaveGeneratorData(timestamp + 3, block, "PWM", "Sq4", "PWM", String.valueOf(freqSqr1), String.valueOf(phaseSqr4), String.valueOf(dutySqr4), lat, lon));
            }
            CustomSnackBar.showSnackBar(coordinatorLayout,
                    getString(R.string.csv_store_text) + " " + csvLogger.getCurrentFilePath()
                    , getString(R.string.open), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(WaveGeneratorActivity.this, DataLoggerActivity.class);
                            intent.putExtra(DataLoggerActivity.CALLER_ACTIVITY, getResources().getString(R.string.wave_generator));
                            startActivity(intent);
                        }
                    }, Snackbar.LENGTH_SHORT);

        } else {
            CustomSnackBar.showSnackBar(findViewById(android.R.id.content),
                    getString(R.string.device_not_connected), null, null, Snackbar.LENGTH_SHORT);
        }
    }

    public void setReceivedData() {
        for (WaveGeneratorData data : recordedWaveData) {
            Log.d("data", data.toString());
            if (data.getMode().equals(MODE_SQUARE)) {
                WaveGeneratorCommon.mode_selected = WaveConst.SQUARE;
                switch (data.getWave()) {
                    case "Wave1":
                        if (data.getShape().equals("sine")) {
                            WaveGeneratorCommon.wave.get(WaveConst.WAVE1).put(WaveConst.WAVETYPE, SIN);
                        } else {
                            WaveGeneratorCommon.wave.get(WaveConst.WAVE1).put(WaveConst.WAVETYPE, TRIANGULAR);
                        }
                        WaveGeneratorCommon.wave.get(WaveConst.WAVE1).put(WaveConst.FREQUENCY, Double.valueOf(data.getFreq()).intValue());
                        break;
                    case "Wave2":
                        if (data.getShape().equals("sine")) {
                            WaveGeneratorCommon.wave.get(WaveConst.WAVE2).put(WaveConst.WAVETYPE, SIN);
                        } else {
                            WaveGeneratorCommon.wave.get(WaveConst.WAVE2).put(WaveConst.WAVETYPE, TRIANGULAR);
                        }
                        WaveGeneratorCommon.wave.get(WaveConst.WAVE2).put(WaveConst.FREQUENCY, Double.valueOf(data.getFreq()).intValue());
                        WaveGeneratorCommon.wave.get(WaveConst.WAVE2).put(WaveConst.PHASE, Double.valueOf(data.getPhase()).intValue());
                        break;
                }
                enableInitialState();
            } else if (data.getMode().equals(MODE_PWM)) {
                WaveGeneratorCommon.mode_selected = WaveConst.PWM;
                switch (data.getWave()) {
                    case "Sq1":
                        WaveGeneratorCommon.wave.get(WaveConst.SQR1).put(WaveConst.FREQUENCY, Double.valueOf(data.getFreq()).intValue());
                        WaveGeneratorCommon.wave.get(WaveConst.SQR1).put(WaveConst.DUTY, ((Double) (Double.valueOf(data.getDuty()) * 100)).intValue());
                        break;
                    case "Sq2":
                        WaveGeneratorCommon.wave.get(WaveConst.SQR2).put(WaveConst.DUTY, ((Double) (Double.valueOf(data.getDuty()) * 100)).intValue());
                        WaveGeneratorCommon.wave.get(WaveConst.SQR2).put(WaveConst.PHASE, ((Double) (Double.valueOf(data.getPhase()) * 360)).intValue());
                        break;
                    case "Sq3":
                        WaveGeneratorCommon.wave.get(WaveConst.SQR3).put(WaveConst.DUTY, ((Double) (Double.valueOf(data.getDuty()) * 100)).intValue());
                        WaveGeneratorCommon.wave.get(WaveConst.SQR3).put(WaveConst.PHASE, ((Double) (Double.valueOf(data.getPhase()) * 360)).intValue());
                        break;
                    case "Sq4":
                        WaveGeneratorCommon.wave.get(WaveConst.SQR4).put(WaveConst.DUTY, ((Double) (Double.valueOf(data.getDuty()) * 100)).intValue());
                        WaveGeneratorCommon.wave.get(WaveConst.SQR4).put(WaveConst.PHASE, ((Double) (Double.valueOf(data.getPhase()) * 360)).intValue());
                        break;
                }
                enableInitialStatePWM();
            }
        }
    }

    private void setWave() {
        double freq1 = (double) (WaveGeneratorCommon.wave.get(WaveConst.WAVE1).get(WaveConst.FREQUENCY));
        double freq2 = (double) WaveGeneratorCommon.wave.get(WaveConst.WAVE2).get(WaveConst.FREQUENCY);
        double phase = (double) WaveGeneratorCommon.wave.get(WaveConst.WAVE2).get(WaveConst.PHASE);

        String waveType1 = WaveGeneratorCommon.wave.get(WaveConst.WAVE1).get(WaveConst.WAVETYPE) == SIN ? "sine" : "tria";
        String waveType2 = WaveGeneratorCommon.wave.get(WaveConst.WAVE2).get(WaveConst.WAVETYPE) == SIN ? "sine" : "tria";

        if (scienceLab.isConnected()) {
            if (digital_mode == WaveConst.SQUARE) {
                if (phase == WaveData.PHASE_MIN.getValue()) {
                    scienceLab.setSI1(freq1, waveType1);
                    scienceLab.setSI2(freq2, waveType2);
                } else {
                    scienceLab.setWaves(freq1, phase, freq2);
                }
            } else {
                double freqSqr1 = (double) WaveGeneratorCommon.wave.get(WaveConst.SQR1).get(WaveConst.FREQUENCY);
                double dutySqr1 = (double) WaveGeneratorCommon.wave.get(WaveConst.SQR1).get(WaveConst.DUTY) / 100;
                double dutySqr2 = ((double) WaveGeneratorCommon.wave.get(WaveConst.SQR2).get(WaveConst.DUTY)) / 100;
                double phaseSqr2 = (double) WaveGeneratorCommon.wave.get(WaveConst.SQR2).get(WaveConst.PHASE) / 360;
                double dutySqr3 = ((double) WaveGeneratorCommon.wave.get(WaveConst.SQR3).get(WaveConst.DUTY)) / 100;
                double phaseSqr3 = (double) WaveGeneratorCommon.wave.get(WaveConst.SQR3).get(WaveConst.PHASE) / 360;
                double dutySqr4 = ((double) WaveGeneratorCommon.wave.get(WaveConst.SQR4).get(WaveConst.DUTY)) / 100;
                double phaseSqr4 = (double) WaveGeneratorCommon.wave.get(WaveConst.SQR4).get(WaveConst.PHASE) / 360;

                scienceLab.sqrPWM(freqSqr1, dutySqr1, phaseSqr2, dutySqr2, phaseSqr3, dutySqr3, phaseSqr4, dutySqr4, false);
            }

        }
    }

    private void viewWaveDialog() {
        waveDialog.show();
        Window window = waveDialog.getWindow();
        window.setLayout(dpToPx(350), dpToPx(300));
        waveDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(WaveGeneratorActivity.this, R.color.colorPrimary));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.wave_generator_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.save_data:
                saveWaveConfig();
                break;
            case R.id.play_data:
                setWave();
                if (scienceLab.isConnected()) {
                    viewWaveDialog();
                } else {
                    CustomSnackBar.showSnackBar(findViewById(android.R.id.content),
                            getString(R.string.device_not_connected), null, null, Snackbar.LENGTH_SHORT);
                }
                break;
            case R.id.show_guide:
                bottomSheetBehavior.setState(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN ?
                        BottomSheetBehavior.STATE_EXPANDED : BottomSheetBehavior.STATE_HIDDEN);
                break;
            case R.id.show_logged_data:
                Intent intent = new Intent(WaveGeneratorActivity.this, DataLoggerActivity.class);
                intent.putExtra(DataLoggerActivity.CALLER_ACTIVITY, getString(R.string.wave_generator));
                startActivity(intent);
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void selectBtn(WaveConst btn_selected) {

        switch (btn_selected) {

            case WAVE1:

                waveBtnActive = WaveConst.WAVE1;

                btnCtrlWave1.setBackground(getResources().getDrawable(R.drawable.btn_back_rounded));
                btnCtrlWave2.setBackground(getResources().getDrawable(R.drawable.btn_back_rounded_light));

                btnCtrlPhase.setEnabled(false);  //disable phase for wave
                wavePhaseValue.setText("--");

                selectWaveform(WaveGeneratorCommon.wave.get(waveBtnActive).get(WaveConst.WAVETYPE));

                fetchPropertyValue(waveBtnActive, WaveConst.FREQUENCY, getString(R.string.unit_hz), waveFreqValue);
                wavePhaseTitle.setText(getResources().getString(R.string.text_phase_colon));
                break;

            case WAVE2:

                waveBtnActive = WaveConst.WAVE2;

                btnCtrlWave2.setBackground(getResources().getDrawable(R.drawable.btn_back_rounded));
                btnCtrlWave1.setBackground(getResources().getDrawable(R.drawable.btn_back_rounded_light));

                btnCtrlPhase.setEnabled(true); // enable phase for wave2

                selectWaveform(WaveGeneratorCommon.wave.get(waveBtnActive).get(WaveConst.WAVETYPE));

                fetchPropertyValue(waveBtnActive, WaveConst.FREQUENCY, getString(R.string.unit_hz), waveFreqValue);
                fetchPropertyValue(waveBtnActive, WaveConst.PHASE, getString(R.string.deg_text), wavePhaseValue);
                wavePhaseTitle.setText(getResources().getString(R.string.text_phase_colon));
                break;

            case SQR1:
                pwmBtnActive = WaveConst.SQR1;
                btnPwmSq1.setBackground(getResources().getDrawable(R.drawable.btn_back_rounded));
                btnPwmSq2.setBackground(getResources().getDrawable(R.drawable.btn_back_rounded_light));
                btnPwmSq3.setBackground(getResources().getDrawable(R.drawable.btn_back_rounded_light));
                btnPwmSq4.setBackground(getResources().getDrawable(R.drawable.btn_back_rounded_light));
                pwmBtnPhase.setEnabled(false);  //phase disabled for sq1
                pwmPhaseValue.setText("--");
                fetchPropertyValue(pwmBtnActive, WaveConst.FREQUENCY, getString(R.string.unit_hz), pwmFreqValue);
                fetchPropertyValue(pwmBtnActive, WaveConst.DUTY, getString(R.string.unit_percent), pwmDutyValue);
                break;

            case SQR2:

                pwmBtnActive = WaveConst.SQR2;
                btnPwmSq1.setBackground(getResources().getDrawable(R.drawable.btn_back_rounded_light));
                btnPwmSq2.setBackground(getResources().getDrawable(R.drawable.btn_back_rounded));
                btnPwmSq3.setBackground(getResources().getDrawable(R.drawable.btn_back_rounded_light));
                btnPwmSq4.setBackground(getResources().getDrawable(R.drawable.btn_back_rounded_light));
                pwmBtnPhase.setEnabled(true);
                fetchPropertyValue(pwmBtnActive, WaveConst.PHASE, getString(R.string.deg_text), pwmPhaseValue);
                fetchPropertyValue(pwmBtnActive, WaveConst.DUTY, getString(R.string.unit_percent), pwmDutyValue);
                break;

            case SQR3:

                pwmBtnActive = WaveConst.SQR3;
                btnPwmSq1.setBackground(getResources().getDrawable(R.drawable.btn_back_rounded_light));
                btnPwmSq2.setBackground(getResources().getDrawable(R.drawable.btn_back_rounded_light));
                btnPwmSq3.setBackground(getResources().getDrawable(R.drawable.btn_back_rounded));
                btnPwmSq4.setBackground(getResources().getDrawable(R.drawable.btn_back_rounded_light));
                pwmBtnPhase.setEnabled(true);
                fetchPropertyValue(pwmBtnActive, WaveConst.PHASE, getString(R.string.deg_text), pwmPhaseValue);
                fetchPropertyValue(pwmBtnActive, WaveConst.DUTY, getString(R.string.unit_percent), pwmDutyValue);
                break;

            case SQR4:

                pwmBtnActive = WaveConst.SQR4;
                btnPwmSq1.setBackground(getResources().getDrawable(R.drawable.btn_back_rounded_light));
                btnPwmSq2.setBackground(getResources().getDrawable(R.drawable.btn_back_rounded_light));
                btnPwmSq3.setBackground(getResources().getDrawable(R.drawable.btn_back_rounded_light));
                btnPwmSq4.setBackground(getResources().getDrawable(R.drawable.btn_back_rounded));
                pwmBtnPhase.setEnabled(true);
                fetchPropertyValue(pwmBtnActive, WaveConst.PHASE, getString(R.string.deg_text), pwmPhaseValue);
                fetchPropertyValue(pwmBtnActive, WaveConst.DUTY, getString(R.string.unit_percent), pwmDutyValue);
                break;

            default:
                waveBtnActive = WaveConst.WAVE1;
                btnCtrlWave1.setBackground(getResources().getDrawable(R.drawable.btn_back_rounded));
                btnCtrlWave2.setBackground(getResources().getDrawable(R.drawable.btn_back_rounded_light));
                btnCtrlPhase.setEnabled(false);  //disable phase for wave
                wavePhaseValue.setText("--");
                selectWaveform(WaveGeneratorCommon.wave.get(waveBtnActive).get(WaveConst.WAVETYPE));
                fetchPropertyValue(waveBtnActive, WaveConst.FREQUENCY, getString(R.string.deg_text), waveFreqValue);
                break;

        }
        prop_active = null;
        toggleSeekBtns(false);
        previewWave();
    }

    private void selectWaveform(final int waveType) {
        String waveFormText;
        Drawable image;

        switch (waveType) {
            case SIN:
                waveFormText = getString(R.string.sine);
                image = getResources().getDrawable(R.drawable.ic_sin);
                break;

            case TRIANGULAR:
                waveFormText = getString(R.string.triangular);
                image = getResources().getDrawable(R.drawable.ic_triangular);
                break;
            case PWM:
                waveFormText = getResources().getString(R.string.text_pwm);
                image = getResources().getDrawable(R.drawable.ic_pwm_pic);
                break;

            default:
                waveFormText = getString(R.string.sine);
                image = getResources().getDrawable(R.drawable.ic_sin);
        }
        selectedWaveText.setText(waveFormText);
        selectedWaveImg.setImageDrawable(image);
        previewWave();
    }

    private void toggleDigitalMode(WaveConst mode) {
        waveMonSelected = false;
        if (mode == WaveConst.SQUARE) {
            digital_mode = WaveConst.SQUARE;
            pwmSelectedModeImg.setImageResource(R.drawable.ic_square);
            pwmMonSelectMode.setText(getString(R.string.square));
            btnPwmSq2.setEnabled(false);
            btnPwmSq3.setEnabled(false);
            btnPwmSq4.setEnabled(false);
            pwmBtnPhase.setEnabled(false);

        } else {
            digital_mode = WaveConst.PWM;
            pwmSelectedModeImg.setImageResource(R.drawable.ic_pwm_pic);
            pwmMonSelectMode.setText(getString(R.string.text_pwm));
            btnPwmSq2.setEnabled(true);
            btnPwmSq3.setEnabled(true);
            btnPwmSq4.setEnabled(true);
        }
        WaveGeneratorCommon.mode_selected = mode;
        previewWave();
    }

    private void fetchPropertyValue(WaveConst btnActive, WaveConst property, String unit, TextView propTextView) {
        Double value = (double) WaveGeneratorCommon.wave.get(btnActive).get(property);
        String valueText;
        switch (unit) {
            case "\u00b0":
                valueText = value.intValue() + unit;
                break;
            default:
                valueText = value.intValue() + " " + unit;
        }
        propTextView.setText(valueText);
    }

    private void setSeekBar(IndicatorSeekBar seekBar) {

        int numTicks;

        switch (prop_active) {
            case FREQUENCY:
                seekMin = WaveData.FREQ_MIN.getValue();
                seekMax = WaveData.FREQ_MAX.getValue();
                numTicks = 100;
                leastCount = 1;
                break;

            case PHASE:
                seekMin = WaveData.PHASE_MIN.getValue();
                seekMax = WaveData.PHASE_MAX.getValue();
                numTicks = 73;
                leastCount = 1;
                break;

            case DUTY:
                seekMin = WaveData.DUTY_MIN.getValue();
                seekMax = WaveData.DUTY_MAX.getValue();
                numTicks = 100;
                leastCount = 1;
                unit = getString(R.string.unit_percent);
                break;

            default:
                seekMin = 0;
                seekMax = 5000;
                numTicks = 51;
                leastCount = 1;
        }
        seekBar.getBuilder().setMin(seekMin).setMax(seekMax).setTickNum(numTicks).apply();

        if (!waveMonSelected) {
            waveMonPropSelect.setText("");
            waveMonPropValueSelect.setText("");

            if (prop_active.equals(WaveConst.FREQUENCY)) {
                seekBar.setProgress(WaveGeneratorCommon.wave.get(WaveConst.SQR1).get(prop_active));
            } else {
                seekBar.setProgress(WaveGeneratorCommon.wave.get(pwmBtnActive).get(prop_active));
            }
        } else {
            pwmMonPropSelect.setText("");
            pwmMonPropSelectValue.setText("");
            seekBar.setProgress(WaveGeneratorCommon.wave.get(waveBtnActive).get(prop_active));
        }
        toggleSeekBtns(true);
    }

    private void incProgressSeekBar(IndicatorSeekBar seekBar) {
        float value = seekBar.getProgressFloat();
        value = value + leastCount;
        if (value > seekMax) {
            value = seekMax;
        }
        seekBar.setProgress(value);
    }

    private void decProgressSeekBar(IndicatorSeekBar seekBar) {
        Integer value = seekBar.getProgress();
        value = value - leastCount;
        if (value < seekMin) {
            value = seekMin;
        }
        seekBar.setProgress(value);
    }

    private void setValue() {
        Integer value = seekBar.getProgress();

        if (!waveMonSelected) {
            if (prop_active == WaveConst.FREQUENCY) {
                WaveGeneratorCommon.wave.get(WaveConst.SQR1).put(prop_active, value);
            } else {
                if (prop_active == WaveConst.DUTY) {
                    if (value != WaveData.DUTY_MIN.getValue()) {
                        WaveGeneratorCommon.state.put(pwmBtnActive.toString(), 1);
                    } else
                        WaveGeneratorCommon.state.put(pwmBtnActive.toString(), 0);
                }
                WaveGeneratorCommon.wave.get(pwmBtnActive).put(prop_active, value);
            }
        } else {
            WaveGeneratorCommon.wave.get(waveBtnActive).put(prop_active, value);
        }
        setWave();
        previewWave();
        Double dValue = (double) value;
        String valueText;
        switch (unit) {
            case "\u00b0":
                valueText = dValue.intValue() + unit;
                break;
            default:
                valueText = dValue.intValue() + " " + unit;
        }
        activePropTv.setText(valueText);
    }

    private void previewWave() {
        List<ILineDataSet> dataSets = new ArrayList<>();
        ArrayList<Entry> entries = getSamplePoints(false);
        ArrayList<Entry> refEntries = getSamplePoints(true);
        LineDataSet dataSet;
        LineDataSet refDataSet;
        if (WaveGeneratorCommon.mode_selected == WaveConst.PWM) {
            dataSet = new LineDataSet(entries, pwmBtnActive.toString());
            refDataSet = new LineDataSet(refEntries, getResources().getString(R.string.reference_wave_title));
        } else {
            dataSet = new LineDataSet(entries, waveBtnActive.toString());
            refDataSet = new LineDataSet(refEntries, getResources().getString(R.string.reference_wave_title));
        }
        dataSet.setDrawCircles(false);
        dataSet.setColor(Color.WHITE);
        refDataSet.setDrawCircles(false);
        refDataSet.setColor(Color.GRAY);
        dataSets.add(refDataSet);
        dataSets.add(dataSet);
        LineData data = new LineData(dataSets);
        data.setDrawValues(false);
        previewChart.setData(data);
        previewChart.notifyDataSetChanged();
        previewChart.invalidate();
    }

    private ArrayList<Entry> getSamplePoints(boolean isReference) {
        ArrayList<Entry> entries = new ArrayList<>();
        if (WaveGeneratorCommon.mode_selected == WaveConst.PWM) {
            double freq = (double) WaveGeneratorCommon.wave.get(WaveConst.SQR1).get(WaveConst.FREQUENCY);
            double duty = ((double) WaveGeneratorCommon.wave.get(pwmBtnActive).get(WaveConst.DUTY)) / 100;
            double phase = 0;
            if (pwmBtnActive != WaveConst.SQR1 && !isReference) {
                phase = (double) WaveGeneratorCommon.wave.get(pwmBtnActive).get(WaveConst.PHASE);
            }
            for (int i = 0; i < 5000; i++) {
                double t = 2 * Math.PI * freq * (i) / 1e6 + phase * Math.PI / 180;
                double y;
                if (t % (2 * Math.PI) < 2 * Math.PI * duty) {
                    y = 5;
                } else {
                    y = -5;
                }
                entries.add(new Entry((float) i, (float) y));
            }
        } else {
            double phase = 0;
            int shape = WaveGeneratorCommon.wave.get(waveBtnActive).get(WaveConst.WAVETYPE);
            double freq = (double) WaveGeneratorCommon.wave.get(waveBtnActive).get(WaveConst.FREQUENCY);

            if (waveBtnActive != WaveConst.WAVE1 && !isReference) {
                phase = (double) WaveGeneratorCommon.wave.get(WaveConst.WAVE2).get(WaveConst.PHASE);
            }
            if (shape == 1) {
                for (int i = 0; i < 5000; i++) {
                    float y = (float) (5 * Math.sin(2 * Math.PI * (freq / 1e6) * i + phase * Math.PI / 180));
                    entries.add(new Entry((float) i, y));
                }
            } else {
                for (int i = 0; i < 5000; i++) {
                    float y = (float) ((10 / Math.PI) * (Math.asin(Math.sin(2 * Math.PI * (freq / 1e6) * i + phase * Math.PI / 180))));
                    entries.add(new Entry((float) i, y));
                }
            }
        }
        return entries;
    }

    private void chartInit() {
        previewChart.setTouchEnabled(true);
        previewChart.setHighlightPerDragEnabled(true);
        previewChart.setDragEnabled(true);
        previewChart.setScaleEnabled(true);
        previewChart.setDrawGridBackground(false);
        previewChart.setPinchZoom(true);
        previewChart.setScaleYEnabled(false);
        previewChart.setBackgroundColor(Color.BLACK);
        previewChart.getDescription().setEnabled(false);
        previewChart.getXAxis().setAxisMaximum(5000);
        previewChart.getXAxis().setAxisMinimum(0);
        previewChart.getXAxis().setTextColor(Color.WHITE);
        previewChart.getAxisLeft().setAxisMaximum(10);
        previewChart.getAxisLeft().setAxisMinimum(-10);
        previewChart.getAxisRight().setAxisMaximum(10);
        previewChart.getAxisRight().setAxisMinimum(-10);
        previewChart.fitScreen();
        previewChart.invalidate();
        Legend l = previewChart.getLegend();
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.WHITE);
    }

    private void toggleSeekBtns(boolean state) {
        if (!state) {
            waveMonPropSelect.setText("");
            waveMonPropValueSelect.setText("");
            pwmMonPropSelect.setText("");
            pwmMonPropSelectValue.setText("");
        }
        imgBtnUp.setEnabled(state);
        imgBtnDown.setEnabled(state);
        seekBar.setEnabled(state);
    }

    private void enableInitialState() {
        selectBtn(WaveConst.WAVE1);
        toggleDigitalMode(WaveConst.SQUARE);
    }

    private void enableInitialStatePWM() {
        selectBtn(WaveConst.SQR2);
        toggleDigitalMode(WaveConst.PWM);
    }

    private void setUpBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        final SharedPreferences settings = this.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Boolean isFirstTime = settings.getBoolean("WaveGenFirstTime", true);

        if (isFirstTime) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            tvShadow.setVisibility(View.VISIBLE);
            tvShadow.setAlpha(0.8f);
            arrowUpDown.setRotation(180);
            bottomSheetSlideText.setText(R.string.hide_guide_text);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("WaveGenFirstTime", false);
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

    /**
     * Click listeners to increment and decrement buttons
     *
     * @param up   increment button
     * @param down decrement button
     */
    private void monitorVariations(ImageButton up, ImageButton down) {
        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                incProgressSeekBar(seekBar);
            }
        });
        up.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View view) {
                fastCounter(true);
                return true;
            }
        });
        down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                decProgressSeekBar(seekBar);
            }
        });
        down.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View view) {
                fastCounter(false);
                return true;
            }
        });
    }

    /**
     * Handles action when user releases long click on an increment or a decrement button
     *
     * @param up   increment button
     * @param down decrement button
     */
    @SuppressLint("ClickableViewAccessibility")
    private void monitorLongClicks(ImageButton up, ImageButton down) {
        up.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                view.onTouchEvent(motionEvent);
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    stopCounter();
                }
                return true;
            }
        });
        down.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                view.onTouchEvent(motionEvent);
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    stopCounter();
                }
                return true;
            }
        });
    }

    /**
     * Stops the Timer that is changing the seekbar value
     */
    private void stopCounter() {
        if (waveGenCounter != null) {
            waveGenCounter.cancel();
            waveGenCounter.purge();
        }
    }

    /**
     * TimerTask implementation to increment or decrement value at the seekbar at a constant
     * rate provided by LONG_CLICK_DELAY
     *
     * @param increaseValue flag for whether it is increase or decrease
     */
    private void fastCounter(final boolean increaseValue) {
        waveGenCounter = new Timer();
        TimerTask task = new TimerTask() {
            public void run() {
                wavegenHandler.post(new Runnable() {
                    public void run() {
                        if (increaseValue) {
                            incProgressSeekBar(seekBar);
                        } else {
                            decProgressSeekBar(seekBar);
                        }
                    }
                });
            }
        };
        waveGenCounter.schedule(task, 1, LONG_CLICK_DELAY);
    }

    private AlertDialog createIntentDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.wavegen_intent_dialog, null);
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                waveDialog.cancel();
            }
        }).setTitle(R.string.open_instrument);

        dialogView.findViewById(R.id.osc_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(WaveGeneratorActivity.this, OscilloscopeActivity.class));
                waveDialog.cancel();
            }
        });

        dialogView.findViewById(R.id.la_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(WaveGeneratorActivity.this, LogicalAnalyzerActivity.class));
                waveDialog.cancel();
            }
        });

        dialogBuilder.setView(dialogView);
        return dialogBuilder.create();
    }

    private int dpToPx(int dp) {
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        return Math.round((float) dp * (displayMetrics.xdpi / 160.0F));
    }

    public void recordSensorDataBlockID(SensorDataBlock block) {
        realm.beginTransaction();
        realm.copyToRealm(block);
        realm.commitTransaction();
    }

    public void recordSensorData(RealmObject sensorData) {
        realm.beginTransaction();
        realm.copyToRealm((WaveGeneratorData) sensorData);
        realm.commitTransaction();
    }

    public enum WaveConst {WAVETYPE, WAVE1, WAVE2, SQR1, SQR2, SQR3, SQR4, FREQUENCY, PHASE, DUTY, SQUARE, PWM}

    public enum WaveData {
        FREQ_MIN(10), DUTY_MIN(0), PHASE_MIN(0), FREQ_MAX(5000), PHASE_MAX(360), DUTY_MAX(100);

        public final int value;

        WaveData(final int v) {
            value = v;
        }

        public final int getValue() {
            return value;
        }
    }

    private class ProduceSoundTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            short[] buffer = new short[1024];
            track = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, buffer.length, AudioTrack.MODE_STREAM);
            float angle = 0;
            float samples[] = new float[1024];

            track.play();
            double frequency;
            while (isPlayingSound) {
                if (WaveGeneratorCommon.mode_selected == WaveConst.SQUARE) {
                    frequency = WaveGeneratorCommon.wave.get(waveBtnActive).get(WaveConst.FREQUENCY);
                } else {
                    frequency = WaveGeneratorCommon.wave.get(WaveConst.SQR1).get(WaveConst.FREQUENCY);
                }
                float increment = (float) ((2 * Math.PI) * frequency / 44100);
                for (int i = 0; i < samples.length; i++) {
                    samples[i] = (float) Math.sin(angle);
                    if (WaveGeneratorCommon.mode_selected == WaveConst.PWM) {
                        samples[i] = (samples[i] >= 0.0) ? 1 : -1;
                    } else {
                        if (WaveGeneratorCommon.wave.get(waveBtnActive).get(WaveConst.WAVETYPE) == 2) {
                            samples[i] = (float) ((2 / Math.PI) * Math.asin(samples[i]));
                        }
                    }
                    buffer[i] = (short) (samples[i] * Short.MAX_VALUE);
                    angle += increment;
                }
                track.write(buffer, 0, buffer.length);
            }
            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            if (track != null) {
                track.flush();
                track.stop();
                track.release();
            }
        }
    }
}
