package io.pslab.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Toast;

import io.pslab.R;
import io.pslab.activity.OscilloscopeActivity;
import io.pslab.others.NothingSelectedSpinnerAdapter;

public class ChannelParametersFragment extends Fragment {

    private static final int RECORD_AUDIO_REQUEST_CODE = 1;

    private CheckBox checkBoxCH1;
    private CheckBox checkBoxCH2;
    private CheckBox checkBoxCH3;
    private CheckBox checkBoxMIC;
    private Spinner spinnerRangeCh1;
    private Spinner spinnerRangeCh2;
    private Spinner spinnerChannelSelect;
    private Spinner spinnerMICSelect;
    private int micSelectedPosition;


    public static ChannelParametersFragment newInstance() {
        return new ChannelParametersFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_channel_parameters, container, false);

        final String[] ranges = {"+/-16V", "+/-8V", "+/-4V", "+/-3V", "+/-2V", "+/-1.5V", "+/-1V", "+/-500mV", "+/-160V"};
        final String[] channels = {"CH1", "CH2", "CH3", "MIC", "CAP", "SEN", "AN8"};
        final String[] mics = {"MICROPHONE", "IN-BUILT MIC"};

        spinnerRangeCh1 = v.findViewById(R.id.spinner_range_ch1_cp);
        spinnerRangeCh2 = v.findViewById(R.id.spinner_range_ch2_cp);
        spinnerChannelSelect = v.findViewById(R.id.spinner_channel_select_cp);
        spinnerMICSelect = v.findViewById(R.id.spinner_mic_select_cp);

        boolean tabletSize = getResources().getBoolean(R.bool.isTablet);
        checkBoxCH1 = v.findViewById(R.id.checkBox_ch1_cp);
        checkBoxCH2 = v.findViewById(R.id.checkBox_ch2_cp);
        checkBoxCH3 = v.findViewById(R.id.checkBox_ch3_cp);
        checkBoxMIC = v.findViewById(R.id.checkBox_mic_cp);

        ArrayAdapter<String> rangesAdapter;
        ArrayAdapter<String> channelsAdapter;
        ArrayAdapter<String> micsAdapter;
        if (tabletSize) {
            rangesAdapter = new ArrayAdapter<>(this.getActivity(), R.layout.custom_spinner_tablet, ranges);
            channelsAdapter = new ArrayAdapter<>(this.getActivity(), R.layout.custom_spinner_tablet, channels);
            micsAdapter = new ArrayAdapter<>(this.getActivity(), R.layout.custom_spinner_mic_tablet, mics);
        } else {
            rangesAdapter = new ArrayAdapter<>(this.getActivity(), R.layout.custom_spinner, ranges);
            channelsAdapter = new ArrayAdapter<>(this.getActivity(), R.layout.custom_spinner, channels);
            micsAdapter = new ArrayAdapter<>(this.getActivity(), R.layout.custom_spinner_mic, mics);

        }
        rangesAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        channelsAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        micsAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        spinnerRangeCh1.setAdapter(rangesAdapter);
        spinnerRangeCh1.setSelection(rangesAdapter.getPosition("+/-16V"), true);
        spinnerRangeCh2.setAdapter(rangesAdapter);
        spinnerRangeCh2.setSelection(rangesAdapter.getPosition("+/-16V"), true);
        spinnerChannelSelect.setAdapter(channelsAdapter);
        spinnerChannelSelect.setSelection(channelsAdapter.getPosition("CH1"), true);
        spinnerMICSelect.setAdapter(new NothingSelectedSpinnerAdapter(micsAdapter,
                R.layout.nothing_selected_spinner_row,
                getActivity()));

