package org.fossasia.pslab.experimentsetup;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.fossasia.pslab.R;
import org.fossasia.pslab.activity.OscilloscopeActivity;

/**
 * Created by akarshan on 8/7/17.
 */

public class RectifierExperiment extends Fragment {
    Button startButton;
    public static RectifierExperiment newInstance() {
        return new RectifierExperiment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.rectifier_setup, container, false);
        startButton = (Button) view.findViewById(R.id.button_start_experiment_rectifier);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), OscilloscopeActivity.class);
                intent.putExtra("who","Half Rectifier");
                startActivity(intent);
            }
        });
        return view;
    }

}