package org.fossasia.pslab.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import org.fossasia.pslab.experimentsetup.schoollevel.MeasureVoltage;
import org.fossasia.pslab.experimentsetup.electricalexperiments.InductorReactanceExperiment;
import org.fossasia.pslab.experimentsetup.electricalexperiments.CapacitorReactanceExperiment;
import org.fossasia.pslab.experimentsetup.schoollevel.HumanBodyResistanceExperiment;
import org.fossasia.pslab.experimentsetup.schoollevel.ACGeneratorExperiment;
import org.fossasia.pslab.experimentsetup.schoollevel.DistanceMeasurementExperiment;
import org.fossasia.pslab.experimentsetup.electronicexperiments.InvertingOpAmpExperiment;
import org.fossasia.pslab.experimentsetup.electricalexperiments.LRPhaseShiftExperiment;
import org.fossasia.pslab.experimentsetup.schoollevel.LemonCellExperiment;
import org.fossasia.pslab.experimentsetup.schoollevel.CapacitorDischargeExperiment;
import org.fossasia.pslab.experimentsetup.schoollevel.LightDependentResistorExperiment;
import org.fossasia.pslab.experimentsetup.electronicexperiments.NFETOutputCharacteristicsExperiment;
import org.fossasia.pslab.experimentsetup.schoollevel.OhmsLawSetupExperiment;
import org.fossasia.pslab.experimentsetup.electronicexperiments.DiodeExperiment;
import org.fossasia.pslab.experimentsetup.electronicexperiments.NFETTransferCharacteristicsExperiment;
import org.fossasia.pslab.experimentsetup.electronicexperiments.NonInvertingOpAmpExperiment;
import org.fossasia.pslab.experimentsetup.electricalexperiments.RCPhaseShiftExperiment;
import org.fossasia.pslab.experimentsetup.electricalexperiments.RCIntegralandderivativeexperiment;
import org.fossasia.pslab.experimentsetup.addonmodules.ServoMotorsExperiment;
import org.fossasia.pslab.experimentsetup.electronicexperiments.SummingJunctionExperiment;
import org.fossasia.pslab.experimentsetup.electronicexperiments.OscillatorExperiment;
import org.fossasia.pslab.experimentsetup.electronicexperiments.PrecisionRectifierExperiment;
import org.fossasia.pslab.experimentsetup.electronicexperiments.RampGeneratorExperiment;
import org.fossasia.pslab.experimentsetup.schoollevel.SemiConductorDiodeExperiment;
import org.fossasia.pslab.experimentsetup.addonmodules.StepperMotors;
import org.fossasia.pslab.experimentsetup.electronicexperiments.TransistorAmplifierExperiment;
import org.fossasia.pslab.experimentsetup.electronicexperiments.TransistorCBSetup;
import org.fossasia.pslab.experimentsetup.electronicexperiments.TransistorCEInputCharacteristicsExperiment;
import org.fossasia.pslab.experimentsetup.electronicexperiments.TransistorCEOutputSetup;
import org.fossasia.pslab.experimentsetup.electronicexperiments.TransistorTransferExperiment;
import org.fossasia.pslab.experimentsetup.schoollevel.WaterResistanceExperiment;
import org.fossasia.pslab.experimentsetup.electronicexperiments.ZenerSetupFragment;
import org.fossasia.pslab.R;
import org.fossasia.pslab.fragment.ExperimentDocFragment;
import org.fossasia.pslab.fragment.ExperimentDocMdFragment;
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
                        return ExperimentDocMdFragment.newInstance("D_diodeIV.md");
                    case "Zener IV Characteristics":
                        return ExperimentDocMdFragment.newInstance("D_ZenerIV.md");
                    case "Half Wave Rectifier":
                        return ExperimentDocMdFragment.newInstance("L_halfWave.md");
                    case "BJT CB Characteristics":
                        return ExperimentDocMdFragment.newInstance("D_transistorCB.md");
                    case "BJT Output Characteristics":
                        return ExperimentDocMdFragment.newInstance("D_transistorCE.md");
                    case "BJT Transfer Characteristics":
                        return ExperimentDocMdFragment.newInstance("D_transistorCE_transfer.md");
                    case "BJT Input Characteristics":
                        return ExperimentDocMdFragment.newInstance("D_transistorCE_input.md");
                    case "BJT Amplifier":
                        return ExperimentDocMdFragment.newInstance("L_TransistorAmplifier.md");
                    case "N-FET Output Characteristics":
                        return ExperimentDocMdFragment.newInstance("D_NFET.md");
                    case "N-FET Transfer Characteristics":
                        return ExperimentDocMdFragment.newInstance("D_NFET_GS_ID.md");
                    case "Astable Multivibrator":
                        return ExperimentDocMdFragment.newInstance("astable-multivibrator.md");
                    case "Monostable Multivibrator":
                        return ExperimentDocMdFragment.newInstance("M_Monostable.md");
                    case "Colpitts Oscillator":
                        return ExperimentDocMdFragment.newInstance("L_Colpitts.md");
                    case "Phase Shift Oscillator":
                        return ExperimentDocMdFragment.newInstance("L_PhaseShift.md");
                    case "Wien Bridge Oscillator":
                        return ExperimentDocMdFragment.newInstance("L_WIEN_BRIDGE.md");
                    case "Transients RLC Response":
                        return ExperimentDocMdFragment.newInstance("E_transientRLC.md");
                    case "Bode Plots":
                        return ExperimentDocFragment.newInstance("K_bodePlots.html");
                    case "Ohms Law":
                        return ExperimentDocMdFragment.newInstance("E_OhmsLaw.md");
                    case "Random Sampling":
                        return ExperimentDocMdFragment.newInstance("M_RANDOM_SAMPLING.md");
                   // case "AC and DC":
                     //   return ExperimentDocMdFragment.newInstance("A_AC_AND_DC.md");
                    case "AC Generator":
                        return ExperimentDocMdFragment.newInstance("C_AC_GENERATOR.md");
                    case "Capacitance Measurement":
                        return ExperimentDocMdFragment.newInstance("I_CAPACITANCE.md");
                    case "Resistance":
                        return ExperimentDocMdFragment.newInstance("D_RESISTANCE.md");
                    case "Measure Voltages":
                        return ExperimentDocMdFragment.newInstance("A_AC_AND_DC.md");
                    case "Electromagnetic Induction":
                        return ExperimentDocMdFragment.newInstance("F_EM_INDUCTION.md");
                    case "Sound Beats Phenomenon":
                        return ExperimentDocMdFragment.newInstance("H_SOUND_BEATS.md");
                    case "Dust Sensor DSM501":
                        return ExperimentDocMdFragment.newInstance("DUST_SENSOR.md");
                    case "Lemon Cell":
                        return ExperimentDocMdFragment.newInstance("B_LEMON_CELL.md");
                    case "Full Wave Rectifier":
                        return ExperimentDocMdFragment.newInstance("M_FullWave.md");
                    case "Diode Clipping":
                        return ExperimentDocMdFragment.newInstance("L_DiodeClipping.md");
                    case "Diode Clamping":
                        return ExperimentDocMdFragment.newInstance("L_DiodeClamping.md");
                    case "Inverting Op-Amp":
                        return ExperimentDocMdFragment.newInstance("L_Inverting.md");
                    case "Non Inverting Op-Amp":
                        return ExperimentDocMdFragment.newInstance("L_NonInverting.md");
                    case "Precision Rectifier":
                        return ExperimentDocMdFragment.newInstance("Precision_Rectifier.md");
                    case "Capacitor Discharge":
                        return ExperimentDocMdFragment.newInstance("L_CAPACITOR_DISCHARGE.md");
                    case "Resistance of Water":
                        return ExperimentDocMdFragment.newInstance("F_WATER_RESISTANCE.md");
                    case "Ramp Generator":
                        return ExperimentDocMdFragment.newInstance("L_LinearRampGen.md");
                    case "Light Dependent Resistor":
                        return ExperimentDocMdFragment.newInstance("K_LDR.md");
                    case "Ultrasonic Range Finder":
                        return ExperimentDocMdFragment.newInstance("Z_DISTANCE.md");
                    case "Inductive Reactance":
                        return ExperimentDocMdFragment.newInstance("O_XL.md");
                    case "LR Phase Shift":
                        return ExperimentDocMdFragment.newInstance("P_InductivePhaseShift.md");
                    case "RC Phase Shift":
                        return ExperimentDocMdFragment.newInstance("P_CapacitivePhaseShift.md");
                    case "Capacitive Reactance":
                        return ExperimentDocMdFragment.newInstance("O_XC.md");
                    case "RC Integrals, Derivatives":
                        return ExperimentDocMdFragment.newInstance("Q_RC_integ_deriv.md");
                    case "Summing Junction":
                        return ExperimentDocMdFragment.newInstance("L_Summing.md");
                    case "Semiconductor Diode":
                        return ExperimentDocMdFragment.newInstance("J_DIODE.md");
                    case "Human Body Resistance":
                        return ExperimentDocMdFragment.newInstance("E_RESISTANCE_BODY.md");
                    case "Stepper Motors":
                        return ExperimentDocMdFragment.newInstance("J_stepper.md");
                    case "Servo Motors":
                        return ExperimentDocMdFragment.newInstance("G_servo_motors.md");
                    case "Speed of Sound":
                        return ExperimentDocMdFragment.newInstance("SpeedOfSound.md");
                    case "Resistance Measurement":
                        return ExperimentDocMdFragment.newInstance("R_ResistanceMeasurement.md");
                    case "Piezo Buzzer":
                        return ExperimentDocMdFragment.newInstance("P_PiezoBuzzer.md");
                }

            case 1:
                switch (experimentTitle) {
                    case "Diode IV Characteristics":
                        return DiodeExperiment.newInstance("Diode IV Characteristics");
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
                    case "Monostable Multivibrator":
                        return OscillatorExperiment.newInstance("Monostable Multivibrator");
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
                    case "Inductive Reactance":
                        return InductorReactanceExperiment.newInstance();
                    case "LR Phase Shift":
                        return LRPhaseShiftExperiment.newInstance();
                    case "RC Phase Shift":
                        return RCPhaseShiftExperiment.newInstance();
                    case "Measure Voltages":
                        return MeasureVoltage.newInstance();
                    case "Capacitive Reactance":
                        return CapacitorReactanceExperiment.newInstance();
                    case "RC Integrals, Derivatives":
                        return RCIntegralandderivativeexperiment.newInstance();
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
                    case "Stepper Motors":
                        return StepperMotors.newInstance();
                    case "Servo Motors":
                        return ServoMotorsExperiment.newInstance();
                    case "Speed of Sound":
                        return OscillatorExperiment.newInstance("Speed of Sound");
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
