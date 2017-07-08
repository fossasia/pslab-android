package org.fossasia.pslab.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;

import org.fossasia.pslab.R;
import org.fossasia.pslab.communication.ScienceLab;
import org.fossasia.pslab.communication.peripherals.I2C;
import org.fossasia.pslab.communication.sensors.MPU6050;
import org.fossasia.pslab.others.ScienceLabCommon;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by asitava on 10/7/17.
 */

public class SensorFragmentMPU6050 extends Fragment {
    private ScienceLab scienceLab;
    private I2C i2c;
    private SensorDataFetch sensorDataFetch;
    private TextView tvSensorMPU6050ax;
    private TextView tvSensorMPU6050ay;
    private TextView tvSensorMPU6050az;
    private TextView tvSensorMPU6050gx;
    private TextView tvSensorMPU6050gy;
    private TextView tvSensorMPU6050gz;
    private TextView tvSensorMPU6050temp;
    private MPU6050 MPU6050;

    public static SensorFragmentMPU6050 newInstance() {
        SensorFragmentMPU6050 sensorFragmentMPU6050 = new SensorFragmentMPU6050();
        return sensorFragmentMPU6050;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scienceLab = ScienceLabCommon.scienceLab;
        i2c = scienceLab.i2c;
        try {
            MPU6050 = new MPU6050(i2c);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (scienceLab.isConnected()) {
                        try {
                            sensorDataFetch = new SensorDataFetch(MPU6050);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        sensorDataFetch.execute();
                    }
                }
            }
        };
        new Thread(runnable).start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sensor_mpu6050, container, false);

        tvSensorMPU6050ax = (TextView) view.findViewById(R.id.tv_sensor_mpu6050_ax);
        tvSensorMPU6050ay = (TextView) view.findViewById(R.id.tv_sensor_mpu6050_ay);
        tvSensorMPU6050az = (TextView) view.findViewById(R.id.tv_sensor_mpu6050_az);
        tvSensorMPU6050gx = (TextView) view.findViewById(R.id.tv_sensor_mpu6050_gx);
        tvSensorMPU6050gy = (TextView) view.findViewById(R.id.tv_sensor_mpu6050_gy);
        tvSensorMPU6050gz = (TextView) view.findViewById(R.id.tv_sensor_mpu6050_gz);
        tvSensorMPU6050temp = (TextView) view.findViewById(R.id.tv_sensor_mpu6050_temp);

        Spinner spinnerSensorMPU60501 = (Spinner) view.findViewById(R.id.spinner_sensor_mpu6050_1);
        Spinner spinnerSensorMPU60502 = (Spinner) view.findViewById(R.id.spinner_sensor_mpu6050_2);
        Spinner spinnerSensorMPU60503 = (Spinner) view.findViewById(R.id.spinner_sensor_mpu6050_3);
        Spinner spinnerSensorMPU60504 = (Spinner) view.findViewById(R.id.spinner_sensor_mpu6050_4);

        try {
            if (MPU6050 != null) {
                MPU6050.setAccelRange(spinnerSensorMPU60502.getSelectedItemPosition() - 1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            if (MPU6050 != null) {
                MPU6050.setGyroRange(spinnerSensorMPU60501.getSelectedItemPosition() - 1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return view;
    }

    private class SensorDataFetch extends AsyncTask<Void, Void, Void> {
        MPU6050 MPU6050 = new MPU6050(i2c);
        ArrayList<Double> dataMPU6050 = new ArrayList<Double>();

        private SensorDataFetch(MPU6050 MPU6050) throws IOException {
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (MPU6050 != null) {
                    dataMPU6050 = MPU6050.getRaw();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            tvSensorMPU6050ax.setText(String.valueOf(dataMPU6050.get(0)));
            tvSensorMPU6050ay.setText(String.valueOf(dataMPU6050.get(1)));
            tvSensorMPU6050az.setText(String.valueOf(dataMPU6050.get(2)));
            tvSensorMPU6050gx.setText(String.valueOf(dataMPU6050.get(3)));
            tvSensorMPU6050gy.setText(String.valueOf(dataMPU6050.get(4)));
            tvSensorMPU6050gz.setText(String.valueOf(dataMPU6050.get(5)));
            tvSensorMPU6050temp.setText(String.valueOf(dataMPU6050.get(6)));
        }
    }
}
