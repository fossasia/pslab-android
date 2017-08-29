package org.fossasia.pslab.experimentsetup.schoollevel;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.fossasia.pslab.R;
import org.fossasia.pslab.communication.ScienceLab;
import org.fossasia.pslab.others.ScienceLabCommon;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static org.fossasia.pslab.others.MathUtils.map;

/**
 * Created by viveksb007 on 20/7/17.
 */

public class OhmsLawSetupExperiment extends Fragment {

    private TextView tvCurrentValue;
    private TextView tvVoltageValue;
    private Spinner channelSelectSpinner;
    private SeekBar seekBar;
    private String[] channels = {"CH1", "CH2", "CH3"};
    private double currentValue = 0;
    private double voltageValue = 0;
    private ScienceLab scienceLab = ScienceLabCommon.scienceLab;
    private String selectedChannel;
    private ArrayList<Float> x = new ArrayList<>();
    private ArrayList<Float> y = new ArrayList<>();
    private LineChart outputChart;
    private DecimalFormat df = new DecimalFormat("0.0000");

    public static OhmsLawSetupExperiment newInstance() {
        return new OhmsLawSetupExperiment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.ohms_law_setup, container, false);
        outputChart = (LineChart) view.findViewById(R.id.ohm_chart);
        tvCurrentValue = (TextView) view.findViewById(R.id.tv_current_value);
        tvCurrentValue.setText("0");
        tvVoltageValue = (TextView) view.findViewById(R.id.tv_voltage_value);
        seekBar = (SeekBar) view.findViewById(R.id.current_seekbar);
        Button btnReadVoltage = (Button) view.findViewById(R.id.btn_read_voltage);
        channelSelectSpinner = (Spinner) view.findViewById(R.id.channel_select_spinner);
        channelSelectSpinner.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, channels));
        seekBar.setMax(330);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                double value = map(progress, 0, 330, 0, 3.3);
                tvCurrentValue.setText(df.format(value));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //
            }
        });

        btnReadVoltage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedChannel = channelSelectSpinner.getSelectedItem().toString();
                currentValue = Double.parseDouble(tvCurrentValue.getText().toString());
                if (scienceLab.isConnected()) {
                    CalcDataPoint calcDataPoint = new CalcDataPoint();
                    calcDataPoint.execute();
                } else {
                    Toast.makeText(getContext(), "Device not connected", Toast.LENGTH_SHORT).show();
                }
            }
        });
        chartInit();
        return view;
    }

    private void chartInit() {
        outputChart.setTouchEnabled(true);
        outputChart.setDragEnabled(true);
        outputChart.setScaleEnabled(true);
        outputChart.setPinchZoom(true);
        outputChart.getDescription().setEnabled(false);
        outputChart.getAxisLeft().setTextColor(Color.WHITE);
        outputChart.getAxisRight().setTextColor(Color.WHITE);
        outputChart.getXAxis().setTextColor(Color.WHITE);
        outputChart.getLegend().setTextColor(Color.WHITE);
        LineData data = new LineData();
        outputChart.setData(data);
    }

    private void updateGraph() {
        tvVoltageValue.setText(df.format(voltageValue));
        List<ILineDataSet> dataSets = new ArrayList<>();
        List<Entry> temp = new ArrayList<>();
        for (int i = 0; i < x.size(); i++) {
            temp.add(new Entry(x.get(i), y.get(i)));
        }
        LineDataSet dataSet = new LineDataSet(temp, "I-V Characteristic");
        dataSet.setColor(Color.RED);
        dataSet.setDrawValues(false);
        //dataSet.setDrawCircles(false);
        dataSets.add(dataSet);
        outputChart.setData(new LineData(dataSets));
        outputChart.invalidate();
    }

    private class CalcDataPoint extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            scienceLab.setPCS((float) currentValue);
            switch (selectedChannel) {
                case "CH1":
                    voltageValue = scienceLab.getVoltage("CH1", 5);
                    break;
                case "CH2":
                    voltageValue = scienceLab.getVoltage("CH2", 5);
                    break;
                case "CH3":
                    voltageValue = scienceLab.getVoltage("CH3", 5);
                    break;
                default:
                    voltageValue = scienceLab.getVoltage("CH1", 5);
            }
            x.add((float) currentValue);
            y.add((float) voltageValue);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            updateGraph();
        }
    }

}
