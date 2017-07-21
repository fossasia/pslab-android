package org.fossasia.pslab.experimentsetup;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import org.fossasia.pslab.R;

/**
 * Created by viveksb007 on 20/7/17.
 */

public class OhmsLawSetupExperiment extends Fragment {

    private TextView tvCurrentValue;
    private TextView tvVoltageValue;
    private Button btnReadVoltage;
    private Spinner channelSelectSpinner;
    private String[] channels = {"CH1", "CH2", "CH3", "CH4"};

    public static OhmsLawSetupExperiment newInstance() {
        OhmsLawSetupExperiment ohmsLawSetupExperiment = new OhmsLawSetupExperiment();
        return ohmsLawSetupExperiment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.ohms_law_setup, container, false);
        tvCurrentValue = (TextView) view.findViewById(R.id.tv_current_value);
        tvVoltageValue = (TextView) view.findViewById(R.id.tv_voltage_value);
        btnReadVoltage = (Button) view.findViewById(R.id.btn_read_voltage);
        channelSelectSpinner = (Spinner) view.findViewById(R.id.channel_select_spinner);
        channelSelectSpinner.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, channels));
        return view;
    }
}