        spinnerRangeCh1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        ((OscilloscopeActivity) getActivity()).setLeftYAxisScale(16, -16);
                        break;
                    case 1:
                        ((OscilloscopeActivity) getActivity()).setLeftYAxisScale(8, -8);
                        break;
                    case 2:
                        ((OscilloscopeActivity) getActivity()).setLeftYAxisScale(4, -4);
                        break;
                    case 3:
                        ((OscilloscopeActivity) getActivity()).setLeftYAxisScale(3, -3);
                        break;
                    case 4:
                        ((OscilloscopeActivity) getActivity()).setLeftYAxisScale(2, -2);
                        break;
                    case 5:
                        ((OscilloscopeActivity) getActivity()).setLeftYAxisScale(1.5, -1.5);
                        break;
                    case 6:
                        ((OscilloscopeActivity) getActivity()).setLeftYAxisScale(1, -1);
                        break;
                    case 7:
                        ((OscilloscopeActivity) getActivity()).setLeftYAxisScale(500, -500);
                        break;
                    case 8:
                        ((OscilloscopeActivity) getActivity()).setLeftYAxisScale(160, -160);
                        openAlertDialogBox("CH1");
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerRangeCh2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        ((OscilloscopeActivity) getActivity()).setRightYAxisScale(16, -16);
                        break;
                    case 1:
                        ((OscilloscopeActivity) getActivity()).setRightYAxisScale(8, -8);
                        break;
                    case 2:
                        ((OscilloscopeActivity) getActivity()).setRightYAxisScale(4, -4);
                        break;
                    case 3:
                        ((OscilloscopeActivity) getActivity()).setRightYAxisScale(3, -3);
                        break;
                    case 4:
                        ((OscilloscopeActivity) getActivity()).setRightYAxisScale(2, -2);
                        break;
                    case 5:
                        ((OscilloscopeActivity) getActivity()).setRightYAxisScale(1.5, -1.5);
                        break;
                    case 6:
                        ((OscilloscopeActivity) getActivity()).setRightYAxisScale(1, -1);
                        break;
                    case 7:
                        ((OscilloscopeActivity) getActivity()).setRightYAxisScale(500, -500);
                        break;
                    case 8:
                        ((OscilloscopeActivity) getActivity()).setRightYAxisScale(160, -160);
                        openAlertDialogBox("CH2");
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerChannelSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        ((OscilloscopeActivity) getActivity()).setLeftYAxisLabel(spinnerChannelSelect.getSelectedItem().toString());
                        spinnerRangeCh1.setEnabled(true);
                        break;
                    case 1:
                        ((OscilloscopeActivity) getActivity()).setLeftYAxisScale(16, -16);
                        ((OscilloscopeActivity) getActivity()).setLeftYAxisLabel(spinnerChannelSelect.getSelectedItem().toString());
                        spinnerRangeCh1.setEnabled(false);
                        break;
                    case 2:
                        ((OscilloscopeActivity) getActivity()).setLeftYAxisScale(3, -3);
                        ((OscilloscopeActivity) getActivity()).setLeftYAxisLabel(spinnerChannelSelect.getSelectedItem().toString());
                        spinnerRangeCh1.setEnabled(false);
                        break;
                    case 3:
                        ((OscilloscopeActivity) getActivity()).setLeftYAxisScale(3, -3);
                        ((OscilloscopeActivity) getActivity()).setLeftYAxisLabel(spinnerChannelSelect.getSelectedItem().toString());
                        break;
                    case 4:
                        ((OscilloscopeActivity) getActivity()).setLeftYAxisScale(3, 0);
                        ((OscilloscopeActivity) getActivity()).setLeftYAxisLabel(spinnerChannelSelect.getSelectedItem().toString());
                        spinnerRangeCh1.setEnabled(false);
                        break;
                    case 5:
                        ((OscilloscopeActivity) getActivity()).setLeftYAxisScale(3, 0);
                        ((OscilloscopeActivity) getActivity()).setLeftYAxisLabel(spinnerChannelSelect.getSelectedItem().toString());
                        spinnerRangeCh1.setEnabled(false);
                        break;
                    case 6:
                        ((OscilloscopeActivity) getActivity()).setLeftYAxisScale(3, 0);
                        ((OscilloscopeActivity) getActivity()).setLeftYAxisLabel(spinnerChannelSelect.getSelectedItem().toString());
                        spinnerRangeCh1.setEnabled(false);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerMICSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                micSelectedPosition = position;
                if (position == 1) {
                    ((OscilloscopeActivity) getActivity()).isInBuiltMicSelected = false;
                    if (checkBoxMIC.isChecked())
                        ((OscilloscopeActivity) getActivity()).isMICSelected = true;
                } else {
                    ((OscilloscopeActivity) getActivity()).isMICSelected = false;
                    if (checkBoxMIC.isChecked())
                        ((OscilloscopeActivity) getActivity()).isInBuiltMicSelected = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        checkBoxCH1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ((OscilloscopeActivity) getActivity()).isCH1Selected = isChecked;
            }
        });

        checkBoxCH2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ((OscilloscopeActivity) getActivity()).isCH2Selected = isChecked;
            }
        });

        checkBoxCH3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ((OscilloscopeActivity) getActivity()).isCH3Selected = isChecked;
            }
        });
        checkBoxMIC.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (micSelectedPosition == 1) {
                    ((OscilloscopeActivity) getActivity()).isMICSelected = isChecked;
                } else if (micSelectedPosition == 2) {
                    // check for RECORD_AUDIO permission if has then change boolean
                    if (isChecked)
                        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_REQUEST_CODE);
                        } else {
                            ((OscilloscopeActivity) getActivity()).isInBuiltMicSelected = true;
                        }
                    else
                        ((OscilloscopeActivity) getActivity()).isInBuiltMicSelected = false;
                }
            }
        });

        return v;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == RECORD_AUDIO_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ((OscilloscopeActivity) getActivity()).isInBuiltMicSelected = true;
            } else {
                Toast.makeText(getActivity(), "This feature won't work.", Toast.LENGTH_SHORT).show();
                if (checkBoxMIC.isChecked())
                    checkBoxMIC.toggle();
            }
        }
    }

    private void openAlertDialogBox(String inputSource) {
        new AlertDialog.Builder(getActivity())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Message")
                .setMessage("Connect a 10MOhm resistor with " + inputSource)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }
}
