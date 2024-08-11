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
import io.pslab.communication.sensors.CCS811;
import io.pslab.communication.sensors.SHT21;
import io.pslab.others.ScienceLabCommon;

public class SensorCCS811 extends AppCompatActivity {
    private static int counter;
    private final Object lock = new Object();
    private ScienceLab scienceLab;
    private SensorCCS811.SensorDataFetch sensorDataFetch;
    private TextView tvSensorCCS811eCO2;
    private TextView tvSensorCCS811TVOC;
    private CCS811 sensorCCS811;
    private LineChart mCharteCO2;
    private LineChart mChartTVOC;
    private long startTime;
    private int flag;
    private ArrayList<Entry> entrieseCO2;
    private ArrayList<Entry> entriesTVOC;
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
        setContentView(R.layout.sensor_ccs811);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.ccs811);
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
            sensorCCS811 = new CCS811(i2c, scienceLab);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        entrieseCO2 = new ArrayList<>();
        entriesTVOC = new ArrayList<>();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (scienceLab.isConnected() && shouldPlay()) {
                        sensorDataFetch = new SensorCCS811.SensorDataFetch();
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

        tvSensorCCS811eCO2 = findViewById(R.id.tv_sensor_ccs811_eCO2);
        tvSensorCCS811TVOC = findViewById(R.id.tv_sensor_ccs811_TVOC);
        mCharteCO2 = findViewById(R.id.chart_eCO2_ccs811);
        mChartTVOC = findViewById(R.id.chart_TVOC_ccs811);

        XAxis xeCO2 = mCharteCO2.getXAxis();
        YAxis yeCO2 = mCharteCO2.getAxisLeft();
        YAxis yeCO22 = mCharteCO2.getAxisRight();

        XAxis xTVOC = mChartTVOC.getXAxis();
        YAxis yTVOC = mChartTVOC.getAxisLeft();
        YAxis yTVOC2 = mChartTVOC.getAxisRight();

        mCharteCO2.setTouchEnabled(true);
        mCharteCO2.setHighlightPerDragEnabled(true);
        mCharteCO2.setDragEnabled(true);
        mCharteCO2.setScaleEnabled(true);
        mCharteCO2.setDrawGridBackground(false);
        mCharteCO2.setPinchZoom(true);
        mCharteCO2.setScaleYEnabled(false);
        mCharteCO2.setBackgroundColor(Color.BLACK);
        mCharteCO2.getDescription().setEnabled(false);

        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);
        mCharteCO2.setData(data);

        Legend l = mCharteCO2.getLegend();
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.WHITE);

        xeCO2.setTextColor(Color.WHITE);
        xeCO2.setDrawGridLines(true);
        xeCO2.setAvoidFirstLastClipping(true);

        yeCO2.setTextColor(Color.WHITE);
        yeCO2.setAxisMaximum(10000f);
        yeCO2.setAxisMinimum(0);
        yeCO2.setDrawGridLines(true);
        yeCO2.setLabelCount(10);

        yeCO22.setDrawGridLines(false);

        mChartTVOC.setTouchEnabled(true);
        mChartTVOC.setHighlightPerDragEnabled(true);
        mChartTVOC.setDragEnabled(true);
        mChartTVOC.setScaleEnabled(true);
        mChartTVOC.setDrawGridBackground(false);
        mChartTVOC.setPinchZoom(true);
        mChartTVOC.setScaleYEnabled(false);
        mChartTVOC.setBackgroundColor(Color.BLACK);
        mChartTVOC.getDescription().setEnabled(false);

        LineData data2 = new LineData();
        data.setValueTextColor(Color.WHITE);
        mChartTVOC.setData(data2);

        Legend l2 = mChartTVOC.getLegend();
        l2.setForm(Legend.LegendForm.LINE);
        l2.setTextColor(Color.WHITE);

        xTVOC.setTextColor(Color.WHITE);
        xTVOC.setDrawGridLines(true);
        xTVOC.setAvoidFirstLastClipping(true);

        yTVOC.setTextColor(Color.WHITE);
        yTVOC.setAxisMaximum(2000f);
        yTVOC.setAxisMinimum(0f);
        yTVOC.setDrawGridLines(true);
        yTVOC.setLabelCount(10);

        yTVOC2.setDrawGridLines(false);

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

        private double[] dataCS811;
        private Double dataCCS811eCO2;
        private Double dataCCS811TVOC;
        private long timeElapsed;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (sensorCCS811 != null) {
                    dataCS811 = sensorCCS811.getRaw();
                    dataCCS811eCO2 = dataCS811[0];
                    dataCCS811TVOC = dataCS811[1];
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            timeElapsed = (System.currentTimeMillis() - startTime) / 1000;
            entrieseCO2.add(new Entry((float) timeElapsed, dataCCS811eCO2.floatValue()));
            entriesTVOC.add(new Entry((float) timeElapsed, dataCCS811TVOC.floatValue()));
            return null;
        }

        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            tvSensorCCS811eCO2.setText(DataFormatter.formatDouble(dataCCS811eCO2, DataFormatter.HIGH_PRECISION_FORMAT));
            tvSensorCCS811TVOC.setText(DataFormatter.formatDouble(dataCCS811TVOC, DataFormatter.HIGH_PRECISION_FORMAT));

            LineDataSet dataSet1 = new LineDataSet(entrieseCO2, getString(R.string.eCO2));
            LineDataSet dataSet2 = new LineDataSet(entriesTVOC, getString(R.string.eTVOC));

            dataSet1.setDrawCircles(true);
            dataSet2.setDrawCircles(true);

            LineData data = new LineData(dataSet1);
            mCharteCO2.setData(data);
            mCharteCO2.notifyDataSetChanged();
            mCharteCO2.setVisibleXRangeMaximum(10);
            mCharteCO2.moveViewToX(data.getEntryCount());
            mCharteCO2.invalidate();

            LineData data2 = new LineData(dataSet2);
            mChartTVOC.setData(data2);
            mChartTVOC.notifyDataSetChanged();
            mChartTVOC.setVisibleXRangeMaximum(10);
            mChartTVOC.moveViewToX(data2.getEntryCount());
            mChartTVOC.invalidate();
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
