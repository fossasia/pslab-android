package org.fossasia.pslab.communication.sensors;

import android.util.Log;

import org.fossasia.pslab.communication.peripherals.I2C;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Created by akarshan on 4/16/17.
 */

public class SHT21 {
    private String TAG = "SHT21";
    private int RESET = 0XFE;
    private int TEMP_ADDRESS = 0xF3;
    private int HUMIDITY_ADDRESS = 0xF5;
    private int selected = 0xF3;
    private int ADDRESS = 0x40;

    public int NUMPLOTS = 1;
    public String[] PLOTNAMES = {"Data"};
    public String name = "Humidity/Temperature";

    public ArrayList<String> selectParameter = new ArrayList<>(Arrays.asList("temperature", "humidity"));
    private I2C i2c;

    public SHT21(I2C i2c) throws IOException, InterruptedException {
        this.i2c = i2c;
        init();
    }

    private void init() throws IOException, InterruptedException {
        i2c.writeBulk(ADDRESS, new int[]{RESET});   //soft reset
        TimeUnit.MILLISECONDS.sleep(100);
    }

    public ArrayList<Double> rawToTemp(ArrayList<Byte> vals) {
        double v;
        ArrayList<Double> v1 = new ArrayList<>();
        if (vals.size() != 0) {
            v = (vals.get(0) << 8) | (vals.get(1) & 0xFC);
            v *= 175.72;
            v /= (1 << 16);
            v -= 46.85;
            v1.add(v);
            return v1;
        } else return null;
    }

    public ArrayList<Double> rawToRH(ArrayList<Byte> vals) {
        double v;
        ArrayList<Double> v1 = new ArrayList<>();
        if (vals.size() != 0) {
            v = (vals.get(0) << 8) | (vals.get(1) & 0xFC);
            v *= 125.;
            v /= (1 << 16);
            v -= 6;
            v1.add(v);
            return v1;
        } else return null;
    }

    public static int calculateChecksum(ArrayList<Byte> data, int numberOfBytes) {

        //CRC
        int POLYNOMIAL = 0x131, byteCtr, crc = 0;
        //calculates 8-Bit checksum with given polynomial
        for (byteCtr = 0; byteCtr < numberOfBytes; byteCtr++) {
            crc ^= data.get(byteCtr);
            for (int bit = 8; bit > 0; bit--) {
                if ((crc & 0X80) != 0)
                    crc = (crc << 1) ^ POLYNOMIAL;
                else
                    crc = crc << 1;
            }
        }
        return crc;
    }

    public void selectParameter(String param) {
        if (param.equals("temperature"))
            selected = TEMP_ADDRESS;
        else if (param.equals("humidity"))
            selected = HUMIDITY_ADDRESS;
    }

    public ArrayList<Double> getRaw() throws IOException, InterruptedException {
        ArrayList<Byte> vals;
        i2c.writeBulk(ADDRESS, new int[]{selected});
        if (selected == TEMP_ADDRESS)
            TimeUnit.MILLISECONDS.sleep(100);
        else if (selected == HUMIDITY_ADDRESS)
            TimeUnit.MILLISECONDS.sleep(50);
        vals = i2c.simpleRead(ADDRESS, 3);
        if (vals.size() != 0) {
            if (calculateChecksum(vals, 2) != vals.get(2))
                Log.v(TAG, vals.toString());
            return null;
        }
        if (selected == TEMP_ADDRESS)
            return rawToTemp(vals);
        else if (selected == HUMIDITY_ADDRESS)
            return rawToRH(vals);
        else
            return null;
    }

}
