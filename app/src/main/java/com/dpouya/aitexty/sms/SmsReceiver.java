package com.dpouya.aitexty.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;

import com.dpouya.aitexty.ai.SpamClassifier;
import com.dpouya.aitexty.data.SmsRepository;
import com.dpouya.aitexty.data.entity.Message;
import com.dpouya.aitexty.privacy.DecoyNotificationBuilder;

public class SmsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;
        // Default SMS app receives SMS_DELIVER only — do not also handle SMS_RECEIVED (causes duplicates).
        if (!Telephony.Sms.Intents.SMS_DELIVER_ACTION.equals(intent.getAction())) {
            return;
        }

        Bundle bundle = intent.getExtras();
        if (bundle == null) return;

        Object[] pdus = (Object[]) bundle.get("pdus");
        if (pdus == null) return;

        String format = bundle.getString("format");
        SmsRepository repo = SmsRepository.getInstance(context);

        for (Object pdu : pdus) {
            SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu, format);
            if (sms == null) continue;

            String address = sms.getDisplayOriginatingAddress();
            String body = sms.getMessageBody();
            long timestamp = sms.getTimestampMillis();

            Message msg = repo.insertIncomingMessage(address, body, timestamp);
            if (msg == null) continue;

            SpamClassifier.getInstance(context).classifyAsync(msg, address, body);

            if (com.dpouya.aitexty.helper.AppSettings.Bool(com.dpouya.aitexty.helper.AppSettings.Key.AUTO_REPLY_ENABLED)) {
                new com.dpouya.aitexty.ai.AutoResponseAgent(context).sendAutoReplyIfEnabled(msg.conversationId, null);
            }

            com.dpouya.aitexty.data.entity.Conversation conv =
                    repo.getDatabase().conversationDao().getById(msg.conversationId);
            if (conv != null && conv.isHidden) {
                DecoyNotificationBuilder.show(context, conv);
            } else if (conv != null) {
                DecoyNotificationBuilder.showNormal(context, conv, body);
            }
        }
    }
}
