package org.fossasia.pslab.communication;

import android.hardware.usb.UsbManager;
import android.util.Log;

import org.fossasia.pslab.communication.analogChannel.AnalogAquisitionChannel;
import org.fossasia.pslab.communication.analogChannel.AnalogConstants;
import org.fossasia.pslab.communication.analogChannel.AnalogInputSource;
import org.fossasia.pslab.communication.digitalChannel.DigitalChannel;
import org.fossasia.pslab.communication.peripherals.I2C;
import org.fossasia.pslab.communication.peripherals.MCP4728;
import org.fossasia.pslab.communication.peripherals.NRF24L01;
import org.fossasia.pslab.communication.peripherals.RadioLink;
import org.fossasia.pslab.communication.peripherals.SPI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by viveksb007 on 28/3/17.
 */

public class ScienceLab {

    private static final String TAG = "ScienceLab";

    public int CAP_AND_PCS = 0;
    public int ADC_SHIFTS_LOCATION1 = 1;
    public int ADC_SHIFTS_LOCATION2 = 2;
    public int ADC_POLYNOMIALS_LOCATION = 3;

    public int DAC_SHIFTS_PV1A = 4;
    public int DAC_SHIFTS_PV1B = 5;
    public int DAC_SHIFTS_PV2A = 6;
    public int DAC_SHIFTS_PV2B = 7;
    public int DAC_SHIFTS_PV3A = 8;
    public int DAC_SHIFTS_PV3B = 9;

    public int BAUD = 1000000;
    public int DDS_CLOCK, MAX_SAMPLES, samples, triggerLevel, triggerChannel, errorCount,
            channelsInBuffer, digitalChannelsInBuffer, dataSplitting, sin1Frequency, sin2Frequency;
    double[] currents, currentScalars, gainValues;
    double SOCKET_CAPACITANCE, resistanceScaling, timebase;
    String[] allAnalogChannels, allDigitalChannels;
    Map<String, AnalogInputSource> analogInputSources;
    Map<String, Integer> squareWaveFrequency, gains;
    Map<String, String> waveType;
    boolean streaming, calibrated = false;
    List<AnalogAquisitionChannel> aChannels;
    List<DigitalChannel> dChannels;

    private CommunicationHandler mCommunicationHandler;
    private PacketHandler mPacketHandler;
    private CommandsProto mCommandsProto;
    private AnalogConstants mAnalogConstants;
    private I2C i2C;
    private SPI spi;
    private NRF24L01 nrf;
    private MCP4728 dac;

