package org.fossasia.pslab.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.text.TextDirectionHeuristicCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import org.fossasia.pslab.R;
import org.fossasia.pslab.adapters.ControlMainAdapter;
import org.fossasia.pslab.communication.ScienceLab;

/**
 * Created by asitava on 6/6/17.
 */

public class ControlFragmentMain extends Fragment{

    private ScienceLab scienceLab;

    Button buttonControlMain1;
    Button buttonControlMain2;
    Button ButtonControlMain3;
    EditText editTextControlMain1;


    public static ControlFragmentMain newInstance() {
        ControlFragmentMain controlFragmentMain = new ControlFragmentMain();
        return controlFragmentMain;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_control_main, container, false);
        RecyclerView mRecyclerView = (RecyclerView) view.findViewById(R.id.control_main_recycler_view);
        mRecyclerView.setHasFixedSize(false);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        ControlMainAdapter mAdapter = new ControlMainAdapter(new String[]{"PV1", "PV2", "PV3", "PCS", "WAVE 1" , "WAVE 2" , "SQUARE"});
        mRecyclerView.setAdapter(mAdapter);

        return view;

    }

}
