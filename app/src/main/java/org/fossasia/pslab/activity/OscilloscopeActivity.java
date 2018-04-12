package org.fossasia.pslab.activity;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;

import android.view.View;
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

import org.fossasia.pslab.communication.AnalyticsClass;
import org.fossasia.pslab.communication.ScienceLab;
import org.fossasia.pslab.experimentsetup.electronicexperiments.OscillatorExperimentFragment;
import org.fossasia.pslab.experimentsetup.physicsexperiments.SpeedOfSoundFragment;
import org.fossasia.pslab.fragment.ChannelParametersFragment;
import org.fossasia.pslab.fragment.DataAnalysisFragment;
import org.fossasia.pslab.experimentsetup.electronicexperiments.DiodeClippingClampingExperiment;
import org.fossasia.pslab.experimentsetup.electronicexperiments.FullWaveRectifierFragment;
import org.fossasia.pslab.experimentsetup.electronicexperiments.HalfWaveRectifierFragment;
import org.fossasia.pslab.fragment.TimebaseTriggerFragment;
import org.fossasia.pslab.fragment.XYPlotFragment;
import org.fossasia.pslab.others.AudioJack;
import org.fossasia.pslab.others.Plot2D;
import org.fossasia.pslab.others.ScienceLabCommon;
import org.fossasia.pslab.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static org.fossasia.pslab.others.MathUtils.map;

/**
 * Created by viveksb007 on 10/5/17.
 */

