package org.fossasia.pslab.communication.sensors;

import android.util.Log;

import org.fossasia.pslab.communication.peripherals.SPI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Created by akarshan on 4/22/17.
 * <p>
 * ScienceLab instance of SPI need to be passed to the MF522 constructor.
 * refer https://github.com/fossasia/pslab-python/blob/development/PSL/SENSORS/MF522.py#L465
 * to port the code in sciencelab.java
 * </p>
 */

public class MF522 {
    private String TAG = "MF522";
    private int CommandReg = 0x01 << 1;             // starts and stops command execution
    private int ComIEnReg = 0x02 << 1;              // enable and disable interrupt request control bits
    private int DivIEnReg = 0x03 << 1;              // enable and disable interrupt request control bits
    private int ComIrqReg = 0x04 << 1;              // interrupt request bits
    private int DivIrqReg = 0x05 << 1;              // interrupt request bits
    private int ErrorReg = 0x06 << 1;               // error bits showing the error status of the last command executed
    private int Status1Reg = 0x07 << 1;             // communication status bits
    private int Status2Reg = 0x08 << 1;              // receiver and transmitter status bits
    private int FIFODataReg = 0x09 << 1;             // input and output of 64 byte FIFO buffer
    private int FIFOLevelReg = 0x0A << 1;            // number of bytes stored in the FIFO buffer
    private int WaterLevelReg = 0x0B << 1;           // level for FIFO underflow and overflow warning
    private int ControlReg = 0x0C << 1;              // miscellaneous control registers
    private int BitFramingReg = 0x0D << 1;           // adjustments for bit-oriented frames
    private int CollReg = 0x0E << 1;                 // bit position of the first bit-collision detected on the RF sciencelab

    private int ModeReg = 0x11 << 1;                 // defines general modes for transmitting and receiving
    private int TxModeReg = 0x12 << 1;               // defines transmission data rate and framing
    private int RxModeReg = 0x13 << 1;               // defines reception data rate and framing
    private int TxControlReg = 0x14 << 1;            // controls the logical behavior of the antenna driver pins TX1 and TX2
    private int TxASKReg = 0x15 << 1;                // controls the setting of the transmission modulation
    private int TxSelReg = 0x16 << 1;                // selects the internal sources for the antenna driver
    private int RxSelReg = 0x17 << 1;                // selects internal receiver settings
    private int RxThresholdReg = 0x18 << 1;          // selects thresholds for the bit decoder
    private int DemodReg = 0x19 << 1;                // defines demodulator settings
    private int MfTxReg = 0x1C << 1;                 // controls some MIFARE communication transmit parameters
    private int MfRxReg = 0x1D << 1;                 // controls some MIFARE communication receive parameters
    private int SerialSpeedReg = 0x1F << 1;          // selects the speed of the serial UART sciencelab

    private int CRCResultRegH = 0x21 << 1;           // shows the MSB and LSB values of the CRC calculation
    private int CRCResultRegL = 0x22 << 1;
    private int ModWidthReg = 0x24 << 1;             // controls the ModWidth setting?
    private int RFCfgReg = 0x26 << 1;                // configures the receiver gain
    private int GsNReg = 0x27 << 1;                  // selects the conductance of the antenna driver pins TX1 and TX2 for modulation
    private int CWGsPReg = 0x28 << 1;                // defines the conductance of the p-driver output during periods of no modulation
    private int ModGsPReg = 0x29 << 1;               // defines the conductance of the p-driver output during periods of modulation
    private int TModeReg = 0x2A << 1;                // defines settings for the internal timer
    private int TPrescalerReg = 0x2B << 1;           // the lower 8 bits of the TPrescaler value. The 4 high bits are in TModeReg.
    private int TReloadRegH = 0x2C << 1;             // defines the 16-bit timer reload value
    private int TReloadRegL = 0x2D << 1;
    private int TCounterValueRegH = 0x2E << 1;       // shows the 16-bit timer value
    private int TCounterValueRegL = 0x2F << 1;

