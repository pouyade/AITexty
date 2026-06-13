package com.dpouya.aitexty.accessibility;

import android.text.TextUtils;

import java.util.Locale;

public final class TextLanguageDetector {
    private TextLanguageDetector() {
    }

    public static Locale detect(String text) {
        if (TextUtils.isEmpty(text)) {
            return fallbackLocale();
        }

        int persian = 0;
        int latin = 0;
        int counted = 0;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (shouldSkip(c)) {
                continue;
            }
            counted++;
            if (isPersianArabicScript(c)) {
                persian++;
            } else if (isLatinLetter(c)) {
                latin++;
            }
        }

        if (counted == 0) {
            return fallbackLocale();
        }

        if (persian > 0 && persian >= latin) {
            return Locale.forLanguageTag("fa-IR");
        }
        if (latin > 0) {
            return Locale.US;
        }
        return fallbackLocale();
    }

    private static Locale fallbackLocale() {
        Locale app = SpeechLocaleHelper.getAppSpeechLocale();
        if (app != null) {
            return app;
        }
        return Locale.getDefault();
    }

    private static boolean shouldSkip(char c) {
        if (Character.isWhitespace(c)) {
            return true;
        }
        if (Character.isDigit(c)) {
            return true;
        }
        return c == '\u200c' || c == '\u200d' || c == '\u200e' || c == '\u200f'
                || (c >= '\u202a' && c <= '\u202e')
                || c == '\ufeff';
    }

    private static boolean isPersianArabicScript(char c) {
        return (c >= 0x0600 && c <= 0x06FF)
                || (c >= 0x0750 && c <= 0x077F)
                || (c >= 0x08A0 && c <= 0x08FF)
                || (c >= 0xFB50 && c <= 0xFDFF)
                || (c >= 0xFE70 && c <= 0xFEFF);
    }

    private static boolean isLatinLetter(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }
}
