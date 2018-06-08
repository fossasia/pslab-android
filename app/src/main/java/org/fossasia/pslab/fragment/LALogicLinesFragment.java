package org.fossasia.pslab.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

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

import org.fossasia.pslab.PSLabApplication;
import org.fossasia.pslab.R;
import org.fossasia.pslab.communication.ScienceLab;
import org.fossasia.pslab.others.ChannelAxisFormatter;
import org.fossasia.pslab.others.ScienceLabCommon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import in.goodiebag.carouselpicker.CarouselPicker;

/**
 * Created by viveksb007 on 9/6/17.
 */

public class LALogicLinesFragment extends Fragment {

    public static final String PREFS_NAME = "customDialogPreference";
    public CheckBox dontShowAgain;
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

    public static LALogicLinesFragment newInstance(Activity activity) {
        LALogicLinesFragment laLogicLinesFragment = new LALogicLinesFragment();
        laLogicLinesFragment.activity = activity;
        return laLogicLinesFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scienceLab = ScienceLabCommon.scienceLab;

        // Inflating custom dialog on how to use Logic Analyzer
        howToConnectDialog(getString(R.string.logic_analyzer_dialog_heading), getString(R.string.logic_analyzer_dialog_text), R.drawable.logic_analyzer_circuit, getString(R.string.logic_analyzer_dialog_description));

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
        View v = inflater.inflate(R.layout.logic_analyzer_logic_lines, container, false);

        ledImageView = v.findViewById(R.id.imageView_led_la);
        tvTimeUnit = v.findViewById(R.id.la_tv_time_unit);
        tvTimeUnit.setText(getString(R.string.time_unit_la));
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
        xCoordinateText = v.findViewById(R.id.x_coordinate_text);
        xCoordinateText.setText("Time:  0.0 mS");
        selectChannelText = (TextView) v.findViewById(R.id.select_channel_description_text);
        selectChannelText.setText(getResources().getString(R.string.channel_selection_description_text));
        selectChannelText.setVisibility(View.VISIBLE);
        channelMode = 0;

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
                    }
                }
            }
        });

        analyze_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (channelMode > 0) {
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
                    }
                    updateLogicLines();
                    YAxis left = logicLinesChart.getAxisLeft();
                    left.setValueFormatter(new ChannelAxisFormatter(channelNames));
                    left.setTextColor(Color.WHITE);
                    left.setGranularity(1f);
                    left.setTextSize(12f);
                    logicLinesChart.getAxisRight().setDrawLabels(false);
                    logicLinesChart.getDescription().setEnabled(false);
                    logicLinesChart.setScaleYEnabled(false);

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
                            Log.i("Nothing selected", "Nothing selected.");
                        }
                    };
                    logicLinesChart.setOnChartValueSelectedListener(listener);
                }
            }
        });

    }

    private void updateLogicLines() {
        boolean high = false;
        List<ILineDataSet> dataSets = new ArrayList<>();
        ArrayList<int[]> timeStamps = generateRandomTimeStamps(channelMode);
        for (int j = 0; j < channelMode; j++) {
            List<Entry> tempInput = new ArrayList<>();
            int[] temp = timeStamps.get(j);
            tempInput.add(new Entry(0, 0 + (j * 2)));
            for (int aTemp : temp) {
                if (high) {
                    tempInput.add(new Entry(aTemp, 1 + (j * 2)));
                    tempInput.add(new Entry(aTemp, 0 + (j * 2)));
                } else {
                    tempInput.add(new Entry(aTemp, 0 + (j * 2)));
                    tempInput.add(new Entry(aTemp, 1 + (j * 2)));
                }
                high = !high;
            }
            LineDataSet lineDataSet = new LineDataSet(tempInput, channelNames.get(j));
            lineDataSet.setCircleRadius(1);
            switch (j) {
                case 0:
                    lineDataSet.setColor(Color.MAGENTA);
                    break;
                case 1:
                    lineDataSet.setColor(Color.GREEN);
                    break;
                case 2:
                    lineDataSet.setColor(Color.CYAN);
                    break;
                case 3:
                    lineDataSet.setColor(Color.YELLOW);
                    break;
                default:
                    lineDataSet.setColor(Color.MAGENTA);
                    break;
            }
            lineDataSet.setLineWidth(2);
            lineDataSet.setCircleColor(Color.GREEN);
            lineDataSet.setDrawValues(false);
            lineDataSet.setDrawCircles(false);
            lineDataSet.setHighLightColor(getResources().getColor(R.color.golden));
            dataSets.add(lineDataSet);
        }
        logicLinesChart.setData(new LineData(dataSets));
        logicLinesChart.invalidate();
    }

    private ArrayList<int[]> generateRandomTimeStamps(int channelMode) {
        ArrayList<int[]> data = new ArrayList<>();
        for (int j = 0; j < channelMode; j++) {
            int[] temp = new int[10];
            Random random = new Random();
            for (int i = 0; i < 10; i++) {
                temp[i] = random.nextInt(((5 * (i + 1) - 1) - (5 * i + 1)) + 1) + (5 * i + 1);
            }
            data.add(temp);
        }
        for (int[] temp : data) {
            Log.v("timestamp", Arrays.toString(temp));
        }
        return data;
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

    @SuppressLint("ResourceType")
    public void howToConnectDialog(String title, String intro, int iconID, String desc) {
        try {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.custom_dialog_box, null);
            final SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
            Boolean skipMessage = settings.getBoolean("skipMessage", false);

            dontShowAgain = (CheckBox) dialogView.findViewById(R.id.toggle_show_again);
            final TextView heading_text = (TextView) dialogView.findViewById(R.id.custom_dialog_text);
            final TextView description_text = (TextView) dialogView.findViewById(R.id.description_text);
            final ImageView schematic = (ImageView) dialogView.findViewById(R.id.custom_dialog_schematic);
            final Button ok_button = (Button) dialogView.findViewById(R.id.dismiss_button);

            builder.setView(dialogView);
            builder.setTitle(title);
            heading_text.setText(intro);
            schematic.setImageResource(iconID);
            description_text.setText(desc);
            final AlertDialog dialog = builder.create();
            ok_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Boolean checkBoxResult = false;
                    if (dontShowAgain.isChecked())
                        checkBoxResult = true;
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean("skipMessage", checkBoxResult);
                    editor.apply();
                    dialog.dismiss();
                }
            });
            if (!skipMessage)
                dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        ((PSLabApplication) getActivity().getApplication()).refWatcher.watch(this, LALogicLinesFragment.class.getSimpleName());
    }
}
