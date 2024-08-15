package io.pslab.communication.sensors;

import android.util.Log;

import io.pslab.communication.ScienceLab;
import io.pslab.communication.peripherals.I2C;

import java.io.IOException;
import java.util.ArrayList;

public class CCS811 {
    private static final int ADDRESS = 0x5A;
    private final I2C i2c;

    // Figure 14: CCS811 Application Register Map
    private static final int ALG_RESULT_DATA = 0x02;  // ALG_RESULT_DATA # R 8 bytes Algorithm result. The most significant 2 bytes contain a up to ppm estimate of the equivalent CO2 (eCO2) level, and
    private static final int HW_ID = 0x20;            // HW_ID # R 1 byte Hardware ID. The value is 0x81
    private static final int FW_BOOT_VERSION = 0x23;  // FW_Boot_Version # R 2 bytes firmware version number for the boot code. Firmware Application Version. The first 2 bytes contain
    private static final int FW_APP_VERSION = 0x24;   // FW_App_Version # R 2 bytes the firmware version number for the application code
    private static final int MEAS_MODE = 0x01;        // MEAS_MODE # R/W 1 byte Measurement mode and conditions register Algorithm result.

    // Figure 25: CCS811 Bootloader Register Map
    // Address Register R/W Size Description
    private static final int HW_Version = 0x21;
    private static final int APP_START = 0xF4;

    // Measurement Mode
    private static final int DRIVE_MODE_1SEC = 0x01;

    public CCS811(I2C i2c, ScienceLab scienceLab) throws Exception {
        this.i2c = i2c;
        if (scienceLab.isConnected()) {
            fetchID();
            appStart();
            Thread.sleep(100);
            disableInterrupt();
            setMeasMode();
        }
    }

    private void setMeasMode() throws IOException {
        i2c.write(ADDRESS, new int[]{1 << 2 | CCS811.DRIVE_MODE_1SEC << 4}, MEAS_MODE);
    }

    private void disableInterrupt() throws IOException {
        i2c.write(ADDRESS, new int[]{1 << 2 | 3 << 4}, MEAS_MODE);
    }

    private void fetchID() throws IOException, InterruptedException {
        int hardwareId = i2c.read(ADDRESS, 1, HW_ID).get(0) & 0xFF;
        Thread.sleep(20);
        int hardwareVersion = i2c.read(ADDRESS, 1, HW_Version).get(0) & 0xFF;
        Thread.sleep(20);
        int bootVersion = i2c.read(ADDRESS, 2, FW_BOOT_VERSION).get(0) & 0xFF;
        Thread.sleep(20);
        int appVersion = i2c.read(ADDRESS, 2, FW_APP_VERSION).get(0) & 0xFF;
        Thread.sleep(20);

        Log.d("CCS811", "Hardware ID: " + hardwareId);
        Log.d("CCS811", "Hardware Version: " + hardwareVersion);
        Log.d("CCS811", "Boot Version: " + bootVersion);
        Log.d("CCS811", "App Version: " + appVersion);
    }

    private void appStart() throws IOException {
        i2c.write(ADDRESS, new int[]{}, APP_START);
    }

    private String decodeError(int error) {
        String e = "";
        if ((error & (1)) > 0) {
            e += ", The CCS811 received an I²C write request addressed to this station but with invalid register address ID";
        }
        if ((error & (1 << 1)) > 0) {
            e += ", The CCS811 received an I²C read request to a mailbox ID that is invalid";
        }
        if ((error & (1 << 2)) > 0) {
            e += ", The CCS811 received an I²C request to write an unsupported mode to MEAS_MODE";
        }
        if ((error & (1 << 3)) > 0) {
            e += ", The sensor resistance measurement has reached or exceeded the maximum range";
        }
        if ((error & (1 << 4)) > 0) {
            e += ", The Heater current in the CCS811 is not in range";
        }
        if ((error & (1 << 5)) > 0) {
            e += ", The Heater voltage is not being applied correctly";
        }
        return "Error: " + e.substring(2);
    }

    public int[] getRaw() throws IOException {
        ArrayList<Integer> data = i2c.read(ADDRESS, 8, ALG_RESULT_DATA);
        int eCO2 = ((data.get(0) & 0xFF) << 8) | (data.get(1) & 0xFF);
        int TVOC = ((data.get(2) & 0xFF) << 8) | (data.get(3) & 0xFF);
        int errorId = data.get(5) & 0xFF;

        if (errorId > 0) {
            Log.d("CCS811", decodeError(errorId));
        }
        return (new int[]{eCO2, TVOC});
    }
}
