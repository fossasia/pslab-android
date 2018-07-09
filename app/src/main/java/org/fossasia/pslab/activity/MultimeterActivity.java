package org.fossasia.pslab.activity;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.fossasia.pslab.R;
import org.fossasia.pslab.communication.ScienceLab;
import org.fossasia.pslab.others.MathUtils;
import org.fossasia.pslab.others.ScienceLabCommon;
import org.fossasia.pslab.others.SwipeGestureDetector;

import java.text.DecimalFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.beppi.knoblibrary.Knob;

/**
 * Created by Abhinav Raj on 26/5/18.
 */

public class MultimeterActivity extends AppCompatActivity {

    private ScienceLab scienceLab;

    @BindView(R.id.knobs)
    Knob knob;
    @BindView(R.id.quantity)
    TextView quantity;
    @BindView(R.id.unit)
    TextView unit;
    @BindView(R.id.description_box)
    TextView descriptionBox;

    //Linear layouts
    @BindView(R.id.id_pins)
    LinearLayout idPins;
    @BindView(R.id.elements_pins)
    LinearLayout voltageElements;
    @BindView(R.id.channel_pins)
    LinearLayout voltageChannels;
    @BindView(R.id.capacitance_units)
    LinearLayout capacitanceUnits;
    @BindView(R.id.resistance_units)
    LinearLayout resistanceUnits;

    private int knobState=0;
    private String[] knobMarker;

    private RadioButton[] id_buttons = new RadioButton[4];
    private String[] id_pins = {"ID1", "ID2", "ID3", "ID4"};

    private RadioButton[] channel_buttons = new RadioButton[3];
    private String[] channel_pins = {"CH1", "CH2", "CH3"};

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multimeter);
        ButterKnife.bind(this);

        knob.setState(knobState);
        changeCardViewLayout(knobState);

        knob.setOnStateChanged(new Knob.OnStateChanged() {
            @Override
            public void onState(int i) {
                knobState = i;
                changeCardViewLayout(knobState);
            }
        });
    }

    private void changeCardViewLayout(int state) {
        switch (state) {
            case 0:
                descriptionBox.setText(R.string.resistance_description);
                showLayout(resistanceUnits);
                break;
            case 1:
                descriptionBox.setText(R.string.voltage_channel_description);
                showLayout(voltageChannels);
                break;
            case 2:
                descriptionBox.setText(R.string.voltage_elements_description);
                showLayout(voltageElements);
                break;
            case 3:
                descriptionBox.setText(R.string.frequency_description);
                showLayout(idPins);
                break;
            case 4:
                descriptionBox.setText(R.string.count_pulse_description);
                showLayout(idPins);
                break;
            case 5:
                descriptionBox.setText(R.string.capacitance_description);
                showLayout(capacitanceUnits);
                break;
            default:
                descriptionBox.setText(R.string.resistance_description);
                showLayout(resistanceUnits);
                break;
        }
    }

    private void showLayout(LinearLayout layout) {
        voltageChannels.setVisibility(View.INVISIBLE);
        voltageElements.setVisibility(View.INVISIBLE);
        resistanceUnits.setVisibility(View.INVISIBLE);
        capacitanceUnits.setVisibility(View.INVISIBLE);
        idPins.setVisibility(View.INVISIBLE);
        layout.setVisibility(View.VISIBLE);
    }
}
