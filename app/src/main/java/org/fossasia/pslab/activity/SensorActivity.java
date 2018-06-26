package org.fossasia.pslab.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.fossasia.pslab.R;
import org.fossasia.pslab.communication.ScienceLab;
import org.fossasia.pslab.communication.peripherals.I2C;
import org.fossasia.pslab.others.ScienceLabCommon;
import org.fossasia.pslab.sensors.SensorADS1115;
import org.fossasia.pslab.sensors.SensorBMP180;
import org.fossasia.pslab.sensors.SensorHMC5883L;
import org.fossasia.pslab.sensors.SensorMLX90614;
import org.fossasia.pslab.sensors.SensorMPU6050;
import org.fossasia.pslab.sensors.SensorMPU925X;
import org.fossasia.pslab.sensors.SensorSHT21;
import org.fossasia.pslab.sensors.SensorTSL2561;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Created by asitava on 18/6/17.
 */

public class SensorActivity extends AppCompatActivity {

    private I2C i2c;
    private ScienceLab scienceLab;
    private LinkedHashMap<Integer, String> sensorAddr = new LinkedHashMap<>();
    private ArrayList<String> dataAddress = new ArrayList<>();
    private ArrayList<String> dataName = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private String tvData = "";
    private ListView lvSensor;
    private TextView tvSensorScan;

    Runnable scanRunnable = new Runnable() {
        @Override
        public void run() {
            if (scienceLab.isConnected()) {
                populateSensors();
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensor_main);
        scienceLab = ScienceLabCommon.scienceLab;

        i2c = scienceLab.i2c;
        sensorAddr.put(0x60, "MCP4728");
        sensorAddr.put(0x48, "ADS1115");
        sensorAddr.put(0x23, "BH1750");
        sensorAddr.put(0x77, "BMP180");
        sensorAddr.put(0x5A, "MLX90614");
        sensorAddr.put(0x1E, "HMC5883L");
        sensorAddr.put(0x68, "MPU6050");
        sensorAddr.put(0x40, "SHT21");
        sensorAddr.put(0x39, "TSL2561");

        adapter = new ArrayAdapter<>(getApplication(), R.layout.sensor_list_item, R.id.tv_sensor_list_item, dataName);

        final CoordinatorLayout coordinatorLayout = findViewById(R.id.layout_container);
        Button buttonSensorAutoScan = findViewById(R.id.button_sensor_autoscan);
        tvSensorScan = findViewById(R.id.tv_sensor_scan);
        tvSensorScan.setText(getResources().getString(R.string.use_autoscan));
        lvSensor = findViewById(R.id.lv_sensor);
        lvSensor.setAdapter(adapter);

        buttonSensorAutoScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (scienceLab.isConnected()) {
                    new Thread(scanRunnable).start();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.device_not_connected), Toast.LENGTH_SHORT).show();
                }
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
        new Thread(scanRunnable).start();
    }

    private void populateSensors() {
        ArrayList<Integer> data = new ArrayList<>();
        dataName.clear();
        dataAddress.clear();
        try {
            tvSensorScan.setText(getResources().getString(R.string.scanning));
            data = i2c.scan(null);
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
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

        }
        else {
            tvData = getResources().getString(R.string.sensor_not_connected);
        }

        for (int key : sensorAddr.keySet()) {
            dataName.add(sensorAddr.get(key));
        }

        for (int key : sensorAddr.keySet()) {
            dataName.add(sensorAddr.get(key));
        }

        for (int key : sensorAddr.keySet()) {
            dataName.add(sensorAddr.get(key));
        }
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                tvSensorScan.setText(tvData);
                adapter.notifyDataSetChanged();
            }
        });
    }
}
