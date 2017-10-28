package org.fossasia.pslab.experimentsetup.addonmodules;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import org.fossasia.pslab.R;
import org.fossasia.pslab.communication.ScienceLab;
import org.fossasia.pslab.others.ScienceLabCommon;

/**
 * Created by Padmal on 8/22/17.
 */

public class StepperMotors extends Fragment {

    private ScienceLab scienceLab = ScienceLabCommon.scienceLab;
    private EditText steps;

    public static StepperMotors newInstance() {
        return new StepperMotors();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.stepper_motor_layout, container, false);
        steps = (EditText) view.findViewById(R.id.step_count);
        Button btnSteps = (Button) view.findViewById(R.id.btn_set_steps);
        btnSteps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (scienceLab.isConnected()) {
                    setSteps();
                } else {
                    Toast.makeText(getContext(), "Device not connected", Toast.LENGTH_SHORT).show();
                }
            }
        });
        ImageButton btnStepForward = (ImageButton) view.findViewById(R.id.btn_step_forward);
        btnStepForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (scienceLab.isConnected()) {
                    stepForward(1);
                } else {
                    Toast.makeText(getContext(), "Device not connected", Toast.LENGTH_SHORT).show();
                }
            }
        });
        ImageButton btnStepBackwards = (ImageButton) view.findViewById(R.id.btn_step_backward);
        btnStepBackwards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (scienceLab.isConnected()) {
                    stepBackward(1);
                } else {
                    Toast.makeText(getContext(), "Device not connected", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }

    private void setSteps() {
        int stepCount = Integer.parseInt(steps.getText().toString());
        if (stepCount > 0) {
            stepForward(stepCount);
        } else {
            stepBackward(stepCount);
        }
    }

    private void stepForward(final int steps) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                scienceLab.stepForward(steps, 100);
            }
        };
        new Thread(runnable).start();
    }

    private void stepBackward(final int steps) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                scienceLab.stepBackward(steps, 100);
            }
        };
        new Thread(runnable).start();
    }
}
