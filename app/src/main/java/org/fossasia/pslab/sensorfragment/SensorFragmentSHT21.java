package org.fossasia.pslab.sensorfragment;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import org.fossasia.pslab.communication.sensors.SHT21;
import org.fossasia.pslab.others.ScienceLabCommon;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by asitava on 10/7/17.
 */

public class SensorFragmentSHT21 extends Fragment {
    private ScienceLab scienceLab;
    private I2C i2c;
    private SensorDataFetch sensorDataFetch;
    private TextView tvSensorSHT21Temp;
    private TextView tvSensorSHT21Humidity;
    private SHT21 sensorSHT21;
    private LineChart mChartTemperature;
    private LineChart mChartHumidity;
    private long startTime;
    private int flag;
    private XAxis xTemperature;
    private YAxis yTemperature;
    private YAxis yTemperature2;
    private XAxis xHumidity;
    private YAxis yHumidity;
    private YAxis yHumidity2;
    private ArrayList<Entry> entriesTemperature;
    private ArrayList<Entry> entriesHumidity;
    private final Object lock = new Object();

    public static SensorFragmentSHT21 newInstance() {
        SensorFragmentSHT21 sensorFragmentSHT21 = new SensorFragmentSHT21();
        return sensorFragmentSHT21;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scienceLab = ScienceLabCommon.scienceLab;
        i2c = scienceLab.i2c;
        try {
            sensorSHT21 = new SHT21(i2c);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        entriesTemperature = new ArrayList<>();
        entriesHumidity = new ArrayList<>();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (scienceLab.isConnected()) {
                        try {
                            sensorDataFetch = new SensorDataFetch();
                        } catch (IOException | InterruptedException e) {
                            e.printStackTrace();
                        }
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
        View view = inflater.inflate(R.layout.sensor_sht21, container, false);

        tvSensorSHT21Temp = (TextView) view.findViewById(R.id.tv_sensor_sht21_temp);
        tvSensorSHT21Humidity = (TextView) view.findViewById(R.id.tv_sensor_sht21_humidity);
        mChartTemperature = (LineChart) view.findViewById(R.id.chart_temperature_sht21);
        mChartHumidity = (LineChart) view.findViewById(R.id.chart_humidity_sht21);

        xTemperature = mChartTemperature.getXAxis();
        yTemperature = mChartTemperature.getAxisLeft();
        yTemperature2 = mChartTemperature.getAxisRight();

        xHumidity = mChartHumidity.getXAxis();
        yHumidity = mChartHumidity.getAxisLeft();
        yHumidity2 = mChartHumidity.getAxisRight();

        mChartTemperature.setTouchEnabled(true);
        mChartTemperature.setHighlightPerDragEnabled(true);
        mChartTemperature.setDragEnabled(true);
        mChartTemperature.setScaleEnabled(true);
        mChartTemperature.setDrawGridBackground(false);
        mChartTemperature.setPinchZoom(true);
        mChartTemperature.setScaleYEnabled(false);
        mChartTemperature.setBackgroundColor(Color.BLACK);
        mChartTemperature.getDescription().setEnabled(false);

        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);
        mChartTemperature.setData(data);

        Legend l = mChartTemperature.getLegend();
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.WHITE);

        xTemperature.setTextColor(Color.WHITE);
        xTemperature.setDrawGridLines(true);
        xTemperature.setAvoidFirstLastClipping(true);

        yTemperature.setTextColor(Color.WHITE);
        yTemperature.setAxisMaximum(125f);
        yTemperature.setAxisMinimum(-40f);
        yTemperature.setDrawGridLines(true);
        yTemperature.setLabelCount(10);

        yTemperature2.setDrawGridLines(false);

        mChartHumidity.setTouchEnabled(true);
        mChartHumidity.setHighlightPerDragEnabled(true);
        mChartHumidity.setDragEnabled(true);
        mChartHumidity.setScaleEnabled(true);
        mChartHumidity.setDrawGridBackground(false);
        mChartHumidity.setPinchZoom(true);
        mChartHumidity.setScaleYEnabled(false);
        mChartHumidity.setBackgroundColor(Color.BLACK);
        mChartHumidity.getDescription().setEnabled(false);

        LineData data2 = new LineData();
        data.setValueTextColor(Color.WHITE);
        mChartHumidity.setData(data2);

        Legend l2 = mChartHumidity.getLegend();
        l2.setForm(Legend.LegendForm.LINE);
        l2.setTextColor(Color.WHITE);

        xHumidity.setTextColor(Color.WHITE);
        xHumidity.setDrawGridLines(true);
        xHumidity.setAvoidFirstLastClipping(true);

        yHumidity.setTextColor(Color.WHITE);
        yHumidity.setAxisMaximum(100f);
        yHumidity.setAxisMinimum(0f);
        yHumidity.setDrawGridLines(true);
        yHumidity.setLabelCount(10);

        yHumidity2.setDrawGridLines(false);

        return view;
    }


    private class SensorDataFetch extends AsyncTask<Void, Void, Void> {
        private ArrayList<Double> dataSHT21Temp = new ArrayList<>();
        private ArrayList<Double> dataSHT21Humidity = new ArrayList<>();
        private long timeElapsed;
        ;

        private SensorDataFetch() throws IOException, InterruptedException {
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (sensorSHT21 != null) {
                    sensorSHT21.selectParameter("temperature");
                    dataSHT21Temp = sensorSHT21.getRaw();
                    sensorSHT21.selectParameter("humidity");
                    dataSHT21Humidity = sensorSHT21.getRaw();
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

            timeElapsed = (System.currentTimeMillis() - startTime) / 1000;

            entriesTemperature.add(new Entry((float) timeElapsed, dataSHT21Temp.get(0).floatValue()));
            entriesTemperature.add(new Entry((float) timeElapsed, dataSHT21Humidity.get(0).floatValue()));

            return null;
        }

        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            tvSensorSHT21Temp.setText(String.valueOf(dataSHT21Temp.get(0)));
            tvSensorSHT21Humidity.setText(String.valueOf(dataSHT21Humidity.get(0)));

            LineDataSet dataset1 = new LineDataSet(entriesTemperature, "Ax");
            LineDataSet dataset2 = new LineDataSet(entriesHumidity, "Ay");

            dataset1.setDrawCircles(true);
            dataset2.setDrawCircles(true);

            LineData data = new LineData(dataset1);
            mChartTemperature.setData(data);
            mChartTemperature.notifyDataSetChanged();
            mChartTemperature.setVisibleXRangeMaximum(10);
            mChartTemperature.moveViewToX(data.getEntryCount());
            mChartTemperature.invalidate();

            LineData data2 = new LineData(dataset2);
            mChartHumidity.setData(data2);
            mChartHumidity.notifyDataSetChanged();
            mChartHumidity.setVisibleXRangeMaximum(10);
            mChartHumidity.moveViewToX(data2.getEntryCount());
            mChartHumidity.invalidate();

            synchronized (lock) {
                lock.notify();
            }
        }
    }
}
