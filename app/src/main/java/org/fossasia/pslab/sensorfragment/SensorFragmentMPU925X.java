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
import org.fossasia.pslab.communication.sensors.MPU925x;
import org.fossasia.pslab.others.ScienceLabCommon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.fossasia.pslab.activity.SensorActivity.counter;

/**
 * Created by asitava on 10/7/17.
 */

public class SensorFragmentMPU925X extends Fragment {

    private ScienceLab scienceLab;
    private SensorDataFetch sensorDataFetch;
    private TextView tvSensorMPU925Xax;
    private TextView tvSensorMPU925Xay;
    private TextView tvSensorMPU925Xaz;
    private TextView tvSensorMPU925Xgx;
    private TextView tvSensorMPU925Xgy;
    private TextView tvSensorMPU925Xgz;
    private TextView tvSensorMPU925Xtemp;
    private MPU925x sensorMPU925X;
    private LineChart mChartAcceleration;
    private LineChart mChartGyroscope;
    private long startTime;
    private int flag;
    private ArrayList<Entry> entriesax;
    private ArrayList<Entry> entriesay;
    private ArrayList<Entry> entriesaz;
    private ArrayList<Entry> entriesgx;
    private ArrayList<Entry> entriesgy;
    private ArrayList<Entry> entriesgz;
    private final Object lock = new Object();

    public static SensorFragmentMPU925X newInstance() {
        return new SensorFragmentMPU925X();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scienceLab = ScienceLabCommon.scienceLab;
        I2C i2c = scienceLab.i2c;
        ((SensorActivity) getActivity()).sensorDock.setVisibility(View.VISIBLE);
        try {
            sensorMPU925X = new MPU925x(i2c);
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
        View view = inflater.inflate(R.layout.sensor_mpu925x, container, false);

        tvSensorMPU925Xax = (TextView) view.findViewById(R.id.tv_sensor_mpu925x_ax);
        tvSensorMPU925Xay = (TextView) view.findViewById(R.id.tv_sensor_mpu925x_ay);
        tvSensorMPU925Xaz = (TextView) view.findViewById(R.id.tv_sensor_mpu925x_az);
        tvSensorMPU925Xgx = (TextView) view.findViewById(R.id.tv_sensor_mpu925x_gx);
        tvSensorMPU925Xgy = (TextView) view.findViewById(R.id.tv_sensor_mpu925x_gy);
        tvSensorMPU925Xgz = (TextView) view.findViewById(R.id.tv_sensor_mpu925x_gz);
        tvSensorMPU925Xtemp = (TextView) view.findViewById(R.id.tv_sensor_mpu925x_temp);

        Spinner spinnerSensorMPU925X1 = (Spinner) view.findViewById(R.id.spinner_sensor_mpu925x_1);
        Spinner spinnerSensorMPU925X2 = (Spinner) view.findViewById(R.id.spinner_sensor_mpu925x_2);
        Spinner spinnerSensorMPU925X3 = (Spinner) view.findViewById(R.id.spinner_sensor_mpu925x_3);
        Spinner spinnerSensorMPU925X4 = (Spinner) view.findViewById(R.id.spinner_sensor_mpu925x_4);

        mChartAcceleration = (LineChart) view.findViewById(R.id.chart_sensor_mpu925x_accelerometer);
        mChartGyroscope = (LineChart) view.findViewById(R.id.chart_sensor_mpu925x_gyroscope);

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
            if (sensorMPU925X != null) {
                sensorMPU925X.setAccelRange(Integer.parseInt(spinnerSensorMPU925X2.getSelectedItem().toString()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            if (sensorMPU925X != null) {
                sensorMPU925X.setGyroRange(Integer.parseInt(spinnerSensorMPU925X1.getSelectedItem().toString()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return view;
    }

    private class SensorDataFetch extends AsyncTask<Void, Void, Void> {

        private double[] dataGyro, dataAccel;
        private double dataTemp;
        private long timeElapsed;

        @Override
        protected Void doInBackground(Void... params) {

            try {
                dataGyro = sensorMPU925X.getGyroscope();
                dataAccel = sensorMPU925X.getAcceleration();
                dataTemp = sensorMPU925X.getTemperature();
            } catch (IOException e) {
                e.printStackTrace();
            }

            timeElapsed = (System.currentTimeMillis() - startTime) / 1000;

            entriesax.add(new Entry((float) timeElapsed, (float) dataAccel[0]));
            entriesay.add(new Entry((float) timeElapsed, (float) dataAccel[1]));
            entriesaz.add(new Entry((float) timeElapsed, (float) dataAccel[2]));

            entriesgx.add(new Entry((float) timeElapsed, (float) dataGyro[0]));
            entriesgy.add(new Entry((float) timeElapsed, (float) dataGyro[1]));
            entriesgz.add(new Entry((float) timeElapsed, (float) dataGyro[2]));

            return null;
        }

        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            tvSensorMPU925Xax.setText(String.valueOf(dataAccel[0]));
            tvSensorMPU925Xay.setText(String.valueOf(dataAccel[1]));
            tvSensorMPU925Xaz.setText(String.valueOf(dataAccel[2]));
            tvSensorMPU925Xgx.setText(String.valueOf(dataGyro[0]));
            tvSensorMPU925Xgy.setText(String.valueOf(dataGyro[1]));
            tvSensorMPU925Xgz.setText(String.valueOf(dataGyro[2]));
            tvSensorMPU925Xtemp.setText(String.valueOf(dataTemp));

            LineDataSet dataSet1 = new LineDataSet(entriesax, getString(R.string.ax));
            LineDataSet dataSet2 = new LineDataSet(entriesay, getString(R.string.ay));
            LineDataSet dataSet3 = new LineDataSet(entriesaz, getString(R.string.az));

            LineDataSet dataSet4 = new LineDataSet(entriesgx, getString(R.string.gx));
            LineDataSet dataSet5 = new LineDataSet(entriesgy, getString(R.string.gy));
            LineDataSet dataSet6 = new LineDataSet(entriesgz, getString(R.string.gz));


            dataSet1.setColor(Color.BLUE);
            dataSet2.setColor(Color.GREEN);
            dataSet3.setColor(Color.RED);

            dataSet4.setColor(Color.BLUE);
            dataSet5.setColor(Color.GREEN);
            dataSet6.setColor(Color.RED);

            List<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(dataSet1);
            dataSets.add(dataSet2);
            dataSets.add(dataSet3);

            List<ILineDataSet> dataSets2 = new ArrayList<>();
            dataSets2.add(dataSet4);
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
