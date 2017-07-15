package org.fossasia.pslab.experimentsetup;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.mikephil.charting.charts.LineChart;

import org.fossasia.pslab.R;

/**
 * Created by viveksb007 on 15/7/17.
 */

public class ZenerSetupFragment extends Fragment {

    private static final String ERROR_MESSAGE = "Invalid Value";
    private LineChart outputChart;

    public static ZenerSetupFragment newInstance() {
        ZenerSetupFragment zenerSetup = new ZenerSetupFragment();
        return zenerSetup;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.zener_setup, container, false);
        outputChart = (LineChart) view.findViewById(R.id.zener_chart);

        Button btnConfigure = (Button) view.findViewById(R.id.btn_configure_dialog);
        btnConfigure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // open Material Dialog
                MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                        .title("Configure Experiment")
                        .customView(R.layout.zener_configure_dialog, true)
                        .positiveText("Start Experiment")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                Toast.makeText(getActivity(), "Positive", Toast.LENGTH_SHORT).show();
                                View customView = dialog.getCustomView();
                                assert customView != null;
                                TextInputEditText etInitialVoltage = (TextInputEditText) customView.findViewById(R.id.et_initial_voltage);
                                TextInputEditText etFinalVoltage = (TextInputEditText) customView.findViewById(R.id.et_final_voltage);
                                TextInputEditText etStepSize = (TextInputEditText) customView.findViewById(R.id.et_step_size);
                                TextInputLayout tilInitialVoltage = (TextInputLayout) customView.findViewById(R.id.text_input_layout_iv);
                                TextInputLayout tilFinalVoltage = (TextInputLayout) customView.findViewById(R.id.text_input_layout_fv);
                                TextInputLayout tilStepSize = (TextInputLayout) customView.findViewById(R.id.text_input_layout_ss);
                                if (TextUtils.isEmpty(etInitialVoltage.getText().toString())) {
                                    tilInitialVoltage.setError(ERROR_MESSAGE);
                                    return;
                                } else
                                    tilInitialVoltage.setError(null);
                                if (TextUtils.isEmpty(etFinalVoltage.getText().toString())) {
                                    tilFinalVoltage.setError(ERROR_MESSAGE);
                                    return;
                                } else
                                    tilFinalVoltage.setError(null);
                                if (TextUtils.isEmpty(etStepSize.getText().toString())) {
                                    tilStepSize.setError(ERROR_MESSAGE);
                                    return;
                                } else
                                    tilStepSize.setError(null);
                                startExperiment();
                            }
                        })
                        .negativeText("Cancel")
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        })
                        .autoDismiss(false)
                        .build();
                dialog.show();
            }
        });
        chartInit();
        return view;
    }

    private void startExperiment() {
        // ToDo : code for changing voltage from IV to FV and read current value for each sample
        Toast.makeText(getActivity(), "Starting Experiment", Toast.LENGTH_SHORT).show();
    }

    private void chartInit() {
        // Initialise chart properties
    }

}

