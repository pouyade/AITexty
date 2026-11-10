package com.dpouya.messages.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.dpouya.messages.core.components.BackIconButton
import com.dpouya.messages.core.theme.Dimens
import com.dpouya.messages.core.theme.MessagesThemeTokens
import com.dpouya.messages.core.theme.MessagesTypography

@Composable
fun SimplePlaceholderScreen(title: String, body: String, onBack: () -> Unit) {
    val colors = MessagesThemeTokens.colors
    Column(
        Modifier
            .fillMaxSize()
            .background(colors.mainBackground),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            BackIconButton(onBack)
            Text(title, style = MessagesTypography.sectionTitle, color = colors.primaryText)
        }
        Text(
            body,
            style = MessagesTypography.messagePreview,
            color = colors.secondaryText,
            modifier = Modifier.padding(Dimens.screenPadding),
        )
    }
}
