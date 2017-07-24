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
import org.fossasia.pslab.communication.sensors.MLX90614;
import org.fossasia.pslab.others.ScienceLabCommon;

import java.io.IOException;

/**
 * Created by asitava on 10/7/17.
 */

public class SensorFragmentMLX90614 extends Fragment {
    private ScienceLab scienceLab;
    private I2C i2c;
    private SensorFragmentMLX90614.SensorDataFetch sensorDataFetch;
    private TextView tvSensorMLX90614ObjectTemp;
    private TextView tvSensorMLX90614AmbientTemp;
    private MLX90614 sensorMLX90614;

    public static SensorFragmentMLX90614 newInstance() {
        SensorFragmentMLX90614 sensorFragmentMLX90614 = new SensorFragmentMLX90614();
        return sensorFragmentMLX90614;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scienceLab = ScienceLabCommon.scienceLab;
        i2c = scienceLab.i2c;
        try {
            sensorMLX90614 = new MLX90614(i2c);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (scienceLab.isConnected()) {
                        try {
                            sensorDataFetch = new SensorFragmentMLX90614.SensorDataFetch();
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
        View view = inflater.inflate(R.layout.sensor_mlx90614, container, false);

        tvSensorMLX90614ObjectTemp = (TextView) view.findViewById(R.id.tv_sensor_mlx90614_object_temp);
        tvSensorMLX90614AmbientTemp = (TextView) view.findViewById(R.id.tv_sensor_mlx90614_ambient_temp);

        return view;
    }


    private class SensorDataFetch extends AsyncTask<Void, Void, Void> {
        Double dataMLX90614ObjectTemp;
        Double dataMLX90614AmbientTemp;

        private SensorDataFetch() throws IOException {
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (sensorMLX90614 != null) {
                    dataMLX90614ObjectTemp = sensorMLX90614.getObjectTemperature();
                    dataMLX90614AmbientTemp = sensorMLX90614.getAmbientTemperature();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            tvSensorMLX90614ObjectTemp.setText(String.valueOf(dataMLX90614ObjectTemp));
            tvSensorMLX90614AmbientTemp.setText(String.valueOf(dataMLX90614AmbientTemp));
        }
    }
}