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
import io.pslab.communication.sensors.MPU925x;
import io.pslab.others.ScienceLabCommon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Harsh on 6/6/18.
 */

public class SensorMPU925X extends AppCompatActivity {
    private static int counter;
    private final Object lock = new Object();
    private ScienceLab scienceLab;
    private SensorMPU925X.SensorDataFetch sensorDataFetch;
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
        setContentView(R.layout.sensor_mpu925x);

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
                    if (scienceLab.isConnected() && shouldPlay()) {
                        sensorDataFetch = new SensorMPU925X.SensorDataFetch();
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

        tvSensorMPU925Xax = findViewById(R.id.tv_sensor_mpu925x_ax);
        tvSensorMPU925Xay = findViewById(R.id.tv_sensor_mpu925x_ay);
        tvSensorMPU925Xaz = findViewById(R.id.tv_sensor_mpu925x_az);
        tvSensorMPU925Xgx = findViewById(R.id.tv_sensor_mpu925x_gx);
        tvSensorMPU925Xgy = findViewById(R.id.tv_sensor_mpu925x_gy);
        tvSensorMPU925Xgz = findViewById(R.id.tv_sensor_mpu925x_gz);
        tvSensorMPU925Xtemp = findViewById(R.id.tv_sensor_mpu925x_temp);

        Spinner spinnerSensorMPU925X1 = findViewById(R.id.spinner_sensor_mpu925x_1);
        Spinner spinnerSensorMPU925X2 = findViewById(R.id.spinner_sensor_mpu925x_2);
        Spinner spinnerSensorMPU925X3 = findViewById(R.id.spinner_sensor_mpu925x_3);
        Spinner spinnerSensorMPU925X4 = findViewById(R.id.spinner_sensor_mpu925x_4);

        mChartAcceleration = findViewById(R.id.chart_sensor_mpu925x_accelerometer);
        mChartGyroscope = findViewById(R.id.chart_sensor_mpu925x_gyroscope);

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

    }

    private boolean shouldPlay() {
        if (play) {
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
            tvSensorMPU925Xax.setText(DataFormatter.formatDouble(dataAccel[0], DataFormatter.HIGH_PRECISION_FORMAT));
            tvSensorMPU925Xay.setText(DataFormatter.formatDouble(dataAccel[1], DataFormatter.HIGH_PRECISION_FORMAT));
            tvSensorMPU925Xaz.setText(DataFormatter.formatDouble(dataAccel[2], DataFormatter.HIGH_PRECISION_FORMAT));
            tvSensorMPU925Xgx.setText(DataFormatter.formatDouble(dataGyro[0], DataFormatter.HIGH_PRECISION_FORMAT));
            tvSensorMPU925Xgy.setText(DataFormatter.formatDouble(dataGyro[1], DataFormatter.HIGH_PRECISION_FORMAT));
            tvSensorMPU925Xgz.setText(DataFormatter.formatDouble(dataGyro[2], DataFormatter.HIGH_PRECISION_FORMAT));
            tvSensorMPU925Xtemp.setText(DataFormatter.formatDouble(dataTemp, DataFormatter.HIGH_PRECISION_FORMAT));
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
