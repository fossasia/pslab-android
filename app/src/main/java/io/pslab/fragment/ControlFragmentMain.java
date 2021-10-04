package io.pslab.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.pslab.R;
import io.pslab.adapters.ControlMainAdapter;


public class ControlFragmentMain extends Fragment {

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
        RecyclerView mRecyclerView = view.findViewById(R.id.control_main_recycler_view);
        mRecyclerView.setHasFixedSize(false);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        return view;

    }
}
