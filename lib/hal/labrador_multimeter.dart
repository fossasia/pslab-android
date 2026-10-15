import 'dart:math';
import 'package:pslab/src/rust/api/labrador.dart' as rust_labrador;
import 'multimeter_interface.dart';
import 'labrador_oscilloscope.dart';

class LabradorMultimeter implements MultimeterInterface {
  final LabradorOscilloscope? oscilloscope;
  MultimeterMode currentMode = MultimeterMode.voltage;
  final double vcc = 3.3;
  final double seriesResistance = 1000.0;
  final double frontendGain = 0.1;
  final double ch1Ref = 1.65;
  int currentChannel = 1;

  LabradorMultimeter([this.oscilloscope]);

  void setChannel(int channel) {
    currentChannel = channel;
  }

  @override
  Future<void> setMode(MultimeterMode mode) async {
    currentMode = mode;
  }

  @override
  Future<MultimeterReading> readMeasurement() async {
    List<double> samples = [];
    if (oscilloscope != null) {
      await oscilloscope!.configureChannel(currentChannel, true);
      oscilloscope!.setAcCoupled(false);
      samples = await oscilloscope!.fetchSamples(50, currentChannel);
    } else {
      final rawBytes = rust_labrador.labradorFetchInterpolated(
        channel: currentChannel,
        sampleWindowSecs: 0.005,
        numSamples: 50,
        delayOffsetSecs: 0.0,
      );
      if (rawBytes.isNotEmpty) {
        samples = rawBytes.map((s) {
          double rawV =
              (s * (vcc / 2.0)) / (frontendGain * 1.0 * 128.0) + ch1Ref;
          return (rawV - 0.23) * (3.3 / (2.56 - 0.23));
        }).toList();
      }
    }

    if (samples.isEmpty) {
      return MultimeterReading(value: 0.0, unit: "V");
    }

    double sum = 0.0;
    double sumSq = 0.0;
    double vMin = 999.0;
    double vMax = -999.0;

    for (final v in samples) {
      sum += v;
      sumSq += v * v;
      if (v < vMin) vMin = v;
      if (v > vMax) vMax = v;
    }

    final double vMean = sum / samples.length;
    final double vRms = sqrt(sumSq / samples.length);

    switch (currentMode) {
      case MultimeterMode.voltage:
        return MultimeterReading(
          value: vMean,
          unit: "V",
          min: vMin,
          max: vMax,
          rms: vRms,
        );

      case MultimeterMode.current:
        final double currentAmps = vMean / seriesResistance;
        return MultimeterReading(
          value: currentAmps * 1000.0,
          unit: "mA",
          min: (vMin / seriesResistance) * 1000.0,
          max: (vMax / seriesResistance) * 1000.0,
          rms: (vRms / seriesResistance) * 1000.0,
        );

      case MultimeterMode.resistance:
        if ((vcc - vMean).abs() < 0.02) {
          return MultimeterReading(value: double.infinity, unit: "Ω");
        }
        final double r = (vMean * seriesResistance) / (vcc - vMean);
        if (r > 1000) {
          return MultimeterReading(value: r / 1000.0, unit: "kΩ");
        }
        return MultimeterReading(value: r.abs(), unit: "Ω");
    }
  }
}
