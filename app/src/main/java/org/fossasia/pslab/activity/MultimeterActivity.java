package org.fossasia.pslab.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import org.fossasia.pslab.R;
import org.fossasia.pslab.communication.ScienceLab;
import org.fossasia.pslab.others.ScienceLabCommon;

import java.text.DecimalFormat;

import it.beppi.knoblibrary.Knob;

public class MultimeterActivity extends AppCompatActivity {

    private ScienceLab scienceLab;

    private TextView tvControlRead1;
    private TextView tvControlRead2;
    private TextView tvControlRead3;
    private TextView tvControlRead4;
    private TextView tvControlRead5;
    private TextView tvControlRead6;
    private TextView tvControlRead7;
    private TextView tvControlRead8;
    private TextView tvControlRead9;
    private TextView tvControlRead10;
    private Spinner spinnerControlRead1;
    private Spinner spinnerControlRead2;
    private Knob knob;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_control_read);
        scienceLab = ScienceLabCommon.scienceLab;

        tvControlRead1 = (TextView) findViewById(R.id.tv_control_read1);
        tvControlRead2 = (TextView) findViewById(R.id.tv_control_read2);
        tvControlRead3 = (TextView) findViewById(R.id.tv_control_read3);
        tvControlRead4 = (TextView) findViewById(R.id.tv_control_read4);
        tvControlRead5 = (TextView) findViewById(R.id.tv_control_read5);
        tvControlRead6 = (TextView) findViewById(R.id.tv_control_read6);
        tvControlRead7 = (TextView) findViewById(R.id.tv_control_read7);
        tvControlRead8 = (TextView) findViewById(R.id.tv_control_read8);
        tvControlRead9 = (TextView) findViewById(R.id.tv_control_read9);
        tvControlRead10 = (TextView) findViewById(R.id.tv_control_read10);
        Button buttonControlRead1 = (Button) findViewById(R.id.button_control_read1);
        Button buttonControlRead2 = (Button) findViewById(R.id.button_control_read2);
        Button buttonControlRead3 = (Button) findViewById(R.id.button_control_read3);
        Button buttonControlRead4 = (Button) findViewById(R.id.button_control_read4);
        Button buttonControlRead5 = (Button) findViewById(R.id.button_control_read5);
        Button buttonControlRead6 = (Button) findViewById(R.id.button_control_read6);
        spinnerControlRead1 = (Spinner) findViewById(R.id.spinner_control_read1);
        spinnerControlRead2 = (Spinner) findViewById(R.id.spinner_control_read2);

        buttonControlRead1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (scienceLab.isConnected()) {
                    DecimalFormat resistanceFormat = new DecimalFormat("#.##");
                    Double resistance = scienceLab.getResistance();
                    String Resistance = "";
                    if (resistance == null) {
                        Resistance = "Infinity";
                    } else {
                        if (resistance > 10e5) {
                            Resistance = resistanceFormat.format((resistance / 10e5)) + " MOhms";
                        } else if (resistance > 10e2) {
                            Resistance = resistanceFormat.format((resistance / 10e2)) + " kOhms";
                        } else if (resistance > 1) {
                            Resistance = resistanceFormat.format(resistance) + " Ohms";
                        } else {
                            Resistance = "Cannot measure!";
                        }
                    }
                    tvControlRead1.setText(Resistance);
                }
            }
        });
        buttonControlRead2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (scienceLab.isConnected()) {
                    Double capacitance = scienceLab.getCapacitance();
                    tvControlRead2.setText(String.valueOf(capacitance));
                }
            }
        });
        buttonControlRead3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String channel = spinnerControlRead1.getSelectedItem().toString();
                if (scienceLab.isConnected()) {
                    Double frequency = scienceLab.getFrequency(channel, null);
                    tvControlRead3.setText(String.valueOf(frequency));
                }
            }
        });
        buttonControlRead4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String channel = spinnerControlRead2.getSelectedItem().toString();
                if (scienceLab.isConnected()) {
                    scienceLab.countPulses(channel);
                    double pulseCount = scienceLab.readPulseCount();
                    tvControlRead4.setText(String.valueOf(pulseCount));
                }
            }
        });
        buttonControlRead5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvControlRead1.setText("");
                tvControlRead2.setText("");
                tvControlRead3.setText("");
                tvControlRead4.setText("");
            }
        });
        buttonControlRead6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (scienceLab.isConnected()) {
                    tvControlRead5.setText(String.valueOf(scienceLab.getVoltage("CH1", 1)));
                    tvControlRead6.setText(String.valueOf(scienceLab.getVoltage("CAP", 1)));
                    tvControlRead7.setText(String.valueOf(scienceLab.getVoltage("CH2", 1)));
                    tvControlRead8.setText(String.valueOf(scienceLab.getVoltage("SEN", 1)));
                    tvControlRead9.setText(String.valueOf(scienceLab.getVoltage("CH3", 1)));
                    tvControlRead10.setText(String.valueOf(scienceLab.getVoltage("AN8", 1)));
                }

            }
        });
    }
}
