package org.fossasia.pslab.communication.peripherals;

import org.fossasia.pslab.communication.PacketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by viveksb007 on 28/3/17.
 */

public class MCP4728 {

    int defaultVDD = 3300;
    int RESET = 6;
    int WAKEUP = 9;
    int UPDATE = 8;
    int WRITEALL = 64;
    int WRITEONE = 88;
    int SEQWRITE = 80;
    int VREFWRITE = 128;
    int GAINWRITE = 192;
    int POWERDOWNWRITE = 160;
    int GENERALCALL = 0;
    private PacketHandler packetHandler;
    private double vref;
    private int devid;
    private I2C i2c;
    private List<Integer> switchedOff;
    private List<Integer> vRefs;
    public Map<String, DACChannel> chans = new LinkedHashMap<>();
    private Map<Integer, String> channelMap = new LinkedHashMap<>();
    private Map<String, Double> values = new LinkedHashMap<>();
    private int addr;

    public MCP4728(PacketHandler packetHandler, I2C i2c) {
        this.packetHandler = packetHandler;
        this.vref = 3.3;
        this.devid = 0;
        switchedOff = new ArrayList<>(Arrays.asList(0, 0, 0, 0));
        vRefs = new ArrayList<>(Arrays.asList(0, 0, 0, 0));
        this.i2c = i2c;
        addr = 0x60 | devid;
        chans.put("PCS", new DACChannel("PCS", new double[]{0, 3.3e-3}, 0));
        chans.put("PV3", new DACChannel("PV3", new double[]{0, 3.3}, 1));
        chans.put("PV2", new DACChannel("PV2", new double[]{-3.3, 3.3}, 2));
        chans.put("PV1", new DACChannel("PV1", new double[]{-5., 5.}, 3));
        channelMap.put(0, "PCS");
        channelMap.put(1, "PV3");
        channelMap.put(2, "PV2");
        channelMap.put(3, "PV1");
        values.put("PV1", 0.);
        values.put("PV2", 0.);
        values.put("PV3", 0.);
        values.put("PCS", 0.);
    }

    public void ignoreCalibration(String name) {
        chans.get(name).calibrationEnabled = "false";
    }

    public double setVoltage(String name, double val) {
        DACChannel dacChannel = chans.get(name);
        int v = (int) (Math.round(dacChannel.VToCode.value(val)));
        return setRawVoltage(name, v);
    }

    public Double getVoltage(String name) {
        return this.values.get(name);
    }

    public double setCurrent(float val) {
        DACChannel dacChannel = chans.get("PCS");
        int v = (int) (Math.round(dacChannel.VToCode.value(val)));
        return setRawVoltage("PCS", v);
    }

    private double setRawVoltage(String name, int v) {
        DACChannel CHAN = chans.get(name);
        int val;
        if (v <= 0) {
            v = 0;
        } else if (v >= 4095) {
            v = 4095;
        }
        val = CHAN.applyCalibration(v);
        try {
            i2c.writeBulk(addr, new int[]{64 | (CHAN.channum << 1), (val >> 8) & 0x0F, val & 0xFF});
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.values.put(name, CHAN.CodeToV.value(v));
        return this.values.get(name);
    }

    public void writeAll(int v1, int v2, int v3, int v4) {
        try {
            i2c.start(addr, 0);
            i2c.send((v1 >> 8) & 0xF);
            i2c.send(v1 & 0xFF);
            i2c.send((v2 >> 8) & 0xF);
            i2c.send(v2 & 0xFF);
            i2c.send((v3 >> 8) & 0xF);
            i2c.send(v3 & 0xFF);
            i2c.send((v4 >> 8) & 0xF);
            i2c.send(v4 & 0xFF);
            i2c.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stat() {
        try {
            i2c.start(addr, 0);
            i2c.send(0x0);
            i2c.restart(addr, 1);
            i2c.read(24);
            i2c.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
