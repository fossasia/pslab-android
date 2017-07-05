package org.fossasia.pslab.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.fossasia.pslab.R;
import org.fossasia.pslab.others.ChannelAxisFormatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by viveksb007 on 9/6/17.
 */

public class LALogicLinesFragment extends Fragment {

    private Bundle params;
    private int channelMode;
    private Context context;
    private LineChart logicLinesChart;
    private ArrayList<String> channelNames = new ArrayList<>();
    private TextView tvTimeUnit;

    public static LALogicLinesFragment newInstance(Bundle params, Context context) {
        LALogicLinesFragment laLogicLinesFragment = new LALogicLinesFragment();
        laLogicLinesFragment.params = params;
        laLogicLinesFragment.context = context;
        return laLogicLinesFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.channelMode = params.getInt("channelMode");
        switch (channelMode) {
            case 1:
                channelNames.add(params.getString("inputChannel1"));
                break;
            case 2:
                channelNames.add(params.getString("inputChannel1"));
                channelNames.add(params.getString("inputChannel2"));
                break;
            case 3:
                channelNames.add(params.getString("inputChannel1"));
                channelNames.add(params.getString("inputChannel2"));
                channelNames.add(params.getString("inputChannel3"));
                break;
            case 4:
                channelNames.add(params.getString("inputChannel1"));
                channelNames.add(params.getString("inputChannel2"));
                channelNames.add(params.getString("inputChannel3"));
                channelNames.add(params.getString("inputChannel4"));
                break;
            default:
                channelNames.add(params.getString("inputChannel1"));
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.logic_analyzer_logic_lines, container, false);

        LinearLayout llLogicLines = (LinearLayout) v.findViewById(R.id.ll_la_logic_lines);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        logicLinesChart = new LineChart(context);
        logicLinesChart.setLayoutParams(params);
        logicLinesChart.setDrawBorders(true);
        logicLinesChart.setBorderWidth(2);
        llLogicLines.addView(logicLinesChart);
        tvTimeUnit = (TextView) v.findViewById(R.id.la_tv_time_unit);
        tvTimeUnit.setText(getString(R.string.time_unit_la));
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateLogicLines();
        YAxis left = logicLinesChart.getAxisLeft();
        left.setValueFormatter(new ChannelAxisFormatter(channelNames));
        left.setGranularity(1f);
        left.setTextColor(Color.BLACK);
        left.setTextSize(12f);
        logicLinesChart.getAxisRight().setDrawLabels(false);
        logicLinesChart.getDescription().setEnabled(false);
        logicLinesChart.setScaleYEnabled(false);

        /*  For HIDING GRID LINES
        logicLinesChart.getAxisLeft().setDrawGridLines(false);
        logicLinesChart.getAxisRight().setDrawGridLines(false);
        logicLinesChart.getXAxis().setDrawGridLines(false);
        */
    }

    private void updateLogicLines() {
        boolean high = false;
        List<ILineDataSet> dataSets = new ArrayList<>();
        ArrayList<int[]> timeStamps = generateRandomTimeStamps(channelMode);
        for (int j = 0; j < channelMode; j++) {
            List<Entry> tempInput = new ArrayList<>();
            int[] temp = timeStamps.get(j);
            tempInput.add(new Entry(0, 0 + (j * 2)));
            for (int i = 0; i < temp.length; i++) {
                if (high) {
                    tempInput.add(new Entry(temp[i], 1 + (j * 2)));
                    tempInput.add(new Entry(temp[i], 0 + (j * 2)));
                } else {
                    tempInput.add(new Entry(temp[i], 0 + (j * 2)));
                    tempInput.add(new Entry(temp[i], 1 + (j * 2)));
                }
                high = !high;
            }
            LineDataSet lineDataSet = new LineDataSet(tempInput, channelNames.get(j));
            lineDataSet.setCircleRadius(1);
            lineDataSet.setColor(Color.RED);
            lineDataSet.setCircleColor(Color.GREEN);
            lineDataSet.setDrawValues(false);
            lineDataSet.setDrawCircles(false);
            dataSets.add(lineDataSet);
        }
        logicLinesChart.setData(new LineData(dataSets));
        logicLinesChart.invalidate();
        /*
        LineDataSet dataSet = new LineDataSet(entries, "Logic Lines");
        dataSet.setColor(Color.RED);
        dataSet.setCircleRadius(0.1f);
        dataSet.setDrawValues(false);
        */

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
}
