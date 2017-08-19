package org.fossasia.pslab.experimentsetup;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.VideoView;

import org.fossasia.pslab.R;
import org.fossasia.pslab.activity.OscilloscopeActivity;


public class AstableMultivibratorExperiment extends Fragment {

    private Button startButton;

    public static AstableMultivibratorExperiment newInstance() {
        return new AstableMultivibratorExperiment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.rectifier_setup, container, false);
        startButton = (Button) v.findViewById(R.id.button_start_experiment_rectifier);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), OscilloscopeActivity.class);
                intent.putExtra("who", "Astable Multivibrator");
                startActivity(intent);
            }
        });
        return v;
    }
}
