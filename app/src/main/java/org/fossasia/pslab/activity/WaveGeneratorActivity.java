package org.fossasia.pslab.activity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.warkiz.widget.IndicatorSeekBar;

import org.fossasia.pslab.R;
import org.fossasia.pslab.others.WaveGeneratorCommon;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WaveGeneratorActivity extends AppCompatActivity {

    //waveform monitor
    @BindView(R.id.wave_mon_wave1)
    TextView waveMonWave1;
    @BindView(R.id.wave_mon_wave2)
    TextView waveMonWave2;
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
    @BindView(R.id.pwm_mon_select_pin)
    TextView pwmMonSelectPin;
    @BindView(R.id.pwm_mon_sq1)
    TextView pwmMonSqr1;
    @BindView(R.id.pwm_mon_sq2)
    TextView pwmMonSqr2;
    @BindView(R.id.pwm_mon_sq3)
    TextView pwmMonSqr3;
    @BindView(R.id.pwm_mon_sq4)
    TextView pwmMonSqr4;
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
    @BindView(R.id.ctrl_img_btn_sq)
    ImageButton imgBtnSq;
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
    @BindView(R.id.btn_set)
    Button btnSet;

    private int leastCount, seekMax, seekMin;
    private String unit;

    //const values
    public static final int FREQ_MIN = 10;
    public static final int DUTY_MIN = 0;
    public static final int PHASE_MIN = 0;

    public static final int FREQ_MAX = 5000;
    public static final int DUTY_MAX = 100;
    public static final int PHASE_MAX = 360;

    public static final int SIN = 1;
    public static final int TRIANGULAR = 2;
    public static final int SQUARE = 3;
    public static final String WAVETYPE = "wavetype";
    public static final String WAVE1 = "wave1";
    public static final String WAVE2 = "wave2";
    public static final String SQ1 = "sq1";
    public static final String SQ2 = "sq2";
    public static final String SQ3 = "sq3";
    public static final String SQ4 = "sq4";
    public static final String FREQUENCY = "frequency";
    public static final String PHASE = "phase";
    public static final String DUTY = "duty";

    private static String waveBtnActive = "";
    private static String pwmBtnActive = "";
    private static String prop_active = "";
    private static boolean waveMonSelected;
    private TextView activePropTv = null;
    private TextView activePwmPinTv = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wave_generator);
        ButterKnife.bind(this);
        new WaveGeneratorCommon();

        enableInitialState();//on starting wave1 and sq1 will be selected

        //wave panel
        btnCtrlWave1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!waveBtnActive.equals(WAVE1)) {
                    waveMonSelected = true;
                    selectBtn(WAVE1);
                }
            }
        });

        btnCtrlWave2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!waveBtnActive.equals(WAVE2)) {
                    waveMonSelected = true;
                    selectBtn(WAVE2);
                }
            }
        });

        imgBtnSin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WaveGeneratorCommon.wave.get(waveBtnActive).put(WAVETYPE, SIN);
                selectWaveform(SIN);
            }
        });

        imgBtnSq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WaveGeneratorCommon.wave.get(waveBtnActive).put(WAVETYPE, SQUARE);
                selectWaveform(SQUARE);
            }
        });

        imgBtnTri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WaveGeneratorCommon.wave.get(waveBtnActive).put(WAVETYPE, TRIANGULAR);
                selectWaveform(TRIANGULAR);
            }
        });

        btnCtrlFreq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                waveMonSelected = true;
                prop_active = FREQUENCY;
                unit = getString(R.string.unit_hz);
                activePropTv = waveFreqValue;
                waveMonPropSelect.setText(getString(R.string.wave_frequency));
                setSeekBar(seekBar);
            }
        });

        btnCtrlPhase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                waveMonSelected = true;
                prop_active = PHASE;
                unit = getString(R.string.deg_text);
                activePropTv = wavePhaseValue;
                waveMonPropSelect.setText(getString(R.string.phase_offset));
                setSeekBar(seekBar);
            }
        });

        //pwm panel
        btnPwmSq1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!pwmBtnActive.equals(SQ1)) {
                    waveMonSelected = false;
                    selectBtn(SQ1);
                }
            }
        });

        btnPwmSq2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!pwmBtnActive.equals(SQ2)) {
                    waveMonSelected = false;
                    selectBtn(SQ2);
                }
            }
        });

        btnPwmSq3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!pwmBtnActive.equals(SQ3)) {
                    waveMonSelected = false;
                    selectBtn(SQ3);
                }
            }
        });

        btnPwmSq4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!pwmBtnActive.equals(SQ4)) {
                    waveMonSelected = false;
                    selectBtn(SQ4);
                }
            }
        });

        pwmBtnFreq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                waveMonSelected = false;
                prop_active = FREQUENCY;
                unit = getString(R.string.unit_hz);
                activePropTv = pwmFreqValue;
                pwmMonPropSelect.setText(getString(R.string.pwm_frequecy));
                setSeekBar(seekBar);
            }
        });

        pwmBtnPhase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                waveMonSelected = false;
                prop_active = PHASE;
                unit = getString(R.string.deg_text);
                activePropTv = pwmPhaseValue;
                pwmMonPropSelect.setText(getString(R.string.pwm_phase));
                setSeekBar(seekBar);
            }
        });

        pwmBtnDuty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                waveMonSelected = false;
                prop_active = DUTY;
                unit = getString(R.string.unit_percent);
                activePropTv = pwmDutyValue;
                pwmMonPropSelect.setText(getString(R.string.pwm_duty));
                setSeekBar(seekBar);
            }
        });

        imgBtnUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                incProgressSeekBar(seekBar);
            }
        });

        imgBtnDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                decProgressSeekBar(seekBar);
            }
        });

        btnSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setValue();
            }
        });

        seekBar.setOnSeekChangeListener(new IndicatorSeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(IndicatorSeekBar seekBar, int progress, float progressFloat, boolean fromUserTouch) {
                String valueText = String.valueOf((float) progress) + " " + unit;
                if (waveMonSelected) {
                    waveMonPropValueSelect.setText(valueText);
                } else {
                    pwmMonPropSelectValue.setText(valueText);
                }
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
    }

    public void selectBtn(String btn_selected) {

        Double value;
        String valueText;

        switch (btn_selected) {

            case WAVE1:

                waveBtnActive = WAVE1;
                waveMonWave1.setEnabled(true);
                waveMonWave2.setEnabled(false);

                btnCtrlPhase.setEnabled(false);  //disable phase for wave
                wavePhaseValue.setText("--");

                selectWaveform(WaveGeneratorCommon.wave.get(waveBtnActive).get(WAVETYPE));

                value = (double) WaveGeneratorCommon.wave.get(waveBtnActive).get(FREQUENCY);
                valueText = value + " " + getString(R.string.unit_hz);
                waveFreqValue.setText(valueText);
                break;

            case WAVE2:

                waveBtnActive = WAVE2;
                waveMonWave1.setEnabled(false);
                waveMonWave2.setEnabled(true);

                btnCtrlPhase.setEnabled(true); // enable phase for wave2

                selectWaveform(WaveGeneratorCommon.wave.get(waveBtnActive).get(WAVETYPE));

                value = (double) WaveGeneratorCommon.wave.get(waveBtnActive).get(FREQUENCY);
                valueText = value + " " + getString(R.string.unit_hz);
                waveFreqValue.setText(valueText);

                value = (double) WaveGeneratorCommon.wave.get(waveBtnActive).get(PHASE);
                valueText = value + " " + getString(R.string.deg_text);
                wavePhaseValue.setText(valueText);
                break;

            case SQ1:

                activePwmPinTv.setEnabled(false);
                pwmBtnActive = SQ1;
                activePwmPinTv = pwmMonSqr1;
                activePwmPinTv.setEnabled(true);
                pwmMonSelectPin.setText(getString(R.string.text_sq1));
                pwmBtnPhase.setEnabled(false);  //phase disabled for sq1
                pwmPhaseValue.setText("--");

                value = (double) WaveGeneratorCommon.wave.get(pwmBtnActive).get(FREQUENCY);
                valueText = value + " " + getString(R.string.unit_hz);
                pwmFreqValue.setText(valueText);

                value = (double) WaveGeneratorCommon.wave.get(pwmBtnActive).get(DUTY);
                valueText = value + " " + getString(R.string.unit_percent);
                pwmDutyValue.setText(valueText);
                break;


            case SQ2:

                activePwmPinTv.setEnabled(false);
                pwmBtnActive = SQ2;
                activePwmPinTv = pwmMonSqr2;
                activePwmPinTv.setEnabled(true);
                pwmMonSelectPin.setText(getString(R.string.text_sq2));
                pwmBtnPhase.setEnabled(true);

                value = (double) WaveGeneratorCommon.wave.get(SQ1).get(FREQUENCY);
                valueText = value + " " + getString(R.string.unit_hz);
                pwmFreqValue.setText(valueText);

                value = (double) WaveGeneratorCommon.wave.get(pwmBtnActive).get(DUTY);
                valueText = value + " " + getString(R.string.unit_percent);
                pwmDutyValue.setText(valueText);

                value = (double) WaveGeneratorCommon.wave.get(pwmBtnActive).get(PHASE);
                valueText = value + " " + getString(R.string.deg_text);
                pwmPhaseValue.setText(valueText);
                break;

            case SQ3:

                activePwmPinTv.setEnabled(false);
                pwmBtnActive = SQ3;
                activePwmPinTv = pwmMonSqr3;
                activePwmPinTv.setEnabled(true);
                pwmMonSelectPin.setText(getString(R.string.text_sq3));
                pwmBtnPhase.setEnabled(true);


                value = (double) WaveGeneratorCommon.wave.get(SQ1).get(FREQUENCY);
                valueText = value + " " + getString(R.string.unit_hz);
                pwmFreqValue.setText(valueText);

                value = (double) WaveGeneratorCommon.wave.get(pwmBtnActive).get(DUTY);
                valueText = value + " " + getString(R.string.unit_percent);
                pwmDutyValue.setText(valueText);

                value = (double) WaveGeneratorCommon.wave.get(pwmBtnActive).get(PHASE);
                valueText = value + " " + getString(R.string.deg_text);
                pwmPhaseValue.setText(valueText);
                break;

            case SQ4:

                activePwmPinTv.setEnabled(false);
                pwmBtnActive = SQ4;
                activePwmPinTv = pwmMonSqr4;
                activePwmPinTv.setEnabled(true);
                pwmMonSelectPin.setText(getString(R.string.text_sq4));
                pwmBtnPhase.setEnabled(true);

                value = (double) WaveGeneratorCommon.wave.get(SQ1).get(FREQUENCY);
                valueText = value + " " + getString(R.string.unit_hz);
                pwmFreqValue.setText(valueText);

                value = (double) WaveGeneratorCommon.wave.get(pwmBtnActive).get(DUTY);
                valueText = value + " " + getString(R.string.unit_percent);
                pwmDutyValue.setText(valueText);

                value = (double) WaveGeneratorCommon.wave.get(pwmBtnActive).get(PHASE);
                valueText = value + " " + getString(R.string.deg_text);
                pwmPhaseValue.setText(valueText);
                break;

            default:
                waveBtnActive = WAVE1;
                waveMonWave1.setEnabled(true);
                waveMonWave2.setEnabled(false);

                btnCtrlPhase.setEnabled(false);  //disable phase for wave
                wavePhaseValue.setText("--");

                selectWaveform(WaveGeneratorCommon.wave.get(waveBtnActive).get(WAVETYPE));

                value = (double) WaveGeneratorCommon.wave.get(waveBtnActive).get(FREQUENCY);
                valueText = value + " " + getString(R.string.unit_hz);
                waveFreqValue.setText(valueText);
                break;
        }
        prop_active = "";
        toggleSeekBtns(false);
    }

    private void selectWaveform(int waveType) {
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

            case SQUARE:
                waveFormText = getString(R.string.square);
                image = getResources().getDrawable(R.drawable.ic_square);
                break;
            default:
                waveFormText = getString(R.string.sine);
                image = getResources().getDrawable(R.drawable.ic_sin);
        }
        selectedWaveText.setText(waveFormText);
        selectedWaveImg.setImageDrawable(image);
    }

    private void setSeekBar(IndicatorSeekBar seekBar) {

        int numTicks;

        switch (prop_active) {
            case FREQUENCY:
                seekMin = FREQ_MIN;
                seekMax = FREQ_MAX;
                numTicks = 100;
                leastCount = 1;
                break;

            case PHASE:
                seekMin = PHASE_MIN;
                seekMax = PHASE_MAX;
                numTicks = 73;
                leastCount = 1;
                break;

            case DUTY:
                seekMin = DUTY_MIN;
                seekMax = DUTY_MAX;
                numTicks = 11;
                leastCount = 10;
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
            if (prop_active.equals(FREQUENCY)) {
                seekBar.setProgress(WaveGeneratorCommon.wave.get(SQ1).get(prop_active));
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
            Toast.makeText(this, R.string.max_val_warning, Toast.LENGTH_SHORT).show();
        }
        seekBar.setProgress(value);
    }

    private void decProgressSeekBar(IndicatorSeekBar seekBar) {
        Integer value = seekBar.getProgress();
        value = value - leastCount;
        if (value < seekMin) {
            value = seekMin;
            Toast.makeText(this, R.string.min_val_warning, Toast.LENGTH_SHORT).show();
        }
        seekBar.setProgress(value);
    }

    private void setValue() {
        Integer value = seekBar.getProgress();

        if (!waveMonSelected) {
            if (prop_active.equals(FREQUENCY)) {
                WaveGeneratorCommon.wave.get(SQ1).put(prop_active, value);
            } else {
                WaveGeneratorCommon.wave.get(pwmBtnActive).put(prop_active, value);
            }
        } else {
            WaveGeneratorCommon.wave.get(waveBtnActive).put(prop_active, value);
        }
        Double dValue = (double) value;
        String valueText = String.valueOf(dValue) + " " + unit;
        activePropTv.setText(valueText);
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
        btnSet.setEnabled(state);
    }

    private void enableInitialState() {
        selectBtn(WAVE1);
        activePwmPinTv = pwmMonSqr1;
        selectBtn(SQ1);
    }
}