package io.pslab.activity;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;

import io.pslab.R;
import io.pslab.fragment.DustSensorDataFragment;
import io.pslab.fragment.DustSensorSettingsFragment;
import io.pslab.models.DustSensorData;
import io.pslab.models.PSLabSensor;
import io.pslab.models.SensorDataBlock;
import io.pslab.others.LocalDataLog;
import io.realm.RealmObject;
import io.realm.RealmResults;

public class DustSensorActivity extends PSLabSensor {

    private static final String PREF_NAME = "customDialogPreference";
    public RealmResults<DustSensorData> recordedDustSensorData;

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
        return "DustSensorFirstTime";
    }

    @Override
    public String getSensorName() {
        return getResources().getString(R.string.dust_sensor);
    }

    @Override
    public int getGuideTitle() {
        return R.string.dust_sensor;
    }

    @Override
    public int getGuideAbstract() {
        return R.string.dust_sensor_intro;
    }

    @Override
    public int getGuideSchematics() {
        return 0;
    }

    @Override
    public int getGuideDescription() {
        return R.string.dust_sensor_desc;
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
        realm.copyToRealm((DustSensorData) sensorData);
        realm.commitTransaction();
    }

    @Override
    public void stopRecordSensorData() {
        LocalDataLog.with().refresh();
    }

    @Override
    public Fragment getSensorFragment() {
        return DustSensorDataFragment.newInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();
        reinstateConfigurations();
    }

    private void reinstateConfigurations() {
        SharedPreferences luxMeterConfigurations;
        luxMeterConfigurations = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        locationEnabled = luxMeterConfigurations.getBoolean(DustSensorSettingsFragment.KEY_INCLUDE_LOCATION, true);
        DustSensorDataFragment.setParameters(
                getValueFromText(luxMeterConfigurations.getString(DustSensorSettingsFragment.KEY_HIGH_LIMIT, "4.0"),
                        0.0, 5.0),
                getValueFromText(luxMeterConfigurations.getString(DustSensorSettingsFragment.KEY_UPDATE_PERIOD, "1000"),
                        100, 1000),
                luxMeterConfigurations.getString(DustSensorSettingsFragment.KEY_DUST_SENSOR_TYPE, "0"));
    }

    private int getValueFromText(String strValue, int lowerBound, int upperBound) {
        if (strValue.isEmpty()) return lowerBound;
        int value = Integer.parseInt(strValue);
        if (value > upperBound) return upperBound;
        else if (value < lowerBound) return lowerBound;
        else return value;
    }

    private double getValueFromText(String strValue, double lowerBound, double upperBound) {
        if (strValue.isEmpty()) return lowerBound;
        double value = Double.parseDouble(strValue);
        if (value > upperBound) return upperBound;
        else if (value < lowerBound) return lowerBound;
        else return value;
    }

    @Override
    public void getDataFromDataLogger() {
        if (getIntent().getExtras() != null && getIntent().getExtras().getBoolean(KEY_LOG)) {
            viewingData = true;
            recordedDustSensorData = LocalDataLog.with()
                    .getBlockOfDustSensorRecords(getIntent().getExtras().getLong(DATA_BLOCK));
            final DustSensorData data = recordedDustSensorData.get(0);
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
