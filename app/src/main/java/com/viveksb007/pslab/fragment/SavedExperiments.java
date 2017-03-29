package com.viveksb007.pslab.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.viveksb007.pslab.R;

/**
 * Created by viveksb007 on 15/3/17.
 */

public class SavedExperiments extends Fragment {

    public static SavedExperiments newInstance() {
        SavedExperiments savedExperiments = new SavedExperiments();
        return savedExperiments;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.saved_experiments_fragment, container, false);
        return view;
    }
}
