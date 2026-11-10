package com.dpouya.messages.core.navigation

import android.Manifest
import android.app.role.RoleManager
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dpouya.messages.core.di.AppContainer
import com.dpouya.messages.features.about.AboutScreen
import com.dpouya.messages.features.compose.ComposeMessageScreen
import com.dpouya.messages.features.contact.ContactDetailsScreen
import com.dpouya.messages.features.conversation.ConversationScreen
import com.dpouya.messages.features.conversation.ConversationViewModel
import com.dpouya.messages.features.home.HomeScreen
import com.dpouya.messages.features.settings.SettingsScreen
import com.dpouya.messages.features.settings.SimplePlaceholderScreen
import com.dpouya.messages.features.splash.SplashScreen

@Composable
fun MessagesNavHost(container: AppContainer) {
    val nav = rememberNavController()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { }
    val roleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { }

    LaunchedEffect(Unit) {
        val perms = mutableListOf(
            Manifest.permission.READ_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_CONTACTS,
        )
        if (Build.VERSION.SDK_INT >= 33) perms += Manifest.permission.POST_NOTIFICATIONS
        permissionLauncher.launch(perms.toTypedArray())

        if (Telephony.Sms.getDefaultSmsPackage(context) != context.packageName) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val rm = context.getSystemService(RoleManager::class.java)
                if (rm != null && rm.isRoleAvailable(RoleManager.ROLE_SMS) && !rm.isRoleHeld(RoleManager.ROLE_SMS)) {
                    roleLauncher.launch(rm.createRequestRoleIntent(RoleManager.ROLE_SMS))
                }
            } else {
                roleLauncher.launch(
                    Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
                        .putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, context.packageName),
                )
            }
        }
    }

    NavHost(
        navController = nav,
        startDestination = Routes.Splash,
        modifier = Modifier.fillMaxSize(),
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() },
    ) {
        composable(Routes.Splash) {
            SplashScreen {
                nav.navigate(Routes.Home) {
                    popUpTo(Routes.Splash) { inclusive = true }
                }
            }
        }
        composable(Routes.Home) {
            HomeScreen(
                container = container,
                onOpenConversation = { id ->
                    nav.navigate(Routes.conversation(id))
                },
                onCompose = { nav.navigate(Routes.Compose) },
                onComposeContact = { contact ->
                    // open compose preselected via query not needed; go compose
                    nav.navigate(Routes.Compose)
                },
                onSettings = { nav.navigate(Routes.Settings) },
            )
        }
        composable(
            Routes.Conversation,
            arguments = listOf(navArgument("id") { type = NavType.LongType }),
        ) { entry ->
            val id = entry.arguments?.getLong("id") ?: return@composable
            val vm: ConversationViewModel = viewModel(
                factory = ConversationViewModel.Factory(id, container.smsRepository),
            )
            ConversationScreen(
                viewModel = vm,
                onBack = { nav.popBackStack() },
                onOpenContact = { cid -> nav.navigate(Routes.contact(cid)) },
            )
        }
        composable(Routes.Compose) {
            ComposeMessageScreen(
                container = container,
                onBack = { nav.popBackStack() },
                onOpenedConversation = { id ->
                    nav.navigate(Routes.conversation(id)) {
                        popUpTo(Routes.Home)
                    }
                },
            )
        }
        composable(
            Routes.ContactDetails,
            arguments = listOf(navArgument("id") { type = NavType.LongType }),
        ) { entry ->
            val id = entry.arguments?.getLong("id") ?: return@composable
            ContactDetailsScreen(
                conversationId = id,
                smsRepository = container.smsRepository,
                onBack = { nav.popBackStack() },
                onMessage = { cid ->
                    nav.navigate(Routes.conversation(cid)) {
                        popUpTo(Routes.Home)
                    }
                },
            )
        }
        composable(Routes.Settings) {
            SettingsScreen(
                themePreferences = container.themePreferences,
                onBack = { nav.popBackStack() },
                onAbout = { nav.navigate(Routes.About) },
                onNotifications = { nav.navigate(Routes.Notifications) },
                onPrivacy = { nav.navigate(Routes.Privacy) },
                onBlocked = { nav.navigate(Routes.Blocked) },
            )
        }
        composable(Routes.About) {
            AboutScreen(onBack = { nav.popBackStack() })
        }
        composable(Routes.Notifications) {
            SimplePlaceholderScreen(
                title = "Notifications",
                body = "Notification preferences will appear here.",
                onBack = { nav.popBackStack() },
            )
        }
        composable(Routes.Privacy) {
            SimplePlaceholderScreen(
                title = "Privacy & Security",
                body = "Privacy controls will appear here.",
                onBack = { nav.popBackStack() },
            )
        }
        composable(Routes.Blocked) {
            SimplePlaceholderScreen(
                title = "Blocked Contacts",
                body = "Blocked numbers will appear here.",
                onBack = { nav.popBackStack() },
            )
        }
    }
}
