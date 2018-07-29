package org.pslab.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import org.pslab.R;
import org.pslab.communication.ScienceLab;
import org.pslab.fragment.LALogicLinesFragment;
import org.pslab.others.ScienceLabCommon;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by viveksb007 on 10/5/17.
 */

public class LogicalAnalyzerActivity extends AppCompatActivity {

    @BindView(R.id.logical_analyzer_toolbar)
    Toolbar toolbar;
    private ScienceLab scienceLab;
    private boolean isRunning = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logic_analyzer);
        scienceLab = ScienceLabCommon.scienceLab;
        ButterKnife.bind(this);
        getSupportFragmentManager().beginTransaction().add(R.id.la_frame_layout, LALogicLinesFragment.newInstance(this)).commit();
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
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

    @Override
    public void onBackPressed() {
        if (!isRunning)
            finish();
    }

    public void setStatus(boolean status) {
        isRunning = status;
    }
}
