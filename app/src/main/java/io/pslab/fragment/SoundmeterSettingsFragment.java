package io.pslab.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.preference.CheckBoxPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import io.pslab.R;
import io.pslab.others.PSLabPermission;

/**
 * @author reckoner1429
 */
public class SoundmeterSettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {


    public static final String KEY_INCLUDE_LOCATION = "include_location_sensor_data";

    private CheckBoxPreference locationPreference;
    private PSLabPermission psLabPermission;
    private SharedPreferences sharedPref;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.sound_meter_settings,rootKey);
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
                    psLabPermission.checkPermissions(
                            getActivity(), PSLabPermission.MAP_PERMISSION);
                }
                break;
            default:
                break;
        }
    }
}
