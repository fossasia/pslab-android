package org.fossasia.pslab.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;

import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;


import org.fossasia.pslab.communication.ScienceLab;
import org.fossasia.pslab.fragment.ChannelParametersFragment;
import org.fossasia.pslab.fragment.DataAnalysisFragment;
import org.fossasia.pslab.fragment.TimebaseTiggerFragment;
import org.fossasia.pslab.fragment.XYPlotFragment;
import org.fossasia.pslab.others.ScienceLabCommon;
import org.fossasia.pslab.R;

/**
 * Created by viveksb007 on 10/5/17.
 */

public class OscilloscopeActivity extends AppCompatActivity implements
        ChannelParametersFragment.OnFragmentInteractionListener,
        TimebaseTiggerFragment.OnFragmentInteractionListener,
        DataAnalysisFragment.OnFragmentInteractionListener,
        XYPlotFragment.OnFragmentInteractionListener {

    private ScienceLab scienceLab;
    public LineChart mChart;
    private LinearLayout linearLayout;
    private FrameLayout frameLayout;
    private ImageButton channelParametersButton;
    private ImageButton timebaseButton;
    private ImageButton dataAnalysisButton;
    private ImageButton xyPlotButton;
    private RelativeLayout mChartLayout;
    int height;
    int width;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oscilloscope);

        scienceLab = ScienceLabCommon.getInstance().scienceLab;
        linearLayout = (LinearLayout) findViewById(R.id.layout_dock_os1);
        mChart = (LineChart) findViewById(R.id.chart_os);
        mChartLayout = (RelativeLayout) findViewById(R.id.layout_chart_os);
        frameLayout = (FrameLayout) findViewById(R.id.layout_dock_os2);
        channelParametersButton = (ImageButton) findViewById(R.id.button_channel_parameters_os);
        timebaseButton = (ImageButton) findViewById(R.id.button_timebase_os);
        dataAnalysisButton = (ImageButton) findViewById(R.id.button_data_analysis_os);
        xyPlotButton = (ImageButton) findViewById(R.id.button_xy_plot_os);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;

        onWindowFocusChanged();

        final Fragment channelParametersFragment = new ChannelParametersFragment();
        final Fragment timebasetiggerFragment = new TimebaseTiggerFragment();
        final Fragment dataAnalysisFragment = new DataAnalysisFragment();
        final Fragment xyPlotFragment = new XYPlotFragment();

        if (findViewById(R.id.layout_dock_os2) != null) {
            addFragment(R.id.layout_dock_os2, channelParametersFragment, "ChannelParametersFragment");
        }

        // going to define a single method handling all the onClick listeners.
        channelParametersButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                replaceFragment(R.id.layout_dock_os2, channelParametersFragment, "ChannelParametersFragment");
                return false;
            }
        });

        timebaseButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                replaceFragment(R.id.layout_dock_os2, timebasetiggerFragment, "TimebaseTiggerFragment");
                return false;
            }
        });

        dataAnalysisButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                replaceFragment(R.id.layout_dock_os2, dataAnalysisFragment, "DataAnalysisFragment");
                return false;
            }
        });

        xyPlotButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                replaceFragment(R.id.layout_dock_os2, xyPlotFragment, "XYPlotFragment");
                return false;
            }
        });

        chartInit(mChart);
    }

    public void onWindowFocusChanged() {
        boolean tabletSize = getResources().getBoolean(R.bool.isTablet);
        //dynamic placing the layouts
        if(tabletSize){
            RelativeLayout.LayoutParams lineChartParams = (RelativeLayout.LayoutParams) mChartLayout.getLayoutParams();
            lineChartParams.height = height * 3 / 4;
            lineChartParams.width = width * 7 / 8;
            mChartLayout.setLayoutParams(lineChartParams);
            RelativeLayout.LayoutParams frameLayoutParams = (RelativeLayout.LayoutParams) frameLayout.getLayoutParams();
            frameLayoutParams.height = height / 4;
            frameLayoutParams.width = width * 7 / 8;
            frameLayout.setLayoutParams(frameLayoutParams);
        }
        else{
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
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Closing Oscilloscope")
                .setMessage("Are you sure you want to close the Oscilloscope?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }

                })
                .setNegativeButton("No", null)
                .show();
    }

    public void chartInit(LineChart mChart){
        mChart.setTouchEnabled(true);
        mChart.setHighlightPerDragEnabled(true);
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);
        mChart.setPinchZoom(true);
        mChart.setBackgroundColor(Color.BLACK);

        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);
        mChart.setData(data);

        Legend l = new Legend();
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.WHITE);

        XAxis x1 = mChart.getXAxis();
        x1.setTextColor(Color.WHITE);
        x1.setDrawGridLines(true);
        x1.setAvoidFirstLastClipping(true);

        YAxis y1 = mChart.getAxisLeft();
        y1.setTextColor(Color.WHITE);
        y1.setAxisMaximum(4f);
        y1.setAxisMinimum(-4f);
        y1.setDrawGridLines(true);

        YAxis y2 = mChart.getAxisRight();
        y2.setAxisMaximum(3f);
        y2.setAxisMinimum(0f);
        y2.setTextColor(Color.WHITE);
        y2.setEnabled(true);
    }
}
