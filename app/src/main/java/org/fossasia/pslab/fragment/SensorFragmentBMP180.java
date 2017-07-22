package org.fossasia.pslab.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.fossasia.pslab.R;
import org.fossasia.pslab.communication.ScienceLab;
import org.fossasia.pslab.communication.peripherals.I2C;
import org.fossasia.pslab.communication.sensors.BMP180;
import org.fossasia.pslab.others.ScienceLabCommon;

import java.io.IOException;

/**
 * Created by asitava on 10/7/17.
 */

public class SensorFragmentBMP180 extends Fragment {
    private ScienceLab scienceLab;
    private I2C i2c;
    private SensorDataFetch sensorDataFetch;
    private TextView tvSensorBMP180Temp;
    private TextView tvSensorBMP180Altitude;
    private TextView tvSensorBMP180Pressure;
    private BMP180 BMP180;

    public static SensorFragmentBMP180 newInstance() {
        SensorFragmentBMP180 sensorFragmentBMP180 = new SensorFragmentBMP180();
        return sensorFragmentBMP180;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scienceLab = ScienceLabCommon.scienceLab;
        i2c = scienceLab.i2c;
        try {
            BMP180 = new BMP180(i2c);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (scienceLab.isConnected()) {
                        try {
                            sensorDataFetch = new SensorDataFetch();
                        } catch (IOException | InterruptedException e) {
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
        View view = inflater.inflate(R.layout.sensor_bmp180, container, false);

        tvSensorBMP180Temp = (TextView) view.findViewById(R.id.tv_sensor_bmp180_temp);
        tvSensorBMP180Altitude = (TextView) view.findViewById(R.id.tv_sensor_bmp180_altitude);
        tvSensorBMP180Pressure = (TextView) view.findViewById(R.id.tv_sensor_bmp180_pressure);

        return view;
    }


    private class SensorDataFetch extends AsyncTask<Void, Void, Void> {
        BMP180 BMP180 = new BMP180(i2c);
        double[] dataBMP180 = new double[3];

        private SensorDataFetch() throws IOException, InterruptedException {
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (BMP180 != null) {
                    dataBMP180 = BMP180.getRaw();
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            tvSensorBMP180Temp.setText(String.valueOf(dataBMP180[0]));
            tvSensorBMP180Altitude.setText(String.valueOf(dataBMP180[1]));
            tvSensorBMP180Pressure.setText(String.valueOf(dataBMP180[2]));
        }
    }
}