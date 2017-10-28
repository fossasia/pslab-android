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
import android.util.Log;
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
 * Created by viveksb007 on 22/7/17.
 */

public class TransistorCBSetup extends Fragment {

    private LineChart outputChart;
    private float initialVoltage = 0;
    private float finalVoltage = 0;
    private float emitterVoltage = 0;
    private float stepVoltage = 0;
    private float resistance = 560;
    private int totalSteps = 0;
    private ScienceLab scienceLab = ScienceLabCommon.scienceLab;
    private final Object lock = new Object();
    private ArrayList<Float> x = new ArrayList<>();
    private ArrayList<Float> y = new ArrayList<>();
    private TextInputEditText etInitialVoltage, etFinalVoltage, etTotalSteps, etEmitterVoltage;
    private TextInputLayout tilInitialVoltage, tilFinalVoltage, tilTotalSteps, tilEmitterVoltage;

    public static TransistorCBSetup newInstance() {
        return new TransistorCBSetup();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // reusing the layout consisting Configure button and graph
        View view = inflater.inflate(R.layout.common_experiment_setup, container, false);
        outputChart = (LineChart) view.findViewById(R.id.line_chart);
        Button btnConfigure = (Button) view.findViewById(R.id.btn_configure_dialog);
        btnConfigure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                        .title("Configure Experiment")
                        .customView(R.layout.transistor_cb_configure_dialog, true)
                        .positiveText("Start Experiment")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                View customView = dialog.getCustomView();
                                assert customView != null;
                                etInitialVoltage = (TextInputEditText) customView.findViewById(R.id.et_initial_voltage);
                                etFinalVoltage = (TextInputEditText) customView.findViewById(R.id.et_final_voltage);
                                etTotalSteps = (TextInputEditText) customView.findViewById(R.id.et_total_steps);
                                etEmitterVoltage = (TextInputEditText) customView.findViewById(R.id.et_emitter_voltage);
                                tilInitialVoltage = (TextInputLayout) customView.findViewById(R.id.text_input_layout_iv);
                                tilFinalVoltage = (TextInputLayout) customView.findViewById(R.id.text_input_layout_fv);
                                tilTotalSteps = (TextInputLayout) customView.findViewById(R.id.text_input_layout_total_steps);
                                tilEmitterVoltage = (TextInputLayout) customView.findViewById(R.id.text_input_layout_voltage);
                                // Initial Voltage
                                if (TextUtils.isEmpty(etInitialVoltage.getText().toString())) {
                                    tilInitialVoltage.setError(ExperimentErrorStrings.ERROR_MESSAGE);
                                    return;
                                } else if (Float.parseFloat(etInitialVoltage.getText().toString()) < -5.0f) {
                                    tilInitialVoltage.setError(ExperimentErrorStrings.MINIMUM_VALUE_5V);
                                    return;
                                } else if (Float.parseFloat(etInitialVoltage.getText().toString()) > 5.0f) {
                                    tilInitialVoltage.setError(ExperimentErrorStrings.MAXIMUM_VALUE_5V);
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
                                } else if (Float.parseFloat(etFinalVoltage.getText().toString()) < -5.0f) {
                                    tilFinalVoltage.setError(ExperimentErrorStrings.MINIMUM_VALUE_5V);
                                    return;
                                } else if (Float.parseFloat(etFinalVoltage.getText().toString()) > 5.0f) {
                                    tilFinalVoltage.setError(ExperimentErrorStrings.MAXIMUM_VALUE_5V);
                                    return;
                                } else {
                                    tilFinalVoltage.setError(null);
                                }
                                finalVoltage = Float.parseFloat(etFinalVoltage.getText().toString());
                                // Step Size
                                if (TextUtils.isEmpty(etTotalSteps.getText().toString())) {
                                    tilTotalSteps.setError(ExperimentErrorStrings.ERROR_MESSAGE);
                                    return;
                                } else
                                    tilTotalSteps.setError(null);
                                totalSteps = Integer.parseInt(etTotalSteps.getText().toString());
                                // Emitter Voltage
                                if (TextUtils.isEmpty(etEmitterVoltage.getText().toString())) {
                                    tilEmitterVoltage.setError(ExperimentErrorStrings.ERROR_MESSAGE);
                                    return;
                                } else if (Float.parseFloat(etEmitterVoltage.getText().toString()) < -3.3f) {
                                    tilEmitterVoltage.setError(ExperimentErrorStrings.MINIMUM_VALUE_3V);
                                    return;
                                } else if (Float.parseFloat(etEmitterVoltage.getText().toString()) > 3.3f) {
                                    tilEmitterVoltage.setError(ExperimentErrorStrings.MAXIMUM_VALUE_3V);
                                    return;
                                } else {
                                    tilEmitterVoltage.setError(null);
                                }
                                emitterVoltage = Float.parseFloat(etEmitterVoltage.getText().toString());
                                stepVoltage = (finalVoltage - initialVoltage) / totalSteps;
                                if (scienceLab.isConnected())
                                    startExperiment();
                                else
                                    Toast.makeText(getContext(), "Device not connected", Toast.LENGTH_SHORT).show();
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
        outputChart.getDescription().setEnabled(false);
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
                scienceLab.setPV2(emitterVoltage);
                for (float i = initialVoltage; i < finalVoltage; i += stepVoltage) {
                    CalcDataPoints dataPoint = new CalcDataPoints(i);
                    dataPoint.execute();
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
        Log.v("X-AXIS", x.toString());
        Log.v("Y-AXIS", y.toString());
        List<ILineDataSet> dataSets = new ArrayList<>();
        List<Entry> temp = new ArrayList<>();
        for (int i = 0; i < x.size(); i++) {
            temp.add(new Entry(x.get(i), y.get(i)));
        }
        LineDataSet dataSet = new LineDataSet(temp, "CB Characteristics");
        dataSet.setColor(Color.RED);
        dataSet.setDrawValues(false);
        dataSet.setDrawCircles(false);
        dataSets.add(dataSet);
        outputChart.setData(new LineData(dataSets));
        outputChart.invalidate();
    }

    private class CalcDataPoints extends AsyncTask<Void, Void, Void> {

        private float voltage;

        CalcDataPoints(float volt) {
            this.voltage = volt;
        }

        @Override
        protected Void doInBackground(Void... params) {
            scienceLab.setPV1(voltage);
            float readVoltage = (float) scienceLab.getVoltage("CH1", 5);
            x.add(readVoltage);
            y.add((voltage - readVoltage) / resistance);
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
