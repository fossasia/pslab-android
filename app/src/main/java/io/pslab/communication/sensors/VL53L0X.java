package io.pslab.communication.sensors;

import java.io.IOException;
import java.util.ArrayList;

import io.pslab.communication.ScienceLab;
import io.pslab.communication.peripherals.I2C;

public class VL53L0X {

    private static final String TAG = "VL53L0X";
    // VL53L0X default address
    private static final int ADDRESS = 0x29;
    private final I2C i2c;

    // Configuration constants, taken from https://github.com/adafruit/Adafruit_CircuitPython_VL53L0X.git
    private static final int SYSRANGE_START = 0x00;
    private static final int SYSTEM_THRESH_HIGH = 0x0C;
    private static final int SYSTEM_THRESH_LOW = 0x0E;
    private static final int SYSTEM_SEQUENCE_CONFIG = 0x01;
    private static final int SYSTEM_RANGE_CONFIG = 0x09;
    private static final int SYSTEM_INTERMEASUREMENT_PERIOD = 0x04;
    private static final int SYSTEM_INTERRUPT_CONFIG_GPIO = 0x0A;
    private static final int GPIO_HV_MUX_ACTIVE_HIGH = 0x84;
    private static final int SYSTEM_INTERRUPT_CLEAR = 0x0B;
    private static final int RESULT_INTERRUPT_STATUS = 0x13;
    private static final int RESULT_RANGE_STATUS = 0x14;
    private static final int RESULT_CORE_AMBIENT_WINDOW_EVENTS_RTN = 0xBC;
    private static final int RESULT_CORE_RANGING_TOTAL_EVENTS_RTN = 0xC0;
    private static final int RESULT_CORE_AMBIENT_WINDOW_EVENTS_REF = 0xD0;
    private static final int RESULT_CORE_RANGING_TOTAL_EVENTS_REF = 0xD4;
    private static final int RESULT_PEAK_SIGNAL_RATE_REF = 0xB6;
    private static final int ALGO_PART_TO_PART_RANGE_OFFSET_MM = 0x28;
    private static final int I2C_SLAVE_DEVICE_ADDRESS = 0x8A;
    private static final int MSRC_CONFIG_CONTROL = 0x60;
    private static final int PRE_RANGE_CONFIG_MIN_SNR = 0x27;
    private static final int PRE_RANGE_CONFIG_VALID_PHASE_LOW = 0x56;
    private static final int PRE_RANGE_CONFIG_VALID_PHASE_HIGH = 0x57;
    private static final int PRE_RANGE_MIN_COUNT_RATE_RTN_LIMIT = 0x64;
    private static final int FINAL_RANGE_CONFIG_MIN_SNR = 0x67;
    private static final int FINAL_RANGE_CONFIG_VALID_PHASE_LOW = 0x47;
    private static final int FINAL_RANGE_CONFIG_VALID_PHASE_HIGH = 0x48;
    private static final int FINAL_RANGE_CONFIG_MIN_COUNT_RATE_RTN_LIMIT = 0x44;
    private static final int PRE_RANGE_CONFIG_SIGMA_THRESH_HI = 0x61;
    private static final int PRE_RANGE_CONFIG_SIGMA_THRESH_LO = 0x62;
    private static final int PRE_RANGE_CONFIG_VCSEL_PERIOD = 0x50;
    private static final int PRE_RANGE_CONFIG_TIMEOUT_MACROP_HI = 0x51;
    private static final int PRE_RANGE_CONFIG_TIMEOUT_MACROP_LO = 0x52;
    private static final int SYSTEM_HISTOGRAM_BIN = 0x81;
    private static final int HISTOGRAM_CONFIG_INITIAL_PHASE_SELECT = 0x33;
    private static final int HISTOGRAM_CONFIG_READOUT_CTRL = 0x55;
    private static final int FINAL_RANGE_CONFIG_VCSEL_PERIOD = 0x70;
    private static final int FINAL_RANGE_CONFIG_TIMEOUT_MACROP_HI = 0x71;
    private static final int FINAL_RANGE_CONFIG_TIMEOUT_MACROP_LO = 0x72;
    private static final int CROSSTALK_COMPENSATION_PEAK_RATE_MCPS = 0x20;
    private static final int MSRC_CONFIG_TIMEOUT_MACROP = 0x46;
    private static final int SOFT_RESET_GO2_SOFT_RESET_N = 0xBF;
    private static final int IDENTIFICATION_MODEL_ID = 0xC0;
    private static final int IDENTIFICATION_REVISION_ID = 0xC2;
    private static final int OSC_CALIBRATE_VAL = 0xF8;
    private static final int GLOBAL_CONFIG_VCSEL_WIDTH = 0x32;
    private static final int GLOBAL_CONFIG_SPAD_ENABLES_REF_0 = 0xB0;
    private static final int GLOBAL_CONFIG_SPAD_ENABLES_REF_1 = 0xB1;
    private static final int GLOBAL_CONFIG_SPAD_ENABLES_REF_2 = 0xB2;
    private static final int GLOBAL_CONFIG_SPAD_ENABLES_REF_3 = 0xB3;
    private static final int GLOBAL_CONFIG_SPAD_ENABLES_REF_4 = 0xB4;
    private static final int GLOBAL_CONFIG_SPAD_ENABLES_REF_5 = 0xB5;
    private static final int GLOBAL_CONFIG_REF_EN_START_SELECT = 0xB6;
    private static final int DYNAMIC_SPAD_NUM_REQUESTED_REF_SPAD = 0x4E;
    private static final int DYNAMIC_SPAD_REF_EN_START_OFFSET = 0x4F;
    private static final int POWER_MANAGEMENT_GO1_POWER_FORCE = 0x80;
    private static final int VHV_CONFIG_PAD_SCL_SDA_EXTSUP_HV = 0x89;
    private static final int ALGO_PHASECAL_LIM = 0x30;
    private static final int ALGO_PHASECAL_CONFIG_TIMEOUT = 0x30;
    private static final int VCSEL_PERIOD_PRE_RANGE = 0;
    private static final int VCSEL_PERIOD_FINAL_RANGE = 1;
    private static final int DISABLE_SIGNAL_RATE_MSRC = 0x2;
    private static final int DISABLE_SIGNAL_RATE_PRE_RANGE = 0x10;

