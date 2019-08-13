package io.pslab.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Padmal on 12/13/18.
 */

public class BaroData extends RealmObject {

    @PrimaryKey
    private long time;
    private long block;
    private float baro;
    private float altitude;
    private double lat, lon;

    public BaroData() {/**/}

    public BaroData(long time, long block, float baro, float altitude, double lat, double lon) {
        this.time = time;
        this.block = block;
        this.baro = baro;
        this.altitude = altitude;
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

    public float getBaro() {
        return baro;
    }

    public void setBaro(float baro) {
        this.baro = baro;
    }

    public float getAltitude() {
        return altitude;
    }

    public void setAltitude(float altitude) {
        this.altitude = altitude;
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
        return "Block - " + block + ", Time - " + time + ", Baro - " + baro + ", Altitude - " + altitude + ", Lat - " + lat + ", Lon - " + lon;
    }
}