package com.viveksb007.pslab.communication.peripherals;

import android.util.Log;

import com.viveksb007.pslab.communication.CommandsProto;
import com.viveksb007.pslab.communication.PacketHandler;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by viveksb007 on 28/3/17.
 */

public class SPI {

    private static final String TAG = "SPI";
    private PacketHandler packetHandler;
    private CommandsProto commandsProto;
    int CKE = 1, CKP = 0, SMP = 1;

    public SPI(PacketHandler packetHandler) {
        this.packetHandler = packetHandler;
        this.commandsProto = new CommandsProto();
    }

    public void setParameters(int primaryPreScalar, int secondaryPreScalar, int CKE, int CKP, int SMP) throws IOException {
        if (CKE != -1) this.CKE = CKE;
        if (CKP != -1) this.CKP = CKP;
        if (SMP != -1) this.SMP = SMP;

        packetHandler.sendByte(commandsProto.SPI_HEADER);
        packetHandler.sendByte(commandsProto.SET_SPI_PARAMETERS);
        packetHandler.sendByte(secondaryPreScalar | (primaryPreScalar << 3) | (CKE << 5) | (CKP << 6) | (SMP << 7));
        packetHandler.getAcknowledgement();
    }

    public void start(int channel) throws IOException {
        packetHandler.sendByte(commandsProto.SPI_HEADER);
        packetHandler.sendByte(commandsProto.START_SPI);
        packetHandler.sendByte(channel);
    }

    public void setCS(String channel, int state) throws IOException {
        String[] chipSelect = new String[]{"CS1", "CS2"};
        channel = channel.toUpperCase();
        if (Arrays.asList(chipSelect).contains(channel)) {
            int csNum = Arrays.asList(chipSelect).indexOf(channel) + 9;
            packetHandler.sendByte(commandsProto.SPI_HEADER);
            if (state == 1)
                packetHandler.sendByte(commandsProto.STOP_SPI);
            else
                packetHandler.sendByte(commandsProto.START_SPI);
            packetHandler.sendByte(csNum);
        } else {
            Log.d(TAG, "Channel does not exist");
        }
    }

    public void stop(int channel) throws IOException {
        packetHandler.sendByte(commandsProto.SPI_HEADER);
        packetHandler.sendByte(commandsProto.STOP_SPI);
        packetHandler.sendByte(channel);
    }

    public byte send8(int value) throws IOException {
        packetHandler.sendByte(commandsProto.SPI_HEADER);
        packetHandler.sendByte(commandsProto.SEND_SPI8);
        packetHandler.sendByte(value);
        byte retValue = packetHandler.getByte();
        packetHandler.getAcknowledgement();
        return retValue;
    }

    public int send16(int value) throws IOException {
        packetHandler.sendByte(commandsProto.SPI_HEADER);
        packetHandler.sendByte(commandsProto.SEND_SPI16);
        packetHandler.sendInt(value);
        int retValue = packetHandler.getInt();
        packetHandler.getAcknowledgement();
        return retValue;
    }

    public void send8Burst(int value) throws IOException {
        packetHandler.sendByte(commandsProto.SPI_HEADER);
        packetHandler.sendByte(commandsProto.SEND_SPI8_BURST);
        packetHandler.sendByte(value);
    }

    public void send16Burst(int value) throws IOException {
        packetHandler.sendByte(commandsProto.SPI_HEADER);
        packetHandler.sendByte(commandsProto.SEND_SPI16_BURST);
        packetHandler.sendInt(value);
    }

}
