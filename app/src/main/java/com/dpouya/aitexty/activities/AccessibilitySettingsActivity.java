package com.dpouya.aitexty.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.dpouya.aitexty.accessibility.AccessibilitySettings;
import com.dpouya.aitexty.accessibility.PersianTtsEngine;
import com.dpouya.aitexty.accessibility.PersianTtsModelStore;
import com.dpouya.aitexty.accessibility.PersianTtsVoice;
import com.dpouya.aitexty.accessibility.SpeechHelper;
import com.dpouya.aitexty.components.BaseActivity;
import com.dpouya.aitexty.helper.AndroidUtilities;
import com.dpouya.aitexty.helper.LayoutHelper;
import com.dpouya.aitexty.helper.LocaleController;
import com.dpouya.aitexty.helper.Theme;
import com.dpouya.aitexty.ui.ActionBar.ActionBar;

import java.util.List;

public class AccessibilitySettingsActivity extends BaseActivity {
    private Button speakToggle;
    private Button sttToggle;
    private Button voiceControlToggle;
    private TextView rateLabel;
    private SeekBar rateBar;
    private TextView voiceLabel;
    private TextView persianStatusLabel;
    private Button installTtsBtn;

    @Override
    protected View initViews(Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(AndroidUtilities.dp20, AndroidUtilities.dp20, AndroidUtilities.dp20, AndroidUtilities.dp20);
        layout.setBackgroundColor(Theme.getColor(Theme.BACKGROUND_COLOR));

        speakToggle = createToggle(context, LocaleController.getString("speak_button"),
                AccessibilitySettings.isSpeakButtonEnabled(),
                v -> AccessibilitySettings.setSpeakButtonEnabled(
                        !AccessibilitySettings.isSpeakButtonEnabled()));
        layout.addView(speakToggle, rowParams());

        sttToggle = createToggle(context, LocaleController.getString("speech_to_text"),
                AccessibilitySettings.isSttComposeEnabled(),
                v -> AccessibilitySettings.setSttComposeEnabled(
                        !AccessibilitySettings.isSttComposeEnabled()));
        layout.addView(sttToggle, rowParams());

        voiceControlToggle = createToggle(context, LocaleController.getString("voice_control"),
                AccessibilitySettings.isVoiceControlEnabled(),
                v -> AccessibilitySettings.setVoiceControlEnabled(
                        !AccessibilitySettings.isVoiceControlEnabled()));
        layout.addView(voiceControlToggle, rowParams());

        rateLabel = new TextView(context);
        rateLabel.setTextColor(Theme.getColor(Theme.TEXT_COLOR));
        updateRateLabel();
        layout.addView(rateLabel, rowParams());

        rateBar = new SeekBar(context);
        rateBar.setMax(150);
        rateBar.setProgress(AccessibilitySettings.getSpeechRatePercent() - 50);
        rateBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                AccessibilitySettings.setSpeechRatePercent(progress + 50);
                updateRateLabel();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SpeechHelper helper = SpeechHelper.getInstance(AccessibilitySettingsActivity.this);
                helper.applySettings();
                helper.speak(LocaleController.getString("test_persian_voice"));
            }
        });
        layout.addView(rateBar, rowParams());

        voiceLabel = new TextView(context);
        voiceLabel.setTextColor(Theme.getColor(Theme.SUB_TEXT_COLOR));
        voiceLabel.setTextSize(14);
        updateVoiceLabel();
        layout.addView(voiceLabel, rowParams());

        persianStatusLabel = new TextView(context);
        persianStatusLabel.setTextColor(Theme.getColor(Theme.SUB_TEXT_COLOR));
        persianStatusLabel.setTextSize(14);
        layout.addView(persianStatusLabel, rowParams());

        installTtsBtn = new Button(context);
        installTtsBtn.setText(LocaleController.getString("install_persian_tts"));
        installTtsBtn.setOnClickListener(v ->
                SpeechHelper.getInstance(this).openInstallTtsData());
        layout.addView(installTtsBtn, rowParams());
        refreshPersianVoiceStatus();

        Button chooseVoice = new Button(context);
        chooseVoice.setText(LocaleController.getString("choose_voice"));
        chooseVoice.setOnClickListener(v -> showVoicePicker());
        layout.addView(chooseVoice, rowParams());

        TextView help = new TextView(context);
        help.setTextColor(Theme.getColor(Theme.SUB_TEXT_COLOR));
        help.setTextSize(13);
        help.setText(LocaleController.getString("voice_commands_help"));
        layout.addView(help, rowParams());

        return layout;
    }

    private LinearLayout.LayoutParams rowParams() {
        return LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 12);
    }

    private Button createToggle(Context context, String title, boolean enabled, View.OnClickListener listener) {
        Button button = new Button(context);
        button.setAllCaps(false);
        button.setText(title + ": " + LocaleController.getString(enabled ? "enabled" : "disabled"));
        button.setOnClickListener(v -> {
            listener.onClick(v);
            refreshToggleLabels();
        });
        return button;
    }

    private void refreshToggleLabels() {
        speakToggle.setText(LocaleController.getString("speak_button") + ": "
                + LocaleController.getString(AccessibilitySettings.isSpeakButtonEnabled() ? "enabled" : "disabled"));
        sttToggle.setText(LocaleController.getString("speech_to_text") + ": "
                + LocaleController.getString(AccessibilitySettings.isSttComposeEnabled() ? "enabled" : "disabled"));
        voiceControlToggle.setText(LocaleController.getString("voice_control") + ": "
                + LocaleController.getString(AccessibilitySettings.isVoiceControlEnabled() ? "enabled" : "disabled"));
    }

    private void updateRateLabel() {
        float rate = AccessibilitySettings.getSpeechRate();
        rateLabel.setText(LocaleController.getString("speech_rate") + ": "
                + String.format(java.util.Locale.getDefault(), "%.1fx", rate));
    }

    private void updateVoiceLabel() {
        String voice = AccessibilitySettings.getVoiceName();
        if (voice == null || voice.isEmpty()) {
            voiceLabel.setText(LocaleController.getString("choose_voice") + ": "
                    + LocaleController.getString("tts_auto_detect"));
        } else if (voice.startsWith("persian:")) {
            voiceLabel.setText(LocaleController.getString("choose_voice") + ": "
                    + PersianTtsVoice.fromSettingValue(voice).getLabel());
        } else if ("builtin_persian".equals(voice)) {
            voiceLabel.setText(LocaleController.getString("choose_voice") + ": "
                    + LocaleController.getString("persian_voice_gyro"));
        } else {
            voiceLabel.setText(LocaleController.getString("choose_voice") + ": " + voice);
        }
    }

    private void refreshPersianVoiceStatus() {
        if (persianStatusLabel == null) {
            return;
        }
        PersianTtsEngine engine = PersianTtsEngine.getInstance(this);
        PersianTtsVoice active = PersianTtsVoice.fromSettingValue(AccessibilitySettings.getVoiceName());
        if (engine.isReady()) {
            persianStatusLabel.setText(LocaleController.getString("persian_voice_status") + ": "
                    + engine.getActiveVoice().getLabel());
            if (installTtsBtn != null) {
                installTtsBtn.setVisibility(View.GONE);
            }
            return;
        }
        if (engine.isVoiceReady(active)) {
            persianStatusLabel.setText(LocaleController.getString("persian_voice_status") + ": "
                    + LocaleController.getString("persian_voice_ready"));
            if (installTtsBtn != null) {
                installTtsBtn.setVisibility(View.GONE);
            }
            return;
        }
        if (PersianTtsModelStore.needsDownload(active)) {
            persianStatusLabel.setText(LocaleController.getString("persian_voice_status") + ": "
                    + LocaleController.getString("persian_voice_missing"));
            if (installTtsBtn != null) {
                installTtsBtn.setVisibility(View.VISIBLE);
            }
            return;
        }
        persianStatusLabel.setText(LocaleController.getString("persian_voice_status") + ": "
                + LocaleController.getString("persian_voice_loading"));
        if (installTtsBtn != null) {
            installTtsBtn.setVisibility(View.GONE);
        }
    }

    private void showVoicePicker() {
        SpeechHelper helper = SpeechHelper.getInstance(this);
        helper.whenReady(success -> {
            if (!success) {
                return;
            }
            List<SpeechHelper.VoiceOption> voices = helper.getAvailableVoices();
            String[] labels = new String[voices.size()];
            for (int i = 0; i < voices.size(); i++) {
                labels[i] = voices.get(i).label;
            }
            new AlertDialog.Builder(this)
                    .setTitle(LocaleController.getString("choose_voice"))
                    .setItems(labels, (d, which) -> {
                        SpeechHelper.VoiceOption selected = voices.get(which);
                        AccessibilitySettings.setVoiceName(selected.name);
                        if (selected.name != null && selected.name.startsWith("persian:")) {
                            PersianTtsEngine.getInstance(AccessibilitySettingsActivity.this)
                                    .setVoice(PersianTtsVoice.fromSettingValue(selected.name));
                        }
                        helper.applySettings();
                        updateVoiceLabel();
                        refreshPersianVoiceStatus();
                        helper.speak(LocaleController.getString("test_persian_voice"));
                    })
                    .show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        PersianTtsEngine.getInstance(this).setProgressActivity(this);
        refreshPersianVoiceStatus();
    }

    @Override
    protected void onPause() {
        PersianTtsEngine.getInstance(this).setProgressActivity(null);
        super.onPause();
    }

    @Override
    protected void initActionbar(ActionBar bar) {
        bar.showBackButton(true);
        bar.setOnIconClick(v -> finish());
        bar.setTitle(LocaleController.getString("accessibility"));
    }
}
