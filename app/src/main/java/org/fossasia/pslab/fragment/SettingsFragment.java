package org.fossasia.pslab.fragment;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import org.fossasia.pslab.R;

/**
 * Created by viveksb007 on 15/3/17.
 */

public class SettingsFragment extends PreferenceFragmentCompat {

    public static SettingsFragment newInstance() {
        SettingsFragment settingsFragment = new SettingsFragment();
        return settingsFragment;
    }


    public SettingsFragment() {

    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_preference_fragment, rootKey);
    }

}
