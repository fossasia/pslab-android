package org.fossasia.pslab.experimentsetup.electronicexperiments;

import android.graphics.Color;
import android.os.AsyncTask;
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
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.fossasia.pslab.R;
import org.fossasia.pslab.communication.ScienceLab;
import org.fossasia.pslab.experimentsetup.ExperimentErrorStrings;
import org.fossasia.pslab.others.ScienceLabCommon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Padmal on 8/9/17.
 */

public class TransistorAmplifierExperiment extends Fragment {

    private LineChart outputChart;
    private float inputFrequency, pv3Voltage;
    private ScienceLab scienceLab = ScienceLabCommon.scienceLab;
    private final Object lock = new Object();

    private TextInputEditText etInitialFrequency, etVoltage;
    private TextInputLayout tilInitialFrequency, tilVoltage;

    public static TransistorAmplifierExperiment newInstance() {
        return new TransistorAmplifierExperiment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.transistor_amplifier_view, container, false);
        outputChart = (LineChart) view.findViewById(R.id.line_chart);
        Button btnConfigure = (Button) view.findViewById(R.id.btn_configure_dialog);
        btnConfigure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                        .title(getString(R.string.configure_experiment))
                        .customView(R.layout.transistor_amplifier_characteristics_dialog, true)
                        .positiveText(getString(R.string.start_experiment))
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                View customView = dialog.getCustomView();
                                assert customView != null;
                                etInitialFrequency = (TextInputEditText) customView.findViewById(R.id.amplifier_initial_frequency);
                                etVoltage = (TextInputEditText) customView.findViewById(R.id.amplifier_pv3_voltage);
                                tilInitialFrequency = (TextInputLayout) customView.findViewById(R.id.amplifier_initial_frequency_layout);
                                tilVoltage = (TextInputLayout) customView.findViewById(R.id.amplifier_pv3_voltage_layout);
                                // Initial Frequency
                                if (TextUtils.isEmpty(etInitialFrequency.getText().toString())) {
                                    tilInitialFrequency.setError(ExperimentErrorStrings.ERROR_MESSAGE);
                                    return;
                                } else if (Float.parseFloat(etInitialFrequency.getText().toString()) < 10.0f) {
                                    tilInitialFrequency.setError(ExperimentErrorStrings.MINIMUM_VALUE_FREQUENCY);
                                    return;
                                } else if (Float.parseFloat(etInitialFrequency.getText().toString()) > 5000.0f) {
                                    tilInitialFrequency.setError(ExperimentErrorStrings.MAXIMUM_VALUE_FREQUENCY);
                                    return;
                                } else {
                                    tilInitialFrequency.setError(null);
                                }
                                inputFrequency = Float.parseFloat(etInitialFrequency.getText().toString());
                                // PV3 Voltage
                                if (TextUtils.isEmpty(etVoltage.getText().toString())) {
                                    tilVoltage.setError(ExperimentErrorStrings.ERROR_MESSAGE);
                                    return;
                                } else if (Float.parseFloat(etVoltage.getText().toString()) < 0.0f) {
                                    tilVoltage.setError(ExperimentErrorStrings.MINIMUM_VALUE_0V);
                                    return;
                                } else if (Float.parseFloat(etVoltage.getText().toString()) > 3.3f) {
                                    tilVoltage.setError(ExperimentErrorStrings.MAXIMUM_VALUE_3V);
                                    return;
                                } else {
                                    tilVoltage.setError(null);
                                }
                                pv3Voltage = Float.parseFloat(etVoltage.getText().toString());
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
                scienceLab.setPV3(pv3Voltage);
                scienceLab.setSine1(inputFrequency);
                while (true) {
                    new RecordChannels().execute();
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

    private class RecordChannels extends AsyncTask<Void, Void, Void> {

        ArrayList<Entry> ch1Data;
        ArrayList<Entry> ch2Data;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                HashMap<String, double[]> data;
                data = scienceLab.captureTwo(1000, 10, "CH1", false);
                double[] xData = data.get("x");
                double[] y1Data = data.get("y1");
                double[] y2Data = data.get("y2");

                ch1Data = new ArrayList<>();
                ch2Data = new ArrayList<>();
                for (int i = 0; i < xData.length; i++) {
                    ch1Data.add(new Entry((float) xData[i] / 1000, (float) y1Data[i]));
                    ch2Data.add(new Entry((float) xData[i] / 1000, (float) y2Data[i]));
                }
            } catch (NullPointerException e) {
                cancel(true);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            LineDataSet dataset1 = new LineDataSet(ch1Data, "CH1");
            LineDataSet dataSet2 = new LineDataSet(ch2Data, "CH2");

            dataset1.setColor(Color.GREEN);
            dataSet2.setColor(Color.RED);

            dataset1.setDrawCircles(false);
            dataSet2.setDrawCircles(false);

            List<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(dataset1);
            dataSets.add(dataSet2);

            LineData data = new LineData(dataSets);
            outputChart.setData(data);
            outputChart.notifyDataSetChanged();
            outputChart.invalidate();
            synchronized (lock) {
                lock.notify();
            }
        }
    }
}