    private int TestSel1Reg = 0x31 << 1;            // general test signal configuration
    private int TestSel2Reg = 0x32 << 1;            // general test signal configuration
    private int TestPinEnReg = 0x33 << 1;           // enables pin output driver on pins D1 to D7
    private int TestPinValueReg = 0x34 << 1;        // defines the values for D1 to D7 when it is used as an I/O bus
    private int TestBusReg = 0x35 << 1;             // shows the status of the internal test bus
    private int AutoTestReg = 0x36 << 1;            // controls the digital self test
    private int VersionReg = 0x37 << 1;             // shows the software version
    private int AnalogTestReg = 0x38 << 1;          // controls the pins AUX1 and AUX2
    private int TestDAC1Reg = 0x39 << 1;            // defines the test value for TestDAC1
    private int TestDAC2Reg = 0x3A << 1;            // defines the test value for TestDAC2
    private int TestADCReg = 0x3B << 1;             // shows the value of ADC I and Q channels

    // MFRC522 commands. Described in chapter 10 of the datasheet.

    private int PCD_Idle = 0x00;                    //no action, cancels current command execution
    private int PCD_Mem = 0x01;                     //stores 25 bytes into the internal buffer
    private int PCD_GenerateRandomID = 0x02;        //generates a 10-byte random ID number
    private int PCD_CalcCRC = 0x03;                 //activates the CRC coprocessor or performs a self test
    private int PCD_Transmit = 0x04;                // transmits data from the FIFO buffer
    private int PCD_NoCmdChange = 0x07;
    private int PCD_Receive = 0x08;                 //activates the receiver circuits
    private int PCD_Transceive = 0x0C;              //transmits data from FIFO buffer to antenna and automatically activates the receiver after transmission
    private int PCD_MFAuthent = 0x0E;               //performs the MIFARE standard authentication as a reader
    private int PCD_SoftReset = 0x0F;               //resets the MFRC522

    private int RxGain_18dB = 0x00 << 4;            // 000b - 18 dB, minimum
    private int RxGain_23dB = 0x01 << 4;            // 001b - 23 dB
    private int RxGain_18dB_2 = 0x02 << 4;          // 010b - 18 dB, it seems 010b is a duplicate for 000b
    private int RxGain_23dB_2 = 0x03 << 4;          // 011b - 23 dB, it seems 011b is a duplicate for 001b
    private int RxGain_33dB = 0x04 << 4;            // 100b - 33 dB, average, and typical default
    private int RxGain_38dB = 0x05 << 4;            // 101b - 38 dB
    private int RxGain_43dB = 0x06 << 4;            // 110b - 43 dB
    private int RxGain_48dB = 0x07 << 4;            // 111b - 48 dB, maximum
    private int RxGain_min = 0x00 << 4;             // 000b - 18 dB, minimum, convenience for RxGain_18dB
    private int RxGain_avg = 0x04 << 4;             // 100b - 33 dB, average, convenience for RxGain_33dB
    private int RxGain_max = 0x07 << 4;             // 111b - 48 dB, maximum, convenience for RxGain_48dB

    // The commands used by the PCD to manage communication with several PICCs (ISO 14443-3, Type A, section 6.4)

    private int PICC_CMD_REQA = 0x26;               // REQuest command, Type A. Invites PICCs in state IDLE to go to READY and prepare for anticollision or selection
    private int PICC_CMD_WUPA = 0x52;               // Wake-UP command, prepare for anticollision or selection. 7 bit frame.
    private int PICC_CMD_CT = 0x88;                 // Cascade Tag. Not really a command, but used during anti collision.
    private int PICC_CMD_SEL_CL1 = 0x93;            // Anti collision/Select, Cascade Level 1
    private int PICC_CMD_SEL_CL2 = 0x95;            // Anti collision/Select, Cascade Level 2
    private int PICC_CMD_SEL_CL3 = 0x97;            // Anti collision/Select, Cascade Level 3
    private int PICC_CMD_HLTA = 0x50;               // HaLT command, Type A. Instructs an ACTIVE PICC to go to state HALT.

