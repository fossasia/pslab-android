package org.fossasia.pslab.fragment;

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
    private ArrayList<Integer> data = new ArrayList<Integer>();
    private ArrayList<String> dataAddress = new ArrayList<String>();
    private ArrayList<String> dataName = new ArrayList<String>();
    private ArrayAdapter<String> adapter;
    private ListView lvSensor;
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sensor_main, container, false);
        sensorAddr.put(0x60, "MCP4728");
        sensorAddr.put(0x48, "ADS1115");
        sensorAddr.put(0x23, "BH1750");
        sensorAddr.put(0x77, "BMP180");
        sensorAddr.put(0x5A, "MLX90614");
        sensorAddr.put(0x1E, "HMC5883L");
        sensorAddr.put(0x68, "MPU6050");
        sensorAddr.put(0x40, "SHT21");
        sensorAddr.put(0x39, "TSL2561");

        Button buttonSensorAutoscan = (Button) view.findViewById(R.id.button_sensor_autoscan);
        final TextView tvSensorScan = (TextView) view.findViewById(R.id.tv_sensor_scan);
        lvSensor = (ListView) view.findViewById(R.id.lv_sensor);

        buttonSensorAutoscan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (scienceLab.isConnected()) {
                    String tvData = "";
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
                            tvData += s + "\n";
                        }
                        tvSensorScan.setText(tvData);
                    }
                    String[] dataDisp = dataName.toArray(new String[dataName.size()]);
                    adapter = new ArrayAdapter<String>(getContext(), R.layout.sensor_list_item, R.id.tv_sensor_list_item, dataDisp);
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
                        break;
                    case "BH1750":
                        break;
                    case "BMP180":
                        break;
                    case "MLX90614":
                        break;
                    case "HMC5883L":
                        selectedFragment = SensorFragmentHMC5883L.newInstance();
                        break;
                    case "MPU6050":
                        selectedFragment = SensorFragmentMPU6050.newInstance();
                        break;
                    case "SHT21":
                        break;
                    case "TSL2561":
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