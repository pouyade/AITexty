package com.dpouya.messages.features.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.dpouya.messages.core.components.BackIconButton
import com.dpouya.messages.core.components.InitialsAvatar
import com.dpouya.messages.core.components.MessagesIconButton
import com.dpouya.messages.core.components.MessagesSearchField
import com.dpouya.messages.core.components.ThinDivider
import com.dpouya.messages.core.di.AppContainer
import com.dpouya.messages.core.theme.Dimens
import com.dpouya.messages.core.theme.MessagesThemeTokens
import com.dpouya.messages.core.theme.MessagesTypography
import com.dpouya.messages.data.repository.ContactItem
import kotlinx.coroutines.launch

@Composable
fun ComposeMessageScreen(
    container: AppContainer,
    onBack: () -> Unit,
    onOpenedConversation: (Long) -> Unit,
) {
    val colors = MessagesThemeTokens.colors
    var query by remember { mutableStateOf("") }
    var contacts by remember { mutableStateOf<List<ContactItem>>(emptyList()) }
    var selected by remember { mutableStateOf<ContactItem?>(null) }
    var draft by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(query) {
        contacts = container.contactRepository.search(query)
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
                .height(Dimens.toolbar),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BackIconButton(onBack)
            Text("New Message", style = MessagesTypography.sectionTitle, color = colors.primaryText)
        }
        ThinDivider()
        Spacer(Modifier.height(12.dp))
        MessagesSearchField(
            value = query,
            onValueChange = { query = it },
            placeholder = "Search contacts...",
            modifier = Modifier.padding(horizontal = Dimens.screenPadding),
        )
        if (selected != null) {
            Spacer(Modifier.height(12.dp))
            Box(
                Modifier
                    .padding(horizontal = Dimens.screenPadding)
                    .clip(RoundedCornerShape(Dimens.buttonRadius))
                    .background(colors.elevatedSurface)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Text(selected!!.name.ifBlank { selected!!.phone }, color = colors.primaryText, style = MessagesTypography.messagePreview)
            }
        }
        Spacer(Modifier.height(8.dp))
        LazyColumn(Modifier.weight(1f)) {
            items(contacts, key = { it.id to it.phone }) { contact ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(62.dp)
                        .clickable { selected = contact }
                        .padding(horizontal = Dimens.screenPadding),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    InitialsAvatar(contact.name.ifBlank { contact.phone }, size = 40.dp)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(contact.name, style = MessagesTypography.settingsLabel.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold), color = colors.primaryText)
                        Text(contact.phone, style = MessagesTypography.timestamp, color = colors.secondaryText)
                    }
                }
                ThinDivider()
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
                onValueChange = { draft = it },
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(Dimens.searchRadius))
                    .background(colors.inputBackground)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                textStyle = MessagesTypography.messagePreview.copy(color = colors.primaryText),
                cursorBrush = SolidColor(colors.accent),
                decorationBox = { inner ->
                    if (draft.isEmpty()) Text("Type a message...", style = MessagesTypography.messagePreview, color = colors.mutedText)
                    inner()
                },
            )
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier
                    .size(Dimens.send)
                    .clip(RoundedCornerShape(Dimens.buttonRadius))
                    .background(colors.accent)
                    .clickable(enabled = selected != null) {
                        val contact = selected ?: return@clickable
                        scope.launch {
                            val conv = container.smsRepository.getOrCreateConversation(contact.phone, contact.name)
                            if (draft.isNotBlank()) {
                                container.smsRepository.sendMessage(conv.id, draft)
                            }
                            onOpenedConversation(conv.id)
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.AutoMirrored.Outlined.Send, "Send", tint = colors.onAccent, modifier = Modifier.size(18.dp))
            }
        }
    }
}
