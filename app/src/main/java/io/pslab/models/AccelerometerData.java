package io.pslab.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Kunal on 18-12-2018.
 */

public class AccelerometerData extends RealmObject {

    @PrimaryKey
    private long time;
    private long block;
    private float accelerometer_X;
    private float accelerometer_Y;
    private float accelerometer_Z;
    private double lat, lon;

    public AccelerometerData() {/**/}

    public AccelerometerData(long time, long block, float accelerometer_X, float accelerometer_Y, float accelerometer_Z, double lat, double lon) {
        this.time = time;
        this.block = block;
        this.accelerometer_X = accelerometer_X;
        this.accelerometer_Y = accelerometer_Y;
        this.accelerometer_Z = accelerometer_Z;
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

    public float getAccelerometerX() {
        return accelerometer_X;
    }

    public void setAccelerometerX(float accelerometer) {
        this.accelerometer_X = accelerometer_X;
    }

    public float getAccelerometerY() {
        return accelerometer_Y;
    }

    public void setAccelerometerY(float accelerometer) {
        this.accelerometer_Y = accelerometer_Y;
    }

    public float getAccelerometerZ() {
        return accelerometer_Z;
    }

    public void setAccelerometerZ(float accelerometer) {
        this.accelerometer_Z = accelerometer_Z;
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
        return "Block - " + block + ", Time - " + time + ", Accelerometer_X - " + accelerometer_X +", Accelerometer_Y - " + accelerometer_Y + ", Accelerometer_Z - " + accelerometer_Z + ", Lat - " + lat + ", Lon - " + lon;
    }
}
