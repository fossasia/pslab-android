package io.pslab.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.anastr.speedviewlib.PointerSpeedometer;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import io.pslab.R;
import io.pslab.activity.LuxMeterActivity;
import io.pslab.communication.ScienceLab;
import io.pslab.communication.sensors.BH1750;
import io.pslab.communication.sensors.TSL2561;
import io.pslab.others.CSVLogger;
import io.pslab.others.GPSLogger;
import io.pslab.others.ScienceLabCommon;
import io.pslab.communication.peripherals.I2C;
import io.pslab.others.CustomSnackBar;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static android.content.Context.SENSOR_SERVICE;

public class LuxMeterFragmentData extends Fragment {

    private static int sensorType = 0;
    private static int highLimit = 2000;
    private static int updatePeriod = 100;
    private final Object lock = new Object();

    @BindView(R.id.lux_stat_max)
    TextView statMax;
    @BindView(R.id.lux_stat_min)
    TextView statMin;
    @BindView(R.id.lux_stat_mean)
    TextView statMean;
    @BindView(R.id.chart_lux_meter)
    LineChart mChart;
    @BindView(R.id.light_meter)

    PointerSpeedometer lightMeter;
    private SensorDataFetch sensorDataFetch;
    private BH1750 sensorBH1750 = null;
    private TSL2561 sensorTSL2561 = null;
    private SensorManager sensorManager;
    private Sensor sensor;
    private ScienceLab scienceLab;
    private long startTime;
    private int flag;
    private ArrayList<Entry> entries;
    private float currentMin;
    private float currentMax;
    private YAxis y;
    private volatile boolean monitor = true;
    private Unbinder unbinder;
    private long previousTimeElapsed = (System.currentTimeMillis() - startTime) / 1000;
    private GPSLogger gpsLogger;
    private Runnable runnable;

    public static LuxMeterFragmentData newInstance() {
        return new LuxMeterFragmentData();
    }

