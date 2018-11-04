package io.pslab.models;

import io.realm.RealmObject;

/**
 * Created by Padmal on 11/5/18.
 */

public class SensorDataBlock extends RealmObject {

    private long block;
    private String sensorType;

    public SensorDataBlock() {/**/}

    public SensorDataBlock(long block, String sensorType) {
        this.block = block;
        this.sensorType = sensorType;
    }

    public long getBlock() {
        return block;
    }

    public void setBlock(long block) {
        this.block = block;
    }

    public String getSensorType() {
        return sensorType;
    }

    public void setSensorType(String sensorType) {
        this.sensorType = sensorType;
    }
}
