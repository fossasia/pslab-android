import 'package:pslab/communication/science_lab.dart';
import 'logic_analyzer_interface.dart';

class PSLabLogicAnalyzer implements LogicAnalyzerInterface {
  final ScienceLab _scienceLab;

  PSLabLogicAnalyzer(this._scienceLab);

  bool get isConnected => _scienceLab.isConnected();

  @override
  List<String> get availableChannels => ['LA1', 'LA2', 'LA3', 'LA4'];

  @override
  Future<void> start({required List<String> channels}) async {}

  @override
  Future<Map<String, LogicChannelData>> capture({
    required List<String> channels,
    required Map<String, String> edgeOptions,
  }) async {
    return {};
  }

  @override
  Future<void> stop() async {}
}
