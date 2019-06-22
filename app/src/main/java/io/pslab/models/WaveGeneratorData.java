package io.pslab.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class WaveGeneratorData extends RealmObject {

    @PrimaryKey
    private long time;
    private long block;
    private String mode, wave, shape, freq, phase, duty;
    private double lat, lon;

    public WaveGeneratorData() {/**/}

    public WaveGeneratorData(long time, long block, String mode, String wave, String shape, String freq, String phase, String duty, double lat, double lon) {
        this.time = time;
        this.block = block;
        this.mode = mode;
        this.wave = wave;
        this.shape = shape;
        this.freq = freq;
        this.phase = phase;
        this.duty = duty;
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

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getFreq() {
        return freq;
    }

    public void setFreq(String freq) {
        this.freq = freq;
    }

    public String getWave() {
        return wave;
    }

    public void setWave(String wave) {
        this.wave = wave;
    }

    public String getShape() {
        return shape;
    }

    public void setShape(String shape) {
        this.shape = shape;
    }

    public String getDuty() {
        return duty;
    }

    public void setDuty(String duty) {
        this.duty = duty;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
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
        return "Block - " + block + ", Time - " + time + ", Mode - " + mode + ", Wave - " + wave + ", Shape - " + shape + ", Freq - " + freq + ", Phase - " + phase + ", Duty - " + duty + ", Lat - " + lat + ", Lon - " + lon;
    }
}
