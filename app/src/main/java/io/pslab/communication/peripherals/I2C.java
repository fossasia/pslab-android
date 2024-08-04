package io.pslab.communication.peripherals;

import android.os.SystemClock;
import android.util.Log;

import io.pslab.communication.CommandsProto;
import io.pslab.communication.PacketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by viveksb007 on 28/3/17.
 */

public class I2C {

    private static final String TAG = "I2C";
    private double[] buffer;
    private int frequency = 100000;
    private CommandsProto commandsProto;
    private PacketHandler packetHandler;
    private int totalBytes, channels, samples, timeGap;

    public I2C(PacketHandler packetHandler) {
        this.buffer = new double[10000];
        Arrays.fill(buffer, 0);
        this.packetHandler = packetHandler;
        this.commandsProto = new CommandsProto();
    }

    public void init() throws IOException {
        packetHandler.sendByte(commandsProto.I2C_HEADER);
        packetHandler.sendByte(commandsProto.I2C_INIT);
        packetHandler.getAcknowledgement(); // can check success or failure by ack
    }

    public void enableSMBus() throws IOException {
        packetHandler.sendByte(commandsProto.I2C_HEADER);
        packetHandler.sendByte(commandsProto.I2C_ENABLE_SMBUS);
        packetHandler.getAcknowledgement();
    }

    public void pullSCLLow(int uSec) throws IOException {
        packetHandler.sendByte(commandsProto.I2C_HEADER);
        packetHandler.sendByte(commandsProto.I2C_PULLDOWN_SCL);
        packetHandler.sendInt(uSec);
        packetHandler.getAcknowledgement();
    }

    public void config(int frequency) throws IOException {
        packetHandler.sendByte(commandsProto.I2C_HEADER);
        packetHandler.sendByte(commandsProto.I2C_CONFIG);
        int BRGVAL = (int) ((1 / frequency - 1 / 1e7) * 64e6 - 1);
        if (BRGVAL > 511) {
            BRGVAL = 511;
            Log.v(TAG, "Frequency too low. Setting to : " + String.valueOf(1 / ((BRGVAL + 1.0) / 64e6 + 1.0 / 1e7)));
        }
        packetHandler.sendInt(BRGVAL);
        packetHandler.getAcknowledgement();
    }

    public int start(int address, int rw) throws IOException {
        packetHandler.sendByte(commandsProto.I2C_HEADER);
        packetHandler.sendByte(commandsProto.I2C_START);
        packetHandler.sendByte((address << 1) | rw & 0xff);
        return (packetHandler.getAcknowledgement() >> 4);
    }

    public void stop() throws IOException {
        packetHandler.sendByte(commandsProto.I2C_HEADER);
        packetHandler.sendByte(commandsProto.I2C_STOP);
        packetHandler.getAcknowledgement();
    }

    public void _wait() throws IOException {
        packetHandler.sendByte(commandsProto.I2C_HEADER);
        packetHandler.sendByte(commandsProto.I2C_WAIT);
        packetHandler.getAcknowledgement();
    }

    public int send(int data) throws IOException {
        packetHandler.sendByte(commandsProto.I2C_HEADER);
        packetHandler.sendByte(commandsProto.I2C_SEND);
        packetHandler.sendByte(data);
        return (packetHandler.getAcknowledgement() >> 4);
    }

    public int restart(int address, int rw) throws IOException {
        packetHandler.sendByte(commandsProto.I2C_HEADER);
        packetHandler.sendByte(commandsProto.I2C_RESTART);
        packetHandler.sendByte((address << 1) | rw & 0xff);
        return (packetHandler.getAcknowledgement() >> 4);
    }

    public ArrayList<Byte> simpleRead(int address, int numBytes) throws IOException {
        this.start(address, 1);
        return this.read(numBytes);
    }

    public ArrayList<Byte> read(int length) throws IOException {
        ArrayList<Byte> data = new ArrayList<>();
        for (int i = 0; i < length - 1; i++) {
            packetHandler.sendByte(commandsProto.I2C_HEADER);
            packetHandler.sendByte(commandsProto.I2C_READ_MORE);
            data.add(packetHandler.getByte());
            packetHandler.getAcknowledgement();
        }
        packetHandler.sendByte(commandsProto.I2C_HEADER);
        packetHandler.sendByte(commandsProto.I2C_READ_END);
        data.add(packetHandler.getByte());
        packetHandler.getAcknowledgement();
        return data;
    }

    public byte readRepeat() throws IOException {
        packetHandler.sendByte(commandsProto.I2C_HEADER);
        packetHandler.sendByte(commandsProto.I2C_READ_MORE);
        byte val = packetHandler.getByte();
        packetHandler.getAcknowledgement();
        return val;
    }

    public byte readEnd() throws IOException {
        packetHandler.sendByte(commandsProto.I2C_HEADER);
        packetHandler.sendByte(commandsProto.I2C_READ_END);
        byte val = packetHandler.getByte();
        packetHandler.getAcknowledgement();
        return val;
    }

