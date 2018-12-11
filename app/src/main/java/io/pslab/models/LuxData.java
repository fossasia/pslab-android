package io.pslab.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Avjeet on 31-07-2018.
 */

public class LuxData extends RealmObject {

    @PrimaryKey
    private long time;
    private long block;
    private float lux;
    private double lat, lon;

    public LuxData() {/**/}

    public LuxData(long time, long block, float lux, double lat, double lon) {
        this.time = time;
        this.block = block;
        this.lux = lux;
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

    public float getLux() {
        return lux;
    }

    public void setLux(float lux) {
        this.lux = lux;
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
        return "Block - " + block + ", Time - " + time + ", Lux - " + lux + ", Lat - " + lat + ", Lon - " + lon;
    }
}
