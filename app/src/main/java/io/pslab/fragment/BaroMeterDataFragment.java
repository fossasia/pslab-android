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
import android.support.v7.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import io.pslab.DataFormatter;

import com.github.anastr.speedviewlib.PointerSpeedometer;
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
import io.pslab.activity.BarometerActivity;
import io.pslab.communication.ScienceLab;
import io.pslab.communication.peripherals.I2C;
import io.pslab.communication.sensors.BMP180;
import io.pslab.models.BaroData;
import io.pslab.models.PSLabSensor;
import io.pslab.models.SensorDataBlock;
import io.pslab.others.CSVLogger;
import io.pslab.others.ScienceLabCommon;

import static android.content.Context.SENSOR_SERVICE;
import static io.pslab.others.CSVLogger.CSV_DIRECTORY;

/**
 * Created by Padmal on 12/13/18.
 */

public class BaroMeterDataFragment extends Fragment {

    private static int sensorType = 0;
    private static float highLimit = 1.2f;
    private static int updatePeriod = 1000;
    private long timeElapsed;
    private int count = 0, turns = 0;
    private float sum = 0;
    private boolean returningFromPause = false;

    private float baroValue = -1;

    private enum BARO_SENSOR {INBUILT_SENSOR, BMP180_SENSOR}

    @BindView(R.id.baro_max)
    TextView statMax;
    @BindView(R.id.baro_min)
    TextView statMin;
    @BindView(R.id.baro_avg)
    TextView statMean;
    @BindView(R.id.label_baro_sensor)
    TextView sensorLabel;
    @BindView(R.id.chart_baro_meter)
    LineChart mChart;
    @BindView(R.id.baro_meter)
    PointerSpeedometer baroMeter;

    private Timer graphTimer;
    private SensorManager sensorManager;
    private Sensor sensor;
    private long startTime, block;
    private ArrayList<Entry> entries;
    private ArrayList<BaroData> recordedBaroArray;
    private BaroData sensorData;
    private float currentMin = 2;
    private float currentMax = 0.5f;
    private YAxis y;
    private Unbinder unbinder;
    private long previousTimeElapsed = (System.currentTimeMillis() - startTime) / updatePeriod;
    private BarometerActivity baroSensor;
    private View rootView;

    public static BaroMeterDataFragment newInstance() {
        return new BaroMeterDataFragment();
    }

