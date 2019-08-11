package io.pslab.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
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
import io.pslab.models.AccelerometerData;
import io.pslab.models.BaroData;
import io.pslab.models.CompassData;
import io.pslab.models.GasSensorData;
import io.pslab.models.GyroData;
import io.pslab.models.LogicAnalyzerData;
import io.pslab.models.LuxData;
import io.pslab.models.OscilloscopeData;
import io.pslab.models.PowerSourceData;
import io.pslab.models.SensorDataBlock;
import io.pslab.models.ServoData;
import io.pslab.models.ThermometerData;
import io.pslab.models.WaveGeneratorData;
import io.pslab.others.CSVLogger;
import io.pslab.others.LocalDataLog;
import io.realm.Realm;
import io.realm.RealmObject;
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
    private RealmResults<SensorDataBlock> categoryData;
    private String selectedDevice = null;
    private Realm realm;
    private String caller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_logger);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        deleteAllProgressBar = findViewById(R.id.delete_all_progbar);
        deleteAllProgressBar.setVisibility(View.GONE);
        realm = LocalDataLog.with().getRealm();
        caller = getIntent().getStringExtra(CALLER_ACTIVITY);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        if (caller == null) caller = "";

        getSupportActionBar().setTitle(caller);
        switch (caller) {
            case "Lux Meter":
                categoryData = LocalDataLog.with().getTypeOfSensorBlocks(getString(R.string.lux_meter));
                break;
            case "Barometer":
                categoryData = LocalDataLog.with().getTypeOfSensorBlocks(getString(R.string.baro_meter));
                break;
            case "Accelerometer":
                categoryData = LocalDataLog.with().getTypeOfSensorBlocks(getString(R.string.accelerometer));
                break;
            case "Multimeter":
                categoryData = LocalDataLog.with().getTypeOfSensorBlocks(getString(R.string.multimeter));
                break;
            case "Gyroscope":
                categoryData = LocalDataLog.with().getTypeOfSensorBlocks(getString(R.string.gyroscope));
                break;
            case "Compass":
                categoryData = LocalDataLog.with().getTypeOfSensorBlocks(getString(R.string.compass));
                break;
            case "Thermometer":
                categoryData = LocalDataLog.with().getTypeOfSensorBlocks(getString(R.string.thermometer));
                break;
            case "Robotic Arm":
                categoryData = LocalDataLog.with().getTypeOfSensorBlocks(getString(R.string.robotic_arm));
                break;
            case "Wave Generator":
                categoryData = LocalDataLog.with().getTypeOfSensorBlocks(getResources().getString(R.string.wave_generator));
                break;
            case "Oscilloscope":
                categoryData = LocalDataLog.with().getTypeOfSensorBlocks(getResources().getString(R.string.oscilloscope));
                break;
            case "Power Source":
                categoryData = LocalDataLog.with().getTypeOfSensorBlocks(getResources().getString(R.string.power_source));
                break;
            case "Logic Analyzer":
                categoryData = LocalDataLog.with().getTypeOfSensorBlocks(getResources().getString(R.string.logical_analyzer));
                break;
            case "Gas Sensor":
                categoryData = LocalDataLog.with().getTypeOfSensorBlocks(getString(R.string.gas_sensor));
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
                break;
            case R.id.action_import_log:
                selectFile();
                break;
            case R.id.delete_all:
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
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.log_activity_menu, menu);
        menu.findItem(R.id.delete_all).setVisible(categoryData.size() > 0);
        return super.onCreateOptionsMenu(menu);
    }

    private void selectFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                String path = uri.getPath();
                path = path.replace("/root_path/", "/");
                File file = new File(path);
                getFileData(file);
            } else
                Toast.makeText(this, this.getResources().getString(R.string.no_file_selected), Toast.LENGTH_SHORT).show();
        }
    }

    private void getFileData(File file) {
        try {
            FileInputStream is = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = reader.readLine();
            int i = 0;
            long block = 0, time = 0;
            while (line != null) {
                if (i > 1) {
                    String[] data = line.split(",");
                    try {
                        time += 1000;
                        realm.beginTransaction();
                        RealmObject object = getObject(selectedDevice, data, time, block);
                        if (object != null) {
                            realm.copyToRealm(object);
                        } else {
                            Toast.makeText(this, getResources().getString(R.string.incorrect_import_format), Toast.LENGTH_SHORT).show();
                        }
                        realm.commitTransaction();
                    } catch (Exception e) {
                        Toast.makeText(this, getResources().getString(R.string.incorrect_import_format), Toast.LENGTH_SHORT).show();
                    }
                } else if (i == 0) {
                    block = System.currentTimeMillis();
                    time = block;
                    selectedDevice = line.split(",")[0];
                    realm.beginTransaction();
                    realm.copyToRealm(new SensorDataBlock(block, selectedDevice));
                    realm.commitTransaction();
                }
                i++;
                line = reader.readLine();
            }
            fillData();
            DataLoggerActivity.this.toolbar.getMenu().findItem(R.id.delete_all).setVisible(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private RealmObject getObject(String objectType, String[] data, long time, long block) {
        RealmObject returnObject = null;
        switch (objectType) {
            case "Lux Meter":
                returnObject = new LuxData(time, block, Float.valueOf(data[2]), Double.valueOf(data[3]), Double.valueOf(data[4]));
                break;
            case "Barometer":
                returnObject = new BaroData(time, block, Float.valueOf(data[2]), Double.valueOf(data[3]), Double.valueOf(data[4]));
                break;
            case "Accelerometer":
                returnObject = new AccelerometerData(time, block, Float.valueOf(data[2]), Float.valueOf(data[3]), Float.valueOf(data[4]), Double.valueOf(data[5]), Double.valueOf(data[6]));
                break;
            case "Gyroscope":
                returnObject = new GyroData(time, block, Float.valueOf(data[2]), Float.valueOf(data[3]), Float.valueOf(data[4]), Double.valueOf(data[5]), Double.valueOf(data[6]));
                break;
            case "Compass":
                returnObject = new CompassData(time, block, data[2].equals("null") ? "0" : data[2], data[3].equals("null") ? "0" : data[3], data[4].equals("null") ? "0" : data[4], data[5], Double.valueOf(data[6]), Double.valueOf(data[7]));
                break;
            case "Thermometer":
                returnObject = new ThermometerData(time, block, Float.valueOf(data[2]), Double.valueOf(data[5]), Double.valueOf(data[6]));
                break;
            case "Robotic Arm":
                returnObject = new ServoData(time, block, data[2], data[3], data[4], data[5], Float.valueOf(data[6]), Float.valueOf(data[7]));
                break;
            case "Wave Generator":
                returnObject = new WaveGeneratorData(time, block, data[2], data[3], data[4], data[5], data[6], data[7], Float.valueOf(data[8]), Float.valueOf(data[9]));
                break;
            case "Oscilloscope":
                returnObject = new OscilloscopeData(time, block, Integer.valueOf(data[2]), data[3], data[4], data[5], Float.valueOf(data[6]), Float.valueOf(data[7]), Float.valueOf(data[8]));
                break;
            case "Power Source":
                returnObject = new PowerSourceData(time, block, Float.valueOf(data[2]), Float.valueOf(data[3]), Float.valueOf(data[4]), Float.valueOf(data[5]), Float.valueOf(data[6]), Float.valueOf(data[7]));
                break;
            case "Logic Analyzer":
                returnObject = new LogicAnalyzerData(time, block, data[2], Integer.valueOf(data[3]), data[4], data[5], Float.valueOf(data[6]), Float.valueOf(data[7]));
                break;
            case "Gas Sensor":
                returnObject = new GasSensorData(time, block, Float.valueOf(data[2]), Double.valueOf(data[3]), Double.valueOf(data[4]));
                break;
            default:
                returnObject = null;
                break;
        }
        return returnObject;
    }

    private class DeleteAllTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            Realm realm = Realm.getDefaultInstance();
            for (SensorDataBlock data : realm.where(SensorDataBlock.class)
                    .findAll()) {
                File logDirectory = new File(
                        Environment.getExternalStorageDirectory().getAbsolutePath() +
                                File.separator + CSVLogger.CSV_DIRECTORY +
                                File.separator + data.getSensorType() +
                                File.separator + CSVLogger.FILE_NAME_FORMAT.format(data.getBlock()) + ".csv");
                logDirectory.delete();
                realm.beginTransaction();
                realm.where(SensorDataBlock.class)
                        .equalTo("block", data.getBlock())
                        .findFirst().deleteFromRealm();
                realm.commitTransaction();
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
            DataLoggerActivity.this.toolbar.getMenu().findItem(R.id.delete_all).setVisible(false);
        }
    }
}
