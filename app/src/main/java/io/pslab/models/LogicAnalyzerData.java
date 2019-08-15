package io.pslab.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class LogicAnalyzerData extends RealmObject {
    @PrimaryKey
    private long time;
    private long block;
    private String dataX, dataY, channel;
    private double lat, lon;
    private int channelMode;

    public LogicAnalyzerData() {/**/}

    public LogicAnalyzerData(long time, long block, String channel, int channelMode, String dataX, String dataY, double lat, double lon) {
        this.time = time;
        this.block = block;
        this.channel = channel;
        this.channelMode = channelMode;
        this.dataX = dataX;
        this.dataY = dataY;
        this.lat = lat;
        this.lon = lon;
    }

    public long getBlock() {
        return block;
    }

    public void setBlock(long block) {
        this.block = block;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public int getChannelMode() {
        return channelMode;
    }

    public void setChannelMode(int channelMode) {
        this.channelMode = channelMode;
    }

    public String getDataY() {
        return dataY;
    }

    public void setDataY(String dataY) {
        this.dataY = dataY;
    }

    public String getDataX() {
        return dataX;
    }

    public void setDataX(String dataX) {
        this.dataX = dataX;
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
        return "Block - " + block + ", Time - " + time + ", Channel - " + channel + ", ChannelMode - " + channelMode + ", dataX - " + dataX + ", dataY - " + dataY + ", Lat - " + lat + ", Lon - " + lon;
    }
}
