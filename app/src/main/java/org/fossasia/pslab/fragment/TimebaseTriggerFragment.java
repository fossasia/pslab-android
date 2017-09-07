package org.fossasia.pslab.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import org.fossasia.pslab.R;
import org.fossasia.pslab.activity.OscilloscopeActivity;
import org.fossasia.pslab.others.FloatSeekBar;

public class TimebaseTriggerFragment extends Fragment {

    private Spinner spinnerTriggerChannelSelect;
    private FloatSeekBar seekBarTimebase;
    private FloatSeekBar seekBarTrigger;
    private TextView textViewTimeBase;
    private TextView textViewTrigger;
    private CheckBox checkBoxTrigger;


    public static TimebaseTriggerFragment newInstance() {
        return new TimebaseTriggerFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_timebase_tigger, container, false);

        //seekBarTimebase = (SeekBar) v.findViewById(R.id.seekBar_timebase_tt);
        seekBarTimebase = (FloatSeekBar) v.findViewById(R.id.seekBar_timebase_tt);
        seekBarTrigger = (FloatSeekBar) v.findViewById(R.id.seekBar_trigger);
        textViewTimeBase = (TextView) v.findViewById(R.id.tv_timebase_values_tt);
        textViewTrigger = (TextView) v.findViewById(R.id.tv_trigger_values_tt);
        spinnerTriggerChannelSelect = (Spinner) v.findViewById(R.id.spinner_trigger_channel_tt);
        checkBoxTrigger = (CheckBox) v.findViewById(R.id.checkbox_trigger_tt);

        boolean tabletSize = getResources().getBoolean(R.bool.isTablet);

        if (tabletSize) {
            textViewTrigger.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            textViewTimeBase.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        }

        seekBarTimebase.setMax(8);
        seekBarTimebase.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //samples are in the power of 2 so that sinefit can be applied
                switch (progress) {
                    case 0:
                        textViewTimeBase.setText(R.string.timebase_label_875);
                        ((OscilloscopeActivity) getActivity()).setXAxisScale(875);
                        ((OscilloscopeActivity) getActivity()).timebase = 875;
                        ((OscilloscopeActivity) getActivity()).samples = 512;
                        ((OscilloscopeActivity) getActivity()).timeGap = 2;
                        break;
                    case 1:
                        textViewTimeBase.setText(R.string.timebase_label_1);
                        ((OscilloscopeActivity) getActivity()).setXAxisScale(1);
                        ((OscilloscopeActivity) getActivity()).timebase = 1000;
                        ((OscilloscopeActivity) getActivity()).samples = 512;
                        ((OscilloscopeActivity) getActivity()).timeGap = 2;
                        break;
                    case 2:
                        textViewTimeBase.setText(R.string.timebase_label_2);
                        ((OscilloscopeActivity) getActivity()).setXAxisScale(2);
                        ((OscilloscopeActivity) getActivity()).timebase = 2000;
                        ((OscilloscopeActivity) getActivity()).samples = 512;
                        ((OscilloscopeActivity) getActivity()).timeGap = 4;
                        break;
                    case 3:
                        textViewTimeBase.setText(R.string.timebase_label_4);
                        ((OscilloscopeActivity) getActivity()).setXAxisScale(4);
                        ((OscilloscopeActivity) getActivity()).timebase = 4000;
                        ((OscilloscopeActivity) getActivity()).samples = 512;
                        ((OscilloscopeActivity) getActivity()).timeGap = 8;
                        break;
                    case 4:
                        textViewTimeBase.setText(R.string.timebase_label_8);
                        ((OscilloscopeActivity) getActivity()).setXAxisScale(8);
                        ((OscilloscopeActivity) getActivity()).timebase = 8000;
                        ((OscilloscopeActivity) getActivity()).samples = 1024;
                        ((OscilloscopeActivity) getActivity()).timeGap = 8;
                        break;
                    case 5:
                        textViewTimeBase.setText(R.string.timebase_label_25_60);
                        ((OscilloscopeActivity) getActivity()).setXAxisScale(25.60);
                        ((OscilloscopeActivity) getActivity()).timebase = 25600;
                        ((OscilloscopeActivity) getActivity()).samples = 1024;
                        ((OscilloscopeActivity) getActivity()).timeGap = 25;
                        break;
                    case 6:
                        textViewTimeBase.setText(R.string.timebase_label_38_40);
                        ((OscilloscopeActivity) getActivity()).setXAxisScale(38.40);
                        ((OscilloscopeActivity) getActivity()).timebase = 38400;
                        ((OscilloscopeActivity) getActivity()).timebase = 1024;
                        ((OscilloscopeActivity) getActivity()).timeGap = 38;
                        break;
                    case 7:
                        textViewTimeBase.setText(R.string.timebase_label_51_20);
                        ((OscilloscopeActivity) getActivity()).setXAxisScale(51.20);
                        ((OscilloscopeActivity) getActivity()).timebase = 51200;
                        ((OscilloscopeActivity) getActivity()).samples = 1024;
                        ((OscilloscopeActivity) getActivity()).timeGap = 50;
                        break;
                    case 8:
                        textViewTimeBase.setText(R.string.timebase_label_102_40);
                        ((OscilloscopeActivity) getActivity()).setXAxisScale(102.40);
                        ((OscilloscopeActivity) getActivity()).timebase = 102400;
                        ((OscilloscopeActivity) getActivity()).samples = 1024;
                        ((OscilloscopeActivity) getActivity()).timeGap = 100;
                        break;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBarTimebase.setProgress(0);

        seekBarTrigger.setters(-16.5, 16.5);
        seekBarTrigger.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textViewTrigger.setText(String.format("%s V", seekBarTrigger.getValue()));
                ((OscilloscopeActivity) getActivity()).trigger = seekBarTrigger.getValue();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBarTrigger.setProgress(50);

        String[] channels = {"CH1", "CH2", "CH3", "MIC"};
        ArrayAdapter<String> channelsAdapter;
        if (tabletSize) {
            channelsAdapter = new ArrayAdapter<>(this.getActivity(), R.layout.custom_spinner_tablet, channels);
        } else {
            channelsAdapter = new ArrayAdapter<>(this.getActivity(), R.layout.custom_spinner, channels);
        }

        channelsAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        spinnerTriggerChannelSelect.setAdapter(channelsAdapter);
        spinnerTriggerChannelSelect.setSelection(channelsAdapter.getPosition("CH1"), true);
        spinnerTriggerChannelSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((OscilloscopeActivity) getActivity()).triggerChannel = spinnerTriggerChannelSelect.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        checkBoxTrigger.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ((OscilloscopeActivity) getActivity()).isTriggerSelected = isChecked;
            }
        });

        return v;
    }
}