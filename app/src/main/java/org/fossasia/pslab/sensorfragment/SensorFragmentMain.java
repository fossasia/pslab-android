package org.fossasia.pslab.sensorfragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.fossasia.pslab.R;
import org.fossasia.pslab.communication.ScienceLab;
import org.fossasia.pslab.communication.peripherals.I2C;
import org.fossasia.pslab.others.ScienceLabCommon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by asitava on 13/7/17.
 */

public class SensorFragmentMain extends Fragment {

    private I2C i2c;
    private ScienceLab scienceLab;
    private HashMap<Integer, String> sensorAddr = new HashMap<>();
    private ArrayList<Integer> data = new ArrayList<>();
    private ArrayList<String> dataAddress = new ArrayList<>();
    private ArrayList<String> dataName = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private String tvData = "";
    private ListView lvSensor;
    private TextView tvSensorScan;
    private Fragment selectedFragment = null;

    public static SensorFragmentMain newInstance() {
        SensorFragmentMain sensorFragmentMain = new SensorFragmentMain();
        return sensorFragmentMain;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        try {
            data = i2c.scan(null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (data != null) {
            for (Integer myInt : data) {
                if (sensorAddr.get(myInt) != null) {
                    dataAddress.add(String.valueOf(myInt));
                    dataName.add(sensorAddr.get(myInt));
                }
            }
            for (String s : dataAddress) {
                tvData += s + ":" + sensorAddr.get(Integer.parseInt(s)) + "\n";
            }
        }
        String[] dataDisp = dataName.toArray(new String[dataName.size()]);
        adapter = new ArrayAdapter<>(getContext(), R.layout.sensor_list_item, R.id.tv_sensor_list_item, dataDisp);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sensor_main, container, false);

        Button buttonSensorAutoscan = (Button) view.findViewById(R.id.button_sensor_autoscan);
        tvSensorScan = (TextView) view.findViewById(R.id.tv_sensor_scan);
        tvSensorScan.setText(tvData);
        lvSensor = (ListView) view.findViewById(R.id.lv_sensor);
        lvSensor.setAdapter(adapter);

        buttonSensorAutoscan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (scienceLab.isConnected()) {
                    try {
                        data = i2c.scan(null);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (data != null) {
                        for (Integer myInt : data) {
                            if (myInt != null && sensorAddr.get(myInt) != null ) {
                                dataAddress.add(String.valueOf(myInt));
                                dataName.add(sensorAddr.get(myInt));
                            }
                        }
                        tvData = "";
                        for (String s : dataAddress) {
                            tvData += s + ":" + sensorAddr.get(Integer.parseInt(s)) + "\n";
                        }

                    }
                    tvSensorScan.setText(tvData);
                    String[] dataDisp = dataName.toArray(new String[dataName.size()]);
                    adapter = new ArrayAdapter<>(getContext(), R.layout.sensor_list_item, R.id.tv_sensor_list_item, dataDisp);
                    lvSensor.setAdapter(adapter);
                }
            }
        });

        lvSensor.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String itemValue = (String) lvSensor.getItemAtPosition(position);
                switch (itemValue) {
                    case "ADS1115":
                        selectedFragment = SensorFragmentADS1115.newInstance();
                        break;
                    case "BH1750":
                        selectedFragment = SensorFragmentBH1750.newInstance();
                        break;
                    case "BMP180":
                        selectedFragment = SensorFragmentBMP180.newInstance();
                        break;
                    case "MLX90614":
                        selectedFragment = SensorFragmentMLX90614.newInstance();
                        break;
                    case "HMC5883L":
                        selectedFragment = SensorFragmentHMC5883L.newInstance();
                        break;
                    case "MPU6050":
                        selectedFragment = SensorFragmentMPU6050.newInstance();
                        break;
                    case "SHT21":
                        selectedFragment = SensorFragmentSHT21.newInstance();
                        break;
                    case "TSL2561":
                        selectedFragment = SensorFragmentTSL2561.newInstance();
                        break;
                    default:
                        break;
                }
                if (selectedFragment != null) {
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.replace(R.id.sensor_layout, selectedFragment);
                    transaction.commit();
                }
            }

        });
        return view;
    }

}