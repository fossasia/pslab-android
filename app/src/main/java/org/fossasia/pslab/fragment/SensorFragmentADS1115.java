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
import org.fossasia.pslab.communication.sensors.ADS1115;
import org.fossasia.pslab.others.ScienceLabCommon;

import java.io.IOException;

/**
 * Created by asitava on 10/7/17.
 */

public class SensorFragmentADS1115 extends Fragment {
    private ScienceLab scienceLab;
    private I2C i2c;
    private SensorDataFetch sensorDataFetch;
    private TextView tvSensorADS1115;

    private ADS1115 ADS1115;

    public static SensorFragmentADS1115 newInstance() {
        SensorFragmentADS1115 sensorFragmentADS1115 = new SensorFragmentADS1115();
        return sensorFragmentADS1115;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scienceLab = ScienceLabCommon.scienceLab;
        i2c = scienceLab.i2c;
        try {
            ADS1115 = new ADS1115(i2c);
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
        View view = inflater.inflate(R.layout.sensor_ads1115, container, false);

        tvSensorADS1115 = (TextView) view.findViewById(R.id.tv_sensor_ads1115);

        Spinner spinnerSensorADS1115Gain = (Spinner) view.findViewById(R.id.spinner_sensor_ads1115_gain);
        Spinner spinnerSensorADS1115Channel = (Spinner) view.findViewById(R.id.spinner_sensor_ads1115_channel);
        Spinner spinnerSensorADS1115Rate = (Spinner) view.findViewById(R.id.spinner_sensor_ads1115_rate);

        if (ADS1115 != null) {
            ADS1115.setGain(spinnerSensorADS1115Gain.getSelectedItem().toString());
        }

        if (ADS1115 != null) {
            ADS1115.setChannel(spinnerSensorADS1115Channel.getSelectedItem().toString());
        }
        if (ADS1115 != null) {
            ADS1115.setDataRate(Integer.parseInt(spinnerSensorADS1115Rate.getSelectedItem().toString()));
        }

        return view;
    }

    private class SensorDataFetch extends AsyncTask<Void, Void, Void> {
        ADS1115 ADS1115 = new ADS1115(i2c);
        private int dataADS1115;

        private SensorDataFetch() throws IOException, InterruptedException {
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (ADS1115 != null) {
                    dataADS1115 = ADS1115.getRaw();
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            tvSensorADS1115.setText(String.valueOf(dataADS1115));
        }
    }
}
