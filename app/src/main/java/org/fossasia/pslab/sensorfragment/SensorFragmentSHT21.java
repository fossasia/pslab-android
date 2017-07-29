package org.fossasia.pslab.sensorfragment;

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
import org.fossasia.pslab.communication.sensors.SHT21;
import org.fossasia.pslab.others.ScienceLabCommon;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by asitava on 10/7/17.
 */

public class SensorFragmentSHT21 extends Fragment {
    private ScienceLab scienceLab;
    private I2C i2c;
    private SensorDataFetch sensorDataFetch;
    private TextView tvSensorSHT21Temp;
    private TextView tvSensorSHT21Humidity;
    private SHT21 sensorSHT21;

    public static SensorFragmentSHT21 newInstance() {
        SensorFragmentSHT21 sensorFragmentSHT21 = new SensorFragmentSHT21();
        return sensorFragmentSHT21;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scienceLab = ScienceLabCommon.scienceLab;
        i2c = scienceLab.i2c;
        try {
            sensorSHT21 = new SHT21(i2c);
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
        View view = inflater.inflate(R.layout.sensor_sht21, container, false);

        tvSensorSHT21Temp = (TextView) view.findViewById(R.id.tv_sensor_sht21_temp);
        tvSensorSHT21Humidity = (TextView) view.findViewById(R.id.tv_sensor_sht21_humidity);

        return view;
    }


    private class SensorDataFetch extends AsyncTask<Void, Void, Void> {
        private ArrayList<Double> dataSHT21Temp = new ArrayList<>();
        private ArrayList<Double> dataSHT21Humidity = new ArrayList<>();

        private SensorDataFetch() throws IOException, InterruptedException {
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (sensorSHT21 != null) {
                    sensorSHT21.selectParameter("temperature");
                    dataSHT21Temp = sensorSHT21.getRaw();
                    sensorSHT21.selectParameter("humidity");
                    dataSHT21Humidity = sensorSHT21.getRaw();
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            tvSensorSHT21Temp.setText(String.valueOf(dataSHT21Temp.get(0)));
            tvSensorSHT21Humidity.setText(String.valueOf(dataSHT21Humidity.get(0)));
        }
    }
}