package com.viveksb007.pslab.communication;

import android.hardware.usb.UsbManager;
import android.util.Log;

import java.io.IOException;

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

    private CommunicationHandler mCommunicationHandler;
    private PacketHandler mPacketHandler;

    public ScienceLab(UsbManager usbManager) {
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
}
