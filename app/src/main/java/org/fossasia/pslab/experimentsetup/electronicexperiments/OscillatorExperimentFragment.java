package org.fossasia.pslab.experimentsetup.electronicexperiments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import org.fossasia.pslab.R;
import org.fossasia.pslab.activity.OscilloscopeActivity;

public class OscillatorExperimentFragment extends Fragment {

    public TextView resultCH1Frequency;
    public TextView resultCH2Frequency;
    public TextView analyseCH2Label;
    public TextView analyseCH1Label;
    public double frequency;

    public OscillatorExperimentFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_oscillator_experiment, container, false);
        Spinner spinnerRangeCh1 = (Spinner) v.findViewById(R.id.spinner_range_astable_multivibrator);
        Button buttonCH1Frequency = (Button) v.findViewById(R.id.button_read_ch1_astable_multivibrator);
        Button buttonCH2Frequency = (Button) v.findViewById(R.id.button_read_ch2_astable_multivibrator);
        resultCH1Frequency = (TextView) v.findViewById(R.id.tv_result_ch1_astable_multivibrator);
        resultCH2Frequency = (TextView) v.findViewById(R.id.tv_result_ch2_astable_multivibrator);
        analyseCH2Label = (TextView) v.findViewById(R.id.tv_abalyse_ch2_astable_multivibrator);
        analyseCH1Label = (TextView) v.findViewById(R.id.tv_analyse_ch1_astable_multivibrator);

        if (((OscilloscopeActivity) getActivity()).isColpittsOscillatorExperiment ||
                ((OscilloscopeActivity) getActivity()).isPhaseShiftOscillatorExperiment ||
                ((OscilloscopeActivity) getActivity()).isWienBridgeOscillatorExperiment ||
                ((OscilloscopeActivity) getActivity()).isMonostableMultivibratorExperiment) {
            buttonCH2Frequency.setVisibility(View.INVISIBLE);
            resultCH2Frequency.setVisibility(View.INVISIBLE);
            analyseCH2Label.setVisibility(View.INVISIBLE);
        }
        if (((OscilloscopeActivity) getActivity()).isMonostableMultivibratorExperiment)
            analyseCH1Label.setText(R.string.acquire_data);

        final String[] ranges = {"+/-16V", "+/-8V", "+/-4V", "+/-3V", "+/-2V", "+/-1.5V", "+/-1V", "+/-500mV", "+/-160V"};

        ArrayAdapter<String> rangesAdapter;
        rangesAdapter = new ArrayAdapter<>(this.getActivity(), R.layout.custom_spinner, ranges);
        rangesAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        spinnerRangeCh1.setAdapter(rangesAdapter);
        spinnerRangeCh1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        ((OscilloscopeActivity) getActivity()).setLeftYAxisScale(16, -16);
                        ((OscilloscopeActivity) getActivity()).setRightYAxisScale(16, -16);
                        break;
                    case 1:
                        ((OscilloscopeActivity) getActivity()).setLeftYAxisScale(8, -8);
                        ((OscilloscopeActivity) getActivity()).setRightYAxisScale(8, -8);
                        break;
                    case 2:
                        ((OscilloscopeActivity) getActivity()).setLeftYAxisScale(4, -4);
                        ((OscilloscopeActivity) getActivity()).setRightYAxisScale(4, -4);
                        break;
                    case 3:
                        ((OscilloscopeActivity) getActivity()).setLeftYAxisScale(3, -3);
                        ((OscilloscopeActivity) getActivity()).setRightYAxisScale(3, -3);
                        break;
                    case 4:
                        ((OscilloscopeActivity) getActivity()).setLeftYAxisScale(2, -2);
                        ((OscilloscopeActivity) getActivity()).setRightYAxisScale(2, -2);
                        break;
                    case 5:
                        ((OscilloscopeActivity) getActivity()).setLeftYAxisScale(1.5, -1.5);
                        ((OscilloscopeActivity) getActivity()).setRightYAxisScale(1.5, -1.5);
                        break;
                    case 6:
                        ((OscilloscopeActivity) getActivity()).setLeftYAxisScale(1, -1);
                        ((OscilloscopeActivity) getActivity()).setRightYAxisScale(1, -1);
                        break;
                    case 7:
                        ((OscilloscopeActivity) getActivity()).setLeftYAxisScale(500, -500);
                        ((OscilloscopeActivity) getActivity()).setRightYAxisScale(500, -500);
                        break;
                    case 8:
                        ((OscilloscopeActivity) getActivity()).setLeftYAxisScale(160, -160);
                        ((OscilloscopeActivity) getActivity()).setRightYAxisScale(160, -160);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        buttonCH1Frequency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((OscilloscopeActivity) getActivity()).runMonostableMultivibratorExperiment = true;
                if (!((OscilloscopeActivity) getActivity()).isMonostableMultivibratorExperiment)
                    ((OscilloscopeActivity) getActivity()).isCH1FrequencyRequired = true;
            }
        });

        buttonCH2Frequency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((OscilloscopeActivity) getActivity()).isCH2FrequencyRequired = true;
            }
        });
        return v;
    }

}