package com.dpouya.aitexty.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

import com.dpouya.aitexty.data.AppDatabase;
import com.dpouya.aitexty.data.entity.ChatTheme;
import com.dpouya.aitexty.helper.Theme;

import java.util.HashMap;
import java.util.Map;

public class ChatThemeManager {
    public static Map<String, Integer> getEffectiveColors(Context context, long conversationId) {
        Map<String, Integer> colors = new HashMap<>(Theme.currentTheme.colors);
        ChatTheme ct = AppDatabase.getInstance(context).chatThemeDao().getByConversationId(conversationId);
        if (ct == null) return colors;
        if (ct.bubbleIncomingColor != null) {
            colors.put(Theme.MESSAGE_INCOMING_BG, Color.parseColor(ct.bubbleIncomingColor));
        }
        if (ct.bubbleOutgoingColor != null) {
            colors.put(Theme.MESSAGE_OUTGOING_BG, Color.parseColor(ct.bubbleOutgoingColor));
        }
        return colors;
    }

    public static int getBackgroundColor(Context context, long conversationId) {
        ChatTheme ct = AppDatabase.getInstance(context).chatThemeDao().getByConversationId(conversationId);
        if (ct != null && "color".equals(ct.backgroundType) && ct.backgroundValue != null) {
            try {
                return Color.parseColor(ct.backgroundValue);
            } catch (Exception ignored) {
            }
        }
        return Theme.getColor(Theme.BACKGROUND_COLOR);
    }

    public static void saveTheme(Context context, long conversationId, String incoming, String outgoing, String bgColor) {
        ChatTheme ct = new ChatTheme();
        ct.conversationId = conversationId;
        ct.backgroundType = "color";
        ct.backgroundValue = bgColor;
        ct.bubbleIncomingColor = incoming;
        ct.bubbleOutgoingColor = outgoing;
        ct.fontScale = 1f;
        AppDatabase.getInstance(context).chatThemeDao().insert(ct);
    }
}
