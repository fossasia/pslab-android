package io.pslab.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.pslab.R;
import io.pslab.adapters.SensorLoggerListAdapter;
import io.pslab.models.SensorLogged;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by Avjeet on 05/08/18.
 */

public class DataLoggerActivity extends AppCompatActivity {
    public static final String CALLER_ACTIVITY = "Caller";
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_logger);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        Realm realm = Realm.getDefaultInstance();
        String caller = getIntent().getStringExtra(CALLER_ACTIVITY);
        if (caller == null)
            caller = "";

        RealmResults<SensorLogged> results;
        String title;
        switch (caller) {
            case "Lux Meter":
                results = realm.where(SensorLogged.class).equalTo("sensor", caller)
                        .findAll()
                        .sort("dateTimeStart", Sort.DESCENDING);
                title = caller + " Data";
                break;
            default:
                results = realm.where(SensorLogged.class)
                        .findAll()
                        .sort("dateTimeStart", Sort.DESCENDING);
                title = getString(R.string.logged_data);
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(title);
        }
        SensorLoggerListAdapter adapter = new SensorLoggerListAdapter(results, this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        DividerItemDecoration itemDecor = new DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL);
        recyclerView.addItemDecoration(itemDecor);
        recyclerView.setAdapter(adapter);
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
