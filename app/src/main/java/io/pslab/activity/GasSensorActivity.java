package io.pslab.activity;

import android.content.SharedPreferences;

import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;

import io.pslab.R;
import io.pslab.fragment.GasSensorDataFragment;
import io.pslab.models.GasSensorData;
import io.pslab.models.PSLabSensor;
import io.pslab.models.SensorDataBlock;
import io.pslab.others.LocalDataLog;
import io.realm.RealmObject;
import io.realm.RealmResults;

public class GasSensorActivity extends PSLabSensor {

    private static final String PREF_NAME = "customDialogPreference";
    public RealmResults<GasSensorData> recordedGasSensorData;

    @Override
    public int getMenu() {
        return R.menu.sensor_data_log_menu;
    }

    @Override
    public SharedPreferences getStateSettings() {
        return this.getSharedPreferences(PREF_NAME, MODE_PRIVATE);

    }

    @Override
    public String getFirstTimeSettingID() {
        return "GasSensorFirstTime";
    }

    @Override
    public String getSensorName() {
        return getResources().getString(R.string.gas_sensor);
    }

    @Override
    public int getGuideTitle() {
        return R.string.gas_sensor;
    }

    @Override
    public int getGuideAbstract() {
        return R.string.gas_sensor;
    }

    @Override
    public int getGuideSchematics() {
        return R.drawable.bmp180_schematic;
    }

    @Override
    public int getGuideDescription() {
        return R.string.gas_sensor;
    }

    @Override
    public int getGuideExtraContent() {
        return 0;
    }

    @Override
    public void recordSensorDataBlockID(SensorDataBlock block) {
        realm.beginTransaction();
        realm.copyToRealm(block);
        realm.commitTransaction();
    }

    @Override
    public void recordSensorData(RealmObject sensorData) {
        realm.beginTransaction();
        realm.copyToRealm((GasSensorData) sensorData);
        realm.commitTransaction();
    }

    @Override
    public void stopRecordSensorData() {
        LocalDataLog.with().refresh();
    }

    @Override
    public Fragment getSensorFragment() {
        return GasSensorDataFragment.newInstance();
    }

    @Override
    public void getDataFromDataLogger() {
        if (getIntent().getExtras() != null && getIntent().getExtras().getBoolean(KEY_LOG)) {
            viewingData = true;
            recordedGasSensorData = LocalDataLog.with()
                    .getBlockOfGasSensorRecords(getIntent().getExtras().getLong(DATA_BLOCK));
            final GasSensorData data = recordedGasSensorData.get(0);
            if (data != null) {
                final String title = titleFormat.format(data.getTime());
                final ActionBar actionBar = getSupportActionBar();
                if (actionBar != null) {
                    actionBar.setTitle(title);
                }
            }
        }
    }

    @Override
    public boolean sensorFound() {
        return false;
    }
}
