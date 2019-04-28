package io.pslab.fragment;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;

import io.pslab.R;
import io.pslab.others.PSLabPermission;

/**
 * Created by Padmal on 12/13/18.
 */

public class BaroMeterSettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String KEY_INCLUDE_LOCATION = "include_location_sensor_data";
    public static final String KEY_UPDATE_PERIOD = "setting_baro_update_period";
    public static final String KEY_HIGH_LIMIT = "setting_baro_high_limit";
    public static final String KEY_BARO_SENSOR_TYPE = "setting_baro_sensor_type";

    private PSLabPermission psLabPermission;

    private EditTextPreference updatePeriodPref;
    private EditTextPreference highLimitPref;
    private CheckBoxPreference locationPreference;
    private ListPreference sensorTypePreference;
    private SharedPreferences sharedPref;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.baro_meter_settings, rootKey);
        updatePeriodPref = (EditTextPreference) getPreferenceScreen().findPreference(KEY_UPDATE_PERIOD);
        highLimitPref = (EditTextPreference) getPreferenceScreen().findPreference(KEY_HIGH_LIMIT);
        locationPreference = (CheckBoxPreference) getPreferenceScreen().findPreference(KEY_INCLUDE_LOCATION);
        sensorTypePreference = (ListPreference) getPreferenceScreen().findPreference(KEY_BARO_SENSOR_TYPE);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        psLabPermission = PSLabPermission.getInstance();
        if (!psLabPermission.checkPermissions(getActivity(), PSLabPermission.MAP_PERMISSION)) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(BaroMeterSettingsFragment.KEY_INCLUDE_LOCATION, true);
            editor.apply();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        locationPreference.setChecked(sharedPref.getBoolean(KEY_INCLUDE_LOCATION, true));
        updatePeriodPref.setSummary(updatePeriodPref.getText() + " ms");
        highLimitPref.setSummary(highLimitPref.getText() + " atm");
        sensorTypePreference.setSummary(sensorTypePreference.getEntry());
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
                    if (updatePeriod > 2000 || updatePeriod < 100) {
                        throw new NumberFormatException();
                    } else {
                        updatePeriodPref.setSummary(String.valueOf(updatePeriod));
                    }
                    updatePeriodPref.setSummary(updatePeriod + " ms");
                } catch (NumberFormatException e) {
                    updatePeriodPref.setSummary("1000 ms");
                    updatePeriodPref.setText("1000");
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(s, "1000");
                    editor.apply();
                }
                break;
            case KEY_HIGH_LIMIT:
                try {
                    Float highLimit = Float.valueOf(highLimitPref.getText());
                    if (highLimit > 1.1 || highLimit < 0.0) {
                        throw new NumberFormatException();
                    } else {
                        highLimitPref.setSummary(String.valueOf(highLimit));
                    }
                } catch (NumberFormatException e) {
                    highLimitPref.setSummary("1.1 atm");
                    highLimitPref.setText("1.1");
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(KEY_HIGH_LIMIT, "1.1");
                    editor.apply();
                }
                break;
            case KEY_BARO_SENSOR_TYPE:
                sensorTypePreference.setSummary(sensorTypePreference.getEntry());
                break;
            default:
                break;
        }
    }
}
