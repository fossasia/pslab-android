import 'package:flutter/foundation.dart';
import 'package:pslab/communication/labrador/labrador_platform_manager.dart';
import 'package:pslab/src/rust/api/labrador.dart' as rust_labrador;
import 'wave_generator_interface.dart';

class LabradorWaveGenerator implements WaveGeneratorInterface {
  Future<void> _sendWave(
      int channel, double freq, String waveType, double amp, double off) async {
    if (kIsWeb) {
      final payload = rust_labrador.labradorCalculateWavePayload(
        channel: channel,
        frequencyHz: freq,
        waveType: waveType,
        amplitudeV: amp,
        offsetV: off,
      );
      await LabradorPlatformManager.instance.sendControl(
        request: payload.requestId,
        value: payload.timerPeriod,
        index: payload.clockDivider,
        data: Uint8List.fromList(payload.samples),
      );
      await LabradorPlatformManager.instance.sendControl(
        request: 0xA4,
        value: payload.tripleMask,
        index: 0,
      );
    } else {
      rust_labrador.labradorGenerateAnalogWave(
        channel: channel,
        frequencyHz: freq,
        waveType: waveType,
        amplitudeV: amp,
        offsetV: off,
      );
    }
  }

  Future<void> _sendDigitalOut(int channel, bool state) async {
    if (kIsWeb) {
      final mask = rust_labrador.labradorCalculateDigitalOutState(
        channel: channel,
        state: state,
      );
      await LabradorPlatformManager.instance.sendControl(
        request: 0xA6,
        value: mask,
        index: 0,
      );
    } else {
      rust_labrador.labradorSetDigitalOut(channel: channel, state: state);
    }
  }

  @override
  Future<void> generateAnalogWaves({
    required double freq1,
    required double freq2,
    required double phase,
    required String waveType1,
    required String waveType2,
    required double amplitude1,
    required double amplitude2,
  }) async {
    if (freq1 > 0) {
      await _sendWave(1, freq1, waveType1, amplitude1, 0.0);
    }

    if (freq2 > 0) {
      await _sendWave(2, freq2, waveType2, amplitude2, 0.0);
    }
  }

  @override
  Future<void> generateDigitalPwms({
    required double freq,
    required double duty1,
    required double phase2,
    required double duty2,
    required double phase3,
    required double duty3,
    required double phase4,
    required double duty4,
  }) async {
    if (duty1 > 0) {
      await _sendWave(1, freq, "pwm:$duty1", 3.2, 0.0);
      await _sendDigitalOut(1, true);
    } else {
      await _sendWave(1, freq, "pwm:0.0", 0.0, 0.0);
      await _sendDigitalOut(1, false);
    }
    if (duty2 > 0) {
      await _sendWave(2, freq, "pwm:$duty2", 3.2, 0.0);
      await _sendDigitalOut(2, true);
    } else {
      await _sendWave(2, freq, "pwm:0.0", 0.0, 0.0);
      await _sendDigitalOut(2, false);
    }
    await _sendDigitalOut(3, duty3 > 0);

    await _sendDigitalOut(4, duty4 > 0);
  }
}
