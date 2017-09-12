package org.fossasia.pslab.experimentsetup.schoollevel;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.fossasia.pslab.R;
import org.fossasia.pslab.communication.ScienceLab;
import org.fossasia.pslab.others.ScienceLabCommon;

/**
 * Created by Padmal on 8/13/17.
 */

public class HumanBodyResistanceExperiment extends Fragment {

    private float voltage, current, resistance;
    private ScienceLab scienceLab = ScienceLabCommon.scienceLab;
    private final Object lock = new Object();
    private final float M = 1.0e6f;
    private final float PV3Voltage = 3.0f;

    private TextView tvVoltage, tvCurrent, tvResistance;

    public static HumanBodyResistanceExperiment newInstance() {
        return new HumanBodyResistanceExperiment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.body_resistance_measurement_layout, container, false);
        tvVoltage = (TextView) view.findViewById(R.id.tv_voltage_measurement);
        tvCurrent = (TextView) view.findViewById(R.id.tv_current_measurement);
        tvResistance = (TextView) view.findViewById(R.id.tv_resistance_measurement);
        Button btnConfigure = (Button) view.findViewById(R.id.btn_begin_experiment);
        btnConfigure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (scienceLab.isConnected()) {
                    startExperiment();
                } else {
                    Toast.makeText(getContext(), "Device not connected", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }

    private void startExperiment() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                scienceLab.setPV3(PV3Voltage);
                while (true) {
                    new MeasureResistance().execute();
                    synchronized (lock) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        new Thread(runnable).start();
    }

    private void updateDataBox() {
        tvVoltage.setText(String.valueOf(voltage));
        tvCurrent.setText(String.valueOf(current));
        tvResistance.setText(String.valueOf(resistance));
    }

    private class MeasureResistance extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            voltage = (float) scienceLab.getVoltage("CH3", 100);
            current = voltage / M;
            resistance = (M * (PV3Voltage - voltage)) / voltage;
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    updateDataBox();
                }
            });
            synchronized (lock) {
                lock.notify();
            }
        }
    }
}
