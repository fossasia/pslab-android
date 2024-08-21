package io.pslab.communication.sensors;

import java.io.IOException;
import java.util.ArrayList;

import io.pslab.communication.ScienceLab;
import io.pslab.communication.peripherals.I2C;

public class APDS9960 {

    // APDS9960 default address
    private static final int APDS9960_I2C_ADDRESS = 0x39;
    private final I2C i2c;

    private static final int APDS9960_ENABLE = 0x80;
    private static final int APDS9960_ATIME = 0x81;
    private static final int APDS9960_PILT = 0x89;
    private static final int APDS9960_PERS = 0x8C;
    private static final int APDS9960_CONTROL = 0x8F;
    private static final int APDS9960_CDATAL = 0x94;
    private static final int APDS9960_PDATA = 0x9C;
    private static final int APDS9960_GPENTH = 0xA0;
    private static final int APDS9960_GEXTH = 0xA1;
    private static final int APDS9960_GCONF1 = 0xA2;
    private static final int APDS9960_GCONF2 = 0xA3;
    private static final int APDS9960_GPULSE = 0xA6;
    private static final int APDS9960_GCONF4 = 0xAB;
    private static final int APDS9960_AICLEAR = 0xE7;

    private static final int BIT_MASK_ENABLE_EN = 0x01;
    private static final int BIT_MASK_ENABLE_COLOR = 0x02;
    private static final int BIT_MASK_ENABLE_PROX = 0x04;
    private static final int BIT_MASK_ENABLE_GESTURE = 0x40;
    private static final int BIT_MASK_GCONF4_GFIFO_CLR = 0x04;

    private static final int BIT_POS_PERS_PPERS = 4;
    private static final int BIT_MASK_PERS_PPERS = 0xF0;

    private static final int BIT_POS_CONTROL_AGAIN = 0;
    private static final int BIT_MASK_CONTROL_AGAIN = 3;

    public APDS9960(I2C i2c, ScienceLab scienceLab) throws Exception {
        this.i2c = i2c;
        if (scienceLab.isConnected()) {
            enableProximity(false);
            enableGesture(false);
            enableColor(false);

            setProximityInterruptThreshold(new int[]{0, 0, 0});
            i2c.write(APDS9960_I2C_ADDRESS, new int[]{0}, APDS9960_GPENTH);
            i2c.write(APDS9960_I2C_ADDRESS, new int[]{0}, APDS9960_GEXTH);
            i2c.write(APDS9960_I2C_ADDRESS, new int[]{0}, APDS9960_GCONF1);
            i2c.write(APDS9960_I2C_ADDRESS, new int[]{0}, APDS9960_GCONF2);
            i2c.write(APDS9960_I2C_ADDRESS, new int[]{0}, APDS9960_GCONF4);
            i2c.write(APDS9960_I2C_ADDRESS, new int[]{0}, APDS9960_GPULSE);
            i2c.write(APDS9960_I2C_ADDRESS, new int[]{255}, APDS9960_ATIME);
            i2c.write(APDS9960_I2C_ADDRESS, new int[]{0}, APDS9960_CONTROL);

            clearInterrupt();

            setBit(APDS9960_GCONF4, BIT_MASK_GCONF4_GFIFO_CLR, true);

            i2c.write(APDS9960_I2C_ADDRESS, new int[]{0}, APDS9960_ENABLE);
            Thread.sleep(25);

            enable(true);
            Thread.sleep(10);

            setProximityInterruptThreshold(new int[]{0, 5, 4});
            i2c.write(APDS9960_I2C_ADDRESS, new int[]{0x05}, APDS9960_GPENTH);
            i2c.write(APDS9960_I2C_ADDRESS, new int[]{0x1E}, APDS9960_GEXTH);
            i2c.write(APDS9960_I2C_ADDRESS, new int[]{0x82}, APDS9960_GCONF1);
            i2c.write(APDS9960_I2C_ADDRESS, new int[]{0x41}, APDS9960_GCONF2);
            i2c.write(APDS9960_I2C_ADDRESS, new int[]{0x85}, APDS9960_GPULSE);
            setColorIntegrationTime(256);
            setColorGain(1);
        }
    }

