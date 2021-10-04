package io.pslab.fragment;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import io.pslab.R;
import io.pslab.others.PSLabPermission;

public class CompassSettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String KEY_COMPASS_SENSOR_TYPE = "setting_compass_sensor_type";
    public static final String KEY_INCLUDE_LOCATION = "include_location_sensor_data";

    private PSLabPermission psLabPermission;

    private ListPreference sensorTypePreference;
    private SharedPreferences sharedPref;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.compass_settings, rootKey);
        sensorTypePreference = (ListPreference) getPreferenceScreen().findPreference(KEY_COMPASS_SENSOR_TYPE);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        psLabPermission = PSLabPermission.getInstance();
        if (!psLabPermission.checkPermissions(getActivity(), PSLabPermission.MAP_PERMISSION)) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(CompassSettingsFragment.KEY_INCLUDE_LOCATION, true);
            editor.apply();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
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
            case KEY_COMPASS_SENSOR_TYPE:
                sensorTypePreference.setSummary(sensorTypePreference.getEntry());
                break;
            default:
                break;
        }
    }
}

