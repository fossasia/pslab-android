package org.fossasia.pslab.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import org.fossasia.pslab.fragment.ExperimentDocFragment;
import org.fossasia.pslab.fragment.ExperimentSetupFragment;

/**
 * Created by viveksb007 on 12/7/17.
 */

public class PerformExperimentAdapter extends FragmentPagerAdapter {

    private final int PAGE_COUNT = 2;
    private String[] tabTitle = new String[]{"Experiment Doc", "Experiment Setup"};

    public PerformExperimentAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return ExperimentDocFragment.newInstance("astable-multivibrator.html");
            case 1:
                return ExperimentSetupFragment.newInstance();
            default:
                return ExperimentDocFragment.newInstance("astable-multivibrator.html");
        }
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitle[position];
    }
}