public class OscilloscopeActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "Oscilloscope Activity";
    private ScienceLab scienceLab;
    @BindView(R.id.chart_os)
    public LineChart mChart;
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
    private int height;
    private int width;
    public int samples;
    public double timeGap;
    public double timebase;
    private XAxis x1;
    private YAxis y1;
    private YAxis y2;
    public boolean isCH1Selected;
    public boolean isCH2Selected;
    public boolean isCH3Selected;
    public boolean isMICSelected;
    public boolean isInBuiltMicSelected;
    public boolean isTriggerSelected;
    public boolean isFourierTransformSelected;
    public boolean isXYPlotSelected;
    public boolean sineFit;
    public boolean squareFit;
    public boolean viewIsClicked;
    public boolean isHalfWaveRectifierExperiment;
    public boolean isFullWaveRectifierExperiment;
    public boolean isDiodeClippingClampingExperiment;
    public boolean isAstableMultivibratorExperiment;
    public boolean isColpittsOscillatorExperiment;
    public boolean isPhaseShiftOscillatorExperiment;
    public boolean isWienBridgeOscillatorExperiment;
    public boolean isMonostableMultivibratorExperiment;
    public boolean runMonostableMultivibratorExperiment;
    public boolean isCH1FrequencyRequired;
    public boolean isCH2FrequencyRequired;
    public boolean runSpeedOfSoundExperiment;
    public boolean isSpeedOfSoundExperiment;
    private String leftYAxisInput;
    public String triggerChannel;
    public String curveFittingChannel1;
    public String curveFittingChannel2;
    public String xyPlotXAxisChannel;
    public String xyPlotYAxisChannel;
    public double trigger;
    Fragment channelParametersFragment;
    Fragment timebaseTriggerFragment;
    Fragment dataAnalysisFragment;
    Fragment xyPlotFragment;
    Fragment halfWaveRectifierFragment;
    Fragment fullWaveRectifierFragment;
    Fragment oscillatorExperimentFragment;
    Fragment diodeClippingClampingFragment;
    Fragment speedOfSoundFragment;
    private final Object lock = new Object();
    private CaptureTask captureTask;
    private CaptureTaskTwo captureTask2;
    private CaptureTaskThree captureTask3;
    private XYPlotTask xyPlotTask;
    private OscillatorTask oscillatorTask;
    private SpeedOfSoundTask speedOfSoundTask;
    @BindView(R.id.imageView_led_os)
    ImageView ledImageView;
    public Plot2D graph;
    private AudioJack audioJack = null;
    private AnalyticsClass analyticsClass;
    private CaptureAudioBuffer captureAudioBuffer;
    private Thread monitorThread;
    private volatile boolean monitor = true;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oscilloscope);
        ButterKnife.bind(this);

        scienceLab = ScienceLabCommon.scienceLab;
        x1 = mChart.getXAxis();
        y1 = mChart.getAxisLeft();
        y2 = mChart.getAxisRight();
        triggerChannel = "CH1";
        trigger = 0;
        timebase = 875;
        samples = 512;
        timeGap = 2;
        sineFit = true;
        squareFit = false;
        graph = new Plot2D(this, new float[]{}, new float[]{}, 1);
        curveFittingChannel1 = "None";
        curveFittingChannel2 = "None";
        xyPlotXAxisChannel = "CH1";
        xyPlotYAxisChannel = "CH2";
        viewIsClicked = false;
        isHalfWaveRectifierExperiment = false;
        isFullWaveRectifierExperiment = false;
        isAstableMultivibratorExperiment = false;
        analyticsClass = new AnalyticsClass();
        isCH1FrequencyRequired = false;
        isCH2FrequencyRequired = false;
        isColpittsOscillatorExperiment = false;
        isPhaseShiftOscillatorExperiment = false;
        isWienBridgeOscillatorExperiment = false;
        isMonostableMultivibratorExperiment = false;
        runMonostableMultivibratorExperiment = false;
        isSpeedOfSoundExperiment = false;
        runSpeedOfSoundExperiment = false;

        //int freq = scienceLab.setSine1(3000);
        //Log.v("SIN Fre", "" + freq);
        //scienceLab.setW1(3000, "sine");

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;

        Bundle extras = getIntent().getExtras();
        if ("Half Wave Rectifier".equals(extras.getString("who"))) {
            isHalfWaveRectifierExperiment = true;
            if (scienceLab.isConnected())
                scienceLab.setSine1(5000);
        } else if ("Full Wave Rectifier".equals(extras.getString("who"))) {
            isFullWaveRectifierExperiment = true;
            if (scienceLab.isConnected()) {
                scienceLab.setWaves(5000, 180, 5000);
            }
        } else if ("Diode Clipping Clamping".equals(extras.getString("who"))) {
            isDiodeClippingClampingExperiment = true;
            if (scienceLab.isConnected()) {
                scienceLab.setSine1(5000);
            }
        } else if ("Astable Multivibrator".equals(extras.getString("who"))) {
            isAstableMultivibratorExperiment = true;
        } else if ("Colpitts Oscillator".equals(extras.getString("who"))) {
            isColpittsOscillatorExperiment = true;
        } else if ("Phase Shift Oscillator".equals(extras.getString("who"))) {
            isPhaseShiftOscillatorExperiment = true;
        } else if ("Wien Bridge Oscillator".equals(extras.getString("who"))) {
            isWienBridgeOscillatorExperiment = true;
        } else if ("Monostable Multivibrator".equals(extras.getString("who"))) {
            isMonostableMultivibratorExperiment = true;
        } else if ("Speed of Sound".equals(extras.getString("who"))) {
            isSpeedOfSoundExperiment = true;
        }

        onWindowFocusChanged();

        channelParametersFragment = new ChannelParametersFragment();
        timebaseTriggerFragment = new TimebaseTriggerFragment();
        dataAnalysisFragment = new DataAnalysisFragment();
        xyPlotFragment = new XYPlotFragment();
        halfWaveRectifierFragment = new HalfWaveRectifierFragment();
        fullWaveRectifierFragment = new FullWaveRectifierFragment();
        diodeClippingClampingFragment = new DiodeClippingClampingExperiment();
        oscillatorExperimentFragment = new OscillatorExperimentFragment();
        speedOfSoundFragment = new SpeedOfSoundFragment();

        if (findViewById(R.id.layout_dock_os2) != null) {
            if (isHalfWaveRectifierExperiment) {
                addFragment(R.id.layout_dock_os2, halfWaveRectifierFragment, "HalfWaveFragment");
            } else if (isFullWaveRectifierExperiment) {
                addFragment(R.id.layout_dock_os2, fullWaveRectifierFragment, "FullWaveFragment");
            } else if (isDiodeClippingClampingExperiment) {
                addFragment(R.id.layout_dock_os2, diodeClippingClampingFragment, "DiodeClippingClampingFragment");
            } else if (isAstableMultivibratorExperiment || isColpittsOscillatorExperiment || isPhaseShiftOscillatorExperiment || isWienBridgeOscillatorExperiment || isMonostableMultivibratorExperiment) {
                addFragment(R.id.layout_dock_os2, oscillatorExperimentFragment, "OscillatorFragment");
            } else if (isSpeedOfSoundExperiment) {
                addFragment(R.id.layout_dock_os2, speedOfSoundFragment, "SpeedOfSoundFragment");
            } else {
                addFragment(R.id.layout_dock_os2, channelParametersFragment, "ChannelParametersFragment");
            }
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
                    if (scienceLab.isConnected() && isCH1Selected && !isCH2Selected && !isCH3Selected && !isMICSelected && !isXYPlotSelected) {
                        captureTask = new CaptureTask();
                        captureTask.execute("CH1");
                        synchronized (lock) {
                            try {
                                lock.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                    }

                    if (scienceLab.isConnected() && isCH2Selected && !isCH1Selected && !isCH3Selected && !isMICSelected && !isXYPlotSelected) {
                        captureTask = new CaptureTask();
                        captureTask.execute("CH2");
                        synchronized (lock) {
                            try {
                                lock.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                    }

                    if (scienceLab.isConnected() && isCH3Selected && !isCH1Selected && !isCH2Selected && !isMICSelected && !isXYPlotSelected) {
                        {
                            captureTask = new CaptureTask();
                            captureTask.execute("CH3");
                            synchronized (lock) {
                                try {
                                    lock.wait();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                    if (scienceLab.isConnected() && isMICSelected && !isCH1Selected && !isCH2Selected && !isCH3Selected && !isXYPlotSelected) {
                        captureTask = new CaptureTask();
                        captureTask.execute("MIC");
                        synchronized (lock) {
                            try {
                                lock.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                    }

                    if (scienceLab.isConnected() && isCH1Selected && isCH2Selected && !isCH3Selected && !isMICSelected && !isXYPlotSelected) {
                        captureTask2 = new CaptureTaskTwo();
                        captureTask2.execute("CH1");
                        synchronized (lock) {
                            try {
                                lock.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if (scienceLab.isConnected() && isCH3Selected && isCH2Selected && !isCH1Selected && !isMICSelected && !isXYPlotSelected) {
                        captureTask2 = new CaptureTaskTwo();
                        captureTask2.execute("CH3");
                        synchronized (lock) {
                            try {
                                lock.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if (scienceLab.isConnected() && isMICSelected && isCH2Selected && !isCH3Selected && !isCH1Selected && !isXYPlotSelected) {
                        captureTask2 = new CaptureTaskTwo();
                        captureTask2.execute("MIC");
                        synchronized (lock) {
                            try {
                                lock.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if (scienceLab.isConnected() && isCH1Selected && isCH2Selected && isCH3Selected && isMICSelected && !isXYPlotSelected) {
                        captureTask3 = new CaptureTaskThree();
                        captureTask3.execute("CH1");
                        synchronized (lock) {
                            try {
                                lock.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if (scienceLab.isConnected() && (isHalfWaveRectifierExperiment || isFullWaveRectifierExperiment || isDiodeClippingClampingExperiment)) {
                        captureTask2 = new CaptureTaskTwo();
                        captureTask2.execute("CH1");
                        synchronized (lock) {
                            try {
                                lock.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if (scienceLab.isConnected() && (isAstableMultivibratorExperiment || isColpittsOscillatorExperiment || isPhaseShiftOscillatorExperiment || isWienBridgeOscillatorExperiment)) {
                        oscillatorTask = new OscillatorTask();
                        oscillatorTask.execute("CH1");
                        synchronized (lock) {
                            try {
                                lock.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if (scienceLab.isConnected() && isMonostableMultivibratorExperiment && runMonostableMultivibratorExperiment) {
                        oscillatorTask = new OscillatorTask();
                        oscillatorTask.execute("CH1");
                        synchronized (lock) {
                            try {
                                lock.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if (!scienceLab.isConnected() || (!isCH1Selected && !isCH2Selected && !isCH3Selected && !isMICSelected)) {
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
                        if (xyPlotXAxisChannel.equals("CH2"))
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

                    if (isInBuiltMicSelected) {
                        if (audioJack == null)
                            audioJack = new AudioJack("input");
                        captureAudioBuffer = new CaptureAudioBuffer(audioJack);
                        captureAudioBuffer.execute();
                        synchronized (lock) {
                            try {
                                lock.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        if (audioJack != null) {
                            audioJack.release();
                            audioJack = null;
                        }
                    }

                    if (scienceLab.isConnected() && isSpeedOfSoundExperiment && runSpeedOfSoundExperiment) {
                        speedOfSoundTask = new SpeedOfSoundTask();
                        speedOfSoundTask.execute();
                        synchronized (lock) {
                            try {
                                lock.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                }
            }
        };

        // if (scienceLab.isConnected())
        monitorThread = new Thread(runnable);
        monitorThread.start();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.button_channel_parameters_os:
                replaceFragment(R.id.layout_dock_os2, channelParametersFragment, "ChannelParametersFragment");
                break;

            case R.id.tv_channel_parameters_os:
                replaceFragment(R.id.layout_dock_os2, channelParametersFragment, "ChannelParametersFragment");
                break;

            case R.id.button_timebase_os:
                replaceFragment(R.id.layout_dock_os2, timebaseTriggerFragment, "TimebaseTiggerFragment");
                break;

            case R.id.tv_timebase_tigger_os:
                replaceFragment(R.id.layout_dock_os2, timebaseTriggerFragment, "TimebaseTiggerFragment");
                break;

            case R.id.button_data_analysis_os:
                replaceFragment(R.id.layout_dock_os2, dataAnalysisFragment, "DataAnalysisFragment");
                break;

            case R.id.tv_data_analysis_os:
                replaceFragment(R.id.layout_dock_os2, dataAnalysisFragment, "DataAnalysisFragment");
                break;

            case R.id.button_xy_plot_os:
                replaceFragment(R.id.layout_dock_os2, xyPlotFragment, "XYPlotFragment");
                break;

            case R.id.tv_xy_plot_os:
                replaceFragment(R.id.layout_dock_os2, xyPlotFragment, "XYPlotFragment");
                break;
        }
    }

    public void onWindowFocusChanged() {
        boolean tabletSize = getResources().getBoolean(R.bool.isTablet);
        //dynamic placing the layouts
        if (isHalfWaveRectifierExperiment || isDiodeClippingClampingExperiment || isAstableMultivibratorExperiment || isSpeedOfSoundExperiment) {
            linearLayout.setVisibility(View.INVISIBLE);
            RelativeLayout.LayoutParams lineChartParams = (RelativeLayout.LayoutParams) mChartLayout.getLayoutParams();
            lineChartParams.height = height * 3 / 4;
            lineChartParams.width = width;
            RelativeLayout.LayoutParams frameLayoutParams = (RelativeLayout.LayoutParams) frameLayout.getLayoutParams();
            frameLayoutParams.height = height / 4;
            frameLayoutParams.width = width;
        } else if (isFullWaveRectifierExperiment || isColpittsOscillatorExperiment || isPhaseShiftOscillatorExperiment || isWienBridgeOscillatorExperiment || isMonostableMultivibratorExperiment) {
            linearLayout.setVisibility(View.INVISIBLE);
            RelativeLayout.LayoutParams lineChartParams = (RelativeLayout.LayoutParams) mChartLayout.getLayoutParams();
            lineChartParams.height = height * 5 / 6;
            lineChartParams.width = width;
            RelativeLayout.LayoutParams frameLayoutParams = (RelativeLayout.LayoutParams) frameLayout.getLayoutParams();
            frameLayoutParams.height = height / 6;
            frameLayoutParams.width = width;
        } else {

            if (tabletSize) {
                RelativeLayout.LayoutParams lineChartParams = (RelativeLayout.LayoutParams) mChartLayout.getLayoutParams();
                lineChartParams.height = height * 3 / 4;
                lineChartParams.width = width * 7 / 8;
                mChartLayout.setLayoutParams(lineChartParams);
                RelativeLayout.LayoutParams frameLayoutParams = (RelativeLayout.LayoutParams) frameLayout.getLayoutParams();
                frameLayoutParams.height = height / 4;
                frameLayoutParams.width = width * 7 / 8;
                frameLayout.setLayoutParams(frameLayoutParams);
            } else {
                RelativeLayout.LayoutParams lineChartParams = (RelativeLayout.LayoutParams) mChartLayout.getLayoutParams();
                lineChartParams.height = height * 2 / 3;
                lineChartParams.width = width * 5 / 6;
                mChartLayout.setLayoutParams(lineChartParams);
                RelativeLayout.LayoutParams frameLayoutParams = (RelativeLayout.LayoutParams) frameLayout.getLayoutParams();
                frameLayoutParams.height = height / 3;
                frameLayoutParams.width = width * 5 / 6;
                frameLayout.setLayoutParams(frameLayoutParams);
            }
        }
    }

    protected void addFragment(@IdRes int containerViewId,
                               @NonNull Fragment fragment,
                               @NonNull String fragmentTag) {
        getSupportFragmentManager()
                .beginTransaction()
                .add(containerViewId, fragment, fragmentTag)
                .commit();
    }

    protected void replaceFragment(@IdRes int containerViewId,
                                   @NonNull Fragment fragment,
                                   @NonNull String fragmentTag) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(containerViewId, fragment, fragmentTag)
                .commit();
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
        if (captureTask2 != null) {
            captureTask2.cancel(true);
        }
        if (captureTask3 != null) {
            captureTask3.cancel(true);
        }
        if (oscillatorTask != null) {
            oscillatorTask.cancel(true);
        }
        if (captureAudioBuffer != null) {
            captureAudioBuffer.cancel(true);
            if (audioJack != null) {
                audioJack.release();
            }
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

    public void setRightYAxisLabel(String rightYAxisInput) {
        rightYAxisLabel.setText(rightYAxisInput);
    }

    public void setXAxisLabel(String xAxisInput) {
        xAxisLabel.setText(xAxisInput);
    }

    public class CaptureTask extends AsyncTask<String, Void, Void> {
        ArrayList<Entry> entries;
        String analogInput;

        @Override
        protected Void doInBackground(String... params) {
            try {
                analogInput = params[0];
                //no. of samples and timeGap still need to be determined
                if (isTriggerSelected) {
                    scienceLab.configureTrigger(0, analogInput, trigger, null, null);
                    scienceLab.captureTraces(1, samples, timeGap, analogInput, true, null);
                } else {
                    scienceLab.captureTraces(1, samples, timeGap, analogInput, false, null);
                }
                Log.v("Sleep Time", "" + (1024 * 10 * 1e-3));
                Thread.sleep((long) (1000 * 10 * 1e-3));
                HashMap<String, double[]> data = scienceLab.fetchTrace(1); //fetching data

                double[] xData = data.get("x");
                double[] yData = data.get("y");
                //Log.v("XDATA", Arrays.toString(xData));
                //Log.v("YDATA", Arrays.toString(yData));

                entries = new ArrayList<>();
                if (timebase == 875) {
                    for (int i = 0; i < xData.length; i++) {
                        entries.add(new Entry((float) xData[i], (float) yData[i]));
                    }
                } else {
                    for (int i = 0; i < xData.length; i++) {
                        entries.add(new Entry((float) xData[i] / 1000, (float) yData[i]));
                    }
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

            LineDataSet dataSet = new LineDataSet(entries, analogInput);
            LineData lineData = new LineData(dataSet);
            dataSet.setDrawCircles(false);
            mChart.setData(lineData);
            mChart.notifyDataSetChanged();
            mChart.invalidate();

            synchronized (lock) {
                lock.notify();
            }

        }
    }

    public class CaptureTaskTwo extends AsyncTask<String, Void, Void> {
        ArrayList<Entry> entries1;
        ArrayList<Entry> entries2;
        String analogInput;

        @Override
        protected Void doInBackground(String... params) {
            try {
                analogInput = params[0];
                //no. of samples and timeGap still need to be determined
                HashMap<String, double[]> data;
                if (isTriggerSelected && (triggerChannel.equals("CH1") || triggerChannel.equals("CH2"))) {
                    if (triggerChannel.equals("CH1"))
                        scienceLab.configureTrigger(0, analogInput, trigger, null, null);
                    else if (triggerChannel.equals("CH2"))
                        scienceLab.configureTrigger(1, "CH2", trigger, null, null);
                    data = scienceLab.captureTwo(samples, timeGap, analogInput, true);
                } else {
                    data = scienceLab.captureTwo(samples, timeGap, analogInput, false);
                }
                double[] xData = data.get("x");
                double[] y1Data = data.get("y1");
                double[] y2Data = data.get("y2");

                entries1 = new ArrayList<>();
                entries2 = new ArrayList<>();
                if (timebase == 875) {
                    for (int i = 0; i < xData.length; i++) {
                        entries1.add(new Entry((float) xData[i], (float) y1Data[i]));
                        entries2.add(new Entry((float) xData[i], (float) y2Data[i]));
                    }
                } else {
                    for (int i = 0; i < xData.length; i++) {
                        entries1.add(new Entry((float) xData[i] / 1000, (float) y1Data[i]));
                        entries2.add(new Entry((float) xData[i] / 1000, (float) y2Data[i]));
                    }
                }
            } catch (NullPointerException e) {
                cancel(true);
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

            LineDataSet dataSet1;
            LineDataSet dataSet2;

            if (isHalfWaveRectifierExperiment || isFullWaveRectifierExperiment || isDiodeClippingClampingExperiment) {
                dataSet1 = new LineDataSet(entries1, analogInput + " INPUT");
                dataSet2 = new LineDataSet(entries2, "CH2" + " OUTPUT");
                dataSet1.setColor(Color.GREEN);
                dataSet2.setColor(Color.RED);

            } else {
                dataSet1 = new LineDataSet(entries1, analogInput);
                dataSet2 = new LineDataSet(entries2, "CH2");
                dataSet2.setColor(Color.GREEN);
            }


            dataSet1.setDrawCircles(false);
            dataSet2.setDrawCircles(false);

            List<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(dataSet1);
            dataSets.add(dataSet2);

            LineData data = new LineData(dataSets);
            mChart.setData(data);
            mChart.notifyDataSetChanged();
            mChart.invalidate();
            synchronized (lock) {
                lock.notify();
            }
        }
    }

    public class CaptureTaskThree extends AsyncTask<String, Void, Void> {
        ArrayList<Entry> entries1;
        ArrayList<Entry> entries2;
        ArrayList<Entry> entries3;
        ArrayList<Entry> entries4;
        String analogInput;

        @Override
        protected Void doInBackground(String... params) {
            try {
                //no. of samples and timeGap still need to be determined
                analogInput = params[0];
                HashMap<String, double[]> data;

                if (isTriggerSelected) {
                    switch (triggerChannel) {
                        case "CH1":
                            scienceLab.configureTrigger(0, analogInput, trigger, null, null);
                            break;
                        case "CH2":
                            scienceLab.configureTrigger(1, analogInput, trigger, null, null);
                            break;
                        case "CH3":
                            scienceLab.configureTrigger(2, analogInput, trigger, null, null);
                            break;
                        case "MIC":
                            scienceLab.configureTrigger(3, analogInput, trigger, null, null);
                            break;
                    }

                    data = scienceLab.captureFour(samples, timeGap, analogInput, true);
                } else {
                    data = scienceLab.captureFour(samples, timeGap, analogInput, false);
                }
                double[] xData = data.get("x");
                double[] y1Data = data.get("y");
                double[] y2Data = data.get("y2");
                double[] y3Data = data.get("y3");
                double[] y4Data = data.get("y4");

                entries1 = new ArrayList<>();
                entries2 = new ArrayList<>();
                entries3 = new ArrayList<>();
                entries4 = new ArrayList<>();
                if (timebase == 875) {
                    for (int i = 0; i < xData.length; i++) {
                        entries1.add(new Entry((float) xData[i], (float) y1Data[i]));
                        entries2.add(new Entry((float) xData[i], (float) y2Data[i]));
                        entries3.add(new Entry((float) xData[i], (float) y3Data[i]));
                        entries4.add(new Entry((float) xData[i], (float) y4Data[i]));
                    }
                } else {
                    for (int i = 0; i < xData.length; i++) {
                        entries1.add(new Entry((float) xData[i] / 1000, (float) y1Data[i]));
                        entries2.add(new Entry((float) xData[i] / 1000, (float) y2Data[i]));
                        entries3.add(new Entry((float) xData[i] / 1000, (float) y3Data[i]));
                        entries4.add(new Entry((float) xData[i] / 1000, (float) y4Data[i]));
                    }
                }
            } catch (NullPointerException e) {
                cancel(true);
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

            LineDataSet dataSet1 = new LineDataSet(entries1, "CH1");
            LineDataSet dataSet2 = new LineDataSet(entries2, "CH2");
            LineDataSet dataSet3 = new LineDataSet(entries3, "CH3");
            LineDataSet dataSet4 = new LineDataSet(entries4, "MIC");

            dataSet1.setColor(Color.BLUE);
            dataSet2.setColor(Color.GREEN);
            dataSet3.setColor(Color.RED);
            dataSet4.setColor(Color.YELLOW);
            dataSet1.setDrawCircles(false);
            dataSet2.setDrawCircles(false);
            dataSet3.setDrawCircles(false);
            dataSet4.setDrawCircles(false);

            List<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(dataSet1);
            dataSets.add(dataSet2);
            dataSets.add(dataSet3);
            dataSets.add(dataSet4);

            LineData data = new LineData(dataSets);
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

    public class CaptureAudioBuffer extends AsyncTask<Void, Void, Void> {

        private AudioJack audioJack;
        private short[] buffer;
        private List<Entry> entries;

        public CaptureAudioBuffer(AudioJack audioJack) {
            this.audioJack = audioJack;
        }

        @Override
        protected Void doInBackground(Void... params) {
            buffer = audioJack.read();
            entries = new ArrayList<>();
            for (int i = 0; i < buffer.length; i++) {
                entries.add(new Entry(i, (float) map(buffer[i], -32768, 32767, -3, 3)));
            }
            //Log.v("AudioBuffer", Arrays.toString(buffer));
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            // Update chart
            LineDataSet lineDataSet = new LineDataSet(entries, "Audio Data");
            lineDataSet.setColor(Color.WHITE);
            lineDataSet.setDrawCircles(false);
            mChart.setData(new LineData(lineDataSet));
            mChart.notifyDataSetChanged();
            mChart.invalidate();
            synchronized (lock) {
                lock.notify();
            }
        }
    }

    public class OscillatorTask extends AsyncTask<String, Void, Void> {
        ArrayList<Entry> entries1;
        ArrayList<Entry> entries2;
        ArrayList<Entry> entries3;
        String analogInput;
        double[] xData;
        double[] y1Data;
        double[] y2Data;
        double[] y3Data;
        double frequencyCH1;
        double frequencyCH2;
        OscillatorExperimentFragment fragment;

        @Override
        protected Void doInBackground(String... params) {
            try {
                analogInput = params[0];
                HashMap<String, double[]> data;
                if (isAstableMultivibratorExperiment || isMonostableMultivibratorExperiment) {
                    data = scienceLab.captureTwo(128, 10, analogInput, false);
                    xData = data.get("x");
                    y1Data = data.get("y1");
                    y2Data = data.get("y2");

                    entries1 = new ArrayList<>();
                    entries2 = new ArrayList<>();
                    for (int i = 0; i < xData.length; i++) {
                        entries1.add(new Entry((float) xData[i], (float) y1Data[i]));
                        entries2.add(new Entry((float) xData[i], (float) y2Data[i]));
                    }
                    if (runMonostableMultivibratorExperiment)
                        runMonostableMultivibratorExperiment = false;

                } else if (isColpittsOscillatorExperiment || isPhaseShiftOscillatorExperiment) {
                    scienceLab.captureTraces(1, 128, 10, analogInput, false, null);
                    Thread.sleep((long) (128 * 10 * 1e-3));
                    data = scienceLab.fetchTrace(1); //fetching data
                    xData = data.get("x");
                    y1Data = data.get("y");
                    entries1 = new ArrayList<>();
                    for (int i = 0; i < xData.length; i++) {
                        entries1.add(new Entry((float) xData[i], (float) y1Data[i]));
                    }
                } else if (isWienBridgeOscillatorExperiment) {
                    data = scienceLab.captureFour(128, 10, "CH1", false);
                    xData = data.get("x");
                    y1Data = data.get("y");
                    y2Data = data.get("y2");
                    y3Data = data.get("y3");
                    entries1 = new ArrayList<>();
                    entries2 = new ArrayList<>();
                    entries3 = new ArrayList<>();
                    for (int i = 0; i < xData.length; i++) {
                        entries1.add(new Entry((float) xData[i], (float) y1Data[i]));
                        entries2.add(new Entry((float) xData[i], (float) y2Data[i]));
                        entries3.add(new Entry((float) xData[i], (float) y3Data[i]));
                    }
                }
                if (isCH1FrequencyRequired)
                    frequencyCH1 = analyticsClass.sineFit(xData, y1Data)[1];

                if (isCH2FrequencyRequired)
                    frequencyCH2 = analyticsClass.sineFit(xData, y2Data)[1];

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
            fragment = (OscillatorExperimentFragment) getSupportFragmentManager().findFragmentById(oscillatorExperimentFragment.getId());
            List<ILineDataSet> dataSets = new ArrayList<>();
            if (isCH1FrequencyRequired) {
                if (frequencyCH1 >= 0)
                    fragment.resultCH1Frequency.setText(String.format("%sHz", new DecimalFormat("#.##").format(frequencyCH1)));
                else
                    fragment.resultCH1Frequency.setText(R.string.fit_failed);
                isCH1FrequencyRequired = false;
            }
            if (isCH2FrequencyRequired) {
                if (frequencyCH1 >= 0)
                    fragment.resultCH2Frequency.setText(new DecimalFormat("#.##").format(frequencyCH2));
                else
                    fragment.resultCH2Frequency.setText(R.string.fit_failed);
                isCH2FrequencyRequired = false;
            }
            if (isAstableMultivibratorExperiment || isMonostableMultivibratorExperiment) {
                LineDataSet dataSet1;
                LineDataSet dataSet2;
                if (isMonostableMultivibratorExperiment) {
                    dataSet1 = new LineDataSet(entries1, "Pulse Output (CH2)");
                    dataSet2 = new LineDataSet(entries2, "Trigger Pulse (CH1)");
                    fragment.resultCH1Frequency.setText(R.string.done);
                } else {
                    dataSet1 = new LineDataSet(entries1, "CH1 INPUT");
                    dataSet2 = new LineDataSet(entries2, "CH2 OUTPUT");
                }
                dataSet1.setColor(Color.GREEN);
                dataSet2.setColor(Color.RED);
                dataSet1.setDrawCircles(false);
                dataSet2.setDrawCircles(false);
                dataSets.add(dataSet1);
                dataSets.add(dataSet2);
            } else if (isColpittsOscillatorExperiment || isPhaseShiftOscillatorExperiment) {
                LineDataSet dataSet1;
                dataSet1 = new LineDataSet(entries1, "CH1 INPUT");
                dataSet1.setColor(Color.GREEN);
                dataSet1.setDrawCircles(false);
                dataSets.add(dataSet1);
            } else if (isWienBridgeOscillatorExperiment) {
                LineDataSet dataSet1;
                LineDataSet dataSet2;
                LineDataSet dataSet3;
                dataSet1 = new LineDataSet(entries1, "Oscillator Output(CH1)");
                dataSet2 = new LineDataSet(entries2, "Monitor #1(CH2)");
                dataSet3 = new LineDataSet(entries3, "Monitor #2(CH3)");
                dataSet1.setColor(Color.GREEN);
                dataSet2.setColor(Color.RED);
                dataSet3.setColor(Color.YELLOW);
                dataSet1.setDrawCircles(false);
                dataSet2.setDrawCircles(false);
                dataSet3.setDrawCircles(false);
                dataSets.add(dataSet1);
                dataSets.add(dataSet2);
                dataSets.add(dataSet3);
            }

            LineData data = new LineData(dataSets);
            mChart.setData(data);
            mChart.notifyDataSetChanged();
            mChart.invalidate();
            synchronized (lock) {
                lock.notify();
            }
        }
    }

    public class SpeedOfSoundTask extends AsyncTask<Void, Void, Void> {
        ArrayList<Entry> entries;
        SpeedOfSoundFragment fragment;

        @Override
        protected Void doInBackground(Void... params) {
            scienceLab.captureFullSpeed("CH1", 128, 10, new ArrayList<>(Collections.singletonList("FIRE_PULSES")), 50);
            HashMap<String, double[]> data = scienceLab.fetchTrace(1); //fetching data

            double[] xData = data.get("x");
            double[] yData = data.get("y");
            //Log.v("XDATA", Arrays.toString(xData));
            //Log.v("YDATA", Arrays.toString(yData));
            entries = new ArrayList<>();
            for (int i = 0; i < xData.length; i++) {
                entries.add(new Entry((float) xData[i], (float) yData[i]));
            }
            runSpeedOfSoundExperiment = false;
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!String.valueOf(ledImageView.getTag()).equals("green")) {
                ledImageView.setImageResource(R.drawable.green_led);
                ledImageView.setTag("green");
            }

            fragment = (SpeedOfSoundFragment) getSupportFragmentManager().findFragmentById(speedOfSoundFragment.getId());
            fragment.resultTextView.setText(R.string.done);
            LineDataSet dataSet = new LineDataSet(entries, "RAMP in CH1");
            LineData lineData = new LineData(dataSet);
            dataSet.setDrawCircles(false);
            mChart.setData(lineData);
            mChart.notifyDataSetChanged();
            mChart.invalidate();

            synchronized (lock) {
                lock.notify();
            }
        }
    }
}