package com.viveksb007.pslab.communication;

import android.hardware.usb.UsbManager;
import android.util.Log;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Created by viveksb007 on 28/3/17.
 */

public class PacketHandler {

    private final int BUFSIZE = 2000;
    private byte[] buffer = new byte[BUFSIZE];
    private boolean loadBurst, connected;
    int inputQueueSize = 0, BAUD = 1000000;
    private CommunicationHandler mCommunicationHandler = null;
    String version = "", expectedVersion = "CS";
    private CommandsProto mCommandsProto;
    private int timeout = 500;

    public PacketHandler(int timeout, UsbManager usbManager) {
        this.loadBurst = false;
        this.connected = false;
        this.timeout = timeout;
        this.mCommandsProto = new CommandsProto();
        this.mCommunicationHandler = new CommunicationHandler(usbManager);
        connected = mCommunicationHandler.isConnected();
    }

    public String getVersion() throws IOException {
        try {
            sendByte(mCommandsProto.COMMON);
            sendByte(mCommandsProto.GET_VERSION);
            mCommunicationHandler.read(buffer, 6, timeout);
            version = new String(Arrays.copyOfRange(buffer, 1, 6), Charset.forName("UTF-8"));
        } catch (IOException e) {
            Log.e("Error in Communication", e.toString());
        }
        return version;
    }

    private void sendByte(int val) throws IOException {
        if (!connected) {
            throw new IOException("Device not connected");
        }
        try {
            mCommunicationHandler.write(new byte[]{(byte) (val & 0xff)}, timeout);
        } catch (IOException e) {
            Log.e("Error in sending byte", e.toString());
            e.printStackTrace();
        }
    }

    private void sendInt(int val) throws IOException {
        if (!connected) {
            throw new IOException("Device not connected");
        }
        try {
            mCommunicationHandler.write(new byte[]{(byte) (val & 0xff), (byte) ((val >> 8) & 0xff)}, timeout);
        } catch (IOException e) {
            Log.e("Error in sending int", e.toString());
            e.printStackTrace();
        }
    }


}
