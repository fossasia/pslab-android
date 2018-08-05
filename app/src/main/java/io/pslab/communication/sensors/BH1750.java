package io.pslab.communication.sensors;

import io.pslab.communication.peripherals.I2C;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class BH1750 {
    private String TAG = "BH1750";
    private int POWER_ON = 0x01;
    private int RESET = 0x07;
    private int RES_1000mLx = 0x10;
    private int RES_500mLx = 0x11;
    private int RES_4000mLx = 0x13;
    private I2C i2c;

    private int[] gainChoices = {RES_500mLx, RES_1000mLx, RES_4000mLx};
    private String[] gainLiteralChoices = {"500mLx", "1000mLx", "4000mLx"};
    public int gain = 0;
    public double[] scaling = {2, 1, 0.25};

    public int NUMPLOTS = 1;
    public String[] PLOTNAMES = {"Lux"};
    private int ADDRESS = 0x23;
    private String name = "Luminosity";


    public BH1750(I2C i2c) throws IOException, InterruptedException {
        this.i2c = i2c;
        init();
    }

    private void init() throws IOException {
        i2c.writeBulk(ADDRESS, new int[]{RES_500mLx});
    }

    public void setRange(String g) throws IOException {
        int gain = Arrays.asList(gainLiteralChoices).indexOf(g);
        i2c.writeBulk(ADDRESS, new int[]{gainChoices[gain]});
    }

    private ArrayList<Byte> getVals(int numbytes) throws IOException {
        ArrayList<Byte> vals = i2c.simpleRead(ADDRESS, numbytes);
        return vals;
    }

    public Double getRaw() throws IOException {
        ArrayList<Byte> vals = getVals(2);
        if (vals.size() == 3)
            return (vals.get(0) << 8 | vals.get(1)) / 1.2;
        else
            return 0.0;
    }
}
