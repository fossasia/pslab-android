package org.fossasia.pslab.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import org.fossasia.pslab.R;
import org.fossasia.pslab.fragment.ExperimentDocFragment;
import org.fossasia.pslab.fragment.ExperimentSetupFragment;

/**
 * Created by viveksb007 on 12/7/17.
 */

public class PerformExperimentAdapter extends FragmentPagerAdapter {

    private final int PAGE_COUNT = 2;
    private String[] tabTitle = new String[]{"Experiment Doc", "Experiment Setup"};
    private String experimentTitle;
    private Context context;

    public PerformExperimentAdapter(FragmentManager fragmentManager, String experimentTitle, Context context) {
        super(fragmentManager);
        this.experimentTitle = experimentTitle;
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                if (experimentTitle.equals(context.getResources().getString(R.string.diode_iv)))
                    return ExperimentDocFragment.newInstance("D_diodeIV.html");
                if (experimentTitle.equals(context.getResources().getString(R.string.zener_iv)))
                    return ExperimentDocFragment.newInstance("D_ZenerIV.html");
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
