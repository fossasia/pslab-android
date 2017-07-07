package org.fossasia.pslab.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import org.fossasia.pslab.R;
import org.fossasia.pslab.communication.ScienceLab;
import org.fossasia.pslab.fragment.LAChannelModeFragment;
import org.fossasia.pslab.fragment.LALogicLinesFragment;

import org.fossasia.pslab.others.ScienceLabCommon;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by viveksb007 on 10/5/17.
 */

public class LogicalAnalyzerActivity extends AppCompatActivity
        implements LAChannelModeFragment.OnChannelSelectedListener {

    private ScienceLab scienceLab;
    @BindView(R.id.logical_analyzer_toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logic_analyzer);
        scienceLab = ScienceLabCommon.scienceLab;
        ButterKnife.bind(this);
        getSupportFragmentManager().beginTransaction().add(R.id.la_frame_layout, LAChannelModeFragment.newInstance(this)).commit();
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void channelSelectedNowAnalyze(Bundle params) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        ft.replace(R.id.la_frame_layout, LALogicLinesFragment.newInstance(params, this)).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
