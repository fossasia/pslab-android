package com.viveksb007.pslab.communication;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
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

    private static final int USB_RECIP_INTERFACE = 0x01;
    private static final int USB_RT_ACM = UsbConstants.USB_TYPE_CLASS | USB_RECIP_INTERFACE;

    private static final int SET_LINE_CODING = 0x20;  // USB CDC 1.1 section 6.2
    private static final int GET_LINE_CODING = 0x21;
    private static final int SET_CONTROL_LINE_STATE = 0x22;
    private static final int SEND_BREAK = 0x23;

    public static final int DEFAULT_READ_BUFFER_SIZE = 16 * 1024;
    public static final int DEFAULT_WRITE_BUFFER_SIZE = 16 * 1024;

    public UsbDevice mUsbDevice;

    protected final Object mReadBufferLock = new Object();
    protected final Object mWriteBufferLock = new Object();

    protected byte[] mReadBuffer;
    protected byte[] mWriteBuffer;

    private UsbDeviceConnection mConnection;

    public boolean device_found = false;
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

    }

    private int sendAcmControlMessage(int request, int value, byte[] buf) {
        return mConnection.controlTransfer(USB_RT_ACM, request, value, 0, buf, buf != null ? buf.length : 0, 5000);
    }

    public void setDtrRts() {
        int value = (mRts ? 0x2 : 0) | (mDtr ? 0x1 : 0);
        sendAcmControlMessage(SET_CONTROL_LINE_STATE, value, null);
    }

}
