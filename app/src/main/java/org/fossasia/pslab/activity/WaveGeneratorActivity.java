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

    //views on the monitor
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

    //buttons on controlling panel
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
    public static final int SIN = 1;
    public static final int TRIANGULAR = 2;
    public static final int SQUARE = 3;
    public static final String WAVETYPE = "wavetype";
    public static final String WAVE1 = "wave1";
    public static final String WAVE2 = "wave2";
    public static final String FREQUENCY = "frequency";
    public static final String PHASE = "phase";
    public static final String DUTY = "duty";
    private static String btn_Active = "";
    private static String prop_active = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wave_generator);
        ButterKnife.bind(this);
        new WaveGeneratorCommon();

        selectWave1();    //on starting wave1 will be selected

        btnCtrlWave1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!btn_Active.equals(WAVE1)) {
                    selectWave1();
                }
            }
        });

        btnCtrlWave2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!btn_Active.equals(WAVE2)) {
                    selectWave2();
                }
            }
        });

        imgBtnSin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WaveGeneratorCommon.wave.get(btn_Active).put(WAVETYPE, SIN);
                selectWaveform(SIN);
            }
        });

        imgBtnSq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WaveGeneratorCommon.wave.get(btn_Active).put(WAVETYPE, SQUARE);
                selectWaveform(SQUARE);
            }
        });

        imgBtnTri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WaveGeneratorCommon.wave.get(btn_Active).put(WAVETYPE, TRIANGULAR);
                selectWaveform(TRIANGULAR);
            }
        });

        btnCtrlFreq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prop_active = FREQUENCY;
                unit = getString(R.string.unit_hz);
                waveMonPropSelect.setText(getString(R.string.wave_frequency));
                setSeekBar(seekBar);
            }
        });

        btnCtrlPhase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prop_active = PHASE;
                unit = getString(R.string.deg_text);
                waveMonPropSelect.setText(getString(R.string.phase_offset));
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
                waveMonPropValueSelect.setText(valueText);
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


    private void selectWave1() {
        btn_Active = "wave1";

        waveMonWave1.setEnabled(true);
        waveMonWave2.setEnabled(false);

        btnCtrlPhase.setEnabled(false);  //disable phase for wave
        wavePhaseValue.setText("--");

        disableSeekBtns();

        selectWaveform(WaveGeneratorCommon.wave.get(btn_Active).get(WAVETYPE));

        Double value = (double) WaveGeneratorCommon.wave.get(btn_Active).get(FREQUENCY);
        String valueText = value + " " + getString(R.string.unit_hz);
        waveFreqValue.setText(valueText);
    }


    private void selectWave2() {
        btn_Active = WAVE2;

        waveMonWave1.setEnabled(false);
        waveMonWave2.setEnabled(true);

        btnCtrlPhase.setEnabled(true); // enable phase for wave2

        disableSeekBtns();

        selectWaveform(WaveGeneratorCommon.wave.get(btn_Active).get(WAVETYPE));
        Double freqValue = (double) WaveGeneratorCommon.wave.get(btn_Active).get(FREQUENCY);
        String freqValueText = freqValue + " " + getString(R.string.unit_hz);
        waveFreqValue.setText(freqValueText);

        Double value = (double) WaveGeneratorCommon.wave.get(btn_Active).get(PHASE);
        String valueText = value + " " + getString(R.string.deg_text);
        wavePhaseValue.setText(valueText);
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
                seekMin = 10;
                seekMax = 5000;
                numTicks = 100;
                leastCount = 1;
                break;

            case PHASE:
                seekMin = 0;
                seekMax = 360;
                numTicks = 73;
                leastCount = 1;
                break;

            case DUTY:
                seekMin = 0;
                seekMax = 100;
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
        seekBar.setProgress(WaveGeneratorCommon.wave.get(btn_Active).get(prop_active));
        enableSeekBtns();
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
        WaveGeneratorCommon.wave.get(btn_Active).put(prop_active, value);
        Double dValue = (double) value;
        String valueText = String.valueOf(dValue) + " " + unit;
        if (prop_active.equals(FREQUENCY)) {
            waveFreqValue.setText(valueText);
        } else if (prop_active.equals(PHASE)) {
            wavePhaseValue.setText(valueText);
        }
    }

    private void disableSeekBtns() {
        waveMonPropSelect.setText("");
        waveMonPropValueSelect.setText("");
        imgBtnUp.setEnabled(false);
        imgBtnDown.setEnabled(false);
        seekBar.setEnabled(false);
        btnSet.setEnabled(false);
    }

    private void enableSeekBtns() {
        imgBtnDown.setEnabled(true);
        imgBtnUp.setEnabled(true);
        btnSet.setEnabled(true);
        seekBar.setEnabled(true);
    }
}