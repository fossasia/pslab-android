package org.fossasia.pslab.sensorfragment;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.fossasia.pslab.R;
import org.fossasia.pslab.communication.ScienceLab;
import org.fossasia.pslab.communication.peripherals.I2C;
import org.fossasia.pslab.others.ScienceLabCommon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Created by asitava on 13/7/17.
 */

public class SensorFragmentMain extends Fragment {

    private static final String TAG = "SensorFragmentMain";

    private I2C i2c;
    private ScienceLab scienceLab;
    private LinkedHashMap<Integer, String> sensorAddr = new LinkedHashMap<>();
    private ArrayList<String> dataAddress = new ArrayList<>();
    private ArrayList<String> dataName = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private String tvData = "";
    private ListView lvSensor;
    private TextView tvSensorScan;
    private Fragment selectedFragment = null;

    public static SensorFragmentMain newInstance() {
        return new SensorFragmentMain();
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

        adapter = new ArrayAdapter<>(getContext(), R.layout.sensor_list_item, R.id.tv_sensor_list_item, dataName);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sensor_main, container, false);
        final CoordinatorLayout coordinatorLayout = view.findViewById(R.id.layout_container);
        Button buttonSensorAutoScan = view.findViewById(R.id.button_sensor_autoscan);
        tvSensorScan = view.findViewById(R.id.tv_sensor_scan);
        tvSensorScan.setText(tvData);
        lvSensor = view.findViewById(R.id.lv_sensor);
        lvSensor.setAdapter(adapter);

        buttonSensorAutoScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (scienceLab.isConnected()) {
                    new Thread(scanRunnable).start();
                } else {
                    Snackbar snackbar = Snackbar.make(coordinatorLayout, "Device not connected", Snackbar.LENGTH_SHORT);
                    View snackBarView = snackbar.getView();
                    TextView snackBarTextView = snackBarView.findViewById(android.support.design.R.id.snackbar_text);
                    snackBarTextView.setTextColor(Color.YELLOW);
                    snackbar.show();
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
                case "BMP180":
                    selectedFragment = SensorFragmentBMP180.newInstance();
                    break;
                case "MLX90614":
                    if (howToConnectDialog(getString(R.string.ir_thermometer), getString(R.string.ir_thermometer_intro), R.drawable.mlx90614_schematic, getString(R.string.ir_thermometer_desc)))

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
                case "MPU925x":
                    selectedFragment = SensorFragmentMPU925X.newInstance();
                default:
                    Toast.makeText(getContext(), "Sensor Not Supported", Toast.LENGTH_SHORT).show();
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

    Runnable scanRunnable = new Runnable() {
        @Override
        public void run() {
            populateSensors();
        }
    };

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        new Thread(scanRunnable).start();
    }

    private void populateSensors() {
        ArrayList<Integer> data = new ArrayList<>();
        dataName.clear();
        dataAddress.clear();
        try {
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
        for (int key : sensorAddr.keySet()) {
            dataName.add(sensorAddr.get(key));
        }

        for (int key: sensorAddr.keySet()) {
            dataName.add(sensorAddr.get(key));
        }

        for (int key: sensorAddr.keySet()) {
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

    @SuppressLint("ResourceType")
    public boolean howToConnectDialog(String title, String intro, int imageID, String desc) {
        if (!tvData.contains(title)) {
            try {
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.custom_dialog_box, null);

                final TextView dialogText = dialogView.findViewById(R.id.custom_dialog_text);
                final TextView dialogDesc = dialogView.findViewById(R.id.description_text);
                final ImageView dialogImage = dialogView.findViewById(R.id.custom_dialog_schematic);
                final CheckBox doNotShowDialog = dialogView.findViewById(R.id.toggle_show_again);
                final Button okButton = dialogView.findViewById(R.id.dismiss_button);
                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                builder.setView(dialogView);
                builder.setTitle(title);

                dialogText.setText(intro);
                dialogImage.setImageResource(imageID);
                dialogDesc.setText(desc);
                final AlertDialog dialog = builder.create();
                final SharedPreferences sharedPreferences = getContext().getSharedPreferences(TAG+title, 0);

                Boolean skipDialog = sharedPreferences.getBoolean("skipDialog", false);
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (doNotShowDialog.isChecked()) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean("skipDialog", true);
                            editor.apply();
                        }
                        dialog.dismiss();
                    }
                });

                if(!skipDialog)
                    dialog.show();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }
}