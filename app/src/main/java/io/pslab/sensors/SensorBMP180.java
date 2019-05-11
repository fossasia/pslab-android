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
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import io.pslab.DataFormatter;
import io.pslab.R;
import io.pslab.communication.ScienceLab;
import io.pslab.communication.peripherals.I2C;
import io.pslab.communication.sensors.BMP180;
import io.pslab.others.ScienceLabCommon;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Harsh on 6/6/18.
 */

public class SensorBMP180 extends AppCompatActivity {
    private static int counter;
    private final Object lock = new Object();
    private ScienceLab scienceLab;
    private SensorBMP180.SensorDataFetch sensorDataFetch;
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
        setContentView(R.layout.sensor_bmp180);

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
            sensorBMP180 = new BMP180(i2c, scienceLab);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (scienceLab.isConnected() && shouldPlay()) {
                        sensorDataFetch = new SensorBMP180.SensorDataFetch();
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

        tvSensorBMP180Temp = findViewById(R.id.tv_sensor_bmp180_temp);
        tvSensorBMP180Altitude = findViewById(R.id.tv_sensor_bmp180_altitude);
        tvSensorBMP180Pressure = findViewById(R.id.tv_sensor_bmp180_pressure);

        mChartTemperature = findViewById(R.id.chart_temp_bmp180);
        mChartAltitude = findViewById(R.id.chart_alt_bmp180);
        mChartPressure = findViewById(R.id.chart_pre_bmp180);

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

        private double[] dataBMP180 = new double[3];
        private long timeElapsed;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (sensorBMP180 != null && scienceLab.isConnected()) {
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
            tvSensorBMP180Temp.setText(DataFormatter.formatDouble(dataBMP180[0], DataFormatter.HIGH_PRECISION_FORMAT));
            tvSensorBMP180Altitude.setText(DataFormatter.formatDouble(dataBMP180[1], DataFormatter.HIGH_PRECISION_FORMAT));
            tvSensorBMP180Pressure.setText(DataFormatter.formatDouble(dataBMP180[2], DataFormatter.HIGH_PRECISION_FORMAT));

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
