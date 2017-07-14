package org.fossasia.pslab.communication.sensors;

import static java.lang.Math.abs;
import static org.apache.commons.math3.util.FastMath.atan2;

/**
 * Created by akarshan on 4/23/17.
 */

public class ComplementaryFilter {
    private double dt, pitch, roll;

    public ComplementaryFilter() {
        pitch = 0.;
        roll = 0.;
        dt = 0.001;
    }

    public void addData(double[] accelerometerData, double[] gyroscopeData) {
        pitch += (gyroscopeData[0]) * dt;        // Angle around the X-axis
        roll -= (gyroscopeData[1]) * dt;        //Angle around the Y-axis
        double pi = 3.14159265359;
        double forceMagnitudeApprox = abs(accelerometerData[0]) + abs(accelerometerData[1]) + abs(accelerometerData[2]);
        double pitchAcc = Math.atan2(accelerometerData[1], accelerometerData[2]) * 180 / pi;
        pitch = pitch * 0.98 + pitchAcc * 0.02;
        double rollAcc = atan2(accelerometerData[0], accelerometerData[2]) * 180 / pi;
        roll = roll * 0.98 + rollAcc * 0.02;

    }

    public double[] getData() {
        return new double[]{roll, pitch};
    }

}
