package io.pslab.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import io.pslab.R;
import io.pslab.communication.ScienceLab;
import io.pslab.communication.peripherals.I2C;
import io.pslab.others.MathUtils;
import io.pslab.others.ScienceLabCommon;
import io.pslab.others.SwipeGestureDetector;
import io.pslab.sensors.SensorADS1115;
import io.pslab.sensors.SensorBMP180;
import io.pslab.sensors.SensorHMC5883L;
import io.pslab.sensors.SensorMLX90614;
import io.pslab.sensors.SensorMPU6050;
import io.pslab.sensors.SensorMPU925X;
import io.pslab.sensors.SensorSHT21;
import io.pslab.sensors.SensorTSL2561;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Created by asitava on 18/6/17.
 */

public class SensorActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "customDialogPreference";
    private I2C i2c;
    private ScienceLab scienceLab;
    private LinkedHashMap<Integer, String> sensorAddr = new LinkedHashMap<>();
    private ArrayList<String> dataAddress = new ArrayList<>();
    private ArrayList<String> dataName = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private String tvData = "";
    private ListView lvSensor;
    private TextView tvSensorScan;
    private Button buttonSensorAutoScan;

    //Bottom Sheet
    private LinearLayout bottomSheet;
    private View tvShadow;
    private ImageView arrowUpDown;
    private TextView bottomSheetSlideText;
    private TextView bottomSheetGuideTitle;
    private TextView bottomSheetText;
    private ImageView bottomSheetSchematic;
    private TextView bottomSheetDesc;
    private BottomSheetBehavior bottomSheetBehavior;
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensor_main);
        scienceLab = ScienceLabCommon.scienceLab;

        // Bottom Sheet guide
        bottomSheet = findViewById(R.id.bottom_sheet);
        tvShadow = findViewById(R.id.shadow);
        arrowUpDown = findViewById(R.id.img_arrow);
        bottomSheetSlideText = findViewById(R.id.sheet_slide_text);
        bottomSheetGuideTitle = findViewById(R.id.guide_title);
        bottomSheetText = findViewById(R.id.custom_dialog_text);
        bottomSheetSchematic = findViewById(R.id.custom_dialog_schematic);
        bottomSheetDesc = findViewById(R.id.custom_dialog_desc);

        setUpBottomSheet();
        tvShadow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                tvShadow.setVisibility(View.GONE);
            }
        });

        i2c = scienceLab.i2c;
        sensorAddr.put(0x48, "ADS1115");
        sensorAddr.put(0x77, "BMP180");
        sensorAddr.put(0x5A, "MLX90614");
        sensorAddr.put(0x1E, "HMC5883L");
        sensorAddr.put(0x68, "MPU6050");
        sensorAddr.put(0x40, "SHT21");
        sensorAddr.put(0x39, "TSL2561");

        adapter = new ArrayAdapter<>(getApplication(), R.layout.sensor_list_item, R.id.tv_sensor_list_item, dataName);

        final CoordinatorLayout coordinatorLayout = findViewById(R.id.layout_container);
        buttonSensorAutoScan = findViewById(R.id.button_sensor_autoscan);
        tvSensorScan = findViewById(R.id.tv_sensor_scan);
        tvSensorScan.setText(getResources().getString(R.string.use_autoscan));
        lvSensor = findViewById(R.id.lv_sensor);
        lvSensor.setAdapter(adapter);

        buttonSensorAutoScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonSensorAutoScan.setClickable(false);
                tvSensorScan.setText(getResources().getString(R.string.scanning));
                new PopulateSensors().execute();
            }
        });
        lvSensor.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String itemValue = (String) lvSensor.getItemAtPosition(position);
                Intent intent;
                switch (itemValue) {
                    case "ADS1115":
                        intent = new Intent(getApplication(), SensorADS1115.class);
                        startActivity(intent);
                        break;
                    case "BMP180":
                        intent = new Intent(getApplication(), SensorBMP180.class);
                        startActivity(intent);
                        break;
                    case "MLX90614":
                        intent = new Intent(getApplication(), SensorMLX90614.class);
                        startActivity(intent);
                        break;
                    case "HMC5883L":
                        intent = new Intent(getApplication(), SensorHMC5883L.class);
                        startActivity(intent);
                        break;
                    case "MPU6050":
                        intent = new Intent(getApplication(), SensorMPU6050.class);
                        startActivity(intent);
                        break;
                    case "SHT21":
                        intent = new Intent(getApplication(), SensorSHT21.class);
                        startActivity(intent);
                        break;
                    case "TSL2561":
                        intent = new Intent(getApplication(), SensorTSL2561.class);
                        startActivity(intent);
                        break;
                    case "MPU925x":
                        intent = new Intent(getApplication(), SensorMPU925X.class);
                        startActivity(intent);
                        break;
                    default:
                        Toast.makeText(getApplication(), "Sensor Not Supported", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setUpBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        final SharedPreferences settings = this.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Boolean isFirstTime = settings.getBoolean("SensorsFirstTime", true);

        bottomSheetGuideTitle.setText(R.string.sensors);
        bottomSheetDesc.setText(R.string.sensors_description);

        if (isFirstTime) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            tvShadow.setVisibility(View.VISIBLE);
            tvShadow.setAlpha(0.8f);
            arrowUpDown.setRotation(180);
            bottomSheetSlideText.setText(R.string.hide_guide_text);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("SensorsFirstTime", false);
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
                tvShadow.setVisibility(View.VISIBLE);
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


    private class PopulateSensors extends AsyncTask<Void, Void, Void> {
        private ArrayList<Integer> data;

        @Override
        protected Void doInBackground(Void... voids) {
            data = new ArrayList<>();
            dataName.clear();
            dataAddress.clear();
            if (scienceLab.isConnected()) {
                try {
                    data = i2c.scan(null);
                } catch (IOException | NullPointerException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (data != null) {
                for (Integer myInt : data) {
                    if (myInt != null && sensorAddr.get(myInt) != null) {
                        dataAddress.add(String.valueOf(myInt));
                    }
                }
                tvData = "";

                for (String s : dataAddress) {
                    tvData += s + ":" + sensorAddr.get(Integer.parseInt(s)) + "\n";
                }

            } else {
                tvData = getResources().getString(R.string.sensor_not_connected);
            }

            for (int key : sensorAddr.keySet()) {
                dataName.add(sensorAddr.get(key));
            }

            if (scienceLab.isConnected()) {
                tvSensorScan.setText(tvData);
            } else {
                tvSensorScan.setText(getString(R.string.not_connected));
            }
            adapter.notifyDataSetChanged();
            buttonSensorAutoScan.setClickable(true);
        }
    }
}
