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
import org.fossasia.pslab.activity.SensorActivity;
import org.fossasia.pslab.communication.ScienceLab;
import org.fossasia.pslab.communication.peripherals.I2C;
import org.fossasia.pslab.communication.sensors.MPU6050;
import org.fossasia.pslab.others.ScienceLabCommon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.fossasia.pslab.activity.SensorActivity.counter;

/**
 * Created by asitava on 10/7/17.
 */

public class SensorFragmentMPU6050 extends Fragment {

    private ScienceLab scienceLab;
    private SensorDataFetch sensorDataFetch;
    private TextView tvSensorMPU6050ax;
    private TextView tvSensorMPU6050ay;
    private TextView tvSensorMPU6050az;
    private TextView tvSensorMPU6050gx;
    private TextView tvSensorMPU6050gy;
    private TextView tvSensorMPU6050gz;
    private TextView tvSensorMPU6050temp;
    private MPU6050 sensorMPU6050;
    private LineChart mChartAcceleration;
    private LineChart mChartGyroscope;
    private long startTime;
    private int flag;
    private ArrayList<Entry> entriesAx;
    private ArrayList<Entry> entriesAy;
    private ArrayList<Entry> entriesAz;
    private ArrayList<Entry> entriesGx;
    private ArrayList<Entry> entriesGy;
    private ArrayList<Entry> entriesGz;
    private final Object lock = new Object();

    public static SensorFragmentMPU6050 newInstance() {
        return new SensorFragmentMPU6050();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scienceLab = ScienceLabCommon.scienceLab;
        I2C i2c = scienceLab.i2c;
        ((SensorActivity) getActivity()).sensorDock.setVisibility(View.VISIBLE);
        try {
            sensorMPU6050 = new MPU6050(i2c);
        } catch (IOException e) {
            e.printStackTrace();
        }

        entriesAx = new ArrayList<>();
        entriesAy = new ArrayList<>();
        entriesAz = new ArrayList<>();
        entriesGx = new ArrayList<>();
        entriesGy = new ArrayList<>();
        entriesGz = new ArrayList<>();

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
        View view = inflater.inflate(R.layout.sensor_mpu6050, container, false);

        tvSensorMPU6050ax = (TextView) view.findViewById(R.id.tv_sensor_mpu6050_ax);
        tvSensorMPU6050ay = (TextView) view.findViewById(R.id.tv_sensor_mpu6050_ay);
        tvSensorMPU6050az = (TextView) view.findViewById(R.id.tv_sensor_mpu6050_az);
        tvSensorMPU6050gx = (TextView) view.findViewById(R.id.tv_sensor_mpu6050_gx);
        tvSensorMPU6050gy = (TextView) view.findViewById(R.id.tv_sensor_mpu6050_gy);
        tvSensorMPU6050gz = (TextView) view.findViewById(R.id.tv_sensor_mpu6050_gz);
        tvSensorMPU6050temp = (TextView) view.findViewById(R.id.tv_sensor_mpu6050_temp);

        Spinner spinnerSensorMPU60501 = (Spinner) view.findViewById(R.id.spinner_sensor_mpu6050_1);
        Spinner spinnerSensorMPU60502 = (Spinner) view.findViewById(R.id.spinner_sensor_mpu6050_2);
        Spinner spinnerSensorMPU60503 = (Spinner) view.findViewById(R.id.spinner_sensor_mpu6050_3);
        Spinner spinnerSensorMPU60504 = (Spinner) view.findViewById(R.id.spinner_sensor_mpu6050_4);

        mChartAcceleration = (LineChart) view.findViewById(R.id.chart_sensor_mpu6050_accelerometer);
        mChartGyroscope = (LineChart) view.findViewById(R.id.chart_sensor_mpu6050_gyroscope);

        XAxis xAccelerometer = mChartAcceleration.getXAxis();
        YAxis yAccelerometer = mChartAcceleration.getAxisLeft();
        YAxis yAccelerometer2 = mChartAcceleration.getAxisRight();

        XAxis xGyroscope = mChartGyroscope.getXAxis();
        YAxis yGyroscope = mChartGyroscope.getAxisLeft();
        YAxis yGyroscope2 = mChartGyroscope.getAxisRight();

        mChartAcceleration.setTouchEnabled(true);
        mChartAcceleration.setHighlightPerDragEnabled(true);
        mChartAcceleration.setDragEnabled(true);
        mChartAcceleration.setScaleEnabled(true);
        mChartAcceleration.setDrawGridBackground(false);
        mChartAcceleration.setPinchZoom(true);
        mChartAcceleration.setScaleYEnabled(false);
        mChartAcceleration.setBackgroundColor(Color.BLACK);
        mChartAcceleration.getDescription().setEnabled(false);

        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);
        mChartAcceleration.setData(data);

