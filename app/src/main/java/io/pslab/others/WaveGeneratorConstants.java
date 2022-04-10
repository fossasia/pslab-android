package io.pslab.others;

import java.util.HashMap;
import java.util.Map;

import io.pslab.activity.WaveGeneratorActivity;
import io.pslab.activity.WaveGeneratorActivity.WaveConst;
import io.pslab.activity.WaveGeneratorActivity.WaveData;

public final class WaveGeneratorConstants {

    public final static Map<WaveGeneratorActivity.WaveConst, HashMap<WaveConst, Integer>> wave = new HashMap<>();

    static {
        wave.put(WaveConst.WAVE1, new HashMap<WaveConst, Integer>() {{
            put(WaveConst.FREQUENCY, WaveData.FREQ_MIN.getValue());
            put(WaveConst.WAVETYPE, WaveGeneratorActivity.SIN);
        }});

        wave.put(WaveConst.WAVE2, new HashMap<WaveConst, Integer>() {{
            put(WaveConst.PHASE, WaveData.PHASE_MIN.getValue());
            put(WaveConst.FREQUENCY, WaveData.FREQ_MIN.getValue());
            put(WaveConst.WAVETYPE, WaveGeneratorActivity.SIN);
        }});

        wave.put(WaveConst.WAVETYPE, new HashMap<>());

        wave.put(WaveConst.SQR1, new HashMap<WaveConst, Integer>() {{
            put(WaveConst.FREQUENCY, WaveData.FREQ_MIN.getValue()); //common frequency for all pins(SQR1,SQR2,SQR3,SQR4)
            put(WaveConst.DUTY, WaveData.DUTY_MIN.getValue());
        }});

        wave.put(WaveConst.SQR2, new HashMap<WaveConst, Integer>() {{
            put(WaveConst.FREQUENCY, WaveData.FREQ_MIN.getValue());
            put(WaveConst.PHASE, WaveData.PHASE_MIN.getValue());
            put(WaveConst.DUTY, WaveData.DUTY_MIN.getValue());
        }});

        wave.put(WaveConst.SQR3, new HashMap<WaveConst, Integer>() {{
            put(WaveConst.PHASE, WaveData.PHASE_MIN.getValue());
            put(WaveConst.DUTY, WaveData.DUTY_MIN.getValue());
        }});

        wave.put(WaveConst.SQR4, new HashMap<WaveConst, Integer>() {{
            put(WaveConst.PHASE, WaveData.PHASE_MIN.getValue());
            put(WaveConst.DUTY, WaveData.DUTY_MIN.getValue());
        }});
    }

    public static WaveConst mode_selected = WaveConst.SQUARE;

    public final static Map<String, Integer> state = new HashMap<String, Integer>() {{
        put("SQR1", 0);
        put("SQR2", 0);
        put("SQR3", 0);
        put("SQR4", 0);
    }};
}
