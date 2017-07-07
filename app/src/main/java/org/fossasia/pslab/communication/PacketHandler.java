package org.fossasia.pslab.communication;

import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Created by viveksb007 on 28/3/17.
 */

public class PacketHandler {

    private static final String TAG = "PacketHandler";
    private final int BUFSIZE = 10000;
    private byte[] buffer = new byte[BUFSIZE];
    private boolean loadBurst, connected;
    int inputQueueSize = 0, BAUD = 1000000;
    private CommunicationHandler mCommunicationHandler = null;
    public static String version = "";
    private CommandsProto mCommandsProto;
    private int timeout = 500, VERSION_STRING_LENGTH = 15;
    ByteBuffer burstBuffer = ByteBuffer.allocate(2000);

    public PacketHandler(int timeout, CommunicationHandler communicationHandler) {
        this.loadBurst = false;
        this.connected = false;
        this.timeout = timeout;
        this.mCommandsProto = new CommandsProto();
        this.mCommunicationHandler = communicationHandler;
        connected = mCommunicationHandler.isConnected();
    }

    public boolean isConnected() {
        connected = mCommunicationHandler.isConnected();
        return connected;
    }

    public String getVersion() {
        try {
            sendByte(mCommandsProto.COMMON);
            sendByte(mCommandsProto.GET_VERSION);
            // Read "<PSLAB Version String>\n"
            mCommunicationHandler.read(buffer, VERSION_STRING_LENGTH + 1, timeout);
            version = new String(Arrays.copyOfRange(buffer, 0, VERSION_STRING_LENGTH), Charset.forName("UTF-8"));
        } catch (IOException e) {
            Log.e("Error in Communication", e.toString());
        }
        return version;
    }

    public void sendByte(int val) throws IOException {
        if (!connected) {
            throw new IOException("Device not connected");
        }
        if (!loadBurst) {
            try {
                mCommunicationHandler.write(new byte[]{(byte) (val & 0xff)}, timeout);
            } catch (IOException e) {
                Log.e("Error in sending byte", e.toString());
                e.printStackTrace();
            }
        } else {
            burstBuffer.put((byte) (val & 0xff));
        }
    }

    public void sendInt(int val) throws IOException {
        if (!connected) {
            throw new IOException("Device not connected");
        }
        if (!loadBurst) {
            try {
                mCommunicationHandler.write(new byte[]{(byte) (val & 0xff), (byte) ((val >> 8) & 0xff)}, timeout);
            } catch (IOException e) {
                Log.e("Error in sending int", e.toString());
                e.printStackTrace();
            }
        } else {
            burstBuffer.put(new byte[]{(byte) (val & 0xff), (byte) ((val >> 8) & 0xff)});
        }
    }

    public int getAcknowledgement() {
        /*
        fetches the response byte
        1 SUCCESS
        2 ARGUMENT_ERROR
        3 FAILED
        used as a handshake
        */
        if (loadBurst) {
            inputQueueSize++;
            return 1;
        } else {
            try {
                mCommunicationHandler.read(buffer, 1, timeout);
                return buffer[0];
            } catch (IOException e) {
                e.printStackTrace();
                return 3;
            }
        }
    }

    public byte getByte() {
        try {
            int numByteRead = mCommunicationHandler.read(buffer, 1, timeout);
            if (numByteRead == 1) {
                return buffer[0];
            } else {
                Log.e(TAG, "Error in reading byte");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int getInt() {
        try {
            int numByteRead = mCommunicationHandler.read(buffer, 2, timeout);
            if (numByteRead == 2) {
                // LSB is read first
                return (buffer[0] & 0xff) | ((buffer[1] << 8) & 0xff00);
            } else {
                Log.e(TAG, "Error in reading byte");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public long getLong() {
        try {
            int numByteRead = mCommunicationHandler.read(buffer, 4, timeout);
            if (numByteRead == 4) {
                // C++ has long of 4-bytes but in Java int has 4-bytes
                // refer "https://stackoverflow.com/questions/7619058/convert-a-byte-array-to-integer-in-java-and-vice-versa" for Endian
                return ByteBuffer.wrap(Arrays.copyOfRange(buffer, 0, 4)).order(ByteOrder.LITTLE_ENDIAN).getInt();
            } else {
                Log.e(TAG, "Error in reading byte");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean waitForData() {
        return false;
    }

    public int read(byte[] dest, int bytesToRead) throws IOException {
        int numBytesRead = mCommunicationHandler.read(buffer, bytesToRead, timeout);
        for (int i = 0; i < bytesToRead; i++) {
            dest[i] = buffer[i];
        }
        if (numBytesRead == bytesToRead) {
            return numBytesRead;
        } else {
            Log.e(TAG, "Error in packetHandler Reading");
        }
        return -1;
    }

    public byte[] sendBurst() {
        try {
            mCommunicationHandler.write(burstBuffer.array(), timeout);
            burstBuffer.clear();
            loadBurst = false;
            int bytesRead = mCommunicationHandler.read(buffer, inputQueueSize, timeout);
            inputQueueSize = 0;
            return Arrays.copyOfRange(buffer, 0, bytesRead);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[]{-1};
    }

}