package org.fossasia.pslab.experimentsetup.physicsexperiments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import org.fossasia.pslab.R;
import org.fossasia.pslab.activity.OscilloscopeActivity;
import org.fossasia.pslab.communication.ScienceLab;
import org.fossasia.pslab.others.FloatSeekBar;
import org.fossasia.pslab.others.ScienceLabCommon;

import java.text.DecimalFormat;

public class SpeedOfSoundFragment extends Fragment {
    Button readButton;
    public TextView resultTextView;
    Spinner rangeSpinner;
    TextView sqr1ProgressTextView;
    ScienceLab scienceLab;
    FloatSeekBar sqr1SeekBar;
    double sqr1Value;

    public SpeedOfSoundFragment() {

    }

    public static SpeedOfSoundFragment newInstance() {
        return new SpeedOfSoundFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_speed_of_sound, container, false);
        readButton = (Button) v.findViewById(R.id.button_read_sos);
        resultTextView = (TextView) v.findViewById(R.id.tv_result_sos);
        rangeSpinner = (Spinner) v.findViewById(R.id.spinner_range_sos);
        sqr1SeekBar = (FloatSeekBar) v.findViewById(R.id.seekBar_sqr1_sos);
        sqr1ProgressTextView = (TextView) v.findViewById(R.id.tv_sqr1_sos);
        scienceLab = ScienceLabCommon.scienceLab;
        final String[] ranges = {"+/-16V", "+/-8V", "+/-4V", "+/-3V", "+/-2V", "+/-1.5V", "+/-1V", "+/-500mV", "+/-160V"};

        sqr1SeekBar.setters(10, 100000);
        sqr1SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(scienceLab.isConnected()) {
                    sqr1Value = scienceLab.setSqr1(sqr1SeekBar.getValue(), -1, false);
                    sqr1ProgressTextView.setText(String.format("%sHz", new DecimalFormat("#.##").format(sqr1Value)));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        ArrayAdapter<String> rangesAdapter;
        rangesAdapter = new ArrayAdapter<>(this.getActivity(), R.layout.custom_spinner, ranges);
        rangesAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        rangeSpinner.setAdapter(rangesAdapter);
        rangeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        ((OscilloscopeActivity) getActivity()).setLeftYAxisScale(16, -16);
                        ((OscilloscopeActivity) getActivity()).setRightYAxisScale(16, -16);
                        break;
                    case 1:
                        ((OscilloscopeActivity) getActivity()).setLeftYAxisScale(8, -8);
                        ((OscilloscopeActivity) getActivity()).setRightYAxisScale(8, -8);
                        break;
                    case 2:
                        ((OscilloscopeActivity) getActivity()).setLeftYAxisScale(4, -4);
                        ((OscilloscopeActivity) getActivity()).setRightYAxisScale(4, -4);
                        break;
                    case 3:
                        ((OscilloscopeActivity) getActivity()).setLeftYAxisScale(3, -3);
                        ((OscilloscopeActivity) getActivity()).setRightYAxisScale(3, -3);
                        break;
                    case 4:
                        ((OscilloscopeActivity) getActivity()).setLeftYAxisScale(2, -2);
                        ((OscilloscopeActivity) getActivity()).setRightYAxisScale(2, -2);
                        break;
                    case 5:
                        ((OscilloscopeActivity) getActivity()).setLeftYAxisScale(1.5, -1.5);
                        ((OscilloscopeActivity) getActivity()).setRightYAxisScale(1.5, -1.5);
                        break;
                    case 6:
                        ((OscilloscopeActivity) getActivity()).setLeftYAxisScale(1, -1);
                        ((OscilloscopeActivity) getActivity()).setRightYAxisScale(1, -1);
                        break;
                    case 7:
                        ((OscilloscopeActivity) getActivity()).setLeftYAxisScale(500, -500);
                        ((OscilloscopeActivity) getActivity()).setRightYAxisScale(500, -500);
                        break;
                    case 8:
                        ((OscilloscopeActivity) getActivity()).setLeftYAxisScale(160, -160);
                        ((OscilloscopeActivity) getActivity()).setRightYAxisScale(160, -160);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        readButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((OscilloscopeActivity)getActivity()).isSpeedOfSoundExperiment = true;
                ((OscilloscopeActivity)getActivity()).runSpeedOfSoundExperiment = true;

            }
        });

        ((OscilloscopeActivity) getActivity()).setLeftYAxisScale(8, -8);
        ((OscilloscopeActivity) getActivity()).setRightYAxisScale(8, -8);

        return v;
    }
}