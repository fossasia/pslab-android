package io.pslab.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.snackbar.Snackbar;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.pslab.R;
import io.pslab.activity.guide.GuideActivity;
import io.pslab.communication.ScienceLab;
import io.pslab.models.SensorDataBlock;
import io.pslab.models.WaveGeneratorData;
import io.pslab.others.CSVDataLine;
import io.pslab.others.CSVLogger;
import io.pslab.others.CustomSnackBar;
import io.pslab.others.GPSLogger;
import io.pslab.others.LocalDataLog;
import io.pslab.others.ScienceLabCommon;
import io.pslab.others.WaveGeneratorConstants;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;

public class WaveGeneratorActivity extends GuideActivity {

    //const values
    public static final int SIN = 1;
    public static final int TRIANGULAR = 2;
    public static final int PWM = 3;
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
    @BindView(R.id.wave_phase)
    TextView wavePhaseTitle;
    @BindView(R.id.btn_produce_sound)
    Button btnProduceSound;
    ScienceLab scienceLab;
    private int leastCount, seekMax, seekMin;
    private String unit;
    private Timer waveGenCounter;
    private final Handler wavegenHandler = new Handler();
    private AlertDialog waveDialog;
    private CSVLogger csvLogger;
    private WaveConst waveBtnActive, pwmBtnActive, prop_active, digital_mode;
    private TextView activePropTv;
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

