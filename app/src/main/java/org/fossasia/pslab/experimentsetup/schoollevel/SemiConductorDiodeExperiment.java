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
import java.util.HashMap;
import java.util.List;

/**
 * Created by Padmal on 8/21/17.
 */

public class SemiConductorDiodeExperiment extends Fragment {

    private final Object lock = new Object();
    private LineChart outputChart;
    private ScienceLab scienceLab = ScienceLabCommon.scienceLab;
    private ArrayList<Float> voltageAxisCH1 = new ArrayList<>();
    private ArrayList<Float> voltageAxisCH2 = new ArrayList<>();
    private boolean runAC, runDC;

    public static SemiConductorDiodeExperiment newInstance() {
        return new SemiConductorDiodeExperiment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.semiconductor_experiment_layout, container, false);
        outputChart = (LineChart) view.findViewById(R.id.line_chart);
        chartInit();
        Button experimentAC = (Button) view.findViewById(R.id.btn_experiment_ac);
        experimentAC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (scienceLab.isConnected()) {
                    startACExperiment();
                } else {
                    Toast.makeText(getContext(), "Device not connected", Toast.LENGTH_SHORT).show();
                }
            }
        });
        Button experimentDC = (Button) view.findViewById(R.id.btn_experiment_dc);
        experimentDC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (scienceLab.isConnected()) {
                    startDCExperiment();
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

    private void startACExperiment() {
        runAC = true;
        runDC = false;
        outputChart.clear();
        scienceLab.setW1(10, "sine");
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (runAC) {
                    new RecordACVoltage().execute();
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

    private void startDCExperiment() {
        runDC = true;
        runAC = false;
        outputChart.clear();
        scienceLab.setPV3(2.0f);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (runDC) {
                    new RecordDCVoltage().execute();
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
        List<Entry> ch1Values = new ArrayList<>();
        List<Entry> ch2Values = new ArrayList<>();
        for (int i = 0; i < voltageAxisCH1.size(); i++) {
            ch1Values.add(new Entry(i, voltageAxisCH1.get(i)));
            ch2Values.add(new Entry(i, voltageAxisCH2.get(i)));
        }

        LineDataSet dataSetCH1 = new LineDataSet(ch1Values, "CH1 (Input)");
        LineDataSet dataSetCH2 = new LineDataSet(ch2Values, "CH2 (Output)");

        dataSetCH1.setColor(Color.GREEN);
        dataSetCH2.setColor(Color.RED);

        dataSetCH1.setDrawValues(false);
        dataSetCH2.setDrawValues(false);

        dataSetCH1.setDrawCircles(false);
        dataSetCH2.setDrawCircles(false);

        dataSets.add(dataSetCH1);
        dataSets.add(dataSetCH2);

        outputChart.setData(new LineData(dataSets));
        outputChart.notifyDataSetChanged();
        outputChart.invalidate();
    }

    private class RecordACVoltage extends AsyncTask<Integer, Void, Void> {

        ArrayList<Entry> ch1Data;
        ArrayList<Entry> ch2Data;

        @Override
        protected Void doInBackground(Integer... params) {
            try {
                HashMap<String, double[]> data;
                data = scienceLab.captureTwo(1000, 10, "CH1", false);
                double[] xData = data.get("x");
                double[] y1Data = data.get("y1"); // Input --> CH1
                double[] y2Data = data.get("y2"); // Output --> CH2
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

            LineDataSet dataset1 = new LineDataSet(ch1Data, "CH1 (Input)");
            LineDataSet dataSet2 = new LineDataSet(ch2Data, "CH2 (Output)");

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

    private class RecordDCVoltage extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                float voltageCH1 = (float) scienceLab.getVoltage("CH1", 10);
                float voltageCH2 = (float) scienceLab.getVoltage("CH2", 10);
                voltageAxisCH1.add(voltageCH1);
                voltageAxisCH2.add(voltageCH2);
            } catch (NullPointerException e) {
                cancel(true);
            }
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
