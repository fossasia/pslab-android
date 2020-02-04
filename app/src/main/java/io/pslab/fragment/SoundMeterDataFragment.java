package io.pslab.fragment;

import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.anastr.speedviewlib.PointerSpeedometer;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.pslab.R;
import io.pslab.activity.SoundMeterActivity;
import io.pslab.models.PSLabSensor;
import io.pslab.models.SensorDataBlock;
import io.pslab.models.SoundData;
import io.pslab.others.AudioJack;
import io.pslab.others.CSVDataLine;
import io.pslab.others.CSVLogger;
import io.pslab.others.CustomSnackBar;

import static io.pslab.others.CSVLogger.CSV_DIRECTORY;

/**
 * @author reckoner1429
 */
public class SoundMeterDataFragment extends Fragment {

    public static final String TAG = "SoundMeterFragment";
    private static final CSVDataLine CSV_HEADER =
            new CSVDataLine()
                    .add("Timestamp")
                    .add("DateTime")
                    .add("Readings")
                    .add("Latitude")
                    .add("Longitude");
    @BindView(R.id.sound_max)
    TextView statMax;
    @BindView(R.id.sound_min)
    TextView statMin;
    @BindView(R.id.sound_avg)
    TextView statMean;
    @BindView(R.id.label_sound_sensor)
    TextView sensorLabel;
    @BindView(R.id.chart_sound_meter)
    LineChart mChart;
    @BindView(R.id.sound_meter)
    PointerSpeedometer decibelMeter;

    private SoundMeterActivity soundMeter;
    private View rootView;
    private Unbinder unbinder;
    private AudioJack audioJack;
    /**
     * Thread to handle recording in background
     */
    private HandlerThread bgThread;

    /**
     * Handler for the background Thread
     */
    private Handler bgThreadHandler;

    /**
     * Handler for the UI Thread, so that background thread could communicate with it
     */
    private Handler uiHandler;

    private boolean isRecording;
    private long recordStartTime;
    private long block;
    private Deque<Entry> chartQ;
    private double maxRmsAmp;
    private double minRmsAmp;
    private double rmsSum;
    private int count;

    public static SoundMeterDataFragment newInstance() {
        return new SoundMeterDataFragment();
    }

    public static void setParameters() {
        /**
         * TODO: Parameters yet to be determined
         */
        Log.i(TAG, "parameters yet to be determined");
    }

    /**********************************************************************************************
     * Fragment Lifecycle Methods
     **********************************************************************************************
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        soundMeter = (SoundMeterActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_sound_meter_data, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        setupInstruments();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();
        startRecording();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopRecording();
        stopBackgroundThread();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        soundMeter = null;
    }

    /**********************************************************************************************
     * Initializer methods
     **********************************************************************************************
     */
    private void setupInstruments() {
        decibelMeter.setMaxSpeed(120);
        decibelMeter.setMinSpeed(1);
        decibelMeter.setWithTremble(false);

        YAxis yAxis = mChart.getAxisLeft();

        yAxis.setAxisMaximum(10);
        yAxis.setAxisMinimum(-10);
        yAxis.setDrawZeroLine(true);
        yAxis.setDrawGridLines(false);
        yAxis.setLabelCount(20);
        mChart.setDrawGridBackground(false);
    }

    /*********************************************************************************************
     * Methods related to sound recording
     *********************************************************************************************
     */
    private void startRecording() {
        isRecording = true;
        audioJack = new AudioJack("input");
        recordStartTime = System.currentTimeMillis();
        chartQ = new ArrayDeque<>();
        bgThreadHandler.post(() -> {
            while (isRecording) {
                short[] buffer = audioJack.read();
                Bundle bundle = new Bundle();
                bundle.putShortArray("buffer", buffer);
                Message msg = new Message();
                msg.setData(bundle);
                uiHandler.sendMessage(msg);
            }
        });
    }

