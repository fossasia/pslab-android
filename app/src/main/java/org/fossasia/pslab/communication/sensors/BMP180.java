package org.fossasia.pslab.communication.sensors;

import android.util.Log;
import org.fossasia.pslab.communication.peripherals.I2C;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import static java.lang.Math.pow;

/**
 * Created by akarshan on 4/18/17.
 */

public class BMP180 {

    private int ADDRESS = 0x77;
    private int REG_CONTROL = 0xF4;
    private int REG_RESULT = 0xF6;
    private int CMD_TEMP = 0x2E;
    private int CMD_P0 = 0x34;
    private int CMD_P1 = 0x74;
    private int CMD_P2 = 0xB4;
    private int CMD_P3 = 0xF4;
    private int oversampling = 0;

    private int NUMPLOTS = 3;
    String[] PLOTNAMES = {"Temperature", "Pressure", "Altitude"};
    String name = "Altimeter BMP180";
    private I2C i2c;

    private int MB = readInt(0xBA);
    private double c3 = 160 * pow(2, -15) * readInt(0xAE);
    private double c4 = pow(10, -3) * pow(2, -15) * readUInt(0xB0);
    private double b1 = pow(160, 2) * pow(2, -30) * readInt(0xB6);
    private double c5 = (pow(2, -15) / 160) * readUInt(0xB2);
    private double c6 = readUInt(0xB4);
    private double mc = (pow(2, 11) / pow(160, 2)) * readInt(0xBC);
    private double md = readInt(0xBE) / 160.0;
    private double x0 = readInt(0xAA);
    private double x1 = 160.0 * pow(2, -13) * readInt(0xAC);
    private double x2 = pow(160, 2) * pow(2, -25) * readInt(0xB8);
    private double y0 = c4 * pow(2, 15);
    private double y1 = c4 * c3;
    private double y2 = c4 * b1;
    private double p0 = (3791.0 - 8.0) / 1600.0;
    private double p1 = 1.0 - 7357.0 * pow(2, -20);
    private double p2 = 3038.0 * 100.0 * pow(2, -36);
    private double T = 25;
    private double PR;
    private double baseline;

    private HashMap<String, ArrayList> params = new java.util.HashMap<>();


    public BMP180(I2C i2c) throws IOException, InterruptedException
    {
        i2c = this.i2c;
        Log.v("calib", (new double[]{c3, c4, b1, c5, c6, mc, md, x0, x1, x2, y0, y1, p0, p1, p2}).toString());
        params.put("setOversampling",new ArrayList(Arrays.asList(0,1,2,3)));
        initTemperature();
        readTemperature();
        initPressure();
        baseline = readPressure();
    }

    int readInt(int addr)
    {
        return(Integer.parseInt(Integer.toString(addr).trim()));
    }
    double readUInt(int addr) throws IOException
    {
        double v;
        ArrayList<Character> vals = i2c.readBulk(ADDRESS, addr, 2);
        v = 1. * ((vals.get(0) << 8) | vals.get(1));
        return v;
    }

    void initTemperature() throws IOException, InterruptedException {
        i2c.writeBulk(ADDRESS, new int[]{REG_CONTROL, CMD_TEMP});
        TimeUnit.MILLISECONDS.sleep(5);
    }

    Double readTemperature() throws IOException {
        ArrayList<Character> vals = i2c.readBulk(ADDRESS, REG_RESULT, 2);
        if (vals.size() == 2) {
            T = (vals.get(0) << 8) + vals.get(1);
            double a = c5 * (T - c6);
            T = a + (mc / (a + md));
            return T;
        } else {
            return null;
        }
    }

     void setOversampling(int num)
    {
        oversampling = num;
    }

    void initPressure() throws IOException, InterruptedException
    {
        int[] os = {0x34, 0x74, 0xb4, 0xf4};
        int [] delays = {5, 8, 14, 26};
        i2c.writeBulk(ADDRESS, new int[] {REG_CONTROL, oversampling});
        TimeUnit.MILLISECONDS.sleep(delays[oversampling]);

    }

    Double readPressure() throws IOException
    {
        ArrayList<Character> vals = i2c.readBulk(ADDRESS,REG_RESULT, 3);
        if (vals.size() == 3)
        {
            double P = 1. * (vals.get(0) << 8) + vals.get(1) + (vals.get(2) / 256.0);
            double s = T - 25.0;
            double x = (x2 * pow(s, 2)) + (x1 * s) + x0;
            double y = (y2 * pow(s, 2)) + (y1 * s) + y0;
            double z = (P - x) / y;
            PR = (p2 * pow(z, 2)) + (p1 * z) + p0;
            return PR;
        }
        else
        {
            return null;
        }
    }

    double altitude()
    {
        // baseline pressure needs to be provided
        return (44330.0 * (1 - pow( PR / baseline, 1 / 5.255)));
    }

   double sealevel(double P, double A)
   {
       //given a calculated pressure and altitude, return the sealevel
       return (PR / pow(1 - (A / 44330.0), 5.255));
   }

    double[] getRaw() throws IOException, InterruptedException
    {
        initTemperature();
        readTemperature();
        initPressure();
        readPressure();
        return (new double[]{T, PR, altitude()});
    }

}