    public static void setParameters(float highLimit, int updatePeriod, String type) {
        BaroMeterDataFragment.highLimit = highLimit;
        BaroMeterDataFragment.updatePeriod = updatePeriod;
        BaroMeterDataFragment.sensorType = Integer.valueOf(type);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startTime = System.currentTimeMillis();
        entries = new ArrayList<>();
        baroSensor = (BarometerActivity) getActivity();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_barometer_data, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        setupInstruments();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (baroSensor.playingData) {
            sensorLabel.setText(getResources().getString(R.string.baro_meter));
            recordedBaroArray = new ArrayList<>();
            resetInstrumentData();
            playRecordedData();
        } else if (baroSensor.viewingData) {
            sensorLabel.setText(getResources().getString(R.string.baro_meter));
            recordedBaroArray = new ArrayList<>();
            resetInstrumentData();
            plotAllRecordedData();
        } else if (!baroSensor.isRecording) {
            updateGraphs();
            sum = 0;
            count = 0;
            currentMin = 2;
            currentMax = 0.5f;
            entries.clear();
            mChart.clear();
            mChart.invalidate();
            initiateBaroSensor(sensorType);
        } else if (returningFromPause) {
            updateGraphs();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (graphTimer != null) {
            graphTimer.cancel();
        }
        if (sensorManager != null) {
            sensorManager.unregisterListener(baroSensorEventListener);
        }
        unbinder.unbind();
    }

    private void plotAllRecordedData() {
        recordedBaroArray.addAll(baroSensor.recordedBaroData);
        if (recordedBaroArray.size() != 0) {
            for (BaroData d : recordedBaroArray) {
                if (currentMax < d.getBaro()) {
                    currentMax = d.getBaro();
                }
                if (currentMin > d.getBaro()) {
                    currentMin = d.getBaro();
                }
                Entry entry = new Entry((float) (d.getTime() - d.getBlock()) / 1000, d.getBaro());
                entries.add(entry);
                baroMeter.setWithTremble(false);
                baroMeter.setSpeedAt(d.getBaro());
                sum += entry.getY();
            }
            y.setAxisMaximum(currentMax);
            y.setAxisMinimum(currentMin);
            y.setLabelCount(10);
            statMax.setText(String.format(Locale.getDefault(), PSLabSensor.BAROMETER_DATA_FORMAT, currentMax));
            statMin.setText(String.format(Locale.getDefault(), PSLabSensor.BAROMETER_DATA_FORMAT, currentMin));
            statMean.setText(String.format(Locale.getDefault(), PSLabSensor.BAROMETER_DATA_FORMAT, (sum / recordedBaroArray.size())));

            LineDataSet dataSet = new LineDataSet(entries, getString(R.string.baro_unit));
            dataSet.setDrawCircles(false);
            dataSet.setDrawValues(false);
            dataSet.setLineWidth(2);
            LineData data = new LineData(dataSet);

            mChart.setData(data);
            mChart.notifyDataSetChanged();
            mChart.setVisibleXRangeMaximum(80);
            mChart.moveViewToX(data.getEntryCount());
            mChart.invalidate();
        }
    }

    private void playRecordedData() {
        recordedBaroArray.addAll(baroSensor.recordedBaroData);
        try {
            if (recordedBaroArray.size() > 1) {
                BaroData i = recordedBaroArray.get(1);
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
                        if (baroSensor.playingData) {
                            try {
                                BaroData d = recordedBaroArray.get(turns);
                                turns++;
                                if (currentMax < d.getBaro()) {
                                    currentMax = d.getBaro();
                                    statMax.setText(String.format(Locale.getDefault(), PSLabSensor.BAROMETER_DATA_FORMAT, d.getBaro()));
                                }
                                if (currentMin > d.getBaro()) {
                                    currentMin = d.getBaro();
                                    statMin.setText(String.format(Locale.getDefault(), PSLabSensor.BAROMETER_DATA_FORMAT, d.getBaro()));
                                }
                                y.setAxisMaximum(currentMax);
                                y.setAxisMinimum(currentMin);
                                y.setLabelCount(10);
                                baroMeter.setWithTremble(false);
                                baroMeter.setSpeedAt(d.getBaro());

                                Entry entry = new Entry((float) (d.getTime() - d.getBlock()) / 1000, d.getBaro());
                                entries.add(entry);
                                count++;
                                sum += entry.getY();
                                statMean.setText(DataFormatter.formatDouble((sum / count), PSLabSensor.BAROMETER_DATA_FORMAT));

                                LineDataSet dataSet = new LineDataSet(entries, getString(R.string.baro_unit));
                                dataSet.setDrawCircles(false);
                                dataSet.setDrawValues(false);
                                dataSet.setLineWidth(2);
                                LineData data = new LineData(dataSet);

                                mChart.setData(data);
                                mChart.notifyDataSetChanged();
                                mChart.setVisibleXRangeMaximum(80);
                                mChart.moveViewToX(data.getEntryCount());
                                mChart.invalidate();
                            } catch (IndexOutOfBoundsException e) {
                                graphTimer.cancel();
                                graphTimer = null;
                                turns = 0;
                                baroSensor.playingData = false;
                                baroSensor.startedPlay = false;
                                baroSensor.invalidateOptionsMenu();
                            }
                        }
                    }
                });
            }
        }, 0, timeGap);
    }

    public void playData() {
        resetInstrumentData();
        baroSensor.startedPlay = true;
        try {
            if (recordedBaroArray.size() > 1) {
                BaroData i = recordedBaroArray.get(1);
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

    public void stopData() {
        if (graphTimer != null) {
            graphTimer.cancel();
            graphTimer = null;
        }
        recordedBaroArray.clear();
        entries.clear();
        plotAllRecordedData();
        baroSensor.startedPlay = false;
        baroSensor.playingData = false;
        turns = 0;
        baroSensor.invalidateOptionsMenu();
    }

    public void saveGraph() {
        String fileName = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(baroSensor.recordedBaroData.get(0).getTime());
        File csvFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                File.separator + CSV_DIRECTORY + File.separator + baroSensor.getSensorName() +
                File.separator + fileName + ".csv");
        if (!csvFile.exists()) {
            try {
                csvFile.createNewFile();
                PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(csvFile, true)));
                out.write( "Timestamp,DateTime,Readings,Latitude,Longitude" + "\n");
                for (BaroData baroData : baroSensor.recordedBaroData) {
                    out.write( baroData.getTime() + ","
                            + CSVLogger.FILE_NAME_FORMAT.format(new Date(baroData.getTime())) + ","
                            + baroData.getBaro() + ","
                            + baroData.getLat() + ","
                            + baroData.getLon() + "," + "\n");
                }
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        View view = rootView.findViewById(R.id.barometer_linearlayout);
        view.setDrawingCacheEnabled(true);
        Bitmap b = view.getDrawingCache();
        try {
            b.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(Environment.getExternalStorageDirectory().getAbsolutePath() +
                    File.separator + CSV_DIRECTORY + File.separator + baroSensor.getSensorName() +
                    File.separator + CSVLogger.FILE_NAME_FORMAT.format(new Date()) + "_graph.jpg" ));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    private void setupInstruments() {
        baroMeter.setMaxSpeed(PreferenceManager.getDefaultSharedPreferences(getActivity()).getFloat(baroSensor.BAROMETER_LIMIT, 2));
        XAxis x = mChart.getXAxis();
        this.y = mChart.getAxisLeft();
        YAxis y2 = mChart.getAxisRight();

        mChart.setTouchEnabled(true);
        mChart.setHighlightPerDragEnabled(true);
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);
        mChart.setPinchZoom(true);
        mChart.setScaleYEnabled(true);
        mChart.setBackgroundColor(Color.BLACK);
        mChart.getDescription().setEnabled(false);

        LineData data = new LineData();
        mChart.setData(data);

        Legend l = mChart.getLegend();
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.WHITE);

        x.setTextColor(Color.WHITE);
        x.setDrawGridLines(true);
        x.setAvoidFirstLastClipping(true);

        y.setTextColor(Color.WHITE);
        y.setAxisMaximum(currentMax);
        y.setAxisMinimum(currentMin);
        y.setDrawGridLines(true);
        y.setLabelCount(10);

        y2.setDrawGridLines(false);
        y2.setMaxWidth(0);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (graphTimer != null) {
            returningFromPause = true;
            graphTimer.cancel();
            graphTimer = null;
            if (baroSensor.playingData) {
                baroSensor.finish();
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

    private void writeLogToFile(long timestamp, float sensorReading) {
        if (getActivity() != null && baroSensor.isRecording) {
            if (baroSensor.writeHeaderToFile) {
                baroSensor.csvLogger.prepareLogFile();
                baroSensor.csvLogger.writeCSVFile("Timestamp,DateTime,Readings,Latitude,Longitude");
                block = timestamp;
                baroSensor.recordSensorDataBlockID(new SensorDataBlock(timestamp, baroSensor.getSensorName()));
                baroSensor.writeHeaderToFile = !baroSensor.writeHeaderToFile;
            }
            if (baroSensor.addLocation && baroSensor.gpsLogger.isGPSEnabled()) {
                String dateTime = CSVLogger.FILE_NAME_FORMAT.format(new Date(timestamp));
                Location location = baroSensor.gpsLogger.getDeviceLocation();
                baroSensor.csvLogger.writeCSVFile(timestamp + "," + dateTime + ","
                        + sensorReading + "," + location.getLatitude() + "," + location.getLongitude());
                sensorData = new BaroData(timestamp, block, baroValue, location.getLatitude(), location.getLongitude());
            } else {
                String dateTime = CSVLogger.FILE_NAME_FORMAT.format(new Date(timestamp));
                baroSensor.csvLogger.writeCSVFile(timestamp + "," + dateTime + ","
                        + sensorReading + ",0.0,0.0");
                sensorData = new BaroData(timestamp, block, baroValue, 0.0, 0.0);
            }
            baroSensor.recordSensorData(sensorData);
        } else {
            baroSensor.writeHeaderToFile = true;
        }
    }

    private void visualizeData() {
        if (currentMax < baroValue) {
            currentMax = baroValue;
            statMax.setText(String.format(Locale.getDefault(), PSLabSensor.BAROMETER_DATA_FORMAT, baroValue));
        }
        if (currentMin > baroValue) {
            currentMin = baroValue;
            statMin.setText(String.format(Locale.getDefault(), PSLabSensor.BAROMETER_DATA_FORMAT, baroValue));
        }
        y.setAxisMaximum(currentMax);
        y.setAxisMinimum(currentMin);
        y.setLabelCount(10);
        if (baroValue >= 0) {
            baroMeter.setWithTremble(false);
            baroMeter.setSpeedAt(baroValue);
            if (baroValue > highLimit)
                baroMeter.setPointerColor(Color.RED);
            else
                baroMeter.setPointerColor(Color.WHITE);

            timeElapsed = ((System.currentTimeMillis() - startTime) / updatePeriod);
            if (timeElapsed != previousTimeElapsed) {
                previousTimeElapsed = timeElapsed;
                Entry entry = new Entry((float) timeElapsed, baroValue);
                Long currentTime = System.currentTimeMillis();
                writeLogToFile(currentTime, baroValue);
                entries.add(entry);

                count++;
                sum += entry.getY();
                statMean.setText(DataFormatter.formatDouble((sum / count), PSLabSensor.BAROMETER_DATA_FORMAT));
                LineDataSet dataSet = new LineDataSet(entries, getString(R.string.baro_unit));
                dataSet.setDrawCircles(false);
                dataSet.setDrawValues(false);
                dataSet.setLineWidth(2);
                LineData data = new LineData(dataSet);

                mChart.setData(data);
                mChart.notifyDataSetChanged();
                mChart.setVisibleXRangeMaximum(80);
                mChart.moveViewToX(data.getEntryCount());
                mChart.invalidate();
            }
        }
    }

    private SensorEventListener baroSensorEventListener = new SensorEventListener() {

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {/**/}

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_PRESSURE) {
                baroValue = Float.valueOf(String.format(Locale.US, PSLabSensor.BAROMETER_DATA_FORMAT, event.values[0] / 1000));
            }
        }
    };

    private void resetInstrumentData() {
        baroValue = 0;
        count = 0;
        currentMin = 2;
        currentMax = 0.5f;
        sum = 0;
        sensor = null;
        if (sensorManager != null) {
            sensorManager.unregisterListener(baroSensorEventListener);
        }
        startTime = System.currentTimeMillis();
        statMax.setText(DataFormatter.formatDouble(0, DataFormatter.LOW_PRECISION_FORMAT));
        statMin.setText(DataFormatter.formatDouble(0, DataFormatter.LOW_PRECISION_FORMAT));
        statMean.setText(DataFormatter.formatDouble(0, DataFormatter.LOW_PRECISION_FORMAT));
        baroMeter.setSpeedAt(0);
        baroMeter.setWithTremble(false);
        entries.clear();
    }

    private void initiateBaroSensor(int type) {
        BaroMeterDataFragment.BARO_SENSOR s = BaroMeterDataFragment.BARO_SENSOR.values()[type];
        resetInstrumentData();
        ScienceLab scienceLab;
        switch (s) {
            case INBUILT_SENSOR:
                sensorLabel.setText(getResources().getStringArray(R.array.baro_sensors)[0]);
                sensorManager = (SensorManager) getContext().getSystemService(SENSOR_SERVICE);
                sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
                if (sensor == null) {
                    Toast.makeText(getContext(), getResources().getString(R.string.no_baro_sensor), Toast.LENGTH_LONG).show();
                } else {
                    float max = sensor.getMaximumRange() / 1000;
                    PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putFloat(baroSensor.BAROMETER_LIMIT, max).apply();
                    baroMeter.setMaxSpeed(max);
                    sensorManager.registerListener(baroSensorEventListener,
                            sensor, SensorManager.SENSOR_DELAY_FASTEST);
                }
                break;
            case BMP180_SENSOR:
                sensorLabel.setText(getResources().getStringArray(R.array.baro_sensors)[1]);
                scienceLab = ScienceLabCommon.scienceLab;
                if (scienceLab.isConnected()) {
                    ArrayList<Integer> data;
                    try {
                        I2C i2c = scienceLab.i2c;
                        data = i2c.scan(null);
                        if (data.contains(0x23)) {
                            BMP180 sensorBMP180 = new BMP180(i2c, scienceLab);
                            sensorBMP180.setOversampling(10);
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
            default:
                break;
        }
    }
}