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
    private float accelerometerX;
    private float accelerometerY;
    private float accelerometerZ;
    private double lat, lon;

    public AccelerometerData() {/**/}

    public AccelerometerData(long time, long block, float accelerometerX, float accelerometerY, float accelerometerZ, double lat, double lon) {
        this.time = time;
        this.block = block;
        this.accelerometerX = accelerometerX;
        this.accelerometerY = accelerometerY;
        this.accelerometerZ = accelerometerZ;
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
        return accelerometerX;
    }

    public void setAccelerometerX(float accelerometerX) {
        this.accelerometerX = accelerometerX;
    }

    public float getAccelerometerY() {
        return accelerometerY;
    }

    public void setAccelerometerY(float accelerometerY) {
        this.accelerometerY = accelerometerY;
    }

    public float getAccelerometerZ() {
        return accelerometerZ;
    }

    public void setAccelerometerZ(float accelerometerZ) {
        this.accelerometerZ = accelerometerZ;
    }

    public float[] getAccelerometer() {
        return new float[]{this.accelerometerX, this.accelerometerY, this.accelerometerZ};
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
        return "Block - " + block + ", Time - " + time + ", Accelerometer_X - " + accelerometerX + ", Accelerometer_Y - " + accelerometerY + ", Accelerometer_Z - " + accelerometerZ + ", Lat - " + lat + ", Lon - " + lon;
    }
}
