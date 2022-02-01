package io.pslab.fragment;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import androidx.annotation.NonNull;

import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.anastr.speedviewlib.PointerSpeedometer;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.pslab.DataFormatter;
import io.pslab.R;
import io.pslab.activity.DustSensorActivity;
import io.pslab.communication.ScienceLab;
import io.pslab.interfaces.OperationCallback;
import io.pslab.models.DustSensorData;
import io.pslab.models.GasSensorData;
import io.pslab.models.SensorDataBlock;
import io.pslab.others.CSVDataLine;
import io.pslab.others.CSVLogger;
import io.pslab.others.CustomSnackBar;
import io.pslab.others.ScienceLabCommon;

import static io.pslab.others.CSVLogger.CSV_DIRECTORY;

public class DustSensorDataFragment extends Fragment implements OperationCallback {

    private static final CSVDataLine CSV_HEADER =
            new CSVDataLine()
                    .add("Timestamp")
                    .add("DateTime")
                    .add("ppmValue")
                    .add("Latitude")
                    .add("Longitude");

    @BindView(R.id.dust_sensor_value)
    TextView dustValue;
    @BindView(R.id.dust_sensor_status)
    TextView dustStatus;
    @BindView(R.id.label_dust_sensor)
    TextView sensorLabel;
    @BindView(R.id.chart_dust_sensor)
    LineChart mChart;
    @BindView(R.id.dust_sensor)
    PointerSpeedometer dustSensorMeter;

    // TODO: Support multiple kinds of dust sensors
    private static int sensorType = 0;
    private static double highLimit = 4.0;
    private static int updatePeriod = 1000;

    private DustSensorActivity dustSensorActivity;
    private View rootView;
    private Unbinder unbinder;
    private ScienceLab scienceLab;
    private YAxis y;
    private Timer graphTimer;
    private ArrayList<Entry> entries;
    private long startTime;
    private long timeElapsed;
    private long previousTimeElapsed = (System.currentTimeMillis() - startTime) / updatePeriod;
    private long block;
    private GasSensorData sensorData;
    private boolean returningFromPause = false;
    private int turns = 0;
    private ArrayList<DustSensorData> recordedDustSensorArray;

    public static DustSensorDataFragment newInstance() {
        return new DustSensorDataFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startTime = System.currentTimeMillis();
        dustSensorActivity = (DustSensorActivity) getActivity();
    }

