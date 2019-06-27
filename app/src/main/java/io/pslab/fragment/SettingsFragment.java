package io.pslab.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceFragmentCompat;

import io.pslab.R;

/**
 * Created by viveksb007 on 15/3/17.
 */

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String KEY_EXPORT_DATA_FORMAT_LIST = "export_data_format_list";
    public static final String KEY_MAP_SERVICES = "map_services";
    private ListPreference listPreference;
    private ListPreference mapPreference;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    public SettingsFragment() {
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_preference_fragment, rootKey);
        listPreference = (ListPreference) getPreferenceScreen().findPreference(KEY_EXPORT_DATA_FORMAT_LIST);
        mapPreference = (ListPreference) getPreferenceScreen().findPreference(KEY_MAP_SERVICES);
    }

    @Override
    public void onResume() {
        super.onResume();
        listPreference.setSummary(listPreference.getEntry().toString());
        mapPreference.setSummary(mapPreference.getEntry().toString());
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
            listPreference.setSummary(listPreference.getEntry().toString());
        }
        else if(KEY_MAP_SERVICES.equals(key)) {
            mapPreference.setSummary(mapPreference.getEntry().toString());
        }
    }
}
