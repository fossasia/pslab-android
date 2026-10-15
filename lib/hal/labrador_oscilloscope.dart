import 'package:flutter/foundation.dart';
import 'package:pslab/communication/labrador/labrador_platform_manager.dart';
import 'package:pslab/src/rust/api/labrador.dart' as rust_labrador;
import 'oscilloscope_interface.dart';

class LabradorOscilloscope implements OscilloscopeInterface {
  final double vcc = 3.3;
  final double ch1Ref = 1.65;
  final double ch2Ref = 1.65;

  final double frontendGainCh1 = 0.1;
  final double frontendGainCh2 = 0.1;

  double currentScopeGain = 1.0;
  bool isAcCoupled = false;
  bool ch2Enabled = false;
  bool isTriggerEnabled = true;
  double triggerLevelV = 1.65;
  double timebaseSeconds = 0.003;

  static int _activeChannels = -1;
  static double _activeGain = -1.0;

  static List<double> _lastValidFrameCh1 = [];
  static List<double> _lastValidFrameCh2 = [];

  static int _consecutiveEmptyCh1 = 0;
  static int _consecutiveEmptyCh2 = 0;

  static int _lastFetchTime = 0;

  void resetState() {
    _activeChannels = -1;
    _activeGain = -1.0;
    _lastValidFrameCh1 = [];
    _lastValidFrameCh2 = [];
    _consecutiveEmptyCh1 = 0;
    _consecutiveEmptyCh2 = 0;
  }

  void setTimebase(double seconds) {
    timebaseSeconds = seconds;
  }

  @override
  Future<void> configureChannel(int channel, bool enabled) async {
    if (channel == 2) ch2Enabled = enabled;
    int targetChannels = ch2Enabled ? 2 : 1;
    if (targetChannels != _activeChannels || currentScopeGain != _activeGain) {
      _activeChannels = targetChannels;
      _activeGain = currentScopeGain;
      rust_labrador.labradorStartOscilloscope(
          channels: _activeChannels, gain: _activeGain);
      if (kIsWeb) {
        final mode = (_activeChannels >= 2) ? 2 : 0;
        final gainMask =
            rust_labrador.labradorCalculateGainMask(gain: _activeGain);
        await LabradorPlatformManager.instance.sendControl(
          request: 0xA5,
          value: mode,
          index: gainMask,
        );
      }
    }
  }

  @override
  Future<void> setGain(int channel, double gain) async {
    currentScopeGain = gain;
    int targetChannels = ch2Enabled ? 2 : 1;
    if (targetChannels != _activeChannels || currentScopeGain != _activeGain) {
      _activeChannels = targetChannels;
      _activeGain = currentScopeGain;
      rust_labrador.labradorStartOscilloscope(
          channels: _activeChannels, gain: _activeGain);
      if (kIsWeb) {
        final mode = (_activeChannels >= 2) ? 2 : 0;
        final gainMask =
            rust_labrador.labradorCalculateGainMask(gain: _activeGain);
        await LabradorPlatformManager.instance.sendControl(
          request: 0xA5,
          value: mode,
          index: gainMask,
        );
      }
    }
  }

  @override
  void setAcCoupled(bool isAc) {
    isAcCoupled = isAc;
  }

  @override
  Future<List<double>> fetchSamples(int numToGet, int channel,
      [double? timeWindowSecs]) async {
    if (channel == 1) {
      final int now = DateTime.now().millisecondsSinceEpoch;
      final int elapsed = now - _lastFetchTime;
      if (elapsed < 65) {
        await Future.delayed(Duration(milliseconds: 65 - elapsed));
      }
      _lastFetchTime = DateTime.now().millisecondsSinceEpoch;
    }
    final double window = (timeWindowSecs != null && timeWindowSecs > 0)
        ? timeWindowSecs
        : timebaseSeconds;

    final rawBytes = rust_labrador.labradorFetchInterpolated(
      channel: channel,
      sampleWindowSecs: window,
      numSamples: numToGet,
      delayOffsetSecs: 0.0,
    );

    final double ref = channel == 1 ? ch1Ref : ch2Ref;

    if (rawBytes.isEmpty || rawBytes.length < numToGet) {
      if (channel == 1) {
        _consecutiveEmptyCh1++;
        if (_consecutiveEmptyCh1 < 5 && _lastValidFrameCh1.length == numToGet) {
          return _lastValidFrameCh1;
        }
      } else {
        _consecutiveEmptyCh2++;
        if (_consecutiveEmptyCh2 < 5 && _lastValidFrameCh2.length == numToGet) {
          return _lastValidFrameCh2;
        }
      }
      return List<double>.filled(numToGet, ref);
    }

    if (channel == 1) {
      _consecutiveEmptyCh1 = 0;
    } else {
      _consecutiveEmptyCh2 = 0;
    }

    const double top = 128.0;
    final double frontendGain =
        channel == 1 ? frontendGainCh1 : frontendGainCh2;

    final voltages = List<double>.filled(rawBytes.length, 0.0);
    double accumulated = 0.0;
    for (int i = 0; i < rawBytes.length; i++) {
      double signedSample = rawBytes[i];
      double rawV = (signedSample * (vcc / 2.0)) /
          (frontendGain * currentScopeGain * top);

      rawV += ref;

      double calibratedV = (rawV - 0.23) * (3.3 / (2.56 - 0.23));
      voltages[i] = calibratedV;
      accumulated += calibratedV;
    }

    if (isAcCoupled) {
      double meanVal = accumulated / voltages.length;
      for (int i = 0; i < voltages.length; i++) {
        voltages[i] -= meanVal;
      }
    }
    if (channel == 1) {
      _lastValidFrameCh1 = voltages;
    } else {
      _lastValidFrameCh2 = voltages;
    }

    return voltages;
  }
}
