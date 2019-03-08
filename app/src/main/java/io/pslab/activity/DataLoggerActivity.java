package io.pslab.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.pslab.R;
import io.pslab.adapters.SensorLoggerListAdapter;
import io.pslab.models.SensorDataBlock;
import io.pslab.others.LocalDataLog;
import io.realm.RealmResults;

/**
 * Created by Avjeet on 05/08/18.
 */

public class DataLoggerActivity extends AppCompatActivity {

    public static final String CALLER_ACTIVITY = "Caller";

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.data_logger_blank_view)
    TextView blankView;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_logger);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        String caller = getIntent().getStringExtra(CALLER_ACTIVITY);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        if (caller == null) caller = "";

        RealmResults<SensorDataBlock> categoryData;

        switch (caller) {
            case "Lux Meter":
                getSupportActionBar().setTitle(caller);
                categoryData = LocalDataLog.with().getTypeOfSensorBlocks(getString(R.string.lux_meter));
                break;
            case "Baro Meter":
                getSupportActionBar().setTitle(caller);
                categoryData = LocalDataLog.with().getTypeOfSensorBlocks(getString(R.string.baro_meter));
                break;
            case "Multimeter":
                getSupportActionBar().setTitle(caller);
                categoryData = LocalDataLog.with().getTypeOfSensorBlocks(getString(R.string.multimeter));
                break;
            default:
                categoryData = LocalDataLog.with().getAllSensorBlocks();
                getSupportActionBar().setTitle(getString(R.string.logged_data));
        }

        if (categoryData.size() > 0) {
            blankView.setVisibility(View.GONE);
            SensorLoggerListAdapter adapter = new SensorLoggerListAdapter(categoryData, this);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(
                    this, LinearLayoutManager.VERTICAL, false);
            recyclerView.setLayoutManager(linearLayoutManager);

            DividerItemDecoration itemDecor = new DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL);
            recyclerView.addItemDecoration(itemDecor);
            recyclerView.setAdapter(adapter);
        } else {
            recyclerView.setVisibility(View.GONE);
            blankView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