    // The commands used for MIFARE Classic (from http://www.mouser.com/ds/2/302/MF1S503x-89574.pdf, Section 9)
    // Use PCD_MFAuthent to authenticate access to a sector, then use these commands to read/write/modify the blocks on the sector.
    // The read/write commands can also be used for MIFARE Ultralight.

    private int PICC_CMD_MF_AUTH_KEY_A = 0x60;      // Perform authentication with Key A
    private int PICC_CMD_MF_AUTH_KEY_B = 0x61;      // Perform authentication with Key B
    private int PICC_CMD_MF_READ = 0x30;            // Reads one 16 byte block from the authenticated sector of the PICC. Also used for MIFARE Ultralight.
    private int PICC_CMD_MF_WRITE = 0xA0;           // Writes one 16 byte block to the authenticated sector of the PICC. Called "COMPATIBILITY WRITE" for MIFARE Ultralight.
    private int PICC_CMD_MF_DECREMENT = 0xC0;       // Decrements the contents of a block and stores the result in the internal data register.
    private int PICC_CMD_MF_INCREMENT = 0xC1;       // Increments the contents of a block and stores the result in the internal data register.
    private int PICC_CMD_MF_RESTORE = 0xC2;         // Reads the contents of a block into the internal data register.
    private int PICC_CMD_MF_TRANSFER = 0xB0;        // Writes the contents of the internal data register to a block.


    private int NRSTPD = 22;
    private int MAX_LEN = 16;
    private int MI_OK = 0;
    private int MI_NOTAGERR = 1;
    private int MI_ERR = 2;

    private int PCD_CALCCRC = 0x03;

    private int PICC_REQIDL = 0x26;
    private int PICC_REQALL = 0x52;
    private int PICC_ANTICOLL = 0x93;
    private int PICC_SElECTTAG = 0x93;
    private int PICC_AUTHENT1A = 0x60;
    private int PICC_AUTHENT1B = 0x61;
    private int PICC_READ = 0x30;
    private int PICC_WRITE = 0xA0;
    private int PICC_DECREMENT = 0xC0;
    private int PICC_INCREMENT = 0xC1;
    private int PICC_RESTORE = 0xC2;
    private int PICC_TRANSFER = 0xB0;
    private int PICC_HALT = 0x50;

    // The commands used for MIFARE Ultralight (from http://www.nxp.com/documents/data_sheet/MF0ICU1.pdf, Section 8.6)
    // The PICC_CMD_MF_READ and PICC_CMD_MF_WRITE can also be used for MIFARE Ultralight.

    private int PICC_CMD_UL_WRITE = 0xA2;       //Writes one 4 byte page to the PICC.

    private int MF_ACK = 0xA;                   // The MIFARE Classic uses a 4 bit ACK/NAK. Any other value than 0xA is NAK.
    private int MF_KEY_SIZE = 6;                // A Mifare Crypto1 key is 6 bytes.
    private String cs;
    private SPI spi;
    private Boolean connected;

    public MF522(SPI spi, String cs) throws IOException, InterruptedException {
        this.cs = cs;
        this.spi = spi;
        spi.setParameters(2, 1, 1, 0, 1);
        if (!reset()) {
            connected = false;
        }
        write(TModeReg, 0x80);
        write(TPrescalerReg, 0xA9);
        write(TReloadRegH, 0x03);
        write(TReloadRegL, 0xE8);

        write(TxASKReg, 0x40);
        write(ModeReg, 0x3D);
        enableAntenna();        //Enable the antenna
    }

    public void enableAntenna() throws IOException {
        int val = read(TxControlReg);
        if ((val & 0x03) != 0x03)
            write(TxControlReg, val | 0x03);
    }

    public boolean reset() throws IOException, InterruptedException {
        write(CommandReg, PCD_SoftReset);
        long startTime = System.currentTimeMillis();
        while ((read(CommandReg) & (1 << 4)) != 0) {
            Log.v(TAG, "wait");
            TimeUnit.MILLISECONDS.sleep(100);
            if (System.currentTimeMillis() - startTime > 0.5)
                return false;
        }
        return true;
    }

