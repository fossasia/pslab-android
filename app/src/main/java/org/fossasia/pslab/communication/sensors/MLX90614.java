package org.fossasia.pslab.communication.sensors;


import android.util.Log;
import org.fossasia.pslab.communication.peripherals.I2C;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by akarshan on 4/17/17.
 */

public class MLX90614 {

    private String TAG = "MLX90614";
    int NUMPLOTS = 1;
    private int ADDRESS = 0x5A;;
    private String[] PLOTNAMES = {"Temp"};
    private String name = "PIR temperature";

    private I2C i2c;
    private int source, OBJADDR = 0x07, AMBADDR = 0x06;
    private HashMap<String, ArrayList> params = new HashMap<>();

    public MLX90614(I2C i2c) throws IOException {
        i2c = this.i2c;
        int source =OBJADDR;
        String name = "Passive IR temperature sensor";
        try {
            Log.d(TAG,"switching baud to 100k");
            i2c.config((int)100e3);
        }
        catch (Exception e)
        {
            Log.d(TAG,"FAILED TO CHANGE BAUD RATE");
        }
        ArrayList<Integer> list = new ArrayList<>();
        for(int i = 0;i < 0x20;i++)
        {
            list.add(i);
        }
        //parameters
        params.put("readReg", list);
        params.put("selectSource", new ArrayList(Arrays.asList("object temperature","ambient temperature")) );
    }

    void selectSource(String source)
    {
        int _source_;
        if(source.equals("object temperature"))
            _source_ = OBJADDR;
        else if (source.equals("ambient temperature"))
            _source_ = AMBADDR;
    }

    void readReg(int addr) throws IOException
    {
        ArrayList<Character> x = getVals(addr, 2);
        Log.v(TAG, Integer.toHexString(addr)+" "+Integer.toHexString(x.get(0) | (x.get(1) << 8)));
    }

    ArrayList<Character> getVals(int addr, int bytes) throws IOException
    {
        ArrayList<Character> vals = i2c.readBulk(ADDRESS, addr, bytes);
        return vals;
    }

    Double[] getRaw() throws IOException
    {
        ArrayList<Character> vals = getVals(source,3);
        if (vals.size() == 3)
            return new Double[]{((((vals.get(1) & 0x007f) << 8) + vals.get(0)) * 0.02) - 0.01 - 273.15};
        else
            return null;
    }

    Double getObjectTemperature() throws IOException
    {
        source = OBJADDR;
        Double [] val = getRaw();
        if (val.length != 0)
            return val[0];
        else
            return null;  //returning null instead of false to match the return data-type. Used wrapper class Double because null cannot be returned on primitive data-type.

    }

    Double getAmbientTemperature() throws IOException
    {
        source = AMBADDR;
        Double[] val = getRaw();
        if (val.length != 0)
            return val[0];
        else
            return null;
    }

}
