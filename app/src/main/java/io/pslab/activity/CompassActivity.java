package io.pslab.activity;

import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;

import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import io.pslab.R;
import io.pslab.fragment.CompassDataFragment;
import io.pslab.fragment.CompassSettingsFragment;
import io.pslab.models.CompassData;
import io.pslab.models.PSLabSensor;
import io.pslab.models.SensorDataBlock;
import io.pslab.others.LocalDataLog;
import io.realm.RealmObject;
import io.realm.RealmResults;

public class CompassActivity extends PSLabSensor {

    private static final String PREF_NAME = "customDialogPreference";
    public RealmResults<CompassData> recordedCompassData;


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
        return "CompassFirstTime";
    }

    @Override
    public String getSensorName() {
        return getResources().getString(R.string.compass);
    }

    @Override
    public int getGuideTitle() {
        return R.string.compass_bottom_sheet_heading;
    }

    @Override
    public int getGuideAbstract() {
        return R.string.compass_bottom_sheet_text;
    }

    @Override
    public int getGuideSchematics() {
        return R.drawable.find_mobile_axis;
    }

    @Override
    public int getGuideDescription() {
        return R.string.compass_description;
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
        realm.copyToRealm((CompassData) sensorData);
        realm.commitTransaction();
    }

    @Override
    public void stopRecordSensorData() {
        LocalDataLog.with().refresh();
    }

    @Override
    public Fragment getSensorFragment() {
        return CompassDataFragment.newInstance();
    }

    @Override
    public void getDataFromDataLogger() {
        if (getIntent().getExtras() != null && getIntent().getExtras().getBoolean(KEY_LOG)) {
            viewingData = true;
            recordedCompassData = LocalDataLog.with()
                    .getBlockOfCompassRecords(getIntent().getExtras().getLong(DATA_BLOCK));
            final CompassData data = recordedCompassData.get(0);
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
        return sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION) != null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        reinstateConfigurations();
    }

    private void reinstateConfigurations() {
        SharedPreferences compassConfigurations;
        compassConfigurations = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        locationEnabled = compassConfigurations.getBoolean(CompassSettingsFragment.KEY_INCLUDE_LOCATION, true);
        CompassDataFragment.setParameters(
                compassConfigurations.getString(CompassSettingsFragment.KEY_COMPASS_SENSOR_TYPE, "0"));
    }
}
