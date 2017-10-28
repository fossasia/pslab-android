package org.fossasia.pslab.experimentsetup.schoollevel;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.fossasia.pslab.R;
import org.fossasia.pslab.communication.ScienceLab;
import org.fossasia.pslab.others.ScienceLabCommon;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Padmal on 8/16/17.
 */

public class ACGeneratorExperiment extends Fragment {

    private LineChart outputChart;
    private ScienceLab scienceLab = ScienceLabCommon.scienceLab;
    private final Object lock = new Object();
    private Spinner ampSpinner;

    public static ACGeneratorExperiment newInstance() {
        return new ACGeneratorExperiment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.ac_generator_experiment_layout, container, false);
        outputChart = (LineChart) view.findViewById(R.id.line_chart);
        ampSpinner = (Spinner) view.findViewById(R.id.amp_range_spinner);
        chartInit();
        setupRangeSpinner();
        Button btnConfigure = (Button) view.findViewById(R.id.btn_configure_dialog);
        btnConfigure.setText(getString(R.string.start_experiment));
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

    private void setupRangeSpinner() {
        final String[] ranges = {"+/-16V", "+/-8V", "+/-4V", "+/-3V", "+/-2V", "+/-1.5V", "+/-1V", "+/-500mV"};
        ArrayAdapter<String> rangesAdapter = new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_spinner_item, ranges);
        rangesAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        ampSpinner.setAdapter(rangesAdapter);
        ampSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        setAxisScale(16, -16);
                        break;
                    case 1:
                        setAxisScale(8, -8);
                        break;
                    case 2:
                        setAxisScale(4, -4);
                        break;
                    case 3:
                        setAxisScale(3, -3);
                        break;
                    case 4:
                        setAxisScale(2, -2);
                        break;
                    case 5:
                        setAxisScale(1.5, -1.5);
                        break;
                    case 6:
                        setAxisScale(1, -1);
                        break;
                    case 7:
                        setAxisScale(0.5, -0.5);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                /**/
            }
        });
    }

    public void setAxisScale(double upperLimit, double lowerLimit) {
        outputChart.getAxisLeft().setAxisMaximum((float) upperLimit);
        outputChart.getAxisLeft().setAxisMinimum((float) lowerLimit);
        outputChart.fitScreen();
        outputChart.invalidate();
    }

    private void chartInit() {
        outputChart.setTouchEnabled(true);
        outputChart.setDragEnabled(true);
        outputChart.setScaleEnabled(true);
        outputChart.setPinchZoom(true);
        outputChart.getAxisLeft().setTextColor(Color.WHITE);
        outputChart.getAxisRight().setTextColor(Color.WHITE);
        outputChart.getXAxis().setTextColor(Color.WHITE);
        outputChart.getLegend().setTextColor(Color.WHITE);
        LineData data = new LineData();
        outputChart.setData(data);
    }

    private void startExperiment() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    new RecordWave().execute();
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

    public class RecordWave extends AsyncTask<String, Void, Void> {

        ArrayList<Entry> entries;

        @Override
        protected Void doInBackground(String... params) {
            try {
                scienceLab.configureTrigger(0, "CH1", 0, null, null);
                scienceLab.captureTraces(1, 1000, 10, "CH1", true, null);

                Thread.sleep((long) (1000 * 10 * 1e-3));
                HashMap<String, double[]> data = scienceLab.fetchTrace(1);

                double[] xData = data.get("x");
                double[] yData = data.get("y");
                entries = new ArrayList<>();
                for (int i = 0; i < xData.length; i++) {
                    entries.add(new Entry((float) xData[i] / 1000, (float) yData[i]));
                }
            } catch (NullPointerException e) {
                cancel(true);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            LineDataSet dataset = new LineDataSet(entries, "CH1");
            LineData lineData = new LineData(dataset);
            dataset.setDrawCircles(false);
            outputChart.setData(lineData);
            outputChart.notifyDataSetChanged();
            outputChart.invalidate();
            synchronized (lock) {
                lock.notify();
            }
        }
    }
}
