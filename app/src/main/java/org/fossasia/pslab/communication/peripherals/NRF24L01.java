package org.fossasia.pslab.communication.peripherals;

import android.util.Log;

import org.fossasia.pslab.communication.CommandsProto;
import org.fossasia.pslab.communication.PacketHandler;
import org.fossasia.pslab.communication.SensorList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by viveksb007 on 28/3/17.
 */

public class NRF24L01 {

    private static final String TAG = "NRF24L01";
    private int R_REG = 0x00;
    private int W_REG = 0x20;
    private int RX_PAYLOAD = 0x61;
    private int TX_PAYLOAD = 0xA0;
    private int ACK_PAYLOAD = 0xA8;
    private int FLUSH_TX = 0xE1;
    private int FLUSH_RX = 0xE2;
    private int ACTIVATE = 0x50;
    private int R_STATUS = 0xFF;

    private int NRF_CONFIG = 0x00;
    private int EN_AA = 0x01;
    private int EN_RXADDR = 0x02;
    private int SETUP_AW = 0x03;
    private int SETUP_RETR = 0x04;
    private int RF_CH = 0x05;
    private int RF_SETUP = 0x06;
    private int NRF_STATUS = 0x07;
    private int OBSERVE_TX = 0x08;
    private int CD = 0x09;
    private int RX_ADDR_P0 = 0x0A;
    private int RX_ADDR_P1 = 0x0B;
    private int RX_ADDR_P2 = 0x0C;
    private int RX_ADDR_P3 = 0x0D;
    private int RX_ADDR_P4 = 0x0E;
    private int RX_ADDR_P5 = 0x0F;
    private int TX_ADDR = 0x10;
    private int RX_PW_P0 = 0x11;
    private int RX_PW_P1 = 0x12;
    private int RX_PW_P2 = 0x13;
    private int RX_PW_P3 = 0x14;
    private int RX_PW_P4 = 0x15;
    private int RX_PW_P5 = 0x16;
    private int R_RX_PL_WID = 0x60;
    private int FIFO_STATUS = 0x17;
    private int DYNPD = 0x1C;
    private int FEATURE = 0x1D;
    private int PAYLOAD_SIZE = 0;
    private int ACK_PAYLOAD_SIZE = 0;
    private int READ_PAYLOAD_SIZE = 0;

    private int ADC_COMMANDS = 1;
    private int READ_ADC = 0 << 4;

    private int I2C_COMMANDS = 2;
    private int I2C_TRANSACTION = 0 << 4;
    private int I2C_WRITE = 1 << 4;
    private int I2C_SCAN = 2 << 4;
    private int PULL_SCL_LOW = 3 << 4;
    private int I2C_CONFIG = 4 << 4;
    private int I2C_READ = 5 << 4;

    private int NRF_COMMANDS = 3;
    private int NRF_READ_REGISTER = 0;
    private int NRF_WRITE_REGISTER = 1 << 4;

    public int CURRENT_ADDRESS = 0xAAAA01;
    private int nodePos = 0, status = 0;
    private int NODELIST_MAXLENGTH = 15;
    public boolean connected = false, ready = false;

    private Map<Integer, Integer> sigs = new LinkedHashMap<>();
    private PacketHandler packetHandler;
    private CommandsProto commandsProto;
    private Map<Integer, ArrayList<Integer>> nodeList = new LinkedHashMap<>();

    public NRF24L01(PacketHandler packetHandler) {
        this.packetHandler = packetHandler;
        this.commandsProto = new CommandsProto();
        sigs.put(CURRENT_ADDRESS, 1);
        if (packetHandler.isConnected()) {
            connected = init();
        }
    }

