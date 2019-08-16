package io.pslab.others;

import android.util.Log;

import io.pslab.communication.CommunicationHandler;
import io.pslab.communication.ScienceLab;

/**
 * Created by viveksb007 on 8/5/17.
 */

public class ScienceLabCommon {

    private static final String TAG = "ScienceLabCommon";
    private static ScienceLabCommon scienceLabCommon = null;
    public static ScienceLab scienceLab;
    public boolean connected = false;
    public static boolean isWifiConnected = false;
    private static String espBaseIP = "";

    private ScienceLabCommon() {
    }

    public boolean openDevice(CommunicationHandler communicationHandler) {
        scienceLab = new ScienceLab(communicationHandler);
        if (!scienceLab.isConnected()) {
            Log.e(TAG, "Error in connection");
            return false;
        }
        connected = true;
        return true;
    }

    public static ScienceLabCommon getInstance() {
        if (scienceLabCommon == null) {
            scienceLabCommon = new ScienceLabCommon();
        }
        return scienceLabCommon;
    }

    public static String getEspIP() {
        return espBaseIP;
    }

    public static void setEspBaseIP(String espBaseIP) {
        ScienceLabCommon.espBaseIP = espBaseIP;
    }

    public static boolean isWifiConnected() {
        return isWifiConnected;
    }

    public static void setIsWifiConnected(boolean wifiConnected) {
        isWifiConnected = wifiConnected;
    }
}
