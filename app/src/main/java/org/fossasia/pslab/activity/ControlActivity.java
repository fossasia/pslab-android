package org.fossasia.pslab.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import org.fossasia.pslab.communication.ScienceLab;
import org.fossasia.pslab.others.ScienceLabCommon;

/**
 * Created by viveksb007 on 10/5/17.
 */

public class ControlActivity extends AppCompatActivity {

    private ScienceLab scienceLab;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView();
        scienceLab = ScienceLabCommon.getInstance().scienceLab;
    }
}
