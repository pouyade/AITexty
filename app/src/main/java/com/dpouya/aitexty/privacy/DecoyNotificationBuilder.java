package com.dpouya.aitexty.privacy;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.dpouya.aitexty.R;
import com.dpouya.aitexty.activities.MainActivity;
import com.dpouya.aitexty.data.entity.Conversation;
import com.dpouya.aitexty.helper.AppSettings;
import com.dpouya.aitexty.helper.LocaleController;

public class DecoyNotificationBuilder {
    private static final String CHANNEL_ID = "aitexty_messages";

    public static void show(Context context, Conversation conv) {
        String style = AppSettings.String(AppSettings.Key.DECOY_NOTIFICATION_STYLE);
        if (style == null || style.isEmpty()) style = "generic";
        String title;
        String body;
        if ("weather".equals(style)) {
            title = conv.decoyTitle != null ? conv.decoyTitle : "Weather";
            body = conv.decoyBody != null ? conv.decoyBody : "Rain expected tomorrow";
        } else if ("news".equals(style)) {
            title = conv.decoyTitle != null ? conv.decoyTitle : "News";
            body = conv.decoyBody != null ? conv.decoyBody : "Breaking: Market update";
        } else if (conv.decoyTitle != null) {
            title = conv.decoyTitle;
            body = conv.decoyBody != null ? conv.decoyBody : "New notification";
        } else {
            title = "System";
            body = "New notification";
        }
        post(context, title, body, (int) conv.id);
    }

    public static void showNormal(Context context, Conversation conv, String body) {
        String title = conv.displayName != null ? conv.displayName : conv.address;
        post(context, title, body, (int) conv.id);
    }

    private static void post(Context context, String title, String body, int id) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(CHANNEL_ID,
                    LocaleController.getString("new_message"), NotificationManager.IMPORTANCE_DEFAULT);
            nm.createNotificationChannel(ch);
        }
        PendingIntent pi = PendingIntent.getActivity(context, id,
                new android.content.Intent(context, MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_comment)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(pi)
                .setAutoCancel(true);
        nm.notify(id, builder.build());
    }
}