    public ScienceLab(UsbManager usbManager) {
        mCommandsProto = new CommandsProto();
        mAnalogConstants = new AnalogConstants();
        mCommunicationHandler = new CommunicationHandler(usbManager);
        if (isDeviceFound()) {
            try {
                mCommunicationHandler.open();
                mPacketHandler = new PacketHandler(500, mCommunicationHandler);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void initializeVariables() {
        DDS_CLOCK = 0;
        timebase = 40;
        MAX_SAMPLES = mCommandsProto.MAX_SAMPLES;
        samples = MAX_SAMPLES;
        triggerChannel = 0;
        triggerLevel = 550;
        errorCount = 0;
        channelsInBuffer = 0;
        digitalChannelsInBuffer = 0;
        currents = new double[]{0.55e-3, 0.55e-6, 0.55e-5, 0.55e-4};
        currentScalars = new double[]{1.0, 1.0, 1.0, 1.0};
        dataSplitting = mCommandsProto.DATA_SPLITTING;
        allAnalogChannels = mAnalogConstants.allAnalogChannels;
        for (String aChannel : allAnalogChannels) {
            analogInputSources.put(aChannel, new AnalogInputSource(aChannel));
        }
        sin1Frequency = 0;
        sin2Frequency = 0;
        squareWaveFrequency.put("SQR1", 0);
        squareWaveFrequency.put("SQR2", 0);
        squareWaveFrequency.put("SQR3", 0);
        squareWaveFrequency.put("SQR4", 0);
    }

    private void runInitSequence() throws IOException {
        ArrayList<String> aboutArray = new ArrayList<>();
        if (!isConnected()) {
            Log.e(TAG, "Check hardware connections. Not connected");
        }
        streaming = false;
        for (String aChannel : mAnalogConstants.biPolars) {
            aChannels.add(new AnalogAquisitionChannel(aChannel));
        }
        gainValues = mAnalogConstants.gains;
        SOCKET_CAPACITANCE = 42e-12;
        resistanceScaling = 1;
        allDigitalChannels = DigitalChannel.digitalChannelNames;
        gains.put("CH1", 0);
        gains.put("CH2", 0);
        for (int i = 0; i < 4; i++) {
            dChannels.add(new DigitalChannel(i));
        }
        i2C = new I2C(mPacketHandler);
        spi = new SPI(mPacketHandler);
        if (isConnected()) {
            for (String temp : new String[]{"CH1", "CH2"}) {
                this.setGain(temp, 0, true);
            }
            for (String temp : new String[]{"W1", "W2"}) {
                loadEquation(temp, "sine");
            }
            spi.setParameters(1, 7, 1, 0, -1);
        }
        nrf = new NRF24L01(mPacketHandler);
        if (nrf.ready) {
            aboutArray.add("Radio Transceiver is : Installed");
        } else {
            aboutArray.add("Radio Transceiver is : Not Installed");
        }
        dac = new MCP4728(mPacketHandler);
        this.calibrated = false;
        // todo : port remaining function

    }

    private void getAverageVoltage(String channelName, int sample) {
        if (sample == -1) sample = 1;
        Map<Integer, Double> poly;
        // Look for polynomial implementation, this might be flawed
        poly = analogInputSources.get(channelName).calPoly12;
        ArrayList<Double> vals = new ArrayList<>();
        for (int i = 0; i < sample; i++) {
            vals.add(getRawAverageVoltage(channelName));
        }
        // todo : return average of vals after some polynomial manipulation

    }

    private double getRawAverageVoltage(String channelName) {
        try {
            int chosa = this.calcCHOSA(channelName);
            mPacketHandler.sendByte(mCommandsProto.ADC);
            mPacketHandler.sendByte(mCommandsProto.GET_VOLTAGE_SUMMED);
            mPacketHandler.sendByte(chosa);
            int vSum = mPacketHandler.getInt();
            mPacketHandler.getAcknowledgement();
            return vSum / 16.;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Error in getRawAverageVoltage");
        }
        return 0;
    }

    private int calcCHOSA(String channelName) {
        channelName = channelName.toUpperCase();
        AnalogInputSource source = analogInputSources.get(channelName);
        boolean found = false;
        for (String temp : allAnalogChannels) {
            if (temp.equals(channelName)) {
                found = true;
                break;
            }
        }
        if (!found) {
            Log.e(TAG, "Not a valid channel name. selecting CH1");
            return calcCHOSA("CH1");
        }

        return source.CHOSA;
    }

    public void setDataSplitting(int dataSplitting) {
        this.dataSplitting = dataSplitting;
    }

    public boolean isDeviceFound() {
        return mCommunicationHandler.isDeviceFound();
    }

    public boolean isConnected() {
        return mCommunicationHandler.isConnected();
    }

    public String getVersion() throws IOException {
        if (isConnected()) {
            return mPacketHandler.getVersion();
        } else {
            return "Not Connected";
        }
    }

    public double getResistence() {

        return 0;
    }

    public void ignoreCalibration() {
    }

    public double setGain(String channel, int gain, boolean force) {
        if (gain < 0 || gain > 8) {
            Log.v(TAG, "Invalid gain parameter. 0-7 only.");
            return 0;
        }
        if (this.analogInputSources.get(channel).gainPGA == -1) {
            Log.v(TAG, "No amplifier exists on this channel : " + channel);
            return 0;
        }
        boolean refresh = false;
        if (this.gains.get(channel) != gain) {
            this.gains.put(channel, gain);
            refresh = true;

        }
        if (refresh || force) {
            analogInputSources.get(channel).setGain(gain); // giving index of gainValues
            if (gain > 7) gain = 0;
            try {
                mPacketHandler.sendByte(mCommandsProto.ADC);
                mPacketHandler.sendByte(mCommandsProto.SET_PGA_GAIN);
                mPacketHandler.sendByte(analogInputSources.get(channel).gainPGA);
                mPacketHandler.sendByte(gain);
                mPacketHandler.getAcknowledgement();
                return this.gainValues[gain];

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    public void loadEquation(String channel, String function) {
        double[] span = new double[2];
        if (function.equals("sine")) {
            span[0] = 0;
            span[1] = 2 * Math.PI;
            waveType.put(channel, "sine");
        } else if (function.equals("tria")) {
            span[0] = -1;
            span[1] = 3;
            waveType.put(channel, "tria");
        } else {
            waveType.put(channel, "arbit");
        }
        double factor = (span[1] - span[0]) / 512;
        ArrayList<Double> x = new ArrayList<>();
        ArrayList<Double> y = new ArrayList<>();
        // for now using switch, proper way is to create an interface and pass it to loadEquation and call interface methods for calculation
        for (int i = 0; i < 512; i++) {
            x.add(span[0] + i * factor);
            switch (function) {
                case "sine":
                    y.add(Math.sin(x.get(i)));
                    break;
                case "tria":
                    y.add(Math.abs(x.get(i) % 4 - 2) - 1);
                    break;
            }
        }
        loadTable(channel, y, waveType.get(channel), -1);
    }

    private void loadTable(String channel, ArrayList<Double> y, String mode, double amp) {
        waveType.put(channel, mode);
        ArrayList<String> channels = new ArrayList<>();
        ArrayList<Double> points = y;
        channels.add("W1");
        channels.add("W2");
        int num;
        if (channels.contains(channel)) {
            num = channels.indexOf(channel) + 1;
        } else {
            Log.e(TAG, "Channel does not exist. Try W1 or W2");
            return;
        }

        if (amp == -1) amp = 0.95;
        double LARGE_MAX = 511 * amp, SMALL_MAX = 63 * amp;
        double min = Collections.min(y);
        for (int i = 0; i < y.size(); i++) {
            y.set(i, y.get(i) - min);
        }
        double max = Collections.max(y);
        ArrayList<Integer> yMod1 = new ArrayList<>();
        for (int i = 0; i < y.size(); i++) {
            double temp = 1 - (y.get(i) / max);
            yMod1.add((int) Math.round(LARGE_MAX - LARGE_MAX * temp));
        }
        y.clear();


        for (int i = 0; i < points.size(); i += 16) {
            y.add(points.get(i));
        }
        min = Collections.min(y);
        for (int i = 0; i < y.size(); i++) {
            y.set(i, y.get(i) - min);
        }
        max = Collections.max(y);
        ArrayList<Integer> yMod2 = new ArrayList<>();
        for (int i = 0; i < y.size(); i++) {
            double temp = 1 - (y.get(i) / max);
            yMod2.add((int) Math.round(SMALL_MAX - SMALL_MAX * temp));
        }

        try {
            mPacketHandler.sendByte(mCommandsProto.WAVEGEN);
            switch (num) {
                case 1:
                    mPacketHandler.sendByte(mCommandsProto.LOAD_WAVEFORM1);
                    break;
                case 2:
                    mPacketHandler.sendByte(mCommandsProto.LOAD_WAVEFORM2);
                    break;
            }
            for (int a : yMod1)
                mPacketHandler.sendInt(a);

            for (int a : yMod2)
                mPacketHandler.sendByte(a);

            // sleep for 0.01
            mPacketHandler.getAcknowledgement();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<Integer, ArrayList<Integer>> getRadioLinks() {
        try {
            return this.nrf.getNodeList();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public RadioLink newRadioLink() {
        return new RadioLink(this.nrf, -1);
    }


    public void setState(Map<String, Integer> args) {
        int data = 0;
        if (args.containsKey("SQR1")) {
            data |= (0x10 | args.get("SQR1"));
        }
        if (args.containsKey("SQR2")) {
            data |= (0x20 | (args.get("SQR2") << 1));
        }
        if (args.containsKey("SQR3")) {
            data |= (0x40 | (args.get("SQR3") << 2));
        }
        if (args.containsKey("SQR4")) {
            data |= (0x80 | (args.get("SQR4") << 3));
        }
        try {
            mPacketHandler.sendByte(mCommandsProto.DOUT);
            mPacketHandler.sendByte(mCommandsProto.SET_STATE);
            mPacketHandler.sendByte(data);
            mPacketHandler.getAcknowledgement();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public int setSqr1(int frequency, double dutyCycle, boolean onlyPrepare) {
        if (dutyCycle == -1) dutyCycle = 50;
        if (frequency == 0 || dutyCycle == 0) return -1;
        if (frequency > 10e6) {
            Log.v(TAG, "Frequency is greater than 10MHz. Please use map_reference_clock for 16 & 32MHz outputs");
            return 0;
        }
        int[] p = new int[]{1, 8, 64, 256};
        int prescalar = 0, wavelength = 0;
        while (prescalar <= 3) {
            wavelength = (int) (64e6 / frequency / p[prescalar]);
            if (wavelength < 65525) break;
            prescalar++;
        }
        if (prescalar == 4 || wavelength == 0) {
            Log.v(TAG, "Out of Range");
            return -1;
        }
        int highTime = (int) (wavelength * dutyCycle / 100);
        if (onlyPrepare) {
            Map<String, Integer> args = new LinkedHashMap<>();
            args.put("SQR1", 0);
            this.setState(args);
        }
        try {
            mPacketHandler.sendByte(mCommandsProto.WAVEGEN);
            mPacketHandler.sendByte(mCommandsProto.SET_SQR1);
            mPacketHandler.sendInt(Math.round(wavelength));
            mPacketHandler.sendInt(Math.round(highTime));
            if (onlyPrepare) prescalar |= 0x4;
            mPacketHandler.sendByte(prescalar);
            mPacketHandler.getAcknowledgement();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.squareWaveFrequency.put("SQR1", (int) (64e6 / wavelength / p[prescalar & 0x3]));
        return this.squareWaveFrequency.get("SQR1");
    }

    public int setSqr2(int frequency, double dutyCycle) {
        int[] p = new int[]{1, 8, 64, 256};
        int prescalar = 0, wavelength = 0;
        while (prescalar <= 3) {
            wavelength = (int) (64e6 / frequency / p[prescalar]);
            if (wavelength < 65525) break;
            prescalar++;
        }
        if (prescalar == 4 || wavelength == 0) {
            Log.v(TAG, "Out of Range");
            return -1;
        }
        int highTime = (int) (wavelength * dutyCycle / 100);
        try {
            mPacketHandler.sendByte(mCommandsProto.WAVEGEN);
            mPacketHandler.sendByte(mCommandsProto.SET_SQR2);
            mPacketHandler.sendInt(Math.round(wavelength));
            mPacketHandler.sendInt(Math.round(highTime));
            mPacketHandler.sendByte(prescalar);
            mPacketHandler.getAcknowledgement();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.squareWaveFrequency.put("SQR2", (int) (64e6 / wavelength / p[prescalar & 0x3]));
        return this.squareWaveFrequency.get("SQR2");
    }

    public void stepperMotor(int steps, int delay, int direction) {
        try {
            mPacketHandler.sendByte(mCommandsProto.NONSTANDARD_IO);
            mPacketHandler.sendByte(mCommandsProto.STEPPER_MOTOR);
            mPacketHandler.sendInt((steps << 1) | direction);
            mPacketHandler.sendInt(delay);
            // todo : sleep for (steps * delay * 1e-3)
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setSqrs(int wavelength, int phase, int highTime1, int highTime2, int prescalar) {
        if (prescalar == -1) prescalar = 1;
        try {
            mPacketHandler.sendByte(mCommandsProto.WAVEGEN);
            mPacketHandler.sendByte(mCommandsProto.SET_SQRS);
            mPacketHandler.sendInt(wavelength);
            mPacketHandler.sendInt(phase);
            mPacketHandler.sendInt(highTime1);
            mPacketHandler.sendInt(highTime2);
            mPacketHandler.sendByte(prescalar);
            mPacketHandler.getAcknowledgement();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int sqrPWM(int frequency, double h0, double p1, double h1, double p2, double h2, double p3, double h3, boolean pulse) {
        if (frequency == 0 || h0 == 0 || h1 == 0 || h2 == 0 || h3 == 0) return -1;
        if (frequency > 10e6) {
            Log.v(TAG, "Frequency is greater than 10MHz. Please use map_reference_clock for 16 & 32MHz outputs");
            return -1;
        }
        int[] p = new int[]{1, 8, 64, 256};
        int prescalar = 0, wavelength = 0;
        while (prescalar <= 3) {
            wavelength = (int) (64e6 / frequency / p[prescalar]);
            if (wavelength < 65525) break;
            prescalar++;
        }
        if (prescalar == 4 || wavelength == 0) {
            Log.v(TAG, "Out of Range");
            return -1;
        }
        if (!pulse) prescalar |= (1 << 5);
        int a1 = (int) (p1 % 1 * wavelength), b1 = (int) ((h1 + p1) % 1 * wavelength);
        int a2 = (int) (p2 % 1 * wavelength), b2 = (int) ((h2 + p2) % 1 * wavelength);
        int a3 = (int) (p3 % 1 * wavelength), b3 = (int) ((h3 + p3) % 1 * wavelength);
        try {
            mPacketHandler.sendByte(mCommandsProto.WAVEGEN);
            mPacketHandler.sendByte(mCommandsProto.SQR4);
            mPacketHandler.sendInt(wavelength - 1);
            mPacketHandler.sendInt((int) (wavelength * h0) - 1);
            mPacketHandler.sendInt(Math.max(0, a1 - 1));
            mPacketHandler.sendInt(Math.max(1, b1 - 1));
            mPacketHandler.sendInt(Math.max(0, a2 - 1));
            mPacketHandler.sendInt(Math.max(1, b2 - 1));
            mPacketHandler.sendInt(Math.max(0, a3 - 1));
            mPacketHandler.sendInt(Math.max(1, b3 - 1));
            mPacketHandler.sendByte(prescalar);
            mPacketHandler.getAcknowledgement();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (String channel : new String[]{"SQR1", "SQR2", "SQR3", "SQR4"}) {
            this.squareWaveFrequency.put(channel, (int) (64e6 / wavelength / p[prescalar & 0x3]));
        }
        return (int) (64e6 / wavelength / p[prescalar & 0x3]);
    }

    public void mapReferenceClock(ArrayList<String> args, int scalar) {
        try {
            mPacketHandler.sendByte(mCommandsProto.WAVEGEN);
            mPacketHandler.sendByte(mCommandsProto.MAP_REFERENCE);
            int channel = 0;
            if (args.contains("SQR1")) channel |= 1;
            if (args.contains("SQR2")) channel |= 2;
            if (args.contains("SQR3")) channel |= 4;
            if (args.contains("SQR4")) channel |= 8;
            if (args.contains("WAVEGEN")) channel |= 16;
            mPacketHandler.sendByte(channel);
            mPacketHandler.sendByte(scalar);
            if (args.contains("WAVEGEN")) {
                this.DDS_CLOCK = (int) 128e6 / (1 << scalar);
            }
            mPacketHandler.getAcknowledgement();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setPV1(int value) {
        this.dac.setVoltage("PV1", value);
    }

    public void setPV2(int value) {
        this.dac.setVoltage("PV2", value);
    }

    public void setPV3(int value) {
        this.dac.setVoltage("PV3", value);
    }

    public void setPCS(int value) {
        this.dac.setCurrent(value);
    }

    public int getPV1() {
        return this.dac.getVoltage("PV1");
    }

    public int getPV2() {
        return this.dac.getVoltage("PV2");
    }

    public int getPV3() {
        return this.dac.getVoltage("PV3");
    }

    public int getPCS() {
        return this.dac.getVoltage("PCS");
    }

    public void WS2812B(int[][] colors, String output) {
        if (output == null) output = "CS1";
        int pin;
        switch (output) {
            case "CS1":
                pin = mCommandsProto.SET_RGB1;
                break;
            case "CS2":
                pin = mCommandsProto.SET_RGB2;
                break;
            case "SQR1":
                pin = mCommandsProto.SET_RGB3;
                break;
            default:
                Log.e(TAG, "Invalid Output");
                return;
        }
        try {
            mPacketHandler.sendByte(mCommandsProto.COMMON);
            mPacketHandler.sendByte(pin);
            mPacketHandler.sendByte(colors.length * 3);
            // todo : cross check access of 2D colors array
            for (int[] col : colors) {
                mPacketHandler.sendByte(col[1]); // Green
                mPacketHandler.sendByte(col[0]); // Red
                mPacketHandler.sendByte(col[2]); // Blue
            }
            mPacketHandler.getAcknowledgement();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public long deviceID() {
        int a = readProgramAddress(0x800FF8);
        int b = readProgramAddress(0x800FFa);
        int c = readProgramAddress(0x800FFc);
        int d = readProgramAddress(0x800FFe);
        long value = d | (c << 16) | (b << 32) | (a << 48);
        Log.v(TAG, "device ID : " + value);
        return value;
    }

    public int readProgramAddress(int address) {
        try {
            mPacketHandler.sendByte(mCommandsProto.COMMON);
            mPacketHandler.sendByte(mCommandsProto.READ_PROGRAM_ADDRESS);
            mPacketHandler.sendInt(address & 0xffff);
            mPacketHandler.sendInt((address >> 16) & 0xffff);
            int value = mPacketHandler.getInt();
            mPacketHandler.getAcknowledgement();
            return value;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void writeProgramAddress(int address, int value) {
        try {
            mPacketHandler.sendByte(mCommandsProto.COMMON);
            mPacketHandler.sendByte(mCommandsProto.WRITE_PROGRAM_ADDRESS);
            mPacketHandler.sendInt(address & 0xffff);
            mPacketHandler.sendInt((address >> 16) & 0xffff);
            mPacketHandler.sendInt(value);
            mPacketHandler.getAcknowledgement();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int readDataAddress(int address) {
        try {
            mPacketHandler.sendByte(mCommandsProto.COMMON);
            mPacketHandler.sendByte(mCommandsProto.READ_DATA_ADDRESS);
            mPacketHandler.sendInt(address & 0xffff);
            int value = mPacketHandler.getInt();
            mPacketHandler.getAcknowledgement();
            return value;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void writeDataAddress(int address, int value) {
        try {
            mPacketHandler.sendByte(mCommandsProto.COMMON);
            mPacketHandler.sendByte(mCommandsProto.WRITE_DATA_ADDRESS);
            mPacketHandler.sendInt(address & 0xffff);
            mPacketHandler.sendInt(value);
            mPacketHandler.getAcknowledgement();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stepForward(int steps, int delay) {
        this.stepperMotor(steps, delay, 1);
    }

    public void stepBackward(int steps, int delay) {
        this.stepperMotor(steps, delay, 0);
    }

    public void servo4(double angle1, double angle2, double angle3, double angle4) {
        int params = (1 << 5) | 2;
        try {
            mPacketHandler.sendByte(mCommandsProto.WAVEGEN);
            mPacketHandler.sendByte(mCommandsProto.SQR4);
            mPacketHandler.sendInt(10000);
            mPacketHandler.sendInt(750 + (int) (angle1 * 1900 / 180));
            mPacketHandler.sendInt(0);
            mPacketHandler.sendInt(750 + (int) (angle2 * 1900 / 180));
            mPacketHandler.sendInt(0);
            mPacketHandler.sendInt(750 + (int) (angle3 * 1900 / 180));
            mPacketHandler.sendInt(0);
            mPacketHandler.sendInt(750 + (int) (angle4 * 1900 / 180));
            mPacketHandler.sendByte(params);
            mPacketHandler.getAcknowledgement();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void enableUARTPassThrough(int baudrate, boolean persist) {
        try {
            mPacketHandler.sendByte(mCommandsProto.PASSTHROUGHS);
            mPacketHandler.sendByte(mCommandsProto.PASS_UART);
            if (persist)
                mPacketHandler.sendByte(1);
            else
                mPacketHandler.sendByte(0);
            mPacketHandler.sendInt((int) Math.round(((64e6 / baudrate) / 4) - 1));
            Log.v(TAG, "BRG2VAL: " + Math.round(((64e6 / baudrate) / 4) - 1));
            // sleep for 0.1 sec
            byte[] junk = new byte[100];
            mPacketHandler.read(junk, 100);
            // Log junk to see :D
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public double estimateDistance() {
        try {
            mPacketHandler.sendByte(mCommandsProto.NONSTANDARD_IO);
            mPacketHandler.sendByte(mCommandsProto.HCSR04_HEADER);
            int timeoutMSB = (int) ((0.3 * 64e6)) >> 16;
            mPacketHandler.sendInt(timeoutMSB);
            long A = mPacketHandler.getLong();
            long B = mPacketHandler.getLong();
            int timeout = mPacketHandler.getInt(); // tmt??
            mPacketHandler.getAcknowledgement();
            if (timeout >= timeoutMSB || B == 0) return 0;
            return (330 * (B - A + 20) / 64e6) / 2;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void opticalArray(double ss, int delay, String channel, int resolution, int tweak) { // ss- stands for?
        int samples = 3694;
        if (resolution == -1) resolution = 10;
        if (tweak == -1) tweak = 1;

        try {
            mPacketHandler.sendByte(mCommandsProto.NONSTANDARD_IO);
            mPacketHandler.sendByte(mCommandsProto.TCD1304_HEADER);
            if (resolution == 10)
                mPacketHandler.sendByte(calcCHOSA(channel));
            else
                mPacketHandler.sendByte(calcCHOSA(channel) | 0x80);
            mPacketHandler.sendByte(tweak);
            mPacketHandler.sendInt(delay);
            mPacketHandler.sendInt((int) ss * 64);
            this.timebase = ss;
            this.aChannels.get(0).setParams("CH1", samples, this.timebase, resolution, this.analogInputSources.get(channel), -1);
            this.samples = samples;
            this.channelsInBuffer = 1;
            // sleep for (2 * delay * 1e-6)
            mPacketHandler.getAcknowledgement();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setUARTBaud(int BAUD) {
        try {
            mPacketHandler.sendByte(mCommandsProto.UART_2);
            mPacketHandler.sendByte(mCommandsProto.SET_BAUD);
            mPacketHandler.sendInt((int) Math.round(((64e6 / BAUD) / 4) - 1));
            Log.v(TAG, "BRG2VAL: " + Math.round(((64e6 / BAUD) / 4) - 1));
            mPacketHandler.getAcknowledgement();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeUART(char character) {
        try {
            mPacketHandler.sendByte(mCommandsProto.UART_2);
            mPacketHandler.sendByte(mCommandsProto.SEND_BYTE);
            mPacketHandler.sendByte(character);
            mPacketHandler.getAcknowledgement();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte readUART() {
        try {
            mPacketHandler.sendByte(mCommandsProto.UART_2);
            mPacketHandler.sendByte(mCommandsProto.READ_BYTE);
            return mPacketHandler.getByte();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int readUARTStatus() {
        try {
            mPacketHandler.sendByte(mCommandsProto.UART_2);
            mPacketHandler.sendByte(mCommandsProto.READ_UART2_STATUS);
            return mPacketHandler.getByte();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void readLog() {
        try {
            mPacketHandler.sendByte(mCommandsProto.COMMON);
            mPacketHandler.sendByte(mCommandsProto.READ_LOG);
            // String log =  mPacketHandler.readline()   -- implement readline in PacketHandler
            mPacketHandler.getAcknowledgement();
            // return log;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}