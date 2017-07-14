package org.fossasia.pslab.communication.sensors;

import android.util.Log;

import org.fossasia.pslab.communication.peripherals.I2C;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.pow;

/**
 * Created by akarshan on 4/18/17.
 */

public class BMP180 {
    private String TAG = "BMP180";
    private int ADDRESS = 0x77;
    private int REG_CONTROL = 0xF4;
    private int REG_RESULT = 0xF6;
    private int CMD_TEMP = 0x2E;
    private int CMD_P0 = 0x34;
    private int CMD_P1 = 0x74;
    private int CMD_P2 = 0xB4;
    private int CMD_P3 = 0xF4;
    private int oversampling = 0;

    public int NUMPLOTS = 3;
    public String[] PLOTNAMES = {"Temperature", "Pressure", "Altitude"};
    public String name = "Altimeter BMP180";

    private I2C i2c;
    private int MB;
    private double c3, c4, b1, c5, c6, mc, md, x0, x1, x2, y0, y1, y2, p0, p1, p2, temperature, pressure, baseline;
    private ArrayList<Integer> setOverSampling = new ArrayList<>(Arrays.asList(0, 1, 2, 3));

    public BMP180(I2C i2c) throws IOException, InterruptedException {
        this.i2c = i2c;
        MB = readInt(0xBA);
        c3 = 160 * pow(2, -15) * readInt(0xAE);
        c4 = pow(10, -3) * pow(2, -15) * readUInt(0xB0);
        b1 = pow(160, 2) * pow(2, -30) * readInt(0xB6);
        c5 = (pow(2, -15) / 160) * readUInt(0xB2);
        c6 = readUInt(0xB4);
        mc = (pow(2, 11) / pow(160, 2)) * readInt(0xBC);
        md = readInt(0xBE) / 160.0;
        x0 = readInt(0xAA);
        x1 = 160.0 * pow(2, -13) * readInt(0xAC);
        x2 = pow(160, 2) * pow(2, -25) * readInt(0xB8);
        y0 = c4 * pow(2, 15);
        y1 = c4 * c3;
        y2 = c4 * b1;
        p0 = (3791.0 - 8.0) / 1600.0;
        p1 = 1.0 - 7357.0 * pow(2, -20);
        p2 = 3038.0 * 100.0 * pow(2, -36);
        temperature = 25;

        Log.v("calib", Arrays.toString((new double[]{c3, c4, b1, c5, c6, mc, md, x0, x1, x2, y0, y1, p0, p1, p2})));
        initTemperature();
        readTemperature();
        initPressure();
        baseline = readPressure();
    }

    public short readInt(int address) throws IOException {
        return (short) readUInt(address);   //short is equivalent to numpy.int16()
    }

    public double readUInt(int address) throws IOException {
        ArrayList<Character> vals = i2c.readBulk(ADDRESS, address, 2);
        return 1. * ((vals.get(0) << 8) | vals.get(1));
    }

    public void initTemperature() throws IOException, InterruptedException {
        i2c.writeBulk(ADDRESS, new int[]{REG_CONTROL, CMD_TEMP});
        TimeUnit.MILLISECONDS.sleep(5);
    }

    public Double readTemperature() throws IOException {
        ArrayList<Character> vals = i2c.readBulk(ADDRESS, REG_RESULT, 2);
        if (vals.size() == 2) {
            double t = (vals.get(0) << 8) + vals.get(1);
            double a = c5 * (t - c6);
            temperature = a + (mc / (a + md));
            return temperature;
        } else
            return null;
    }

    public void setOversampling(int num) {
        oversampling = num;
    }

    public void initPressure() throws IOException, InterruptedException {
        int[] os = {0x34, 0x74, 0xb4, 0xf4};
        int[] delays = {5, 8, 14, 26};
        i2c.writeBulk(ADDRESS, new int[]{REG_CONTROL, oversampling});
        TimeUnit.MILLISECONDS.sleep(delays[oversampling]);
    }

    public Double readPressure() throws IOException {
        ArrayList<Character> vals = i2c.readBulk(ADDRESS, REG_RESULT, 3);
        if (vals.size() == 3) {
            double p = 1. * (vals.get(0) << 8) + vals.get(1) + (vals.get(2) / 256.0);
            double s = temperature - 25.0;
            double x = (x2 * pow(s, 2)) + (x1 * s) + x0;
            double y = (y2 * pow(s, 2)) + (y1 * s) + y0;
            double z = (p - x) / y;
            pressure = (p2 * pow(z, 2)) + (p1 * z) + p0;
            return pressure;
        } else
            return null;
    }

    public double altitude() {
        // baseline pressure needs to be provided
        return (44330.0 * (1 - pow(pressure / baseline, 1 / 5.255)));
    }

    public double seaLevel(double pressure, double altitude) {
        //given a calculated pressure and altitude, return the sealevel
        return (pressure / pow(1 - (altitude / 44330.0), 5.255));
    }

    public double[] getRaw() throws IOException, InterruptedException {
        initTemperature();
        readTemperature();
        initPressure();
        readPressure();
        return (new double[]{temperature, pressure, altitude()});
    }

}
