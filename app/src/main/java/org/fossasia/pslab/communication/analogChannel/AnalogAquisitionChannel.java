package org.fossasia.pslab.communication.analogChannel;

import java.util.Arrays;

/**
 * Created by viveksb007 on 24/3/17.
 */

public class AnalogAquisitionChannel {

    private int resolution;
    private AnalogInputSource analogInputSource;
    private double gain;
    private String channel;
    private double calibration_ref196;
    private int length;
    private double timebase;
    private double[] xaxis = new double[10000];
    private double[] yaxis = new double[10000];

    public AnalogAquisitionChannel(String channel) //initialize
    {
        gain = 0;
        String channel_names[] = new AnalogConstants().allAnalogChannels;
        this.channel = channel;
        calibration_ref196 = 1.;
        resolution = 10;
        length = 100;
        timebase = 1.;
        Arrays.fill(xaxis, 0);
        Arrays.fill(yaxis, 0);
        analogInputSource = new AnalogInputSource("CH1");
    }

    double fixValue(int val) {
        if (resolution == 12) {
            return (calibration_ref196 * (analogInputSource.calPoly12.get(0) + analogInputSource.calPoly12.get(1) * val + analogInputSource.calPoly12.get(2) * val * val));
        } else {
            return (calibration_ref196 * (analogInputSource.calPoly10.get(0) + analogInputSource.calPoly10.get(1) * val + analogInputSource.calPoly10.get(2) * val * val));
        }
    }

    void setYVal(int pos, int val) {
        yaxis[pos] = fixValue(val);
    }

    void setXVal(int pos, int val) {
        xaxis[pos] = fixValue(val);
    }

    public void setParams(String channel, int length, double timebase, int resolution, AnalogInputSource source, double gain) {
        if (gain != -1) this.gain = gain;
        if (source != null) this.analogInputSource = source;
        if (channel != null) this.channel = channel;
        if (resolution != -1) this.resolution = resolution;
        if (length != -1) this.length = length;
        if (timebase != -1) this.timebase = timebase;
        regenerateXAxis();
    }

    void regenerateXAxis() {
        for (int i = 0; i < length; i++) {
            xaxis[i] = timebase * i;
        }
    }

    double getXAxis() {
        return (xaxis[length]);
    }

    double getYAxis() {
        return (yaxis[length]);
    }

}
