package io.pslab.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import io.pslab.R;
import io.pslab.activity.OscilloscopeActivity;

public class OscilloscopePlaybackFragment extends Fragment {

    private OscilloscopeActivity oscilloscopeActivity;
    private TextView timebaseTextView;

    public static OscilloscopePlaybackFragment newInstance() {
        return new OscilloscopePlaybackFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_oscilloscope_playback, container, false);
        timebaseTextView = rootView.findViewById(R.id.timebase_data);
        CheckBox fourierCheckBox = rootView.findViewById(R.id.fourier_checkbox);

        fourierCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                oscilloscopeActivity.isPlaybackFourierChecked = isChecked;
            }
        });
        return rootView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        oscilloscopeActivity = (OscilloscopeActivity) getActivity();
    }

    public void setTimeBase(String timeBase) {
        timebaseTextView.setText(timeBase);
    }
}

