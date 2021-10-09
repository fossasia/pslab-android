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

import io.pslab.DataFormatter;
import io.pslab.R;
import io.pslab.others.CustomSnackBar;
import io.pslab.others.PSLabPermission;

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
                    Integer updatePeriod = Integer.parseInt(updatePeriodPref.getText());
                    if (updatePeriod > 2000 || updatePeriod < 100) {
                        throw new NumberFormatException();
                    } else {
                        updatePeriodPref.setSummary(String.valueOf(updatePeriod) + " ms");
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
            case KEY_HIGH_LIMIT:
                try {
                    double highLimit = Double.parseDouble(highLimitPref.getText());
                    if (highLimit > 1.10 || highLimit < 0.00) {
                        throw new NumberFormatException();
                    } else {
                        highLimitPref.setSummary(DataFormatter.formatDouble(highLimit, DataFormatter.LOW_PRECISION_FORMAT) + " atm");
                    }
                } catch (NumberFormatException e) {
                    CustomSnackBar.showSnackBar(getActivity().findViewById(android.R.id.content),
                            getString(R.string.high_limit_msg),null,null,Snackbar.LENGTH_SHORT);
                    highLimitPref.setSummary("1.10 atm");
                    highLimitPref.setText("1.10");
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(KEY_HIGH_LIMIT, "1.10");
                    editor.commit();
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
