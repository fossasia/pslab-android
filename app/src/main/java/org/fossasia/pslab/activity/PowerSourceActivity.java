package org.fossasia.pslab.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import org.fossasia.pslab.R;
import org.fossasia.pslab.adapters.ControlMainAdapter;

/**
 * Created by Abhinav Raj on 1/6/18.
 */

public class PowerSourceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_power_source);

        ControlMainAdapter mAdapter = new ControlMainAdapter(new String[]{"PV1", "PV2", "PV3", "PCS", "WAVE 1", "WAVE 2", "SQUARE"});
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.control_main_recycler_view);
        mRecyclerView.setHasFixedSize(false);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