    public int readStatus() throws IOException {
        packetHandler.sendByte(commandsProto.I2C_HEADER);
        packetHandler.sendByte(commandsProto.I2C_STATUS);
        int val = packetHandler.getInt();
        packetHandler.getAcknowledgement();
        return val;
    }

    public ArrayList<Character> readBulk(int deviceAddress, int registerAddress, int bytesToRead) throws IOException {
        packetHandler.sendByte(commandsProto.I2C_HEADER);
        packetHandler.sendByte(commandsProto.I2C_READ_BULK);
        packetHandler.sendByte(deviceAddress);
        packetHandler.sendByte(registerAddress);
        packetHandler.sendByte(bytesToRead);
        byte[] data = new byte[bytesToRead + 1];
        packetHandler.read(data, bytesToRead + 1);
        ArrayList<Character> charData = new ArrayList<>();
        for (int i = 0; i < bytesToRead; i++) {
            charData.add((char) data[i]);
        }
        return charData;
    }

    public void writeBulk(int deviceAddress, int[] data) throws IOException {
        packetHandler.sendByte(commandsProto.I2C_HEADER);
        packetHandler.sendByte(commandsProto.I2C_WRITE_BULK);
        packetHandler.sendByte(deviceAddress);
        packetHandler.sendByte(data.length);
        for (int aData : data) {
            packetHandler.sendByte(aData);
        }
        packetHandler.getAcknowledgement();
    }

    public ArrayList<Integer> scan(Integer frequency) throws IOException {
        if (frequency == null) frequency = 100000;
        config(frequency);
        ArrayList<Integer> addresses = new ArrayList<>();
        for (int i = 0; i < 128; i++) {
            int x = start(i, 0);
            if ((x & 1) == 0) {
                addresses.add(i);
            }
            stop();
        }
        return addresses;
    }

    public void sendBurst(int data) throws IOException {
        packetHandler.sendByte(commandsProto.I2C_HEADER);
        packetHandler.sendByte(commandsProto.I2C_SEND);
        packetHandler.sendByte(data);
    }

    public ArrayList<Byte> retreiveBuffer() throws IOException {
        int totalIntSamples = totalBytes / 2;
        Log.v(TAG, "Fetching samples : " + totalIntSamples + ", split : " + commandsProto.DATA_SPLITTING);
        ArrayList<Byte> listData = new ArrayList<>();
        for (int i = 0; i < (totalIntSamples / commandsProto.DATA_SPLITTING); i++) {
            packetHandler.sendByte(commandsProto.ADC);
            packetHandler.sendByte(commandsProto.GET_CAPTURE_CHANNEL);
            packetHandler.sendByte(0);
            packetHandler.sendInt(commandsProto.DATA_SPLITTING);
            packetHandler.sendInt(i * commandsProto.DATA_SPLITTING);
            int remaining = commandsProto.DATA_SPLITTING * 2 + 1;
            // reading in single go, change if create communication problem
            byte[] data = new byte[remaining];
            packetHandler.read(data, remaining);
            for (int j = 0; j < data.length - 1; j++)
                listData.add(data[j]);
        }

        if ((totalIntSamples % commandsProto.DATA_SPLITTING) != 0) {
            packetHandler.sendByte(commandsProto.ADC);
            packetHandler.sendByte(commandsProto.GET_CAPTURE_CHANNEL);
            packetHandler.sendByte(0);
            packetHandler.sendInt(totalIntSamples % commandsProto.DATA_SPLITTING);
            packetHandler.sendInt(totalIntSamples - totalIntSamples % commandsProto.DATA_SPLITTING);
            int remaining = 2 * (totalIntSamples % commandsProto.DATA_SPLITTING) + 1;
            byte[] data = new byte[remaining];
            packetHandler.read(data, remaining);
            for (int j = 0; j < data.length - 1; j++)
                listData.add(data[j]);
        }

        Log.v(TAG, "Final Pass : length = " + listData.size());
        return listData;
    }

    public Map<String, ArrayList> dataProcessor(ArrayList<Byte> data, Boolean inInt) {
        if (inInt) {
            for (int i = 0; i < (this.channels * this.samples) / 2; i++)
                this.buffer[i] = (data.get(i * 2) << 8) | (data.get(i * 2 + 1));
        } else {
            for (int i = 0; i < (this.channels * this.samples); i++)
                this.buffer[i] = data.get(i);
        }
        Map<String, ArrayList> retData = new LinkedHashMap<>();
        ArrayList<Double> timeBase = new ArrayList<>();
        double factor = timeGap * (this.samples - 1) / this.samples;
        for (double i = 0; i < timeGap * (this.samples - 1); i += factor) timeBase.add(i);
        retData.put("time", timeBase);
        for (int i = 0; i < this.channels / 2; i++) {
            ArrayList<Double> yValues = new ArrayList<>();
            for (int j = i; j < this.samples * this.channels / 2; j += this.channels / 2) {
                yValues.add(buffer[j]);
            }
            retData.put("CH" + String.valueOf(i + 1), yValues);
        }
        return retData;
    }
}
