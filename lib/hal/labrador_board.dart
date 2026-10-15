import 'package:flutter/foundation.dart';
import 'package:pslab/communication/labrador/labrador_platform_manager.dart';
import 'package:pslab/src/rust/api/labrador.dart' as rust_labrador;
import 'package:pslab/src/rust/api/common.dart' as rust_common;
import 'hardware_board.dart';
import 'oscilloscope_interface.dart';
import 'multimeter_interface.dart';
import 'wave_generator_interface.dart';
import 'power_source_interface.dart';
import 'logic_analyzer_interface.dart';
import 'labrador_oscilloscope.dart';
import 'labrador_multimeter.dart';
import 'labrador_wave_generator.dart';
import 'labrador_power_source.dart';
import 'labrador_logic_analyzer.dart';

class LabradorHardwareBoard implements HardwareBoard {
  final String version;
  final int vid;
  final int pid;

  late final LabradorOscilloscope _oscilloscope = LabradorOscilloscope();
  late final LabradorMultimeter _multimeter = LabradorMultimeter(_oscilloscope);
  final LabradorWaveGenerator _waveGenerator = LabradorWaveGenerator();
  final LabradorPowerSource _powerSource = LabradorPowerSource();
  final LabradorLogicAnalyzer _logicAnalyzer = LabradorLogicAnalyzer();
  bool _connected = true;

  LabradorHardwareBoard({
    required this.version,
    required this.vid,
    required this.pid,
  });

  @override
  String get boardName => version;

  @override
  bool get isConnected => _connected;

  @override
  OscilloscopeInterface? get oscilloscope => _oscilloscope;

  @override
  MultimeterInterface? get multimeter => _multimeter;

  @override
  WaveGeneratorInterface? get waveGenerator => _waveGenerator;

  @override
  PowerSourceInterface? get powerSource => _powerSource;

  @override
  LogicAnalyzerInterface? get logicAnalyzer => _logicAnalyzer;

  Future<void> jumpToBootloader() async {
    try {
      if (kIsWeb) {
        await LabradorPlatformManager.instance.sendControl(
          request: 0xA7,
          value: 1,
          index: 0,
        );
      }
    } catch (_) {}
    await disconnect();
  }

  @override
  Future<void> disconnect() async {
    await _powerSource.disablePsu();
    await _logicAnalyzer.stop();
    if (kIsWeb) {
      await LabradorPlatformManager.instance.disconnect();
    }
    rust_labrador.labradorStopStreaming();
    rust_common.closeUsb();
    _connected = false;
  }
}
