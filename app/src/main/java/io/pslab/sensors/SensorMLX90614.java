package io.pslab.sensors;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import io.pslab.communication.sensors.MLX90614;
import io.pslab.others.ScienceLabCommon;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Harsh on 6/6/18.
 */

public class SensorMLX90614 extends AppCompatActivity {

    private static final String PREF_NAME = "SensorMLX90614";
    private static final String KEY = "SensorMLX90614Key";

    private static int counter;
    private final Object lock = new Object();
    private ScienceLab scienceLab;
    private SensorMLX90614.SensorDataFetch sensorDataFetch;
    private TextView tvSensorMLX90614ObjectTemp;
    private TextView tvSensorMLX90614AmbientTemp;
    private MLX90614 sensorMLX90614;
    private LineChart mChartObjectTemperature;
    private LineChart mChartAmbientTemperature;
    private long startTime;
    private int flag;
    private ArrayList<Entry> entriesObjectTemperature;
    private ArrayList<Entry> entriesAmbientTemperature;
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
        setContentView(R.layout.sensor_mlx90614);
        howToConnectDialog(getString(R.string.ir_thermometer), getString(R.string.ir_thermometer_intro), R.drawable.mlx90614_schematic, getString(R.string.ir_thermometer_desc));

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
            sensorMLX90614 = new MLX90614(i2c);
        } catch (IOException e) {
            e.printStackTrace();
        }

        entriesObjectTemperature = new ArrayList<>();
        entriesAmbientTemperature = new ArrayList<>();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (scienceLab.isConnected() && shouldPlay()) {
                        sensorDataFetch = new SensorMLX90614.SensorDataFetch();
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

        tvSensorMLX90614ObjectTemp = findViewById(R.id.tv_sensor_mlx90614_object_temp);
        tvSensorMLX90614AmbientTemp = findViewById(R.id.tv_sensor_mlx90614_ambient_temp);

        mChartObjectTemperature = findViewById(R.id.chart_obj_temp_mlx);
        mChartAmbientTemperature = findViewById(R.id.chart_amb_temp_mlx);

        XAxis xObjectTemperature = mChartObjectTemperature.getXAxis();
        YAxis yObjectTemperature = mChartObjectTemperature.getAxisLeft();
        YAxis yObjectTemperature2 = mChartObjectTemperature.getAxisRight();

        XAxis xAmbientTemperature = mChartAmbientTemperature.getXAxis();
        YAxis yAmbientTemperature = mChartAmbientTemperature.getAxisLeft();
        YAxis yAmbientTemperature2 = mChartAmbientTemperature.getAxisRight();

        mChartObjectTemperature.setTouchEnabled(true);
        mChartObjectTemperature.setHighlightPerDragEnabled(true);
        mChartObjectTemperature.setDragEnabled(true);
        mChartObjectTemperature.setScaleEnabled(true);
        mChartObjectTemperature.setDrawGridBackground(false);
        mChartObjectTemperature.setPinchZoom(true);
        mChartObjectTemperature.setScaleYEnabled(false);
        mChartObjectTemperature.setBackgroundColor(Color.BLACK);
        mChartObjectTemperature.getDescription().setEnabled(false);

        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);
        mChartObjectTemperature.setData(data);

        Legend l = mChartObjectTemperature.getLegend();
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.WHITE);

        xObjectTemperature.setTextColor(Color.WHITE);
        xObjectTemperature.setDrawGridLines(true);
        xObjectTemperature.setAvoidFirstLastClipping(true);

        yObjectTemperature.setTextColor(Color.WHITE);
        yObjectTemperature.setAxisMaximum(125f);
        yObjectTemperature.setAxisMinimum(-40f);
        yObjectTemperature.setDrawGridLines(true);
        yObjectTemperature.setLabelCount(10);

        yObjectTemperature2.setDrawGridLines(false);

        mChartAmbientTemperature.setTouchEnabled(true);
        mChartAmbientTemperature.setHighlightPerDragEnabled(true);
        mChartAmbientTemperature.setDragEnabled(true);
        mChartAmbientTemperature.setScaleEnabled(true);
        mChartAmbientTemperature.setDrawGridBackground(false);
        mChartAmbientTemperature.setPinchZoom(true);
        mChartAmbientTemperature.setScaleYEnabled(false);
        mChartAmbientTemperature.setBackgroundColor(Color.BLACK);
        mChartAmbientTemperature.getDescription().setEnabled(false);

        LineData data2 = new LineData();
        data.setValueTextColor(Color.WHITE);
        mChartAmbientTemperature.setData(data2);

        Legend l2 = mChartAmbientTemperature.getLegend();
        l2.setForm(Legend.LegendForm.LINE);
        l2.setTextColor(Color.WHITE);

        xAmbientTemperature.setTextColor(Color.WHITE);
        xAmbientTemperature.setDrawGridLines(true);
        xAmbientTemperature.setAvoidFirstLastClipping(true);

        yAmbientTemperature.setTextColor(Color.WHITE);
        yAmbientTemperature.setAxisMaximum(380f);
        yAmbientTemperature.setAxisMinimum(-70f);
        yAmbientTemperature.setDrawGridLines(true);
        yAmbientTemperature.setLabelCount(10);

        yAmbientTemperature2.setDrawGridLines(false);
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

    @SuppressLint("ResourceType")
    public void howToConnectDialog(String title, String intro, int imageID, String desc) {
        try {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.custom_dialog_box, null);
            builder.setView(dialogView);
            builder.setTitle(title);

            final TextView dialogText = (TextView) dialogView.findViewById(R.id.custom_dialog_text);
            final TextView dialogDesc = (TextView) dialogView.findViewById(R.id.description_text);
            final ImageView dialogImage = (ImageView) dialogView.findViewById(R.id.custom_dialog_schematic);
            final CheckBox doNotShowDialog = (CheckBox) dialogView.findViewById(R.id.toggle_show_again);
            final Button okButton = (Button) dialogView.findViewById(R.id.dismiss_button);
            dialogText.setText(intro);
            dialogImage.setImageResource(imageID);
            dialogDesc.setText(desc);

            final SharedPreferences sharedPreferences = this.getSharedPreferences(PREF_NAME, MODE_PRIVATE);
            final AlertDialog dialog = builder.create();
            Boolean skipDialog = sharedPreferences.getBoolean(KEY, false);
            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (doNotShowDialog.isChecked()) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean(KEY, true);
                        editor.apply();
                    }
                    dialog.dismiss();
                }
            });
            if (!skipDialog) {
                dialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class SensorDataFetch extends AsyncTask<Void, Void, Void> {

        private Double dataMLX90614ObjectTemp;
        private Double dataMLX90614AmbientTemp;
        private long timeElapsed;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (sensorMLX90614 != null) {
                    dataMLX90614ObjectTemp = sensorMLX90614.getObjectTemperature();
                    dataMLX90614AmbientTemp = sensorMLX90614.getAmbientTemperature();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            timeElapsed = (System.currentTimeMillis() - startTime) / 1000;
            entriesObjectTemperature.add(new Entry((float) timeElapsed, dataMLX90614ObjectTemp.floatValue()));
            entriesAmbientTemperature.add(new Entry((float) timeElapsed, dataMLX90614AmbientTemp.floatValue()));
            return null;
        }

        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            tvSensorMLX90614ObjectTemp.setText(DataFormatter.formatDouble(dataMLX90614ObjectTemp, DataFormatter.HIGH_PRECISION_FORMAT));
            tvSensorMLX90614AmbientTemp.setText(DataFormatter.formatDouble(dataMLX90614AmbientTemp, DataFormatter.HIGH_PRECISION_FORMAT));

            LineDataSet dataSet1 = new LineDataSet(entriesObjectTemperature, getString(R.string.object_temp));
            LineDataSet dataSet2 = new LineDataSet(entriesAmbientTemperature, getString(R.string.ambient_temp));

            dataSet1.setDrawCircles(true);
            dataSet2.setDrawCircles(true);

            LineData data1 = new LineData(dataSet1);
            mChartObjectTemperature.setData(data1);
            mChartObjectTemperature.notifyDataSetChanged();
            mChartObjectTemperature.setVisibleXRangeMaximum(10);
            mChartObjectTemperature.moveViewToX(data1.getEntryCount());
            mChartObjectTemperature.invalidate();

            LineData data2 = new LineData(dataSet2);
            mChartAmbientTemperature.setData(data2);
            mChartAmbientTemperature.notifyDataSetChanged();
            mChartAmbientTemperature.setVisibleXRangeMaximum(10);
            mChartAmbientTemperature.moveViewToX(data2.getEntryCount());
            mChartAmbientTemperature.invalidate();
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
