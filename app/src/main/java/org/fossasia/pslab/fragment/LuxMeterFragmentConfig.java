package org.fossasia.pslab.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;

import org.fossasia.pslab.R;
import org.fossasia.pslab.communication.ScienceLab;
import org.fossasia.pslab.communication.peripherals.I2C;
import org.fossasia.pslab.communication.sensors.BH1750;
import org.fossasia.pslab.others.ScienceLabCommon;

import java.io.IOException;

public class LuxMeterFragmentConfig extends Fragment {

    private EditText highLimit;
    private EditText updatePeriod;
    private SeekBar highLimitSeek;
    private SeekBar updatePeriodSeek;
    private Spinner gainValue;
    private Spinner selectSensor;
    private CardView gainRangeCard;

    final int highLimitMax = 1000;
    final int updatePeriodMax = 980;
    private static int highValue = 0;
    private static int updatePeriodValue = 20;

    private static ScienceLab scienceLab;
    private static BH1750 bh1750;

    public static LuxMeterFragmentConfig newInstance() {
        return new LuxMeterFragmentConfig();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lux_meter_config, container, false);

        highLimit = (EditText) view.findViewById(R.id.lux_hight_limit_text);
        highLimitSeek = (SeekBar) view.findViewById(R.id.lux_hight_limit_seekbar);
        updatePeriod = (EditText) view.findViewById(R.id.lux_update_period_text);
        updatePeriodSeek = (SeekBar) view.findViewById(R.id.lux_update_period_seekbar);
        gainValue = (Spinner) view.findViewById(R.id.spinner_bh1750_gain);
        selectSensor = (Spinner) view.findViewById(R.id.spinner_select_light);
        gainRangeCard = (CardView) view.findViewById(R.id.cardview_gain_range);

        highLimitSeek.setMax(highLimitMax);
        updatePeriodSeek.setMax(updatePeriodMax);

        highLimit.setText(String.format("%d", highValue));
        highLimitSeek.setProgress(highValue);
        updatePeriod.setText(String.format("%d", updatePeriodValue + 20));
        updatePeriodSeek.setProgress(updatePeriodValue + 20);

        highLimitSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                highValue = progress;
                highLimit.setText(String.format("%d", progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //do nothing
            }
        });

        updatePeriodSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updatePeriodValue = progress + 20;
                updatePeriod.setText("" + updatePeriodValue);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //do nothing
            }
        });

        gainValue.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    if (bh1750 != null) {
                        bh1750.setRange(gainValue.getSelectedItem().toString());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //do nothing
            }
        });

        selectSensor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        gainRangeCard.setFocusable(false);
                        break;

                     case 1:
                         gainRangeCard.setFocusable(true);
                         scienceLab = ScienceLabCommon.scienceLab;
                         I2C i2c = scienceLab.i2c;

                        try {
                            bh1750 = new BH1750(i2c);
                        } catch (InterruptedException | IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //do nothing
            }
        });

        return view;
    }
}
