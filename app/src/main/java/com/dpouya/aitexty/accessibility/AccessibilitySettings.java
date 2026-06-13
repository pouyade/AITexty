package com.dpouya.aitexty.accessibility;

import com.dpouya.aitexty.NotificationCenter;
import com.dpouya.aitexty.helper.AppSettings;

public final class AccessibilitySettings {
    private AccessibilitySettings() {
    }

    public static boolean isSpeakButtonEnabled() {
        return AppSettings.Bool(AppSettings.Key.TTS_SPEAK_BUTTON_ENABLED);
    }

    public static void setSpeakButtonEnabled(boolean enabled) {
        AppSettings.Bool(AppSettings.Key.TTS_SPEAK_BUTTON_ENABLED, enabled);
        notifyChanged();
    }

    public static boolean isSttComposeEnabled() {
        return AppSettings.Bool(AppSettings.Key.STT_COMPOSE_ENABLED);
    }

    public static void setSttComposeEnabled(boolean enabled) {
        AppSettings.Bool(AppSettings.Key.STT_COMPOSE_ENABLED, enabled);
        notifyChanged();
    }

    public static boolean isVoiceControlEnabled() {
        return AppSettings.Bool(AppSettings.Key.VOICE_CONTROL_ENABLED);
    }

    public static void setVoiceControlEnabled(boolean enabled) {
        AppSettings.Bool(AppSettings.Key.VOICE_CONTROL_ENABLED, enabled);
        notifyChanged();
    }

    public static float getSpeechRate() {
        return AppSettings.Int(AppSettings.Key.TTS_SPEECH_RATE) / 100f;
    }

    public static void setSpeechRatePercent(int percent) {
        AppSettings.Int(AppSettings.Key.TTS_SPEECH_RATE, percent);
        notifyChanged();
    }

    public static int getSpeechRatePercent() {
        return AppSettings.Int(AppSettings.Key.TTS_SPEECH_RATE);
    }

    public static String getVoiceName() {
        String voice = AppSettings.String(AppSettings.Key.TTS_VOICE_NAME);
        return voice != null ? voice : "";
    }

    public static void setVoiceName(String voiceName) {
        AppSettings.String(AppSettings.Key.TTS_VOICE_NAME, voiceName != null ? voiceName : "");
        notifyChanged();
    }

    public static void notifyChanged() {
        NotificationCenter.getInstance().postNotification(NotificationCenter.didAccessibilitySettingsChanged);
    }
}
