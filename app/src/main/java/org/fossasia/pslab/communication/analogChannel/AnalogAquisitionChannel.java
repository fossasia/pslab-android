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
    public int length;
    private double timebase;
    private double[] xAxis = new double[10000];
    public double[] yAxis = new double[10000];

    public AnalogAquisitionChannel(String channel) {
        gain = 0;
        this.channel = channel;
        calibration_ref196 = 1.;
        resolution = 10;
        length = 100;
        timebase = 1.;
        Arrays.fill(xAxis, 0);
        Arrays.fill(yAxis, 0);
        analogInputSource = new AnalogInputSource("CH1");
    }

    public double[] fixValue(double[] val) {
        double[] calcData = new double[val.length];
        if (resolution == 12)
            for (int i = 0; i < val.length; i++)
                calcData[i] = calibration_ref196 * (analogInputSource.calPoly12.value(val[i]));
        else
            for (int i = 0; i < val.length; i++)
                calcData[i] = calibration_ref196 * (analogInputSource.calPoly10.value(val[i]));
        return calcData;
    }

    void setYVal(int pos, int val) {
        yAxis[pos] = fixValue(new double[]{val})[0];
    }

    void setXVal(int pos, int val) {
        xAxis[pos] = fixValue(new double[]{val})[0];
    }

    public void setParams(String channel, int length, double timebase, int resolution, AnalogInputSource source, Double gain) {
        if (gain != null) this.gain = gain;
        if (source != null) this.analogInputSource = source;
        if (channel != null) this.channel = channel;
        if (resolution != -1) this.resolution = resolution;
        if (length != -1) this.length = length;
        if (timebase != -1) this.timebase = timebase;
        regenerateXAxis();
    }

    void regenerateXAxis() {
        for (int i = 0; i < length; i++) {
            xAxis[i] = timebase * i;
        }
    }

    public double[] getXAxis() {
        return Arrays.copyOfRange(xAxis, 0, length);
    }

    public double[] getYAxis() {
        return Arrays.copyOfRange(yAxis, 0, length);
    }

}
