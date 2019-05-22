package io.pslab.fragment;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.pslab.R;
import io.pslab.activity.AccelerometerActivity;
import io.pslab.communication.ScienceLab;
import io.pslab.communication.peripherals.I2C;
import io.pslab.communication.sensors.BH1750;
import io.pslab.communication.sensors.TSL2561;
import io.pslab.models.AccelerometerData;
import io.pslab.models.SensorDataBlock;
import io.pslab.others.CSVLogger;
import io.pslab.others.ScienceLabCommon;

import static android.content.Context.SENSOR_SERVICE;
import static io.pslab.others.CSVLogger.CSV_DIRECTORY;

/**
 * Created by Kunal on 18-12-18
 */

public class AccelerometerDataFragment extends Fragment {

    private static int sensorType = 0;
    private static int highLimit = 2000;
    private static int updatePeriod = 100;
    private static int gain = 1;
    private int count = 0, turns = 0;
    private float sum = 0;
    private boolean returningFromPause = false;
    private int[] colors = {Color.YELLOW, Color.MAGENTA, Color.GREEN};
    private float accelerometerValue_X = -1;
    private float accelerometerValue_Y = -1;
    private float accelerometerValue_Z = -1;

    private enum ACCELEROMETER_SENSOR {INBUILT_SENSOR, BH1750_SENSOR, TSL2561_SENSOR}

    @BindView(R.id.x_axis_image)
    ImageView x_axis;
    @BindView(R.id.x_accel_value)
    TextView x_accel_value;
    @BindView(R.id.x_accel_min_text)
    TextView x_accel_min;
    @BindView(R.id.x_accel_max_text)
    TextView x_accel_max;
    @BindView(R.id.x_tv_graph_label_xaxis_hmc)
    TextView x_tv_graph_label_xaxis_hmc;
    @BindView(R.id.x_tv_label_left_yaxis_hmc)
    TextView x_tv_label_left_yaxis_hmc;
    @BindView(R.id.chart_accelerometer_x)
    LineChart x_chart_accelerometer;

    @BindView(R.id.y_axis_image)
    ImageView y_axis;
    @BindView(R.id.y_accel_value)
    TextView y_accel_value;
    @BindView(R.id.y_accel_min_text)
    TextView y_accel_min_text;
    @BindView(R.id.y_accel_max_text)
    TextView y_accel_max_text;
    @BindView(R.id.y_tv_graph_label_xaxis_hmc)
    TextView y_tv_graph_label_xaxis_hmc;
    @BindView(R.id.y_tv_label_left_yaxis_hmc)
    TextView y_tv_label_left_yaxis_hmc;
    @BindView(R.id.y_chart_accelerometer)
    LineChart y_chart_accelerometer;

    @BindView(R.id.z_axis_image)
    ImageView z_axis_image;
    @BindView(R.id.z_accel_value)
    TextView z_accel_value;
    @BindView(R.id.z_accel_min_text)
    TextView z_accel_min_text;
    @BindView(R.id.z_accel_max_text)
    TextView z_accel_max_text;
    @BindView(R.id.z_tv_graph_label_xaxis_hmc)
    TextView z_tv_graph_label_xaxis_hmc;
    @BindView(R.id.z_tv_label_left_yaxis_hmc)
    TextView z_tv_label_left_yaxis_hmc;
    @BindView(R.id.z_chart_accelerometer)
    LineChart z_chart_accelerometer;

    private Timer graphTimer;
    private SensorManager sensorManager;
    private Sensor sensor;
    private long startTime, block;
    private ArrayList<Entry> entriesX, entriesY, entriesZ;
    private ArrayList<AccelerometerData> recordedAccelerometerArray;
    private AccelerometerData sensorData;
    private float currentMinX = Integer.MAX_VALUE;
    private float currentMaxX = Integer.MIN_VALUE;
    private float currentMinY = Integer.MAX_VALUE;
    private float currentMaxY = Integer.MIN_VALUE;
    private float currentMinZ = Integer.MAX_VALUE;
    private float currentMaxZ = Integer.MIN_VALUE;
    private YAxis y;
    private Unbinder unbinder;
    private long previousTimeElapsed_X = (System.currentTimeMillis() - startTime) / updatePeriod;
    private long previousTimeElapsed_Y = (System.currentTimeMillis() - startTime) / updatePeriod;
    private long previousTimeElapsed_Z = (System.currentTimeMillis() - startTime) / updatePeriod;
    private AccelerometerActivity accelerometerSensor;
    private DecimalFormat df = new DecimalFormat("+#0.0;-#0.0");
    private View rootView;

