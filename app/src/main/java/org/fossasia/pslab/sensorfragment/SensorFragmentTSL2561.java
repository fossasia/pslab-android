package org.fossasia.pslab.sensorfragment;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;
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
import org.fossasia.pslab.communication.ScienceLab;
import org.fossasia.pslab.communication.peripherals.I2C;
import org.fossasia.pslab.communication.sensors.TSL2561;
import org.fossasia.pslab.others.ScienceLabCommon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by asitava on 10/7/17.
 */

public class SensorFragmentTSL2561 extends Fragment {
    private ScienceLab scienceLab;
    private I2C i2c;
    private SensorDataFetch sensorDataFetch;
    private TextView tvSensorTSL2561FullSpectrum;
    private TextView tvSensorTSL2561Infrared;
    private TextView tvSensorTSL2561Visible;
    private Spinner spinnerSensorTSL2561Gain;
    private EditText etSensorTSL2561Timing;
    private TSL2561 sensorTSL2561;
    private LineChart mChart;
    private long startTime;
    private int flag;
    private XAxis x;
    private YAxis y;
    private YAxis y2;
    private ArrayList<Entry> entriesfull;
    private ArrayList<Entry> entriesinfrared;
    private ArrayList<Entry> entriesvisible;
    private final Object lock = new Object();

    public static SensorFragmentTSL2561 newInstance() {
        SensorFragmentTSL2561 sensorFragmentTSL2561 = new SensorFragmentTSL2561();
        return sensorFragmentTSL2561;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scienceLab = ScienceLabCommon.scienceLab;
        i2c = scienceLab.i2c;

        entriesfull = new ArrayList<Entry>();
        entriesinfrared = new ArrayList<Entry>();
        entriesvisible = new ArrayList<Entry>();

        try {
            sensorTSL2561 = new TSL2561(i2c);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (scienceLab.isConnected()) {
                        try {
                            sensorDataFetch = new SensorDataFetch();
                        } catch (IOException | InterruptedException e) {
                            e.printStackTrace();
                        }
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
                            Thread.sleep(500);
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
        View view = inflater.inflate(R.layout.sensor_tsl2561, container, false);
        tvSensorTSL2561FullSpectrum = (TextView) view.findViewById(R.id.tv_sensor_tsl2561_full);
        tvSensorTSL2561Infrared = (TextView) view.findViewById(R.id.tv_sensor_tsl2561_infrared);
        tvSensorTSL2561Visible = (TextView) view.findViewById(R.id.tv_sensor_tsl2561_visible);
        spinnerSensorTSL2561Gain = (Spinner) view.findViewById(R.id.spinner_sensor_tsl2561_gain);
        etSensorTSL2561Timing = (EditText) view.findViewById(R.id.et_sensor_tsl2561_timing);
        mChart = (LineChart) view.findViewById(R.id.chart_tsl2561);

        try {
            if (sensorTSL2561 != null) {
                sensorTSL2561.setGain(spinnerSensorTSL2561Gain.getSelectedItem().toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        x = mChart.getXAxis();
        y = mChart.getAxisLeft();
        y2 = mChart.getAxisRight();

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
        y.setAxisMaximum(1700f);
        y.setAxisMinimum(0f);
        y.setDrawGridLines(true);
        y.setLabelCount(10);

        y2.setDrawGridLines(false);


        return view;
    }


    private class SensorDataFetch extends AsyncTask<Void, Void, Void> {
        TSL2561 sensorTSL2561 = new TSL2561(i2c);
        private int[] dataTSL2561;
        long timeElapsed;

        private SensorDataFetch() throws IOException, InterruptedException {
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (sensorTSL2561 != null) {
                    dataTSL2561 = sensorTSL2561.getRaw();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            timeElapsed = (System.currentTimeMillis() - startTime) / 1000;
            entriesfull.add(new Entry((float) timeElapsed, dataTSL2561[0]));
            entriesinfrared.add(new Entry((float) timeElapsed, dataTSL2561[1]));
            entriesvisible.add(new Entry((float) timeElapsed, dataTSL2561[2]));
            return null;
        }

        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            tvSensorTSL2561FullSpectrum.setText(String.valueOf(dataTSL2561[0]));
            tvSensorTSL2561Infrared.setText(String.valueOf(dataTSL2561[1]));
            tvSensorTSL2561Visible.setText(String.valueOf(dataTSL2561[2]));

            LineDataSet dataset1 = new LineDataSet(entriesfull, "full");
            LineDataSet dataSet2 = new LineDataSet(entriesinfrared, "infrared");
            LineDataSet dataSet3 = new LineDataSet(entriesvisible, "visible");

            dataset1.setColor(Color.BLUE);
            dataSet2.setColor(Color.GREEN);
            dataSet3.setColor(Color.RED);

            dataset1.setDrawCircles(true);
            dataSet2.setDrawCircles(true);
            dataSet3.setDrawCircles(true);

            List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
            dataSets.add(dataset1);
            dataSets.add(dataSet2);
            dataSets.add(dataSet3);

            LineData data = new LineData(dataSets);
            mChart.setData(data);
            mChart.notifyDataSetChanged();
            mChart.setVisibleXRangeMaximum(10);
            mChart.moveViewToX(data.getEntryCount());
            mChart.invalidate();

            synchronized (lock) {
                lock.notify();
            }
        }
    }
}