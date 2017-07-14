package org.fossasia.pslab.communication.sensors;

import org.fossasia.pslab.communication.peripherals.I2C;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class ADS1115 {
    private int ADDRESS = 0x48;
    private I2C i2c;

    private int REG_POINTER_MASK = 0x3;
    private int REG_POINTER_CONVERT = 0;
    private int REG_POINTER_CONFIG = 1;
    private int REG_POINTER_LOWTHRESH = 2;
    private int REG_POINTER_HITHRESH = 3;

    private int REG_CONFIG_OS_MASK = 0x8000;
    private int REG_CONFIG_OS_SINGLE = 0x8000;
    private int REG_CONFIG_OS_BUSY = 0x0000;
    private int REG_CONFIG_OS_NOTBUSY = 0x8000;

    private int REG_CONFIG_MUX_MASK = 0x7000;
    private int REG_CONFIG_MUX_DIFF_0_1 = 0x0000;
    private int REG_CONFIG_MUX_DIFF_0_3 = 0x1000;
    private int REG_CONFIG_MUX_DIFF_1_3 = 0x2000;
    private int REG_CONFIG_MUX_DIFF_2_3 = 0x3000;
    private int REG_CONFIG_MUX_SINGLE_0 = 0x4000;
    private int REG_CONFIG_MUX_SINGLE_1 = 0x5000;
    private int REG_CONFIG_MUX_SINGLE_2 = 0x6000;
    private int REG_CONFIG_MUX_SINGLE_3 = 0x7000;

    private int REG_CONFIG_PGA_MASK = 0x0E00;
    private int REG_CONFIG_PGA_6_144V = 0 << 9;
    private int REG_CONFIG_PGA_4_096V = 1 << 9;
    private int REG_CONFIG_PGA_2_048V = 2 << 9;
    private int REG_CONFIG_PGA_1_024V = 3 << 9;
    private int REG_CONFIG_PGA_0_512V = 4 << 9;
    private int REG_CONFIG_PGA_0_256V = 5 << 9;

    private int REG_CONFIG_MODE_MASK = 0x0100;
    private int REG_CONFIG_MODE_CONTIN = 0 << 8;
    private int REG_CONFIG_MODE_SINGLE = 1 << 8;

    private int REG_CONFIG_DR_MASK = 0x00E0;
    private int REG_CONFIG_DR_8SPS = 0 << 5;
    private int REG_CONFIG_DR_16SPS = 1 << 5;
    private int REG_CONFIG_DR_32SPS = 2 << 5;
    private int REG_CONFIG_DR_64SPS = 3 << 5;
    private int REG_CONFIG_DR_128SPS = 4 << 5;
    private int REG_CONFIG_DR_250SPS = 5 << 5;
    private int REG_CONFIG_DR_475SPS = 6 << 5;
    private int REG_CONFIG_DR_860SPS = 7 << 5;

    private int REG_CONFIG_CMODE_MASK = 0x0010;
    private int REG_CONFIG_CMODE_TRAD = 0x0000;
    private int REG_CONFIG_CMODE_WINDOW = 0x0010;

    private int REG_CONFIG_CPOL_MASK = 0x0008;
    private int REG_CONFIG_CPOL_ACTVLOW = 0x0000;
    private int REG_CONFIG_CPOL_ACTVHI = 0x0008;

    private int REG_CONFIG_CLAT_MASK = 0x0004;
    private int REG_CONFIG_CLAT_NONLAT = 0x0000;
    private int REG_CONFIG_CLAT_LATCH = 0x0004;

    private int REG_CONFIG_CQUE_MASK = 0x0003;
    private int REG_CONFIG_CQUE_1CONV = 0x0000;
    private int REG_CONFIG_CQUE_2CONV = 0x0001;
    private int REG_CONFIG_CQUE_4CONV = 0x0002;
    private int REG_CONFIG_CQUE_NONE = 0x0003;

    private HashMap<String, Integer> gains = new HashMap<String, Integer>();
    private HashMap<String, Double> gainScaling = new HashMap<String, Double>();
    private HashMap<String, String> typeSelection = new HashMap<String, String>();
    private HashMap<Integer, Integer> sdrSelection = new HashMap<Integer, Integer>();

    private String channel;
    private String gain;
    private int rate;

    public int NUMPLOTS = 1;
    public String[] PLOTNAMES = {"mV"};

    public ADS1115(I2C i2c) throws IOException, InterruptedException {
        this.i2c = i2c;
        channel = "UNI_0";
        gain = "GAIN_ONE";
        rate = 128;

        setGain("GAIN_ONE");
        setChannel("UNI_0");
        setDataRate(128);

        int conversionDelay = 8;
        String name = "ADS1115 16-bit ADC";

        gains.put("GAIN_TWOTHIRDS", REG_CONFIG_PGA_6_144V);
        gains.put("GAIN_ONE", REG_CONFIG_PGA_4_096V);
        gains.put("GAIN_TWO", REG_CONFIG_PGA_2_048V);
        gains.put("GAIN_FOUR", REG_CONFIG_PGA_1_024V);
        gains.put("GAIN_EIGHT", REG_CONFIG_PGA_0_512V);
        gains.put("GAIN_SIXTEEN", REG_CONFIG_PGA_0_256V);

        gainScaling.put("GAIN_TWOTHIRDS", 0.1875);
        gainScaling.put("GAIN_ONE", 0.125);
        gainScaling.put("GAIN_TWO", 0.0625);
        gainScaling.put("GAIN_FOUR", 0.03125);
        gainScaling.put("GAIN_EIGHT", 0.015625);
        gainScaling.put("GAIN_SIXTEEN", 0.0078125);

        typeSelection.put("UNI_0", "0");
        typeSelection.put("UNI_1", "1");
        typeSelection.put("UNI_2", "2");
        typeSelection.put("UNI_3", "3");
        typeSelection.put("DIFF_01", "01");
        typeSelection.put("DIFF_23", "23");

        sdrSelection.put(8, REG_CONFIG_DR_8SPS);
        sdrSelection.put(16, REG_CONFIG_DR_16SPS);
        sdrSelection.put(32, REG_CONFIG_DR_32SPS);
        sdrSelection.put(64, REG_CONFIG_DR_64SPS);
        sdrSelection.put(128, REG_CONFIG_DR_128SPS);
        sdrSelection.put(250, REG_CONFIG_DR_250SPS);
        sdrSelection.put(475, REG_CONFIG_DR_475SPS);
        sdrSelection.put(860, REG_CONFIG_DR_860SPS);

    }

    public int readInt(int addr) throws IOException, InterruptedException {
        ArrayList<Character> vals = i2c.readBulk(ADDRESS, addr, 2);
        int v = (int) (1. * ((vals.get(0) << 8) | vals.get(1)));
        return v;
    }

    public void initTemperature() throws IOException, InterruptedException {
        i2c.writeBulk(ADDRESS, new int[]{ADDRESS});
        TimeUnit.SECONDS.sleep((long) 0.005);
    }

    public int readRegister(int register) throws IOException {
        ArrayList<Character> vals = i2c.readBulk(ADDRESS, register, 2);
        return (vals.get(0) << 8) | vals.get(1);
    }

    public void writeRegister(int reg, int value) throws IOException {
        i2c.writeBulk(ADDRESS, new int[]{ADDRESS, (value >> 8) & 0xFF, value & 0xFF});
    }

    public void setGain(String gain) {
        /*options : 'GAIN_TWOTHIRDS','GAIN_ONE','GAIN_TWO','GAIN_FOUR','GAIN_EIGHT','GAIN_SIXTEEN'*/
        this.gain = gain;
    }

    public void setChannel(String channel) {
        /*options 'UNI_0','UNI_1','UNI_2','UNI_3','DIFF_01','DIFF_23'*/
        this.channel = channel;
    }

    public void setDataRate(int rate) {
        /*data rate options 8,16,32,64,128,250,475,860 SPS*/
        this.rate = rate;
    }

    public double readADCSingleEnded(int chan) throws IOException, InterruptedException {
        if (chan > 3) {
            return -1;
        }
        //start with default values
        int config = REG_CONFIG_CQUE_NONE             //Disable the comparator (default val)
                | REG_CONFIG_CLAT_NONLAT              //Non-latching (default val)
                | REG_CONFIG_CPOL_ACTVLOW             //Alert/Rdy active low   (default val)
                | REG_CONFIG_CMODE_TRAD               // Traditional comparator (default val)
                | REG_CONFIG_MODE_SINGLE              // Single-shot mode (default)
                | sdrSelection.get(rate);            //1600 samples per second (default)

        //Set PGA/voltage range
        config = config | gains.get(gain);

        if (chan == 0)
            config = config | REG_CONFIG_MUX_SINGLE_0;
        else if (chan == 1)
            config = config | REG_CONFIG_MUX_SINGLE_1;
        else if (chan == 2)
            config = config | REG_CONFIG_MUX_SINGLE_2;
        else if (chan == 3)
            config = config | REG_CONFIG_MUX_SINGLE_3;

        //Set 'start single-conversion' bit
        config = config | REG_CONFIG_OS_SINGLE;
        writeRegister(REG_POINTER_CONFIG, config);
        TimeUnit.MILLISECONDS.sleep((long) ((1. / rate + 0.002) * 1000));       //convert to mS to S
        return readRegister(REG_POINTER_CONVERT) * gainScaling.get(gain);
    }

    public short readADCDifferential(String chan) throws IOException, InterruptedException {
        //start with default values
        int config = REG_CONFIG_CQUE_NONE              //Disable the comparator (default val)
                | REG_CONFIG_CLAT_NONLAT               //Non-latching (default val)
                | REG_CONFIG_CPOL_ACTVLOW              //Alert/Rdy active low   (default val)
                | REG_CONFIG_CMODE_TRAD                // Traditional comparator (default val)
                | REG_CONFIG_MODE_SINGLE               // Single-shot mode (default)
                | sdrSelection.get(rate);             //1600 samples per second (default)

        //Set PGA/voltage range
        config = config | gains.get(gain);

        if (chan.equals("01"))
            config = config | REG_CONFIG_MUX_DIFF_0_1;
        else if (chan.equals("23"))
            config = config | REG_CONFIG_MUX_DIFF_2_3;

        //Set 'start single-conversion' bit
        config = config | REG_CONFIG_OS_SINGLE;
        writeRegister(REG_POINTER_CONFIG, config);
        TimeUnit.MILLISECONDS.sleep((long) ((1. / rate + 0.002) * 1000));       //convert to mS to S

        return (short) (readRegister(REG_POINTER_CONVERT) * gainScaling.get(gain));
    }

    public short getLastResults() throws IOException {
        return (short) (readRegister(REG_POINTER_CONVERT) * gainScaling.get(gain));
    }

    public int[] getRaw() throws IOException, InterruptedException {
        //return values in mV
        String chan = typeSelection.get(channel);
        if (channel.contains("UNI"))
            return new int[]{(int) readADCSingleEnded(Integer.parseInt(chan))};
        else if (channel.contains("DIF"))
            return new int[]{readADCDifferential(chan)};
        return new int[0];
    }
}
