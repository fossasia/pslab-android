package org.fossasia.pslab.communication.sensors;


import android.util.Log;

import org.fossasia.pslab.communication.peripherals.I2C;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by akarshan on 4/17/17.
 */

public class MLX90614 {
    private String TAG = "MLX90614";
    private int ADDRESS = 0x5A;

    public int NUMPLOTS = 1;
    public String[] PLOTNAMES = {"Temp"};
    public String name = "PIR temperature";

    private I2C i2c;
    private int source, OBJADDR = 0x07, AMBADDR = 0x06;

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
        ArrayList<Integer> readReg = new ArrayList<>();
        for (int i = 0; i < 0x20; i++)
            readReg.add(i);
        ArrayList<String> selectSource = new ArrayList<>(Arrays.asList("object temperature", "ambient temperature"));
    }

    public void selectSource(String source) {
        if (source.equals("object temperature"))
            this.source = OBJADDR;
        else if (source.equals("ambient temperature"))
            this.source = AMBADDR;
    }

    public void readReg(int address) throws IOException {
        ArrayList<Character> x = getVals(address, 2);
        Log.v(TAG, Integer.toHexString(address) + " " + Integer.toHexString(x.get(0) | (x.get(1) << 8)));
    }

    public ArrayList<Character> getVals(int addr, int bytes) throws IOException {
        ArrayList<Character> vals = i2c.readBulk(ADDRESS, addr, bytes);
        return vals;
    }

    public Double[] getRaw() throws IOException {
        ArrayList<Character> vals = getVals(source, 3);
        if (vals.size() == 3)
            return new Double[]{((((vals.get(1) & 0x007f) << 8) + vals.get(0)) * 0.02) - 0.01 - 273.15};
        else
            return null;
    }

    public Double getObjectTemperature() throws IOException {
        source = OBJADDR;
        Double[] val = getRaw();
        if (val.length != 0)
            return val[0];
        else
            return null;
    }

    public Double getAmbientTemperature() throws IOException {
        source = AMBADDR;
        Double[] val = getRaw();
        if (val.length != 0)
            return val[0];
        else
            return null;
    }

}
