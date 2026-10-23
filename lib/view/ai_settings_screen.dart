import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:provider/provider.dart';
import '../l10n/app_localizations.dart';
import '../providers/ai_assistant_provider.dart';
import '../theme/colors.dart';
import 'widgets/navigation_drawer.dart';

class AiSettingsScreen extends StatefulWidget {
  const AiSettingsScreen({super.key});

  @override
  State<AiSettingsScreen> createState() => _AiSettingsScreenState();
}

class _AiSettingsScreenState extends State<AiSettingsScreen> {
  final TextEditingController _apiKeyController = TextEditingController();
  bool _obscureApiKey = true;
  bool _isTesting = false;

  @override
  void dispose() {
    _apiKeyController.dispose();
    super.dispose();
  }

  Widget _buildSectionCard({
    required String title,
    required Widget child,
  }) {
    return Stack(
      children: [
        Container(
          width: double.infinity,
          margin: const EdgeInsets.only(top: 8, bottom: 14),
          padding: const EdgeInsets.fromLTRB(14, 16, 14, 14),
          decoration: BoxDecoration(
            border: Border.all(width: 1, color: primaryRed),
            borderRadius: BorderRadius.circular(10),
            color: Colors.white,
          ),
          child: child,
        ),
        Positioned(
          left: 0,
          right: 0,
          top: 1,
          child: Align(
            alignment: Alignment.center,
            child: Container(
              padding: const EdgeInsets.symmetric(horizontal: 6),
              decoration: BoxDecoration(color: oscilloscopeOptionTitleBoxColor),
              child: Text(
                title,
                style: TextStyle(
                  color: oscilloscopeOptionTitleColor,
                  fontWeight: FontWeight.bold,
                  fontSize: 12,
                ),
              ),
            ),
          ),
        ),
      ],
    );
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;

    return Scaffold(
      backgroundColor: Colors.white,
      appBar: AppBar(
        iconTheme: IconThemeData(color: appBarContentColor),
        systemOverlayStyle: SystemUiOverlayStyle(
          statusBarColor: appBarColor,
          statusBarIconBrightness: Brightness.light,
          statusBarBrightness: Brightness.dark,
        ),
        backgroundColor: primaryRed,
        title: Text(
          l10n.aiAssistantTitle,
          style: TextStyle(
            color: appBarContentColor,
            fontSize: 16,
            fontWeight: FontWeight.bold,
          ),
        ),
      ),
      drawer: const NavDrawer(selectedIndex: 14),
      body: SafeArea(
        child: Consumer<AiAssistantProvider>(
          builder: (context, provider, child) {
            return SingleChildScrollView(
              padding:
                  const EdgeInsets.symmetric(horizontal: 14.0, vertical: 10.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _buildSectionCard(
                    title: l10n.aiAuthStatusTitle,
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          children: [
                            Icon(
                              provider.isAuthenticated
                                  ? Icons.check_circle_outline
                                  : Icons.radio_button_unchecked,
                              color: provider.isAuthenticated
                                  ? Colors.green.shade700
                                  : Colors.grey.shade600,
                              size: 20,
                            ),
                            const SizedBox(width: 8),
                            Text(
                              provider.isAuthenticated
                                  ? l10n.aiConnectedOpenRouter
                                  : l10n.aiNotConnected,
                              style: TextStyle(
                                fontSize: 14,
                                fontWeight: FontWeight.bold,
                                color: provider.isAuthenticated
                                    ? Colors.green.shade800
                                    : Colors.black87,
                              ),
                            ),
                          ],
                        ),
                        if (provider.isAuthenticated &&
                            provider.maskedApiKey != null) ...[
                          const SizedBox(height: 8),
                          Text(
                            l10n.aiActiveKey(provider.maskedApiKey!),
                            style: const TextStyle(
                              fontFamily: 'monospace',
                              fontSize: 12,
                              color: Colors.black54,
                            ),
                          ),
                        ],
                        if (provider.statusMessage != null) ...[
                          const SizedBox(height: 6),
                          Text(
                            provider.statusMessage!,
                            style: TextStyle(
                              fontSize: 12,
                              color: provider.statusMessage!
                                          .toLowerCase()
                                          .contains('fail') ||
                                      provider.statusMessage!
                                          .toLowerCase()
                                          .contains('error')
                                  ? primaryRed
                                  : Colors.black54,
                            ),
                          ),
                        ],
                        if (provider.isAuthenticated) ...[
                          const SizedBox(height: 12),
                          Wrap(
                            spacing: 8,
                            runSpacing: 8,
                            children: [
                              ElevatedButton.icon(
                                onPressed: (_isTesting || provider.isLoading)
                                    ? null
                                    : () async {
                                        setState(() {
                                          _isTesting = true;
                                        });
                                        final res =
                                            await provider.testConnection();
                                        if (!context.mounted) return;
                                        setState(() {
                                          _isTesting = false;
                                        });
                                        ScaffoldMessenger.of(context)
                                            .showSnackBar(
                                          SnackBar(
                                            content: Text(
                                              res['message'] as String,
                                              style: const TextStyle(
                                                color: Colors.white,
                                                fontWeight: FontWeight.w600,
                                              ),
                                            ),
                                            backgroundColor: Colors.black,
                                            behavior: SnackBarBehavior.floating,
                                            shape: RoundedRectangleBorder(
                                              borderRadius:
                                                  BorderRadius.circular(8),
                                            ),
                                            duration:
                                                const Duration(seconds: 4),
                                          ),
                                        );
                                      },
                                style: ElevatedButton.styleFrom(
                                  backgroundColor: primaryRed,
                                  foregroundColor: Colors.white,
                                  shape: RoundedRectangleBorder(
                                    borderRadius: BorderRadius.circular(6),
                                  ),
                                ),
                                icon: _isTesting
                                    ? const SizedBox(
                                        width: 14,
                                        height: 14,
                                        child: CircularProgressIndicator(
                                          strokeWidth: 2,
                                          valueColor:
                                              AlwaysStoppedAnimation<Color>(
                                                  Colors.white),
                                        ),
                                      )
                                    : const Icon(Icons.speed, size: 16),
                                label: Text(_isTesting
                                    ? l10n.aiTesting
                                    : l10n.aiTestConnection),
                              ),
                              OutlinedButton.icon(
                                onPressed: provider.isLoading
                                    ? null
                                    : () => provider.signOut(),
                                style: OutlinedButton.styleFrom(
                                  side: BorderSide(color: primaryRed),
                                  foregroundColor: primaryRed,
                                  shape: RoundedRectangleBorder(
                                    borderRadius: BorderRadius.circular(6),
                                  ),
                                ),
                                icon: const Icon(Icons.logout, size: 16),
                                label: Text(l10n.aiDisconnectOpenRouter),
                              ),
                            ],
                          ),
                        ],
                      ],
                    ),
                  ),
                  if (!provider.isAuthenticated)
                    _buildSectionCard(
                      title: l10n.aiOpenRouterSignInTitle,
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            l10n.aiOpenRouterSignInDesc,
                            style: const TextStyle(
                                fontSize: 13,
                                color: Colors.black87,
                                height: 1.3),
                          ),
                          const SizedBox(height: 12),
                          if (provider.isLoading)
                            Row(
                              children: [
                                Expanded(
                                  child: ElevatedButton.icon(
                                    onPressed: null,
                                    style: ElevatedButton.styleFrom(
                                      backgroundColor: primaryRed,
                                      foregroundColor: Colors.white,
                                      elevation: 0,
                                      padding: const EdgeInsets.symmetric(
                                          vertical: 12),
                                      shape: RoundedRectangleBorder(
                                        borderRadius: BorderRadius.circular(6),
                                      ),
                                    ),
                                    icon: const SizedBox(
                                      width: 18,
                                      height: 18,
                                      child: CircularProgressIndicator(
                                        strokeWidth: 2,
                                        valueColor:
                                            AlwaysStoppedAnimation<Color>(
                                                Colors.white),
                                      ),
                                    ),
                                    label: Text(
                                      l10n.aiAuthenticating,
                                      style: const TextStyle(
                                          fontWeight: FontWeight.bold),
                                    ),
                                  ),
                                ),
                                const SizedBox(width: 8),
                                OutlinedButton(
                                  onPressed: () => provider.cancelLogin(),
                                  style: OutlinedButton.styleFrom(
                                    side: BorderSide(color: primaryRed),
                                    foregroundColor: primaryRed,
                                    padding: const EdgeInsets.symmetric(
                                        vertical: 12, horizontal: 16),
                                    shape: RoundedRectangleBorder(
                                      borderRadius: BorderRadius.circular(6),
                                    ),
                                  ),
                                  child: Text(l10n.cancel),
                                ),
                              ],
                            )
                          else
                            SizedBox(
                              width: double.infinity,
                              child: ElevatedButton.icon(
                                onPressed: () async {
                                  await provider.startLogin();
                                },
                                style: ElevatedButton.styleFrom(
                                  backgroundColor: primaryRed,
                                  foregroundColor: Colors.white,
                                  elevation: 0,
                                  padding:
                                      const EdgeInsets.symmetric(vertical: 12),
                                  shape: RoundedRectangleBorder(
                                    borderRadius: BorderRadius.circular(6),
                                  ),
                                ),
                                icon:
                                    const Icon(Icons.open_in_browser, size: 18),
                                label: Text(
                                  l10n.aiSignInOpenRouter,
                                  style: const TextStyle(
                                      fontWeight: FontWeight.bold),
                                ),
                              ),
                            ),
                        ],
                      ),
                    ),
                  _buildSectionCard(
                    title: l10n.aiManualKeyTitle,
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          l10n.aiManualKeyDesc,
                          style: const TextStyle(
                              fontSize: 13, color: Colors.black87, height: 1.3),
                        ),
                        const SizedBox(height: 10),
                        TextField(
                          controller: _apiKeyController,
                          obscureText: _obscureApiKey,
                          decoration: InputDecoration(
                            isDense: true,
                            hintText: l10n.aiManualKeyHint,
                            hintStyle: const TextStyle(
                                fontSize: 13, color: Colors.black38),
                            border: OutlineInputBorder(
                              borderSide:
                                  BorderSide(color: Colors.grey.shade400),
                              borderRadius: BorderRadius.circular(6),
                            ),
                            focusedBorder: OutlineInputBorder(
                              borderSide:
                                  BorderSide(color: primaryRed, width: 1.5),
                              borderRadius: BorderRadius.circular(6),
                            ),
                            suffixIcon: IconButton(
                              icon: Icon(
                                _obscureApiKey
                                    ? Icons.visibility_off
                                    : Icons.visibility,
                                size: 18,
                                color: Colors.black54,
                              ),
                              onPressed: () {
                                setState(() {
                                  _obscureApiKey = !_obscureApiKey;
                                });
                              },
                            ),
                          ),
                        ),
                        const SizedBox(height: 10),
                        Align(
                          alignment: Alignment.centerRight,
                          child: ElevatedButton(
                            onPressed: provider.isLoading
                                ? null
                                : () async {
                                    final text = _apiKeyController.text.trim();
                                    if (text.isEmpty) {
                                      ScaffoldMessenger.of(context)
                                          .showSnackBar(
                                        SnackBar(
                                          content: Text(
                                            l10n.aiEnterApiKey,
                                            style: const TextStyle(
                                              color: Colors.white,
                                              fontWeight: FontWeight.w600,
                                            ),
                                          ),
                                          backgroundColor: Colors.black,
                                          behavior: SnackBarBehavior.floating,
                                          shape: RoundedRectangleBorder(
                                            borderRadius:
                                                BorderRadius.circular(8),
                                          ),
                                        ),
                                      );
                                      return;
                                    }
                                    final success =
                                        await provider.saveManualApiKey(text);
                                    if (success) {
                                      _apiKeyController.clear();
                                      if (context.mounted) {
                                        ScaffoldMessenger.of(context)
                                            .showSnackBar(
                                          SnackBar(
                                            content: Text(
                                              l10n.aiApiKeySaved,
                                              style: const TextStyle(
                                                color: Colors.white,
                                                fontWeight: FontWeight.w600,
                                              ),
                                            ),
                                            backgroundColor: Colors.black,
                                            behavior: SnackBarBehavior.floating,
                                            shape: RoundedRectangleBorder(
                                              borderRadius:
                                                  BorderRadius.circular(8),
                                            ),
                                          ),
                                        );
                                      }
                                    }
                                  },
                            style: ElevatedButton.styleFrom(
                              backgroundColor: primaryRed,
                              foregroundColor: Colors.white,
                              elevation: 0,
                              shape: RoundedRectangleBorder(
                                borderRadius: BorderRadius.circular(6),
                              ),
                            ),
                            child: Text(l10n.aiSaveApiKey),
                          ),
                        ),
                      ],
                    ),
                  ),
                  _buildSectionCard(
                    title: l10n.aiInferenceModelTitle,
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          l10n.aiInferenceModelDesc,
                          style: const TextStyle(
                              fontSize: 13, color: Colors.black87, height: 1.3),
                        ),
                        const SizedBox(height: 10),
                        Container(
                          padding: const EdgeInsets.symmetric(horizontal: 10),
                          decoration: BoxDecoration(
                            border: Border.all(color: primaryRed),
                            borderRadius: BorderRadius.circular(6),
                          ),
                          child: DropdownButtonHideUnderline(
                            child: DropdownButton<String>(
                              value: provider.selectedModel,
                              isExpanded: true,
                              icon: Icon(Icons.arrow_drop_down,
                                  color: primaryRed),
                              style: const TextStyle(
                                fontSize: 13,
                                color: Colors.black87,
                                fontWeight: FontWeight.w600,
                              ),
                              items: AiAssistantProvider.availableModels
                                  .map((model) {
                                final isFree = model.contains(':free') ||
                                    model == 'openrouter/free';
                                String label = model;
                                if (model == 'openrouter/free') {
                                  label = l10n.aiModelAutoFree;
                                } else if (model ==
                                    'nvidia/nemotron-3.5-lightning:free') {
                                  label = 'NVIDIA Nemotron 3.5 Lightning';
                                } else if (model ==
                                    'minimax/minimax-m3.1-flash:free') {
                                  label = 'MiniMax M3.1 Flash (1M Context)';
                                } else if (model ==
                                    'cohere/north-mini-code:free') {
                                  label = 'Cohere North Mini Code';
                                } else if (model ==
                                    'google/gemma-4-31b-it:free') {
                                  label = 'Google Gemma 4 31B';
                                } else if (model ==
                                    'google/gemma-4-26b-a4b-it:free') {
                                  label = 'Google Gemma 4 26B';
                                }
                                return DropdownMenuItem<String>(
                                  value: model,
                                  child: Row(
                                    children: [
                                      Expanded(
                                        child: Text(
                                          label,
                                          overflow: TextOverflow.ellipsis,
                                        ),
                                      ),
                                      Container(
                                        padding: const EdgeInsets.symmetric(
                                            horizontal: 6, vertical: 2),
                                        margin: const EdgeInsets.only(left: 6),
                                        decoration: BoxDecoration(
                                          color: isFree
                                              ? Colors.green.shade100
                                              : Colors.blueGrey.shade100,
                                          borderRadius:
                                              BorderRadius.circular(4),
                                        ),
                                        child: Text(
                                          isFree
                                              ? l10n.aiBadgeFree
                                              : l10n.aiBadgePaid,
                                          style: TextStyle(
                                            fontSize: 10,
                                            fontWeight: FontWeight.bold,
                                            color: isFree
                                                ? Colors.green.shade800
                                                : Colors.blueGrey.shade800,
                                          ),
                                        ),
                                      ),
                                    ],
                                  ),
                                );
                              }).toList(),
                              onChanged: (newModel) {
                                if (newModel != null) {
                                  provider.setSelectedModel(newModel);
                                }
                              },
                            ),
                          ),
                        ),
                      ],
                    ),
                  ),
                  _buildSectionCard(
                    title: l10n.aiOverlayControlsTitle,
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        SwitchListTile.adaptive(
                          contentPadding: EdgeInsets.zero,
                          activeThumbColor: primaryRed,
                          activeTrackColor: primaryRed.withAlpha(80),
                          title: Text(
                            l10n.aiFloatingAssistantTitle,
                            style: const TextStyle(
                                fontSize: 14, fontWeight: FontWeight.bold),
                          ),
                          subtitle: Text(
                            l10n.aiFloatingAssistantDesc,
                            style: const TextStyle(
                                fontSize: 12, color: Colors.black54),
                          ),
                          value: provider.isFloatingEnabled,
                          onChanged: (val) {
                            provider.setFloatingEnabled(val);
                          },
                        ),
                        const Divider(),
                        Row(
                          children: [
                            Icon(Icons.science_outlined,
                                size: 18, color: primaryRed),
                            const SizedBox(width: 8),
                            Expanded(
                              child: Text(
                                l10n.aiAutoAttachContextDesc,
                                style: const TextStyle(
                                    fontSize: 12, color: Colors.black87),
                              ),
                            ),
                          ],
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            );
          },
        ),
      ),
    );
  }
}
