import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_markdown/flutter_markdown.dart';
import 'package:flutter_math_fork/flutter_math.dart';
import 'package:markdown/markdown.dart' as md;
import 'package:provider/provider.dart';
import 'package:url_launcher/url_launcher.dart';
import '../../l10n/app_localizations.dart';
import '../../providers/ai_assistant_provider.dart';
import '../../services/ai_context_scooper.dart';
import '../../theme/colors.dart';

class AiChatBottomSheet extends StatefulWidget {
  const AiChatBottomSheet({super.key});

  @override
  State<AiChatBottomSheet> createState() => _AiChatBottomSheetState();
}

class _AiChatBottomSheetState extends State<AiChatBottomSheet> {
  final TextEditingController _inputController = TextEditingController();
  final ScrollController _scrollController = ScrollController();
  final PslabSyntaxHighlighter _pslabSyntaxHighlighter =
      PslabSyntaxHighlighter();
  bool _showTelemetryDetails = false;
  int _previousMessageCount = 0;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _scrollToBottom();
    });
  }

  @override
  void dispose() {
    _inputController.dispose();
    _scrollController.dispose();
    super.dispose();
  }

  void _scrollToBottom([int delayMs = 120]) {
    Future.delayed(Duration(milliseconds: delayMs), () {
      if (mounted && _scrollController.hasClients) {
        _scrollController.animateTo(
          _scrollController.position.maxScrollExtent,
          duration: const Duration(milliseconds: 250),
          curve: Curves.easeOut,
        );
      }
    });
  }

  void _sendQuery(String text, AiAssistantProvider provider) {
    if (text.trim().isEmpty) return;
    _inputController.clear();
    FocusScope.of(context).unfocus();
    provider.sendMessage(text);
    _scrollToBottom();
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    final mediaQuery = MediaQuery.of(context);
    final bottomInset = mediaQuery.viewInsets.bottom;
    final screenHeight = mediaQuery.size.height;

    final scooper = AiContextRegistry.activeScooper;
    final activeInstrument =
        scooper?.getInstrumentName() ?? 'Instruments Overview';
    final liveTelemetry = scooper?.getLiveTelemetry();

    return Consumer<AiAssistantProvider>(
      builder: (context, provider, child) {
        if (provider.isLoading ||
            provider.messages.length != _previousMessageCount) {
          _previousMessageCount = provider.messages.length;
          _scrollToBottom(50);
        }
        return Align(
          alignment: Alignment.bottomCenter,
          child: ConstrainedBox(
            constraints: const BoxConstraints(maxWidth: 640),
            child: Container(
              height: screenHeight * 0.85,
              margin: EdgeInsets.only(bottom: bottomInset),
              decoration: const BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.vertical(top: Radius.circular(16)),
              ),
              child: Column(
                children: [
                  Container(
                    padding: const EdgeInsets.symmetric(
                        horizontal: 14, vertical: 10),
                    decoration: BoxDecoration(
                      color: primaryRed,
                      borderRadius:
                          const BorderRadius.vertical(top: Radius.circular(16)),
                    ),
                    child: Row(
                      children: [
                        const Icon(Icons.auto_awesome,
                            color: Colors.white, size: 20),
                        const SizedBox(width: 8),
                        Expanded(
                          child: Text(
                            l10n?.aiAssistantTitle ?? 'AI Assistant',
                            style: const TextStyle(
                              color: Colors.white,
                              fontWeight: FontWeight.bold,
                              fontSize: 15,
                            ),
                          ),
                        ),
                        IconButton(
                          tooltip: l10n?.aiClearChatHistoryTooltip ??
                              'Clear Chat History',
                          icon: const Icon(Icons.delete_sweep_outlined,
                              color: Colors.white, size: 20),
                          onPressed: provider.messages.isEmpty
                              ? null
                              : () {
                                  provider.clearChat();
                                },
                        ),
                        IconButton(
                          tooltip: l10n?.close ?? 'Close',
                          icon: const Icon(Icons.close,
                              color: Colors.white, size: 20),
                          onPressed: () => Navigator.pop(context),
                        ),
                      ],
                    ),
                  ),
                  Padding(
                    padding: const EdgeInsets.fromLTRB(12, 8, 12, 4),
                    child: Stack(
                      children: [
                        Container(
                          width: double.infinity,
                          margin: const EdgeInsets.only(top: 6),
                          padding: const EdgeInsets.symmetric(
                              horizontal: 10, vertical: 8),
                          decoration: BoxDecoration(
                            border: Border.all(width: 1, color: primaryRed),
                            borderRadius: BorderRadius.circular(8),
                            color: Colors.white,
                          ),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Row(
                                children: [
                                  Expanded(
                                    child: Text(
                                      activeInstrument,
                                      style: const TextStyle(
                                        fontSize: 12,
                                        fontWeight: FontWeight.w600,
                                        color: Colors.black87,
                                      ),
                                    ),
                                  ),
                                  if (liveTelemetry != null &&
                                      liveTelemetry.isNotEmpty)
                                    InkWell(
                                      onTap: () {
                                        setState(() {
                                          _showTelemetryDetails =
                                              !_showTelemetryDetails;
                                        });
                                      },
                                      child: Padding(
                                        padding: const EdgeInsets.symmetric(
                                            horizontal: 4, vertical: 2),
                                        child: Row(
                                          children: [
                                            Text(
                                              _showTelemetryDetails
                                                  ? (l10n?.aiHide ?? 'Hide')
                                                  : (l10n?.aiInspect ??
                                                      'Inspect'),
                                              style: TextStyle(
                                                fontSize: 11,
                                                fontWeight: FontWeight.bold,
                                                color: primaryRed,
                                              ),
                                            ),
                                            Icon(
                                              _showTelemetryDetails
                                                  ? Icons.keyboard_arrow_up
                                                  : Icons.keyboard_arrow_down,
                                              size: 16,
                                              color: primaryRed,
                                            ),
                                          ],
                                        ),
                                      ),
                                    ),
                                ],
                              ),
                              if (_showTelemetryDetails &&
                                  liveTelemetry != null) ...[
                                const SizedBox(height: 6),
                                Container(
                                  width: double.infinity,
                                  padding: const EdgeInsets.all(8),
                                  decoration: BoxDecoration(
                                    color: Colors.grey.shade100,
                                    borderRadius: BorderRadius.circular(4),
                                    border:
                                        Border.all(color: Colors.grey.shade300),
                                  ),
                                  child: Text(
                                    liveTelemetry,
                                    style: const TextStyle(
                                      fontFamily: 'monospace',
                                      fontSize: 10.5,
                                      color: Colors.black87,
                                    ),
                                  ),
                                ),
                              ],
                            ],
                          ),
                        ),
                        Positioned(
                          left: 0,
                          right: 0,
                          top: 0,
                          child: Align(
                            alignment: Alignment.center,
                            child: Container(
                              padding:
                                  const EdgeInsets.symmetric(horizontal: 4),
                              decoration: BoxDecoration(
                                  color: oscilloscopeOptionTitleBoxColor),
                              child: Text(
                                l10n?.aiContext ?? 'CONTEXT',
                                style: TextStyle(
                                  color: oscilloscopeOptionTitleColor,
                                  fontWeight: FontWeight.bold,
                                  fontSize: 10,
                                ),
                              ),
                            ),
                          ),
                        ),
                      ],
                    ),
                  ),
                  Expanded(
                    child: provider.messages.isEmpty
                        ? _buildEmptyState(provider)
                        : ListView.builder(
                            controller: _scrollController,
                            padding: const EdgeInsets.symmetric(
                                horizontal: 12, vertical: 8),
                            itemCount: provider.messages.length,
                            itemBuilder: (context, index) {
                              final message = provider.messages[index];
                              return _buildMessageItem(message);
                            },
                          ),
                  ),
                  if (provider.isLoading) ...[
                    LinearProgressIndicator(
                      minHeight: 2,
                      color: primaryRed,
                      backgroundColor: Colors.grey.shade200,
                    ),
                    Padding(
                      padding: const EdgeInsets.symmetric(
                          vertical: 4, horizontal: 12),
                      child: Row(
                        children: [
                          SizedBox(
                            width: 10,
                            height: 10,
                            child: CircularProgressIndicator(
                              strokeWidth: 1.5,
                              valueColor:
                                  AlwaysStoppedAnimation<Color>(primaryRed),
                            ),
                          ),
                          const SizedBox(width: 8),
                          Expanded(
                            child: Text(
                              provider.messages.isNotEmpty &&
                                      provider.messages.last.role ==
                                          'assistant' &&
                                      provider.messages.last.content.isNotEmpty
                                  ? (l10n?.aiStreamingResponse ??
                                      'Streaming response...')
                                  : (l10n?.aiConnectingToModel(
                                          provider.selectedModel) ??
                                      'Connecting to ${provider.selectedModel}...'),
                              style: TextStyle(
                                  fontSize: 11, color: Colors.grey.shade600),
                            ),
                          ),
                        ],
                      ),
                    ),
                  ],
                  if (provider.messages.isEmpty)
                    SingleChildScrollView(
                      scrollDirection: Axis.horizontal,
                      padding: const EdgeInsets.symmetric(
                          horizontal: 12, vertical: 4),
                      child: Row(
                        children: _buildContextualChips(provider),
                      ),
                    ),
                  Container(
                    padding: const EdgeInsets.fromLTRB(12, 6, 8, 8),
                    decoration: BoxDecoration(
                      color: Colors.white,
                      border:
                          Border(top: BorderSide(color: Colors.grey.shade300)),
                    ),
                    child: Row(
                      children: [
                        Expanded(
                          child: TextField(
                            controller: _inputController,
                            minLines: 1,
                            maxLines: 4,
                            style: const TextStyle(fontSize: 13.5),
                            textInputAction: TextInputAction.send,
                            onSubmitted: (text) => _sendQuery(text, provider),
                            decoration: InputDecoration(
                              hintText: provider.isAuthenticated
                                  ? (l10n?.aiAskInstrumentHint ??
                                      'Ask about this instrument or experiment...')
                                  : (l10n?.aiConnectKeyHint ??
                                      'Connect OpenRouter key in Settings...'),
                              hintStyle: TextStyle(
                                  fontSize: 13, color: Colors.grey.shade500),
                              isDense: true,
                              contentPadding: const EdgeInsets.symmetric(
                                  horizontal: 12, vertical: 10),
                              border: OutlineInputBorder(
                                borderRadius: BorderRadius.circular(20),
                                borderSide:
                                    BorderSide(color: Colors.grey.shade300),
                              ),
                              focusedBorder: OutlineInputBorder(
                                borderRadius: BorderRadius.circular(20),
                                borderSide:
                                    BorderSide(color: primaryRed, width: 1.5),
                              ),
                            ),
                          ),
                        ),
                        const SizedBox(width: 6),
                        IconButton(
                          icon: Icon(Icons.send_rounded,
                              color: primaryRed, size: 22),
                          onPressed: provider.isLoading
                              ? null
                              : () =>
                                  _sendQuery(_inputController.text, provider),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ),
          ),
        );
      },
    );
  }

  Widget _buildEmptyState(AiAssistantProvider provider) {
    return const SizedBox.expand();
  }

  List<Widget> _buildContextualChips(AiAssistantProvider provider) {
    final scooper = AiContextRegistry.activeScooper;
    final instrumentName =
        scooper?.getInstrumentName() ?? 'Instruments Overview';

    final List<String> queries;
    if (instrumentName == 'Oscilloscope') {
      queries = [
        'Analyze Waveform',
        'Explain MIC Channel',
        'Check Trigger Settings',
        'Calculate Vpp & RMS',
      ];
    } else if (instrumentName == 'Multimeter') {
      queries = [
        'Measure Resistance (RES)',
        'Test Continuity',
        'Measure Capacitance (CAP)',
        'Measure Voltage Pins',
      ];
    } else if (instrumentName == 'Wave Generator') {
      queries = [
        'Setup 1kHz Sine on W1',
        'Square Wave PWM',
        'W1 to CH1 Loopback',
        'Wave Generator Guide',
      ];
    } else if (instrumentName == 'Power Source') {
      queries = [
        'Powering an LED Experiment',
        'PV1 vs PV3 Limits',
        'Constant Current PCS',
        'Diode Bias Setup',
      ];
    } else if (instrumentName == 'Logic Analyzer') {
      queries = [
        'Digital Capture Setup',
        'Decode I2C on LA1/LA2',
        'Trigger Modes',
        'UART Protocol Guide',
      ];
    } else if (instrumentName == 'Sound Meter') {
      queries = [
        'Check Current dB Level',
        'Explain Sound Events / Noise',
        'Acoustic Analysis Experiment',
        'Calibrate Microphone (dB SPL)',
      ];
    } else if (instrumentName == 'Lux Meter') {
      queries = [
        'Measure Light Intensity (Lux)',
        'BH1750 vs TSL2561 Guide',
        'Light vs Distance Experiment',
        'Check Sensor Calibration',
      ];
    } else if (instrumentName == 'Barometer') {
      queries = [
        'Atmospheric Pressure Reading',
        'Calculate Altitude from Pressure',
        'Pressure vs Altitude Experiment',
        'BMP180 I2C Pinout',
      ];
    } else if (instrumentName == 'Accelerometer') {
      queries = [
        'Check 3-Axis (X,Y,Z) G-Force',
        'Calibrate Tilt / Inclination',
        'MPU6050 Accelerometer Guide',
        'Vibration Analysis Experiment',
      ];
    } else if (instrumentName == 'Gyroscope') {
      queries = [
        'Measure Angular Velocity (rad/s)',
        'Detect Device Rotation',
        'MPU6050 Gyro Pinout',
        'Centripetal Acceleration Test',
      ];
    } else if (instrumentName == 'Compass') {
      queries = [
        'Heading & Magnetic Field',
        'HMC5883L Sensor Wiring',
        'Magnetic Declination Setup',
        'Earth Magnetic Field Experiment',
      ];
    } else if (instrumentName == 'Thermometer') {
      queries = [
        'Check Temperature (°C/°F)',
        'SHT21 Humidity & Temp Sensor',
        'Thermal Equilibrium Experiment',
        'Diode as Temp Sensor',
      ];
    } else if (instrumentName == 'Gas Sensor') {
      queries = [
        'Gas Concentration / MQ Sensor',
        'Preheat & Calibration Time',
        'PPM Calculation Formula',
        'Air Quality Monitoring',
      ];
    } else if (instrumentName == 'Robotic Arm') {
      queries = [
        'Servo PWM Control (50Hz)',
        'Set 4-Axis Positions',
        'Timeline Sequence Playback',
        'Robotic Arm Wiring to PSLab',
      ];
    } else if (instrumentName == 'OLED Display') {
      queries = [
        'SSD1306 / SH1106 I2C Pinout',
        'Display Text & Bitmaps',
        'Custom Graphics Buffer',
        'I2C Bus Addressing (0x3C)',
      ];
    } else if (instrumentName == 'Experiments') {
      queries = [
        'List All Available Experiments',
        'Acoustic Sound Analysis',
        'Ohm\'s Law & Diode IV Curve',
        'RC Circuit Transient Response',
      ];
    } else {
      queries = [
        'What instruments exist?',
        'What experiments can I perform?',
        'Sound Analysis Experiment',
        'How to Connect Board',
      ];
    }

    return queries.map((q) => _buildQueryChip(q, provider)).toList();
  }

  Widget _buildQueryChip(String query, AiAssistantProvider provider) {
    return Padding(
      padding: const EdgeInsets.only(right: 6),
      child: ActionChip(
        label: Text(query),
        labelStyle: TextStyle(
          fontSize: 11,
          color: primaryRed,
          fontWeight: FontWeight.w600,
        ),
        backgroundColor: Colors.red.shade50,
        side: BorderSide(color: primaryRed.withAlpha(102)),
        onPressed: provider.isLoading
            ? null
            : () {
                _sendQuery(query, provider);
              },
      ),
    );
  }

  Widget _buildMessageItem(ChatMessage message) {
    final l10n = AppLocalizations.of(context);
    final isUser = message.role == 'user';
    final isError = !isUser &&
        (message.content.startsWith('Error (') ||
            message.content.startsWith('Failed to communicate') ||
            message.content.contains('Diagnostic Error') ||
            message.content.contains('No OpenRouter API key'));

    return Align(
      alignment: isUser ? Alignment.centerRight : Alignment.centerLeft,
      child: Container(
        margin: const EdgeInsets.symmetric(vertical: 4),
        constraints: BoxConstraints(
          maxWidth: isUser
              ? MediaQuery.of(context).size.width * 0.82
              : MediaQuery.of(context).size.width * 0.90,
        ),
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
        decoration: BoxDecoration(
          color: isUser
              ? primaryRed
              : (isError ? const Color(0xFFFFF5F5) : const Color(0xFFF6F8FA)),
          border: isUser
              ? null
              : Border.all(
                  color:
                      isError ? Colors.red.shade300 : primaryRed.withAlpha(90),
                  width: 1,
                ),
          borderRadius: BorderRadius.only(
            topLeft: const Radius.circular(8),
            topRight: const Radius.circular(8),
            bottomLeft: Radius.circular(isUser ? 8 : 2),
            bottomRight: Radius.circular(isUser ? 2 : 8),
          ),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            if (isUser)
              Padding(
                padding: const EdgeInsets.only(bottom: 6),
                child: Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Text(
                      l10n?.aiYou ?? 'You',
                      style: const TextStyle(
                        fontSize: 11,
                        fontWeight: FontWeight.bold,
                        color: Colors.white,
                      ),
                    ),
                    const SizedBox(width: 8),
                    InkWell(
                      onTap: () {
                        Clipboard.setData(ClipboardData(text: message.content));
                        ScaffoldMessenger.of(context).showSnackBar(
                          SnackBar(
                            content: Text(
                              l10n?.aiMessageCopied ??
                                  'Message copied to clipboard.',
                              style: const TextStyle(color: Colors.white),
                            ),
                            backgroundColor: Colors.black87,
                            behavior: SnackBarBehavior.floating,
                            shape: RoundedRectangleBorder(
                                borderRadius: BorderRadius.circular(8)),
                            duration: const Duration(seconds: 2),
                          ),
                        );
                      },
                      child: const Padding(
                        padding:
                            EdgeInsets.symmetric(horizontal: 2, vertical: 2),
                        child:
                            Icon(Icons.copy, size: 13, color: Colors.white70),
                      ),
                    ),
                  ],
                ),
              ),
            if (isUser && message.attachedTelemetry != null)
              Container(
                margin: const EdgeInsets.only(bottom: 6),
                padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                decoration: BoxDecoration(
                  color: Colors.white24,
                  borderRadius: BorderRadius.circular(4),
                ),
                child: Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    const Icon(Icons.attachment,
                        color: Colors.white70, size: 12),
                    const SizedBox(width: 4),
                    Text(
                      l10n?.aiTelemetrySnapshotIncluded ??
                          'Telemetry Snapshot Included',
                      style:
                          const TextStyle(color: Colors.white70, fontSize: 10),
                    ),
                  ],
                ),
              ),
            if (!isUser)
              Padding(
                padding: const EdgeInsets.only(bottom: 6),
                child: Row(
                  children: [
                    Icon(
                      isError ? Icons.error_outline : Icons.auto_awesome,
                      size: 14,
                      color: isError ? Colors.red.shade700 : primaryRed,
                    ),
                    const SizedBox(width: 4),
                    Text(
                      isError
                          ? (l10n?.aiErrorLabel ?? 'Error')
                          : (l10n?.aiAssistantTitle ?? 'AI Assistant'),
                      style: TextStyle(
                        fontSize: 11,
                        fontWeight: FontWeight.bold,
                        color: isError ? Colors.red.shade700 : primaryRed,
                      ),
                    ),
                    const Spacer(),
                    InkWell(
                      onTap: () {
                        Clipboard.setData(ClipboardData(text: message.content));
                        ScaffoldMessenger.of(context).showSnackBar(
                          SnackBar(
                            content: Text(
                              l10n?.aiResponseCopied ??
                                  'Response copied to clipboard.',
                              style: const TextStyle(color: Colors.white),
                            ),
                            backgroundColor: Colors.black87,
                            behavior: SnackBarBehavior.floating,
                            shape: RoundedRectangleBorder(
                                borderRadius: BorderRadius.circular(8)),
                            duration: const Duration(seconds: 2),
                          ),
                        );
                      },
                      child: const Padding(
                        padding:
                            EdgeInsets.symmetric(horizontal: 4, vertical: 2),
                        child:
                            Icon(Icons.copy, size: 14, color: Colors.black45),
                      ),
                    ),
                  ],
                ),
              ),
            if (isUser)
              SelectableText(
                message.content,
                style: const TextStyle(
                  color: Colors.white,
                  fontSize: 13.5,
                  height: 1.4,
                ),
              )
            else if (isError)
              SelectableText(
                message.content,
                style: TextStyle(
                  color: Colors.red.shade900,
                  fontSize: 13,
                  height: 1.4,
                ),
              )
            else
              _buildAssistantMarkdown(message.content, context),
          ],
        ),
      ),
    );
  }

  Widget _buildAssistantMarkdown(String content, BuildContext context) {
    final theme = Theme.of(context);
    final markdownStyleSheet = MarkdownStyleSheet.fromTheme(theme).copyWith(
      p: const TextStyle(
        fontSize: 13.5,
        height: 1.45,
        color: Color(0xFF24292F),
      ),
      pPadding: const EdgeInsets.only(bottom: 6),
      h1: TextStyle(
        fontSize: 17,
        fontWeight: FontWeight.bold,
        color: primaryRed,
        height: 1.3,
      ),
      h1Padding: const EdgeInsets.only(top: 10, bottom: 4),
      h2: const TextStyle(
        fontSize: 15,
        fontWeight: FontWeight.bold,
        color: Color(0xFF1F2328),
        height: 1.3,
      ),
      h2Padding: const EdgeInsets.only(top: 8, bottom: 4),
      h3: const TextStyle(
        fontSize: 14,
        fontWeight: FontWeight.w600,
        color: Color(0xFF212529),
        height: 1.25,
      ),
      h3Padding: const EdgeInsets.only(top: 6, bottom: 2),
      h4: const TextStyle(
        fontSize: 13.5,
        fontWeight: FontWeight.w600,
        color: Color(0xFF212529),
      ),
      h4Padding: const EdgeInsets.only(top: 4, bottom: 2),
      strong: const TextStyle(
        fontWeight: FontWeight.bold,
        color: Color(0xFF1F2328),
      ),
      em: const TextStyle(
        fontStyle: FontStyle.italic,
      ),
      code: TextStyle(
        fontSize: 12,
        fontFamily: 'monospace',
        fontWeight: FontWeight.w600,
        color: primaryRed,
        backgroundColor: const Color(0xFFECEFF1),
      ),
      codeblockDecoration: BoxDecoration(
        color: const Color(0xFF1E2124),
        borderRadius: BorderRadius.circular(6),
        border: Border.all(color: const Color(0xFF333842), width: 1),
      ),
      codeblockPadding: const EdgeInsets.all(10),
      tableBorder: TableBorder.all(
        color: const Color(0xFFD0D7DE),
        width: 1,
      ),
      tableHead: const TextStyle(
        fontWeight: FontWeight.bold,
        fontSize: 11.5,
        color: Color(0xFF24292F),
      ),
      tableBody: const TextStyle(
        fontSize: 11,
        color: Color(0xFF24292F),
      ),
      tableCellsPadding: const EdgeInsets.symmetric(horizontal: 6, vertical: 5),
      blockquote: const TextStyle(
        fontSize: 13,
        color: Color(0xFF57606A),
        fontStyle: FontStyle.italic,
      ),
      blockquoteDecoration: BoxDecoration(
        color: const Color(0xFFF6F8FA),
        border: Border(left: BorderSide(color: primaryRed, width: 3.5)),
        borderRadius: BorderRadius.circular(4),
      ),
      blockquotePadding:
          const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
      listBullet: const TextStyle(
        fontSize: 13.5,
        color: Color(0xFF24292F),
      ),
      listBulletPadding: const EdgeInsets.only(right: 4),
      listIndent: 20,
      blockSpacing: 6,
      horizontalRuleDecoration: const BoxDecoration(
        border: Border(top: BorderSide(color: Color(0xFFE1E4E8), width: 1)),
      ),
      a: TextStyle(
        color: primaryRed,
        fontWeight: FontWeight.w600,
        decoration: TextDecoration.underline,
      ),
    );

    return MarkdownBody(
      data: content,
      selectable: true,
      styleSheet: markdownStyleSheet,
      syntaxHighlighter: _pslabSyntaxHighlighter,
      inlineSyntaxes: _mathSyntaxes,
      builders: _mathBuilders,
      onTapLink: (text, href, title) async {
        if (href != null) {
          final uri = Uri.tryParse(href);
          if (uri != null) {
            try {
              await launchUrl(uri, mode: LaunchMode.externalApplication);
            } catch (e) {
              debugPrint('Could not launch URL: $e');
            }
          }
        }
      },
    );
  }

  static final List<md.InlineSyntax> _mathSyntaxes = [
    LatexDisplayBracketsSyntax(),
    LatexDisplayDoubleDollarSyntax(),
    LatexInlineParenthesesSyntax(),
    LatexInlineDollarSyntax(),
  ];

  static final Map<String, MarkdownElementBuilder> _mathBuilders = {
    'latex_inline': LatexElementBuilder(),
    'latex_display': LatexElementBuilder(isDisplay: true),
  };
}

