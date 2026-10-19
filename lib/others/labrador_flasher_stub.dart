Future<void> flashLabradorWeb({
  required String hexContent,
  required Function(double progress, String status) onProgress,
  required Function() onSuccess,
  required Function(String error) onError,
}) async {
  throw UnsupportedError("Web flashing is only supported on Web platform");
}
