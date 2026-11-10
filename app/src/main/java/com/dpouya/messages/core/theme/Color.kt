package com.dpouya.messages.core.theme

import androidx.compose.ui.graphics.Color

object DarkColors {
    val MainBackground = Color(0xFF090B10)
    val SecondaryBackground = Color(0xFF10131B)
    val Surface = Color(0xFF151923)
    val ElevatedSurface = Color(0xFF1B202B)
    val PrimaryText = Color(0xFFF5F7FA)
    val SecondaryText = Color(0xFF929AAA)
    val MutedText = Color(0xFF606A7A)
    val Divider = Color(0xFF252B37)
    val AccentBlue = Color(0xFF3478F6)
    val AccentBluePressed = Color(0xFF2865D8)
    val IncomingMessage = Color(0xFF1B202B)
    val OutgoingMessage = Color(0xFF3478F6)
    val Error = Color(0xFFFF5B62)
    val Success = Color(0xFF32C98A)
    val InputBackground = Color(0xFF151923)
    val AvatarFallback = Color(0xFF35456F)
}

object LightColors {
    val MainBackground = Color(0xFFFFFFFF)
    val SecondaryBackground = Color(0xFFF7F8FA)
    val Surface = Color(0xFFFFFFFF)
    val ElevatedSurface = Color(0xFFF0F2F6)
    val PrimaryText = Color(0xFF111827)
    val SecondaryText = Color(0xFF687386)
    val MutedText = Color(0xFF9AA3B2)
    val Divider = Color(0xFFE8EBF0)
    val AccentBlue = Color(0xFF3478F6)
    val AccentBluePressed = Color(0xFF2865D8)
    val IncomingMessage = Color(0xFFF0F2F6)
    val OutgoingMessage = Color(0xFF3478F6)
    val Error = Color(0xFFDC444C)
    val Success = Color(0xFF159D6A)
    val InputBackground = Color(0xFFF0F2F6)
    val AvatarFallback = Color(0xFF35456F)
}

data class MessagesColors(
    val mainBackground: Color,
    val secondaryBackground: Color,
    val surface: Color,
    val elevatedSurface: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val mutedText: Color,
    val divider: Color,
    val accent: Color,
    val accentPressed: Color,
    val incomingMessage: Color,
    val outgoingMessage: Color,
    val error: Color,
    val success: Color,
    val inputBackground: Color,
    val avatarFallback: Color,
    val onAccent: Color = Color.White,
)

val DarkPalette = MessagesColors(
    mainBackground = DarkColors.MainBackground,
    secondaryBackground = DarkColors.SecondaryBackground,
    surface = DarkColors.Surface,
    elevatedSurface = DarkColors.ElevatedSurface,
    primaryText = DarkColors.PrimaryText,
    secondaryText = DarkColors.SecondaryText,
    mutedText = DarkColors.MutedText,
    divider = DarkColors.Divider,
    accent = DarkColors.AccentBlue,
    accentPressed = DarkColors.AccentBluePressed,
    incomingMessage = DarkColors.IncomingMessage,
    outgoingMessage = DarkColors.OutgoingMessage,
    error = DarkColors.Error,
    success = DarkColors.Success,
    inputBackground = DarkColors.InputBackground,
    avatarFallback = DarkColors.AvatarFallback,
)

val LightPalette = MessagesColors(
    mainBackground = LightColors.MainBackground,
    secondaryBackground = LightColors.SecondaryBackground,
    surface = LightColors.Surface,
    elevatedSurface = LightColors.ElevatedSurface,
    primaryText = LightColors.PrimaryText,
    secondaryText = LightColors.SecondaryText,
    mutedText = LightColors.MutedText,
    divider = LightColors.Divider,
    accent = LightColors.AccentBlue,
    accentPressed = LightColors.AccentBluePressed,
    incomingMessage = LightColors.IncomingMessage,
    outgoingMessage = LightColors.OutgoingMessage,
    error = LightColors.Error,
    success = LightColors.Success,
    inputBackground = LightColors.InputBackground,
    avatarFallback = LightColors.AvatarFallback,
)
