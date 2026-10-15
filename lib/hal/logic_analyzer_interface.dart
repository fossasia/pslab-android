class LogicChannelData {
  final List<double> timestampsUs;
  final List<double> logicLevels;

  LogicChannelData({
    required this.timestampsUs,
    required this.logicLevels,
  });
}

abstract class LogicAnalyzerInterface {
  List<String> get availableChannels;

  Future<void> start({required List<String> channels});

  Future<Map<String, LogicChannelData>> capture({
    required List<String> channels,
    required Map<String, String> edgeOptions,
  });

  Future<void> stop();
}
