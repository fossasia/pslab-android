package org.fossasia.pslab.fragment;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.anastr.speedviewlib.PointerSpeedometer;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.fossasia.pslab.PSLabApplication;
import org.fossasia.pslab.R;
import org.fossasia.pslab.communication.sensors.BH1750;

import java.io.IOException;
import java.util.ArrayList;

import static android.content.Context.SENSOR_SERVICE;

public class LuxMeterFragmentData extends Fragment {

    private SensorDataFetch sensorDataFetch;
    private TextView statMax;
    private TextView statMin;
    private TextView statMean;
    private BH1750 sensorBH1750 = null;
    private SensorManager sensorManager;
    private Sensor sensor;
    private LineChart mChart;
    private long startTime;
    private int flag;
    private ArrayList<Entry> entries;
    private Thread dataThread;
    private PointerSpeedometer lightMeter;
    private float currentMin;
    private float currentMax;
    private YAxis y;

    public static LuxMeterFragmentData newInstance() {
        return new LuxMeterFragmentData();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        currentMin = 2000;
        entries = new ArrayList<>();
        sensorManager = (SensorManager) getContext().getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lux_meter_data, container, false);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                sensorDataFetch = new SensorDataFetch();
                sensorDataFetch.execute();
                if (flag == 0) {
                    startTime = System.currentTimeMillis();
                    flag = 1;
                }
            }
        };
        dataThread = new Thread(runnable);
        dataThread.start();

        statMax = (TextView) view.findViewById(R.id.lux_stat_max);
        statMin = (TextView) view.findViewById(R.id.lux_stat_min);
        statMean = (TextView) view.findViewById(R.id.lux_stat_mean);
        lightMeter = (PointerSpeedometer) view.findViewById(R.id.light_meter);

        lightMeter.setMaxSpeed(2000);

        mChart = view.findViewById(R.id.chart_lux_meter);
        XAxis x = mChart.getXAxis();
        this.y = mChart.getAxisLeft();
        YAxis y2 = mChart.getAxisRight();

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
        mChart.setData(data);

        Legend l = mChart.getLegend();
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.WHITE);

        x.setTextColor(Color.WHITE);
        x.setDrawGridLines(true);
        x.setAvoidFirstLastClipping(true);

        y.setTextColor(Color.WHITE);
        y.setAxisMaximum(currentMax);
        y.setAxisMinimum(currentMin);
        y.setDrawGridLines(true);
        y.setLabelCount(10);

        y2.setDrawGridLines(false);

        return view;
    }

    private class SensorDataFetch extends AsyncTask<Void, Void, Void> implements SensorEventListener {

        private float data;
        private long timeElapsed;
        private int count = 0;
        private float sum = 0;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (sensorBH1750 != null) {
                    data = sensorBH1750.getRaw().floatValue();
                    sensorManager.unregisterListener(this);
                } else if (sensor != null) {
                    sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            data = event.values[0];
            if (currentMax < data) {
                currentMax = data;
                statMax.setText(String.valueOf(data));
            } else if (currentMin > data) {
                currentMin = data;
                statMin.setText(String.valueOf(data));
            }

            y.setAxisMaximum(currentMax);
            y.setAxisMinimum(currentMin);
            y.setLabelCount(10);
            lightMeter.speedTo(data);

            timeElapsed = (System.currentTimeMillis() - startTime) / 1000;
            entries.add(new Entry((float) timeElapsed, data));

            for (Entry item : entries) {
                count++;
                sum += item.getY();
            }
            statMean.setText(Float.toString(sum / count));

            LineDataSet dataSet = new LineDataSet(entries, getString(R.string.bx));
            LineData data = new LineData(dataSet);
            dataSet.setDrawCircles(false);
            mChart.setData(data);
            mChart.notifyDataSetChanged();
            mChart.setVisibleXRangeMaximum(10);
            mChart.moveViewToX(data.getEntryCount());
            mChart.invalidate();
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        sensorDataFetch.cancel(true);
        dataThread.interrupt();
        ((PSLabApplication) getActivity().getApplication()).refWatcher.watch(this, LuxMeterFragmentData.class.getSimpleName());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