        Legend l = mChartAcceleration.getLegend();
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.WHITE);

        xAccelerometer.setTextColor(Color.WHITE);
        xAccelerometer.setDrawGridLines(true);
        xAccelerometer.setAvoidFirstLastClipping(true);

        yAccelerometer.setTextColor(Color.WHITE);
        yAccelerometer.setAxisMaximum(25f);
        yAccelerometer.setAxisMinimum(-25f);
        yAccelerometer.setDrawGridLines(true);
        yAccelerometer.setLabelCount(10);

        yAccelerometer2.setDrawGridLines(false);

        mChartGyroscope.setTouchEnabled(true);
        mChartGyroscope.setHighlightPerDragEnabled(true);
        mChartGyroscope.setDragEnabled(true);
        mChartGyroscope.setScaleEnabled(true);
        mChartGyroscope.setDrawGridBackground(false);
        mChartGyroscope.setPinchZoom(true);
        mChartGyroscope.setScaleYEnabled(false);
        mChartGyroscope.setBackgroundColor(Color.BLACK);
        mChartGyroscope.getDescription().setEnabled(false);

        LineData data2 = new LineData();
        data.setValueTextColor(Color.WHITE);
        mChartGyroscope.setData(data2);

        Legend l2 = mChartGyroscope.getLegend();
        l2.setForm(Legend.LegendForm.LINE);
        l2.setTextColor(Color.WHITE);

        xGyroscope.setTextColor(Color.WHITE);
        xGyroscope.setDrawGridLines(true);
        xGyroscope.setAvoidFirstLastClipping(true);

        yGyroscope.setTextColor(Color.WHITE);
        yGyroscope.setAxisMaximum(200f);
        yGyroscope.setAxisMinimum(-200f);
        yGyroscope.setDrawGridLines(true);
        yGyroscope.setLabelCount(10);

        yGyroscope2.setDrawGridLines(false);

        try {
            if (sensorMPU6050 != null) {
                sensorMPU6050.setAccelerationRange(Integer.parseInt(spinnerSensorMPU60502.getSelectedItem().toString()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            if (sensorMPU6050 != null) {
                sensorMPU6050.setGyroRange(Integer.parseInt(spinnerSensorMPU60501.getSelectedItem().toString()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return view;
    }

    private class SensorDataFetch extends AsyncTask<Void, Void, Void> {

        private ArrayList<Double> dataMPU6050 = new ArrayList<>();
        private long timeElapsed;

        @Override
        protected Void doInBackground(Void... params) {

            try {
                dataMPU6050 = sensorMPU6050.getRaw();
            } catch (IOException e) {
                e.printStackTrace();
            }

            timeElapsed = (System.currentTimeMillis() - startTime) / 1000;

            entriesAx.add(new Entry((float) timeElapsed, dataMPU6050.get(0).floatValue()));
            entriesAy.add(new Entry((float) timeElapsed, dataMPU6050.get(1).floatValue()));
            entriesAz.add(new Entry((float) timeElapsed, dataMPU6050.get(2).floatValue()));

            entriesGx.add(new Entry((float) timeElapsed, dataMPU6050.get(4).floatValue()));
            entriesGy.add(new Entry((float) timeElapsed, dataMPU6050.get(5).floatValue()));
            entriesGz.add(new Entry((float) timeElapsed, dataMPU6050.get(6).floatValue()));

            return null;
        }

        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            tvSensorMPU6050ax.setText(String.valueOf(dataMPU6050.get(0)));
            tvSensorMPU6050ay.setText(String.valueOf(dataMPU6050.get(1)));
            tvSensorMPU6050az.setText(String.valueOf(dataMPU6050.get(2)));
            tvSensorMPU6050gx.setText(String.valueOf(dataMPU6050.get(4)));
            tvSensorMPU6050gy.setText(String.valueOf(dataMPU6050.get(5)));
            tvSensorMPU6050gz.setText(String.valueOf(dataMPU6050.get(6)));
            tvSensorMPU6050temp.setText(String.valueOf(dataMPU6050.get(3)));

            LineDataSet dataset1 = new LineDataSet(entriesAx, getString(R.string.ax));
            LineDataSet dataSet2 = new LineDataSet(entriesAy, getString(R.string.ay));
            LineDataSet dataSet3 = new LineDataSet(entriesAz, getString(R.string.az));

            LineDataSet dataset4 = new LineDataSet(entriesGx, getString(R.string.gx));
            LineDataSet dataSet5 = new LineDataSet(entriesGy, getString(R.string.gy));
            LineDataSet dataSet6 = new LineDataSet(entriesGz, getString(R.string.gz));


            dataset1.setColor(Color.BLUE);
            dataSet2.setColor(Color.GREEN);
            dataSet3.setColor(Color.RED);

            dataset4.setColor(Color.BLUE);
            dataSet5.setColor(Color.GREEN);
            dataSet6.setColor(Color.RED);

            List<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(dataset1);
            dataSets.add(dataSet2);
            dataSets.add(dataSet3);

            List<ILineDataSet> dataSets2 = new ArrayList<>();
            dataSets2.add(dataset4);
            dataSets2.add(dataSet5);
            dataSets2.add(dataSet6);

            LineData data = new LineData(dataSets);
            mChartAcceleration.setData(data);
            mChartAcceleration.notifyDataSetChanged();
            mChartAcceleration.setVisibleXRangeMaximum(10);
            mChartAcceleration.moveViewToX(data.getEntryCount());
            mChartAcceleration.invalidate();

            LineData data2 = new LineData(dataSets2);
            mChartGyroscope.setData(data2);
            mChartGyroscope.notifyDataSetChanged();
            mChartGyroscope.setVisibleXRangeMaximum(10);
            mChartGyroscope.moveViewToX(data2.getEntryCount());
            mChartGyroscope.invalidate();
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
