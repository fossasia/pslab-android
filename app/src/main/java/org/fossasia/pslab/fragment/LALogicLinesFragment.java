package org.fossasia.pslab.fragment;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

import org.fossasia.pslab.R;
import org.fossasia.pslab.activity.LogicalAnalyzerActivity;
import org.fossasia.pslab.communication.ScienceLab;
import org.fossasia.pslab.communication.digitalChannel.DigitalChannel;
import org.fossasia.pslab.others.ChannelAxisFormatter;
import org.fossasia.pslab.others.MathUtils;
import org.fossasia.pslab.others.ScienceLabCommon;
import org.fossasia.pslab.others.SwipeGestureDetector;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import butterknife.ButterKnife;
import in.goodiebag.carouselpicker.CarouselPicker;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by viveksb007 on 9/6/17.
 */

public class LALogicLinesFragment extends Fragment {

    public static final String PREFS_NAME = "LogicAnalyzerPreference";
    private final Object lock = new Object();
    List<Entry> tempInput;
    DigitalChannel digitalChannel;
    BottomSheetBehavior bottomSheetBehavior;
    GestureDetector gestureDetector;

    //Bottom Sheet
    private LinearLayout bottomSheet;
    private View tvShadow;
    private ImageView arrowUpDown;
    private TextView bottomSheetSlideText;
    private TextView bottomSheetGuideTitle;
    private TextView bottomSheetText;
    private ImageView bottomSheetSchematic;
    private TextView bottomSheetDesc;

    private Activity activity;
    private int channelMode;
    private ScienceLab scienceLab;
    private LineChart logicLinesChart;
    private ArrayList<String> channelNames = new ArrayList<>();
    private ArrayList<String> edgesNames = new ArrayList<>();
    private TextView tvTimeUnit, xCoordinateText, selectChannelText;
    private ImageView ledImageView;
    private Runnable logicAnalysis;
    private OnChartValueSelectedListener listener;
    private CarouselPicker carouselPicker;
    private LinearLayout llChannel1, llChannel2, llChannel3, llChannel4;
    private Spinner channelSelectSpinner1, channelSelectSpinner2, channelSelectSpinner3, channelSelectSpinner4;
    private Spinner edgeSelectSpinner1, edgeSelectSpinner2, edgeSelectSpinner3, edgeSelectSpinner4;
    private Button analyze_button;
    private ProgressBar progressBar;
    private CaptureOne captureOne;

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

