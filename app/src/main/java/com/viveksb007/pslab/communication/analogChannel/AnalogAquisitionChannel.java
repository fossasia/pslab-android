package com.viveksb007.pslab.communication.analogChannel;

/**
 * Created by viveksb007 on 24/3/17.
 */

public class AnalogAquisitionChannel {
    private int resolution;
    private AnalogInputSource analogAquisitionChannel;
    private double gain;
    private String name;
    private double calibration_ref196;
    private int length;
    private double timebase;
    private double[] xaxis = new double[10000];
    private double[] yaxis = new double[10000];
    AnalogAquisitionChannel(String channel) //initialize
    {
        gain = 0;
        String channel_names[] = AnalogConstants.allAnalogChannels;
        calibration_ref196 = 1.;
        resolution = 10;
        length = 100;
        timebase = 1.;
        for(int i=0;i<10000;i++)   //going to find a library to increase the speed
        {
            xaxis[i]=0;
            yaxis[i]=0;
        }

        analogAquisitionChannel = new AnalogInputSource(channel);
    }
    double fixValue(int val)
    {
        if (resolution==12)
        {
            return (calibration_ref196 * (analogAquisitionChannel.calPoly12.get(0) + analogAquisitionChannel.calPoly12.get(1) * val + analogAquisitionChannel.calPoly12.get(2) * val * val));
        }
        else
        {
            return (calibration_ref196 * (analogAquisitionChannel.calPoly10.get(0) + analogAquisitionChannel.calPoly10.get(1) * val + analogAquisitionChannel.calPoly10.get(2) * val * val));
        }
    }
    void setYVal(int pos, int val)
    {
        yaxis[pos]=fixValue(val);
    }
    void setXVal(int pos, int val)
    {
        xaxis[pos]=fixValue(val);
    }
    void setParams()
    {
        //these parameters will be set by the user
    }
    void regenerateXAxis()
    {
        for(int i=0;i<length;i++)
        {
            xaxis[i] = timebase*i;
        }
    }
    double getXAxis()
    {
        return(xaxis[length]);
    }
    double getYAxis()
    {
        return(yaxis[length]);
    }

}
