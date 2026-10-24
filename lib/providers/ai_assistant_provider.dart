import 'dart:async';
import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';
import '../services/ai_context_scooper.dart';
import '../services/ai_rag_service.dart';
import '../services/openrouter_auth_service.dart';

class ChatMessage {
  final String role;
  String content;
  final String? attachedTelemetry;
  final DateTime timestamp;

  ChatMessage({
    required this.role,
    required this.content,
    this.attachedTelemetry,
    DateTime? timestamp,
  }) : timestamp = timestamp ?? DateTime.now();
}

class AiAssistantProvider extends ChangeNotifier {
  static const String _prefModelKey = 'openrouter_selected_model';
  static const String _prefFloatingKey =
      'openrouter_floating_assistant_enabled';
  static const String defaultModel = 'openrouter/free';

  static const List<String> availableModels = [
    'openrouter/free',
    'nvidia/nemotron-3.5-lightning:free',
    'minimax/minimax-m3.1-flash:free',
    'cohere/north-mini-code:free',
    'google/gemma-4-31b-it:free',
    'google/gemma-4-26b-a4b-it:free',
  ];

  final OpenRouterAuthService _authService = OpenRouterAuthService();
  bool _isAuthenticated = false;
  bool _isFloatingEnabled = true;
  String _selectedModel = defaultModel;
  bool _isLoading = false;
  String? _statusMessage;

  final List<ChatMessage> _messages = [];
  final List<String> _diagnosticLogs = [];

  String? _maskedApiKey;

  bool get isAuthenticated => _isAuthenticated;
  bool get isFloatingEnabled => _isFloatingEnabled;
  String get selectedModel => _selectedModel;
  bool get isLoading => _isLoading;
  String? get statusMessage => _statusMessage;
  String? get maskedApiKey => _maskedApiKey;
  List<ChatMessage> get messages => List.unmodifiable(_messages);
  List<String> get diagnosticLogs => List.unmodifiable(_diagnosticLogs);
  String get diagnosticLogsText => _diagnosticLogs.join('\n');

  void addLog(String message) {
    final timestamp = DateTime.now().toIso8601String().substring(11, 19);
    final entry = '[$timestamp] $message';
    _diagnosticLogs.add(entry);
    if (_diagnosticLogs.length > 100) {
      _diagnosticLogs.removeAt(0);
    }
    debugPrint('[AiAssistant] $message');
  }

  void clearDiagnosticLogs() {
    _diagnosticLogs.clear();
    addLog('Diagnostic logs cleared.');
    notifyListeners();
  }

  Future<Map<String, dynamic>> testConnection() async {
    addLog('Starting test connection...');
    final apiKey = await _authService.getApiKey();
    if (apiKey == null || apiKey.isEmpty) {
      addLog('Test Connection Failed: No API key found in storage.');
      return {
        'success': false,
        'message': 'No API key configured. Please enter your key first.',
      };
    }
    final cleanKey = OpenRouterAuthService.sanitizeKey(apiKey);

    final stopwatch = Stopwatch()..start();
    try {
      addLog(
          'Sending test ping to OpenRouter [Model: $_selectedModel, Key: $_maskedApiKey]...');
      final response = await http
          .post(
            Uri.parse('https://openrouter.ai/api/v1/chat/completions'),
            headers: {
              'Content-Type': 'application/json',
              'Authorization': 'Bearer $cleanKey',
              'HTTP-Referer': 'https://pslab.io',
              'X-Title': 'PSLab Hardware Assistant',
            },
            body: jsonEncode({
              'model': _selectedModel,
              'messages': [
                {'role': 'user', 'content': 'ping'}
              ],
              'max_tokens': 10,
            }),
          )
          .timeout(const Duration(seconds: 20));
      stopwatch.stop();

      addLog(
          'Test ping response: HTTP ${response.statusCode} in ${stopwatch.elapsedMilliseconds}ms.');

      if (response.statusCode >= 200 && response.statusCode < 300) {
        final snippet = response.body.length > 80
            ? '${response.body.substring(0, 80)}...'
            : response.body;
        addLog('Test Connection Successful! Body: $snippet');
        return {
          'success': true,
          'message':
              'Connected successfully (${stopwatch.elapsedMilliseconds}ms latency)',
        };
      } else {
        final errorMsg = _parseApiError(response.body, response.statusCode);
        addLog(
            'Test Connection Failed: HTTP ${response.statusCode} - $errorMsg (Raw: ${response.body})');
        return {
          'success': false,
          'message': 'Error (${response.statusCode}): $errorMsg',
        };
      }
    } catch (e) {
      stopwatch.stop();
      addLog(
          'Test Connection Exception ($e) after ${stopwatch.elapsedMilliseconds}ms');
      return {
        'success': false,
        'message': 'Connection error: $e',
      };
    }
  }