    public int write(int register, int val) throws IOException {
        spi.setCS(cs, 0);
        int ret = spi.send16(((register & 0x7F) << 8) | val);
        spi.setCS(cs, 1);
        return ret & 0xFF;
    }

    public int read(int register) throws IOException {
        spi.setCS(cs, 0);
        int ret = spi.send16(((register & 0x80) << 8));
        spi.setCS(cs, 1);
        return ret & 0xFF;
    }

    public ArrayList<Byte> readMany(int register, int total) throws IOException {
        spi.setCS(cs, 0);
        spi.send8(register);
        ArrayList<Byte> vals = new ArrayList<>();
        for (int a = 0; a < total - 1; a++)
            vals.add(spi.send8(register));
        vals.add(spi.send8(0));
        spi.setCS(cs, 1);
        return vals;
    }

    public int getStatus() throws IOException {
        return read(Status1Reg);
    }

    public int getVersion() throws IOException {
        int version = read(VersionReg);
        if (version == 0x88) Log.v(TAG, "Cloned version: Fudan Semiconductors");
        else if (version == 0x90) Log.v(TAG, "version 1.0");
        else if (version == 0x91) Log.v(TAG, "version 1.0");
        else if (version == 0x92) Log.v(TAG, "version 2.0");
        else Log.v(TAG, "Unknown version " + version);
        return version;
    }

    public void setBitMask(int register, int mask) throws IOException {
        int tmp = read(register);
        write(register, tmp | mask);
    }

    public void clearBitMask(int register, int mask) throws IOException {
        int tmp = read(register);
        write(register, tmp & (~mask));
    }

    public ArrayList<Object> MFRC522ToCard(int command, ArrayList<Integer> sendData) throws IOException {
        ArrayList<Integer> returnedData = new ArrayList<>();
        int backLen = 0;
        int status = MI_ERR;
        int irqEn = 0x00;
        int waitIRq = 0x00;
        int lastBits;
        int n = 0;
        int i = 0;

        if (command == PCD_MFAuthent) {
            irqEn = 0x12;
            waitIRq = 0x10;
        }
        if (command == PCD_Transceive) {
            irqEn = 0x77;
            waitIRq = 0x30;
        }
        write(ComIEnReg, irqEn | 0x80);
        clearBitMask(ComIrqReg, 0x80);
        setBitMask(FIFOLevelReg, 0x80);

        write(CommandReg, PCD_Idle);

        for (int a = 0; a < sendData.size(); a++)
            write(FIFODataReg, a);
        write(CommandReg, command);

        if (command == PCD_Transceive)
            setBitMask(BitFramingReg, 0x80);

        i = 2000;
        while (true) {
            n = read(ComIrqReg);
            i = i - 1;
            if (!(i != 0 && ~(n & 0x01) != 0 && ~(n & waitIRq) != 0))         //needs to be checked
                break;
        }

        clearBitMask(BitFramingReg, 0x80);

        if (i != 0) {
            if ((read(ErrorReg) & 0x1B) == 0x00) {
                status = MI_OK;
                if ((n & irqEn & 0x01) != 0)
                    status = MI_NOTAGERR;
                if (command == PCD_Transceive) {
                    n = read(FIFOLevelReg);
                    lastBits = read(ControlReg) & 0x07;
                    if (lastBits != 0)
                        backLen = (n - 1) * 8 + lastBits;
                    else
                        backLen = n * 8;
                    if (n == 0)
                        n = 1;
                    if (n > MAX_LEN)
                        n = MAX_LEN;

                    i = 0;
                    while (i < n) {
                        returnedData.add(read(FIFODataReg));
                        i = i + 1;
                    }
                }
            } else
                status = MI_ERR;
        }
        return new ArrayList<Object>(Arrays.asList(status, returnedData, backLen));
    }

