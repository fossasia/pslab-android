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
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.fossasia.pslab.R;
import org.fossasia.pslab.communication.ScienceLab;
import org.fossasia.pslab.communication.peripherals.I2C;
import org.fossasia.pslab.communication.sensors.ADS1115;
import org.fossasia.pslab.others.ScienceLabCommon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by asitava on 10/7/17.
 */

public class SensorFragmentADS1115 extends Fragment {
    private ScienceLab scienceLab;
    private I2C i2c;
    private SensorDataFetch sensorDataFetch;
    private TextView tvSensorADS1115;
    private LineChart mChart;
    private long startTime;
    private int flag;
    private XAxis x;
    private YAxis y;
    private YAxis y2;
    private ArrayList<Entry> entries;
    private final Object lock = new Object();

    private ADS1115 sensorADS1115;

    public static SensorFragmentADS1115 newInstance() {
        SensorFragmentADS1115 sensorFragmentADS1115 = new SensorFragmentADS1115();
        return sensorFragmentADS1115;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scienceLab = ScienceLabCommon.scienceLab;
        i2c = scienceLab.i2c;
        entries = new ArrayList<Entry>();
        try {
            sensorADS1115 = new ADS1115(i2c);
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
                        if (flag == 0) {
                            startTime = System.currentTimeMillis();
                            flag = 1;
                        }
                    }
                    synchronized (lock) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        new Thread(runnable).start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sensor_ads1115, container, false);

        tvSensorADS1115 = (TextView) view.findViewById(R.id.tv_sensor_ads1115);
        mChart = (LineChart) view.findViewById(R.id.chart_sensor_ads);

        Spinner spinnerSensorADS1115Gain = (Spinner) view.findViewById(R.id.spinner_sensor_ads1115_gain);
        Spinner spinnerSensorADS1115Channel = (Spinner) view.findViewById(R.id.spinner_sensor_ads1115_channel);
        Spinner spinnerSensorADS1115Rate = (Spinner) view.findViewById(R.id.spinner_sensor_ads1115_rate);

        if (sensorADS1115 != null) {
            sensorADS1115.setGain(spinnerSensorADS1115Gain.getSelectedItem().toString());
        }

        if (sensorADS1115 != null) {
            sensorADS1115.setChannel(spinnerSensorADS1115Channel.getSelectedItem().toString());
        }
        if (sensorADS1115 != null) {
            sensorADS1115.setDataRate(Integer.parseInt(spinnerSensorADS1115Rate.getSelectedItem().toString()));
        }
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
        y.setAxisMaximum(6.15f);
        y.setAxisMinimum(-6.15f);
        y.setDrawGridLines(true);
        y.setLabelCount(10);

        y2.setDrawGridLines(false);

        return view;
    }

    private class SensorDataFetch extends AsyncTask<Void, Void, Void> {
        private int dataADS1115;
        long timeElapsed;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (sensorADS1115 != null) {
                    dataADS1115 = sensorADS1115.getRaw();
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

            timeElapsed = (System.currentTimeMillis() - startTime) / 1000;
            entries.add(new Entry((float) timeElapsed, dataADS1115));

            return null;
        }

        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            tvSensorADS1115.setText(String.valueOf(dataADS1115));

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

