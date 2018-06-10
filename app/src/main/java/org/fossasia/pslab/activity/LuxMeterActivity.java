package org.fossasia.pslab.activity;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.preference.PreferenceManager;

import org.fossasia.pslab.R;
import org.fossasia.pslab.fragment.LuxMeterFragmentConfig;
import org.fossasia.pslab.fragment.LuxMeterFragmentData;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LuxMeterActivity extends AppCompatActivity {
    private static final String PREF_NAME = "customDialogPreference";
    private static final String KEY = "skipLuxMeterDialog";

    @BindView(R.id.navigation_lux_meter)
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lux_meter);
        ButterKnife.bind(this);

        bottomNavigationView.setOnNavigationItemSelectedListener
                (new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        Fragment selectedFragment = null;
                        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frame_layout_lux_meter);

                        switch (item.getItemId()) {
                            case R.id.action_data:
                                if (!(fragment instanceof LuxMeterFragmentData))
                                    selectedFragment = LuxMeterFragmentData.newInstance();
                                break;
                            case R.id.action_config:
                                if (!(fragment instanceof LuxMeterFragmentConfig))
                                    //
                                break;
                            default:
                                break;
                        }
                        if (selectedFragment != null) {
                            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                            transaction.replace(R.id.frame_layout_lux_meter, selectedFragment);
                            transaction.commit();
                        }
                        return true;
                    }
                });
        howToConnectDialog(getString(R.string.lux_meter), getString(R.string.lux_meter_intro), R.drawable.bh1750_schematic, getString(R.string.lux_meter_desc));
        try {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_layout_lux_meter, LuxMeterFragmentData.newInstance());
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void howToConnectDialog(String title, String intro, int iconID, String desc) {
        try {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.custom_dialog_box, null);
            builder.setView(dialogView);
            builder.setTitle(title);
            final TextView luxMeterGuideText = (TextView) dialogView.findViewById(R.id.custom_dialog_text);
            final TextView luxMeterGuideDesc = (TextView) dialogView.findViewById(R.id.description_text);
            final ImageView bh1750Schematic = (ImageView) dialogView.findViewById(R.id.custom_dialog_schematic);
            final CheckBox doNotShowDialog = (CheckBox) dialogView.findViewById(R.id.toggle_show_again);
            final Button dismisButton = (Button) dialogView.findViewById(R.id.dismiss_button);
            luxMeterGuideText.setText(intro);
            bh1750Schematic.setImageResource(iconID);
            luxMeterGuideDesc.setText(desc);

            final SharedPreferences sharedPreferences = this.getSharedPreferences(PREF_NAME, MODE_PRIVATE);
            final AlertDialog dialog = builder.create();
            Boolean skipDialog = sharedPreferences.getBoolean(KEY, false);
            dismisButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (doNotShowDialog.isChecked()) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean(KEY, true);
                        editor.apply();
                    }
                    dialog.dismiss();
                }
            });
            if (!skipDialog) {
                dialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}