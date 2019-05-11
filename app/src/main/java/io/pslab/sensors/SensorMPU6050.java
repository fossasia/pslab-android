package io.pslab.sensors;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
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

import io.pslab.DataFormatter;
import io.pslab.R;
import io.pslab.communication.ScienceLab;
import io.pslab.communication.peripherals.I2C;
import io.pslab.communication.sensors.MPU6050;
import io.pslab.others.ScienceLabCommon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Harsh on 6/6/18.
 */

public class SensorMPU6050 extends AppCompatActivity {
    private static int counter;
    private final Object lock = new Object();
    private ScienceLab scienceLab;
    private SensorMPU6050.SensorDataFetch sensorDataFetch;
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
    private RelativeLayout sensorDock;
    private CheckBox indefiniteSamplesCheckBox;
    private EditText samplesEditBox;
    private SeekBar timeGapSeekbar;
    private TextView timeGapLabel;
    private ImageButton playPauseButton;
    private boolean play;
    private boolean runIndefinitely;
    private int timeGap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensor_mpu6050);

        sensorDock = (RelativeLayout) findViewById(R.id.sensor_control_dock_layout);
        indefiniteSamplesCheckBox = (CheckBox) findViewById(R.id.checkBox_samples_sensor);
        samplesEditBox = (EditText) findViewById(R.id.editBox_samples_sensors);
        timeGapSeekbar = (SeekBar) findViewById(R.id.seekBar_timegap_sensor);
        timeGapLabel = (TextView) findViewById(R.id.tv_timegap_label);
        playPauseButton = (ImageButton) findViewById(R.id.imageButton_play_pause_sensor);
        setSensorDock();
        sensorDock.setVisibility(View.VISIBLE);

        scienceLab = ScienceLabCommon.scienceLab;
        I2C i2c = scienceLab.i2c;
        try {
            sensorMPU6050 = new MPU6050(i2c, scienceLab);
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
                    if (scienceLab.isConnected() && shouldPlay()) {
                        sensorDataFetch = new SensorMPU6050.SensorDataFetch();
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
                            Thread.sleep(timeGap);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        new Thread(runnable).start();

        tvSensorMPU6050ax = findViewById(R.id.tv_sensor_mpu6050_ax);
        tvSensorMPU6050ay = findViewById(R.id.tv_sensor_mpu6050_ay);
        tvSensorMPU6050az = findViewById(R.id.tv_sensor_mpu6050_az);
        tvSensorMPU6050gx = findViewById(R.id.tv_sensor_mpu6050_gx);
        tvSensorMPU6050gy = findViewById(R.id.tv_sensor_mpu6050_gy);
        tvSensorMPU6050gz = findViewById(R.id.tv_sensor_mpu6050_gz);
        tvSensorMPU6050temp = findViewById(R.id.tv_sensor_mpu6050_temp);

        Spinner spinnerSensorMPU60501 = findViewById(R.id.spinner_sensor_mpu6050_1);
        Spinner spinnerSensorMPU60502 = findViewById(R.id.spinner_sensor_mpu6050_2);
        Spinner spinnerSensorMPU60503 = findViewById(R.id.spinner_sensor_mpu6050_3);
        Spinner spinnerSensorMPU60504 = findViewById(R.id.spinner_sensor_mpu6050_4);

        mChartAcceleration = findViewById(R.id.chart_sensor_mpu6050_accelerometer);
        mChartGyroscope = findViewById(R.id.chart_sensor_mpu6050_gyroscope);

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
            if (sensorMPU6050 != null && scienceLab.isConnected()) {
                sensorMPU6050.setAccelerationRange(Integer.parseInt(spinnerSensorMPU60502.getSelectedItem().toString()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            if (sensorMPU6050 != null && scienceLab.isConnected()) {
                sensorMPU6050.setGyroRange(Integer.parseInt(spinnerSensorMPU60501.getSelectedItem().toString()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private boolean shouldPlay() {
        if (play && scienceLab.isConnected()) {
            if (indefiniteSamplesCheckBox.isChecked())
                return true;
            else if (counter >= 0) {
                counter--;
                return true;
            } else {
                play = false;
                return false;
            }
        } else {
            return false;
        }
    }

    private void setSensorDock() {
        play = false;
        runIndefinitely = true;
        timeGap = 100;
        final int step = 1;
        final int max = 1000;
        final int min = 100;

        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (play && scienceLab.isConnected()) {
                    playPauseButton.setImageResource(R.drawable.play);
                    play = false;
                } else if (!scienceLab.isConnected()) {
                    playPauseButton.setImageResource(R.drawable.play);
                    play = false;
                } else {
                    playPauseButton.setImageResource(R.drawable.pause);
                    play = true;
                    if (!indefiniteSamplesCheckBox.isChecked()) {
                        counter = Integer.parseInt(samplesEditBox.getText().toString());
                    }
                }
            }
        });
        sensorDock.setVisibility(View.VISIBLE);

        indefiniteSamplesCheckBox.setChecked(true);
        samplesEditBox.setEnabled(false);
        indefiniteSamplesCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    runIndefinitely = true;
                    samplesEditBox.setEnabled(false);
                } else {
                    runIndefinitely = false;
                    samplesEditBox.setEnabled(true);
                }
            }
        });

        timeGapSeekbar.setMax((max - min) / step);
        timeGapSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                timeGap = min + (progress * step);
                timeGapLabel.setText(timeGap + "ms");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
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
            tvSensorMPU6050ax.setText(DataFormatter.formatDouble(dataMPU6050.get(0), DataFormatter.HIGH_PRECISION_FORMAT));
            tvSensorMPU6050ay.setText(DataFormatter.formatDouble(dataMPU6050.get(1), DataFormatter.HIGH_PRECISION_FORMAT));
            tvSensorMPU6050az.setText(DataFormatter.formatDouble(dataMPU6050.get(2), DataFormatter.HIGH_PRECISION_FORMAT));
            tvSensorMPU6050gx.setText(DataFormatter.formatDouble(dataMPU6050.get(4), DataFormatter.HIGH_PRECISION_FORMAT));
            tvSensorMPU6050gy.setText(DataFormatter.formatDouble(dataMPU6050.get(5), DataFormatter.HIGH_PRECISION_FORMAT));
            tvSensorMPU6050gz.setText(DataFormatter.formatDouble(dataMPU6050.get(6), DataFormatter.HIGH_PRECISION_FORMAT));
            tvSensorMPU6050temp.setText(DataFormatter.formatDouble(dataMPU6050.get(3), DataFormatter.HIGH_PRECISION_FORMAT));

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
            samplesEditBox.setText(String.valueOf(counter));
            if (counter == 0 && !runIndefinitely) {
                play = false;
                playPauseButton.setImageResource(R.drawable.play);
            }
            synchronized (lock) {
                lock.notify();
            }
        }
    }
}
