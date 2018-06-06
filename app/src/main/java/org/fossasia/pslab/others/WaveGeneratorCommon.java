package org.fossasia.pslab.others;

import org.fossasia.pslab.activity.WaveGeneratorActivity;

import java.util.HashMap;

public class WaveGeneratorCommon {

    public static HashMap<String, HashMap<String, Integer>> wave;

    public WaveGeneratorCommon() {
        wave = new HashMap<>();

        wave.put(WaveGeneratorActivity.WAVE1, new HashMap<String, Integer>());
        wave.put(WaveGeneratorActivity.WAVE2, new HashMap<String, Integer>());

        wave.get(WaveGeneratorActivity.WAVE1).put(WaveGeneratorActivity.WAVETYPE, 1);
        wave.get(WaveGeneratorActivity.WAVE1).put(WaveGeneratorActivity.FREQUENCY, 10);

        wave.get(WaveGeneratorActivity.WAVE2).put(WaveGeneratorActivity.WAVETYPE, 1);
        wave.get(WaveGeneratorActivity.WAVE2).put(WaveGeneratorActivity.PHASE, 0);
        wave.get(WaveGeneratorActivity.WAVE2).put(WaveGeneratorActivity.FREQUENCY, 10);
    }
}
