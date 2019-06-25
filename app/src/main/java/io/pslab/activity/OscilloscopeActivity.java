package io.pslab.activity;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.apache.commons.math3.complex.Complex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.pslab.R;
import io.pslab.communication.AnalyticsClass;
import io.pslab.communication.ScienceLab;
import io.pslab.fragment.ChannelParametersFragment;
import io.pslab.fragment.DataAnalysisFragment;
import io.pslab.fragment.OscilloscopePlaybackFragment;
import io.pslab.fragment.TimebaseTriggerFragment;
import io.pslab.fragment.XYPlotFragment;
import io.pslab.models.OscilloscopeData;
import io.pslab.models.SensorDataBlock;
import io.pslab.others.AudioJack;
import io.pslab.others.CSVLogger;
import io.pslab.others.CustomSnackBar;
import io.pslab.others.GPSLogger;
import io.pslab.others.LocalDataLog;
import io.pslab.others.MathUtils;
import io.pslab.others.Plot2D;
import io.pslab.others.ScienceLabCommon;
import io.pslab.others.SwipeGestureDetector;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;

import static io.pslab.others.MathUtils.map;

/**
 * Created by viveksb007 on 10/5/17.
 */

public class OscilloscopeActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String PREF_NAME = "OscilloscopeActivity";
    private final Object lock = new Object();
    @BindView(R.id.chart_os)
    public LineChart mChart;
    @BindView(R.id.tv_label_left_yaxis_os)
    public TextView leftYAxisLabel;
    @BindView(R.id.tv_unit_left_yaxis_os)
    public TextView leftYAxisLabelUnit;
    @BindView(R.id.tv_label_right_yaxis_os)
    public TextView rightYAxisLabel;
    @BindView(R.id.tv_unit_right_yaxis_os)
    public TextView rightYAxisLabelUnit;
    @BindView(R.id.tv_graph_label_xaxis_os)
    public TextView xAxisLabel;
    @BindView(R.id.tv_unit_xaxis_os)
    public TextView xAxisLabelUnit;
    public int samples;
    public double timeGap;
    public double timebase;
    public double xAxisScale = 875f;
    public boolean isCH1Selected;
    public boolean isCH2Selected;
    public boolean isCH3Selected;
    public boolean isMICSelected;
    public boolean isInBuiltMicSelected;
    public boolean isAudioInputSelected;
    public boolean isTriggerSelected;
    public boolean isFourierTransformSelected;
    public boolean isXYPlotSelected;
    public boolean sineFit;
    public boolean squareFit;
    public boolean viewIsClicked;
    public boolean isCH1FrequencyRequired;
    public boolean isCH2FrequencyRequired;
    public String triggerChannel;
    public String curveFittingChannel1;
    public String curveFittingChannel2;
    public String xyPlotXAxisChannel;
    public String xyPlotYAxisChannel;
    public double trigger;
    public Plot2D graph;
    @BindView(R.id.layout_dock_os1)
    LinearLayout linearLayout;
    @BindView(R.id.layout_dock_os2)
    FrameLayout frameLayout;
    @BindView(R.id.layout_chart_os)
    RelativeLayout mChartLayout;
    @BindView(R.id.button_channel_parameters_os)
    ImageButton channelParametersButton;
    @BindView(R.id.button_timebase_os)
    ImageButton timebaseButton;
    @BindView(R.id.button_data_analysis_os)
    ImageButton dataAnalysisButton;
    @BindView(R.id.button_xy_plot_os)
    ImageButton xyPlotButton;
    @BindView(R.id.tv_channel_parameters_os)
    TextView channelParametersTextView;
    @BindView(R.id.tv_timebase_tigger_os)
    TextView timebaseTiggerTextView;
    @BindView(R.id.tv_data_analysis_os)
    TextView dataAnalysisTextView;
    @BindView(R.id.tv_xy_plot_os)
    TextView xyPlotTextView;
    @BindView(R.id.img_arrow_oscilloscope)
    ImageView arrowUpDown;
    @BindView(R.id.sheet_slide_text_oscilloscope)
    TextView bottomSheetSlideText;
    @BindView(R.id.parent_layout)
    View parentLayout;
    @BindView(R.id.bottom_sheet_oscilloscope)
    LinearLayout bottomSheet;
    private Fragment channelParametersFragment;
    private Fragment timebaseTriggerFragment;
    private Fragment dataAnalysisFragment;
    private Fragment xyPlotFragment;
    private Fragment playbackFragment;
    @BindView(R.id.imageView_led_os)
    ImageView ledImageView;
    @BindView(R.id.show_guide_oscilloscope)
    TextView showText;
    private ScienceLab scienceLab;
    private int height;
    private int width;
    private XAxis x1;
    private YAxis y1;
    private YAxis y2;
    private XYPlotTask xyPlotTask;
    private AudioJack audioJack = null;
    private AnalyticsClass analyticsClass;
    private CaptureTask captureTask;
    private Thread monitorThread;
    private volatile boolean monitor = true;
    private BottomSheetBehavior bottomSheetBehavior;
    private GestureDetector gestureDetector;
    private boolean btnLongpressed;
    private double maxAmp, maxFreq;
    private ImageView recordButton;
    private boolean isRecording = false;
    private Realm realm;
    public RealmResults<OscilloscopeData> recordedOscilloscopeData;
    private CSVLogger csvLogger;
    private GPSLogger gpsLogger;
    private long block;
    private Timer recordTimer;
    private long recordPeriod = 100;
    private String oscilloscopeCSVHeader = "Timestamp,DateTime,Channel,xData,yData,Timebase,lat,lon";
    private String loggingXdata = "";
    private final String KEY_LOG = "has_log";
    private final String DATA_BLOCK = "data_block";
    private int currentPosition = 0;
    private Timer playbackTimer;
    private View mainLayout;
    private double lat;
    private double lon;
    public boolean isPlaybackFourierChecked = false;
    private HashMap<String, Integer> channelIndexMap;
    private Integer[] channelColors = {Color.CYAN, Color.GREEN, Color.WHITE, Color.MAGENTA};
    private String[] loggingYdata = new String[4];

    private enum CHANNEL {CH1, CH2, CH3, MIC}

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_oscilloscope);
        ButterKnife.bind(this);

        removeStatusBar();
        setUpBottomSheet();
        parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                parentLayout.setVisibility(View.GONE);
            }
        });
        mainLayout = findViewById(R.id.oscilloscope_mail_layout);

        channelIndexMap = new HashMap<>();
        channelIndexMap.put(CHANNEL.CH1.toString(), 0);
        channelIndexMap.put(CHANNEL.CH2.toString(), 1);
        channelIndexMap.put(CHANNEL.CH3.toString(), 2);
        channelIndexMap.put(CHANNEL.MIC.toString(), 3);

        realm = LocalDataLog.with().getRealm();
        gpsLogger = new GPSLogger(this,
                (LocationManager) getSystemService(Context.LOCATION_SERVICE));
        csvLogger = new CSVLogger(getString(R.string.oscilloscope));

        recordButton = findViewById(R.id.oscilloscope_record_button);

        scienceLab = ScienceLabCommon.scienceLab;
        x1 = mChart.getXAxis();
        y1 = mChart.getAxisLeft();
        y2 = mChart.getAxisRight();
        triggerChannel = CHANNEL.CH1.toString();
        trigger = 0;
        timebase = 875;
        samples = 512;
        timeGap = 2;
        sineFit = true;
        squareFit = false;
        graph = new Plot2D(this, new float[]{}, new float[]{}, 1);
        curveFittingChannel1 = "None";
        curveFittingChannel2 = "None";
        xyPlotXAxisChannel = CHANNEL.CH1.toString();
        xyPlotYAxisChannel = CHANNEL.CH2.toString();
        viewIsClicked = false;
        analyticsClass = new AnalyticsClass();
        isCH1FrequencyRequired = false;
        isCH2FrequencyRequired = false;

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;

        onWindowFocusChanged();

        channelParametersFragment = new ChannelParametersFragment();
        timebaseTriggerFragment = new TimebaseTriggerFragment();
        dataAnalysisFragment = new DataAnalysisFragment();
        xyPlotFragment = new XYPlotFragment();
        playbackFragment = new OscilloscopePlaybackFragment();

        if (findViewById(R.id.layout_dock_os2) != null) {
            addFragment(R.id.layout_dock_os2, channelParametersFragment);
        }

        channelParametersButton.setOnClickListener(this);
        timebaseButton.setOnClickListener(this);
        dataAnalysisButton.setOnClickListener(this);
        xyPlotButton.setOnClickListener(this);
        channelParametersTextView.setOnClickListener(this);
        timebaseTiggerTextView.setOnClickListener(this);
        dataAnalysisTextView.setOnClickListener(this);
        xyPlotTextView.setOnClickListener(this);

        chartInit();

        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                //Thread to check which checkbox is enabled
                while (monitor) {
                    if (isInBuiltMicSelected && audioJack == null) {
                        audioJack = new AudioJack("input");
                    }

                    if (scienceLab.isConnected() && isCH1Selected && !isCH2Selected && !isCH3Selected && !isAudioInputSelected && !isXYPlotSelected) {
                        captureTask = new CaptureTask();
                        captureTask.execute(CHANNEL.CH1.toString());
                        synchronized (lock) {
                            try {
                                lock.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if (scienceLab.isConnected() && isCH2Selected && !isCH1Selected && !isCH3Selected && !isAudioInputSelected && !isXYPlotSelected) {
                        captureTask = new CaptureTask();
                        captureTask.execute(CHANNEL.CH2.toString());
                        synchronized (lock) {
                            try {
                                lock.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if (scienceLab.isConnected() && isCH3Selected && !isCH1Selected && !isCH2Selected && !isAudioInputSelected && !isXYPlotSelected) {
                        captureTask = new CaptureTask();
                        captureTask.execute(CHANNEL.CH3.toString());
                        synchronized (lock) {
                            try {
                                lock.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if (scienceLab.isConnected() && isAudioInputSelected && !isCH1Selected && !isCH2Selected && !isCH3Selected && !isXYPlotSelected) {
                        captureTask = new CaptureTask();
                        captureTask.execute(CHANNEL.MIC.toString());
                        synchronized (lock) {
                            try {
                                lock.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if (scienceLab.isConnected() && isCH1Selected && isCH2Selected && !isCH3Selected && !isAudioInputSelected && !isXYPlotSelected) {
                        captureTask = new CaptureTask();
                        captureTask.execute(CHANNEL.CH1.toString(), CHANNEL.CH2.toString());
                        synchronized (lock) {
                            try {
                                lock.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if (scienceLab.isConnected() && isCH1Selected && !isCH2Selected && isCH3Selected && !isAudioInputSelected && !isXYPlotSelected) {
                        captureTask = new CaptureTask();
                        captureTask.execute(CHANNEL.CH1.toString(), CHANNEL.CH3.toString());
                        synchronized (lock) {
                            try {
                                lock.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if (scienceLab.isConnected() && isAudioInputSelected && isCH1Selected && !isCH3Selected && !isCH2Selected && !isXYPlotSelected) {
                        captureTask = new CaptureTask();
                        captureTask.execute(CHANNEL.CH1.toString(), CHANNEL.MIC.toString());
                        synchronized (lock) {
                            try {
                                lock.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if (scienceLab.isConnected() && isCH2Selected && isCH3Selected && !isCH1Selected && !isAudioInputSelected && !isXYPlotSelected) {
                        captureTask = new CaptureTask();
                        captureTask.execute(CHANNEL.CH2.toString(), CHANNEL.CH3.toString());
                        synchronized (lock) {
                            try {
                                lock.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if (scienceLab.isConnected() && isCH2Selected && isAudioInputSelected && !isCH3Selected && !isCH1Selected && !isXYPlotSelected) {
                        captureTask = new CaptureTask();
                        captureTask.execute(CHANNEL.CH2.toString(), CHANNEL.MIC.toString());
                        synchronized (lock) {
                            try {
                                lock.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if (scienceLab.isConnected() && isCH3Selected && isAudioInputSelected && !isCH2Selected && !isCH1Selected && !isXYPlotSelected) {
                        captureTask = new CaptureTask();
                        captureTask.execute(CHANNEL.CH3.toString(), CHANNEL.MIC.toString());
                        synchronized (lock) {
                            try {
                                lock.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if (scienceLab.isConnected() && isCH1Selected && isCH2Selected && isCH3Selected && !isAudioInputSelected && !isXYPlotSelected) {
                        captureTask = new CaptureTask();
                        captureTask.execute(CHANNEL.CH1.toString(), CHANNEL.CH2.toString(), CHANNEL.CH3.toString());
                        synchronized (lock) {
                            try {
                                lock.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    if (scienceLab.isConnected() && isCH1Selected && isCH2Selected && isCH3Selected && isAudioInputSelected && !isXYPlotSelected) {
                        captureTask = new CaptureTask();
                        captureTask.execute(CHANNEL.CH1.toString(), CHANNEL.CH2.toString(), CHANNEL.CH3.toString(), CHANNEL.MIC.toString());
                        synchronized (lock) {
                            try {
                                lock.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if (!scienceLab.isConnected() || (!isCH1Selected && !isCH2Selected && !isCH3Selected && !isAudioInputSelected)) {
                        if (!String.valueOf(ledImageView.getTag()).equals("red")) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ledImageView.setImageResource(R.drawable.red_led);
                                    ledImageView.setTag("red");
                                }
                            });
                        }
                    }

                    if (scienceLab.isConnected() && viewIsClicked && isXYPlotSelected) {
                        xyPlotTask = new XYPlotTask();
                        if (xyPlotXAxisChannel.equals(CHANNEL.CH2.toString()))
                            xyPlotTask.execute(xyPlotYAxisChannel);
                        else
                            xyPlotTask.execute(xyPlotXAxisChannel);
                        synchronized (lock) {
                            try {
                                lock.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if (!isInBuiltMicSelected) {
                        if (audioJack != null) {
                            audioJack.release();
                            audioJack = null;
                        }
                    }
                }
            }
        };
        monitorThread = new Thread(runnable);
        monitorThread.start();

        ImageView guideImageView = findViewById(R.id.oscilloscope_guide_button);
        guideImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN ?
                        BottomSheetBehavior.STATE_EXPANDED : BottomSheetBehavior.STATE_HIDDEN);
            }
        });
        guideImageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showText.setVisibility(View.VISIBLE);
                btnLongpressed = true;
                return true;
            }
        });
        guideImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.onTouchEvent(event);
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (btnLongpressed) {
                        showText.setVisibility(View.GONE);
                        btnLongpressed = false;
                    }
                }
                return true;
            }
        });

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecording) {
                    isRecording = false;
                    recordButton.setImageResource(R.drawable.ic_record_white);
                    CustomSnackBar.showSnackBar(mainLayout,
                            getString(R.string.csv_store_text) + " " + csvLogger.getCurrentFilePath()
                            , getString(R.string.open), new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent(OscilloscopeActivity.this, DataLoggerActivity.class);
                                    intent.putExtra(DataLoggerActivity.CALLER_ACTIVITY, getResources().getString(R.string.oscilloscope));
                                    startActivity(intent);
                                }
                            }, Snackbar.LENGTH_SHORT);
                } else {
                    isRecording = true;
                    recordButton.setImageResource(R.drawable.ic_record_stop_white);
                    block = System.currentTimeMillis();
                    if (gpsLogger.isGPSEnabled()) {
                        Location location = gpsLogger.getDeviceLocation();
                        if (location != null) {
                            lat = location.getLatitude();
                            lon = location.getLongitude();
                        } else {
                            lat = 0.0;
                            lon = 0.0;
                        }
                    } else {
                        lat = 0.0;
                        lon = 0.0;
                    }
                    csvLogger = new CSVLogger(getResources().getString(R.string.oscilloscope));
                    csvLogger.prepareLogFile();
                    csvLogger.writeCSVFile(oscilloscopeCSVHeader);
                    recordSensorDataBlockID(new SensorDataBlock(block, getResources().getString(R.string.oscilloscope)));
                    CustomSnackBar.showSnackBar(mainLayout, getString(R.string.data_recording_start), null, null, Snackbar.LENGTH_SHORT);
                }
            }
        });

        if (getIntent().getExtras() != null && getIntent().getExtras().getBoolean(KEY_LOG)) {
            recordedOscilloscopeData = LocalDataLog.with()
                    .getBlockOfOscilloscopeRecords(getIntent().getExtras().getLong(DATA_BLOCK));
            setLayoutForPlayback();
        }
    }

    private void setLayoutForPlayback() {
        findViewById(R.id.layout_dock_os1).setVisibility(View.GONE);
        recordButton.setVisibility(View.GONE);
        RelativeLayout.LayoutParams lineChartParams = (RelativeLayout.LayoutParams) mChartLayout.getLayoutParams();
        RelativeLayout.LayoutParams frameLayoutParams = (RelativeLayout.LayoutParams) frameLayout.getLayoutParams();
        lineChartParams.height = height * 4 / 5;
        lineChartParams.width = width;
        mChartLayout.setLayoutParams(lineChartParams);
        frameLayoutParams.height = height / 5;
        frameLayoutParams.width = width;
        frameLayout.setLayoutParams(frameLayoutParams);
        replaceFragment(R.id.layout_dock_os2, playbackFragment, "Playback Fragment");
    }

    public void playRecordedData() {
        final Handler handler = new Handler();
        if (playbackTimer == null) {
            playbackTimer = new Timer();
        }
        playbackTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (currentPosition < recordedOscilloscopeData.size()) {
                                OscilloscopeData data = recordedOscilloscopeData.get(currentPosition);
                                int mode = data.getMode();
                                List<ILineDataSet> dataSets = new ArrayList<>();
                                ArrayList<ArrayList<Entry>> entries = new ArrayList<>();
                                for (int i = 0; i < mode; i++) {
                                    data = recordedOscilloscopeData.get(currentPosition);
                                    entries.add(new ArrayList<>());
                                    String[] xData = data.getDataX().split(" ");
                                    String[] yData = data.getDataY().split(" ");
                                    if (!isPlaybackFourierChecked) {
                                        int n = Math.min(xData.length, yData.length);
                                        for (int j = 0; j < n; j++) {
                                            if (xData[j].length() > 0 && yData[j].length() > 0) {
                                                entries.get(i).add(new Entry(Float.valueOf(xData[j]), Float.valueOf(yData[j])));
                                            }
                                        }
                                        setLeftYAxisScale(16f, -16f);
                                        setRightYAxisScale(16f, -16f);
                                        setXAxisScale(data.getTimebase());
                                    } else {
                                        Complex[] yComplex = new Complex[yData.length];
                                        for (int j = 0; j < yData.length; j++) {
                                            yComplex[j] = Complex.valueOf(Double.valueOf(yData[j]));
                                        }
                                        Complex[] fftOut = fft(yComplex);
                                        int n = fftOut.length;
                                        double mA = 0;
                                        double factor = samples * timeGap * 1e-3;
                                        double mF = (n / 2 - 1) / factor;
                                        for (int j = 0; j < n / 2; j++) {
                                            float y = (float) fftOut[j].abs() / samples;
                                            if (y > mA) {
                                                mA = y;
                                            }
                                            entries.get(i).add(new Entry((float) (j / factor), y));
                                        }
                                        setLeftYAxisScale(mA, 0);
                                        setRightYAxisScale(mA, 0);
                                        setXAxisScale(mF);
                                    }
                                    currentPosition++;
                                    LineDataSet dataSet;
                                    dataSet = new LineDataSet(entries.get(i), data.getChannel());
                                    dataSet.setDrawCircles(false);
                                    dataSet.setColor(channelColors[i]);
                                    dataSets.add(dataSet);
                                    ((OscilloscopePlaybackFragment) playbackFragment).setTimeBase(String.valueOf(data.getTimebase()));
                                }
                                LineData lineData = new LineData(dataSets);
                                mChart.setData(lineData);
                                mChart.notifyDataSetChanged();
                                mChart.invalidate();
                            } else {
                                playbackTimer.cancel();
                                playbackTimer = null;
                                ((OscilloscopePlaybackFragment) playbackFragment).resetPlayButton();
                                currentPosition = 0;
                            }
                        } catch (Exception e) {
                            if (playbackTimer != null) {
                                playbackTimer.cancel();
                                playbackTimer = null;
                            }
                            ((OscilloscopePlaybackFragment) playbackFragment).resetPlayButton();
                            currentPosition = 0;
                        }
                    }
                });

            }
        }, 0, recordPeriod);
    }

    public void pauseData() {
        playbackTimer.cancel();
        playbackTimer = null;
    }

    private void logChannelData(String[] channels) {
        long timestamp = System.currentTimeMillis();
        int noOfChannels = channels.length;
        String timeData = timestamp + "," + CSVLogger.FILE_NAME_FORMAT.format(new Date(timestamp));
        String locationData = lat + "," + lon;
        for (int i = 0; i < noOfChannels; i++) {
            recordSensorData(new OscilloscopeData(timestamp + i, block, noOfChannels, channels[i], loggingXdata, loggingYdata[i], xAxisScale, lat, lon));
            String data = timeData + "," + channels[i] + "," + loggingXdata + "," + loggingYdata[i] + "," + xAxisScale + "," + locationData;
            csvLogger.writeCSVFile(data);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        removeStatusBar();
    }

    private void removeStatusBar() {
        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            View decorView = getWindow().getDecorView();

            decorView.setSystemUiVisibility((View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_channel_parameters_os:
            case R.id.tv_channel_parameters_os:
                replaceFragment(R.id.layout_dock_os2, channelParametersFragment, "ChannelParametersFragment");
                clearTextBackgroundColor();
                channelParametersTextView.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                break;

            case R.id.button_timebase_os:
            case R.id.tv_timebase_tigger_os:
                replaceFragment(R.id.layout_dock_os2, timebaseTriggerFragment, "TimebaseTiggerFragment");
                clearTextBackgroundColor();
                timebaseTiggerTextView.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                break;

            case R.id.button_data_analysis_os:
            case R.id.tv_data_analysis_os:
                replaceFragment(R.id.layout_dock_os2, dataAnalysisFragment, "DataAnalysisFragment");
                clearTextBackgroundColor();
                dataAnalysisTextView.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                break;

            case R.id.button_xy_plot_os:
            case R.id.tv_xy_plot_os:
                replaceFragment(R.id.layout_dock_os2, xyPlotFragment, "XYPlotFragment");
                clearTextBackgroundColor();
                xyPlotTextView.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                break;

            default:
                break;
        }
    }

    @SuppressLint("ResourceType")
    private void clearTextBackgroundColor() {
        channelParametersTextView.setBackgroundColor(getResources().getColor(R.color.customBorderFill));
        timebaseTiggerTextView.setBackgroundColor(getResources().getColor(R.color.customBorderFill));
        dataAnalysisTextView.setBackgroundColor(getResources().getColor(R.color.customBorderFill));
        xyPlotTextView.setBackgroundColor(getResources().getColor(R.color.customBorderFill));
    }

    public void onWindowFocusChanged() {
        RelativeLayout.LayoutParams lineChartParams = (RelativeLayout.LayoutParams) mChartLayout.getLayoutParams();
        RelativeLayout.LayoutParams frameLayoutParams = (RelativeLayout.LayoutParams) frameLayout.getLayoutParams();
        if (getResources().getBoolean(R.bool.isTablet)) {
            lineChartParams.height = height * 3 / 4;
            lineChartParams.width = width * 7 / 8;
            mChartLayout.setLayoutParams(lineChartParams);
            frameLayoutParams.height = height / 4;
            frameLayoutParams.width = width * 7 / 8;
            frameLayout.setLayoutParams(frameLayoutParams);
        } else {
            lineChartParams.height = height * 2 / 3;
            lineChartParams.width = width * 5 / 6;
            mChartLayout.setLayoutParams(lineChartParams);
            frameLayoutParams.height = height / 3;
            frameLayoutParams.width = width * 5 / 6;
            frameLayout.setLayoutParams(frameLayoutParams);
        }
    }

    protected void addFragment(@IdRes int containerViewId,
                               @NonNull Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .add(containerViewId, fragment, "ChannelParametersFragment").commit();
    }

    protected void replaceFragment(@IdRes int containerViewId,
                                   @NonNull Fragment fragment,
                                   @NonNull String fragmentTag) {
        getSupportFragmentManager().beginTransaction()
                .replace(containerViewId, fragment, fragmentTag).commit();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onDestroy() {
        monitor = false;
        if (captureTask != null) {
            captureTask.cancel(true);
        }
        if (recordTimer != null) {
            recordTimer.cancel();
            recordTimer = null;
        }
        super.onDestroy();
    }

    public void chartInit() {
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

        x1.setTextColor(Color.WHITE);
        x1.setDrawGridLines(true);
        x1.setAvoidFirstLastClipping(true);
        x1.setAxisMinimum(0f);
        x1.setAxisMaximum(875f);

        y1.setTextColor(Color.WHITE);
        y1.setAxisMaximum(16f);
        y1.setAxisMinimum(-16f);
        y1.setDrawGridLines(true);

        y2.setAxisMaximum(16f);
        y2.setAxisMinimum(-16f);
        y2.setTextColor(Color.WHITE);
        y2.setEnabled(true);
    }

    public void setXAxisScale(double timebase) {
        x1.setAxisMinimum(0);
        x1.setAxisMaximum((float) timebase);
        if (timebase == 875f)
            xAxisLabelUnit.setText("(μs)");
        else
            xAxisLabelUnit.setText("(ms)");

        this.timebase = timebase;
        mChart.fitScreen();
        mChart.invalidate();
    }

    public void setLeftYAxisScale(double upperLimit, double lowerLimit) {
        y1.setAxisMaximum((float) upperLimit);
        y1.setAxisMinimum((float) lowerLimit);
        if (upperLimit == 500f)
            leftYAxisLabelUnit.setText("(mV)");
        else
            leftYAxisLabelUnit.setText("(V)");
        mChart.fitScreen();
        mChart.invalidate();
    }

    public void setRightYAxisScale(double upperLimit, double lowerLimit) {
        y2.setAxisMaximum((float) upperLimit);
        y2.setAxisMinimum((float) lowerLimit);
        if (upperLimit == 500f)
            rightYAxisLabelUnit.setText("(mV)");
        else
            rightYAxisLabelUnit.setText("(V)");
        mChart.fitScreen();
        mChart.invalidate();
    }

    public void setLeftYAxisLabel(String leftYAxisInput) {
        leftYAxisLabel.setText(leftYAxisInput);
    }

    public void setXAxisLabel(String xAxisInput) {
        xAxisLabel.setText(xAxisInput);
    }

    private void setUpBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        final SharedPreferences settings = this.getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        Boolean isFirstTime = settings.getBoolean("OscilloscopeFirstTime", true);

        if (isFirstTime) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            parentLayout.setVisibility(View.VISIBLE);
            parentLayout.setAlpha(0.8f);
            arrowUpDown.setRotation(180);
            bottomSheetSlideText.setText(R.string.hide_guide_text);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("OscilloscopeFirstTime", false);
            editor.apply();
        } else {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            private Handler handler = new Handler();
            private Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }
            };

            @Override
            public void onStateChanged(@NonNull final View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_EXPANDED:
                        handler.removeCallbacks(runnable);
                        bottomSheetSlideText.setText(R.string.hide_guide_text);
                        break;

                    case BottomSheetBehavior.STATE_COLLAPSED:
                        handler.postDelayed(runnable, 2000);
                        break;

                    default:
                        handler.removeCallbacks(runnable);
                        bottomSheetSlideText.setText(R.string.show_guide_text);
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                Float value = (float) MathUtils.map((double) slideOffset, 0.0, 1.0, 0.0, 0.8);
                parentLayout.setVisibility(View.VISIBLE);
                parentLayout.setAlpha(value);
                arrowUpDown.setRotation(slideOffset * 180);
            }
        });
        gestureDetector = new GestureDetector(this, new SwipeGestureDetector(bottomSheetBehavior));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);                 //Gesture detector need this to transfer touch event to the gesture detector.
        return super.onTouchEvent(event);
    }

    public class CaptureTask extends AsyncTask<String, Void, Void> {
        private ArrayList<ArrayList<Entry>> entries = new ArrayList<>();
        private Integer noOfChannels;
        private String[] paramsChannels;

        @Override
        protected Void doInBackground(String... channels) {
            paramsChannels = channels;
            noOfChannels = channels.length;
            if (isInBuiltMicSelected) {
                noOfChannels--;
            }
            try {
                double[] xData;
                double[] yData;
                ArrayList<String[]> yDataString = new ArrayList<>();
                String[] xDataString = null;
                maxAmp = 0;
                for (int i = 0; i < noOfChannels; i++) {
                    entries.add(new ArrayList<>());
                    String channel = channels[i];
                    HashMap<String, double[]> data;
                    if (triggerChannel.equals(channel))
                        scienceLab.configureTrigger(channelIndexMap.get(channel), channel, trigger, null, null);
                    scienceLab.captureTraces(1, samples, timeGap, channel, isTriggerSelected, null);
                    data = scienceLab.fetchTrace(1);
                    Thread.sleep((long) (1000 * 10 * 1e-3));
                    xData = data.get("x");
                    yData = data.get("y");
                    int n = Math.min(xData.length, yData.length);
                    xDataString = new String[n];
                    yDataString.add(new String[n]);
                    Complex[] fftOut = null;
                    if (isFourierTransformSelected) {
                        Complex[] yComplex = new Complex[yData.length];
                        for (int j = 0; j < yData.length; j++) {
                            yComplex[j] = Complex.valueOf(yData[j]);
                        }
                        fftOut = fft(yComplex);
                    }
                    double factor = samples * timeGap * 1e-3;
                    maxFreq = (n / 2 - 1) / factor;
                    double mA = 0;
                    for (int j = 0; j < n; j++) {
                        xData[j] = xData[j] / ((timebase == 875) ? 1 : 1000);
                        if (!isFourierTransformSelected) {
                            entries.get(i).add(new Entry((float) xData[j], (float) yData[j]));
                        } else {
                            if (j < n / 2) {
                                float y = (float) fftOut[j].abs() / samples;
                                if (y > mA) {
                                    mA = y;
                                }
                                entries.get(i).add(new Entry((float) (j / factor), y));
                            }
                        }
                        xDataString[j] = String.valueOf(xData[j]);
                        yDataString.get(i)[j] = String.valueOf(yData[j]);
                    }
                    if (mA > maxAmp) {
                        maxAmp = mA;
                    }
                }
                if (isInBuiltMicSelected) {
                    noOfChannels++;
                    entries.add(new ArrayList<>());
                    if (audioJack == null) {
                        audioJack = new AudioJack("input");
                    }
                    short[] buffer = audioJack.read();
                    yDataString.add(new String[buffer.length]);
                    for (int i = 0; i < buffer.length; i++) {
                        float audioValue = (float) map(buffer[i], -32768, 32767, -3, 3);
                        yDataString.get(noOfChannels - 1)[i] = String.valueOf(audioValue);
                        entries.get(noOfChannels - 1).add(new Entry(i, audioValue));
                    }
                }
                if (isRecording) {
                    loggingXdata = String.join(" ", xDataString);
                    for (int i = 0; i < noOfChannels; i++) {
                        loggingYdata[i] = String.join(" ", yDataString.get(i));
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            logChannelData(paramsChannels);
                        }
                    });
                }

            } catch (NullPointerException e) {
                cancel(true);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!String.valueOf(ledImageView.getTag()).equals("green")) {
                ledImageView.setImageResource(R.drawable.green_led);
                ledImageView.setTag("green");
            }

            List<ILineDataSet> dataSets = new ArrayList<>();
            for (int i = 0; i < Math.min(entries.size(), paramsChannels.length); i++) {
                LineDataSet dataSet;
                dataSet = new LineDataSet(entries.get(i), paramsChannels[i]);
                dataSet.setDrawCircles(false);
                dataSet.setColor(channelColors[i]);
                dataSets.add(dataSet);

            }
            LineData data = new LineData(dataSets);
            if (!isFourierTransformSelected) {
                setXAxisScale(xAxisScale);
                setLeftYAxisScale(16, -16);
                setRightYAxisScale(16, -16);
            } else {
                setXAxisScale(maxFreq);
                setLeftYAxisScale(maxAmp, 0);
                setRightYAxisScale(maxAmp, 0);
            }
            mChart.setData(data);
            mChart.notifyDataSetChanged();
            mChart.invalidate();
            synchronized (lock) {
                lock.notify();
            }
        }
    }

    public class XYPlotTask extends AsyncTask<String, Void, Void> {
        String analogInput;
        float[] xFloatData = new float[1000];
        float[] yFloatData = new float[1000];

        @Override
        protected Void doInBackground(String... params) {
            HashMap<String, double[]> data;
            if ("CH2".equals(xyPlotXAxisChannel) || "CH2".equals(xyPlotYAxisChannel)) {
                analogInput = params[0];
                data = scienceLab.captureTwo(1000, 10, analogInput, false);
                double y1Data[] = data.get("y1");
                double y2Data[] = data.get("y2");
                if ("CH2".equals(xyPlotYAxisChannel)) {
                    for (int i = 0; i < y1Data.length; i++) {
                        xFloatData[i] = (float) y1Data[i];
                        yFloatData[i] = (float) y2Data[i];
                    }
                } else {
                    for (int i = 0; i < y1Data.length; i++) {
                        xFloatData[i] = (float) y2Data[i];
                        yFloatData[i] = (float) y1Data[i];
                    }
                }
            } else {
                data = scienceLab.captureFour(1000, 10, analogInput, false);
                double[] y1Data = data.get("y");
                double[] y3Data = data.get("y3");
                double[] y4Data = data.get("y4");
                switch (xyPlotXAxisChannel) {
                    case "CH1":
                        for (int i = 0; i < y1Data.length; i++) {
                            xFloatData[i] = (float) y1Data[i];
                        }
                        break;
                    case "CH3":
                        for (int i = 0; i < y3Data.length; i++) {
                            xFloatData[i] = (float) y3Data[i];
                        }
                        break;
                    case "MIC":
                        for (int i = 0; i < y4Data.length; i++) {
                            xFloatData[i] = (float) y4Data[i];
                        }
                        break;
                }

                switch (xyPlotYAxisChannel) {
                    case "CH1":
                        for (int i = 0; i < y1Data.length; i++) {
                            yFloatData[i] = (float) y1Data[i];
                        }
                        break;
                    case "CH3":
                        for (int i = 0; i < y3Data.length; i++) {
                            yFloatData[i] = (float) y3Data[i];
                        }
                        break;
                    case "MIC":
                        for (int i = 0; i < y4Data.length; i++) {
                            yFloatData[i] = (float) y4Data[i];
                        }
                        break;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            graph.plotData(xFloatData, yFloatData, 1);
        }
    }

    public Complex[] fft(Complex[] input) {
        Complex[] x = input;
        int n = x.length;

        if (n == 1) return new Complex[]{x[0]};

        if (n % 2 != 0) {
            x = Arrays.copyOfRange(x, 0, x.length - 1);
        }

        Complex[] halfArray = new Complex[n / 2];
        for (int k = 0; k < n / 2; k++) {
            halfArray[k] = x[2 * k];
        }
        Complex[] q = fft(halfArray);

        for (int k = 0; k < n / 2; k++) {
            halfArray[k] = x[2 * k + 1];
        }
        Complex[] r = fft(halfArray);

        Complex[] y = new Complex[n];
        for (int k = 0; k < n / 2; k++) {
            double kth = -2 * k * Math.PI / n;
            Complex wk = new Complex(Math.cos(kth), Math.sin(kth));
            y[k] = q[k].add(wk.multiply(r[k]));
            y[k + n / 2] = q[k].subtract(wk.multiply(r[k]));
        }
        return y;
    }

    public void recordSensorDataBlockID(SensorDataBlock block) {
        realm.beginTransaction();
        realm.copyToRealm(block);
        realm.commitTransaction();
    }

    public void recordSensorData(RealmObject sensorData) {
        realm.beginTransaction();
        realm.copyToRealm((OscilloscopeData) sensorData);
        realm.commitTransaction();
    }
}