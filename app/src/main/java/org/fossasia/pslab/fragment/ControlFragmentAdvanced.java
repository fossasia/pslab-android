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
import org.fossasia.pslab.others.EditTextWidget;
import org.fossasia.pslab.others.ScienceLabCommon;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by asitava on 6/6/17.
 */

public class ControlFragmentAdvanced extends Fragment {

    private ScienceLab scienceLab;
    private Map<String, Integer> state = new HashMap<>();

    public static ControlFragmentAdvanced newInstance() {
        return new ControlFragmentAdvanced();
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

        final EditTextWidget etWidgetControlAdvanced1 = (EditTextWidget) view.findViewById(R.id.etwidget_control_advanced1);
        final EditTextWidget etWidgetControlAdvanced2 = (EditTextWidget) view.findViewById(R.id.etwidget_control_advanced2);
        final EditTextWidget etWidgetControlAdvanced3 = (EditTextWidget) view.findViewById(R.id.etwidget_control_advanced3);
        final EditTextWidget etWidgetControlAdvanced4 = (EditTextWidget) view.findViewById(R.id.etwidget_control_advanced4);
        final EditTextWidget etWidgetControlAdvanced5 = (EditTextWidget) view.findViewById(R.id.etwidget_control_advanced5);
        final EditTextWidget etWidgetControlAdvanced6 = (EditTextWidget) view.findViewById(R.id.etwidget_control_advanced6);
        final EditTextWidget etWidgetControlAdvanced7 = (EditTextWidget) view.findViewById(R.id.etwidget_control_advanced7);
        final EditTextWidget etWidgetControlAdvanced8 = (EditTextWidget) view.findViewById(R.id.etwidget_control_advanced8);
        final EditTextWidget etWidgetControlAdvanced9 = (EditTextWidget) view.findViewById(R.id.etwidget_control_advanced9);
        final EditTextWidget etWidgetControlAdvanced10 = (EditTextWidget) view.findViewById(R.id.etwidget_control_advanced10);
        final EditTextWidget etWidgetControlAdvanced11 = (EditTextWidget) view.findViewById(R.id.etwidget_control_advanced11);

        final Spinner spinnerControlAdvanced1 = (Spinner) view.findViewById(R.id.spinner_control_advanced1);
        final Spinner spinnerControlAdvanced2 = (Spinner) view.findViewById(R.id.spinner_control_advanced2);

        CheckBox checkBoxControlAdvanced1 = (CheckBox) view.findViewById(R.id.checkbox_control_advanced1);
        CheckBox checkBoxControlAdvanced2 = (CheckBox) view.findViewById(R.id.checkbox_control_advanced2);
        CheckBox checkBoxControlAdvanced3 = (CheckBox) view.findViewById(R.id.checkbox_control_advanced3);
        CheckBox checkBoxControlAdvanced4 = (CheckBox) view.findViewById(R.id.checkbox_control_advanced4);

        etWidgetControlAdvanced1.init(getContext(), 1.0, 10.0, 5000.0);
        etWidgetControlAdvanced2.init(getContext(), 1.0, 10.0, 5000.0);
        etWidgetControlAdvanced3.init(getContext(), 1.0, 10.0, 5000.0);
        etWidgetControlAdvanced4.init(getContext(), 0.1, 0.0, 1.0);
        etWidgetControlAdvanced5.init(getContext(), 1.0, 0.0, 360.0);
        etWidgetControlAdvanced6.init(getContext(), 0.1, 0.0, 1.0);
        etWidgetControlAdvanced7.init(getContext(), 1.0, 0.0, 360.0);
        etWidgetControlAdvanced8.init(getContext(), 0.1, 0.0, 1.0);
        etWidgetControlAdvanced9.init(getContext(), 1.0, 0.0, 360.0);
        etWidgetControlAdvanced10.init(getContext(), 0.1, 0.0, 1.0);
        etWidgetControlAdvanced11.init(getContext(), 1.0, 0.0, 360.0);

        buttonControlAdvanced1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Double frequencyW1 = Double.valueOf(etWidgetControlAdvanced1.getText());
                    Double frequencyW2 = Double.valueOf(etWidgetControlAdvanced2.getText());
                    int phase = Integer.valueOf(etWidgetControlAdvanced3.getText());

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
                    etWidgetControlAdvanced1.setText("0");
                    etWidgetControlAdvanced2.setText("0");
                    etWidgetControlAdvanced3.setText("0");
                }
            }
        });

        buttonControlAdvanced2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    double phase2 = Double.valueOf(etWidgetControlAdvanced5.getText());
                    double phase3 = Double.valueOf(etWidgetControlAdvanced7.getText());
                    double phase4 = Double.valueOf(etWidgetControlAdvanced9.getText());

                    double dutyCycle1 = Double.valueOf(etWidgetControlAdvanced4.getText());
                    double dutyCycle2 = Double.valueOf(etWidgetControlAdvanced6.getText());
                    double dutyCycle3 = Double.valueOf(etWidgetControlAdvanced8.getText());
                    double dutyCycle4 = Double.valueOf(etWidgetControlAdvanced10.getText());

                    double frequency = Double.valueOf(etWidgetControlAdvanced11.getText());

                    if (scienceLab.isConnected())
                        scienceLab.sqrPWM(frequency, dutyCycle1, phase2, dutyCycle2, phase3, dutyCycle3,
                                phase4, dutyCycle4, true);
                } catch (NumberFormatException e) {
                    etWidgetControlAdvanced4.setText("0");
                    etWidgetControlAdvanced5.setText("0");
                    etWidgetControlAdvanced6.setText("0");
                    etWidgetControlAdvanced7.setText("0");
                    etWidgetControlAdvanced8.setText("0");
                    etWidgetControlAdvanced9.setText("0");
                    etWidgetControlAdvanced10.setText("0");
                    etWidgetControlAdvanced11.setText("0");
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
