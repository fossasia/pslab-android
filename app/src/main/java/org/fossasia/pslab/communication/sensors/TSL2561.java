package org.fossasia.pslab.communication.sensors;

import android.util.Log;
import org.fossasia.pslab.communication.peripherals.I2C;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by akarshan on 4/15/17.
 */

public class TSL2561 {
    private String TAG = "TSL2561";
    private int VISIBLE = 2;  // channel 0 - channel 1
    private int INFRARED = 1;  // channel 1
    private int FULLSPECTRUM = 0;  // channel 0

    private int READBIT = 0x01;
    private int COMMAND_BIT = 0x80; // Must be 1

    private int CONTROL_POWERON = 0x03;
    private int CONTROL_POWEROFF = 0x00;

    private int REGISTER_CONTROL = 0x00;
    private int REGISTER_TIMING = 0x01;
    private int REGISTER_ID = 0x0A;

    private int INTEGRATIONTIME_13MS = 0x00;  // 13.7ms
    private int INTEGRATIONTIME_101MS = 0x01;  // 101ms
    private int INTEGRATIONTIME_402MS = 0x02;  // 402ms

    private int GAIN_1X = 0x00;     // No gain
    private int GAIN_16X = 0x10;    // 16x gain
    private int GAIN_OX;

    private int ADDRESS = 0x39;  // addr normal
    private int timing = INTEGRATIONTIME_13MS;
    private int gain = GAIN_16X;

    String name = "TSL2561 Luminosity";
    int NUMPLOTS = 3;
    String[] PLOTNAMES = {"Full", "IR", "Visible"};

    private I2C i2c;
    private int _full_, _infra_;
    private ArrayList<Character> infra, full;
    private HashMap<String, ArrayList> params = new java.util.HashMap<>();


    public TSL2561(I2C i2c) throws IOException, InterruptedException {
        this.i2c = i2c;
        enable();
        _wait_();
        i2c.writeBulk(ADDRESS, new int[]{0x80 | 0x01, 0x01 | 0x10});

        //full scale luminosity
        infra = i2c.readBulk(ADDRESS,0x80 | 0x20 | 0x0E,2);
        full = i2c.readBulk(ADDRESS,0x80 | 0x20 | 0x0C, 2);
        _full_ =  (full.get(1) << 8) | full.get(0);
        _infra_ = (infra.get(1) << 8) | infra.get(0);

        //parameters
        params.put("setGain", new ArrayList(Arrays.asList("1x", "16x")));
        params.put("setTiming", new ArrayList(Arrays.asList(0, 1, 2)) );

        Log.v(TAG, "Full - " + Integer.toString(_full_));
        Log.v(TAG, "Infrared - " + Integer.toString(_infra_));
        Log.v(TAG, "Visible -"+Integer.toString(_full_ - _infra_));
    }

    int getID() throws IOException
    {
        ArrayList<Character> ID = i2c.readBulk(ADDRESS,REGISTER_ID,1);
        int _ID_ =  Integer.parseInt(Character.getNumericValue(ID.get(0)) +"",16);
        Log.d("ID",Integer.toString(_ID_));
        return _ID_;
    }

    int[] getRaw() throws IOException
    {

        full = i2c.readBulk(ADDRESS, 0x80 | 0x20 | 0x0E, 2);
        infra = i2c.readBulk(ADDRESS, 0x80 | 0x20 | 0x0C, 2);
        if(!infra.isEmpty())
        {
            _full_ = (full.get(0) << 8) | full.get(0);
            _infra_ = (infra.get(0) <<8) | infra.get(0);
            return (new int[] {_full_,_infra_,_full_ - _infra_});
        }
        else
        {
            return null;    //returning null instead of False to match return data-type
        }
    }

    void setGain(String gain) throws IOException
    {
        int _gain_;
        if(gain.equals("1x"))
        {
            _gain_ = GAIN_1X;
        }
        else if (gain.equals("16x"))
        {
            _gain_ = GAIN_16X;
        }
        else
        {
            _gain_ = GAIN_OX;
        }
        i2c.writeBulk(ADDRESS, new int[]{COMMAND_BIT | REGISTER_TIMING, _gain_ | timing});
    }

    void setTiming(int timing) throws IOException
    {
        System.out.println(new int[]{13,101,404}[timing]+ "mS");
        this.timing = timing;
        i2c.writeBulk(ADDRESS,new int[]{COMMAND_BIT | REGISTER_TIMING, gain | timing});
    }

    private void enable() throws IOException
    {
        i2c.writeBulk(ADDRESS,new int[]{COMMAND_BIT | REGISTER_CONTROL, CONTROL_POWERON});
    }

    void disable() throws IOException
    {
        i2c.writeBulk(ADDRESS, new int[]{COMMAND_BIT | REGISTER_CONTROL, CONTROL_POWEROFF});
    }

    private void _wait_() throws InterruptedException
    {
        if (timing == INTEGRATIONTIME_13MS)
        {
            TimeUnit.MILLISECONDS.sleep(14);
        }
        if (timing == INTEGRATIONTIME_101MS)
        {
            TimeUnit.MILLISECONDS.sleep(102);
        }
        if (timing == INTEGRATIONTIME_402MS)
        {
            TimeUnit.MILLISECONDS.sleep(403);
        }
    }
}
