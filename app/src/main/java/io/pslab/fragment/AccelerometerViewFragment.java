package io.pslab.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

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

public class AccelerometerViewFragment extends Fragment {

    private TextView accelerationValue, accelerationMin, accelerationMax;
    private LineChart accelerationChart;
    private ImageView accelerationAxisImage;
    private YAxis y;
    private float currentMax = Integer.MIN_VALUE;
    private float currentMin = Integer.MAX_VALUE;
    private float currentValue = 0;
    private ArrayList<Entry> entries;
    private long startTime;
    private static int updatePeriod = 100;
    private long previousTimeElapsed = (System.currentTimeMillis() - startTime) / updatePeriod;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.accelerometer_list_item, container, false);

        this.accelerationValue = rootView.findViewById(R.id.acceleration_value);
        this.accelerationMax = rootView.findViewById(R.id.acceleration_max_text);
        this.accelerationMin = rootView.findViewById(R.id.acceleration_min_text);
        this.accelerationChart = rootView.findViewById(R.id.chart_accelerometer);
        this.accelerationAxisImage = rootView.findViewById(R.id.acceleration_axis_image);
        this.entries = new ArrayList<>();
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
       super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public ImageView getAccelerationAxisImage() {
        return accelerationAxisImage;
    }

    public float getCurrentMax() {
        return currentMax;
    }

    public void setCurrentMax(float currentMax) {
        this.currentMax = currentMax;
    }

    public float getCurrentMin() {
        return currentMin;
    }

    public void setCurrentMin(float currentMin) {
        this.currentMin = currentMin;
    }

    public float getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(float currentValue) {
        this.currentValue = currentValue;
    }

    public void setUp() {
        XAxis x = this.accelerationChart.getXAxis();
        this.y = this.accelerationChart.getAxisLeft();
        YAxis y2 = this.accelerationChart.getAxisRight();

        this.accelerationChart.setTouchEnabled(true);
        this.accelerationChart.setHighlightPerDragEnabled(true);
        this.accelerationChart.setDragEnabled(true);
        this.accelerationChart.setScaleEnabled(true);
        this.accelerationChart.setDrawGridBackground(false);
        this.accelerationChart.setPinchZoom(true);
        this.accelerationChart.setScaleYEnabled(true);
        this.accelerationChart.setBackgroundColor(Color.BLACK);
        this.accelerationChart.getDescription().setEnabled(false);

        LineData data = new LineData();
        this.accelerationChart.setData(data);

        Legend l = this.accelerationChart.getLegend();
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

    public void addEntry(Entry entry) {
        this.entries.add(entry);
    }

    public ArrayList<Entry> getEntries() {
        return this.entries;
    }

    public void clearEntry() {
        this.entries.clear();
    }

    public void setAccelerationValue(CharSequence value) {
        this.accelerationValue.setText(value);
    }

    public void setAccelerationMax(CharSequence value) {
        this.accelerationMax.setText(value);
    }

    public void setAccelerationMin(CharSequence value) {
        this.accelerationMin.setText(value);
    }

    public void setYaxis(float maxLimit) {
        this.y.setAxisMaximum(maxLimit);
        this.y.setAxisMinimum(-maxLimit);
        this.y.setLabelCount(5);
    }

    public void setChartData(LineData data) {
        this.accelerationChart.setData(data);
        this.accelerationChart.notifyDataSetChanged();
        this.accelerationChart.setVisibleXRangeMaximum(3);
        this.accelerationChart.moveViewToX(data.getEntryCount());
        this.accelerationChart.invalidate();
    }

    public void clear() {
        this.currentMax = Integer.MIN_VALUE;
        this.currentMin = Integer.MAX_VALUE;
        this.entries.clear();
        this.accelerationChart.clear();
        this.accelerationChart.invalidate();
        this.startTime = System.currentTimeMillis();
    }

    public long getPreviousTimeElapsed() {
        return previousTimeElapsed;
    }

    public void setPreviousTimeElapsed(long previousTimeElapsed) {
        this.previousTimeElapsed = previousTimeElapsed;
    }
}
