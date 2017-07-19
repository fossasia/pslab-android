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
    private LineChart outputChart;
    private float initialVoltage = 0;
    private float finalVoltage = 0;
    private float stepVoltage = 0;
    private final Object lock = new Object();
    private ScienceLab scienceLab = ScienceLabCommon.scienceLab;
    private ArrayList<Float> x1 = new ArrayList<>();
    private ArrayList<Float> y1 = new ArrayList<>();

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
                                initialVoltage = Float.parseFloat(etInitialVoltage.getText().toString());
                                finalVoltage = Float.parseFloat(etFinalVoltage.getText().toString());
                                stepVoltage = Float.parseFloat(etStepSize.getText().toString());
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        startExperiment();
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

