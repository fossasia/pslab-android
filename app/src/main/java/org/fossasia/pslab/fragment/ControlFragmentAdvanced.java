package org.fossasia.pslab.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;

import org.fossasia.pslab.R;
import org.fossasia.pslab.communication.ScienceLab;
import org.fossasia.pslab.others.Edittextwidget;
import org.fossasia.pslab.others.ScienceLabCommon;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by asitava on 6/6/17.
 */

public class ControlFragmentAdvanced extends Fragment {

    private ScienceLab scienceLab;
    Map<String, Integer> state = new HashMap<>();


    public static ControlFragmentAdvanced newInstance() {
        ControlFragmentAdvanced controlFragmentAdvanced = new ControlFragmentAdvanced();
        return controlFragmentAdvanced;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scienceLab = ScienceLabCommon.scienceLab;

        state.put("SQR1", 0);
        state.put("SQR2", 0);
        state.put("SQR3", 0);
        state.put("SQR4", 0);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_control_advanced, container, false);

        Button buttonControlAdvanced1 = (Button) view.findViewById(R.id.button_control_advanced1);
        Button buttonControlAdvanced2 = (Button) view.findViewById(R.id.button_control_advanced2);

        final Edittextwidget etwidgetControlAdvanced1 = (Edittextwidget) view.findViewById(R.id.etwidget_control_advanced1);
        final Edittextwidget etwidgetControlAdvanced2 = (Edittextwidget) view.findViewById(R.id.etwidget_control_advanced2);
        final Edittextwidget etwidgetControlAdvanced3 = (Edittextwidget) view.findViewById(R.id.etwidget_control_advanced3);
        final Edittextwidget etwidgetControlAdvanced4 = (Edittextwidget) view.findViewById(R.id.etwidget_control_advanced4);
        final Edittextwidget etwidgetControlAdvanced5 = (Edittextwidget) view.findViewById(R.id.etwidget_control_advanced5);
        final Edittextwidget etwidgetControlAdvanced6 = (Edittextwidget) view.findViewById(R.id.etwidget_control_advanced6);
        final Edittextwidget etwidgetControlAdvanced7 = (Edittextwidget) view.findViewById(R.id.etwidget_control_advanced7);
        final Edittextwidget etwidgetControlAdvanced8 = (Edittextwidget) view.findViewById(R.id.etwidget_control_advanced8);
        final Edittextwidget etwidgetControlAdvanced9 = (Edittextwidget) view.findViewById(R.id.etwidget_control_advanced9);
        final Edittextwidget etwidgetControlAdvanced10 = (Edittextwidget) view.findViewById(R.id.etwidget_control_advanced10);
        final Edittextwidget etwidgetControlAdvanced11 = (Edittextwidget) view.findViewById(R.id.etwidget_control_advanced11);

        final Spinner spinnerControlAdvanced1 = (Spinner) view.findViewById(R.id.spinner_control_advanced1);
        final Spinner spinnerControlAdvanced2 = (Spinner) view.findViewById(R.id.spinner_control_advanced2);

        CheckBox checkBoxControlAdvanced1 = (CheckBox) view.findViewById(R.id.checkbox_control_advanced1);
        CheckBox checkBoxControlAdvanced2 = (CheckBox) view.findViewById(R.id.checkbox_control_advanced2);
        CheckBox checkBoxControlAdvanced3 = (CheckBox) view.findViewById(R.id.checkbox_control_advanced3);
        CheckBox checkBoxControlAdvanced4 = (CheckBox) view.findViewById(R.id.checkbox_control_advanced4);

        etwidgetControlAdvanced1.init(getContext(), 1.0, 10.0, 5000.0);
        etwidgetControlAdvanced2.init(getContext(), 1.0, 10.0, 5000.0);
        etwidgetControlAdvanced3.init(getContext(), 1.0, 10.0, 5000.0);
        etwidgetControlAdvanced4.init(getContext(), 0.1, 0.0, 1.0);
        etwidgetControlAdvanced5.init(getContext(), 1.0, 0.0, 360.0);
        etwidgetControlAdvanced6.init(getContext(), 0.1, 0.0, 1.0);
        etwidgetControlAdvanced7.init(getContext(), 1.0, 0.0, 360.0);
        etwidgetControlAdvanced8.init(getContext(), 0.1, 0.0, 1.0);
        etwidgetControlAdvanced9.init(getContext(), 1.0, 0.0, 360.0);
        etwidgetControlAdvanced10.init(getContext(), 0.1, 0.0, 1.0);
        etwidgetControlAdvanced11.init(getContext(), 1.0, 0.0, 360.0);

