package com.dpouya.messages.core.navigation

object Routes {
    const val Splash = "splash"
    const val Home = "home"
    const val Conversation = "conversation/{id}"
    const val Compose = "compose"
    const val ContactDetails = "contact/{id}"
    const val Settings = "settings"
    const val About = "about"
    const val Notifications = "notifications"
    const val Privacy = "privacy"
    const val Blocked = "blocked"

    fun conversation(id: Long) = "conversation/$id"
    fun contact(id: Long) = "contact/$id"
}
