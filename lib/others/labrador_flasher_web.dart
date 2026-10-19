import 'labrador_web_firmware_flasher.dart';

Future<void> flashLabradorWeb({
  required String hexContent,
  required Function(double progress, String status) onProgress,
  required Function() onSuccess,
  required Function(String error) onError,
}) async {
  final flasher = LabradorWebFirmwareFlasher(
    onProgress: onProgress,
    onSuccess: onSuccess,
    onError: onError,
  );
  await flasher.flashFirmware(hexContent);
}
