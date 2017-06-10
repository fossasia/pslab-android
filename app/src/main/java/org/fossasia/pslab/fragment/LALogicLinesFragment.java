package org.fossasia.pslab.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;

import org.apache.commons.math3.geometry.euclidean.threed.Line;
import org.fossasia.pslab.R;

/**
 * Created by viveksb007 on 9/6/17.
 */

public class LALogicLinesFragment extends Fragment {

    private Bundle params;
    private int channelMode;
    private Context context;

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
        for (int i = 0; i < channelMode; i++) {
            llLogicLines.addView(new LineChart(context));
        }
        Toast.makeText(getContext(), "" + channelMode, Toast.LENGTH_SHORT).show();
        return v;
    }

}
