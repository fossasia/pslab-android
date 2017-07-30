package org.fossasia.pslab.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import org.fossasia.pslab.R;
import org.fossasia.pslab.activity.PerformExperimentActivity;
import org.fossasia.pslab.experimentsetup.OhmsLawSetupExperiment;
import org.fossasia.pslab.experimentsetup.TransistorCBSetup;
import org.fossasia.pslab.experimentsetup.TransistorCEOutputSetup;
import org.fossasia.pslab.experimentsetup.ZenerSetupFragment;
import org.fossasia.pslab.items.ExperimentHeaderHolder;
import org.fossasia.pslab.items.IndividualExperimentHolder;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by viveksb007 on 15/3/17.
 * Modified by Padmal on 30/7/17
 */

public class SavedExperiments extends Fragment {

    private Unbinder unbinder;
    private Context context;
    private ViewGroup viewGroup;

    @BindView(R.id.saved_experiment_container)
    LinearLayout experimentListContainer;

    public static SavedExperiments newInstance() {
        return new SavedExperiments();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.saved_experiments_fragment, container, false);
        viewGroup = container;
        unbinder = ButterKnife.bind(this, view);
        // Main Root
        final TreeNode Root = TreeNode.root();
        Root.addChildren(
                loadElectronicExperiments(),
                loadElectricalExperiments(),
                loadPhysicsExperiments(),
                loadAddOnModules(),
                loadSchoolExperiments()
        );
        // Set up the tree view
        AndroidTreeView experimentsListTree = new AndroidTreeView(getActivity(), Root);
        experimentsListTree.setDefaultAnimation(true);
        experimentListContainer.addView(experimentsListTree.getView());

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private TreeNode loadElectronicExperiments() {
        // Electronic Tree Node
        TreeNode treeElectronics = new TreeNode(new ExperimentHeaderHolder.ExperimentHeader("Electronics Experiments", 0))
                .setViewHolder(new ExperimentHeaderHolder(context));
        // Sub nodes of electronics experiments
        TreeNode treeBJT = new TreeNode(new ExperimentHeaderHolder.ExperimentHeader("BJTs and FETs", 1))
                .setViewHolder(new ExperimentHeaderHolder(context));
        treeBJT = loadBJTTree(treeBJT);
        TreeNode treeDiode = new TreeNode(new ExperimentHeaderHolder.ExperimentHeader("Diode Circuits", 1))
                .setViewHolder(new ExperimentHeaderHolder(context));
        treeDiode = loadDiodeTree(treeDiode);
        TreeNode treeOpAmp = new TreeNode(new ExperimentHeaderHolder.ExperimentHeader("OpAmp Circuits", 1))
                .setViewHolder(new ExperimentHeaderHolder(context));
        treeOpAmp = loadOpAmpTree(treeOpAmp);
        TreeNode treeOsc = new TreeNode(new ExperimentHeaderHolder.ExperimentHeader("Oscillators", 1))
                .setViewHolder(new ExperimentHeaderHolder(context));
        treeOsc = loadOscTree(treeOsc);
        TreeNode treeCom = new TreeNode(new ExperimentHeaderHolder.ExperimentHeader("Communication", 1))
                .setViewHolder(new ExperimentHeaderHolder(context));
        treeCom = loadComTree(treeCom);
        treeElectronics.addChildren(treeBJT, treeDiode, treeOpAmp, treeOsc, treeCom);
        // Return Electronics Experiments List
        return treeElectronics;
    }

