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
import io.pslab.activity.GasSensorActivity;
import io.pslab.communication.ScienceLab;
import io.pslab.interfaces.OperationCallback;
import io.pslab.models.GasSensorData;
import io.pslab.models.SensorDataBlock;
import io.pslab.others.CSVDataLine;
import io.pslab.others.CSVLogger;
import io.pslab.others.CustomSnackBar;
import io.pslab.others.ScienceLabCommon;

import static io.pslab.others.CSVLogger.CSV_DIRECTORY;

public class GasSensorDataFragment extends Fragment implements OperationCallback {

    private static final CSVDataLine CSV_HEADER = new CSVDataLine()
            .add("Timestamp")
            .add("DateTime")
            .add("ppmValue")
            .add("Latitude")
            .add("Longitude");
    @BindView(R.id.gas_sensor_value)
    TextView gasValue;
    @BindView(R.id.label_gas_sensor)
    TextView sensorLabel;
    @BindView(R.id.chart_gas_sensor)
    LineChart mChart;
    @BindView(R.id.gas_sensor)
    PointerSpeedometer gasSensorMeter;
    private GasSensorActivity gasSensorActivity;
    private View rootView;
    private Unbinder unbinder;
    private ScienceLab scienceLab;
    private YAxis y;
    private Timer graphTimer;
    private ArrayList<Entry> entries;
    private long updatePeriod = 1000;
    private long startTime;
    private long timeElapsed;
    private long previousTimeElapsed = (System.currentTimeMillis() - startTime) / updatePeriod;
    private long block;
    private GasSensorData sensorData;
    private boolean returningFromPause = false;
    private int turns = 0;
    private ArrayList<GasSensorData> recordedGasSensorArray;


