package io.pslab.fragment;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.widget.Toast;

import io.pslab.R;
import io.pslab.others.PSLabPermission;

public class GyroscopeSettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String KEY_INCLUDE_LOCATION = "include_location_sensor_data";
    public static final String KEY_UPDATE_PERIOD = "setting_lux_update_period";
    public static final String KEY_HIGH_LIMIT = "setting_lux_high_limit";
    public static final String KEY_GYROSCOPE_SENSOR_GAIN = "setting_lux_sensor_gain";

    private PSLabPermission psLabPermission;

    private EditTextPreference updatePeriodPref;
    private EditTextPreference higLimitPref;
    private EditTextPreference sensorGainPref;
    private CheckBoxPreference locationPreference;
    private SharedPreferences sharedPref;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.gyro_scope_settings, rootKey);
        updatePeriodPref = (EditTextPreference) getPreferenceScreen().findPreference(KEY_UPDATE_PERIOD);
        higLimitPref = (EditTextPreference) getPreferenceScreen().findPreference(KEY_HIGH_LIMIT);
        sensorGainPref = (EditTextPreference) getPreferenceScreen().findPreference(KEY_GYROSCOPE_SENSOR_GAIN);
        locationPreference = (CheckBoxPreference) getPreferenceScreen().findPreference(KEY_INCLUDE_LOCATION);
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
        higLimitPref.setSummary(higLimitPref.getText() + " rad/s");
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
                    Integer updatePeriod = Integer.parseInt(updatePeriodPref.getText());
                    if (updatePeriod > 2000 || updatePeriod < 100) {
                        throw new NumberFormatException();
                    } else {
                        updatePeriodPref.setSummary(updatePeriod + " ms");
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.update_period_msg), Toast.LENGTH_SHORT).show();
                    updatePeriodPref.setSummary("1000 ms");
                    updatePeriodPref.setText("1000");
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(s, "1000");
                    editor.commit();
                }
                break;
            case KEY_GYROSCOPE_SENSOR_GAIN:
                try {
                    Integer gain = Integer.valueOf(sensorGainPref.getText());
                    sensorGainPref.setSummary(String.valueOf(gain));
                } catch (NumberFormatException e) {
                    sensorGainPref.setSummary("1");
                    sensorGainPref.setText("1");
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(KEY_GYROSCOPE_SENSOR_GAIN, "1");
                    editor.commit();
                }
                break;
            case KEY_HIGH_LIMIT:
                try {
                    Integer highLimit = Integer.parseInt(higLimitPref.getText());
                    if (highLimit > 1000 || highLimit < 0) {
                        throw new NumberFormatException();
                    } else {
                        higLimitPref.setSummary(String.valueOf(highLimit) + " rad/s");
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.high_limit_msg), Toast.LENGTH_SHORT).show();
                    higLimitPref.setSummary("20 " + "rad/s");
                    higLimitPref.setText("20");
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(KEY_HIGH_LIMIT, "20");
                    editor.commit();
                }
                break;
            default:
                break;
        }
    }
}