    private void enable(Boolean value) throws IOException {
        setBit(APDS9960_ENABLE, BIT_MASK_ENABLE_EN, value);
    }

    public void enableProximity(Boolean value) throws IOException {
        setBit(APDS9960_ENABLE, BIT_MASK_ENABLE_PROX, value);
    }

    public void enableGesture(Boolean value) throws IOException {
        setBit(APDS9960_ENABLE, BIT_MASK_ENABLE_GESTURE, value);
    }

    public void enableColor(Boolean value) throws IOException {
        setBit(APDS9960_ENABLE, BIT_MASK_ENABLE_COLOR, value);
    }

    private void setProximityInterruptThreshold(int[] settingArray) throws IOException {
        if (settingArray.length != 0 && settingArray[0] >= 0 && settingArray[0] <= 255) {
            i2c.write(APDS9960_I2C_ADDRESS, new int[]{settingArray[0]}, APDS9960_PILT);
        }
        if (settingArray.length > 1 && settingArray[0] >= 0 && settingArray[0] <= 255) {
            i2c.write(APDS9960_I2C_ADDRESS, new int[]{settingArray[1]}, APDS9960_PILT);
        }
        int persist = 4;
        if (settingArray.length > 2 && settingArray[0] >= 0 && settingArray[0] <= 15) {
            persist = Math.min(settingArray[2], 15);
            setBits(APDS9960_PERS, BIT_POS_PERS_PPERS, BIT_MASK_PERS_PPERS, persist);
        }
    }

    private void clearInterrupt() throws IOException {
        i2c.write(APDS9960_I2C_ADDRESS, new int[]{}, APDS9960_AICLEAR);
    }

    private void setColorIntegrationTime(int value) throws IOException {
        i2c.write(APDS9960_I2C_ADDRESS, new int[]{256 - value}, APDS9960_ATIME);
    }

    private void setColorGain(int value) throws IOException {
        setBits(APDS9960_CONTROL, BIT_POS_CONTROL_AGAIN, BIT_MASK_CONTROL_AGAIN, value);
    }

    public int getProximity() throws IOException {
        ArrayList<Integer> data = i2c.read(APDS9960_I2C_ADDRESS, 1, APDS9960_PDATA);
        return data.get(0) & 0xFF;
    }

    public int[] getColorData() throws IOException {
        return new int[]{
                colorData16(APDS9960_CDATAL + 2),
                colorData16(APDS9960_CDATAL + 4),
                colorData16(APDS9960_CDATAL + 6),
                colorData16(APDS9960_CDATAL)
        };
    }

    private void setBit(int register, int mask, Boolean value) throws IOException {
        ArrayList<Integer> data = i2c.read(APDS9960_I2C_ADDRESS, 1, register);
        if (value) {
            data.set(0, (data.get(0) & 0xFF) | mask);
        } else {
            data.set(0, (data.get(0) & 0xFF) & ~mask);
        }
        i2c.write(APDS9960_I2C_ADDRESS, data.stream().mapToInt(Integer::intValue).toArray(), register);
    }

    private void setBits(int register, int pos, int mask, int value) throws IOException {
        ArrayList<Integer> data = i2c.read(APDS9960_I2C_ADDRESS, 1, register);
        data.set(0, ((data.get(0) & 0xFF) & ~mask) | (value << pos));
        i2c.write(APDS9960_I2C_ADDRESS, data.stream().mapToInt(Integer::intValue).toArray(), register);
    }

    private int colorData16(int register) throws IOException {
        ArrayList<Integer> data = i2c.read(APDS9960_I2C_ADDRESS, 2, register);
        return ((data.get(1) & 0xFF) << 8) | ((data.get(0) & 0xFF));
    }
}
