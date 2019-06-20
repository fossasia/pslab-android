package io.pslab.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class OscilloscopeData extends RealmObject {

    @PrimaryKey
    private long time;
    private long block;
    private String dataX, dataY, channel;
    private double lat, lon, timebase;
    private int mode;

    public OscilloscopeData() {/**/}

    public OscilloscopeData(long time, long block, int mode, String channel, String dataX, String dataY, double timebase, double lat, double lon) {
        this.time = time;
        this.block = block;
        this.mode = mode;
        this.channel = channel;
        this.dataX = dataX;
        this.dataY = dataY;
        this.timebase = timebase;
        this.lat = lat;
        this.lon = lon;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getBlock() {
        return block;
    }

    public void setBlock(long block) {
        this.block = block;
    }

    public String getDataX() {
        return dataX;
    }

    public void setDataX(String dataX) {
        this.dataX = dataX;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public String getDataY() {
        return dataY;
    }

    public void setDataY(String dataY) {
        this.dataY = dataY;
    }

    public double getTimebase() {
        return timebase;
    }

    public void setTimebase(double timebase) {
        this.timebase = timebase;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    @Override
    public String toString() {
        return "Block - " + block + ", Time - " + time + ", Channel - " + channel + ", Mode - " + mode + ", dataX - " + dataX + ", dataY - " + dataY + ", timebase - " + timebase + ", Lat - " + lat + ", Lon - " + lon;
    }
}
