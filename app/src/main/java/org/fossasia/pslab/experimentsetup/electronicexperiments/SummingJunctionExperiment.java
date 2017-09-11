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
import android.widget.Spinner;
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
 * Created by asitava on 9/8/17.
 */

public class SummingJunctionExperiment extends Fragment {

    private LineChart outputChart;
    private float frequency, voltage, phase, current;
    private ScienceLab scienceLab = ScienceLabCommon.scienceLab;
    private final Object lock = new Object();
    private ArrayList<Float> voltageAxis1 = new ArrayList<>();
    private ArrayList<Float> voltageAxis2 = new ArrayList<>();
    private ArrayList<Float> voltageAxis3 = new ArrayList<>();
    private ArrayList<Float> timeAxis = new ArrayList<>();

    private TextInputEditText etFrequency, etVoltage, etPhase;
    private TextInputLayout tilFrequency, tilVoltage, tilPhase;
    private Spinner spinnerVoltageSource, spinnerWaveGenerator, spinnerWaveType;

    private String voltageSource, waveGenerator, waveType;
    private float readVoltageCH1, readVoltageCH2, readVoltageCH3;

    public static SummingJunctionExperiment newInstance() {
        return new SummingJunctionExperiment();
    }

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.common_experiment_setup, container, false); //reusing the common_experiment_setup layout file as it is identical.
        outputChart = (LineChart) view.findViewById(R.id.line_chart);
        Button btnConfigure = (Button) view.findViewById(R.id.btn_configure_dialog);
        btnConfigure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                        .title("Configure Experiment")
                        .customView(R.layout.summing_junction_dialog, true)
                        .positiveText("Start Experiment")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                View customView = dialog.getCustomView();
                                assert customView != null;
                                etFrequency = (TextInputEditText) customView.findViewById(R.id.frequency);
                                tilFrequency = (TextInputLayout) customView.findViewById(R.id.frequency_layout);
                                spinnerVoltageSource = (Spinner) customView.findViewById(R.id.voltage_source);
                                etVoltage = (TextInputEditText) customView.findViewById(R.id.voltage);
                                tilVoltage = (TextInputLayout) customView.findViewById(R.id.voltage_layout);
                                spinnerWaveGenerator = (Spinner) customView.findViewById(R.id.wave_generator);
                                spinnerWaveType = (Spinner) customView.findViewById(R.id.wave_type);
                                etPhase = (TextInputEditText) customView.findViewById(R.id.phase);
                                tilPhase = (TextInputLayout) customView.findViewById(R.id.phase_layout);

                                voltageSource = spinnerVoltageSource.getSelectedItem().toString();

                                if ("PV1".equals(voltageSource)) {
                                    if (TextUtils.isEmpty(etFrequency.getText().toString())) {
                                        tilFrequency.setError(ExperimentErrorStrings.ERROR_MESSAGE);
                                        return;
                                    } else if (Float.parseFloat(etFrequency.getText().toString()) < -5.0f) {
                                        tilFrequency.setError(ExperimentErrorStrings.MINIMUM_VALUE_5V);
                                        return;
                                    } else if (Float.parseFloat(etFrequency.getText().toString()) > 5.0f) {
                                        tilFrequency.setError(ExperimentErrorStrings.MAXIMUM_VALUE_5V);
                                        return;
                                    } else {
                                        tilFrequency.setError(null);
                                    }
                                    voltage = Float.parseFloat(etVoltage.getText().toString());
                                } else if ("PV2".equals(voltageSource)) {
                                    if (TextUtils.isEmpty(etFrequency.getText().toString())) {
                                        tilFrequency.setError(ExperimentErrorStrings.ERROR_MESSAGE);
                                        return;
                                    } else if (Float.parseFloat(etFrequency.getText().toString()) < -3.3f) {
                                        tilFrequency.setError(ExperimentErrorStrings.MINIMUM_VALUE_3V);
                                        return;
                                    } else if (Float.parseFloat(etFrequency.getText().toString()) > 3.3f) {
                                        tilFrequency.setError(ExperimentErrorStrings.MAXIMUM_VALUE_3V);
                                        return;
                                    } else {
                                        tilFrequency.setError(null);
                                    }
                                    voltage = Float.parseFloat(etVoltage.getText().toString());
                                } else if ("PV3".equals(voltageSource)) {
                                    if (TextUtils.isEmpty(etFrequency.getText().toString())) {
                                        tilFrequency.setError(ExperimentErrorStrings.ERROR_MESSAGE);
                                        return;
                                    } else if (Float.parseFloat(etFrequency.getText().toString()) < 0.0f) {
                                        tilFrequency.setError(ExperimentErrorStrings.MINIMUM_VALUE_0V);
                                        return;
                                    } else if (Float.parseFloat(etFrequency.getText().toString()) > 3.3f) {
                                        tilFrequency.setError(ExperimentErrorStrings.MAXIMUM_VALUE_3V);
                                        return;
                                    } else {
                                        tilFrequency.setError(null);
                                    }
                                    voltage = Float.parseFloat(etVoltage.getText().toString());
                                } else if ("PCS".equals(voltageSource)) {
                                    if (TextUtils.isEmpty(etFrequency.getText().toString())) {
                                        tilFrequency.setError(ExperimentErrorStrings.ERROR_MESSAGE);
                                        return;
                                    } else if (Float.parseFloat(etFrequency.getText().toString()) < 0.0f) {
                                        tilFrequency.setError(ExperimentErrorStrings.MINIMUM_VALUE_CURRENT);
                                        return;
                                    } else if (Float.parseFloat(etFrequency.getText().toString()) > 3.3f) {
                                        tilFrequency.setError(ExperimentErrorStrings.MAXIMUM_VALUE_CURRENT);
                                        return;
                                    } else {
                                        tilFrequency.setError(null);
                                    }
                                    current = Float.parseFloat(etVoltage.getText().toString());
                                }

                                waveGenerator = spinnerWaveGenerator.getSelectedItem().toString();
                                waveType = spinnerWaveType.getSelectedItem().toString();

                                if (TextUtils.isEmpty(etFrequency.getText().toString())) {
                                    tilFrequency.setError(ExperimentErrorStrings.ERROR_MESSAGE);
                                    return;
                                } else if (Float.parseFloat(etFrequency.getText().toString()) < 10.0f) {
                                    tilFrequency.setError(ExperimentErrorStrings.MINIMUM_VALUE_FREQUENCY);
                                    return;
                                } else if (Float.parseFloat(etFrequency.getText().toString()) > 5000.0f) {
                                    tilFrequency.setError(ExperimentErrorStrings.MAXIMUM_VALUE_FREQUENCY);
                                    return;
                                } else {
                                    tilFrequency.setError(null);
                                }
                                frequency = Float.parseFloat(etFrequency.getText().toString());

                                if (TextUtils.isEmpty(etPhase.getText().toString())) {
                                    tilPhase.setError(ExperimentErrorStrings.ERROR_MESSAGE);
                                    return;
                                } else if (Float.parseFloat(etPhase.getText().toString()) < 0.0f) {
                                    tilPhase.setError(ExperimentErrorStrings.MINIMUM_VALUE_PHASE);
                                    return;
                                } else if (Float.parseFloat(etPhase.getText().toString()) > 180.0f) {
                                    tilPhase.setError(ExperimentErrorStrings.MAXIMUM_VALUE_PHASE);
                                    return;
                                } else {
                                    tilPhase.setError(null);
                                }
                                phase = Float.parseFloat(etPhase.getText().toString());

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
                for (float i = 0; i < 2000; i += 1) {
                    new SummingJunctionExperiment.CalcDataPoint().execute(i);
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
        List<Entry> temp3 = new ArrayList<>();
        for (int i = 0; i < timeAxis.size(); i++) {
            temp1.add(new Entry(timeAxis.get(i), voltageAxis1.get(i)));
            temp2.add(new Entry(timeAxis.get(i), voltageAxis2.get(i)));
            temp3.add(new Entry(timeAxis.get(i), voltageAxis3.get(i)));
        }
        LineDataSet dataSetCh1 = new LineDataSet(temp1, "CH1");
        LineDataSet dataSetCh2 = new LineDataSet(temp2, "CH2");
        LineDataSet dataSetCh3 = new LineDataSet(temp3, "CH3");
        dataSetCh1.setColor(Color.RED);
        dataSetCh1.setDrawValues(false);
        dataSetCh1.setDrawCircles(false);
        dataSetCh2.setColor(Color.BLUE);
        dataSetCh2.setDrawValues(false);
        dataSetCh2.setDrawCircles(false);
        dataSetCh3.setColor(Color.GREEN);
        dataSetCh3.setDrawValues(false);
        dataSetCh3.setDrawCircles(false);
        dataSets.add(dataSetCh1);
        dataSets.add(dataSetCh2);
        dataSets.add(dataSetCh3);
        outputChart.setData(new LineData(dataSets));
        outputChart.invalidate();
    }

    private class CalcDataPoint extends AsyncTask<Float, Void, Void> {

        @Override
        protected Void doInBackground(Float... params) {
            float i = params[0];
            if ("W1".equals(waveGenerator))
                scienceLab.setW1(frequency, waveType);
            if ("W2".equals(waveGenerator))
                scienceLab.setW2(frequency, waveType);

            if ("PV1".equals(voltageSource))
                scienceLab.setPV1(voltage);
            else if ("PV2".equals(voltageSource))
                scienceLab.setPV2(voltage);
            else if ("PV3".equals(voltageSource))
                scienceLab.setPV3(voltage);
            else if ("PCS".equals(voltageSource))
                scienceLab.setPCS(current);

            readVoltageCH1 = (float) scienceLab.getVoltage("CH1", 10);
            readVoltageCH2 = (float) scienceLab.getVoltage("CH2", 10);
            readVoltageCH3 = (float) scienceLab.getVoltage("CH3", 10);
            timeAxis.add(i);
            voltageAxis1.add(readVoltageCH1);
            voltageAxis2.add(readVoltageCH2);
            voltageAxis3.add(readVoltageCH3);
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