package org.fossasia.pslab.communication;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;

public class CommunicationHandler {

    private final String TAG = this.getClass().getSimpleName();
    private static final int PSLAB_VENDOR_ID = 1240;
    private static final int PSLAB_PRODUCT_ID = 223;

    private UsbInterface mControlInterface;
    private UsbInterface mDataInterface;

    private UsbEndpoint mControlEndpoint;
    private UsbEndpoint mReadEndpoint;
    private UsbEndpoint mWriteEndpoint;

    private boolean mRts = false;
    private boolean mDtr = false;
    private boolean connected = false, device_found = false;

    private static final int USB_RECIP_INTERFACE = 0x01;
    private static final int USB_RT_ACM = UsbConstants.USB_TYPE_CLASS | USB_RECIP_INTERFACE;

    private static final int SET_LINE_CODING = 0x20;  // USB CDC 1.1 section 6.2
    private static final int GET_LINE_CODING = 0x21;
    private static final int SET_CONTROL_LINE_STATE = 0x22;
    private static final int SEND_BREAK = 0x23;

    public static final int DEFAULT_READ_BUFFER_SIZE = 16 * 1024;
    public static final int DEFAULT_WRITE_BUFFER_SIZE = 16 * 1024;

    public UsbDevice mUsbDevice = null;

    protected final Object mReadBufferLock = new Object();
    protected final Object mWriteBufferLock = new Object();

    protected byte[] mReadBuffer;
    protected byte[] mWriteBuffer;

    private UsbDeviceConnection mConnection;

    private UsbManager mUsbManager;

    public CommunicationHandler(UsbManager usbManager) {
        this.mUsbManager = usbManager;
        mUsbDevice = null;
        for (final UsbDevice device : mUsbManager.getDeviceList().values()) {
            Log.d(TAG, "VID : " + device.getVendorId() + "PID : " + device.getProductId());
            if (device.getVendorId() == PSLAB_VENDOR_ID && device.getProductId() == PSLAB_PRODUCT_ID) {
                Log.d(TAG, "Found PSLAB Device");
                mUsbDevice = device;
                device_found = true;
                break;
            }
        }
        mReadBuffer = new byte[DEFAULT_READ_BUFFER_SIZE];
        mWriteBuffer = new byte[DEFAULT_WRITE_BUFFER_SIZE];
    }

    public void open() throws IOException {
        if (!device_found) {
            throw new IOException("Device not Connected");
        }
        mConnection = mUsbManager.openDevice(mUsbDevice);
        Log.d(TAG, "Claiming interfaces, count=" + mUsbDevice.getInterfaceCount());

        mControlInterface = mUsbDevice.getInterface(0);
        Log.d(TAG, "Control interface=" + mControlInterface);

        if (!mConnection.claimInterface(mControlInterface, true)) {
            throw new IOException("Could not claim control interface.");
        }

        mControlEndpoint = mControlInterface.getEndpoint(0);
        Log.d(TAG, "Control endpoint direction: " + mControlEndpoint.getDirection());

        Log.d(TAG, "Claiming data interface.");
        mDataInterface = mUsbDevice.getInterface(1);
        Log.d(TAG, "data interface=" + mDataInterface);

        if (!mConnection.claimInterface(mDataInterface, true)) {
            throw new IOException("Could not claim data interface.");
        }
        mReadEndpoint = mDataInterface.getEndpoint(1);
        Log.d(TAG, "Read endpoint direction: " + mReadEndpoint.getDirection());
        mWriteEndpoint = mDataInterface.getEndpoint(0);
        Log.d(TAG, "Write endpoint direction: " + mWriteEndpoint.getDirection());
        connected = true;
        setBaudRate(1000000);
        //Thread.sleep(1000);
        clear();
    }

    public boolean isDeviceFound() {
        return device_found;
    }

    public boolean isConnected() {
        return connected;
    }

    public void close() throws IOException {
        if (mConnection == null) {
            return;
        }
        mConnection.releaseInterface(mDataInterface);
        mConnection.releaseInterface(mControlInterface);
        mConnection.close();
        connected = false;
        mConnection = null;
    }

