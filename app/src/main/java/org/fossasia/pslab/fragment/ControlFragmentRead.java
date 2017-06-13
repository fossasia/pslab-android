package org.fossasia.pslab.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.fossasia.pslab.R;
import org.fossasia.pslab.communication.ScienceLab;
import org.fossasia.pslab.others.ScienceLabCommon;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by asitava on 6/6/17.
 */

public class ControlFragmentRead extends Fragment {

    private ScienceLab scienceLab;

    @BindView(R.id.tv_control_read1) TextView textControlRead1;
    @BindView(R.id.tv_control_read2) TextView textControlRead2;
    @BindView(R.id.tv_control_read3) TextView textControlRead3;
    @BindView(R.id.tv_control_read4) TextView textControlRead4;
    @BindView(R.id.tv_control_read5) TextView textControlRead5;
    @BindView(R.id.tv_control_read6) TextView textControlRead6;
    @BindView(R.id.tv_control_read7) TextView textControlRead7;
    @BindView(R.id.tv_control_read8) TextView textControlRead8;
    @BindView(R.id.tv_control_read9) TextView textControlRead9;
    @BindView(R.id.tv_control_read10) TextView textControlRead10;
    @BindView(R.id.button_control_read1) Button buttonControlRead1;
    @BindView(R.id.button_control_read2) Button buttonControlRead2;
    @BindView(R.id.button_control_read3) Button buttonControlRead3;
    @BindView(R.id.button_control_read4) Button buttonControlRead4;
    @BindView(R.id.button_control_read5) Button buttonControlRead5;
    @BindView(R.id.button_control_read6) Button buttonControlRead6;

    private Unbinder unbinder;


    public static ControlFragmentRead newInstance() {
        ControlFragmentRead controlFragmentRead = new ControlFragmentRead();
        return controlFragmentRead;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scienceLab = ScienceLabCommon.getInstance().scienceLab;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_control_read, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