class LatexInlineParenthesesSyntax extends md.InlineSyntax {
  LatexInlineParenthesesSyntax() : super(r'\\\(([\s\S]*?)\\\)');

  @override
  bool onMatch(md.InlineParser parser, Match match) {
    final content = match.group(1);
    final el = md.Element('latex_inline', [md.Text(content ?? '')]);
    parser.addNode(el);
    return true;
  }
}

class LatexDisplayBracketsSyntax extends md.InlineSyntax {
  LatexDisplayBracketsSyntax() : super(r'\\\[([\s\S]*?)\\\]');

  @override
  bool onMatch(md.InlineParser parser, Match match) {
    final content = match.group(1);
    final el = md.Element('latex_display', [md.Text(content ?? '')]);
    parser.addNode(el);
    return true;
  }
}

class LatexDisplayDoubleDollarSyntax extends md.InlineSyntax {
  LatexDisplayDoubleDollarSyntax() : super(r'\$\$([\s\S]*?)\$\$');

  @override
  bool onMatch(md.InlineParser parser, Match match) {
    final content = match.group(1);
    final el = md.Element('latex_display', [md.Text(content ?? '')]);
    parser.addNode(el);
    return true;
  }
}

class LatexInlineDollarSyntax extends md.InlineSyntax {
  LatexInlineDollarSyntax() : super(r'(?<![\\\$])\$(?!\s)(.*?)(?<!\s)\$(?!\d)');

