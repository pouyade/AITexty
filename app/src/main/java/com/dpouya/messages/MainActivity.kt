package com.dpouya.messages

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dpouya.messages.core.navigation.MessagesNavHost
import com.dpouya.messages.core.theme.MessagesTheme
import com.dpouya.messages.core.theme.ThemeMode

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as MessagesApplication).container
        setContent {
            val themeMode by container.themePreferences.themeMode.collectAsStateWithLifecycle(
                initialValue = ThemeMode.SYSTEM
            )
            MessagesTheme(themeMode = themeMode) {
                MessagesNavHost(container = container)
            }
        }
    }
}
