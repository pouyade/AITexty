package com.dpouya.messages.features.home

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.PeopleOutline
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dpouya.messages.core.di.AppContainer
import com.dpouya.messages.core.theme.MessagesThemeTokens
import com.dpouya.messages.core.theme.MessagesTypography
import com.dpouya.messages.data.repository.ContactItem
import com.dpouya.messages.features.contacts.ContactsScreen
import com.dpouya.messages.features.inbox.InboxScreen
import com.dpouya.messages.features.inbox.InboxViewModel

@Composable
fun HomeScreen(
    container: AppContainer,
    onOpenConversation: (Long) -> Unit,
    onCompose: () -> Unit,
    onComposeContact: (ContactItem) -> Unit,
    onSettings: () -> Unit,
) {
    val colors = MessagesThemeTokens.colors
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val inboxVm: InboxViewModel = viewModel(factory = InboxViewModel.Factory(container.smsRepository))

    Scaffold(
        containerColor = colors.mainBackground,
        bottomBar = {
            NavigationBar(
                containerColor = colors.mainBackground,
                tonalElevation = 0.dp,
            ) {
                NavigationBarItem(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    icon = { Icon(Icons.Outlined.ChatBubbleOutline, "Messages", modifier = Modifier.size(22.dp)) },
                    label = { Text("Messages", style = MessagesTypography.navLabel) },
                    colors = navColors(colors.accent, colors.mutedText),
                )
                NavigationBarItem(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    icon = { Icon(Icons.Outlined.PeopleOutline, "Contacts", modifier = Modifier.size(22.dp)) },
                    label = { Text("Contacts", style = MessagesTypography.navLabel) },
                    colors = navColors(colors.accent, colors.mutedText),
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onSettings,
                    icon = { Icon(Icons.Outlined.Settings, "Settings", modifier = Modifier.size(22.dp)) },
                    label = { Text("Settings", style = MessagesTypography.navLabel) },
                    colors = navColors(colors.accent, colors.mutedText),
                )
            }
        },
    ) { padding ->
        when (tab) {
            0 -> InboxScreen(
                viewModel = inboxVm,
                onOpenConversation = onOpenConversation,
                onCompose = onCompose,
                onOpenSettings = onSettings,
                contentPadding = padding,
            )
            else -> ContactsScreen(
                container = container,
                contentPadding = padding,
                onOpenComposeWith = onComposeContact,
            )
        }
    }
}

@Composable
private fun navColors(selected: Color, unselected: Color) = NavigationBarItemDefaults.colors(
    selectedIconColor = selected,
    selectedTextColor = selected,
    unselectedIconColor = unselected,
    unselectedTextColor = unselected,
    indicatorColor = Color.Transparent,
)
