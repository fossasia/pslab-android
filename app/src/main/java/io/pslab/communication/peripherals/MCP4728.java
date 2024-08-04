package io.pslab.communication.peripherals;

import io.pslab.communication.PacketHandler;

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
    private Map<Integer, String> channelMap = new LinkedHashMap<>();
    private int addr;

    public MCP4728(PacketHandler packetHandler, I2C i2c) {
        this.packetHandler = packetHandler;
        this.vref = 3.3;
        this.devid = 0;
        switchedOff = new ArrayList<>(Arrays.asList(0, 0, 0, 0));
        vRefs = new ArrayList<>(Arrays.asList(0, 0, 0, 0));
        this.i2c = i2c;
        addr = 0x60 | devid;
        channelMap.put(0, "PCS");
        channelMap.put(1, "PV3");
        channelMap.put(2, "PV2");
        channelMap.put(3, "PV1");

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
