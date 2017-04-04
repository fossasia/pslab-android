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
    int gainPGA, inversion = 1, defaultOffsetCode = 0, scaling = 1, CHOSA;
    private String channelName;


    public AnalogInputSource(String channelName) {
        AnalogConstants analogConstants = new AnalogConstants();
        this.channelName = channelName;
        range = analogConstants.inputRanges.get(channelName);
        this.gainValues = analogConstants.gains;
        this.CHOSA = analogConstants.picADCMultiplex.get(channelName);
        if (range[1] - range[0] < 0) {
            inverted = true;
            inversion = -1;
        }
        if (channelName.equals("CH1")) {
            gainEnabled = true;
            gainPGA = 1;
            gain = 0;
        } else if (channelName.equals("CH2")) {
            gainEnabled = true;
            gainPGA = 2;
            gain = 0;
        }
        gain = 0;
        regenerateCalibration();
    }

    boolean setGain(int index) {
        if (!gainEnabled) {
            Log.e(TAG, "Analog gain is not available on " + channelName);
            return false;
        }
        gain = gainValues[index];
        regenerateCalibration();
        return true;
    }

    boolean inRange() {
        return true;
    }

    boolean conservativeInRange() {
        return true;
    }

    void loadCalibrationTable() {

    }

    void loadPolynomials() {

    }

    void ignoreCalibration() {
        caliberationReady = false;
    }

    private void regenerateCalibration() {
        double A = range[0], B = range[1], intercept = range[0];
        if (gain != 0) {
            A /= gain;
            B /= gain;
        }
        double slope = B - A;
        if (caliberationReady & (gain != 8)) {

        } else {

        }
    }
}