  @override
  bool onMatch(md.InlineParser parser, Match match) {
    final content = match.group(1);
    final el = md.Element('latex_inline', [md.Text(content ?? '')]);
    parser.addNode(el);
    return true;
  }
}

class LatexElementBuilder extends MarkdownElementBuilder {
  final bool isDisplay;
  LatexElementBuilder({this.isDisplay = false});

  @override
  Widget? visitElementAfter(md.Element element, TextStyle? preferredStyle) {
    final text = element.textContent.trim();
    if (text.isEmpty) return const SizedBox.shrink();

    final mathWidget = Math.tex(
      text,
      mathStyle: isDisplay ? MathStyle.display : MathStyle.text,
      textStyle: preferredStyle?.copyWith(
        color: const Color(0xFF1F2328),
      ),
      onErrorFallback: (err) => Text(
        text,
        style: preferredStyle?.copyWith(fontFamily: 'monospace'),
      ),
    );

    if (isDisplay) {
      return Container(
        width: double.infinity,
        alignment: Alignment.center,
        padding: const EdgeInsets.symmetric(vertical: 8),
        child: SingleChildScrollView(
          scrollDirection: Axis.horizontal,
          child: mathWidget,
        ),
      );
    }

    return mathWidget;
  }
}

class PslabSyntaxHighlighter extends SyntaxHighlighter {
  final TextStyle baseStyle;

