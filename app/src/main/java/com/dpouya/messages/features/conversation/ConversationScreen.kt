package com.dpouya.messages.features.conversation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dpouya.messages.core.components.BackIconButton
import com.dpouya.messages.core.components.InitialsAvatar
import com.dpouya.messages.core.components.MessagesIconButton
import com.dpouya.messages.core.components.ThinDivider
import com.dpouya.messages.core.theme.Dimens
import com.dpouya.messages.core.theme.MessagesThemeTokens
import com.dpouya.messages.core.theme.MessagesTypography
import com.dpouya.messages.core.utils.formatMessageTime
import com.dpouya.messages.data.local.MessageEntity

@Composable
fun ConversationScreen(
    viewModel: ConversationViewModel,
    onBack: () -> Unit,
    onOpenContact: (Long) -> Unit,
) {
    val colors = MessagesThemeTokens.colors
    val conversation by viewModel.conversation.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val draft by viewModel.draft.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val maxBubble = LocalConfiguration.current.screenWidthDp.dp * 0.78f

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(colors.mainBackground)
            .imePadding(),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(Dimens.chatToolbar)
                .padding(end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BackIconButton(onBack)
            InitialsAvatar(
                name = conversation?.displayName ?: conversation?.address ?: "?",
                size = Dimens.chatAvatar,
            )
            Spacer(Modifier.width(10.dp))
            Column(
                Modifier
                    .weight(1f)
                    .clickable(enabled = conversation != null) {
                        conversation?.let { onOpenContact(it.id) }
                    },
            ) {
                Text(
                    conversation?.displayName ?: "",
                    style = MessagesTypography.contactName,
                    color = colors.primaryText,
                    maxLines = 1,
                )
                Text("Mobile", style = MessagesTypography.timestamp, color = colors.secondaryText)
            }
            MessagesIconButton(Icons.Outlined.Phone, "Call", onClick = {})
            MessagesIconButton(Icons.Outlined.MoreVert, "More", onClick = {})
        }
        ThinDivider()

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(messages, key = { it.id }) { message ->
                MessageBubble(message, maxBubble)
            }
        }

        ThinDivider()
        Row(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MessagesIconButton(Icons.Outlined.AttachFile, "Attach", onClick = {}, size = 40.dp)
            Spacer(Modifier.width(6.dp))
            BasicTextField(
                value = draft,
                onValueChange = viewModel::onDraftChange,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 44.dp)
                    .clip(RoundedCornerShape(Dimens.searchRadius))
                    .background(colors.inputBackground)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                textStyle = MessagesTypography.messagePreview.copy(color = colors.primaryText),
                cursorBrush = SolidColor(colors.accent),
                decorationBox = { inner ->
                    if (draft.isEmpty()) {
                        Text("Type a message...", style = MessagesTypography.messagePreview, color = colors.mutedText)
                    }
                    inner()
                },
            )
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier
                    .size(Dimens.send)
                    .clip(RoundedCornerShape(Dimens.buttonRadius))
                    .background(colors.accent)
                    .clickable(enabled = draft.isNotBlank()) { viewModel.send() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.AutoMirrored.Outlined.Send, "Send", tint = colors.onAccent, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun MessageBubble(message: MessageEntity, maxWidth: androidx.compose.ui.unit.Dp) {
    val colors = MessagesThemeTokens.colors
    val outgoing = message.isOutgoing
    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = if (outgoing) Alignment.End else Alignment.Start,
    ) {
        Box(
            Modifier
                .widthIn(max = maxWidth)
                .clip(RoundedCornerShape(Dimens.bubbleRadius))
                .background(if (outgoing) colors.outgoingMessage else colors.incomingMessage)
                .padding(horizontal = 12.dp, vertical = 9.dp),
        ) {
            Text(
                message.body,
                style = MessagesTypography.chatMessage,
                color = if (outgoing) colors.onAccent else colors.primaryText,
            )
        }
        Spacer(Modifier.height(2.dp))
        val status = when {
            !outgoing -> formatMessageTime(message.timestamp)
            message.status == MessageEntity.Status.FAILED -> "${formatMessageTime(message.timestamp)} · Failed"
            message.status == MessageEntity.Status.DELIVERED -> "${formatMessageTime(message.timestamp)} · Delivered"
            else -> "${formatMessageTime(message.timestamp)} · Sent"
        }
        Text(
            status,
            style = MessagesTypography.timestamp.copy(fontSize = 10.sp),
            color = if (message.status == MessageEntity.Status.FAILED) colors.error else colors.secondaryText,
        )
    }
}
