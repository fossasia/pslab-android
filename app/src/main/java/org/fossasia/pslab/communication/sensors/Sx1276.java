package org.fossasia.pslab.communication.sensors;

import android.util.Log;

import org.fossasia.pslab.communication.peripherals.SPI;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Padmal on 6/12/17.
 */

public class Sx1276 {

    private String name = "SX1276";
    // Registers
    private int REG_FIFO = 0x00;
    private int REG_OP_MODE = 0x01;
    private int REG_FRF_MSB = 0x06;
    private int REG_FRF_MID = 0x07;
    private int REG_FRF_LSB = 0x08;
    private int REG_PA_CONFIG = 0x09;
    private int REG_LNA = 0x0c;
    private int REG_FIFO_ADDR_PTR = 0x0d;
    private int REG_FIFO_TX_BASE_ADDR = 0x0e;
    private int REG_FIFO_RX_BASE_ADDR = 0x0f;
    private int REG_FIFO_RX_CURRENT_ADDR = 0x10;
    private int REG_IRQ_FLAGS = 0x12;
    private int REG_RX_NB_BYTES = 0x13;
    private int REG_PKT_RSSI_VALUE = 0x1a;
    private int REG_PKT_SNR_VALUE = 0x1b;
    private int REG_MODEM_CONFIG_1 = 0x1d;
    private int REG_MODEM_CONFIG_2 = 0x1e;
    private int REG_PREAMBLE_MSB = 0x20;
    private int REG_PREAMBLE_LSB = 0x21;
    private int REG_PAYLOAD_LENGTH = 0x22;
    private int REG_MODEM_CONFIG_3 = 0x26;
    private int REG_RSSI_WIDEBAND = 0x2c;
    private int REG_DETECTION_OPTIMIZE = 0x31;
    private int REG_DETECTION_THRESHOLD = 0x37;
    private int REG_SYNC_WORD = 0x39;
    private int REG_DIO_MAPPING_1 = 0x40;
    private int REG_VERSION = 0x42;
    private int REG_PA_DAC = 0x4D;
    // Modes
    private int MODE_LONG_RANGE_MODE = 0x80;
    private int MODE_SLEEP = 0x00;
    private int MODE_STDBY = 0x01;
    private int MODE_TX = 0x03;
    private int MODE_RX_CONTINUOUS = 0x05;
    private int MODE_RX_SINGLE = 0x06;
    // PA config
    private int PA_BOOST = 0x80;
    // IRQ masks
    private int IRQ_TX_DONE_MASK = 0x08;
    private int IRQ_PAYLOAD_CRC_ERROR_MASK = 0x20;
    private int IRQ_RX_DONE_MASK = 0x40;

    private int MAX_PKT_LENGTH = 255;
    private int PA_OUTPUT_RFO_PIN = 0;
    private int PA_OUTPUT_PA_BOOST_PIN = 1;
    private int onReceive = 0;
    private int frequency = 10;
    private int packetIndex = 0;
    private int packetLength = 0;
    private int version = 0;
    private int implicitHeaderMode = 0;

    private SPI spi;
    private ArrayList<Byte> bytes = new ArrayList<>();

    public Sx1276(SPI spi, int frequency, int power, boolean boost, double bw, int sf, int cf) throws IOException {
        initiateSX1276(spi, frequency);
        // Output Power 17dbm
        setTxPower(power, boost ? PA_OUTPUT_PA_BOOST_PIN : PA_OUTPUT_RFO_PIN);
        idle();
        // Set bandwidth
        setSignalBandwidth(bw);
        setSpreadingFactor(sf);
        setCodingRate4(cf);
    }

    public Sx1276(SPI spi, int frequency) throws IOException {

        initiateSX1276(spi, frequency);
        // Output Power 17dbm
        setTxPower(17, PA_OUTPUT_RFO_PIN);
        idle();
        // Set bandwidth
        setSignalBandwidth(125e3);
        setSpreadingFactor(12);
        setCodingRate4(5);
    }

    private void initiateSX1276(SPI spi, int frequency) throws IOException {

        this.spi = spi;
        this.spi.setParameters(2, 6, 1, 0, 1);
        this.frequency = frequency;
        this.name = "SX1276";

        reset();

        this.version = SPIRead(REG_VERSION, 1).get(0);
        if (version != 0x12) {
            Log.d(name, "Version error " + version);
        }
        sleep();
        setFrequency(frequency);
        // Set base address
        setupBytesArray(0);
        SPIWrite(REG_FIFO_TX_BASE_ADDR, bytes);
        SPIWrite(REG_FIFO_RX_BASE_ADDR, bytes);
        // Set LNA boost
        setupBytesArray(SPIRead(REG_LNA, 1).get(0) | 0x03);
        SPIWrite(REG_LNA, bytes);
        // Set auto ADC
        setupBytesArray(0x04);
        SPIWrite(REG_MODEM_CONFIG_3, bytes);
    }

