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
import org.fossasia.pslab.communication.sensors.MLX90614;
import org.fossasia.pslab.others.ScienceLabCommon;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by asitava on 10/7/17.
 */

public class SensorFragmentMLX90614 extends Fragment {
    private ScienceLab scienceLab;
    private I2C i2c;
    private SensorFragmentMLX90614.SensorDataFetch sensorDataFetch;
    private TextView tvSensorMLX90614ObjectTemp;
    private TextView tvSensorMLX90614AmbientTemp;
    private MLX90614 sensorMLX90614;
    private LineChart mChartObjectTemperature;
    private LineChart mChartAmbientTemperature;
    private long startTime;
    private int flag;
    private XAxis xObjectTemperature;
    private YAxis yObjectTemperature;
    private YAxis yObjectTemperature2;
    private XAxis xAmbientTemperature;
    private YAxis yAmbientTemperature;
    private YAxis yAmbientTemperature2;
    private ArrayList<Entry> entriesObjectTemperature;
    private ArrayList<Entry> entriesAmbientTemperature;
    private final Object lock = new Object();


    public static SensorFragmentMLX90614 newInstance() {
        SensorFragmentMLX90614 sensorFragmentMLX90614 = new SensorFragmentMLX90614();
        return sensorFragmentMLX90614;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scienceLab = ScienceLabCommon.scienceLab;
        i2c = scienceLab.i2c;
        try {
            sensorMLX90614 = new MLX90614(i2c);
        } catch (IOException e) {
            e.printStackTrace();
        }
        entriesObjectTemperature = new ArrayList<>();
        entriesAmbientTemperature = new ArrayList<>();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (scienceLab.isConnected()) {
                        sensorDataFetch = new SensorFragmentMLX90614.SensorDataFetch();
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
        View view = inflater.inflate(R.layout.sensor_mlx90614, container, false);

        tvSensorMLX90614ObjectTemp = (TextView) view.findViewById(R.id.tv_sensor_mlx90614_object_temp);
        tvSensorMLX90614AmbientTemp = (TextView) view.findViewById(R.id.tv_sensor_mlx90614_ambient_temp);

        mChartObjectTemperature = (LineChart) view.findViewById(R.id.chart_obj_temp_mlx);
        mChartAmbientTemperature = (LineChart) view.findViewById(R.id.chart_amb_temp_mlx);

        xObjectTemperature = mChartObjectTemperature.getXAxis();
        yObjectTemperature = mChartObjectTemperature.getAxisLeft();
        yObjectTemperature2 = mChartObjectTemperature.getAxisRight();

        xAmbientTemperature = mChartAmbientTemperature.getXAxis();
        yAmbientTemperature = mChartAmbientTemperature.getAxisLeft();
        yAmbientTemperature2 = mChartAmbientTemperature.getAxisRight();

        mChartObjectTemperature.setTouchEnabled(true);
        mChartObjectTemperature.setHighlightPerDragEnabled(true);
        mChartObjectTemperature.setDragEnabled(true);
        mChartObjectTemperature.setScaleEnabled(true);
        mChartObjectTemperature.setDrawGridBackground(false);
        mChartObjectTemperature.setPinchZoom(true);
        mChartObjectTemperature.setScaleYEnabled(false);
        mChartObjectTemperature.setBackgroundColor(Color.BLACK);
        mChartObjectTemperature.getDescription().setEnabled(false);

        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);
        mChartObjectTemperature.setData(data);

        Legend l = mChartObjectTemperature.getLegend();
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.WHITE);

        xObjectTemperature.setTextColor(Color.WHITE);
        xObjectTemperature.setDrawGridLines(true);
        xObjectTemperature.setAvoidFirstLastClipping(true);

        yObjectTemperature.setTextColor(Color.WHITE);
        yObjectTemperature.setAxisMaximum(125f);
        yObjectTemperature.setAxisMinimum(-40f);
        yObjectTemperature.setDrawGridLines(true);
        yObjectTemperature.setLabelCount(10);

        yObjectTemperature2.setDrawGridLines(false);

        mChartAmbientTemperature.setTouchEnabled(true);
        mChartAmbientTemperature.setHighlightPerDragEnabled(true);
        mChartAmbientTemperature.setDragEnabled(true);
        mChartAmbientTemperature.setScaleEnabled(true);
        mChartAmbientTemperature.setDrawGridBackground(false);
        mChartAmbientTemperature.setPinchZoom(true);
        mChartAmbientTemperature.setScaleYEnabled(false);
        mChartAmbientTemperature.setBackgroundColor(Color.BLACK);
        mChartAmbientTemperature.getDescription().setEnabled(false);

        LineData data2 = new LineData();
        data.setValueTextColor(Color.WHITE);
        mChartAmbientTemperature.setData(data2);

        Legend l2 = mChartAmbientTemperature.getLegend();
        l2.setForm(Legend.LegendForm.LINE);
        l2.setTextColor(Color.WHITE);

        xAmbientTemperature.setTextColor(Color.WHITE);
        xAmbientTemperature.setDrawGridLines(true);
        xAmbientTemperature.setAvoidFirstLastClipping(true);

        yAmbientTemperature.setTextColor(Color.WHITE);
        yAmbientTemperature.setAxisMaximum(380f);
        yAmbientTemperature.setAxisMinimum(-70f);
        yAmbientTemperature.setDrawGridLines(true);
        yAmbientTemperature.setLabelCount(10);

        yAmbientTemperature2.setDrawGridLines(false);

        return view;
    }

    private class SensorDataFetch extends AsyncTask<Void, Void, Void> {
        Double dataMLX90614ObjectTemp;
        Double dataMLX90614AmbientTemp;
        private long timeElapsed;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (sensorMLX90614 != null) {
                    dataMLX90614ObjectTemp = sensorMLX90614.getObjectTemperature();
                    dataMLX90614AmbientTemp = sensorMLX90614.getAmbientTemperature();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            timeElapsed = (System.currentTimeMillis() - startTime) / 1000;

            entriesObjectTemperature.add(new Entry((float) timeElapsed, dataMLX90614ObjectTemp.floatValue()));
            entriesAmbientTemperature.add(new Entry((float) timeElapsed, dataMLX90614AmbientTemp.floatValue()));

            return null;
        }

        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            tvSensorMLX90614ObjectTemp.setText(String.valueOf(dataMLX90614ObjectTemp));
            tvSensorMLX90614AmbientTemp.setText(String.valueOf(dataMLX90614AmbientTemp));

            LineDataSet dataset1 = new LineDataSet(entriesObjectTemperature, "Object Temperature");
            LineDataSet dataset2 = new LineDataSet(entriesAmbientTemperature, "Ambient Temperature");

            dataset1.setDrawCircles(true);
            dataset2.setDrawCircles(true);

            LineData data1 = new LineData(dataset1);
            mChartObjectTemperature.setData(data1);
            mChartObjectTemperature.notifyDataSetChanged();
            mChartObjectTemperature.setVisibleXRangeMaximum(10);
            mChartObjectTemperature.moveViewToX(data1.getEntryCount());
            mChartObjectTemperature.invalidate();

            LineData data2 = new LineData(dataset2);
            mChartAmbientTemperature.setData(data2);
            mChartAmbientTemperature.notifyDataSetChanged();
            mChartAmbientTemperature.setVisibleXRangeMaximum(10);
            mChartAmbientTemperature.moveViewToX(data2.getEntryCount());
            mChartAmbientTemperature.invalidate();

            synchronized (lock) {
                lock.notify();
            }
        }
    }
}