package org.fossasia.pslab.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.fossasia.pslab.R;

/**
 * Created by asitava on 6/6/17.
 */

public class ControlFragmentAdvanced extends Fragment {

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

        return view;

    }

}