    private void reset() {
        /**/
    }

    private ArrayList<Byte> SPIRead(int adr, int total_bytes) throws IOException {
        // CS1 => 9
        ArrayList<Byte> data = new ArrayList<>();
        data.add((byte) adr);
        for (int i = 0; i < total_bytes; i++) {
            data.add((byte) 0);
        }
        data.remove(0);
        return spi.xfer(9, data);
    }

    private void beginPacket(boolean implicitHeader) throws IOException {
        idle();
        if (implicitHeader) {
            implicitHeaderMode();
        } else {
            explicitHeaderMode();
        }
        // reset FIFO & payload length
        bytes.clear();
        bytes.add((byte) 0);
        SPIWrite(REG_FIFO_ADDR_PTR, bytes);
        SPIWrite(REG_PAYLOAD_LENGTH, bytes);
    }

    private void endPacket() throws InterruptedException, IOException {
        // put in TX mode
        bytes.clear();
        bytes.add((byte) (MODE_LONG_RANGE_MODE | MODE_TX));
        SPIWrite(REG_OP_MODE, bytes);

        while (true) { // Wait for TX done
            if ((SPIRead(REG_IRQ_FLAGS, 1).get(0) & IRQ_TX_DONE_MASK) == 1) {
                break;
            } else {
                Thread.sleep(100);
            }
        }
        setupBytesArray(IRQ_TX_DONE_MASK);
        SPIWrite(REG_IRQ_FLAGS, bytes);
    }

    private int parsePacket(int size) throws IOException {
        packetLength = 0;
        int irqFlags = SPIRead(REG_IRQ_FLAGS, 1).get(0);
        if (size > 0) {
            implicitHeaderMode();
            setupBytesArray(size & 0xFF);
            SPIWrite(REG_PAYLOAD_LENGTH, bytes);
        } else {
            explicitHeaderMode();
        }
        setupBytesArray(irqFlags);
        SPIWrite(REG_IRQ_FLAGS, bytes);

        if (((irqFlags & IRQ_RX_DONE_MASK) == 1) && ((irqFlags & IRQ_PAYLOAD_CRC_ERROR_MASK) == 0)) {
            packetIndex = 0;
            if (implicitHeaderMode == 1) {
                packetLength = SPIRead(REG_PAYLOAD_LENGTH, 1).get(0);
            } else {
                packetLength = SPIRead(REG_RX_NB_BYTES, 1).get(0);
            }
            SPIWrite(REG_FIFO_ADDR_PTR, SPIRead(REG_FIFO_RX_CURRENT_ADDR, 1));
            idle();
        } else if (SPIRead(REG_OP_MODE, 1).get(0) != (MODE_LONG_RANGE_MODE | MODE_RX_SINGLE)) {
            setupBytesArray(0);
            SPIWrite(REG_FIFO_ADDR_PTR, bytes);
            setupBytesArray(MODE_LONG_RANGE_MODE | MODE_RX_SINGLE);
            SPIWrite(REG_OP_MODE, bytes);
        }
        return packetLength;
    }

    private int packetRssi() throws IOException {
        return SPIRead(REG_PKT_RSSI_VALUE, 1).get(0) - ((frequency < 868e6) ? 164 : 157);
    }

    private double packetSnr() throws IOException {
        return SPIRead(REG_PKT_SNR_VALUE, 1).get(0) * 0.25;
    }

    public int write(ArrayList<Byte> byteArray) throws IOException {
        int size = byteArray.size();
        int currentLength = SPIRead(REG_PAYLOAD_LENGTH, 1).get(0);
        if ((currentLength + size) > MAX_PKT_LENGTH) {
            size = MAX_PKT_LENGTH - currentLength;
        }
        SPIWrite(REG_FIFO, (ArrayList<Byte>) byteArray.subList(0, size));
        setupBytesArray(currentLength + size);
        SPIWrite(REG_PAYLOAD_LENGTH, bytes);
        return size;
    }

