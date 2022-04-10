package io.pslab.sensors;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
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

import io.pslab.R;
import io.pslab.communication.ScienceLab;
import io.pslab.communication.peripherals.I2C;
import io.pslab.communication.sensors.ADS1115;
import io.pslab.others.ScienceLabCommon;

public class SensorADS1115 extends AppCompatActivity {
    private static int counter;
    private final Object lock = new Object();
    private ScienceLab scienceLab;
    private SensorADS1115.SensorDataFetch sensorDataFetch;
    private TextView tvSensorADS1115;
    private LineChart mChart;
    private long startTime;
    private int flag;
    private ArrayList<Entry> entries;
    private RelativeLayout sensorDock;
    private CheckBox indefiniteSamplesCheckBox;
    private EditText samplesEditBox;
    private SeekBar timeGapSeekbar;
    private TextView timeGapLabel;
    private ImageButton playPauseButton;
    private boolean play;
    private boolean runIndefinitely;
    private int timeGap;

    private ADS1115 sensorADS1115;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensor_ads1115);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.ads1115);
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

        scienceLab = ScienceLabCommon.scienceLab;
        I2C i2c = scienceLab.i2c;
        entries = new ArrayList<>();
        try {
            sensorADS1115 = new ADS1115(i2c);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (scienceLab.isConnected() && shouldPlay()) {
                        sensorDataFetch = new SensorADS1115.SensorDataFetch();
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

        tvSensorADS1115 = findViewById(R.id.tv_sensor_ads1115);
        mChart = findViewById(R.id.chart_sensor_ads);

        Spinner spinnerSensorADS1115Gain = findViewById(R.id.spinner_sensor_ads1115_gain);
        Spinner spinnerSensorADS1115Channel = findViewById(R.id.spinner_sensor_ads1115_channel);
        Spinner spinnerSensorADS1115Rate = findViewById(R.id.spinner_sensor_ads1115_rate);

        if (sensorADS1115 != null) {
            sensorADS1115.setGain(spinnerSensorADS1115Gain.getSelectedItem().toString());
        }
        if (sensorADS1115 != null) {
            sensorADS1115.setChannel(spinnerSensorADS1115Channel.getSelectedItem().toString());
        }
        if (sensorADS1115 != null) {
            sensorADS1115.setDataRate(Integer.parseInt(spinnerSensorADS1115Rate.getSelectedItem().toString()));
        }
        XAxis x = mChart.getXAxis();
        YAxis y = mChart.getAxisLeft();
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
        data.setValueTextColor(Color.WHITE);
        mChart.setData(data);

        Legend l = mChart.getLegend();
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.WHITE);

        x.setTextColor(Color.WHITE);
        x.setDrawGridLines(true);
        x.setAvoidFirstLastClipping(true);

        y.setTextColor(Color.WHITE);
        y.setAxisMaximum(6.15f);
        y.setAxisMinimum(-6.15f);
        y.setDrawGridLines(true);
        y.setLabelCount(10);

        y2.setDrawGridLines(false);
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

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private class SensorDataFetch extends AsyncTask<Void, Void, Void> {

        private int dataADS1115;
        private long timeElapsed;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (sensorADS1115 != null) {
                    dataADS1115 = sensorADS1115.getRaw();
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

            timeElapsed = (System.currentTimeMillis() - startTime) / 1000;
            entries.add(new Entry((float) timeElapsed, dataADS1115));

            return null;
        }

        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            tvSensorADS1115.setText(String.valueOf(dataADS1115));

            LineDataSet dataSet = new LineDataSet(entries, getString(R.string.bx));
            dataSet.setDrawCircles(true);
            LineData data = new LineData(dataSet);
            mChart.setData(data);
            mChart.notifyDataSetChanged();
            mChart.setVisibleXRangeMaximum(10);
            mChart.moveViewToX(data.getEntryCount());
            mChart.invalidate();
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
