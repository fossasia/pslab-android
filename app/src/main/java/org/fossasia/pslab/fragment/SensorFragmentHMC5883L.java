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
import org.fossasia.pslab.communication.sensors.HMC5883L;
import org.fossasia.pslab.others.ScienceLabCommon;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by asitava on 10/7/17.
 */

public class SensorFragmentHMC5883L extends Fragment {
    private ScienceLab scienceLab;
    private I2C i2c;
    private SensorFragmentHMC5883L.SensorDataFetch sensorDataFetch;
    private TextView tvSensorHMC5883Lbx;
    private TextView tvSensorHMC5883Lby;
    private TextView tvSensorHMC5883Lbz;
    private HMC5883L HMC5883L;

    public static SensorFragmentHMC5883L newInstance() {
        SensorFragmentHMC5883L sensorFragmentHMC5883L = new SensorFragmentHMC5883L();
        return sensorFragmentHMC5883L;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scienceLab = ScienceLabCommon.scienceLab;
        i2c = scienceLab.i2c;
        try {
            HMC5883L = new HMC5883L(i2c);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (scienceLab.isConnected()) {
                        try {
                            sensorDataFetch = new SensorFragmentHMC5883L.SensorDataFetch();
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
        View view = inflater.inflate(R.layout.sensor_hmc5883l, container, false);

        tvSensorHMC5883Lbx = (TextView) view.findViewById(R.id.tv_sensor_hmc5883l_bx);
        tvSensorHMC5883Lby = (TextView) view.findViewById(R.id.tv_sensor_hmc5883l_by);
        tvSensorHMC5883Lbz = (TextView) view.findViewById(R.id.tv_sensor_hmc5883l_bz);

        return view;
    }


    private class SensorDataFetch extends AsyncTask<Void, Void, Void> {
        HMC5883L HMC5883L = new HMC5883L(i2c);
        ArrayList<Double> dataHMC5883L = new ArrayList<Double>();

        private SensorDataFetch() throws IOException {
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (HMC5883L != null) {
                    dataHMC5883L = HMC5883L.getRaw();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            tvSensorHMC5883Lbx.setText(String.valueOf(dataHMC5883L.get(0)));
            tvSensorHMC5883Lby.setText(String.valueOf(dataHMC5883L.get(1)));
            tvSensorHMC5883Lbz.setText(String.valueOf(dataHMC5883L.get(2)));
        }
    }
}