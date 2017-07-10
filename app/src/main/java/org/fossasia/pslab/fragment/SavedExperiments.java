package org.fossasia.pslab.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.Toast;

import org.fossasia.pslab.R;
import org.fossasia.pslab.activity.PerformExperimentActivity;
import org.fossasia.pslab.adapters.SavedExperimentAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by viveksb007 on 15/3/17.
 */

public class SavedExperiments extends Fragment {

    private Unbinder unbinder;
    private Context context;

    @BindView(R.id.saved_experiments_elv)
    ExpandableListView experimentExpandableList;

    private SavedExperimentAdapter experimentAdapter;
    private List<String> experimentGroupHeaders;
    private List<String> experimentDescription;
    private HashMap<String, List<String>> experimentList;

    public static SavedExperiments newInstance(Context context) {
        SavedExperiments savedExperiments = new SavedExperiments();
        savedExperiments.context = context;
        return savedExperiments;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prepareExperimentList();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.saved_experiments_fragment, container, false);
        unbinder = ButterKnife.bind(this, view);
        experimentAdapter = new SavedExperimentAdapter(context, experimentGroupHeaders, experimentList, experimentDescription);
        experimentExpandableList.setAdapter(experimentAdapter);
        experimentExpandableList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Toast.makeText(context, experimentList.get(experimentGroupHeaders.get(groupPosition)).get(childPosition) + " Clicked", Toast.LENGTH_SHORT).show();
                // Open Fragment/Activity to perform corresponding experiment or to see experiment description like Help-Files
                Intent intent = new Intent(context, PerformExperimentActivity.class);
                intent.putExtra("toolbar_title", experimentGroupHeaders.get(groupPosition));
                startActivity(intent);
                return true;
            }
        });
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private void prepareExperimentList() {
        experimentGroupHeaders = new ArrayList<>();
        experimentList = new HashMap<>();
        experimentDescription = new ArrayList<>();

        experimentGroupHeaders.add("Electronics");
        experimentDescription.add("Experiments related to Diodes, BJT, FET, OpAmps, Oscillators, etc.");

        experimentGroupHeaders.add("Electrical");
        experimentDescription.add("Experiments related to Resistance, Capacitance, Inductance, RLC Circuits, etc.");

        experimentGroupHeaders.add("Physics");
        experimentDescription.add("Experiments related to Sound, Pendulum Time Period, Simple Pendulum, etc.");

        experimentGroupHeaders.add("School Level");
        experimentDescription.add("Experiments related to AC/DC, Sound Basics, Water Resistance, etc.");

        experimentGroupHeaders.add("Miscellaneous");
        experimentDescription.add("Experiments related to Sensors like Dust Sensor, Temperature Sensor, etc.");

        experimentGroupHeaders.add("My Experiments");
        experimentDescription.add("My Designed Experiments");

        List<String> electronicsExperiments = new ArrayList<>();
        electronicsExperiments.add("Diode I-V");
        electronicsExperiments.add("Zener I-V");
        electronicsExperiments.add("Diode Clamping");
        electronicsExperiments.add("Half Wave");
        electronicsExperiments.add("Transistor CB");
        electronicsExperiments.add("Transistor CE");
        electronicsExperiments.add("Transistor Amplifier");
        electronicsExperiments.add("Inverting Op-Amp");
        electronicsExperiments.add("Astable Multi-vibrator");
        electronicsExperiments.add("Phase Shift Oscillator");

        List<String> electricalExperiments = new ArrayList<>();
        electricalExperiments.add("Transients RLC");
        electricalExperiments.add("Bode Plots");
        electricalExperiments.add("Ohms Law");
        electricalExperiments.add("Capacitive Phase Shift");
        electricalExperiments.add("Inductive Phase Shift");

        List<String> physicsExperiments = new ArrayList<>();
        physicsExperiments.add("Speed of Sound");
        physicsExperiments.add("Piezo Frequency Response");
        physicsExperiments.add("Sensor Pendulum");
        physicsExperiments.add("M Random Sampling");

        List<String> miscellaneousExperiments = new ArrayList<>();
        miscellaneousExperiments.add("Dust Sensor");
        miscellaneousExperiments.add("Temperature Sensor");
        miscellaneousExperiments.add("Servo Motor");
        miscellaneousExperiments.add("Stepper Motor");
        miscellaneousExperiments.add("RGB LEDs");

        List<String> schoolExperiments = new ArrayList<>();
        schoolExperiments.add("AC and DC");
        schoolExperiments.add("AC Generator");
        schoolExperiments.add("Resistance");
        schoolExperiments.add("EM Induction");
        schoolExperiments.add("Sound Basics");
        schoolExperiments.add("Sound Beats");
        schoolExperiments.add("Capacitance");
        schoolExperiments.add("Light Dependent Resistor");

        List<String> myExperiments = new ArrayList<>();
        myExperiments.add("Experiment 1");
        myExperiments.add("Experiment 2");

        experimentList.put(experimentGroupHeaders.get(0), electronicsExperiments);
        experimentList.put(experimentGroupHeaders.get(1), electricalExperiments);
        experimentList.put(experimentGroupHeaders.get(2), physicsExperiments);
        experimentList.put(experimentGroupHeaders.get(3), schoolExperiments);
        experimentList.put(experimentGroupHeaders.get(4), miscellaneousExperiments);
        experimentList.put(experimentGroupHeaders.get(5), myExperiments);
    }

}
