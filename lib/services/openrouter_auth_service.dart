import 'dart:async';
import 'dart:convert';
import 'dart:io';
import 'dart:math';

import 'package:app_links/app_links.dart';
import 'package:crypto/crypto.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';
import 'package:url_launcher/url_launcher.dart';

class OpenRouterAuthService {
  static const String _storageKey = 'openrouter_api_key';

  final FlutterSecureStorage _storage;
  final AppLinks _appLinks;

  OpenRouterAuthService({
    FlutterSecureStorage? storage,
    AppLinks? appLinks,
  })  : _storage = storage ?? const FlutterSecureStorage(),
        _appLinks = appLinks ?? AppLinks();

  String? _pendingVerifier;
  HttpServer? _loopbackServer;
  StreamSubscription<Uri>? _linkSubscription;

  static String generateCodeVerifier() {
    final random = Random.secure();
    final values = List<int>.generate(48, (_) => random.nextInt(256));
    return base64UrlEncode(values).replaceAll('=', '');
  }

  static String generateCodeChallenge(String verifier) {
    final bytes = ascii.encode(verifier);
    final digest = sha256.convert(bytes);
    return base64UrlEncode(digest.bytes).replaceAll('=', '');
  }

  Future<bool> isAuthenticated() async {
    final key = await getApiKey();
    return key != null && key.trim().isNotEmpty;
  }

  static String sanitizeKey(String raw) {
    var k = raw
        .replaceAll('\u00A0', ' ')
        .replaceAll('\u200B', '')
        .replaceAll('\uFEFF', '')
        .trim();
    if (k.toLowerCase().startsWith('bearer ')) {
      k = k.substring(7).trim();
    }
    while ((k.startsWith('"') && k.endsWith('"')) ||
        (k.startsWith("'") && k.endsWith("'"))) {
      k = k.substring(1, k.length - 1).trim();
    }
    final skIndex = k.indexOf('sk-');
    if (skIndex > 0) {
      k = k.substring(skIndex).trim();
    }
    k = k.replaceAll(RegExp(r'[\x00-\x1F\x7F-\x9F\xA0]'), '').trim();
    return k;
  }

  Future<String?> getApiKey() async {
    try {
      final key = await _storage.read(key: _storageKey);
      if (key != null && key.trim().isNotEmpty) {
        debugPrint(
            '[OpenRouterAuthService] API key retrieved from FlutterSecureStorage');
        return sanitizeKey(key);
      }
    } catch (e) {
      debugPrint(
          '[OpenRouterAuthService] FlutterSecureStorage read failed ($e), trying SharedPreferences...');
    }
    try {
      final prefs = await SharedPreferences.getInstance();
      final key = prefs.getString(_storageKey);
      if (key != null && key.trim().isNotEmpty) {
        debugPrint(
            '[OpenRouterAuthService] API key retrieved from SharedPreferences');
        return sanitizeKey(key);
      }
    } catch (e) {
      debugPrint('[OpenRouterAuthService] SharedPreferences read failed: $e');
    }
    return null;
  }

  Future<void> saveApiKey(String key) async {
    final clean = sanitizeKey(key);
    try {
      await _storage.write(key: _storageKey, value: clean);
      debugPrint('[OpenRouterAuthService] Saved key to FlutterSecureStorage');
    } catch (e) {
      debugPrint(
          '[OpenRouterAuthService] FlutterSecureStorage write failed ($e), saving to SharedPreferences');
    }
    try {
      final prefs = await SharedPreferences.getInstance();
      await prefs.setString(_storageKey, clean);
      debugPrint('[OpenRouterAuthService] Saved key to SharedPreferences');
    } catch (e) {
      debugPrint('[OpenRouterAuthService] SharedPreferences write failed: $e');
    }
  }

  Future<void> signOut() async {
    try {
      await _storage.delete(key: _storageKey);
    } catch (_) {}
    try {
      final prefs = await SharedPreferences.getInstance();
      await prefs.remove(_storageKey);
    } catch (_) {}
    debugPrint('[OpenRouterAuthService] Cleared API keys from all storages');
  }

