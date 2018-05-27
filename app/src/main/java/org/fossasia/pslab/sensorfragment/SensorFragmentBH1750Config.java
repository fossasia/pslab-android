package org.fossasia.pslab.sensorfragment;

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

public class SensorFragmentBH1750Config extends Fragment {

    EditText highLimit;
    EditText updatePeriod;
    SeekBar highLimitSeek;
    SeekBar updatePeriodSeek;
    Spinner gainValue;
    Spinner selectSensor;
    CardView gainRangeCard;
    private static ScienceLab scienceLab;
    private static BH1750 bh1750;

    public static ScienceLab getScienceLab() {
        return scienceLab;
    }

    public static BH1750 getBh1750() {
        return bh1750;
    }

    public static int getHighValue() {
        return highValue;
    }

    public static int getUpdatePeriodValue() {
        return updatePeriodValue;
    }

    final int highLimitMax = 1000;
    final int updatePeriodMax = 980;
    private static int highValue = 0;
    private static int updatePeriodValue = 20;
    public SensorFragmentBH1750Config() {
        // Required empty public constructor
    }

    public static SensorFragmentBH1750Config newInstance() {
        return new SensorFragmentBH1750Config();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sensor_bh1750_config, container, false);

        highLimit = (EditText) view.findViewById(R.id.bh1750_hight_limit_text);
        highLimitSeek = (SeekBar) view.findViewById(R.id.bh1750_hight_limit_seekbar);
        updatePeriod = (EditText) view.findViewById(R.id.bh1750_update_period_text);
        updatePeriodSeek = (SeekBar) view.findViewById(R.id.bh1750_update_period_seekbar);
        gainValue = (Spinner) view.findViewById(R.id.spinner_bh1750_gain);
        selectSensor = (Spinner) view.findViewById(R.id.spinner_select_light);
        gainRangeCard = (CardView) view.findViewById(R.id.cardview_gain_range);

        highLimitSeek.setMax(highLimitMax);
        updatePeriodSeek.setMax(updatePeriodMax);

        highLimit.setText(String.format("%d", highValue));
        highLimitSeek.setProgress(highValue);
        updatePeriod.setText(String.format("%d" ,updatePeriodValue+20));
        updatePeriodSeek.setProgress(updatePeriodValue+20);

        highLimitSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                highValue = progress;
                highLimit.setText(String.format("%d", progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        updatePeriodSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updatePeriodValue = progress+20;
                updatePeriod.setText(""+updatePeriodValue);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

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

            }
        });

        selectSensor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                 switch (position){
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
                 }
                 SensorFragmentBH1750Data.sensorChanged(position);


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return view;
    }

}
