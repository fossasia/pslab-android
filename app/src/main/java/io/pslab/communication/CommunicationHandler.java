package io.pslab.communication;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.hoho.android.usbserial.driver.CdcAcmSerialDriver;
import com.hoho.android.usbserial.driver.Cp21xxSerialDriver;
import com.hoho.android.usbserial.driver.ProbeTable;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.IOException;
import java.util.List;

public class CommunicationHandler {
    private final String TAG = this.getClass().getSimpleName();
    private static final int PSLAB_VENDOR_ID_V5 = 1240;
    private static final int PSLAB_PRODUCT_ID_V5 = 223;
    private static final int PSLAB_VENDOR_ID_V6 = 0x10C4;
    private static final int PSLAB_PRODUCT_ID_V6 = 0xEA60;
    private boolean connected = false, device_found = false;
    private UsbManager mUsbManager;
    private UsbDeviceConnection mConnection;
    private UsbSerialDriver driver;
    private UsbSerialPort port;
    public UsbDevice mUsbDevice;
    List<UsbSerialDriver> drivers;

    private static final int DEFAULT_READ_BUFFER_SIZE = 32 * 1024;
    private static final int DEFAULT_WRITE_BUFFER_SIZE = 32 * 1024;

    private byte[] mReadBuffer;
    private byte[] mWriteBuffer;

    public CommunicationHandler(UsbManager usbManager) {
        this.mUsbManager = usbManager;
        mUsbDevice = null;
        ProbeTable customTable = new ProbeTable();
        customTable.addProduct(PSLAB_VENDOR_ID_V5, PSLAB_PRODUCT_ID_V5, CdcAcmSerialDriver.class);
        customTable.addProduct(PSLAB_VENDOR_ID_V6, PSLAB_PRODUCT_ID_V6, Cp21xxSerialDriver.class);

        UsbSerialProber prober = new UsbSerialProber(customTable);
        drivers = prober.findAllDrivers(usbManager);

        if (drivers.isEmpty()) {
            Log.d(TAG, "No drivers found");
        } else {
            Log.d(TAG, "Found PSLab device");
            device_found = true;
            driver = drivers.get(0);
            mUsbDevice = driver.getDevice();
        }
    }

    public void open() throws IOException {
        if (!device_found) {
            throw new IOException("Device not Connected");
        }
        mConnection = mUsbManager.openDevice(mUsbDevice);
        if (mConnection == null) {
            throw new IOException("Could not open device.");
        }
        connected = true;
        port = driver.getPorts().get(0);
        port.open(mConnection);
        port.setParameters(1000000, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

        mReadBuffer = new byte[DEFAULT_READ_BUFFER_SIZE];
        mWriteBuffer = new byte[DEFAULT_WRITE_BUFFER_SIZE];
        clear();
        //Thread.sleep(1000);
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
        port.close();
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
            readNow = port.read(mReadBuffer, bytesToBeReadTemp, timeoutMillis);
            if (readNow == 0) {
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

    public void write(byte[] src, int timeoutMillis) throws IOException {
        int writeLength;
        writeLength = mWriteBuffer.length;
        port.write(src, writeLength, timeoutMillis);
    }

    private void clear() throws IOException {
        port.read(mReadBuffer, 100, 50);
    }
}
