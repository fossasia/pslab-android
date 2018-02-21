package org.fossasia.pslab.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;

import org.fossasia.pslab.PSLabApplication;
import org.fossasia.pslab.R;
import org.fossasia.pslab.activity.OscilloscopeActivity;

public class DataAnalysisFragment extends Fragment {

    private Spinner spinnerCurveFit;
    private Spinner spinnerChannelSelect1;
    private Spinner spinnerChannelSelect2;
    private CheckBox checkBoxFouierTransform;

    public static DataAnalysisFragment newInstance() {
        return new DataAnalysisFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_data_analysis, container, false);
        String[] curveFits = {"Sine Fit", "Square Fit"};
        String[] channels = {"None", "CH1", "CH2", "CH3", "MIC"};

        spinnerCurveFit = (Spinner) v.findViewById(R.id.spinner_curve_fit_da);
        spinnerChannelSelect1 = (Spinner) v.findViewById(R.id.spinner_channel_select_da1);
        spinnerChannelSelect2 = (Spinner) v.findViewById(R.id.spinner_channel_select_da2);
        checkBoxFouierTransform = (CheckBox) v.findViewById(R.id.checkBox_fourier_da);
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

        spinnerCurveFit.setSelection(curveFitAdapter.getPosition("Sine Fit"), true);
        spinnerChannelSelect1.setSelection(adapter.getPosition("None"), true);
        spinnerChannelSelect2.setSelection(adapter.getPosition("None"), true);
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

        checkBoxFouierTransform.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ((OscilloscopeActivity) getActivity()).isFourierTransformSelected = isChecked;
            }
        });

        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        ((PSLabApplication)getActivity().getApplication()).refWatcher.watch(this, DataAnalysisFragment.class.getSimpleName());
    }
}
