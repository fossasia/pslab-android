package io.pslab.fragment;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
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

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.pslab.R;
import io.pslab.communication.ScienceLab;
import io.pslab.communication.peripherals.I2C;
import io.pslab.communication.sensors.BH1750;
import io.pslab.communication.sensors.TSL2561;
import io.pslab.models.LuxData;
import io.pslab.models.SensorLogged;
import io.pslab.others.GPSLogger;
import io.pslab.others.ScienceLabCommon;
import io.realm.Realm;

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
    @BindView(R.id.spinner_lux_sensor_gain)
    Spinner gainValue;
    @BindView(R.id.light_meter)
    PointerSpeedometer lightMeter;
    @BindView(R.id.cardview_gain_range)
    CardView gainRangeCardView;

    private SensorDataFetch sensorDataFetch;
    private BH1750 sensorBH1750 = null;
    private TSL2561 sensorTSL2561 = null;
    private SensorManager sensorManager;
    private Sensor sensor;
    private ScienceLab scienceLab;
    private long startTime;
    private long endTime;
    private int flag;
    private ArrayList<Entry> entries;
    private ArrayList<LuxData> luxRealmData;
    private float currentMin;
    private float currentMax;
    private YAxis y;
    private volatile boolean monitor = true;
    private Unbinder unbinder;
    private long previousTimeElapsed = (System.currentTimeMillis() - startTime) / 1000;
    private GPSLogger gpsLogger;
    private Runnable runnable;
    private Realm realm;
    private Spinner selectSensor;

    public static LuxMeterFragmentData newInstance() {
        return new LuxMeterFragmentData();
    }

    public static void setParameters(int highLimit, int updatePeriod) {
        LuxMeterFragmentData.highLimit = highLimit;
        LuxMeterFragmentData.updatePeriod = updatePeriod;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        currentMin = 10000;
        currentMax = 0;
        entries = new ArrayList<>();
        luxRealmData = new ArrayList<>();
        realm = Realm.getDefaultInstance();

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lux_meter_data, container, false);
        unbinder = ButterKnife.bind(this, view);
        selectSensor = (Spinner) view.findViewById(R.id.spinner_select_light);
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

        changeSensor(sensorType);
        gainRangeCardView.setVisibility(View.GONE);

        selectSensor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                changeSensor(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //do nothing
            }
        });

        gainValue.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    switch (position) {
                        case 0:
                        case 1:
                        case 2:
                            if (sensorBH1750 != null) {
                                sensorBH1750.setRange(gainValue.getSelectedItem().toString());
                            }
                            break;
                        case 3:
                        case 4:
                        case 5:
                            if (sensorTSL2561 != null) {
                                sensorTSL2561.setGain(gainValue.getSelectedItem().toString());
                            }
                            break;
                        default:
                            break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //do nothing
            }
        });

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
                    Entry entry = new Entry((float) timeElapsed, data);
                    entries.add(entry);
                    Long currentTime = System.currentTimeMillis();
                    LuxData tempObject = new LuxData(data, currentTime, timeElapsed);
                    luxRealmData.add(tempObject);

                    count++;
                    sum += entry.getY();
                    statMean.setText(Float.toString(Float.valueOf(df.format(sum / count))));

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
        endTime = System.currentTimeMillis();
        if (sensor != null && sensorDataFetch != null) {
            sensorManager.unregisterListener(sensorDataFetch);
            sensorDataFetch.cancel(true);
            lightMeter.setWithTremble(false);
            lightMeter.speedTo(0f, 500);
            lightMeter.setPointerColor(ContextCompat.getColor(getActivity(), R.color.white));
        }
    }

    public boolean saveDataInRealm(Long uniqueRef, boolean includeLocation, GPSLogger gpsLogger) {
        boolean flag = luxRealmData.isEmpty();
        if (!flag) {
            realm.beginTransaction();

            SensorLogged sensorLogged = realm.createObject(SensorLogged.class, uniqueRef);
            sensorLogged.setSensor(getResources().getString(R.string.lux_meter));
            sensorLogged.setDateTimeStart(startTime);
            sensorLogged.setDateTimeEnd(endTime);
            sensorLogged.setTimeZone(TimeZone.getDefault().getDisplayName());

            if (includeLocation && gpsLogger != null) {
                Location location = gpsLogger.getBestLocation();
                if (location != null) {
                    sensorLogged.setLatitude(location.getLatitude());
                    sensorLogged.setLongitude(location.getLongitude());
                } else {
                    sensorLogged.setLatitude(0.0);
                    sensorLogged.setLongitude(0.0);
                }
                gpsLogger.removeUpdate();
            } else {
                sensorLogged.setLatitude(0.0);
                sensorLogged.setLongitude(0.0);
            }

            for (int i = 0; i < luxRealmData.size(); i++) {
                LuxData tempObject = luxRealmData.get(i);
                tempObject.setId(i);
                tempObject.setForeignKey(uniqueRef);
                realm.copyToRealm(tempObject);
                Log.i("dataResult", String.valueOf(tempObject.getLux()));
            }
            realm.copyToRealm(sensorLogged);
            realm.commitTransaction();
            luxRealmData.clear();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mChart.clear();
        mChart.invalidate();
    }

    private void changeSensor(int sensorTypeSelected) {
        switch (sensorTypeSelected) {
            case 0:
                sensorManager = (SensorManager) getContext().getSystemService(SENSOR_SERVICE);
                sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
                break;
            case 1:
                scienceLab = ScienceLabCommon.scienceLab;
                if (scienceLab.isConnected()) {
                    ArrayList<Integer> data = new ArrayList<>();
                    try {
                        I2C i2c = scienceLab.i2c;
                        data = i2c.scan(null);
                        if (data.contains(0x23)) {
                            sensorBH1750 = new BH1750(i2c);
                            gainRangeCardView.setVisibility(View.VISIBLE);
                            sensorType = 0;
                        } else {
                            Toast.makeText(getContext(), getResources().getText(R.string.sensor_not_connected_tls), Toast.LENGTH_SHORT).show();
                            sensorType = 0;
                            selectSensor.setSelection(0);
                        }
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getContext(), getResources().getText(R.string.device_not_found), Toast.LENGTH_SHORT).show();
                    sensorType = 0;
                    selectSensor.setSelection(0);
                }

                break;
            case 2:
                scienceLab = ScienceLabCommon.scienceLab;
                if (scienceLab.isConnected()) {
                    try {
                        I2C i2c = scienceLab.i2c;
                        ArrayList<Integer> data = new ArrayList<>();
                        data = i2c.scan(null);
                        if (data.contains(0x39)) {
                            sensorTSL2561 = new TSL2561(i2c, scienceLab);
                            gainRangeCardView.setVisibility(View.VISIBLE);
                            sensorType = 2;
                        } else {
                            Toast.makeText(getContext(), getResources().getText(R.string.sensor_not_connected_tls), Toast.LENGTH_SHORT).show();
                            sensorType = 0;
                            selectSensor.setSelection(0);
                        }
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getContext(), getResources().getText(R.string.device_not_found), Toast.LENGTH_SHORT).show();
                    sensorType = 0;
                    selectSensor.setSelection(0);
                }
                break;
            default:
                break;
        }
    }
}
