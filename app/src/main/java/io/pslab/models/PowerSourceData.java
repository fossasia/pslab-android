package io.pslab.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class PowerSourceData extends RealmObject {

    @PrimaryKey
    private long time;
    private long block;
    private float pv1;
    private float pv2;
    private float pv3;
    private float pcs;
    private double lat;
    private double lon;

    public PowerSourceData() {/**/}

    public PowerSourceData(long time, long block, float pv1, float pv2, float pv3, float pcs, double lat, double lon) {
        this.time = time;
        this.block = block;
        this.pv1 = pv1;
        this.pv2 = pv2;
        this.pv3 = pv3;
        this.pcs = pcs;
        this.lat = lat;
        this.lon = lon;
    }

    public float getPv1() {
        return pv1;
    }

    public void setPv1(float pv1) {
        this.pv1 = pv1;
    }

    public float getPv2() {
        return pv2;
    }

    public void setPv2(float pv2) {
        this.pv2 = pv2;
    }

    public float getPv3() {
        return pv3;
    }

    public void setPv3(float pv3) {
        this.pv3 = pv3;
    }

    public float getPcs() {
        return pcs;
    }

    public void setPcs(float pcs) {
        this.pcs = pcs;
    }

    public long getBlock() {
        return block;
    }

    public void setBlock(long block) {
        this.block = block;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
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
        return "Block - " + block + ", Time - " + time + ", PV1 - " + pv1 + ", PV2 - " + pv2 + ", PV3 - " + pv3 + ", PCS - " + pcs + ", Lat - " + lat + ", Lon - " + lon;
    }
}
