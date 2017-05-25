package org.fossasia.pslab.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.fossasia.pslab.R;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by viveksb007 on 15/3/17.
 */

public class DesignExperiments extends Fragment {

    private Unbinder unbinder;

    public static DesignExperiments newInstance() {
        DesignExperiments designExperiments = new DesignExperiments();
        return designExperiments;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.design_experiments_fragment, container, false);
        unbinder = ButterKnife.bind(this,view);
        return view;
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
