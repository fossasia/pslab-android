package org.fossasia.pslab.communication.sensors;

import org.fossasia.pslab.communication.peripherals.I2C;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by akarshan on 4/23/17.
 * <p>
 * ScienceLab instance of I2C need to be passed to the MF522 constructor.
 * </p>
 */

public class HMC5883L {
    private int CONFA = 0x00;
    private int CONFB = 0x01;
    private int MODE = 0x02;
    private int STATUS = 0x09;

    //--------CONFA register bits. 0x00-----------//

    private int samplesToAverage = 0;
    private ArrayList<Integer> samplesToAverageChoices = new ArrayList<>(Arrays.asList(1, 2, 4, 8));

    private int dataOutputRate = 6;
    private ArrayList<Double> dataOutputRateChoices = new ArrayList<Double>(Arrays.asList(0.75, 1.5, 3., 7.5, 15., 30., 75.));

    private int measurementConf = 0;

    //--------CONFB register bits. 0x01-----------//

    private int gainValue = 7;      //least sensitive
    private ArrayList<Integer> gainChoices = new ArrayList<>(Arrays.asList(8, 7, 6, 5, 4, 3, 2, 1));
    private ArrayList<Double> scaling = new ArrayList<>(Arrays.asList(1370., 1090., 820., 660., 440., 390., 330., 230.));
    private int ADDRESS = 0x1E;
    public String name = "Magnetometer";
    public int NUMPLOTS = 3;
    public String[] PLOTNAMES = {"Bx", "By", "Bz"};

    private I2C i2c;

    public HMC5883L(I2C i2c) throws IOException {
        this.i2c = i2c;
        init();
    }

    private void init() throws IOException {
        writeCONFA();
        writeCONFB();
        i2c.writeBulk(ADDRESS, new int[]{MODE, 0});       //enable continuous measurement mode

    }

    public void writeCONFB() throws IOException {
        i2c.writeBulk(ADDRESS, new int[]{CONFB, gainValue << 5});     //set gain
    }


    public void writeCONFA() throws IOException {
        i2c.writeBulk(ADDRESS, new int[]{CONFA, (dataOutputRate << 2) | (samplesToAverage << 5) | (measurementConf)});
    }

    public void setSamplesToAverage(int num) throws IOException {
        samplesToAverage = samplesToAverageChoices.indexOf(num);
        writeCONFA();
    }

    public void setDataOutputRate(double rate) throws IOException {
        dataOutputRate = dataOutputRateChoices.indexOf(rate);
        writeCONFA();
    }

    public void setGain(int gain) throws IOException {
        gainValue = gainChoices.indexOf(gain);
        writeCONFB();
    }

    public ArrayList<Character> getVals(int addr, int bytes) throws IOException {
        return i2c.readBulk(ADDRESS, addr, bytes);
    }

    public ArrayList<Double> getRaw() throws IOException {
        ArrayList<Double> returnList = new ArrayList<>();
        ArrayList<Character> vals = getVals(0x03, 6);
        if (vals.size() == 6) {
            for (int a = 0; a < 3; a++) {
                returnList.add((vals.get(a * 2) << 8 | vals.get(a * 2 + 1)) / scaling.get(gainValue));
            }
            return returnList;
        } else
            return null;
    }

}

