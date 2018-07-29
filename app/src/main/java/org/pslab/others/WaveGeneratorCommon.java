package org.pslab.others;

import org.pslab.activity.WaveGeneratorActivity;
import org.pslab.activity.WaveGeneratorActivity.WaveConst;
import org.pslab.activity.WaveGeneratorActivity.WaveData;

import java.util.HashMap;
import java.util.Map;

public class WaveGeneratorCommon {

    public static HashMap<WaveGeneratorActivity.WaveConst, HashMap<WaveConst, Integer>> wave;
    public static boolean isInitialized = false;
    public static WaveConst mode_selected;
    public static Map<String, Integer> state ;

    public WaveGeneratorCommon(boolean flag) {
        mode_selected = WaveConst.SQUARE;
        isInitialized = flag;
        wave = new HashMap<>();
        state = new HashMap<>();

        wave.put(WaveConst.WAVE1, new HashMap<WaveConst, Integer>());
        wave.put(WaveConst.WAVE2, new HashMap<WaveConst, Integer>());
        wave.put(WaveConst.WAVETYPE, new HashMap<WaveConst, Integer>());

        wave.put(WaveConst.SQR1, new HashMap<WaveConst, Integer>());
        wave.put(WaveConst.SQR2, new HashMap<WaveConst, Integer>());
        wave.put(WaveConst.SQR3, new HashMap<WaveConst, Integer>());
        wave.put(WaveConst.SQR4, new HashMap<WaveConst, Integer>());

        initializeStates();

        initializeWaveValue();

        intializeDigitalValue();
    }

    private void initializeStates() {
        state.put("SQR1", 0);
        state.put("SQR2", 0);
        state.put("SQR3", 0);
        state.put("SQR4", 0);
    }

    public static void initializeWaveValue() {
        wave.get(WaveConst.WAVE1).put(WaveConst.FREQUENCY, WaveData.FREQ_MIN.getValue());
        wave.get(WaveConst.WAVE1).put(WaveConst.WAVETYPE, WaveGeneratorActivity.SIN);

        wave.get(WaveConst.WAVE2).put(WaveConst.PHASE, WaveData.PHASE_MIN.getValue());
        wave.get(WaveConst.WAVE2).put(WaveConst.FREQUENCY, WaveData.FREQ_MIN.getValue());
        wave.get(WaveConst.WAVE2).put(WaveConst.WAVETYPE, WaveGeneratorActivity.SIN);
    }

    public static void intializeDigitalValue() {
        wave.get(WaveConst.SQR1).put(WaveConst.FREQUENCY, WaveData.FREQ_MIN.getValue()); //common frequency for all pins(SQR1,SQR2,SQR3,SQR4)
        wave.get(WaveConst.SQR1).put(WaveConst.DUTY, WaveData.DUTY_MIN.getValue());

        wave.get(WaveConst.SQR2).put(WaveConst.FREQUENCY, WaveData.FREQ_MIN.getValue());
        wave.get(WaveConst.SQR2).put(WaveConst.PHASE, WaveData.PHASE_MIN.getValue());
        wave.get(WaveConst.SQR2).put(WaveConst.DUTY, WaveData.DUTY_MIN.getValue());

        wave.get(WaveConst.SQR3).put(WaveConst.PHASE, WaveData.PHASE_MIN.getValue());
        wave.get(WaveConst.SQR3).put(WaveConst.DUTY, WaveData.DUTY_MIN.getValue());

        wave.get(WaveConst.SQR4).put(WaveConst.PHASE, WaveData.PHASE_MIN.getValue());
        wave.get(WaveConst.SQR4).put(WaveConst.DUTY, WaveData.DUTY_MIN.getValue());
    }
}