package io.pslab.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import io.pslab.R;
import io.pslab.activity.OscilloscopeActivity;

public class OscilloscopePlaybackFragment extends Fragment {

    private OscilloscopeActivity oscilloscopeActivity;
    private TextView timebaseTextView;
    private ImageView playButton;
    private boolean isPlaying = false;

    public static OscilloscopePlaybackFragment newInstance() {
        return new OscilloscopePlaybackFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_oscilloscope_playback, container, false);
        timebaseTextView = rootView.findViewById(R.id.timebase_data);
        playButton = rootView.findViewById(R.id.play_button);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying) {
                    isPlaying = false;
                    playButton.setImageResource(R.drawable.ic_play_button);
                    oscilloscopeActivity.pauseData();
                } else {
                    isPlaying = true;
                    playButton.setImageResource(R.drawable.pause_icon);
                    oscilloscopeActivity.playRecordedData();
                }
            }
        });
        return rootView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        oscilloscopeActivity = (OscilloscopeActivity) getActivity();
    }

    public void resetPlayButton() {
        playButton.setImageResource(R.drawable.ic_play_button);
        isPlaying = false;
    }

    public void setTimeBase(String timeBase) {
        timebaseTextView.setText(timeBase);
    }
}

