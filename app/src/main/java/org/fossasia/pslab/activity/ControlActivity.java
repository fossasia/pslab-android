package org.fossasia.pslab.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import org.fossasia.pslab.communication.ScienceLab;
import org.fossasia.pslab.fragment.ControlFragmentAdvanced;
import org.fossasia.pslab.fragment.ControlFragmentMain;
import org.fossasia.pslab.fragment.ControlFragmentRead;
import org.fossasia.pslab.others.ScienceLabCommon;

import org.fossasia.pslab.R;
import butterknife.ButterKnife;

/**
 * Created by viveksb007 on 10/5/17.
 */

public class ControlActivity extends AppCompatActivity {

    private ScienceLab scienceLab;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        scienceLab = ScienceLabCommon.scienceLab;
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener
                (new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        Fragment selectedFragment = null;
                        switch (item.getItemId()) {
                            case R.id.action_item1:
                                selectedFragment = ControlFragmentMain.newInstance();
                                break;
                            case R.id.action_item2:
                                selectedFragment = ControlFragmentRead.newInstance();
                                break;
                            case R.id.action_item3:
                                selectedFragment = ControlFragmentAdvanced.newInstance();
                                break;
                        }
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.frame_layout_control, selectedFragment);
                        transaction.commit();
                        return true;
                    }
                });

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout_control, ControlFragmentMain.newInstance());
        transaction.commit();
    }
}
