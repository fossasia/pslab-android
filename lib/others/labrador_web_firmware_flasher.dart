import 'dart:async';
import 'dart:js_interop';
import 'dart:js_interop_unsafe';
import 'dart:typed_data';
import 'package:web/web.dart' as web;

@JS()
extension type _USB(JSObject _) implements JSObject {
  external JSPromise<_USBDevice> requestDevice(
      _USBDeviceRequestOptions options);
}

extension type _USBDeviceRequestOptions._(JSObject _) implements JSObject {
  external factory _USBDeviceRequestOptions(
      {JSArray<_USBDeviceFilter> filters});
}

extension type _USBDeviceFilter._(JSObject _) implements JSObject {
  external factory _USBDeviceFilter({int vendorId, int? productId});
}

@JS()
extension type _USBDevice(JSObject _) implements JSObject {
  external int get productId;
  external JSPromise<JSAny?> open();
  external JSPromise<JSAny?> close();
  external JSPromise<JSAny?> selectConfiguration(int configurationValue);
  external JSPromise<JSAny?> claimInterface(int interfaceNumber);
  external JSPromise<_USBInTransferResult> controlTransferIn(
      _USBControlTransferParameters setup, int length);
  external JSPromise<_USBOutTransferResult> controlTransferOut(
      _USBControlTransferParameters setup,
      [JSAny? data]);
}

extension type _USBControlTransferParameters._(JSObject _) implements JSObject {
  external factory _USBControlTransferParameters({
    required String requestType,
    required String recipient,
    required int request,
    required int value,
    required int index,
  });
}

@JS()
extension type _USBInTransferResult(JSObject _) implements JSObject {
  external JSDataView? get data;
}

@JS()
extension type _USBOutTransferResult(JSObject _) implements JSObject {
  external int get bytesWritten;
}

class LabradorWebFirmwareFlasher {
  final Function(double progress, String status) onProgress;
  final Function() onSuccess;
  final Function(String error) onError;

  LabradorWebFirmwareFlasher({
    required this.onProgress,
    required this.onSuccess,
    required this.onError,
  });

  Future<void> flashFirmware(String hexContent) async {
    _USBDevice? dev;
    try {
      final navigator = web.window.navigator as JSObject;
      if (!navigator.has('usb')) {
        throw Exception(
            "WebUSB is not supported in this browser. Please use Chrome, Edge, or Brave.");
      }

      onProgress(0.05, "Selecting USB device...");
      final usb = _USB(navigator['usb'] as JSObject);
      final filters = [
        _USBDeviceFilter(vendorId: 0x03EB, productId: 0x2FE4), // DFU Bootloader
        _USBDeviceFilter(vendorId: 0x03EB, productId: 0xBA94), // Runtime
      ];
      final options = _USBDeviceRequestOptions(filters: filters.toJS);
      dev = await usb.requestDevice(options).toDart;

      if (dev.productId == 0xBA94) {
        await _safeOpen(dev);
        try {
          final jumpSetup = _USBControlTransferParameters(
            requestType: 'vendor',
            recipient: 'device',
            request: 0xA7,
            value: 1,
            index: 0,
          );
          await dev.controlTransferOut(jumpSetup).toDart;
        } catch (_) {}
        try {
          await dev.close().toDart;
        } catch (_) {}

        throw Exception(
            "Labrador board was in runtime mode and has now rebooted into DFU bootloader mode!\n\n"
            "Please click 'Flash Labrador Firmware' again and select the DFU bootloader device (VID 03EB, PID 2FE4).");
      }

      await _safeOpen(dev);
      try {
        await dev.selectConfiguration(1).toDart;
      } catch (_) {}
      await dev.claimInterface(0).toDart;

      int blockCounter = 0;
      onProgress(0.10, "Initializing DFU state machine...");
      await _clrStatus(dev);
      onProgress(0.15, "Erasing ATxmega32A4U flash memory...");
      final eraseCmd = Uint8List.fromList([0x04, 0x00, 0xFF, 0x00, 0x00, 0x00]);
      await _dnload(dev, blockCounter++, eraseCmd);

      int eraseRetries = 30;
      while (eraseRetries-- > 0) {
        await Future.delayed(const Duration(milliseconds: 50));
        final stat = await _getStatus(dev);
        if (stat[0] == 0) break;
        if (stat[0] == 0x09 && stat[1] == 0x04) continue;
        await _clrStatus(dev);
      }

      onProgress(0.25, "Selecting flash memory unit...");
      final selectMemCmd =
          Uint8List.fromList([0x06, 0x03, 0x00, 0x00, 0x00, 0x00]);
      await _dnload(dev, blockCounter++, selectMemCmd);
      final selectPageCmd =
          Uint8List.fromList([0x06, 0x03, 0x01, 0x00, 0x00, 0x00]);
      await _dnload(dev, blockCounter++, selectPageCmd);
      onProgress(0.30, "Parsing Intel HEX firmware image...");
      final (flashImage, maxAddr) = _parseHex(hexContent);
      int endAddr = maxAddr;
      int rem = endAddr % 512;
      if (rem != 0) endAddr += 512 - rem;
      if (endAddr > 32768) endAddr = 32768;

      int totalBytes = endAddr;
      int written = 0;

      for (int offset = 0; offset < endAddr; offset += 512) {
        int chunkSize = 512;
        if (offset + chunkSize > endAddr) chunkSize = endAddr - offset;

        int startHi = (offset >> 8) & 0xFF;
        int startLo = offset & 0xFF;
        int endHi = ((offset + chunkSize - 1) >> 8) & 0xFF;
        int endLo = (offset + chunkSize - 1) & 0xFF;

        final cmd =
            Uint8List.fromList([0x01, 0x00, startHi, startLo, endHi, endLo]);
        final buffer = Uint8List(64 + chunkSize);
        buffer.setRange(0, 6, cmd);
        buffer.fillRange(6, 64, 0);
        buffer.setRange(
            64, 64 + chunkSize, flashImage.sublist(offset, offset + chunkSize));

        await _dnload(dev, blockCounter++, buffer);
        final stat = await _getStatus(dev);
        if (stat[0] != 0) {
          throw Exception(
              "Write failed at offset 0x${offset.toRadixString(16)} (status: 0x${stat[0].toRadixString(16)})");
        }

        written += chunkSize;
        double prog = 0.30 + ((written / totalBytes) * 0.60);
        onProgress(prog, "Writing flash: ${(prog * 100).toInt()}%");
      }

      onProgress(0.92, "Launching application firmware...");
      final launchCmd =
          Uint8List.fromList([0x04, 0x03, 0x00, 0x00, 0x00, 0x00]);
      await _dnload(dev, blockCounter++, launchCmd);
      await Future.delayed(const Duration(milliseconds: 100));
      try {
        await _dnload(dev, blockCounter++, launchCmd);
      } catch (_) {}

      onProgress(1.0, "SUCCESS! Labrador bulk firmware flashed successfully.");
      onSuccess();
    } catch (e) {
      onError(e.toString().replaceAll("Exception: ", ""));
    } finally {
      if (dev != null) {
        try {
          await dev.close().toDart;
        } catch (_) {}
      }
    }
  }

