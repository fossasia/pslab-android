package org.fossasia.pslab.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.fossasia.pslab.R;

public class DataAnalysisFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private Spinner spinnerCurveFit;
    private Spinner spinnerChannelSelect1;
    private Spinner spinnerChannelSelect2;

    public static DataAnalysisFragment newInstance(String param1, String param2) {
        DataAnalysisFragment fragment = new DataAnalysisFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_data_analysis, container, false);
        String [] curvefits = {"Sine Fit", "Square Fit"};
        String[] channels ={"None", "CH1", "CH2", "CH3", "MIC"};

        spinnerCurveFit = (Spinner) v.findViewById(R.id.spinner_curve_fit_da);
        spinnerChannelSelect1 = (Spinner) v.findViewById(R.id.spinner_channel_select_da1);
        spinnerChannelSelect2 = (Spinner) v.findViewById(R.id.spinner_channel_select_da2);
        boolean tabletSize = getResources().getBoolean(R.bool.isTablet);
        ArrayAdapter<String> curvefitAdapter;
        ArrayAdapter<String> adapter;

        if(tabletSize){
            curvefitAdapter = new ArrayAdapter<String>(this.getActivity(), R.layout.custom_spinner_tablet, curvefits);
            adapter = new ArrayAdapter<String>(this.getActivity(), R.layout.custom_spinner_tablet, channels);
        }
        else {
            curvefitAdapter = new ArrayAdapter<String>(this.getActivity(), R.layout.custom_spinner, curvefits);
            adapter = new ArrayAdapter<String>(this.getActivity(), R.layout.custom_spinner, channels);
        }

        curvefitAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        spinnerCurveFit.setAdapter(curvefitAdapter);
        spinnerChannelSelect1.setAdapter(adapter);
        spinnerChannelSelect2.setAdapter(adapter);

        spinnerCurveFit.setSelection(curvefitAdapter.getPosition("Sine Fit"),true);
        spinnerChannelSelect1.setSelection(adapter.getPosition("None"), true);
        spinnerChannelSelect2.setSelection(adapter.getPosition("None"), true);

        return v;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
