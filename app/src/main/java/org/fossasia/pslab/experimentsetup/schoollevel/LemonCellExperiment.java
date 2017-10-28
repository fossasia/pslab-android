package org.fossasia.pslab.experimentsetup.schoollevel;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

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
 * Created by Padmal on 8/11/17.
 */

public class LemonCellExperiment extends Fragment {

    private LineChart outputChart;
    private ScienceLab scienceLab = ScienceLabCommon.scienceLab;
    private final Object lock = new Object();
    private int steps = 2000;
    private ArrayList<Float> voltageAxis = new ArrayList<>();
    private ArrayList<Float> timeAxis = new ArrayList<>();

    public static LemonCellExperiment newInstance() {
        return new LemonCellExperiment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.common_experiment_setup, container, false);
        outputChart = (LineChart) view.findViewById(R.id.line_chart);
        chartInit();
        Button btnConfigure = (Button) view.findViewById(R.id.btn_configure_dialog);
        btnConfigure.setText(getString(R.string.start_experiment));
        btnConfigure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (scienceLab.isConnected()) {
                    startExperiment();
                } else {
                    Toast.makeText(getContext(), "Device not connected", Toast.LENGTH_SHORT).show();
                }
            }
        });

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
                for (int i = 0; i < steps; i++) {
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
        for (int i = 0; i < timeAxis.size(); i++) {
            temp.add(new Entry(timeAxis.get(i), voltageAxis.get(i)));
        }
        LineDataSet dataSet = new LineDataSet(temp, "Lemon Cell Voltage Level");
        dataSet.setColor(Color.RED);
        dataSet.setDrawValues(false);
        dataSet.setDrawCircles(false);
        dataSets.add(dataSet);
        outputChart.setData(new LineData(dataSets));
        outputChart.invalidate();
    }

    private class CalcDataPoint extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... steps) {
            float step = (float) steps[0];
            float voltage = (float) scienceLab.getVoltage("CH1", 10);
            timeAxis.add(step);
            voltageAxis.add(voltage);
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
