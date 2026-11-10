package com.dpouya.messages.data.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.dpouya.messages.MessagesApplication

class SmsDeliverReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_DELIVER_ACTION) return
        val app = context.applicationContext as? MessagesApplication ?: return
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
        val body = messages.joinToString(separator = "") { it.displayMessageBody ?: "" }
        val address = messages.firstOrNull()?.displayOriginatingAddress ?: return
        val timestamp = messages.firstOrNull()?.timestampMillis ?: System.currentTimeMillis()
        app.container.smsRepository.onIncomingSms(address, body, timestamp)
    }
}
