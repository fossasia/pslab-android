package org.fossasia.pslab.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import org.fossasia.pslab.experimentsetup.ZenerSetupFragment;
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
                if (experimentTitle.equals(context.getResources().getString(R.string.half_wave_rectifier)))
                    return ExperimentDocFragment.newInstance("L_halfWave.html");
                if (experimentTitle.equals(context.getResources().getString(R.string.transistor_cb)))
                    return ExperimentDocFragment.newInstance("D_transistorCB.html");
                if (experimentTitle.equals(context.getResources().getString(R.string.transistor_ce)))
                    return ExperimentDocFragment.newInstance("D_transistorCE.html");
                if (experimentTitle.equals(context.getResources().getString(R.string.astable_multivibrator)))
                    return ExperimentDocFragment.newInstance("astable-multivibrator.html");
                if (experimentTitle.equals(context.getResources().getString(R.string.transient_rlc)))
                    return ExperimentDocFragment.newInstance("E_transientRLC.html");
                if (experimentTitle.equals(context.getResources().getString(R.string.bode_plots)))
                    return ExperimentDocFragment.newInstance("K_bodePlots.html");
                if (experimentTitle.equals(context.getResources().getString(R.string.ohms_law)))
                    return ExperimentDocFragment.newInstance("E_OhmsLaw.html");
                if (experimentTitle.equals(context.getResources().getString(R.string.m_random_sampling)))
                    return ExperimentDocFragment.newInstance("M_RANDOM_SAMPLING.html");
                if (experimentTitle.equals(context.getResources().getString(R.string.ac_and_dc)))
                    return ExperimentDocFragment.newInstance("A_AC_AND_DC.html");
                if (experimentTitle.equals(context.getResources().getString(R.string.ac_generator)))
                    return ExperimentDocFragment.newInstance("C_AC_GENERATOR.html");
                if (experimentTitle.equals(context.getResources().getString(R.string.capacitance)))
                    return ExperimentDocFragment.newInstance("I_CAPACITANCE.html");
                if (experimentTitle.equals(context.getResources().getString(R.string.resistance)))
                    return ExperimentDocFragment.newInstance("D_RESISTANCE.html");
                if (experimentTitle.equals(context.getResources().getString(R.string.em_induction)))
                    return ExperimentDocFragment.newInstance("F_EM_INDUCTION.html");
                if (experimentTitle.equals(context.getResources().getString(R.string.sound_beats)))
                    return ExperimentDocFragment.newInstance("H_SOUND_BEATS.html");
                if (experimentTitle.equals(context.getResources().getString(R.string.dust_sensor)))
                    return ExperimentDocFragment.newInstance("DUST_SENSOR.html");
                if (experimentTitle.equals(context.getResources().getString(R.string.lemon_cell)))
                    return ExperimentDocFragment.newInstance("B_LEMON_CELL.html");
                if (experimentTitle.equals(context.getResources().getString(R.string.full_wave_rectifier)))
                    return ExperimentDocFragment.newInstance("M_FullWave.html");
            case 1:
                if (experimentTitle.equals(context.getResources().getString(R.string.zener_iv)))
                    return ZenerSetupFragment.newInstance();
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
