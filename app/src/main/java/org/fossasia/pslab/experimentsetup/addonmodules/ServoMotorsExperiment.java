package org.fossasia.pslab.experimentsetup.addonmodules;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.fossasia.pslab.R;
import org.fossasia.pslab.communication.ScienceLab;
import org.fossasia.pslab.others.ScienceLabCommon;

/**
 * Created by Padmal on 8/24/17.
 */

public class ServoMotorsExperiment extends Fragment {

    private ScienceLab scienceLab = ScienceLabCommon.scienceLab;
    private EditText angle1, angle2, angle3, angle4;

    public static ServoMotorsExperiment newInstance() {
        return new ServoMotorsExperiment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.servo_motor_layout, container, false);
        Button btnSetServos = (Button) view.findViewById(R.id.btn_set_angles);
        btnSetServos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (scienceLab.isConnected()) {
                    setServos();
                } else {
                    Toast.makeText(getContext(), "Device not connected", Toast.LENGTH_SHORT).show();
                }
            }
        });
        Button btnResetServos = (Button) view.findViewById(R.id.btn_reset_angles);
        btnResetServos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (scienceLab.isConnected()) {
                    scienceLab.servo4(0, 0, 0, 0);
                    angle1.setText("0");
                    angle2.setText("0");
                    angle3.setText("0");
                    angle4.setText("0");
                } else {
                    Toast.makeText(getContext(), "Device not connected", Toast.LENGTH_SHORT).show();
                }
            }
        });

        angle1 = (EditText) view.findViewById(R.id.servo_one_angle);
        angle2 = (EditText) view.findViewById(R.id.servo_two_angle);
        angle3 = (EditText) view.findViewById(R.id.servo_three_angle);
        angle4 = (EditText) view.findViewById(R.id.servo_four_angle);

        return view;
    }

    private void setServos() {
        double a1 = 0, a2 = 0, a3 = 0, a4 = 0;
        try {
            a1 = Double.parseDouble(angle1.getText().toString()) % 360;
        } catch (Exception e) {
            a1 = 0;
        } finally {
            angle1.setText(String.valueOf((int) a1));
        }
        try {
            a2 = Double.parseDouble(angle2.getText().toString()) % 360;
        } catch (Exception e) {
            a2 = 0;
        } finally {
            angle2.setText(String.valueOf((int) a2));
        }
        try {
            a3 = Double.parseDouble(angle3.getText().toString()) % 360;
        } catch (Exception e) {
            a3 = 0;
        } finally {
            angle3.setText(String.valueOf((int) a3));
        }
        try {
            a4 = Double.parseDouble(angle4.getText().toString()) % 360;
        } catch (Exception e) {
            a4 = 0;
        } finally {
            angle4.setText(String.valueOf((int) a4));
        }
        scienceLab.servo4(a1, a2, a3, a4);
    }
}
