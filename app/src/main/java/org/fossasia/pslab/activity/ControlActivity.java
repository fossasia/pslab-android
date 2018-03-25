package org.fossasia.pslab.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import org.fossasia.pslab.fragment.ControlFragmentAdvanced;
import org.fossasia.pslab.fragment.ControlFragmentMain;
import org.fossasia.pslab.fragment.ControlFragmentRead;

import org.fossasia.pslab.R;
import org.fossasia.pslab.others.ControlActivityCommon;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by viveksb007 on 10/5/17.
 */

public class ControlActivity extends AppCompatActivity {
    ControlActivityCommon common = new ControlActivityCommon();
    @BindView(R.id.navigation)
    BottomNavigationView bottomNavigationView;
    @BindView(R.id.control_toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        bottomNavigationView.setOnNavigationItemSelectedListener
                (new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        Fragment selectedFragment = null;
                        Fragment fragment=getSupportFragmentManager().findFragmentById(R.id.frame_layout_control);

                        switch (item.getItemId()) {
                            case R.id.action_item1:
                                if (!(fragment instanceof ControlFragmentMain))
                                    selectedFragment = ControlFragmentMain.newInstance();
                                break;
                            case R.id.action_item2:
                                if (!(fragment instanceof ControlFragmentRead))
                                    selectedFragment = ControlFragmentRead.newInstance();
                                break;
                            case R.id.action_item3:
                                if (!(fragment instanceof ControlFragmentAdvanced))
                                    selectedFragment = ControlFragmentAdvanced.newInstance();
                                break;
                        }
                        if (selectedFragment != null) {
                            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                            transaction.replace(R.id.frame_layout_control, selectedFragment);
                            transaction.commit();
                        }
                        return true;
                    }
                });

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout_control, ControlFragmentMain.newInstance());
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.closing_control_title)
                .setMessage(R.string.closing_control_message)
                .setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        ControlActivityCommon.editTextValues=null;
                        finish();
                    }
                })
                .setNegativeButton(R.string.dialog_no, null)
                .show();

    }
}
