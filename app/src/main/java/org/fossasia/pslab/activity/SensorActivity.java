package org.fossasia.pslab.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import org.fossasia.pslab.R;
import org.fossasia.pslab.communication.ScienceLab;
import org.fossasia.pslab.others.ScienceLabCommon;

import butterknife.ButterKnife;

/**
 * Created by asitava on 18/6/17.
 */

public class SensorActivity extends AppCompatActivity {
    private ScienceLab scienceLab;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);
        scienceLab = ScienceLabCommon.getInstance().scienceLab;
        ButterKnife.bind(this);
    }
}
