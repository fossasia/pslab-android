import 'dart:math';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../providers/ai_assistant_provider.dart';
import '../../services/ai_context_scooper.dart';
import '../../theme/colors.dart';
import 'ai_chat_bottom_sheet.dart';

final GlobalKey<NavigatorState> rootNavigatorKey = GlobalKey<NavigatorState>();

class AiFloatingAssistantWrapper extends StatefulWidget {
  final Widget child;

  const AiFloatingAssistantWrapper({
    super.key,
    required this.child,
  });

  @override
  State<AiFloatingAssistantWrapper> createState() =>
      _AiFloatingAssistantWrapperState();
}

class _AiFloatingAssistantWrapperState
    extends State<AiFloatingAssistantWrapper> {
  Offset? _position;
  bool _isBottomSheetOpen = false;

  Future<void> _openChatBottomSheet() async {
    if (_isBottomSheetOpen) return;
    final navContext = rootNavigatorKey.currentContext ?? context;
    setState(() {
      _isBottomSheetOpen = true;
    });

    try {
      await showModalBottomSheet(
        context: navContext,
        isScrollControlled: true,
        backgroundColor: Colors.transparent,
        useRootNavigator: true,
        builder: (ctx) => const AiChatBottomSheet(),
      );
    } finally {
      if (mounted) {
        setState(() {
          _isBottomSheetOpen = false;
        });
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Consumer<AiAssistantProvider>(
      builder: (context, provider, _) {
        if (!provider.isFloatingEnabled) {
          return widget.child;
        }

        final screenSize = MediaQuery.of(context).size;
        final safePadding = MediaQuery.of(context).padding;
        final initialX = screenSize.width - 66.0;
        final initialY = max(safePadding.top + 50.0, screenSize.height - 130.0);

        _position ??= Offset(initialX, initialY);

        final clampedX = _position!.dx
            .clamp(10.0, max(10.0, screenSize.width - 60.0))
            .toDouble();
        final clampedY = _position!.dy
            .clamp(
              safePadding.top + 10.0,
              max(safePadding.top + 10.0,
                  screenSize.height - safePadding.bottom - 60.0),
            )
            .toDouble();
        _position = Offset(clampedX, clampedY);

        final hasActiveTelemetry = AiContextRegistry.activeScooper != null;

        return Stack(
          children: [
            widget.child,
            if (!_isBottomSheetOpen)
              Positioned(
                left: _position!.dx,
                top: _position!.dy,
                child: GestureDetector(
                  behavior: HitTestBehavior.opaque,
                  onTap: _openChatBottomSheet,
                  onPanUpdate: (details) {
                    setState(() {
                      _position = Offset(
                        _position!.dx + details.delta.dx,
                        _position!.dy + details.delta.dy,
                      );
                    });
                  },
                  child: MouseRegion(
                    cursor: SystemMouseCursors.click,
                    child: Container(
                      width: 52,
                      height: 52,
                      decoration: BoxDecoration(
                        color: Colors.white,
                        shape: BoxShape.circle,
                        border: Border.all(color: primaryRed, width: 2),
                        boxShadow: const [
                          BoxShadow(
                            color: Colors.black26,
                            blurRadius: 6,
                            offset: Offset(0, 3),
                          ),
                        ],
                      ),
                      child: Stack(
                        alignment: Alignment.center,
                        children: [
                          Icon(
                            Icons.chat_bubble_rounded,
                            color: primaryRed,
                            size: 26,
                          ),
                          if (hasActiveTelemetry)
                            Positioned(
                              top: 6,
                              right: 6,
                              child: Container(
                                width: 9,
                                height: 9,
                                decoration: BoxDecoration(
                                  color: Colors.green.shade600,
                                  shape: BoxShape.circle,
                                  border: Border.all(
                                      color: Colors.white, width: 1.5),
                                ),
                              ),
                            ),
                        ],
                      ),
                    ),
                  ),
                ),
              ),
          ],
        );
      },
    );
  }
}
