package io.pslab.fragment;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.anastr.speedviewlib.PointerSpeedometer;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.pslab.R;
import io.pslab.activity.SoundMeterActivity;
import io.pslab.interfaces.OperationCallback;
import io.pslab.models.PSLabSensor;
import io.pslab.models.SensorDataBlock;
import io.pslab.models.SoundData;
import io.pslab.others.AudioJack;
import io.pslab.others.CSVDataLine;
import io.pslab.others.CSVLogger;

import static io.pslab.others.CSVLogger.CSV_DIRECTORY;

public class SoundMeterDataFragment extends Fragment implements OperationCallback {

    public static final String TAG = "SoundMeterFragment";
    private static final CSVDataLine CSV_HEADER =
            new CSVDataLine()
                    .add("Timestamp")
                    .add("DateTime")
                    .add("Readings")
                    .add("Latitude")
                    .add("Longitude");
    private static final String KEY_LOUDNESS = "key loudness";
    private static final String KEY_MAX_LOUDNESS = "key max loudness";
    private static final String KEY_MIN_LOUDNESS = "key min loudness";
    private static final String KEY_AVG_LOUDNESS = "key average loudness";
    private static final int ANIMATION_BUFFER_SIZE = 500;

    private static double refIntensity;
    private static int movingAvgWindowSize;

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
    private List<SoundData> recordedSoundData;
    private int counter;

    /**
     * Thread to handle processing in background
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

    /**
     * Scheduled executor to view recorded data
     */
    private ScheduledExecutorService scheduledExecutorService;

    /**
     * Recorded data player handle to cancel the scheduled task created by scheduledExecutorService
     */
    ScheduledFuture<?> dataPlayerHandle;

    private boolean isProcessing;

    /**
     * variable to store the starting time of recording
     */
    private long recordStartTime;

    /**
     * variable to store resume time to calculate offset
     */
    private long resumeTime;

    /**
     * variable to store the current time when playing is paused
     */
    private long pauseTime;

    /**
     * offset to keep record of time that has already been played before pausing
     */
    private long offset;

    private long block;

    /*
        Variables to store values during processing
     */
    private double maxRmsAmp;
    private double minRmsAmp;
    private double rmsSum;

    /**
     * Double ended queue to how chart entries for current window
     */
    private Deque<Entry> chartQ;

    /**
     * Window to calculate the moving average
     */
    private Deque<Double> movingAvgWindow;

    public static SoundMeterDataFragment newInstance() {
        return new SoundMeterDataFragment();
    }

    public static void setParameters(double refIntensity, int movingAvgWindowSize) {
        SoundMeterDataFragment.refIntensity = refIntensity;
        SoundMeterDataFragment.movingAvgWindowSize = movingAvgWindowSize;
    }

