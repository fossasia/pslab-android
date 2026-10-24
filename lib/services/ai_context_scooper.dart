import 'package:flutter/material.dart';
import '../providers/locator.dart';
import '../providers/board_state_provider.dart';

abstract class AiContextScooper {
  String getInstrumentName();
  String getLiveTelemetry();
}

class AiRouteObserver extends NavigatorObserver {
  @override
  void didPush(Route<dynamic> route, Route<dynamic>? previousRoute) {
    super.didPush(route, previousRoute);
    if (route.settings.name != null) {
      AiContextRegistry.setActiveRoute(route.settings.name!);
    }
  }

  @override
  void didPop(Route<dynamic> route, Route<dynamic>? previousRoute) {
    super.didPop(route, previousRoute);
    if (previousRoute?.settings.name != null) {
      AiContextRegistry.setActiveRoute(previousRoute!.settings.name!);
    }
  }

  @override
  void didReplace({Route<dynamic>? newRoute, Route<dynamic>? oldRoute}) {
    super.didReplace(newRoute: newRoute, oldRoute: oldRoute);
    if (newRoute?.settings.name != null) {
      AiContextRegistry.setActiveRoute(newRoute!.settings.name!);
    }
  }
}

class _RouteFallbackScooper implements AiContextScooper {
  final String instrumentName;

  _RouteFallbackScooper(this.instrumentName);

  @override
  String getInstrumentName() => instrumentName;

  @override
  String getLiveTelemetry() {
    final buffer = StringBuffer();
    buffer.writeln('Active Screen: $instrumentName');
    try {
      if (getIt.isRegistered<BoardStateProvider>()) {
        final board = getIt.get<BoardStateProvider>();
        final connected =
            board.pslabIsConnected || board.pslabVersionID != 'Not Connected';
        buffer.writeln(
            'Hardware Connection: ${connected ? "Connected (${board.pslabVersionID})" : "Disconnected / Standalone"}');
      }
    } catch (_) {}
    return buffer.toString().trim();
  }
}

class AiContextRegistry {
  static final List<AiContextScooper> _scoopers = [];
  static final AiRouteObserver routeObserver = AiRouteObserver();
  static String _activeRoute = '/';

  static final Map<String, String> _routeInstrumentMap = {
    '/': 'Instruments Overview',
    '/oscilloscope': 'Oscilloscope',
    '/multimeter': 'Multimeter',
    '/waveGenerator': 'Wave Generator',
    '/logicAnalyzer': 'Logic Analyzer',
    '/powerSource': 'Power Source',
    '/soundmeter': 'Sound Meter',
    '/luxmeter': 'Lux Meter',
    '/barometer': 'Barometer',
    '/accelerometer': 'Accelerometer',
    '/gyroscope': 'Gyroscope',
    '/compass': 'Compass',
    '/thermometer': 'Thermometer',
    '/gassensor': 'Gas Sensor',
    '/roboticArm': 'Robotic Arm',
    '/oledDisplay': 'OLED Display',
    '/experiments': 'Experiments',
    '/sensors': 'Sensors Overview',
    '/loggedData': 'Logged Data',
    '/connectDevice': 'Device Connection',
    '/settings': 'Settings',
    '/aiAssistant': 'AI Assistant Settings',
  };

  static String get activeRoute => _activeRoute;

  static void setActiveRoute(String routeName) {
    _activeRoute = routeName;
    debugPrint('[AiContextRegistry] Active route updated: $routeName');
  }

  static AiContextScooper? get activeScooper {
    final expectedInstrument = _routeInstrumentMap[_activeRoute];
    if (expectedInstrument != null) {
      for (int i = _scoopers.length - 1; i >= 0; i--) {
        final scooper = _scoopers[i];
        if (scooper.getInstrumentName().toLowerCase() ==
            expectedInstrument.toLowerCase()) {
          return scooper;
        }
      }

      if (_activeRoute != '/') {
        return _RouteFallbackScooper(expectedInstrument);
      }
    }
    if (_scoopers.isNotEmpty) {
      return _scoopers.last;
    }

    return expectedInstrument != null
        ? _RouteFallbackScooper(expectedInstrument)
        : null;
  }

  static void register(AiContextScooper scooper) {
    _scoopers.remove(scooper);
    _scoopers.add(scooper);
    debugPrint(
        '[AiContextRegistry] Registered active scooper: ${scooper.getInstrumentName()}');
  }

  static void unregister(AiContextScooper scooper) {
    _scoopers.remove(scooper);
    debugPrint(
        '[AiContextRegistry] Unregistered scooper: ${scooper.getInstrumentName()}');
  }
}
