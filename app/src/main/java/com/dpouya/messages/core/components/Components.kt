package com.dpouya.messages.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dpouya.messages.core.theme.Dimens
import com.dpouya.messages.core.theme.MessagesThemeTokens
import com.dpouya.messages.core.theme.MessagesTypography

@Composable
fun InitialsAvatar(
    name: String,
    size: Dp = Dimens.avatar,
    modifier: Modifier = Modifier,
) {
    val colors = MessagesThemeTokens.colors
    val initials = name.trim()
        .split(" ")
        .filter { it.isNotEmpty() }
        .take(2)
        .joinToString("") { it.first().uppercaseChar().toString() }
        .ifBlank { "?" }
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(colors.avatarFallback),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            color = colors.onAccent,
            style = MessagesTypography.contactName.copy(fontSize = (size.value * 0.35f).sp),
        )
    }
}

@Composable
fun MessagesSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    val colors = MessagesThemeTokens.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(Dimens.searchHeight)
            .clip(RoundedCornerShape(Dimens.searchRadius))
            .background(colors.secondaryBackground)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.Search,
            contentDescription = null,
            tint = colors.mutedText,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(10.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = MessagesTypography.messagePreview.copy(color = colors.primaryText),
            cursorBrush = SolidColor(colors.accent),
            modifier = Modifier.weight(1f),
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text(placeholder, style = MessagesTypography.messagePreview, color = colors.mutedText)
                }
                inner()
            },
        )
    }
}

@Composable
fun MessagesIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
) {
    val colors = MessagesThemeTokens.colors
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    Box(
        modifier = modifier
            .size(size)
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (pressed) colors.accentPressed else colors.primaryText,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
fun BackIconButton(onClick: () -> Unit) {
    MessagesIconButton(
        icon = Icons.AutoMirrored.Outlined.ArrowBack,
        contentDescription = "Back",
        onClick = onClick,
        size = 48.dp,
    )
}

@Composable
fun SettingsRow(
    title: String,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    subtitle: String? = null,
    destructive: Boolean = false,
    trailing: @Composable (() -> Unit)? = {
        Icon(Icons.Outlined.ChevronRight, null, tint = MessagesThemeTokens.colors.mutedText, modifier = Modifier.size(20.dp))
    },
) {
    val colors = MessagesThemeTokens.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, null, tint = if (destructive) colors.error else colors.secondaryText, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MessagesTypography.settingsLabel,
                color = if (destructive) colors.error else colors.primaryText,
            )
            if (subtitle != null) {
                Text(subtitle, style = MessagesTypography.timestamp, color = colors.secondaryText)
            }
        }
        trailing?.invoke()
    }
}

@Composable
fun ThinDivider() {
    HorizontalDivider(color = MessagesThemeTokens.colors.divider, thickness = 1.dp)
}

@Composable
fun ScreenTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MessagesTypography.screenTitle,
        color = MessagesThemeTokens.colors.primaryText,
        modifier = modifier,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
fun AccentFab(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MessagesThemeTokens.colors
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    Box(
        modifier = modifier
            .size(Dimens.fab)
            .clip(RoundedCornerShape(Dimens.buttonRadius))
            .background(if (pressed) colors.accentPressed else colors.accent)
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription, tint = colors.onAccent, modifier = Modifier.size(22.dp))
    }
}

@Composable
fun SegmentedThemeControl(
    selected: com.dpouya.messages.core.theme.ThemeMode,
    onSelect: (com.dpouya.messages.core.theme.ThemeMode) -> Unit,
) {
    val colors = MessagesThemeTokens.colors
    val options = listOf(
        com.dpouya.messages.core.theme.ThemeMode.LIGHT to "Light",
        com.dpouya.messages.core.theme.ThemeMode.DARK to "Dark",
        com.dpouya.messages.core.theme.ThemeMode.SYSTEM to "System",
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.cardRadius))
            .background(colors.elevatedSurface)
            .padding(4.dp),
    ) {
        options.forEach { (mode, label) ->
            val on = selected == mode
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .clip(RoundedCornerShape(Dimens.buttonRadius))
                    .background(if (on) colors.accent else colors.elevatedSurface)
                    .clickable { onSelect(mode) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    style = MessagesTypography.button,
                    color = if (on) colors.onAccent else colors.secondaryText,
                )
            }
        }
    }
}
