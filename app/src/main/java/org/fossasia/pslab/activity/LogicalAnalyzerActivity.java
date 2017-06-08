package org.fossasia.pslab.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import org.fossasia.pslab.R;
import org.fossasia.pslab.communication.ScienceLab;
import org.fossasia.pslab.fragment.LAChannelModeFragment;
import org.fossasia.pslab.others.ScienceLabCommon;

import butterknife.ButterKnife;

/**
 * Created by viveksb007 on 10/5/17.
 */

public class LogicalAnalyzerActivity extends AppCompatActivity {

    private ScienceLab scienceLab;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logic_analyzer);
        scienceLab = ScienceLabCommon.getInstance().scienceLab;
        ButterKnife.bind(this);
        getSupportFragmentManager().beginTransaction().add(R.id.la_frame_layout, LAChannelModeFragment.newInstance()).commit();
    }

}
