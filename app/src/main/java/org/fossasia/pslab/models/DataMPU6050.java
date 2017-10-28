package org.fossasia.pslab.models;

import io.realm.RealmObject;

/**
 * Created by viveksb007 on 4/8/17.
 */

public class DataMPU6050 extends RealmObject {

    private long trial;
    private long id;
    private double ax, ay, az;
    private double gx, gy, gz;
    private double temperature;

    public DataMPU6050() {

    }

    public DataMPU6050(double ax, double ay, double az, double gx, double gy, double gz, double temperature) {
        this.ax = ax;
        this.ay = ay;
        this.az = az;
        this.gx = gx;
        this.gy = gy;
        this.gz = gz;
        this.temperature = temperature;
    }

    public double getAx() {
        return ax;
    }

    public void setAx(double ax) {
        this.ax = ax;
    }

    public double getAy() {
        return ay;
    }

    public void setAy(double ay) {
        this.ay = ay;
    }

    public double getAz() {
        return az;
    }

    public void setAz(double az) {
        this.az = az;
    }

    public double getGx() {
        return gx;
    }

    public void setGx(double gx) {
        this.gx = gx;
    }

    public double getGy() {
        return gy;
    }

    public void setGy(double gy) {
        this.gy = gy;
    }

    public double getGz() {
        return gz;
    }

    public void setGz(double gz) {
        this.gz = gz;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public long getTrial() {
        return trial;
    }

    public void setTrial(long trial) {
        this.trial = trial;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
