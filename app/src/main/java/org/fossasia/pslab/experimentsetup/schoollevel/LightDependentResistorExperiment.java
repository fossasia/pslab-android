package org.fossasia.pslab.experimentsetup.schoollevel;

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

import org.fossasia.pslab.R;
import org.fossasia.pslab.communication.ScienceLab;
import org.fossasia.pslab.experimentsetup.ExperimentErrorStrings;
import org.fossasia.pslab.others.ScienceLabCommon;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Padmal on 8/17/17.
 */

public class LightDependentResistorExperiment extends Fragment {

    private LineChart outputChart;
    private float frequency;
    private ScienceLab scienceLab = ScienceLabCommon.scienceLab;
    private final Object lock = new Object();

    private TextInputEditText etFrequency;
    private TextInputLayout tilFrequency;

    public static LightDependentResistorExperiment newInstance() {
        return new LightDependentResistorExperiment();
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
                if (scienceLab.isConnected()) {
                MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                        .title(getString(R.string.configure_experiment))
                        .customView(R.layout.ldr_experiment_dialog, true)
                        .positiveText(getString(R.string.start_experiment))
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                View customView = dialog.getCustomView();
                                assert customView != null;
                                etFrequency = (TextInputEditText) customView.findViewById(R.id.ldr_frequency);
                                tilFrequency = (TextInputLayout) customView.findViewById(R.id.ldr_frequency_layout);
                                // Initial Frequency
                                if (TextUtils.isEmpty(etFrequency.getText().toString())) {
                                    tilFrequency.setError(ExperimentErrorStrings.ERROR_MESSAGE);
                                    return;
                                } else if (Float.parseFloat(etFrequency.getText().toString()) < 10.0) {
                                    tilFrequency.setError(ExperimentErrorStrings.MINIMUM_VALUE_FREQUENCY);
                                    return;
                                } else if (Float.parseFloat(etFrequency.getText().toString()) > 300.0f) {
                                    tilFrequency.setError(ExperimentErrorStrings.MAXIMUM_VALUE_FREQUENCY_300);
                                    return;
                                } else {
                                    tilFrequency.setError(null);
                                }
                                frequency = Float.parseFloat(etFrequency.getText().toString());
                                
                                startExperiment();
                                
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
                 } else {
                             Toast.makeText(getContext(), "Device not connected", Toast.LENGTH_SHORT).show();
                        }
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
                scienceLab.setSqr1(frequency, 50, false);
                while (true) {
                    new RecordChannel().execute();
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

    private class RecordChannel extends AsyncTask<Void, Void, Void> {

        ArrayList<Entry> channelData;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                scienceLab.captureTraces(1, 1000, 10, "SEN", false, null);
                Thread.sleep((long) (1000 * 10 *1e-3));

                HashMap<String, double[]> data = scienceLab.fetchTrace(1);
                double[] xData = data.get("x");
                double[] yData = data.get("y");

                channelData = new ArrayList<>();
                for (int i = 0; i < xData.length; i++) {
                    channelData.add(new Entry((float) xData[i] / 1000, (float) yData[i]));
                }
            } catch (NullPointerException e) {
                cancel(true);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            LineDataSet dataSet = new LineDataSet(channelData, "Resistance (SEN)");
            LineData lineData = new LineData(dataSet);
            dataSet.setDrawCircles(false);
            outputChart.setData(lineData);
            outputChart.notifyDataSetChanged();
            outputChart.invalidate();

            synchronized (lock) {
                lock.notify();
            }
        }
    }
}