    public static void setParameters(double highLimit, int updatePeriod, String type) {
        DustSensorDataFragment.highLimit = highLimit;
        DustSensorDataFragment.updatePeriod = updatePeriod;
        DustSensorDataFragment.sensorType = Integer.valueOf(type);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_dust_sensor, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        scienceLab = ScienceLabCommon.scienceLab;
        entries = new ArrayList<>();
        setupInstruments();
        if (!scienceLab.isConnected())
            CustomSnackBar.showSnackBar(getActivity().findViewById(android.R.id.content), getString(R.string.not_connected), null, null, Snackbar.LENGTH_SHORT);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (dustSensorActivity.playingData) {
            sensorLabel.setText(getResources().getString(R.string.baro_meter));
            recordedDustSensorArray = new ArrayList<>();
            resetInstrumentData();
            playRecordedData();
        } else if (dustSensorActivity.viewingData) {
            sensorLabel.setText(getResources().getString(R.string.baro_meter));
            recordedDustSensorArray = new ArrayList<>();
            resetInstrumentData();
            plotAllRecordedData();
        } else if (!dustSensorActivity.isRecording) {
            updateGraphs();
            entries.clear();
            mChart.clear();
            mChart.invalidate();
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
        unbinder.unbind();
    }

    private void plotAllRecordedData() {
        recordedDustSensorArray.addAll(dustSensorActivity.recordedDustSensorData);
        if (recordedDustSensorArray.size() != 0) {
            for (DustSensorData d : recordedDustSensorArray) {
                Entry entry = new Entry((float) (d.getTime() - d.getBlock()) / 1000, d.getPpmValue());
                entries.add(entry);
                dustSensorMeter.setWithTremble(false);
                float ppm = d.getPpmValue();
                dustSensorMeter.setSpeedAt(ppm);
                dustSensorMeter.setPointerColor(ppm > highLimit ? Color.WHITE : Color.RED);
                dustValue.setText(String.format(Locale.getDefault(), "%.2f", ppm));
                String status = ppm > highLimit ? "Good" : "Bad";
                dustStatus.setText(status);
            }
            y.setAxisMaximum(5);
            y.setAxisMinimum(0);
            y.setLabelCount(10);

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
        recordedDustSensorArray.addAll(dustSensorActivity.recordedDustSensorData);
        try {
            if (recordedDustSensorArray.size() > 1) {
                DustSensorData i = recordedDustSensorArray.get(1);
                long timeGap = i.getTime() - i.getBlock();
                processRecordedData(timeGap);
            } else {
                processRecordedData(0);
            }
        } catch (IllegalArgumentException e) {
            CustomSnackBar.showSnackBar(getActivity().findViewById(android.R.id.content),
                    getString(R.string.no_data_fetched), null, null, Snackbar.LENGTH_SHORT);
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
                        if (dustSensorActivity.playingData) {
                            try {
                                DustSensorData d = recordedDustSensorArray.get(turns);
                                turns++;
                                float ppm = d.getPpmValue();
                                dustSensorMeter.setPointerColor(ppm > highLimit ? Color.WHITE : Color.RED);
                                dustValue.setText(String.format(Locale.getDefault(), "%.2f", ppm));
                                String status = ppm > highLimit ? "Good" : "Bad";
                                dustStatus.setText(status);

                                y.setAxisMaximum(5);
                                y.setAxisMinimum(0);
                                y.setLabelCount(10);
                                dustSensorMeter.setWithTremble(false);
                                dustSensorMeter.setSpeedAt(ppm);

                                Entry entry = new Entry((float) (d.getTime() - d.getBlock()) / 1000, d.getPpmValue());
                                entries.add(entry);

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
                                dustSensorActivity.playingData = false;
                                dustSensorActivity.startedPlay = false;
                                dustSensorActivity.invalidateOptionsMenu();
                            }
                        }
                    }
                });
            }
        }, 0, timeGap);
    }

    @Override
    public void playData() {
        resetInstrumentData();
        dustSensorActivity.startedPlay = true;
        try {
            if (recordedDustSensorArray.size() > 1) {
                DustSensorData i = recordedDustSensorArray.get(1);
                long timeGap = i.getTime() - i.getBlock();
                processRecordedData(timeGap);
            } else {
                processRecordedData(0);
            }
        } catch (IllegalArgumentException e) {
            CustomSnackBar.showSnackBar(getActivity().findViewById(android.R.id.content),
                    getString(R.string.no_data_fetched), null, null, Snackbar.LENGTH_SHORT);
        }
    }

    @Override
    public void stopData() {
        if (graphTimer != null) {
            graphTimer.cancel();
            graphTimer = null;
        }
        recordedDustSensorArray.clear();
        entries.clear();
        plotAllRecordedData();
        dustSensorActivity.startedPlay = false;
        dustSensorActivity.playingData = false;
        turns = 0;
        dustSensorActivity.invalidateOptionsMenu();
    }

    @Override
    public void saveGraph() {
        dustSensorActivity.csvLogger.prepareLogFile();
        dustSensorActivity.csvLogger.writeMetaData(getResources().getString(R.string.gas_sensor));
        dustSensorActivity.csvLogger.writeCSVFile(CSV_HEADER);
        for (DustSensorData dustSensorData : dustSensorActivity.recordedDustSensorData) {
            dustSensorActivity.csvLogger.writeCSVFile(
                    new CSVDataLine()
                            .add(dustSensorData.getTime())
                            .add(CSVLogger.FILE_NAME_FORMAT.format(new Date(dustSensorData.getTime())))
                            .add(dustSensorData.getPpmValue())
                            .add(dustSensorData.getLat())
                            .add(dustSensorData.getLon())
            );
        }
        View view = rootView.findViewById(R.id.gas_sensor_linearlayout);
        view.setDrawingCacheEnabled(true);
        Bitmap b = view.getDrawingCache();
        try {
            b.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(Environment.getExternalStorageDirectory().getAbsolutePath() +
                    File.separator + CSV_DIRECTORY + File.separator + dustSensorActivity.getSensorName() +
                    File.separator + CSVLogger.FILE_NAME_FORMAT.format(new Date()) + "_graph.jpg"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (graphTimer != null) {
            returningFromPause = true;
            graphTimer.cancel();
            graphTimer = null;
            if (dustSensorActivity.playingData) {
                dustSensorActivity.finish();
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
                handler.post(() -> {
                    try {
                        visualizeData();
                    } catch (NullPointerException e) {
                    }
                });
            }
        }, 0, 1000);
    }

    private void writeLogToFile(long timestamp, float ppmValue) {
        if (getActivity() != null && dustSensorActivity.isRecording) {
            if (dustSensorActivity.writeHeaderToFile) {
                dustSensorActivity.csvLogger.prepareLogFile();
                dustSensorActivity.csvLogger.writeCSVFile(CSV_HEADER);
                block = timestamp;
                dustSensorActivity.recordSensorDataBlockID(new SensorDataBlock(timestamp, dustSensorActivity.getSensorName()));
                dustSensorActivity.writeHeaderToFile = !dustSensorActivity.writeHeaderToFile;
            }
            if (dustSensorActivity.addLocation && dustSensorActivity.gpsLogger.isGPSEnabled()) {
                Location location = dustSensorActivity.gpsLogger.getDeviceLocation();
                dustSensorActivity.csvLogger.writeCSVFile(
                        new CSVDataLine()
                                .add(timestamp)
                                .add(CSVLogger.FILE_NAME_FORMAT.format(new Date(timestamp)))
                                .add(ppmValue)
                                .add(location.getLatitude())
                                .add(location.getLongitude())
                );
                sensorData = new GasSensorData(timestamp, block, ppmValue, location.getLatitude(), location.getLongitude());
            } else {
                dustSensorActivity.csvLogger.writeCSVFile(
                        new CSVDataLine()
                                .add(timestamp)
                                .add(CSVLogger.FILE_NAME_FORMAT.format(new Date(timestamp)))
                                .add(ppmValue)
                                .add(0.0)
                                .add(0.0)
                );
                sensorData = new GasSensorData(timestamp, block, ppmValue, 0.0, 0.0);
            }
            dustSensorActivity.recordSensorData(sensorData);
        } else {
            dustSensorActivity.writeHeaderToFile = true;
        }
    }

    private void visualizeData() {
        if (scienceLab.isConnected()) {
            double ppm = scienceLab.getVoltage("CH1", 1);
            dustSensorMeter.setPointerColor(ppm > highLimit ? Color.WHITE : Color.RED);
            dustValue.setText(String.format(Locale.getDefault(), "%.2f", ppm));
            String status = ppm > highLimit ? "Good" : "Bad";
            dustStatus.setText(status);
            dustSensorMeter.setWithTremble(false);
            dustSensorMeter.setSpeedAt((float) ppm);
            timeElapsed = ((System.currentTimeMillis() - startTime) / updatePeriod);
            if (timeElapsed != previousTimeElapsed) {
                previousTimeElapsed = timeElapsed;
                Entry entry = new Entry((float) timeElapsed, (float) ppm);
                entries.add(entry);
                writeLogToFile(System.currentTimeMillis(), (float) ppm);
                LineDataSet dataSet = new LineDataSet(entries, getString(R.string.gas_sensor_unit));
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

    private void setupInstruments() {
        dustSensorMeter.setMaxSpeed(5);
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
        y.setAxisMaximum(5);
        y.setAxisMinimum(0);
        y.setDrawGridLines(true);
        y.setLabelCount(10);

        y2.setDrawGridLines(false);
        y2.setMaxWidth(0);
    }

    private void resetInstrumentData() {
        startTime = System.currentTimeMillis();
        dustValue.setText(DataFormatter.formatDouble(0, DataFormatter.LOW_PRECISION_FORMAT));
        dustStatus.setText(getString(R.string.unknown));
        dustSensorMeter.setSpeedAt(0);
        dustSensorMeter.setWithTremble(false);
        entries.clear();
    }

}