    public static AccelerometerDataFragment newInstance() {
        return new AccelerometerDataFragment();
    }

    public static void setParameters(int highLimit, int updatePeriod, String type, String gain) {
        AccelerometerDataFragment.highLimit = highLimit;
        AccelerometerDataFragment.updatePeriod = updatePeriod;
        AccelerometerDataFragment.sensorType = Integer.valueOf(type);
        AccelerometerDataFragment.gain = Integer.valueOf(gain);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startTime = System.currentTimeMillis();
        entriesX = new ArrayList<>();
        entriesY = new ArrayList<>();
        entriesZ = new ArrayList<>();
        accelerometerSensor = (AccelerometerActivity) getActivity();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_accelerometer_data, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        setupInstruments();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (accelerometerSensor.playingData) {
            recordedAccelerometerArray = new ArrayList<>();
            resetInstrumentData();
            playRecordedData();
        } else if (accelerometerSensor.viewingData) {
            recordedAccelerometerArray = new ArrayList<>();
            resetInstrumentData();
            plotAllRecordedData();
        } else if (!accelerometerSensor.isRecording) {
            updateGraphs();
            sum = 0;
            count = 0;
            currentMinX = Integer.MAX_VALUE;
            currentMaxX = Integer.MIN_VALUE;
            currentMinY = Integer.MAX_VALUE;
            currentMaxY = Integer.MIN_VALUE;
            currentMinZ = Integer.MAX_VALUE;
            currentMaxZ = Integer.MIN_VALUE;
            entriesX.clear();
            entriesY.clear();
            entriesZ.clear();

            x_chart_accelerometer.clear();
            x_chart_accelerometer.invalidate();

            y_chart_accelerometer.clear();
            y_chart_accelerometer.invalidate();

            z_chart_accelerometer.clear();
            z_chart_accelerometer.invalidate();

            initiateAccelerometerSensor(sensorType);
        } else if (returningFromPause) {
            updateGraphs();
        }
    }

