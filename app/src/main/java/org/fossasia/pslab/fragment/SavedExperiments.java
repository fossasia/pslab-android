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
        experimentAdapter = new SavedExperimentAdapter(context, experimentGroupHeaders, experimentList);
        experimentExpandableList.setAdapter(experimentAdapter);
        experimentExpandableList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Toast.makeText(context, experimentList.get(experimentGroupHeaders.get(groupPosition)).get(childPosition) + " Clicked", Toast.LENGTH_SHORT).show();
                // Open Fragment/Activity to perform corresponding experiment or to see experiment description like Help-Files
                Intent intent = new Intent(context, PerformExperimentActivity.class);
                intent.putExtra("toolbar_title", experimentGroupHeaders.get(groupPosition));
                intent.putExtra("experiment_title", experimentList.get(experimentGroupHeaders.get(groupPosition)).get(childPosition));
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

        experimentGroupHeaders.add("Electronics");
        experimentGroupHeaders.add("Electrical");
        experimentGroupHeaders.add("Physics");
        experimentGroupHeaders.add("School Level");
        experimentGroupHeaders.add("Miscellaneous");
        experimentGroupHeaders.add("My Experiments");

        List<String> electronicsExperiments = new ArrayList<>();
        electronicsExperiments.add(getResources().getString(R.string.diode_iv));
        electronicsExperiments.add(getResources().getString(R.string.zener_iv));
        electronicsExperiments.add("Diode Clamping");
        electronicsExperiments.add(getResources().getString(R.string.half_wave_rectifier));
        electronicsExperiments.add(getResources().getString(R.string.full_wave_rectifier));
        electronicsExperiments.add(getResources().getString(R.string.transistor_cb));
        electronicsExperiments.add(getResources().getString(R.string.transistor_ce));
        electronicsExperiments.add("Inverting Op-Amp");
        electronicsExperiments.add(getResources().getString(R.string.astable_multivibrator));
        electronicsExperiments.add("Phase Shift Oscillator");

        List<String> electricalExperiments = new ArrayList<>();
        electricalExperiments.add(getResources().getString(R.string.transient_rlc));
        electricalExperiments.add(getResources().getString(R.string.bode_plots));
        electricalExperiments.add(getResources().getString(R.string.ohms_law));
        electricalExperiments.add("Capacitive Phase Shift");
        electricalExperiments.add("Inductive Phase Shift");

        List<String> physicsExperiments = new ArrayList<>();
        physicsExperiments.add("Speed of Sound");
        physicsExperiments.add("Piezo Frequency Response");
        physicsExperiments.add("Sensor Pendulum");
        physicsExperiments.add(getResources().getString(R.string.m_random_sampling));

        List<String> miscellaneousExperiments = new ArrayList<>();
        miscellaneousExperiments.add(getResources().getString(R.string.dust_sensor));
        miscellaneousExperiments.add("Temperature Sensor");
        miscellaneousExperiments.add("Servo Motor");
        miscellaneousExperiments.add("Stepper Motor");
        miscellaneousExperiments.add("RGB LEDs");

        List<String> schoolExperiments = new ArrayList<>();
        schoolExperiments.add(getResources().getString(R.string.ac_and_dc));
        schoolExperiments.add(getResources().getString(R.string.ac_generator));
        schoolExperiments.add(getResources().getString(R.string.resistance));
        schoolExperiments.add(getResources().getString(R.string.em_induction));
        schoolExperiments.add(getResources().getString(R.string.lemon_cell));
        schoolExperiments.add(getResources().getString(R.string.sound_beats));
        schoolExperiments.add(getResources().getString(R.string.capacitance));
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
