package io.pslab.fragment;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.pslab.R;
import io.pslab.activity.ThermometerActivity;
import io.pslab.models.SensorDataBlock;
import io.pslab.models.ThermometerData;
import io.pslab.others.CSVLogger;

import static android.content.Context.SENSOR_SERVICE;

public class ThermometerDataFragment extends Fragment {

    private float currentTemp = 0f;
    private SensorManager sensorManager;
    private Sensor sensor;
    private long startTime, block;
    private ArrayList<ThermometerData> recordedThermometerArray;
    private ThermometerData thermometerData = new ThermometerData();
    private ThermometerActivity thermometerActivity;
    private static int updatePeriod = 1000;
    private long previousTimeElapsed = (System.currentTimeMillis() - startTime) / updatePeriod;
    private int turns = 0;
    private boolean returningFromPause = false;
    private View rootView;

    public static ThermometerDataFragment newInstance() {
        return new ThermometerDataFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startTime = System.currentTimeMillis();
        thermometerActivity = (ThermometerActivity) getActivity();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_thermometer, container, false);
        thermometerActivity.addLocation = true;
        return rootView;
    }

    private SensorEventListener thermometerEventListner = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    private void setThermometerAnimation(float degree) {


    }

    @Override
    public void onResume() {
        super.onResume();
        if (thermometerActivity.playingData) {
            recordedThermometerArray = new ArrayList<>();
            resetInstrumentData();
            playRecordedData();
        } else if (thermometerActivity.viewingData) {
            recordedThermometerArray = new ArrayList<>();
            resetInstrumentData();
            plotAllRecordedData();
        } else if (!thermometerActivity.isRecording) {
            updateData();
            initiateThermometerSensor();
        } else if (returningFromPause) {
            updateData();
        }
    }

    private void processRecordedData(long timeGap) {
    }

    public void playData() {
        resetInstrumentData();
        thermometerActivity.startedPlay = true;
        try {
            if (recordedThermometerArray.size() > 1) {
                ThermometerData i = recordedThermometerArray.get(1);
                long timeGap = i.getTime() - i.getBlock();
                processRecordedData(timeGap);
            } else {
                processRecordedData(0);
            }
        } catch (IllegalArgumentException e) {
            Toast.makeText(getActivity(),
                    getActivity().getResources().getString(R.string.no_data_fetched), Toast.LENGTH_SHORT).show();
        }
    }

    public void stopData() {
        recordedThermometerArray.clear();
        plotAllRecordedData();
        thermometerActivity.startedPlay = false;
        thermometerActivity.playingData = false;
        turns = 0;
        thermometerActivity.invalidateOptionsMenu();
    }

    private void plotAllRecordedData() {
        recordedThermometerArray.addAll(thermometerActivity.recordedThermometerData);
        if (recordedThermometerArray.size() != 0) {
            for (ThermometerData d : recordedThermometerArray) {
                float degree = 0;
                setThermometerAnimation(degree);
            }
        }
    }

    private void playRecordedData() {
        recordedThermometerArray.addAll(thermometerActivity.recordedThermometerData);
        try {
            if (recordedThermometerArray.size() > 1) {
                ThermometerData i = recordedThermometerArray.get(1);
                long timeGap = i.getTime() - i.getBlock();
                processRecordedData(timeGap);
            } else {
                processRecordedData(0);
            }
        } catch (IllegalArgumentException e) {
            Toast.makeText(getActivity(),
                    getActivity().getResources().getString(R.string.no_data_fetched), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void updateData() {
        final Handler handler = new Handler();
    }

    private void visualizeData() {
        long timeElapsed = ((System.currentTimeMillis() - startTime) / updatePeriod);
        if (timeElapsed != previousTimeElapsed) {
            previousTimeElapsed = timeElapsed;
            long currentTime = System.currentTimeMillis();
            writeLogToFile(currentTime, thermometerData.getTemp());
        }
    }

    private void writeLogToFile(long timestamp, float temperature) {
        if (getActivity() != null && thermometerActivity.isRecording) {
            if (thermometerActivity.writeHeaderToFile) {
                thermometerActivity.csvLogger.prepareLogFile();
                thermometerActivity.csvLogger.writeCSVFile("Timestamp,DateTime,X-reading,Y-reading,Z-reading,Axis,Latitude,Longitude");
                block = timestamp;
                thermometerActivity.recordSensorDataBlockID(new SensorDataBlock(timestamp, thermometerActivity.getSensorName()));
                thermometerActivity.writeHeaderToFile = !thermometerActivity.writeHeaderToFile;
            }
            if (thermometerActivity.addLocation && thermometerActivity.gpsLogger.isGPSEnabled()) {
                String dateTime = CSVLogger.FILE_NAME_FORMAT.format(new Date(timestamp));
                Location location = thermometerActivity.gpsLogger.getDeviceLocation();
                thermometerActivity.csvLogger.writeCSVFile(timestamp + "," + dateTime + ","
                        + temperature);
                thermometerData = new ThermometerData(timestamp, block, temperature, location.getLatitude(), location.getLongitude());
            } else {
                String dateTime = CSVLogger.FILE_NAME_FORMAT.format(new Date(timestamp));
                thermometerActivity.csvLogger.writeCSVFile(timestamp + "," + dateTime + ","
                        + temperature);
                thermometerData = new ThermometerData(timestamp, block,temperature, 0.0, 0.0);
            }
            thermometerActivity.recordSensorData(thermometerData);
        } else {
            thermometerActivity.writeHeaderToFile = true;
        }
    }

    private void resetInstrumentData() {
        sensor = null;
        if (sensorManager != null) {
            sensorManager.unregisterListener(thermometerEventListner);
        }
        startTime = System.currentTimeMillis();
    }

    private void initiateThermometerSensor() {
        sensorManager = (SensorManager) getContext().getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        if (sensor == null) {
            Toast.makeText(getContext(), getContext().getResources().getString(R.string.no_thermometer_sensor), Toast.LENGTH_SHORT).show();
        } else {
            sensorManager.registerListener(thermometerEventListner, sensor, SensorManager.SENSOR_DELAY_GAME);
        }
    }
}