package io.pslab.communication.sensors;


import android.util.Log;

import java.io.IOException;

import io.pslab.communication.peripherals.SPI;


/**
 * Created by akarshan on 4/23/17.
 * <p>
 * ScienceLab instance of SPI and DDS_CLOCK are required to be passed to the AD9833 constructor.
 * mapReferenceClock(new ArrayList<>(Collections.singletonList("WAVEGEN")), 4) is needed to be called prior to creation of AD9833 object.
 * </p>
 */

public class AD9833 {
    //control bytes
    private static final String TAG = "AD9833";
    private final int DDS_MAX_FREQ = 0xFFFFFFF - 1;
    private final int DDS_B28 = 13;
    private final int DDS_HLB = 12;
    private final int DDS_FSELECT = 11;
    private final int DDS_PSELECT = 10;
    private final int DDS_RESET = 8;
    private final int DDS_SLEEP1 = 7;
    private final int DDS_SLEEP12 = 6;
    private final int DDS_OPBITEN = 5;
    private final int DDS_DIV2 = 3;
    private final int DDS_MODE = 1;

    private final int DDS_FSYNC = 9;

    private final int[] DDS_SINE = {0};
    private final int DDS_TRIANGLE = (1 << DDS_MODE);
    private final int DDS_SQUARE = (1 << DDS_OPBITEN);
    private final int DDS_RESERVED = (1 << DDS_OPBITEN) | (1 << DDS_MODE);
    private final int DDS_CLOCK;
    private final int clockScaler = 4;       // 8MHz
    private int waveformMode;
    private int activeChannel;
    private int frequency;
    private final int cs;
    private final SPI spi;

    public AD9833(SPI spi, int DDS_CLOCK) throws IOException {
        cs = 9;
        this.spi = spi;
        this.spi.setParameters(2, 2, 1, 1, 0);
        this.DDS_CLOCK = DDS_CLOCK;
        waveformMode = DDS_TRIANGLE;

        Log.v(TAG, "clock set to: " + DDS_CLOCK);
        write(1 << DDS_RESET);
        write((1 << DDS_B28) | waveformMode);               //finished loading data
        activeChannel = 0;
        frequency = 1000;
    }

    private void write(int con) throws IOException {
        spi.start(cs);
        spi.send16(con);
        spi.stop(cs);
    }

    private void setFrequency(int frequency, int register, int phase) throws IOException {
        int regSel;
        activeChannel = register;
        this.frequency = frequency;

        int frequencySetting = (Math.round(frequency * DDS_MAX_FREQ / DDS_CLOCK));
        int modeBits = (1 << DDS_B28) | waveformMode;
        if (register > 0) {
            modeBits |= (1 << DDS_FSELECT);
            regSel = 0x8000;
        } else
            regSel = 0x4000;

        write((1 << DDS_RESET) | modeBits);                                     //Ready to load DATA
        write((regSel | (frequencySetting & 0x3FFF)) & 0xFFFF);                 //LSB
        write((regSel | ((frequencySetting >> 14) & 0x3FFF)) & 0xFFFF);         //MSB
        write(0xc000 | phase);                                                   //Phase
        write(modeBits);                                                         //finished loading data
    }

    public void setVoltage(int voltage) throws IOException {
        waveformMode = DDS_TRIANGLE;
        setFrequency(0, 0, voltage);                                             //0xfff*v/.6
    }

    public void selectFrequencyRegister(int register) throws IOException {
        activeChannel = register;
        int modeBits = waveformMode;
        if (register != 0)
            modeBits |= (1 << DDS_FSELECT);
        write(modeBits);
    }

    public void setWaveformMode(int mode) throws IOException {
        waveformMode = mode;
        int modeBits = mode;
        if (activeChannel != 0)
            modeBits |= (1 << DDS_FSELECT);
        write(modeBits);
    }
}
