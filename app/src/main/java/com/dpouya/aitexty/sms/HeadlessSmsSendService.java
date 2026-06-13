package com.dpouya.aitexty.sms;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.text.TextUtils;

import com.dpouya.aitexty.data.SmsRepository;

public class HeadlessSmsSendService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && "android.intent.action.RESPOND_VIA_MESSAGE".equals(intent.getAction())) {
            Uri data = intent.getData();
            if (data != null) {
                String address = data.getSchemeSpecificPart();
                String message = intent.getStringExtra("sms_body");
                if (!TextUtils.isEmpty(address) && !TextUtils.isEmpty(message)) {
                    SmsManager.getDefault().sendTextMessage(address, null, message, null, null);
                    SmsRepository repo = SmsRepository.getInstance(this);
                    com.dpouya.aitexty.data.entity.Conversation conv = repo.getOrCreateConversation(address, address);
                    repo.sendMessage(conv.id, message);
                }
            }
        }
        stopSelf(startId);
        return START_NOT_STICKY;
    }
}
