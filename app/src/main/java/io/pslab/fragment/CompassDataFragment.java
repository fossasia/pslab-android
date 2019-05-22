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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.pslab.R;
import io.pslab.activity.CompassActivity;
import io.pslab.models.CompassData;
import io.pslab.models.SensorDataBlock;
import io.pslab.others.CSVLogger;

import static android.content.Context.SENSOR_SERVICE;
import static io.pslab.others.CSVLogger.CSV_DIRECTORY;

public class CompassDataFragment extends Fragment {

    private Unbinder unbinder;
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
            compassData.setBx(String.valueOf(degree));
            xAxisMagneticField.setText(String.valueOf(degree));

            degree = Math.round(event.values[1]);
            if (degree < 0)
                degree += 360;
            compassData.setBy(String.valueOf(degree));
            yAxisMagneticField.setText(String.valueOf(degree));

            degree = Math.round(event.values[2]);
            if (degree < 0)
                degree += 360;
            compassData.setBz(String.valueOf(degree));
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
            initiateCompassSensor();
        } else if (returningFromPause) {
            updateData();
        }
    }

    public void saveGraph() {
        String fileName = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(compassActivity.recordedCompassData.get(0).getTime());
        File csvFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                File.separator + CSV_DIRECTORY + File.separator + compassActivity.getSensorName() +
                File.separator + fileName + ".csv");
        if (!csvFile.exists()) {
            try {
                csvFile.createNewFile();
                PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(csvFile, true)));
                out.write("Timestamp,DateTime,X-reading,Y-reading,Z-reading,Axis,Latitude,Longitude\n");
                for (CompassData compassData : compassActivity.recordedCompassData) {
                    out.write(compassData.getTime() + ","
                            + CSVLogger.FILE_NAME_FORMAT.format(new Date(compassData.getTime())) + ","
                            + compassData.getBx() + ","
                            + compassData.getBy() + ","
                            + compassData.getBz() + ","
                            + compassData.getAxis() + ","
                            + compassData.getLat() + ","
                            + compassData.getLon() + "," + "\n");
                }
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
                                if (d.getAxis().equals(getContext().getResources().getString(R.string.compass_X_axis))){
                                    direction = 0;
                                    xAxisRadioButton.setChecked(true);
                                    yAxisRadioButton.setChecked(false);
                                    zAxisRadioButton.setChecked(false);
                                } else if (d.getAxis().equals(getContext().getResources().getString(R.string.compass_Y_axis))){
                                    direction = 1;
                                    xAxisRadioButton.setChecked(false);
                                    yAxisRadioButton.setChecked(true);
                                    zAxisRadioButton.setChecked(false);
                                } else if (d.getAxis().equals(getContext().getResources().getString(R.string.compass_Z_axis))){
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
                                            degree = Math.round(Float.valueOf(d.getBx()));
                                            if (degree < 0)
                                                degree += 360;
                                        }
                                        break;
                                    case 1:
                                        if (d.getBy() != null) {
                                            degree = Math.round(Float.valueOf(d.getBy()));
                                            if (degree < 0)
                                                degree += 360;
                                        }
                                        break;
                                    case 2:
                                        if (d.getBz() != null) {
                                            degree = Math.round(Float.valueOf(d.getBz()));
                                            if (degree < 0)
                                                degree += 360;
                                        }
                                        break;
                                    default:
                                        if (d.getBx() != null) {
                                            degree = Math.round(Float.valueOf(d.getBx()));
                                        }
                                        break;
                                }

                                setCompassAnimation(degree);

                                degreeIndicator.setText(String.valueOf(degree));
                                currentDegree = -degree;

                                if (d.getBx() != null) {
                                    degree = Math.round(Float.valueOf(d.getBx()));
                                }
                                if (degree < 0)
                                    degree += 360;
                                compassData.setBx(String.valueOf(degree));
                                xAxisMagneticField.setText(String.valueOf(degree));

                                if (d.getBy() != null) {
                                    degree = Math.round(Float.valueOf(d.getBy()));
                                }
                                if (degree < 0)
                                    degree += 360;
                                compassData.setBy(String.valueOf(degree));
                                yAxisMagneticField.setText(String.valueOf(degree));

                                if (d.getBz() != null) {
                                    degree = Math.round(Float.valueOf(d.getBz()));
                                }
                                if (degree < 0)
                                    degree += 360;
                                compassData.setBz(String.valueOf(degree));
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
            Toast.makeText(getActivity(),
                    getActivity().getResources().getString(R.string.no_data_fetched), Toast.LENGTH_SHORT).show();
        }
    }

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
                            degree = Math.round(Float.valueOf(d.getBx()));
                            if (degree < 0)
                                degree += 360;
                        }
                        break;
                    case 1:
                        if (d.getBy() != null) {
                            degree = Math.round(Float.valueOf(d.getBy()));
                            if (degree < 0)
                                degree += 360;
                        }
                        break;
                    case 2:
                        if (d.getBz() != null) {
                            degree = Math.round(Float.valueOf(d.getBz()));
                            if (degree < 0)
                                degree += 360;
                        }
                        break;
                    default:
                        if (d.getBx() != null) {
                            degree = Math.round(Float.valueOf(d.getBx()));
                        }
                        break;
                }

                setCompassAnimation(degree);

                degreeIndicator.setText(String.valueOf(degree));
                currentDegree = -degree;

                if (d.getBx() != null) {
                    degree = Math.round(Float.valueOf(d.getBx()));
                }
                if (degree < 0)
                    degree += 360;
                compassData.setBx(String.valueOf(degree));
                xAxisMagneticField.setText(String.valueOf(degree));

                if (d.getBy() != null) {
                    degree = Math.round(Float.valueOf(d.getBy()));
                }
                if (degree < 0)
                    degree += 360;
                compassData.setBy(String.valueOf(degree));
                yAxisMagneticField.setText(String.valueOf(degree));

                if (d.getBz() != null) {
                    degree = Math.round(Float.valueOf(d.getBz()));
                }
                if (degree < 0)
                    degree += 360;
                compassData.setBz(String.valueOf(degree));
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
            Toast.makeText(getActivity(),
                    getActivity().getResources().getString(R.string.no_data_fetched), Toast.LENGTH_SHORT).show();
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

    private void writeLogToFile(long timestamp, String compassXvalue, String compassYvalue, String compassZvalue, String compassAxis) {
        if (getActivity() != null && compassActivity.isRecording) {
            if (compassActivity.writeHeaderToFile) {
                compassActivity.csvLogger.prepareLogFile();
                compassActivity.csvLogger.writeCSVFile("Timestamp,DateTime,X-reading,Y-reading,Z-reading,Axis,Latitude,Longitude");
                block = timestamp;
                compassActivity.recordSensorDataBlockID(new SensorDataBlock(timestamp, compassActivity.getSensorName()));
                compassActivity.writeHeaderToFile = !compassActivity.writeHeaderToFile;
            }
            if (compassActivity.addLocation && compassActivity.gpsLogger.isGPSEnabled()) {
                String dateTime = CSVLogger.FILE_NAME_FORMAT.format(new Date(timestamp));
                Location location = compassActivity.gpsLogger.getDeviceLocation();
                compassActivity.csvLogger.writeCSVFile(timestamp + "," + dateTime + ","
                        + compassXvalue + "," + compassYvalue + "," + compassZvalue + "," + compassAxis + "," + location.getLatitude() + "," + location.getLongitude());
                compassData = new CompassData(timestamp, block, compassXvalue, compassYvalue, compassZvalue, compassAxis, location.getLatitude(), location.getLongitude());
            } else {
                String dateTime = CSVLogger.FILE_NAME_FORMAT.format(new Date(timestamp));
                compassActivity.csvLogger.writeCSVFile(timestamp + "," + dateTime + ","
                        + compassXvalue + "," + compassYvalue + "," + compassZvalue + "," + compassAxis + ",0.0, 0.0");
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

    private void initiateCompassSensor() {
        sensorManager = (SensorManager) getContext().getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        if (sensor == null) {
            Toast.makeText(getContext(), getContext().getResources().getString(R.string.no_compass_sensor), Toast.LENGTH_SHORT).show();
        } else {
            sensorManager.registerListener(compassEventListner, sensor, SensorManager.SENSOR_DELAY_GAME);
        }
    }
}