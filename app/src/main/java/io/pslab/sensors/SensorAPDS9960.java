package io.pslab.sensors;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.io.IOException;
import java.util.ArrayList;

import io.pslab.DataFormatter;
import io.pslab.R;
import io.pslab.communication.ScienceLab;
import io.pslab.communication.peripherals.I2C;
import io.pslab.communication.sensors.APDS9960;
import io.pslab.others.ScienceLabCommon;

public class SensorAPDS9960 extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();
    private static int counter;
    private final Object lock = new Object();
    private ScienceLab scienceLab;
    private SensorAPDS9960.SensorDataFetch sensorDataFetch;
    private TextView tvSensorAPDS9960Red;
    private TextView tvSensorAPDS9960Green;
    private TextView tvSensorAPDS9960Blue;
    private TextView tvSensorAPDS9960Clear;
    private TextView tvSensorAPDS9960Proximity;
    private TextView tvSensorAPDS9960Gesture;
    private APDS9960 sensorAPDS9960;
    private LineChart mChartLux;
    private LineChart mChartProximity;
    private long startTime;
    private int flag;
    private ArrayList<Entry> entriesLux;
    private ArrayList<Entry> entriesProximity;
    private RelativeLayout sensorDock;
    private CheckBox indefiniteSamplesCheckBox;
    private EditText samplesEditBox;
    private SeekBar timeGapSeekbar;
    private TextView timeGapLabel;
    private ImageButton playPauseButton;
    private Spinner spinnerMode;
    private boolean play;
    private boolean runIndefinitely;
    private int timeGap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensor_apds9960);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.apds9960);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        sensorDock = findViewById(R.id.sensor_control_dock_layout);
        indefiniteSamplesCheckBox = findViewById(R.id.checkBox_samples_sensor);
        samplesEditBox = findViewById(R.id.editBox_samples_sensors);
        timeGapSeekbar = findViewById(R.id.seekBar_timegap_sensor);
        timeGapLabel = findViewById(R.id.tv_timegap_label);
        playPauseButton = findViewById(R.id.imageButton_play_pause_sensor);
        setSensorDock();
        sensorDock.setVisibility(View.VISIBLE);
        spinnerMode = findViewById(R.id.spinner_sensor_apds9960);

        scienceLab = ScienceLabCommon.scienceLab;
        I2C i2c = scienceLab.i2c;
        try {
            sensorAPDS9960 = new APDS9960(i2c, scienceLab);
        } catch (Exception e) {
            Log.e(TAG, "Sensor initialization failed.");
        }

        entriesLux = new ArrayList<>();
        entriesProximity = new ArrayList<>();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (scienceLab.isConnected() && shouldPlay()) {
                        sensorDataFetch = new SensorAPDS9960.SensorDataFetch();
                        sensorDataFetch.execute();

                        if (flag == 0) {
                            startTime = System.currentTimeMillis();
                            flag = 1;
                        }

                        synchronized (lock) {
                            try {
                                lock.wait();
                            } catch (InterruptedException e) {
                                Log.e(TAG, "Thread interrupted while waiting.");
                            }
                        }
                        try {
                            Thread.sleep(timeGap);
                        } catch (InterruptedException e) {
                            Log.e(TAG, "Thread interrupted during sleep.");
                        }
                    }
                }
            }
        };
        new Thread(runnable).start();

        tvSensorAPDS9960Red = findViewById(R.id.tv_sensor_apds9960_red);
        tvSensorAPDS9960Green = findViewById(R.id.tv_sensor_apds9960_green);
        tvSensorAPDS9960Blue = findViewById(R.id.tv_sensor_apds9960_blue);
        tvSensorAPDS9960Clear = findViewById(R.id.tv_sensor_apds9960_clear);
        tvSensorAPDS9960Proximity = findViewById(R.id.tv_sensor_apds9960_proximity);
        tvSensorAPDS9960Gesture = findViewById(R.id.tv_sensor_apds9960_gesture);
        mChartLux = findViewById(R.id.chart_sensor_apds9960_lux);
        mChartProximity = findViewById(R.id.chart_sensor_apds9960_proximity);

        XAxis xLux = mChartLux.getXAxis();
        YAxis yLux = mChartLux.getAxisLeft();
        YAxis yLux2 = mChartLux.getAxisRight();

        XAxis xProximity = mChartProximity.getXAxis();
        YAxis yProximity = mChartProximity.getAxisLeft();
        YAxis yProximity2 = mChartProximity.getAxisRight();

        mChartLux.setTouchEnabled(true);
        mChartLux.setHighlightPerDragEnabled(true);
        mChartLux.setDragEnabled(true);
        mChartLux.setScaleEnabled(true);
        mChartLux.setDrawGridBackground(false);
        mChartLux.setPinchZoom(true);
        mChartLux.setScaleYEnabled(false);
        mChartLux.setBackgroundColor(Color.BLACK);
        mChartLux.getDescription().setEnabled(false);

        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);
        mChartLux.setData(data);

        Legend l = mChartLux.getLegend();
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.WHITE);

        xLux.setTextColor(Color.WHITE);
        xLux.setDrawGridLines(true);
        xLux.setAvoidFirstLastClipping(true);

        yLux.setTextColor(Color.WHITE);
        yLux.setAxisMaximum(10000f);
        yLux.setAxisMinimum(0);
        yLux.setDrawGridLines(true);
        yLux.setLabelCount(10);

        yLux2.setDrawGridLines(false);

        mChartProximity.setTouchEnabled(true);
        mChartProximity.setHighlightPerDragEnabled(true);
        mChartProximity.setDragEnabled(true);
        mChartProximity.setScaleEnabled(true);
        mChartProximity.setDrawGridBackground(false);
        mChartProximity.setPinchZoom(true);
        mChartProximity.setScaleYEnabled(false);
        mChartProximity.setBackgroundColor(Color.BLACK);
        mChartProximity.getDescription().setEnabled(false);

        LineData data2 = new LineData();
        data.setValueTextColor(Color.WHITE);
        mChartProximity.setData(data2);

        Legend l2 = mChartProximity.getLegend();
        l2.setForm(Legend.LegendForm.LINE);
        l2.setTextColor(Color.WHITE);

        xProximity.setTextColor(Color.WHITE);
        xProximity.setDrawGridLines(true);
        xProximity.setAvoidFirstLastClipping(true);

        yProximity.setTextColor(Color.WHITE);
        yProximity.setAxisMaximum(256f);
        yProximity.setAxisMinimum(0f);
        yProximity.setDrawGridLines(true);
        yProximity.setLabelCount(10);

        yProximity2.setDrawGridLines(false);
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
                    playPauseButton.setImageResource(R.drawable.circle_play_button);
                    play = false;
                } else if (!scienceLab.isConnected()) {
                    playPauseButton.setImageResource(R.drawable.circle_play_button);
                    play = false;
                } else {
                    playPauseButton.setImageResource(R.drawable.circle_pause_button);
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
                // Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }
        });
    }

    private class SensorDataFetch extends AsyncTask<Void, Void, Void> {

        private int[] dataAPDS9960Color;
        private double dataAPDS9960Lux;
        private int dataAPDS9960Proximity;
        private int dataAPDS9960Gesture;
        private long timeElapsed;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (sensorAPDS9960 != null) {
                    if (spinnerMode.getSelectedItemPosition() == 0) {
                        sensorAPDS9960.enableGesture(false);
                        sensorAPDS9960.enableColor(true);
                        sensorAPDS9960.enableProximity(true);
                        dataAPDS9960Color = sensorAPDS9960.getColorData();
                        dataAPDS9960Lux = (-0.32466 * dataAPDS9960Color[0]) + (1.57837 * dataAPDS9960Color[1]) + (-0.73191 * dataAPDS9960Color[2]);
                        dataAPDS9960Proximity = sensorAPDS9960.getProximity();
                    } else {
                        sensorAPDS9960.enableColor(false);
                        sensorAPDS9960.enableGesture(true);
                        sensorAPDS9960.enableProximity(true);
                        dataAPDS9960Gesture = sensorAPDS9960.getGesture();
                    }
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
            timeElapsed = (System.currentTimeMillis() - startTime) / 1000;
            entriesLux.add(new Entry((float) timeElapsed, (float) dataAPDS9960Lux));
            entriesProximity.add(new Entry((float) timeElapsed, dataAPDS9960Proximity));
            return null;
        }

        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (spinnerMode.getSelectedItemPosition() == 0) {
                tvSensorAPDS9960Red.setText(DataFormatter.formatDouble(dataAPDS9960Color[0], DataFormatter.HIGH_PRECISION_FORMAT));
                tvSensorAPDS9960Green.setText(DataFormatter.formatDouble(dataAPDS9960Color[1], DataFormatter.HIGH_PRECISION_FORMAT));
                tvSensorAPDS9960Blue.setText(DataFormatter.formatDouble(dataAPDS9960Color[2], DataFormatter.HIGH_PRECISION_FORMAT));
                tvSensorAPDS9960Clear.setText(DataFormatter.formatDouble(dataAPDS9960Color[3], DataFormatter.HIGH_PRECISION_FORMAT));
                tvSensorAPDS9960Proximity.setText(DataFormatter.formatDouble(dataAPDS9960Proximity, DataFormatter.HIGH_PRECISION_FORMAT));

                LineDataSet dataSet1 = new LineDataSet(entriesLux, getString(R.string.light_lux));
                LineDataSet dataSet2 = new LineDataSet(entriesProximity, getString(R.string.proximity));

                dataSet1.setDrawCircles(true);
                dataSet2.setDrawCircles(true);

                LineData data = new LineData(dataSet1);
                mChartLux.setData(data);
                mChartLux.notifyDataSetChanged();
                mChartLux.setVisibleXRangeMaximum(10);
                mChartLux.moveViewToX(data.getEntryCount());
                mChartLux.invalidate();

                LineData data2 = new LineData(dataSet2);
                mChartProximity.setData(data2);
                mChartProximity.notifyDataSetChanged();
                mChartProximity.setVisibleXRangeMaximum(10);
                mChartProximity.moveViewToX(data2.getEntryCount());
                mChartProximity.invalidate();
            } else {
                switch (dataAPDS9960Gesture) {
                    case 1:
                        tvSensorAPDS9960Gesture.setText(R.string.up);
                        break;
                    case 2:
                        tvSensorAPDS9960Gesture.setText(R.string.down);
                        break;
                    case 3:
                        tvSensorAPDS9960Gesture.setText(R.string.left);
                        break;
                    case 4:
                        tvSensorAPDS9960Gesture.setText(R.string.right);
                        break;
                    default:
                        break;
                }
            }

            samplesEditBox.setText(String.valueOf(counter));
            if (counter == 0 && !runIndefinitely) {
                play = false;
                playPauseButton.setImageResource(R.drawable.circle_play_button);
            }
            synchronized (lock) {
                lock.notify();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return true;
    }
}