    private static final int[][] ID_REGISTERS = {
            {0xC0, 0xEE},
            {0xC1, 0xAA},
            {0xC2, 0x10}
    };

    private static final int[][] SPAD_CONFIG = {
            {0xFF, 0x01},
            {DYNAMIC_SPAD_REF_EN_START_OFFSET, 0x00},
            {DYNAMIC_SPAD_NUM_REQUESTED_REF_SPAD, 0x2C},
            {0xFF, 0x00},
            {GLOBAL_CONFIG_REF_EN_START_SELECT, 0xB4}
    };

    private static final int[][] TUNING_CONFIG = {
            {0xFF, 0x01},
            {0x00, 0x00},
            {0xFF, 0x00},
            {0x09, 0x00},
            {0x10, 0x00},
            {0x11, 0x00},
            {0x24, 0x01},
            {0x25, 0xFF},
            {0x75, 0x00},
            {0xFF, 0x01},
            {0x4E, 0x2C},
            {0x48, 0x00},
            {0x30, 0x20},
            {0xFF, 0x00},
            {0x30, 0x09},
            {0x54, 0x00},
            {0x31, 0x04},
            {0x32, 0x03},
            {0x40, 0x83},
            {0x46, 0x25},
            {0x60, 0x00},
            {0x27, 0x00},
            {0x50, 0x06},
            {0x51, 0x00},
            {0x52, 0x96},
            {0x56, 0x08},
            {0x57, 0x30},
            {0x61, 0x00},
            {0x62, 0x00},
            {0x64, 0x00},
            {0x65, 0x00},
            {0x66, 0xA0},
            {0xFF, 0x01},
            {0x22, 0x32},
            {0x47, 0x14},
            {0x49, 0xFF},
            {0x4A, 0x00},
            {0xFF, 0x00},
            {0x7A, 0x0A},
            {0x7B, 0x00},
            {0x78, 0x21},
            {0xFF, 0x01},
            {0x23, 0x34},
            {0x42, 0x00},
            {0x44, 0xFF},
            {0x45, 0x26},
            {0x46, 0x05},
            {0x40, 0x40},
            {0x0E, 0x06},
            {0x20, 0x1A},
            {0x43, 0x40},
            {0xFF, 0x00},
            {0x34, 0x03},
            {0x35, 0x44},
            {0xFF, 0x01},
            {0x31, 0x04},
            {0x4B, 0x09},
            {0x4C, 0x05},
            {0x4D, 0x04},
            {0xFF, 0x00},
            {0x44, 0x00},
            {0x45, 0x20},
            {0x47, 0x08},
            {0x48, 0x28},
            {0x67, 0x00},
            {0x70, 0x04},
            {0x71, 0x01},
            {0x72, 0xFE},
            {0x76, 0x00},
            {0x77, 0x00},
            {0xFF, 0x01},
            {0x0D, 0x01},
            {0xFF, 0x00},
            {0x80, 0x01},
            {0x01, 0xF8},
            {0xFF, 0x01},
            {0x8E, 0x01},
            {0x00, 0x01},
            {0xFF, 0x00},
            {0x80, 0x00}
    };

