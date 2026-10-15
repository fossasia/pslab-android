import 'package:pslab/communication/science_lab.dart';
import 'package:pslab/providers/locator.dart';
import 'hardware_board.dart';
import 'oscilloscope_interface.dart';
import 'multimeter_interface.dart';
import 'wave_generator_interface.dart';
import 'power_source_interface.dart';
import 'logic_analyzer_interface.dart';
import 'pslab_oscilloscope.dart';
import 'pslab_multimeter.dart';
import 'pslab_wave_generator.dart';
import 'pslab_power_source.dart';
import 'pslab_logic_analyzer.dart';

class PSLabHardwareBoard implements HardwareBoard {
  final String version;
  final int vid;
  final int pid;

  late final PSLabOscilloscope _oscilloscope;
  late final PSLabMultimeter _multimeter;
  late final PSLabWaveGenerator _waveGenerator;
  late final PSLabPowerSource _powerSource;
  late final PSLabLogicAnalyzer _logicAnalyzer;

  PSLabHardwareBoard({
    required this.version,
    required this.vid,
    required this.pid,
  }) {
    final scienceLab = getIt.get<ScienceLab>();
    _oscilloscope = PSLabOscilloscope(scienceLab);
    _multimeter = PSLabMultimeter(scienceLab);
    _waveGenerator = PSLabWaveGenerator(scienceLab);
    _powerSource = PSLabPowerSource(scienceLab);
    _logicAnalyzer = PSLabLogicAnalyzer(scienceLab);
  }

  @override
  String get boardName => version;

  @override
  bool get isConnected => getIt.get<ScienceLab>().isConnected();

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

  @override
  Future<void> disconnect() async {}
}
