package org.fossasia.pslab.experimentsetup.electronicexperiments;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.fossasia.pslab.R;
import org.fossasia.pslab.communication.ScienceLab;
import org.fossasia.pslab.experimentsetup.ExperimentErrorStrings;
import org.fossasia.pslab.others.ScienceLabCommon;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Padmal on 8/6/17.
 */

public class TransistorCEInputCharacteristicsExperiment extends Fragment {

    private LineChart outputChart;
    private float initialVoltage;
    private float finalVoltage;
    private float collectorVoltage;
    private float stepVoltage;
    private float totalSteps;
    private ScienceLab scienceLab = ScienceLabCommon.scienceLab;
    private final Object lock = new Object();
    private ArrayList<Float> voltageAxis = new ArrayList<>();
    private ArrayList<Float> currentAxis = new ArrayList<>();

    private TextInputEditText etInitialVoltage, etFinalVoltage, etStepSize, etCollectorVoltage;
    private TextInputLayout tilInitialVoltage, tilFinalVoltage, tilStepSize, tilCollectorVoltage;

    public static TransistorCEInputCharacteristicsExperiment newInstance() {
        return new TransistorCEInputCharacteristicsExperiment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.common_experiment_setup, container, false);
        outputChart = view.findViewById(R.id.line_chart);
        Button btnConfigure = view.findViewById(R.id.btn_configure_dialog);
        btnConfigure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                        .title("Configure Experiment")
                        .customView(R.layout.bjt_input_characteristic_dialog, true)
                        .positiveText("Start Experiment")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                View customView = dialog.getCustomView();
                                assert customView != null;
                                etInitialVoltage = customView.findViewById(R.id.bjt_input_initial_voltage);
                                etFinalVoltage = customView.findViewById(R.id.bjt_input_final_voltage);
                                etStepSize = customView.findViewById(R.id.bjt_input_step_size);
                                etCollectorVoltage = customView.findViewById(R.id.bjt_input_collector_voltage);
                                tilInitialVoltage = customView.findViewById(R.id.bjt_input_initial_voltage_layout);
                                tilFinalVoltage = customView.findViewById(R.id.bjt_input_final_voltage_layout);
                                tilStepSize = customView.findViewById(R.id.bjt_input_step_size_layout);
                                tilCollectorVoltage = customView.findViewById(R.id.bjt_input_collector_voltage_layout);
                                // Initial Voltage
                                if (TextUtils.isEmpty(etInitialVoltage.getText().toString())) {
                                    tilInitialVoltage.setError(ExperimentErrorStrings.ERROR_MESSAGE);
                                    return;
                                } else if (Float.parseFloat(etInitialVoltage.getText().toString()) < -3.3f) {
                                    tilInitialVoltage.setError(ExperimentErrorStrings.MINIMUM_VALUE_3V);
                                    return;
                                } else if (Float.parseFloat(etInitialVoltage.getText().toString()) > 3.3f) {
                                    tilInitialVoltage.setError(ExperimentErrorStrings.MAXIMUM_VALUE_3V);
                                    return;
                                } else {
                                    tilInitialVoltage.setError(null);
                                }
                                initialVoltage = Float.parseFloat(etInitialVoltage.getText().toString());
                                // Final Voltage
                                if (TextUtils.isEmpty(etFinalVoltage.getText().toString())) {
                                    tilFinalVoltage.setError(ExperimentErrorStrings.ERROR_MESSAGE);
                                    return;
                                } else if (initialVoltage >= Float.parseFloat(etFinalVoltage.getText().toString())) {
                                    tilFinalVoltage.setError(ExperimentErrorStrings.INVALID_VALUE);
                                    return;
                                } else if (Float.parseFloat(etFinalVoltage.getText().toString()) < -3.3f) {
                                    tilFinalVoltage.setError(ExperimentErrorStrings.MINIMUM_VALUE_3V);
                                    return;
                                } else if (Float.parseFloat(etFinalVoltage.getText().toString()) > 3.3f) {
                                    tilFinalVoltage.setError(ExperimentErrorStrings.MAXIMUM_VALUE_3V);
                                    return;
                                } else {
                                    tilFinalVoltage.setError(null);
                                }
                                finalVoltage = Float.parseFloat(etFinalVoltage.getText().toString());
                                // Step Size
                                if (TextUtils.isEmpty(etStepSize.getText().toString())) {
                                    tilStepSize.setError(ExperimentErrorStrings.ERROR_MESSAGE);
                                    return;
                                } else {
                                    tilStepSize.setError(null);
                                }
                                totalSteps = Float.parseFloat(etStepSize.getText().toString());
                                // Collector Voltage
                                if (TextUtils.isEmpty(etCollectorVoltage.getText().toString())) {
                                    tilCollectorVoltage.setError(ExperimentErrorStrings.ERROR_MESSAGE);
                                    return;
                                } else if (Float.parseFloat(etCollectorVoltage.getText().toString()) < -5.0f) {
                                    tilCollectorVoltage.setError(ExperimentErrorStrings.MINIMUM_VALUE_5V);
                                    return;
                                } else if (Float.parseFloat(etCollectorVoltage.getText().toString()) > 5.0f) {
                                    tilCollectorVoltage.setError(ExperimentErrorStrings.MAXIMUM_VALUE_5V);
                                    return;
                                } else {
                                    tilCollectorVoltage.setError(null);
                                }
                                collectorVoltage = Float.parseFloat(etCollectorVoltage.getText().toString());
                                stepVoltage = (finalVoltage - initialVoltage) / totalSteps;

                                if (scienceLab.isConnected()) {
                                    startExperiment();
                                } else {
                                    Toast.makeText(getContext(), "Device not connected", Toast.LENGTH_SHORT).show();
                                }
                                dialog.dismiss();
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
        outputChart.getAxisLeft().setTextColor(Color.WHITE);
        outputChart.getAxisRight().setTextColor(Color.WHITE);
        outputChart.getXAxis().setTextColor(Color.WHITE);
        outputChart.getLegend().setTextColor(Color.WHITE);
        LineData data = new LineData();
        outputChart.setData(data);
    }

    private void startExperiment() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                scienceLab.setGain("CH1", 2, false);
                scienceLab.setPV1(collectorVoltage);
                for (float i = initialVoltage; i < finalVoltage; i += stepVoltage) {
                    new CalcDataPoint().execute(i);
                    synchronized (lock) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        new Thread(runnable).start();
    }

    private void updateChart() {
        List<ILineDataSet> dataSets = new ArrayList<>();
        List<Entry> temp = new ArrayList<>();
        for (int i = 0; i < voltageAxis.size(); i++) {
            temp.add(new Entry(voltageAxis.get(i), currentAxis.get(i)));
        }
        LineDataSet dataSet = new LineDataSet(temp, "BJT Input Characteristics");
        dataSet.setColor(Color.RED);
        dataSet.setDrawValues(false);
        dataSet.setDrawCircles(false);
        dataSets.add(dataSet);
        outputChart.setData(new LineData(dataSets));
        outputChart.invalidate();
    }

    private class CalcDataPoint extends AsyncTask<Float, Void, Void> {

        @Override
        protected Void doInBackground(Float... params) {
            float voltage = params[0];
            scienceLab.setPV2(voltage);
            float readVoltage = (float) scienceLab.getVoltage("CH3", 10);
            voltageAxis.add(readVoltage);
            float resistance = 200 * 1000;
            currentAxis.add((voltage - readVoltage) / resistance);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    updateChart();
                }
            });
            synchronized (lock) {
                lock.notify();
            }
        }
    }
}
