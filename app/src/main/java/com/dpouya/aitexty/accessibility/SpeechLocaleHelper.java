package com.dpouya.aitexty.accessibility;

import android.os.Build;
import android.speech.tts.Voice;

import com.dpouya.aitexty.helper.LocaleController;

import java.util.Locale;

public final class SpeechLocaleHelper {
    private SpeechLocaleHelper() {
    }

    public static boolean isPersianAppLanguage() {
        LocaleController.Language lang = LocaleController.getDefaultLanguage();
        return lang != null && "fa".equalsIgnoreCase(lang.langCode);
    }

    public static Locale getAppSpeechLocale() {
        LocaleController.Language lang = LocaleController.getDefaultLanguage();
        if (lang != null && "fa".equalsIgnoreCase(lang.langCode)) {
            return Locale.forLanguageTag("fa-IR");
        }
        if (lang != null && lang.langCode != null && !lang.langCode.isEmpty()) {
            if (lang.langCode.contains("-")) {
                return Locale.forLanguageTag(lang.langCode);
            }
            return new Locale(lang.langCode);
        }
        return Locale.getDefault();
    }

    public static String getRecognitionLanguageTag() {
        if (isPersianAppLanguage()) {
            return "fa-IR";
        }
        Locale locale = getAppSpeechLocale();
        if (locale.getCountry() != null && !locale.getCountry().isEmpty()) {
            return locale.getLanguage() + "-" + locale.getCountry();
        }
        return locale.getLanguage();
    }

    public static boolean isPersianLocale(Locale locale) {
        if (locale == null) {
            return false;
        }
        String language = locale.getLanguage().toLowerCase(Locale.US);
        return "fa".equals(language) || "pes".equals(language) || "fas".equals(language);
    }

    public static boolean isPersianVoice(Voice voice) {
        if (voice == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return false;
        }
        if (isPersianLocale(voice.getLocale())) {
            return true;
        }
        return voiceNameMatchesLocale(voice.getName(), Locale.forLanguageTag("fa-IR"));
    }

    public static boolean voiceMatchesLocale(Voice voice, Locale target) {
        if (voice == null || target == null) {
            return false;
        }
        if (voice.getLocale() != null) {
            Locale voiceLocale = voice.getLocale();
            if (target.getLanguage().equalsIgnoreCase(voiceLocale.getLanguage())) {
                if (target.getCountry() == null || target.getCountry().isEmpty()) {
                    return true;
                }
                if (voiceLocale.getCountry() == null || voiceLocale.getCountry().isEmpty()) {
                    return true;
                }
                return target.getCountry().equalsIgnoreCase(voiceLocale.getCountry());
            }
            if (isPersianLocale(target) && isPersianLocale(voiceLocale)) {
                return true;
            }
        }
        return voiceNameMatchesLocale(voice.getName(), target);
    }

    public static boolean voiceNameMatchesLocale(String voiceName, Locale target) {
        if (voiceName == null || target == null) {
            return false;
        }
        String lower = voiceName.toLowerCase(Locale.US);
        String language = target.getLanguage().toLowerCase(Locale.US);

        if (isPersianLocale(target)) {
            return lower.contains("fa-ir")
                    || lower.contains("fa_ir")
                    || lower.contains("/fa")
                    || lower.contains(":fa-")
                    || lower.contains("-fa-")
                    || lower.contains("_fa_")
                    || lower.contains("fars")
                    || lower.contains("persian")
                    || lower.contains("pes-")
                    || lower.contains("pes_");
        }

        if ("en".equals(language)) {
            return lower.contains("en-us")
                    || lower.contains("en_us")
                    || lower.contains(":en-")
                    || lower.contains("-en-");
        }

        return lower.contains(language + "-")
                || lower.contains(language + "_")
                || lower.contains(":" + language + "-");
    }

    public static Locale getDisplayLocale() {
        return isPersianAppLanguage() ? Locale.forLanguageTag("fa-IR") : Locale.getDefault();
    }
}
