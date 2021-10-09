package io.pslab.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;

import io.pslab.R;

/**
 * Created by viveksb007 on 15/3/17.
 */

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String KEY_EXPORT_DATA_FORMAT_LIST = "export_data_format_list";
    private ListPreference listPreference;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    public SettingsFragment() {
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_preference_fragment, rootKey);
        listPreference = (ListPreference) getPreferenceScreen().findPreference(KEY_EXPORT_DATA_FORMAT_LIST);
    }

    @Override
    public void onResume() {
        super.onResume();
        listPreference.setSummary("Current format is " + listPreference.getEntry().toString());
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
    }
}
