package com.dpouya.messages.features.contacts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dpouya.messages.core.components.InitialsAvatar
import com.dpouya.messages.core.components.MessagesSearchField
import com.dpouya.messages.core.components.ScreenTitle
import com.dpouya.messages.core.components.ThinDivider
import com.dpouya.messages.core.di.AppContainer
import com.dpouya.messages.core.theme.Dimens
import com.dpouya.messages.core.theme.MessagesThemeTokens
import com.dpouya.messages.core.theme.MessagesTypography
import com.dpouya.messages.data.repository.ContactItem

@Composable
fun ContactsScreen(
    container: AppContainer,
    contentPadding: PaddingValues,
    onOpenComposeWith: (ContactItem) -> Unit,
) {
    val colors = MessagesThemeTokens.colors
    var query by remember { mutableStateOf("") }
    var contacts by remember { mutableStateOf<List<ContactItem>>(emptyList()) }

    LaunchedEffect(query) {
        contacts = container.contactRepository.search(query)
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(colors.mainBackground)
            .padding(contentPadding),
    ) {
        ScreenTitle(
            "Contacts",
            Modifier.padding(horizontal = Dimens.screenPadding, vertical = Dimens.titleTop),
        )
        MessagesSearchField(
            value = query,
            onValueChange = { query = it },
            placeholder = "Search contacts...",
            modifier = Modifier.padding(horizontal = Dimens.screenPadding),
        )
        Spacer(Modifier.height(12.dp))
        LazyColumn(Modifier.fillMaxSize()) {
            items(contacts, key = { "${it.id}-${it.phone}" }) { contact ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(62.dp)
                        .clickable { onOpenComposeWith(contact) }
                        .padding(horizontal = Dimens.screenPadding),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    InitialsAvatar(contact.name.ifBlank { contact.phone }, 40.dp)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(contact.name.ifBlank { contact.phone }, style = MessagesTypography.contactName, color = colors.primaryText)
                        Text(contact.phone, style = MessagesTypography.timestamp, color = colors.secondaryText)
                    }
                }
                ThinDivider()
            }
        }
    }
}
