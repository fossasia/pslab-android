package io.pslab.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ServoData extends RealmObject {

    @PrimaryKey
    private long time;
    private long block;
    private String degree1, degree2, degree3, degree4;
    private double lat, lon;

    public ServoData() {/**/}

    public ServoData(long time, long block, String degree1, String degree2, String degree3, String degree4, double lat, double lon) {
        this.time = time;
        this.block = block;
        this.degree1 = degree1;
        this.degree2 = degree2;
        this.degree3 = degree3;
        this.degree4 = degree4;
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

    public String getDegree1() {
        return degree1;
    }

    public void setDegree1(String degree) {
        this.degree1 = degree;
    }

    public String getDegree2() {
        return degree2;
    }

    public void setDegree2(String degree) {
        this.degree2 = degree;
    }

    public String getDegree3() {
        return degree3;
    }

    public void setDegree3(String degree) {
        this.degree3 = degree;
    }

    public String getDegree4() {
        return degree4;
    }

    public void setDegree4(String degree) {
        this.degree4 = degree;
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
        return "Block - " + block + ", Time - " + time + ", Servo1 - " + degree1 + ", Servo2 - " + degree2 + ", Servo3 - " + degree3 + ", Servo4 - " + degree4 + ", Lat - " + lat + ", Lon - " + lon;
    }
}
