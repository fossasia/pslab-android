package org.fossasia.pslab.communication.peripherals;

import android.util.Log;

import org.fossasia.pslab.communication.SensorList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by viveksb007 on 28/3/17.
 */

public class RadioLink {

    private static final String TAG = "RadioLink";
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
    private int NRF_READ_REGISTER = 0 << 4;
    private int NRF_WRITE_REGISTER = 1 << 4;

    private int MISC_COMMANDS = 4;
    private int WS2812B_CMD = 0 << 4;

    private NRF24L01 nrf24L01;
    private int ADDRESS = 0x010101;
    private int timeout = 200;

    public RadioLink(NRF24L01 nrf24L01, int address) {
        this.nrf24L01 = nrf24L01;
        if (address != -1) ADDRESS = address;

    }

    public void selectMe() {
        if (this.nrf24L01.CURRENT_ADDRESS != this.ADDRESS) {
            this.nrf24L01.selectAddress(this.ADDRESS);
        }
    }

    public ArrayList<Character> writeI2C(int I2CAddress, int regAddress, int[] data) throws IOException {
        selectMe();
        int[] newData = new int[3 + data.length];
        newData[0] = I2C_COMMANDS | I2C_WRITE;
        newData[1] = I2CAddress;
        newData[2] = regAddress;
        System.arraycopy(data, 0, newData, 3, data.length);
        return this.nrf24L01.transaction(newData, 0, timeout);
    }

    public ArrayList<Character> readI2C(int I2CAddress, int regAddress, int numBytes) throws IOException {
        selectMe();
        return this.nrf24L01.transaction(new int[]{I2C_COMMANDS | I2C_TRANSACTION, I2CAddress, regAddress, numBytes}, 0, timeout);
    }

    public ArrayList<Character> writeBulk(int I2CAddress, int[] data) throws IOException {
        selectMe();
        int[] newData = new int[2 + data.length];
        newData[0] = I2C_COMMANDS | I2C_WRITE;
        newData[1] = I2CAddress;
        System.arraycopy(data, 0, newData, 2, data.length);
        return this.nrf24L01.transaction(newData, 0, timeout);
    }

    public ArrayList<Character> readBulk(int I2CAddress, int regAddress, int numBytes) throws IOException {
        selectMe();
        return this.nrf24L01.transactionWithRetries(new int[]{I2C_COMMANDS | I2C_TRANSACTION, I2CAddress, regAddress, numBytes}, -1);
    }

    public ArrayList<Character> simpleRead(int I2CAddress, int numBytes) throws IOException {
        selectMe();
        return this.nrf24L01.transactionWithRetries(new int[]{I2C_COMMANDS | I2C_READ, I2CAddress, numBytes}, -1);
    }

    public ArrayList<Character> readADC(int channel) throws IOException {
        selectMe();
        return this.nrf24L01.transaction(new int[]{ADC_COMMANDS | READ_ADC, channel}, 0, timeout);
    }

    public ArrayList<Integer> i2CScan() throws IOException {
        selectMe();
        Log.v(TAG, "Scanning addresses 0-127...");
        ArrayList<Integer> addresses = new ArrayList<>();
        ArrayList<Character> temp = this.nrf24L01.transaction(new int[]{I2C_COMMANDS | I2C_SCAN | 0x80}, 0, 500);
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

    public ArrayList<Integer> pullSCLLow(int t_ms) throws IOException {
        selectMe();
        ArrayList<Character> data;
        data = this.nrf24L01.transaction(new int[]{I2C_COMMANDS | PULL_SCL_LOW, t_ms}, 0, timeout);
        if (data != null) {
            int[] tempData = new int[data.size()];
            for (int i = 0; i < data.size(); i++) {
                tempData[i] = data.get(i);
            }
            return decodeI2CList(tempData);
        } else {
            return new ArrayList<>();
        }
    }

    public ArrayList<Character> configI2C(int frequency) throws IOException {
        selectMe();
        int brgVal = (int) 32e6 / frequency / 4 - 1;
        return this.nrf24L01.transaction(new int[]{I2C_COMMANDS | I2C_CONFIG, brgVal}, 0, timeout);
    }

    public ArrayList<Character> writeRegister(int register, int value) throws IOException {
        selectMe();
        return this.nrf24L01.transaction(new int[]{NRF_COMMANDS | NRF_WRITE_REGISTER, register, value}, 0, timeout);
    }

    public int readRegister(int register) throws IOException {
        selectMe();
        ArrayList<Character> data = this.nrf24L01.transaction(new int[]{NRF_COMMANDS | NRF_READ_REGISTER, register}, 0, timeout);
        if (data != null) {
            return (int) data.get(0);
        } else
            return -1;
    }

    public ArrayList<Character> WS2812B(int[][] cols) throws IOException {
        selectMe();
        int[] colorArray = new int[1 + cols.length * 3];
        colorArray[0] = MISC_COMMANDS | WS2812B_CMD;
        for (int i = 1, j = 1; i < cols.length; i++, j += 3) {
            colorArray[j] = cols[i][1];
            colorArray[j + 1] = cols[i][0];
            colorArray[j + 2] = cols[i][2];
        }
        return this.nrf24L01.transaction(colorArray, 0, timeout);
    }

}
