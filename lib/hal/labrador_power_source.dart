import 'package:flutter/foundation.dart';
import 'package:pslab/communication/labrador/labrador_platform_manager.dart';
import 'package:pslab/src/rust/api/labrador.dart' as rust_labrador;
import 'power_source_interface.dart';

class LabradorPowerSource implements PowerSourceInterface {
  double _currentPsuVoltage = 4.5;
  bool _isEnabled = false;

  @override
  bool get isPsuSupported => true;

  @override
  bool get isFourChannelSupported => false;

  @override
  List<double> get psuRange => [4.5, 12.0];

  double get currentPsuVoltage => _currentPsuVoltage;
  bool get isEnabled => _isEnabled;

  bool _isSendingPsu = false;
  double? _pendingPsuVoltage;

  @override
  Future<void> setPsuVoltage(double voltage) async {
    _currentPsuVoltage = voltage.clamp(4.5, 15.0);
    _isEnabled = true;
    _pendingPsuVoltage = _currentPsuVoltage;
    if (_isSendingPsu) return;
    _isSendingPsu = true;

    while (_pendingPsuVoltage != null) {
      final target = _pendingPsuVoltage!;
      _pendingPsuVoltage = null;
      try {
        rust_labrador.labradorSetPsuVoltage(voltage: target);
        if (kIsWeb) {
          final duty = rust_labrador.labradorCalculatePsuDuty(voltage: target);
          await LabradorPlatformManager.instance.sendControl(
            request: 0xA3,
            value: duty,
            index: 0,
          );
        }
      } catch (e) {
        debugPrint('Error setting PSU voltage: $e');
      }
    }
    _isSendingPsu = false;
  }

  @override
  Future<void> disablePsu() async {
    _isEnabled = false;
    _pendingPsuVoltage = null;
    try {
      rust_labrador.labradorSetPsuVoltage(voltage: 0.0);
      if (kIsWeb) {
        await LabradorPlatformManager.instance.sendControl(
          request: 0xA3,
          value: 0,
          index: 0,
        );
      }
    } catch (e) {
      debugPrint('Error disabling PSU: $e');
    }
  }

  @override
  Future<void> setPV1(double voltage) async {
    // Unsupported on Labrador hardware
  }

  @override
  Future<void> setPV2(double voltage) async {
    // Unsupported on Labrador hardware
  }

  @override
  Future<void> setPV3(double voltage) async {
    // Unsupported on Labrador hardware
  }

  @override
  Future<void> setPCS(double current) async {
    // Unsupported on Labrador hardware
  }
}
