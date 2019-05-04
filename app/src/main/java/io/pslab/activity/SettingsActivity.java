package io.pslab.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.FrameLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.pslab.R;
import io.pslab.fragment.BaroMeterSettingsFragment;
import io.pslab.fragment.LuxMeterSettingFragment;
import io.pslab.fragment.SettingsFragment;
import io.pslab.models.PSLabSensor;
import io.pslab.others.GPSLogger;

/**
 * Created by Avjeet on 7/7/18.
 */

public class SettingsActivity extends AppCompatActivity {

    @BindView(R.id.setting_toolbar)
    Toolbar toolbar;
    @BindView(R.id.content)
    FrameLayout content;
    private Unbinder unBinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        unBinder = ButterKnife.bind(this);

        Intent intent = getIntent();
        String title = intent.getStringExtra("title");

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(title);
        }

        Fragment fragment;
        switch (title) {
            case PSLabSensor.LUXMETER_CONFIGURATIONS:
                fragment = new LuxMeterSettingFragment();
                break;
            case PSLabSensor.BAROMETER_CONFIGURATIONS:
                fragment = new BaroMeterSettingsFragment();
                break;
            default:
                fragment = new SettingsFragment();
                break;
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.content, fragment).commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unBinder.unbind();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case GPSLogger.PSLAB_PERMISSION_FOR_MAPS: {
                if (grantResults.length <= 0
                        || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit();
                    editor.putBoolean(LuxMeterSettingFragment.KEY_INCLUDE_LOCATION, false);
                    editor.apply();
                }
            }
        }
    }
}