        buttonControlAdvanced1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Double frequencyW1 = Double.valueOf(etwidgetControlAdvanced1.getText());
                    Double frequencyW2 = Double.valueOf(etwidgetControlAdvanced2.getText());
                    int phase = Integer.valueOf(etwidgetControlAdvanced3.getText());

                    String wavetypeW1 = spinnerControlAdvanced1.getSelectedItem().toString();
                    String wavetypeW2 = spinnerControlAdvanced2.getSelectedItem().toString();

                    if ("SINE".equals(wavetypeW1) && scienceLab.isConnected())
                        scienceLab.setSine1(frequencyW1);
                    else if ("SQUARE".equals(wavetypeW1) && scienceLab.isConnected())
                        scienceLab.setSqr1(frequencyW1, -1, false);

                    if ("SINE".equals(wavetypeW2) && scienceLab.isConnected())
                        scienceLab.setSine2(frequencyW2);
                    else if ("SQUARE".equals(wavetypeW2) && scienceLab.isConnected())
                        scienceLab.setSqr2(frequencyW2, -1);
                } catch (NumberFormatException e) {
                    etwidgetControlAdvanced1.setText("0");
                    etwidgetControlAdvanced2.setText("0");
                    etwidgetControlAdvanced3.setText("0");
                }
            }
        });

        buttonControlAdvanced2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    double phase2 = Double.valueOf(etwidgetControlAdvanced5.getText());
                    double phase3 = Double.valueOf(etwidgetControlAdvanced7.getText());
                    double phase4 = Double.valueOf(etwidgetControlAdvanced9.getText());

                    double dutyCycle1 = Double.valueOf(etwidgetControlAdvanced4.getText());
                    double dutyCycle2 = Double.valueOf(etwidgetControlAdvanced6.getText());
                    double dutyCycle3 = Double.valueOf(etwidgetControlAdvanced8.getText());
                    double dutyCycle4 = Double.valueOf(etwidgetControlAdvanced10.getText());

                    double frequency = Double.valueOf(etwidgetControlAdvanced11.getText());

                    if (scienceLab.isConnected())
                        scienceLab.sqrPWM(frequency, dutyCycle1, phase2, dutyCycle2, phase3, dutyCycle3,
                                phase4, dutyCycle4, true);
                } catch (NumberFormatException e) {
                    etwidgetControlAdvanced4.setText("0");
                    etwidgetControlAdvanced5.setText("0");
                    etwidgetControlAdvanced6.setText("0");
                    etwidgetControlAdvanced7.setText("0");
                    etwidgetControlAdvanced8.setText("0");
                    etwidgetControlAdvanced9.setText("0");
                    etwidgetControlAdvanced10.setText("0");
                    etwidgetControlAdvanced11.setText("0");
                }
            }
        });

        checkBoxControlAdvanced1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) state.put("SQR1", 1);
                else state.put("SQR1", 0);
                if (scienceLab.isConnected())
                    scienceLab.setState(state);
            }
        });

        checkBoxControlAdvanced2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) state.put("SQR2", 1);
                else state.put("SQR2", 0);
                if (scienceLab.isConnected())
                    scienceLab.setState(state);
            }
        });

        checkBoxControlAdvanced3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) state.put("SQR3", 1);
                else state.put("SQR3", 0);
                if (scienceLab.isConnected())
                    scienceLab.setState(state);
            }
        });

        checkBoxControlAdvanced4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) state.put("SQR4", 1);
                else state.put("SQR4", 0);
                if (scienceLab.isConnected())
                    scienceLab.setState(state);
            }
        });

        return view;
    }

}
