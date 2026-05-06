package com.zammy.app.domain.model

data class DisplaySettings(
    val themeMode: String = "SYSTEM",   // LIGHT | DARK | SYSTEM
    val accentColorHex: String = "#4F8EF7",
    val listLayout: String = "cards",        // cards | rows | compact
    val sortBy: String = "updated",          // updated | created | priority | id
    val density: String = "regular",         // compact | regular | comfy
    val showAvatars: Boolean = true,
    val showTags: Boolean = true,
    val showPriority: Boolean = true,
    val showTicketId: Boolean = true,
    val boldUnread: Boolean = true,
    val highlightEscalated: Boolean = true,
    val bubbleStyle: String = "chat",        // rounded | chat | square
    val showTimestamps: Boolean = true,
    val showCustomerAvatar: Boolean = true,
    val showInternalBadge: Boolean = true,
    val language: String = "system",         // system | en | de
)
