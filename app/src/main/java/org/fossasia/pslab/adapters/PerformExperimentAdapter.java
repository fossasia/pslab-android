package org.fossasia.pslab.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import org.fossasia.pslab.experimentsetup.HumanBodyResistanceExperiment;
import org.fossasia.pslab.experimentsetup.ACGeneratorExperiment;
import org.fossasia.pslab.experimentsetup.DistanceMeasurementExperiment;
import org.fossasia.pslab.experimentsetup.InvertingOpAmpExperiment;
import org.fossasia.pslab.experimentsetup.LemonCellExperiment;
import org.fossasia.pslab.experimentsetup.CapacitorDischargeExperiment;
import org.fossasia.pslab.experimentsetup.LightDependentResistorExperiment;
import org.fossasia.pslab.experimentsetup.NFETOutputCharacteristicsExperiment;
import org.fossasia.pslab.experimentsetup.OhmsLawSetupExperiment;
import org.fossasia.pslab.experimentsetup.DiodeExperiment;
import org.fossasia.pslab.experimentsetup.NFETTransferCharacteristicsExperiment;
import org.fossasia.pslab.experimentsetup.NonInvertingOpAmpExperiment;
import org.fossasia.pslab.experimentsetup.ServoMotorsExperiment;
import org.fossasia.pslab.experimentsetup.SummingJunctionExperiment;
import org.fossasia.pslab.experimentsetup.OscillatorExperiment;
import org.fossasia.pslab.experimentsetup.PrecisionRectifierExperiment;
import org.fossasia.pslab.experimentsetup.RampGeneratorExperiment;
import org.fossasia.pslab.experimentsetup.SemiConductorDiodeExperiment;
import org.fossasia.pslab.experimentsetup.TransistorAmplifierExperiment;
import org.fossasia.pslab.experimentsetup.TransistorCBSetup;
import org.fossasia.pslab.experimentsetup.TransistorCEInputCharacteristicsExperiment;
import org.fossasia.pslab.experimentsetup.TransistorCEOutputSetup;
import org.fossasia.pslab.experimentsetup.TransistorTransferExperiment;
import org.fossasia.pslab.experimentsetup.WaterResistanceExperiment;
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
                switch (experimentTitle) {
                    case "Diode IV Characteristics":
                        return ExperimentDocFragment.newInstance("D_diodeIV.html");
                    case "Zener IV Characteristics":
                        return ExperimentDocFragment.newInstance("D_ZenerIV.html");
                    case "Half Wave Rectifier":
                        return ExperimentDocFragment.newInstance("L_halfWave.html");
                    case "BJT CB Characteristics":
                        return ExperimentDocFragment.newInstance("D_transistorCB.html");
                    case "BJT Output Characteristics":
                        return ExperimentDocFragment.newInstance("D_transistorCE.html");
                    case "BJT Transfer Characteristics":
                        return ExperimentDocFragment.newInstance("D_transistorCE_transfer.html");
                    case "BJT Input Characteristics":
                        return ExperimentDocFragment.newInstance("D_transistorCE_input.html");
                    case "BJT Amplifier":
                        return ExperimentDocFragment.newInstance("L_TransistorAmplifier.md");
                    case "N-FET Output Characteristics":
                        return ExperimentDocFragment.newInstance("D_NFET.html");
                    case "N-FET Transfer Characteristics":
                        return ExperimentDocFragment.newInstance("D_NFET_GS_ID.md");
                    case "Astable Multivibrator":
                        return ExperimentDocFragment.newInstance("astable-multivibrator.html");
                    case "Colpitts Oscillator":
                        return ExperimentDocFragment.newInstance("L_Colpitts.md");
                    case "Phase Shift Oscillator":
                        return ExperimentDocFragment.newInstance("L_PhaseShift.md");
                    case "Wien Bridge Oscillator":
                        return ExperimentDocFragment.newInstance("L_WIEN_BRIDGE.md");
                    case "Transients RLC Response":
                        return ExperimentDocFragment.newInstance("E_transientRLC.html");
                    case "Bode Plots":
                        return ExperimentDocFragment.newInstance("K_bodePlots.html");
                    case "Ohms Law":
                        return ExperimentDocFragment.newInstance("E_OhmsLaw.html");
                    case "Random Sampling":
                        return ExperimentDocFragment.newInstance("M_RANDOM_SAMPLING.html");
                    case "AC and DC":
                        return ExperimentDocFragment.newInstance("A_AC_AND_DC.html");
                    case "AC Generator":
                        return ExperimentDocFragment.newInstance("C_AC_GENERATOR.html");
                    case "Capacitance":
                        return ExperimentDocFragment.newInstance("I_CAPACITANCE.html");
                    case "Resistance":
                        return ExperimentDocFragment.newInstance("D_RESISTANCE.html");
                    case "Electromagnetic Induction":
                        return ExperimentDocFragment.newInstance("F_EM_INDUCTION.html");
                    case "Sound Beats Phenomenon":
                        return ExperimentDocFragment.newInstance("H_SOUND_BEATS.html");
                    case "Dust Sensor DSM501":
                        return ExperimentDocFragment.newInstance("DUST_SENSOR.html");
                    case "Lemon Cell":
                        return ExperimentDocFragment.newInstance("B_LEMON_CELL.html");
                    case "Full Wave Rectifier":
                        return ExperimentDocFragment.newInstance("M_FullWave.html");
                    case "Diode Clipping":
                        return ExperimentDocFragment.newInstance("L_DiodeClipping.md");
                    case "Diode Clamping":
                        return ExperimentDocFragment.newInstance("L_DiodeClamping.md");
                    case "Inverting Op-Amp":
                        return ExperimentDocFragment.newInstance("L_Inverting.md");
                    case "Non Inverting Op-Amp":
                        return ExperimentDocFragment.newInstance("L_NonInverting.md");
                    case "Precision Rectifier":
                        return ExperimentDocFragment.newInstance("Precision_Rectifier.html");
                    case "Capacitor Discharge":
                        return ExperimentDocFragment.newInstance("L_CAPACITOR_DISCHARGE.md");
                    case "Resistance of Water":
                        return ExperimentDocFragment.newInstance("F_WATER_RESISTANCE.html");
                    case "Ramp Generator":
                        return ExperimentDocFragment.newInstance("L_LinearRampGen.html");
                    case "Light Dependent Resistor":
                        return ExperimentDocFragment.newInstance("K_LDR.md");
                    case "Ultrasonic Range Finder":
                        return ExperimentDocFragment.newInstance("Z_DISTANCE.md");
                    case "Summing Junction":
                        return ExperimentDocFragment.newInstance("L_Summing.html");
                    case "Semiconductor Diode":
                        return ExperimentDocFragment.newInstance("J_DIODE.md");
                    case "Human Body Resistance":
                        return ExperimentDocFragment.newInstance("E_RESISTANCE_BODY.html");
                    case "Servo Motors":
                        return ExperimentDocFragment.newInstance("G_servo_motors.md");
                }

            case 1:
                switch (experimentTitle) {
                    case "Diode IV Characteristics":
                        return ZenerSetupFragment.newInstance();
                    case "Zener IV Characteristics":
                        return ZenerSetupFragment.newInstance();
                    case "Half Wave Rectifier":
                        return DiodeExperiment.newInstance(context.getString(R.string.half_wave_rectifier));
                    case "BJT CB Characteristics":
                        return TransistorCBSetup.newInstance();
                    case "BJT Output Characteristics":
                        return TransistorCEOutputSetup.newInstance();
                    case "BJT Transfer Characteristics":
                        return TransistorTransferExperiment.newInstance();
                    case "BJT Input Characteristics":
                        return TransistorCEInputCharacteristicsExperiment.newInstance();
                    case "BJT Amplifier":
                        return TransistorAmplifierExperiment.newInstance();
                    case "N-FET Output Characteristics":
                        return NFETOutputCharacteristicsExperiment.newInstance();
                    case "N-FET Transfer Characteristics":
                        return NFETTransferCharacteristicsExperiment.newInstance();
                    case "Astable Multivibrator":
                        return OscillatorExperiment.newInstance("Astable Multivibrator");
                    case "Colpitts Oscillator":
                        return OscillatorExperiment.newInstance("Colpitts Oscillator");
                    case "Phase Shift Oscillator":
                        return OscillatorExperiment.newInstance("Phase Shift Oscillator");
                    case "Wien Bridge Oscillator":
                        return OscillatorExperiment.newInstance("Wien Bridge Oscillator");
                    case "Ohms Law":
                        return OhmsLawSetupExperiment.newInstance();
                    case "AC Generator":
                        return ACGeneratorExperiment.newInstance();
                    case "Lemon Cell":
                        return LemonCellExperiment.newInstance();
                    case "Full Wave Rectifier":
                        return DiodeExperiment.newInstance(context.getResources().getString(R.string.full_wave_rectifier));
                    case "Diode Clipping":
                        return DiodeExperiment.newInstance(context.getResources().getString(R.string.diode_clipping));
                    case "Diode Clamping":
                        return DiodeExperiment.newInstance(context.getResources().getString(R.string.diode_clamping));
                    case "Inverting Op-Amp":
                        return InvertingOpAmpExperiment.newInstance();
                    case "Non Inverting Op-Amp":
                        return NonInvertingOpAmpExperiment.newInstance();
                    case "Summing Junction":
                        return SummingJunctionExperiment.newInstance();
                    case "Precision Rectifier":
                        return PrecisionRectifierExperiment.newInstance();
                    case "Capacitor Discharge":
                        return CapacitorDischargeExperiment.newInstance();
                    case "Resistance of Water":
                        return WaterResistanceExperiment.newInstance();
                    case "Ramp Generator":
                        return RampGeneratorExperiment.newInstance();
                    case "Light Dependent Resistor":
                        return LightDependentResistorExperiment.newInstance();
                    case "Ultrasonic Range Finder":
                        return DistanceMeasurementExperiment.newInstance();
                    case "Semiconductor Diode":
                        return SemiConductorDiodeExperiment.newInstance();
                    case "Human Body Resistance":
                        return HumanBodyResistanceExperiment.newInstance();
                    case "Servo Motors":
                        return ServoMotorsExperiment.newInstance();
                    default:
                        return ExperimentSetupFragment.newInstance();
                }
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
