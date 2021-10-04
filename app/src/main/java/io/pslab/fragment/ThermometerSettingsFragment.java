package io.pslab.fragment;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import io.pslab.R;
import io.pslab.others.CustomSnackBar;
import io.pslab.others.PSLabPermission;

public class ThermometerSettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String KEY_INCLUDE_LOCATION = "include_location_sensor_data";
    public static final String KEY_UPDATE_PERIOD = "setting_thermo_update_period";
    public static final String KEY_THERMO_SENSOR_TYPE = "setting_thermo_sensor_type";
    public static final String KEY_THERMO_UNIT = "select_temp_unit";

    private PSLabPermission psLabPermission;

    private EditTextPreference updatePeriodPref;
    private CheckBoxPreference locationPreference;
    private ListPreference sensorTypePreference;
    private SharedPreferences sharedPref;
    private ListPreference unitPref;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.thermometer_settings, rootKey);
        updatePeriodPref = (EditTextPreference) getPreferenceScreen().findPreference(KEY_UPDATE_PERIOD);
        locationPreference = (CheckBoxPreference) getPreferenceScreen().findPreference(KEY_INCLUDE_LOCATION);
        sensorTypePreference = (ListPreference) getPreferenceScreen().findPreference(KEY_THERMO_SENSOR_TYPE);
        unitPref = (ListPreference) getPreferenceScreen().findPreference(KEY_THERMO_UNIT);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        psLabPermission = PSLabPermission.getInstance();
        if (!psLabPermission.checkPermissions(getActivity(), PSLabPermission.MAP_PERMISSION)) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(ThermometerSettingsFragment.KEY_INCLUDE_LOCATION, true);
            editor.apply();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        locationPreference.setChecked(sharedPref.getBoolean(KEY_INCLUDE_LOCATION, true));
        updatePeriodPref.setSummary(updatePeriodPref.getText() + " ms");
        sensorTypePreference.setSummary(sensorTypePreference.getEntry());
        unitPref.setSummary(unitPref.getEntry());
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
                    if (updatePeriod > 1000 || updatePeriod < 100) {
                        throw new NumberFormatException();
                    } else {
                        updatePeriodPref.setSummary(updatePeriod + " ms");
                    }
                } catch (NumberFormatException e) {
                    CustomSnackBar.showSnackBar(getActivity().findViewById(android.R.id.content),
                            getString(R.string.update_period_msg),null,null, Snackbar.LENGTH_SHORT);
                    updatePeriodPref.setSummary("1000 ms");
                    updatePeriodPref.setText("1000");
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(s, "1000");
                    editor.commit();
                }
                break;
            case KEY_THERMO_SENSOR_TYPE:
                sensorTypePreference.setSummary(sensorTypePreference.getEntry());
                break;
            case KEY_THERMO_UNIT:
                unitPref.setSummary(unitPref.getEntry());
                break;
            default:
                break;
        }
    }
}
