package com.viveksb007.pslab.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.viveksb007.pslab.R;

/**
 * Created by viveksb007 on 29/3/17.
 */

public class ApplicationsFragment extends Fragment {

    public static ApplicationsFragment newInstance() {
        ApplicationsFragment applicationsFragment = new ApplicationsFragment();
        return applicationsFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.applications_fragment, container, false);
        return view;
    }
}