    public int read(byte[] dest, int bytesToBeRead, int timeoutMillis) throws IOException {
        int numBytesRead = 0;
        //synchronized (mReadBufferLock) {
        int readNow;
        Log.v(TAG, "TO read : " + bytesToBeRead);
        int bytesToBeReadTemp = bytesToBeRead;
        while (numBytesRead < bytesToBeRead) {
            readNow = mConnection.bulkTransfer(mReadEndpoint, mReadBuffer, bytesToBeReadTemp, timeoutMillis);
            if (readNow < 0) {
                Log.e(TAG, "Read Error: " + bytesToBeReadTemp);
                return numBytesRead;
            } else {
                //Log.v(TAG, "Read something" + mReadBuffer);
                System.arraycopy(mReadBuffer, 0, dest, numBytesRead, readNow);
                numBytesRead += readNow;
                bytesToBeReadTemp -= readNow;
                //Log.v(TAG, "READ : " + numBytesRead);
                //Log.v(TAG, "REMAINING: " + bytesToBeRead);
            }
        }
        //}
        Log.v("Bytes Read", "" + numBytesRead);
        return numBytesRead;
    }

    public int write(byte[] src, int timeoutMillis) throws IOException {
        if (Build.VERSION.SDK_INT < 18) {
            return writeSupportAPI(src, timeoutMillis);
        }
        int written = 0;
        while (written < src.length) {
            int writeLength, amtWritten;
            //synchronized (mWriteBufferLock) {
            writeLength = Math.min(mWriteBuffer.length, src.length - written);
            // bulk transfer supports offset from API 18
            amtWritten = mConnection.bulkTransfer(mWriteEndpoint, src, written, writeLength, timeoutMillis);
            //}
            if (amtWritten < 0) {
                throw new IOException("Error writing " + writeLength
                        + " bytes at offset " + written + " length=" + src.length);
            }
            written += amtWritten;
        }
        return written;
    }


    // For supporting devices with API version < 18
    public int writeSupportAPI(byte[] src, int timeoutMillis) throws IOException {
        int written = 0;
        while (written < src.length) {
            final int writeLength;
            final int amtWritten;
            //synchronized (mWriteBufferLock) {
            final byte[] writeBuffer;
            writeLength = Math.min(src.length - written, mWriteBuffer.length);
            if (written == 0) {
                writeBuffer = src;
            } else {
                // bulkTransfer does not support offsets for API level < 18, so make a copy.
                System.arraycopy(src, written, mWriteBuffer, 0, writeLength);
                writeBuffer = mWriteBuffer;
            }
            amtWritten = mConnection.bulkTransfer(mWriteEndpoint, writeBuffer, writeLength, timeoutMillis);
            //}
            if (amtWritten <= 0) {
                throw new IOException("Error writing " + writeLength
                        + " bytes at offset " + written + " length=" + src.length);
            }
            written += amtWritten;
        }
        return written;
    }


    public void clear() {
        mConnection.bulkTransfer(mReadEndpoint, mReadBuffer, 100, 50);
    }

    public void setBaudRate(int baudRate) {
        byte[] msg = {
                (byte) (baudRate & 0xff),
                (byte) ((baudRate >> 8) & 0xff),
                (byte) ((baudRate >> 16) & 0xff),
                (byte) ((baudRate >> 24) & 0xff),
                (byte) 0,
                (byte) 0,
                (byte) 8};
        sendAcmControlMessage(SET_LINE_CODING, 0, msg);
        SystemClock.sleep(100);
        clear();
    }

    private int sendAcmControlMessage(int request, int value, byte[] buf) {
        return mConnection.controlTransfer(USB_RT_ACM, request, value, 0, buf, buf != null ? buf.length : 0, 5000);
    }

    public void setDtrRts() {
        int value = (mRts ? 0x2 : 0) | (mDtr ? 0x1 : 0);
        sendAcmControlMessage(SET_CONTROL_LINE_STATE, value, null);
    }

}
