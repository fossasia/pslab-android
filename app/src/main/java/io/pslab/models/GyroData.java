package io.pslab.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class GyroData extends RealmObject {

    @PrimaryKey
    private long time;
    private long block;
    private float gyro_x;
    private float gyro_y;
    private float gyro_z;
    private double lat, lon;

    public GyroData() {/**/}

    public GyroData(long time, long block, float gyro_x, float gyro_y, float gyro_z, double lat, double lon) {
        this.time = time;
        this.block = block;
        this.gyro_x = gyro_x;
        this.gyro_y = gyro_y;
        this.gyro_z = gyro_z;
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

    public float getGyroX() {
        return gyro_x;
    }

    public void setGyroX(float gyro_x) {
        this.gyro_x = gyro_x;
    }

    public float getGyroY() {
        return gyro_y;
    }

    public void setGyroY(float gyro_y) {
        this.gyro_y = gyro_y;
    }

    public float getGyroZ() {
        return gyro_z;
    }

    public void setGyroZ(float gyro_z) {
        this.gyro_z = gyro_z;
    }

    public float[] getGyro(){
        return new float[]{this.gyro_x, this.gyro_y, this.gyro_z};
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
        return "Block - " + block + ", Time - " + time + ", Gyroscope_X - " + gyro_x + ", Gyroscope_Y - " + gyro_y + ", Gyroscope_Y - " + gyro_z + ", Lat - " + lat + ", Lon - " + lon;
    }
}
