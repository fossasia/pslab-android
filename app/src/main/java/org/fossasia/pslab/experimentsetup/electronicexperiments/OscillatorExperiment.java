package org.fossasia.pslab.experimentsetup.electronicexperiments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.fossasia.pslab.R;
import org.fossasia.pslab.activity.OscilloscopeActivity;

public class OscillatorExperiment extends Fragment {

    private Button startButton;
    private static String experiment;

    public OscillatorExperiment() {

    }

    public static OscillatorExperiment newInstance(String param) {
        experiment = param;
        return new OscillatorExperiment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.rectifier_setup, container, false);
        startButton = v.findViewById(R.id.button_start_experiment_rectifier);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), OscilloscopeActivity.class);
                switch (experiment) {
                    case "Astable Multivibrator":
                        intent.putExtra("who", "Astable Multivibrator");
                        break;
                    case "Colpitts Oscillator":
                        intent.putExtra("who", "Colpitts Oscillator");
                        break;
                    case "Phase Shift Oscillator":
                        intent.putExtra("who", "Phase Shift Oscillator");
                        break;
                    case "Wien Bridge Oscillator":
                        intent.putExtra("who", "Wien Bridge Oscillator");
                        break;
                    case "Monostable Multivibrator":
                        intent.putExtra("who", "Monostable Multivibrator");
                        break;
                    case "Speed of Sound":
                        intent.putExtra("who", "Speed of Sound");
                        break;
                    default:
                        break;
                }
                startActivity(intent);
            }
        });
        return v;
    }
}