    public boolean available() throws IOException {
        return (SPIRead(REG_RX_NB_BYTES, 1).get(0) - packetIndex) == 1;
    }

    public int checkRx() throws IOException {
        byte irqFlags = SPIRead(REG_IRQ_FLAGS, 1).get(0);
        if (((irqFlags & IRQ_RX_DONE_MASK) == 1) && ((irqFlags & IRQ_PAYLOAD_CRC_ERROR_MASK) == 0)) {
            return 1;
        } else {
            return 0;
        }
    }

    public byte read() throws IOException {
        if (available()) {
            packetIndex++;
            return SPIRead(REG_FIFO, 1).get(0);
        } else return -1;
    }

    public ArrayList<Byte> readAll() throws IOException {
        ArrayList<Byte> p = new ArrayList<>();
        while (available()) {
            p.add(read());
        }
        return p;
    }

    public byte peek() throws IOException {
        if (available()) {
            ArrayList<Byte> currentAddress = SPIRead(REG_FIFO_ADDR_PTR, 1);
            byte val = SPIRead(REG_FIFO, 1).get(0);
            SPIWrite(REG_FIFO_ADDR_PTR, currentAddress);
            return val;
        } else {
            return -1;
        }
    }

    public void flush() {
        /**/
    }

    public void receive(int size) throws IOException {
        if (size > 0) {
            implicitHeaderMode();
            setupBytesArray(size & 0xFF);
            SPIWrite(REG_PAYLOAD_LENGTH, bytes);
        } else {
            explicitHeaderMode();
            setupBytesArray(MODE_LONG_RANGE_MODE | MODE_RX_SINGLE);
            SPIWrite(REG_OP_MODE, bytes);
        }
    }

    private void idle() throws IOException {
        setupBytesArray(MODE_LONG_RANGE_MODE | MODE_STDBY);
        SPIWrite(REG_OP_MODE, bytes);
    }

    private void sleep() throws IOException {
        setupBytesArray(MODE_LONG_RANGE_MODE | MODE_SLEEP);
        SPIWrite(REG_OP_MODE, bytes);
    }

    private void setTxPower(int level, int pin) throws IOException {
        if (pin == PA_OUTPUT_RFO_PIN) {
            if (level < 0) {
                level = 0;
            } else if (level > 14) {
                level = 14;
            }
            setupBytesArray(0x70 | level);
            SPIWrite(REG_PA_CONFIG, bytes);
        } else {
            if (level < 2) {
                level = 2;
            } else if (level > 17) {
                level = 17;
            }
            if (level == 17) {
                Log.d(name, "max power output");
                setupBytesArray(0x87);
                SPIWrite(REG_PA_DAC, bytes);
            } else {
                setupBytesArray(0x84);
                SPIWrite(REG_PA_DAC, bytes);
            }
            setupBytesArray(PA_BOOST | 0x70 | (level - 2));
            SPIWrite(REG_PA_CONFIG, bytes);
        }
        Log.d(name, "Power " + SPIRead(REG_PA_CONFIG, 1).get(0));
    }

    private void setFrequency(int frq) throws IOException {
        this.frequency = frq;
        int frf = (frq << 19) / 32000000;
        Log.d(name, "frf = " + frf);
        Log.d(name, "freq = " + ((frf >> 16) & 0xFF) + " " + ((frf >> 8) & 0xFF) + " " + (frf & 0xFF));
        setupBytesArray((frf >> 16) & 0xFF);
        SPIWrite(REG_FRF_MSB, bytes);
        setupBytesArray((frf >> 8) & 0xFF);
        SPIWrite(REG_FRF_MID, bytes);
        setupBytesArray(frf & 0xFF);
        SPIWrite(REG_FRF_LSB, bytes);
    }

    private void setSpreadingFactor(int spreadingFactor) throws IOException {
        if (spreadingFactor < 6) {
            spreadingFactor = 6;
        } else if (spreadingFactor > 12) {
            spreadingFactor = 12;
        }

        if (spreadingFactor == 6) {
            setupBytesArray(0xc5);
            SPIWrite(REG_DETECTION_OPTIMIZE, bytes);
            setupBytesArray(0x0c);
            SPIWrite(REG_DETECTION_THRESHOLD, bytes);
        } else {
            setupBytesArray(0xc3);
            SPIWrite(REG_DETECTION_OPTIMIZE, bytes);
            setupBytesArray(0x0a);
            SPIWrite(REG_DETECTION_THRESHOLD, bytes);
        }
        setupBytesArray((SPIRead(REG_MODEM_CONFIG_2, 1).get(0) & 0x0F) | ((spreadingFactor << 4) & 0xF0));
        SPIWrite(REG_MODEM_CONFIG_2, bytes);
    }

