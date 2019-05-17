package io.pslab.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;

import java.util.ArrayList;

import io.pslab.R;

public class GyroscopeViewFragment extends Fragment {

    private TextView gyroValue, gyroMin, gyroMax;
    private LineChart gyroChart;
    private ImageView gyroAxisImage;
    private YAxis y;
    private float currentMax = Integer.MIN_VALUE;
    private float currentMin = Integer.MAX_VALUE;
    private float currentValue = 0;
    private ArrayList<Entry> entries;
    private long startTime;
    private static int updatePeriod = 100;
    private long previousTimeElapsed = (System.currentTimeMillis() - startTime) / updatePeriod;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.gyroscope_list_item, container, false);

        this.gyroValue = rootView.findViewById(R.id.gyro_value);
        this.gyroMax = rootView.findViewById(R.id.gyro_max_text);
        this.gyroMin = rootView.findViewById(R.id.gyro_min_text);
        this.gyroChart = rootView.findViewById(R.id.chart_gyroscope);
        this.gyroAxisImage = rootView.findViewById(R.id.axis_image);
        this.entries = new ArrayList<>();
        return rootView;
    }

    public ImageView getGyroAxisImage() {
        return gyroAxisImage;
    }

    public LineChart getGyroChart() {
        return gyroChart;
    }

    public TextView getGyroMax() {
        return gyroMax;
    }

    public TextView getGyroMin() {
        return gyroMin;
    }

    public TextView getGyroValue() {
        return gyroValue;
    }

    public void setUp() {
        XAxis x = this.gyroChart.getXAxis();
        this.y = this.gyroChart.getAxisLeft();
        YAxis y2 = this.gyroChart.getAxisRight();

        this.gyroChart.setTouchEnabled(true);
        this.gyroChart.setHighlightPerDragEnabled(true);
        this.gyroChart.setDragEnabled(true);
        this.gyroChart.setScaleEnabled(true);
        this.gyroChart.setDrawGridBackground(false);
        this.gyroChart.setPinchZoom(true);
        this.gyroChart.setScaleYEnabled(true);
        this.gyroChart.setBackgroundColor(Color.BLACK);
        this.gyroChart.getDescription().setEnabled(false);

        LineData data = new LineData();
        this.gyroChart.setData(data);

        Legend l = this.gyroChart.getLegend();
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.WHITE);

        x.setTextColor(Color.WHITE);
        x.setDrawGridLines(true);
        x.setAvoidFirstLastClipping(true);
        x.setDrawLabels(false);

        this.y.setTextColor(Color.WHITE);
        this.y.setAxisMaximum(currentMax);
        this.y.setAxisMinimum(currentMin);
        this.y.setDrawGridLines(true);
        this.y.setLabelCount(10);

        y2.setDrawGridLines(false);
        y2.setMaxWidth(0);
    }

    public void setCurrentMax(float currentMax) {
        this.currentMax = currentMax;
    }

    public float getCurrentMax() {
        return this.currentMax;
    }

    public void setCurrentMin(float currentMin) {
        this.currentMin = currentMin;
    }

    public float getCurrentMin() {
        return this.currentMin;
    }

    public void setCurrentValue(float currentValue) {
        this.currentValue = currentValue;
    }

    public float getCurrentValue() {
        return this.currentValue;
    }

    public void addEntry(Entry entry) {
        this.entries.add(entry);
    }

    public ArrayList<Entry> getEntries() {
        return this.entries;
    }

    public void clearEntry() {
        this.entries.clear();
    }

    public void setGyroValue(CharSequence value) {
        this.gyroValue.setText(value);
    }

    public void setGyroMax(CharSequence value) {
        this.gyroMax.setText(value);
    }

    public void setGyroMin(CharSequence value) {
        this.gyroMin.setText(value);
    }

    public void setYaxis(float maxLimit) {
        this.y.setAxisMaximum(maxLimit);
        this.y.setAxisMinimum(-maxLimit);
        this.y.setLabelCount(5);
    }

    public void setChartData(LineData data) {
        this.gyroChart.setData(data);
        this.gyroChart.notifyDataSetChanged();
        this.gyroChart.setVisibleXRangeMaximum(3);
        this.gyroChart.moveViewToX(data.getEntryCount());
        this.gyroChart.invalidate();
    }

    public void clear() {
        this.currentMax = Integer.MIN_VALUE;
        this.currentMin = Integer.MAX_VALUE;
        this.entries.clear();
        this.gyroChart.clear();
        this.gyroChart.invalidate();
        this.startTime = System.currentTimeMillis();
    }

    public long getPreviousTimeElapsed() {
        return previousTimeElapsed;
    }

    public void setPreviousTimeElapsed(long previousTimeElapsed) {
        this.previousTimeElapsed = previousTimeElapsed;
    }
}