  Future<void> _clrStatus(_USBDevice dev) async {
    final setup = _USBControlTransferParameters(
      requestType: 'class',
      recipient: 'interface',
      request: 4,
      value: 0,
      index: 0,
    );
    await dev.controlTransferOut(setup, Uint8List(0).toJS).toDart;
  }

  Future<List<int>> _getStatus(_USBDevice dev) async {
    final setup = _USBControlTransferParameters(
      requestType: 'class',
      recipient: 'interface',
      request: 3,
      value: 0,
      index: 0,
    );
    final res = await dev.controlTransferIn(setup, 6).toDart;
    if (res.data != null) {
      final bd = res.data!.toDart;
      return [bd.getUint8(0), bd.getUint8(4)]; // bStatus, bState
    }
    return [0, 0];
  }

  Future<void> _dnload(_USBDevice dev, int blockNum, Uint8List data) async {
    final setup = _USBControlTransferParameters(
      requestType: 'class',
      recipient: 'interface',
      request: 1,
      value: blockNum,
      index: 0,
    );
    await dev.controlTransferOut(setup, data.toJS).toDart;
  }

  Future<void> _safeOpen(_USBDevice dev) async {
    try {
      await dev.open().toDart;
    } catch (e) {
      final err = e.toString().toLowerCase();
      if (err.contains('access denied') || err.contains('securityerror')) {
        throw Exception(
            "Access Denied (SecurityError) opening USB device in browser.\n\n"
            "On Windows, Chromium (Chrome / Edge) WebUSB strictly requires the 'WinUSB' driver, "
            "but this device currently has 'libusb0' or another driver assigned.\n\n"
            "Solutions:\n"
            "1. Recommended: Run the native Windows Desktop app (flutter run -d windows), which natively supports libusb0 without needing driver changes.\n"
            "2. Or to use Web in Chrome: Open Zadig (zadig-2.9.exe in Downloads), select Options -> List All Devices, choose ATxmega32A4U / Labrador, switch driver to 'WinUSB', and click 'Replace Driver'.");
      }
      rethrow;
    }
  }

  (Uint8List, int) _parseHex(String hexStr) {
    final flash = Uint8List(32768);
    flash.fillRange(0, 32768, 0xFF);
    int maxAddr = 0;
    int extAddr = 0;

    for (final rawLine in hexStr.split('\n')) {
      final line = rawLine.trim();
      if (!line.startsWith(':') || line.length < 11) continue;
      final len = int.parse(line.substring(1, 3), radix: 16);
      final addr = int.parse(line.substring(3, 7), radix: 16);
      final type = int.parse(line.substring(7, 9), radix: 16);

      if (type == 0x00) {
        final fullAddr = extAddr + addr;
        for (int i = 0; i < len; i++) {
          final target = fullAddr + i;
          if (target < 32768) {
            flash[target] =
                int.parse(line.substring(9 + i * 2, 11 + i * 2), radix: 16);
            if (target + 1 > maxAddr) maxAddr = target + 1;
          }
        }
      } else if (type == 0x02) {
        extAddr = int.parse(line.substring(9, 13), radix: 16) << 4;
      } else if (type == 0x04) {
        extAddr = int.parse(line.substring(9, 13), radix: 16) << 16;
      } else if (type == 0x01) {
        break;
      }
    }
    return (flash, maxAddr);
  }
}
