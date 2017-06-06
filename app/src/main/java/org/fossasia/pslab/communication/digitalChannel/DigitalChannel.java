package org.fossasia.pslab.communication.digitalChannel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by viveksb007 on 26/3/17.
 */

public class DigitalChannel {

    private static final int EVERY_SIXTEENTH_RISING_EDGE = 5;
    private static final int EVERY_FOURTH_RISING_EDGE = 4;
    private static final int EVERY_RISING_EDGE = 3;
    private static final int EVERY_FALLING_EDGE = 2;
    public static final int EVERY_EDGE = 1;
    public static final int DISABLED = 0;

    public static String[] digitalChannelNames = {"ID1", "ID2", "ID3", "ID4", "SEN", "EXT", "CNTR"};
    public String channelName, dataType;
    boolean initialState;
    public int initialStateOverride, channelNumber, length, prescalar, trigger, dlength, plotLength, maxTime, mode;
    double gain, maxT;
    public double xAxis[], yAxis[], timestamps[];

    public DigitalChannel(int channelNumber) {
        this.channelNumber = channelNumber;
        this.channelName = digitalChannelNames[channelNumber];
        this.gain = 0;
        this.xAxis = new double[20000];
        this.yAxis = new double[20000];
        this.timestamps = new double[10000];
        this.length = 100;
        this.initialState = false;
        this.prescalar = 0;
        this.dataType = "int";
        this.trigger = 0;
        this.dlength = 0;
        this.plotLength = 0;
        this.maxT = 0;
        this.maxTime = 0;
        this.initialStateOverride = 0;
        this.mode = EVERY_EDGE;
    }

    void setParams(String channelName, int channelNumber) {
        this.channelName = channelName;
        this.channelNumber = channelNumber;
    }

    void setPrescalar(int prescalar) {
        this.prescalar = prescalar;
    }

    public void loadData(LinkedHashMap<String, Integer> initialStates, double[] timestamps) {
        if (initialStateOverride != 0) {
            this.initialState = (initialStateOverride - 1) == 1;
            this.initialStateOverride = 0;
        } else {
            if (initialStates.get(channelName) == 1) {
                this.initialState = true;
            } else {
                this.initialState = false;
            }
        }
        System.arraycopy(timestamps, 0, this.timestamps, 0, timestamps.length);
        this.dlength = timestamps.length; //
        double factor;
        switch (prescalar) {
            case 0:
                factor = 64;
                break;
            case 1:
                factor = 8;
                break;
            case 2:
                factor = 4;
                break;
            default:
                factor = 1;
        }
        for (int i = 0; i < this.timestamps.length; i++) this.timestamps[i] /= factor;
        if (dlength > 0)
            this.maxT = this.timestamps[this.timestamps.length - 1];
        else
            this.maxT = 0;

    }

    public void generateAxes() {
        int HIGH = 1, LOW = 0, state;
        if (initialState)
            state = LOW;
        else
            state = HIGH;

        if (this.mode == DISABLED) {
            xAxis[0] = 0;
            yAxis[0] = 0;
            this.plotLength = 1;
        } else if (this.mode == EVERY_EDGE) {
            xAxis[0] = 0;
            yAxis[0] = state;
            int i, j;
            for (i = 1, j = 1; i < this.dlength; i++, j++) {
                xAxis[j] = timestamps[i];
                yAxis[j] = state;
                if (state == HIGH)
                    state = LOW;
                else
                    state = HIGH;
                j++;
                xAxis[j] = timestamps[i];
                yAxis[j] = state;
            }
            plotLength = j;
        } else if (this.mode == EVERY_FALLING_EDGE) {
            xAxis[0] = 0;
            yAxis[0] = HIGH;
            int i, j;
            for (i = 1, j = 1; i < this.dlength; i++, j++) {
                xAxis[j] = timestamps[i];
                yAxis[j] = HIGH;
                j++;
                xAxis[j] = timestamps[i];
                yAxis[j] = LOW;
                j++;
                xAxis[j] = timestamps[i];
                yAxis[j] = HIGH;
            }
            state = HIGH;
            plotLength = j;
        } else if (this.mode == EVERY_RISING_EDGE || this.mode == EVERY_FOURTH_RISING_EDGE || this.mode == EVERY_SIXTEENTH_RISING_EDGE) {
            xAxis[0] = 0;
            yAxis[0] = LOW;
            int i, j;
            for (i = 1, j = 1; i < this.dlength; i++, j++) {
                xAxis[j] = timestamps[i];
                yAxis[j] = LOW;
                j++;
                xAxis[j] = timestamps[i];
                yAxis[j] = HIGH;
                j++;
                xAxis[j] = timestamps[i];
                yAxis[j] = LOW;
            }
            state = LOW;
            plotLength = j;
        }

    }

    double[] getXAxis() {
        return Arrays.copyOfRange(this.xAxis, 0, plotLength);
    }

    double[] getYAxis() {
        return Arrays.copyOfRange(this.yAxis, 0, plotLength);
    }
}
