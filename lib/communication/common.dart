import 'dart:async';
import 'package:pslab/communication/handler/base.dart';
import 'package:pslab/communication/packet_handler.dart';
import 'package:pslab/others/logger_service.dart';

class Common {
  late CommunicationHandler mCommunicationHandler;
  late PacketHandler mPacketHandler;

  Common(CommunicationHandler communicationHandler) {
    mCommunicationHandler = communicationHandler;
  }

  bool isConnected() {
    return mCommunicationHandler.isConnected();
  }

  bool isDeviceFound() {
    return mCommunicationHandler.isDeviceFound();
  }

  Future<void> connect() async {
    if (isDeviceFound()) {
      try {
        await mCommunicationHandler.open();
        mPacketHandler = PacketHandler(500, mCommunicationHandler);
      } catch (e) {
        logger.e(e);
      }
    }
    if (isConnected()) {
      if (mCommunicationHandler.openedBoard?.version == 'EspoTek Labrador') {
        return;
      }
      await getVersion();
      await initializeVariables();
    }
  }

  Future<void> connectWiFi() async {
    try {
      await mCommunicationHandler.open();
      mPacketHandler = PacketHandler(500, mCommunicationHandler);
    } catch (e) {
      logger.e(e);
    }
    if (isConnected()) {
      await getVersion();
      await initializeVariables();
    }
  }

  Future<String> getVersion() async {
    if (mCommunicationHandler.openedBoard?.version == 'EspoTek Labrador') {
      return 'EspoTek Labrador';
    }
    if (isConnected()) {
      return await mPacketHandler.getVersion();
    } else {
      return 'Not Connected';
    }
  }

  Future<int> getFirmwareVersion() async {
    if (isConnected()) {
      return await mPacketHandler.getFirmwareVersion();
    }
    return 0;
  }

  Future<void> initializeVariables() async {}
}

typedef CommonCommunication = Common;
