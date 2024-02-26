package io.pslab.activity;

import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;

import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import io.pslab.R;
import io.pslab.fragment.GyroscopeDataFragment;
import io.pslab.fragment.GyroscopeSettingsFragment;
import io.pslab.models.GyroData;
import io.pslab.models.PSLabSensor;
import io.pslab.models.SensorDataBlock;
import io.pslab.others.LocalDataLog;
import io.realm.RealmObject;
import io.realm.RealmResults;

public class GyroscopeActivity extends PSLabSensor {

    private static final String PREF_NAME = "customDialogPreference";
    public final String GYROSCOPE_LIMIT = "gyroscope_limit";
    public RealmResults<GyroData> recordedGyroData;

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
        return "GyroscopeFirstTime";
    }

    @Override
    public String getSensorName() {
        return getResources().getString(R.string.gyroscope);
    }

    @Override
    public int getGuideTitle() {
        return R.string.gyroscope;
    }

    @Override
    public int getGuideAbstract() {
        return R.string.gyroscope_intro;
    }

    @Override
    public int getGuideSchematics() {
        return R.drawable.gyroscope_axes_orientation;
    }

    @Override
    public int getGuideDescription() {
        return R.string.gyroscope_description_text;
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
        realm.copyToRealm((GyroData) sensorData);
        realm.commitTransaction();
    }

    @Override
    public void stopRecordSensorData() {
        LocalDataLog.with().refresh();
    }

    @Override
    public Fragment getSensorFragment() {
        return GyroscopeDataFragment.newInstance();
    }

    @Override
    public void getDataFromDataLogger() {
        if (getIntent().getExtras() != null && getIntent().getExtras().getBoolean(KEY_LOG)) {
            viewingData = true;
            recordedGyroData = LocalDataLog.with()
                    .getBlockOfGyroRecords(getIntent().getExtras().getLong(DATA_BLOCK));
            final GyroData data = recordedGyroData.get(0);
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
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        return sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        reinstateConfigurations();
    }

    private void reinstateConfigurations() {
        SharedPreferences gyroscopeConfigurations;
        gyroscopeConfigurations = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        locationEnabled = gyroscopeConfigurations.getBoolean(GyroscopeSettingsFragment.KEY_INCLUDE_LOCATION, true);
        GyroscopeDataFragment.setParameters(
                Float.valueOf(gyroscopeConfigurations.getString(GyroscopeSettingsFragment.KEY_HIGH_LIMIT, "20")),
                Integer.valueOf(gyroscopeConfigurations.getString(GyroscopeSettingsFragment.KEY_UPDATE_PERIOD, "1000")),
                gyroscopeConfigurations.getString(GyroscopeSettingsFragment.KEY_GYROSCOPE_SENSOR_GAIN, "1"));
    }
}