    private void stopRecording() {
        isRecording = false;
        audioJack.release();
        audioJack = null;
        chartQ.clear();
        chartQ = null;
        mChart.clear();
    }

    /*********************************************************************************************
     * Members related to handling Background Thread
     *********************************************************************************************
     */
    private void startBackgroundThread() {
        Log.i(TAG, "starting background thread");
        bgThread = new HandlerThread("Audio Recorder Thread");
        bgThread.start();
        bgThreadHandler = new Handler(bgThread.getLooper());
        uiHandler = new UIHandler(this);
        Log.i(TAG, "background Thread started");
    }

    private void stopBackgroundThread() {
        Log.i(TAG, "stopping background thread");
        if (bgThread != null) {
            bgThread.quitSafely();
            bgThread = null;
        }
        bgThreadHandler = null;
        uiHandler = null;
        Log.i(TAG, "Background Thread Stopped");
    }

    /**********************************************************************************************
     * Methods related to data visualization
     **********************************************************************************************
     */
    private void updateMeter(short[] buffer) {
        double sqrsum = 0.0;
        for (int i = 0; i < buffer.length; ++i) {
            sqrsum += Math.pow(buffer[i], 2);
        }
        double rmsamp = Math.sqrt((sqrsum / buffer.length));

        maxRmsAmp = Math.max(rmsamp, maxRmsAmp);
        minRmsAmp = Math.min(rmsamp, minRmsAmp);
        rmsSum = (count < Integer.MAX_VALUE) ? (rmsSum + rmsamp) : rmsamp;
        count = (count < Integer.MAX_VALUE) ? (count + 1) : 1;
        double avgRmsAmp = rmsSum / count;

        double loudness = rmsamp > 0 ? (10 * Math.log10(rmsamp / 1d)) : 1;
        double maxLoudness = maxRmsAmp > 0 ? (10 * Math.log10(maxRmsAmp / 1d)) : 1;
        double minLoudness = minRmsAmp > 0 ? (10 * Math.log10(minRmsAmp / 1d)) : 1;
        double avgLoudness = avgRmsAmp > 0 ? (10 * Math.log10(avgRmsAmp / 1d)) : 1;

        decibelMeter.setSpeedAt((float) loudness);
        statMax.setText(String.format(Locale.getDefault(), PSLabSensor.SOUNDMETER_DATA_FORMAT, maxLoudness));
        statMin.setText(String.format(Locale.getDefault(), PSLabSensor.SOUNDMETER_DATA_FORMAT, minLoudness));
        statMean.setText(String.format(Locale.getDefault(), PSLabSensor.SOUNDMETER_DATA_FORMAT, avgLoudness));
        writeLog(System.currentTimeMillis(), (float) loudness);
    }

