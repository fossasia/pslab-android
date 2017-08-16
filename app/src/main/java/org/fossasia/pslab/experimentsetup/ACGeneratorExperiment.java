package org.fossasia.pslab.experimentsetup;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
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

import org.fossasia.pslab.R;
import org.fossasia.pslab.communication.ScienceLab;
import org.fossasia.pslab.others.ScienceLabCommon;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Padmal on 8/16/17.
 */

public class ACGeneratorExperiment extends Fragment {

    private LineChart outputChart;
    private ScienceLab scienceLab = ScienceLabCommon.scienceLab;
    private final Object lock = new Object();

    public static ACGeneratorExperiment newInstance() {
        return new ACGeneratorExperiment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.diode_setup, container, false);
        outputChart = (LineChart) view.findViewById(R.id.line_chart);
        chartInit();
        Button btnConfigure = (Button) view.findViewById(R.id.btn_configure_dialog);
        btnConfigure.setText(getString(R.string.start_experiment));
        btnConfigure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                while (true) {
                    new RecordWave().execute();
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

    public class RecordWave extends AsyncTask<String, Void, Void> {

        ArrayList<Entry> entries;

        @Override
        protected Void doInBackground(String... params) {
            try {
                scienceLab.configureTrigger(0, "CH1", 0, null, null);
                scienceLab.captureTraces(1, 1000, 10, "CH1", true, null);

                Thread.sleep((long) (1000 * 10 * 1e-3));
                HashMap<String, double[]> data = scienceLab.fetchTrace(1);

                double[] xData = data.get("x");
                double[] yData = data.get("y");
                entries = new ArrayList<>();
                for (int i = 0; i < xData.length; i++) {
                    entries.add(new Entry((float) xData[i] / 1000, (float) yData[i]));
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
            LineDataSet dataset = new LineDataSet(entries, "CH1");
            LineData lineData = new LineData(dataset);
            dataset.setDrawCircles(false);
            outputChart.setData(lineData);
            outputChart.notifyDataSetChanged();
            outputChart.invalidate();
            synchronized (lock) {
                lock.notify();
            }
        }
    }
}
