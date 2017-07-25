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

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineData;

import org.fossasia.pslab.R;

/**
 * Created by viveksb007 on 22/7/17.
 */

public class TransistorCEOutputSetup extends Fragment {

    private static final String ERROR_MESSAGE = "Invalid Value";
    private LineChart outputChart;
    private float initialVoltage = 0;
    private float finalVoltage = 0;
    private float baseVoltage = 0;
    private int totalSteps = 0;

    public static TransistorCEOutputSetup newInstance() {
        TransistorCEOutputSetup transistorCEOutputSetup = new TransistorCEOutputSetup();
        return transistorCEOutputSetup;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // reusing the layout consisting Configure button and graph
        View view = inflater.inflate(R.layout.diode_setup, container, false);
        outputChart = (LineChart) view.findViewById(R.id.line_chart);
        Button btnConfigure = (Button) view.findViewById(R.id.btn_configure_dialog);
        btnConfigure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                        .title("Configure Experiment")
                        .customView(R.layout.transistor_ce_output_configure_dialog, true)
                        .positiveText("Start Experiment")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                View customView = dialog.getCustomView();
                                assert customView != null;
                                TextInputEditText etInitialVoltage = (TextInputEditText) customView.findViewById(R.id.et_initial_voltage);
                                TextInputEditText etFinalVoltage = (TextInputEditText) customView.findViewById(R.id.et_final_voltage);
                                TextInputEditText etTotalSteps = (TextInputEditText) customView.findViewById(R.id.et_total_steps);
                                TextInputEditText etBaseVoltage = (TextInputEditText) customView.findViewById(R.id.et_base_voltage);
                                TextInputLayout tilInitialVoltage = (TextInputLayout) customView.findViewById(R.id.text_input_layout_iv);
                                TextInputLayout tilFinalVoltage = (TextInputLayout) customView.findViewById(R.id.text_input_layout_fv);
                                TextInputLayout tilTotalSteps = (TextInputLayout) customView.findViewById(R.id.text_input_layout_total_steps);
                                TextInputLayout tilBaseVoltage = (TextInputLayout) customView.findViewById(R.id.text_input_layout_voltage);
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
                                if (TextUtils.isEmpty(etTotalSteps.getText().toString())) {
                                    tilTotalSteps.setError(ERROR_MESSAGE);
                                    return;
                                } else
                                    tilTotalSteps.setError(null);
                                if (TextUtils.isEmpty(etBaseVoltage.getText().toString())) {
                                    tilBaseVoltage.setError(ERROR_MESSAGE);
                                    return;
                                } else
                                    tilBaseVoltage.setError(null);
                                initialVoltage = Float.parseFloat(etInitialVoltage.getText().toString());
                                finalVoltage = Float.parseFloat(etFinalVoltage.getText().toString());
                                totalSteps = Integer.parseInt(etTotalSteps.getText().toString());
                                baseVoltage = Float.parseFloat(etBaseVoltage.getText().toString());

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

    private void chartInit() {
        outputChart.setTouchEnabled(true);
        outputChart.setDragEnabled(true);
        outputChart.setScaleEnabled(true);
        outputChart.setPinchZoom(true);
        LineData data = new LineData();
        outputChart.setData(data);
    }

}
