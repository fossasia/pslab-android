package org.fossasia.pslab.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.apache.commons.math3.geometry.euclidean.threed.Line;
import org.fossasia.pslab.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by viveksb007 on 9/6/17.
 */

public class LALogicLinesFragment extends Fragment {

    private Bundle params;
    private int channelMode;
    private Context context;
    private LineChart logicLine1, logicLine2, logicLine3, logicLine4;

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
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.logic_analyzer_logic_lines, container, false);
        LinearLayout llLogicLines = (LinearLayout) v.findViewById(R.id.ll_la_logic_lines);
        llLogicLines.setWeightSum(channelMode);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
        for (int i = 0; i < channelMode; i++) {
            switch (i) {
                case 0:
                    logicLine1 = new LineChart(context);
                    logicLine1.setLayoutParams(params);
                    logicLine1.setDrawBorders(true);
                    logicLine1.setBorderWidth(2);
                    llLogicLines.addView(logicLine1);
                    break;
                case 1:
                    logicLine2 = new LineChart(context);
                    logicLine2.setLayoutParams(params);
                    logicLine2.setDrawBorders(true);
                    logicLine2.setBorderWidth(2);
                    llLogicLines.addView(logicLine2);
                    break;
                case 2:
                    logicLine3 = new LineChart(context);
                    logicLine3.setLayoutParams(params);
                    logicLine3.setDrawBorders(true);
                    logicLine3.setBorderWidth(2);
                    llLogicLines.addView(logicLine3);
                    break;
                case 3:
                    logicLine4 = new LineChart(context);
                    logicLine4.setLayoutParams(params);
                    logicLine4.setDrawBorders(true);
                    logicLine4.setBorderWidth(2);
                    llLogicLines.addView(logicLine4);
                    break;
            }
        }
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateLogicLines();
    }

    private void updateLogicLines() {
        List<Entry> entries = new ArrayList<>();
        boolean high = false;
        for (int i = 1; i <= 60; i++) {
            if (high) {
                entries.add(new Entry(i, 1));
            } else {
                entries.add(new Entry(i, 0));
            }
            if (i % 5 == 0) high = !high;
        }
        LineDataSet dataSet = new LineDataSet(entries, "Logic Line 1");
        dataSet.setColor(Color.RED);
        dataSet.setCircleRadius(0.1f);
        dataSet.setDrawValues(false);
        switch (channelMode) {
            case 1:
                logicLine1.setData(new LineData(dataSet));
                logicLine1.invalidate();
                break;
            case 2:
                logicLine1.setData(new LineData(dataSet));
                logicLine1.invalidate();
                logicLine2.setData(new LineData(dataSet));
                logicLine2.invalidate();
                break;
            case 3:
                logicLine1.setData(new LineData(dataSet));
                logicLine1.invalidate();
                logicLine2.setData(new LineData(dataSet));
                logicLine2.invalidate();
                logicLine3.setData(new LineData(dataSet));
                logicLine3.invalidate();
                break;
            case 4:
                logicLine1.setData(new LineData(dataSet));
                logicLine1.invalidate();
                logicLine2.setData(new LineData(dataSet));
                logicLine2.invalidate();
                logicLine3.setData(new LineData(dataSet));
                logicLine3.invalidate();
                logicLine4.setData(new LineData(dataSet));
                logicLine4.invalidate();
                break;
        }
    }
}