    private void updateChart(short[] buffer) {
        for (int i = 0; i < buffer.length; ++i) {
            float x = (System.currentTimeMillis() - recordStartTime) / 1000f;
            float y = buffer[i] / 1000f;
            if (chartQ.size() >= buffer.length)
                chartQ.removeFirst();
            chartQ.addLast(new Entry(x, y));
            Log.i(TAG, "x : " + x + "  " + "y : " + y);
        }
        List<Entry> entries = new ArrayList<>(chartQ);
        LineDataSet dataSet = new LineDataSet(entries, "Amplitude");
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);
        dataSet.setLineWidth(0.5f);
        mChart.setData(new LineData(dataSet));
        mChart.notifyDataSetChanged();
        mChart.setVisibleXRangeMaximum(entries.size());
        mChart.invalidate();
    }

    /**
     * Method to play data which was previously recorded
     */
    public void playData() {
        CustomSnackBar.showSnackBar(getActivity().findViewById(android.R.id.content), getString(R.string.in_progress),
                null, null, Snackbar.LENGTH_SHORT);
        /**
         * TODO: To be implemented
         */

    }

    /**
     * Method to stop playing the previously recorded data
     */
    public void stopData() {
        CustomSnackBar.showSnackBar(getActivity().findViewById(android.R.id.content), getString(R.string.in_progress),
                null, null, Snackbar.LENGTH_SHORT);
        /**
         * TODO: To be implemented
         */
    }

    /**********************************************************************************************
     * Method Related to saving sound data
     **********************************************************************************************
     */
    private void writeLog(long timestamp, float dB) {
        SoundData soundData;
        if (getActivity() != null && soundMeter.isRecording) {
            if (soundMeter.writeHeaderToFile) {
                soundMeter.csvLogger.prepareLogFile();
                soundMeter.csvLogger.writeMetaData(getResources().getString(R.string.lux_meter));
                soundMeter.csvLogger.writeCSVFile(CSV_HEADER);
                block = timestamp;
                soundMeter.recordSensorDataBlockID(new SensorDataBlock(timestamp, soundMeter.getSensorName()));
                soundMeter.writeHeaderToFile = !soundMeter.writeHeaderToFile;
            }
            if (soundMeter.addLocation && soundMeter.gpsLogger.isGPSEnabled()) {
                String dateTime = CSVLogger.FILE_NAME_FORMAT.format(new Date(timestamp));
                Location location = soundMeter.gpsLogger.getDeviceLocation();
                soundMeter.csvLogger.writeCSVFile(
                        new CSVDataLine()
                                .add(timestamp)
                                .add(dateTime)
                                .add(dB)
                                .add(location.getLatitude())
                                .add(location.getLongitude()));
                soundData = new SoundData(timestamp, block, dB, location.getLatitude(), location.getLongitude());
            } else {
                String dateTime = CSVLogger.FILE_NAME_FORMAT.format(new Date(timestamp));
                soundMeter.csvLogger.writeCSVFile(
                        new CSVDataLine()
                                .add(timestamp)
                                .add(dateTime)
                                .add(dB)
                                .add(0.0)
                                .add(0.0));
                soundData = new SoundData(timestamp, block, dB, 0.0, 0.0);
            }
            soundMeter.recordSensorData(soundData);
        } else {
            soundMeter.writeHeaderToFile = true;
        }
    }

    public void saveGraph() {
        soundMeter.csvLogger.prepareLogFile();
        soundMeter.csvLogger.writeMetaData(getResources().getString(R.string.lux_meter));
        soundMeter.csvLogger.writeCSVFile(CSV_HEADER);
        for (SoundData soundData : soundMeter.recordedSoundData) {
            soundMeter.csvLogger.writeCSVFile(
                    new CSVDataLine()
                            .add(soundData.getTime())
                            .add(CSVLogger.FILE_NAME_FORMAT.format(new Date(soundData.getTime())))
                            .add(soundData.getdB())
                            .add(soundData.getLat())
                            .add(soundData.getLon()));
        }
        View view = rootView.findViewById(R.id.soundmeter_linearlayout);
        view.setDrawingCacheEnabled(true);
        Bitmap b = view.getDrawingCache();
        try {
            b.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(Environment.getExternalStorageDirectory().getAbsolutePath() +
                    File.separator + CSV_DIRECTORY + File.separator + soundMeter.getSensorName() +
                    File.separator + CSVLogger.FILE_NAME_FORMAT.format(new Date()) + "_graph.jpg"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static class UIHandler extends Handler {
        private SoundMeterDataFragment soundMeterDataFragment;

        UIHandler(SoundMeterDataFragment fragment) {
            this.soundMeterDataFragment = fragment;
        }

        @Override
        public void handleMessage(Message msg) { //handle the message passed by the background thread which is recording the audio
            if (soundMeterDataFragment.isResumed()) {
                short[] buffer = msg.getData().getShortArray("buffer");
                soundMeterDataFragment.updateMeter(buffer);
                /**
                 * TODO: smooth animation for the graph required
                 */
                soundMeterDataFragment.updateChart(buffer);
                Log.i(TAG, "handling message " + buffer.length + buffer[0]);
            }
        }
    }

}
