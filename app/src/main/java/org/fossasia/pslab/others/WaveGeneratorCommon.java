package org.fossasia.pslab.others;

import org.fossasia.pslab.activity.WaveGeneratorActivity;
import org.fossasia.pslab.activity.WaveGeneratorActivity.WaveConst;
import org.fossasia.pslab.activity.WaveGeneratorActivity.WaveData;
import java.util.HashMap;

public class WaveGeneratorCommon {

    public static HashMap<WaveGeneratorActivity.WaveConst, HashMap<WaveConst, Integer>> wave;


    public WaveGeneratorCommon() {
        wave = new HashMap<>();

        wave.put(WaveConst.WAVE1, new HashMap<WaveConst, Integer>());
        wave.put(WaveConst.WAVE2, new HashMap<WaveConst, Integer>());
        wave.put(WaveConst.WAVETYPE,new HashMap<WaveConst, Integer>());

        wave.put(WaveConst.SQ1, new HashMap<WaveConst, Integer>());
        wave.put(WaveConst.SQ2, new HashMap<WaveConst, Integer>());
        wave.put(WaveConst.SQ3, new HashMap<WaveConst, Integer>());
        wave.put(WaveConst.SQ4, new HashMap<WaveConst, Integer>());

        wave.get(WaveConst.WAVE1).put(WaveConst.FREQUENCY, WaveData.FREQ_MIN.getValue());
        wave.get(WaveConst.WAVE1).put(WaveConst.WAVETYPE, WaveGeneratorActivity.SIN);

        wave.get(WaveConst.WAVE2).put(WaveConst.PHASE, WaveData.PHASE_MIN.getValue());
        wave.get(WaveConst.WAVE2).put(WaveConst.FREQUENCY, WaveData.FREQ_MIN.getValue());
        wave.get(WaveConst.WAVE2).put(WaveConst.WAVETYPE, WaveGeneratorActivity.SIN);

        wave.get(WaveConst.SQ1).put(WaveConst.FREQUENCY, WaveData.FREQ_MIN.getValue()); //common frequency for all pins(SQ1,SQ2,SQ3,SQ4)
        wave.get(WaveConst.SQ1).put(WaveConst.DUTY, WaveData.DUTY_MIN.getValue());

        wave.get(WaveConst.SQ2).put(WaveConst.PHASE, WaveData.PHASE_MIN.getValue());
        wave.get(WaveConst.SQ2).put(WaveConst.DUTY, WaveData.DUTY_MIN.getValue());

        wave.get(WaveConst.SQ3).put(WaveConst.PHASE, WaveData.PHASE_MIN.getValue());
        wave.get(WaveConst.SQ3).put(WaveConst.DUTY, WaveData.DUTY_MIN.getValue());

        wave.get(WaveConst.SQ4).put(WaveConst.PHASE, WaveData.PHASE_MIN.getValue());
        wave.get(WaveConst.SQ4).put(WaveConst.DUTY, WaveData.DUTY_MIN.getValue());
    }
}