package org.fossasia.pslab.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.fossasia.pslab.R;

/**
 * Created by viveksb007 on 12/7/17.
 */

public class ExperimentSetupFragment extends Fragment {

    public static ExperimentSetupFragment newInstance() {
        ExperimentSetupFragment experimentSetupFragment = new ExperimentSetupFragment();
        return experimentSetupFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.experiment_setup, container, false);
        return view;
    }

}