    public ArrayList<Object> MFRC522Request(int reqMode) throws IOException, NullPointerException {
        ArrayList<Object> mfrc522ToCard;
        ArrayList<Integer> returnedData = new ArrayList<>();
        int backBits = 0;
        int status;
        ArrayList<Integer> TagType = new ArrayList<>();

        write(BitFramingReg, 0x07);

        TagType.add(reqMode);
        mfrc522ToCard = MFRC522ToCard(PCD_Transceive, TagType);
        status = (int) mfrc522ToCard.get(0);
        returnedData = (ArrayList<Integer>) mfrc522ToCard.get(1);
        backBits = (int) mfrc522ToCard.get(2);

        if (status != MI_OK | backBits != 0x10)
            status = MI_ERR;

        return new ArrayList(Arrays.asList(status, returnedData));
    }

    public ArrayList<Object> MFRC522Anticoll() throws IOException {
        ArrayList<Integer> returnedData;
        int status;
        int backLen;
        int serNumCheck = 0;
        ArrayList<Integer> serNum = new ArrayList<>();

        write(BitFramingReg, 0x00);

        serNum.add(PICC_ANTICOLL);
        serNum.add(0x20);

        ArrayList<Object> mfrc522ToCard = MFRC522ToCard(PCD_Transceive, serNum);
        status = (int) mfrc522ToCard.get(0);
        returnedData = (ArrayList<Integer>) mfrc522ToCard.get(1);
        backLen = (int) mfrc522ToCard.get(2);
        if (status == MI_OK) {
            int i = 0;
            if (returnedData.size() == 5) {
                while (i < 4) {
                    serNumCheck = serNumCheck ^ returnedData.get(i);
                    i = i + 1;
                }
                if (serNumCheck != returnedData.get(i))
                    status = MI_ERR;
            } else status = MI_ERR;
        }
        return new ArrayList<Object>(Arrays.asList(status, returnedData));

    }

    public ArrayList<Integer> calulateCRC(ArrayList<Integer> pIndata) throws IOException {
        int n;
        ArrayList<Integer> pOutData = new ArrayList<>();
        clearBitMask(DivIrqReg, 0x04);
        setBitMask(FIFOLevelReg, 0x80);
        for (int a = 0; a < pIndata.size(); a++)
            write(FIFODataReg, a);
        write(CommandReg, PCD_CALCCRC);
        for (int i = 0; i < 0xFF; i++) {
            n = read(DivIrqReg);
            if ((n & 0x040) != 0)
                break;
        }
        pOutData.add(read(CRCResultRegL));
        pOutData.add(read(CRCResultRegH));
        return pOutData;
    }

    public int MFRC522SelectTag(ArrayList<Integer> serNum) throws IOException {
        ArrayList<Integer> returnedData;
        ArrayList<Integer> buf = new ArrayList<>();
        int status;
        int backLen;

        buf.add(PICC_SElECTTAG);
        buf.add(0x70);
        int i = 0;
        while (i < 5) {
            buf.add(serNum.get(i));
            i = i + 1;
        }
        ArrayList<Integer> pOut = calulateCRC(buf);
        buf.add(pOut.get(0));
        buf.add(pOut.get(1));
        ArrayList mfrc522ToCard = MFRC522ToCard(PCD_Transceive, buf);
        status = (int) mfrc522ToCard.get(0);
        returnedData = (ArrayList<Integer>) mfrc522ToCard.get(1);
        backLen = (int) mfrc522ToCard.get(2);
        if ((status == MI_OK) && (backLen == 0x18)) {
            return returnedData.get(0);
        } else
            return 0;
    }

