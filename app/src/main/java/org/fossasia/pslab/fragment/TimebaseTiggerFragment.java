package org.fossasia.pslab.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import org.fossasia.pslab.R;
import org.fossasia.pslab.others.FloatSeekBar;

public class TimebaseTiggerFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private Spinner spinnerTiggerChannelSelect;
    private SeekBar seekBarTimebase;
    private FloatSeekBar seekBarTigger;
    private TextView textViewTimeBase;
    private TextView textViewTigger;


    public static TimebaseTiggerFragment newInstance(String param1, String param2) {
        TimebaseTiggerFragment fragment = new TimebaseTiggerFragment();
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

        View v = inflater.inflate(R.layout.fragment_timebase_tigger, container, false);

        seekBarTimebase = (SeekBar) v.findViewById(R.id.seekBar_timebase_tt);
        seekBarTigger = (FloatSeekBar) v.findViewById(R.id.seekBar_tigger);
        textViewTimeBase = (TextView) v.findViewById(R.id.tv_timebase_values_tt);
        textViewTigger = (TextView) v.findViewById(R.id.tv_tigger_values_tt);
        spinnerTiggerChannelSelect = (Spinner) v.findViewById(R.id.spinner_tigger_channel_tt);

        seekBarTimebase.setMax(8);
        seekBarTimebase.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //incomplete
                if(progress == 0)
                    textViewTimeBase.setText("875.00 μs");
                if(progress == 1)
                    textViewTimeBase.setText("1.00 ms");
                if(progress == 2)
                    textViewTimeBase.setText("2.00 ms");
                if(progress == 3)
                    textViewTimeBase.setText("4.00 ms");
                if(progress == 4)
                    textViewTimeBase.setText("8.00 ms");
                if(progress == 5)
                    textViewTimeBase.setText("25.60 ms");
                if(progress == 6)
                    textViewTimeBase.setText("38.40 ms");
                if(progress == 7)
                    textViewTimeBase.setText("51.20 ms");
                if(progress == 8)
                    textViewTimeBase.setText("102.40 ms");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBarTimebase.setProgress(1);
        seekBarTigger.setters(-16.5, 16.5);
        seekBarTigger.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textViewTigger.setText(seekBarTigger.getValue() + " V");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBarTigger.setProgress(50);

        String [] channels = {"CH1", "CH2", "CH3", "MIC"};
        ArrayAdapter<String> channelsAdapter = new ArrayAdapter<String>(this.getActivity(), R.layout.custom_spinner, channels);
        channelsAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spinnerTiggerChannelSelect.setAdapter(channelsAdapter);
        spinnerTiggerChannelSelect.setSelection(channelsAdapter.getPosition("CH1"),true);

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