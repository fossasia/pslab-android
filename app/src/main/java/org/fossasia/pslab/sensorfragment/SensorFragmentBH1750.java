package org.fossasia.pslab.sensorfragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.fossasia.pslab.R;

import butterknife.BindView;

/**
 * Created by asitava on 10/7/17.
 */

public class SensorFragmentBH1750 extends Fragment {

    @BindView(R.id.bh1750_navigation)
    BottomNavigationView bottomNavigationView;

    public static SensorFragmentBH1750 newInstance() {
        return new SensorFragmentBH1750();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sensor_bh1750, container, false);

        bottomNavigationView = view.findViewById(R.id.bh1750_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener
                (new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        Fragment selectedFragment = null;
                        Fragment fragment = getChildFragmentManager().findFragmentById(R.id.frame_layout_bh1750);

                        switch (item.getItemId()) {
                            case R.id.action_data:
                                if (!(fragment instanceof SensorFragmentBH1750Data))
                                    selectedFragment = SensorFragmentBH1750Data.newInstance();
                                break;
                            case R.id.action_config:
                                if (!(fragment instanceof SensorFragmentBH1750Config))
                                    selectedFragment = SensorFragmentBH1750Config.newInstance();
                                break;
                        }
                        if (selectedFragment != null) {
                            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                            transaction.replace(R.id.frame_layout_bh1750, selectedFragment);
                            transaction.commit();
                        }
                        return true;
                    }
                });
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout_bh1750, SensorFragmentBH1750Data.newInstance());
        transaction.commit();

        return view;
    }

}