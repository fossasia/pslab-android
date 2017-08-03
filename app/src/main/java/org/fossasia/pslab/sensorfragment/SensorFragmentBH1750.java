package org.fossasia.pslab.sensorfragment;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.fossasia.pslab.R;
import org.fossasia.pslab.communication.ScienceLab;
import org.fossasia.pslab.communication.peripherals.I2C;
import org.fossasia.pslab.communication.sensors.BH1750;
import org.fossasia.pslab.others.ScienceLabCommon;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by asitava on 10/7/17.
 */

public class SensorFragmentBH1750 extends Fragment {
    private ScienceLab scienceLab;
    private I2C i2c;
    private SensorDataFetch sensorDataFetch;
    private TextView tvSensorBH1750Luminosity;
    private BH1750 sensorBH1750;
    private LineChart mChart;
    private long startTime;
    private int flag;
    private XAxis x;
    private YAxis y;
    private YAxis y2;
    private ArrayList<Entry> entries;
    private final Object lock = new Object();

    public static SensorFragmentBH1750 newInstance() {
        SensorFragmentBH1750 sensorFragmentBH1750 = new SensorFragmentBH1750();
        return sensorFragmentBH1750;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scienceLab = ScienceLabCommon.scienceLab;
        i2c = scienceLab.i2c;
        entries = new ArrayList<Entry>();

        try {
            sensorBH1750 = new BH1750(i2c);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (scienceLab.isConnected()) {
                        sensorDataFetch = new SensorDataFetch();
                        sensorDataFetch.execute();
                    }
                    if (flag == 0) {
                        startTime = System.currentTimeMillis();
                        flag = 1;
                    }
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

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sensor_bh1750, container, false);
        tvSensorBH1750Luminosity = (TextView) view.findViewById(R.id.tv_sensor_bh1750_luminosity);
        Spinner spinnerSensorBH1750 = (Spinner) view.findViewById(R.id.spinner_sensor_bh1750);
        mChart = (LineChart) view.findViewById(R.id.chart_bh1750);
        x = mChart.getXAxis();
        y = mChart.getAxisLeft();
        y2 = mChart.getAxisRight();

        mChart.setTouchEnabled(true);
        mChart.setHighlightPerDragEnabled(true);
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);
        mChart.setPinchZoom(true);
        mChart.setScaleYEnabled(false);
        mChart.setBackgroundColor(Color.BLACK);
        mChart.getDescription().setEnabled(false);

        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);
        mChart.setData(data);

        Legend l = mChart.getLegend();
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.WHITE);

        x.setTextColor(Color.WHITE);
        x.setDrawGridLines(true);
        x.setAvoidFirstLastClipping(true);

        y.setTextColor(Color.WHITE);
        y.setAxisMaximum(10f);
        y.setAxisMinimum(-10f);
        y.setDrawGridLines(true);
        y.setLabelCount(10);

        y2.setDrawGridLines(false);

        try {
            if (sensorBH1750 != null) {
                sensorBH1750.setRange(spinnerSensorBH1750.getSelectedItem().toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return view;
    }

    private class SensorDataFetch extends AsyncTask<Void, Void, Void> {
        Double dataBH1750;
        long timeElapsed;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (sensorBH1750 != null) {
                    dataBH1750 = sensorBH1750.getRaw();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            tvSensorBH1750Luminosity.setText(String.valueOf(dataBH1750));

            timeElapsed = (System.currentTimeMillis() - startTime) / 1000;
            entries.add(new Entry((float) timeElapsed, dataBH1750.floatValue()));
            LineDataSet dataset = new LineDataSet(entries, "Bx");
            dataset.setDrawCircles(true);

            LineData data = new LineData(dataset);
            mChart.setData(data);
            mChart.notifyDataSetChanged();
            mChart.setVisibleXRangeMaximum(10);
            mChart.moveViewToX(data.getEntryCount());
            mChart.invalidate();

            synchronized (lock) {
                lock.notify();
            }
        }
    }
}