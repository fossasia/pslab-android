package io.pslab.fragment;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import io.pslab.R;
import io.pslab.others.PSLabPermission;

/**
 * Created by Kunal on 18-12-2018.
 */
public class AccelerometerSettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String KEY_INCLUDE_LOCATION = "include_location_sensor_data";
    public static final String KEY_UPDATE_PERIOD = "setting_accelerometer_update_period";
    public static final String KEY_HIGH_LIMIT = "setting_accelerometer_high_limit";
    public static final String KEY_ACCELEROMETER_SENSOR_TYPE = "setting_accelerometer_sensor_type";
    public static final String KEY_ACCELEROMETER_SENSOR_GAIN = "setting_accelerometer_sensor_gain";

    private PSLabPermission psLabPermission;

    private EditTextPreference updatePeriodPref;
    private EditTextPreference higLimitPref;
    private EditTextPreference sensorGainPref;
    private CheckBoxPreference locationPreference;
    private ListPreference sensorTypePreference;
    private SharedPreferences sharedPref;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.accelerometer_settings, rootKey);
        updatePeriodPref = (EditTextPreference) getPreferenceScreen().findPreference(KEY_UPDATE_PERIOD);
        higLimitPref = (EditTextPreference) getPreferenceScreen().findPreference(KEY_HIGH_LIMIT);
        sensorGainPref = (EditTextPreference) getPreferenceScreen().findPreference(KEY_ACCELEROMETER_SENSOR_GAIN);
        locationPreference = (CheckBoxPreference) getPreferenceScreen().findPreference(KEY_INCLUDE_LOCATION);
        sensorTypePreference = (ListPreference) getPreferenceScreen().findPreference(KEY_ACCELEROMETER_SENSOR_TYPE);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        psLabPermission = PSLabPermission.getInstance();
        if (!psLabPermission.checkPermissions(getActivity(), PSLabPermission.MAP_PERMISSION)) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(LuxMeterSettingFragment.KEY_INCLUDE_LOCATION, true);
            editor.apply();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        locationPreference.setChecked(sharedPref.getBoolean(KEY_INCLUDE_LOCATION, true));
        updatePeriodPref.setSummary(updatePeriodPref.getText() + " ms");
        higLimitPref.setSummary(higLimitPref.getText() + " m/s²");
        sensorTypePreference.setSummary(sensorTypePreference.getEntry());
        sensorGainPref.setSummary(sensorGainPref.getText());
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @SuppressLint("ApplySharedPref")
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        switch (s) {
            case KEY_INCLUDE_LOCATION:
                if (locationPreference.isChecked()) {
                    psLabPermission.checkPermissions(
                            getActivity(), PSLabPermission.MAP_PERMISSION);
                }
                break;
            case KEY_UPDATE_PERIOD:
                try {
                    Integer updatePeriod = Integer.valueOf(updatePeriodPref.getText());
                    updatePeriodPref.setSummary(updatePeriod + " ms");
                } catch (NumberFormatException e) {
                    updatePeriodPref.setSummary("1000 ms");
                    updatePeriodPref.setText("1000");
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(s, "1000");
                    editor.commit();
                }
                break;
            case KEY_ACCELEROMETER_SENSOR_GAIN:
                try {
                    Integer gain = Integer.valueOf(sensorGainPref.getText());
                    sensorGainPref.setSummary(String.valueOf(gain));
                } catch (NumberFormatException e) {
                    sensorGainPref.setSummary("1");
                    sensorGainPref.setText("1");
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(KEY_ACCELEROMETER_SENSOR_GAIN, "1");
                    editor.commit();
                }
                break;
            case KEY_HIGH_LIMIT:
                try {
                    Integer highLimit = Integer.valueOf(higLimitPref.getText());
                    higLimitPref.setSummary(String.valueOf(highLimit));
                } catch (NumberFormatException e) {
                    higLimitPref.setSummary("2000 Lx");
                    higLimitPref.setText("2000");
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(KEY_HIGH_LIMIT, "2000");
                    editor.commit();
                }
                break;
            case KEY_ACCELEROMETER_SENSOR_TYPE:
                sensorTypePreference.setSummary(sensorTypePreference.getEntry());
                break;
            default:
                break;
        }
    }
}