    private void plotAllRecordedData() {
        recordedAccelerometerArray.addAll(accelerometerSensor.recordedAccelerometerData);
        if (recordedAccelerometerArray.size() != 0) {
            for (AccelerometerData d : recordedAccelerometerArray) {
                if (currentMaxX < d.getAccelerometerX()) {
                    currentMaxX = d.getAccelerometerX();
                }
                if (currentMinX > d.getAccelerometerX()) {
                    currentMinX = d.getAccelerometerX();
                }
                Entry entryX = new Entry((float) (d.getTime() - d.getBlock()) / 1000, d.getAccelerometerX());
                entriesX.add(entryX);
                count++;
                sum += entryX.getY();
                Entry entryY = new Entry((float) (d.getTime() - d.getBlock()) / 1000, d.getAccelerometerY());
                entriesY.add(entryY);
                count++;
                sum += entryY.getY();
                Entry entryZ = new Entry((float) (d.getTime() - d.getBlock()) / 1000, d.getAccelerometerZ());
                entriesZ.add(entryZ);
                count++;
                sum += entryZ.getY();
            }
            y.setAxisMaximum(20);
            y.setAxisMinimum(-20);
            y.setLabelCount(5);

            LineDataSet dataSet_X = new LineDataSet(entriesX, getString(R.string.accelerometer));
            LineData data_x = new LineData(dataSet_X);
            dataSet_X.setDrawCircles(false);
            dataSet_X.setDrawValues(false);
            dataSet_X.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            dataSet_X.setLineWidth(1);
            dataSet_X.setColor(colors[0]);

            x_chart_accelerometer.setData(data_x);
            x_chart_accelerometer.notifyDataSetChanged();
            x_chart_accelerometer.setVisibleXRangeMaximum(3);
            x_chart_accelerometer.moveViewToX(data_x.getEntryCount());
            x_chart_accelerometer.invalidate();

            LineDataSet dataSet_Y = new LineDataSet(entriesY, getString(R.string.accelerometer));
            LineData data_y = new LineData(dataSet_Y);
            dataSet_Y.setDrawCircles(false);
            dataSet_Y.setDrawValues(false);
            dataSet_Y.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            dataSet_Y.setLineWidth(1);
            dataSet_Y.setColor(colors[1]);

            y_chart_accelerometer.setData(data_y);
            y_chart_accelerometer.notifyDataSetChanged();
            y_chart_accelerometer.setVisibleXRangeMaximum(3);
            y_chart_accelerometer.moveViewToX(data_y.getEntryCount());
            y_chart_accelerometer.invalidate();

            LineDataSet dataSet_Z = new LineDataSet(entriesZ, getString(R.string.accelerometer));
            LineData data_z = new LineData(dataSet_Z);
            dataSet_Z.setDrawCircles(false);
            dataSet_Z.setDrawValues(false);
            dataSet_Z.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            dataSet_Z.setLineWidth(1);
            dataSet_Z.setColor(colors[2]);

            z_chart_accelerometer.setData(data_z);
            z_chart_accelerometer.notifyDataSetChanged();
            z_chart_accelerometer.setVisibleXRangeMaximum(3);
            z_chart_accelerometer.moveViewToX(data_z.getEntryCount());
            z_chart_accelerometer.invalidate();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (graphTimer != null) {
            graphTimer.cancel();
        }
        if (sensorManager != null) {
            sensorManager.unregisterListener(accelerometerSensorEventListener);
        }
        unbinder.unbind();
    }

    private void playRecordedData() {
        recordedAccelerometerArray.addAll(accelerometerSensor.recordedAccelerometerData);
        try {
            if (recordedAccelerometerArray.size() > 1) {
                AccelerometerData i = recordedAccelerometerArray.get(1);
                long timeGap = i.getTime() - i.getBlock();
                processRecordedData(timeGap);
            } else {
                processRecordedData(0);
            }
        } catch (IllegalArgumentException e) {
            Toast.makeText(getActivity(),
                    getActivity().getResources().getString(R.string.no_data_fetched), Toast.LENGTH_SHORT).show();
        }
    }

    private void processRecordedData(long timeGap) {
        final Handler handler = new Handler();
        if (graphTimer != null) {
            graphTimer.cancel();
        } else {
            graphTimer = new Timer();
        }
        graphTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            boolean playComplete = false;
                            AccelerometerData d = recordedAccelerometerArray.get(turns);
                            turns++;
                            StringBuilder builder_x = new StringBuilder();
                            builder_x.append(df.format(d.getAccelerometerX()));
                            builder_x.append(" ");
                            builder_x.append(getResources().getString(R.string.meters_per_sec_text));
                            x_accel_value.setText(Html.fromHtml(builder_x.toString()));

                            if (currentMaxX < d.getAccelerometerX()) {
                                currentMaxX = d.getAccelerometerX();
                                StringBuilder builder_x_max = new StringBuilder();
                                builder_x_max.append("Max: ");
                                builder_x_max.append(df.format(currentMaxX));
                                builder_x_max.append(" ");
                                builder_x_max.append(getResources().getString(R.string.meters_per_sec_text));
                                x_accel_max.setText(Html.fromHtml(builder_x_max.toString()));
                            }
                            if (currentMinX > d.getAccelerometerX()) {
                                currentMinX = d.getAccelerometerX();
                                StringBuilder builder_x_min = new StringBuilder();
                                builder_x_min.append("Min: ");
                                builder_x_min.append(df.format(currentMinX));
                                builder_x_min.append(" ");
                                builder_x_min.append(getResources().getString(R.string.meters_per_sec_text));
                                x_accel_min.setText(Html.fromHtml(builder_x_min.toString()));
                            }
                            StringBuilder builder_y = new StringBuilder();
                            builder_y.append(df.format(d.getAccelerometerY()));
                            builder_y.append(" ");
                            builder_y.append(getResources().getString(R.string.meters_per_sec_text));
                            y_accel_value.setText(Html.fromHtml(builder_y.toString()));
                            if (currentMaxY < d.getAccelerometerY()) {
                                currentMaxY = d.getAccelerometerY();
                                StringBuilder builder_y_max = new StringBuilder();
                                builder_y_max.append("Max: ");
                                builder_y_max.append(df.format(currentMaxY));
                                builder_y_max.append(" ");
                                builder_y_max.append(getResources().getString(R.string.meters_per_sec_text));
                                y_accel_max_text.setText(Html.fromHtml(builder_y_max.toString()));
                            }
                            if (currentMinY > d.getAccelerometerY()) {
                                currentMinY = d.getAccelerometerY();
                                StringBuilder builder_y_min = new StringBuilder();
                                builder_y_min.append("Min: ");
                                builder_y_min.append(df.format(currentMinY));
                                builder_y_min.append(" ");
                                builder_y_min.append(getResources().getString(R.string.meters_per_sec_text));
                                y_accel_min_text.setText(Html.fromHtml(builder_y_min.toString()));
                            }
                            StringBuilder builder_z = new StringBuilder();
                            builder_z.append(df.format(d.getAccelerometerZ()));
                            builder_z.append(" ");
                            builder_z.append(getResources().getString(R.string.meters_per_sec_text));
                            z_accel_value.setText(Html.fromHtml(builder_z.toString()));
                            if (currentMaxZ < d.getAccelerometerZ()) {
                                currentMaxZ = d.getAccelerometerZ();
                                StringBuilder builder_z_max = new StringBuilder();
                                builder_z_max.append("Max: ");
                                builder_z_max.append(df.format(currentMaxZ));
                                builder_z_max.append(" ");
                                builder_z_max.append(getResources().getString(R.string.meters_per_sec_text));
                                z_accel_max_text.setText(Html.fromHtml(builder_z_max.toString()));
                            }
                            if (currentMinZ > d.getAccelerometerZ()) {
                                currentMinZ = d.getAccelerometerZ();
                                StringBuilder builder_z_min = new StringBuilder();
                                builder_z_min.append("Min: ");
                                builder_z_min.append(df.format(currentMinZ));
                                builder_z_min.append(" ");
                                builder_z_min.append(getResources().getString(R.string.meters_per_sec_text));
                                z_accel_min_text.setText(Html.fromHtml(builder_z_min.toString()));
                            }
                            y.setAxisMaximum(20);
                            y.setAxisMinimum(-20);
                            y.setLabelCount(5);

                            Entry entryX = new Entry((float) (d.getTime() - d.getBlock()) / 1000, d.getAccelerometerX());
                            entriesX.add(entryX);
                            count++;
                            sum += entryX.getY();
                            Entry entryY = new Entry((float) (d.getTime() - d.getBlock()) / 1000, d.getAccelerometerY());
                            entriesY.add(entryY);
                            count++;
                            sum += entryY.getY();
                            Entry entryZ = new Entry((float) (d.getTime() - d.getBlock()) / 1000, d.getAccelerometerZ());
                            entriesZ.add(entryZ);
                            count++;
                            sum += entryZ.getY();

                            LineDataSet dataSet_X = new LineDataSet(entriesX, getString(R.string.accelerometer));
                            LineData data_x = new LineData(dataSet_X);
                            dataSet_X.setDrawCircles(false);
                            dataSet_X.setDrawValues(false);
                            dataSet_X.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                            dataSet_X.setLineWidth(1);
                            dataSet_X.setColor(colors[0]);

                            x_chart_accelerometer.setData(data_x);
                            x_chart_accelerometer.notifyDataSetChanged();
                            x_chart_accelerometer.setVisibleXRangeMaximum(3);
                            x_chart_accelerometer.moveViewToX(data_x.getEntryCount());
                            x_chart_accelerometer.invalidate();

                            LineDataSet dataSet_Y = new LineDataSet(entriesY, getString(R.string.accelerometer));
                            LineData data_y = new LineData(dataSet_Y);
                            dataSet_Y.setDrawCircles(false);
                            dataSet_Y.setDrawValues(false);
                            dataSet_Y.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                            dataSet_Y.setLineWidth(1);
                            dataSet_Y.setColor(colors[1]);

                            y_chart_accelerometer.setData(data_y);
                            y_chart_accelerometer.notifyDataSetChanged();
                            y_chart_accelerometer.setVisibleXRangeMaximum(3);
                            y_chart_accelerometer.moveViewToX(data_y.getEntryCount());
                            y_chart_accelerometer.invalidate();

                            LineDataSet dataSet_Z = new LineDataSet(entriesZ, getString(R.string.accelerometer));
                            LineData data_z = new LineData(dataSet_Z);
                            dataSet_Z.setDrawCircles(false);
                            dataSet_Z.setDrawValues(false);
                            dataSet_Z.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                            dataSet_Z.setLineWidth(1);
                            dataSet_Z.setColor(colors[2]);

                            z_chart_accelerometer.setData(data_z);
                            z_chart_accelerometer.notifyDataSetChanged();
                            z_chart_accelerometer.setVisibleXRangeMaximum(3);
                            z_chart_accelerometer.moveViewToX(data_z.getEntryCount());
                            z_chart_accelerometer.invalidate();

                        } catch (IndexOutOfBoundsException e) {
                            graphTimer.cancel();
                            graphTimer = null;
                            turns = 0;
                            accelerometerSensor.playingData = false;
                            accelerometerSensor.startedPlay = false;
                            accelerometerSensor.invalidateOptionsMenu();
                        }
                    }
                });
            }
        }, 0, timeGap);
    }

    public void stopData() {
        if (graphTimer != null) {
            graphTimer.cancel();
            graphTimer = null;
        }
        recordedAccelerometerArray.clear();
        entriesX.clear();
        entriesY.clear();
        entriesZ.clear();
        plotAllRecordedData();
        accelerometerSensor.startedPlay = false;
        accelerometerSensor.playingData = false;
        turns = 0;
        accelerometerSensor.invalidateOptionsMenu();
    }

    public void saveGraph() {
        String fileName = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(accelerometerSensor.recordedAccelerometerData.get(0).getTime());
        File csvFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                File.separator + CSV_DIRECTORY + File.separator + accelerometerSensor.getSensorName() +
                File.separator + fileName + ".csv");
        if (!csvFile.exists()) {
            try {
                csvFile.createNewFile();
                PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(csvFile, true)));
                out.write("Timestamp,DateTime,ReadingsX,ReadingsY,ReadingsZ,Latitude,Longitude\n");
                for (AccelerometerData accelerometerData : accelerometerSensor.recordedAccelerometerData) {
                    out.write(accelerometerData.getTime() + ","
                            + CSVLogger.FILE_NAME_FORMAT.format(new Date(accelerometerData.getTime())) + ","
                            + accelerometerData.getAccelerometerX() + ","
                            + accelerometerData.getAccelerometerY() + ","
                            + accelerometerData.getAccelerometerZ() + ","
                            + accelerometerData.getLat() + ","
                            + accelerometerData.getLon() + "," + "\n");
                }
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        View view = rootView.findViewById(R.id.accelerometer_linearlayout);
        view.setDrawingCacheEnabled(true);
        Bitmap b = view.getDrawingCache();
        try {
            b.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(Environment.getExternalStorageDirectory().getAbsolutePath() +
                    File.separator + CSV_DIRECTORY + File.separator + accelerometerSensor.getSensorName() +
                    File.separator + CSVLogger.FILE_NAME_FORMAT.format(new Date()) + "_graph.jpg"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    public void playData() {
        resetInstrumentData();
        accelerometerSensor.startedPlay = true;
        try {
            if (recordedAccelerometerArray.size() > 1) {
                AccelerometerData i = recordedAccelerometerArray.get(1);
                long timeGap = i.getTime() - i.getBlock();
                processRecordedData(timeGap);
            } else {
                processRecordedData(0);
            }
        } catch (IllegalArgumentException e) {
            Toast.makeText(getActivity(),
                    getActivity().getResources().getString(R.string.no_data_fetched), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupInstruments() {
        LineData data_x = new LineData();
        XAxis x = x_chart_accelerometer.getXAxis();
        this.y = x_chart_accelerometer.getAxisLeft();
        YAxis y2 = x_chart_accelerometer.getAxisRight();

        x_chart_accelerometer.setTouchEnabled(true);
        x_chart_accelerometer.setHighlightPerDragEnabled(true);
        x_chart_accelerometer.setDragEnabled(true);
        x_chart_accelerometer.setScaleEnabled(true);
        x_chart_accelerometer.setDrawGridBackground(false);
        x_chart_accelerometer.setPinchZoom(true);
        x_chart_accelerometer.setScaleYEnabled(false);
        x_chart_accelerometer.setBackgroundColor(Color.BLACK);
        x_chart_accelerometer.getDescription().setEnabled(false);

        Legend l = x_chart_accelerometer.getLegend();
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.WHITE);

        x_chart_accelerometer.setData(data_x);

        x.setTextColor(Color.WHITE);
        x.setDrawGridLines(true);
        x.setAvoidFirstLastClipping(true);
        x.setDrawLabels(false);

        y.setTextColor(Color.WHITE);
        y.setAxisMaximum(20);
        y.setAxisMinimum(-20);
        y.setDrawGridLines(true);
        y.setLabelCount(6);

        y2.setDrawGridLines(false);

        LineData data_y = new LineData();
        XAxis x_1 = y_chart_accelerometer.getXAxis();
        this.y = y_chart_accelerometer.getAxisLeft();
        YAxis y2_1 = y_chart_accelerometer.getAxisRight();

        y_chart_accelerometer.setTouchEnabled(true);
        y_chart_accelerometer.setHighlightPerDragEnabled(true);
        y_chart_accelerometer.setDragEnabled(true);
        y_chart_accelerometer.setScaleEnabled(true);
        y_chart_accelerometer.setDrawGridBackground(false);
        y_chart_accelerometer.setPinchZoom(true);
        y_chart_accelerometer.setScaleYEnabled(false);
        y_chart_accelerometer.setBackgroundColor(Color.BLACK);
        y_chart_accelerometer.getDescription().setEnabled(false);

        Legend l_1 = y_chart_accelerometer.getLegend();
        l_1.setForm(Legend.LegendForm.LINE);
        l_1.setTextColor(Color.WHITE);

        y_chart_accelerometer.setData(data_y);

        x_1.setTextColor(Color.WHITE);
        x_1.setDrawGridLines(true);
        x_1.setAvoidFirstLastClipping(true);
        x_1.setDrawLabels(false);

        y.setTextColor(Color.WHITE);
        y.setAxisMaximum(20);
        y.setAxisMinimum(-20);
        y.setDrawGridLines(true);
        y.setLabelCount(6);

        y2_1.setDrawGridLines(false);

        LineData data_z = new LineData();
        XAxis x_2 = z_chart_accelerometer.getXAxis();
        this.y = z_chart_accelerometer.getAxisLeft();
        YAxis y2_2 = z_chart_accelerometer.getAxisRight();

        z_chart_accelerometer.setTouchEnabled(true);
        z_chart_accelerometer.setHighlightPerDragEnabled(true);
        z_chart_accelerometer.setDragEnabled(true);
        z_chart_accelerometer.setScaleEnabled(true);
        z_chart_accelerometer.setDrawGridBackground(false);
        z_chart_accelerometer.setPinchZoom(true);
        z_chart_accelerometer.setScaleYEnabled(false);
        z_chart_accelerometer.setBackgroundColor(Color.BLACK);
        z_chart_accelerometer.getDescription().setEnabled(false);

        Legend l_2 = z_chart_accelerometer.getLegend();
        l_2.setForm(Legend.LegendForm.LINE);
        l_2.setTextColor(Color.WHITE);

        z_chart_accelerometer.setData(data_z);

        x_2.setTextColor(Color.WHITE);
        x_2.setDrawGridLines(true);
        x_2.setAvoidFirstLastClipping(true);
        x_2.setDrawLabels(false);

        y.setTextColor(Color.WHITE);
        y.setAxisMaximum(20);
        y.setAxisMinimum(-20);
        y.setDrawGridLines(true);
        y.setLabelCount(6);

        y2_2.setDrawGridLines(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (graphTimer != null) {
            returningFromPause = true;
            graphTimer.cancel();
            graphTimer = null;
            if (accelerometerSensor.playingData) {
                accelerometerSensor.finish();
            }
        }
    }

    private void updateGraphs() {
        final Handler handler = new Handler();
        if (graphTimer != null) {
            graphTimer.cancel();
        }
        graphTimer = new Timer();
        graphTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            visualizeData();
                        } catch (NullPointerException e) {
                            /* Pass for another refresh round */
                        }
                    }
                });
            }
        }, 0, updatePeriod);
    }

    private void writeLogToFile(long timestamp, float readingX, float readingY, float readingZ) {
        if (getActivity() != null && accelerometerSensor.isRecording) {
            if (accelerometerSensor.writeHeaderToFile) {
                accelerometerSensor.csvLogger.prepareLogFile();
                accelerometerSensor.csvLogger.writeCSVFile("Timestamp,DateTime,ReadingsX,ReadingsY,ReadingsZ,Latitude,Longitude");
                block = timestamp;
                accelerometerSensor.recordSensorDataBlockID(new SensorDataBlock(timestamp, accelerometerSensor.getSensorName()));
                accelerometerSensor.writeHeaderToFile = !accelerometerSensor.writeHeaderToFile;
            }
            if (accelerometerSensor.addLocation && accelerometerSensor.gpsLogger.isGPSEnabled()) {
                String dateTime = CSVLogger.FILE_NAME_FORMAT.format(new Date(timestamp));
                Location location = accelerometerSensor.gpsLogger.getDeviceLocation();
                accelerometerSensor.csvLogger.writeCSVFile(timestamp + "," + dateTime + ","
                        + readingX + "," + readingY + "," + readingZ + "," + location.getLatitude() + "," + location.getLongitude());
                sensorData = new AccelerometerData(timestamp, block, accelerometerValue_X, accelerometerValue_Y, accelerometerValue_Z, location.getLatitude(), location.getLongitude());
            } else {
                String dateTime = CSVLogger.FILE_NAME_FORMAT.format(new Date(timestamp));
                accelerometerSensor.csvLogger.writeCSVFile(timestamp + "," + dateTime + ","
                        + readingX + "," + readingY + "," + readingZ + ",0.0,0.0");
                sensorData = new AccelerometerData(timestamp, block, accelerometerValue_X, accelerometerValue_Y, accelerometerValue_Z, 0.0, 0.0);
            }
            accelerometerSensor.recordSensorData(sensorData);
        } else {
            accelerometerSensor.writeHeaderToFile = true;
        }
    }

    private void visualizeData() {
        boolean toWrite = false;
        long timeElapsed = (System.currentTimeMillis() - startTime) / 1000;
        if (timeElapsed != previousTimeElapsed_X) {
            toWrite = true;
            previousTimeElapsed_X = timeElapsed;
            entriesX.add(new Entry((float) timeElapsed, accelerometerValue_X));

            LineDataSet dataSet = new LineDataSet(entriesX, getString(R.string.accelerometer));
            LineData data = new LineData(dataSet);
            dataSet.setDrawCircles(false);
            dataSet.setDrawValues(false);
            dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            dataSet.setLineWidth(1);
            dataSet.setColor(colors[0]);

            x_chart_accelerometer.setData(data);
            x_chart_accelerometer.notifyDataSetChanged();
            x_chart_accelerometer.setVisibleXRangeMaximum(3);
            x_chart_accelerometer.moveViewToX(data.getEntryCount());
            x_chart_accelerometer.invalidate();
        }

        timeElapsed = (System.currentTimeMillis() - startTime) / 1000;
        if (timeElapsed != previousTimeElapsed_Y) {
            toWrite = true;
            previousTimeElapsed_Y = timeElapsed;
            entriesY.add(new Entry((float) timeElapsed, accelerometerValue_Y));

            LineDataSet dataSet = new LineDataSet(entriesY, getString(R.string.accelerometer));
            LineData data = new LineData(dataSet);
            dataSet.setDrawCircles(false);
            dataSet.setDrawValues(false);
            dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            dataSet.setLineWidth(1);
            dataSet.setColor(colors[1]);

            y_chart_accelerometer.setData(data);
            y_chart_accelerometer.notifyDataSetChanged();
            y_chart_accelerometer.setVisibleXRangeMaximum(3);
            y_chart_accelerometer.moveViewToX(data.getEntryCount());
            y_chart_accelerometer.invalidate();
        }
        timeElapsed = (System.currentTimeMillis() - startTime) / 1000;
        if (timeElapsed != previousTimeElapsed_Z) {
            toWrite = true;
            previousTimeElapsed_Z = timeElapsed;
            entriesZ.add(new Entry((float) timeElapsed, accelerometerValue_Z));

            LineDataSet dataSet = new LineDataSet(entriesZ, getString(R.string.accelerometer));
            LineData data = new LineData(dataSet);
            dataSet.setDrawCircles(false);
            dataSet.setDrawValues(false);
            dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            dataSet.setLineWidth(1);
            dataSet.setColor(colors[2]);

            z_chart_accelerometer.setData(data);
            z_chart_accelerometer.notifyDataSetChanged();
            z_chart_accelerometer.setVisibleXRangeMaximum(3);
            z_chart_accelerometer.moveViewToX(data.getEntryCount());
            z_chart_accelerometer.invalidate();
        }
        if (toWrite) {
            long curretTime = System.currentTimeMillis();
            writeLogToFile(curretTime, accelerometerValue_X, accelerometerValue_Y, accelerometerValue_Z);
        }
        y.setAxisMaximum(20);
        y.setAxisMinimum(-20);
        y.setLabelCount(5);
    }

    private SensorEventListener accelerometerSensorEventListener = new SensorEventListener() {

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {/**/}

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                accelerometerValue_X = event.values[0];
                StringBuilder builder_x = new StringBuilder();
                builder_x.append(df.format(accelerometerValue_X));
                builder_x.append(" ");
                builder_x.append(getResources().getString(R.string.meters_per_sec_text));
                x_accel_value.setText(Html.fromHtml(builder_x.toString()));
                if (accelerometerValue_X > currentMaxX) {
                    builder_x.insert(0, getResources().getString(R.string.text_max));
                    builder_x.insert(3, " ");
                    x_accel_max.setText(Html.fromHtml(builder_x.toString()));
                    currentMaxX = accelerometerValue_X;
                } else if (accelerometerValue_X < currentMinX) {
                    builder_x.insert(0, getResources().getString(R.string.text_min));
                    builder_x.insert(3, " ");
                    x_accel_min.setText(Html.fromHtml(builder_x.toString()));
                    currentMinX = accelerometerValue_X;
                }
                accelerometerValue_Y = event.values[1];
                StringBuilder builder_y = new StringBuilder();
                builder_y.append(df.format(accelerometerValue_Y));
                builder_y.append(" ");
                builder_y.append(getResources().getString(R.string.meters_per_sec_text));
                y_accel_value.setText(Html.fromHtml(builder_y.toString()));
                if (accelerometerValue_Y > currentMaxY) {
                    builder_y.insert(0, getResources().getString(R.string.text_max));
                    builder_y.insert(3, " ");
                    y_accel_max_text.setText(Html.fromHtml(builder_y.toString()));
                    currentMaxY = accelerometerValue_Y;
                } else if (accelerometerValue_Y < currentMinY) {
                    builder_y.insert(0, getResources().getString(R.string.text_min));
                    builder_y.insert(3, " ");
                    y_accel_min_text.setText(Html.fromHtml(builder_y.toString()));
                    currentMinY = accelerometerValue_Y;
                }
                accelerometerValue_Z = event.values[2];
                StringBuilder builder_z = new StringBuilder();
                builder_z.append(df.format(accelerometerValue_Z));
                builder_z.append(" ");
                builder_z.append(getResources().getString(R.string.meters_per_sec_text));
                z_accel_value.setText(Html.fromHtml(builder_z.toString()));

                if (accelerometerValue_Z > currentMaxZ) {
                    builder_z.insert(0, getResources().getString(R.string.text_max));
                    builder_z.insert(3, " ");
                    z_accel_max_text.setText(Html.fromHtml(builder_z.toString()));
                    currentMaxZ = accelerometerValue_Z;
                } else if (accelerometerValue_Z < currentMinZ) {
                    builder_z.insert(0, getResources().getString(R.string.text_min));
                    builder_z.insert(3, " ");
                    z_accel_min_text.setText(Html.fromHtml(builder_z.toString()));
                    currentMinZ = accelerometerValue_Z;
                }
            }
        }
    };

    private void resetInstrumentData() {
        accelerometerValue_X = 0;
        accelerometerValue_Y = 0;
        accelerometerValue_Z = 0;
        count = 0;
        currentMinX = Integer.MAX_VALUE;
        currentMaxX = Integer.MIN_VALUE;
        currentMinY = Integer.MAX_VALUE;
        currentMaxY = Integer.MIN_VALUE;
        currentMinZ = Integer.MAX_VALUE;
        currentMaxZ = Integer.MIN_VALUE;
        sum = 0;
        sensor = null;
        startTime = System.currentTimeMillis();
        z_axis_image.setImageResource(R.drawable.phone_z_axis);
        y_axis.setImageResource(R.drawable.phone_y_axis);
        x_axis.setImageResource(R.drawable.phone_x_axis);
        entriesX.clear();
        entriesZ.clear();
        entriesY.clear();
    }

    private void initiateAccelerometerSensor(int type) {
        ACCELEROMETER_SENSOR s = ACCELEROMETER_SENSOR.values()[type];
        resetInstrumentData();
        ScienceLab scienceLab;
        switch (s) {
            case INBUILT_SENSOR:
                sensorManager = (SensorManager) getContext().getSystemService(SENSOR_SERVICE);
                sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                if (sensor == null) {
                    Toast.makeText(getContext(), getResources().getString(R.string.no_accelerometer_sensor), Toast.LENGTH_LONG).show();
                } else {
                    float max = sensor.getMaximumRange();
                    sensorManager.registerListener(accelerometerSensorEventListener,
                            sensor, SensorManager.SENSOR_DELAY_FASTEST);
                }
                break;
            case BH1750_SENSOR:
                scienceLab = ScienceLabCommon.scienceLab;
                if (scienceLab.isConnected()) {
                    ArrayList<Integer> data;
                    try {
                        I2C i2c = scienceLab.i2c;
                        data = i2c.scan(null);
                        if (data.contains(0x23)) {
                            BH1750 sensorBH1750 = new BH1750(i2c);
                            sensorBH1750.setRange(String.valueOf(gain));
                            sensorType = 0;
                        } else {
                            Toast.makeText(getContext(), getResources().getText(R.string.sensor_not_connected_tls), Toast.LENGTH_SHORT).show();
                            sensorType = 0;
                        }
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getContext(), getResources().getText(R.string.device_not_found), Toast.LENGTH_SHORT).show();
                    sensorType = 0;
                }
                break;
            case TSL2561_SENSOR:
                scienceLab = ScienceLabCommon.scienceLab;
                if (scienceLab.isConnected()) {
                    try {
                        I2C i2c = scienceLab.i2c;
                        ArrayList<Integer> data;
                        data = i2c.scan(null);
                        if (data.contains(0x39)) {
                            TSL2561 sensorTSL2561 = new TSL2561(i2c, scienceLab);
                            sensorTSL2561.setGain(String.valueOf(gain));
                            sensorType = 2;
                        } else {
                            Toast.makeText(getContext(), getResources().getText(R.string.sensor_not_connected_tls), Toast.LENGTH_SHORT).show();
                            sensorType = 0;
                        }
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getContext(), getResources().getText(R.string.device_not_found), Toast.LENGTH_SHORT).show();
                    sensorType = 0;
                }
                break;
            default:
                break;
        }
    }
}