  Future<String?> startOAuthFlow({
    Function(String message)? onStatusUpdate,
  }) async {
    cancelPendingFlow();

    final verifier = generateCodeVerifier();
    _pendingVerifier = verifier;
    final challenge = generateCodeChallenge(verifier);

    final completer = Completer<String?>();

    int? port;
    try {
      if (!kIsWeb) {
        try {
          _loopbackServer = await HttpServer.bind(
            InternetAddress.loopbackIPv4,
            0,
          );
        } catch (_) {
          _loopbackServer = await HttpServer.bind(
            InternetAddress.anyIPv4,
            0,
          );
        }
        port = _loopbackServer!.port;
        debugPrint(
            '[OpenRouterAuthService] Loopback server listening on port $port');

        _loopbackServer!.listen((HttpRequest request) async {
          final uri = request.uri;
          final response = request.response;
          response.headers.add('Access-Control-Allow-Origin', '*');
          response.headers
              .add('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
          response.headers.add('Access-Control-Allow-Headers', '*');
          response.headers.add('Access-Control-Allow-Private-Network', 'true');

          if (request.method == 'OPTIONS') {
            response.statusCode = HttpStatus.noContent;
            await response.close();
            return;
          }
          if (uri.path == '/favicon.ico') {
            response.statusCode = HttpStatus.notFound;
            await response.close();
            return;
          }

          final queryCode = uri.queryParameters['code'] ??
              uri.queryParameters['auth_code'] ??
              uri.queryParameters['code_verifier'];
          final queryKey = uri.queryParameters['key'] ??
              uri.queryParameters['api_key'] ??
              uri.queryParameters['access_token'] ??
              uri.queryParameters['token'];
          final error = uri.queryParameters['error'];

          String? postCode;
          String? postKey;
          if (request.method == 'POST') {
            try {
              final rawBody = await utf8.decoder.bind(request).join();
              if (rawBody.isNotEmpty) {
                if (rawBody.trim().startsWith('{')) {
                  final map = jsonDecode(rawBody);
                  if (map is Map) {
                    postCode =
                        map['code']?.toString() ?? map['auth_code']?.toString();
                    postKey =
                        map['key']?.toString() ?? map['api_key']?.toString();
                  }
                } else {
                  final split = Uri.splitQueryString(rawBody);
                  postCode = split['code'] ?? split['auth_code'];
                  postKey = split['key'] ?? split['api_key'];
                }
              }
            } catch (_) {}
          }

          final code = queryCode ?? postCode;
          final directKey = queryKey ?? postKey;

          response.headers.contentType = ContentType.html;

          if (directKey != null && directKey.isNotEmpty) {
            response.write(_buildAuthSuccessHtml());
            await response.close();
            if (!completer.isCompleted) {
              onStatusUpdate?.call('API key received directly');
              await saveApiKey(directKey);
              completer.complete(directKey);
            }
          } else if (code != null && code.isNotEmpty) {
            response.write(_buildAuthSuccessHtml());
            await response.close();

            if (!completer.isCompleted) {
              onStatusUpdate?.call('Exchanging authorization code...');
              final apiKey = await exchangeCodeForApiKey(code, verifier);
              completer.complete(apiKey);
            }
          } else if (error != null) {
            response.write(_buildAuthErrorHtml(error));
            await response.close();
            if (!completer.isCompleted) {
              completer.complete(null);
            }
          } else {
            response.write('''
              <!DOCTYPE html>
              <html>
                <head>
                  <title>PSLab Authentication</title>
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <style>
                    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background: #121212; color: #fff; text-align: center; padding: 32px 16px; margin: 0; }
                    .card { background: #1E1E1E; border: 1px solid #D32F2F; border-radius: 8px; max-width: 420px; margin: 0 auto; padding: 24px; box-sizing: border-box; }
                    h2 { color: #D32F2F; margin-top: 0; font-size: 20px; }
                    p { color: #ccc; font-size: 13.5px; line-height: 1.4; }
                    input { width: 100%; box-sizing: border-box; padding: 12px; border-radius: 6px; border: 1px solid #444; background: #2A2A2A; color: #fff; font-size: 14px; margin: 12px 0; }
                    button { background: #D32F2F; color: #fff; border: none; padding: 12px 24px; border-radius: 6px; font-weight: bold; font-size: 14px; cursor: pointer; width: 100%; }
                    button:active { background: #B71C1C; }
                    a { color: #D32F2F; text-decoration: none; font-size: 13px; display: inline-block; margin-top: 14px; }
                  </style>
                </head>
                <body>
                  <div class="card">
                    <h2 id="heading">Completing Authentication...</h2>
                    <p id="msg">Processing credentials. Please wait...</p>
                    <div id="fallback" style="display: none; margin-top: 16px;">
                      <p style="color: #bbb; font-size: 13px;">If OpenRouter displayed an authorization code or API key on screen, paste it below to link PSLab immediately:</p>
                      <input type="text" id="manual-code" placeholder="Paste OpenRouter code or API key" />
                      <button onclick="submitManual()">Complete Sign-In</button>
                      <div>
                        <a href="pslab://auth/callback">Return to PSLab App &rarr;</a>
                      </div>
                    </div>
                  </div>
                  <script>
                    function handleCode(code) {
                      document.getElementById('heading').innerText = 'Linking PSLab...';
                      document.getElementById('msg').innerText = 'Exchanging authorization credentials with PSLab...';
                      document.getElementById('fallback').style.display = 'none';

                      fetch('/callback?code=' + encodeURIComponent(code)).then(function() {
                        document.getElementById('heading').innerText = 'Authentication Successful';
                        document.getElementById('msg').innerText = 'Your OpenRouter account is now linked! You can return to PSLab.';
                        // Also trigger deep link intent as backup
                        setTimeout(function() {
                          window.location.href = 'pslab://auth/callback?code=' + encodeURIComponent(code);
                        }, 500);
                      }).catch(function(err) {
                        document.getElementById('msg').innerText = 'Error: ' + err;
                        document.getElementById('fallback').style.display = 'block';
                      });
                    }

                    function submitManual() {
                      var input = document.getElementById('manual-code');
                      var val = input.value ? input.value.trim() : '';
                      if (val) {
                        handleCode(val);
                      }
                    }

                    (function() {
                      var hash = window.location.hash ? window.location.hash.substring(1) : '';
                      var search = window.location.search ? window.location.search.substring(1) : '';
                      var combined = hash ? (search ? search + '&' + hash : hash) : search;
                      var params = new URLSearchParams(combined);
                      var code = params.get('code') || params.get('auth_code') || params.get('key') || params.get('api_key');

                      if (!code && document.referrer) {
                        try {
                          var refUrl = new URL(document.referrer);
                          code = refUrl.searchParams.get('code') || refUrl.searchParams.get('auth_code');
                        } catch(_) {}
                      }

                      if (code) {
                        handleCode(code);
                      } else {
                        document.getElementById('heading').innerText = 'Authentication Callback';
                        document.getElementById('msg').innerText = 'Redirect completed. Complete sign-in below if not automatically detected:';
                        document.getElementById('fallback').style.display = 'block';
                      }
                    })();
                  </script>
                </body>
              </html>
            ''');
            await response.close();
          }
        });
      }
    } catch (e) {
      debugPrint('[OpenRouterAuthService] Loopback server error: $e');
    }
    try {
      _linkSubscription = _appLinks.uriLinkStream.listen((uri) async {
        _handleIncomingUri(uri, verifier, completer, onStatusUpdate);
      });
      final initialUri = await _appLinks.getInitialLink();
      if (initialUri != null) {
        _handleIncomingUri(initialUri, verifier, completer, onStatusUpdate);
      }
    } catch (e) {
      debugPrint('[OpenRouterAuthService] AppLinks error: $e');
    }
    final String host = Platform.isAndroid ? '127.0.0.1' : 'localhost';
    final callbackUrl =
        port != null ? 'http://$host:$port/callback' : 'pslab://auth/callback';

    final authUri = Uri.https('openrouter.ai', '/auth', {
      'callback_url': callbackUrl,
      'code_challenge': challenge,
      'code_challenge_method': 'S256',
    });

    onStatusUpdate?.call('Opening OpenRouter login...');
    bool launched = false;
    if (Platform.isAndroid || Platform.isIOS) {
      try {
        launched = await launchUrl(
          authUri,
          mode: LaunchMode.inAppBrowserView,
        );
      } catch (_) {}
    }
    if (!launched) {
      launched = await launchUrl(
        authUri,
        mode: LaunchMode.externalApplication,
      );
    }

    if (!launched) {
      cancelPendingFlow();
      throw Exception('Could not launch OpenRouter authorization URL.');
    }
    final timeoutTimer = Timer(const Duration(minutes: 3), () {
      if (!completer.isCompleted) {
        cancelPendingFlow();
        completer.complete(null);
      }
    });

    try {
      final key = await completer.future;
      if (key != null) {
        await saveApiKey(key);
      }
      return key;
    } finally {
      timeoutTimer.cancel();
      cancelPendingFlow();
    }
  }

  void _handleIncomingUri(
    Uri uri,
    String verifier,
    Completer<String?> completer,
    Function(String)? onStatusUpdate,
  ) async {
    if (uri.scheme == 'pslab' &&
        (uri.host == 'auth' || uri.path.contains('callback'))) {
      final code =
          uri.queryParameters['code'] ?? uri.queryParameters['auth_code'];
      final directKey =
          uri.queryParameters['key'] ?? uri.queryParameters['api_key'];
      if (directKey != null && directKey.isNotEmpty && !completer.isCompleted) {
        onStatusUpdate?.call('API key received directly');
        await saveApiKey(directKey);
        completer.complete(directKey);
      } else if (code != null && code.isNotEmpty && !completer.isCompleted) {
        onStatusUpdate?.call('Exchanging authorization code...');
        final apiKey = await exchangeCodeForApiKey(code, verifier);
        completer.complete(apiKey);
      }
    }
  }

  Future<String?> exchangeCodeForApiKey(String code, [String? verifier]) async {
    final activeVerifier = verifier ?? _pendingVerifier;
    if (activeVerifier == null) {
      throw Exception('Missing code verifier for PKCE exchange.');
    }

    final response = await http.post(
      Uri.parse('https://openrouter.ai/api/v1/auth/keys'),
      headers: {
        'Content-Type': 'application/json',
      },
      body: jsonEncode({
        'code': code.trim(),
        'code_verifier': activeVerifier,
        'code_challenge_method': 'S256',
      }),
    );

    if (response.statusCode >= 200 && response.statusCode < 300) {
      final body = jsonDecode(response.body);
      final key = body['key'] as String?;
      if (key != null && key.isNotEmpty) {
        await saveApiKey(key);
        return key;
      }
    }

    debugPrint(
        '[OpenRouterAuthService] Key exchange failed: ${response.statusCode} - ${response.body}');
    return null;
  }

  void cancelPendingFlow() {
    _loopbackServer?.close(force: true);
    _loopbackServer = null;
    _linkSubscription?.cancel();
    _linkSubscription = null;
  }

  static String _buildAuthSuccessHtml() {
    return '''
      <!DOCTYPE html>
      <html>
        <head>
          <title>PSLab Authentication</title>
          <meta name="viewport" content="width=device-width, initial-scale=1">
          <style>
            body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background: #121212; color: #FFFFFF; text-align: center; padding: 48px 16px; }
            .card { background: #1E1E1E; border: 1px solid #D32F2F; border-radius: 8px; max-width: 400px; margin: 0 auto; padding: 24px; }
            h2 { color: #D32F2F; margin-top: 0; }
            p { color: #CCCCCC; font-size: 14px; }
          </style>
        </head>
        <body>
          <div class="card">
            <h2>Authentication Successful</h2>
            <p>PSLab has successfully connected to your OpenRouter account.</p>
            <p>You may now close this browser tab and return to the application.</p>
          </div>
        </body>
      </html>
    ''';
  }

  static String _buildAuthErrorHtml(String? error) {
    return '''
      <!DOCTYPE html>
      <html>
        <head>
          <title>PSLab Authentication</title>
          <style>
            body { font-family: -apple-system, sans-serif; background: #121212; color: #fff; text-align: center; padding: 40px; }
            .card { background: #1E1E1E; border: 1px solid #D32F2F; border-radius: 8px; max-width: 400px; margin: 0 auto; padding: 24px; }
            h2 { color: #D32F2F; }
            p { color: #ccc; }
          </style>
        </head>
        <body>
          <div class="card">
            <h2>Authentication Canceled</h2>
            <p>\${error ?? 'No authorization code received.'}</p>
          </div>
        </body>
      </html>
    ''';
  }
}
