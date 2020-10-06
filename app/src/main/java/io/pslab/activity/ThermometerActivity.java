package io.pslab.activity;

import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;

import io.pslab.R;
import io.pslab.fragment.ThermometerDataFragment;
import io.pslab.fragment.ThermometerSettingsFragment;
import io.pslab.models.PSLabSensor;
import io.pslab.models.SensorDataBlock;
import io.pslab.models.ThermometerData;
import io.pslab.others.LocalDataLog;
import io.realm.RealmObject;
import io.realm.RealmResults;

public class ThermometerActivity extends PSLabSensor {

    private static final String PREF_NAME = "customDialogPreference";
    public RealmResults<ThermometerData> recordedThermometerData;
    public final String THERMOMETER_MAX_LIMIT = "thermometer_max_limit";
    public final String THERMOMETER_MIN_LIMIT = "thermometer_min_limit";

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
        return "ThermometerFirstTIme";
    }

    @Override
    public String getSensorName() {
        return getResources().getString(R.string.thermometer);
    }

    @Override
    public int getGuideTitle() {
        return R.string.thermometer;
    }

    @Override
    public int getGuideAbstract() {
        return R.string.thermometer_bottom_sheet_text;
    }

    @Override
    public int getGuideSchematics() {
        return 0;
    }

    @Override
    public int getGuideDescription() {
        return R.string.thermometer_bottom_sheet_desc;
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
        realm.copyToRealm((ThermometerData) sensorData);
        realm.commitTransaction();
    }

    @Override
    public void stopRecordSensorData() {
        LocalDataLog.with().refresh();
    }

    @Override
    public Fragment getSensorFragment() {
        return ThermometerDataFragment.newInstance();
    }

    @Override
    public void getDataFromDataLogger() {
        if (getIntent().getExtras() != null && getIntent().getExtras().getBoolean(KEY_LOG)) {
            viewingData = true;
            recordedThermometerData = LocalDataLog.with()
                    .getBlockOfThermometerRecords(getIntent().getExtras().getLong(DATA_BLOCK));
            String title = titleFormat.format(recordedThermometerData.get(0).getTime());
            getSupportActionBar().setTitle(title);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        reinstateConfigurations();
    }

    private void reinstateConfigurations() {
        SharedPreferences thermometerConfigurations;
        thermometerConfigurations = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        locationEnabled = thermometerConfigurations.getBoolean(ThermometerSettingsFragment.KEY_INCLUDE_LOCATION, true);
        ThermometerDataFragment.setParameters(
                getValueFromText(thermometerConfigurations.getString(ThermometerSettingsFragment.KEY_UPDATE_PERIOD, "1000"),
                        100, 1000),
                thermometerConfigurations.getString(ThermometerSettingsFragment.KEY_THERMO_SENSOR_TYPE, "0"),
                thermometerConfigurations.getString(ThermometerSettingsFragment.KEY_THERMO_UNIT, "°C"));
    }

    private int getValueFromText(String strValue, int lowerBound, int upperBound) {
        if (strValue.isEmpty()) return lowerBound;
        int value = Integer.parseInt(strValue);
        if (value > upperBound) return upperBound;
        else if (value < lowerBound) return lowerBound;
        else return value;
    }
}
