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
    private List<Integer> SWITCHEDOFF;
    private List<Integer> VREFS;
    private Map<String, DACChannel> CHANS = new LinkedHashMap<>();
    private Map<Integer, String> CHANNELMAP = new LinkedHashMap<>();
    private int addr;

    public MCP4728(PacketHandler packetHandler) {
        this.packetHandler = packetHandler;
        this.vref = 3.3;
        this.devid = 0;
        SWITCHEDOFF = new ArrayList<>(Arrays.asList(0, 0, 0, 0));
        VREFS = new ArrayList<>(Arrays.asList(0, 0, 0, 0));
        i2c = new I2C(packetHandler);
        addr = 0x60 | devid;
        CHANS.put("PCS", new DACChannel("PCS", new double[]{0, 3.3e-3}, 0));
        CHANS.put("PV3", new DACChannel("PV3", new double[]{0, 3.3}, 1));
        CHANS.put("PV2", new DACChannel("PV2", new double[]{-3.3, 3.3}, 2));
        CHANS.put("PV1", new DACChannel("PV1", new double[]{-5., 5.}, 3));
        CHANNELMAP.put(0, "PCS");
        CHANNELMAP.put(1, "PV3");
        CHANNELMAP.put(2, "PV2");
        CHANNELMAP.put(3, "PV1");
    }

    void ignoreCalibration(String name) {
        CHANS.get(name).calibration_enabled = "false";
    }

    public double setVoltage(String name, int v) {
        DACChannel dacChannel = CHANS.get(name);
        v = (int) (Math.round(dacChannel.VToCode.get(0) + dacChannel.VToCode.get(1) * v);
        return setRawVoltage("name", v);
    }

    public int getVoltage(String name) {
        // todo : add method when resolved in pslab-python
        return -1;
    }

    public double setCurrent(int v) {
        DACChannel dacChannel = CHANS.get("PCS");
        v = (int) (Math.round(dacChannel.VToCode.get(0) + dacChannel.VToCode.get(1) * v);
        return setRawVoltage("PCS", v);
    }

    private double setRawVoltage(String name, int v) {
        DACChannel CHAN = CHANS.get(name);
        int val;
        if (v <= 0) {
            v = 0;
        } else if (v >= 4095) {
            v = 4095;
        }
        val = CHAN.ApplyCalibration(v);
        try {
            i2c.writeBulk(addr, new int[]{64 | (CHAN.channum << 1), (val >> 8) & 0x0F, val & 0xFF});
        } catch (IOException e) {
            e.printStackTrace();
        }
        return (CHAN.VToCode.get(0) + CHAN.VToCode.get(1) * v + CHAN.VToCode.get(2) * v * v);
    }

    void writeAll(int v1, int v2, int v3, int v4) {
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

    void stat() {
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
