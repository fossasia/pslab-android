package org.fossasia.pslab.others;

import org.fossasia.pslab.activity.WaveGeneratorActivity;

import java.util.HashMap;

public class WaveGeneratorCommon {

    public static HashMap<String, HashMap<String, Integer>> wave;


    public WaveGeneratorCommon() {
        wave = new HashMap<>();

        wave.put(WaveGeneratorActivity.WAVE1, new HashMap<String, Integer>());
        wave.put(WaveGeneratorActivity.WAVE2, new HashMap<String, Integer>());

        wave.put(WaveGeneratorActivity.SQ1, new HashMap<String, Integer>());
        wave.put(WaveGeneratorActivity.SQ2, new HashMap<String, Integer>());
        wave.put(WaveGeneratorActivity.SQ3, new HashMap<String, Integer>());
        wave.put(WaveGeneratorActivity.SQ4, new HashMap<String, Integer>());

        wave.get(WaveGeneratorActivity.WAVE1).put(WaveGeneratorActivity.FREQUENCY, WaveGeneratorActivity.FREQ_MIN);
        wave.get(WaveGeneratorActivity.WAVE1).put(WaveGeneratorActivity.WAVETYPE, WaveGeneratorActivity.SIN);

        wave.get(WaveGeneratorActivity.WAVE2).put(WaveGeneratorActivity.PHASE, WaveGeneratorActivity.PHASE_MIN);
        wave.get(WaveGeneratorActivity.WAVE2).put(WaveGeneratorActivity.FREQUENCY, WaveGeneratorActivity.FREQ_MIN);
        wave.get(WaveGeneratorActivity.WAVE2).put(WaveGeneratorActivity.WAVETYPE, WaveGeneratorActivity.SIN);

        wave.get(WaveGeneratorActivity.SQ1).put(WaveGeneratorActivity.FREQUENCY, WaveGeneratorActivity.FREQ_MIN); //common frequency for all pins(SQ1,SQ2,SQ3,SQ4)
        wave.get(WaveGeneratorActivity.SQ1).put(WaveGeneratorActivity.DUTY, WaveGeneratorActivity.DUTY_MIN);

        wave.get(WaveGeneratorActivity.SQ2).put(WaveGeneratorActivity.PHASE, WaveGeneratorActivity.PHASE_MIN);
        wave.get(WaveGeneratorActivity.SQ2).put(WaveGeneratorActivity.DUTY, WaveGeneratorActivity.DUTY_MIN);

        wave.get(WaveGeneratorActivity.SQ3).put(WaveGeneratorActivity.PHASE, WaveGeneratorActivity.PHASE_MIN);
        wave.get(WaveGeneratorActivity.SQ3).put(WaveGeneratorActivity.DUTY, WaveGeneratorActivity.DUTY_MIN);

        wave.get(WaveGeneratorActivity.SQ4).put(WaveGeneratorActivity.PHASE, WaveGeneratorActivity.PHASE_MIN);
        wave.get(WaveGeneratorActivity.SQ4).put(WaveGeneratorActivity.DUTY, WaveGeneratorActivity.DUTY_MIN);
    }
}