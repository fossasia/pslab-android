package io.pslab.activity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.anastr.speedviewlib.TubeSpeedometer;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import io.pslab.R;
import io.pslab.communication.ScienceLab;
import io.pslab.communication.sensors.BMP180;
import io.pslab.others.MathUtils;
import io.pslab.others.ScienceLabCommon;
import io.pslab.others.SwipeGestureDetector;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class BarometerActivity extends AppCompatActivity {
    BottomSheetBehavior bottomSheetBehavior;
    GestureDetector gestureDetector;
    private static final String PREF_NAME = "customDialogPreference";

    private static int sensorType = 0;
    private static int highLimit = 1000;
    private static int updatePeriod = 100;
    private BarometerActivity.SensorDataFetch sensorDataFetch;

    @BindView(R.id.barometer_max)
    TextView statMax;
    @BindView(R.id.barometer_min)
    TextView statMin;
    @BindView(R.id.barometer_mean)
    TextView statMean;
    @BindView(R.id.chart_barometer)
    LineChart mChart;
    @BindView(R.id.barometer)
    TubeSpeedometer barometer;

    private BMP180 sensorBMP180 = null;
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

    private final Object lock = new Object();

    //bottomSheet
    @BindView(R.id.bottom_sheet)
    LinearLayout bottomSheet;
    @BindView(R.id.shadow)
    View tvShadow;
    @BindView(R.id.img_arrow)
    ImageView arrowUpDown;
    @BindView(R.id.sheet_slide_text)
    TextView bottomSheetSlideText;
    @BindView(R.id.guide_title)
    TextView bottomSheetGuideTitle;
    @BindView(R.id.custom_dialog_text)
    TextView bottomSheetText;
    @BindView(R.id.custom_dialog_schematic)
    ImageView bottomSheetSchematic;
    @BindView(R.id.custom_dialog_desc)
    TextView bottomSheetDesc;

    public static BarometerActivity newInstance() {
        return new BarometerActivity();
    }

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barometer_main);
        ButterKnife.bind(this);
        setUpBottomSheet();
        BarometerActivity.newInstance();

        currentMin = 10000;
        entries = new ArrayList<>();
        switch (sensorType) {
            case 0:
                sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
                sensor = sensorManager != null ? sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE) : null;
                if (sensor == null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!isFinishing()) {
                                new AlertDialog.Builder(BarometerActivity.this)
                                        .setTitle(R.string.barometer_alert_title)
                                        .setMessage(R.string.barometer_alert_description)
                                        .setCancelable(false)
                                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        }).show();
                            }
                        }
                    });
                }
                break;
            case 1:
                scienceLab = ScienceLabCommon.scienceLab;
                break;
            default:
                break;
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_barometer, container, false);
        unbinder = ButterKnife.bind(this, view);
        Runnable runnable = new Runnable() {
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
        Thread dataThread = new Thread(runnable);
        dataThread.start();

        barometer.setMaxSpeed(10000);

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

    public static void setParameters(int sensorType, int highLimit, int updatePeriod) {
        BarometerActivity.sensorType = sensorType;
        BarometerActivity.highLimit = highLimit;
        BarometerActivity.updatePeriod = updatePeriod;
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
                if (sensorBMP180 != null) {
                    data = Float.valueOf(Arrays.toString(sensorBMP180.getRaw()));
                    sensorManager.unregisterListener(this);
                } else if (sensor != null) {
                    sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
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
            barometer.setSpeedAt(data);

            if (data > highLimit) barometer.setIndicatorColor(Color.RED);
            else barometer.setIndicatorColor(Color.WHITE);
            timeElapsed = (System.currentTimeMillis() - startTime) / 1000;
            entries.add(new Entry((float) timeElapsed, data));

            for (Entry item : entries) {
                count++;
                sum += item.getY();
            }
            statMean.setText(Float.toString(Float.valueOf(df.format(sum / count))));

            LineDataSet dataSet = new LineDataSet(entries, getString(R.string.barometer));
            LineData data = new LineData(dataSet);
            dataSet.setDrawCircles(false);

            mChart.setData(data);
            mChart.notifyDataSetChanged();
            mChart.setVisibleXRangeMaximum(10);
            mChart.moveViewToX(data.getEntryCount());
            mChart.invalidate();
        }


        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            //do nothing
        }

        private void unRegisterListener() {
            sensorManager.unregisterListener(this);
        }
    }

    public void onDestroyView() {
        super.onDestroy();
        monitor = false;
        if (sensor != null && sensorDataFetch != null) {
            sensorManager.unregisterListener(sensorDataFetch);
            sensorDataFetch.cancel(true);
        }
        unbinder.unbind();
    }

    private void setUpBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        final SharedPreferences settings = this.getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        Boolean isFirstTime = settings.getBoolean("BarometerFirstTime", true);

        bottomSheetGuideTitle.setText(R.string.barometer);
        bottomSheetDesc.setText(R.string.barometer_intro);
        bottomSheetSchematic.setImageResource(R.drawable.bmp180_schematic);
        bottomSheetDesc.setText(R.string.barometer_desc);

        if (isFirstTime) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            tvShadow.setAlpha(0.8f);
            arrowUpDown.setRotation(180);
            bottomSheetSlideText.setText(R.string.hide_guide_text);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("BarometerFirstTime", false);
            editor.apply();
        } else {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            private Handler handler = new Handler();
            private Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }
            };

            @Override
            public void onStateChanged(@NonNull final View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_EXPANDED:
                        handler.removeCallbacks(runnable);
                        bottomSheetSlideText.setText(R.string.hide_guide_text);
                        break;

                    case BottomSheetBehavior.STATE_COLLAPSED:
                        handler.postDelayed(runnable, 2000);
                        break;

                    default:
                        handler.removeCallbacks(runnable);
                        bottomSheetSlideText.setText(R.string.show_guide_text);
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                Float value = (float) MathUtils.map((double) slideOffset, 0.0, 1.0, 0.0, 0.8);
                tvShadow.setAlpha(value);
                arrowUpDown.setRotation(slideOffset * 180);
            }
        });
        gestureDetector = new GestureDetector(this, new SwipeGestureDetector(bottomSheetBehavior));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);                 //Gesture detector need this to transfer touch event to the gesture detector.
        return super.onTouchEvent(event);
    }
}
