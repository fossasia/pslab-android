package io.pslab.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * @author reckoner1429
 */
public class SoundData extends RealmObject {

    @PrimaryKey
    private long time;
    private long block;
    private float dB;
    private float avgLoudness;
    private float maxLoudness;
    private float minLoudness;
    private double lat;
    private double lon;

    public SoundData() {
        /* no arg constructor */
    }

    public SoundData(long time, long block, float dB, float avgLoudness, float maxLoudness,
                     float minLoudness, double lat, double lon) {
        this.time = time;
        this.block = block;
        this.dB = dB;
        this.avgLoudness = avgLoudness;
        this.minLoudness = minLoudness;
        this.maxLoudness = maxLoudness;
        this.lat = lat;
        this.lon = lon;
    }
    public long getTime() {
        return time;
    }

    public long getBlock() {
        return block;
    }

    public float getdB() {
        return dB;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public float getAvgLoudness() {
        return avgLoudness;
    }

    public float getMaxLoudness() {
        return maxLoudness;
    }

    public float getMinLoudness() {
        return minLoudness;
    }
}
