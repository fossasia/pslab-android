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
import org.fossasia.pslab.others.ScienceLabCommon;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by viveksb007 on 15/7/17.
 */

public class ZenerSetupFragment extends Fragment {

    private static final String ERROR_MESSAGE = "Invalid Value";
    private static final String INVALID_VALUE = "Voltage value too low";
    private static final String MINIMUM_VALUE = "Voltage is beyond minimum of -5V";
    private static final String MAXIMUM_VALUE = "Voltage is beyond maximum of 5V";
    private LineChart outputChart;
    private float initialVoltage = 0;
    private float finalVoltage = 0;
    private float stepVoltage = 0;
    private final Object lock = new Object();
    private ScienceLab scienceLab = ScienceLabCommon.scienceLab;
    private ArrayList<Float> x1 = new ArrayList<>();
    private ArrayList<Float> y1 = new ArrayList<>();
    private TextInputEditText etInitialVoltage, etFinalVoltage, etStepSize;
    private TextInputLayout tilInitialVoltage, tilFinalVoltage, tilStepSize;

    public static ZenerSetupFragment newInstance() {
        return new ZenerSetupFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.diode_setup, container, false);
        outputChart = (LineChart) view.findViewById(R.id.line_chart);

        Button btnConfigure = (Button) view.findViewById(R.id.btn_configure_dialog);
        btnConfigure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // open Material Dialog
                MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                        .title("Configure Experiment")
                        .customView(R.layout.diode_configure_dialog, true)
                        .positiveText("Start Experiment")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                View customView = dialog.getCustomView();
                                assert customView != null;
                                etInitialVoltage = (TextInputEditText) customView.findViewById(R.id.et_initial_voltage);
                                etFinalVoltage = (TextInputEditText) customView.findViewById(R.id.et_final_voltage);
                                etStepSize = (TextInputEditText) customView.findViewById(R.id.et_step_size);
                                tilInitialVoltage = (TextInputLayout) customView.findViewById(R.id.text_input_layout_iv);
                                tilFinalVoltage = (TextInputLayout) customView.findViewById(R.id.text_input_layout_fv);
                                tilStepSize = (TextInputLayout) customView.findViewById(R.id.text_input_layout_ss);
                                // Initial Voltage
                                if (TextUtils.isEmpty(etInitialVoltage.getText().toString())) {
                                    tilInitialVoltage.setError(ERROR_MESSAGE);
                                    return;
                                } else if (Float.parseFloat(etInitialVoltage.getText().toString()) < -5.0f) {
                                    tilInitialVoltage.setError(MINIMUM_VALUE);
                                    return;
                                } else if (Float.parseFloat(etInitialVoltage.getText().toString()) > 5.0f) {
                                    tilInitialVoltage.setError(MAXIMUM_VALUE);
                                    return;
                                } else {
                                    tilInitialVoltage.setError(null);
                                }
                                initialVoltage = Float.parseFloat(etInitialVoltage.getText().toString());
                                // Final Voltage
                                if (TextUtils.isEmpty(etFinalVoltage.getText().toString())) {
                                    tilFinalVoltage.setError(ERROR_MESSAGE);
                                    return;
                                } else if (initialVoltage >= Float.parseFloat(etFinalVoltage.getText().toString())) {
                                    tilFinalVoltage.setError(INVALID_VALUE);
                                    return;
                                } else if (Float.parseFloat(etFinalVoltage.getText().toString()) < -5.0f) {
                                    tilFinalVoltage.setError(MINIMUM_VALUE);
                                    return;
                                } else if (Float.parseFloat(etFinalVoltage.getText().toString()) > 5.0f) {
                                    tilFinalVoltage.setError(MAXIMUM_VALUE);
                                    return;
                                } else {
                                    tilFinalVoltage.setError(null);
                                }
                                finalVoltage = Float.parseFloat(etFinalVoltage.getText().toString());
                                // Step Size
                                if (TextUtils.isEmpty(etStepSize.getText().toString())) {
                                    tilStepSize.setError(ERROR_MESSAGE);
                                    return;
                                } else {
                                    tilStepSize.setError(null);
                                }
                                stepVoltage = Float.parseFloat(etStepSize.getText().toString());
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (scienceLab.isConnected())
                                            startExperiment();
                                        else
                                            Toast.makeText(getContext(), "Device not connected", Toast.LENGTH_SHORT).show();
                                    }
                                }, 100);
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

    private void startExperiment() {
        // code for changing voltage from IV to FV and read current value for each sample
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                for (float i = initialVoltage; i < finalVoltage; i += stepVoltage) {
                    CalcDataPoint dataPoint = new CalcDataPoint(outputChart, i);
                    dataPoint.execute();
                    synchronized (lock) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        updateChart();
                    }
                });
            }
        };
        new Thread(runnable).start();
    }

    private void chartInit() {
        outputChart.setTouchEnabled(true);
        outputChart.setDragEnabled(true);
        outputChart.setScaleEnabled(true);
        //outputChart.setDrawGridBackground(false);
        outputChart.setPinchZoom(true);
        LineData data = new LineData();
        outputChart.setData(data);
    }

    private void updateChart() {
        List<ILineDataSet> dataSets = new ArrayList<>();
        List<Entry> temp = new ArrayList<>();
        for (int i = 0; i < x1.size(); i++) {
            temp.add(new Entry(x1.get(i), y1.get(i)));
        }
        LineDataSet dataSet = new LineDataSet(temp, "I-V Characteristic");
        dataSet.setColor(Color.RED);
        dataSet.setDrawValues(false);
        dataSet.setDrawCircles(false);
        dataSets.add(dataSet);
        outputChart.setData(new LineData(dataSets));
        outputChart.invalidate();
    }


    private class CalcDataPoint extends AsyncTask<Void, Void, Void> {

        private LineChart chart;
        private float volti, voltf, x, y;

        CalcDataPoint(LineChart chart, float volti) {
            this.chart = chart;
            this.volti = volti;
        }

        @Override
        protected Void doInBackground(Void... params) {
            scienceLab.setPV1(volti);
            voltf = (float) scienceLab.getVoltage("CH1", 10);
            x = voltf;
            y = (float) ((volti - voltf) / 1.e3);
            x1.add(x);
            y1.add(y);
            Log.v("XY", "" + x + " , " + y);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            /*
            LineData data = chart.getData();
            if (data != null) {
                ILineDataSet set = data.getDataSetByIndex(0);
                if (set == null) {
                    LineDataSet lSet = new LineDataSet(null, "DD");
                    lSet.setCircleColor(Color.WHITE);
                    set = lSet;
                }
                set.addEntry(new Entry(x, y));
                data.notifyDataChanged();
                chart.notifyDataSetChanged();
                chart.invalidate();
            }
            */
            synchronized (lock) {
                lock.notify();
            }
        }
    }

}

