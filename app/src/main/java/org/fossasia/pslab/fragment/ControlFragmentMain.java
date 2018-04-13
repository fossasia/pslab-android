package org.fossasia.pslab.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.fossasia.pslab.PSLabApplication;
import org.fossasia.pslab.R;
import org.fossasia.pslab.adapters.ControlMainAdapter;

/**
 * Created by asitava on 6/6/17.
 */

public class ControlFragmentMain extends Fragment{

    private ControlMainAdapter mAdapter;

    public static ControlFragmentMain newInstance() {
        return new ControlFragmentMain();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new ControlMainAdapter(new String[]{"PV1", "PV2", "PV3", "PCS", "WAVE 1" , "WAVE 2" , "SQUARE"});
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_control_main, container, false);
        RecyclerView mRecyclerView = (RecyclerView) view.findViewById(R.id.control_main_recycler_view);
        mRecyclerView.setHasFixedSize(false);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        return view;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        ((PSLabApplication)getActivity().getApplication()).refWatcher.watch(this, ControlFragmentMain.class.getSimpleName());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
