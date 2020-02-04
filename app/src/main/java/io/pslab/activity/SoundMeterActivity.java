package io.pslab.activity;

import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;

import io.pslab.R;
import io.pslab.fragment.SoundMeterDataFragment;
import io.pslab.fragment.SoundmeterSettingsFragment;
import io.pslab.models.PSLabSensor;
import io.pslab.models.SensorDataBlock;
import io.pslab.models.SoundData;
import io.pslab.others.LocalDataLog;
import io.realm.RealmObject;
import io.realm.RealmResults;

/**
 * @author reckoner1429
 */
public class SoundMeterActivity extends PSLabSensor {

    private static final String PREF_NAME = "customDialogPreference";
    public RealmResults<SoundData> recordedSoundData;

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
        return "SoundMeterFirstTime";
    }

    @Override
    public String getSensorName() {
        return getResources().getString(R.string.sound_meter);
    }

    @Override
    public int getGuideTitle() {
        return R.string.sound_meter;
    }

    @Override
    public int getGuideAbstract() {
        return R.string.sound_meter_intro;
    }

    @Override
    public int getGuideSchematics() {
        return R.drawable.bh1750_schematic;
    }

    @Override
    public int getGuideDescription() {
        return R.string.sound_meter_desc;
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
        realm.copyToRealm((SoundData) sensorData);
        realm.commitTransaction();
    }

    @Override
    public void stopRecordSensorData() {
        LocalDataLog.with().refresh();
    }

    @Override
    public Fragment getSensorFragment() {
        return SoundMeterDataFragment.newInstance();
    }

    @Override
    public void getDataFromDataLogger() {
        if (getIntent().getExtras() != null && getIntent().getExtras().getBoolean(KEY_LOG)) {
            //playingData = true;
            viewingData = true;
            recordedSoundData = LocalDataLog.with()
                    .getBlockOfSoundRecords(getIntent().getExtras().getLong(DATA_BLOCK));
            String title = titleFormat.format(recordedSoundData.get(0).getTime());
            getSupportActionBar().setTitle(title);
        }
    }

    /**
     * Once settings have been changed, those changes can be captured from onResume method.
     * reinstateConfigurations() will update the logs with new settings
     */
    @Override
    protected void onResume() {
        super.onResume();
        reinstateConfigurations();
    }

    private void reinstateConfigurations() {
        SharedPreferences luxMeterConfigurations;
        luxMeterConfigurations = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        locationEnabled = luxMeterConfigurations.getBoolean(SoundmeterSettingsFragment.KEY_INCLUDE_LOCATION, true);
        SoundMeterDataFragment.setParameters();
        /**
         * TODO: set parameters
         */
    }
}
