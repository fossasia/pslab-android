package io.pslab.communication.sensors;

import android.util.Log;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.pslab.communication.ScienceLab;
import io.pslab.communication.peripherals.I2C;

/**
 * Created by akarshan on 4/15/17.
 */

public class TSL2561 {
    private final String TAG = "TSL2561";
    private final int VISIBLE = 2;  // channel 0 - channel 1
    private final int INFRARED = 1;  // channel 1
    private final int FULLSPECTRUM = 0;  // channel 0

    private final int READBIT = 0x01;
    private final int COMMAND_BIT = 0x80; // Must be 1

    private final int CONTROL_POWERON = 0x03;
    private final int CONTROL_POWEROFF = 0x00;

    private final int REGISTER_CONTROL = 0x00;
    private final int REGISTER_TIMING = 0x01;
    private final int REGISTER_ID = 0x0A;

    private final int INTEGRATIONTIME_13MS = 0x00;  // 13.7ms
    private final int INTEGRATIONTIME_101MS = 0x01;  // 101ms
    private final int INTEGRATIONTIME_402MS = 0x02;  // 402ms

    private final int GAIN_1X = 0x00;     // No gain
    private final int GAIN_16X = 0x10;    // 16x gain
    private int GAIN_OX;

    private final int ADDRESS = 0x39;  // addr normal
    private int timing = INTEGRATIONTIME_13MS;
    private int gain = GAIN_16X;

    public String name = "TSL2561 Luminosity";
    public int NUMPLOTS = 3;
    public String[] PLOTNAMES = {"Full", "IR", "Visible"};

    private final I2C i2c;
    private int full, infra;
    private List<Character> infraList, fullList;
    private final List<java.io.Serializable> setGain = Arrays.asList("1x", "16x");
    private final List<java.io.Serializable> setTiming = Arrays.asList(0, 1, 2);

    public TSL2561(I2C i2c, ScienceLab scienceLab) throws IOException, InterruptedException {
        this.i2c = i2c;
        // set timing 101ms & 16x gain
        if (scienceLab.isConnected()) {
            enable();
            _wait();
            i2c.writeBulk(ADDRESS, new int[]{0x80 | 0x01, 0x01 | 0x10});
            //full scale luminosity
            infraList = i2c.readBulk(ADDRESS, 0x80 | 0x20 | 0x0E, 2);
            fullList = i2c.readBulk(ADDRESS, 0x80 | 0x20 | 0x0C, 2);
            full = (fullList.get(1) << 8) | fullList.get(0);
            infra = (infraList.get(1) << 8) | infraList.get(0);

            Log.v(TAG, "Full - " + full);
            Log.v(TAG, "Infrared - " + infra);
            Log.v(TAG, "Visible -" + (full - infra));
        }
    }

    public int getID() throws IOException {
        List<Character> _ID_ = i2c.readBulk(ADDRESS, REGISTER_ID, 1);
        int ID = Integer.parseInt(Character.getNumericValue(_ID_.get(0)) + "", 16);
        Log.d("ID", Integer.toString(ID));
        return ID;
    }

    public int[] getRaw() throws IOException {
        fullList = i2c.readBulk(ADDRESS, 0x80 | 0x20 | 0x0E, 2);
        infraList = i2c.readBulk(ADDRESS, 0x80 | 0x20 | 0x0C, 2);
        if (!infraList.isEmpty()) {
            full = (fullList.get(0) << 8) | fullList.get(0);
            infra = (infraList.get(0) << 8) | infraList.get(0);
            return (new int[]{full, infra, full - infra});
        } else
            return null;
    }

    public void setGain(String _gain_) throws IOException {
        if (_gain_.equals("1x"))
            gain = GAIN_1X;

        else if (_gain_.equals("16x"))
            gain = GAIN_16X;
        else
            gain = GAIN_OX;
        i2c.writeBulk(ADDRESS, new int[]{COMMAND_BIT | REGISTER_TIMING, gain | timing});
    }

    public void setTiming(int timing) throws IOException {
        Log.v(TAG, new int[]{13, 101, 404}[timing] + "mS");
        this.timing = timing;
        i2c.writeBulk(ADDRESS, new int[]{COMMAND_BIT | REGISTER_TIMING, gain | timing});
    }

    private void enable() throws IOException {
        i2c.writeBulk(ADDRESS, new int[]{COMMAND_BIT | REGISTER_CONTROL, CONTROL_POWERON});
    }

    public void disable() throws IOException {
        i2c.writeBulk(ADDRESS, new int[]{COMMAND_BIT | REGISTER_CONTROL, CONTROL_POWEROFF});
    }

    private void _wait() throws InterruptedException {
        if (timing == INTEGRATIONTIME_13MS) TimeUnit.MILLISECONDS.sleep(14);
        if (timing == INTEGRATIONTIME_101MS) TimeUnit.MILLISECONDS.sleep(102);
        if (timing == INTEGRATIONTIME_402MS) TimeUnit.MILLISECONDS.sleep(403);

    }

}
