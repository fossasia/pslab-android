package io.pslab.fragment;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;

import io.pslab.R;
import io.pslab.others.GPSLogger;

/**
 * Created by Avjeet on 10-08-2018.
 */
public class LuxMeterSettingFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String KEY_INCLUDE_LOCATION = "include_location_sensor_data";
    public static final String KEY_UPDATE_PERIOD = "setting_lux_update_period";
    public static final String KEY_HIGH_LIMIT = "setting_lux_high_limit";

    private EditTextPreference updatePeriodPref;
    private EditTextPreference higLimitPref;
    private CheckBoxPreference locationPreference;
    private SharedPreferences sharedPref;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.lux_meter_settings, rootKey);
        updatePeriodPref = (EditTextPreference) getPreferenceScreen().findPreference(KEY_UPDATE_PERIOD);
        higLimitPref = (EditTextPreference) getPreferenceScreen().findPreference(KEY_HIGH_LIMIT);
        locationPreference = (CheckBoxPreference) getPreferenceScreen().findPreference(KEY_INCLUDE_LOCATION);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(LuxMeterSettingFragment.KEY_INCLUDE_LOCATION, true);
            editor.commit();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        locationPreference.setChecked(sharedPref.getBoolean(KEY_INCLUDE_LOCATION, true));
        updatePeriodPref.setSummary("Update Period is " + updatePeriodPref.getText() + " ms");
        higLimitPref.setSummary("High Limit is " + higLimitPref.getText());
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        switch (s) {
            case KEY_INCLUDE_LOCATION:
                if (locationPreference.isChecked()) {
                    new GPSLogger(getActivity()).requestPermissionIfNotGiven();
                }
                break;
            case KEY_UPDATE_PERIOD:
                updatePeriodPref.setSummary("Update Period is " + updatePeriodPref.getText() + " " + "ms");
                break;
            case KEY_HIGH_LIMIT:
                higLimitPref.setSummary("High Limit is " + higLimitPref.getText());
                break;
            default:
                break;
        }
    }
}
