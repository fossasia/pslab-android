package io.pslab.communication.sensors;


import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.pslab.communication.peripherals.I2C;

/**
 * Created by akarshan on 4/17/17.
 */

public class MLX90614 {
    private final String TAG = "MLX90614";
    private final int ADDRESS = 0x5A;

    public int NUMPLOTS = 1;
    public String[] PLOTNAMES = {"Temp"};
    public String name = "PIR temperature";

    private final I2C i2c;
    private int source;
    private final int OBJADDR = 0x07;
    private final int AMBADDR = 0x06;

    public MLX90614(I2C i2c) throws IOException {
        this.i2c = i2c;
        source = OBJADDR;
        String name = "Passive IR temperature sensor";
        try {
            Log.d(TAG, "switching baud to 100k");
            i2c.config((int) 100e3);
        } catch (Exception e) {
            Log.d(TAG, "failed to change baud rate");
        }
        List<Integer> readReg = new ArrayList<>();
        for (int i = 0; i < 0x20; i++)
            readReg.add(i);
        List<String> selectSource = Arrays.asList("object temperature", "ambient temperature");
    }

    public void selectSource(String source) {
        if (source.equals("object temperature"))
            this.source = OBJADDR;
        else if (source.equals("ambient temperature"))
            this.source = AMBADDR;
    }

    public void readReg(int address) throws IOException {
        List<Character> x = getVals(address, 2);
        Log.v(TAG, Integer.toHexString(address) + " " + Integer.toHexString(x.get(0) | (x.get(1) << 8)));
    }

    private List<Character> getVals(int addr, int bytes) throws IOException {
        return i2c.readBulk(ADDRESS, addr, bytes);
    }

    public Double getRaw() throws IOException {
        List<Character> vals = getVals(source, 3);
        if (vals.size() == 3)
            return ((((vals.get(1) & 0x007f) << 8) + vals.get(0)) * 0.02) - 0.01 - 273.15;
        else
            return null;
    }

    public Double getObjectTemperature() throws IOException {
        source = OBJADDR;
        return getRaw();

    }

    public Double getAmbientTemperature() throws IOException {
        source = AMBADDR;
        return getRaw();

    }

}
