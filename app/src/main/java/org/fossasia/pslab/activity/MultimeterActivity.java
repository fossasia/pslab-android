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

import android.widget.TextView;

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

    //bottomSheet
    @BindView(R.id.bottom_sheet)
    LinearLayout bottomSheet;
    @BindView(R.id.shadow)
    View tvShadow;
    @BindView(R.id.img_arrow)
    ImageView arrowUpDown;
    @BindView(R.id.sheet_slide_text)
    TextView bottomSheetSlideText;
    @BindView(R.id.guide_title)
    TextView bottomSheetGuideTitle;
    @BindView(R.id.custom_dialog_text)
    TextView bottomSheetText;
    @BindView(R.id.custom_dialog_schematic)
    ImageView bottomSheetSchematic;
    @BindView(R.id.custom_dialog_desc)
    TextView bottomSheetDesc;

    private int knobState;
    private String[] knobMarker;

    BottomSheetBehavior bottomSheetBehavior;
    GestureDetector gestureDetector;
    public static final String PREFS_NAME = "customDialogPreference";

    public static final String NAME = "savingData";
    SharedPreferences multimeter_data;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multimeter_main);
        ButterKnife.bind(this);
        scienceLab = ScienceLabCommon.scienceLab;
        knobMarker = getResources().getStringArray(org.fossasia.pslab.R.array.multimeter_knob_states);
        setUpBottomSheet();
        multimeter_data = this.getSharedPreferences(NAME, MODE_PRIVATE);
        knobState = multimeter_data.getInt("KnobState", 0);

        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/digital-7 (italic).ttf");
        quantity.setTypeface(tf);

        String text_quantity = multimeter_data.getString("TextBox", null);
        String text_unit = multimeter_data.getString("TextBoxUnit", null);
        knob.setState(knobState);
        knobSelection.setText(knobMarker[knobState]);
        quantity.setText(text_quantity);
        unit.setText(text_unit);

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
                    saveAndSetData(Resistance, resistanceUnit);
                }
            }
        });
        readCapacitance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (scienceLab.isConnected()) {
                    Double capacitance = scienceLab.getCapacitance();
                    saveAndSetData(String.valueOf(capacitance), getString(R.string.capacitance_unit));
                }
            }
        });
        knob.setOnStateChanged(new Knob.OnStateChanged() {
            @Override
            public void onState(int state) {
                knobState = state;
                saveKnobState(knobState);
                knobSelection.setText(knobMarker[knobState]);
            }
        });
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                multimeter_data.edit().clear().commit();
                knobState = 0;
                knob.setState(knobState);
                knobSelection.setText(knobMarker[knobState]);
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
                        saveAndSetData(String.valueOf(pulseCount), "");
                    }
                } else if (knobState < 8) {
                    if (scienceLab.isConnected()) {
                        Double frequency = scienceLab.getFrequency(knobMarker[knobState], null);
                        saveAndSetData(String.valueOf(frequency), getString(R.string.frequency_unit));
                    }
                } else {
                    if (scienceLab.isConnected()) {
                        saveAndSetData(String.valueOf(String.format(Locale.ENGLISH, "%.2f", scienceLab.getVoltage(knobMarker[knobState], 1))), getString(R.string.multimeter_voltage_unit));
                    }
                }
            }
        });

    }

    private void setUpBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        final SharedPreferences settings = this.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Boolean isFirstTime = settings.getBoolean("MultimeterFirstTime", true);

        bottomSheetGuideTitle.setText(R.string.multimeter_dialog_heading);
        bottomSheetText.setText(R.string.multimeter_dialog_text);
        bottomSheetSchematic.setImageResource(R.drawable.multimeter_circuit);
        bottomSheetDesc.setText(R.string.multimeter_dialog_description);

        if (isFirstTime) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            tvShadow.setAlpha(0.8f);
            arrowUpDown.setRotation(180);
            bottomSheetSlideText.setText(R.string.hide_guide_text);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("MultimeterFirstTime", false);
            editor.apply();
        } else {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            private Handler handler = new Handler();
            private Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }
            };

            @Override
            public void onStateChanged(@NonNull final View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_EXPANDED:
                        handler.removeCallbacks(runnable);
                        bottomSheetSlideText.setText(R.string.hide_guide_text);
                        break;

                    case BottomSheetBehavior.STATE_COLLAPSED:
                        handler.postDelayed(runnable, 2000);
                        break;

                    default:
                        handler.removeCallbacks(runnable);
                        bottomSheetSlideText.setText(R.string.show_guide_text);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                Float value = (float) MathUtils.map((double) slideOffset, 0.0, 1.0, 0.0, 0.8);
                tvShadow.setAlpha(value);
                arrowUpDown.setRotation(slideOffset * 180);
            }
        });
        gestureDetector = new GestureDetector(this, new SwipeGestureDetector(bottomSheetBehavior));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);                 //Gesture detector need this to transfer touch event to the gesture detector.
        return super.onTouchEvent(event);
    }

    private void saveAndSetData(String Quantity, String Unit) {
        SharedPreferences.Editor editor = multimeter_data.edit();
        editor.putString("TextBox", Quantity);
        editor.putString("TextBoxUnit", Unit);
        editor.commit();
        quantity.setText(Quantity);
        unit.setText(Unit);
    }

    private void saveKnobState(int state) {
        SharedPreferences.Editor editor = multimeter_data.edit();
        editor.putInt("KnobState", state);
        editor.commit();
    }
}
