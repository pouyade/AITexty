package com.dpouya.messages

import android.app.Application
import com.dpouya.messages.core.di.AppContainer

class MessagesApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
