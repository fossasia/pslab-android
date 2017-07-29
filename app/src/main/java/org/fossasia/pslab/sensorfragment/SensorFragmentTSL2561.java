package org.fossasia.pslab.sensorfragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.fossasia.pslab.R;
import org.fossasia.pslab.communication.ScienceLab;
import org.fossasia.pslab.communication.peripherals.I2C;
import org.fossasia.pslab.communication.sensors.TSL2561;
import org.fossasia.pslab.others.ScienceLabCommon;

import java.io.IOException;

/**
 * Created by asitava on 10/7/17.
 */

public class SensorFragmentTSL2561 extends Fragment {
    private ScienceLab scienceLab;
    private I2C i2c;
    private SensorDataFetch sensorDataFetch;
    private TextView tvSensorTSL2561FullSpectrum;
    private TextView tvSensorTSL2561Infrared;
    private TextView tvSensorTSL2561Visible;
    private Spinner spinnerSensorTSL2561Gain;
    private EditText etSensorTSL2561Timing;
    private TSL2561 sensorTSL2561;

    public static SensorFragmentTSL2561 newInstance() {
        SensorFragmentTSL2561 sensorFragmentTSL2561 = new SensorFragmentTSL2561();
        return sensorFragmentTSL2561;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scienceLab = ScienceLabCommon.scienceLab;
        i2c = scienceLab.i2c;
        try {
            sensorTSL2561 = new TSL2561(i2c);
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
        View view = inflater.inflate(R.layout.sensor_tsl2561, container, false);
        tvSensorTSL2561FullSpectrum = (TextView) view.findViewById(R.id.tv_sensor_tsl2561_full);
        tvSensorTSL2561Infrared = (TextView) view.findViewById(R.id.tv_sensor_tsl2561_infrared);
        tvSensorTSL2561Visible = (TextView) view.findViewById(R.id.tv_sensor_tsl2561_visible);
        spinnerSensorTSL2561Gain = (Spinner) view.findViewById(R.id.spinner_sensor_tsl2561_gain);
        etSensorTSL2561Timing = (EditText) view.findViewById(R.id.et_sensor_tsl2561_timing);

        try {
            if (sensorTSL2561 != null) {
                sensorTSL2561.setGain(spinnerSensorTSL2561Gain.getSelectedItem().toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return view;
    }


    private class SensorDataFetch extends AsyncTask<Void, Void, Void> {
        TSL2561 sensorTSL2561 = new TSL2561(i2c);
        private int[] dataTSL2561;

        private SensorDataFetch() throws IOException, InterruptedException {
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (sensorTSL2561 != null) {
                    dataTSL2561 = sensorTSL2561.getRaw();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            tvSensorTSL2561FullSpectrum.setText(String.valueOf(dataTSL2561[0]));
            tvSensorTSL2561Infrared.setText(String.valueOf(dataTSL2561[0]));
            tvSensorTSL2561Visible.setText(String.valueOf(dataTSL2561[0]));
        }
    }
}