package com.dpouya.messages.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dpouya.messages.core.components.BackIconButton
import com.dpouya.messages.core.components.ScreenTitle
import com.dpouya.messages.core.components.SegmentedThemeControl
import com.dpouya.messages.core.components.SettingsRow
import com.dpouya.messages.core.components.ThinDivider
import com.dpouya.messages.core.theme.Dimens
import com.dpouya.messages.core.theme.MessagesThemeTokens
import com.dpouya.messages.core.theme.MessagesTypography
import com.dpouya.messages.core.theme.ThemeMode
import com.dpouya.messages.data.preferences.ThemePreferences
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    themePreferences: ThemePreferences,
    onBack: () -> Unit,
    onAbout: () -> Unit,
    onNotifications: () -> Unit,
    onPrivacy: () -> Unit,
    onBlocked: () -> Unit,
) {
    val colors = MessagesThemeTokens.colors
    val themeMode by themePreferences.themeMode.collectAsStateWithLifecycle(ThemeMode.SYSTEM)
    val scope = rememberCoroutineScope()

    Column(
        Modifier
            .fillMaxSize()
            .background(colors.mainBackground),
    ) {
        BackIconButton(onBack)
        ScreenTitle(
            "Settings",
            Modifier.padding(horizontal = Dimens.screenPadding),
        )
        Spacer(Modifier.height(24.dp))
        Column(
            Modifier
                .padding(horizontal = Dimens.screenPadding)
                .clip(RoundedCornerShape(Dimens.cardRadius))
                .background(colors.secondaryBackground)
                .padding(14.dp),
        ) {
            Text("Appearance", style = MessagesTypography.sectionTitle, color = colors.primaryText)
            Spacer(Modifier.height(4.dp))
            Text("Choose theme", style = MessagesTypography.messagePreview, color = colors.secondaryText)
            Spacer(Modifier.height(14.dp))
            SegmentedThemeControl(themeMode) { mode ->
                scope.launch { themePreferences.setThemeMode(mode) }
            }
        }
        Spacer(Modifier.height(12.dp))
        Column(
            Modifier
                .padding(horizontal = Dimens.screenPadding)
                .clip(RoundedCornerShape(Dimens.cardRadius))
                .background(colors.secondaryBackground),
        ) {
            SettingsRow("Notifications", onClick = onNotifications, icon = Icons.Outlined.Notifications)
            ThinDivider()
            SettingsRow("Privacy & Security", onClick = onPrivacy, icon = Icons.Outlined.Lock)
            ThinDivider()
            SettingsRow("Blocked Contacts", onClick = onBlocked, icon = Icons.Outlined.Block)
            ThinDivider()
            SettingsRow("About", onClick = onAbout, icon = Icons.Outlined.Info)
        }
        Spacer(Modifier.weight(1f))
        Text(
            "Version 1.0.0",
            style = MessagesTypography.timestamp,
            color = colors.secondaryText,
            modifier = Modifier.padding(Dimens.screenPadding),
        )
    }
}
