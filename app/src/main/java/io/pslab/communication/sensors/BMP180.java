package io.pslab.communication.sensors;

import android.util.Log;

import io.pslab.communication.ScienceLab;
import io.pslab.communication.peripherals.I2C;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.pow;

/**
 * Created by akarshan on 4/18/17.
 */

public class BMP180 {

    private static final String TAG = "BMP180";
    // BMP180 default address
    private static final int ADDRESS = 0x77;

    // Operating Modes
    private static final int ULTRALOWPOWER = 0;
    private static final int STANDARD = 1;
    private static final int HIGHRES = 2;
    private static final int ULTRAHIGHRES = 3;

    // BMP180 Registers
    private static final int CAL_AC1 = 0xAA;
    private static final int CAL_AC2 = 0xAC;
    private static final int CAL_AC3 = 0xAE;
    private static final int CAL_AC4 = 0xB0;
    private static final int CAL_AC5 = 0xB2;
    private static final int CAL_AC6 = 0xB4;
    private static final int CAL_B1 = 0xB6;
    private static final int CAL_B2 = 0xB8;
    private static final int CAL_MB = 0xBA;
    private static final int CAL_MC = 0xBC;
    private static final int CAL_MD = 0xBE;
    private static final int CONTROL = 0xF4;
    private static final int TEMPDATA = 0xF6;
    private static final int PRESSDATA = 0xF6;

    // BMP180 Commands
    private static final int READTEMPCMD = 0x2E;
    private static final int READPRESSURECMD = 0x34;

    private int mode = HIGHRES;
    private int oversampling = mode;

    public int NUMPLOTS = 3;
    public String[] PLOTNAMES = {"Temperature", "Pressure", "Altitude"};
    public String name = "Altimeter BMP180";

    private I2C i2c;
    private int ac1;
    private int ac2;
    private int ac3;
    private int ac4;
    private int ac5;
    private int ac6;
    private int b1;
    private int b2;
    private int mb;
    private int mc;
    private int md;
    private double temperature;
    private double pressure;
    private static final double SEA_LEVEL_PRESSURE = 101325.0;

    public BMP180(I2C i2c, ScienceLab scienceLab) throws IOException, InterruptedException {
        this.i2c = i2c;
        if (scienceLab.isConnected()) {
            ac1 = readInt16(CAL_AC1);
            ac2 = readInt16(CAL_AC2);
            ac3 = readInt16(CAL_AC3);
            ac4 = readUInt16(CAL_AC4);
            ac5 = readUInt16(CAL_AC5);
            ac6 = readUInt16(CAL_AC6);
            b1 = readInt16(CAL_B1);
            b2 = readInt16(CAL_B2);
            mb = readInt16(CAL_MB);
            mc = readInt16(CAL_MC);
            md = readInt16(CAL_MD);

            Log.v("calib", Arrays.toString((new double[]{ac1, ac2, ac3, ac4, ac5, ac6, b1, b2, mb, mc, md})));
        }
    }

    private int readInt16(int address) throws IOException {
        ArrayList<Integer> data = i2c.read(ADDRESS, 2, address);
        int value = ((data.get(0) & 0xFF) << 8) | (data.get(1) & 0xFF);
        if ((value & 0x8000) != 0) {  // Check if the sign bit is set
            value |= 0xFFFF0000;  // Sign-extend to 32 bits
        }
        return value;
    }

    private int readUInt16(int address) throws IOException {
        ArrayList<Integer> data = i2c.read(ADDRESS, 2, address);
        return ((data.get(0) & 0xFF) << 8) | (data.get(1) & 0xFF);
    }

    private int readRawTemperature() throws IOException, InterruptedException {
        i2c.write(ADDRESS, new int[]{READTEMPCMD}, CONTROL);
        TimeUnit.MILLISECONDS.sleep(5);
        int raw = readUInt16(TEMPDATA);
        return raw;
    }

    private Double readTemperature() throws IOException, InterruptedException {
        int ut = readRawTemperature();
        // Calculations from section 3.5 of the datasheet
        int x1 = ((ut - ac6) * ac5) >> 15;
        int x2 = (mc << 11) / (x1 + md);
        int b5 = x1 + x2;
        temperature = ((b5 + 8) >> 4) / 10.0;
        return temperature;
    }

    public void setOversampling(int num) {
        oversampling = num;
    }

    private int readRawPressure() throws IOException, InterruptedException {
        int[] delays = {5, 8, 14, 26};
        i2c.write(ADDRESS, new int[]{READPRESSURECMD + (mode << 6)}, CONTROL);
        TimeUnit.MILLISECONDS.sleep(delays[oversampling]);
        int msb = i2c.readByte(ADDRESS, PRESSDATA) & 0xFF;
        int lsb = i2c.readByte(ADDRESS, PRESSDATA + 1) & 0xFF;
        int xlsb = i2c.readByte(ADDRESS, PRESSDATA + 2) & 0xFF;
        return ((msb << 16) + (lsb << 8) + xlsb) >> (8 - mode);
    }

    private Double readPressure() throws IOException, InterruptedException {
        int ut = readRawTemperature();
        int up = readRawPressure();
        // Calculations from section 3.5 of the datasheet
        int x1 = ((ut - ac6) * ac5) >> 15;
        int x2 = (mc << 11) / (x1 + md);
        int b5 = x1 + x2;
        // Pressure Calculations
        int b6 = b5 - 4000;
        x1 = (b2 * (b6 * b6) >> 12) >> 11;
        x2 = (ac2 * b6) >> 11;
        int x3 = x1 + x2;
        int b3 = (((ac1 * 4 + x3) << mode) + 2) / 4;
        x1 = (ac3 * b6) >> 13;
        x2 = (b1 * ((b6 * b6) >> 12)) >> 16;
        x3 = ((x1 + x2) + 2) >> 2;
        int b4 = (ac4 * (x3 + 32768)) >> 15;
        int b7 = (up - b3) * (50000 >> mode);
        int p;
        if (b7 < 0x80000000) {
            p = (b7 * 2) / b4;
        } else {
            p = (b7 / b4) * 2;
        }
        x1 = (p >> 8) * (p >> 8);
        x1 = (x1 * 3038) >> 16;
        x2 = (-7357 * p) >> 16;
        pressure = p + ((x1 + x2 + 3791) >> 4);
        return pressure;
    }

    public double altitude() {
        // Calculation from section 3.6 of the datasheet
        return (44330.0 * (1 - pow(pressure / SEA_LEVEL_PRESSURE, 1 / 5.255)));
    }

    public double seaLevel(double pressure, double altitude) {
        //given a calculated pressure and altitude, return the sealevel
        return (pressure / pow(1 - (altitude / 44330.0), 5.255));
    }

    public double[] getRaw() throws IOException, InterruptedException {
        temperature = readTemperature();
        pressure = readPressure();
        return (new double[]{temperature, altitude(), pressure});
    }

}