    /* ********************************************************************************************
     * Fragment Lifecycle Methods
     * ********************************************************************************************
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        soundMeter = (SoundMeterActivity) getActivity();
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        chartQ = new ArrayDeque<>();
        counter = 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_sound_meter_data, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        setupInstruments();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();
        if(soundMeter.viewingData) {
            /*
             * reset counter to 0
             */
            recordedSoundData = new ArrayList<>();
            recordedSoundData.addAll(soundMeter.recordedSoundData);
        } else {
            /*
             * Start processing the sound from the environment
             */
            startProcessing();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(soundMeter.playingData) {
                pausePlaying();
        } else if (isProcessing) {
            stopProcessing();
        }
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
        scheduledExecutorService = null;
        chartQ.clear();
        chartQ = null;
    }

    /* ********************************************************************************************
     * Initializer methods
     * ********************************************************************************************
     */
    private void setupInstruments() {
        decibelMeter.setMaxSpeed(200.0f);
        decibelMeter.setMinSpeed(0.0f);
        decibelMeter.setWithTremble(false);

        YAxis yAxis = mChart.getAxisLeft();

        yAxis.setAxisMaximum(200);
        yAxis.setAxisMinimum(0);
        yAxis.setLabelCount(40);
        yAxis.setDrawGridLines(false);
        yAxis.setTextColor(Color.WHITE);
        LimitLine dangerLine = new LimitLine(100, getString(R.string.limit_dangerous));
        dangerLine.setLineColor(Color.RED);
        dangerLine.setTextColor(Color.RED);
        yAxis.addLimitLine(dangerLine);

        XAxis x = mChart.getXAxis();

        x.setTextColor(Color.WHITE);
        x.setDrawGridLines(true);
        x.setAvoidFirstLastClipping(true);


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

    }

    /* ********************************************************************************************
     * Members related to handling Background Thread
     * ********************************************************************************************
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

    /* ********************************************************************************************
     * Methods related to sound processing
     * ********************************************************************************************
     */
    private void startProcessing() {
        isProcessing = true;
        audioJack = new AudioJack("input");
        recordStartTime = System.currentTimeMillis();
        movingAvgWindow = new ArrayDeque<>();
        bgThreadHandler.post(() -> {
            while (isProcessing) {
                /*
                 * read the audio samples from the hardware device
                 */
                short[] buffer = audioJack.read();

                /*
                 * Calculate the root mean square amplitude of the values in the buffer.
                 */
                double sqrsum = 0.0;
                for (short val : buffer) {
                    sqrsum += Math.pow(val, 2);
                }
                double rmsamp = Math.sqrt((sqrsum / buffer.length));

                /*
                 * update the moving average window
                 */
                if ((movingAvgWindow.size() >= movingAvgWindowSize)) {
                    rmsSum -= movingAvgWindow.removeFirst();
                }
                movingAvgWindow.addLast(rmsamp);
                rmsSum += rmsamp;

                /*
                 * Calculate average, max and min root-mean-square(rms) amplitude
                 */
                double avgRmsAmp = rmsSum / movingAvgWindow.size();
                maxRmsAmp = Math.max(rmsamp, maxRmsAmp);
                minRmsAmp = Math.min(rmsamp, minRmsAmp);

                /*
                 * Calculate the current, max, min and average loudness for the current instant
                 */
                double loudness = rmsamp > 0 ? (10 * Math.log10(rmsamp / refIntensity)) : 1;
                double maxLoudness = maxRmsAmp > 0 ? (10 * Math.log10(maxRmsAmp / refIntensity)) : 1;
                double minLoudness = minRmsAmp > 0 ? (10 * Math.log10(minRmsAmp / refIntensity)) : 1;
                double avgLoudness = avgRmsAmp > 0 ? (10 * Math.log10(avgRmsAmp / refIntensity)) : 1;

                /*
                 * Bundle the values to be sent to the ui handler
                 */
                Bundle bundle = new Bundle();
                bundle.putDouble(KEY_LOUDNESS, loudness);
                bundle.putDouble(KEY_MAX_LOUDNESS, maxLoudness);
                bundle.putDouble(KEY_MIN_LOUDNESS, minLoudness);
                bundle.putDouble(KEY_AVG_LOUDNESS, avgLoudness);
                Message msg = new Message();
                msg.setData(bundle);
                uiHandler.sendMessage(msg);
            }
        });
    }

    private void stopProcessing() {
        isProcessing = false;
        audioJack.release();
        audioJack = null;
        resetViews();
    }

    /* ********************************************************************************************
     * Methods related to data visualization
     * ********************************************************************************************
     */
    private void updateMeter(double loudness, double avgLoudness, double maxLoudness, double minLoudness) {
        decibelMeter.setSpeedAt((float) loudness);
        statMax.setText(String.format(Locale.getDefault(), PSLabSensor.SOUNDMETER_DATA_FORMAT, maxLoudness));
        statMin.setText(String.format(Locale.getDefault(), PSLabSensor.SOUNDMETER_DATA_FORMAT, minLoudness));
        statMean.setText(String.format(Locale.getDefault(), PSLabSensor.SOUNDMETER_DATA_FORMAT, avgLoudness));
    }

    private void updateChart(double loudness, double avgLoudness, double maxLoudness, double minLoudness, long startTime, long offset) {
        float x = (offset + (System.currentTimeMillis() - startTime)) / 1000f;
        chartQ.addLast(new Entry(x, (float)loudness));
        if(chartQ.size() > ANIMATION_BUFFER_SIZE)
            chartQ.removeFirst();
        List<Entry> entries = new ArrayList<>(chartQ);
        LineDataSet dataSet = new LineDataSet(entries, getString(R.string.sound_chart_label));
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(true);
        dataSet.setLineWidth(0.5f);
        mChart.setData(new LineData(dataSet));
        mChart.notifyDataSetChanged();
        mChart.invalidate();
    }

    private void resetViews() {
        chartQ.clear();
        mChart.clear();
        decibelMeter.setSpeedAt(0.0f);
        Log.i(TAG,"view reset complete");
    }

    /* ********************************************************************************************
     * Methods related to view previously recorded data
     * ********************************************************************************************
     */

    private void playRecordedData(long startTime, long offset) {
        long period = ( recordedSoundData.get(recordedSoundData.size()-1).getTime() -
                recordedSoundData.get(0).getTime() ) / recordedSoundData.size();
        dataPlayerHandle = scheduledExecutorService.scheduleWithFixedDelay(()-> {
            SoundData soundData = recordedSoundData.get(counter);
            uiHandler.post(() -> {
                if(soundMeter.playingData) {
                    updateChart(soundData.getdB(), soundData.getAvgLoudness(),
                            soundData.getMaxLoudness(), soundData.getMinLoudness(), startTime, offset);
                    updateMeter(soundData.getdB(), soundData.getAvgLoudness(),
                            soundData.getMaxLoudness(), soundData.getMinLoudness());
                }
            });
            counter ++;
            if(counter == recordedSoundData.size()) {
                stopPlaying();
            }
        }, 0, period, TimeUnit.MILLISECONDS);
    }

    private void startPlaying() {
        soundMeter.startedPlay = true;
        resumeTime = System.currentTimeMillis();
        playRecordedData(resumeTime, 0);
    }

    private void stopPlaying() {
        uiHandler.post(()-> {
            dataPlayerHandle.cancel(false);
            soundMeter.playingData = false;
            soundMeter.startedPlay = false;
            soundMeter.invalidateOptionsMenu();
            resetViews();
            counter = 0;
            resumeTime = 0;
            offset = 0;
            pauseTime = 0;
        });
    }

    private void resumePlaying() {
        offset += pauseTime - resumeTime;
        resumeTime = System.currentTimeMillis();
        playRecordedData(resumeTime, offset);
    }

    private void pausePlaying() {
        uiHandler.post(()-> {
            dataPlayerHandle.cancel(false);
            pauseTime = System.currentTimeMillis();
            soundMeter.playingData = false;
            soundMeter.invalidateOptionsMenu();
        });
    }

    /**
     * Method to play data which was previously recorded
     */
    @Override
    public void playData() {
        startPlaying();
    }

    /**
     * Method to pause playing
     */
    public void pause() {
        pausePlaying();
    }

    /**
     * Method to resume playing
     */
    public void resume() {
        resumePlaying();
    }

    /**
     * Method to stop playing the previously recorded data
     */
    @Override
    public void stopData() {
        stopPlaying();
    }

    /* ********************************************************************************************
     * Method Related to saving sound data
     * ********************************************************************************************
     */
    private void writeLog(long timestamp, float dB, float avgLoudness, float maxLoudness, float minLoudness) {
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
                                .add((location != null)?location.getLatitude():0.0d)
                                .add((location != null)?location.getLongitude():0.0d));
                soundData = new SoundData(timestamp, block, dB, avgLoudness, maxLoudness, minLoudness,
                        (location!=null)?location.getLatitude():0.0d, (location!=null)?location.getLongitude():0.0d);
            } else {
                String dateTime = CSVLogger.FILE_NAME_FORMAT.format(new Date(timestamp));
                soundMeter.csvLogger.writeCSVFile(
                        new CSVDataLine()
                                .add(timestamp)
                                .add(dateTime)
                                .add(dB)
                                .add(0.0)
                                .add(0.0));
                soundData = new SoundData(timestamp, block, dB, avgLoudness, maxLoudness, minLoudness,
                        0.0, 0.0);
            }
            soundMeter.recordSensorData(soundData);
        } else {
            soundMeter.writeHeaderToFile = true;
        }
    }

    @Override
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

    /**
     * The implementation of Handler class for the UI Thread
     */
    private static class UIHandler extends Handler {
        private SoundMeterDataFragment soundMeterDataFragment;

        UIHandler(SoundMeterDataFragment fragment) {
            this.soundMeterDataFragment = fragment;
        }

        @Override
        public void handleMessage(Message msg) { //handle the message passed by the background thread which is processing the audio
            if (soundMeterDataFragment.isResumed()) {
                Bundle bundle = msg.getData();
                double loudness = bundle.getDouble(KEY_LOUDNESS);
                double maxLoudness = bundle.getDouble(KEY_MAX_LOUDNESS);
                double minLoudness = bundle.getDouble(KEY_MIN_LOUDNESS);
                double avgLoudness = bundle.getDouble(KEY_AVG_LOUDNESS);
                soundMeterDataFragment.updateMeter(loudness, avgLoudness, maxLoudness, minLoudness);
                soundMeterDataFragment.updateChart(loudness, avgLoudness, maxLoudness, minLoudness, soundMeterDataFragment.recordStartTime, 0);
                soundMeterDataFragment.writeLog(System.currentTimeMillis(), (float) loudness, (float)avgLoudness, (float)maxLoudness, (float)minLoudness);
            }
        }
    }

}