    private void setSignalBandwidth(double sbw) throws IOException {
        int bw = 9;
        int num = 0;
        double[] referenceList = {7.8e3, 10.4e3, 15.6e3, 20.8e3, 31.25e3, 41.7e3, 62.5e3, 125e3, 250e3};
        for (double item : referenceList) {
            if (sbw <= item) {
                bw = num;
                break;
            }
            num++;
        }
        Log.d(name, "Bandwidth " + bw);
        setupBytesArray((SPIRead(REG_MODEM_CONFIG_1, 1).get(0) & 0x0F) | (bw << 4));
        SPIWrite(REG_MODEM_CONFIG_1, bytes);
    }

    private void setCodingRate4(int denominator) throws IOException {
        if (denominator < 5) {
            denominator = 5;
        } else if (denominator > 8) {
            denominator = 8;
        }
        setupBytesArray((SPIRead(REG_MODEM_CONFIG_1, 1).get(0) & 0xF1) | ((denominator - 4) << 4));
        SPIWrite(REG_MODEM_CONFIG_1, bytes);
    }

    public void setPreambleLength(int length) throws IOException {
        setupBytesArray((length >> 8) & 0xFF);
        SPIWrite(REG_PREAMBLE_MSB, bytes);
        setupBytesArray(length & 0xFF);
        SPIWrite(REG_PREAMBLE_LSB, bytes);
    }

    public void setSyncWord(ArrayList<Byte> Word) throws IOException {
        SPIWrite(REG_SYNC_WORD, Word);
    }

    public void crc() throws IOException {
        setupBytesArray(SPIRead(REG_MODEM_CONFIG_2, 1).get(0) | 0x04);
        SPIWrite(REG_MODEM_CONFIG_2, bytes);
    }

    public void noCrc() throws IOException {
        setupBytesArray(SPIRead(REG_MODEM_CONFIG_2, 1).get(0) & 0xFB);
        SPIWrite(REG_MODEM_CONFIG_2, bytes);
    }

    public byte random() throws IOException {
        return (SPIRead(REG_RSSI_WIDEBAND, 1).get(0));
    }

    private void explicitHeaderMode() throws IOException {
        implicitHeaderMode = 0;
        setupBytesArray(SPIRead(REG_MODEM_CONFIG_1, 1).get(0) & 0xFE);
        SPIWrite(REG_MODEM_CONFIG_1, bytes);
    }

    private void implicitHeaderMode() throws IOException {
        implicitHeaderMode = 1;
        setupBytesArray(SPIRead(REG_MODEM_CONFIG_1, 1).get(0) | 0x01);
        SPIWrite(REG_MODEM_CONFIG_1, bytes);
    }

    public void handleDio0Rise() throws IOException {
        byte irqFlags = SPIRead(REG_IRQ_FLAGS, 1).get(0);
        setupBytesArray(irqFlags);
        SPIWrite(REG_IRQ_FLAGS, bytes);

        if ((irqFlags & IRQ_PAYLOAD_CRC_ERROR_MASK) == 0) {
            packetIndex = 0;
            if (implicitHeaderMode == 0) {
                packetLength = SPIRead(REG_PAYLOAD_LENGTH, 1).get(0);
            } else {
                packetLength = SPIRead(REG_RX_NB_BYTES, 1).get(0);
            }

            SPIWrite(REG_FIFO_ADDR_PTR, SPIRead(REG_FIFO_RX_CURRENT_ADDR, 1));
            if (onReceive == 1) {
                Log.d(name, "Packet Length " + packetLength);
            }
        }

        setupBytesArray(0);
        SPIWrite(REG_FIFO_ADDR_PTR, bytes);
    }

    private ArrayList<Byte> SPIWrite(int adr, ArrayList<Byte> byteArray) throws IOException {
        // CS1 => 9
        ArrayList<Byte> data = new ArrayList<>();
        data.add((byte) (0x80 | adr));
        data.addAll(byteArray);
        ArrayList<Byte> XFER = spi.xfer(9, data);
        XFER.remove(0);
        return XFER;
    }

    public ArrayList<Byte> getRaw() throws IOException {
        return SPIRead(0x02, 1);
    }

    private void setupBytesArray(int data) {
        bytes.clear();
        bytes.add((byte) data);
    }
}