    private static final int MAYBE_TIMER_REG = 0x83;
    private static final int[][] SPAD_1 = {
            {0x80, 0x01},
            {0xFF, 0x01},
            {0x00, 0x00},
            {0xFF, 0x06}
    };
    private static final int[][] SPAD_2 = {
            {0xFF, 0x07},
            {0x81, 0x01},
            {0x80, 0x01},
            {0x94, 0x6B},
            {MAYBE_TIMER_REG, 0x00}
    };
    private static final int[][] SPAD_3 = {
            {0x81, 0x00},
            {0xFF, 0x06}
    };
    private static final int[][] SPAD_4 = {
            {0xFF, 0x01},
            {0x00, 0x01},
            {0xFF, 0x00},
            {0x80, 0x00}
    };

    private static final int IO_TIMEOUT = 10;
    private int stopByte;

    public VL53L0X(I2C i2c, ScienceLab scienceLab) throws IOException, InterruptedException {
        this.i2c = i2c;
        if (scienceLab.isConnected()) {
            for (int[] regValPair : new int[][]{
                    {0x88, 0x00},
                    {0x80, 0x01},
                    {0xFF, 0x01},
                    {0x00, 0x00}
            }) {
                i2c.write(ADDRESS, new int[]{regValPair[1]}, regValPair[0]);
            }
            stopByte = i2c.readByte(ADDRESS, 0x91);

            for (int[] regValPair : new int[][]{
                    {0x00, 0x01},
                    {0xFF, 0x00},
                    {0x80, 0x00}
            }) {
                i2c.write(ADDRESS, new int[]{regValPair[1]}, regValPair[0]);
            }
            int configControl = i2c.readByte(ADDRESS, MSRC_CONFIG_CONTROL) | (DISABLE_SIGNAL_RATE_MSRC | DISABLE_SIGNAL_RATE_PRE_RANGE);

            i2c.write(ADDRESS, new int[configControl], MSRC_CONFIG_CONTROL);

            i2c.write(ADDRESS, new int[0xFF], SYSTEM_SEQUENCE_CONFIG);

            spadConfig();

            for (int[] regValPair : TUNING_CONFIG) {
                i2c.write(ADDRESS, new int[]{regValPair[1]}, regValPair[0]);
            }

            i2c.write(ADDRESS, new int[]{0x04}, SYSTEM_INTERRUPT_CONFIG_GPIO);
            int gpioHvMuxActiveHigh = i2c.readByte(ADDRESS, GPIO_HV_MUX_ACTIVE_HIGH);
            i2c.write(ADDRESS, new int[]{gpioHvMuxActiveHigh & ~0x10}, GPIO_HV_MUX_ACTIVE_HIGH);
            i2c.write(ADDRESS, new int[]{0x01}, SYSTEM_INTERRUPT_CLEAR);
            i2c.write(ADDRESS, new int[]{0xE8}, SYSTEM_SEQUENCE_CONFIG);

            i2c.write(ADDRESS, new int[]{0x01}, SYSTEM_SEQUENCE_CONFIG);
            performSingleRefCalibration(0x40);
            i2c.write(ADDRESS, new int[]{0x01}, SYSTEM_SEQUENCE_CONFIG);
            i2c.write(ADDRESS, new int[]{0x02}, SYSTEM_SEQUENCE_CONFIG);
            performSingleRefCalibration(0x00);

            i2c.write(ADDRESS, new int[]{0xE8}, SYSTEM_SEQUENCE_CONFIG);
        }
    }

