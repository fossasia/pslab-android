package io.pslab.activity;

import static android.os.Build.VERSION.SDK_INT;

import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;

import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import io.pslab.R;
import io.pslab.fragment.LuxMeterDataFragment;
import io.pslab.fragment.LuxMeterSettingFragment;
import io.pslab.models.LuxData;
import io.pslab.models.PSLabSensor;
import io.pslab.models.SensorDataBlock;
import io.pslab.others.LocalDataLog;
import io.realm.RealmObject;
import io.realm.RealmResults;

public class LuxMeterActivity extends PSLabSensor {

    private static final String PREF_NAME = "customDialogPreference";
    public final String LUXMETER_LIMIT = "luxmeter_limit";
    public RealmResults<LuxData> recordedLuxData;

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
        return "LuxMeterFirstTime";
    }

    @Override
    public String getSensorName() {
        return getResources().getString(R.string.lux_meter);
    }

    @Override
    public int getGuideTitle() {
        return R.string.lux_meter;
    }

    @Override
    public int getGuideAbstract() {
        return R.string.lux_meter_intro;
    }

    @Override
    public int getGuideSchematics() {
        return R.drawable.bh1750_schematic;
    }

    @Override
    public int getGuideDescription() {
        return R.string.lux_meter_desc;
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
        realm.copyToRealm((LuxData) sensorData);
        realm.commitTransaction();
    }

    @Override
    public void stopRecordSensorData() {
        LocalDataLog.with().refresh();
    }

    @Override
    public Fragment getSensorFragment() {
        return LuxMeterDataFragment.newInstance();
    }

    @Override
    public void getDataFromDataLogger() {
        if (SDK_INT >= 21 && getIntent().getExtras() != null && getIntent().getExtras().getBoolean(KEY_LOG)) {
            //playingData = true;
            viewingData = true;
            recordedLuxData = LocalDataLog.with()
                    .getBlockOfLuxRecords(getIntent().getExtras().getLong(DATA_BLOCK));
            final LuxData data = recordedLuxData.get(0);
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
        return sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null;
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
        locationEnabled = luxMeterConfigurations.getBoolean(LuxMeterSettingFragment.KEY_INCLUDE_LOCATION, true);
        LuxMeterDataFragment.setParameters(
                getValueFromText(luxMeterConfigurations.getString(LuxMeterSettingFragment.KEY_HIGH_LIMIT, "2000"),
                        10, 10000),
                getValueFromText(luxMeterConfigurations.getString(LuxMeterSettingFragment.KEY_UPDATE_PERIOD, "1000"),
                        100, 1000),
                luxMeterConfigurations.getString(LuxMeterSettingFragment.KEY_LUX_SENSOR_TYPE, "0"),
                luxMeterConfigurations.getString(LuxMeterSettingFragment.KEY_LUX_SENSOR_GAIN, "1"));
    }

    private int getValueFromText(String strValue, int lowerBound, int upperBound) {
        if (strValue.isEmpty()) return lowerBound;
        int value = Integer.parseInt(strValue);
        if (value > upperBound) return upperBound;
        else return Math.max(value, lowerBound);
    }
}
