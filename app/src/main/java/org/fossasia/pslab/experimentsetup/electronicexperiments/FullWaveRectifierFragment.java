package org.fossasia.pslab.experimentsetup.electronicexperiments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.fossasia.pslab.R;
import org.fossasia.pslab.activity.OscilloscopeActivity;

public class FullWaveRectifierFragment extends Fragment {

    public FullWaveRectifierFragment() {
    }

    public static FullWaveRectifierFragment newInstance() {
        return new FullWaveRectifierFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_full_wave_rectifier, container, false);
        ((OscilloscopeActivity) getActivity()).setLeftYAxisScale(4, -4);
        ((OscilloscopeActivity) getActivity()).setRightYAxisScale(4, -4);
        return v;
    }

}
