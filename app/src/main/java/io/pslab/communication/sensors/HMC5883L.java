package io.pslab.communication.sensors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.pslab.communication.ScienceLab;
import io.pslab.communication.peripherals.I2C;

/**
 * Created by akarshan on 4/23/17.
 * <p>
 * ScienceLab instance of I2C need to be passed to the MF522 constructor.
 * </p>
 */

public class HMC5883L {
    private final int CONFA = 0x00;
    private final int CONFB = 0x01;
    private final int MODE = 0x02;
    private final int STATUS = 0x09;

    //--------CONFA register bits. 0x00-----------//

    private int samplesToAverage = 0;
    private final List<Integer> samplesToAverageChoices = Arrays.asList(1, 2, 4, 8);

    private int dataOutputRate = 6;
    private final List<Double> dataOutputRateChoices = Arrays.asList(0.75, 1.5, 3., 7.5, 15., 30., 75.);

    private final int measurementConf = 0;

    //--------CONFB register bits. 0x01-----------//

    private int gainValue = 7;      //least sensitive
    private final List<Integer> gainChoices = Arrays.asList(8, 7, 6, 5, 4, 3, 2, 1);
    private final List<Double> scaling = Arrays.asList(1370., 1090., 820., 660., 440., 390., 330., 230.);
    private final int ADDRESS = 0x1E;
    public String name = "Magnetometer";
    public int NUMPLOTS = 3;
    public String[] PLOTNAMES = {"Bx", "By", "Bz"};

    private final I2C i2c;

    public HMC5883L(I2C i2c, ScienceLab scienceLab) throws IOException {
        this.i2c = i2c;
        if (scienceLab.isConnected()) {
            init();
        }
    }

    private void init() throws IOException {
        writeCONFA();
        writeCONFB();
        i2c.writeBulk(ADDRESS, new int[]{MODE, 0});       //enable continuous measurement mode

    }

    private void writeCONFB() throws IOException {
        i2c.writeBulk(ADDRESS, new int[]{CONFB, gainValue << 5});     //set gain
    }


    private void writeCONFA() throws IOException {
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

    public List<Character> getVals(int addr, int bytes) throws IOException {
        return i2c.readBulk(ADDRESS, addr, bytes);
    }

    public List<Double> getRaw() throws IOException {
        List<Double> returnList = new ArrayList<>();
        List<Character> vals = getVals(0x03, 6);
        if (vals.size() == 6) {
            for (int a = 0; a < 3; a++) {
                returnList.add((vals.get(a * 2) << 8 | vals.get(a * 2 + 1)) / scaling.get(gainValue));
            }
            return returnList;
        } else
            return null;
    }

}

