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
import android.view.MenuItem;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        ButterKnife.bind(this);

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
        ControlActivityCommon.editTextValues=null;
        finish();
    }
}
