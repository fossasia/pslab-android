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
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.pslab.R;
import io.pslab.communication.ScienceLab;
import io.pslab.communication.peripherals.I2C;
import io.pslab.communication.sensors.TSL2561;
import io.pslab.others.ScienceLabCommon;

/**
 * Created by Harsh on 6/6/18.
 */

public class SensorTSL2561 extends AppCompatActivity {
    private static int counter;
    private final Object lock = new Object();
    private ScienceLab scienceLab;
    private SensorTSL2561.SensorDataFetch sensorDataFetch;
    private TextView tvSensorTSL2561FullSpectrum;
    private TextView tvSensorTSL2561Infrared;
    private TextView tvSensorTSL2561Visible;
    private EditText etSensorTSL2561Timing;
    private TSL2561 sensorTSL2561;
    private LineChart mChart;
    private long startTime;
    private int flag;
    private ArrayList<Entry> entriesFull;
    private ArrayList<Entry> entriesInfrared;
    private ArrayList<Entry> entriesVisible;
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
        setContentView(R.layout.sensor_tsl2561);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.tsl2561);
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
        try {
            sensorTSL2561 = new TSL2561(i2c, scienceLab);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        entriesFull = new ArrayList<>();
        entriesInfrared = new ArrayList<>();
        entriesVisible = new ArrayList<>();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (scienceLab.isConnected() && shouldPlay()) {
                        try {
                            sensorDataFetch = new SensorTSL2561.SensorDataFetch();
                        } catch (IOException | InterruptedException e) {
                            e.printStackTrace();
                        }
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

        tvSensorTSL2561FullSpectrum = findViewById(R.id.tv_sensor_tsl2561_full);
        tvSensorTSL2561Infrared = findViewById(R.id.tv_sensor_tsl2561_infrared);
        tvSensorTSL2561Visible = findViewById(R.id.tv_sensor_tsl2561_visible);
        Spinner spinnerSensorTSL2561Gain = findViewById(R.id.spinner_sensor_tsl2561_gain);
        etSensorTSL2561Timing = findViewById(R.id.et_sensor_tsl2561_timing);
        mChart = findViewById(R.id.chart_tsl2561);

        try {
            if (sensorTSL2561 != null & scienceLab.isConnected()) {
                sensorTSL2561.setGain(spinnerSensorTSL2561Gain.getSelectedItem().toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
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
        y.setAxisMaximum(1700f);
        y.setAxisMinimum(0f);
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

        private int[] dataTSL2561;
        private long timeElapsed;

        private SensorDataFetch() throws IOException, InterruptedException {
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (sensorTSL2561 != null) {
                    dataTSL2561 = sensorTSL2561.getRaw();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            timeElapsed = (System.currentTimeMillis() - startTime) / 1000;
            entriesFull.add(new Entry((float) timeElapsed, dataTSL2561[0]));
            entriesInfrared.add(new Entry((float) timeElapsed, dataTSL2561[1]));
            entriesVisible.add(new Entry((float) timeElapsed, dataTSL2561[2]));
            return null;
        }

        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            tvSensorTSL2561FullSpectrum.setText(String.valueOf(dataTSL2561[0]));
            tvSensorTSL2561Infrared.setText(String.valueOf(dataTSL2561[1]));
            tvSensorTSL2561Visible.setText(String.valueOf(dataTSL2561[2]));

            LineDataSet dataset1 = new LineDataSet(entriesFull, getString(R.string.full));
            LineDataSet dataSet2 = new LineDataSet(entriesInfrared, getString(R.string.infrared));
            LineDataSet dataSet3 = new LineDataSet(entriesVisible, getString(R.string.visible));

            dataset1.setColor(Color.BLUE);
            dataSet2.setColor(Color.GREEN);
            dataSet3.setColor(Color.RED);

            dataset1.setDrawCircles(true);
            dataSet2.setDrawCircles(true);
            dataSet3.setDrawCircles(true);

            List<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(dataset1);
            dataSets.add(dataSet2);
            dataSets.add(dataSet3);

            LineData data = new LineData(dataSets);
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