  AiAssistantProvider() {
    initialize();
  }

  Future<void> initialize() async {
    final prefs = await SharedPreferences.getInstance();
    _isFloatingEnabled = prefs.getBool(_prefFloatingKey) ?? true;
    final savedModel = prefs.getString(_prefModelKey);
    if (savedModel != null && availableModels.contains(savedModel)) {
      _selectedModel = savedModel;
    } else {
      _selectedModel = defaultModel;
      await prefs.setString(_prefModelKey, defaultModel);
    }
    final rawKey = await _authService.getApiKey();
    if (rawKey != null && rawKey.trim().isNotEmpty) {
      final cleanKey = OpenRouterAuthService.sanitizeKey(rawKey);
      if (cleanKey != rawKey) {
        await _authService.saveApiKey(cleanKey);
      }
      _isAuthenticated = true;
      _updateMaskedApiKey(cleanKey);
    } else {
      _isAuthenticated = false;
      _updateMaskedApiKey(null);
    }
    addLog(
        'Initialized Assistant. Model: $_selectedModel, Authenticated: $_isAuthenticated (Key: ${_maskedApiKey ?? "None"})');
    unawaited(AiRagService.loadKnowledgeAsset());
    notifyListeners();
  }

  void _updateMaskedApiKey(String? key) {
    if (key != null && key.trim().isNotEmpty) {
      final trimmed = key.trim();
      if (trimmed.length > 10) {
        _maskedApiKey =
            '${trimmed.substring(0, 6)}...${trimmed.substring(trimmed.length - 4)}';
      } else {
        _maskedApiKey = '••••••••';
      }
    } else {
      _maskedApiKey = null;
    }
  }

  Future<void> setFloatingEnabled(bool enabled) async {
    _isFloatingEnabled = enabled;
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool(_prefFloatingKey, enabled);
    addLog('Floating assistant toggled: $enabled');
    notifyListeners();
  }

