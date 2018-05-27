package org.fossasia.pslab.sensorfragment;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.github.anastr.speedviewlib.PointerSpeedometer;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.fossasia.pslab.R;
import org.fossasia.pslab.activity.SensorActivity;
import org.fossasia.pslab.communication.ScienceLab;
import org.fossasia.pslab.communication.peripherals.I2C;
import org.fossasia.pslab.communication.sensors.BH1750;
import org.fossasia.pslab.others.ScienceLabCommon;

import java.io.IOException;
import java.util.ArrayList;

import static org.fossasia.pslab.activity.SensorActivity.counter;


public class SensorFragmentBH1750Data extends Fragment {

    private ScienceLab scienceLab;
    private SensorDataFetch sensorDataFetch;
    private EditText statMax;
    private EditText statMin;
    private EditText statMean;
    private static BH1750 sensorBH1750;
    private static SensorManager sensorManager;
    private static Sensor sensorBuiltIn;
    private boolean builtIn = true;
    private LineChart mChart;
    private long startTime;
    private int flag;
    private ArrayList<Entry> entries;
    private static Runnable runnable;
    private static Thread dataThread;
    private final Object lock = new Object();
    private PointerSpeedometer lightMeter;
    private float currentMin;
    private float currentMax;


    public static SensorFragmentBH1750Data newInstance() {
        return new SensorFragmentBH1750Data();
    }

    public SensorFragmentBH1750Data() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        entries = new ArrayList<>();
        ((SensorActivity) getActivity()).sensorDock.setVisibility(View.VISIBLE);

        runnable = new Runnable() {
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
        dataThread = new Thread(runnable);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sensor_bh1750_data, container, false);
        statMax = (EditText) view.findViewById(R.id.stat_max);
        statMin = (EditText) view.findViewById(R.id.stat_min);
        statMean = (EditText) view.findViewById(R.id.stat_mean);
        lightMeter = (PointerSpeedometer) view.findViewById(R.id.light_meter);

        statMin.setFocusable(false);
        statMax.setFocusable(false);
        statMean.setFocusable(false);

        mChart = view.findViewById(R.id.chart_bh1750);
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

    public static void sensorChanged(int sensor) {
        switch (sensor) {
            case 0:
                if (dataThread != null) {
                    try {
                        dataThread.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                sensorBuiltIn = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
                sensorBH1750 = null;
                break;
            case 1:
                sensorBH1750 = SensorFragmentBH1750Config.getBh1750();
                sensorBuiltIn = null;
                dataThread.run();
        }
    }


    private class SensorDataFetch extends AsyncTask<Void, Void, Void> implements SensorEventListener{

        private float dataBH1750;
        private long timeElapsed;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (sensorBH1750 != null) {
                    dataBH1750 = sensorBH1750.getRaw().floatValue();
                    sensorManager.unregisterListener(this);
                }
                else if (sensorBuiltIn != null) {
                    sensorManager.registerListener(this, sensorBuiltIn, SensorManager.SENSOR_DELAY_NORMAL);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
//            tvSensorBH1750Luminosity.setText(String.valueOf(dataBH1750));
            if (currentMax < dataBH1750){
                currentMax = dataBH1750;
                statMax.setText(String.valueOf(dataBH1750));
            }
            else if (currentMin > dataBH1750) {
                currentMin = dataBH1750;
                statMin.setText(String.valueOf(dataBH1750));
            }

            lightMeter.speedTo(dataBH1750);

            timeElapsed = (System.currentTimeMillis() - startTime) / 1000;
            entries.add(new Entry((float) timeElapsed, dataBH1750));
            LineDataSet dataSet = new LineDataSet(entries, getString(R.string.bx));
            dataSet.setDrawCircles(true);

            LineData data = new LineData(dataSet);
            mChart.setData(data);
            mChart.notifyDataSetChanged();
            mChart.setVisibleXRangeMaximum(10);
            mChart.moveViewToX(data.getEntryCount());
            mChart.invalidate();
            ((SensorActivity) getActivity()).samplesEditBox.setText(String.valueOf(counter));
            if (counter == 0 && !((SensorActivity) getActivity()).runIndefinitely) {
                ((SensorActivity) getActivity()).play = false;
                ((SensorActivity) getActivity()).playPauseButton.setImageResource(R.drawable.play);
            }
            synchronized (lock) {
                lock.notify();
            }
        }
        @Override
        public void onSensorChanged(SensorEvent event) {
            dataBH1750 = event.values[0];
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

    }


}
