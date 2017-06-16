package org.fossasia.pslab.others;

import android.util.Log;

import org.fossasia.pslab.communication.CommunicationHandler;
import org.fossasia.pslab.communication.ScienceLab;

/**
 * Created by viveksb007 on 8/5/17.
 */

public class ScienceLabCommon {

    private static final String TAG = "ScienceLabCommon";
    private static ScienceLabCommon scienceLabCommon = null;
    public static ScienceLab scienceLab;
    public boolean connected = false;

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
}
