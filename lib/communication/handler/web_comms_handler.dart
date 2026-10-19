import 'dart:async';
import 'dart:js_interop';
import 'dart:js_interop_unsafe';
import 'dart:typed_data';

import 'package:web/web.dart' as web;
import '../../others/logger_service.dart';
import 'base.dart';
import 'package:pslab/src/rust/api/common.dart' as rust_api;

@JS()
extension type Serial._(JSObject _) implements JSObject {
  external JSPromise<SerialPort> requestPort();
}

@JS()
extension type SerialPort._(JSObject _) implements JSObject {
  external JSPromise<JSAny?> open(SerialOptions options);
  external JSPromise<JSAny?> setSignals(SerialOutputSignals options);
  external JSPromise<JSAny?> close();
  external web.ReadableStream? get readable;
  external web.WritableStream? get writable;
}

extension type SerialOptions._(JSObject _) implements JSObject {
  external factory SerialOptions({int baudRate});
}

extension type SerialOutputSignals._(JSObject _) implements JSObject {
  external factory SerialOutputSignals(
      {bool dataTerminalReady, bool requestToSend});
}

@JS()
extension type ReadResult._(JSObject _) implements JSObject {
  external bool get done;
  external JSUint8Array? get value;
}

extension NavigatorSerial on web.Navigator {
  Serial get serial => (this as JSObject).getProperty<Serial>('serial'.toJS);
}

class WebCommsHandler implements CommunicationHandler {
  @override
  bool connected = false;

  @override
  bool deviceFound = false;

  @override
  Board? openedBoard;

  SerialPort? _port;
  web.ReadableStreamDefaultReader? _reader;
  web.WritableStreamDefaultWriter? _writer;
  bool _isReading = false;

  final List<int> _rxBuffer = [];
  final List<Uint8List> _writeQueue = [];
  bool _isWriting = false;

  @override
  Future<void> initialize() async {
    final navigator = web.window.navigator as JSObject;
    final hasSerial = navigator.hasProperty('serial'.toJS).toDart;
    deviceFound = web.window.isSecureContext && hasSerial;

    if (deviceFound) {
      logger.d("Found Web Serial API support");
    } else {
      logger.d("Web Serial API not supported or not in secure context");
    }
  }

  @override
  Future<void> open({int overrideBaud = 1000000}) async {
    if (!deviceFound) {
      throw Exception("Web Serial API not available");
    }

    try {
      logger.d("Requesting Web Serial port (waiting for user selection)...");
      _port = await web.window.navigator.serial.requestPort().toDart;
      logger.d("Opening Web Serial port at $overrideBaud baud...");
      await _port!.open(SerialOptions(baudRate: overrideBaud)).toDart;

      logger.d("Setting DTR/RTS signals...");
      try {
        await _port!
            .setSignals(SerialOutputSignals(
                dataTerminalReady: true, requestToSend: false))
            .toDart;
      } catch (e) {
        logger.w("setSignals error (non-fatal): $e");
      }
      await Future.delayed(const Duration(milliseconds: 300));

      _writer = _port!.writable!.getWriter();
      _reader = _port!.readable!.getReader() as web.ReadableStreamDefaultReader;

      connected = true;
      _isReading = true;
      _rxBuffer.clear();
      _writeQueue.clear();
      _isWriting = false;
      logger.i("Connected to Web Serial device.");

      _pumpData();
    } catch (e) {
      connected = false;
      logger.e("User cancelled or connection failed: $e");
      throw Exception("Failed to open Web Serial port");
    }
  }

  void _pumpData() async {
    logger.d("Started Web Serial receive pump.");
    try {
      while (_isReading && connected && _reader != null) {
        final resultJS = await _reader!.read().toDart;
        final result = resultJS as ReadResult;

        if (result.done) {
          logger.d("Web Serial stream closed from hardware side.");
          connected = false;
          break;
        }

        if (result.value != null) {
          final data = result.value!.toDart;
          _rxBuffer.addAll(data);
          try {
            rust_api.pushWebData(data: data);
          } catch (_) {}
        }
      }
    } catch (e) {
      logger.e("Stream closed or device disconnected: $e");
      connected = false;
    }
  }

  @override
  bool isDeviceFound() => deviceFound;

  @override
  bool isConnected() => connected;

  @override
  void close() async {
    if (!connected) return;
    logger.d("Closing Web Serial connection and unlocking streams...");
    _isReading = false;
    connected = false;
    _rxBuffer.clear();
    _writeQueue.clear();
    _isWriting = false;

    try {
      if (_reader != null) {
        await _reader!.cancel().toDart.catchError((_) => null);
        (_reader as JSObject).callMethod('releaseLock'.toJS);
        _reader = null;
      }

      if (_writer != null) {
        await _writer!.close().toDart.catchError((_) => null);
        (_writer as JSObject).callMethod('releaseLock'.toJS);
        _writer = null;
      }

      if (_port != null) {
        await _port!.close().toDart.catchError((_) => null);
        _port = null;
      }
      logger.d("Web Serial port safely closed.");
    } catch (e) {
      logger.e("Error during Web Serial lock release: $e");
    }

    try {
      rust_api.closeUsb();
    } catch (_) {}
  }

  @override
  void write(Uint8List src, int timeoutMillis) {
    if (!connected || _writer == null) return;
    _writeQueue.add(Uint8List.fromList(src));
    _flushWriteQueue();
  }

  Future<void> _flushWriteQueue() async {
    if (_isWriting) return;
    _isWriting = true;
    try {
      while (_writeQueue.isNotEmpty && connected && _writer != null) {
        final chunk = _writeQueue.removeAt(0);
        await _writer!.write(chunk.toJS).toDart;
      }
    } catch (e) {
      logger.e("Error writing to Web Serial: $e");
    } finally {
      _isWriting = false;
    }
  }

  @override
  Future<int> read(Uint8List dest, int bytesToRead, int timeoutMillis) async {
    int numBytesRead = 0;
    int elapsed = 0;
    const int checkInterval = 2;

    try {
      while (numBytesRead < bytesToRead && elapsed < timeoutMillis) {
        if (_rxBuffer.isNotEmpty) {
          int available = _rxBuffer.length;
          int needed = bytesToRead - numBytesRead;
          int take = available < needed ? available : needed;

          for (int i = 0; i < take; i++) {
            dest[numBytesRead + i] = _rxBuffer.removeAt(0);
          }
          numBytesRead += take;

          if (numBytesRead == bytesToRead) {
            break;
          }
        }
        await Future.delayed(const Duration(milliseconds: checkInterval));
        elapsed += checkInterval;
      }

      if (numBytesRead < bytesToRead && elapsed >= timeoutMillis) {
        logger.w(
            "Web Serial timeout: Expected $bytesToRead bytes, got $numBytesRead bytes in ${elapsed}ms");
      }
    } catch (e) {
      logger.e("Exception during Web Serial read: $e");
    }

    return numBytesRead;
  }
}
