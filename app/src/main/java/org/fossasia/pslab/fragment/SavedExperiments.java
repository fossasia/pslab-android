package org.fossasia.pslab.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import org.fossasia.pslab.PSLabApplication;
import org.fossasia.pslab.R;
import org.fossasia.pslab.activity.PerformExperimentActivity;
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

        ((PSLabApplication)getActivity().getApplication()).refWatcher.watch(this, SavedExperiments.class.getSimpleName());
    }

    private TreeNode loadElectronicExperiments() {
        // Electronic Tree Node
        TreeNode treeElectronics = new TreeNode(new ExperimentHeaderHolder.ExperimentHeader(getString(R.string.electronics_experiments), 0))
                .setViewHolder(new ExperimentHeaderHolder(context));
        // Sub nodes of electronics experiments
        TreeNode treeBJT = new TreeNode(new ExperimentHeaderHolder.ExperimentHeader(getString(R.string.bjts_and_fets), 1))
                .setViewHolder(new ExperimentHeaderHolder(context));
        treeBJT = loadBJTTree(treeBJT);
        TreeNode treeDiode = new TreeNode(new ExperimentHeaderHolder.ExperimentHeader(getString(R.string.diode_circuits), 1))
                .setViewHolder(new ExperimentHeaderHolder(context));
        treeDiode = loadDiodeTree(treeDiode);
        TreeNode treeOpAmp = new TreeNode(new ExperimentHeaderHolder.ExperimentHeader(getString(R.string.opamp_circuits), 1))
                .setViewHolder(new ExperimentHeaderHolder(context));
        treeOpAmp = loadOpAmpTree(treeOpAmp);
        TreeNode treeOsc = new TreeNode(new ExperimentHeaderHolder.ExperimentHeader(getString(R.string.oscillators), 1))
                .setViewHolder(new ExperimentHeaderHolder(context));
        treeOsc = loadOscTree(treeOsc);
        TreeNode treeCom = new TreeNode(new ExperimentHeaderHolder.ExperimentHeader(getString(R.string.communication), 1))
                .setViewHolder(new ExperimentHeaderHolder(context));
        treeCom = loadComTree(treeCom);
        treeElectronics.addChildren(treeBJT, treeDiode, treeOpAmp, treeOsc, treeCom);
        // Return Electronics Experiments List
        return treeElectronics;
    }

    private TreeNode loadBJTTree(TreeNode tree) {
        TreeNode treeNFETOutput = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.nfet_output_characteristics)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        startExperiment(value);
                    }
                });
        TreeNode treeNFETTransfer = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.nfet_transfer_characteristics)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        startExperiment(value);
                    }
                });
        TreeNode treeBJTCB = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.transistor_cb)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        startExperiment(value);
                    }
                });
        TreeNode treeBJTCEOutput = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.transistor_ce)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        startExperiment(value);
                    }
                });
        TreeNode treeBJTCEInput = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.bjt_input_characteristics)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        startExperiment(value);
                    }
                });
        TreeNode treeBJTTransfer = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.bjt_transfer_characteristics)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        startExperiment(value);
                    }
                });
        TreeNode treeBJTCEBackup = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.bjt_ce_backup)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        // Open BJT CE Backup experiment
                        DisplayTemporaryToast();
                    }
                });
        TreeNode treeBJTAmplifier = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.bjt_amplifer)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        startExperiment(value);
                    }
                });
        tree.addChildren(treeNFETOutput, treeNFETTransfer, treeBJTCB, treeBJTCEOutput, treeBJTCEInput, treeBJTTransfer, treeBJTCEBackup, treeBJTAmplifier);
        return tree;
    }

    private TreeNode loadDiodeTree(TreeNode tree) {
        TreeNode treeZener = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.zener_iv)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        startExperiment(value);
                    }
                });
        TreeNode treeDiode = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.diode_iv)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        startExperiment(value);
                    }
                });
        TreeNode treeDiodeClamp = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.diode_clamping)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        startExperiment(value);
                    }
                });
        TreeNode treeDiodeClip = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.diode_clipping)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        startExperiment(value);
                    }
                });
        TreeNode treeHalfRectifier = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.half_wave_rectifier)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        startExperiment(value);
                    }
                });
        TreeNode treeFullWave = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.full_wave_rectifier)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        startExperiment(value);
                    }
                });
        tree.addChildren(treeZener, treeDiode, treeDiodeClamp, treeDiodeClip, treeHalfRectifier, treeFullWave);
        return tree;
    }

    private TreeNode loadOpAmpTree(TreeNode tree) {
        TreeNode treeInverting = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.inverting_opamp)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        startExperiment(value);
                    }
                });
        TreeNode treeRamp = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.ramp_generator)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        startExperiment(value);
                    }
                });
        TreeNode treeNonInverting = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.non_inverting_opamp)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        startExperiment(value);
                    }
                });
        TreeNode treeSumming = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.summing_junction)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        startExperiment(value);
                    }
                });
        TreeNode treePrecision = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.precision_rectifier)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        startExperiment(value);
                    }
                });
        tree.addChildren(treeInverting, treeRamp, treeNonInverting, treeSumming, treePrecision);
        return tree;
    }

    private TreeNode loadOscTree(TreeNode tree) {
        TreeNode treeAsable = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.astable_multivibrator)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        startExperiment(value);
                    }
                });
        TreeNode treeColpitts = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.colpitts_oscillator)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        startExperiment(value);
                    }
                });
        TreeNode treePhase = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.phase_shift_oscillator)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        startExperiment(value);
                    }
                });
        TreeNode treeWien = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.wien_bridge_oscillator)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        startExperiment(value);
                    }
                });
        TreeNode treeMono = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.monostable_multivibrator)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        startExperiment(value);
                    }
                });
        tree.addChildren(treeAsable, treeColpitts, treePhase, treeWien, treeMono);
        return tree;
    }

    private TreeNode loadComTree(TreeNode tree) {
        TreeNode treeAM = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.amplitude_modulation)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        DisplayTemporaryToast();
                    }
                });
        tree.addChild(treeAM);
        return tree;
    }

    private TreeNode loadElectricalExperiments() {
        // Electrical Tree Node
        TreeNode treeElectrical = new TreeNode(new ExperimentHeaderHolder.ExperimentHeader(getString(R.string.electrical_experiments), 0))
                .setViewHolder(new ExperimentHeaderHolder(context));
        treeElectrical = loadElectricalTree(treeElectrical);
        // Return Electrical Experiments List
        return treeElectrical;
    }

    private TreeNode loadElectricalTree(TreeNode tree) {
        TreeNode treeRLC = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.transient_rlc)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        DisplayTemporaryToast();
                    }
                });
        TreeNode treeFilter = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.filter_characteristics)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        DisplayTemporaryToast();
                    }
                });
        TreeNode treeCapReactance = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.capacitive_reactance)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        startExperiment(value);
                    }
                });
        TreeNode treeIndReactance = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.inductive_reactance)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        startExperiment(value);
                    }
                });
        TreeNode treeOhm = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.ohms_law)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        startExperiment(value);
                    }
                });
        TreeNode treeRCPhase = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.rc_phase_shift)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        startExperiment(value);
                    }
                });
        TreeNode treeLRPhase = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.lr_phase_shift)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        startExperiment(value);
                    }
                });
        TreeNode treeLRC = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.lcr_steady_state)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        DisplayTemporaryToast();
                    }
                });
        TreeNode treeRCIntegral = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.rc_integrals_derivatives)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                         startExperiment(value);
                    }
                });
        TreeNode treeLPF = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.voltage_controlled_low_pass_filter)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        DisplayTemporaryToast();
                    }
                });
        tree.addChildren(treeRLC, treeFilter, treeCapReactance, treeIndReactance, treeOhm, treeRCPhase, treeLRPhase, treeLRC, treeRCIntegral, treeLPF);
        return tree;
    }

    private TreeNode loadPhysicsExperiments() {
        // Physics Tree Node
        TreeNode treePhysics = new TreeNode(new ExperimentHeaderHolder.ExperimentHeader(getString(R.string.physics_experiments), 0))
                .setViewHolder(new ExperimentHeaderHolder(context));
        treePhysics = loadPhysicsTree(treePhysics);
        // Return Physics Experiments List
        return treePhysics;
    }

    private TreeNode loadPhysicsTree(TreeNode tree) {
        TreeNode treeSoundSpeed = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.speed_of_sound)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        startExperiment(value);
                    }
                });
        TreeNode treePendulum = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.pendulum_time)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        DisplayTemporaryToast();
                    }
                });
        TreeNode treeRandomSample = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.random_sampling)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        DisplayTemporaryToast();
                    }
                });
        TreeNode treeSimplePendulum = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.simple_pendulum)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        DisplayTemporaryToast();
                    }
                });
        TreeNode treePiezo = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.piezo_bandwidth_characteristics)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        DisplayTemporaryToast();
                    }
                });
        TreeNode treeMPU = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.mpu6050_imu_pendulum)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        DisplayTemporaryToast();
                    }
                });
        tree.addChildren(treeSoundSpeed, treePendulum, treeRandomSample, treeSimplePendulum, treePiezo, treeMPU);
        return tree;
    }

    private TreeNode loadAddOnModules() {
        // Add Ons Tree Node
        TreeNode treeAddOnModules = new TreeNode(new ExperimentHeaderHolder.ExperimentHeader(getString(R.string.add_on_modules), 0))
                .setViewHolder(new ExperimentHeaderHolder(context));
        // Sub nodes of Add Ons experiments
        TreeNode treeAddOns = new TreeNode(new ExperimentHeaderHolder.ExperimentHeader(getString(R.string.add_on_modules), 1))
                .setViewHolder(new ExperimentHeaderHolder(context));
        treeAddOns = loadAddOnsTree(treeAddOns);
        TreeNode treeVaries = new TreeNode(new ExperimentHeaderHolder.ExperimentHeader(getString(R.string.various_utilities), 1))
                .setViewHolder(new ExperimentHeaderHolder(context));
        treeVaries = loadVariesTree(treeVaries);
        treeAddOnModules.addChildren(treeAddOns, treeVaries);
        // Return Add Ons Experiments List
        return treeAddOnModules;
    }

    private TreeNode loadAddOnsTree(TreeNode tree) {
        TreeNode treeDust = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.dust_sensor)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        DisplayTemporaryToast();
                    }
                });
        TreeNode treeOLED = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.oled_display)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        DisplayTemporaryToast();
                    }
                });
        TreeNode treeRGB = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.wireless_rgb_lights)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        DisplayTemporaryToast();
                    }
                });
        TreeNode treeTMP = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.sensor_tmp_logger)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        DisplayTemporaryToast();
                    }
                });
        TreeNode treeRFID = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.rfid_reader_mf522)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        DisplayTemporaryToast();
                    }
                });
        TreeNode treeWS2812B = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.rgb_led_ws2812b)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        DisplayTemporaryToast();
                    }
                });
        tree.addChildren(treeDust, treeOLED, treeRGB, treeTMP, treeRFID, treeWS2812B);
        return tree;
    }

    private TreeNode loadVariesTree(TreeNode tree) {
        TreeNode treeADS1115 = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.ads1115_based_calibrator)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        DisplayTemporaryToast();
                    }
                });
        TreeNode treeDeviceTest = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.device_testing)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        DisplayTemporaryToast();
                    }
                });
        TreeNode treeServo = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.servo_motors)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        startExperiment(value);
                    }
                });
        TreeNode treeStepper = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.stepper_motors)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        startExperiment(value);
                    }
                });
        TreeNode treeFlowChart = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.make_a_flow_chart)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        DisplayTemporaryToast();
                    }
                });
        TreeNode treeCalib = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.calibration_loader)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        DisplayTemporaryToast();
                    }
                });
        TreeNode treeDeviceCalib = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.device_calibrator)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        DisplayTemporaryToast();
                    }
                });
        TreeNode treeRemote = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.remote_access)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        DisplayTemporaryToast();
                    }
                });
        tree.addChildren(treeADS1115, treeDeviceTest, treeServo, treeStepper, treeFlowChart, treeCalib, treeDeviceCalib, treeRemote);
        return tree;
    }

    private TreeNode loadSchoolExperiments() {
        // School Experiment Tree Node
        TreeNode treeGettingStarted = new TreeNode(new ExperimentHeaderHolder.ExperimentHeader(getString(R.string.school_level), 0))
                .setViewHolder(new ExperimentHeaderHolder(context));
        treeGettingStarted = loadSchoolTree(treeGettingStarted);
        // Return School Experiments List
        return treeGettingStarted;
    }

    private TreeNode loadSchoolTree(TreeNode tree) {
        TreeNode treeMeasureVoltage = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.measure_voltages)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                       startExperiment(value);
                    }
                });
        TreeNode treeLemonCell = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.lemon_cell)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        startExperiment(value);
                    }
                });
        TreeNode treeACGen = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.ac_generator)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        startExperiment(value);
                    }
                });
        TreeNode treeResistance = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.resistance_measurement)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        startExperiment(value);
                    }
                });
        TreeNode treeOhmsLaw = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.ohms_law)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        startExperiment(value);
                    }
                });
        TreeNode treeBodyResistance = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.human_body_resistance)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        startExperiment(value);
                    }
                });
        TreeNode treeElectromagnetic = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.em_induction)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        DisplayTemporaryToast();
                    }
                });
        TreeNode treeResistanceWater = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.resistance_of_water)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        startExperiment(value);
                    }
                });
        TreeNode treeSound = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.frequency_of_sound)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        DisplayTemporaryToast();
                    }
                });
        TreeNode treePiezo = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.piezo_buzzer)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        startExperiment(value);
                    }
                });
        TreeNode treeSoundB = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.sound_beats)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        DisplayTemporaryToast();
                    }
                });
        TreeNode treeCap = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.capacitance_measurement)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        startExperiment(value);
                    }
                });
        TreeNode treeSemiconductor = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.semiconductor_diode)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        startExperiment(value);
                    }
                });
        TreeNode treeLDR = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.light_dependent_resistor)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        startExperiment(value);
                    }
                });
        TreeNode treeCapDis = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.capacitor_discharge)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        startExperiment(value);
                    }
                });
        TreeNode treeUltra = new TreeNode(new IndividualExperimentHolder.IndividualExperiment(getString(R.string.ultrasonic_range_finder)))
                .setViewHolder(new IndividualExperimentHolder(context))
                .setClickListener(new TreeNode.TreeNodeClickListener() {
                    @Override
                    public void onClick(TreeNode node, Object value) {
                        startExperiment(value);
                    }
                });
        tree.addChildren(treeMeasureVoltage, treeLemonCell, treeACGen, treeResistance, treeOhmsLaw,
                treeBodyResistance, treeElectromagnetic, treeResistanceWater, treeSound, treePiezo, treeSoundB,
                treeCap, treeSemiconductor, treeLDR, treeCapDis, treeUltra);
        return tree;
    }

    private void startExperiment(Object value) {
        Intent intent = new Intent(context, PerformExperimentActivity.class);
        intent.putExtra("toolbar_title", ((IndividualExperimentHolder.IndividualExperiment) value).label);
        intent.putExtra("experiment_title", ((IndividualExperimentHolder.IndividualExperiment) value).label);
        startActivity(intent);
    }

    private void DisplayTemporaryToast() {
        Toast.makeText(getActivity(),getString(R.string.temporary_toast),Toast.LENGTH_SHORT).show();
    }

}
