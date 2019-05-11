package io.pslab.activity;

import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import io.pslab.R;
import io.pslab.fragment.AccelerometerDataFragment;
import io.pslab.fragment.AccelerometerSettingsFragment;
import io.pslab.models.AccelerometerData;
import io.pslab.models.PSLabSensor;
import io.pslab.models.SensorDataBlock;
import io.pslab.others.LocalDataLog;
import io.realm.RealmObject;
import io.realm.RealmResults;

public class AccelerometerActivity extends PSLabSensor {

    private static final String PREF_NAME = "customDialogPreference";
    public final String ACCELEROMETER_LIMIT = "accelerometer_limit";
    public RealmResults<AccelerometerData> recordedAccelerometerData;

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
        return "AccelerometerFirstTime";
    }

    @Override
    public String getSensorName() {
        return getResources().getString(R.string.accelerometer);
    }

    @Override
    public int getGuideTitle() {
        return R.string.accelerometer;
    }

    @Override
    public int getGuideAbstract() {
        return R.string.accelerometer_intro;
    }


    @Override
    public int getGuideSchematics() {
        return R.drawable.bh1750_schematic;
    }

    @Override
    public int getGuideDescription() {
        return R.string.accelerometer_description_text;
    }

    @Override
    public int getGuideExtraContent() {
        return 0;
    }

    @Override
    public void recordSensorDataBlockID(SensorDataBlock categoryData) {
        realm.beginTransaction();
        realm.copyToRealm(categoryData);
        realm.commitTransaction();
    }

    @Override
    public void recordSensorData(RealmObject sensorData) {
        realm.beginTransaction();
        realm.copyToRealm((AccelerometerData) sensorData);
        realm.commitTransaction();
    }

    @Override
    public void stopRecordSensorData() {
        LocalDataLog.with().refresh();
    }

    @Override
    public Fragment getSensorFragment() {
        return AccelerometerDataFragment.newInstance();
    }

    @Override
    public void getDataFromDataLogger() {
        if (getIntent().getExtras() != null && getIntent().getExtras().getBoolean(KEY_LOG)) {
            //playingData = true;
            viewingData = true;
            recordedAccelerometerData = LocalDataLog.with()
                    .getBlockOfAccelerometerRecords(getIntent().getExtras().getLong(DATA_BLOCK));
            String title = titleFormat.format(recordedAccelerometerData.get(0).getTime());
            getSupportActionBar().setTitle(title);
        }
    }

    /**
     * Once settings have been changed, those changes can be captured from onResume method.
     */
    @Override
    protected void onResume() {
        super.onResume();
        reinstateConfigurations();
    }

    private void reinstateConfigurations() {
        SharedPreferences accelerometerConfigurations;
        accelerometerConfigurations = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        locationEnabled = accelerometerConfigurations.getBoolean(AccelerometerSettingsFragment.KEY_INCLUDE_LOCATION, true);
        AccelerometerDataFragment.setParameters(
                getValueFromText(accelerometerConfigurations.getString(AccelerometerSettingsFragment.KEY_HIGH_LIMIT, "2000"),
                        10, 10000),
                getValueFromText(accelerometerConfigurations.getString(AccelerometerSettingsFragment.KEY_UPDATE_PERIOD, "1000"),
                        100, 1000),
                accelerometerConfigurations.getString(AccelerometerSettingsFragment.KEY_ACCELEROMETER_SENSOR_TYPE, "0"),
                accelerometerConfigurations.getString(AccelerometerSettingsFragment.KEY_ACCELEROMETER_SENSOR_GAIN, "1"));
    }

    private int getValueFromText(String strValue, int lowerBound, int upperBound) {
        if (strValue.isEmpty()) return lowerBound;
        int value = Integer.parseInt(strValue);
        if (value > upperBound) return upperBound;
        else if (value < lowerBound) return lowerBound;
        else return value;
    }
}