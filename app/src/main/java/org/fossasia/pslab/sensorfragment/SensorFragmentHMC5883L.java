package org.fossasia.pslab.sensorfragment;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.fossasia.pslab.R;
import org.fossasia.pslab.activity.SensorActivity;
import org.fossasia.pslab.communication.ScienceLab;
import org.fossasia.pslab.communication.peripherals.I2C;
import org.fossasia.pslab.communication.sensors.HMC5883L;
import org.fossasia.pslab.others.ScienceLabCommon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.fossasia.pslab.activity.SensorActivity.counter;

/**
 * Created by asitava on 10/7/17.
 */

public class SensorFragmentHMC5883L extends Fragment {

    private ScienceLab scienceLab;
    private SensorDataFetch sensorDataFetch;
    private TextView tvSensorHMC5883Lbx;
    private TextView tvSensorHMC5883Lby;
    private TextView tvSensorHMC5883Lbz;
    private HMC5883L sensorHMC5883L;
    private LineChart mChart;
    private long startTime;
    private int flag;
    private ArrayList<Entry> entriesBx;
    private ArrayList<Entry> entriesBy;
    private ArrayList<Entry> entriesBz;
    private final Object lock = new Object();


    public static SensorFragmentHMC5883L newInstance() {
        return new SensorFragmentHMC5883L();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scienceLab = ScienceLabCommon.scienceLab;
        I2C i2c = scienceLab.i2c;

        entriesBx = new ArrayList<>();
        entriesBy = new ArrayList<>();
        entriesBz = new ArrayList<>();
        ((SensorActivity) getActivity()).sensorDock.setVisibility(View.VISIBLE);
        try {
            sensorHMC5883L = new HMC5883L(i2c);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (scienceLab.isConnected() && ((SensorActivity) getActivity()).shouldPlay()) {
                        sensorDataFetch = new SensorDataFetch();
                        sensorDataFetch.execute();
                        if (flag == 0) {
                            startTime = System.currentTimeMillis();
                            flag = 1;
                        }
                        synchronized (lock) {
                            try {
                                lock.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        try {
                            Thread.sleep(((SensorActivity) getActivity()).timeGap);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        new Thread(runnable).start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sensor_hmc5883l, container, false);

        tvSensorHMC5883Lbx = (TextView) view.findViewById(R.id.tv_sensor_hmc5883l_bx);
        tvSensorHMC5883Lby = (TextView) view.findViewById(R.id.tv_sensor_hmc5883l_by);
        tvSensorHMC5883Lbz = (TextView) view.findViewById(R.id.tv_sensor_hmc5883l_bz);
        mChart = (LineChart) view.findViewById(R.id.chart_hmc5883l);
        XAxis x = mChart.getXAxis();
        YAxis y = mChart.getAxisLeft();
        YAxis y2 = mChart.getAxisRight();

        mChart.setTouchEnabled(true);
        mChart.setHighlightPerDragEnabled(true);
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);
        mChart.setPinchZoom(true);
        mChart.setScaleYEnabled(false);
        mChart.setBackgroundColor(Color.BLACK);
        mChart.getDescription().setEnabled(false);

        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);
        mChart.setData(data);

        Legend l = mChart.getLegend();
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.WHITE);

        x.setTextColor(Color.WHITE);
        x.setDrawGridLines(true);
        x.setAvoidFirstLastClipping(true);

        y.setTextColor(Color.WHITE);
        y.setAxisMaximum(10f);
        y.setAxisMinimum(-10f);
        y.setDrawGridLines(true);
        y.setLabelCount(10);

        y2.setDrawGridLines(false);
        return view;
    }


    private class SensorDataFetch extends AsyncTask<Void, Void, Void> {

        private ArrayList<Double> dataHMC5883L = new ArrayList<>();
        private long timeElapsed;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (sensorHMC5883L != null) {
                    dataHMC5883L = sensorHMC5883L.getRaw();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            timeElapsed = (System.currentTimeMillis() - startTime) / 1000;
            entriesBx.add(new Entry((float) timeElapsed, dataHMC5883L.get(0).floatValue()));
            entriesBy.add(new Entry((float) timeElapsed, dataHMC5883L.get(1).floatValue()));
            entriesBz.add(new Entry((float) timeElapsed, dataHMC5883L.get(2).floatValue()));
            return null;
        }

        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            tvSensorHMC5883Lbx.setText(String.valueOf(dataHMC5883L.get(0)));
            tvSensorHMC5883Lby.setText(String.valueOf(dataHMC5883L.get(1)));
            tvSensorHMC5883Lbz.setText(String.valueOf(dataHMC5883L.get(2)));

            LineDataSet dataset1 = new LineDataSet(entriesBx, getString(R.string.bx));
            LineDataSet dataSet2 = new LineDataSet(entriesBy, getString(R.string.by));
            LineDataSet dataSet3 = new LineDataSet(entriesBz, getString(R.string.bz));

            dataset1.setColor(Color.BLUE);
            dataSet2.setColor(Color.GREEN);
            dataSet3.setColor(Color.RED);

            dataset1.setDrawCircles(true);
            dataSet2.setDrawCircles(true);
            dataSet3.setDrawCircles(true);

            List<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(dataset1);
            dataSets.add(dataSet2);
            dataSets.add(dataSet3);

            LineData data = new LineData(dataSets);
            mChart.setData(data);
            mChart.notifyDataSetChanged();
            mChart.setVisibleXRangeMaximum(10);
            mChart.moveViewToX(data.getEntryCount());
            mChart.invalidate();

            ((SensorActivity) getActivity()).samplesEditBox.setText(String.valueOf(counter));
            if(counter == 0 && !((SensorActivity) getActivity()).runIndefinitely) {
                ((SensorActivity) getActivity()).play = false;
                ((SensorActivity) getActivity()).playPauseButton.setImageResource(R.drawable.play);
            }
            synchronized (lock) {
                lock.notify();
            }
        }
    }
}
