package org.fossasia.pslab.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

import org.fossasia.pslab.R;

/**
 * Created by viveksb007 on 7/6/17.
 */

public class LAChannelModeFragment extends Fragment {


    private LinearLayout llChannelSelectMode;
    private String[] channels = {"ID1", "ID2", "ID3", "ID4"};
    private String[] edges = {"EVERY EDGE", "EVERY FALLING EDGE", "EVERY RISING EDGE", "EVERY FOURTH RISING EDGE", "EVERY SIXTEENTH RISING EDGE"};
    private LinearLayout llChannel1, llChannel2, llChannel3, llChannel4;

    public static LAChannelModeFragment newInstance() {
        LAChannelModeFragment laChannelModeFragment = new LAChannelModeFragment();
        return laChannelModeFragment;
    }


    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.logic_analyzer_select_channel, container, false);
        Spinner modeSelectSpinner = (Spinner) v.findViewById(R.id.channel_mode_select_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.select_channel_mode_la, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modeSelectSpinner.setAdapter(adapter);
        modeSelectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        llChannel1.setVisibility(View.VISIBLE);
                        llChannel2.setVisibility(View.GONE);
                        llChannel3.setVisibility(View.GONE);
                        llChannel4.setVisibility(View.GONE);
                        break;
                    case 1:
                        llChannel1.setVisibility(View.VISIBLE);
                        llChannel2.setVisibility(View.VISIBLE);
                        llChannel3.setVisibility(View.GONE);
                        llChannel4.setVisibility(View.GONE);
                        break;
                    case 2:
                        llChannel1.setVisibility(View.VISIBLE);
                        llChannel2.setVisibility(View.VISIBLE);
                        llChannel3.setVisibility(View.VISIBLE);
                        llChannel4.setVisibility(View.GONE);
                        break;
                    case 3:
                        llChannel1.setVisibility(View.VISIBLE);
                        llChannel2.setVisibility(View.VISIBLE);
                        llChannel3.setVisibility(View.VISIBLE);
                        llChannel4.setVisibility(View.VISIBLE);
                        break;

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        llChannelSelectMode = (LinearLayout) v.findViewById(R.id.channel_select_mode_ll);
        View selectView = createSingleChannelMode(inflater);
        llChannel1 = (LinearLayout) selectView.findViewById(R.id.ll_channel1);
        llChannel2 = (LinearLayout) selectView.findViewById(R.id.ll_channel2);
        llChannel3 = (LinearLayout) selectView.findViewById(R.id.ll_channel3);
        llChannel4 = (LinearLayout) selectView.findViewById(R.id.ll_channel4);
        llChannelSelectMode.addView(selectView);
        return v;
    }

    private View createSingleChannelMode(LayoutInflater inflater) {
        View selectView = inflater.inflate(R.layout.single_channel_mode_la, null);
        Spinner channelSelectSpinner1 = (Spinner) selectView.findViewById(R.id.single_channel_input_1);
        channelSelectSpinner1.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, channels));
        Spinner edgeSelectSpinner1 = (Spinner) selectView.findViewById(R.id.single_channel_edge_1);
        edgeSelectSpinner1.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, edges));

        Spinner channelSelectSpinner2 = (Spinner) selectView.findViewById(R.id.single_channel_input_2);
        channelSelectSpinner2.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, channels));
        Spinner edgeSelectSpinner2 = (Spinner) selectView.findViewById(R.id.single_channel_edge_2);
        edgeSelectSpinner2.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, edges));

        Spinner channelSelectSpinner3 = (Spinner) selectView.findViewById(R.id.single_channel_input_3);
        channelSelectSpinner3.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, channels));
        Spinner edgeSelectSpinner3 = (Spinner) selectView.findViewById(R.id.single_channel_edge_3);
        edgeSelectSpinner3.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, edges));

        Spinner channelSelectSpinner4 = (Spinner) selectView.findViewById(R.id.single_channel_input_4);
        channelSelectSpinner4.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, channels));
        Spinner edgeSelectSpinner4 = (Spinner) selectView.findViewById(R.id.single_channel_edge_4);
        edgeSelectSpinner4.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, edges));

        return selectView;
    }
}