        logicAnalysis = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (scienceLab.isConnected()) {
                        if (!String.valueOf(ledImageView.getTag()).equals("green")) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ledImageView.setImageResource(R.drawable.green_led);
                                    ledImageView.setTag("green");
                                }
                            });
                        }
                    } else {
                        if (!String.valueOf(ledImageView.getTag()).equals("red")) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ledImageView.setImageResource(R.drawable.red_led);
                                    ledImageView.setTag("red");
                                }
                            });
                        }
                    }
                }
            }
        };
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.logic_analyzer_main_layout, container, false);

        // LED Indicator
        ledImageView = v.findViewById(R.id.imageView_led_la);

        // Heading
        tvTimeUnit = v.findViewById(R.id.la_tv_time_unit);
        tvTimeUnit.setText(getString(R.string.time_unit_la));

        // Carousel View
        carouselPicker = (CarouselPicker) v.findViewById(R.id.carouselPicker);
        llChannel1 = (LinearLayout) v.findViewById(R.id.ll_chart_channel_1);
        llChannel1.setVisibility(View.GONE);
        llChannel2 = (LinearLayout) v.findViewById(R.id.ll_chart_channel_2);
        llChannel2.setVisibility(View.GONE);
        llChannel3 = (LinearLayout) v.findViewById(R.id.ll_chart_channel_3);
        llChannel3.setVisibility(View.GONE);
        llChannel4 = (LinearLayout) v.findViewById(R.id.ll_chart_channel_4);
        llChannel4.setVisibility(View.GONE);
        channelSelectSpinner1 = (Spinner) v.findViewById(R.id.channel_select_spinner_1);
        channelSelectSpinner2 = (Spinner) v.findViewById(R.id.channel_select_spinner_2);
        channelSelectSpinner3 = (Spinner) v.findViewById(R.id.channel_select_spinner_3);
        channelSelectSpinner4 = (Spinner) v.findViewById(R.id.channel_select_spinner_4);
        edgeSelectSpinner1 = (Spinner) v.findViewById(R.id.edge_select_spinner_1);
        edgeSelectSpinner2 = (Spinner) v.findViewById(R.id.edge_select_spinner_2);
        edgeSelectSpinner3 = (Spinner) v.findViewById(R.id.edge_select_spinner_3);
        edgeSelectSpinner4 = (Spinner) v.findViewById(R.id.edge_select_spinner_4);
        analyze_button = (Button) v.findViewById(R.id.analyze_button);
        selectChannelText = (TextView) v.findViewById(R.id.select_channel_description_text);
        selectChannelText.setText(getResources().getString(R.string.channel_selection_description_text));
        selectChannelText.setVisibility(View.VISIBLE);
        channelMode = 0;

        // Axis Indicator
        xCoordinateText = v.findViewById(R.id.x_coordinate_text);
        xCoordinateText.setText("Time:  0.0 mS");
        progressBar = v.findViewById(R.id.la_progressBar);
        progressBar.setVisibility(View.GONE);
        ((LogicalAnalyzerActivity) getActivity()).setStatus(false);

        // Bottom Sheet guide
        bottomSheet = (LinearLayout) v.findViewById(R.id.bottom_sheet);
        tvShadow = (View) v.findViewById(R.id.shadow);
        arrowUpDown = (ImageView) v.findViewById(R.id.img_arrow);
        bottomSheetSlideText = (TextView) v.findViewById(R.id.sheet_slide_text);
        bottomSheetGuideTitle = (TextView) v.findViewById(R.id.guide_title);
        bottomSheetText = (TextView) v.findViewById(R.id.custom_dialog_text);
        bottomSheetSchematic = (ImageView) v.findViewById(R.id.custom_dialog_schematic);
        bottomSheetDesc = (TextView) v.findViewById(R.id.custom_dialog_desc);

        // Inflating bottom sheet dialog on how to use Logic Analyzer
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        setUpBottomSheet();
        v.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });

        // Creating base layout for chart
        logicLinesChart = v.findViewById(R.id.chart_la);
        Legend legend = logicLinesChart.getLegend();
        legend.setTextColor(Color.WHITE);
        logicLinesChart.setBorderWidth(2);
        XAxis xAxis = logicLinesChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.TOP);
        xAxis.setTextColor(Color.WHITE);

        setAdapters();
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (scienceLab.isConnected()) {
            new Thread(logicAnalysis).start();
        }

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
                    switch (carouselPicker.getCurrentItem()) {
                        case 0:
                            channelMode = 0;
                            selectChannelText.setVisibility(View.VISIBLE);
                            llChannel1.setVisibility(View.GONE);
                            llChannel2.setVisibility(View.GONE);
                            llChannel3.setVisibility(View.GONE);
                            llChannel4.setVisibility(View.GONE);
                            break;
                        case 1:
                            channelMode = 1;
                            selectChannelText.setVisibility(View.GONE);
                            llChannel1.setVisibility(View.VISIBLE);
                            llChannel2.setVisibility(View.GONE);
                            llChannel3.setVisibility(View.GONE);
                            llChannel4.setVisibility(View.GONE);
                            channelSelectSpinner1.setEnabled(true);
                            break;
                        case 2:
                            channelMode = 2;
                            selectChannelText.setVisibility(View.GONE);
                            llChannel1.setVisibility(View.VISIBLE);
                            llChannel2.setVisibility(View.VISIBLE);
                            llChannel3.setVisibility(View.GONE);
                            llChannel4.setVisibility(View.GONE);
                            channelSelectSpinner1.setEnabled(true);
                            channelSelectSpinner2.setEnabled(true);
                            break;
                        case 3:
                            channelMode = 3;
                            selectChannelText.setVisibility(View.GONE);
                            llChannel1.setVisibility(View.VISIBLE);
                            llChannel2.setVisibility(View.VISIBLE);
                            llChannel3.setVisibility(View.VISIBLE);
                            llChannel4.setVisibility(View.GONE);
                            channelSelectSpinner1.setSelection(0);
                            channelSelectSpinner2.setSelection(1);
                            channelSelectSpinner3.setSelection(2);
                            channelSelectSpinner1.setEnabled(false);
                            channelSelectSpinner2.setEnabled(false);
                            channelSelectSpinner3.setEnabled(false);
                            break;
                        case 4:
                            channelMode = 4;
                            selectChannelText.setVisibility(View.GONE);
                            llChannel1.setVisibility(View.VISIBLE);
                            llChannel2.setVisibility(View.VISIBLE);
                            llChannel3.setVisibility(View.VISIBLE);
                            llChannel4.setVisibility(View.VISIBLE);
                            channelSelectSpinner1.setSelection(0);
                            channelSelectSpinner2.setSelection(1);
                            channelSelectSpinner3.setSelection(2);
                            channelSelectSpinner4.setSelection(3);
                            channelSelectSpinner1.setEnabled(false);
                            channelSelectSpinner2.setEnabled(false);
                            channelSelectSpinner3.setEnabled(false);
                            channelSelectSpinner4.setEnabled(false);
                            break;
                        default:
                            channelMode = 1;
                            selectChannelText.setVisibility(View.GONE);
                            llChannel1.setVisibility(View.VISIBLE);
                            llChannel2.setVisibility(View.GONE);
                            llChannel3.setVisibility(View.GONE);
                            llChannel4.setVisibility(View.GONE);
                            channelSelectSpinner1.setEnabled(true);
                            break;
                    }
                }
            }
        });

        analyze_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (channelMode > 0) {
                    if (scienceLab.isConnected()) {
                        analyze_button.setClickable(false);
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

                        if (channelMode == 1) {
                            progressBar.setVisibility(View.VISIBLE);
                            ((LogicalAnalyzerActivity) getActivity()).setStatus(true);
                            Thread monitor = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    captureOne = new CaptureOne();
                                    captureOne.execute(channelNames.get(0));
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
                        } else {
                            Toast.makeText(getContext(), getResources().getString(R.string.needs_implementation), Toast.LENGTH_SHORT).show();
                        }

                        // Setting cursor to display time at highlighted points
                        listener = new OnChartValueSelectedListener() {
                            @Override
                            public void onValueSelected(Entry e, Highlight h) {
                                double result = Math.round(e.getX() * 100.0) / 100.0;
                                xCoordinateText.setText("Time:  " + String.valueOf(result) + " mS");
                                Log.i("Entry selected", e.toString());
                            }

                            @Override
                            public void onNothingSelected() {

                            }
                        };
                        logicLinesChart.setOnChartValueSelectedListener(listener);
                    } else
                        Toast.makeText(getContext(), getResources().getString(R.string.device_not_found), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void updateLogicLines(double[] xData, double[] yData) {

        tempInput = new ArrayList<>();
        for (int j = 0; j < channelMode; j++) {
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
            if (yaxis.get(1).equals(yaxis.get(0)))
                tempInput.add(new Entry(xaxis.get(0), yaxis.get(0)));
            else {
                tempInput.add(new Entry(xaxis.get(0), yaxis.get(0)));
                tempInput.add(new Entry(xaxis.get(0), yaxis.get(1)));
            }
            for (int i = 1; i < xaxis.size() - 1; i++) {
                if (yaxis.get(i).equals(yaxis.get(i + 1)))
                    tempInput.add(new Entry(xaxis.get(i), yaxis.get(i)));
                else {
                    tempInput.add(new Entry(xaxis.get(i), yaxis.get(i)));
                    tempInput.add(new Entry(xaxis.get(i), yaxis.get(i + 1)));
                }

            }
            tempInput.add(new Entry(xaxis.get(xaxis.size() - 1), yaxis.get(xaxis.size() - 1)));
        }
    }

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

        // Items for Carousel Picker
        List<CarouselPicker.PickerItem> channelModes = new ArrayList<>();
        channelModes.add(new CarouselPicker.TextItem("SELECT", 9));
        channelModes.add(new CarouselPicker.TextItem("1", 9));
        channelModes.add(new CarouselPicker.TextItem("2", 9));
        channelModes.add(new CarouselPicker.TextItem("3", 9));
        channelModes.add(new CarouselPicker.TextItem("4", 9));

        CarouselPicker.CarouselViewAdapter channelAdapter = new CarouselPicker.CarouselViewAdapter(getContext(), channelModes, 0);
        carouselPicker.setAdapter(channelAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null)
            ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
    }

    @Override
    public void onStop() {
        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null)
            ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        super.onStop();
    }

    private void setUpBottomSheet() {
        gestureDetector = new GestureDetector(getContext(), new SwipeGestureDetector(bottomSheetBehavior));

        final SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Boolean isFirstTime = settings.getBoolean("LogicAnalyzerFirstTime", true);

        bottomSheetGuideTitle.setText(R.string.logical_analyzer);
        bottomSheetText.setText(R.string.logic_analyzer_dialog_text);
        bottomSheetSchematic.setImageResource(R.drawable.logic_analyzer_circuit);
        bottomSheetDesc.setText(R.string.logic_analyzer_dialog_description);

        if (isFirstTime) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            tvShadow.setAlpha(0.8f);
            arrowUpDown.setRotation(180);
            bottomSheetSlideText.setText(R.string.hide_guide_text);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("LogicAnalyzerFirstTime", false);
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
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                Float value = (float) MathUtils.map((double) slideOffset, 0.0, 1.0, 0.0, 0.8);
                tvShadow.setAlpha(value);
                arrowUpDown.setRotation(slideOffset * 180);
            }
        });
    }

    private class CaptureOne extends AsyncTask<String, Void, Void> {
        boolean holder;

        @Override
        protected Void doInBackground(String... params) {

            try {
                scienceLab.startOneChannelLA(params[0], 1, params[0], 3);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                LinkedHashMap<String, Integer> data = scienceLab.getLAInitialStates();

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                String[] channels = getResources().getStringArray(R.array.channel_choices);
                int channelNumber = 0;
                for (int i = 0; i < channels.length; i++) {
                    if (params[0].equals(channels[i])) {
                        channelNumber = i;
                        break;
                    }
                }
                holder = scienceLab.fetchLAChannel(channelNumber, data);
                digitalChannel = scienceLab.getDigitalChannel(channelNumber);
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

                String str = "";
                for (int i = 0; i < xaxis.length; i++) {
                    str += String.valueOf(xaxis[i]);
                    str += " ";
                }
                Log.v("x Axis", str);

                // Plot the fetched data
                updateLogicLines(xaxis, yaxis);
                progressBar.setVisibility(View.GONE);
                ((LogicalAnalyzerActivity) getActivity()).setStatus(false);

                List<ILineDataSet> dataSets = new ArrayList<>();
                LineDataSet lineDataSet = new LineDataSet(tempInput, channelNames.get(0));
                lineDataSet.setCircleRadius(1);
                lineDataSet.setLineWidth(2);
                lineDataSet.setCircleColor(Color.GREEN);
                lineDataSet.setDrawValues(false);
                lineDataSet.setDrawCircles(false);
                dataSets.add(lineDataSet);

                logicLinesChart.setData(new LineData(dataSets));
                logicLinesChart.invalidate();

                YAxis left = logicLinesChart.getAxisLeft();
                left.setValueFormatter(new ChannelAxisFormatter(channelNames));
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
                Toast.makeText(getContext(), getResources().getString(R.string.no_data_generated), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
