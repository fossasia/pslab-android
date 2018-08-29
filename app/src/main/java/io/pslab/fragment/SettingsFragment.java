package io.pslab.fragment;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;

import io.pslab.R;
import io.pslab.others.GPSLogger;

/**
 * Created by viveksb007 on 15/3/17.
 */

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String KEY_EXPORT_DATA_FORMAT_LIST = "export_data_format_list";
    public static final String KEY_INCLUDE_LOCATION = "include_location_sensor_data";
    private ListPreference listPreference;
    private CheckBoxPreference locationPreference;
    private SharedPreferences sharedPref ;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    public SettingsFragment() {
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_preference_fragment, rootKey);
        listPreference = (ListPreference) getPreferenceScreen().findPreference(KEY_EXPORT_DATA_FORMAT_LIST);
        locationPreference = (CheckBoxPreference) getPreferenceScreen().findPreference(KEY_INCLUDE_LOCATION);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        /* If permission is disabled explicitly then this will put false in location preference */

        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(SettingsFragment.KEY_INCLUDE_LOCATION,false);
            editor.commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        listPreference.setSummary("Current format is " + listPreference.getEntry().toString());
        locationPreference.setChecked(sharedPref.getBoolean(KEY_INCLUDE_LOCATION,false));
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (KEY_EXPORT_DATA_FORMAT_LIST.equals(key)) {
            listPreference.setSummary("Current format is " + listPreference.getEntry().toString());
        }

        if(KEY_INCLUDE_LOCATION.equals(key) && locationPreference.isChecked()){
            new GPSLogger(getActivity()).requestPermissionIfNotGiven();
        }
    }
}