  PslabSyntaxHighlighter({
    this.baseStyle = const TextStyle(
      fontFamily: 'monospace',
      fontSize: 12,
      color: Color(0xFFE6EDF3),
      height: 1.35,
    ),
  });

  static final RegExp _tokenRegex = RegExp(
    r'(?<comment>#[^\n]*|//[^\n]*)'
    r'|(?<string>"[^"\\]*(?:\\.[^"\\]*)*"|'
    r"'(?:[^'\\]*(?:\\.[^'\\]*)*)')"
    r'|(?<keyword>\b(?:def|class|import|from|return|while|for|if|else|elif|in|is|not|True|False|None|void|int|float|double|bool|const|final|var|function|async|await)\b)'
    r'|(?<number>\b\d+(?:\.\d+)?(?:mA|V|Hz|kHz|MHz|BPM|ms|s|%)?\b)'
    r'|(?<arrow>→|=>|->)'
    r'|(?<other>[^#/"\x27a-zA-Z0-9\s]+|\w+|\s+)',
    multiLine: true,
  );

  @override
  TextSpan format(String source) {
    final spans = <TextSpan>[];
    final matches = _tokenRegex.allMatches(source);

    for (final match in matches) {
      final text = match.group(0) ?? '';
      if (match.namedGroup('comment') != null) {
        spans.add(TextSpan(
          text: text,
          style: baseStyle.copyWith(
            color: const Color(0xFF8B949E),
            fontStyle: FontStyle.italic,
          ),
        ));
      } else if (match.namedGroup('string') != null) {
        spans.add(TextSpan(
          text: text,
          style: baseStyle.copyWith(color: const Color(0xFF7EE787)),
        ));
      } else if (match.namedGroup('keyword') != null) {
        spans.add(TextSpan(
          text: text,
          style: baseStyle.copyWith(
            color: const Color(0xFFFF7B72),
            fontWeight: FontWeight.w600,
          ),
        ));
      } else if (match.namedGroup('number') != null) {
        spans.add(TextSpan(
          text: text,
          style: baseStyle.copyWith(color: const Color(0xFF79C0FF)),
        ));
      } else if (match.namedGroup('arrow') != null) {
        spans.add(TextSpan(
          text: text,
          style: baseStyle.copyWith(
            color: const Color(0xFFFFA657),
            fontWeight: FontWeight.bold,
          ),
        ));
      } else {
        spans.add(TextSpan(
          text: text,
          style: baseStyle,
        ));
      }
    }

    if (spans.isEmpty) {
      return TextSpan(text: source, style: baseStyle);
    }

    return TextSpan(children: spans);
  }
}
