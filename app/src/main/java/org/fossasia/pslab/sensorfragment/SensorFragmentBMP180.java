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
import org.fossasia.pslab.activity.SensorActivity;
import org.fossasia.pslab.communication.ScienceLab;
import org.fossasia.pslab.communication.peripherals.I2C;
import org.fossasia.pslab.communication.sensors.BMP180;
import org.fossasia.pslab.others.ScienceLabCommon;

import java.io.IOException;
import java.util.ArrayList;

import static org.fossasia.pslab.activity.SensorActivity.counter;

/**
 * Created by asitava on 10/7/17.
 */

public class SensorFragmentBMP180 extends Fragment {

    private ScienceLab scienceLab;
    private SensorDataFetch sensorDataFetch;
    private TextView tvSensorBMP180Temp;
    private TextView tvSensorBMP180Altitude;
    private TextView tvSensorBMP180Pressure;
    private BMP180 sensorBMP180;
    private LineChart mChartTemperature;
    private LineChart mChartAltitude;
    private LineChart mChartPressure;
    private long startTime;
    private int flag;
    private ArrayList<Entry> entriesTemperature;
    private ArrayList<Entry> entriesAltitude;
    private ArrayList<Entry> entriesPressure;
    private final Object lock = new Object();

    public static SensorFragmentBMP180 newInstance() {
        return new SensorFragmentBMP180();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scienceLab = ScienceLabCommon.scienceLab;
        I2C i2c = scienceLab.i2c;
        ((SensorActivity) getActivity()).sensorDock.setVisibility(View.VISIBLE);
        try {
            sensorBMP180 = new BMP180(i2c);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (scienceLab.isConnected() && ((SensorActivity) getActivity()).shouldPlay()) {
                        sensorDataFetch = new SensorDataFetch();
                        sensorDataFetch.execute();

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
                            Thread.sleep(((SensorActivity) getActivity()).timeGap);
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
        View view = inflater.inflate(R.layout.sensor_bmp180, container, false);

        tvSensorBMP180Temp = (TextView) view.findViewById(R.id.tv_sensor_bmp180_temp);
        tvSensorBMP180Altitude = (TextView) view.findViewById(R.id.tv_sensor_bmp180_altitude);
        tvSensorBMP180Pressure = (TextView) view.findViewById(R.id.tv_sensor_bmp180_pressure);

        mChartTemperature = (LineChart) view.findViewById(R.id.chart_temp_bmp180);
        mChartAltitude = (LineChart) view.findViewById(R.id.chart_alt_bmp180);
        mChartPressure = (LineChart) view.findViewById(R.id.chart_pre_bmp180);

        XAxis xTemperature = mChartTemperature.getXAxis();
        YAxis yTemperature = mChartTemperature.getAxisLeft();
        YAxis yTemperature2 = mChartTemperature.getAxisRight();

        XAxis xAltitude = mChartAltitude.getXAxis();
        YAxis yAltitude = mChartAltitude.getAxisLeft();
        YAxis yAltitude2 = mChartAltitude.getAxisRight();

        XAxis xPressure = mChartPressure.getXAxis();
        YAxis yPressure = mChartPressure.getAxisLeft();
        YAxis yPressure2 = mChartPressure.getAxisRight();

        entriesTemperature = new ArrayList<>();
        entriesAltitude = new ArrayList<>();
        entriesPressure = new ArrayList<>();

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
        yTemperature.setAxisMaximum(70f);
        yTemperature.setAxisMinimum(0f);
        yTemperature.setDrawGridLines(true);
        yTemperature.setLabelCount(10);

        yTemperature2.setDrawGridLines(false);

        mChartAltitude.setTouchEnabled(true);
        mChartAltitude.setHighlightPerDragEnabled(true);
        mChartAltitude.setDragEnabled(true);
        mChartAltitude.setScaleEnabled(true);
        mChartAltitude.setDrawGridBackground(false);
        mChartAltitude.setPinchZoom(true);
        mChartAltitude.setScaleYEnabled(false);
        mChartAltitude.setBackgroundColor(Color.BLACK);
        mChartAltitude.getDescription().setEnabled(false);

        LineData data2 = new LineData();
        data.setValueTextColor(Color.WHITE);
        mChartAltitude.setData(data2);

        Legend l2 = mChartAltitude.getLegend();
        l2.setForm(Legend.LegendForm.LINE);
        l2.setTextColor(Color.WHITE);

        xAltitude.setTextColor(Color.WHITE);
        xAltitude.setDrawGridLines(true);
        xAltitude.setAvoidFirstLastClipping(true);

        yAltitude.setTextColor(Color.WHITE);
        yAltitude.setAxisMaximum(3000f);
        yAltitude.setAxisMinimum(0f);
        yAltitude.setDrawGridLines(true);
        yAltitude.setLabelCount(10);

        yAltitude2.setDrawGridLines(false);

        mChartPressure.setTouchEnabled(true);
        mChartPressure.setHighlightPerDragEnabled(true);
        mChartPressure.setDragEnabled(true);
        mChartPressure.setScaleEnabled(true);
        mChartPressure.setDrawGridBackground(false);
        mChartPressure.setPinchZoom(true);
        mChartPressure.setScaleYEnabled(false);
        mChartPressure.setBackgroundColor(Color.BLACK);
        mChartPressure.getDescription().setEnabled(false);

        LineData data3 = new LineData();
        data.setValueTextColor(Color.WHITE);
        mChartTemperature.setData(data3);

        Legend l3 = mChartTemperature.getLegend();
        l3.setForm(Legend.LegendForm.LINE);
        l3.setTextColor(Color.WHITE);

        xPressure.setTextColor(Color.WHITE);
        xPressure.setDrawGridLines(true);
        xPressure.setAvoidFirstLastClipping(true);

        yPressure.setTextColor(Color.WHITE);
        yPressure.setAxisMaximum(1000000f);
        yPressure.setAxisMinimum(0f);
        yPressure.setDrawGridLines(true);
        yPressure.setLabelCount(10);

        yPressure2.setDrawGridLines(false);
        return view;
    }


    private class SensorDataFetch extends AsyncTask<Void, Void, Void> {

        private double[] dataBMP180 = new double[3];
        private long timeElapsed;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (sensorBMP180 != null) {
                    dataBMP180 = sensorBMP180.getRaw();
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

            timeElapsed = (System.currentTimeMillis() - startTime) / 1000;
            entriesTemperature.add(new Entry((float) timeElapsed, (float) dataBMP180[0]));
            entriesAltitude.add(new Entry((float) timeElapsed, (float) dataBMP180[1]));
            entriesPressure.add(new Entry((float) timeElapsed, (float) dataBMP180[2]));
            return null;
        }

        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            tvSensorBMP180Temp.setText(String.valueOf(dataBMP180[0]));
            tvSensorBMP180Altitude.setText(String.valueOf(dataBMP180[1]));
            tvSensorBMP180Pressure.setText(String.valueOf(dataBMP180[2]));

            LineDataSet dataSet1 = new LineDataSet(entriesTemperature, getString(R.string.temperature));
            LineDataSet dataSet2 = new LineDataSet(entriesAltitude, getString(R.string.altitude));
            LineDataSet dataSet3 = new LineDataSet(entriesPressure, getString(R.string.pressure));

            dataSet1.setColor(Color.BLUE);
            dataSet2.setColor(Color.GREEN);
            dataSet3.setColor(Color.RED);

            LineData data = new LineData(dataSet1);
            mChartTemperature.setData(data);
            mChartTemperature.notifyDataSetChanged();
            mChartTemperature.setVisibleXRangeMaximum(10);
            mChartTemperature.moveViewToX(data.getEntryCount());
            mChartTemperature.invalidate();

            LineData data2 = new LineData(dataSet2);
            mChartAltitude.setData(data2);
            mChartAltitude.notifyDataSetChanged();
            mChartAltitude.setVisibleXRangeMaximum(10);
            mChartAltitude.moveViewToX(data.getEntryCount());
            mChartAltitude.invalidate();

            LineData data3 = new LineData(dataSet3);
            mChartPressure.setData(data3);
            mChartPressure.notifyDataSetChanged();
            mChartPressure.setVisibleXRangeMaximum(10);
            mChartPressure.moveViewToX(data.getEntryCount());
            mChartPressure.invalidate();
            ((SensorActivity) getActivity()).samplesEditBox.setText(String.valueOf(counter));
            if (counter == 0 && !((SensorActivity) getActivity()).runIndefinitely) {
                ((SensorActivity) getActivity()).play = false;
                ((SensorActivity) getActivity()).playPauseButton.setImageResource(R.drawable.play);
            }
            synchronized (lock) {
                lock.notify();
            }
        }
    }
}