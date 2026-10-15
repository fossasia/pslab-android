import 'dart:typed_data';

abstract class LabradorPlatformManager {
  static final LabradorPlatformManager _instance =
      _LabradorPlatformManagerNative();
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

class _LabradorPlatformManagerNative implements LabradorPlatformManager {
  @override
  bool get isWebSupported => false;

  @override
  String? get lastError => null;

  @override
  Future<bool> connectWeb() async {
    throw UnsupportedError('connectWeb is only supported on Web platform.');
  }

  @override
  Future<void> sendControl({
    required int request,
    required int value,
    int index = 0,
    Uint8List? data,
  }) async {
    // Native platforms (Desktop & Android OTG) execute control transfers directly
    // through Rust USB_HANDLE.
  }

  @override
  Future<void> disconnect() async {
    // Handled in Rust native layer.
  }
}
