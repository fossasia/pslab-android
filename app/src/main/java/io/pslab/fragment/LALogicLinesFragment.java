package io.pslab.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import butterknife.ButterKnife;
import in.goodiebag.carouselpicker.CarouselPicker;
import io.pslab.DataFormatter;
import io.pslab.R;
import io.pslab.activity.DataLoggerActivity;
import io.pslab.activity.LogicalAnalyzerActivity;
import io.pslab.communication.ScienceLab;
import io.pslab.communication.digitalChannel.DigitalChannel;
import io.pslab.models.LogicAnalyzerData;
import io.pslab.models.SensorDataBlock;
import io.pslab.others.CSVDataLine;
import io.pslab.others.CSVLogger;
import io.pslab.others.CustomSnackBar;
import io.pslab.others.GPSLogger;
import io.pslab.others.LocalDataLog;
import io.pslab.others.LogicAnalyzerAxisFormatter;
import io.pslab.others.ScienceLabCommon;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;

/**
 * Created by viveksb007 on 9/6/17.
 */

public class LALogicLinesFragment extends Fragment {

    private static final int EVERY_EDGE = 1;
    private static final int DISABLED = 0;
    private static final int EVERY_FOURTH_RISING_EDGE = 4;
    private static final int EVERY_RISING_EDGE = 3;
    private static final int EVERY_FALLING_EDGE = 2;

    private static final CSVDataLine CSV_HEADER =
            new CSVDataLine()
                    .add("Timestamp")
                    .add("DateTime")
                    .add("Channel")
                    .add("ChannelMode")
                    .add("xData")
                    .add("yData")
                    .add("lat")
                    .add("lon");

    private final Object lock = new Object();
    List<Entry> tempInput;
    DigitalChannel digitalChannel;
    ArrayList<DigitalChannel> digitalChannelArray;
    List<ILineDataSet> dataSets;

    // Graph Plot
    private CarouselPicker carouselPicker;
    private LinearLayout llChannel1, llChannel2, llChannel3, llChannel4;
    private Spinner channelSelectSpinner1, channelSelectSpinner2, channelSelectSpinner3, channelSelectSpinner4;
    private Spinner edgeSelectSpinner1, edgeSelectSpinner2, edgeSelectSpinner3, edgeSelectSpinner4;
    private Button analyze_button;
    private ProgressBar progressBar;
    private CaptureOne captureOne;
    private CaptureTwo captureTwo;
    private CaptureThree captureThree;
    private CaptureFour captureFour;
    private int currentChannel = 0;
    private int[] colors = new int[]{Color.MAGENTA, Color.GREEN, Color.CYAN, Color.YELLOW};
    private OnChartValueSelectedListener listener;

    private Activity activity;
    private int channelMode;
    private ScienceLab scienceLab;
    private LineChart logicLinesChart;
    private ArrayList<String> channelNames = new ArrayList<>();
    private ArrayList<String> edgesNames = new ArrayList<>();
    private TextView tvTimeUnit, xCoordinateText;
    private Realm realm;
    private GPSLogger gpsLogger;
    private CSVLogger csvLogger;
    private ArrayList<String> recordXAxis;
    private ArrayList<String> recordYAxis;
    private ArrayList<Integer> recordChannelMode;
    private String[] channels = new String[]{"LA1", "LA2", "LA3", "LA4"};
    private HashMap<String, Integer> channelMap;
    private ArrayList<Spinner> channelSelectSpinners;
    private ArrayList<Spinner> edgeSelectSpinners;
    private View rootView;

    public static LALogicLinesFragment newInstance(Activity activity) {
        LALogicLinesFragment laLogicLinesFragment = new LALogicLinesFragment();
        laLogicLinesFragment.activity = activity;
        return laLogicLinesFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(getActivity());
        scienceLab = ScienceLabCommon.scienceLab;
        realm = LocalDataLog.with().getRealm();
        gpsLogger = new GPSLogger(getContext(), (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE));
        csvLogger = new CSVLogger(getString(R.string.logical_analyzer));
        recordXAxis = new ArrayList<>();
        recordYAxis = new ArrayList<>();
        recordChannelMode = new ArrayList<>();
        channelSelectSpinners = new ArrayList<>();
        edgeSelectSpinners = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.logic_analyzer_logic_lines, container, false);

        // Heading
        tvTimeUnit = rootView.findViewById(R.id.la_tv_time_unit);
        tvTimeUnit.setText(getString(R.string.time_unit_la));

        // Carousel View
        carouselPicker = rootView.findViewById(R.id.carouselPicker);
        llChannel1 = rootView.findViewById(R.id.ll_chart_channel_1);
        llChannel1.setVisibility(View.VISIBLE);
        llChannel2 = rootView.findViewById(R.id.ll_chart_channel_2);
        llChannel2.setVisibility(View.GONE);
        llChannel3 = rootView.findViewById(R.id.ll_chart_channel_3);
        llChannel3.setVisibility(View.GONE);
        llChannel4 = rootView.findViewById(R.id.ll_chart_channel_4);
        llChannel4.setVisibility(View.GONE);
        channelSelectSpinner1 = rootView.findViewById(R.id.channel_select_spinner_1);
        channelSelectSpinner2 = rootView.findViewById(R.id.channel_select_spinner_2);
        channelSelectSpinner3 = rootView.findViewById(R.id.channel_select_spinner_3);
        channelSelectSpinner4 = rootView.findViewById(R.id.channel_select_spinner_4);
        edgeSelectSpinner1 = rootView.findViewById(R.id.edge_select_spinner_1);
        edgeSelectSpinner2 = rootView.findViewById(R.id.edge_select_spinner_2);
        edgeSelectSpinner3 = rootView.findViewById(R.id.edge_select_spinner_3);
        edgeSelectSpinner4 = rootView.findViewById(R.id.edge_select_spinner_4);
        analyze_button = rootView.findViewById(R.id.analyze_button);
        channelMode = 1;
        channelSelectSpinners.add(channelSelectSpinner1);
        channelSelectSpinners.add(channelSelectSpinner2);
        channelSelectSpinners.add(channelSelectSpinner3);
        channelSelectSpinners.add(channelSelectSpinner4);

        edgeSelectSpinners.add(edgeSelectSpinner1);
        edgeSelectSpinners.add(edgeSelectSpinner2);
        edgeSelectSpinners.add(edgeSelectSpinner3);
        edgeSelectSpinners.add(edgeSelectSpinner4);
        channelMap = new HashMap<>();
        channelMap.put(channels[0], 0);
        channelMap.put(channels[1], 1);
        channelMap.put(channels[2], 2);
        channelMap.put(channels[3], 3);
        // Axis Indicator
        xCoordinateText = rootView.findViewById(R.id.x_coordinate_text);
        xCoordinateText.setText("Time:  0.0 mS");
        progressBar = rootView.findViewById(R.id.la_progressBar);
        progressBar.setVisibility(View.GONE);
        ((LogicalAnalyzerActivity) getActivity()).setStatus(false);

