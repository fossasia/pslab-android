package io.pslab.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.anastr.speedviewlib.PointerSpeedometer;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.pslab.R;
import io.pslab.activity.GasSensorActivity;
import io.pslab.communication.ScienceLab;
import io.pslab.others.ScienceLabCommon;

public class GasSensorDataFragment extends Fragment {

    @BindView(R.id.gas_sensor_value)
    TextView gasValue;
    @BindView(R.id.label_gas_sensor)
    TextView sensorLabel;
    @BindView(R.id.chart_gas_sensor)
    LineChart mChart;
    @BindView(R.id.gas_sensor)
    PointerSpeedometer gasSensorMeter;
    private GasSensorActivity gasSensorActivity;
    private View rootView;
    private Unbinder unbinder;
    private ScienceLab scienceLab;
    private YAxis y;
    private Timer graphTimer;
    private ArrayList<Entry> entries;
    private long updatePeriod = 1000;
    private long startTime;
    private long timeElapsed;
    private long previousTimeElapsed = (System.currentTimeMillis() - startTime) / updatePeriod;

    public static GasSensorDataFragment newInstance() {
        return new GasSensorDataFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startTime = System.currentTimeMillis();
        gasSensorActivity = (GasSensorActivity) getActivity();
        graphTimer = new Timer();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_gas_sensor, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        scienceLab = ScienceLabCommon.scienceLab;
        entries = new ArrayList<>();
        setupInstruments();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mChart.clear();
        entries.clear();
        mChart.invalidate();
        updateGraphs();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (graphTimer != null) {
            graphTimer.cancel();
        }
        unbinder.unbind();
    }

    private void updateGraphs() {
        final Handler handler = new Handler();
        if (graphTimer != null) {
            graphTimer.cancel();
        }
        graphTimer = new Timer();
        graphTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            visualizeData();
                        } catch (NullPointerException e) {
                        }
                    }
                });
            }
        }, 0, 1000);
    }

    private void visualizeData() {
        if (scienceLab.isConnected()) {
            double volt = scienceLab.getVoltage("CH1", 1);
            double ppmValue = (volt / 3.3) * 1024.0;
            gasValue.setText(String.valueOf(String.format("%.2f", ppmValue)));
            gasSensorMeter.setWithTremble(false);
            gasSensorMeter.setSpeedAt((float) ppmValue);
            timeElapsed = ((System.currentTimeMillis() - startTime) / updatePeriod);
            if (timeElapsed != previousTimeElapsed) {
                previousTimeElapsed = timeElapsed;
                Entry entry = new Entry((float) timeElapsed, (float) ppmValue);
                entries.add(entry);

                LineDataSet dataSet = new LineDataSet(entries, getString(R.string.gas_sensor_unit));
                dataSet.setDrawCircles(false);
                dataSet.setDrawValues(false);
                dataSet.setLineWidth(2);
                LineData data = new LineData(dataSet);

                mChart.setData(data);
                mChart.notifyDataSetChanged();
                mChart.setVisibleXRangeMaximum(80);
                mChart.moveViewToX(data.getEntryCount());
                mChart.invalidate();
            }
        } else {
            Toast.makeText(getContext(), R.string.not_connected, Toast.LENGTH_SHORT).show();
        }
    }

    private void setupInstruments() {
        gasSensorMeter.setMaxSpeed(1024);
        XAxis x = mChart.getXAxis();
        this.y = mChart.getAxisLeft();
        YAxis y2 = mChart.getAxisRight();

        mChart.setTouchEnabled(true);
        mChart.setHighlightPerDragEnabled(true);
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);
        mChart.setPinchZoom(true);
        mChart.setScaleYEnabled(true);
        mChart.setBackgroundColor(Color.BLACK);
        mChart.getDescription().setEnabled(false);

        LineData data = new LineData();
        mChart.setData(data);

        Legend l = mChart.getLegend();
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.WHITE);

        x.setTextColor(Color.WHITE);
        x.setDrawGridLines(true);
        x.setAvoidFirstLastClipping(true);

        y.setTextColor(Color.WHITE);
        y.setAxisMaximum(1024);
        y.setAxisMinimum(0);
        y.setDrawGridLines(true);
        y.setLabelCount(10);

        y2.setDrawGridLines(false);
        y2.setMaxWidth(0);
    }
}
