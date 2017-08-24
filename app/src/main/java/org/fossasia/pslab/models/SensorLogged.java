package org.fossasia.pslab.models;

import io.realm.RealmObject;

/**
 * Created by viveksb007 on 15/8/17.
 */

public class SensorLogged extends RealmObject {

    private String sensor;

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
}
