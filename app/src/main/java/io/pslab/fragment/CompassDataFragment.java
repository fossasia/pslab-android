package io.pslab.fragment;

import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import androidx.annotation.NonNull;

import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.pslab.R;
import io.pslab.activity.CompassActivity;
import io.pslab.communication.ScienceLab;
import io.pslab.communication.peripherals.I2C;
import io.pslab.interfaces.OperationCallback;
import io.pslab.models.CompassData;
import io.pslab.models.SensorDataBlock;
import io.pslab.others.CSVDataLine;
import io.pslab.others.CSVLogger;
import io.pslab.others.CustomSnackBar;
import io.pslab.others.ScienceLabCommon;

import static android.content.Context.SENSOR_SERVICE;
import static io.pslab.others.CSVLogger.CSV_DIRECTORY;

public class CompassDataFragment extends Fragment implements OperationCallback {

    private static final CSVDataLine CSV_HEADER = new CSVDataLine()
            .add("Timestamp")
            .add("DateTime")
            .add("X-reading")
            .add("Y-reading")
            .add("Z-reading")
            .add("Axis")
            .add("Latitude")
            .add("Longitude");
    private Unbinder unbinder;
    private static int sensorType = 0;
    @Nullable
    @BindView(R.id.compass)
    ImageView compass;
    @BindView(R.id.degree_indicator)
    TextView degreeIndicator;

    @BindView(R.id.compass_radio_button_x_axis)
    RadioButton xAxisRadioButton;
    @BindView(R.id.compass_radio_button_y_axis)
    RadioButton yAxisRadioButton;
    @BindView(R.id.compass_radio_button_z_axis)
    RadioButton zAxisRadioButton;

    @BindView(R.id.tv_sensor_hmc5883l_bx)
    TextView xAxisMagneticField;
    @BindView(R.id.tv_sensor_hmc5883l_by)
    TextView yAxisMagneticField;
    @BindView(R.id.tv_sensor_hmc5883l_bz)
    TextView zAxisMagneticField;

    private enum COMPASS_SENSOR {INBUILT_SENSOR, HMC5883L_SENSOR}

    private float currentDegree = 0f;
    private int direction;
    private SensorManager sensorManager;
    private Sensor sensor;
    private Timer graphTimer;
    private long startTime, block;
    private ArrayList<CompassData> recordedCompassArray;
    private CompassData compassData = new CompassData();
    private CompassActivity compassActivity;
    private static int updatePeriod = 1000;
    private long previousTimeElapsed = (System.currentTimeMillis() - startTime) / updatePeriod;
    private int turns = 0;
    private boolean returningFromPause = false;
    private View rootView;

    public static CompassDataFragment newInstance() {
        return new CompassDataFragment();
    }

    public static void setParameters(String type) {
        CompassDataFragment.sensorType = Integer.valueOf(type);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startTime = System.currentTimeMillis();
        compassActivity = (CompassActivity) getActivity();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_compass, container, false);
        unbinder = ButterKnife.bind(this, rootView);

        xAxisRadioButton.setChecked(true);
        direction = 0;

        xAxisRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                xAxisRadioButton.setChecked(true);
                yAxisRadioButton.setChecked(false);
                zAxisRadioButton.setChecked(false);
                compassData.setAxis(getContext().getResources().getString(R.string.compass_X_axis));
                direction = 0;
            }
        });

        yAxisRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                xAxisRadioButton.setChecked(false);
                yAxisRadioButton.setChecked(true);
                zAxisRadioButton.setChecked(false);
                compassData.setAxis(getContext().getResources().getString(R.string.compass_Y_axis));
                direction = 1;
            }
        });

        zAxisRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                xAxisRadioButton.setChecked(false);
                yAxisRadioButton.setChecked(false);
                zAxisRadioButton.setChecked(true);
                compassData.setAxis(getContext().getResources().getString(R.string.compass_Z_axis));
                direction = 2;
            }
        });
        compassActivity.addLocation = true;
        return rootView;
    }

    private SensorEventListener compassEventListner = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float degree;
            switch (direction) {
                case 0:
                    degree = Math.round(event.values[0]);
                    if (degree < 0)
                        degree += 360;
                    break;
                case 1:
                    degree = Math.round(event.values[1]);
                    if (degree < 0)
                        degree += 360;
                    break;
                case 2:
                    degree = Math.round(event.values[2]);
                    if (degree < 0)
                        degree += 360;
                    break;
                default:
                    degree = Math.round(event.values[0]);
                    break;
            }

            setCompassAnimation(degree);

            degreeIndicator.setText(String.valueOf(degree));
            currentDegree = -degree;

            degree = Math.round(event.values[0]);
            if (degree < 0)
                degree += 360;
            compassData.setBx(degree);
            xAxisMagneticField.setText(String.valueOf(degree));

            degree = Math.round(event.values[1]);
            if (degree < 0)
                degree += 360;
            compassData.setBy(degree);
            yAxisMagneticField.setText(String.valueOf(degree));

            degree = Math.round(event.values[2]);
            if (degree < 0)
                degree += 360;
            compassData.setBz(degree);
            zAxisMagneticField.setText(String.valueOf(degree));
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    private void setCompassAnimation(float degree) {

        RotateAnimation ra = new RotateAnimation(
                currentDegree,
                -degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);

        ra.setDuration(210);
        ra.setFillAfter(true);

        compass.startAnimation(ra);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (compassActivity.playingData) {
            recordedCompassArray = new ArrayList<>();
            resetInstrumentData();
            playRecordedData();
        } else if (compassActivity.viewingData) {
            recordedCompassArray = new ArrayList<>();
            resetInstrumentData();
            plotAllRecordedData();
        } else if (!compassActivity.isRecording) {
            updateData();
            initiateCompassSensor(sensorType);
        } else if (returningFromPause) {
            updateData();
        }
    }

    @Override
    public void saveGraph() {
        compassActivity.csvLogger.prepareLogFile();
        compassActivity.csvLogger.writeMetaData(getResources().getString(R.string.compass));
        compassActivity.csvLogger.writeCSVFile(CSV_HEADER);
        for (CompassData compassData : compassActivity.recordedCompassData) {
            compassActivity.csvLogger.writeCSVFile(
                    new CSVDataLine()
                            .add(compassData.getTime())
                            .add(CSVLogger.FILE_NAME_FORMAT.format(new Date(compassData.getTime())))
                            .add(compassData.getBx())
                            .add(compassData.getBy())
                            .add(compassData.getBz())
                            .add(compassData.getAxis())
                            .add(compassData.getLat())
                            .add(compassData.getLon())
            );
        }
        View view = rootView.findViewById(R.id.compass_card);
        view.setDrawingCacheEnabled(true);
        Bitmap b = view.getDrawingCache();
        try {
            b.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(Environment.getExternalStorageDirectory().getAbsolutePath() +
                    File.separator + CSV_DIRECTORY + File.separator + compassActivity.getSensorName() +
                    File.separator + CSVLogger.FILE_NAME_FORMAT.format(new Date()) + "_graph.jpg"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void processRecordedData(long timeGap) {
        final Handler handler = new Handler();
        if (graphTimer != null) {
            graphTimer.cancel();
        } else {
            graphTimer = new Timer();
        }
        graphTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (compassActivity.playingData) {
                            try {
                                CompassData d = recordedCompassArray.get(turns);
                                if (d.getAxis().equals(getContext().getResources().getString(R.string.compass_X_axis))) {
                                    direction = 0;
                                    xAxisRadioButton.setChecked(true);
                                    yAxisRadioButton.setChecked(false);
                                    zAxisRadioButton.setChecked(false);
                                } else if (d.getAxis().equals(getContext().getResources().getString(R.string.compass_Y_axis))) {
                                    direction = 1;
                                    xAxisRadioButton.setChecked(false);
                                    yAxisRadioButton.setChecked(true);
                                    zAxisRadioButton.setChecked(false);
                                } else if (d.getAxis().equals(getContext().getResources().getString(R.string.compass_Z_axis))) {
                                    direction = 2;
                                    xAxisRadioButton.setChecked(false);
                                    yAxisRadioButton.setChecked(false);
                                    zAxisRadioButton.setChecked(true);
                                }
                                turns++;
                                float degree = 0;
                                switch (direction) {
                                    case 0:
                                        if (d.getBx() != null) {
                                            degree = Math.round(d.getBx());
                                            if (degree < 0)
                                                degree += 360;
                                        }
                                        break;
                                    case 1:
                                        if (d.getBy() != null) {
                                            degree = Math.round(d.getBy());
                                            if (degree < 0)
                                                degree += 360;
                                        }
                                        break;
                                    case 2:
                                        if (d.getBz() != null) {
                                            degree = Math.round(d.getBz());
                                            if (degree < 0)
                                                degree += 360;
                                        }
                                        break;
                                    default:
                                        if (d.getBx() != null) {
                                            degree = Math.round(d.getBx());
                                        }
                                        break;
                                }

                                setCompassAnimation(degree);

                                degreeIndicator.setText(String.valueOf(degree));
                                currentDegree = -degree;

                                if (d.getBx() != null) {
                                    degree = Math.round(d.getBx());
                                }
                                if (degree < 0)
                                    degree += 360;
                                compassData.setBx(degree);
                                xAxisMagneticField.setText(String.valueOf(degree));

                                if (d.getBy() != null) {
                                    degree = Math.round(d.getBy());
                                }
                                if (degree < 0)
                                    degree += 360;
                                compassData.setBy(degree);
                                yAxisMagneticField.setText(String.valueOf(degree));

                                if (d.getBz() != null) {
                                    degree = Math.round(d.getBz());
                                }
                                if (degree < 0)
                                    degree += 360;
                                compassData.setBz(degree);
                                zAxisMagneticField.setText(String.valueOf(degree));

                            } catch (IndexOutOfBoundsException e) {
                                graphTimer.cancel();
                                graphTimer = null;
                                turns = 0;
                                compassActivity.playingData = false;
                                compassActivity.startedPlay = false;
                                compassActivity.invalidateOptionsMenu();
                            }
                        }
                    }
                });
            }
        }, 0, timeGap);
    }

    @Override
    public void playData() {
        resetInstrumentData();
        compassActivity.startedPlay = true;
        try {
            if (recordedCompassArray.size() > 1) {
                CompassData i = recordedCompassArray.get(1);
                long timeGap = i.getTime() - i.getBlock();
                processRecordedData(timeGap);
            } else {
                processRecordedData(0);
            }
        } catch (IllegalArgumentException e) {
            CustomSnackBar.showSnackBar(getActivity().findViewById(android.R.id.content),
                    getString(R.string.no_data_fetched), null, null, Snackbar.LENGTH_SHORT);
        }
    }

    @Override
    public void stopData() {
        if (graphTimer != null) {
            graphTimer.cancel();
            graphTimer = null;
        }
        recordedCompassArray.clear();
        plotAllRecordedData();
        compassActivity.startedPlay = false;
        compassActivity.playingData = false;
        turns = 0;
        compassActivity.invalidateOptionsMenu();
    }

    private void plotAllRecordedData() {
        recordedCompassArray.addAll(compassActivity.recordedCompassData);
        if (recordedCompassArray.size() != 0) {
            for (CompassData d : recordedCompassArray) {
                float degree = 0;
                switch (direction) {
                    case 0:
                        if (d.getBx() != null) {
                            degree = Math.round(d.getBx());
                            if (degree < 0)
                                degree += 360;
                        }
                        break;
                    case 1:
                        if (d.getBy() != null) {
                            degree = Math.round(d.getBy());
                            if (degree < 0)
                                degree += 360;
                        }
                        break;
                    case 2:
                        if (d.getBz() != null) {
                            degree = Math.round(d.getBz());
                            if (degree < 0)
                                degree += 360;
                        }
                        break;
                    default:
                        if (d.getBx() != null) {
                            degree = Math.round(d.getBx());
                        }
                        break;
                }

                setCompassAnimation(degree);

                degreeIndicator.setText(String.valueOf(degree));
                currentDegree = -degree;

                if (d.getBx() != null) {
                    degree = Math.round(d.getBx());
                }
                if (degree < 0)
                    degree += 360;
                compassData.setBx(degree);
                xAxisMagneticField.setText(String.valueOf(degree));

                if (d.getBy() != null) {
                    degree = Math.round(d.getBy());
                }
                if (degree < 0)
                    degree += 360;
                compassData.setBy(degree);
                yAxisMagneticField.setText(String.valueOf(degree));

                if (d.getBz() != null) {
                    degree = Math.round(d.getBz());
                }
                if (degree < 0)
                    degree += 360;
                compassData.setBz(degree);
                zAxisMagneticField.setText(String.valueOf(degree));

                if (d.getAxis().equals(getContext().getResources().getString(R.string.compass_X_axis))) {
                    xAxisRadioButton.setChecked(true);
                    yAxisRadioButton.setChecked(false);
                    zAxisRadioButton.setChecked(false);
                } else if (d.getAxis().equals(getContext().getResources().getString(R.string.compass_Y_axis))) {
                    xAxisRadioButton.setChecked(false);
                    yAxisRadioButton.setChecked(true);
                    zAxisRadioButton.setChecked(false);
                } else if (d.getAxis().equals(getContext().getResources().getString(R.string.compass_Z_axis))) {
                    xAxisRadioButton.setChecked(false);
                    yAxisRadioButton.setChecked(false);
                    zAxisRadioButton.setChecked(true);
                }
            }
        }
    }

    private void playRecordedData() {
        recordedCompassArray.addAll(compassActivity.recordedCompassData);
        try {
            if (recordedCompassArray.size() > 1) {
                CompassData i = recordedCompassArray.get(1);
                long timeGap = i.getTime() - i.getBlock();
                processRecordedData(timeGap);
            } else {
                processRecordedData(0);
            }
        } catch (IllegalArgumentException e) {
            CustomSnackBar.showSnackBar(getActivity().findViewById(android.R.id.content),
                    getString(R.string.no_data_fetched), null, null, Snackbar.LENGTH_SHORT);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (graphTimer != null) {
            graphTimer.cancel();
        }
        if (sensorManager != null) {
            sensorManager.unregisterListener(compassEventListner);
        }
        unbinder.unbind();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (graphTimer != null) {
            returningFromPause = true;
            graphTimer.cancel();
            graphTimer = null;
            if (compassActivity.playingData) {
                compassActivity.finish();
            }
        }
    }

    private void updateData() {
        final Handler handler = new Handler();
        if (graphTimer != null) {
            graphTimer.cancel();
        }
        graphTimer = new Timer();
        graphTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            visualizeData();
                        } catch (NullPointerException e) {
                            /* Pass for another refresh round */
                        }
                    }
                });
            }
        }, 0, updatePeriod);
    }

    private void visualizeData() {
        long timeElapsed = ((System.currentTimeMillis() - startTime) / updatePeriod);
        if (timeElapsed != previousTimeElapsed) {
            previousTimeElapsed = timeElapsed;
            long currentTime = System.currentTimeMillis();
            writeLogToFile(currentTime, compassData.getBx(), compassData.getBy(), compassData.getBz(), compassData.getAxis());
        }
    }

    private void writeLogToFile(long timestamp, Float compassXvalue, Float compassYvalue, Float compassZvalue, String compassAxis) {
        if (getActivity() != null && compassActivity.isRecording) {
            if (compassActivity.writeHeaderToFile) {
                compassActivity.csvLogger.prepareLogFile();
                compassActivity.csvLogger.writeMetaData(getResources().getString(R.string.compass));
                compassActivity.csvLogger.writeCSVFile(CSV_HEADER);
                block = timestamp;
                compassActivity.recordSensorDataBlockID(new SensorDataBlock(timestamp, compassActivity.getSensorName()));
                compassActivity.writeHeaderToFile = !compassActivity.writeHeaderToFile;
            }
            if (compassActivity.addLocation && compassActivity.gpsLogger.isGPSEnabled()) {
                Location location = compassActivity.gpsLogger.getDeviceLocation();
                compassActivity.csvLogger.writeCSVFile(
                        new CSVDataLine()
                                .add(timestamp)
                                .add(CSVLogger.FILE_NAME_FORMAT.format(new Date(timestamp)))
                                .add(compassXvalue)
                                .add(compassYvalue)
                                .add(compassZvalue)
                                .add(compassAxis)
                                .add(location.getLatitude())
                                .add(location.getLongitude())
                );
                compassData = new CompassData(timestamp, block, compassXvalue, compassYvalue, compassZvalue, compassAxis, location.getLatitude(), location.getLongitude());
            } else {
                compassActivity.csvLogger.writeCSVFile(
                        new CSVDataLine()
                                .add(timestamp)
                                .add(CSVLogger.FILE_NAME_FORMAT.format(new Date(timestamp)))
                                .add(compassXvalue)
                                .add(compassYvalue)
                                .add(compassZvalue)
                                .add(compassAxis)
                                .add(0.0)
                                .add(0.0)
                );
                compassData = new CompassData(timestamp, block, compassXvalue, compassYvalue, compassZvalue, compassAxis, 0.0, 0.0);
            }
            compassActivity.recordSensorData(compassData);
        } else {
            compassActivity.writeHeaderToFile = true;
        }
    }

    private void resetInstrumentData() {
        sensor = null;
        if (sensorManager != null) {
            sensorManager.unregisterListener(compassEventListner);
        }
        startTime = System.currentTimeMillis();
        xAxisMagneticField.setText(getResources().getString(R.string.value_null));
        yAxisMagneticField.setText(getResources().getString(R.string.value_null));
        zAxisMagneticField.setText(getResources().getString(R.string.value_null));
    }

    private void initiateCompassSensor(int type) {

        CompassDataFragment.COMPASS_SENSOR s = CompassDataFragment.COMPASS_SENSOR.values()[type];
        resetInstrumentData();
        ScienceLab scienceLab;
        switch (s) {
            case INBUILT_SENSOR:
                degreeIndicator.setText(getResources().getStringArray(R.array.compass_sensors)[0]);
                sensorManager = (SensorManager) getContext().getSystemService(SENSOR_SERVICE);
                sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
                if (sensor == null) {
                    CustomSnackBar.showSnackBar(getActivity().findViewById(android.R.id.content),
                            getString(R.string.no_compass_sensor), null, null, Snackbar.LENGTH_LONG);
                } else {
                    sensorManager.registerListener(compassEventListner, sensor, SensorManager.SENSOR_DELAY_GAME);
                }
                break;
            case HMC5883L_SENSOR:
                degreeIndicator.setText(getResources().getStringArray(R.array.compass_sensors)[1]);
                scienceLab = ScienceLabCommon.scienceLab;
                if (scienceLab.isConnected()) {
                    try {
                        I2C i2c = scienceLab.i2c;
                        ArrayList<Integer> data;
                        data = i2c.scan(null);
                        if (data.contains(0x39)) {
                            sensorType = 1;
                        } else {
                            CustomSnackBar.showSnackBar(getActivity().findViewById(android.R.id.content),
                                    getString(R.string.sensor_not_connected_tls), null, null, Snackbar.LENGTH_SHORT);
                            sensorType = 0;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    CustomSnackBar.showSnackBar(getActivity().findViewById(android.R.id.content),
                            getString(R.string.device_not_found), null, null, Snackbar.LENGTH_SHORT);
                    sensorType = 0;
                }
                break;
            default:
                break;
        }

    }
}