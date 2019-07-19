package io.pslab.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class MultimeterData extends RealmObject {
    @PrimaryKey
    private long time;
    private long block;
    private String data;
    private String value;
    private double lat;
    private double lon;

    public MultimeterData() {/**/}

    public MultimeterData(long time, long block, String data, String value, double lat, double lon) {
        this.time = time;
        this.block = block;
        this.data = data;
        this.value = value;
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

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    @Override
    public String toString() {
        return "Block - " + block + ", Time - " + time + ", Data - " + data + ", Value - " + value + ", Lat - " + lat + ", Lon - " + lon;
    }
}