    public static GasSensorDataFragment newInstance() {
        return new GasSensorDataFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startTime = System.currentTimeMillis();
        gasSensorActivity = (GasSensorActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_gas_sensor, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        scienceLab = ScienceLabCommon.scienceLab;
        if (!scienceLab.isConnected()) {
            CustomSnackBar.showSnackBar(getActivity().findViewById(android.R.id.content),
                    getString(R.string.not_connected), null, null, Snackbar.LENGTH_SHORT);
        }
        entries = new ArrayList<>();
        setupInstruments();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (gasSensorActivity.playingData) {
            sensorLabel.setText(getResources().getString(R.string.baro_meter));
            recordedGasSensorArray = new ArrayList<>();
            resetInstrumentData();
            playRecordedData();
        } else if (gasSensorActivity.viewingData) {
            sensorLabel.setText(getResources().getString(R.string.baro_meter));
            recordedGasSensorArray = new ArrayList<>();
            resetInstrumentData();
            plotAllRecordedData();
        } else if (!gasSensorActivity.isRecording) {
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
        recordedGasSensorArray.addAll(gasSensorActivity.recordedGasSensorData);
        if (recordedGasSensorArray.size() != 0) {
            for (GasSensorData d : recordedGasSensorArray) {
                Entry entry = new Entry((float) (d.getTime() - d.getBlock()) / 1000, d.getPpmValue());
                entries.add(entry);
                gasSensorMeter.setWithTremble(false);
                gasSensorMeter.setSpeedAt(d.getPpmValue());
                gasValue.setText(String.format(Locale.getDefault(), "%.2f", d.getPpmValue()));
            }
            y.setAxisMaximum(1024);
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
        recordedGasSensorArray.addAll(gasSensorActivity.recordedGasSensorData);
        try {
            if (recordedGasSensorArray.size() > 1) {
                GasSensorData i = recordedGasSensorArray.get(1);
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
                        if (gasSensorActivity.playingData) {
                            try {
                                GasSensorData d = recordedGasSensorArray.get(turns);
                                turns++;
                                gasValue.setText(String.format(Locale.getDefault(), "%.2f", d.getPpmValue()));
                                y.setAxisMaximum(1024);
                                y.setAxisMinimum(0);
                                y.setLabelCount(10);
                                gasSensorMeter.setWithTremble(false);
                                gasSensorMeter.setSpeedAt(d.getPpmValue());

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
                                gasSensorActivity.playingData = false;
                                gasSensorActivity.startedPlay = false;
                                gasSensorActivity.invalidateOptionsMenu();
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
        gasSensorActivity.startedPlay = true;
        try {
            if (recordedGasSensorArray.size() > 1) {
                GasSensorData i = recordedGasSensorArray.get(1);
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
        recordedGasSensorArray.clear();
        entries.clear();
        plotAllRecordedData();
        gasSensorActivity.startedPlay = false;
        gasSensorActivity.playingData = false;
        turns = 0;
        gasSensorActivity.invalidateOptionsMenu();
    }

    @Override
    public void saveGraph() {
        gasSensorActivity.csvLogger.prepareLogFile();
        gasSensorActivity.csvLogger.writeMetaData(getResources().getString(R.string.gas_sensor));
        gasSensorActivity.csvLogger.writeCSVFile(CSV_HEADER);
        for (GasSensorData baroData : gasSensorActivity.recordedGasSensorData) {
            gasSensorActivity.csvLogger.writeCSVFile(
                    new CSVDataLine()
                            .add(baroData.getTime())
                            .add(CSVLogger.FILE_NAME_FORMAT.format(new Date(baroData.getTime())))
                            .add(baroData.getPpmValue())
                            .add(baroData.getLat())
                            .add(baroData.getLon())
            );
        }
        View view = rootView.findViewById(R.id.gas_sensor_linearlayout);
        view.setDrawingCacheEnabled(true);
        Bitmap b = view.getDrawingCache();
        try {
            b.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(Environment.getExternalStorageDirectory().getAbsolutePath() +
                    File.separator + CSV_DIRECTORY + File.separator + gasSensorActivity.getSensorName() +
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
            if (gasSensorActivity.playingData) {
                gasSensorActivity.finish();
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
                        }
                    }
                });
            }
        }, 0, 1000);
    }

    private void writeLogToFile(long timestamp, float ppmValue) {
        if (getActivity() != null && gasSensorActivity.isRecording) {
            if (gasSensorActivity.writeHeaderToFile) {
                gasSensorActivity.csvLogger.prepareLogFile();
                gasSensorActivity.csvLogger.writeCSVFile(CSV_HEADER);
                block = timestamp;
                gasSensorActivity.recordSensorDataBlockID(new SensorDataBlock(timestamp, gasSensorActivity.getSensorName()));
                gasSensorActivity.writeHeaderToFile = !gasSensorActivity.writeHeaderToFile;
            }
            if (gasSensorActivity.addLocation && gasSensorActivity.gpsLogger.isGPSEnabled()) {
                Location location = gasSensorActivity.gpsLogger.getDeviceLocation();
                gasSensorActivity.csvLogger.writeCSVFile(
                        new CSVDataLine()
                                .add(timestamp)
                                .add(CSVLogger.FILE_NAME_FORMAT.format(new Date(timestamp)))
                                .add(ppmValue)
                                .add(location.getLatitude())
                                .add(location.getLongitude())
                );
                sensorData = new GasSensorData(timestamp, block, ppmValue, location.getLatitude(), location.getLongitude());
            } else {
                gasSensorActivity.csvLogger.writeCSVFile(
                        new CSVDataLine()
                                .add(timestamp)
                                .add(CSVLogger.FILE_NAME_FORMAT.format(new Date(timestamp)))
                                .add(ppmValue)
                                .add(0.0)
                                .add(0.0)
                );
                sensorData = new GasSensorData(timestamp, block, ppmValue, 0.0, 0.0);
            }
            gasSensorActivity.recordSensorData(sensorData);
        } else {
            gasSensorActivity.writeHeaderToFile = true;
        }
    }

    private void visualizeData() {
        double ppmValue = 0d;
        if (scienceLab.isConnected()) {
            double volt = scienceLab.getVoltage("CH1", 1);
            ppmValue = (volt / 3.3) * 1024.0;
            gasValue.setText(String.format(Locale.getDefault(), "%.2f", ppmValue));
            gasSensorMeter.setWithTremble(false);
            gasSensorMeter.setSpeedAt((float) ppmValue);
            timeElapsed = ((System.currentTimeMillis() - startTime) / updatePeriod);
            if (timeElapsed != previousTimeElapsed) {
                previousTimeElapsed = timeElapsed;
                Entry entry = new Entry((float) timeElapsed, (float) ppmValue);
                entries.add(entry);
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
        writeLogToFile(System.currentTimeMillis(), (float) ppmValue);
    }

    private void setupInstruments() {
        gasSensorMeter.setMaxSpeed(1024);
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
        y.setAxisMaximum(1024);
        y.setAxisMinimum(0);
        y.setDrawGridLines(true);
        y.setLabelCount(10);

        y2.setDrawGridLines(false);
        y2.setMaxWidth(0);
    }

    private void resetInstrumentData() {
        startTime = System.currentTimeMillis();
        gasValue.setText(DataFormatter.formatDouble(0, DataFormatter.LOW_PRECISION_FORMAT));
        gasSensorMeter.setSpeedAt(0);
        gasSensorMeter.setWithTremble(false);
        entries.clear();
    }
}
