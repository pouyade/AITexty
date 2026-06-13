package com.dpouya.aitexty.accessibility;

import android.text.TextUtils;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class VoiceCommandParser {
    public enum Type {
        SEND_MESSAGE,
        READ_MESSAGE,
        READ_ALL_MESSAGES,
        FIND_CHAT,
        GO_BACK,
        DICTATION
    }

    public static class ParsedCommand {
        public final Type type;
        public final String argument;

        public ParsedCommand(Type type, String argument) {
            this.type = type;
            this.argument = argument;
        }
    }

    private static final Pattern FIND_EN = Pattern.compile(
            "(?:find|open|search)\\s+(?:chat\\s+)?(?:with\\s+)?(.+)",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern FIND_FA = Pattern.compile(
            "(?:پیدا\\s+کن|باز\\s+کن|جستجو\\s+کن)\\s+(?:گفتگو\\s+)?(?:با\\s+)?(.+)");

    private VoiceCommandParser() {
    }

    public static ParsedCommand parse(String spokenText) {
        if (TextUtils.isEmpty(spokenText)) {
            return new ParsedCommand(Type.DICTATION, "");
        }
        String text = spokenText.trim();
        String lower = text.toLowerCase(Locale.getDefault());

        if (matchesSend(lower, text)) {
            return new ParsedCommand(Type.SEND_MESSAGE, null);
        }
        if (matchesReadAll(lower, text)) {
            return new ParsedCommand(Type.READ_ALL_MESSAGES, null);
        }
        if (matchesRead(lower, text)) {
            return new ParsedCommand(Type.READ_MESSAGE, null);
        }
        if (matchesBack(lower, text)) {
            return new ParsedCommand(Type.GO_BACK, null);
        }

        Matcher en = FIND_EN.matcher(text);
        if (en.find()) {
            return new ParsedCommand(Type.FIND_CHAT, en.group(1).trim());
        }
        Matcher fa = FIND_FA.matcher(text);
        if (fa.find()) {
            return new ParsedCommand(Type.FIND_CHAT, fa.group(1).trim());
        }

        return new ParsedCommand(Type.DICTATION, text);
    }

    private static boolean matchesSend(String lower, String original) {
        return lower.contains("send message")
                || lower.equals("send")
                || lower.contains("send sms")
                || original.contains("ارسال پیام")
                || original.contains("ارسال");
    }

    private static boolean matchesRead(String lower, String original) {
        return lower.contains("read message")
                || lower.contains("read last")
                || lower.equals("read")
                || original.contains("خواندن پیام")
                || original.contains("بخوان");
    }

    private static boolean matchesReadAll(String lower, String original) {
        return lower.contains("read all")
                || lower.contains("read messages")
                || original.contains("خواندن همه");
    }

    private static boolean matchesBack(String lower, String original) {
        return lower.contains("go back")
                || lower.equals("back")
                || original.contains("برگشت")
                || original.contains("بازگشت");
    }
}