        // Declaring digital data set
        digitalChannelArray = new ArrayList<>();
        dataSets = new ArrayList<>();

        // Creating base layout for chart
        logicLinesChart = rootView.findViewById(R.id.chart_la);
        logicLinesChart.setBorderWidth(2);
        Legend legend = logicLinesChart.getLegend();
        legend.setTextColor(Color.WHITE);
        XAxis xAxis = logicLinesChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.TOP);
        xAxis.setTextColor(Color.WHITE);

        setCarouselPicker();
        setAdapters();
        LogicalAnalyzerActivity laActivity = (LogicalAnalyzerActivity) getActivity();
        if (laActivity.isPlayback) {
            if (laActivity.recordedLAData.isEmpty()) {
                CustomSnackBar.showSnackBar(container, getString(R.string.no_playback_data),
                        null, null, Snackbar.LENGTH_SHORT);
            } else {
                setPlayBackData(laActivity.recordedLAData);
            }
        }
        return rootView;
    }

    public void logData() {
        long block = System.currentTimeMillis();
        double lat;
        double lon;
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
        csvLogger.prepareLogFile();
        csvLogger.writeMetaData(getContext().getResources().getString(R.string.logical_analyzer));
        csvLogger.writeCSVFile(CSV_HEADER);
        recordSensorDataBlockID(new SensorDataBlock(block, getResources().getString(R.string.logical_analyzer)));
        long timestamp = System.currentTimeMillis();
        String timeData = timestamp + "," + CSVLogger.FILE_NAME_FORMAT.format(new Date(timestamp));
        for (int i = 0; i < recordXAxis.size(); i++) {
            recordSensorData(new LogicAnalyzerData(timestamp + i, block, channels[i], recordChannelMode.get(i), recordXAxis.get(i), recordYAxis.get(i), lat, lon));

            CSVDataLine data = new CSVDataLine()
                    .add(timeData)
                    .add(channels[i])
                    .add(recordChannelMode.get(i))
                    .add(recordXAxis.get(i))
                    .add(recordYAxis.get(i))
                    .add(lat)
                    .add(lon);
            csvLogger.writeCSVFile(data);
        }
        CustomSnackBar.showSnackBar(rootView,
                getString(R.string.csv_store_text) + " " + csvLogger.getCurrentFilePath()
                , getString(R.string.open), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getContext(), DataLoggerActivity.class);
                        intent.putExtra(DataLoggerActivity.CALLER_ACTIVITY, getResources().getString(R.string.logical_analyzer));
                        startActivity(intent);
                    }
                }, Snackbar.LENGTH_SHORT);
    }

    private void setPlayBackData(RealmResults<LogicAnalyzerData> data) {
        analyze_button.setVisibility(View.GONE);
        currentChannel = 0;
        setViewVisibility(data.size() - 1);
        channelNames.clear();
        disableSpinners();
        carouselPicker.setCurrentItem(data.size() - 1);
        for (int i = 0; i < data.size(); i++) {
            LogicAnalyzerData laData = data.get(i);
            channelNames.add(laData.getChannel());
            edgeSelectSpinners.get(i).setSelection(laData.getChannelMode() - 1);
            channelSelectSpinners.get(i).setSelection(channelMap.get(laData.getChannel()));
            String[] xPoints = laData.getDataX().split(" ");
            String[] yPoints = laData.getDataY().split(" ");
            int n = Math.min(xPoints.length, yPoints.length);
            double[] xaxis = new double[n];
            double[] yaxis = new double[n];
            for (int j = 0; j < n; j++) {
                xaxis[j] = Double.valueOf(xPoints[j]);
                yaxis[j] = Double.valueOf(yPoints[j]);
            }
            switch (laData.getChannelMode()) {
                case 1:
                    singleChannelEveryEdge(xaxis, yaxis);
                    break;
                case 4:
                    singleChannelFourthRisingEdge(xaxis);
                    break;
                case 3:
                    singleChannelRisingEdges(xaxis, yaxis);
                    break;
                case 2:
                    singleChannelFallingEdges(xaxis, yaxis);
                    break;
                default:
                    singleChannelOtherEdges(xaxis, yaxis);
                    break;
            }
            currentChannel++;
        }
        logicLinesChart.setData(new LineData(dataSets));
        logicLinesChart.invalidate();

        YAxis left = logicLinesChart.getAxisLeft();
        left.setValueFormatter(new LogicAnalyzerAxisFormatter(channelNames));
        left.setTextColor(Color.WHITE);
        left.setGranularity(1f);
        left.setTextSize(12f);
        logicLinesChart.getAxisRight().setDrawLabels(false);
        logicLinesChart.getDescription().setEnabled(false);
        logicLinesChart.setScaleYEnabled(false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        carouselPicker.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == 0) {
                    setViewVisibility(carouselPicker.getCurrentItem());
                }
            }
        });

        analyze_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (channelMode > 0) {
                    if (scienceLab != null && scienceLab.isConnected()) {
                        analyze_button.setClickable(false);

                        // Change all variables to default value
                        currentChannel = 0;
                        dataSets.clear();
                        digitalChannelArray.clear();
                        channelNames.clear();
                        edgesNames.clear();
                        logicLinesChart.clear();
                        logicLinesChart.invalidate();

                        switch (channelMode) {
                            case 1:
                                channelNames.add(channelSelectSpinner1.getSelectedItem().toString());
                                edgesNames.add(edgeSelectSpinner1.getSelectedItem().toString());
                                break;
                            case 2:
                                channelNames.add(channelSelectSpinner1.getSelectedItem().toString());
                                channelNames.add(channelSelectSpinner2.getSelectedItem().toString());
                                edgesNames.add(edgeSelectSpinner1.getSelectedItem().toString());
                                edgesNames.add(edgeSelectSpinner2.getSelectedItem().toString());
                                break;
                            case 3:
                                channelNames.add(channelSelectSpinner1.getSelectedItem().toString());
                                channelNames.add(channelSelectSpinner2.getSelectedItem().toString());
                                channelNames.add(channelSelectSpinner3.getSelectedItem().toString());
                                edgesNames.add(edgeSelectSpinner1.getSelectedItem().toString());
                                edgesNames.add(edgeSelectSpinner2.getSelectedItem().toString());
                                edgesNames.add(edgeSelectSpinner3.getSelectedItem().toString());
                                break;
                            case 4:
                                channelNames.add(channelSelectSpinner1.getSelectedItem().toString());
                                channelNames.add(channelSelectSpinner2.getSelectedItem().toString());
                                channelNames.add(channelSelectSpinner3.getSelectedItem().toString());
                                channelNames.add(channelSelectSpinner4.getSelectedItem().toString());
                                edgesNames.add(edgeSelectSpinner1.getSelectedItem().toString());
                                edgesNames.add(edgeSelectSpinner2.getSelectedItem().toString());
                                edgesNames.add(edgeSelectSpinner3.getSelectedItem().toString());
                                edgesNames.add(edgeSelectSpinner4.getSelectedItem().toString());
                                break;
                            default:
                                channelNames.add(channelSelectSpinner1.getSelectedItem().toString());
                                edgesNames.add(edgeSelectSpinner1.getSelectedItem().toString());
                                break;
                        }
                        Thread monitor;
                        switch (channelMode) {
                            case 1:
                                progressBar.setVisibility(View.VISIBLE);
                                ((LogicalAnalyzerActivity) getActivity()).setStatus(true);
                                monitor = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        captureOne = new CaptureOne();
                                        captureOne.execute(channelNames.get(0), edgesNames.get(0));
                                        synchronized (lock) {
                                            try {
                                                lock.wait();
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                });
                                monitor.start();
                                break;
                            case 2:
                                progressBar.setVisibility(View.VISIBLE);
                                ((LogicalAnalyzerActivity) getActivity()).setStatus(true);
                                monitor = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        captureTwo = new CaptureTwo();
                                        ArrayList<String> channels = new ArrayList<>();
                                        channels.add(channelNames.get(0));
                                        channels.add(channelNames.get(1));
                                        ArrayList<String> edges = new ArrayList<>();
                                        edges.add(edgesNames.get(0));
                                        edges.add(edgesNames.get(1));
                                        captureTwo.execute(channels, edges);
                                        synchronized (lock) {
                                            try {
                                                lock.wait();
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                });
                                monitor.start();
                                break;
                            case 3:
                                progressBar.setVisibility(View.VISIBLE);
                                ((LogicalAnalyzerActivity) getActivity()).setStatus(true);
                                monitor = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        captureThree = new CaptureThree();
                                        ArrayList<String> channels = new ArrayList<>();
                                        channels.add(channelNames.get(0));
                                        channels.add(channelNames.get(1));
                                        channels.add(channelNames.get(2));
                                        ArrayList<String> edges = new ArrayList<>();
                                        edges.add(edgesNames.get(0));
                                        edges.add(edgesNames.get(1));
                                        edges.add(edgesNames.get(2));
                                        captureThree.execute(channels, edges);
                                        synchronized (lock) {
                                            try {
                                                lock.wait();
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                });
                                monitor.start();
                                break;
                            case 4:
                                progressBar.setVisibility(View.VISIBLE);
                                ((LogicalAnalyzerActivity) getActivity()).setStatus(true);
                                monitor = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        captureFour = new CaptureFour();
                                        ArrayList<String> channels = new ArrayList<>();
                                        channels.add(channelNames.get(0));
                                        channels.add(channelNames.get(1));
                                        channels.add(channelNames.get(2));
                                        channels.add(channelNames.get(3));
                                        ArrayList<String> edges = new ArrayList<>();
                                        edges.add(edgesNames.get(0));
                                        edges.add(edgesNames.get(1));
                                        edges.add(edgesNames.get(2));
                                        edges.add(edgesNames.get(3));
                                        captureFour.execute(channels, edges);
                                        synchronized (lock) {
                                            try {
                                                lock.wait();
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                });
                                monitor.start();
                                break;
                            default:
                                CustomSnackBar.showSnackBar(getActivity().findViewById(android.R.id.content),
                                        getString(R.string.needs_implementation), null, null, Snackbar.LENGTH_SHORT);
                                break;
                        }

                        // Setting cursor to display time at highlighted points
                        listener = new OnChartValueSelectedListener() {
                            @Override
                            public void onValueSelected(Entry e, Highlight h) {
                                double result = Math.round(e.getX() * 100.0) / 100.0;
                                xCoordinateText.setText("Time:  " + DataFormatter.formatDouble(result, DataFormatter.LOW_PRECISION_FORMAT) + " mS");
                            }

                            @Override
                            public void onNothingSelected() {

                            }
                        };
                        logicLinesChart.setOnChartValueSelectedListener(listener);
                    } else
                        CustomSnackBar.showSnackBar(getActivity().findViewById(android.R.id.content),
                                getString(R.string.device_not_found), null, null, Snackbar.LENGTH_SHORT);
                }
            }
        });
    }

    private void disableSpinners() {
        channelSelectSpinner1.setEnabled(false);
        channelSelectSpinner2.setEnabled(false);
        channelSelectSpinner3.setEnabled(false);
        channelSelectSpinner4.setEnabled(false);
        edgeSelectSpinner1.setEnabled(false);
        edgeSelectSpinner2.setEnabled(false);
        edgeSelectSpinner3.setEnabled(false);
        edgeSelectSpinner4.setEnabled(false);
        carouselPicker.setEnabled(false);
    }

    private void setViewVisibility(int mode) {
        switch (mode) {
            case 0:
                channelMode = 1;
                setAdapters();
                llChannel1.setVisibility(View.VISIBLE);
                llChannel2.setVisibility(View.GONE);
                llChannel3.setVisibility(View.GONE);
                llChannel4.setVisibility(View.GONE);
                channelSelectSpinner1.setEnabled(true);
                break;
            case 1:
                channelMode = 2;
                setAdapterForTwoChannelMode();
                llChannel1.setVisibility(View.VISIBLE);
                llChannel2.setVisibility(View.VISIBLE);
                llChannel3.setVisibility(View.GONE);
                llChannel4.setVisibility(View.GONE);
                channelSelectSpinner1.setEnabled(true);
                channelSelectSpinner2.setEnabled(true);
                break;
            case 2:
                channelMode = 3;
                setAdapters();
                llChannel1.setVisibility(View.VISIBLE);
                llChannel2.setVisibility(View.VISIBLE);
                llChannel3.setVisibility(View.VISIBLE);
                llChannel4.setVisibility(View.GONE);
                channelSelectSpinner1.setSelection(0);
                channelSelectSpinner2.setSelection(1);
                channelSelectSpinner3.setSelection(2);
                channelSelectSpinner1.setEnabled(true);
                channelSelectSpinner2.setEnabled(true);
                channelSelectSpinner3.setEnabled(true);
                break;
            case 3:
                channelMode = 4;
                setAdapters();
                llChannel1.setVisibility(View.VISIBLE);
                llChannel2.setVisibility(View.VISIBLE);
                llChannel3.setVisibility(View.VISIBLE);
                llChannel4.setVisibility(View.VISIBLE);
                channelSelectSpinner1.setSelection(0);
                channelSelectSpinner2.setSelection(1);
                channelSelectSpinner3.setSelection(2);
                channelSelectSpinner4.setSelection(3);
                channelSelectSpinner1.setEnabled(true);
                channelSelectSpinner2.setEnabled(true);
                channelSelectSpinner3.setEnabled(true);
                channelSelectSpinner4.setEnabled(true);
                break;
            default:
                channelMode = 1;
                setAdapters();
                llChannel1.setVisibility(View.VISIBLE);
                llChannel2.setVisibility(View.GONE);
                llChannel3.setVisibility(View.GONE);
                llChannel4.setVisibility(View.GONE);
                channelSelectSpinner1.setEnabled(true);
                break;
        }
    }

    /**
     * Plots every edge of a digital pulse for one channel at a time
     *
     * @param xData Data points fetched for X-axis
     * @param yData Data points fetched for Y-axis
     */

    private void singleChannelEveryEdge(double[] xData, double[] yData) {
        tempInput = new ArrayList<>();
        int[] temp = new int[xData.length];
        int[] yAxis = new int[yData.length];

        for (int i = 0; i < xData.length; i++) {
            temp[i] = (int) xData[i];
            yAxis[i] = (int) yData[i];
        }

        ArrayList<Integer> xaxis = new ArrayList<>();
        ArrayList<Integer> yaxis = new ArrayList<>();
        xaxis.add(temp[0]);
        yaxis.add(yAxis[0]);

        for (int i = 1; i < xData.length; i++) {
            if (temp[i] != temp[i - 1]) {
                xaxis.add(temp[i]);
                yaxis.add(yAxis[i]);
            }
        }

        // Add data to axis in actual graph
        if (yaxis.size() > 1) {
            if (yaxis.get(1).equals(yaxis.get(0)))
                tempInput.add(new Entry(xaxis.get(0), yaxis.get(0) + 2 * currentChannel));
            else {
                tempInput.add(new Entry(xaxis.get(0), yaxis.get(0) + 2 * currentChannel));
                tempInput.add(new Entry(xaxis.get(0), yaxis.get(1) + 2 * currentChannel));
            }
            for (int i = 1; i < xaxis.size() - 1; i++) {
                if (yaxis.get(i).equals(yaxis.get(i + 1)))
                    tempInput.add(new Entry(xaxis.get(i), yaxis.get(i) + 2 * currentChannel));
                else {
                    tempInput.add(new Entry(xaxis.get(i), yaxis.get(i) + 2 * currentChannel));
                    tempInput.add(new Entry(xaxis.get(i), yaxis.get(i + 1) + 2 * currentChannel));
                }
            }
            tempInput.add(new Entry(xaxis.get(xaxis.size() - 1), yaxis.get(xaxis.size() - 1) + 2 * currentChannel));
        } else {
            tempInput.add(new Entry(xaxis.get(0), yaxis.get(0) + 2 * currentChannel));
        }

        setLineDataSet();
    }

    /**
     * Plots every fourth rising edge of a digital pulse for one channel at a time
     *
     * @param xData Data points fetched for X-axis
     */

    private void singleChannelFourthRisingEdge(double[] xData) {
        tempInput = new ArrayList<>();
        int xaxis = (int) xData[0];
        tempInput.add(new Entry(xaxis, 0 + 2 * currentChannel));
        tempInput.add(new Entry(xaxis, 1 + 2 * currentChannel));
        tempInput.add(new Entry(xaxis, 0 + 2 * currentChannel));
        int check = xaxis;
        int count = 0;

        if (xData.length > 1) {
            for (int i = 1; i < xData.length; i++) {
                xaxis = (int) xData[i];
                if (xaxis != check) {
                    if (count == 3) {
                        tempInput.add(new Entry(xaxis, 0 + 2 * currentChannel));
                        tempInput.add(new Entry(xaxis, 1 + 2 * currentChannel));
                        tempInput.add(new Entry(xaxis, 0 + 2 * currentChannel));
                        count = 0;
                    } else
                        count++;
                    check = xaxis;
                }
            }
        }

        setLineDataSet();
    }

    /**
     * Plots every rising edges of a digital pulse for one channel at a time
     *
     * @param xData Data points fetched for X-axis
     * @param yData Data points fetched for Y-axis
     */

    private void singleChannelRisingEdges(double[] xData, double[] yData) {
        tempInput = new ArrayList<>();

        for (int i = 1; i < xData.length; i += 6) {
            tempInput.add(new Entry((int) xData[i], (int) yData[i] + 2 * currentChannel));
            tempInput.add(new Entry((int) xData[i + 1], (int) yData[i + 1] + 2 * currentChannel));
            tempInput.add(new Entry((int) xData[i + 2], (int) yData[i + 2] + 2 * currentChannel));
        }

        setLineDataSet();
    }

    /**
     * Plots every falling edges of a digital pulse for one channel at a time
     *
     * @param xData Data points fetched for X-axis
     * @param yData Data points fetched for Y-axis
     */

    private void singleChannelFallingEdges(double[] xData, double[] yData) {
        tempInput = new ArrayList<>();

        for (int i = 4; i < xData.length; i += 6) {
            tempInput.add(new Entry((int) xData[i], (int) yData[i] + 2 * currentChannel));
            tempInput.add(new Entry((int) xData[i + 1], (int) yData[i + 1] + 2 * currentChannel));
            tempInput.add(new Entry((int) xData[i + 2], (int) yData[i + 2] + 2 * currentChannel));
        }

        setLineDataSet();
    }

    /**
     * Plots every data point fetched for a digital pulse (default case)
     *
     * @param xData Data points fetched for X-axis
     * @param yData Data points fetched for Y-axis
     */

    private void singleChannelOtherEdges(double[] xData, double[] yData) {
        tempInput = new ArrayList<>();

        for (int i = 0; i < xData.length; i++) {
            int xaxis = (int) xData[i];
            int yaxis = (int) yData[i];
            tempInput.add(new Entry(xaxis, yaxis + 2 * currentChannel));
        }

        setLineDataSet();
    }

    /**
     * Plot the entries available in tuple (X-axis, Y-axis) on the graph
     */

    private void setLineDataSet() {
        LineDataSet lineDataSet = new LineDataSet(tempInput, channelNames.get(currentChannel));
        lineDataSet.setColor(colors[currentChannel]);
        lineDataSet.setCircleRadius(1);
        lineDataSet.setLineWidth(2);
        lineDataSet.setCircleColor(Color.GREEN);
        lineDataSet.setDrawValues(false);
        lineDataSet.setDrawCircles(false);
        lineDataSet.setHighLightColor(getResources().getColor(R.color.golden));
        dataSets.add(lineDataSet);
    }

    /**
     * Sets adapters to spinners for all modes except for TwoChannel Mode
     */

    private void setAdapters() {
        String[] channels = getResources().getStringArray(R.array.channel_choices);
        String[] edges = getResources().getStringArray(R.array.edge_choices);

        ArrayAdapter<String> channel_adapter = new ArrayAdapter<>(getContext(), R.layout.modified_spinner_dropdown_list, channels);
        ArrayAdapter<String> edges_adapter = new ArrayAdapter<>(getContext(), R.layout.modified_spinner_dropdown_list, edges);

        channelSelectSpinner1.setAdapter(channel_adapter);
        channelSelectSpinner2.setAdapter(channel_adapter);
        channelSelectSpinner3.setAdapter(channel_adapter);
        channelSelectSpinner4.setAdapter(channel_adapter);

        edgeSelectSpinner1.setAdapter(edges_adapter);
        edgeSelectSpinner2.setAdapter(edges_adapter);
        edgeSelectSpinner3.setAdapter(edges_adapter);
        edgeSelectSpinner4.setAdapter(edges_adapter);

    }

    /**
     * Sets adapters to spinners for TwoChannel Mode
     */

    private void setAdapterForTwoChannelMode() {
        final String[] channels = getResources().getStringArray(R.array.channel_choices);
        final String[] edges = getResources().getStringArray(R.array.edge_choices);

        final List<String> channel_one_list = new ArrayList<>(Arrays.asList(channels));
        final List<String> channel_two_list = new ArrayList<>(Arrays.asList(channels));

        final ArrayAdapter<String> channel_one_adapter = new ArrayAdapter<>(getContext(), R.layout.modified_spinner_dropdown_list, channel_one_list);
        final ArrayAdapter<String> channel_two_adapter = new ArrayAdapter<>(getContext(), R.layout.modified_spinner_dropdown_list, channel_two_list);
        ArrayAdapter<String> edges_adapter = new ArrayAdapter<>(getContext(), R.layout.modified_spinner_dropdown_list, edges);

        channelSelectSpinner1.setAdapter(channel_one_adapter);
        channelSelectSpinner2.setAdapter(channel_two_adapter);

        edgeSelectSpinner1.setAdapter(edges_adapter);
        edgeSelectSpinner2.setAdapter(edges_adapter);

        channelSelectSpinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = channelSelectSpinner1.getItemAtPosition(position).toString();
                channel_two_list.clear();
                for (int i = 0; i < channels.length; i++) {
                    if (!channels[i].equals(selection)) {
                        channel_two_list.add(channels[i]);
                    }
                }
                channel_two_adapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No use
            }
        });

        channelSelectSpinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = channelSelectSpinner2.getItemAtPosition(position).toString();
                channel_one_list.clear();
                for (int i = 0; i < channels.length; i++) {
                    if (!channels[i].equals(selection)) {
                        channel_one_list.add(channels[i]);
                    }
                }
                channel_one_adapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No use
            }
        });
    }

    /**
     * Sets the text in Carousel Picker
     */

    private void setCarouselPicker() {
        // Calculation made for setting the text size in Carousel Picker for different screens
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        double wi = (double) width / (double) dm.xdpi;
        double hi = (double) height / (double) dm.ydpi;
        double x = Math.pow(wi, 2);
        double y = Math.pow(hi, 2);
        double screenInches = Math.sqrt(x + y) + 0.01;
        int textsize;
        if (screenInches < 5)
            textsize = 11;
        else
            textsize = 9;

        // Items for Carousel Picker
        List<CarouselPicker.PickerItem> channelModes = new ArrayList<>();
        channelModes.add(new CarouselPicker.TextItem("1", textsize));
        channelModes.add(new CarouselPicker.TextItem("2", textsize));
        channelModes.add(new CarouselPicker.TextItem("3", textsize));
        channelModes.add(new CarouselPicker.TextItem("4", textsize));

        CarouselPicker.CarouselViewAdapter channelAdapter = new CarouselPicker.CarouselViewAdapter(getContext(), channelModes, 0);
        carouselPicker.setAdapter(channelAdapter);
        carouselPicker.setCurrentItem(0);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
        }
        super.onStop();
    }

    /**
     * Used to delay a thread by some given time in milliseconds
     *
     * @param delay Time to delay in milliseconds
     */

    public void delayThread(long delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void storeAxisValues(double[] xaxis, double[] yaxis, int mode) {
        StringBuilder stringBuilder1 = new StringBuilder();
        StringBuilder stringBuilder2 = new StringBuilder();
        for (int i = 0; i < xaxis.length; i++) {
            stringBuilder1.append(DataFormatter.formatDouble(xaxis[i], DataFormatter.LOW_PRECISION_FORMAT));
            stringBuilder2.append(DataFormatter.formatDouble(yaxis[i], DataFormatter.LOW_PRECISION_FORMAT));
            stringBuilder1.append(" ");
            stringBuilder2.append(" ");
        }
        recordXAxis.add(stringBuilder1.toString());
        recordYAxis.add(stringBuilder2.toString());
        recordChannelMode.add(mode);
    }

    public void recordSensorDataBlockID(SensorDataBlock block) {
        realm.beginTransaction();
        realm.copyToRealm(block);
        realm.commitTransaction();
    }

    public void recordSensorData(RealmObject sensorData) {
        realm.beginTransaction();
        realm.copyToRealm((LogicAnalyzerData) sensorData);
        realm.commitTransaction();
    }

    private class CaptureOne extends AsyncTask<String, String, Void> {
        private String edgeOption = "";
        private boolean holder;

        @Override
        protected Void doInBackground(String... params) {
            try {
                channels[0] = params[0];

                int channelNumber = scienceLab.calculateDigitalChannel(params[0]);
                digitalChannel = scienceLab.getDigitalChannel(channelNumber);
                edgeOption = params[1];

                switch (edgeOption) {
                    case "EVERY EDGE":
                        digitalChannel.mode = EVERY_EDGE;
                        break;
                    case "EVERY FALLING EDGE":
                        digitalChannel.mode = EVERY_FALLING_EDGE;
                        break;
                    case "EVERY RISING EDGE":
                        digitalChannel.mode = EVERY_RISING_EDGE;
                        break;
                    case "EVERY FOURTH RISING EDGE":
                        digitalChannel.mode = EVERY_FOURTH_RISING_EDGE;
                        break;
                    case "DISABLED":
                        digitalChannel.mode = DISABLED;
                        break;
                    default:
                        digitalChannel.mode = EVERY_EDGE;
                }

                scienceLab.startOneChannelLA(params[0], digitalChannel.mode, params[0], 3);
                delayThread(1000);
                LinkedHashMap<String, Integer> data = scienceLab.getLAInitialStates();
                delayThread(1000);
                holder = scienceLab.fetchLAChannel(channelNumber, data);

            } catch (NullPointerException e) {
                cancel(true);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (holder) {

                double[] xaxis = digitalChannel.getXAxis();
                double[] yaxis = digitalChannel.getYAxis();

                StringBuilder stringBuilder1 = new StringBuilder();
                StringBuilder stringBuilder2 = new StringBuilder();
                for (int i = 0; i < xaxis.length; i++) {
                    stringBuilder1.append(DataFormatter.formatDouble(xaxis[i], DataFormatter.LOW_PRECISION_FORMAT));
                    stringBuilder2.append(DataFormatter.formatDouble(yaxis[i], DataFormatter.LOW_PRECISION_FORMAT));
                    stringBuilder1.append(" ");
                    stringBuilder2.append(" ");
                }
                Log.v("x Axis", stringBuilder1.toString());
                Log.v("y Axis", stringBuilder2.toString());

                recordXAxis.clear();
                recordXAxis.add(stringBuilder1.toString());
                recordYAxis.add(stringBuilder2.toString());
                recordChannelMode.add(digitalChannel.mode);
                // Plot the fetched data
                switch (edgeOption) {
                    case "EVERY EDGE":
                        singleChannelEveryEdge(xaxis, yaxis);
                        break;
                    case "EVERY FOURTH RISING EDGE":
                        singleChannelFourthRisingEdge(xaxis);
                        break;
                    case "EVERY RISING EDGE":
                        singleChannelRisingEdges(xaxis, yaxis);
                        break;
                    case "EVERY FALLING EDGE":
                        singleChannelFallingEdges(xaxis, yaxis);
                        break;
                    default:
                        singleChannelOtherEdges(xaxis, yaxis);
                        break;
                }
                progressBar.setVisibility(View.GONE);
                ((LogicalAnalyzerActivity) getActivity()).setStatus(false);

                logicLinesChart.setData(new LineData(dataSets));
                logicLinesChart.notifyDataSetChanged();
                logicLinesChart.invalidate();

                YAxis left = logicLinesChart.getAxisLeft();
                left.setValueFormatter(new LogicAnalyzerAxisFormatter(channelNames));
                left.setTextColor(Color.WHITE);
                left.setGranularity(1f);
                left.setTextSize(12f);
                logicLinesChart.getAxisRight().setDrawLabels(false);
                logicLinesChart.getDescription().setEnabled(false);
                logicLinesChart.setScaleYEnabled(false);

                synchronized (lock) {
                    lock.notify();
                }
            } else {
                progressBar.setVisibility(View.GONE);
                ((LogicalAnalyzerActivity) getActivity()).setStatus(false);
                CustomSnackBar.showSnackBar(getActivity().findViewById(android.R.id.content),
                        getString(R.string.no_data_generated), null, null, Snackbar.LENGTH_SHORT);
                analyze_button.setClickable(true);
            }

            analyze_button.setClickable(true);
        }
    }

    private class CaptureTwo extends AsyncTask<ArrayList<String>, ArrayList<String>, Void> {
        private String[] edgeOption = new String[channelMode];
        private boolean holder1, holder2;

        @SafeVarargs
        @Override
        protected final Void doInBackground(ArrayList<String>... arrayLists) {
            try {
                channels[0] = arrayLists[0].get(0);
                channels[1] = arrayLists[0].get(1);

                int channelNumber1 = scienceLab.calculateDigitalChannel(arrayLists[0].get(0));
                int channelNumber2 = scienceLab.calculateDigitalChannel(arrayLists[0].get(1));

                digitalChannelArray.add(scienceLab.getDigitalChannel(channelNumber1));
                digitalChannelArray.add(scienceLab.getDigitalChannel(channelNumber2));
                edgeOption[0] = arrayLists[1].get(0);
                edgeOption[1] = arrayLists[1].get(1);

                ArrayList<Integer> modes = new ArrayList<>();
                for (int i = 0; i < channelMode; i++) {
                    switch (edgeOption[i]) {
                        case "EVERY EDGE":
                            digitalChannelArray.get(i).mode = EVERY_EDGE;
                            modes.add(EVERY_EDGE);
                            break;
                        case "EVERY FALLING EDGE":
                            digitalChannelArray.get(i).mode = EVERY_FALLING_EDGE;
                            modes.add(EVERY_FALLING_EDGE);
                            break;
                        case "EVERY RISING EDGE":
                            digitalChannelArray.get(i).mode = EVERY_RISING_EDGE;
                            modes.add(EVERY_RISING_EDGE);
                            break;
                        case "EVERY FOURTH RISING EDGE":
                            digitalChannelArray.get(i).mode = EVERY_FOURTH_RISING_EDGE;
                            modes.add(EVERY_FOURTH_RISING_EDGE);
                            break;
                        case "DISABLED":
                            digitalChannelArray.get(i).mode = DISABLED;
                            modes.add(DISABLED);
                            break;
                        default:
                            digitalChannelArray.get(i).mode = EVERY_EDGE;
                            modes.add(EVERY_EDGE);
                    }
                }

                scienceLab.startTwoChannelLA(arrayLists[0], modes, 67, null, null, null);
                delayThread(1000);
                LinkedHashMap<String, Integer> data = scienceLab.getLAInitialStates();
                delayThread(1000);
                holder1 = scienceLab.fetchLAChannel(channelNumber1, data);
                delayThread(1000);
                holder2 = scienceLab.fetchLAChannel(channelNumber2, data);

            } catch (NullPointerException e) {
                cancel(true);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (holder1 && holder2) {

                ArrayList<double[]> xaxis = new ArrayList<>();
                xaxis.add(digitalChannelArray.get(0).getXAxis());
                xaxis.add(digitalChannelArray.get(1).getXAxis());

                ArrayList<double[]> yaxis = new ArrayList<>();
                yaxis.add(digitalChannelArray.get(0).getYAxis());
                yaxis.add(digitalChannelArray.get(1).getYAxis());

                recordXAxis.clear();
                recordYAxis.clear();
                recordChannelMode.clear();
                // Plot the fetched data
                for (int i = 0; i < channelMode; i++) {
                    storeAxisValues(xaxis.get(i), yaxis.get(i), digitalChannelArray.get(i).mode);
                    switch (edgeOption[i]) {
                        case "EVERY EDGE":
                            singleChannelEveryEdge(xaxis.get(i), yaxis.get(i));
                            break;
                        case "EVERY FOURTH RISING EDGE":
                            singleChannelFourthRisingEdge(xaxis.get(i));
                            break;
                        case "EVERY RISING EDGE":
                            singleChannelRisingEdges(xaxis.get(i), yaxis.get(i));
                            break;
                        case "EVERY FALLING EDGE":
                            singleChannelFallingEdges(xaxis.get(i), yaxis.get(i));
                            break;
                        default:
                            singleChannelOtherEdges(xaxis.get(i), yaxis.get(i));
                            break;
                    }
                    currentChannel++;
                }

                progressBar.setVisibility(View.GONE);
                ((LogicalAnalyzerActivity) getActivity()).setStatus(false);

                logicLinesChart.setData(new LineData(dataSets));
                logicLinesChart.invalidate();

                YAxis left = logicLinesChart.getAxisLeft();
                left.setValueFormatter(new LogicAnalyzerAxisFormatter(channelNames));
                left.setTextColor(Color.WHITE);
                left.setGranularity(1f);
                left.setTextSize(12f);
                logicLinesChart.getAxisRight().setDrawLabels(false);
                logicLinesChart.getDescription().setEnabled(false);
                logicLinesChart.setScaleYEnabled(false);

                synchronized (lock) {
                    lock.notify();
                }
            } else {
                progressBar.setVisibility(View.GONE);
                ((LogicalAnalyzerActivity) getActivity()).setStatus(false);
                CustomSnackBar.showSnackBar(getActivity().findViewById(android.R.id.content),
                        getString(R.string.no_data_generated), null, null, Snackbar.LENGTH_SHORT);
            }

            analyze_button.setClickable(true);
        }
    }

    private class CaptureThree extends AsyncTask<ArrayList<String>, ArrayList<String>, Void> {
        private String[] edgeOption = new String[channelMode];
        private boolean holder1, holder2, holder3;

        @SafeVarargs
        @Override
        protected final Void doInBackground(ArrayList<String>... arrayLists) {
            try {
                channels[0] = arrayLists[0].get(0);
                channels[1] = arrayLists[0].get(1);
                channels[2] = arrayLists[0].get(2);

                int channelNumber1 = scienceLab.calculateDigitalChannel(arrayLists[0].get(0));
                int channelNumber2 = scienceLab.calculateDigitalChannel(arrayLists[0].get(1));
                int channelNumber3 = scienceLab.calculateDigitalChannel(arrayLists[0].get(2));

                digitalChannelArray.add(scienceLab.getDigitalChannel(channelNumber1));
                digitalChannelArray.add(scienceLab.getDigitalChannel(channelNumber2));
                digitalChannelArray.add(scienceLab.getDigitalChannel(channelNumber3));
                edgeOption[0] = arrayLists[1].get(0);
                edgeOption[1] = arrayLists[1].get(1);
                edgeOption[2] = arrayLists[1].get(2);

                ArrayList<Integer> modes = new ArrayList<>();
                for (int i = 0; i < channelMode; i++) {
                    switch (edgeOption[i]) {
                        case "EVERY EDGE":
                            digitalChannelArray.get(i).mode = EVERY_EDGE;
                            modes.add(EVERY_EDGE);
                            break;
                        case "EVERY FALLING EDGE":
                            digitalChannelArray.get(i).mode = EVERY_FALLING_EDGE;
                            modes.add(EVERY_FALLING_EDGE);
                            break;
                        case "EVERY RISING EDGE":
                            digitalChannelArray.get(i).mode = EVERY_RISING_EDGE;
                            modes.add(EVERY_RISING_EDGE);
                            break;
                        case "EVERY FOURTH RISING EDGE":
                            digitalChannelArray.get(i).mode = EVERY_FOURTH_RISING_EDGE;
                            modes.add(EVERY_FOURTH_RISING_EDGE);
                            break;
                        case "DISABLED":
                            digitalChannelArray.get(i).mode = DISABLED;
                            modes.add(DISABLED);
                            break;
                        default:
                            digitalChannelArray.get(i).mode = EVERY_EDGE;
                            modes.add(EVERY_EDGE);
                    }
                }

                scienceLab.startThreeChannelLA(modes, null, null);
                delayThread(1000);
                LinkedHashMap<String, Integer> data = scienceLab.getLAInitialStates();
                delayThread(1000);
                holder1 = scienceLab.fetchLAChannel(channelNumber1, data);
                delayThread(1000);
                holder2 = scienceLab.fetchLAChannel(channelNumber2, data);
                delayThread(1000);
                holder3 = scienceLab.fetchLAChannel(channelNumber3, data);

            } catch (NullPointerException e) {
                cancel(true);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (holder1 && holder2 && holder3) {

                ArrayList<double[]> xaxis = new ArrayList<>();
                xaxis.add(digitalChannelArray.get(0).getXAxis());
                xaxis.add(digitalChannelArray.get(1).getXAxis());
                xaxis.add(digitalChannelArray.get(2).getXAxis());

                ArrayList<double[]> yaxis = new ArrayList<>();
                yaxis.add(digitalChannelArray.get(0).getYAxis());
                yaxis.add(digitalChannelArray.get(1).getYAxis());
                yaxis.add(digitalChannelArray.get(2).getYAxis());

                recordXAxis.clear();
                recordYAxis.clear();
                recordChannelMode.clear();
                // Plot the fetched data
                for (int i = 0; i < channelMode; i++) {
                    storeAxisValues(xaxis.get(i), yaxis.get(i), digitalChannelArray.get(i).mode);
                    switch (edgeOption[i]) {
                        case "EVERY EDGE":
                            singleChannelEveryEdge(xaxis.get(i), yaxis.get(i));
                            break;
                        case "EVERY FOURTH RISING EDGE":
                            singleChannelFourthRisingEdge(xaxis.get(i));
                            break;
                        case "EVERY RISING EDGE":
                            singleChannelRisingEdges(xaxis.get(i), yaxis.get(i));
                            break;
                        case "EVERY FALLING EDGE":
                            singleChannelFallingEdges(xaxis.get(i), yaxis.get(i));
                            break;
                        default:
                            singleChannelOtherEdges(xaxis.get(i), yaxis.get(i));
                            break;
                    }
                    currentChannel++;
                }

                progressBar.setVisibility(View.GONE);
                ((LogicalAnalyzerActivity) getActivity()).setStatus(false);

                logicLinesChart.setData(new LineData(dataSets));
                logicLinesChart.invalidate();

                YAxis left = logicLinesChart.getAxisLeft();
                left.setValueFormatter(new LogicAnalyzerAxisFormatter(channelNames));
                left.setTextColor(Color.WHITE);
                left.setGranularity(1f);
                left.setTextSize(12f);
                logicLinesChart.getAxisRight().setDrawLabels(false);
                logicLinesChart.getDescription().setEnabled(false);
                logicLinesChart.setScaleYEnabled(false);

                synchronized (lock) {
                    lock.notify();
                }
            } else {
                progressBar.setVisibility(View.GONE);
                ((LogicalAnalyzerActivity) getActivity()).setStatus(false);
                CustomSnackBar.showSnackBar(getActivity().findViewById(android.R.id.content),
                        getString(R.string.no_data_generated), null, null, Snackbar.LENGTH_SHORT);
            }

            analyze_button.setClickable(true);
        }
    }

    private class CaptureFour extends AsyncTask<ArrayList<String>, ArrayList<String>, Void> {
        private String[] edgeOption = new String[channelMode];
        private boolean holder1, holder2, holder3, holder4;

        @Override
        protected Void doInBackground(ArrayList<String>... arrayLists) {
            try {
                channels[0] = arrayLists[0].get(0);
                channels[1] = arrayLists[0].get(1);
                channels[2] = arrayLists[0].get(2);
                channels[3] = arrayLists[0].get(3);

                int channelNumber1 = scienceLab.calculateDigitalChannel(arrayLists[0].get(0));
                int channelNumber2 = scienceLab.calculateDigitalChannel(arrayLists[0].get(1));
                int channelNumber3 = scienceLab.calculateDigitalChannel(arrayLists[0].get(2));
                int channelNumber4 = scienceLab.calculateDigitalChannel(arrayLists[0].get(3));

                digitalChannelArray.add(scienceLab.getDigitalChannel(channelNumber1));
                digitalChannelArray.add(scienceLab.getDigitalChannel(channelNumber2));
                digitalChannelArray.add(scienceLab.getDigitalChannel(channelNumber3));
                digitalChannelArray.add(scienceLab.getDigitalChannel(channelNumber4));
                edgeOption[0] = arrayLists[1].get(0);
                edgeOption[1] = arrayLists[1].get(1);
                edgeOption[2] = arrayLists[1].get(2);
                edgeOption[3] = arrayLists[1].get(3);

                ArrayList<Integer> modes = new ArrayList<>();
                for (int i = 0; i < channelMode; i++) {
                    switch (edgeOption[i]) {
                        case "EVERY EDGE":
                            digitalChannelArray.get(i).mode = EVERY_EDGE;
                            modes.add(EVERY_EDGE);
                            break;
                        case "EVERY FALLING EDGE":
                            digitalChannelArray.get(i).mode = EVERY_FALLING_EDGE;
                            modes.add(EVERY_FALLING_EDGE);
                            break;
                        case "EVERY RISING EDGE":
                            digitalChannelArray.get(i).mode = EVERY_RISING_EDGE;
                            modes.add(EVERY_RISING_EDGE);
                            break;
                        case "EVERY FOURTH RISING EDGE":
                            digitalChannelArray.get(i).mode = EVERY_FOURTH_RISING_EDGE;
                            modes.add(EVERY_FOURTH_RISING_EDGE);
                            break;
                        case "DISABLED":
                            digitalChannelArray.get(i).mode = DISABLED;
                            modes.add(DISABLED);
                            break;
                        default:
                            digitalChannelArray.get(i).mode = EVERY_EDGE;
                            modes.add(EVERY_EDGE);
                    }
                }
                ArrayList<Boolean> triggerChannel = new ArrayList<>();
                triggerChannel.add(true);
                triggerChannel.add(true);
                triggerChannel.add(true);

                scienceLab.startFourChannelLA(null, null, modes, null, triggerChannel);
                delayThread(1000);
                LinkedHashMap<String, Integer> data = scienceLab.getLAInitialStates();
                delayThread(1000);
                holder1 = scienceLab.fetchLAChannel(channelNumber1, data);
                delayThread(1000);
                holder2 = scienceLab.fetchLAChannel(channelNumber2, data);
                delayThread(1000);
                holder3 = scienceLab.fetchLAChannel(channelNumber3, data);
                delayThread(1000);
                holder4 = scienceLab.fetchLAChannel(channelNumber4, data);

            } catch (NullPointerException e) {
                cancel(true);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (holder1 && holder2 && holder3 && holder4) {

                ArrayList<double[]> xaxis = new ArrayList<>();
                xaxis.add(digitalChannelArray.get(0).getXAxis());
                xaxis.add(digitalChannelArray.get(1).getXAxis());
                xaxis.add(digitalChannelArray.get(2).getXAxis());
                xaxis.add(digitalChannelArray.get(3).getXAxis());

                ArrayList<double[]> yaxis = new ArrayList<>();
                yaxis.add(digitalChannelArray.get(0).getYAxis());
                yaxis.add(digitalChannelArray.get(1).getYAxis());
                yaxis.add(digitalChannelArray.get(2).getYAxis());
                yaxis.add(digitalChannelArray.get(3).getYAxis());

                recordXAxis.clear();
                recordYAxis.clear();
                recordChannelMode.clear();
                // Plot the fetched data
                for (int i = 0; i < channelMode; i++) {
                    storeAxisValues(xaxis.get(i), yaxis.get(i), digitalChannelArray.get(i).mode);
                    switch (edgeOption[i]) {
                        case "EVERY EDGE":
                            singleChannelEveryEdge(xaxis.get(i), yaxis.get(i));
                            break;
                        case "EVERY FOURTH RISING EDGE":
                            singleChannelFourthRisingEdge(xaxis.get(i));
                            break;
                        case "EVERY RISING EDGE":
                            singleChannelRisingEdges(xaxis.get(i), yaxis.get(i));
                            break;
                        case "EVERY FALLING EDGE":
                            singleChannelFallingEdges(xaxis.get(i), yaxis.get(i));
                            break;
                        default:
                            singleChannelOtherEdges(xaxis.get(i), yaxis.get(i));
                            break;
                    }
                    currentChannel++;
                }

                progressBar.setVisibility(View.GONE);
                ((LogicalAnalyzerActivity) getActivity()).setStatus(false);

                logicLinesChart.setData(new LineData(dataSets));
                logicLinesChart.invalidate();

                YAxis left = logicLinesChart.getAxisLeft();
                left.setValueFormatter(new LogicAnalyzerAxisFormatter(channelNames));
                left.setTextColor(Color.WHITE);
                left.setGranularity(1f);
                left.setTextSize(12f);
                logicLinesChart.getAxisRight().setDrawLabels(false);
                logicLinesChart.getDescription().setEnabled(false);
                logicLinesChart.setScaleYEnabled(false);

                synchronized (lock) {
                    lock.notify();
                }
            } else {
                progressBar.setVisibility(View.GONE);
                ((LogicalAnalyzerActivity) getActivity()).setStatus(false);
                CustomSnackBar.showSnackBar(getActivity().findViewById(android.R.id.content),
                        getString(R.string.no_data_generated), null, null, Snackbar.LENGTH_SHORT);
            }

            analyze_button.setClickable(true);
        }
    }
}