    private boolean init() {
        try {
            packetHandler.sendByte(commandsProto.NRFL01);
            packetHandler.sendByte(commandsProto.NRF_SETUP);
            packetHandler.getAcknowledgement();
            // add code for sleep for 15mSec <Settling time>
            status = getStatus();
            if ((status & 0x80) != 0) {
                Log.e(TAG, "Radio transceiver not installed/not found");
                return false;
            } else {
                ready = true;
            }
            selectAddress(CURRENT_ADDRESS);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void selectAddress(int address) {
        try {
            packetHandler.sendByte(commandsProto.NRFL01);
            packetHandler.sendByte(commandsProto.NRF_WRITEADDRESS);
            packetHandler.sendByte(address & 0xff);
            packetHandler.sendByte((address >> 8) & 0xff);
            packetHandler.sendByte((address >> 16) & 0xff);
            packetHandler.getAcknowledgement();
            this.CURRENT_ADDRESS = address;
            if (!sigs.containsKey(address)) {
                sigs.put(address, 1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeAddress(int register, int address) {
        try {
            packetHandler.sendByte(commandsProto.NRFL01);
            packetHandler.sendByte(commandsProto.NRF_WRITEADDRESSES);
            packetHandler.sendByte(register);
            packetHandler.sendByte(address & 0xff);
            packetHandler.sendByte((address >> 8) & 0xff);
            packetHandler.sendByte((address >> 16) & 0xff);
            packetHandler.getAcknowledgement();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getStatus() {
        int val = -1;
        try {
            packetHandler.sendByte(commandsProto.NRFL01);
            packetHandler.sendByte(commandsProto.NRF_GETSTATUS);
            val = packetHandler.getByte();
            packetHandler.getAcknowledgement();
            return val;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return val;
    }

    public void rxMode() throws IOException {
        packetHandler.sendByte(commandsProto.NRFL01);
        packetHandler.sendByte(commandsProto.NRF_RXMODE);
        packetHandler.getAcknowledgement();
    }

    public void txMode() throws IOException {
        packetHandler.sendByte(commandsProto.NRFL01);
        packetHandler.sendByte(commandsProto.NRF_TXMODE);
        packetHandler.getAcknowledgement();
    }

    public void powerDown() throws IOException {
        packetHandler.sendByte(commandsProto.NRFL01);
        packetHandler.sendByte(commandsProto.NRF_POWER_DOWN);
        packetHandler.getAcknowledgement();
    }

    public char rxChar() throws IOException {
        int val = -1;
        packetHandler.sendByte(commandsProto.NRFL01);
        packetHandler.sendByte(commandsProto.NRF_RXCHAR);
        val = packetHandler.getByte();
        packetHandler.getAcknowledgement();
        return ((char) (val & 0xff));
    }

    public int txChar(char character) throws IOException {
        packetHandler.sendByte(commandsProto.NRFL01);
        packetHandler.sendByte(commandsProto.NRF_TXCHAR);
        packetHandler.sendByte(character);
        return packetHandler.getAcknowledgement() >> 4;
    }

    public int hasData() throws IOException {
        int val = -1;
        packetHandler.sendByte(commandsProto.NRFL01);
        packetHandler.sendByte(commandsProto.NRF_HASDATA);
        val = packetHandler.getByte();
        packetHandler.getAcknowledgement();
        return val;
    }

    public void flush() throws IOException {
        packetHandler.sendByte(commandsProto.NRFL01);
        packetHandler.sendByte(commandsProto.NRF_FLUSH);
        packetHandler.getAcknowledgement();
    }

    public void writeRegister(int address, int value) throws IOException {
        packetHandler.sendByte(commandsProto.NRFL01);
        packetHandler.sendByte(commandsProto.NRF_WRITEREG);
        packetHandler.sendByte(address);
        packetHandler.sendByte(value);
        packetHandler.getAcknowledgement();
    }

    public byte readRegister(int address) throws IOException {
        byte val = -1;
        packetHandler.sendByte(commandsProto.NRFL01);
        packetHandler.sendByte(commandsProto.NRF_READREG);
        packetHandler.sendByte(address);
        val = packetHandler.getByte();
        packetHandler.getAcknowledgement();
        return val;
    }

    public void writeCommand(int command) throws IOException {
        packetHandler.sendByte(commandsProto.NRFL01);
        packetHandler.sendByte(commandsProto.NRF_WRITECOMMAND);
        packetHandler.sendByte(command);
        packetHandler.getAcknowledgement();
    }

    public ArrayList<Character> readPayload(int numBytes) throws IOException {
        packetHandler.sendByte(commandsProto.NRFL01);
        packetHandler.sendByte(commandsProto.NRF_READPAYLOAD);
        packetHandler.sendByte(numBytes);
        byte[] data = new byte[numBytes];
        packetHandler.read(data, numBytes);
        packetHandler.getAcknowledgement();
        ArrayList<Character> charData = new ArrayList<>();
        for (int i = 0; i < numBytes; i++) {
            charData.add((char) data[i]);
        }
        return charData;
    }

    public int writePayload(int[] data, boolean rxMode) throws IOException {
        packetHandler.sendByte(commandsProto.NRFL01);
        packetHandler.sendByte(commandsProto.NRF_WRITEPAYLOAD);
        int numBytes = data.length | 0x80;
        if (rxMode) numBytes |= 0x40;
        packetHandler.sendByte(numBytes);
        packetHandler.sendByte(TX_PAYLOAD);
        for (int _data : data) {
            packetHandler.sendByte(_data);
        }
        int val = packetHandler.getAcknowledgement() >> 4;
        if ((val & 0x2) != 0)
            Log.e(TAG, "NRF radio not found. Connect one to the add-on port");
        else if ((val & 0x1) != 0)
            Log.e(TAG, "Node probably dead/out of range. It failed to acknowledge");
        return val;
    }

    public void startTokenManager() throws IOException {
        packetHandler.sendByte(commandsProto.NRFL01);
        packetHandler.sendByte(commandsProto.NRF_START_TOKEN_MANAGER);
        packetHandler.getAcknowledgement();
    }

    public void stopTokenManager() throws IOException {
        packetHandler.sendByte(commandsProto.NRFL01);
        packetHandler.sendByte(commandsProto.NRF_STOP_TOKEN_MANAGER);
        packetHandler.getAcknowledgement();
    }

    public int totalTokens() throws IOException {
        packetHandler.sendByte(commandsProto.NRFL01);
        packetHandler.sendByte(commandsProto.NRF_TOTAL_TOKENS);
        int val = packetHandler.getByte();
        packetHandler.getAcknowledgement();
        return val;
    }

    public ArrayList<Byte> fetchReport(int num) throws IOException {
        packetHandler.sendByte(commandsProto.NRFL01);
        packetHandler.sendByte(commandsProto.NRF_REPORTS);
        packetHandler.sendByte(num);
        ArrayList<Byte> data = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            data.add(packetHandler.getByte());
        }
        packetHandler.getAcknowledgement();
        return data;
    }

    public ArrayList<Integer> decodeI2CList(int[] data) {
        int sum = 0;
        ArrayList<Integer> addressList = new ArrayList<>();
        for (int _data : data) {
            sum += _data;
        }
        if (sum == 0) return addressList;
        for (int i = 0; i < data.length; i++) {
            if ((data[i] ^ 255) != 0) {
                for (int j = 0; j < 8; j++) {
                    if ((data[i] & (0x80 >> j)) == 0) {
                        addressList.add(8 * i + j);
                    }
                }
            }
        }
        return addressList;
    }

    public Map<Integer, ArrayList<Integer>> getNodeList() throws IOException {
        int total = totalTokens();
        if (this.nodePos != total) {
            for (int i = 0; i < this.NODELIST_MAXLENGTH; i++) {
                ArrayList<Byte> data = fetchReport(i);
                int txrx = (data.get(0)) | (data.get(1) << 8) | (data.get(2) << 16);
                if (txrx == 0) continue;
                int[] tempData = new int[17];
                for (int j = 3; j < 20; j++)
                    tempData[j - 3] = data.get(j);
                nodeList.put(txrx, decodeI2CList(tempData));
                this.nodePos = total;
            }
        }
        Map<Integer, ArrayList<Integer>> filteredList = new LinkedHashMap<>();
        for (Map.Entry<Integer, ArrayList<Integer>> entry : nodeList.entrySet()) {
            if (isAlive(entry.getKey()) != null) {
                filteredList.put(entry.getKey(), entry.getValue());
            }
        }
        return filteredList;
    }

    public ArrayList<Character> isAlive(int address) throws IOException {
        selectAddress(address);
        return transaction(new int[]{NRF_COMMANDS | NRF_READ_REGISTER, R_STATUS}, 0, 100);
    }

    public ArrayList<Character> transaction(int[] data, int listen, int timeout) throws IOException {
        packetHandler.sendByte(commandsProto.NRFL01);
        packetHandler.sendByte(commandsProto.NRF_TRANSACTION);
        packetHandler.sendByte(data.length);
        packetHandler.sendInt(timeout);
        for (int _data : data) {
            packetHandler.sendByte(_data);
        }
        ArrayList<Character> characterData = new ArrayList<>();
        int numBytes = packetHandler.getByte();
        byte[] readData;
        if (numBytes != -1) {
            readData = new byte[numBytes];
            packetHandler.read(readData, numBytes);
        } else {
            readData = null;
        }
        int val = packetHandler.getAcknowledgement() >> 4;
        if ((val & 0x1) != 0) Log.e(TAG, "Node not found " + CURRENT_ADDRESS);
        if ((val & 0x2) != 0) Log.e(TAG, "NRF on-board transmitter not found " + CURRENT_ADDRESS);
        if ((val & 0x4) != 0 & (listen == 1))
            Log.e(TAG, "Node received command but did not reply " + CURRENT_ADDRESS);

        if ((val & 0x7) != 0) {
            flush();
            sigs.put(CURRENT_ADDRESS, sigs.get(CURRENT_ADDRESS) * 50 / 51);
            return null;
        }
        sigs.put(CURRENT_ADDRESS, (sigs.get(CURRENT_ADDRESS) * 50 + 1) / 51);
        if (readData == null) return characterData;
        for (int i = 0; i < numBytes; i++) {
            characterData.add((char) readData[i]);
        }
        return characterData;
    }

    public ArrayList<Character> transactionWithRetries(int[] data, int retries) throws IOException {
        if (retries == -1) retries = 5;
        ArrayList<Character> reply = null;
        while (retries > 0) {
            reply = transaction(data, 0, 200);
            if (reply != null) {
                break;
            }
            retries--;
        }
        return reply;
    }

    public void deleteRegisteredNode(int num) throws IOException {
        packetHandler.sendByte(commandsProto.NRFL01);
        packetHandler.sendByte(commandsProto.NRF_DELETE_REPORT_ROW);
        packetHandler.sendByte(num);
        packetHandler.getAcknowledgement();
    }

    public void deleteAllRegisteredNodes() throws IOException {
        while (totalTokens() != 0) {
            deleteRegisteredNode(0);
        }
    }

    public void initShockBurstTransmitter(int payloadSize, int myAddress, int sendAddress) throws IOException {
        if (payloadSize != -1) PAYLOAD_SIZE = payloadSize;
        if (myAddress != -1) myAddress = 0xAAAA01;
        if (sendAddress != -1) sendAddress = 0xAAAA01;

        init();
        writeAddress(RX_ADDR_P0, myAddress);
        writeAddress(TX_ADDR, sendAddress);
        writeRegister(RX_PW_P0, PAYLOAD_SIZE);
        rxMode();
        // Add code for sleep 0.1 sec
        flush();
    }

    public void initShockBurstReceiver(int payloadSize, int[] myAddress) throws IOException {
        if (payloadSize != -1) {
            PAYLOAD_SIZE = payloadSize;
        }
        if (myAddress[0] != -1) {
            myAddress[0] = 0xA523B5;
        }
        init();
        writeRegister(RF_SETUP, 0x26);
        int enabledPipes = 0;
        for (int i = 0; i < 6; i++) {
            if (myAddress[i] != -1) {
                enabledPipes |= (1 << i);
                writeAddress(RX_ADDR_P0 + i, myAddress[i]);
            }
        }
        if (myAddress[1] != -1)
            writeAddress(RX_ADDR_P1, myAddress[1]);

        writeRegister(EN_RXADDR, enabledPipes);
        writeRegister(EN_AA, enabledPipes);
        writeRegister(DYNPD, enabledPipes);
        writeRegister(FEATURE, 0x06);

        rxMode();
        // Add code for sleep 0.1 sec
        flush();
    }

    public void triggerAll(int val) throws IOException {
        txMode();
        selectAddress(0x111111);
        writeRegister(EN_AA, 0x00);
        writePayload(new int[]{val}, true);
        writeRegister(EN_AA, 0x01);
    }

    public int writeAckPayload(int[] data, int pipe) throws IOException {
        if (data.length != ACK_PAYLOAD_SIZE) {
            ACK_PAYLOAD_SIZE = data.length;
            if (ACK_PAYLOAD_SIZE > 15) {
                Log.v(TAG, "too large. Truncating");
                ACK_PAYLOAD_SIZE = 15;
                data = Arrays.copyOf(data, 15);
            } else {
                Log.v(TAG, "Ack payload size " + ACK_PAYLOAD_SIZE);
            }
        }
        packetHandler.sendByte(commandsProto.NRFL01);
        packetHandler.sendByte(commandsProto.NRF_WRITEPAYLOAD);
        packetHandler.sendByte(data.length);
        packetHandler.sendByte(ACK_PAYLOAD | pipe);
        for (int _data : data) {
            packetHandler.sendByte(_data);
        }
        return packetHandler.getAcknowledgement() >> 4;
    }

    public ArrayList<Integer> i2CScan() throws IOException {
        ArrayList<Integer> addresses = new ArrayList<>();
        ArrayList<Character> temp = transaction(new int[]{I2C_COMMANDS | I2C_SCAN | 0x80}, 0, 500);
        if (temp == null) return addresses;
        int sum = 0;
        for (int i = 0; i < temp.size(); i++) {
            sum += (int) temp.get(i);
        }
        if (sum == 0) return addresses;

        for (int i = 0; i < 16; i++) {
            if ((temp.get(i) ^ 255) != 0) {
                for (int j = 0; j < 8; j++) {
                    if ((temp.get(i) & (0x80 >> j)) == 0) {
                        addresses.add(8 * i + j);
                    }
                }
            }
        }
        return addresses;
    }

    public ArrayList<Integer> guessingScan() throws IOException {
        ArrayList<Integer> addresses = new ArrayList<>();
        ArrayList<Character> temp = transaction(new int[]{I2C_COMMANDS | I2C_SCAN | 0x80}, 0, 500);
        if (temp == null) return addresses;
        int sum = 0;
        for (int i = 0; i < temp.size(); i++) {
            sum += (int) temp.get(i);
        }
        if (sum == 0) return addresses;
        Log.v(TAG, "Address \t Possible Devices");
        SensorList sensorList = new SensorList();
        for (int i = 0; i < 16; i++) {
            if ((temp.get(i) ^ 255) != 0) {
                for (int j = 0; j < 8; j++) {
                    if ((temp.get(i) & (0x80 >> j)) == 0) {
                        int address = 8 * i + j;
                        addresses.add(address);
                        Log.v(TAG, Integer.toHexString(address) + "\t" + Arrays.toString(sensorList.sensorList.get(address)));
                    }
                }
            }
        }

        return addresses;
    }

}