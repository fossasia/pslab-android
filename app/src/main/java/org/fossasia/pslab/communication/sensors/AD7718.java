package org.fossasia.pslab.communication.sensors;

import android.util.Log;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.fossasia.pslab.communication.peripherals.SPI;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Padmal on 5/3/17.
 */

public class AD7718 {

    private double VREF = 3.3;

    private int STATUS = 0;
    private int MODE = 1;
    private int ADCCON = 2;
    private int FILTER = 3;
    private int ADCDATA = 4;
    private int ADCOFFSET = 5;
    private int ADCGAIN = 6;
    private int IOCON = 7;
    private int TEST1 = 12;
    private int TEST2 = 13;
    private int ID = 15;

    // Bit definitions
    private int MODE_PD = 0;
    private int MODE_IDLE = 1;
    private int MODE_SINGLE = 2;
    private int MODE_CONT = 3;
    private int MODE_INT_ZEROCAL = 4;
    private int MODE_INT_FULLCAL = 5;
    private int MODE_SYST_ZEROCAL = 6;
    private int MODE_SYST_FULLCAL = 7;

    private int MODE_OSCPD = bitShift(1, 3);
    private int MODE_CHCON = bitShift(1, 4);
    private int MODE_REFSEL = bitShift(1, 5);
    private int MODE_NEGBUF = bitShift(1, 6);
    private int MODE_NOCHOP = bitShift(1, 7);

    private int CON_AIN1AINCOM = bitShift(0, 4);
    private int CON_AIN2AINCOM = bitShift(1, 4);
    private int CON_AIN3AINCOM = bitShift(2, 4);
    private int CON_AIN4AINCOM = bitShift(3, 4);
    private int CON_AIN5AINCOM = bitShift(4, 4);
    private int CON_AIN6AINCOM = bitShift(5, 4);
    private int CON_AIN7AINCOM = bitShift(6, 4);
    private int CON_AIN8AINCOM = bitShift(7, 4);
    private int CON_AIN1AIN2 = bitShift(8, 4);
    private int CON_AIN3AIN4 = bitShift(9, 4);
    private int CON_AIN5AIN6 = bitShift(10, 4);
    private int CON_AIN7AIN8 = bitShift(11, 4);
    private int CON_AIN2AIN2 = bitShift(12, 4);
    private int CON_AINCOMAINCOM = bitShift(13, 4);
    private int CON_REFINREFIN = bitShift(14, 4);
    private int CON_OPEN = bitShift(15, 4);
    private int CON_UNIPOLAR = bitShift(1, 3);

    private int CON_RANGE0 = 0;  // +-20mV
    private int CON_RANGE1 = 1;  // +-40mV
    private int CON_RANGE2 = 2;  // +-80mV
    private int CON_RANGE3 = 3;  // +-160mV
    private int CON_RANGE4 = 4;  // +-320mV
    private int CON_RANGE5 = 5;  // +-640mV
    private int CON_RANGE6 = 6;  // +-1280mV
    private int CON_RANGE7 = 7;  // +-2560mV
    private int gain = 1;

    private String[] CHAN_NAMES = {
            "AIN1AINCOM",
            "AIN2AINCOM",
            "AIN3AINCOM",
            "AIN4AINCOM",
            "AIN5AINCOM",
            "AIN6AINCOM",
            "AIN7AINCOM",
            "AIN8AINCOM"
    };

    private SPI spi;
    private boolean caldone;
    private String cs;

    private final String TAG = "AD7718";

    private HashMap<String, double[]> calibs = new HashMap<>();
    private HashMap<String, double[]> caldata = new HashMap<>();


    public AD7718(SPI spi) throws IOException {
        this.spi = spi;
        this.cs = "CS1";
        // Populate Calibrations
        populateCalibrationMap();
        // Set SPI Parameters
        spi.setParameters(2, 1, 0, 1, 1);
        writeRegister(FILTER, 20);
        writeRegister(MODE, MODE_SINGLE | MODE_CHCON | MODE_REFSEL);

        for (String key : calibs.keySet()) {
            double[] convertedList = new PolynomialFunction(calibs.get(key)).getCoefficients();
            ArrayUtils.reverse(convertedList);
            caldata.put(key, convertedList);
        }
    }

    public void setCalibrationMap(HashMap<String, double[]> calibrationMap) {
        this.calibs = calibrationMap;
    }

    /**
     * Initiates calibration HashMap with default values
     */
    private void populateCalibrationMap() {
        calibs.put("AIN1AINCOM", new double[]{
                8.220199e-05, -4.587100e-04, 1.001015e+00, -1.684517e-04});
        calibs.put("AIN2AINCOM", new double[]{
                5.459186e-06, -1.749624e-05, 1.000268e+00, 1.907896e-04});
        calibs.put("AIN3AINCOM", new double[]{
                -3.455831e-06, 2.861689e-05, 1.000195e+00, 3.802349e-04});
        calibs.put("AIN4AINCOM", new double[]{
                4.135213e-06, -1.973478e-05, 1.000277e+00, 2.115374e-04});
        calibs.put("AIN5AINCOM", new double[]{
                -1.250787e-07, -9.203838e-07, 1.000299e+00, -1.262684e-03});
        calibs.put("AIN6AINCOM", new double[]{
                6.993123e-07, -1.563294e-06, 9.994211e-01, -4.596018e-03});
        calibs.put("AIN7AINCOM", new double[]{
                3.911521e-07, -1.706405e-06, 1.002294e+00, -1.286302e-02});
        calibs.put("AIN8AINCOM", new double[]{
                8.290843e-07, -7.129532e-07, 9.993159e-01, 3.307947e-03});
        calibs.put("AIN9AINCOM", new double[]{
                7.652808e+00, 1.479229e+00, 2.832601e-01, 4.495232e-02});
    }

