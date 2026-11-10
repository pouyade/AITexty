package com.dpouya.messages.data.sms

import android.app.Service
import android.content.Intent
import android.os.IBinder

/** Required stub for default SMS role. */
class HeadlessSmsSendService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
}
