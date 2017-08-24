package org.fossasia.pslab.experimentsetup;

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
import android.widget.Spinner;
import android.widget.TextView;
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
import org.fossasia.pslab.others.ScienceLabCommon;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by asitava on 9/8/17.
 */

public class CapacitorReactanceExperiment extends Fragment{

    private static final String ERROR_MESSAGE = "Invalid Value";
    private static final String MINIMUM_VALUE_FREQUENCY = "Frequency should be more than 10Hz";
    private static final String MAXIMUM_VALUE_FREQUENCY = "Frequency should be less than 5000Hz";
    private LineChart outputChart;
    private float frequency;
    private ScienceLab scienceLab = ScienceLabCommon.scienceLab;
    private final Object lock = new Object();
    private ArrayList<Float> voltageAxis1 = new ArrayList<>();
    private ArrayList<Float> voltageAxis2 = new ArrayList<>();
    private ArrayList<Float> timeAxis = new ArrayList<>();

    private TextInputEditText etFrequency;
    private TextInputLayout tilFrequency;

    private float readVoltage1, readVoltage2;

    public static CapacitorReactanceExperiment newInstance() {
        return new CapacitorReactanceExperiment();
    }

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.diode_setup, container, false);
        outputChart = (LineChart) view.findViewById(R.id.line_chart);
        Button btnConfigure = (Button) view.findViewById(R.id.btn_configure_dialog);
        btnConfigure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                        .title("Configure Experiment")
                        .customView(R.layout.capacitor_reactance_dialog, true)
                        .positiveText("Start Experiment")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                View customView = dialog.getCustomView();
                                assert customView != null;
                                etFrequency = (TextInputEditText) customView.findViewById(R.id.capacitor_frequency);
                                tilFrequency = (TextInputLayout) customView.findViewById(R.id.capacitor_frequency_layout);

                                if (TextUtils.isEmpty(etFrequency.getText().toString())) {
                                    tilFrequency.setError(ERROR_MESSAGE);
                                    return;
                                } else if (Float.parseFloat(etFrequency.getText().toString()) < 10.0f) {
                                    tilFrequency.setError(MINIMUM_VALUE_FREQUENCY);
                                    return;
                                } else if (Float.parseFloat(etFrequency.getText().toString()) > 5000.0f) {
                                    tilFrequency.setError(MAXIMUM_VALUE_FREQUENCY);
                                    return;
                                } else {
                                    tilFrequency.setError(null);
                                }

                                frequency = Float.parseFloat(etFrequency.getText().toString());

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
        outputChart.getDescription().setEnabled(false);
        LineData data = new LineData();
        outputChart.setData(data);
    }

    private void startExperiment() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                scienceLab.setSine1(frequency);
                for (float i = 0; i < 2000; i += 1) {
                    new CapacitorReactanceExperiment.CalcDataPoint().execute(i);
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
        List<Entry> temp1 = new ArrayList<>();
        List<Entry> temp2 = new ArrayList<>();
        for (int i = 0; i < timeAxis.size(); i++) {
            temp1.add(new Entry(timeAxis.get(i),voltageAxis1.get(i)));
            temp2.add(new Entry(timeAxis.get(i),voltageAxis2.get(i)));
        }
        LineDataSet dataSet1 = new LineDataSet(temp1, "CH1");
        LineDataSet dataSet2 = new LineDataSet(temp2, "CH2");
        dataSet1.setColor(Color.RED);
        dataSet1.setDrawValues(false);
        dataSet1.setDrawCircles(false);
        dataSet2.setColor(Color.BLUE);
        dataSet2.setDrawValues(false);
        dataSet2.setDrawCircles(false);
        dataSets.add(dataSet1);
        dataSets.add(dataSet2);
        outputChart.setData(new LineData(dataSets));
        outputChart.invalidate();
    }

    private class CalcDataPoint extends AsyncTask<Float, Void, Void> {

        @Override
        protected Void doInBackground(Float... params) {
            float i = params[0];
            readVoltage1 = (float) scienceLab.getVoltage("CH1", 10);
            readVoltage2 = (float) scienceLab.getVoltage("CH2", 10);
            timeAxis.add(i);
            voltageAxis1.add(readVoltage1);
            voltageAxis2.add(readVoltage2);
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