    private int[] getSpadInfo() throws IOException {
        for (int[] regValPair : SPAD_1) {
            i2c.write(ADDRESS, new int[]{regValPair[1]}, regValPair[0]);
        }

        int uu = i2c.readByte(ADDRESS, MAYBE_TIMER_REG) | 0x04;
        i2c.write(ADDRESS, new int[]{uu}, MAYBE_TIMER_REG);

        for (int[] regValPair : SPAD_2) {
            i2c.write(ADDRESS, new int[]{regValPair[1]}, regValPair[0]);
        }

        long start = System.currentTimeMillis();

        while (i2c.readByte(ADDRESS, MAYBE_TIMER_REG) == 0x00) {
            if (IO_TIMEOUT > 0 && (System.currentTimeMillis() - start) / 1000.0 >= IO_TIMEOUT) {
                throw new RuntimeException("Timeout waiting for VL53L0X!");
            }
        }

        i2c.write(ADDRESS, new int[]{0x01}, MAYBE_TIMER_REG);

        int tmp = i2c.readByte(ADDRESS, 0X92);
        int count = tmp & 0x7F;
        boolean isAperture = ((tmp >> 7) & 0x01) == 1;

        for (int[] regValPair : SPAD_3) {
            i2c.write(ADDRESS, new int[]{regValPair[1]}, regValPair[0]);
        }

        int vv = i2c.readByte(ADDRESS, MAYBE_TIMER_REG) & ~0x04;
        i2c.write(ADDRESS, new int[]{vv}, MAYBE_TIMER_REG);

        for (int[] regValPair : SPAD_4) {
            i2c.write(ADDRESS, new int[]{regValPair[1]}, regValPair[0]);
        }
        return new int[]{count, isAperture ? 1 : 0};
    }

    private void spadConfig() throws IOException {
        int[] spadInfo = getSpadInfo();
        int spadCount = spadInfo[0];
        int spadIsAperture = spadInfo[1];

        i2c.write(ADDRESS, new int[]{0}, GLOBAL_CONFIG_SPAD_ENABLES_REF_0);
        ArrayList<Integer> spadMap = i2c.read(ADDRESS, 6, GLOBAL_CONFIG_SPAD_ENABLES_REF_0);

        for (int[] regValPair : SPAD_CONFIG) {
            i2c.write(ADDRESS, new int[]{regValPair[1]}, regValPair[0]);
        }

        int firstSpadToEnable = (spadIsAperture == 1) ? 12 : 0;
        int spadsEnabled = 0;

        for (int i = 0; i < 48; i++) {
            int index = i / 8;
            if (i < firstSpadToEnable || spadsEnabled == spadCount) {
                spadMap.set(index, spadMap.get(index) & ~(1 << (i % 8)));
            } else if (((spadMap.get(index) >> (i % 8)) & 0x1) > 0) {
                spadsEnabled++;
            }
        }

        i2c.write(ADDRESS, spadMap.stream().mapToInt(Integer::intValue).toArray(), GLOBAL_CONFIG_SPAD_ENABLES_REF_0);
    }

    private void performSingleRefCalibration(int vhvInitByte) throws IOException {
        i2c.write(ADDRESS, new int[]{0x01 | vhvInitByte & 0xFF}, SYSRANGE_START);
        long start = System.currentTimeMillis();

        while ((i2c.readByte(ADDRESS, RESULT_INTERRUPT_STATUS) & 0x07) == 0) {
            if (IO_TIMEOUT > 0 && (System.currentTimeMillis() - start) / 1000.0 >= IO_TIMEOUT) {
                throw new RuntimeException("Timeout waiting for VL53L0X!");
            }
        }
        i2c.write(ADDRESS, new int[]{0x01}, SYSTEM_INTERRUPT_CLEAR);
        i2c.write(ADDRESS, new int[]{0x00}, SYSRANGE_START);
    }

    public int getRaw() throws IOException {
        for (int[] regValPair : new int[][]{
                {0x80, 0x01},
                {0xFF, 0x01},
                {0x00, 0x00},
                {0x91, stopByte},
                {0x00, 0x01},
                {0xFF, 0x00},
                {0x80, 0x00},
                {SYSRANGE_START, 0x01}
        }) {
            i2c.write(ADDRESS, new int[]{regValPair[1]}, regValPair[0]);
        }

        long start = System.currentTimeMillis();

        while ((i2c.readByte(ADDRESS, SYSRANGE_START) & 0x01) > 0) {
            if (IO_TIMEOUT > 0 && (System.currentTimeMillis() - start) / 1000.0 >= IO_TIMEOUT) {
                throw new RuntimeException("Timeout waiting for VL53L0X!");
            }
        }

        start = System.currentTimeMillis();

        while ((i2c.readByte(ADDRESS, RESULT_INTERRUPT_STATUS) & 0x07) == 0) {
            if (IO_TIMEOUT > 0 && (System.currentTimeMillis() - start) / 1000.0 >= IO_TIMEOUT) {
                throw new RuntimeException("Timeout waiting for VL53L0X!");
            }
        }

        ArrayList<Integer> data = i2c.read(ADDRESS, 2, RESULT_RANGE_STATUS + 10);
        i2c.write(ADDRESS, new int[]{0x01}, SYSTEM_INTERRUPT_CLEAR);
        return ((data.get(0) & 0xFF) << 8) | (data.get(1) & 0xFF);
    }
}