    public static void setParameters(int sensorType, int highLimit, int updatePeriod) {
        LuxMeterFragmentData.sensorType = sensorType;
        LuxMeterFragmentData.highLimit = highLimit;
        LuxMeterFragmentData.updatePeriod = updatePeriod;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        currentMin = 10000;
        currentMax = 30;
        entries = new ArrayList<>();
        switch (sensorType) {
            case 0:
                sensorManager = (SensorManager) getContext().getSystemService(SENSOR_SERVICE);
                sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
                break;
            case 1:
                scienceLab = ScienceLabCommon.scienceLab;
                if (scienceLab.isConnected()) {
                    try {
                        I2C i2c = scienceLab.i2c;
                        sensorBH1750 = new BH1750(i2c);
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case 2:
                scienceLab = ScienceLabCommon.scienceLab;
                if (scienceLab.isConnected()) {
                    try {
                        I2C i2c = scienceLab.i2c;
                        sensorTSL2561 = new TSL2561(i2c, scienceLab);
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            default:
                break;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lux_meter_data, container, false);
        unbinder = ButterKnife.bind(this, view);
        runnable = new Runnable() {
            @Override
            public void run() {
                if (flag == 0) {
                    startTime = System.currentTimeMillis();
                    flag = 1;
                    switch (sensorType) {
                        case 0:
                            while (monitor) {
                                if (sensor != null) {
                                    sensorDataFetch = new SensorDataFetch();
                                    sensorDataFetch.execute();
                                    synchronized (lock) {
                                        try {
                                            lock.wait();
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    try {
                                        Thread.sleep(updatePeriod);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            break;
                        case 1:
                        case 2:
                            while (monitor) {
                                if (scienceLab.isConnected()) {
                                    sensorDataFetch = new SensorDataFetch();
                                    sensorDataFetch.execute();
                                    synchronized (lock) {
                                        try {
                                            lock.wait();
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    try {
                                        Thread.sleep(updatePeriod);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        };

        lightMeter.setMaxSpeed(10000);

        XAxis x = mChart.getXAxis();
        this.y = mChart.getAxisLeft();
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
        mChart.setData(data);

        Legend l = mChart.getLegend();
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.WHITE);

        x.setTextColor(Color.WHITE);
        x.setDrawGridLines(true);
        x.setAvoidFirstLastClipping(true);

        y.setTextColor(Color.WHITE);
        y.setAxisMaximum(currentMax);
        y.setAxisMinimum(currentMin);
        y.setDrawGridLines(true);
        y.setLabelCount(10);

        y2.setDrawGridLines(false);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        monitor = false;
        if (sensor != null && sensorDataFetch != null) {
            sensorManager.unregisterListener(sensorDataFetch);
            sensorDataFetch.cancel(true);
        }
        unbinder.unbind();
    }

    private class SensorDataFetch extends AsyncTask<Void, Void, Void> implements SensorEventListener {

        private float data;
        private long timeElapsed;
        private int count = 0;
        private float sum = 0;
        private DecimalFormat df = new DecimalFormat("#.##");

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (sensorBH1750 != null && scienceLab.isConnected()) {
                    data = sensorBH1750.getRaw().floatValue();
                    sensorManager.unregisterListener(this);
                } else if (sensorTSL2561 != null && scienceLab.isConnected()) {
                    int[] dataSet = sensorTSL2561.getRaw();
                    data = (float) dataSet[2];
                    sensorManager.unregisterListener(this);
                } else if (sensor != null) {
                    sensorManager.registerListener(this, sensor, updatePeriod);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            visualizeData();
            synchronized (lock) {
                lock.notify();
            }
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            data = Float.valueOf(df.format(event.values[0]));
            visualizeData();
            unRegisterListener();
        }

        @SuppressLint("SetTextI18n")
        private void visualizeData() {
            if (currentMax < data) {
                currentMax = data;
                statMax.setText(String.valueOf(data));
            } else if (currentMin > data) {
                currentMin = data;
                statMin.setText(String.valueOf(data));
            }

            y.setAxisMaximum(currentMax);
            y.setAxisMinimum(currentMin);
            y.setLabelCount(10);
            if (data != 0) {
                lightMeter.setSpeedAt(data);

                if (data > highLimit)
                    lightMeter.setPointerColor(Color.RED);
                else
                    lightMeter.setPointerColor(Color.WHITE);

                timeElapsed = ((System.currentTimeMillis() - startTime) / 1000);
                if (timeElapsed != previousTimeElapsed) {
                    previousTimeElapsed = timeElapsed;
                    entries.add(new Entry((float) timeElapsed, data));
                    final LuxMeterActivity parent = (LuxMeterActivity) getActivity();
                    for (Entry item : entries) {
                        assert parent != null;

                        if (parent.recordData) {
                            gpsLogger = parent.gpsLogger;
                            String data = String.valueOf(System.currentTimeMillis()) + "," +
                                    item.getX() + "," + item.getY() + "\n";
                            parent.luxLogger.writeCSVFile(data);
                        }

                        if (parent.exportData) {
                            parent.exportData = false;
                            if (parent.locationPref && gpsLogger != null) {
                                String data;
                                Location location = gpsLogger.getBestLocation();
                                if (location != null) {
                                    data = "\nLocation" + "," + String.valueOf(location.getLatitude()) + "," + String.valueOf(location.getLongitude() + "\n");
                                } else {
                                    data = "\nLocation" + "," + "null" + "," + "null";
                                }
                                parent.luxLogger.writeCSVFile(data);
                                gpsLogger.removeUpdate();
                            }
                            CustomSnackBar.showSnackBar((CoordinatorLayout) parent.findViewById(R.id.cl),
                                    getString(R.string.csv_store_text) + " " + parent.luxLogger.getCurrentFilePath()
                                    , getString(R.string.delete_capital), new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            new AlertDialog.Builder(parent, R.style.AlertDialogStyle)
                                                    .setTitle(R.string.delete_file)
                                                    .setMessage(R.string.delete_warning)
                                                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            parent.luxLogger.deleteFile();
                                                        }
                                                    })
                                                    .setNegativeButton(R.string.cancel, null)
                                                    .create()
                                                    .show();
                                        }
                                    });
                        }

                        count++;
                        sum += item.getY();
                    }
                    try {
                        statMean.setText(Float.toString(Float.valueOf(df.format(sum / count))));
                    } catch (NumberFormatException e) {
                        statMean.setText(getString(R.string.lux_meter_none));
                    }

                    LineDataSet dataSet = new LineDataSet(entries, getString(R.string.lux));
                    LineData data = new LineData(dataSet);
                    dataSet.setDrawCircles(false);
                    dataSet.setDrawValues(false);
                    dataSet.setLineWidth(2);

                    mChart.setData(data);
                    mChart.notifyDataSetChanged();
                    mChart.setVisibleXRangeMaximum(80);
                    mChart.moveViewToX(data.getEntryCount());
                    mChart.invalidate();
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            //do nothing
        }

        private void unRegisterListener() {
            sensorManager.unregisterListener(this);
        }
    }

    public void startSensorFetching() {
        entries.clear();
        mChart.invalidate();
        mChart.clear();
        monitor = true;
        flag = 0;
        lightMeter.setWithTremble(true);
        Thread dataThread = new Thread(runnable);
        dataThread.start();
    }

    public void stopSensorFetching() {
        monitor = false;
        if (sensor != null && sensorDataFetch != null) {
            sensorManager.unregisterListener(sensorDataFetch);
            sensorDataFetch.cancel(true);
            lightMeter.setWithTremble(false);
            lightMeter.speedTo(0f, 500);
            lightMeter.setPointerColor(ContextCompat.getColor(getActivity(), R.color.white));
        }
    }

}
