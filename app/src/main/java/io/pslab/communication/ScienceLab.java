package io.pslab.communication;

import static org.apache.commons.lang3.math.NumberUtils.max;
import static io.pslab.others.MathUtils.linSpace;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import io.pslab.activity.MainActivity;
import io.pslab.communication.analogChannel.AnalogAquisitionChannel;
import io.pslab.communication.analogChannel.AnalogConstants;
import io.pslab.communication.analogChannel.AnalogInputSource;
import io.pslab.communication.digitalChannel.DigitalChannel;
import io.pslab.communication.peripherals.I2C;
import io.pslab.communication.peripherals.MCP4728;
import io.pslab.communication.peripherals.NRF24L01;
import io.pslab.communication.peripherals.RadioLink;
import io.pslab.communication.peripherals.SPI;
import io.pslab.fragment.HomeFragment;
import io.pslab.others.InitializationVariable;

/**
 * Created by viveksb007 on 28/3/17.
 */

public class ScienceLab {

    private static final String TAG = "ScienceLab";
    public static Thread initialisationThread;

    private int CAP_AND_PCS = 0;
    private int ADC_SHIFTS_LOCATION1 = 1;
    private int ADC_SHIFTS_LOCATION2 = 2;
    private int ADC_POLYNOMIALS_LOCATION = 3;

    private int DAC_SHIFTS_PV1A = 4;
    private int DAC_SHIFTS_PV1B = 5;
    private int DAC_SHIFTS_PV2A = 6;
    private int DAC_SHIFTS_PV2B = 7;
    private int DAC_SHIFTS_PV3A = 8;
    private int DAC_SHIFTS_PV3B = 9;

    public int BAUD = 1000000;
    public int DDS_CLOCK, MAX_SAMPLES, samples, triggerLevel, triggerChannel, errorCount,
            channelsInBuffer, digitalChannelsInBuffer, dataSplitting;
    public double sin1Frequency, sin2Frequency;
    double[] currents, currentScalars, gainValues, buffer;
    double SOCKET_CAPACITANCE, resistanceScaling, timebase;
    public boolean streaming, calibrated = false;

    String[] allAnalogChannels, allDigitalChannels;
    HashMap<String, AnalogInputSource> analogInputSources = new HashMap<>();
    HashMap<String, Double> squareWaveFrequency = new HashMap<>();
    HashMap<String, Integer> gains = new HashMap<>();
    HashMap<String, String> waveType = new HashMap<>();
    ArrayList<AnalogAquisitionChannel> aChannels = new ArrayList<>();
    ArrayList<DigitalChannel> dChannels = new ArrayList<>();

    private CommunicationHandler mCommunicationHandler;
    private PacketHandler mPacketHandler;
    private CommandsProto mCommandsProto;
    private AnalogConstants mAnalogConstants;
    public I2C i2c;
    public SPI spi;
    private NRF24L01 nrf;
    private MCP4728 dac;

