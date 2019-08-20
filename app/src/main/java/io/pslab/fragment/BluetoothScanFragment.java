package io.pslab.fragment;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

import io.pslab.R;

public class BluetoothScanFragment extends DialogFragment {
    private Button bluetoothScanStopButton;
    private ProgressBar scanProgressBar;
    private ListView scannedDevicesListView;
    private ArrayAdapter<String> deviceListAdapter;
    private ArrayList<String> deviceList;
    private ArrayList<BluetoothDevice> bluetoothDevices;
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    String deviceName = device.getName();
                    deviceList.add(deviceName);
                    bluetoothDevices.add(device);
                    deviceListAdapter.notifyDataSetChanged();
                }
            }
        }
    };
    private BluetoothAdapter bluetoothAdapter;
    private boolean isScanning = false;
    private BluetoothDevice bluetoothDevice;
    private BluetoothSocket mSocket;
    private OutputStream mOutputStream;
    private InputStream mInputStream;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_bluetooth_scan, container, false);
        scanProgressBar = rootView.findViewById(R.id.bluetooth_scan_progressbar);
        scanProgressBar.setVisibility(View.GONE);
        scannedDevicesListView = rootView.findViewById(R.id.bluetooth_scanned_devices_list);
        deviceList = new ArrayList<>();
        bluetoothDevices = new ArrayList<>();
        deviceListAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, deviceList);
        scannedDevicesListView.setAdapter(deviceListAdapter);
        bluetoothScanStopButton = rootView.findViewById(R.id.bluetooth_scan_stop_button);


        bluetoothScanStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isScanning) {
                    if (bluetoothAdapter != null) {
                        bluetoothAdapter.cancelDiscovery();
                    }
                    scanProgressBar.setVisibility(View.GONE);
                    isScanning = false;
                    bluetoothScanStopButton.setText(getResources().getString(R.string.bluetooth_scan_text));
                    scannedDevicesListView.setClickable(true);
                } else {
                    scanDevices();
                }
            }
        });


        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        getContext().registerReceiver(broadcastReceiver, filter);

        scannedDevicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getContext(), bluetoothDevices.get(position).getAddress(), Toast.LENGTH_SHORT).show();
                bluetoothDevice = bluetoothDevices.get(position);
                getDialog().cancel();
                connectBluetooth();
            }
        });
        scanDevices();
        return rootView;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
    }

    private void scanDevices() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            isScanning = false;
            Toast.makeText(getContext(), getResources().getString(R.string.bluetooth_not_supported), Toast.LENGTH_SHORT).show();
        } else {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                int BLUETOOTH_REQUEST_CODE = 100;
                startActivityForResult(enableBtIntent, BLUETOOTH_REQUEST_CODE);
                isScanning = false;
                bluetoothScanStopButton.setText(getResources().getString(R.string.bluetooth_scan_text));
                scannedDevicesListView.setClickable(true);
            } else {
                isScanning = true;
                scannedDevicesListView.setClickable(false);
                bluetoothAdapter.startDiscovery();
                scanProgressBar.setVisibility(View.VISIBLE);
                bluetoothScanStopButton.setText(getResources().getString(R.string.bluetooth_stop_text));
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
    }

    private void connectBluetooth() {
        try {
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
            mSocket = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid);
            mSocket.connect();
            mOutputStream = mSocket.getOutputStream();
            mInputStream = mSocket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
