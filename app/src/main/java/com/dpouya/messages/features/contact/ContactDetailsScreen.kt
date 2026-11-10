package com.dpouya.messages.features.contact

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dpouya.messages.core.components.BackIconButton
import com.dpouya.messages.core.components.InitialsAvatar
import com.dpouya.messages.core.components.MessagesIconButton
import com.dpouya.messages.core.components.SettingsRow
import com.dpouya.messages.core.components.ThinDivider
import com.dpouya.messages.core.theme.Dimens
import com.dpouya.messages.core.theme.MessagesThemeTokens
import com.dpouya.messages.core.theme.MessagesTypography
import com.dpouya.messages.data.repository.SmsRepository

@Composable
fun ContactDetailsScreen(
    conversationId: Long,
    smsRepository: SmsRepository,
    onBack: () -> Unit,
    onMessage: (Long) -> Unit,
) {
    val colors = MessagesThemeTokens.colors
    val conversation by smsRepository.observeConversation(conversationId)
        .collectAsStateWithLifecycle(initialValue = null)
    val name = conversation?.displayName ?: ""
    val phone = conversation?.address ?: ""

    Column(
        Modifier
            .fillMaxSize()
            .background(colors.mainBackground),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(Dimens.toolbar),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            BackIconButton(onBack)
            MessagesIconButton(Icons.Outlined.MoreVert, "More", onClick = {})
        }
        Column(
            Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            InitialsAvatar(name.ifBlank { phone }, Dimens.profileAvatar)
            Spacer(Modifier.height(14.dp))
            Text(name.ifBlank { phone }, style = MessagesTypography.profileName, color = colors.primaryText)
            Spacer(Modifier.height(4.dp))
            Text(phone, style = MessagesTypography.messagePreview, color = colors.secondaryText)
        }
        Spacer(Modifier.height(28.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            QuickAction(Icons.Outlined.Call, "Call")
            QuickAction(Icons.Outlined.ChatBubbleOutline, "Message") { onMessage(conversationId) }
            QuickAction(Icons.Outlined.Videocam, "Video")
        }
        Spacer(Modifier.height(28.dp))
        ThinDivider()
        SettingsRow("Notifications", onClick = {}, icon = Icons.Outlined.Notifications)
        ThinDivider()
        SettingsRow("Media, links and documents", onClick = {}, icon = Icons.Outlined.FolderOpen)
        ThinDivider()
        SettingsRow("Chat settings", onClick = {}, icon = Icons.Outlined.Settings)
        ThinDivider()
        SettingsRow("Block contact", onClick = {}, icon = Icons.Outlined.Block, destructive = true)
    }
}

@Composable
private fun QuickAction(icon: ImageVector, label: String, onClick: () -> Unit = {}) {
    val colors = MessagesThemeTokens.colors
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(Dimens.buttonRadius))
                .background(colors.surface)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, label, tint = colors.accent, modifier = Modifier.size(21.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(label, style = MessagesTypography.navLabel, color = colors.secondaryText)
    }
}
