package io.pslab.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by viveksb007 on 15/8/17.
 */

public class SensorLogged extends RealmObject {

    private String sensor;
    private long dateTimeStamp;
    @PrimaryKey
    private long uniqueRef;


    public SensorLogged() {
    }

    public SensorLogged(String sensor) {
        this.sensor = sensor;
    }

    public String getSensor() {
        return sensor;
    }

    public void setSensor(String sensor) {
        this.sensor = sensor;
    }

    public long getDateTimeStamp() {
        return dateTimeStamp;
    }

    public void setDateTimeStamp(long dateTimeStamp) {
        this.dateTimeStamp = dateTimeStamp;
    }

    public long getUniqueRef() {
        return uniqueRef;
    }

    public void setUniqueRef(long uniqueRef) {
        this.uniqueRef = uniqueRef;
    }

}
