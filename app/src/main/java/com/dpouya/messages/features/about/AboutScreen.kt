package com.dpouya.messages.features.about

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.dpouya.messages.core.components.BackIconButton
import com.dpouya.messages.core.theme.Dimens
import com.dpouya.messages.core.theme.MessagesThemeTokens
import com.dpouya.messages.core.theme.MessagesTypography

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val colors = MessagesThemeTokens.colors
    Column(
        Modifier
            .fillMaxSize()
            .background(colors.mainBackground),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            BackIconButton(onBack)
            Text("About", style = MessagesTypography.sectionTitle, color = colors.primaryText)
        }
        Spacer(Modifier.height(48.dp))
        Canvas(Modifier.size(80.dp)) {
            drawRoundRect(
                color = colors.accent,
                topLeft = Offset(size.width * 0.12f, size.height * 0.1f),
                size = Size(size.width * 0.76f, size.height * 0.62f),
                cornerRadius = CornerRadius(8.dp.toPx()),
                style = Stroke(width = 4.dp.toPx()),
            )
            val cy = size.height * 0.38f
            val r = 3.8.dp.toPx()
            listOf(0.32f, 0.5f, 0.68f).forEach { x ->
                drawCircle(colors.accent, radius = r, center = Offset(size.width * x, cy))
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("Messages", style = MessagesTypography.aboutTitle, color = colors.primaryText)
        Spacer(Modifier.height(6.dp))
        Text("Version 1.0.0", style = MessagesTypography.timestamp, color = colors.secondaryText)
        Spacer(Modifier.height(6.dp))
        Text("Simple. Fast. Reliable.", style = MessagesTypography.messagePreview, color = colors.secondaryText)
        Spacer(Modifier.height(40.dp))
        AboutLink("Terms of Service")
        Spacer(Modifier.height(10.dp))
        AboutLink("Privacy Policy")
    }
}

@Composable
private fun AboutLink(title: String) {
    val colors = MessagesThemeTokens.colors
    Row(
        Modifier
            .padding(horizontal = Dimens.screenPadding)
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(Dimens.cardRadius))
            .background(colors.secondaryBackground)
            .clickable { }
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MessagesTypography.settingsLabel, color = colors.primaryText, modifier = Modifier.weight(1f))
        Icon(Icons.Outlined.ChevronRight, null, tint = colors.mutedText)
    }
}
