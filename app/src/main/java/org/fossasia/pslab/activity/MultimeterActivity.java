package org.fossasia.pslab.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import org.fossasia.pslab.R;
import org.fossasia.pslab.communication.ScienceLab;
import org.fossasia.pslab.others.ScienceLabCommon;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.beppi.knoblibrary.Knob;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

/**
 * Created by Abhinav Raj on 26/5/18.
 */

public class MultimeterActivity extends AppCompatActivity {

    private ScienceLab scienceLab;

    @BindView(R.id.quantity)
    TextView quantity;
    @BindView(R.id.unit)
    TextView unit;
    @BindView(R.id.knob_center)
    TextView knobSelection;
    @BindView(R.id.knobs)
    Knob knob;
    @BindView(R.id.reset)
    Button reset;
    @BindView(R.id.read)
    Button read;
    @BindView(R.id.capacitance)
    Button readCapacitance;
    @BindView(R.id.resistance)
    Button readResistance;

    private int knobState;
    private String[] knobMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multimeter);
        ButterKnife.bind(this);
        scienceLab = ScienceLabCommon.scienceLab;
        knobMarker = getResources().getStringArray(org.fossasia.pslab.R.array.multimeter_knob_states);

        SharedPreferences pref1 = android.preference.PreferenceManager.getDefaultSharedPreferences(this);
        boolean toShow = pref1.getBoolean("MultiRun",true);
        if(toShow) {
            ShowcaseConfig config = new ShowcaseConfig();
            config.setDelay(500);

            MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this);
            sequence.setConfig(config);

            sequence.addSequenceItem(readResistance, "This button will make your multimeter read resistance.", "Got It");
            sequence.addSequenceItem(readCapacitance, "This button will make your multimeter read capacitance", "Got It");
            sequence.addSequenceItem(read, "On clicking this button multimeter will start reading values.", "Got It");
            sequence.addSequenceItem(reset, "This button will reset your multimeter.", "Got It");

            sequence.start();

            SharedPreferences.Editor editor = pref1.edit();
            editor.putBoolean("MultiRun",false);
            editor.apply();
        }

        readResistance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (scienceLab.isConnected()) {
                    DecimalFormat resistanceFormat = new DecimalFormat("#.##");
                    Double resistance;
                    Double avgResistance = 0.0;
                    int loops = 20;
                    for (int i = 0; i < loops; i++) {
                        resistance = scienceLab.getResistance();
                        if (resistance == null) {
                            avgResistance = null;
                            break;
                        } else {
                            avgResistance = avgResistance + resistance / loops;
                        }
                    }
                    String resistanceUnit;
                    String Resistance = "";
                    if (avgResistance == null) {
                        Resistance = "Infinity";
                        resistanceUnit = "Ohms";
                    } else {
                        if (avgResistance > 10e5) {
                            Resistance = resistanceFormat.format((avgResistance / 10e5));
                            resistanceUnit = "MOhms";
                        } else if (avgResistance > 10e2) {
                            Resistance = resistanceFormat.format((avgResistance / 10e2));
                            resistanceUnit = "kOhms";
                        } else if (avgResistance > 1) {
                            Resistance = resistanceFormat.format(avgResistance);
                            resistanceUnit = "Ohms";
                        } else {
                            Resistance = "Cannot measure!";
                            resistanceUnit = "";
                        }
                    }
                    quantity.setText(Resistance);
                    unit.setText(resistanceUnit);
                }
            }
        });
        readCapacitance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (scienceLab.isConnected()) {
                    Double capacitance = scienceLab.getCapacitance();
                    quantity.setText(String.valueOf(capacitance));
                    unit.setText(R.string.capacitance_unit);
                }
            }
        });
        knob.setOnStateChanged(new Knob.OnStateChanged() {
            @Override
            public void onState(int state) {
                knobState = state;
                knobSelection.setText(knobMarker[knobState]);
            }
        });
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quantity.setText("");
                unit.setText("");
            }
        });
        read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (knobState < 4) {
                    if (scienceLab.isConnected()) {
                        scienceLab.countPulses(knobMarker[knobState]);
                        double pulseCount = scienceLab.readPulseCount();
                        quantity.setText(String.valueOf(pulseCount));
                        unit.setText("");
                    }
                } else if (knobState < 8) {
                    if (scienceLab.isConnected()) {
                        Double frequency = scienceLab.getFrequency(knobMarker[knobState], null);
                        quantity.setText(String.valueOf(frequency));
                        unit.setText(R.string.frequency_unit);
                    }
                } else {
                    if (scienceLab.isConnected()) {
                        quantity.setText(String.valueOf(scienceLab.getVoltage(knobMarker[knobState], 1)));
                        unit.setText(R.string.multimeter_voltage_unit);
                    }
                }
            }
        });

    }
}
