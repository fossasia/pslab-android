package org.fossasia.pslab.communication.peripherals;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by viveksb007 on 28/3/17.
 */

public class DACChannel {
    private String name;
    int channum;
    private int offset;
    private double[] range;
    private double slope, intercept;
    Map<Integer, Double> VToCode = new LinkedHashMap<>(); //(power,coefficient)
    Map<Integer, Double> CodeToV = new LinkedHashMap<>(); //(power,coefficient)
    String calibrationEnabled;
    private List<Double> calibrationTable = new ArrayList<>();

    public DACChannel(String name, double[] span, int channum) {
        this.name = name;
        this.range = span;
        this.channum = channum;
        this.slope = span[1] - span[0];
        this.intercept = span[0];
        this.VToCode.put(0, -4095. * intercept / slope);
        this.VToCode.put(1, 4095. / slope);
        this.CodeToV.put(0, intercept);
        this.CodeToV.put(1, slope / 4095.);
        this.calibrationEnabled = "false";
        this.slope = 1;
        this.offset = 0;
    }

    public void loadCalibrationTable(List<Double> table) {
        calibrationEnabled = "table";
        calibrationTable = table;
    }

    public void loadCalibrationTwopoint(double slope, int offset) {
        calibrationEnabled = "twopoint";
        this.slope = slope;
        this.offset = offset;
    }

    int applyCalibration(int v) {
        if (calibrationEnabled.equals("table")) {
            if (v + calibrationTable.get(v) <= 0) {
                return 0;
            } else if (v + calibrationTable.get(v) > 0 && v + calibrationTable.get(v) < 4095) {
                return ((int) (v + calibrationTable.get(v)));
            } else {
                return 4095;
            }
        } else if (calibrationEnabled.equals("twopoint")) {
            if (slope * v + offset <= 0) {
                return 0;
            } else if (slope * v + offset > 0 && slope * v + offset < 4095) {
                return ((int) (slope * v + offset));
            } else {
                return 4095;
            }
        } else {
            return v;
        }
    }
}
