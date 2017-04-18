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
    String calibration_enabled;
    private List<Double> calibration_table = new ArrayList<>();

    DACChannel(String name, double[] span, int channum) {
        this.name = name;
        this.range = span;
        this.channum = channum;
        this.slope = span[1] - span[0];
        this.intercept = span[0];
        this.VToCode.put(0, -4095. * intercept / slope);
        this.VToCode.put(1, 4095. / slope);
        this.VToCode.put(2,0.);
        this.CodeToV.put(0, intercept);
        this.CodeToV.put(1, slope / 4095.);
        this.CodeToV.put(2,0.);
        this.calibration_enabled = "false";
        this.slope = 1;
        this.offset = 0;
    }

    void LoadCalibrationTable(List<Double> table) {
        calibration_enabled = "table";
        calibration_table = table;
    }

    void LoadCalibrationTwopoint(double slope, int offset) {
        calibration_enabled = "twopoint";
        this.slope = slope;
        this.offset = offset;
    }

    int ApplyCalibration(int v) {
        if (calibration_enabled.equals("table")) {
            if (v + calibration_table.get(v) <= 0) {
                return 0;
            } else if (v + calibration_table.get(v) > 0 && v + calibration_table.get(v) < 4095) {
                return ((int) (v + calibration_table.get(v)));
            } else {
                return 4095;
            }
        } else if (calibration_enabled.equals("twopoint")) {
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
