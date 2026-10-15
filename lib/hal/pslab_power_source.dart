import 'package:pslab/communication/science_lab.dart';
import 'power_source_interface.dart';

class PSLabPowerSource implements PowerSourceInterface {
  final ScienceLab _scienceLab;

  PSLabPowerSource(this._scienceLab);

  @override
  bool get isPsuSupported => false;

  @override
  bool get isFourChannelSupported => true;

  @override
  List<double> get psuRange => [0.0, 0.0];

  @override
  Future<void> setPsuVoltage(double voltage) async {
    // Not supported on standard PSLab
  }

  @override
  Future<void> disablePsu() async {
    // Not supported on standard PSLab
  }

  @override
  Future<void> setPV1(double voltage) async {
    await _scienceLab.setPV1(voltage);
  }

  @override
  Future<void> setPV2(double voltage) async {
    await _scienceLab.setPV2(voltage);
  }

  @override
  Future<void> setPV3(double voltage) async {
    await _scienceLab.setPV3(voltage);
  }

  @override
  Future<void> setPCS(double current) async {
    await _scienceLab.setPCS(current);
  }
}
