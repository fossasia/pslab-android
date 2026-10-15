abstract class PowerSourceInterface {
  bool get isPsuSupported;
  bool get isFourChannelSupported;

  List<double> get psuRange;

  Future<void> setPsuVoltage(double voltage);

  Future<void> disablePsu();

  Future<void> setPV1(double voltage);

  Future<void> setPV2(double voltage);

  Future<void> setPV3(double voltage);

  Future<void> setPCS(double current);
}