  Future<void> setSelectedModel(String model) async {
    _selectedModel = model;
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_prefModelKey, model);
    addLog('Selected model changed to: $model');
    notifyListeners();
  }

  Future<void> startLogin() async {
    _isLoading = true;
    _statusMessage = 'Connecting to OpenRouter...';
    notifyListeners();

    try {
      final key = await _authService.startOAuthFlow(
        onStatusUpdate: (status) {
          _statusMessage = status;
          notifyListeners();
        },
      );

      _isAuthenticated = key != null && key.isNotEmpty;
      _updateMaskedApiKey(key);
      _statusMessage =
          _isAuthenticated ? 'Connected successfully' : 'Login canceled';
    } catch (e) {
      _statusMessage = 'Authentication failed: $e';
      _isAuthenticated = false;
      _maskedApiKey = null;
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  void cancelLogin() {
    _authService.cancelPendingFlow();
    _isLoading = false;
    _statusMessage = 'Login canceled';
    addLog('OAuth login flow canceled by user.');
    notifyListeners();
  }

  Future<bool> saveManualApiKey(String key) async {
    final cleanKey = OpenRouterAuthService.sanitizeKey(key);
    if (cleanKey.isEmpty) return false;

    _isLoading = true;
    notifyListeners();

    try {
      String keyToSave = cleanKey;
      if (cleanKey.contains('code=') || cleanKey.contains('key=')) {
        try {
          final uri = Uri.parse(cleanKey);
          final code = uri.queryParameters['code'];
          final directKey =
              uri.queryParameters['key'] ?? uri.queryParameters['api_key'];
          if (directKey != null && directKey.isNotEmpty) {
            keyToSave = directKey;
          } else if (code != null && code.isNotEmpty) {
            final exchanged = await _authService.exchangeCodeForApiKey(code);
            if (exchanged != null && exchanged.isNotEmpty) {
              keyToSave = exchanged;
            }
          }
        } catch (_) {}
      } else if (!cleanKey.startsWith('sk-')) {
        try {
          final exchanged = await _authService.exchangeCodeForApiKey(cleanKey);
          if (exchanged != null && exchanged.isNotEmpty) {
            keyToSave = exchanged;
          }
        } catch (_) {}
      }

      await _authService.saveApiKey(keyToSave);
      _isAuthenticated = true;
      _updateMaskedApiKey(keyToSave);
      _statusMessage = 'Key saved successfully';
      return true;
    } catch (e) {
      _statusMessage = 'Failed to save key: $e';
      return false;
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<void> signOut() async {
    await _authService.signOut();
    _isAuthenticated = false;
    _maskedApiKey = null;
    _statusMessage = 'Disconnected';
    _messages.clear();
    notifyListeners();
  }

  void clearChat() {
    _messages.clear();
    notifyListeners();
  }

  Future<void> sendMessage(String text) async {
    final cleanText = text.trim();
    if (cleanText.isEmpty) {
      addLog('sendMessage: Empty query ignored.');
      return;
    }
    if (_isLoading) {
      addLog('sendMessage: Ignored because another request is in progress.');
      return;
    }

    addLog('sendMessage: "$cleanText"');
    final apiKey = await _authService.getApiKey();
    if (apiKey == null || apiKey.isEmpty) {
      addLog('sendMessage ERROR: No API key found in storage.');
      _messages.add(ChatMessage(
        role: 'assistant',
        content:
            'Please connect your OpenRouter account or enter an API key in Assistant Settings first.',
      ));
      notifyListeners();
      return;
    }
    final cleanKey = OpenRouterAuthService.sanitizeKey(apiKey);

    final scooper = AiContextRegistry.activeScooper;
    final attachedTelemetry = scooper?.getLiveTelemetry();
    final instrumentName =
        scooper?.getInstrumentName() ?? 'Instruments Overview';
    addLog(
        'Active instrument: $instrumentName, telemetry attached: ${attachedTelemetry != null && attachedTelemetry.isNotEmpty}');

    // Add user message
    _messages.add(ChatMessage(
      role: 'user',
      content: cleanText,
      attachedTelemetry: attachedTelemetry,
    ));

    _isLoading = true;
    notifyListeners();

    try {
      final apiMessages = <Map<String, String>>[];
      final systemPrompt = StringBuffer();
      systemPrompt.writeln(
          'You are the Pocket Science Lab (PSLab) engineering and science assistant.');
      systemPrompt.writeln(
          'Help the user with scientific experiments, circuit wiring, sensor measurements, and hardware instrumentation with engineering precision.');
      systemPrompt.writeln('CRITICAL INSTRUCTIONS:\n'
          '• Output ONLY your direct answer to the user.\n'
          '• Do NOT output your thought process, analysis, scratchpad, planning steps, or meta-commentary (NEVER write "1. Analyze the User\'s Request", "2. Determine the Goal", or "Drafting Experiments").\n'
          '• Start immediately with the experiment guides or answers.\n'
          '• Format mathematical formulas with standard LaTeX: use \\(...\\) for inline math, and \\[...\\] for display formulas.\n'
          '• When asked about experiments for sensors, provide practical, step-by-step procedures for the sensor instruments (Lux Meter, Accelerometer, Barometer, Compass, Gyroscope, Thermometer, Gas Sensor, Sound Meter) including wiring/pins, formulas, and observable outcomes.\n'
          '• When asked about how the PSLab app is built or its technology stack, explain that the modern app is 100% built with Flutter (Dart) for UI and reactive state management (Provider/ChangeNotifier) and Rust for backend communication, low-level USB/serial protocols, and high-performance hardware interfacing (bridged via flutter_rust_bridge). Emphasize that the app is NOT written in Python (pslab-python was a legacy 2014-2018 library); never state or hallucinate that the app is made with Python.');
      systemPrompt.writeln('[CURRENT APPLICATION SCREEN CONTEXT]');
      systemPrompt.writeln('Active Screen / Instrument: $instrumentName');
      if (attachedTelemetry != null && attachedTelemetry.isNotEmpty) {
        systemPrompt.writeln('Live Telemetry:\n$attachedTelemetry');
      } else {
        systemPrompt
            .writeln('Live Telemetry: Main Instruments Overview screen.');
      }

      final ragBlock = AiRagService.formatPromptBlock(
        query: cleanText,
        activeInstrument: instrumentName,
        telemetry: attachedTelemetry,
      );
      if (ragBlock.isNotEmpty) {
        systemPrompt.writeln();
        systemPrompt.writeln(ragBlock);
        addLog('Injected RAG domain knowledge into prompt.');
      }

      apiMessages.add({
        'role': 'system',
        'content': systemPrompt.toString().trim(),
      });

      final recentHistory = _messages.length > 6
          ? _messages.sublist(_messages.length - 6)
          : _messages;

      for (final msg in recentHistory) {
        apiMessages.add({
          'role': msg.role,
          'content': msg.content,
        });
      }

      Future<http.StreamedResponse> sendApiRequest(
          String model, int timeoutSec) {
        final request = http.Request(
            'POST', Uri.parse('https://openrouter.ai/api/v1/chat/completions'))
          ..headers.addAll({
            'Content-Type': 'application/json',
            'Authorization': 'Bearer $cleanKey',
            'HTTP-Referer': 'https://pslab.io',
            'X-Title': 'PSLab AI Assistant',
          })
          ..body = jsonEncode({
            'model': model,
            'messages': apiMessages,
            'stream': true,
            'max_tokens': 1600,
            'include_reasoning': false,
            'temperature': 0.6,
          });
        return request.send().timeout(Duration(seconds: timeoutSec));
      }

      addLog(
          'Calling OpenRouter API (model: $_selectedModel, messages: ${apiMessages.length})...');
      final stopwatch = Stopwatch()..start();

      http.StreamedResponse res;
      bool usedFallback = false;

      try {
        res = await sendApiRequest(_selectedModel, 25);
        stopwatch.stop();
        addLog(
            'OpenRouter stream connected: HTTP ${res.statusCode} in ${stopwatch.elapsedMilliseconds}ms');
      } on TimeoutException {
        stopwatch.stop();
        addLog(
            'Request for $_selectedModel timed out after 25 seconds waiting for connection.');
        if (_selectedModel != defaultModel) {
          addLog(
              'Retrying automatically with fallback router ($defaultModel)...');
          usedFallback = true;
          try {
            final fallbackStopwatch = Stopwatch()..start();
            res = await sendApiRequest(defaultModel, 25);
            fallbackStopwatch.stop();
            addLog(
                'Fallback connected: HTTP ${res.statusCode} in ${fallbackStopwatch.elapsedMilliseconds}ms');
          } on TimeoutException {
            addLog('Fallback model ($defaultModel) also timed out.');
            _messages.add(ChatMessage(
              role: 'assistant',
              content:
                  'Request timed out. Free tier endpoints on OpenRouter are currently congested. Please check your connection or try another model in Settings.',
            ));
            return;
          }
        } else {
          _messages.add(ChatMessage(
            role: 'assistant',
            content:
                'Request timed out after 25 seconds. OpenRouter took too long to respond. Please check your network or try again.',
          ));
          return;
        }
      }
      if (res.statusCode < 200 || res.statusCode >= 300) {
        final errBytes = await res.stream.toBytes();
        final errBody = utf8.decode(errBytes);
        final code = res.statusCode;
        final bodyLower = errBody.toLowerCase();
        final isCapacityOrNotFound = code == 404 ||
            code == 429 ||
            code == 502 ||
            code == 503 ||
            code == 504 ||
            code == 524 ||
            bodyLower.contains('no endpoints') ||
            bodyLower.contains('at capacity') ||
            bodyLower.contains('overloaded') ||
            bodyLower.contains('rate limit');

        if (_selectedModel != defaultModel &&
            !usedFallback &&
            isCapacityOrNotFound) {
          addLog(
              'Model $_selectedModel returned HTTP $code ($errBody). Retrying automatically with $defaultModel...');
          usedFallback = true;
          try {
            final fallbackStopwatch = Stopwatch()..start();
            res = await sendApiRequest(defaultModel, 25);
            fallbackStopwatch.stop();
            addLog(
                'Fallback stream connected: HTTP ${res.statusCode} in ${fallbackStopwatch.elapsedMilliseconds}ms');
          } catch (e) {
            addLog('Fallback request failed: $e');
            _messages.add(ChatMessage(
              role: 'assistant',
              content:
                  'Primary model and fallback were temporarily unavailable: $e',
            ));
            return;
          }
        }
        if (res.statusCode < 200 || res.statusCode >= 300) {
          final errorMsg = _parseApiError(errBody, res.statusCode);
          addLog(
              'API error HTTP ${res.statusCode}: $errorMsg | Full: $errBody');
          _messages.add(ChatMessage(
            role: 'assistant',
            content: 'Error (${res.statusCode}): $errorMsg',
          ));
          return;
        }
      }
      final contentType = res.headers['content-type'] ?? '';
      if (contentType.contains('text/event-stream')) {
        final assistantMessage = ChatMessage(
          role: 'assistant',
          content: '',
        );
        _messages.add(assistantMessage);
        notifyListeners();

        int lastNotifyTime = DateTime.now().millisecondsSinceEpoch;
        final streamStopwatch = Stopwatch()..start();
        String? streamErrorMessage;
        String reasoningAccumulator = '';

        await for (final line in res.stream
            .transform(utf8.decoder)
            .transform(const LineSplitter())) {
          final trimmed = line.trim();
          if (trimmed.isEmpty) continue;
          if (trimmed.startsWith('data: ')) {
            final dataStr = trimmed.substring(6).trim();
            if (dataStr == '[DONE]') break;
            try {
              final json = jsonDecode(dataStr);
              if (json is Map && json.containsKey('error')) {
                final err = json['error'];
                final msg = err is Map
                    ? (err['message'] ?? err.toString())
                    : err.toString();
                streamErrorMessage = msg.toString();
                addLog('OpenRouter stream event error: $streamErrorMessage');
              }
              final choices = json['choices'] as List<dynamic>?;
              if (choices != null && choices.isNotEmpty) {
                final delta = choices[0]['delta'] as Map<String, dynamic>?;
                final textChunk = (delta?['content'] as String?) ??
                    (delta?['text'] as String?) ??
                    (choices[0]['text'] as String?);
                final reasoningChunk = (delta?['reasoning'] as String?) ??
                    (delta?['thought'] as String?);

                if (textChunk != null && textChunk.isNotEmpty) {
                  assistantMessage.content += textChunk;
                } else if (reasoningChunk != null &&
                    reasoningChunk.isNotEmpty) {
                  reasoningAccumulator += reasoningChunk;
                }

                if (assistantMessage.content.isNotEmpty) {
                  final now = DateTime.now().millisecondsSinceEpoch;
                  if (now - lastNotifyTime > 50) {
                    lastNotifyTime = now;
                    notifyListeners();
                  }
                }
              }
            } catch (_) {}
          }
        }
        streamStopwatch.stop();
        addLog(
            'Stream finished: ${assistantMessage.content.length} chars (reasoning: ${reasoningAccumulator.length}) in ${streamStopwatch.elapsedMilliseconds}ms.');
        if (assistantMessage.content.trim().isEmpty &&
            reasoningAccumulator.trim().isNotEmpty) {
          addLog(
              'Using accumulated reasoning text as response content (${reasoningAccumulator.length} chars).');
          assistantMessage.content = reasoningAccumulator.trim();
        }
        if (assistantMessage.content.trim().isEmpty) {
          addLog(
              'Stream yielded 0 content. Triggering robust non-streaming completion fallback...');
          try {
            final fbRes = await http
                .post(
                  Uri.parse('https://openrouter.ai/api/v1/chat/completions'),
                  headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer $cleanKey',
                    'HTTP-Referer': 'https://pslab.io',
                    'X-Title': 'PSLab AI Assistant',
                  },
                  body: jsonEncode({
                    'model': defaultModel,
                    'messages': apiMessages,
                    'max_tokens': 1600,
                    'include_reasoning': false,
                    'temperature': 0.6,
                  }),
                )
                .timeout(const Duration(seconds: 30));

            if (fbRes.statusCode >= 200 && fbRes.statusCode < 300) {
              final fbBody = jsonDecode(utf8.decode(fbRes.bodyBytes));
              final choices = fbBody['choices'] as List<dynamic>?;
              if (choices != null && choices.isNotEmpty) {
                final reply = (choices[0]['message']?['content'] as String?) ??
                    (choices[0]['message']?['reasoning'] as String?) ??
                    '';
                if (reply.trim().isNotEmpty) {
                  assistantMessage.content = reply.trim();
                  addLog(
                      'Recovered response via non-streaming fallback (${assistantMessage.content.length} chars).');
                }
              }
            }
          } catch (e) {
            addLog('Non-streaming fallback failed: $e');
          }
        }
        assistantMessage.content =
            sanitizeAssistantResponse(assistantMessage.content);

        if (assistantMessage.content.trim().isEmpty) {
          assistantMessage.content = streamErrorMessage != null
              ? 'OpenRouter error: $streamErrorMessage'
              : 'The model server returned an empty response. Please verify your connection or switch to "$defaultModel" in Assistant Settings.';
        }
        notifyListeners();
      } else {
        final rawBody = await res.stream.bytesToString();
        try {
          final body = jsonDecode(rawBody);
          final choices = body['choices'] as List<dynamic>?;
          if (choices != null && choices.isNotEmpty) {
            final reply = (choices[0]['message']?['content'] as String?) ??
                (choices[0]['message']?['reasoning'] as String?) ??
                'No response received.';
            _messages.add(ChatMessage(
              role: 'assistant',
              content: sanitizeAssistantResponse(reply),
            ));
          } else {
            _messages.add(ChatMessage(
              role: 'assistant',
              content:
                  'Received empty response choices from OpenRouter. Please verify your selected model.',
            ));
          }
        } catch (e) {
          _messages.add(ChatMessage(
            role: 'assistant',
            content: 'Error decoding response: $e',
          ));
        }
        notifyListeners();
      }
    } catch (e, stack) {
      addLog('Network/Request Exception: $e');
      debugPrint('[AiAssistantProvider] Exception: $e\n$stack');
      _messages.add(ChatMessage(
        role: 'assistant',
        content: 'Failed to communicate with OpenRouter: $e',
      ));
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  String _parseApiError(String bodyString, int statusCode) {
    try {
      final parsed = jsonDecode(bodyString);
      if (parsed is Map && parsed.containsKey('error')) {
        final errorObj = parsed['error'];
        if (errorObj is Map && errorObj.containsKey('message')) {
          final msg = errorObj['message'].toString();
          if (msg.toLowerCase().contains('credit') ||
              msg.toLowerCase().contains('balance')) {
            return 'Your OpenRouter account has no prepaid credits. Switch to "openrouter/free" or a ":free" model to query 100% free with no credit card required.';
          }
          return msg;
        }
        return errorObj.toString();
      }
    } catch (_) {}
    if (statusCode == 402 || bodyString.toLowerCase().contains('credit')) {
      return 'Account credit limit reached for paid models. Switch to "openrouter/free" or a ":free" model for free unlimited queries.';
    }
    if (statusCode == 429) {
      return 'Rate limit exceeded on OpenRouter. Try switching to "openrouter/free" or another model.';
    }
    if (statusCode == 401) {
      return 'Invalid or expired API key. Please re-authenticate in Settings.';
    }
    return 'Server returned status $statusCode.';
  }

  static String sanitizeAssistantResponse(String raw) {
    if (raw.trim().isEmpty) return '';
    String cleaned = raw
        .replaceAll(
            RegExp(r'<think>[\s\S]*?<\/think>', caseSensitive: false), '')
        .replaceAll(
            RegExp(r'<thought>[\s\S]*?<\/thought>', caseSensitive: false), '')
        .trim();
    final scratchpadMatch = RegExp(
      r'^\s*1\.\s*\*{0,2}(?:Analyze|Identify|Determine|Review|Understand)[\s\S]*?(?=(?:\n\s*#{1,4}\s+|\n\s*\*{2}[A-Z]|\n\s*\*\s+\*{2}[A-Z]|\n\s*\d+\.\s+\*{2}(?!(?:Analyze|Determine|Identify|Draft|Review))[A-Z]))',
      caseSensitive: false,
    ).firstMatch(cleaned);

    if (scratchpadMatch != null && scratchpadMatch.end < cleaned.length) {
      final stripped = cleaned.substring(scratchpadMatch.end).trim();
      if (stripped.isNotEmpty) {
        cleaned = stripped;
      }
    }

    return cleaned.isNotEmpty ? cleaned : raw.trim();
  }
}
