package io.pslab.models;


import io.realm.RealmObject;


/**
 * Created by Avjeet on 31-07-2018.
 */
public class LuxData extends RealmObject {
    private long foreignKey;
    private long id;
    private long time;
    private float lux;
    private long timeElapsed;

    public LuxData() {
    }

    public LuxData(float lux, long time, long timeElapsed) {
        this.lux = lux;
        this.time = time;
        this.timeElapsed = timeElapsed;
    }
    public long getForeignKey() {
        return foreignKey;
    }

    public void setForeignKey(long foreignKey) {
        this.foreignKey = foreignKey;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    public long getTime() {
        return time;
    }

    public float getLux() {
        return lux;
    }

    public long getTimeElapsed() {
        return timeElapsed;
    }
}
