package com.viveksb007.pslab.communication.analogChannel;

import android.util.Log;

/**
 * Created by viveksb007 on 24/3/17.
 */

public class AnalogInputSource {

    private static String TAG = "AnalogInputSource";

    double gainValues[], range[];
    boolean gainEnabled = false, inverted = false, caliberationReady = false;
    double gain = 0;
    int gainPGA, inversion = 1, defaultOffsetCode = 0, scaling = 1;
    private String name;


    public AnalogInputSource(String name) {
        AnalogConstants analogConstants = new AnalogConstants();
        this.name = name;
        range = analogConstants.inputRanges.get(name);
        if (range[1] - range[0] < 0) {
            inverted = true;
            inversion = -1;
        }
        if (name.equals("CH1")) {
            gainEnabled = true;
            gainPGA = 1;
            gain = 0;
        } else if (name.equals("CH2")) {
            gainEnabled = true;
            gainPGA = 2;
            gain = 0;
        }
        gain = 0;
        regenerateCalibration();
    }

    boolean setGain(int index) {
        if (!gainEnabled) {
            Log.e(TAG, "Analog gain is not available on " + name);
            return false;
        }
        gain = gainValues[index];
        regenerateCalibration();
        return true;
    }

    boolean inRange(){
        return true;
    }

    boolean conservativeInRange(){
        return true;
    }

    void loadCalibrationTable(){

    }

    void loadPolynomials(){

    }

    void ignoreCalibration(){
        caliberationReady = false;
    }

    private void regenerateCalibration() {

    }
}
