import 'package:pslab/providers/wave_generator_state_provider.dart';

class WaveGeneratorConstants {
  Map<WaveConst, Map<WaveConst, int>> wave = {
    WaveConst.wave1: {
      WaveConst.frequency: 1000,
      WaveConst.amplitude: 30,
      WaveConst.waveType: WaveGeneratorStateProvider.sin,
    },
    WaveConst.wave2: {
      WaveConst.phase: 0,
      WaveConst.frequency: 1000,
      WaveConst.amplitude: 30,
      WaveConst.waveType: WaveGeneratorStateProvider.sin,
    },
    WaveConst.waveType: {},
    WaveConst.sqr1: {
      WaveConst.frequency: 1000,
      WaveConst.duty: 50,
    },
    WaveConst.sqr2: {
      WaveConst.phase: 0,
      WaveConst.duty: 50,
    },
    WaveConst.sqr3: {
      WaveConst.phase: 0,
      WaveConst.duty: 50,
    },
    WaveConst.sqr4: {
      WaveConst.phase: 0,
      WaveConst.duty: 50,
    },
  };

  WaveConst modeSelected = WaveConst.square;

  final Map<String, int> state = {
    'SQR1': 0,
    'SQR2': 0,
    'SQR3': 0,
    'SQR4': 0,
  };
}
