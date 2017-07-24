package org.fossasia.pslab.sensorfragment;

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
import org.fossasia.pslab.communication.sensors.BH1750;
import org.fossasia.pslab.others.ScienceLabCommon;

import java.io.IOException;

/**
 * Created by asitava on 10/7/17.
 */

public class SensorFragmentBH1750 extends Fragment {
    private ScienceLab scienceLab;
    private I2C i2c;
    private SensorDataFetch sensorDataFetch;
    private TextView tvSensorBH1750Luminosity;
    private BH1750 sensorBH1750;

    public static SensorFragmentBH1750 newInstance() {
        SensorFragmentBH1750 sensorFragmentBH1750 = new SensorFragmentBH1750();
        return sensorFragmentBH1750;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scienceLab = ScienceLabCommon.scienceLab;
        i2c = scienceLab.i2c;
        try {
            sensorBH1750 = new BH1750(i2c);
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
        View view = inflater.inflate(R.layout.sensor_bh1750, container, false);
        tvSensorBH1750Luminosity = (TextView) view.findViewById(R.id.tv_sensor_bh1750_luminosity);
        Spinner spinnerSensorBH1750 = (Spinner) view.findViewById(R.id.spinner_sensor_bh1750);
        try {
            if (sensorBH1750 != null) {
                sensorBH1750.setRange(spinnerSensorBH1750.getSelectedItem().toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return view;
    }


    private class SensorDataFetch extends AsyncTask<Void, Void, Void> {
        Double dataBH1750;

        private SensorDataFetch() throws IOException, InterruptedException {
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (sensorBH1750 != null) {
                    dataBH1750 = sensorBH1750.getRaw();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            tvSensorBH1750Luminosity.setText(String.valueOf(dataBH1750));
        }
    }
}