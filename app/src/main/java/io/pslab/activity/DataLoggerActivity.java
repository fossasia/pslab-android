package io.pslab.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.pslab.R;
import io.pslab.adapters.SensorLoggerListAdapter;
import io.pslab.models.SensorDataBlock;
import io.pslab.others.CSVLogger;
import io.pslab.others.LocalDataLog;
import io.realm.Realm;
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

    private ProgressBar deleteAllProgressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_logger);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        deleteAllProgressBar = findViewById(R.id.delete_all_progbar);
        deleteAllProgressBar.setVisibility(View.GONE);
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
        if (item.getItemId() == R.id.delete_all){
            Context context = DataLoggerActivity.this;
            new AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.delete))
                    .setMessage(context.getString(R.string.delete_all_message))
                    .setPositiveButton(context.getString(R.string.delete), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteAllProgressBar.setVisibility(View.VISIBLE);
                            new DeleteAllTask().execute();
                        }
            }).setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).create().show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.log_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    private class DeleteAllTask extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... voids) {
            Realm realm = Realm.getDefaultInstance();
            for (SensorDataBlock data : realm.where(SensorDataBlock.class)
                    .findAll()){
                File logDirectory = new File(
                        Environment.getExternalStorageDirectory().getAbsolutePath() +
                                File.separator + CSVLogger.CSV_DIRECTORY +
                                File.separator + data.getSensorType() +
                                File.separator + CSVLogger.FILE_NAME_FORMAT.format(data.getBlock()) + ".csv");
                if (logDirectory.delete()){
                    realm.beginTransaction();
                    realm.where(SensorDataBlock.class)
                            .equalTo("block", data.getBlock())
                            .findFirst().deleteFromRealm();
                    realm.commitTransaction();
                }
            }
            realm.close();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            deleteAllProgressBar.setVisibility(View.GONE);
            if (LocalDataLog.with().getAllSensorBlocks().size() <= 0) {
                blankView.setVisibility(View.VISIBLE);
            }
        }
    }
}
