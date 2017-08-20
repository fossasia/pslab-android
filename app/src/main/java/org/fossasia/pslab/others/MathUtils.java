package org.fossasia.pslab.others;

import java.util.ArrayList;

/**
 * Created by viveksb007 on 28/7/17.
 */

public class MathUtils {


    /*
    *  Maps a number from one range to another.
    * */
    public static double map(double x, double in_min, double in_max, double out_min, double out_max) {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }


    /*
    * Returns evenly spaced numbers over a specified interval.
    * */
    public static double[] linSpace(double start, double stop, int samples) {
        ArrayList<Double> dataPoints = new ArrayList<>();
        double factor = (stop - start) / samples;
        for (double i = start; i < stop; i += factor) dataPoints.add(i);
        double[] evenlySpacedArray = new double[dataPoints.size()];
        for (int i = 0; i < dataPoints.size(); i++) {
            evenlySpacedArray[i] = dataPoints.get(i);
        }
        return evenlySpacedArray;
    }

    /*
    * Returns RMS value of a given list of double values
    * */
    public static double rms(double[] list) {
        double ms = 0;
        for (double i : list) ms += i * i;
        ms /= list.length;
        return Math.sqrt(ms);
    }
}