    public void start() throws IOException {
        spi.setCS(cs, 0);
    }

    public void stop() throws IOException {
        spi.setCS(cs, 1);
    }

    public int send8(int val) throws IOException {
        return spi.send8(val);
    }

    public int send16(int val) throws IOException {
        return spi.send16(val);
    }

    public int readRegister(int reg) throws IOException {
        start();
        int val = send16(0x4000 | (reg << 8));
        stop();
        val &= 0x00FF;
        return val;
    }

    public int readData() throws IOException {
        start();
        int val = send16(0x4000 | (ADCDATA << 8));
        val &= 0xFF;
        val <<= 16;
        val |= send16(0x0000);
        stop();
        return val;
    }

    public int writeRegister(int reg, int value) throws IOException {
        start();
        int val = send16((reg << 8) | value);
        stop();
        return val;
    }

    public void internalCalibration(int chan) throws IOException, InterruptedException {
        start();
        int val = send16((ADCCON << 8) | (chan << 4) | 7);  // range=7
        long start_time = System.currentTimeMillis();
        caldone = false;
        val = send16((MODE << 8) | 4);
        while (!caldone) {
            Thread.sleep(500);
            caldone = (send16(0x4000 | (MODE << 8)) & 7) == 1;
            Log.d(TAG, String.format("Waiting for zero scale calibration... %.2f S, %s",
                    (float) (System.currentTimeMillis() - start_time),
                    caldone)
            );
        }
        caldone = false;
        val = send16((MODE << 8) | 5);
        while (!caldone) {
            Thread.sleep(500);
            caldone = (send16(0x4000 | (MODE << 8)) & 7) == 1;
            Log.d(TAG, String.format("Waiting for full scale calibration... %.2f S, %s",
                    (float) (System.currentTimeMillis() - start_time),
                    caldone)
            );
        }
        stop();
    }

    public List readCalibration() throws IOException {
        start();
        int off = send16(0x4000 | (ADCOFFSET << 8));
        off &= 0xFF;
        off <<= 16;
        off |= send16(0x0000);

        int gn = send16(0x4000 | (ADCGAIN << 8));
        gn &= 0xFF;
        gn <<= 16;
        gn |= send16(0x0000);
        stop();
        return Arrays.asList(new int[]{off, gn});
    }


    public void configADC(int adccon) throws IOException {
        writeRegister(ADCCON, adccon); // unipolar channels, range
        gain = 2 ^ (7 - adccon & 3);
    }

    public void printstat() throws IOException {
        int stat = readRegister(STATUS);
        String[] P = {"PLL LOCKED", "RES", "RES", "ADC ERROR", "RES", "CAL DONE", "RES", "READY"};
        String[] N = {"PLL ERROR", "RES", "RES", "ADC OKAY", "RES", "CAL LOW", "RES", "NOT READY"};
        StringBuilder sb = new StringBuilder();
        for (int a = 0; a < 8; a++) {
            if ((stat & (1 << a)) == 1) {
                sb.append(P[a]);
            } else {
                sb.append(N[a]);
            }
        }
        Log.d(TAG, stat + ", " + sb.toString());
    }

    public float convertUniPolar(float x) {
        return (float) (1.024 * VREF * x) / (gain * 2 ^ 24);
    }

    public float convertBipolar(float x) {
        return (float) (((x / (2 ^ 24)) - 1) * (1.024 * VREF) / (gain));
    }

    private boolean startRead(String chan) throws IOException {
        List channels = Arrays.asList(CHAN_NAMES);
        if (channels.contains(chan)) {
            int channelID = channels.indexOf(chan);
            configADC(CON_RANGE7 | CON_UNIPOLAR | channelID << 4);
            writeRegister(MODE, MODE_SINGLE | MODE_CHCON | MODE_REFSEL);
            return true;
        } else {
            Log.d(TAG, "Invalid Channel Name. try AIN1AINCOM");
            return false;
        }
    }

    private boolean fetchData(String chan) throws IOException, InterruptedException {

        while (true) {
            int stat = readRegister(STATUS);
            if ((stat & 0x80) == 1) {
                float data = readData();
                data = convertUniPolar(data);
                List channelList = Collections.singletonList(chan);
                if ((int) channelList.get(3) > 4) {
                    data = (data - 3.3f / 2) * 4;
                }
                PolynomialFunction function = new PolynomialFunction(caldata.get(chan));
                return function.value(data) == 0;
            } else {
                Thread.sleep(100);
                Log.d(TAG, "Increase Delay");
            }
        }
    }

    public boolean readVoltage(String channel) throws IOException, InterruptedException {
        if (startRead(channel)) {
            return false;
        }
        Thread.sleep(150);
        return fetchData(channel);
    }

    private boolean fetchRawData(String chan) throws IOException, InterruptedException {
        while (true) {
            int stat = readRegister(STATUS);
            if ((stat & 0x80) == 1) {
                float data = readData();
                return convertUniPolar(data) == 1;
            } else {
                Thread.sleep(100);
                Log.d(TAG, "Increase Delay");
            }
        }
    }

    public boolean readRawVoltage(String channel) throws IOException, InterruptedException {
        if (startRead(channel)) {
            return false;
        }
        Thread.sleep(150);
        return fetchRawData(channel);
    }

    private int bitShift(int y, int x) {
        return y << x;
    }
}