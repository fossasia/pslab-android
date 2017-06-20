package org.fossasia.pslab.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;

import org.fossasia.pslab.R;
import org.fossasia.pslab.communication.ScienceLab;
import org.fossasia.pslab.others.Edittextwidget;

/**
 * Created by asitava on 6/6/17.
 */

public class ControlFragmentAdvanced extends Fragment{

    private ScienceLab scienceLab;

    public static ControlFragmentAdvanced newInstance() {
        ControlFragmentAdvanced controlFragmentAdvanced = new ControlFragmentAdvanced();
        return controlFragmentAdvanced;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_control_advanced, container, false);

        Button buttonControlAdvanced1 = (Button) view.findViewById(R.id.button_control_advanced1);
        buttonControlAdvanced1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        Button buttonControlAdvanced2 = (Button) view.findViewById(R.id.button_control_advanced2);
        buttonControlAdvanced2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        Edittextwidget etwidgetControlAdvanced1 = (Edittextwidget) view.findViewById(R.id.etwidget_control_advanced1);
        Edittextwidget etwidgetControlAdvanced2 = (Edittextwidget) view.findViewById(R.id.etwidget_control_advanced2);
        Edittextwidget etwidgetControlAdvanced3 = (Edittextwidget) view.findViewById(R.id.etwidget_control_advanced3);
        Edittextwidget etwidgetControlAdvanced4 = (Edittextwidget) view.findViewById(R.id.etwidget_control_advanced4);
        Edittextwidget etwidgetControlAdvanced5 = (Edittextwidget) view.findViewById(R.id.etwidget_control_advanced5);
        Edittextwidget etwidgetControlAdvanced6 = (Edittextwidget) view.findViewById(R.id.etwidget_control_advanced6);
        Edittextwidget etwidgetControlAdvanced7 = (Edittextwidget) view.findViewById(R.id.etwidget_control_advanced7);
        Edittextwidget etwidgetControlAdvanced8 = (Edittextwidget) view.findViewById(R.id.etwidget_control_advanced8);
        Edittextwidget etwidgetControlAdvanced9 = (Edittextwidget) view.findViewById(R.id.etwidget_control_advanced9);
        Edittextwidget etwidgetControlAdvanced10 = (Edittextwidget) view.findViewById(R.id.etwidget_control_advanced10);
        Edittextwidget etwidgetControlAdvanced11 = (Edittextwidget) view.findViewById(R.id.etwidget_control_advanced11);

        Spinner spinnerControlAdvanced1 = (Spinner) view.findViewById(R.id.spinner_control_advanced1);
        Spinner spinnerControlAdvanced2 = (Spinner) view.findViewById(R.id.spinner_control_advanced2);

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

        return view;
    }

}
