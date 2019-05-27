package io.pslab.models;

import io.realm.RealmObject;

public class ThermometerData extends RealmObject {
    private long time;
    private long block;
    private float temp;
    private double lat, lon;

    public ThermometerData() {/**/}

    public ThermometerData(long time, long block,float temp, double lat, double lon) {

        this.time = time;
        this.block = block;
        this.lat = lat;
        this.lon = lon;
        this.temp = temp;

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

    public float getTemp() {
        return temp;
    }

    public void setTemp(float temp) {
        this.temp = temp;
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
        return "Block - " + block + ", Time - " + time + ",Temprature - " + temp + ", Lat - " + lat + ", Lon - " + lon;
    }
}
