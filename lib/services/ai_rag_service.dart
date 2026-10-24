import 'dart:convert';
import 'dart:io';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

class AiRagDocument {
  final String id;
  final String title;
  final String instrument;
  final List<String> keywords;
  final String content;

  const AiRagDocument({
    required this.id,
    required this.title,
    required this.instrument,
    required this.keywords,
    required this.content,
  });

  factory AiRagDocument.fromJson(Map<String, dynamic> json) {
    return AiRagDocument(
      id: json['id'] as String? ?? '',
      title: json['title'] as String? ?? '',
      instrument: json['instrument'] as String? ?? '',
      keywords: (json['keywords'] as List<dynamic>?)
              ?.map((e) => e.toString().toLowerCase())
              .toList() ??
          [],
      content: json['content'] as String? ?? '',
    );
  }

  Map<String, dynamic> toJson() => {
        'id': id,
        'title': title,
        'instrument': instrument,
        'keywords': keywords,
        'content': content,
      };
}

class AiRagService {
  static List<AiRagDocument>? _loadedDocuments;

  static Future<void> loadKnowledgeAsset([
    String assetPath = 'assets/knowledge/knowledge.json',
  ]) async {
    try {
      String? jsonString;
      try {
        jsonString = await rootBundle.loadString(assetPath);
      } catch (rootError) {
        if (!kIsWeb) {
          final file = File(assetPath);
          if (await file.exists()) {
            jsonString = await file.readAsString();
          }
        }
        if (jsonString == null) rethrow;
      }

      final dynamic decoded = jsonDecode(jsonString);
      if (decoded is Map<String, dynamic> && decoded['documents'] is List) {
        final docsList = decoded['documents'] as List<dynamic>;
        _loadedDocuments = docsList
            .whereType<Map<String, dynamic>>()
            .map((e) => AiRagDocument.fromJson(e))
            .toList();
        debugPrint(
            '[AiRagService] Loaded ${_loadedDocuments!.length} documents from $assetPath');
      }
    } catch (e) {
      debugPrint('[AiRagService] Notice: Asset load ($assetPath) error: $e');
    }
  }

  static List<AiRagDocument> get allDocuments {
    if (_loadedDocuments != null) return _loadedDocuments!;
    if (!kIsWeb) {
      try {
        final file = File('assets/knowledge/knowledge.json');
        if (file.existsSync()) {
          final jsonString = file.readAsStringSync();
          final dynamic decoded = jsonDecode(jsonString);
          if (decoded is Map<String, dynamic> && decoded['documents'] is List) {
            final docsList = decoded['documents'] as List<dynamic>;
            _loadedDocuments = docsList
                .whereType<Map<String, dynamic>>()
                .map((e) => AiRagDocument.fromJson(e))
                .toList();
            return _loadedDocuments!;
          }
        }
      } catch (_) {}
    }
    return const [];
  }

  @visibleForTesting
  static void setDocumentsForTesting(List<AiRagDocument>? docs) {
    _loadedDocuments = docs;
  }

  static List<AiRagDocument> retrieveKnowledge({
    required String query,
    String? activeInstrument,
    String? telemetry,
    int maxResults = 3,
  }) {
    final cleanQuery = query.toLowerCase();
    final queryTokens = cleanQuery
        .split(RegExp(r'[\s,._\-?!\/\\]+'))
        .where((t) => t.length > 1)
        .toSet();

    final cleanTelemetry = (telemetry ?? '').toLowerCase();
    final telemetryTokens = cleanTelemetry
        .split(RegExp(r'[\s,._\-?!\/\\]+'))
        .where((t) => t.length > 1)
        .toSet();

    final isOverviewOrGeneral = activeInstrument == null ||
        activeInstrument.isEmpty ||
        activeInstrument.toLowerCase().contains('overview') ||
        activeInstrument.toLowerCase().contains('home') ||
        cleanQuery.contains('what is pslab') ||
        cleanQuery.contains('instruments') ||
        cleanQuery.contains('what can') ||
        cleanQuery.contains('perform') ||
        cleanQuery.contains('help');

    final scoredDocs = <_ScoredDocument>[];

    for (final doc in allDocuments) {
      double score = 0.0;
      if (activeInstrument != null && activeInstrument.isNotEmpty) {
        if (doc.instrument.toLowerCase() == activeInstrument.toLowerCase()) {
          score += 6.0;
        } else if (doc.instrument.toLowerCase() == 'sensors' &&
            [
              'lux meter',
              'barometer',
              'accelerometer',
              'gyroscope',
              'compass',
              'thermometer',
              'sound meter',
              'gas sensor'
            ].contains(activeInstrument.toLowerCase())) {
          score += 3.0;
        }
      }
      for (final keyword in doc.keywords) {
        if (queryTokens.contains(keyword)) {
          score += 3.5;
        } else if (cleanQuery.contains(keyword)) {
          score += 2.0;
        }
      }
      for (final keyword in doc.keywords) {
        if (telemetryTokens.contains(keyword)) {
          score += 2.5;
        }
      }

      if (doc.id == 'oscilloscope_mic_channel') {
        if (cleanQuery.contains('mic') ||
            cleanQuery.contains('sound') ||
            cleanQuery.contains('audio') ||
            cleanTelemetry.contains('mic')) {
          score += 8.0;
        }
      }
      if (doc.id == 'instruments_catalog_overview' && isOverviewOrGeneral) {
        score += 7.0;
      }

      if (cleanQuery.contains('experiment') ||
          cleanQuery.contains('perform') ||
          cleanQuery.contains('how to test') ||
          cleanQuery.contains('circuit')) {
        if (doc.id.startsWith('experiment_')) {
          score += 5.0;
        }
      }
      if (doc.id == 'pslab_software_architecture_flutter_rust') {
        if (cleanQuery.contains('architecture') ||
            cleanQuery.contains('flutter') ||
            cleanQuery.contains('rust') ||
            cleanQuery.contains('python') ||
            cleanQuery.contains('made with') ||
            cleanQuery.contains('how is it made') ||
            cleanQuery.contains('how pslab app is made') ||
            cleanQuery.contains('how is the app made') ||
            cleanQuery.contains('tech stack') ||
            cleanQuery.contains('technology') ||
            cleanQuery.contains('language') ||
            cleanQuery.contains('framework') ||
            cleanQuery.contains('statemanagement') ||
            cleanQuery.contains('state management') ||
            cleanQuery.contains('backend')) {
          score += 10.0;
        }
      }

      if (score > 0) {
        scoredDocs.add(_ScoredDocument(doc: doc, score: score));
      }
    }

    scoredDocs.sort((a, b) => b.score.compareTo(a.score));

    return scoredDocs.take(maxResults).map((s) => s.doc).toList();
  }

  static String formatPromptBlock({
    required String query,
    String? activeInstrument,
    String? telemetry,
    int maxResults = 3,
  }) {
    final docs = retrieveKnowledge(
      query: query,
      activeInstrument: activeInstrument,
      telemetry: telemetry,
      maxResults: maxResults,
    );

    if (docs.isEmpty) return '';

    final buffer = StringBuffer();
    buffer.writeln('[PSLAB DOMAIN KNOWLEDGE BASE (RETRIEVED CONTEXT)]');
    for (final doc in docs) {
      buffer.writeln('• ${doc.title}:');
      buffer.writeln('  ${doc.content}');
    }
    return buffer.toString().trim();
  }
}

class _ScoredDocument {
  final AiRagDocument doc;
  final double score;

  _ScoredDocument({required this.doc, required this.score});
}
