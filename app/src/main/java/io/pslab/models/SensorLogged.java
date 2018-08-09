package io.pslab.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by viveksb007 on 15/8/17.
 */

public class SensorLogged extends RealmObject {

    private String sensor;
    private long dateTimeStart;
    @PrimaryKey
    private long uniqueRef;
    private double latitude;
    private double longitude;
    private long dateTimeEnd;
    private String timeZone;


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

    public long getDateTimeStart() {
        return dateTimeStart;
    }

    public void setDateTimeStart(long dateTimeStart) {
        this.dateTimeStart = dateTimeStart;
    }

    public long getUniqueRef() {
        return uniqueRef;
    }

    public void setUniqueRef(long uniqueRef) {
        this.uniqueRef = uniqueRef;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }


    public long getDateTimeEnd() {
        return dateTimeEnd;
    }

    public void setDateTimeEnd(long dateTimeEnd) {
        this.dateTimeEnd = dateTimeEnd;
    }


    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }
}