    public int MFRC522Auth(int authMode, int blockAddress, int[] sectorkey, int[] serNum) throws IOException {
        ArrayList<Integer> buff = new ArrayList<>();
        ArrayList<Integer> returnedData;
        int status;
        int backLen;
        // First byte should be the authMode (A or B)
        buff.add(authMode);
        // Second byte is the trailerBlock (usually 7)
        buff.add(blockAddress);
        // Now we need to append the authKey which usually is 6 bytes of 0xFF
        int i = 0;
        while (i < sectorkey.length) {
            buff.add(sectorkey[i]);
            i = i + 1;
        }
        i = 0;
        // Next we append the first 4 bytes of the UID
        while (i < 4) {
            buff.add(serNum[i]);
            i = i + 1;
        }
        // Now we start the authentication itself
        ArrayList<Object> mfrc522ToCard = MFRC522ToCard(PCD_MFAuthent, buff);
        status = (int) mfrc522ToCard.get(0);
        returnedData = (ArrayList<Integer>) mfrc522ToCard.get(1);
        backLen = (int) mfrc522ToCard.get(2);

        // Check if an error occurred
        if (status != MI_OK)
            Log.v(TAG, "AUTH ERROR !!");
        if ((read(Status2Reg) & 0x08) == 0)
            Log.v(TAG, "AUTH ERROR(status2reg & 0x08) != 0");

        // Return the status
        return status;
    }

    public void MFRC522StopCrypto1() throws IOException {
        clearBitMask(Status2Reg, 0x08);
        setBitMask(CommandReg, 0x10);
    }

    public ArrayList<Integer> MFRC522Read(int blockAddress) throws IOException {
        ArrayList<Integer> recvData = new ArrayList<>();
        int status;
        int backLen;
        ArrayList<Integer> returnedData;

        recvData.add(PICC_READ);
        recvData.add(blockAddress);
        ArrayList<Integer> pOut = calulateCRC(recvData);
        recvData.add(pOut.get(0));
        recvData.add(pOut.get(1));
        ArrayList<Object> mfrc522ToCard = MFRC522ToCard(PCD_Transceive, recvData);

        status = (int) mfrc522ToCard.get(0);
        returnedData = (ArrayList<Integer>) mfrc522ToCard.get(1);
        backLen = (int) mfrc522ToCard.get(2);

        if (status != MI_OK) {
            Log.v(TAG, "Error while reading!");
        }
        return returnedData;
    }

    public void MFRC522Write(int blockAddress, int[] writeData) throws IOException {
        ArrayList<Integer> buff = new ArrayList<>();
        int status;
        int backLen;
        ArrayList<Integer> returnedData;

        buff.add(PICC_WRITE);
        buff.add(blockAddress);
        ArrayList<Integer> crc = calulateCRC(buff);
        buff.add(crc.get(0));
        buff.add(crc.get(1));

        ArrayList<Object> mfrc522ToCard = MFRC522ToCard(PCD_Transceive, buff);
        status = (int) mfrc522ToCard.get(0);
        returnedData = (ArrayList<Integer>) mfrc522ToCard.get(1);
        backLen = (int) mfrc522ToCard.get(2);

        if ((status != MI_OK) || (backLen != 4) || ((returnedData.get(0) & 0x0F) != 0x0A))
            status = MI_ERR;

        Log.v(TAG, backLen + " returnedData &0x0F == 0x0A " + (returnedData.get(0) & 0x0F));
        if (status == MI_OK) {
            int i = 0;
            ArrayList<Integer> buf = new ArrayList<>();

            while (i < 16) {
                buf.add(writeData[i]);
                i = i + 1;
            }

            ArrayList<Integer> bufCRC = calulateCRC(buf);
            buf.add(bufCRC.get(0));
            buf.add(bufCRC.get(1));

            mfrc522ToCard = MFRC522ToCard(PCD_Transceive, buff);
            status = (int) mfrc522ToCard.get(0);
            returnedData = (ArrayList<Integer>) mfrc522ToCard.get(1);
            backLen = (int) mfrc522ToCard.get(2);
            if ((status != MI_OK) || (backLen != 4) || ((returnedData.get(0) & 0x0F) != 0x0A))
                Log.v(TAG, "Error while writing");
            if (status == MI_OK)
                Log.v(TAG, "Data written");
        }

    }

    public void MFRC522DumpClassic1K(int key[], int uid[]) throws IOException {
        int i = 0;
        while (i < 64) {
            int status = MFRC522Auth(PICC_AUTHENT1A, i, key, uid);
            // Check if authenticated
            if (status == MI_OK)
                MFRC522Read(i);
            else {
                Log.v(TAG, "Authentication error");
                i = i + 1;
            }
        }
    }
}
