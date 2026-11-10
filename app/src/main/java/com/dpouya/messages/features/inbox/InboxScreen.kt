package com.dpouya.messages.features.inbox

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dpouya.messages.core.components.AccentFab
import com.dpouya.messages.core.components.InitialsAvatar
import com.dpouya.messages.core.components.MessagesIconButton
import com.dpouya.messages.core.components.MessagesSearchField
import com.dpouya.messages.core.components.ScreenTitle
import com.dpouya.messages.core.components.ThinDivider
import com.dpouya.messages.core.theme.Dimens
import com.dpouya.messages.core.theme.MessagesThemeTokens
import com.dpouya.messages.core.theme.MessagesTypography
import com.dpouya.messages.core.utils.formatConversationTime
import com.dpouya.messages.data.local.ConversationEntity

@Composable
fun InboxScreen(
    viewModel: InboxViewModel,
    onOpenConversation: (Long) -> Unit,
    onCompose: () -> Unit,
    onOpenSettings: () -> Unit,
    contentPadding: PaddingValues,
) {
    val colors = MessagesThemeTokens.colors
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val query by viewModel.searchQuery.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.sync() }

    Box(
        Modifier
            .fillMaxSize()
            .background(colors.mainBackground)
            .padding(contentPadding),
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.screenPadding)
                    .padding(top = Dimens.titleTop),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ScreenTitle("Messages", Modifier.weight(1f))
                MessagesIconButton(
                    icon = Icons.Outlined.Edit,
                    contentDescription = "New message",
                    onClick = onCompose,
                )
            }
            Spacer(Modifier.height(16.dp))
            MessagesSearchField(
                value = query,
                onValueChange = viewModel::onQueryChange,
                placeholder = "Search conversations...",
                modifier = Modifier.padding(horizontal = Dimens.screenPadding),
            )
            Spacer(Modifier.height(18.dp))
            if (conversations.isEmpty()) {
                EmptyInbox(Modifier.weight(1f))
            } else {
                LazyColumn(Modifier.weight(1f)) {
                    items(conversations, key = { it.id }) { item ->
                        ConversationRow(item) { onOpenConversation(item.id) }
                        ThinDivider()
                    }
                }
            }
        }

        AccentFab(
            icon = Icons.Outlined.Edit,
            contentDescription = "Compose",
            onClick = onCompose,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp),
        )
    }
}

@Composable
private fun ConversationRow(item: ConversationEntity, onClick: () -> Unit) {
    val colors = MessagesThemeTokens.colors
    Row(
        Modifier
            .fillMaxWidth()
            .height(Dimens.conversationRow)
            .clickable(onClick = onClick)
            .padding(horizontal = Dimens.screenPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        InitialsAvatar(item.displayName.ifBlank { item.address })
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.displayName.ifBlank { item.address },
                    style = MessagesTypography.contactName,
                    color = colors.primaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = formatConversationTime(item.timestamp),
                    style = MessagesTypography.timestamp,
                    color = colors.secondaryText,
                )
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.snippet,
                    style = MessagesTypography.messagePreview,
                    color = colors.secondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (item.unreadCount > 0) {
                    Spacer(Modifier.width(8.dp))
                    Box(
                        Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(colors.accent),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = item.unreadCount.coerceAtMost(99).toString(),
                            color = colors.onAccent,
                            fontSize = 11.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyInbox(modifier: Modifier = Modifier) {
    val colors = MessagesThemeTokens.colors
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Outlined.ChatBubbleOutline,
                contentDescription = null,
                tint = colors.mutedText,
                modifier = Modifier.size(64.dp),
            )
            Spacer(Modifier.height(16.dp))
            Text("No conversations yet", style = MessagesTypography.emptyTitle, color = colors.primaryText)
            Spacer(Modifier.height(8.dp))
            Text(
                "Start a new chat to connect with someone.",
                style = MessagesTypography.messagePreview,
                color = colors.secondaryText,
            )
        }
    }
}
