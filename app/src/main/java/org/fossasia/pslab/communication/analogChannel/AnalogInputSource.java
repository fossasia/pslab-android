package org.fossasia.pslab.communication.analogChannel;

import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AnalogInputSource {

    private static String TAG = "AnalogInputSource";

    private double gainValues[], range[];
    private boolean gainEnabled = false, inverted = false, calibrationReady = false;
    private double gain = 0;
    public int gainPGA, CHOSA;
    private int inversion = 1;
    private int defaultOffsetCode = 0;
    private int scaling = 1;
    private String channelName;
    public Map<Integer, Double> calPoly10 = new LinkedHashMap<>(); //(power,coefficient)
    public Map<Integer, Double> calPoly12 = new LinkedHashMap<>(); //(power,coefficient)
    public Map<Integer, Double> voltToCode10 = new LinkedHashMap<>(); //(power,coefficient)
    private Map<Integer, Double> voltToCode12 = new LinkedHashMap<>(); //(power,coefficient)
    private List<Double> adc_shifts = new ArrayList<>();
    private List<LinkedHashMap<Integer, Double>> polynomials = new ArrayList<>(); //list of maps

    public AnalogInputSource(String channelName) {
        AnalogConstants analogConstants = new AnalogConstants();
        this.channelName = channelName;
        range = analogConstants.inputRanges.get(channelName);
        gainValues = analogConstants.gains;
        this.CHOSA = analogConstants.picADCMultiplex.get(channelName);

        calPoly10.put(0, 0.);
        calPoly10.put(1, 3.3 / 1023);
        calPoly10.put(2, 0.);

        calPoly12.put(0, 0.);
        calPoly12.put(1, 3.3 / 4095);
        calPoly12.put(2, 0.);

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

    public Boolean setGain(int index) {
        if (!gainEnabled) {
            Log.e(channelName, "Analog gain is not available");
            return false;
        }
        gain = gainValues[index];
        regenerateCalibration();
        return true;
    }

    boolean inRange(double val) {
        double sum = voltToCode12.get(0) + val * voltToCode12.get(1) + val * val * voltToCode12.get(2);
        if (sum >= 50 && sum <= 4095) {
            return true;
        }
        return false;
    }

    boolean conservativeInRange(double val) {
        double solution = voltToCode12.get(0) + val * voltToCode12.get(1) + val * val * voltToCode12.get(2);
        if (solution >= 50 && solution <= 4000) {
            return true;
        }
        return false;
    }

    List<Double> loadCalibrationTable(double[] table, double slope, double intercept) {
        for (int i = 0; i < table.length; i++) {
            adc_shifts.add(table[i] * slope - intercept);
        }
        return adc_shifts;
    }

    void ignoreCalibration() {
        calibrationReady = false;
    }

    void loadPolynomials(List<Double[]> polys) //polys --> A LIST OF TUPLES (in Python) ~ A LIST OF ARRAYS (IN JAVA))
    //polynomial is a list of hashes and we are gonna load values via loadPolynomials
    {
        for (int i = 0; i < polys.size(); i++) {
            LinkedHashMap<Integer, Double> temp = new LinkedHashMap<>();
            for (int j = polys.get(i).length - 1; j >= 0; j--) {
                temp.put(j, polys.get(i)[j]);//assuming coeffecient of lowest degree (tuple of polys) is in the end.
            }
            polynomials.add(temp);  //if I am correct polynomials will appear like this [(0:1,1:9,2:0),(0:2,1:3,2:4),.....]
            temp.clear();
        }
    }                           //** unsure about this method

    private void regenerateCalibration() {
        double A, B, intercept, slope;
        B = range[1];
        A = range[0];
        if (gain != -1) {
            gain = gainValues[(int) gain];
            B /= gain;
            A /= gain;
        }
        slope = B - A;
        intercept = A;
        if (!calibrationReady && gain == 8) {
            calPoly10.clear();
            calPoly10.put(0, intercept);
            calPoly10.put(1, slope / 1023);
            calPoly10.put(2, 0.);
            calPoly12.clear();
            calPoly12.put(0, intercept);
            calPoly12.put(1, slope / 4095);
            calPoly12.put(2, 0.);
        }                                       //else cases need to be worked on!!!
        voltToCode10.put(0, -1023 * intercept / slope);
        voltToCode10.put(1, 1023. / slope);
        voltToCode10.put(2, 0.);

        voltToCode12.put(0, -4095 * intercept / slope);
        voltToCode12.put(1, 4095. / slope);
        voltToCode12.put(2, 0.);
    }

    double cal12(double RAW) {
        double avg_shifts = (adc_shifts.get((int) Math.floor(RAW)) + adc_shifts.get((int) Math.ceil(RAW))) / 2;
        RAW -= 4095 * avg_shifts / 3.3;
        return (polynomials.get((int) gain).get(0) + RAW * polynomials.get((int) gain).get(1) + RAW * RAW * polynomials.get((int) gain).get(2)); //gonna define a new method for this

    }

    double cal10(double RAW) {
        RAW *= 4095 / 1023;
        double avg_shifts = (adc_shifts.get((int) Math.floor(RAW)) + adc_shifts.get((int) Math.ceil(RAW))) / 2;
        RAW -= 4095 * avg_shifts / 3.3;
        return (polynomials.get((int) gain).get(0) + RAW * polynomials.get((int) gain).get(1) + RAW * RAW * polynomials.get((int) gain).get(2));
    }
}
