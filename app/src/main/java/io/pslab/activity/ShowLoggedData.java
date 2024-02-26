package io.pslab.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.pslab.R;
import io.pslab.adapters.MPUDataAdapter;
import io.pslab.models.DataMPU6050;
import io.pslab.models.SensorLogged;
import io.pslab.others.CustomSnackBar;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by viveksb007 on 12/8/17.
 * deprecated
 */

public class ShowLoggedData extends AppCompatActivity {


    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.layout_container)
    LinearLayout linearLayout;

    private Realm realm;
    private Context context;
    private ListView sensorListView;
    private ListView trialListView;
    private RecyclerView recyclerView;
    private String mSensor;
    private String format;
    private boolean isRecyclerViewOnStack = false;
    private boolean isTrialListViewOnStack = false;
    private boolean isSensorListViewOnStack = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_logged_data);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        realm = Realm.getDefaultInstance();
        context = this;
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getResources().getString(R.string.sensor_logged_data));
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String formatValue = preferences.getString("export_data_format_list", "0");
        if ("0".equals(formatValue))
            format = "txt";
        else
            format = "csv";

        showSensorList();
    }

    private void showSensorList() {
        sensorListView = new ListView(this);
        linearLayout.addView(sensorListView);
        isSensorListViewOnStack = true;
        RealmResults<SensorLogged> results = realm.where(SensorLogged.class).findAll();
        ArrayList<String> sensorList = new ArrayList<>();
        if (results != null) {
            for (SensorLogged temp : results) {
                sensorList.add(temp.getSensor());
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                sensorList);
        sensorListView.setAdapter(adapter);
        sensorListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String sensor = ((TextView) view).getText().toString();
                mSensor = sensor;
                showSensorTrialData(sensor);
            }
        });

        sensorListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final String sensor = ((TextView) view).getText().toString();
                final MaterialDialog dialog = new MaterialDialog.Builder(context)
                        .title(sensor)
                        .customView(R.layout.sensor_list_long_click_dailog, false)
                        .build();
                dialog.show();
                View customView = dialog.getCustomView();
                assert customView != null;
                ListView clickOptions = customView.findViewById(R.id.lv_sensor_list_click);
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.sensor_click_list));
                clickOptions.setAdapter(arrayAdapter);

                clickOptions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        switch (position) {
                            case 0:
                                // todo : check for permission first
                                exportCompleteSensorData(sensor);
                                break;
                            case 1:
                                break;
                        }
                        dialog.dismiss();
                    }
                });
                return true;
            }
        });
    }

    private void exportCompleteSensorData(String sensor) {
        File folder = new File(Environment.getExternalStorageDirectory() + File.separator + "PSLab Android");
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdir();
        }
        if (success) {
            if ("txt".equals(format)) {
                FileOutputStream stream = null;
                File file;
                switch (sensor) {
                    case "MPU6050":
                        file = new File(folder, "MPU6050_" + System.currentTimeMillis() + ".txt");
                        RealmResults<DataMPU6050> results = realm.where(DataMPU6050.class).findAll();
                        try {
                            stream = new FileOutputStream(file);
                            for (DataMPU6050 temp : results) {
                                stream.write((temp.getAx() + " " + temp.getAy() + " " + temp.getAz() + " " +
                                        temp.getGx() + " " + temp.getGy() + " " + temp.getGz() + " " + temp.getTemperature() + "\n").getBytes());
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                if (stream != null) {
                                    stream.close();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        CustomSnackBar.showSnackBar(findViewById(android.R.id.content),
                                "MPU6050 data exported successfully", null, null, Snackbar.LENGTH_SHORT);
                        break;
                }
            } else {
                File file;
                PrintWriter writer;
                switch (sensor) {
                    case "MPU6050":
                        file = new File(folder, "MPU6050_" + System.currentTimeMillis() + ".csv");
                        RealmResults<DataMPU6050> results = realm.where(DataMPU6050.class).findAll();
                        try {
                            writer = new PrintWriter(file);
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append("Ax,Ay,Ax,Gx,Gy,Gz,Temperature\n");
                            for (DataMPU6050 temp : results) {
                                stringBuilder.append(temp.getAx());
                                stringBuilder.append(',');
                                stringBuilder.append(temp.getAy());
                                stringBuilder.append(',');
                                stringBuilder.append(temp.getAz());
                                stringBuilder.append(',');
                                stringBuilder.append(temp.getGx());
                                stringBuilder.append(',');
                                stringBuilder.append(temp.getGy());
                                stringBuilder.append(',');
                                stringBuilder.append(temp.getGz());
                                stringBuilder.append(',');
                                stringBuilder.append(temp.getTemperature());
                                stringBuilder.append('\n');
                            }
                            writer.write(stringBuilder.toString());
                            writer.close();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        CustomSnackBar.showSnackBar(findViewById(android.R.id.content),
                                "MPU6050 data exported successfully", null, null, Snackbar.LENGTH_SHORT);
                        break;
                }
            }
        } else {
            CustomSnackBar.showSnackBar(findViewById(android.R.id.content),
                    "Can't write to storage", null, null, Snackbar.LENGTH_SHORT);
        }
    }

    private void showSensorTrialData(final String sensor) {
        Number trial;
        ArrayList<String> trialList = new ArrayList<>();

        switch (sensor) {
            case "MPU6050":
                trial = realm.where(DataMPU6050.class).max("trial");
                if (trial == null) return;
                long maxTrials = (long) trial + 1;
                for (int i = 0; i < maxTrials; i++) {
                    trialList.add("Trial #" + (i + 1));
                }
                break;
            default:
                // Todo : Add cases for other sensor
        }

        linearLayout.removeView(sensorListView);
        isSensorListViewOnStack = false;
        trialListView = new ListView(context);
        linearLayout.addView(trialListView);
        isTrialListViewOnStack = true;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, trialList);
        trialListView.setAdapter(adapter);
        trialListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                populateSensorData(sensor, position);
            }
        });
    }

    private void populateSensorData(String sensor, long trial) {
        linearLayout.removeView(trialListView);
        isTrialListViewOnStack = false;
        recyclerView = new RecyclerView(this);
        linearLayout.addView(recyclerView);
        isRecyclerViewOnStack = true;

        switch (sensor) {
            case "MPU6050":
                RealmResults<DataMPU6050> queryResults = realm.where(DataMPU6050.class).equalTo("trial", trial).findAll();
                MPUDataAdapter mpuDataAdapter = new MPUDataAdapter(queryResults);
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.setAdapter(mpuDataAdapter);
                break;
            default:
                // Todo : Add other cases
        }

    }

    @Override
    public void onBackPressed() {
        if (isRecyclerViewOnStack) {
            linearLayout.removeView(recyclerView);
            isRecyclerViewOnStack = false;
            showSensorTrialData(mSensor);
            return;
        } else if (isTrialListViewOnStack) {
            linearLayout.removeView(trialListView);
            isTrialListViewOnStack = false;
            showSensorList();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_show_item_logged_data, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.export_logged_data:
                // Exporting locally logged data
                break;
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
