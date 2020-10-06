package io.pslab.activity;

import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;

import io.pslab.R;
import io.pslab.fragment.BaroMeterDataFragment;
import io.pslab.fragment.BaroMeterSettingsFragment;
import io.pslab.models.BaroData;
import io.pslab.models.PSLabSensor;
import io.pslab.models.SensorDataBlock;
import io.pslab.others.LocalDataLog;
import io.realm.RealmObject;
import io.realm.RealmResults;

/**
 * Created by Padmal on 12/13/18.
 */

public class BarometerActivity extends PSLabSensor {

    private static final String PREF_NAME = "customDialogPreference";
    public final String BAROMETER_LIMIT = "barometer_limit";
    public RealmResults<BaroData> recordedBaroData;

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
        return "BaroMeterFirstTime";
    }

    @Override
    public String getSensorName() {
        return getResources().getString(R.string.baro_meter);
    }

    @Override
    public int getGuideTitle() {
        return R.string.baro_meter;
    }

    @Override
    public int getGuideAbstract() {
        return R.string.baro_meter_intro;
    }

    @Override
    public int getGuideSchematics() {
        return R.drawable.bmp180_schematic;
    }

    @Override
    public int getGuideDescription() {
        return R.string.baro_meter_desc;
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
        realm.copyToRealm((BaroData) sensorData);
        realm.commitTransaction();
    }

    @Override
    public void stopRecordSensorData() {
        LocalDataLog.with().refresh();
    }

    @Override
    public Fragment getSensorFragment() {
        return BaroMeterDataFragment.newInstance();
    }

    @Override
    public void getDataFromDataLogger() {
        if (getIntent().getExtras() != null && getIntent().getExtras().getBoolean(KEY_LOG)) {
            viewingData = true;
            recordedBaroData = LocalDataLog.with()
                    .getBlockOfBaroRecords(getIntent().getExtras().getLong(DATA_BLOCK));
            String title = titleFormat.format(recordedBaroData.get(0).getTime());
            getSupportActionBar().setTitle(title);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        reinstateConfigurations();
    }

    private void reinstateConfigurations() {
        SharedPreferences BaroMeterConfigurations;
        BaroMeterConfigurations = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        locationEnabled = BaroMeterConfigurations.getBoolean(BaroMeterSettingsFragment.KEY_INCLUDE_LOCATION, true);
        BaroMeterDataFragment.setParameters(
                Float.valueOf(BaroMeterConfigurations.getString(BaroMeterSettingsFragment.KEY_HIGH_LIMIT, "1.1")),
                Integer.valueOf(BaroMeterConfigurations.getString(BaroMeterSettingsFragment.KEY_UPDATE_PERIOD, "1000")),
                BaroMeterConfigurations.getString(BaroMeterSettingsFragment.KEY_BARO_SENSOR_TYPE, "0"));
    }
}
