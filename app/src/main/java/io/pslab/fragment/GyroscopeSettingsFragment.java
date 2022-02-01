package io.pslab.fragment;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import io.pslab.R;
import io.pslab.others.CustomSnackBar;
import io.pslab.others.PSLabPermission;

public class GyroscopeSettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String KEY_INCLUDE_LOCATION = "include_location_sensor_data";
    public static final String KEY_UPDATE_PERIOD = "setting_gyro_update_period";
    public static final String KEY_HIGH_LIMIT = "setting_gyro_high_limit";
    public static final String KEY_GYROSCOPE_SENSOR_GAIN = "setting_gyro_sensor_gain";

    private PSLabPermission psLabPermission;

    private EditTextPreference updatePeriodPref;
    private EditTextPreference higLimitPref;
    private EditTextPreference sensorGainPref;
    private CheckBoxPreference locationPreference;
    private SharedPreferences sharedPref;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.gyro_scope_settings, rootKey);
        updatePeriodPref = (EditTextPreference) getPreferenceScreen().findPreference(KEY_UPDATE_PERIOD);
        higLimitPref = (EditTextPreference) getPreferenceScreen().findPreference(KEY_HIGH_LIMIT);
        sensorGainPref = (EditTextPreference) getPreferenceScreen().findPreference(KEY_GYROSCOPE_SENSOR_GAIN);
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
        updatePeriodPref.setSummary(updatePeriodPref.getText() + " ms");
        higLimitPref.setSummary(higLimitPref.getText() + " rad/s");
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
                    if (updatePeriod > 2000 || updatePeriod < 100) {
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
            case KEY_GYROSCOPE_SENSOR_GAIN:
                try {
                    Integer gain = Integer.valueOf(sensorGainPref.getText());
                    sensorGainPref.setSummary(String.valueOf(gain));
                } catch (NumberFormatException e) {
                    sensorGainPref.setSummary("1");
                    sensorGainPref.setText("1");
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(KEY_GYROSCOPE_SENSOR_GAIN, "1");
                    editor.commit();
                }
                break;
            case KEY_HIGH_LIMIT:
                try {
                    Integer highLimit = Integer.parseInt(higLimitPref.getText());
                    if (highLimit > 1000 || highLimit < 0) {
                        throw new NumberFormatException();
                    } else {
                        higLimitPref.setSummary(String.valueOf(highLimit) + " rad/s");
                    }
                } catch (NumberFormatException e) {
                    CustomSnackBar.showSnackBar(getActivity().findViewById(android.R.id.content),
                            getString(R.string.high_limit_msg),null,null, Snackbar.LENGTH_SHORT);
                    higLimitPref.setSummary("20 " + "rad/s");
                    higLimitPref.setText("20");
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(KEY_HIGH_LIMIT, "20");
                    editor.commit();
                }
                break;
            default:
                break;
        }
    }
}
