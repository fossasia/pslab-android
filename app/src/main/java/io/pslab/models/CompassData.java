package io.pslab.models;

import io.realm.RealmObject;

public class CompassData extends RealmObject {
    private String Bx;
    private String By;
    private String Bz;
    private String Axis = "X-axis";
    private long time;
    private long block;
    private double lat,lon;

    public CompassData() {/**/}

    public CompassData(long time, long block, String Bx, String By, String Bz, String axis, double lat, double lon) {
        this.Bx = Bx;
        this.By = By;
        this.Bz = Bz;
        this.time = time;
        this.block = block;
        this.lat = lat;
        this.lon = lon;
        this.Axis = axis;
    }

    public String getBx() {
        return Bx;
    }

    public void setBx(String Bx) {
        this.Bx = Bx;
    }

    public String getBy() {
        return By;
    }

    public void setBy(String By) {
        this.By = By;
    }

    public String getBz() {
        return Bz;
    }

    public void setBz(String Bz) {
        this.Bz = Bz;
    }

    public void setAxis(String axis) {
        Axis = axis;
    }

    public String getAxis() {
        return Axis;
    }

    public void setBlock(long block) {
        this.block = block;
    }

    public long getBlock() {
        return block;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLat() {
        return lat;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLon() {
        return lon;
    }

    @Override
    public String toString() {
        return "Block - " + block + ", Time - " + time + ", Compass_X - " + Bx + ", Compass_Y - " + By + ", Compass_Z - " + Bz + ", Compass_axis" + Axis + ", Lat - " + lat + ", Lon - " + lon;
    }
}
