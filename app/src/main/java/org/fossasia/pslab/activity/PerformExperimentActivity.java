package org.fossasia.pslab.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.gigamole.navigationtabstrip.NavigationTabStrip;

import org.fossasia.pslab.R;
import org.fossasia.pslab.adapters.PerformExperimentAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by viveksb007 on 10/7/17.
 */

public class PerformExperimentActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.saved_experiment_vpager)
    ViewPager viewPager;
    @BindView(R.id.perform_experiment_tab_strip)
    NavigationTabStrip tabStrip;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String toolbarTitle = getIntent().getStringExtra("toolbar_title");
        String experimentTitle = getIntent().getStringExtra("experiment_title");
        setContentView(R.layout.activity_perform_experiment);
        ButterKnife.bind(this);
        toolbar.setTitle(toolbarTitle);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        viewPager.setAdapter(new PerformExperimentAdapter(getSupportFragmentManager(),experimentTitle,this));
        tabStrip.setViewPager(viewPager);
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
