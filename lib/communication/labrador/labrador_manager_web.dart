import 'dart:async';
import 'dart:js_interop';
import 'dart:js_interop_unsafe';
import 'dart:typed_data';

import 'package:web/web.dart' as web;
import 'package:pslab/others/logger_service.dart';
import 'package:pslab/src/rust/api/labrador.dart' as rust_labrador;

@JS()
extension type USB(JSObject _) implements JSObject {
  external JSPromise<USBDevice> requestDevice(USBDeviceRequestOptions options);
  external JSPromise<JSArray<USBDevice>> getDevices();
}

extension type USBDeviceRequestOptions._(JSObject _) implements JSObject {
  external factory USBDeviceRequestOptions({JSArray<USBDeviceFilter> filters});
}

extension type USBDeviceFilter._(JSObject _) implements JSObject {
  external factory USBDeviceFilter({int vendorId, int? productId});
}

@JS()
extension type USBConfiguration(JSObject _) implements JSObject {
  external int get configurationValue;
}

@JS()
extension type USBDevice(JSObject _) implements JSObject {
  external bool get opened;
  external int get vendorId;
  external int get productId;
  external String? get productName;
  external String? get manufacturerName;
  external USBConfiguration? get configuration;
  external JSPromise<JSAny?> open();
  external JSPromise<JSAny?> close();
  external JSPromise<JSAny?> selectConfiguration(int configurationValue);
  external JSPromise<JSAny?> claimInterface(int interfaceNumber);
  external JSPromise<JSAny?> releaseInterface(int interfaceNumber);
  external JSPromise<JSAny?> selectAlternateInterface(
      int interfaceNumber, int alternateSetting);
  external JSPromise<USBInTransferResult> transferIn(
      int endpointNumber, int length);
  external JSPromise<USBOutTransferResult> transferOut(
      int endpointNumber, JSAny data);
  external JSPromise<USBInTransferResult> controlTransferIn(
      USBControlTransferParameters setup, int length);
  external JSPromise<USBOutTransferResult> controlTransferOut(
      USBControlTransferParameters setup,
      [JSAny? data]);
  external JSPromise<JSAny?> clearHalt(String direction, int endpointNumber);
  external JSPromise<JSAny?> reset();
}

extension type USBControlTransferParameters._(JSObject _) implements JSObject {
  external factory USBControlTransferParameters({
    required String requestType,
    required String recipient,
    required int request,
    required int value,
    required int index,
  });
}

@JS()
extension type USBInTransferResult(JSObject _) implements JSObject {
  external JSDataView? get data;
  external String get status;
}

@JS()
extension type USBOutTransferResult(JSObject _) implements JSObject {
  external int get bytesWritten;
  external String get status;
}

extension NavigatorUSB on web.Navigator {
  USB get webUsb => USB((this as JSObject)['usb'] as JSObject);
}

abstract class LabradorPlatformManager {
  static final LabradorPlatformManager _instance =
      _LabradorPlatformManagerWeb();
  static LabradorPlatformManager get instance => _instance;

  bool get isWebSupported;
  String? get lastError;
  Future<bool> connectWeb();
  Future<void> sendControl({
    required int request,
    required int value,
    int index = 0,
    Uint8List? data,
  });
  Future<void> disconnect();
}

class _LabradorPlatformManagerWeb implements LabradorPlatformManager {
  USBDevice? _device;
  bool _connected = false;
  bool _isStreaming = false;
  String? _lastError;

  @override
  String? get lastError => _lastError;

  @override
  bool get isWebSupported {
    final navigator = web.window.navigator as JSObject;
    return web.window.isSecureContext && navigator.has('usb');
  }

