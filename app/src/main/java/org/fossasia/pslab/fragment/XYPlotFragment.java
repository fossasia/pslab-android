package org.fossasia.pslab.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.fossasia.pslab.R;
import org.fossasia.pslab.activity.OscilloscopeActivity;
import org.fossasia.pslab.others.ViewGroupUtils;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;

public class XYPlotFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private Spinner spinnerChannelSelect1;
    private Spinner spinnerChannelSelect2;
    private CheckBox checkBoxXYPlot;
    private Button viewButton;

    public static XYPlotFragment newInstance(String param1, String param2) {
        XYPlotFragment fragment = new XYPlotFragment();
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
        View v = inflater.inflate(R.layout.fragment_xyplot, container, false);
        final String[] channels = {"CH1", "CH2", "CH3", "MIC"};
        spinnerChannelSelect1 = (Spinner) v.findViewById(R.id.spinner_channel_select_xy1);
        spinnerChannelSelect2 = (Spinner) v.findViewById(R.id.spinner_channel_select_xy2);
        checkBoxXYPlot = (CheckBox) v.findViewById(R.id.checkBox_enable_xy_xy);
        viewButton = (Button) v.findViewById(R.id.button_view_xy);
        spinnerChannelSelect1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if (((OscilloscopeActivity) getActivity()).isXYPlotSelected) {
                    ((OscilloscopeActivity) getActivity()).setXAxisLabel(channels[position]);
                    ((OscilloscopeActivity) getActivity()).xAxisLabelUnit.setText("(V)");
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        spinnerChannelSelect2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if (((OscilloscopeActivity) getActivity()).isXYPlotSelected) {
                    ((OscilloscopeActivity) getActivity()).setLeftYAxisLabel(channels[position]);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        boolean tabletSize = getResources().getBoolean(R.bool.isTablet);
        ArrayAdapter<String> channelsAdapter;

        if (tabletSize) {
            channelsAdapter = new ArrayAdapter<String>(this.getActivity(), R.layout.custom_spinner_tablet, channels);
        } else {
            channelsAdapter = new ArrayAdapter<String>(this.getActivity(), R.layout.custom_spinner, channels);
        }

        channelsAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        spinnerChannelSelect1.setAdapter(channelsAdapter);
        spinnerChannelSelect2.setAdapter(channelsAdapter);

        spinnerChannelSelect1.setSelection(channelsAdapter.getPosition("CH1"), true);
        spinnerChannelSelect2.setSelection(channelsAdapter.getPosition("CH2"), true);

        checkBoxXYPlot.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ((OscilloscopeActivity) getActivity()).isXYPlotSelected = isChecked;
                if (isChecked) {
                    ViewGroupUtils.replaceView(((OscilloscopeActivity) getActivity()).mChart,
                            ((OscilloscopeActivity) getActivity()).graph);
                    ((OscilloscopeActivity) getActivity()).setXAxisLabel(spinnerChannelSelect1.getSelectedItem().toString());
                    ((OscilloscopeActivity) getActivity()).setLeftYAxisLabel(spinnerChannelSelect2.getSelectedItem().toString());
                    ((OscilloscopeActivity) getActivity()).xAxisLabelUnit.setText("(V)");
                    ((OscilloscopeActivity) getActivity()).rightYAxisLabel.setVisibility(View.INVISIBLE);
                    ((OscilloscopeActivity) getActivity()).rightYAxisLabelUnit.setVisibility(View.INVISIBLE);


                } else {
                    ViewGroupUtils.replaceView(((OscilloscopeActivity) getActivity()).graph,
                            ((OscilloscopeActivity) getActivity()).mChart);
                    ((OscilloscopeActivity) getActivity()).rightYAxisLabel.setVisibility(View.VISIBLE);
                    ((OscilloscopeActivity) getActivity()).rightYAxisLabelUnit.setVisibility(View.VISIBLE);
                    ((OscilloscopeActivity) getActivity()).setXAxisLabel("time");
                    ((OscilloscopeActivity) getActivity()).setXAxisScale(((OscilloscopeActivity) getActivity()).timebase);

                }

            }
        });

        viewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((OscilloscopeActivity) getActivity()).viewIsClicked = true;
            }
        });

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
