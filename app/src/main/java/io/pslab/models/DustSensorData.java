package io.pslab.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class DustSensorData extends RealmObject {

    @PrimaryKey
    private long time;
    private long block;
    private float ppmValue;
    private double lat, lon;

    public DustSensorData() { /**/ }

    public DustSensorData(long time, long block, float ppmValue, double lat, double lon) {
        this.time = time;
        this.block = block;
        this.ppmValue = ppmValue;
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

    public float getPpmValue() {
        return ppmValue;
    }

    public void setPpmValue(float ppmValue) {
        this.ppmValue = ppmValue;
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
        return "Block - " + block + ", Time - " + time + ", PPM value - " + ppmValue + ", Lat - " + lat + ", Lon - " + lon;
    }
}
