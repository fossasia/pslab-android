import 'oscilloscope_interface.dart';
import 'multimeter_interface.dart';
import 'wave_generator_interface.dart';
import 'power_source_interface.dart';
import 'logic_analyzer_interface.dart';

abstract class HardwareBoard {
  String get boardName;
  bool get isConnected;

  OscilloscopeInterface? get oscilloscope;
  MultimeterInterface? get multimeter;
  WaveGeneratorInterface? get waveGenerator;
  PowerSourceInterface? get powerSource;
  LogicAnalyzerInterface? get logicAnalyzer;
  Future<void> disconnect();
}
