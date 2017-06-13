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

import com.github.mikephil.charting.charts.LineChart;

import org.fossasia.pslab.R;
import org.fossasia.pslab.activity.OscilloscopeActivity;
import org.fossasia.pslab.communication.ScienceLab;
import org.fossasia.pslab.others.ScienceLabCommon;

public class ChannelParametersFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    public LineChart mChart;
    private Spinner spinnerRangeCh1;
    private Spinner spinnerRangeCh2;
    private Spinner spinnerChannelSelect;
    private ScienceLab scienceLab;

    public static ChannelParametersFragment newInstance(String param1, String param2) {
        ChannelParametersFragment fragment = new ChannelParametersFragment();
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

        View v = inflater.inflate(R.layout.fragment_channel_parameters, container, false);

        scienceLab = ScienceLabCommon.getInstance().scienceLab;
        mChart = ((OscilloscopeActivity) getActivity()).mChart;
        boolean tabletSize = getResources().getBoolean(R.bool.isTablet);

        String [] ranges = {"+/-16V", "+/-8V", "+/-4V", "+/-3V", "+/-2V", "+/-1.5V", "+/-1V", "+/-500mV", "+/-160V"};
        String [] channels = {"CH1", "CH2", "CH3", "MIC", "CAP","SEN", "AN8"};

        spinnerRangeCh1 = (Spinner) v.findViewById(R.id.spinner_range_ch1_cp);
        spinnerRangeCh2 = (Spinner) v.findViewById(R.id.spinner_range_ch2_cp);
        spinnerChannelSelect = (Spinner) v.findViewById(R.id.spinner_channel_select_cp);
        ArrayAdapter<String> rangesAdapter;
        ArrayAdapter<String> channelsAdapter;
        if(tabletSize){
            rangesAdapter = new ArrayAdapter<String>(this.getActivity(), R.layout.custom_spinner_tablet, ranges);
            channelsAdapter = new ArrayAdapter<String>(this.getActivity(), R.layout.custom_spinner_tablet, channels);
        }
        else {
            rangesAdapter = new ArrayAdapter<String>(this.getActivity(), R.layout.custom_spinner, ranges);
            channelsAdapter = new ArrayAdapter<String>(this.getActivity(), R.layout.custom_spinner, channels);
        }
        rangesAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        channelsAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        spinnerRangeCh1.setAdapter(rangesAdapter);
        spinnerRangeCh1.setSelection(rangesAdapter.getPosition("+/-4V"),true);
        spinnerRangeCh2.setAdapter(rangesAdapter);
        spinnerRangeCh2.setSelection(rangesAdapter.getPosition("+/-4V"),true);
        spinnerChannelSelect.setAdapter(channelsAdapter);
        spinnerChannelSelect.setSelection(channelsAdapter.getPosition("CH1"),true);

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
