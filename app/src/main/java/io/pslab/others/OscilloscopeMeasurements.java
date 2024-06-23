package io.pslab.others;

import java.util.HashMap;
import java.util.Map;

import io.pslab.activity.OscilloscopeActivity.ChannelMeasurements;
import io.pslab.activity.OscilloscopeActivity.CHANNEL;


public class OscilloscopeMeasurements {

    public final static Map<CHANNEL, HashMap<ChannelMeasurements, Double>> channel = new HashMap<>();

    static {
        channel.put(CHANNEL.CH1, new HashMap<ChannelMeasurements, Double>() {{
            put(ChannelMeasurements.FREQUENCY, 0.00);
            put(ChannelMeasurements.PERIOD, 0.00);
            put(ChannelMeasurements.AMPLITUDE, 0.00);
            put(ChannelMeasurements.POSITIVE_PEAK, 0.00);
            put(ChannelMeasurements.NEGATIVE_PEAK, 0.00);
        }});

        channel.put(CHANNEL.CH2, new HashMap<ChannelMeasurements, Double>() {{
            put(ChannelMeasurements.FREQUENCY, 0.00);
            put(ChannelMeasurements.PERIOD, 0.00);
            put(ChannelMeasurements.AMPLITUDE, 0.00);
            put(ChannelMeasurements.POSITIVE_PEAK, 0.00);
            put(ChannelMeasurements.NEGATIVE_PEAK, 0.00);
        }});

        channel.put(CHANNEL.CH3, new HashMap<ChannelMeasurements, Double>() {{
            put(ChannelMeasurements.FREQUENCY, 0.00);
            put(ChannelMeasurements.PERIOD, 0.00);
            put(ChannelMeasurements.AMPLITUDE, 0.00);
            put(ChannelMeasurements.POSITIVE_PEAK, 0.00);
            put(ChannelMeasurements.NEGATIVE_PEAK, 0.00);
        }});

        channel.put(CHANNEL.MIC, new HashMap<ChannelMeasurements, Double>() {{
            put(ChannelMeasurements.FREQUENCY, 0.00);
            put(ChannelMeasurements.PERIOD, 0.00);
            put(ChannelMeasurements.AMPLITUDE, 0.00);
            put(ChannelMeasurements.POSITIVE_PEAK, 0.00);
            put(ChannelMeasurements.NEGATIVE_PEAK, 0.00);
        }});
    }
}
