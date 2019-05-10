package io.pslab.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.pslab.R;
import io.pslab.adapters.SensorLoggerListAdapter;
import io.pslab.models.BaroData;
import io.pslab.models.LuxData;
import io.pslab.models.SensorDataBlock;
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

    private String selectedDevice = null;
    private Realm realm;
    private RealmResults<SensorDataBlock> categoryData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_logger);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        realm = LocalDataLog.with().getRealm();
        String caller = getIntent().getStringExtra(CALLER_ACTIVITY);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        if (caller == null) caller = "";


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
        fillData();
    }

    private void fillData() {
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
            case R.id.action_import_log:
                importLog();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_data_logger_menu, menu);
        return true;
    }

    private void importLog() {
        AlertDialog alertDialog;
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.import_log_device_type_alert_layout, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setPositiveButton(getResources().getString(R.string.import_log_positive_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                RadioGroup importLogRadioGroup = dialogView.findViewById(R.id.import_log_device_radio_group);
                try {
                    RadioButton selectedRadioButton = dialogView.findViewById(importLogRadioGroup.getCheckedRadioButtonId());
                    selectedDevice = selectedRadioButton.getText().toString();
                    selectFile();
                } catch (Exception e) {
                    Toast.makeText(DataLoggerActivity.this, getResources().getString(R.string.import_data_log_no_selection_error), Toast.LENGTH_SHORT).show();
                }
            }
        });
        alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    private void selectFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 100) {
            Uri uri = data.getData();
            String path = uri.getPath();
            path = path.replace("/root_path/", "/");
            File file = new File(path);
            getFileData(file);
        }
    }

    private void getFileData(File file) {
        if (selectedDevice != null && selectedDevice.equals(getResources().getString(R.string.baro_meter))) {
            try {
                FileInputStream is = new FileInputStream(file);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line = reader.readLine();
                int i = 0;
                long block = 0, time = 0;
                while (line != null) {
                    if (i != 0) {
                        String[] data = line.split(",");
                        try {
                            time += 1000;
                            BaroData baroData = new BaroData(time, block, Float.valueOf(data[2]), Double.valueOf(data[3]), Double.valueOf(data[4]));
                            realm.beginTransaction();
                            realm.copyToRealm(baroData);
                            realm.commitTransaction();
                        } catch (Exception e) {
                            Toast.makeText(this, getResources().getString(R.string.incorrect_import_format), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        block = System.currentTimeMillis();
                        time = block;
                        realm.beginTransaction();
                        realm.copyToRealm(new SensorDataBlock(block, getResources().getString(R.string.baro_meter)));
                        realm.commitTransaction();
                    }
                    i++;
                    line = reader.readLine();
                }
                fillData();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (selectedDevice != null && selectedDevice.equals(getResources().getString(R.string.lux_meter))) {
            try {
                FileInputStream is = new FileInputStream(file);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line = reader.readLine();
                int i = 0;
                long block = 0, time = 0;
                while (line != null) {
                    if (i != 0) {
                        String[] data = line.split(",");
                        try {
                            time += 1000;
                            LuxData luxData = new LuxData(time, block, Float.valueOf(data[2]), Double.valueOf(data[3]), Double.valueOf(data[4]));
                            realm.beginTransaction();
                            realm.copyToRealm(luxData);
                            realm.commitTransaction();
                        } catch (Exception e) {
                            Toast.makeText(this, getResources().getString(R.string.incorrect_import_format), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        block = System.currentTimeMillis();
                        time = block;
                        realm.beginTransaction();
                        realm.copyToRealm(new SensorDataBlock(block, getResources().getString(R.string.lux_meter)));
                        realm.commitTransaction();
                    }
                    i++;
                    line = reader.readLine();
                }
                fillData();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
