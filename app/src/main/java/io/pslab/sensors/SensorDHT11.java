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

import java.io.IOException;
import java.util.ArrayList;

import io.pslab.DataFormatter;
import io.pslab.R;
import io.pslab.communication.ScienceLab;
import io.pslab.communication.peripherals.I2C;
import io.pslab.communication.sensors.DHT11;
import io.pslab.others.ScienceLabCommon;

public class SensorDHT11 extends AppCompatActivity {
    private static int counter;
    private final Object lock = new Object();
    private ScienceLab scienceLab;
    private SensorDHT11.SensorDataFetch sensorDataFetch;
    private TextView tvSensorDHT11Temp;
    private TextView tvSensorDHT11Humidity;
    private DHT11 sensorDHT11;
    private LineChart mChartTemperature;
    private LineChart mChartHumidity;
    private long startTime;
    private int flag;
    private ArrayList<Entry> entriesTemperature;
    private ArrayList<Entry> entriesHumidity;
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
        setContentView(R.layout.sensor_dht11);

        sensorDock = findViewById(R.id.sensor_control_dock_layout);
        indefiniteSamplesCheckBox = findViewById(R.id.checkBox_samples_sensor);
        samplesEditBox = findViewById(R.id.editBox_samples_sensors);
        timeGapSeekbar = findViewById(R.id.seekBar_timegap_sensor);
        timeGapLabel = findViewById(R.id.tv_timegap_label);
        playPauseButton = findViewById(R.id.imageButton_play_pause_sensor);
        setSensorDock();
        sensorDock.setVisibility(View.VISIBLE);

        scienceLab = ScienceLabCommon.scienceLab;
        I2C i2c = scienceLab.i2c;
        try {
            sensorDHT11 = new DHT11(i2c, scienceLab);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        entriesTemperature = new ArrayList<>();
        entriesHumidity = new ArrayList<>();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (scienceLab.isConnected() && shouldPlay()) {
                        sensorDataFetch = new SensorDHT11.SensorDataFetch();
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

        tvSensorDHT11Temp = findViewById(R.id.tv_sensor_dht11_temp);
        tvSensorDHT11Humidity = findViewById(R.id.tv_sensor_dht11_humidity);
        mChartTemperature = findViewById(R.id.chart_temperature_dht11);
        mChartHumidity = findViewById(R.id.chart_humidity_dht11);

        XAxis xTemperature = mChartTemperature.getXAxis();
        YAxis yTemperature = mChartTemperature.getAxisLeft();
        YAxis yTemperature2 = mChartTemperature.getAxisRight();

        XAxis xHumidity = mChartHumidity.getXAxis();
        YAxis yHumidity = mChartHumidity.getAxisLeft();
        YAxis yHumidity2 = mChartHumidity.getAxisRight();

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
        yTemperature.setAxisMaximum(125f);
        yTemperature.setAxisMinimum(-40f);
        yTemperature.setDrawGridLines(true);
        yTemperature.setLabelCount(10);

        yTemperature2.setDrawGridLines(false);

        mChartHumidity.setTouchEnabled(true);
        mChartHumidity.setHighlightPerDragEnabled(true);
        mChartHumidity.setDragEnabled(true);
        mChartHumidity.setScaleEnabled(true);
        mChartHumidity.setDrawGridBackground(false);
        mChartHumidity.setPinchZoom(true);
        mChartHumidity.setScaleYEnabled(false);
        mChartHumidity.setBackgroundColor(Color.BLACK);
        mChartHumidity.getDescription().setEnabled(false);

        LineData data2 = new LineData();
        data.setValueTextColor(Color.WHITE);
        mChartHumidity.setData(data2);

        Legend l2 = mChartHumidity.getLegend();
        l2.setForm(Legend.LegendForm.LINE);
        l2.setTextColor(Color.WHITE);

        xHumidity.setTextColor(Color.WHITE);
        xHumidity.setDrawGridLines(true);
        xHumidity.setAvoidFirstLastClipping(true);

        yHumidity.setTextColor(Color.WHITE);
        yHumidity.setAxisMaximum(100f);
        yHumidity.setAxisMinimum(0f);
        yHumidity.setDrawGridLines(true);
        yHumidity.setLabelCount(10);

        yHumidity2.setDrawGridLines(false);

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

        private ArrayList<Double> dataDHT11Temp = new ArrayList<>();
        private ArrayList<Double> dataDHT11Humidity = new ArrayList<>();
        private long timeElapsed;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (sensorDHT11 != null) {
                    sensorDHT11.selectParameter("temperature");
                    dataDHT11Temp = sensorDHT11.getRaw();
                    sensorDHT11.selectParameter("humidity");
                    dataDHT11Humidity = sensorDHT11.getRaw();
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
            timeElapsed = (System.currentTimeMillis() - startTime) / 1000;
            entriesTemperature.add(new Entry((float) timeElapsed, dataDHT11Temp.get(0).floatValue()));
            entriesTemperature.add(new Entry((float) timeElapsed, dataDHT11Humidity.get(0).floatValue()));
            return null;
        }

        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            tvSensorDHT11Temp.setText(DataFormatter.formatDouble(dataDHT11Temp.get(0), DataFormatter.HIGH_PRECISION_FORMAT));
            tvSensorDHT11Humidity.setText(DataFormatter.formatDouble(dataDHT11Humidity.get(0), DataFormatter.HIGH_PRECISION_FORMAT));

            LineDataSet dataSet1 = new LineDataSet(entriesTemperature, getString(R.string.temperature));
            LineDataSet dataSet2 = new LineDataSet(entriesHumidity, getString(R.string.humidity));

            dataSet1.setDrawCircles(true);
            dataSet2.setDrawCircles(true);

            LineData data = new LineData(dataSet1);
            mChartTemperature.setData(data);
            mChartTemperature.notifyDataSetChanged();
            mChartTemperature.setVisibleXRangeMaximum(10);
            mChartTemperature.moveViewToX(data.getEntryCount());
            mChartTemperature.invalidate();

            LineData data2 = new LineData(dataSet2);
            mChartHumidity.setData(data2);
            mChartHumidity.notifyDataSetChanged();
            mChartHumidity.setVisibleXRangeMaximum(10);
            mChartHumidity.moveViewToX(data2.getEntryCount());
            mChartHumidity.invalidate();
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
