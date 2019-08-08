package io.pslab.activity;

import android.content.SharedPreferences;
import android.support.v4.app.Fragment;

import io.pslab.R;
import io.pslab.fragment.GasSensorDataFragment;
import io.pslab.models.PSLabSensor;
import io.pslab.models.SensorDataBlock;
import io.realm.RealmObject;

public class GasSensorActivity extends PSLabSensor {

    private static final String PREF_NAME = "customDialogPreference";

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

    }

    @Override
    public void recordSensorData(RealmObject sensorData) {

    }

    @Override
    public void stopRecordSensorData() {

    }

    @Override
    public Fragment getSensorFragment() {
        return GasSensorDataFragment.newInstance();
    }

    @Override
    public void getDataFromDataLogger() {

    }
}
