package org.fossasia.pslab.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import org.fossasia.pslab.R;

import butterknife.ButterKnife;

/**
 * Created by viveksb007 on 15/3/17.
 */

public class HelpAndFeedback extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_feedback);
        ButterKnife.bind(this);
    }
}
