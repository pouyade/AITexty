package com.dpouya.messages.core.di

import android.content.Context
import com.dpouya.messages.data.local.MessagesDatabase
import com.dpouya.messages.data.preferences.ThemePreferences
import com.dpouya.messages.data.repository.ContactRepository
import com.dpouya.messages.data.repository.SmsRepository

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    val database: MessagesDatabase = MessagesDatabase.get(appContext)
    val themePreferences = ThemePreferences(appContext)
    val smsRepository = SmsRepository(appContext, database)
    val contactRepository = ContactRepository(appContext)
}
