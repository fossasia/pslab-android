package org.fossasia.pslab.others;

/**
 * Created by viveksb007 on 28/7/17.
 */

public class MathUtils {

    public static double map(int x, int in_min, int in_max, double out_min, double out_max) {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }

}
