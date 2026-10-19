import 'dart:typed_data';

abstract class LabradorPlatformManager {
  static LabradorPlatformManager get instance => throw UnsupportedError(
      'Cannot create LabradorPlatformManager without dart:io or dart:js_interop');

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
