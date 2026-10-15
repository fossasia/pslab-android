import 'dart:math';
import 'package:pslab/communication/science_lab.dart';
import 'package:pslab/others/logger_service.dart';
import 'oscilloscope_interface.dart';

class PSLabOscilloscope implements OscilloscopeInterface {
  final ScienceLab scienceLab;
  bool _ch1Enabled = true;
  bool _ch2Enabled = false;
  bool _ch3Enabled = false;
  bool _ch4Enabled = false;
  bool _isAc = false;
  bool _captured = false;
  final Set<int> _fetchedChannelsThisFrame = {};
  double _lastTimeGap = -1.0;
  int _lastNumToGet = -1;

  PSLabOscilloscope(this.scienceLab);

  @override
  Future<void> configureChannel(int channel, bool enabled) async {
    bool changed = false;
    if (channel == 1 && _ch1Enabled != enabled) {
      _ch1Enabled = enabled;
      changed = true;
    } else if (channel == 2 && _ch2Enabled != enabled) {
      _ch2Enabled = enabled;
      changed = true;
    } else if (channel == 3 && _ch3Enabled != enabled) {
      _ch3Enabled = enabled;
      changed = true;
    } else if (channel == 4 && _ch4Enabled != enabled) {
      _ch4Enabled = enabled;
      changed = true;
    }

    if (changed) {
      logger.d("Configuring CH$channel to Enabled = $enabled");
      _captured = false;
      _fetchedChannelsThisFrame.clear();
    }
  }

  @override
  Future<void> setGain(int channel, double gain) async {
    if (channel != 1 && channel != 2) {
      return;
    }

    int gainCode = 0;
    if (gain >= 8) {
      gainCode = 3;
    } else if (gain >= 4) {
      gainCode = 2;
    } else if (gain >= 2) {
      gainCode = 1;
    }

    String chName = channel == 1 ? 'CH1' : 'CH2';
    logger.d("Setting Gain for $chName to ${gain}x (Code: $gainCode)");
    await scienceLab.setGain(chName, gainCode, true);
  }

  @override
  void setAcCoupled(bool isAc) {
    logger.d("Setting AC Coupling to $isAc");
    _isAc = isAc;
  }

  @override
  Future<List<double>> fetchSamples(int numToGet, int channel,
      [double? timeWindowSecs]) async {
    String chName = 'CH1';
    if (channel == 2) chName = 'CH2';
    if (channel == 3) chName = 'CH3';
    if (channel == 4) chName = 'MIC';
    double timeGap = 2.0;
    if (timeWindowSecs != null && numToGet > 0) {
      timeGap = (timeWindowSecs * 1000000.0) / numToGet;
    }

    int channelsToCapture = 1;
    if (_ch3Enabled || _ch4Enabled || channel >= 3) {
      channelsToCapture = 4;
    } else if (_ch2Enabled || channel == 2) {
      channelsToCapture = 2;
    }
    if (channelsToCapture >= 4 && timeGap < 1.75) {
      timeGap = 1.75;
    } else if (channelsToCapture == 2 && timeGap < 0.875) {
      timeGap = 0.875;
    } else if (timeGap < 0.5) {
      timeGap = 0.5;
    }
    final bool isNewFrame = !_captured ||
        _fetchedChannelsThisFrame.contains(channel) ||
        (timeGap - _lastTimeGap).abs() > 0.001 ||
        numToGet != _lastNumToGet;

    if (isNewFrame) {
      _fetchedChannelsThisFrame.clear();
      _lastTimeGap = timeGap;
      _lastNumToGet = numToGet;

      logger.d(
          "Capturing $channelsToCapture channel(s), $numToGet samples, timeGap: ${timeGap.toStringAsFixed(3)}us");
      final stopwatch = Stopwatch()..start();
      await scienceLab.captureTraces(
        channelsToCapture,
        numToGet,
        timeGap,
        'CH1',
        false,
        null,
      );
      final elapsedMs = stopwatch.elapsedMilliseconds;
      final expectedMs = (numToGet * timeGap * 1e-3).ceil();
      if (elapsedMs < expectedMs) {
        await Future.delayed(Duration(milliseconds: expectedMs - elapsedMs));
      }
      _captured = true;
    }

    _fetchedChannelsThisFrame.add(channel);

    final trace = await scienceLab.fetchTrace(channel);
    List<double> samples = trace['y'] ?? [];

    if (samples.isEmpty) {
      logger.w("FETCH FAILED! trace['y'] for $chName returned an empty list.");
      return samples;
    }

    double minVal = samples.reduce(min);
    double maxVal = samples.reduce(max);
    double meanVal = samples.reduce((a, b) => a + b) / samples.length;
    double vPP = maxVal - minVal;

    logger.i("OSC_HAL: $chName Stats -> "
        "Count: ${samples.length} | "
        "Min: ${minVal.toStringAsFixed(3)}V | "
        "Max: ${maxVal.toStringAsFixed(3)}V | "
        "Mean: ${meanVal.toStringAsFixed(3)}V | "
        "Vpp: ${vPP.toStringAsFixed(3)}V");

    if (vPP < 0.05) {
      logger.w(
          "LOW AMPLITUDE WARNING on $chName! Vpp is only ${vPP.toStringAsFixed(3)}V. The wave will appear almost flat/invisible.");
    }
    if (_isAc) {
      samples = samples.map((v) => v - meanVal).toList();
      logger.d(
          "Applied AC Coupling (shifted by -${meanVal.toStringAsFixed(3)}V)");
    }

    return samples;
  }
}
