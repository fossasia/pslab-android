import 'package:flutter/foundation.dart';
import 'package:pslab/communication/labrador/labrador_platform_manager.dart';
import 'package:pslab/src/rust/api/labrador.dart' as rust_labrador;
import 'logic_analyzer_interface.dart';

class LabradorLogicAnalyzer implements LogicAnalyzerInterface {
  bool _isStreaming = false;

  @override
  List<String> get availableChannels => ['LA1', 'LA2'];

  @override
  Future<void> start({required List<String> channels}) async {
    final int chCount = channels.length.clamp(1, 2);
    rust_labrador.labradorStartLogicAnalyzer(channels: chCount);
    if (kIsWeb) {
      final mode = (chCount <= 2) ? 3 : 4;
      await LabradorPlatformManager.instance.sendControl(
        request: 0xA5,
        value: mode,
        index: 0,
      );
    }
    _isStreaming = true;
  }

  @override
  Future<Map<String, LogicChannelData>> capture({
    required List<String> channels,
    required Map<String, String> edgeOptions,
  }) async {
    if (!_isStreaming) {
      await start(channels: channels);
    }
    await Future.delayed(const Duration(milliseconds: 120));

    final Map<String, LogicChannelData> results = {};
    const int requestedBytes = 3750;
    const double usPerSample = 1.0 / 3.0;

    for (final chName in channels) {
      final int chNum = (chName == 'LA2') ? 2 : 1;

      Uint8List rawBytes = rust_labrador.labradorFetchRawBytes(
        channel: chNum,
        numBytes: requestedBytes,
      );

      if (rawBytes.isEmpty) {
        await Future.delayed(const Duration(milliseconds: 60));
        rawBytes = rust_labrador.labradorFetchRawBytes(
          channel: chNum,
          numBytes: requestedBytes,
        );
      }

      if (rawBytes.isEmpty) {
        results[chName] = LogicChannelData(
          timestampsUs: [0.0, 10000.0],
          logicLevels: [0.0, 0.0],
        );
        continue;
      }

      final List<double> timestampsUs = [];
      final List<double> logicLevels = [];

      final int totalSamples = rawBytes.length * 8;
      double currentLevel = (rawBytes[0] & 1).toDouble();

      timestampsUs.add(0.0);
      logicLevels.add(currentLevel);

      for (int i = 1; i < totalSamples; i++) {
        final int byteIdx = i >> 3;
        final int bitIdx = i & 7;
        final double sampleLevel =
            ((rawBytes[byteIdx] >> bitIdx) & 1).toDouble();

        if (sampleLevel != currentLevel) {
          final double t = i * usPerSample;
          timestampsUs.add(t);
          logicLevels.add(currentLevel);

          timestampsUs.add(t);
          logicLevels.add(sampleLevel);

          currentLevel = sampleLevel;
        }
      }

      final double endT = (totalSamples - 1) * usPerSample;
      timestampsUs.add(endT);
      logicLevels.add(currentLevel);

      results[chName] = LogicChannelData(
        timestampsUs: timestampsUs,
        logicLevels: logicLevels,
      );
    }

    return results;
  }

  @override
  Future<void> stop() async {
    rust_labrador.labradorStopStreaming();
    _isStreaming = false;
  }
}
