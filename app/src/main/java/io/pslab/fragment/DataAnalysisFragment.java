package io.pslab.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

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

import io.pslab.R;
import io.pslab.activity.OscilloscopeActivity;
import io.pslab.others.FloatSeekBar;

public class DataAnalysisFragment extends Fragment {

    private Spinner spinnerCurveFit;
    private Spinner spinnerChannelSelect1;
    private Spinner spinnerChannelSelect2;
    private CheckBox checkBoxFouierTransform;
    private Spinner spinnerChannelSelectHorizontalOffset;
    private Spinner spinnerChannelSelectVerticalOffset;
    private FloatSeekBar seekBarHorizontalOffset;
    private FloatSeekBar seekBarVerticalOffset;
    private TextView textViewHorizontalOffset;
    private TextView textViewVerticalOffset;

    public static DataAnalysisFragment newInstance() {
        return new DataAnalysisFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_data_analysis_main, container, false);
        String[] curveFits = {"Sine Fit", "Square Fit"};
        String[] channels = {"None", "CH1", "CH2", "CH3", "MIC"};

        spinnerCurveFit = v.findViewById(R.id.spinner_curve_fit_da);
        spinnerChannelSelect1 = v.findViewById(R.id.spinner_channel_select_da1);
        spinnerChannelSelect2 = v.findViewById(R.id.spinner_channel_select_da2);
        spinnerChannelSelectHorizontalOffset = v.findViewById(R.id.spinner_channel_select_horizontal_offset);
        spinnerChannelSelectVerticalOffset = v.findViewById(R.id.spinner_channel_select_vertical_offset);
        seekBarHorizontalOffset = v.findViewById(R.id.seekbar_horizontal_offset);
        seekBarVerticalOffset = v.findViewById(R.id.seekbar_vertical_offset);
        textViewHorizontalOffset = v.findViewById(R.id.textview_horizontal_offset);
        textViewVerticalOffset = v.findViewById(R.id.textview_vertical_offset);
        checkBoxFouierTransform = v.findViewById(R.id.checkBox_fourier_da);
        boolean tabletSize = getResources().getBoolean(R.bool.isTablet);
        ArrayAdapter<String> curveFitAdapter;
        ArrayAdapter<String> adapter;

        if (tabletSize) {
            curveFitAdapter = new ArrayAdapter<>(this.getActivity(), R.layout.custom_spinner_tablet, curveFits);
            adapter = new ArrayAdapter<>(this.getActivity(), R.layout.custom_spinner_tablet, channels);
        } else {
            curveFitAdapter = new ArrayAdapter<>(this.getActivity(), R.layout.custom_spinner, curveFits);
            adapter = new ArrayAdapter<>(this.getActivity(), R.layout.custom_spinner, channels);
        }

        curveFitAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        spinnerCurveFit.setAdapter(curveFitAdapter);
        spinnerChannelSelect1.setAdapter(adapter);
        spinnerChannelSelect2.setAdapter(adapter);
        spinnerChannelSelectHorizontalOffset.setAdapter(adapter);
        spinnerChannelSelectVerticalOffset.setAdapter(adapter);

        spinnerCurveFit.setSelection(curveFitAdapter.getPosition("Sine Fit"), true);
        spinnerChannelSelect1.setSelection(adapter.getPosition("None"), true);
        spinnerChannelSelect2.setSelection(adapter.getPosition("None"), true);
        spinnerChannelSelectHorizontalOffset.setSelection(adapter.getPosition("None"), true);
        spinnerChannelSelectVerticalOffset.setSelection(adapter.getPosition("None"), true);

        spinnerCurveFit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    ((OscilloscopeActivity) getActivity()).sineFit = true;
                    ((OscilloscopeActivity) getActivity()).squareFit = false;
                } else {
                    ((OscilloscopeActivity) getActivity()).sineFit = false;
                    ((OscilloscopeActivity) getActivity()).squareFit = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerChannelSelect1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((OscilloscopeActivity) getActivity()).curveFittingChannel1 = spinnerChannelSelect1.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerChannelSelect2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((OscilloscopeActivity) getActivity()).curveFittingChannel2 = spinnerChannelSelect2.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerChannelSelectHorizontalOffset.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (spinnerChannelSelectHorizontalOffset.getSelectedItem() != "None") {
                    seekBarHorizontalOffset.setValue(((OscilloscopeActivity) getActivity()).xOffsets.get(spinnerChannelSelectHorizontalOffset.getSelectedItem().toString()));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing
            }
        });

        spinnerChannelSelectVerticalOffset.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (spinnerChannelSelectVerticalOffset.getSelectedItem() != "None") {
                    seekBarVerticalOffset.setValue(((OscilloscopeActivity) getActivity()).yOffsets.get(spinnerChannelSelectVerticalOffset.getSelectedItem().toString()));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing
            }
        });

        if (((OscilloscopeActivity) getActivity()).xAxisScale == 875) {
            seekBarHorizontalOffset.setters(0, ((OscilloscopeActivity) getActivity()).xAxisScale / 1000.0);
        } else {
            seekBarHorizontalOffset.setters(0, ((OscilloscopeActivity) getActivity()).xAxisScale);
        }
        seekBarHorizontalOffset.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                textViewHorizontalOffset.setText(String.format("%sms", seekBarHorizontalOffset.getValue()));
                ((OscilloscopeActivity) getActivity()).xOffsets.put(spinnerChannelSelectHorizontalOffset.getSelectedItem().toString(), seekBarHorizontalOffset.getValue());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }
        });
        seekBarHorizontalOffset.setProgress(0);

        seekBarVerticalOffset.setters(-1 * ((OscilloscopeActivity) getActivity()).yAxisScale, ((OscilloscopeActivity) getActivity()).yAxisScale);
        seekBarVerticalOffset.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                textViewVerticalOffset.setText(String.format("%sV", seekBarVerticalOffset.getValue()));
                ((OscilloscopeActivity) getActivity()).yOffsets.put(spinnerChannelSelectVerticalOffset.getSelectedItem().toString(), seekBarVerticalOffset.getValue());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }
        });
        seekBarVerticalOffset.setProgress(50);

        checkBoxFouierTransform.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ((OscilloscopeActivity) getActivity()).isFourierTransformSelected = isChecked;
            }
        });

        return v;
    }
}
