package io.pslab.fragment;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;

import io.pslab.R;
import io.pslab.others.CustomSnackBar;
import io.pslab.others.PSLabPermission;

/**
 * Created by Avjeet on 10-08-2018.
 */
public class LuxMeterSettingFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String KEY_INCLUDE_LOCATION = "include_location_sensor_data";
    public static final String KEY_UPDATE_PERIOD = "setting_lux_update_period";
    public static final String KEY_HIGH_LIMIT = "setting_lux_high_limit";
    public static final String KEY_LUX_RESSOR_TYPE = "setting_lux_sensor_type";
    public static final String KEY_LUX_RESSOR_GAIN = "setting_lux_sensor_gain";

    private PSLabPermission psLabPermission;

    private EditTextPreference updatePeriodPref;
    private EditTextPreference higLimitPref;
    private EditTextPreference sensorGainPref;
    private CheckBoxPreference locationPreference;
    private ListPreference sensorTypePreference;
    private SharedPreferences sharedPref;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.lux_meter_settings, rootKey);
        updatePeriodPref = (EditTextPreference) getPreferenceScreen().findPreference(KEY_UPDATE_PERIOD);
        higLimitPref = (EditTextPreference) getPreferenceScreen().findPreference(KEY_HIGH_LIMIT);
        sensorGainPref = (EditTextPreference) getPreferenceScreen().findPreference(KEY_LUX_RESSOR_GAIN);
        locationPreference = (CheckBoxPreference) getPreferenceScreen().findPreference(KEY_INCLUDE_LOCATION);
        sensorTypePreference = (ListPreference) getPreferenceScreen().findPreference(KEY_LUX_RESSOR_TYPE);
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
        updatePeriodPref.setSummary(updatePeriodPref.getText() + " ms");
        higLimitPref.setSummary(higLimitPref.getText() + " Lx");
        sensorTypePreference.setSummary(sensorTypePreference.getEntry());
        sensorGainPref.setSummary(sensorGainPref.getText());
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
            case KEY_LUX_RESSOR_GAIN:
                try {
                    Integer gain = Integer.parseInt(sensorGainPref.getText());
                    sensorGainPref.setSummary(String.valueOf(gain));
                } catch (NumberFormatException e) {
                    sensorGainPref.setSummary("1");
                    sensorGainPref.setText("1");
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(KEY_LUX_RESSOR_GAIN, "1");
                    editor.commit();
                }
                break;
            case KEY_HIGH_LIMIT:
                try {
                    Integer highLimit = Integer.parseInt(higLimitPref.getText());
                    if (highLimit > 10000 || highLimit < 10) {
                        throw new NumberFormatException();
                    } else {
                        higLimitPref.setSummary(String.valueOf(highLimit) + " Lx");
                    }
                } catch (NumberFormatException e) {
                    CustomSnackBar.showSnackBar(getActivity().findViewById(android.R.id.content),
                            getString(R.string.high_limit_msg),null,null, Snackbar.LENGTH_SHORT);
                    higLimitPref.setSummary("2000 Lx");
                    higLimitPref.setText("2000");
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(KEY_HIGH_LIMIT, "2000");
                    editor.commit();
                }
                break;
            case KEY_LUX_RESSOR_TYPE:
                sensorTypePreference.setSummary(sensorTypePreference.getEntry());
                break;
            default:
                break;
        }
    }
}
