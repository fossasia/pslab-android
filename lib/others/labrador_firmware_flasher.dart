import 'dart:async';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:pslab/src/rust/api/bootloader.dart';
import 'package:pslab/src/rust/api/common.dart' as rust_common;
import 'package:pslab/src/rust/api/labrador_bootloader.dart';
import 'labrador_flasher_bridge.dart';

class LabradorFirmwareFlasher {
  final Function(double progress, String status) onProgress;
  final Function() onSuccess;
  final Function(String error) onError;

  StreamSubscription<FlashState>? _subscription;

  LabradorFirmwareFlasher({
    required this.onProgress,
    required this.onSuccess,
    required this.onError,
  });

  Future<void> flashFirmware({
    required String hexContent,
    String? hexFileName,
  }) async {
    if (kIsWeb) {
      await flashLabradorWeb(
        hexContent: hexContent,
        onProgress: onProgress,
        onSuccess: onSuccess,
        onError: onError,
      );
      return;
    }

    if (defaultTargetPlatform == TargetPlatform.android) {
      try {
        const channel = MethodChannel('usb_serial');
        final fd = await channel
            .invokeMethod<int>('getAndroidFd', {'vid': 0x03EB, 'pid': 0x2FE4});
        if (fd != null) {
          rust_common.initAndroid(fd: fd);
        }
      } catch (e) {
        onError(
            "No Labrador board found in DFU bootloader mode (VID 03EB, PID 2FE4).\n\n"
            "Please plug in the board with DO1 connected to GND, or tap 'Jump Connected Board to Bootloader'.");
        return;
      }
    }

    try {
      _subscription?.cancel();
      _subscription = flashLabradorFirmware(hexStr: hexContent).listen(
        (state) {
          if (state is FlashState_Connecting) {
            onProgress(0.05, "Opening USB DFU connection...");
          } else if (state is FlashState_Erasing) {
            onProgress(0.15, "Erasing ATxmega32A4U flash memory...");
          } else if (state is FlashState_Writing) {
            double progress = 0.20 + (state.progressPercent / 100.0 * 0.70);
            onProgress(progress, "Writing Flash: ${state.progressPercent}%");
          } else if (state is FlashState_Verifying) {
            onProgress(0.95, "Verifying & launching firmware...");
          } else if (state is FlashState_Finished) {
            onProgress(
                1.0, "SUCCESS! Labrador bulk firmware flashed successfully.");
            onSuccess();
          } else if (state is FlashState_Error) {
            onError(state.message);
          }
        },
        onError: (err) {
          onError(err.toString());
        },
      );
    } catch (e) {
      onError(e.toString());
    }
  }

  void dispose() {
    _subscription?.cancel();
  }
}
