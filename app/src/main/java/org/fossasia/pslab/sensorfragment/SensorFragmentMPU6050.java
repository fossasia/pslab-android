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
import org.fossasia.pslab.communication.sensors.MPU6050;
import org.fossasia.pslab.others.ScienceLabCommon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by asitava on 10/7/17.
 */

public class SensorFragmentMPU6050 extends Fragment {
    private ScienceLab scienceLab;
    private I2C i2c;
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
    private XAxis xAccelerometer;
    private YAxis yAccelerometer;
    private YAxis yAccelerometer2;
    private XAxis xGyroscope;
    private YAxis yGyroscope;
    private YAxis yGyroscope2;
    private long startTime;
    private int flag;
    private ArrayList<Entry> entriesax;
    private ArrayList<Entry> entriesay;
    private ArrayList<Entry> entriesaz;
    private ArrayList<Entry> entriesgx;
    private ArrayList<Entry> entriesgy;
    private ArrayList<Entry> entriesgz;
    private final Object lock = new Object();

    public static SensorFragmentMPU6050 newInstance() {
        SensorFragmentMPU6050 sensorFragmentMPU6050 = new SensorFragmentMPU6050();
        return sensorFragmentMPU6050;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scienceLab = ScienceLabCommon.scienceLab;
        i2c = scienceLab.i2c;
        try {
            sensorMPU6050 = new MPU6050(i2c);
        } catch (IOException e) {
            e.printStackTrace();
        }

        entriesax = new ArrayList<>();
        entriesay = new ArrayList<>();
        entriesaz = new ArrayList<>();
        entriesgx = new ArrayList<>();
        entriesgy = new ArrayList<>();
        entriesgz = new ArrayList<>();

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

        xAccelerometer = mChartAcceleration.getXAxis();
        yAccelerometer = mChartAcceleration.getAxisLeft();
        yAccelerometer2 = mChartAcceleration.getAxisRight();

        xGyroscope = mChartGyroscope.getXAxis();
        yGyroscope = mChartGyroscope.getAxisLeft();
        yGyroscope2 = mChartGyroscope.getAxisRight();

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
                sensorMPU6050.setAccelRange(Integer.parseInt(spinnerSensorMPU60502.getSelectedItem().toString()));
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
        ArrayList<Double> dataMPU6050 = new ArrayList<Double>();
        long timeElapsed;

        @Override
        protected Void doInBackground(Void... params) {

            try {
                dataMPU6050 = sensorMPU6050.getRaw();
            } catch (IOException e) {
                e.printStackTrace();
            }

            timeElapsed = (System.currentTimeMillis() - startTime) / 1000;

            entriesax.add(new Entry((float) timeElapsed, dataMPU6050.get(0).floatValue()));
            entriesay.add(new Entry((float) timeElapsed, dataMPU6050.get(1).floatValue()));
            entriesaz.add(new Entry((float) timeElapsed, dataMPU6050.get(2).floatValue()));

            entriesgx.add(new Entry((float) timeElapsed, dataMPU6050.get(3).floatValue()));
            entriesgy.add(new Entry((float) timeElapsed, dataMPU6050.get(4).floatValue()));
            entriesgz.add(new Entry((float) timeElapsed, dataMPU6050.get(5).floatValue()));

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

            LineDataSet dataset1 = new LineDataSet(entriesax, "Ax");
            LineDataSet dataSet2 = new LineDataSet(entriesay, "Ay");
            LineDataSet dataSet3 = new LineDataSet(entriesaz, "Az");

            LineDataSet dataset4 = new LineDataSet(entriesgx, "Gx");
            LineDataSet dataSet5 = new LineDataSet(entriesgy, "Gy");
            LineDataSet dataSet6 = new LineDataSet(entriesgz, "Gz");


            dataset1.setColor(Color.BLUE);
            dataSet2.setColor(Color.GREEN);
            dataSet3.setColor(Color.RED);

            dataset4.setColor(Color.BLUE);
            dataSet5.setColor(Color.GREEN);
            dataSet6.setColor(Color.RED);

            dataset1.setDrawCircles(true);
            dataSet2.setDrawCircles(true);
            dataSet3.setDrawCircles(true);

            dataset4.setDrawCircles(true);
            dataSet5.setDrawCircles(true);
            dataSet6.setDrawCircles(true);

            List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
            dataSets.add(dataset1);
            dataSets.add(dataSet2);
            dataSets.add(dataSet3);

            List<ILineDataSet> dataSets2 = new ArrayList<ILineDataSet>();
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

            synchronized (lock) {
                lock.notify();
            }
        }
    }
}