    public WaveGeneratorActivity() {
        super(R.layout.activity_wave_generator_main);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        realm = LocalDataLog.with().getRealm();
        gpsLogger = new GPSLogger(this,
                (LocationManager) getSystemService(Context.LOCATION_SERVICE));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.wave_generator));
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
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
        seekBar.setSaveEnabled(false);

        if (savedInstanceState != null) {
            switch (Objects.requireNonNull(savedInstanceState.getString("digital_mode"))) {
                case "SQUARE":
                    toggleDigitalMode(WaveConst.SQUARE);
                    break;
                case "PWM":
                    toggleDigitalMode(WaveConst.PWM);
                    break;
                default:
                    break;
            }
            switch (Objects.requireNonNull(savedInstanceState.getString("waveBtnActive"))) {
                case "WAVE1":
                    waveMonSelected = true;
                    selectBtn(WaveConst.WAVE1);
                    switch (Objects.requireNonNull(savedInstanceState.getString("prop_active"))) {
                        case "FREQUENCY":
                            waveMonSelected = true;
                            prop_active = WaveConst.FREQUENCY;
                            unit = getString(R.string.unit_hz);
                            activePropTv = waveFreqValue;
                            btnCtrlFreq.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                            btnCtrlPhase.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                            break;
                        case "PHASE":
                            waveMonSelected = true;
                            prop_active = WaveConst.PHASE;
                            unit = getString(R.string.deg_text);
                            activePropTv = wavePhaseValue;
                            btnCtrlFreq.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                            btnCtrlPhase.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                            break;
                        default:
                            break;
                    }
                    break;
                case "WAVE2":
                    waveMonSelected = true;
                    selectBtn(WaveConst.WAVE2);
                    switch (Objects.requireNonNull(savedInstanceState.getString("prop_active"))) {
                        case "FREQUENCY":
                            waveMonSelected = true;
                            prop_active = WaveConst.FREQUENCY;
                            unit = getString(R.string.unit_hz);
                            activePropTv = waveFreqValue;
                            btnCtrlFreq.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                            btnCtrlPhase.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                            break;
                        case "PHASE":
                            waveMonSelected = true;
                            prop_active = WaveConst.PHASE;
                            unit = getString(R.string.deg_text);
                            activePropTv = wavePhaseValue;
                            btnCtrlFreq.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                            btnCtrlPhase.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }
            switch (Objects.requireNonNull(savedInstanceState.getString("pwmBtnActive"))) {
                case "SQR1":
                    waveMonSelected = false;
                    selectBtn(WaveConst.SQR1);
                    switch (Objects.requireNonNull(savedInstanceState.getString("prop_active"))) {
                        case "FREQUENCY":
                            waveMonSelected = false;
                            prop_active = WaveConst.FREQUENCY;
                            unit = getString(R.string.unit_hz);
                            activePropTv = pwmFreqValue;
                            pwmBtnFreq.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                            pwmBtnPhase.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                            pwmBtnDuty.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                            break;
                        case "PHASE":
                            waveMonSelected = false;
                            prop_active = WaveConst.PHASE;
                            unit = getString(R.string.deg_text);
                            activePropTv = pwmPhaseValue;
                            pwmBtnFreq.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                            pwmBtnPhase.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                            pwmBtnDuty.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                            break;
                        case "DUTY":
                            waveMonSelected = false;
                            prop_active = WaveConst.DUTY;
                            unit = getString(R.string.unit_percent);
                            activePropTv = pwmDutyValue;
                            pwmBtnFreq.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                            pwmBtnPhase.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                            pwmBtnDuty.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                            break;
                        default:
                            break;
                    }
                    break;
                case "SQR2":
                    selectBtn(WaveConst.SQR2);
                    switch (Objects.requireNonNull(savedInstanceState.getString("prop_active"))) {
                        case "FREQUENCY":
                            waveMonSelected = false;
                            prop_active = WaveConst.FREQUENCY;
                            unit = getString(R.string.unit_hz);
                            activePropTv = pwmFreqValue;
                            pwmBtnFreq.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                            pwmBtnPhase.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                            pwmBtnDuty.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                            break;
                        case "PHASE":
                            waveMonSelected = false;
                            prop_active = WaveConst.PHASE;
                            unit = getString(R.string.deg_text);
                            activePropTv = pwmPhaseValue;
                            pwmBtnFreq.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                            pwmBtnPhase.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                            pwmBtnDuty.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                            break;
                        case "DUTY":
                            waveMonSelected = false;
                            prop_active = WaveConst.DUTY;
                            unit = getString(R.string.unit_percent);
                            activePropTv = pwmDutyValue;
                            pwmBtnFreq.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                            pwmBtnPhase.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                            pwmBtnDuty.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                            break;
                        default:
                            break;
                    }
                    break;
                case "SQR3":
                    selectBtn(WaveConst.SQR3);
                    switch (Objects.requireNonNull(savedInstanceState.getString("prop_active"))) {
                        case "FREQUENCY":
                            waveMonSelected = false;
                            prop_active = WaveConst.FREQUENCY;
                            unit = getString(R.string.unit_hz);
                            activePropTv = pwmFreqValue;
                            pwmBtnFreq.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                            pwmBtnPhase.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                            pwmBtnDuty.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                            break;
                        case "PHASE":
                            waveMonSelected = false;
                            prop_active = WaveConst.PHASE;
                            unit = getString(R.string.deg_text);
                            activePropTv = pwmPhaseValue;
                            pwmBtnFreq.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                            pwmBtnPhase.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                            pwmBtnDuty.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                            break;
                        case "DUTY":
                            waveMonSelected = false;
                            prop_active = WaveConst.DUTY;
                            unit = getString(R.string.unit_percent);
                            activePropTv = pwmDutyValue;
                            pwmBtnFreq.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                            pwmBtnPhase.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                            pwmBtnDuty.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                            break;
                        default:
                            break;
                    }
                    break;
                case "SQR4":
                    selectBtn(WaveConst.SQR4);
                    switch (Objects.requireNonNull(savedInstanceState.getString("prop_active"))) {
                        case "FREQUENCY":
                            waveMonSelected = false;
                            prop_active = WaveConst.FREQUENCY;
                            unit = getString(R.string.unit_hz);
                            activePropTv = pwmFreqValue;
                            pwmBtnFreq.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                            pwmBtnPhase.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                            pwmBtnDuty.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                            break;
                        case "PHASE":
                            waveMonSelected = false;
                            prop_active = WaveConst.PHASE;
                            unit = getString(R.string.deg_text);
                            activePropTv = pwmPhaseValue;
                            pwmBtnFreq.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                            pwmBtnPhase.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                            pwmBtnDuty.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                            break;
                        case "DUTY":
                            waveMonSelected = false;
                            prop_active = WaveConst.DUTY;
                            unit = getString(R.string.unit_percent);
                            activePropTv = pwmDutyValue;
                            pwmBtnFreq.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                            pwmBtnPhase.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                            pwmBtnDuty.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }
        } else {
            enableInitialState();
        }
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
                selectWaveform(SIN);
            }
        });

        imgBtnTri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                btnCtrlFreq.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded, null));
                btnCtrlPhase.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
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
                btnCtrlFreq.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                btnCtrlPhase.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded, null));
            }
        });

        btnAnalogMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleDigitalMode(WaveConst.SQUARE);
            }
        });

        btnDigitalMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleDigitalMode(WaveConst.PWM);
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
                pwmBtnFreq.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded, null));
                pwmBtnPhase.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                pwmBtnDuty.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
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
                pwmBtnFreq.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                pwmBtnPhase.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded, null));
                pwmBtnDuty.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
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
                pwmBtnFreq.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                pwmBtnPhase.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                pwmBtnDuty.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded, null));
            }
        });

        monitorVariations(imgBtnUp, imgBtnDown);

        monitorLongClicks(imgBtnUp, imgBtnDown);

        seekBar.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {
                String valueText = formatWithUnit(seekParams.progress, unit);

                if (waveMonSelected) {
                    waveMonPropValueSelect.setText(valueText);
                } else {
                    pwmMonPropSelectValue.setText(valueText);
                }
                setValue();
            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {
                // Unused method override
            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
                // Unused method override
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

        activePropTv = prop_active == WaveConst.FREQUENCY ? waveFreqValue : wavePhaseValue;
    }

    public void saveWaveConfig() {
        long block = System.currentTimeMillis();
        csvLogger.prepareLogFile();
        csvLogger.writeMetaData(getResources().getString(R.string.wave_generator));
        long timestamp;
        double lat, lon;
        csvLogger.writeCSVFile(CSV_HEADER);
        recordSensorDataBlockID(new SensorDataBlock(block, getResources().getString(R.string.wave_generator)));
        double freq1 = (double) (WaveGeneratorConstants.wave.get(WaveConst.WAVE1).get(WaveConst.FREQUENCY));
        double freq2 = (double) WaveGeneratorConstants.wave.get(WaveConst.WAVE2).get(WaveConst.FREQUENCY);
        double phase = (double) WaveGeneratorConstants.wave.get(WaveConst.WAVE2).get(WaveConst.PHASE);

        String waveType1 = WaveGeneratorConstants.wave.get(WaveConst.WAVE1).get(WaveConst.WAVETYPE) == SIN ? "sine" : "tria";
        String waveType2 = WaveGeneratorConstants.wave.get(WaveConst.WAVE2).get(WaveConst.WAVETYPE) == SIN ? "sine" : "tria";

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
        String timeData = timestamp + "," + CSVLogger.FILE_NAME_FORMAT.format(new Date(timestamp));
        String locationData = lat + "," + lon;
        String dateTime = CSVLogger.FILE_NAME_FORMAT.format(new Date(timestamp));
        if (scienceLab.isConnected()) {
            if (digital_mode == WaveConst.SQUARE) {
                csvLogger.writeCSVFile(new CSVDataLine().add(timestamp).add(dateTime).add("Square").add("Wave1").add(waveType1).add(freq1).add(0).add(0).add(lat).add(lon)); //wave1
                recordSensorData(new WaveGeneratorData(timestamp, block, "Square", "Wave1", waveType1, String.valueOf(freq1), "0", "0", lat, lon));
                csvLogger.writeCSVFile(new CSVDataLine().add(timestamp).add(dateTime).add("Square").add("Wave2").add(waveType2).add(freq2).add(phase).add(0).add(lat).add(lon)); //wave2
                recordSensorData(new WaveGeneratorData(timestamp + 1, block, "Square", "Wave2", waveType2, String.valueOf(freq2), String.valueOf(phase), "0", lat, lon));
            } else {
                double freqSqr1 = (double) WaveGeneratorConstants.wave.get(WaveConst.SQR1).get(WaveConst.FREQUENCY);
                double dutySqr1 = (double) WaveGeneratorConstants.wave.get(WaveConst.SQR1).get(WaveConst.DUTY) / 100;
                double dutySqr2 = ((double) WaveGeneratorConstants.wave.get(WaveConst.SQR2).get(WaveConst.DUTY)) / 100;
                double phaseSqr2 = (double) WaveGeneratorConstants.wave.get(WaveConst.SQR2).get(WaveConst.PHASE) / 360;
                double dutySqr3 = ((double) WaveGeneratorConstants.wave.get(WaveConst.SQR3).get(WaveConst.DUTY)) / 100;
                double phaseSqr3 = (double) WaveGeneratorConstants.wave.get(WaveConst.SQR3).get(WaveConst.PHASE) / 360;
                double dutySqr4 = ((double) WaveGeneratorConstants.wave.get(WaveConst.SQR4).get(WaveConst.DUTY)) / 100;
                double phaseSqr4 = (double) WaveGeneratorConstants.wave.get(WaveConst.SQR4).get(WaveConst.PHASE) / 360;

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
                WaveGeneratorConstants.mode_selected = WaveConst.SQUARE;
                switch (data.getWave()) {
                    case "Wave1":
                        if (data.getShape().equals("sine")) {
                            WaveGeneratorConstants.wave.get(WaveConst.WAVE1).put(WaveConst.WAVETYPE, SIN);
                        } else {
                            WaveGeneratorConstants.wave.get(WaveConst.WAVE1).put(WaveConst.WAVETYPE, TRIANGULAR);
                        }
                        WaveGeneratorConstants.wave.get(WaveConst.WAVE1).put(WaveConst.FREQUENCY, Double.valueOf(data.getFreq()).intValue());
                        break;
                    case "Wave2":
                        if (data.getShape().equals("sine")) {
                            WaveGeneratorConstants.wave.get(WaveConst.WAVE2).put(WaveConst.WAVETYPE, SIN);
                        } else {
                            WaveGeneratorConstants.wave.get(WaveConst.WAVE2).put(WaveConst.WAVETYPE, TRIANGULAR);
                        }
                        WaveGeneratorConstants.wave.get(WaveConst.WAVE2).put(WaveConst.FREQUENCY, Double.valueOf(data.getFreq()).intValue());
                        WaveGeneratorConstants.wave.get(WaveConst.WAVE2).put(WaveConst.PHASE, Double.valueOf(data.getPhase()).intValue());
                        break;
                }
                enableInitialState();
            } else if (data.getMode().equals(MODE_PWM)) {
                WaveGeneratorConstants.mode_selected = WaveConst.PWM;
                switch (data.getWave()) {
                    case "Sq1":
                        WaveGeneratorConstants.wave.get(WaveConst.SQR1).put(WaveConst.FREQUENCY, Double.valueOf(data.getFreq()).intValue());
                        WaveGeneratorConstants.wave.get(WaveConst.SQR1).put(WaveConst.DUTY, ((Double) (Double.valueOf(data.getDuty()) * 100)).intValue());
                        break;
                    case "Sq2":
                        WaveGeneratorConstants.wave.get(WaveConst.SQR2).put(WaveConst.DUTY, ((Double) (Double.valueOf(data.getDuty()) * 100)).intValue());
                        WaveGeneratorConstants.wave.get(WaveConst.SQR2).put(WaveConst.PHASE, ((Double) (Double.valueOf(data.getPhase()) * 360)).intValue());
                        break;
                    case "Sq3":
                        WaveGeneratorConstants.wave.get(WaveConst.SQR3).put(WaveConst.DUTY, ((Double) (Double.valueOf(data.getDuty()) * 100)).intValue());
                        WaveGeneratorConstants.wave.get(WaveConst.SQR3).put(WaveConst.PHASE, ((Double) (Double.valueOf(data.getPhase()) * 360)).intValue());
                        break;
                    case "Sq4":
                        WaveGeneratorConstants.wave.get(WaveConst.SQR4).put(WaveConst.DUTY, ((Double) (Double.valueOf(data.getDuty()) * 100)).intValue());
                        WaveGeneratorConstants.wave.get(WaveConst.SQR4).put(WaveConst.PHASE, ((Double) (Double.valueOf(data.getPhase()) * 360)).intValue());
                        break;
                }
                enableInitialStatePWM();
            }
        }
    }

    private void setWave() {
        double freq1 = (double) (WaveGeneratorConstants.wave.get(WaveConst.WAVE1).get(WaveConst.FREQUENCY));
        double freq2 = (double) WaveGeneratorConstants.wave.get(WaveConst.WAVE2).get(WaveConst.FREQUENCY);
        double phase = (double) WaveGeneratorConstants.wave.get(WaveConst.WAVE2).get(WaveConst.PHASE);

        String waveType1 = WaveGeneratorConstants.wave.get(WaveConst.WAVE1).get(WaveConst.WAVETYPE) == SIN ? "sine" : "tria";
        String waveType2 = WaveGeneratorConstants.wave.get(WaveConst.WAVE2).get(WaveConst.WAVETYPE) == SIN ? "sine" : "tria";

        if (scienceLab.isConnected()) {
            if (digital_mode == WaveConst.SQUARE) {
                if (phase == WaveData.PHASE_MIN.getValue()) {
                    scienceLab.setSI1(freq1, waveType1);
                    scienceLab.setSI2(freq2, waveType2);
                } else {
                    scienceLab.setWaves(freq1, phase, freq2);
                }
            } else {
                double freqSqr1 = (double) WaveGeneratorConstants.wave.get(WaveConst.SQR1).get(WaveConst.FREQUENCY);
                double dutySqr1 = (double) WaveGeneratorConstants.wave.get(WaveConst.SQR1).get(WaveConst.DUTY) / 100;
                double dutySqr2 = ((double) WaveGeneratorConstants.wave.get(WaveConst.SQR2).get(WaveConst.DUTY)) / 100;
                double phaseSqr2 = (double) WaveGeneratorConstants.wave.get(WaveConst.SQR2).get(WaveConst.PHASE) / 360;
                double dutySqr3 = ((double) WaveGeneratorConstants.wave.get(WaveConst.SQR3).get(WaveConst.DUTY)) / 100;
                double phaseSqr3 = (double) WaveGeneratorConstants.wave.get(WaveConst.SQR3).get(WaveConst.PHASE) / 360;
                double dutySqr4 = ((double) WaveGeneratorConstants.wave.get(WaveConst.SQR4).get(WaveConst.DUTY)) / 100;
                double phaseSqr4 = (double) WaveGeneratorConstants.wave.get(WaveConst.SQR4).get(WaveConst.PHASE) / 360;

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
                if (produceSoundTask != null)
                    produceSoundTask.cancel(true);
                produceSoundTask = null;
                isPlayingSound = false;
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
                toggleGuide();
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

    public void selectBtn(WaveConst btn_selected) {

        switch (btn_selected) {

            case WAVE1:

                waveBtnActive = WaveConst.WAVE1;

                btnCtrlWave1.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded, null));
                btnCtrlWave2.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));

                btnCtrlPhase.setEnabled(false);  //disable phase for wave
                btnCtrlPhase.setVisibility(View.INVISIBLE);
                wavePhaseValue.setText("--");

                selectWaveform(WaveGeneratorConstants.wave.get(waveBtnActive).get(WaveConst.WAVETYPE));

                fetchPropertyValue(waveBtnActive, WaveConst.FREQUENCY, getString(R.string.unit_hz), waveFreqValue);
                wavePhaseTitle.setText(getResources().getString(R.string.text_phase_colon));
                break;

            case WAVE2:

                waveBtnActive = WaveConst.WAVE2;

                btnCtrlWave2.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded, null));
                btnCtrlWave1.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));

                btnCtrlPhase.setEnabled(true); // enable phase for wave2
                btnCtrlPhase.setVisibility(View.VISIBLE);

                selectWaveform(WaveGeneratorConstants.wave.get(waveBtnActive).get(WaveConst.WAVETYPE));

                fetchPropertyValue(waveBtnActive, WaveConst.FREQUENCY, getString(R.string.unit_hz), waveFreqValue);
                fetchPropertyValue(waveBtnActive, WaveConst.PHASE, getString(R.string.deg_text), wavePhaseValue);
                wavePhaseTitle.setText(getResources().getString(R.string.text_phase_colon));
                break;

            case SQR1:
                pwmBtnActive = WaveConst.SQR1;
                btnPwmSq1.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded, null));
                btnPwmSq2.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                btnPwmSq3.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                btnPwmSq4.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                pwmBtnPhase.setEnabled(false);  //phase disabled for sq1
                pwmBtnPhase.setVisibility(View.INVISIBLE);
                pwmPhaseValue.setText("--");
                fetchPropertyValue(pwmBtnActive, WaveConst.FREQUENCY, getString(R.string.unit_hz), pwmFreqValue);
                fetchPropertyValue(pwmBtnActive, WaveConst.DUTY, getString(R.string.unit_percent), pwmDutyValue);
                break;

            case SQR2:

                pwmBtnActive = WaveConst.SQR2;
                btnPwmSq1.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                btnPwmSq2.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded, null));
                btnPwmSq3.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                btnPwmSq4.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                pwmBtnPhase.setEnabled(true);
                pwmBtnPhase.setVisibility(View.VISIBLE);
                fetchPropertyValue(WaveConst.SQR1, WaveConst.FREQUENCY, getString(R.string.unit_hz), pwmFreqValue);
                fetchPropertyValue(pwmBtnActive, WaveConst.PHASE, getString(R.string.deg_text), pwmPhaseValue);
                fetchPropertyValue(pwmBtnActive, WaveConst.DUTY, getString(R.string.unit_percent), pwmDutyValue);
                break;

            case SQR3:

                pwmBtnActive = WaveConst.SQR3;
                btnPwmSq1.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                btnPwmSq2.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                btnPwmSq3.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded, null));
                btnPwmSq4.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                pwmBtnPhase.setEnabled(true);
                pwmBtnPhase.setVisibility(View.VISIBLE);
                fetchPropertyValue(WaveConst.SQR1, WaveConst.FREQUENCY, getString(R.string.unit_hz), pwmFreqValue);
                fetchPropertyValue(pwmBtnActive, WaveConst.PHASE, getString(R.string.deg_text), pwmPhaseValue);
                fetchPropertyValue(pwmBtnActive, WaveConst.DUTY, getString(R.string.unit_percent), pwmDutyValue);
                break;

            case SQR4:

                pwmBtnActive = WaveConst.SQR4;
                btnPwmSq1.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                btnPwmSq2.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                btnPwmSq3.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                btnPwmSq4.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded, null));
                pwmBtnPhase.setEnabled(true);
                pwmBtnPhase.setVisibility(View.VISIBLE);
                fetchPropertyValue(WaveConst.SQR1, WaveConst.FREQUENCY, getString(R.string.unit_hz), pwmFreqValue);
                fetchPropertyValue(pwmBtnActive, WaveConst.PHASE, getString(R.string.deg_text), pwmPhaseValue);
                fetchPropertyValue(pwmBtnActive, WaveConst.DUTY, getString(R.string.unit_percent), pwmDutyValue);
                break;

            default:
                waveBtnActive = WaveConst.WAVE1;
                btnCtrlWave1.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded, null));
                btnCtrlWave2.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                btnCtrlPhase.setEnabled(false);  //disable phase for wave
                pwmBtnPhase.setVisibility(View.INVISIBLE);
                wavePhaseValue.setText("--");
                selectWaveform(WaveGeneratorConstants.wave.get(waveBtnActive).get(WaveConst.WAVETYPE));
                fetchPropertyValue(waveBtnActive, WaveConst.FREQUENCY, getString(R.string.unit_hz), waveFreqValue);
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
                imgBtnSin.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded, null));
                imgBtnTri.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                WaveGeneratorConstants.wave.get(waveBtnActive).put(WaveConst.WAVETYPE, SIN);
                image = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_sin, null);
                break;

            case TRIANGULAR:
                waveFormText = getString(R.string.triangular);
                imgBtnSin.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
                imgBtnTri.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded, null));
                WaveGeneratorConstants.wave.get(waveBtnActive).put(WaveConst.WAVETYPE, TRIANGULAR);
                image = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_triangular, null);
                break;
            case PWM:
                waveFormText = getResources().getString(R.string.text_pwm);
                WaveGeneratorConstants.wave.get(waveBtnActive).put(WaveConst.WAVETYPE, PWM);
                image = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_pwm_pic, null);
                break;

            default:
                waveFormText = getString(R.string.sine);
                WaveGeneratorConstants.wave.get(waveBtnActive).put(WaveConst.WAVETYPE, SIN);
                image = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_sin, null);
        }
        selectedWaveText.setText(waveFormText);
        selectedWaveImg.setImageDrawable(image);
        previewWave();
    }

    private void toggleDigitalMode(WaveConst mode) {
        waveMonSelected = false;
        if (mode == WaveConst.SQUARE) {
            digital_mode = WaveConst.SQUARE;
            pwmModeLayout.setVisibility(View.GONE);
            pwmModeControls.setVisibility(View.GONE);
            squareModeLayout.setVisibility(View.VISIBLE);
            squareModeControls.setVisibility(View.VISIBLE);
            imgBtnSin.setEnabled(true);
            imgBtnTri.setEnabled(true);
            pwmSelectedModeImg.setImageResource(R.drawable.ic_square);
            pwmMonSelectMode.setText(getString(R.string.square));
            btnPwmSq2.setEnabled(false);
            btnPwmSq3.setEnabled(false);
            btnPwmSq4.setEnabled(false);
            pwmBtnPhase.setEnabled(false);
            btnDigitalMode.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
            btnAnalogMode.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded, null));

        } else {
            digital_mode = WaveConst.PWM;
            pwmModeLayout.setVisibility(View.VISIBLE);
            pwmModeControls.setVisibility(View.VISIBLE);
            squareModeLayout.setVisibility(View.GONE);
            squareModeControls.setVisibility(View.GONE);
            pwmSelectedModeImg.setImageResource(R.drawable.ic_pwm_pic);
            pwmMonSelectMode.setText(getString(R.string.text_pwm));
            btnPwmSq2.setEnabled(true);
            btnPwmSq3.setEnabled(true);
            btnPwmSq4.setEnabled(true);
            imgBtnSin.setEnabled(false);
            imgBtnTri.setEnabled(false);
            selectBtn(WaveConst.SQR2);
            btnDigitalMode.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded, null));
            btnAnalogMode.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_back_rounded_light, null));
        }
        WaveGeneratorConstants.mode_selected = mode;
        previewWave();
    }

    private void fetchPropertyValue(WaveConst btnActive, WaveConst property, String unit, TextView propTextView) {
        if (WaveGeneratorConstants.wave.get(btnActive).get(property) != null) {
            int value = WaveGeneratorConstants.wave.get(btnActive).get(property);
            propTextView.setText(formatWithUnit(value, unit));
        } else {
            if (property == WaveConst.FREQUENCY) {
                int value = WaveData.FREQ_MIN.getValue();
                propTextView.setText(formatWithUnit(value, unit));
            } else if (property == WaveConst.PHASE) {
                int value = WaveData.PHASE_MIN.getValue();
                propTextView.setText(formatWithUnit(value, unit));
            } else {
                int value = WaveData.DUTY_MIN.getValue();
                propTextView.setText(formatWithUnit(value, unit));
            }
        }
    }

    private void setSeekBar(IndicatorSeekBar seekBar) {

        int numTicks;

        switch (prop_active) {
            case FREQUENCY:
                seekMin = WaveData.FREQ_MIN.getValue();
                seekMax = WaveData.FREQ_MAX.getValue();
                numTicks = 50;
                leastCount = 1;
                break;

            case PHASE:
                seekMin = WaveData.PHASE_MIN.getValue();
                seekMax = WaveData.PHASE_MAX.getValue();
                numTicks = 50;
                leastCount = 1;
                break;

            case DUTY:
                seekMin = WaveData.DUTY_MIN.getValue();
                seekMax = WaveData.DUTY_MAX.getValue();
                numTicks = 50;
                leastCount = 1;
                unit = getString(R.string.unit_percent);
                break;

            default:
                seekMin = 0;
                seekMax = 5000;
                numTicks = 50;
                leastCount = 1;
        }

        seekBar.setMin(seekMin);
        seekBar.setMax(seekMax);
        seekBar.setTickCount(numTicks);

        if (!waveMonSelected) {
            waveMonPropSelect.setText("");
            waveMonPropValueSelect.setText("");

            if (prop_active.equals(WaveConst.FREQUENCY)) {
                if (WaveGeneratorConstants.wave.get(WaveConst.SQR1).get(prop_active) != null) {
                    seekBar.setProgress(WaveGeneratorConstants.wave.get(WaveConst.SQR1).get(prop_active));
                }
            } else {
                if (WaveGeneratorConstants.wave.get(pwmBtnActive).get(prop_active) != null) {
                    seekBar.setProgress(WaveGeneratorConstants.wave.get(pwmBtnActive).get(prop_active));
                }
            }
        } else {
            pwmMonPropSelect.setText("");
            pwmMonPropSelectValue.setText("");
            if (WaveGeneratorConstants.wave.get(waveBtnActive).get(prop_active) != null) {
                seekBar.setProgress(WaveGeneratorConstants.wave.get(waveBtnActive).get(prop_active));
            }
        }
        toggleSeekBtns(true);
    }

    private void incProgressSeekBar() {
        seekBar.setProgress(seekBar.getProgress() + leastCount);
    }

    private void decProgressSeekBar() {
        seekBar.setProgress(seekBar.getProgress() - leastCount);
    }

    private void setValue() {
        int value = seekBar.getProgress();

        if (!waveMonSelected) {
            if (prop_active == WaveConst.FREQUENCY) {
                WaveGeneratorConstants.wave.get(WaveConst.SQR1).put(prop_active, value);
            } else {
                if (prop_active == WaveConst.DUTY) {
                    if (value != WaveData.DUTY_MIN.getValue()) {
                        WaveGeneratorConstants.state.put(pwmBtnActive.toString(), 1);
                    } else
                        WaveGeneratorConstants.state.put(pwmBtnActive.toString(), 0);
                }
                WaveGeneratorConstants.wave.get(pwmBtnActive).put(prop_active, value);
            }
        } else {
            WaveGeneratorConstants.wave.get(waveBtnActive).put(prop_active, value);
        }
        setWave();
        previewWave();
        activePropTv.setText(formatWithUnit(value, unit));
    }

    private static String formatWithUnit(int value, String unit) {
        return value + ("\u00b0".equals(unit) ? "" : " ") + unit;
    }

    private void previewWave() {
        List<ILineDataSet> dataSets = new ArrayList<>();
        ArrayList<Entry> entries = getSamplePoints(false);
        ArrayList<Entry> refEntries = getSamplePoints(true);
        LineDataSet dataSet;
        LineDataSet refDataSet;
        if (WaveGeneratorConstants.mode_selected == WaveConst.PWM) {
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
        if (WaveGeneratorConstants.mode_selected == WaveConst.PWM) {
            double freq = (double) WaveGeneratorConstants.wave.get(WaveConst.SQR1).get(WaveConst.FREQUENCY);
            double duty = ((double) WaveGeneratorConstants.wave.get(pwmBtnActive).get(WaveConst.DUTY)) / 100;
            double phase = 0;
            if (pwmBtnActive != WaveConst.SQR1 && !isReference) {
                phase = (double) WaveGeneratorConstants.wave.get(pwmBtnActive).get(WaveConst.PHASE);
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
            int shape = WaveGeneratorConstants.wave.get(waveBtnActive).get(WaveConst.WAVETYPE);
            double freq = (double) WaveGeneratorConstants.wave.get(waveBtnActive).get(WaveConst.FREQUENCY);

            if (waveBtnActive != WaveConst.WAVE1 && !isReference) {
                phase = (double) WaveGeneratorConstants.wave.get(WaveConst.WAVE2).get(WaveConst.PHASE);
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

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("waveBtnActive", String.valueOf(waveBtnActive));
        outState.putString("pwmBtnActive", String.valueOf(pwmBtnActive));
        outState.putString("prop_active", String.valueOf(prop_active));
        outState.putString("digital_mode", String.valueOf(digital_mode));
        super.onSaveInstanceState(outState);
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
                incProgressSeekBar();
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
                decProgressSeekBar();
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
                            incProgressSeekBar();
                        } else {
                            decProgressSeekBar();
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
            float[] samples = new float[1024];

            track.play();
            double frequency;
            while (isPlayingSound) {
                if (WaveGeneratorConstants.mode_selected == WaveConst.SQUARE) {
                    frequency = WaveGeneratorConstants.wave.get(waveBtnActive).get(WaveConst.FREQUENCY);
                } else {
                    frequency = WaveGeneratorConstants.wave.get(WaveConst.SQR1).get(WaveConst.FREQUENCY);
                }
                float increment = (float) ((2 * Math.PI) * frequency / 44100);
                for (int i = 0; i < samples.length; i++) {
                    samples[i] = (float) Math.sin(angle);
                    if (WaveGeneratorConstants.mode_selected == WaveConst.PWM) {
                        samples[i] = (samples[i] >= 0.0) ? 1 : -1;
                    } else {
                        if (WaveGeneratorConstants.wave.get(waveBtnActive).get(WaveConst.WAVETYPE) == 2) {
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (produceSoundTask != null)
            produceSoundTask.cancel(true);
        produceSoundTask = null;
        isPlayingSound = false;
    }
}
