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
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.fossasia.pslab.R;
import org.fossasia.pslab.communication.ScienceLab;
import org.fossasia.pslab.others.MathUtils;
import org.fossasia.pslab.others.ScienceLabCommon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Padmal on 8/15/17.
 */

public class WaterResistanceExperiment extends Fragment {

    private LineChart outputChart;
    private ScienceLab scienceLab = ScienceLabCommon.scienceLab;
    private final Object lock = new Object();
    private ArrayList<Float> voltageAxisDC = new ArrayList<>();
    private TextView tv_resistance;
    private boolean runAC, runDC;

    public static WaterResistanceExperiment newInstance() {
        return new WaterResistanceExperiment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.water_resistance_measurement_layout, container, false);
        outputChart = (LineChart) view.findViewById(R.id.line_chart);
        tv_resistance = (TextView) view.findViewById(R.id.tv_water_resistance);
        chartInit();
        Button measureACResistance = (Button) view.findViewById(R.id.btn_measure_ac);
        measureACResistance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (scienceLab.isConnected()) {
                    startACExperiment();
                } else {
                    Toast.makeText(getContext(), "Device not connected", Toast.LENGTH_SHORT).show();
                }
            }
        });
        Button measureDCResistance = (Button) view.findViewById(R.id.btn_measure_dc);
        measureDCResistance.setOnClickListener(new View.OnClickListener() {
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
        scienceLab.setW1(500, "sine");
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
        List<Entry> temp = new ArrayList<>();
        for (int i = 0; i < voltageAxisDC.size(); i++) {
            temp.add(new Entry(i, voltageAxisDC.get(i)));
        }
        LineDataSet dataSet = new LineDataSet(temp, "Voltage");
        dataSet.setColor(Color.RED);
        dataSet.setDrawValues(false);
        dataSet.setDrawCircles(false);
        dataSets.add(dataSet);
        outputChart.setData(new LineData(dataSets));
        outputChart.invalidate();
    }

    private class RecordACVoltage extends AsyncTask<Integer, Void, Void> {

        ArrayList<Entry> ch1Data;
        ArrayList<Entry> ch2Data;
        float resistance;

        @Override
        protected Void doInBackground(Integer... params) {
            try {
                HashMap<String, double[]> data;
                data = scienceLab.captureTwo(1000, 10, "CH1", false);
                double[] xData = data.get("x");
                double[] y1Data = data.get("y1"); // Output --> CH1
                double[] y2Data = data.get("y2"); // Input --> CH2
                float rmsCH1 = (float) MathUtils.rms(y1Data);
                float rmsCH2 = (float) MathUtils.rms(y2Data);
                // Voltage division rule using RMS ~ DC
                resistance = (100 * (rmsCH1 - rmsCH2)) / rmsCH2;
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

            tv_resistance.setText(String.valueOf(resistance));

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

    private class RecordDCVoltage extends AsyncTask<Void, Void, Void> {

        float resistance;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                float voltageCH1 = (float) scienceLab.getVoltage("CH1", 10);
                float voltageCH2 = (float) scienceLab.getVoltage("CH2", 10);
                voltageAxisDC.add(voltageCH1 - voltageCH2);
                // Using voltage division rule
                resistance = (100 * (voltageCH1 - voltageCH2)) / voltageCH2;
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
                    tv_resistance.setText(String.valueOf(resistance));
                    updateChart();
                }
            });
            synchronized (lock) {
                lock.notify();
            }
        }
    }
}