    private TreeNode loadBJTTree(TreeNode tree) {
        TreeNode treeNFETOutput = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("N-FET Output Characteristics"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
            @Override
            public void onClick(TreeNode node, Object value) {
                Intent intent = new Intent(context, PerformExperimentActivity.class);
                intent.putExtra("toolbar_title", ((IndividualExperimentHolder.IndividualExperiment) value).label);
                intent.putExtra("experiment_title", ((IndividualExperimentHolder.IndividualExperiment) value).label);
                startActivity(intent);
            }
        });
        TreeNode treeNFETTransfer = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("N-FET Transfer Characteristics"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        // Open NFET Transfer Experiment
                    }
                });
        TreeNode treeBJTCB = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("BJT CB Characteristics"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        startExperiment(TransistorCBSetup.newInstance(), value);
                    }
                });
        TreeNode treeBJTCEOutput = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("BJT Output Characteristics"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        startExperiment(TransistorCEOutputSetup.newInstance(), value);
                    }
                });
        TreeNode treeBJTCEInput = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("BJT Input Characteristics"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        // Open BJT CE Input Experiment
                    }
                });
        TreeNode treeBJTTransfer = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("BJT Transfer Characteristics"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        // Open BJT Transfer Experiment
                    }
                });
        TreeNode treeBJTCEBackup = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("BJT CE Backup"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        // Open BJT CE Backup experiment
                    }
                });
        TreeNode treeBJTAmplifier = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("BJT Amplifier"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        // Open BJT Amplifier Experiment
                    }
                });
        tree.addChildren(treeNFETOutput, treeNFETTransfer, treeBJTCB, treeBJTCEOutput, treeBJTCEInput, treeBJTTransfer, treeBJTCEBackup, treeBJTAmplifier);
        return tree;
    }

    private TreeNode loadDiodeTree(TreeNode tree) {
        TreeNode treeZener = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("Zener IV Characteristics"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        startExperiment(ZenerSetupFragment.newInstance(), value);
                    }
                });
        TreeNode treeDiode = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("Diode IV Characteristics"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {

                    }
                });
        TreeNode treeDiodeClamp = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("Diode Clamping"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {

                    }
                });
        TreeNode treeDiodeClip = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("Diode Clipping"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {

                    }
                });
        TreeNode treeHalfRectifier = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("Half Wave Rectifier"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {

                    }
                });
        TreeNode treeFullWave = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("Full Wave Rectifier"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {

                    }
                });
        tree.addChildren(treeZener, treeDiode, treeDiodeClamp, treeDiodeClip, treeHalfRectifier, treeFullWave);
        return tree;
    }

    private TreeNode loadOpAmpTree(TreeNode tree) {
        TreeNode treeInverting = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("Inverting Op-Amp"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {

                    }
                });
        TreeNode treeRamp = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("Ramp Generator"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {

                    }
                });
        TreeNode treeNonInverting = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("Non Inverting Op-Amp"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {

                    }
                });
        TreeNode treeSumming = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("Summing Junction"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {

                    }
                });
        TreeNode treePrecision = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("Precision Rectifier"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {

                    }
                });
        tree.addChildren(treeInverting, treeRamp, treeNonInverting, treeSumming, treePrecision);
        return tree;
    }

    private TreeNode loadOscTree(TreeNode tree) {
        TreeNode treeAsable = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("Astable Multivibrator"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {

                    }
                });
        TreeNode treeColpitts = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("Colpitts Oscillator"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {

                    }
                });
        TreeNode treePhase = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("Phase Shift Oscillator"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {

                    }
                });
        TreeNode treeWien = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("Wien Bridge Oscillator"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {

                    }
                });
        TreeNode treeMono = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("Monostable Multivibrator"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {

                    }
                });
        tree.addChildren(treeAsable, treeColpitts, treePhase, treeWien, treeMono);
        return tree;
    }

    private TreeNode loadComTree(TreeNode tree) {
        TreeNode treeAM = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("Amplitude Modulation"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {

                    }
                });
        tree.addChild(treeAM);
        return tree;
    }

    private TreeNode loadElectricalExperiments() {
        // Electrical Tree Node
        TreeNode treeElectrical = new TreeNode(new ExperimentHeaderHolder.ExperimentHeader("Electrical Experiments", 0))
                .setViewHolder(new ExperimentHeaderHolder(context));
        treeElectrical = loadElectricalTree(treeElectrical);
        // Return Electrical Experiments List
        return treeElectrical;
    }

    private TreeNode loadElectricalTree(TreeNode tree) {
        TreeNode treeRLC = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("Transient RLC Response"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {

                    }
                });
        TreeNode treeFilter = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("Filter Characteristics"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {

                    }
                });
        TreeNode treeCapReactance = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("Capacitive Reactance"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {

                    }
                });
        TreeNode treeIndReactance = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("Inductive Reactance"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {

                    }
                });
        TreeNode treeOhm = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("Ohm's Law"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        startExperiment(OhmsLawSetupExperiment.newInstance(), value);
                    }
                });
        TreeNode treeRCPhase = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("RC Phase Shift"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {

                    }
                });
        TreeNode treeLRPhase = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("LR Phase Shift"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {

                    }
                });
        TreeNode treeLRC = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("LCR Steady State"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {

                    }
                });
        TreeNode treeRCIntegral = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("RC Integrals, Derivatives"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {

                    }
                });
        TreeNode treeLPF = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("Voltage Controlled Low Pass Filter"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {

                    }
                });
        tree.addChildren(treeRLC, treeFilter, treeCapReactance, treeIndReactance, treeOhm, treeRCPhase, treeLRPhase, treeLRC, treeRCIntegral, treeLPF);
        return tree;
    }

    private TreeNode loadPhysicsExperiments() {
        // Physics Tree Node
        TreeNode treePhysics = new TreeNode(new ExperimentHeaderHolder.ExperimentHeader("Physics Experiments", 0))
                .setViewHolder(new ExperimentHeaderHolder(context));
        treePhysics = loadPhysicsTree(treePhysics);
        // Return Physics Experiments List
        return treePhysics;
    }

    private TreeNode loadPhysicsTree(TreeNode tree) {
        TreeNode treeSoundSpeed = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("Speed of Sound"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {

                    }
                });
        TreeNode treePendulum = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("Pendulum Time Period"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {

                    }
                });
        TreeNode treeRandomSample = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("Random Sampling"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {

                    }
                });
        TreeNode treeSimplePendulum = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("Simple Pendulum"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {

                    }
                });
        TreeNode treePiezo = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("Piezo Bandwidth Characteristics"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {

                    }
                });
        TreeNode treeMPU = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("MPU6050 IMU Pendulum"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {

                    }
                });
        tree.addChildren(treeSoundSpeed, treePendulum, treeRandomSample, treeSimplePendulum, treePiezo, treeMPU);
        return tree;
    }

    private TreeNode loadAddOnModules() {
        // Add Ons Tree Node
        TreeNode treeAddOnModules = new TreeNode(new ExperimentHeaderHolder.ExperimentHeader("Add-On Modules", 0))
                .setViewHolder(new ExperimentHeaderHolder(context));
        // Sub nodes of Add Ons experiments
        TreeNode treeAddOns = new TreeNode(new ExperimentHeaderHolder.ExperimentHeader("Add-On Modules", 1))
                .setViewHolder(new ExperimentHeaderHolder(context));
        treeAddOns = loadAddOnsTree(treeAddOns);
        TreeNode treeVaries = new TreeNode(new ExperimentHeaderHolder.ExperimentHeader("Various Utilities", 1))
                .setViewHolder(new ExperimentHeaderHolder(context));
        treeVaries = loadVariesTree(treeVaries);
        treeAddOnModules.addChildren(treeAddOns, treeVaries);
        // Return Add Ons Experiments List
        return treeAddOnModules;
    }

    private TreeNode loadAddOnsTree(TreeNode tree) {
        TreeNode treeDust = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("Dust Sensor DSM501"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {

                    }
                });
        TreeNode treeOLED = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("OLED Display"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {

                    }
                });
        TreeNode treeRGB = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("Wireless RGB Lights"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {

                    }
                });
        TreeNode treeTMP = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("Sensor TMP Logger"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {

                    }
                });
        TreeNode treeRFID = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("RFID Reader MF522"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {

                    }
                });
        TreeNode treeWS2812B = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("RGB LED WS2812B"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {

                    }
                });
        tree.addChildren(treeDust, treeOLED, treeRGB, treeTMP, treeRFID, treeWS2812B);
        return tree;
    }

    private TreeNode loadVariesTree(TreeNode tree) {
        TreeNode treeADS1115 = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("ADS1115 Based Calibrator"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {

                    }
                });
        TreeNode treeDeviceTest = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("Device Testing"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {

                    }
                });
        TreeNode treeServo = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("Servo Motors"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {

                    }
                });
        TreeNode treeStepper = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("Stepper Motors"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {

                    }
                });
        TreeNode treeFlowChart = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("Make a Flow Chart"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {

                    }
                });
        TreeNode treeCalib = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("Calibration Loader"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {

                    }
                });
        TreeNode treeDeviceCalib = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("Device Calibrator"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {

                    }
                });
        TreeNode treeRemote = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("Remote Access"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {

                    }
                });
        tree.addChildren(treeADS1115, treeDeviceTest, treeServo, treeStepper, treeFlowChart, treeCalib, treeDeviceCalib, treeRemote);
        return tree;
    }

    private TreeNode loadSchoolExperiments() {
        // School Experiment Tree Node
        TreeNode treeGettingStarted = new TreeNode(new ExperimentHeaderHolder.ExperimentHeader("School Level", 0))
                .setViewHolder(new ExperimentHeaderHolder(context));
        treeGettingStarted = loadSchoolTree(treeGettingStarted);
        // Return School Experiments List
        return treeGettingStarted;
    }

    private TreeNode loadSchoolTree(TreeNode tree) {
        TreeNode treeMeasureVoltage = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("Measure Voltages"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {

                    }
                });
        TreeNode treeLemonCell = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("Lemon Cell"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {

                    }
                });
        TreeNode treeACGen = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("AC Generator"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {

                    }
                });
        TreeNode treeResistance = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("Resistance Measurement"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {

                    }
                });
        TreeNode treeOhmsLaw = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("Ohm's Law"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        startExperiment(OhmsLawSetupExperiment.newInstance(), value);
                    }
                });
        TreeNode treeBodyResistance = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("Human Body Resistance"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {

                    }
                });
        TreeNode treeElectromagnetic = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("Electromagnetic Induction"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {

                    }
                });
        TreeNode treeResistanceWater = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("Resistance of Water"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {

                    }
                });
        TreeNode treeSound = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("Frequency of Sound"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {

                    }
                });
        TreeNode treePiezo = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("Piezo Buzzer"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {

                    }
                });
        TreeNode treeSoundB = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("Sound Beats Phenomenon"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {

                    }
                });
        TreeNode treeCap = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("Capacitance Measurement"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {

                    }
                });
        TreeNode treeSemiconductor = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("Semiconductor Diode"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {

                    }
                });
        TreeNode treeLDR = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("Light Dependent Resistor"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {

                    }
                });
        TreeNode treeCapDis = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("Capacitor Discharge"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {

                    }
                });
        TreeNode treeUltra = new TreeNode(new IndividualExperimentHolder.IndividualExperiment("Ultrasonic Range Finder"))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {

                    }
                });
        tree.addChildren(treeMeasureVoltage, treeLemonCell, treeACGen, treeResistance, treeOhmsLaw,
                treeBodyResistance, treeElectromagnetic, treeResistanceWater, treeSound, treePiezo, treeSoundB,
                treeCap, treeSemiconductor, treeLDR, treeCapDis, treeUltra);
        return tree;
    }

    private void startExperiment(Fragment fragment, Object value) {
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
                android.R.anim.fade_out);
        fragmentTransaction.replace(viewGroup.getId(), fragment, "Experiments");
        fragmentTransaction.commitNowAllowingStateLoss();
    }

}
