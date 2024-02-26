package io.pslab.activity;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.pslab.R;
import io.pslab.fragment.ControlFragmentAdvanced;
import io.pslab.fragment.ControlFragmentMain;
import io.pslab.fragment.ControlFragmentRead;
import io.pslab.others.ControlActivityCommon;

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
                        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frame_layout_control);

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
        ControlActivityCommon.editTextValues = null;
        finish();
    }
}
