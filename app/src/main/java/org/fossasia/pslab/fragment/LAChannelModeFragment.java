package org.fossasia.pslab.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.fossasia.pslab.PSLabApplication;
import org.fossasia.pslab.R;

/**
 * Created by viveksb007 on 7/6/17.
 */

public class LAChannelModeFragment extends Fragment {

    public static final String PREFS_NAME = "customDialogPreference";
    public CheckBox dontShowAgain;
    private String[] channels = {"ID1", "ID2", "ID3", "ID4", "SEN", "EXT", "CNTR"};
    private String[] edges = {"EVERY EDGE", "EVERY FALLING EDGE", "EVERY RISING EDGE", "EVERY FOURTH RISING EDGE", "DISABLED"};
    private LinearLayout llChannel1, llChannel2, llChannel3, llChannel4;
    private int channelMode = 1;
    private Activity activity;
    private Spinner channelSelectSpinner1;
    private Spinner channelSelectSpinner2;
    private Spinner channelSelectSpinner3;
    private Spinner channelSelectSpinner4;

    public static LAChannelModeFragment newInstance(Activity activity) {
        LAChannelModeFragment laChannelModeFragment = new LAChannelModeFragment();
        laChannelModeFragment.activity = activity;
        return laChannelModeFragment;
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.logic_analyzer_select_channel, container, false);
        howToConnectDialog(getString(R.string.logic_analyzer_dialog_heading), getString(R.string.logic_analyzer_dialog_text), R.drawable.logic_analyzer_circuit, getString(R.string.logic_analyzer_dialog_description));
        Spinner modeSelectSpinner = v.findViewById(R.id.channel_mode_select_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.select_channel_mode_la, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modeSelectSpinner.setAdapter(adapter);
        modeSelectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        channelMode = 1;
                        llChannel1.setVisibility(View.VISIBLE);
                        llChannel2.setVisibility(View.GONE);
                        llChannel3.setVisibility(View.GONE);
                        llChannel4.setVisibility(View.GONE);
                        channelSelectSpinner1.setEnabled(true);
                        break;
                    case 1:
                        channelMode = 2;
                        llChannel1.setVisibility(View.VISIBLE);
                        llChannel2.setVisibility(View.VISIBLE);
                        llChannel3.setVisibility(View.GONE);
                        llChannel4.setVisibility(View.GONE);
                        channelSelectSpinner1.setEnabled(true);
                        channelSelectSpinner2.setEnabled(true);
                        break;
                    case 2:
                        channelMode = 3;
                        channelSelectSpinner1.setSelection(0);
                        channelSelectSpinner2.setSelection(1);
                        channelSelectSpinner3.setSelection(2);
                        channelSelectSpinner1.setEnabled(false);
                        channelSelectSpinner2.setEnabled(false);
                        channelSelectSpinner3.setEnabled(false);
                        llChannel1.setVisibility(View.VISIBLE);
                        llChannel2.setVisibility(View.VISIBLE);
                        llChannel3.setVisibility(View.VISIBLE);
                        llChannel4.setVisibility(View.GONE);
                        break;
                    case 3:
                        channelMode = 4;
                        channelSelectSpinner1.setSelection(0);
                        channelSelectSpinner2.setSelection(1);
                        channelSelectSpinner3.setSelection(2);
                        channelSelectSpinner4.setSelection(3);
                        channelSelectSpinner1.setEnabled(false);
                        channelSelectSpinner2.setEnabled(false);
                        channelSelectSpinner3.setEnabled(false);
                        channelSelectSpinner4.setEnabled(false);
                        llChannel1.setVisibility(View.VISIBLE);
                        llChannel2.setVisibility(View.VISIBLE);
                        llChannel3.setVisibility(View.VISIBLE);
                        llChannel4.setVisibility(View.VISIBLE);
                        break;
                    default:
                        channelMode = 1;
                        llChannel1.setVisibility(View.VISIBLE);
                        llChannel2.setVisibility(View.GONE);
                        llChannel3.setVisibility(View.GONE);
                        llChannel4.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //
            }
        });

        LinearLayout llChannelSelectMode = v.findViewById(R.id.channel_select_mode_ll);
        View selectView = createSingleChannelMode(inflater);
        llChannel1 = selectView.findViewById(R.id.ll_channel1);
        llChannel1.setVisibility(View.VISIBLE);
        llChannel2 = selectView.findViewById(R.id.ll_channel2);
        llChannel2.setVisibility(View.GONE);
        llChannel3 = selectView.findViewById(R.id.ll_channel3);
        llChannel3.setVisibility(View.GONE);
        llChannel4 = selectView.findViewById(R.id.ll_channel4);
        llChannel4.setVisibility(View.GONE);
        llChannelSelectMode.addView(selectView);
        return v;
    }

    private View createSingleChannelMode(LayoutInflater inflater) {

        View selectView = inflater.inflate(R.layout.logic_analyzer_select_channel_mode, null);

        channelSelectSpinner1 = selectView.findViewById(R.id.single_channel_input_1);
        channelSelectSpinner1.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, channels));
        final Spinner edgeSelectSpinner1 = selectView.findViewById(R.id.single_channel_edge_1);
        edgeSelectSpinner1.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, edges));

        channelSelectSpinner2 = selectView.findViewById(R.id.single_channel_input_2);
        channelSelectSpinner2.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, channels));
        final Spinner edgeSelectSpinner2 = selectView.findViewById(R.id.single_channel_edge_2);
        edgeSelectSpinner2.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, edges));

        channelSelectSpinner3 = selectView.findViewById(R.id.single_channel_input_3);
        channelSelectSpinner3.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, channels));
        final Spinner edgeSelectSpinner3 = selectView.findViewById(R.id.single_channel_edge_3);
        edgeSelectSpinner3.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, edges));

        channelSelectSpinner4 = selectView.findViewById(R.id.single_channel_input_4);
        channelSelectSpinner4.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, channels));
        final Spinner edgeSelectSpinner4 = selectView.findViewById(R.id.single_channel_edge_4);
        edgeSelectSpinner4.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, edges));

        Button btnAnalyze = selectView.findViewById(R.id.btn_la_analyze);
        btnAnalyze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle params = new Bundle();
                params.putInt("channelMode", channelMode);
                switch (channelMode) {
                    case 1:
                        params.putString("inputChannel1", channelSelectSpinner1.getSelectedItem().toString());
                        params.putString("edge1", edgeSelectSpinner1.getSelectedItem().toString());
                        break;
                    case 2:
                        params.putString("inputChannel1", channelSelectSpinner1.getSelectedItem().toString());
                        params.putString("edge1", edgeSelectSpinner1.getSelectedItem().toString());
                        params.putString("inputChannel2", channelSelectSpinner2.getSelectedItem().toString());
                        params.putString("edge2", edgeSelectSpinner2.getSelectedItem().toString());
                        break;
                    case 3:
                        params.putString("inputChannel1", channelSelectSpinner1.getSelectedItem().toString());
                        params.putString("edge1", edgeSelectSpinner1.getSelectedItem().toString());
                        params.putString("inputChannel2", channelSelectSpinner2.getSelectedItem().toString());
                        params.putString("edge2", edgeSelectSpinner2.getSelectedItem().toString());
                        params.putString("inputChannel3", channelSelectSpinner3.getSelectedItem().toString());
                        params.putString("edge3", edgeSelectSpinner3.getSelectedItem().toString());
                        break;
                    case 4:
                        params.putString("inputChannel1", channelSelectSpinner1.getSelectedItem().toString());
                        params.putString("edge1", edgeSelectSpinner1.getSelectedItem().toString());
                        params.putString("inputChannel2", channelSelectSpinner2.getSelectedItem().toString());
                        params.putString("edge2", edgeSelectSpinner2.getSelectedItem().toString());
                        params.putString("inputChannel3", channelSelectSpinner3.getSelectedItem().toString());
                        params.putString("edge3", edgeSelectSpinner3.getSelectedItem().toString());
                        params.putString("inputChannel4", channelSelectSpinner4.getSelectedItem().toString());
                        params.putString("edge4", edgeSelectSpinner4.getSelectedItem().toString());
                        break;
                }
                try {
                    ((OnChannelSelectedListener) activity).channelSelectedNowAnalyze(params);
                } catch (ClassCastException cce) {
                    cce.printStackTrace();
                }
            }
        });

        return selectView;
    }

    @SuppressLint("ResourceType")
    public void howToConnectDialog(String title, String intro, int iconID, String desc) {
        try {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.custom_dialog_box, null);
            final SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
            Boolean skipMessage = settings.getBoolean("skipMessage", false);

            dontShowAgain = (CheckBox) dialogView.findViewById(R.id.toggle_show_again);
            final TextView heading_text = (TextView) dialogView.findViewById(R.id.custom_dialog_text);
            final TextView description_text = (TextView) dialogView.findViewById(R.id.description_text);
            final ImageView schematic = (ImageView) dialogView.findViewById(R.id.custom_dialog_schematic);
            final Button ok_button = (Button) dialogView.findViewById(R.id.dismiss_button);

            builder.setView(dialogView);
            builder.setTitle(title);
            heading_text.setText(intro);
            schematic.setImageResource(iconID);
            description_text.setText(desc);
            final AlertDialog dialog = builder.create();
            ok_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Boolean checkBoxResult = false;
                    if (dontShowAgain.isChecked())
                        checkBoxResult = true;
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean("skipMessage", checkBoxResult);
                    editor.apply();
                    dialog.dismiss();
                }
            });
            if (!skipMessage)
                dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        ((PSLabApplication) getActivity().getApplication()).refWatcher.watch(this, LAChannelModeFragment.class.getSimpleName());
    }

    public interface OnChannelSelectedListener {
        void channelSelectedNowAnalyze(Bundle params);
    }
}