  @override
  Future<bool> connectWeb() async {
    _lastError = null;
    if (!isWebSupported) {
      _lastError =
          "WebUSB API not available or context not secure (HTTPS / localhost required).";
      logger.e(_lastError!);
      return false;
    }

    try {
      logger.i(
          "Requesting EspoTek Labrador USB device (VID: 0x03EB, PID: 0xBA94)...");
      final filter = USBDeviceFilter(vendorId: 0x03EB, productId: 0xBA94);
      final options = USBDeviceRequestOptions(filters: [filter].toJS);

      _device = await web.window.navigator.webUsb.requestDevice(options).toDart;
      logger.i(
          "Device granted: ${_device!.productName ?? 'Labrador'}. Opening device...");

      try {
        await _device!.open().toDart;
      } catch (openErr) {
        final err = openErr.toString().toLowerCase();
        if (err.contains('access denied') || err.contains('securityerror')) {
          _lastError =
              "Access Denied (SecurityError) opening Labrador USB device in browser.\n\n"
              "On Windows, Chromium (Chrome / Edge) WebUSB requires the 'WinUSB' driver, "
              "but the device currently has 'libusb0' or another driver bound.\n\n"
              "Solutions:\n"
              "1. Run the native Windows Desktop app (flutter run -d windows).\n"
              "2. Or to use Web in Chrome: Open Zadig (zadig-2.9.exe in Downloads), select Options -> List All Devices, choose Labrador, switch driver to 'WinUSB', and click 'Replace Driver'.";
          logger.e(_lastError!);
          await disconnect();
          return false;
        }
        rethrow;
      }
      logger.d("Labrador device opened successfully.");

      try {
        if (_device!.configuration == null ||
            _device!.configuration!.configurationValue != 1) {
          await _device!.selectConfiguration(1).toDart;
          logger.d("Configuration 1 selected.");
        }
      } catch (e) {
        logger.w("selectConfiguration warning (ignorable): $e");
      }

      try {
        await _device!.claimInterface(0).toDart;
        logger.d("Claimed interface 0 (control).");
      } catch (e) {
        logger.w("claimInterface(0) warning (ignorable): $e");
      }

      try {
        await _device!.claimInterface(2).toDart;
        logger.d("Claimed interface 2 (bulk).");
      } catch (e) {
        logger.e("Failed to claim interface 2: $e");
        rethrow;
      }

      try {
        await _device!.selectAlternateInterface(2, 1).toDart;
        logger.d(
            "Selected alternate setting 1 on interface 2 (Bulk streaming activated).");
      } catch (e) {
        logger.e("Failed to select alternate setting: $e");
        rethrow;
      }

      try {
        await _device!.clearHalt('in', 8).toDart;
      } catch (e) {
        logger.w("clearHalt warning (ignorable): $e");
      }

      _connected = true;
      _isStreaming = true;

      _startBulkStreamingPump();
      final initialGainMask =
          rust_labrador.labradorCalculateGainMask(gain: 1.0);
      rust_labrador.labradorStartOscilloscope(channels: 1, gain: 1.0);
      await sendControl(request: 0xA5, value: 0, index: initialGainMask);

      logger.i(
          "EspoTek Labrador WebUSB connection complete and streaming started.");
      return true;
    } catch (e) {
      _lastError = e.toString();
      logger.e("Failed to connect EspoTek Labrador WebUSB: $e");
      await disconnect();
      return false;
    }
  }

  void _startBulkStreamingPump() async {
    logger.i("Started Labrador WebUSB bulk streaming pump to Rust WASM.");
    const int endpointNumber = 8;
    const int bufferLength = 9984;

    while (_isStreaming && _connected && _device != null) {
      try {
        final result =
            await _device!.transferIn(endpointNumber, bufferLength).toDart;
        if (result.status == 'stall') {
          logger.w("Bulk endpoint stalled, clearing halt...");
          await _device!.clearHalt('in', endpointNumber).toDart;
          continue;
        }

        if (result.data != null) {
          final ByteData bd = result.data!.toDart;
          final Uint8List chunk =
              bd.buffer.asUint8List(bd.offsetInBytes, bd.lengthInBytes);
          if (chunk.isNotEmpty) {
            rust_labrador.labradorPushWebBulkData(data: chunk);
          }
        }
      } catch (e) {
        if (_isStreaming && _connected) {
          logger.e("Error during WebUSB bulk transfer: $e");
          await Future.delayed(const Duration(milliseconds: 10));
        }
      }
    }
    logger.d("Labrador WebUSB bulk streaming pump terminated.");
  }

  @override
  Future<void> sendControl({
    required int request,
    required int value,
    int index = 0,
    Uint8List? data,
  }) async {
    if (!_connected || _device == null) {
      logger.w(
          "Cannot send control transfer 0x${request.toRadixString(16)}: not connected.");
      return;
    }

    try {
      final setup = USBControlTransferParameters(
        requestType: 'vendor',
        recipient: 'device',
        request: request,
        value: value,
        index: index,
      );

      if (data != null && data.isNotEmpty) {
        await _device!.controlTransferOut(setup, data.toJS).toDart;
      } else {
        await _device!.controlTransferOut(setup).toDart;
      }
    } catch (e) {
      logger.e("Control transfer 0x${request.toRadixString(16)} failed: $e");
    }
  }

  @override
  Future<void> disconnect() async {
    _isStreaming = false;
    _connected = false;

    rust_labrador.labradorStopStreaming();

    if (_device != null) {
      try {
        await _device!.releaseInterface(2).toDart;
      } catch (_) {}
      try {
        await _device!.releaseInterface(0).toDart;
      } catch (_) {}
      try {
        await _device!.close().toDart;
      } catch (_) {}
      _device = null;
    }
    logger.i("EspoTek Labrador WebUSB disconnected.");
  }
}