    /**
     * Constructor
     *
     * @param communicationHandler
     */
    public ScienceLab(CommunicationHandler communicationHandler) {
        mCommandsProto = new CommandsProto();
        mAnalogConstants = new AnalogConstants();
        mCommunicationHandler = communicationHandler;
        if (isDeviceFound() && MainActivity.hasPermission) {
            try {
                mCommunicationHandler.open();
                //Thread.sleep(200);
                mPacketHandler = new PacketHandler(50, mCommunicationHandler);
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
            }
        }
        if (isConnected()) {
            initializeVariables();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    initialisationThread = new Thread(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                runInitSequence(true);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            calibrated = true;
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    if (HomeFragment.booleanVariable == null) {
                                        HomeFragment.booleanVariable = new InitializationVariable();
                                    }
                                    HomeFragment.booleanVariable.setVariable(true);
                                }
                            });
                        }
                    });
                    initialisationThread.start();

                }
            }, 1000);
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
        squareWaveFrequency.put("SQR1", 0.0);
        squareWaveFrequency.put("SQR2", 0.0);
        squareWaveFrequency.put("SQR3", 0.0);
        squareWaveFrequency.put("SQR4", 0.0);
    }

    private void runInitSequence(Boolean loadCalibrationData) throws IOException {
        ArrayList<String> aboutArray = new ArrayList<>();
        if (!isConnected()) {
            Log.e(TAG, "Check hardware connections. Not connected");
        }
        streaming = false;
        for (String aChannel : mAnalogConstants.biPolars) {
            aChannels.add(new AnalogAquisitionChannel(aChannel));
        }
        gainValues = mAnalogConstants.gains;
        this.buffer = new double[10000];
        Arrays.fill(this.buffer, 0);
        SOCKET_CAPACITANCE = 42e-12;
        resistanceScaling = 1;
        allDigitalChannels = DigitalChannel.digitalChannelNames;
        gains.put("CH1", 0);
        gains.put("CH2", 0);
        for (int i = 0; i < 4; i++) {
            dChannels.add(new DigitalChannel(i));
        }
        i2c = new I2C(mPacketHandler);
        spi = new SPI(mPacketHandler);
        if (isConnected()) {
            for (String temp : new String[]{"CH1", "CH2"}) {
                this.setGain(temp, 0, true);
            }
            for (String temp : new String[]{"SI1", "SI2"}) {
                loadEquation(temp, "sine");
            }
            spi.setParameters(1, 7, 1, 0, null);
        }
        nrf = new NRF24L01(mPacketHandler);
        if (nrf.ready) {
            aboutArray.add("Radio Transceiver is : Installed");
        } else {
            aboutArray.add("Radio Transceiver is : Not Installed");
        }
        dac = new MCP4728(mPacketHandler, i2c);
        this.calibrated = false;
        // Check for calibration data if connected. And process them if found
        if (loadCalibrationData && isConnected()) {

            /* CAPS AND PCS CALIBRATION */
            byte[] capAndPCS = readBulkFlash(this.CAP_AND_PCS, 8 * 4 + 5); // 5 for READY and 32 (8 float numbers) for data

            ArrayList<Byte> capsAndPCSByteData = new ArrayList<>();

            for (byte temp : capAndPCS) {
                capsAndPCSByteData.add(temp);
            }

            /*
            for (int i = 0; i <= 37 / 16; i++) {
                byte[] temp = readFlash(CAP_AND_PCS, i);
                for (byte a : temp) {
                    capsAndPCSByteData.add(a);
                }
            }
            */

            String isReady = "";
            for (int i = 0; i < 5; i++) {
                try {
                    isReady += new String(new byte[]{capsAndPCSByteData.get(i)}, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            if ("READY".equals(isReady)) {
                ArrayList<Float> scalars = new ArrayList<>();
                for (int i = 0; i < 8; i++) {
                    // unpacking byte data to float, total 32 bytes -> 8 floats of 4 bytes each
                    scalars.add(Float.intBitsToFloat(
                            (capsAndPCSByteData.get(5 + i * 4) & 0xFF) |
                                    (capsAndPCSByteData.get(5 + i * 4 + 1) & 0xFF) << 8 |
                                    (capsAndPCSByteData.get(5 + i * 4 + 2) & 0xFF) << 16 |
                                    (capsAndPCSByteData.get(5 + i * 4 + 3) & 0xFF) << 24)
                    );
                }
                this.SOCKET_CAPACITANCE = scalars.get(0);
                this.dac.chans.get("PCS").loadCalibrationTwopoint(scalars.get(1), scalars.get(2).intValue());
                double[] tempScalars = new double[scalars.size() - 4];

                for (int i = 4; i < scalars.size(); i++) {
                    tempScalars[i - 4] = scalars.get(i);
                }
                this.calibrateCTMU(tempScalars);
                this.resistanceScaling = scalars.get(3);
                // add info in aboutArray
            } else {
                this.SOCKET_CAPACITANCE = 42e-12;
                Log.v(TAG, "Cap and PCS calibration invalid");
            }


            /* POLYNOMIAL DATA FOR CALIBRATION */
            //byte[] polynomials = readBulkFlash(this.ADC_POLYNOMIALS_LOCATION, 2048);

            ArrayList<Byte> polynomialsByteData = new ArrayList<>();
            for (int i = 0; i < 2048 / 16; i++) {
                byte[] temp = readFlash(ADC_POLYNOMIALS_LOCATION, i);
                for (byte a : temp) {
                    polynomialsByteData.add(a);
                }
            }

            //Log.v("PolynomialByteDataSize:", "" + polynomialsByteData.size());

            String polynomialByteString = "";
            for (int i = 0; i < 2048; i++) {
                try {
                    polynomialByteString += new String(new byte[]{polynomialsByteData.get(i)}, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            // todo : change to "PSLab" after PSLab firmware is ready
            if ("SEELablet".equals(polynomialByteString.substring(0, 9))) {
                Log.v(TAG, "ADC calibration found...");
                this.calibrated = true;
                ArrayList<Byte> adcShifts = new ArrayList<>();
                for (int i = 0; i < 2048 / 16; i++) {
                    byte[] temp = readFlash(ADC_SHIFTS_LOCATION1, i);
                    for (byte a : temp) {
                        adcShifts.add(a);
                    }
                }
                for (int i = 0; i < 2048 / 16; i++) {
                    byte[] temp = readFlash(ADC_SHIFTS_LOCATION2, i);
                    for (byte a : temp) {
                        adcShifts.add(a);
                    }
                }
                String[] polySections = polynomialByteString.split("STOP");
                String adcSlopeOffsets = polySections[0];
                String dacSlopeIntercept = polySections[1];
                String inlSlopeIntercept = polySections[2];

                Log.v("adcSlopeOffsets", adcSlopeOffsets);
                Log.v("dacSlopeIntercept", dacSlopeIntercept);
                Log.v("inlSlopeIntercept", inlSlopeIntercept);

                Map<String, ArrayList<Double[]>> polyDict = new LinkedHashMap<>();
                String[] adcSlopeOffsetsSplit = adcSlopeOffsets.split(Pattern.quote(">|"));
                /*
                for (String temp : adcSlopeOffsetsSplit) {
                    Log.v("zz", temp);
                }
                */
                for (int i = 0; i < adcSlopeOffsetsSplit.length; i++) {
                    if (i == 0) continue;
                    //Log.v("" + i, adcSlopeOffsetsSplit[i]);
                    String cals = adcSlopeOffsetsSplit[i].substring(5);
                    polyDict.put(adcSlopeOffsetsSplit[i].substring(0, 3), new ArrayList<Double[]>());
                    for (int j = 0; j < cals.length() / 16; j++) {
                        Double[] poly = new Double[4];
                        for (int k = 0; k < 4; k++) {
                            Float f = ByteBuffer.wrap(cals.substring((16 * j) + (k * 4), (16 * j) + (k * 4) + 4).getBytes()).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                            poly[k] = (double) f;
                        }
                        polyDict.get(adcSlopeOffsetsSplit[i].substring(0, 3)).add(poly);
                    }
                }

                double[] inlSlopeInterceptD = new double[]{ByteBuffer.wrap(inlSlopeIntercept.substring(0, 4).getBytes()).order(ByteOrder.LITTLE_ENDIAN).getFloat(), ByteBuffer.wrap(inlSlopeIntercept.substring(4, 8).getBytes()).order(ByteOrder.LITTLE_ENDIAN).getFloat()};
                double[] adcShiftsDouble = new double[adcShifts.size()];
                for (int i = 0; i < adcShifts.size(); i++) {
                    adcShiftsDouble[i] = ByteBuffer.wrap(new byte[]{0, adcShifts.get(i)}).order(ByteOrder.LITTLE_ENDIAN).getShort();
                }
                for (Map.Entry<String, AnalogInputSource> entry : this.analogInputSources.entrySet()) {
                    this.analogInputSources.get(entry.getKey()).loadCalibrationTable(adcShiftsDouble, inlSlopeInterceptD[0], inlSlopeInterceptD[1]);
                    if (polyDict.containsKey(entry.getKey())) {
                        this.analogInputSources.get(entry.getKey()).loadPolynomials(polyDict.get(entry.getKey()));
                        this.analogInputSources.get(entry.getKey()).calibrationReady = true;
                    }
                    this.analogInputSources.get(entry.getKey()).regenerateCalibration();
                }

                String[] dacSlopeInterceptSplit = dacSlopeIntercept.split(Pattern.quote(">|"));
                for (int i = 0; i < dacSlopeInterceptSplit.length; i++) {
                    if (i == 0) continue;
                    String name = dacSlopeInterceptSplit[i].substring(0, 3);
                    double[] fits = new double[6];
                    try {
                        for (int j = 0; j < 6; j++) {
                            fits[j] = ByteBuffer.wrap(dacSlopeInterceptSplit[i].substring(5 + j * 4, 5 + (j + 1) * 4).getBytes()).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                        }
                    } catch (StringIndexOutOfBoundsException e) {
                        e.printStackTrace();
                        continue;
                    }
                    double slope = fits[0];
                    double intercept = fits[1];
                    double[] fitVals = Arrays.copyOfRange(fits, 2, fits.length);
                    if ("PV1".equals(name) | "PV2".equals(name) | "PV3".equals(name)) {

                        double[] DACX = new double[4096];
                        double factor = (this.dac.chans.get(name).range[1] - this.dac.chans.get(name).range[0]) / 4096;
                        int index = 0;
                        for (double j = this.dac.chans.get(name).range[0]; index < 4096; j += factor) {
                            DACX[index++] = j;
                        }

                        /*
                        Log.v("factor", "" + factor);
                        Log.v("rang0", "" + this.dac.chans.get(name).range[0]);
                        Log.v("rang1", "" + this.dac.chans.get(name).range[1]);
                        Log.v("DACX", Arrays.toString(DACX));
                        Log.v("Last", "" + DACX[4095]);
                        */

                        ArrayList<Byte> OFF = new ArrayList<>();
                        if ("PV1".equals(name)) {
                            for (int j = 0; j < 2048 / 16; j++) {
                                byte[] temp = readFlash(DAC_SHIFTS_PV1A, j);
                                for (byte a : temp) {
                                    OFF.add(a);
                                }
                            }
                            for (int j = 0; j < 2048 / 16; j++) {
                                byte[] temp = readFlash(DAC_SHIFTS_PV1B, j);
                                for (byte a : temp) {
                                    OFF.add(a);
                                }
                            }
                        }
                        if ("PV2".equals(name)) {
                            for (int j = 0; j < 2048 / 16; j++) {
                                byte[] temp = readFlash(DAC_SHIFTS_PV2A, j);
                                for (byte a : temp) {
                                    OFF.add(a);
                                }
                            }
                            for (int j = 0; j < 2048 / 16; j++) {
                                byte[] temp = readFlash(DAC_SHIFTS_PV2B, j);
                                for (byte a : temp) {
                                    OFF.add(a);
                                }
                            }
                        }
                        if ("PV3".equals(name)) {
                            for (int j = 0; j < 2048 / 16; j++) {
                                byte[] temp = readFlash(DAC_SHIFTS_PV3A, j);
                                for (byte a : temp) {
                                    OFF.add(a);
                                }
                            }
                            for (int j = 0; j < 2048 / 16; j++) {
                                byte[] temp = readFlash(DAC_SHIFTS_PV3B, j);
                                for (byte a : temp) {
                                    OFF.add(a);
                                }
                            }
                        }

                        ArrayUtils.reverse(fitVals);
                        PolynomialFunction fitFunction = new PolynomialFunction(fitVals);
                        double[] yData = new double[DACX.length];
                        for (int j = 0; j < DACX.length; j++) {
                            yData[j] = fitFunction.value(DACX[j]) - (OFF.get(j) * slope + intercept);
                        }
                        int LOOKBEHIND = 100;
                        int LOOKAHEAD = 100;
                        ArrayList<Double> offset = new ArrayList<>();
                        for (int j = 0; j < 4096; j++) {
                            double[] temp = new double[Math.min(4095, j + LOOKAHEAD) - Math.max(j - LOOKBEHIND, 0)];
                            for (int k = Math.max(j - LOOKBEHIND, 0), p = 0; k < Math.min(4095, j + LOOKAHEAD); k++, p++) {
                                temp[p] = yData[k] - DACX[j];
                                if (temp[p] < 0) temp[p] = -temp[p];
                            }
                            // finding minimum in temp array
                            double min = temp[0];
                            for (double aTemp : temp) {
                                if (min > aTemp)
                                    min = aTemp;
                            }
                            offset.add(min - (j - Math.max(j - LOOKBEHIND, 0)));
                        }
                        this.dac.chans.get(name).loadCalibrationTable(offset);

                    }
                }


                /*
                int count = 0;
                int tempADC = 0, tempDAC = 0;
                for (int i = 0; i < polynomialsByteData.size() - 3; i++) {
                    if (polynomialsByteData.get(i) == 'S' && polynomialsByteData.get(i + 1) == 'T' && polynomialsByteData.get(i + 2) == 'O' && polynomialsByteData.get(i + 3) == 'P') {
                        switch (count) {
                            case 0:
                                tempADC = i;
                                count++;
                                break;
                            case 1:
                                tempDAC = i;
                                count++;
                                break;
                        }
                    }
                }
                List<Byte> adcSlopeOffsets = polynomialsByteData.subList(0, tempADC);  //Arrays.copyOfRange(polynomials, 0, tempADC);
                List<Byte> dacSlopeIntercept = polynomialsByteData.subList(tempADC + 4, tempDAC);  //Arrays.copyOfRange(polynomials, tempADC + 4, tempDAC);
                List<Byte> inlSlopeIntercept = polynomialsByteData.subList(tempDAC + 4, polynomialsByteData.size());  //Arrays.copyOfRange(polynomials, tempDAC + 4, polynomialsByteData.size());
                int marker = 0;
                List<List<Byte>> tempSplit = new ArrayList<>();
                for (int i = 0; i < adcSlopeOffsets.size() - 1; i++) {
                    if (adcSlopeOffsets.get(i) == '>' && adcSlopeOffsets.get(i + 1) == '|') {
                        if (marker != 0) {
                            tempSplit.add(adcSlopeOffsets.subList(marker, i));
                        }
                        marker = i + 2;
                    }
                }
                Map<Byte, ArrayList<Float>> polyDict = new LinkedHashMap<>();
                for (List<Byte> temp : tempSplit) {
                    List<Byte> temp_ = temp.subList(5, temp.size());
                    for (int i = 0; i < temp_.size() / 16; i++) {
                    }
                }
                */

            }
        }
    }

    /**
     * @return Resistance of connected resistor between RES an GND pins
     */
    public Double getResistance() {
        double volt = this.getAverageVoltage("RES", null);
        if (volt > 3.295) return null;
        double current = (3.3 - volt) / 5.1e3;
        return (volt / current) * this.resistanceScaling;
    }

    public void ignoreCalibration() {
        for (Map.Entry<String, AnalogInputSource> tempAnalogInputSource : this.analogInputSources.entrySet()) {
            tempAnalogInputSource.getValue().ignoreCalibration();
            tempAnalogInputSource.getValue().regenerateCalibration();
        }
        for (String temp : new String[]{"PV1", "PV2", "PV3"}) {
            this.dac.ignoreCalibration(temp);
        }
    }

    public String getVersion() throws IOException {
        if (isConnected()) {
            return mPacketHandler.getVersion();
        } else {
            return "Not Connected";
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

    public void close() {
        try {
            mCommunicationHandler.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Blocking call that fetches an oscilloscope trace from the specified input channel
     *
     * @param channel Channel to select as input. ['CH1','CH2','CH3','MIC',RES']
     * @param samples Number of samples to fetch. Maximum 10000
     * @param timeGap Timegap between samples in microseconds
     * @return Arrays X(timestamps),Y(Corresponding Voltage values)
     */
    public HashMap<String, double[]> captureOne(String channel, int samples, double timeGap) {
        return this.captureFullSpeed(channel, samples, timeGap, new ArrayList<String>(), null);
    }

    /**
     * Blocking call that fetches oscilloscope traces from CH1,CH2
     *
     * @param samples       Number of samples to fetch. Maximum 5000
     * @param timeGap       Timegap between samples in microseconds
     * @param traceOneRemap Choose the analog input for channel 1. It is connected to CH1 by default. Channel 2 always reads CH2.
     * @param trigger       boolean whether trigger is selected or not
     * @return Arrays X(timestamps),Y1(Voltage at CH1),Y2(Voltage at CH2)
     */
    public HashMap<String, double[]> captureTwo(int samples, double timeGap, String traceOneRemap, boolean trigger) {
        if (traceOneRemap == null) traceOneRemap = "CH1";
        this.captureTraces(2, samples, timeGap, traceOneRemap, trigger, null);
        try {
            Thread.sleep((long) (1e-6 * this.samples * this.timebase + 0.1) * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while (this.oscilloscopeProgress()[0] == 0) ;
        this.fetchChannel(1);
        this.fetchChannel(2);
        HashMap<String, double[]> retData = new HashMap<>();
        retData.put("x", this.aChannels.get(0).getXAxis());
        retData.put("y1", this.aChannels.get(0).getYAxis());
        retData.put("y2", this.aChannels.get(1).getYAxis());
        return retData;
    }

    /**
     * Blocking call that fetches oscilloscope traces from CH1,CH2,CH3,MIC
     *
     * @param samples       Number of samples to fetch. Maximum 2500
     * @param timeGap       Timegap between samples in microseconds. Minimum 1.75uS
     * @param traceOneRemap Choose the analog input for channel 1. It is connected to CH1 by default. Channel 2 always reads CH2.
     * @param trigger       boolean whether trigger is selected or not
     * @return Arrays X(timestamps),Y1(Voltage at CH1),Y2(Voltage at CH2),Y3(Voltage at CH3),Y4(Voltage at CH4)
     */
    public HashMap<String, double[]> captureFour(int samples, double timeGap, String traceOneRemap, boolean trigger) {
        if (traceOneRemap == null) traceOneRemap = "CH1";
        this.captureTraces(4, samples, timeGap, traceOneRemap, trigger, null);
        try {
            Thread.sleep((long) (1e-6 * this.samples * this.timebase + 0.1) * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while (this.oscilloscopeProgress()[0] == 0) ;
        HashMap<String, double[]> retData = new HashMap<>();
        HashMap<String, double[]> tempMap = this.fetchTrace(1);
        retData.put("x", tempMap.get("x"));
        retData.put("y", tempMap.get("y"));
        retData.put("y2", this.fetchTrace(2).get("y"));
        retData.put("y3", this.fetchTrace(3).get("y"));
        retData.put("y4", this.fetchTrace(4).get("y"));
        return retData;
    }

    /**
     * Blocking call that fetches oscilloscope traces from a set of specified channels
     *
     * @param samples Number of samples to fetch. Maximum 10000/(total specified channels)
     * @param timeGap Timegap between samples in microseconds.
     * @param args    Channel names
     * @return Arrays X(timestamps),Y1,Y2 ..
     */
    public Map<String, ArrayList<Double>> captureMultiple(int samples, double timeGap, List<String> args) {
        if (args.size() == 0) {
            Log.v(TAG, "Please specify channels to record");
            return null;
        }
        timeGap = (int) (timeGap * 8) / 8;
        if (timeGap < 1.5) timeGap = (int) (1.5 * 8) / 8;
        int totalChannels = args.size();
        int totalSamples = samples * totalChannels;
        if (totalSamples > this.MAX_SAMPLES) {
            Log.v(TAG, "Sample limit exceeded. 10,000 max");
            totalSamples = this.MAX_SAMPLES;
            samples = this.MAX_SAMPLES / totalChannels;
        }
        int channelSelection = 0;
        for (String channel : args) {
            channelSelection |= (1 << this.analogInputSources.get(channel).CHOSA);
        }
        Log.v(TAG, "Selection " + channelSelection + args.size() + Integer.toHexString(channelSelection | ((totalChannels - 1) << 12)));
        try {
            mPacketHandler.sendByte(mCommandsProto.ADC);
            mPacketHandler.sendByte(mCommandsProto.CAPTURE_MULTIPLE);
            mPacketHandler.sendInt(channelSelection | ((totalChannels - 1) << 12));
            mPacketHandler.sendInt(totalSamples);
            mPacketHandler.sendInt((int) this.timebase * 8);
            mPacketHandler.getAcknowledgement();
            Log.v(TAG, "Wait");
            Thread.sleep((long) (1e-6 * totalSamples * timeGap + .01) * 1000);
            Log.v(TAG, "Done");
            ArrayList<Integer> listData = new ArrayList<>();
            for (int i = 0; i < totalSamples / this.dataSplitting; i++) {
                mPacketHandler.sendByte(mCommandsProto.ADC);
                mPacketHandler.sendByte(mCommandsProto.GET_CAPTURE_CHANNEL);
                mPacketHandler.sendByte(0);
                mPacketHandler.sendInt(this.dataSplitting);
                mPacketHandler.sendInt(i * this.dataSplitting);
                byte[] data = new byte[this.dataSplitting * 2 + 1];
                mPacketHandler.read(data, this.dataSplitting * 2 + 1);
                for (int j = 0; j < data.length - 1; j++)
                    listData.add((int) data[j] & 0xff);
                //mPacketHandler.getAcknowledgement();
            }

            if ((totalSamples % this.dataSplitting) != 0) {
                mPacketHandler.sendByte(mCommandsProto.ADC);
                mPacketHandler.sendByte(mCommandsProto.GET_CAPTURE_CHANNEL);
                mPacketHandler.sendByte(0);
                mPacketHandler.sendInt(totalSamples % this.dataSplitting);
                mPacketHandler.sendInt(totalSamples - totalSamples % this.dataSplitting);
                byte[] data = new byte[2 * (totalSamples % this.dataSplitting) + 1];
                mPacketHandler.read(data, 2 * (totalSamples % this.dataSplitting) + 1);
                for (int j = 0; j < data.length - 1; j++)
                    listData.add((int) data[j] & 0xff);
                //mPacketHandler.getAcknowledgement();
            }
            for (int i = 0; i < totalSamples; i++) {
                this.buffer[i] = (listData.get(i * 2) | (listData.get(i * 2 + 1) << 8));
            }
            Map<String, ArrayList<Double>> retData = new LinkedHashMap<>();
            ArrayList<Double> timeBase = new ArrayList<>();
            double factor = timeGap * (samples - 1) / samples;
            for (double i = 0; i < timeGap * (samples - 1); i += factor) timeBase.add(i);
            retData.put("time", timeBase);
            for (int i = 0; i < totalChannels; i++) {
                ArrayList<Double> yValues = new ArrayList<>();
                for (int j = i; j < totalSamples; j += totalChannels) {
                    yValues.add(buffer[j]);
                }
                retData.put("CH" + String.valueOf(i + 1), yValues);
            }
            return retData;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void captureFullSpeedInitialize(String channel, int samples, double timeGap, List<String> args, Integer interval) {
        if (interval == null) interval = 1000;
        timeGap = (int) (timeGap * 8) / 8;
        if (timeGap < 0.5) timeGap = (int) (0.5 * 8) / 8;
        if (samples > this.MAX_SAMPLES) {
            Log.v(TAG, "Sample limit exceeded. 10,000 max");
            samples = this.MAX_SAMPLES;
        }
        this.timebase = (int) (timeGap * 8) / 8;
        this.samples = samples;
        int CHOSA = this.analogInputSources.get(channel).CHOSA;
        this.aChannels.get(0).setParams(channel, samples, this.timebase, 10, this.analogInputSources.get(channel), null);
        this.channelsInBuffer = 1;

        try {
            mPacketHandler.sendByte(mCommandsProto.ADC);
            if (args.contains("SET_LOW"))
                mPacketHandler.sendByte(mCommandsProto.SET_LO_CAPTURE);
            else if (args.contains("SET_HIGH"))
                mPacketHandler.sendByte(mCommandsProto.SET_HI_CAPTURE);
            else if (args.contains("FIRE_PULSES")) {
                mPacketHandler.sendByte(mCommandsProto.PULSE_TRAIN);
                Log.v(TAG, "Firing SQR1 pulses for " + interval + " uSec");
            } else
                mPacketHandler.sendByte(mCommandsProto.CAPTURE_DMASPEED);
            mPacketHandler.sendByte(CHOSA);
            mPacketHandler.sendInt(samples);
            mPacketHandler.sendInt((int) timeGap * 8);
            if (args.contains("FIRE_PULSES")) {
                mPacketHandler.sendInt(interval);
                Thread.sleep((long) (interval * 1e-6));
            }
            mPacketHandler.getAcknowledgement();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Blocking call that fetches oscilloscope traces from a single oscilloscope channel at a maximum speed of 2MSPS
     *
     * @param channel  Channel name 'CH1','CH2','CH3','MIC','RES'
     * @param samples  Number of samples to fetch. Maximum 10000/(total specified channels)
     * @param timeGap  Timegap between samples in microseconds.
     * @param args     Specify if SQR1 must be toggled right before capturing.
     *                 'SET_LOW': Set SQR1 to 0V
     *                 'SET_HIGH': Set SQR1 to 1V
     *                 'FIRE_PULSES': output a preset frequency on SQR1 for a given interval (keyword arg 'interval' must be specified or it will default to 1000uS) before acquiring data. This is used for measuring speed of sound using piezos if no arguments are specified, a regular capture will be executed.
     * @param interval Units: uS. Necessary if 'FIRE_PULSES' argument was supplied. Default 1000uS
     * @return Timestamp array ,voltage_value array
     */
    public HashMap<String, double[]> captureFullSpeed(String channel, int samples, double timeGap, List<String> args, Integer interval) {
        /*
         * Blocking call that fetches oscilloscope traces from a single oscilloscope channel at a maximum speed of 2MSPS
         */

        this.captureFullSpeedInitialize(channel, samples, timeGap, args, interval);

        try {
            Thread.sleep((long) (1000 * (1e-6 * this.samples * this.timebase + 0.1 + ((interval != null) ? interval : 0) * 1e-6)));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        this.fetchChannel(1);
        HashMap<String, double[]> retData = new HashMap<>();
        retData.put("x", this.aChannels.get(0).getXAxis());
        retData.put("y", this.aChannels.get(0).getYAxis());
        return retData;
    }

    private void captureFullSpeedHrInitialize(String channel, int samples, double timeGap, List<String> args) {
        timeGap = (int) (timeGap * 8) / 8;
        if (timeGap < 0.5) timeGap = (int) (0.5 * 8) / 8;
        if (samples > this.MAX_SAMPLES) {
            Log.v(TAG, "Sample limit exceeded. 10,000 max");
            samples = this.MAX_SAMPLES;
        }
        this.timebase = (int) (timeGap * 8) / 8;
        this.samples = samples;
        int CHOSA = this.analogInputSources.get(channel).CHOSA;

        try {
            mPacketHandler.sendByte(mCommandsProto.ADC);
            if (args.contains("SET_LOW"))
                mPacketHandler.sendByte(mCommandsProto.SET_LO_CAPTURE);
            else if (args.contains("SET_HIGH"))
                mPacketHandler.sendByte(mCommandsProto.SET_HI_CAPTURE);
            else if (args.contains("READ_CAP")) {
                mPacketHandler.sendByte(mCommandsProto.MULTIPOINT_CAPACITANCE);
            } else
                mPacketHandler.sendByte(mCommandsProto.CAPTURE_DMASPEED);
            mPacketHandler.sendByte(CHOSA | 0x80);
            mPacketHandler.sendInt(samples);
            mPacketHandler.sendInt((int) timeGap * 8);
            mPacketHandler.getAcknowledgement();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param channel Channel name 'CH1' / 'CH2' ... 'RES'
     * @param samples Number of samples to fetch. Maximum 10000/(total specified channels)
     * @param timeGap Timegap between samples in microseconds.
     * @param args    timestamp array ,voltage_value array
     * @return Timestamp array ,voltage_value array
     */
    public Map<String, double[]> captureFullSpeedHr(String channel, int samples, double timeGap, List<String> args) {
        this.captureFullSpeedHrInitialize(channel, samples, timeGap, args);
        try {
            Thread.sleep((long) (1e-6 * this.samples * this.timebase + 0.1));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Map<String, double[]> axisData = retrieveBufferData(channel, this.samples, this.timebase);
        if (axisData == null) {
            Log.v(TAG, "Retrieved Buffer Data as null");
            return null;
        }
        Map<String, double[]> retData = new HashMap<>();
        retData.put("x", axisData.get("x"));
        retData.put("y", this.analogInputSources.get(channel).cal12(axisData.get("y")));
        return retData;
    }

    private Map<String, double[]> retrieveBufferData(String channel, int samples, double timeGap) {
        ArrayList<Integer> listData = new ArrayList<>();
        try {
            for (int i = 0; i < samples / this.dataSplitting; i++) {
                mPacketHandler.sendByte(mCommandsProto.ADC);
                mPacketHandler.sendByte(mCommandsProto.GET_CAPTURE_CHANNEL);
                mPacketHandler.sendByte(0);
                mPacketHandler.sendInt(this.dataSplitting);
                mPacketHandler.sendInt(i * this.dataSplitting);
                byte[] data = new byte[this.dataSplitting * 2 + 1];
                mPacketHandler.read(data, this.dataSplitting * 2 + 1);
                for (int j = 0; j < data.length - 1; j++)
                    listData.add((int) data[j] & 0xff);
                //mPacketHandler.getAcknowledgement();
            }

            if ((samples % this.dataSplitting) != 0) {
                mPacketHandler.sendByte(mCommandsProto.ADC);
                mPacketHandler.sendByte(mCommandsProto.GET_CAPTURE_CHANNEL);
                mPacketHandler.sendByte(0);
                mPacketHandler.sendInt(samples * this.dataSplitting);
                mPacketHandler.sendInt(samples - samples % this.dataSplitting);
                byte[] data = new byte[2 * (samples % this.dataSplitting)];
                mPacketHandler.read(data, 2 * (samples % this.dataSplitting));
                for (int j = 0; j < data.length - 1; j++)
                    listData.add((int) data[j] & 0xff);
                //mPacketHandler.getAcknowledgement();
            }

            for (int i = 0; i < samples; i++) {
                this.buffer[i] = (listData.get(i * 2) | (listData.get(i * 2 + 1) << 8));
            }

            double[] timeAxis = linSpace(0, timeGap * (samples - 1), samples);
            Map<String, double[]> retData = new HashMap<>();
            retData.put("x", timeAxis);
            retData.put("y", Arrays.copyOfRange(buffer, 0, samples));
            return retData;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Instruct the ADC to start sampling. use fetchTrace to retrieve the data
     *
     * @param number          Channels to acquire. 1/2/4
     * @param samples         Total points to store per channel. Maximum 3200 total
     * @param timeGap         Timegap between two successive samples (in uSec)
     * @param channelOneInput Map channel 1 to 'CH1'
     * @param trigger         Whether or not to trigger the oscilloscope based on the voltage level set
     * @param CH123SA
     */
    public void captureTraces(int number, int samples, double timeGap, String channelOneInput, Boolean trigger, Integer CH123SA) {
        if (CH123SA == null) CH123SA = 0;
        if (channelOneInput == null) channelOneInput = "CH1";
        int triggerOrNot = 0;
        if (trigger) triggerOrNot = 0x80;
        this.timebase = timeGap;
        this.timebase = (int) (this.timebase * 8) / 8;
        if (!this.analogInputSources.containsKey(channelOneInput)) {
            Log.e(TAG, "Invalid input channel");
            return;
        }
        int CHOSA = this.analogInputSources.get(channelOneInput).CHOSA;
        try {
            mPacketHandler.sendByte(mCommandsProto.ADC);
            if (number == 1) {
                if (this.timebase < 1.5)
                    this.timebase = (int) (1.5 * 8) / 8;
                if (samples > this.MAX_SAMPLES) samples = this.MAX_SAMPLES;
                this.aChannels.get(0).setParams(channelOneInput, samples, this.timebase, 10, this.analogInputSources.get(channelOneInput), null);
                mPacketHandler.sendByte(mCommandsProto.CAPTURE_ONE);
                mPacketHandler.sendByte(CHOSA | triggerOrNot);
            } else if (number == 2) {
                if (this.timebase < 1.75)
                    this.timebase = (int) (1.75 * 8) / 8;
                if (samples > this.MAX_SAMPLES / 2) samples = this.MAX_SAMPLES / 2;
                this.aChannels.get(0).setParams(channelOneInput, samples, this.timebase, 10, this.analogInputSources.get(channelOneInput), null);
                this.aChannels.get(1).setParams("CH2", samples, this.timebase, 10, this.analogInputSources.get("CH2"), null);
                mPacketHandler.sendByte(mCommandsProto.CAPTURE_TWO);
                mPacketHandler.sendByte(CHOSA | triggerOrNot);
            } else if (number == 3 | number == 4) {
                if (this.timebase < 1.75)
                    this.timebase = (int) (1.75 * 8) / 8;
                if (samples > this.MAX_SAMPLES / 4) samples = this.MAX_SAMPLES / 4;
                this.aChannels.get(0).setParams(channelOneInput, samples, this.timebase, 10, this.analogInputSources.get(channelOneInput), null);
                int i = 1;
                for (String temp : new String[]{"CH2", "CH3", "MIC"}) {
                    this.aChannels.get(i).setParams(temp, samples, this.timebase, 10, this.analogInputSources.get(temp), null);
                    i++;
                }
                mPacketHandler.sendByte(mCommandsProto.CAPTURE_FOUR);
                mPacketHandler.sendByte(CHOSA | (CH123SA << 4) | triggerOrNot);
            }
            this.samples = samples;
            mPacketHandler.sendInt(samples);
            mPacketHandler.sendInt((int) this.timebase * 8);
            mPacketHandler.getAcknowledgement();
            this.channelsInBuffer = number;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Instruct the ADC to start sampling. Use fetchTrace to retrieve the data
     *
     * @param channel Channel to acquire data from 'CH1', 'CH2', 'CH3', 'MIC'
     * @param samples Total points to store per channel. Maximum 3200 total.
     * @param timeGap Timegap between two successive samples (in uSec)
     * @param trigger Whether or not to trigger the oscilloscope based on the voltage level set by
     */
    public void captureHighResolutionTraces(String channel, int samples, double timeGap, Boolean trigger) {
        int triggerOrNot = 0;
        if (trigger) triggerOrNot = 0x80;
        this.timebase = timeGap;
        try {
            mPacketHandler.sendByte(mCommandsProto.ADC);
            int CHOSA = this.analogInputSources.get(channel).CHOSA;
            if (this.timebase < 3) this.timebase = 3;
            if (samples > this.MAX_SAMPLES) samples = this.MAX_SAMPLES;
            this.aChannels.get(0).setParams(channel, samples, timebase, 12, this.analogInputSources.get(channel), null);
            mPacketHandler.sendByte(mCommandsProto.CAPTURE_12BIT);
            mPacketHandler.sendByte(CHOSA | triggerOrNot);
            this.samples = samples;
            mPacketHandler.sendInt(samples);
            mPacketHandler.sendInt((int) this.timebase * 8);
            mPacketHandler.getAcknowledgement();
            this.channelsInBuffer = 1;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Fetches a channel(1-4) captured by :func:captureTraces called prior to this, and returns xAxis,yAxis
     *
     * @param channelNumber Any of the maximum of four channels that the oscilloscope captured. 1/2/3/4
     * @return time array,voltage array
     */
    public HashMap<String, double[]> fetchTrace(int channelNumber) {
        this.fetchChannel(channelNumber);
        HashMap<String, double[]> retData = new HashMap<>();
        retData.put("x", this.aChannels.get(channelNumber - 1).getXAxis());
        retData.put("y", this.aChannels.get(channelNumber - 1).getYAxis());
        return retData;
    }

    /**
     * Returns the number of samples acquired by the capture routines, and the conversion_done status
     *
     * @return conversion done(bool) ,samples acquired (number)
     */
    public int[] oscilloscopeProgress() {
        /*
         * returns the number of samples acquired by the capture routines, and the conversion_done status
         *
         * return structure int[]{conversionDone, samples}
         */

        int conversionDone = 0;
        int samples = 0;
        try {
            mPacketHandler.sendByte(mCommandsProto.ADC);
            mPacketHandler.sendByte(mCommandsProto.GET_CAPTURE_STATUS);
            conversionDone = (int) mPacketHandler.getByte() & 0xff;
            samples = mPacketHandler.getInt();
            mPacketHandler.getAcknowledgement();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new int[]{conversionDone, samples};
    }


    private boolean fetchChannel(int channelNumber) {
        int samples = this.aChannels.get(channelNumber - 1).length;
        if (channelNumber > this.channelsInBuffer) {
            Log.v(TAG, "Channel Unavailable");
            return false;
        }
        Log.v("Samples", "" + samples);
        Log.v("Data Splitting", "" + this.dataSplitting);
        ArrayList<Integer> listData = new ArrayList<>();
        try {
            for (int i = 0; i < samples / this.dataSplitting; i++) {
                mPacketHandler.sendByte(mCommandsProto.ADC);
                mPacketHandler.sendByte(mCommandsProto.GET_CAPTURE_CHANNEL);
                mPacketHandler.sendByte(channelNumber - 1);
                mPacketHandler.sendInt(this.dataSplitting);
                mPacketHandler.sendInt(i * this.dataSplitting);
                byte[] data = new byte[this.dataSplitting * 2 + 1];
                mPacketHandler.read(data, this.dataSplitting * 2 + 1);
                for (int j = 0; j < data.length - 1; j++)
                    listData.add((int) data[j] & 0xff);
                //mPacketHandler.getAcknowledgement();
            }

            if ((samples % this.dataSplitting) != 0) {
                mPacketHandler.sendByte(mCommandsProto.ADC);
                mPacketHandler.sendByte(mCommandsProto.GET_CAPTURE_CHANNEL);
                mPacketHandler.sendByte(channelNumber - 1);
                mPacketHandler.sendInt(samples % this.dataSplitting);
                mPacketHandler.sendInt(samples - samples % this.dataSplitting);
                byte[] data = new byte[2 * (samples % this.dataSplitting) + 1];
                mPacketHandler.read(data, 2 * (samples % this.dataSplitting) + 1);
                for (int j = 0; j < data.length - 1; j++)
                    listData.add((int) data[j] & 0xff);
                //mPacketHandler.getAcknowledgement();
            }

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        for (int i = 0; i < listData.size() / 2; i++) {
            this.buffer[i] = (listData.get(i * 2)) | (listData.get(i * 2 + 1) << 8);

            // Hack for now
            while (this.buffer[i] > 1023) this.buffer[i] -= 1023;

            //(listData.get(i * 2)) | ((listData.get(i * 2 + 1) & 0xff) << 8);
            //ByteBuffer.wrap(new byte[]{listData.get(i * 2), listData.get(i * 2 + 1)}).order(ByteOrder.LITTLE_ENDIAN).getShort();
            //(listData.get(i * 2)) | (listData.get(i * 2 + 1) << 8);
        }

        Log.v("RAW DATA:", Arrays.toString(Arrays.copyOfRange(buffer, 0, samples)));

        this.aChannels.get(channelNumber - 1).yAxis = this.aChannels.get(channelNumber - 1).fixValue(Arrays.copyOfRange(this.buffer, 0, samples));
        return true;
    }


    public boolean fetchChannelOneShot(int channelNumber) {
        int offset = 0;
        int samples = this.aChannels.get(channelNumber - 1).length;
        if (channelNumber > this.channelsInBuffer) {
            Log.e(TAG, "Channel unavailable");
            return false;
        }
        try {
            mPacketHandler.sendByte(mCommandsProto.ADC);
            mPacketHandler.sendByte(mCommandsProto.GET_CAPTURE_CHANNEL);
            mPacketHandler.sendByte(channelNumber - 1);
            mPacketHandler.sendInt(samples);
            mPacketHandler.sendInt(offset);
            byte[] data = new byte[samples * 2 + 1];
            mPacketHandler.read(data, samples * 2 + 1);
            //mPacketHandler.getAcknowledgement();
            for (int i = 0; i < samples; i++) {
                this.buffer[i] = (((int) data[i * 2] & 0xff) | (((int) data[i * 2 + 1] & 0xff) << 8));
            }
            this.aChannels.get(channelNumber - 1).yAxis = this.aChannels.get(channelNumber - 1).fixValue(Arrays.copyOfRange(this.buffer, 0, samples));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * Configure trigger parameters for 10-bit capture commands
     * The capture routines will wait till a rising edge of the input signal crosses the specified level.
     * The trigger will timeout within 8mS, and capture routines will start regardless.
     * These settings will not be used if the trigger option in the capture routines are set to False
     *
     * @param channel     Channel 0,1,2,3. Corresponding to the channels being recorded by the capture routine(not the analog inputs)
     * @param channelName Name of the channel. 'CH1','CH2','CH3','MIC','V+'
     * @param voltage     The voltage level that should trigger the capture sequence(in Volts)
     * @param resolution
     * @param prescalar
     */
    public void configureTrigger(int channel, String channelName, double voltage, Integer resolution, Integer prescalar) {
        if (resolution == null) resolution = 10;
        if (prescalar == null) prescalar = 0;
        try {
            mPacketHandler.sendByte(mCommandsProto.ADC);
            mPacketHandler.sendByte(mCommandsProto.CONFIGURE_TRIGGER);
            mPacketHandler.sendByte((prescalar << 4) | (1 << channel));
            double level;
            if (resolution == 12) {
                level = this.analogInputSources.get(channelName).voltToCode12.value(voltage);
                if (level < 0) level = 0;
                else if (level > 4095) level = 4095;
            } else {
                level = this.analogInputSources.get(channelName).voltToCode10.value(voltage);
                if (level < 0) level = 0;
                else if (level > 1023) level = 1023;
            }
            if (level > Math.pow(2, resolution - 1))
                level = Math.pow(2, resolution - 1);
            else if (level < 0)
                level = 0;
            mPacketHandler.sendInt((int) level);
            mPacketHandler.getAcknowledgement();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set the gain of the selected PGA
     *
     * @param channel 'CH1','CH2'
     * @param gain    (0-8) -> (1x,2x,4x,5x,8x,10x,16x,32x,1/11x)
     * @param force   If True, the amplifier gain will be set even if it was previously set to the same value.
     * @return
     */
    public double setGain(String channel, int gain, Boolean force) {
        if (force == null) force = false;
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

    /**
     * set the gain of the selected PGA
     *
     * @param channel      'CH1','CH2'
     * @param voltageRange Choose from [16,8,4,3,2,1.5,1,.5,160]
     * @return
     */
    public Double selectRange(String channel, double voltageRange) {
        double[] ranges = new double[]{16, 8, 4, 3, 2, 1.5, 1, .5, 160};
        if (Arrays.asList(ArrayUtils.toObject(ranges)).contains(voltageRange)) {
            return this.setGain(channel, Arrays.asList(ArrayUtils.toObject(ranges)).indexOf(voltageRange), null);
        } else
            Log.e(TAG, "Not a valid Range");
        return null;
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

    public double getVoltage(String channelName, Integer sample) {
        this.voltmeterAutoRange(channelName);
        double Voltage = this.getAverageVoltage(channelName, sample);
        if (channelName.equals("CH2") || channelName.equals("CH1")) {
            return 2 * Voltage;
        } else {
            return Voltage;
        }
    }

    private void voltmeterAutoRange(String channelName) {
        if (this.analogInputSources.get(channelName).gainPGA != 0) {
            this.setGain(channelName, 0, true);
        }
    }

    private double autoSelectRange(String channelName, double V) {
        double[] keys = new double[]{8.0, 4.0, 3.0, 2.0, 1.5, 1.0, 0.5, 0.0};
        Map<Double, Integer> cutoffs = new HashMap<>();
        cutoffs.put(8.0, 0);
        cutoffs.put(4.0, 1);
        cutoffs.put(3.0, 2);
        cutoffs.put(2.0, 3);
        cutoffs.put(1.5, 4);
        cutoffs.put(1.0, 5);
        cutoffs.put(0.5, 6);
        cutoffs.put(0.0, 7);

        int g = 0;
        for (double key : keys) {
            if (Math.abs(V) > key) {
                g = cutoffs.get(key);
                break;
            }
        }
        this.setGain(channelName, g, true);
        return g;
    }

    private void autoRangeScope(double tg) {
        Map<String, double[]> tmp = this.captureTwo(1000, tg, null, false);
        double[] x = tmp.get("x");
        double[] y1 = tmp.get("y1");
        double[] y2 = tmp.get("y2");
        for (int i = 0; i < y1.length; i++) {
            y1[i] = Math.abs(y1[i]);
            y2[i] = Math.abs(y2[i]);
        }
        this.autoSelectRange("CH1", max(y1));
        this.autoSelectRange("CH2", max(y2));

    }

    /**
     * Return the voltage on the selected channel
     *
     * @param channelName : 'CH1','CH2','CH3','MIC','IN1','RES','V+'
     * @param sample      Samples to average
     * @return Voltage on the selected channel
     */
    private double getAverageVoltage(String channelName, Integer sample) {
        if (sample == null) sample = 1;
        PolynomialFunction poly;
        double sum = 0;
        poly = analogInputSources.get(channelName).calPoly12;
        ArrayList<Double> vals = new ArrayList<>();
        for (int i = 0; i < sample; i++) {
            vals.add(getRawAverageVoltage(channelName));
        }
        for (int j = 0; j < vals.size(); j++) {
            sum = sum + poly.value(vals.get(j));
        }
        return sum / 2 * vals.size();
    }

    private double getRawAverageVoltage(String channelName) {
        try {
            int chosa = this.calcCHOSA(channelName);
            mPacketHandler.sendByte(mCommandsProto.ADC);
            mPacketHandler.sendByte(mCommandsProto.GET_VOLTAGE_SUMMED);
            mPacketHandler.sendByte(chosa);
            int vSum = mPacketHandler.getVoltageSummation();
            mPacketHandler.getAcknowledgement();
            return vSum / 16.0;
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            Log.e(TAG, "Error in getRawAverageVoltage");
        }
        return 0;
    }

    /**
     * Fetches a section of the ADC hardware buffer
     *
     * @param startingPosition Starting index
     * @param totalPoints      Total points to fetch
     */
    private void fetchBuffer(int startingPosition, int totalPoints) {
        startingPosition = 0;
        totalPoints = 100;
        try {
            mPacketHandler.sendByte(mCommandsProto.COMMON);
            mPacketHandler.sendByte(mCommandsProto.RETRIEVE_BUFFER);
            mPacketHandler.sendInt(startingPosition);
            mPacketHandler.sendInt(totalPoints);
            for (int i = 0; i < totalPoints; i++) {
                this.buffer[i] = mPacketHandler.getInt();
            }
            mPacketHandler.getAcknowledgement();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Error in fetching buffer");
        }
    }

    /**
     * Clears a section of the ADC hardware buffer
     *
     * @param startingPosition Starting index
     * @param totalPoints      Total points to fetch
     */
    private void clearBuffer(int startingPosition, int totalPoints) {
        try {
            mPacketHandler.sendByte(mCommandsProto.COMMON);
            mPacketHandler.sendByte(mCommandsProto.CLEAR_BUFFER);
            mPacketHandler.sendInt(startingPosition);
            mPacketHandler.sendInt(totalPoints);
            mPacketHandler.getAcknowledgement();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Error in clearing buffer");
        }
    }

    /**
     * Fill a section of the ADC hardware buffer with data
     *
     * @param startingPosition Starting index
     * @param pointArray       Total points to fetch
     */
    private void fillBuffer(int startingPosition, int[] pointArray) {
        try {
            mPacketHandler.sendByte(mCommandsProto.COMMON);
            mPacketHandler.sendByte(mCommandsProto.FILL_BUFFER);
            mPacketHandler.sendInt(startingPosition);
            mPacketHandler.sendInt(pointArray.length);
            for (int aPointArray : pointArray) {
                mPacketHandler.sendInt(aPointArray);
            }
            mPacketHandler.getAcknowledgement();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Error in filling Buffer");
        }

    }

    /**
     * Instruct the ADC to start streaming 8-bit data. use stop_streaming to stop
     *
     * @param tg      Timegap. 250KHz clock
     * @param channel Channel 'CH1'... 'CH9','IN1','RES'
     * @throws IOException
     */
    private void startStreaming(int tg, String channel) throws IOException {
        if (this.streaming)
            this.stopStreaming();
        try {
            mPacketHandler.sendByte(mCommandsProto.ADC);
            mPacketHandler.sendByte(mCommandsProto.START_ADC_STREAMING);
            mPacketHandler.sendByte(this.calcCHOSA(channel));
            mPacketHandler.sendInt(tg);
            this.streaming = true;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Start streaming failed");
        }
    }

    /**
     * Instruct the ADC to stop streaming data
     *
     * @throws IOException
     */
    private void stopStreaming() throws IOException {
        if (this.streaming) {
            mPacketHandler.sendByte(mCommandsProto.STOP_STREAMING);
            byte[] data = new byte[2000];
            mPacketHandler.read(data, 2000);
            //mPacketHandler.flush(); flush not implemented
        } else
            Log.e(TAG, "Not streaming");
        this.streaming = false;
    }

    public void setDataSplitting(int dataSplitting) {
        this.dataSplitting = dataSplitting;
    }

    /**
     * Checks if PSLab device is found
     *
     * @return true is device found; false otherwise
     */
    public boolean isDeviceFound() {
        return mCommunicationHandler.isDeviceFound();
    }

    /**
     * Checks if PSLab device is connected
     *
     * @return true is device is connected; false otherwise
     */
    public boolean isConnected() {
        return mCommunicationHandler.isConnected();
    }





    /* DIGITAL SECTION */

    public Integer calculateDigitalChannel(String name) {
        if (Arrays.asList(DigitalChannel.digitalChannelNames).contains(name))
            return Arrays.asList(DigitalChannel.digitalChannelNames).indexOf(name);
        else {
            Log.v(TAG, "Invalid channel " + name + " , selecting LA1 instead ");
            return null;
        }
    }

    private Double getHighFrequencyBackup(String pin) {
        try {
            mPacketHandler.sendByte(mCommandsProto.COMMON);
            mPacketHandler.sendByte(mCommandsProto.GET_HIGH_FREQUENCY);
            mPacketHandler.sendByte(this.calculateDigitalChannel(pin));
            int scale = mPacketHandler.getByte();
            long value = mPacketHandler.getLong();
            mPacketHandler.getAcknowledgement();
            return scale * value / 1.0e-1;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Retrieves the frequency of the signal connected to LA1. For frequencies > 1MHz
     * Also good for lower frequencies, but avoid using it since the oscilloscope cannot be used simultaneously due to hardware limitations.
     * The input frequency is fed to a 32 bit counter for a period of 100mS.
     * The value of the counter at the end of 100mS is used to calculate the frequency.
     *
     * @param pin The input pin to measure frequency from : ['LA1','LA2','LA3','LA4','RES','EXT','FRQ']
     * @return frequency
     */
    public Double getHighFrequency(String pin) {
        /*
        Retrieves the frequency of the signal connected to LA1. for frequencies > 1MHz
		also good for lower frequencies, but avoid using it since
		the oscilloscope cannot be used simultaneously due to hardware limitations.
		The input frequency is fed to a 32 bit counter for a period of 100mS.
		The value of the counter at the end of 100mS is used to calculate the frequency.
        */
        try {
            mPacketHandler.sendByte(mCommandsProto.COMMON);
            mPacketHandler.sendByte(mCommandsProto.GET_ALTERNATE_HIGH_FREQUENCY);
            mPacketHandler.sendByte(this.calculateDigitalChannel(pin));
            int scale = mPacketHandler.getByte();
            long value = mPacketHandler.getLong();
            mPacketHandler.getAcknowledgement();
            return scale * value / 1.0e-1;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Frequency measurement on IDx.
     * Measures time taken for 16 rising edges of input signal.
     * Returns the frequency in Hertz
     *
     * @param channel The input to measure frequency from. ['LA1','LA2','LA3','LA4','RES','EXT','FRQ']
     * @param timeout This is a blocking call which will wait for one full wavelength before returning the calculated frequency. Use the timeout option if you're unsure of the input signal. Returns 0 if timed out
     * @return frequency
     */
    public Double getFrequency(String channel, Integer timeout) {
        /*
        Frequency measurement on IDx.
		Measures time taken for 16 rising edges of input signal.
		returns the frequency in Hertz
        */
        if (channel == null) channel = "FRQ";
        if (timeout == null) timeout = 2;
        try {
            mPacketHandler.sendByte(mCommandsProto.COMMON);
            mPacketHandler.sendByte(mCommandsProto.GET_FREQUENCY);
            int timeoutMSB = ((int) (timeout * 64e6)) >> 16;
            mPacketHandler.sendInt(timeoutMSB);
            mPacketHandler.sendByte(this.calculateDigitalChannel(channel));
            mPacketHandler.waitForData(); // todo : complete "waitForData"
            int tmt = mPacketHandler.getByte();
            long[] x = new long[2];
            x[0] = mPacketHandler.getLong();
            x[1] = mPacketHandler.getLong();
            mPacketHandler.getAcknowledgement();
            if (tmt != 0) return null;
            if ((x[1] - x[0]) != 0)
                return 16 * 64e6 / (x[1] - x[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Stores a list of rising edges that occurred within the timeout period.
     *
     * @param channel   The input to measure time between two rising edges.['LA1','LA2','LA3','LA4','RES','EXT','FRQ']
     * @param skipCycle Number of points to skip. eg. Pendulums pass through light barriers twice every cycle. SO 1 must be skipped
     * @param timeout   Number of seconds to wait for datapoints. (Maximum 60 seconds)
     * @return
     */
    public Double r2rTime(String channel, Integer skipCycle, Integer timeout) {
        /*
        Return a list of rising edges that occured within the timeout period.
        */
        if (skipCycle == null) skipCycle = 0;
        if (timeout == null) timeout = 5;
        if (timeout > 60) timeout = 60;
        this.startOneChannelLA(channel, 3, null, 0);
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < timeout) {
            LinkedHashMap<String, Integer> initialStates = this.getLAInitialStates();
            if (initialStates.get("A") == this.MAX_SAMPLES / 4)
                initialStates.put("A", 0);
            if (initialStates.get("A") >= skipCycle + 2) {
                long[] data = this.fetchLongDataFromLA(initialStates.get("A"), 1);
                LinkedHashMap<String, Integer> tempMap = new LinkedHashMap<>();
                tempMap.put("LA1", initialStates.get("LA1"));
                tempMap.put("LA2", initialStates.get("LA2"));
                tempMap.put("LA3", initialStates.get("LA3"));
                tempMap.put("LA4", initialStates.get("LA4"));
                tempMap.put("RES", initialStates.get("RES"));
                double[] doubleData = new double[data.length];
                for (int i = 0; i < data.length; i++) {
                    doubleData[i] = data[i];
                }
                this.dChannels.get(0).loadData(tempMap, doubleData);
                return 1e-6 * (this.dChannels.get(0).timestamps[skipCycle + 1] - this.dChannels.get(0).timestamps[0]);
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Stores a list of falling edges that occured within the timeout period.
     *
     * @param channel   The input to measure time between two falling edges.['LA1','LA2','LA3','LA4','RES','EXT','FRQ']
     * @param skipCycle Number of points to skip. eg. Pendulums pass through light barriers twice every cycle. SO 1 must be skipped
     * @param timeout   Number of seconds to wait for datapoints. (Maximum 60 seconds)
     * @return
     */
    public Double f2fTime(String channel, Integer skipCycle, Integer timeout) {
        /*
        Return a list of falling edges that occured within the timeout period.
        */
        if (skipCycle == null) skipCycle = 0;
        if (timeout == null) timeout = 5;
        if (timeout > 60) timeout = 60;
        this.startOneChannelLA(channel, 2, null, 0);
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < timeout) {
            LinkedHashMap<String, Integer> initialStates = this.getLAInitialStates();
            if (initialStates.get("A") == this.MAX_SAMPLES / 4)
                initialStates.put("A", 0);
            if (initialStates.get("A") >= skipCycle + 2) {
                long[] data = this.fetchLongDataFromLA(initialStates.get("A"), 1);
                LinkedHashMap<String, Integer> tempMap = new LinkedHashMap<>();
                tempMap.put("LA1", initialStates.get("LA1"));
                tempMap.put("LA2", initialStates.get("LA2"));
                tempMap.put("LA3", initialStates.get("LA3"));
                tempMap.put("LA4", initialStates.get("LA4"));
                tempMap.put("RES", initialStates.get("RES"));
                double[] doubleData = new double[data.length];
                for (int i = 0; i < data.length; i++) {
                    doubleData[i] = data[i];
                }
                this.dChannels.get(0).loadData(tempMap, doubleData);
                return 1e-6 * (this.dChannels.get(0).timestamps[skipCycle + 1] - this.dChannels.get(0).timestamps[0]);
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Measures time intervals between two logic level changes on any two digital inputs(both can be the same) and returns the calculated time.
     * For example, one can measure the time interval between the occurrence of a rising edge on LA1, and a falling edge on LA3.
     * If the returned time is negative, it simply means that the event corresponding to channel2 occurred first.
     *
     * @param channel1 The input pin to measure first logic level change
     * @param channel2 The input pin to measure second logic level change -['LA1','LA2','LA3','LA4','RES','EXT','FRQ']
     * @param edge1    The type of level change to detect in order to start the timer - ['rising', 'falling', 'four rising edges']
     * @param edge2    The type of level change to detect in order to stop the timer - ['rising', 'falling', 'four rising edges']
     * @param timeout  Use the timeout option if you're unsure of the input signal time period. Returns -1 if timed out
     * @return time
     */
    public Double measureInterval(String channel1, String channel2, String edge1, String edge2, Float timeout) {
        /*
        Measures time intervals between two logic level changes on any two digital inputs(both can be the same)
		For example, one can measure the time interval between the occurence of a rising edge on LA1, and a falling edge on LA3.
		If the returned time is negative, it simply means that the event corresponding to channel2 occurred first.
		Returns the calculated time
        */

        if (timeout == null) timeout = 0.1f;
        try {
            mPacketHandler.sendByte(mCommandsProto.TIMING);
            mPacketHandler.sendByte(mCommandsProto.INTERVAL_MEASUREMENTS);
            int timeoutMSB = ((int) (timeout * 64e6)) >> 16;
            mPacketHandler.sendInt(timeoutMSB);
            mPacketHandler.sendByte(this.calculateDigitalChannel(channel1) | (this.calculateDigitalChannel(channel2) << 4));
            int params = 0;
            if ("rising".equals(edge1))
                params |= 3;
            else if ("falling".equals(edge1))
                params |= 2;
            else
                params |= 4;

            if ("rising".equals(edge2))
                params |= 3 << 3;
            else if ("falling".equals(edge2))
                params |= 2 << 3;
            else
                params |= 4 << 3;

            mPacketHandler.sendByte(params);
            long A = mPacketHandler.getLong();
            long B = mPacketHandler.getLong();
            int tmt = mPacketHandler.getInt();
            mPacketHandler.getAcknowledgement();
            if (tmt > timeoutMSB || B == 0) return null;

            return (B - A + 20) / 64e6;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Duty cycle measurement on channel. Returns wavelength(seconds), and length of first half of pulse(high time)
     * Low time = (wavelength - high time)
     *
     * @param channel The input pin to measure wavelength and high time.['LA1','LA2','LA3','LA4','RES','EXT','FRQ']
     * @param timeout Use the timeout option if you're unsure of the input signal time period. Returns 0 if timed out
     * @return Wavelength, Duty cycle
     */
    public double[] dutyCycle(String channel, Double timeout) {
        /*
        duty cycle measurement on channel
		returns wavelength(seconds), and length of first half of pulse(high time)
		low time = (wavelength - high time)
        */
        if (channel == null) channel = "LA1";
        if (timeout == null) timeout = 1.;
        Map<String, double[]> data = this.measureMultipleDigitalEdges(channel, channel, "rising", "falling", 2, 2, timeout, null, true);
        double[] retData = new double[2];
        if (data != null) {
            double[] x = data.get("CHANNEL1");
            double[] y = data.get("CHANNEL2");
            if (x != null && y != null) {  // Both timers registered something. did not timeout
                if (y[0] > 0) {
                    retData[0] = y[0];
                    retData[1] = x[1];
                } else {
                    if (y[1] > x[1]) {
                        retData[0] = -1;
                        retData[1] = -1;
                        return retData;
                    }
                    retData[0] = y[1];
                    retData[1] = x[1];
                }
                double[] params = new double[2];
                params[0] = retData[1];
                params[1] = retData[0] / retData[1];
                if (params[1] > 0.5) {
                    Log.v(TAG, Arrays.toString(x) + "\n" + Arrays.toString(y) + "\n" + Arrays.toString(retData));
                }
                return params;
            }
        }
        retData[0] = -1;
        retData[1] = -1;
        return retData;
    }

    /**
     * Duty cycle measurement on channel. Returns wavelength(seconds), and length of first half of pulse(high time)
     * Low time = (wavelength - high time)
     *
     * @param channel   The input pin to measure wavelength and high time.['LA1','LA2','LA3','LA4','RES','EXT','FRQ']
     * @param pulseType Type of pulse to detect. May be 'HIGH' or 'LOW'
     * @param timeout   Use the timeout option if you're unsure of the input signal time period. Returns 0 if timed out
     * @return Pulse width
     */
    public Double pulseTime(String channel, String pulseType, Double timeout) {
        if (channel == null) channel = "LA1";
        if (pulseType == null) pulseType = "LOW";
        if (timeout == null) timeout = 0.1;

        Map<String, double[]> data = this.measureMultipleDigitalEdges(channel, channel, "rising", "falling", 2, 2, timeout, null, true);
        if (data != null) {
            double[] x = data.get("CHANNEL1");
            double[] y = data.get("CHANNEL2");
            if (x != null && y != null) { // Both timers registered something. did not timeout
                if (y[0] > 0) {
                    if ("HIGH".equals(pulseType))
                        return y[0];
                    else if ("LOW".equals(pulseType)) {
                        return x[1] - y[0];
                    }
                } else {
                    if ("HIGH".equals(pulseType))
                        return y[1];
                    else if ("LOW".equals(pulseType)) {
                        return Math.abs(y[0]);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Measures a set of timestamped logic level changes(Type can be selected) from two different digital inputs.
     *
     * @param channel1  The input pin to measure first logic level change
     * @param channel2  The input pin to measure second logic level change -['LA1','LA2','LA3','LA4','RES','EXT','FRQ']
     * @param edgeType1 The type of level change that should be recorded - ['rising', 'falling', 'four rising edges(default)']
     * @param edgeType2 The type of level change that should be recorded - ['rising', 'falling', 'four rising edges(default)']
     * @param points1   Number of data points to obtain for input 1 (Max 4)
     * @param points2   Number of data points to obtain for input 2 (Max 4)
     * @param timeout   Use the timeout option if you're unsure of the input signal time period. returns -1 if timed out
     * @param SQR1      Set the state of SQR1 output(LOW or HIGH) and then start the timer. eg. SQR1 = 'LOW'
     * @param zero      subtract the timestamp of the first point from all the others before returning. Default: True
     * @return time
     */
    private Map<String, double[]> measureMultipleDigitalEdges(String channel1, String channel2, String edgeType1, String edgeType2, int points1, int points2, Double timeout, String SQR1, Boolean zero) {

        if (timeout == null) timeout = 0.1;
        try {
            mPacketHandler.sendByte(mCommandsProto.TIMING);
            mPacketHandler.sendByte(mCommandsProto.TIMING_MEASUREMENTS);
            int timeoutMSB = ((int) (timeout * 64e6)) >> 16;
            mPacketHandler.sendInt(timeoutMSB);
            mPacketHandler.sendByte(this.calculateDigitalChannel(channel1) | (this.calculateDigitalChannel(channel2) << 4));
            int params = 0;
            if ("rising".equals(edgeType1))
                params |= 3;
            else if ("falling".equals(edgeType1))
                params |= 2;
            else
                params |= 4;

            if ("rising".equals(edgeType2))
                params |= 3 << 3;
            else if ("falling".equals(edgeType2))
                params |= 2 << 3;
            else
                params |= 4 << 3;
            if (SQR1 != null) {
                params |= (1 << 6);
                if ("HIGH".equals(SQR1))
                    params |= (1 << 7);
            }
            mPacketHandler.sendByte(params);
            if (points1 > 4) points1 = 4;
            if (points2 > 4) points2 = 4;
            mPacketHandler.sendByte(points1 | (points2 << 4));

            //mPacketHandler.waitForData(timeout); todo : complete waitForData in PacketHandler.java
            long[] A = new long[points1];
            long[] B = new long[points2];
            for (int i = 0; i < points1; i++)
                A[i] = mPacketHandler.getLong();
            for (int i = 0; i < points2; i++)
                B[i] = mPacketHandler.getLong();
            int tmt = mPacketHandler.getInt();
            mPacketHandler.getAcknowledgement();
            Map<String, double[]> retData = new HashMap<>();
            if (tmt > timeoutMSB) {
                retData.put("CHANNEL1", null);
                retData.put("CHANNEL2", null);
                return retData;
            }
            if (zero == null) zero = true;
            double[] A1 = new double[A.length];
            double[] B1 = new double[B.length];
            if (zero) {
                for (int i = 0; i < A.length; i++) {
                    A[i] -= A[0];
                    A1[i] = A[i] / 64e6;
                }
                for (int i = 0; i < B.length; i++) {
                    B[i] -= B[0];
                    B1[i] = B[i] / 64e6;
                }
            } else {
                for (int i = 0; i < A.length; i++) {
                    A1[i] = A[i] / 64e6;
                }
                for (int i = 0; i < B.length; i++) {
                    B1[i] = B[i] / 64e6;
                }
            }
            retData.put("CHANNEL1", A1);
            retData.put("CHANNEL2", B1);
            return retData;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Log timestamps of rising/falling edges on one digital input
     *
     * @param waitingTime    Total time to allow the logic analyzer to collect data. This is implemented using a simple sleep routine, so if large delays will be involved, refer to startOneChannelLA() to start the acquisition, and fetchLAChannels() to retrieve data from the hardware after adequate time. The retrieved data is stored in the array self.dchans[0].timestamps.
     * @param aquireChannel  LA1',...,'LA4'
     * @param triggerChannel LA1',...,'LA4'
     * @param aquireMode     EVERY_SIXTEENTH_RISING_EDGE = 5
     *                       EVERY_FOURTH_RISING_EDGE = 4
     *                       EVERY_RISING_EDGE = 3
     *                       EVERY_FALLING_EDGE = 2
     *                       EVERY_EDGE = 1
     *                       DISABLED = 0
     *                       default = 3
     * @param triggerMode    same as aquireMode. default_value : 3
     * @return
     */
    public double[] captureEdgesOne(Integer waitingTime, String aquireChannel, String triggerChannel, Integer aquireMode, Integer triggerMode) {
        /*
        Log timestamps of rising/falling edges on one digital input
        */
        if (waitingTime == null) waitingTime = 1;
        if (aquireChannel == null) aquireChannel = "LA1";
        if (triggerChannel == null) triggerChannel = aquireChannel;
        if (aquireMode == null) aquireMode = 3;
        if (triggerMode == null) triggerMode = 3;
        this.startOneChannelLA(aquireChannel, aquireMode, triggerChannel, triggerMode);
        try {
            Thread.sleep(waitingTime * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LinkedHashMap<String, Integer> data = this.getLAInitialStates();
        long[] temp = this.fetchLongDataFromLA(data.get("A"), 1);
        double[] retData = new double[temp.length];
        for (int i = 0; i < temp.length; i++) {
            retData[i] = temp[i] / 64e6;
        }
        return retData;
    }

    /**
     * Start logging timestamps of rising/falling edges on LA1
     *
     * @param trigger         Bool . Enable edge trigger on LA1. use keyword argument edge = 'rising' or 'falling'
     * @param channel         ['LA1','LA2','LA3','LA4','RES','EXT','FRQ']
     * @param maximumTime     Total time to sample. If total time exceeds 67 seconds, a prescaler will be used in the reference clock.
     * @param triggerChannels array of digital input names that can trigger the acquisition. Eg, trigger = ['LA1','LA2','LA3'] will triggger when a logic change specified by the keyword argument 'edge' occurs on either or the three specified trigger inputs.
     * @param edge            'rising' or 'falling' . trigger edge type for trigger_channels.
     */
    public void startOneChannelLABackup(Integer trigger, String channel, Integer maximumTime, ArrayList<String> triggerChannels, String edge) {
        /*
        start logging timestamps of rising/falling edges on LA1
        */
        try {
            this.clearBuffer(0, this.MAX_SAMPLES / 2);
            mPacketHandler.sendByte(mCommandsProto.TIMING);
            mPacketHandler.sendByte(mCommandsProto.START_ONE_CHAN_LA);
            mPacketHandler.sendInt(this.MAX_SAMPLES / 4);
            if (triggerChannels != null & (trigger & 1) != 0) {
                if (triggerChannels.contains("LA1")) trigger |= (1 << 4);
                if (triggerChannels.contains("LA2")) trigger |= (1 << 5);
                if (triggerChannels.contains("LA3")) trigger |= (1 << 6);
            } else {
                trigger |= 1 << (this.calculateDigitalChannel(channel) + 4);
            }
            if ("rising".equals(edge)) trigger |= 2;
            trigger |= (this.calculateDigitalChannel(channel) << 2);

            mPacketHandler.sendByte(trigger);
            mPacketHandler.getAcknowledgement();
            this.digitalChannelsInBuffer = 1;
            for (DigitalChannel dChan : this.dChannels) {
                dChan.prescalar = 0;
                dChan.dataType = "long";
                dChan.length = this.MAX_SAMPLES / 4;
                dChan.maxTime = (int) (maximumTime * 1e6);
                dChan.mode = DigitalChannel.EVERY_EDGE;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Start logging timestamps of rising/falling edges on LA1.
     *
     * @param channel        ['LA1','LA2','LA3','LA4','RES','EXT','FRQ']
     * @param channelMode    acquisition mode default value: 1(EVERY_EDGE)
     *                       - EVERY_SIXTEENTH_RISING_EDGE = 5
     *                       - EVERY_FOURTH_RISING_EDGE    = 4
     *                       - EVERY_RISING_EDGE           = 3
     *                       - EVERY_FALLING_EDGE          = 2
     *                       - EVERY_EDGE                  = 1
     *                       - DISABLED                    = 0
     * @param triggerChannel ['LA1','LA2','LA3','LA4','RES','EXT','FRQ']
     * @param triggerMode    1=Falling edge, 0=Rising Edge, -1=Disable Trigger
     */
    public void startOneChannelLA(String channel, Integer channelMode, String triggerChannel, Integer triggerMode) {
        if (channel == null) channel = "LA1";
        if (channelMode == null) channelMode = 1;
        if (triggerChannel == null) triggerChannel = "LA1";
        if (triggerMode == null) triggerMode = 3;
        try {
            this.clearBuffer(0, this.MAX_SAMPLES / 2);
            mPacketHandler.sendByte(mCommandsProto.TIMING);
            mPacketHandler.sendByte(mCommandsProto.START_ALTERNATE_ONE_CHAN_LA);
            mPacketHandler.sendInt(this.MAX_SAMPLES / 4);
            int aqChannel = this.calculateDigitalChannel(channel);
            int aqMode = channelMode;
            int trChannel = this.calculateDigitalChannel(triggerChannel);
            int trMode = triggerMode;
            mPacketHandler.sendByte((aqChannel << 4) | aqMode);
            mPacketHandler.sendByte((trChannel << 4) | trMode);
            mPacketHandler.getAcknowledgement();
            this.digitalChannelsInBuffer = 1;
            this.dChannels.get(aqChannel).prescalar = 0;
            this.dChannels.get(aqChannel).dataType = "long";
            this.dChannels.get(aqChannel).length = this.MAX_SAMPLES / 4;
            this.dChannels.get(aqChannel).maxTime = (int) (67 * 1e6);
            this.dChannels.get(aqChannel).mode = channelMode;
            this.dChannels.get(aqChannel).channelName = channel;
            if (trMode == 3 || trMode == 4 || trMode == 5)
                this.dChannels.get(aqChannel).initialStateOverride = 2;
            else if (trMode == 2)
                this.dChannels.get(0).initialStateOverride = 1;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Start logging timestamps of rising/falling edges on LA1,LA2
     *
     * @param channels       Channels to acquire data from . default ['LA1','LA2']
     * @param modes          modes for each channel. Array . default value: [1,1]
     *                       - EVERY_SIXTEENTH_RISING_EDGE = 5
     *                       - EVERY_FOURTH_RISING_EDGE    = 4
     *                       - EVERY_RISING_EDGE           = 3
     *                       - EVERY_FALLING_EDGE          = 2
     *                       - EVERY_EDGE                  = 1
     *                       - DISABLED                    = 0
     * @param maximumTime    Total time to sample. If total time exceeds 67 seconds, a prescaler will be used in the reference clock
     * @param trigger        Bool . Enable rising edge trigger on LA1
     * @param edge           'rising' or 'falling' . trigger edge type for trigger_channels.
     * @param triggerChannel channel to trigger on . Any digital input. default CH1
     */
    public void startTwoChannelLA(ArrayList<String> channels, ArrayList<Integer> modes, Integer maximumTime, Integer trigger, String edge, String triggerChannel) {
        if (maximumTime == null) maximumTime = 67;
        if (trigger == null) trigger = 0;
        if (edge == null) edge = "rising";
        if (channels == null) {
            channels = new ArrayList<>();
            channels.add("LA1");
            channels.add("LA2");
        }
        if (modes == null) {
            modes = new ArrayList<>();
            modes.add(1);
            modes.add(1);
        }
        int[] chans = new int[]{this.calculateDigitalChannel(channels.get(0)), this.calculateDigitalChannel(channels.get(1))};
        if (triggerChannel == null) triggerChannel = channels.get(0);
        if (trigger != 0) {
            trigger = 1;
            if ("falling".equals(edge)) trigger |= 2;
            trigger |= (this.calculateDigitalChannel(triggerChannel) << 4);
        }
        try {
            this.clearBuffer(0, this.MAX_SAMPLES);
            mPacketHandler.sendByte(mCommandsProto.TIMING);
            mPacketHandler.sendByte(mCommandsProto.START_TWO_CHAN_LA);
            mPacketHandler.sendInt(this.MAX_SAMPLES / 4);
            mPacketHandler.sendByte(trigger);
            mPacketHandler.sendByte((modes.get(1) << 4) | modes.get(0));
            mPacketHandler.sendByte((chans[1] << 4) | chans[0]);
            mPacketHandler.getAcknowledgement();
            for (int i = 0; i < 2; i++) {
                DigitalChannel temp = this.dChannels.get(i);
                temp.prescalar = 0;
                temp.length = this.MAX_SAMPLES / 4;
                temp.dataType = "long";
                temp.maxTime = (int) (maximumTime * 1e6);
                temp.mode = modes.get(i);
                temp.channelNumber = chans[i];
                temp.channelName = channels.get(i);
            }
            this.digitalChannelsInBuffer = 2;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Start logging timestamps of rising/falling edges on LA1,LA2,LA3
     *
     * @param modes          modes for each channel. Array. default value: [1,1,1]
     *                       - EVERY_SIXTEENTH_RISING_EDGE = 5
     *                       - EVERY_FOURTH_RISING_EDGE    = 4
     *                       - EVERY_RISING_EDGE           = 3
     *                       - EVERY_FALLING_EDGE          = 2
     *                       - EVERY_EDGE                  = 1
     *                       - DISABLED                    = 0
     * @param triggerChannel ['LA1','LA2','LA3','LA4','RES','EXT','FRQ']
     * @param triggerMode    same as modes(previously documented keyword argument)
     *                       default_value : 3
     */
    public void startThreeChannelLA(ArrayList<Integer> modes, String triggerChannel, Integer triggerMode) {
        if (modes == null) {
            modes = new ArrayList<>();
            modes.add(1);
            modes.add(1);
            modes.add(1);
        }
        if (triggerChannel == null) {
            triggerChannel = "LA1";
        }
        if (triggerMode == null) {
            triggerMode = 3;
        }
        try {
            this.clearBuffer(0, this.MAX_SAMPLES);
            mPacketHandler.sendByte(mCommandsProto.TIMING);
            mPacketHandler.sendByte(mCommandsProto.START_THREE_CHAN_LA);
            mPacketHandler.sendInt(this.MAX_SAMPLES / 4);
            int trChan = this.calculateDigitalChannel(triggerChannel);
            int trMode = triggerMode;

            mPacketHandler.sendInt(modes.get(0) | (modes.get(1) << 4) | (modes.get(2) << 8));
            mPacketHandler.sendByte((trChan << 4) | trMode);
            mPacketHandler.getAcknowledgement();
            this.digitalChannelsInBuffer = 3;

            for (int i = 0; i < 3; i++) {
                DigitalChannel temp = this.dChannels.get(i);
                temp.prescalar = 0;
                temp.length = this.MAX_SAMPLES / 4;
                temp.dataType = "int";
                temp.maxTime = (int) (1e3);
                temp.mode = modes.get(i);
                temp.channelName = DigitalChannel.digitalChannelNames[i];
                if (trMode == 3 || trMode == 4 || trMode == 5) {
                    temp.initialStateOverride = 2;
                } else if (trMode == 2) {
                    temp.initialStateOverride = 1;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Four channel Logic Analyzer.
     * Start logging timestamps from a 64MHz counter to record level changes on LA1,LA2,LA3,LA4.
     * triggerChannel[0] -> LA1
     * triggerChannel[1] -> LA2
     * triggerChannel[2] -> LA3
     *
     * @param trigger        Bool. Enable rising edge trigger on LA1.
     * @param maximumTime    Maximum delay expected between two logic level changes.
     *                       If total time exceeds 1 mS, a prescaler will be used in the reference clock.
     *                       However, this only refers to the maximum time between two successive level changes. If a delay larger
     *                       than .26 S occurs, it will be truncated by modulo .26 S.
     *                       If you need to record large intervals, try single channel/two channel modes which use 32 bit counters
     *                       capable of time interval up to 67 seconds.
     * @param modes          modes for each channel. List with four elements\n
     *                       default values: [1,1,1,1]
     *                       - EVERY_SIXTEENTH_RISING_EDGE = 5
     *                       - EVERY_FOURTH_RISING_EDGE    = 4
     *                       - EVERY_RISING_EDGE           = 3
     *                       - EVERY_FALLING_EDGE          = 2
     *                       - EVERY_EDGE                  = 1
     *                       - DISABLED                    = 0
     * @param edge           'rising' or 'falling'. Trigger edge type for trigger_channels.
     * @param triggerChannel ['LA1','LA2','LA3','LA4','RES','EXT','FRQ']
     */
    public void startFourChannelLA(Integer trigger, Double maximumTime, ArrayList<Integer> modes, String edge, ArrayList<Boolean> triggerChannel) {
        if (trigger == null) trigger = 1;
        if (maximumTime == null) maximumTime = 0.001;
        if (modes == null) {
            modes = new ArrayList<>();
            modes.add(1);
            modes.add(1);
            modes.add(1);
        }
        if (edge == null) edge = "0";
        this.clearBuffer(0, this.MAX_SAMPLES);
        int prescale = 0;
        try {
            mPacketHandler.sendByte(mCommandsProto.TIMING);
            mPacketHandler.sendByte(mCommandsProto.START_FOUR_CHAN_LA);
            mPacketHandler.sendInt(this.MAX_SAMPLES / 4);
            mPacketHandler.sendInt(modes.get(0) | (modes.get(1) << 4) | (modes.get(2) << 8) | (modes.get(3) << 12));
            mPacketHandler.sendByte(prescale);
            int triggerOptions = 0;
            if (triggerChannel.get(0)) triggerOptions |= 4;
            if (triggerChannel.get(1)) triggerOptions |= 8;
            if (triggerChannel.get(2)) triggerOptions |= 16;
            if (triggerOptions == 0)
                triggerOptions |= 4;  // Select one trigger channel(LA1) if none selected
            if ("rising".equals(edge)) triggerOptions |= 2;
            trigger |= triggerOptions;
            mPacketHandler.sendByte(trigger);
            mPacketHandler.getAcknowledgement();
            this.digitalChannelsInBuffer = 4;
            int i = 0;
            for (DigitalChannel dChan : this.dChannels) {
                dChan.prescalar = prescale;
                dChan.dataType = "int";
                dChan.length = this.MAX_SAMPLES / 4;
                dChan.maxTime = (int) (maximumTime * 1e6);
                dChan.mode = modes.get(i);
                dChan.channelName = DigitalChannel.digitalChannelNames[i];
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Fetches the initial states of digital inputs that were recorded right before the Logic analyzer was started,
     * and the total points each channel recorded.
     *
     * @return CH1 progress,CH2 progress,CH3 progress,CH4 progress,[LA1,LA2,LA3,LA4]. eg. [1,0,1,1]
     */
    public LinkedHashMap<String, Integer> getLAInitialStates() {
        try {
            mPacketHandler.sendByte(mCommandsProto.TIMING);
            mPacketHandler.sendByte(mCommandsProto.GET_INITIAL_DIGITAL_STATES);
            int initial = mPacketHandler.getInt();
            int A = (mPacketHandler.getInt() - initial) / 2;
            int B = (mPacketHandler.getInt() - initial) / 2;
            int C = (mPacketHandler.getInt() - initial) / 2;
            int D = (mPacketHandler.getInt() - initial) / 2;
            int s = mPacketHandler.getByte();
            int sError = mPacketHandler.getByte();
            mPacketHandler.getAcknowledgement();

            if (A == 0) A = this.MAX_SAMPLES / 4;
            if (B == 0) B = this.MAX_SAMPLES / 4;
            if (C == 0) C = this.MAX_SAMPLES / 4;
            if (D == 0) D = this.MAX_SAMPLES / 4;

            if (A < 0) A = 0;
            if (B < 0) B = 0;
            if (C < 0) C = 0;
            if (D < 0) D = 0;

            LinkedHashMap<String, Integer> retData = new LinkedHashMap<>();
            retData.put("A", A);
            retData.put("B", B);
            retData.put("C", C);
            retData.put("D", D);

            // putting 1 -> true & 0 -> false
            if ((s & 1) != 0)
                retData.put("LA1", 1);
            else
                retData.put("LA1", 0);

            if ((s & 2) != 0)
                retData.put("LA2", 1);
            else
                retData.put("LA2", 0);

            if ((s & 4) != 0)
                retData.put("LA3", 1);
            else
                retData.put("LA3", 0);

            if ((s & 8) != 0)
                retData.put("LA4", 1);
            else
                retData.put("LA4", 0);

            if ((s & 16) != 16)
                retData.put("RES", 1);
            else
                retData.put("RES", 0);

            return retData;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Stop any running logic analyzer function.
     */
    public void stopLA() {
        try {
            mPacketHandler.sendByte(mCommandsProto.TIMING);
            mPacketHandler.sendByte(mCommandsProto.STOP_LA);
            mPacketHandler.getAcknowledgement();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Fetches the data stored by DMA. integer address increments
     *
     * @param bytes   number of readings(integer) to fetch
     * @param channel channel number (1-4)
     * @return array of integer data fetched from Logic Analyser.
     */
    public int[] fetchIntDataFromLA(Integer bytes, Integer channel) {
        if (channel == null) channel = 1;
        try {
            mPacketHandler.sendByte(mCommandsProto.TIMING);
            mPacketHandler.sendByte(mCommandsProto.FETCH_INT_DMA_DATA);
            mPacketHandler.sendInt(bytes);
            mPacketHandler.sendByte(channel - 1);
            byte[] readData = new byte[bytes * 2];
            mPacketHandler.read(readData, bytes * 2);
            Log.v("Bytes Obtained : ", String.valueOf(readData.length));

            if (readData.length > 0) {

                ArrayList<Integer> l = new ArrayList<>();
                for (int i = 0; i < readData.length; i += 1) {
                    l.add((int) readData[i] & 0xff);
                }

                StringBuilder stringBuilder = new StringBuilder();
                int[] timeStamps = new int[(int) readData.length / 2 + 1];
                for (int i = 0; i < (int) (readData.length / 2); i++) {
                    double t = (l.get(i * 2) | (l.get(i * 2 + 1) << 8));
                    timeStamps[i + 1] = (int) t;
                    stringBuilder.append(String.valueOf(t));
                    stringBuilder.append(" ");
                }
                Log.v("Fetched points : ", stringBuilder.toString());
                mPacketHandler.getAcknowledgement();
                Arrays.sort(timeStamps);
                timeStamps[0] = 1;
                return timeStamps;
            } else {
                Log.e("Error : ", "Obtained bytes = 0");
                int[] temp = new int[2501];
                Arrays.fill(temp, 0);
                return temp;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Fetches the data stored by DMA. long address increments.
     *
     * @param bytes   number of readings(long integers) to fetch
     * @param channel channel number (1-2)
     * @return array of long integers data fetched from Logic Analyser.
     */
    public long[] fetchLongDataFromLA(Integer bytes, Integer channel) {
        if (channel == null) channel = 1;
        try {
            mPacketHandler.sendByte(mCommandsProto.TIMING);
            mPacketHandler.sendByte(mCommandsProto.FETCH_LONG_DMA_DATA);
            mPacketHandler.sendInt(bytes);
            mPacketHandler.sendByte(channel - 1);
            byte[] readData = new byte[bytes * 4];
            mPacketHandler.read(readData, bytes * 4);
            mPacketHandler.getAcknowledgement();
            long[] data = new long[bytes];
            for (int i = 0; i < bytes; i++) {
                data[i] = ByteBuffer.wrap(Arrays.copyOfRange(readData, 4 * i, 4 * i + 4)).order(ByteOrder.LITTLE_ENDIAN).getLong();
            }
            // Trimming array data
            int markerA = 0;
            for (int i = 0; i < data.length; i++) {
                if (data[i] != 0) {
                    markerA = i;
                    break;
                }
            }
            int markerB = 0;
            for (int i = data.length - 1; i >= 0; i--) {
                if (data[i] != 0) {
                    markerB = i;
                    break;
                }
            }
            return Arrays.copyOfRange(data, markerA, markerB + 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Reads and stores the channels in this.dChannels.
     *
     * @return true if LA channels fetched successfully.
     */
    public boolean fetchLAChannels() {
        LinkedHashMap<String, Integer> data = this.getLAInitialStates();
        for (int i = 0; i < 4; i++) {
            if (this.dChannels.get(i).channelNumber < this.digitalChannelsInBuffer) {
                this.fetchLAChannel(i, data);
            }
        }
        return true;
    }

    /**
     * @param channelNumber Channel number being used e.g. CH1, CH2, CH3, CH4.
     * @param initialStates State of the digital inputs. returns dictionary with keys 'LA1','LA2','LA3','LA4','RES'
     * @return true if data fetched/loaded successfully.
     */
    public boolean fetchLAChannel(Integer channelNumber, LinkedHashMap<String, Integer> initialStates) {
        DigitalChannel dChan = this.dChannels.get(channelNumber);

        LinkedHashMap<String, Integer> tempMap = new LinkedHashMap<>();
        tempMap.put("LA1", initialStates.get("LA1"));
        tempMap.put("LA2", initialStates.get("LA2"));
        tempMap.put("LA3", initialStates.get("LA3"));
        tempMap.put("LA4", initialStates.get("LA4"));
        tempMap.put("RES", initialStates.get("RES"));

        //  Used LinkedHashMap above (initialStates) in which iteration is done sequentially as <key-value> were inserted
        int i = 0;
        for (Map.Entry<String, Integer> entry : initialStates.entrySet()) {
            if (dChan.channelNumber == i) {
                i = entry.getValue();
                break;
            }
            i++;
        }

        int[] temp = this.fetchIntDataFromLA(i, dChan.channelNumber + 1);
        double[] data = new double[temp.length - 1];
        if (temp[0] == 1) {
            for (int j = 1; j < temp.length; j++) {
                data[j - 1] = temp[j];
            }
        } else {
            Log.e("Error : ", "Can't load data");
            return false;
        }
        dChan.loadData(tempMap, data);

        dChan.generateAxes();
        return true;
    }

    public DigitalChannel getDigitalChannel(int i) {
        return dChannels.get(i);
    }

    /**
     * Gets the state of the digital inputs.
     *
     * @return dictionary with keys 'LA1','LA2','LA3','LA4'.
     */
    public Map<String, Boolean> getStates() {
        try {
            mPacketHandler.sendByte(mCommandsProto.DIN);
            mPacketHandler.sendByte(mCommandsProto.GET_STATES);
            byte state = mPacketHandler.getByte();
            mPacketHandler.getAcknowledgement();
            Map<String, Boolean> states = new LinkedHashMap<>();
            states.put("LA1", ((state & 1) != 0));
            states.put("LA2", ((state & 2) != 0));
            states.put("LA3", ((state & 4) != 0));
            states.put("LA4", ((state & 8) != 0));
            return states;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Fetch the state of given input ID.
     *
     * @param inputID the input channel
     *                'LA1' -> state of LA1
     *                'LA4' -> state of LA4
     * @return the logic level on the specified input (LA1,LA2,LA3, or LA4)
     */
    public Boolean getState(String inputID) {
        return this.getStates().get(inputID);
    }

    /**
     * set the logic level on digital outputs SQR1,SQR2,SQR3,SQR4.
     *
     * @param args SQR1,SQR2,SQR3,SQR4
     *             states(0 or 1)
     */
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

    /**
     * Count pulses on a digital input. Retrieve total pulses using readPulseCount.
     *
     * @param channel The input pin to measure rising edges on : ['LA1','LA2','LA3','LA4','RES','EXT','FRQ']
     */
    public void countPulses(String channel) {
        if (channel == null) channel = "RES";
        try {
            mPacketHandler.sendByte(mCommandsProto.COMMON);
            mPacketHandler.sendByte(mCommandsProto.START_COUNTING);
            mPacketHandler.sendByte(this.calculateDigitalChannel(channel));
            mPacketHandler.getAcknowledgement();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Read pulses counted using a digital input. Call countPulses before using this.
     *
     * @return number of pulse.
     */
    public int readPulseCount() {
        try {
            mPacketHandler.sendByte(mCommandsProto.COMMON);
            mPacketHandler.sendByte(mCommandsProto.FETCH_COUNT);
            int count = mPacketHandler.getVoltageSummation();
            mPacketHandler.getAcknowledgement();
            return 10 * count;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void chargeCap(int state, int t) {
        try {
            mPacketHandler.sendByte(mCommandsProto.ADC);
            mPacketHandler.sendByte(mCommandsProto.SET_CAP);
            mPacketHandler.sendByte(state);
            mPacketHandler.sendInt(t);
            mPacketHandler.getAcknowledgement();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public double[] captureCapacitance(int samples, int timeGap) {
        AnalyticsClass analyticsClass = new AnalyticsClass();
        this.chargeCap(1, 50000);
        Map<String, double[]> data = this.captureFullSpeedHr("CAP", samples, timeGap, Arrays.asList("READ_CAP"));
        double[] x = data.get("x");
        double[] y = data.get("y");
        for (int i = 0; i < x.length; i++) {
            x[i] = x[i] * 1e-6;
        }
        ArrayList<double[]> fitres = analyticsClass.fitExponential(x, y);
        if (fitres != null) {
            // Not return extra data as in python-communication library. Not required at this point.
            return fitres.get(0);
        }
        return null;
    }

    public Double capacitanceViaRCDischarge() {
        double cap = getCapacitorRange()[1];
        double time = 2 * cap * 20e3 * 1e6; // uSec
        int samples = 500;
        if (time > 5000 && time < 10e6) {
            if (time > 50e3) samples = 250;
            double RC = this.captureCapacitance(samples, (int) (time / samples))[1]; // todo : complete statement after writing captureCapacitance method
            return RC / 10e3;
        } else {
            Log.v(TAG, "cap out of range " + time + cap);
            return null;
        }
    }

    /**
     * Charges a capacitor connected to IN1 via a 20K resistor from a 3.3V source for a fixed interval.
     *
     * @param cTime range of time
     * @return the capacitance calculated using the formula Vc = Vs(1-exp(-t/RC))
     */
    public double[] getCapacitorRange(int cTime) {
        // returns values as a double array arr[0] = v,  arr[1] = c
        this.chargeCap(0, 30000);
        try {
            mPacketHandler.sendByte(mCommandsProto.COMMON);
            mPacketHandler.sendByte(mCommandsProto.GET_CAP_RANGE);
            mPacketHandler.sendInt(cTime);
            int vSum = mPacketHandler.getVoltageSummation();
            mPacketHandler.getAcknowledgement();
            double v = vSum * 3.3 / 16 / 4095;
            double c = -cTime * 1e-6 / 1e4 / Math.log(1 - v / 3.3);
            return new double[]{v, c};
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Charges a capacitor connected to IN1 via a 20K resistor from a 3.3V source for a fixed interval
     *
     * @return the capacitance calculated using the formula Vc = Vs(1-exp(-t/RC))
     */
    public double[] getCapacitorRange() {
        double[] range = new double[]{1.5, 50e-12};
        for (int i = 0; i < 4; i++) {
            range = getCapacitorRange(50 * (int) (Math.pow(10, i)));
            if (range[0] > 1.5) {
                if (i == 0 && range[0] > 3.28) {
                    range[1] = 50e-12;
                }
                break;
            }
        }
        return range;
    }

    /**
     * Measures capacitance of component connected between CAP and ground
     *
     * @return Capacitance (F)
     */
    public Double getCapacitance() {
        double[] GOOD_VOLTS = new double[]{2.5, 2.8};
        int CT = 10;
        int CR = 1;
        int iterations = 0;
        long startTime = System.currentTimeMillis() / 1000;
        while (System.currentTimeMillis() / 1000 - startTime < 5) {
            if (CT > 65000) {
                Log.v(TAG, "CT too high");
                return this.capacitanceViaRCDischarge();
            }
            double[] temp = getCapacitance(CR, 0, CT);
            double V = temp[0];
            double C = temp[1];
            if (CT > 30000 && V < 0.1) {
                Log.v(TAG, "Capacitance too high for this method");
                return null;
            } else if (V > GOOD_VOLTS[0] && V < GOOD_VOLTS[1])
                return C;
            else if (V < GOOD_VOLTS[0] && V > 0.01 && CT < 40000) {
                if (GOOD_VOLTS[0] / V > 1.1 && iterations < 10) {
                    CT = (int) (CT * GOOD_VOLTS[0] / V);
                    iterations += 1;
                    Log.v(TAG, "Increased CT " + CT);
                } else if (iterations == 10)
                    return null;
                else return C;
            } else if (V <= 0.1 && CR < 3)
                CR += 1;
            else if (CR == 3) {
                Log.v(TAG, "Capture mode");
                return this.capacitanceViaRCDischarge();
            }
        }
        return null;
    }

    public double[] getCapacitance(int currentRange, double trim, int chargeTime) {  // time in uSec
        this.chargeCap(0, 30000);
        try {
            mPacketHandler.sendByte(mCommandsProto.COMMON);
            mPacketHandler.sendByte(mCommandsProto.GET_CAPACITANCE);
            mPacketHandler.sendByte(currentRange);
            if (trim < 0)
                mPacketHandler.sendByte((int) (31 - Math.abs(trim) / 2) | 32);
            else
                mPacketHandler.sendByte((int) trim / 2);
            mPacketHandler.sendInt(chargeTime);
            Thread.sleep((long) (chargeTime * 1e-6 + .02));
            int VCode;
            int i = 0;
            do VCode = mPacketHandler.getVoltageSummation();
            while (VCode == -1 & i++ < 10);
            double v = 3.3 * VCode / 4095;
            mPacketHandler.getAcknowledgement();
            double chargeCurrent = this.currents[currentRange] * (100 + trim) / 100.0;
            double c = 0;
            if (v != 0)
                c = (chargeCurrent * chargeTime * 1e-6 / v - this.SOCKET_CAPACITANCE) / this.currentScalars[currentRange];
            return new double[]{v, c};
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Fetch the processor's temperature.
     *
     * @return the processor's temperature.
     */
    public Double getTemperature() {
        int cs = 3;
        double V = getCTMUVoltage("", cs, 0); // todo inspect this binary channel
        return (760 - V * 1000) / 1.56; // current source = 3 for best results
    }

    public void calibrateCTMU(double[] scalars) {
        this.currents = new double[]{0.55e-3, 0.55e-6, 0.55e-5, 0.55e-4};
        this.currentScalars = scalars;
    }

    /**
     * get_ctmu_voltage(5,2)  will activate a constant current source of 5.5uA on IN1 and then measure the voltage at the output.
     * If a diode is used to connect IN1 to ground, the forward voltage drop of the diode will be returned.
     *
     * @param channel
     * @param cRange  CRange=0   implies 550uA,
     *                CRange=1   implies 0.55uA,
     *                CRange=2   implies 5.5uA,
     *                CRange=3   implies 55uA
     * @param tgen
     * @return Voltage
     */
    public double getCTMUVoltage(String channel, int cRange, int tgen) {
        if (tgen == -1) tgen = 1;
        int channelI = 0;
        if ("CAP".equals(channel))
            channelI = 5;
        try {
            mPacketHandler.sendByte(mCommandsProto.COMMON);
            mPacketHandler.sendByte(mCommandsProto.GET_CTMU_VOLTAGE);
            mPacketHandler.sendByte((channelI) | (cRange << 5) | (tgen << 7));
            int v = mPacketHandler.getInt();
            mPacketHandler.getAcknowledgement();
            return (3.3 * v / 16 / 4095.);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void startCTMU(int cRange, int trim, int tgen) { // naming of arguments ??
        if (tgen == -1) tgen = 1;
        try {
            mPacketHandler.sendByte(mCommandsProto.COMMON);
            mPacketHandler.sendByte(mCommandsProto.START_CTMU);
            mPacketHandler.sendByte(cRange | (tgen << 7));
            mPacketHandler.sendByte(trim);
            mPacketHandler.getAcknowledgement();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopCTMU() {
        try {
            mPacketHandler.sendByte(mCommandsProto.COMMON);
            mPacketHandler.sendByte(mCommandsProto.STOP_CTMU);
            mPacketHandler.getAcknowledgement();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void resetHardware() {
        /*
        Resets the device, and standalone mode will be enabled if an OLED is connected to the I2C port
        */
        try {
            mPacketHandler.sendByte(mCommandsProto.COMMON);
            mPacketHandler.sendByte(mCommandsProto.RESTORE_STANDALONE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] readFlash(int page, int location) {
        /*  Reads 16 BYTES from the specified location  */
        try {
            mPacketHandler.sendByte(mCommandsProto.FLASH);
            mPacketHandler.sendByte(mCommandsProto.READ_FLASH);
            mPacketHandler.sendByte(page);
            mPacketHandler.sendByte(location);
            byte[] data = new byte[17];
            mPacketHandler.read(data, 17);
            //mPacketHandler.getAcknowledgement();
            return Arrays.copyOfRange(data, 0, 16);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] readBulkFlash(int page, int numOfBytes) {
        /*  Reads BYTES from the specified location  */

        try {
            mPacketHandler.sendByte(mCommandsProto.FLASH);
            mPacketHandler.sendByte(mCommandsProto.READ_BULK_FLASH);
            int bytesToRead = numOfBytes;
            if (numOfBytes % 2 == 1) bytesToRead += 1;
            mPacketHandler.sendInt(bytesToRead);
            mPacketHandler.sendByte(page);
            byte[] data = new byte[bytesToRead + 1];
            mPacketHandler.read(data, bytesToRead + 1);
            //mPacketHandler.getAcknowledgement();
            if (numOfBytes % 2 == 1)
                return Arrays.copyOfRange(data, 0, data.length - 1);
            else
                return Arrays.copyOfRange(data, 0, data.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void writeFlash(int page, int location, String data) {
        /*
        write a 16 BYTE string to the selected location (0-63)

        DO NOT USE THIS UNLESS YOU'RE ABSOLUTELY SURE KNOW THIS!
		YOU MAY END UP OVERWRITING THE CALIBRATION DATA, AND WILL HAVE
		TO GO THROUGH THE TROUBLE OF GETTING IT FROM THE MANUFACTURER AND
		RE-FLASHING IT.
        */
        while (data.length() < 16) data += '.';
        try {
            mPacketHandler.sendByte(mCommandsProto.FLASH);
            mPacketHandler.sendByte(mCommandsProto.WRITE_FLASH);
            mPacketHandler.sendByte(page);
            mPacketHandler.sendByte(location);
            mCommunicationHandler.write(data.getBytes(), 500);
            Thread.sleep(100);
            mPacketHandler.getAcknowledgement();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void writeBulkFlash(int location, ArrayList<Integer> data) {
        /*
        write a byte array to the entire flash page. Erases any other data

        DO NOT USE THIS UNLESS YOU'RE ABSOLUTELY SURE KNOW THIS!
		YOU MAY END UP OVERWRITING THE CALIBRATION DATA, AND WILL HAVE
		TO GO THROUGH THE TROUBLE OF GETTING IT FROM THE MANUFACTURER AND
		RE-FLASHING IT.
        */
        if (data.size() % 2 == 1) data.add(0);
        try {
            mPacketHandler.sendByte(mCommandsProto.FLASH);
            mPacketHandler.sendByte(mCommandsProto.WRITE_BULK_FLASH);
            mPacketHandler.sendInt(data.size());
            mPacketHandler.sendByte(location);
            for (int a : data) {
                mPacketHandler.sendByte(a);
            }
            mPacketHandler.getAcknowledgement();
        } catch (IOException e) {
            e.printStackTrace();
        }
        boolean equal = true;
        byte[] receiveData = readBulkFlash(location, data.size());
        for (int i = 0; i < data.size(); i++) {
            if (receiveData[i] != (data.get(i) & 0xff)) {
                equal = false;
                Log.v(TAG, "Verification by read-back failed");
            }
        }
        if (equal)
            Log.v(TAG, "Verification by read-back successful");
    }

    /* WAVEGEN SECTION */

    public void setWave(String channel, double frequency) {
        if ("SI1".equals(channel))
            this.setSI1(frequency, null);
        else if ("SI2".equals(channel))
            this.setSI2(frequency, null);
    }

    public double setSine1(double frequency) {
        return this.setSI1(frequency, "sine");
    }

    public double setSine2(double frequency) {
        return this.setSI2(frequency, "sine");
    }

    public double setSI1(double frequency, String waveType) {
        int HIGHRES, tableSize;
        if (frequency < 0.1) {
            Log.v(TAG, "frequency too low");
            return -1;
        } else if (frequency < 1100) {
            HIGHRES = 1;
            tableSize = 512;
        } else {
            HIGHRES = 0;
            tableSize = 32;
        }
        if (waveType != null) {
            if ("sine".equals(waveType) | "tria".equals(waveType)) {
                if (!(this.waveType.get("SI1").equals(waveType))) {
                    this.loadEquation("SI1", waveType);
                }
            } else {
                Log.v(TAG, "Not a valid waveform. try sine or tria");
            }
        }
        int[] p = new int[]{1, 8, 64, 256};
        int prescalar = 0, wavelength = 0;
        while (prescalar <= 3) {
            wavelength = (int) (64e6 / frequency / p[prescalar] / tableSize);
            frequency = 64e6 / wavelength / p[prescalar] / tableSize;
            if (wavelength < 65525) break;
            prescalar++;
        }
        if (prescalar == 4) {
            Log.v(TAG, "Out of range");
            return -1;
        }
        try {
            mPacketHandler.sendByte(mCommandsProto.WAVEGEN);
            mPacketHandler.sendByte(mCommandsProto.SET_SINE1);
            mPacketHandler.sendByte(HIGHRES | (prescalar << 1));
            mPacketHandler.sendInt(wavelength - 1);
            mPacketHandler.getAcknowledgement();
            this.sin1Frequency = frequency;
            return this.sin1Frequency;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public double setSI2(double frequency, String waveType) {
        int HIGHRES;
        int tableSize;
        if (frequency < 0.1) {
            Log.v(TAG, "frequency too low");
            return -1;
        } else if (frequency < 1100) {
            HIGHRES = 1;
            tableSize = 512;
        } else {
            HIGHRES = 0;
            tableSize = 32;
        }
        if (waveType != null) {
            if ("sine".equals(waveType) | "tria".equals(waveType)) {
                if (!(this.waveType.get("SI2").equals(waveType))) {
                    this.loadEquation("SI2", waveType);
                }
            } else {
                Log.v(TAG, "Not a valid waveform. try sine or tria");
            }
        }
        int[] p = new int[]{1, 8, 64, 256};
        int prescalar = 0, wavelength = 0;
        while (prescalar <= 3) {
            wavelength = (int) (64e6 / frequency / p[prescalar] / tableSize);
            frequency = 64e6 / wavelength / p[prescalar] / tableSize;
            if (wavelength < 65525) break;
            prescalar++;
        }
        if (prescalar == 4) {
            Log.v(TAG, "Out of range");
            return -1;
        }
        try {
            mPacketHandler.sendByte(mCommandsProto.WAVEGEN);
            mPacketHandler.sendByte(mCommandsProto.SET_SINE2);
            mPacketHandler.sendByte(HIGHRES | (prescalar << 1));
            mPacketHandler.sendInt(wavelength - 1);
            mPacketHandler.getAcknowledgement();
            this.sin2Frequency = frequency;
            return this.sin2Frequency;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public double readBackWaveform(String channel) {
        if ("SI1".equals(channel))
            return this.sin1Frequency;
        else if ("SI2".equals(channel))
            return this.sin2Frequency;
        else if ("SQR".startsWith(channel))
            return this.squareWaveFrequency.get(channel);
        return -1;
    }

    public double setWaves(double frequency, double phase, double frequency2) {
        // used frequency as double ( python code demanded ), maybe its taken in KHz or something ( Clarify )
        int HIGHRES, tableSize, HIGHRES2, tableSize2, wavelength = 0, wavelength2 = 0;
        if (frequency2 == -1) frequency2 = frequency;
        if (frequency < 0.1) {
            Log.v(TAG, "frequency 1 too low");
            return -1;
        } else if (frequency < 1100) {
            HIGHRES = 1;
            tableSize = 512;
        } else {
            HIGHRES = 0;
            tableSize = 32;
        }
        if (frequency2 < 0.1) {
            Log.v(TAG, "frequency 2 too low");
            return -1;
        } else if (frequency2 < 1100) {
            HIGHRES2 = 1;
            tableSize2 = 512;
        } else {
            HIGHRES2 = 0;
            tableSize2 = 32;
        }
        if (frequency < 1 || frequency2 < 1)
            Log.v(TAG, "extremely low frequencies will have reduced amplitudes due to AC coupling restrictions");

        int[] p = new int[]{1, 8, 64, 256};
        int prescalar = 0;
        double retFrequency = 0;
        while (prescalar <= 3) {
            wavelength = (int) (64e6 / frequency / p[prescalar] / tableSize);
            retFrequency = 64e6 / wavelength / p[prescalar] / tableSize;
            if (wavelength < 65525) break;
            prescalar++;
        }
        if (prescalar == 4) {
            Log.v(TAG, "#1 out of range");
            return -1;
        }
        int prescalar2 = 0;
        double retFrequency2 = 0;
        while (prescalar2 <= 3) {
            wavelength2 = (int) (64e6 / frequency2 / p[prescalar2] / tableSize2);
            retFrequency2 = 64e6 / wavelength2 / p[prescalar2] / tableSize2;
            if (wavelength2 < 65525) break;
            prescalar2++;
        }
        if (prescalar2 == 4) {
            Log.v(TAG, "#2 out of range");
            return -1;
        }

        int phaseCoarse = (int) (tableSize2 * (phase) / 360.);
        int phaseFine = (int) (wavelength2 * (phase - (phaseCoarse) * 360. / tableSize2) / (360. / tableSize2));
        try {
            mPacketHandler.sendByte(mCommandsProto.WAVEGEN);
            mPacketHandler.sendByte(mCommandsProto.SET_BOTH_WG);
            mPacketHandler.sendInt(wavelength - 1);
            mPacketHandler.sendInt(wavelength2 - 1);
            mPacketHandler.sendInt(phaseCoarse);
            mPacketHandler.sendInt(phaseFine);
            mPacketHandler.sendByte((prescalar2 << 4) | (prescalar << 2) | (HIGHRES2 << 1) | (HIGHRES));
            mPacketHandler.getAcknowledgement();
            this.sin1Frequency = retFrequency;
            this.sin2Frequency = retFrequency2;
            return retFrequency;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void loadEquation(String channel, String function) {
        double[] span = new double[2];
        if ("sine".equals(function)) {
            span[0] = 0;
            span[1] = 2 * Math.PI;
            waveType.put(channel, "sine");
        } else if ("tria".equals(function)) {
            span[0] = 0;
            span[1] = 4;
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
                    y.add(Math.abs(x.get(i) % 4 - 2));
                    break;
            }
        }
        loadTable(channel, y, waveType.get(channel), -1);
    }

    private void loadTable(String channel, ArrayList<Double> y, String mode, double amp) {
        waveType.put(channel, mode);
        ArrayList<String> channels = new ArrayList<>();
        ArrayList<Double> points = y;
        channels.add("SI1");
        channels.add("SI2");
        int num;
        if (channels.contains(channel)) {
            num = channels.indexOf(channel) + 1;
        } else {
            Log.e(TAG, "Channel does not exist. Try SI1 or SI2");
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
        y = new ArrayList<Double>();


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


    public double setSqr1(double frequency, double dutyCycle, boolean onlyPrepare) {
        if (dutyCycle == -1) dutyCycle = 50;
        if (frequency == 0 || dutyCycle == 0) return -1;
        if (frequency > 10e6) {
            Log.v(TAG, "Frequency is greater than 10MHz. Please use map_reference_clock for 16 & 32MHz outputs");
            return 0;
        }
        int[] p = new int[]{1, 8, 64, 256};
        int prescalar = 0;
        int wavelength = 0;
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
        this.squareWaveFrequency.put("SQR1", 64e6 / wavelength / p[prescalar & 0x3]);
        return this.squareWaveFrequency.get("SQR1");
    }

    public double setSqr2(double frequency, double dutyCycle) {
        int[] p = new int[]{1, 8, 64, 256};
        int prescalar = 0;
        int wavelength = 0;
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
        this.squareWaveFrequency.put("SQR2", 64e6 / wavelength / p[prescalar & 0x3]);
        return this.squareWaveFrequency.get("SQR2");
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

    public double sqrPWM(double frequency, double h0, double p1, double h1, double p2, double h2, double p3, double h3, boolean pulse) {
        if (frequency == 0) return -1;
        if (h0 == 0) {
            h0 = 0.1;
        }
        if (h1 == 0) {
            h1 = 0.1;
        }
        if (h2 == 0) {
            h2 = 0.1;
        }
        if (h3 == 0) {
            h3 = 0.1;
        }
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
            this.squareWaveFrequency.put(channel, 64e6 / wavelength / p[prescalar & 0x3]);
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

    /*  ANALOG OUTPUTS  */

    public void setPV1(float value) {
        this.dac.setVoltage("PV1", value);
    }

    public void setPV2(float value) {
        this.dac.setVoltage("PV2", value);
    }

    public void setPV3(float value) {
        this.dac.setVoltage("PV3", value);
    }

    public void setPCS(float value) {
        this.dac.setCurrent(value);
    }

    public double getPV1() {
        return this.dac.getVoltage("PV1");
    }

    public double getPV2() {
        return this.dac.getVoltage("PV2");
    }

    public double getPV3() {
        return this.dac.getVoltage("PV3");
    }

    public double getPCS() {
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


    /* READ PROGRAM AND DATA ADDRESSES */

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

    /* MOTOR SIGNALLING */

    public void stepperMotor(int steps, int delay, int direction) {
        try {
            mPacketHandler.sendByte(mCommandsProto.NONSTANDARD_IO);
            mPacketHandler.sendByte(mCommandsProto.STEPPER_MOTOR);
            mPacketHandler.sendInt((steps << 1) | direction);
            mPacketHandler.sendInt(delay);
            Thread.sleep((long) (steps * delay * 1e-3 * 1000));
        } catch (IOException | InterruptedException e) {
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
            int timeout = mPacketHandler.getInt();
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
            this.aChannels.get(0).setParams("CH1", samples, this.timebase, resolution, this.analogInputSources.get(channel), null);
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

    public void disconnect() throws IOException {
        mCommunicationHandler.close();
        PacketHandler.version = "";
    }
}