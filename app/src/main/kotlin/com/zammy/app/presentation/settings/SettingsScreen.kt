package com.zammy.app.presentation.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zammy.app.ui.components.Avatar
import com.zammy.app.ui.theme.ThemeMode
import com.zammy.app.ui.theme.ZammyColors

private val accentPresets = listOf(
    "#4F8EF7", "#A78BFA", "#22C55E", "#EF4444",
    "#F59E0B", "#06B6D4", "#F97316", "#EC4899"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.savedMessage) {
        uiState.savedMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Profile card ─────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Avatar(name = uiState.username.ifBlank { "?" }, size = 44.dp, color = MaterialTheme.colorScheme.primary)
                Column(modifier = Modifier.weight(1f)) {
                    Text(uiState.username.ifBlank { "—" }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(uiState.serverUrl.ifBlank { "—" }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(ZammyColors.StatusOpen.copy(alpha = 0.12f))
                        .border(1.dp, ZammyColors.StatusOpen.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("Online", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ZammyColors.StatusOpen)
                }
            }

            // ── Sections ─────────────────────────────────────────────────────
            val sections = listOf(
                Triple("account",       "Account",       Color(0xFF4F8EF7)),
                Triple("appearance",    "Appearance",    Color(0xFFA78BFA)),
                Triple("layout",        "Layout",        Color(0xFF06B6D4)),
                Triple("chat",          "Chat",          Color(0xFF22C55E)),
                Triple("notifications", "Notifications", Color(0xFFF59E0B)),
                Triple("language",      "Language",      Color(0xFF22C55E)),
                Triple("about",         "About",         MaterialTheme.colorScheme.onSurfaceVariant),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
            ) {
                sections.forEachIndexed { index, (key, label, color) ->
                    val expanded = uiState.expandedSection == key
                    val chevronAngle by animateFloatAsState(if (expanded) 90f else 0f, label = "chevron")

                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.toggleSection(key) }
                                .padding(horizontal = 16.dp, vertical = 13.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(color.copy(alpha = 0.13f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (key) {
                                        "account"       -> "👤"
                                        "appearance"    -> "🌙"
                                        "layout"        -> "📋"
                                        "chat"          -> "💬"
                                        "notifications" -> "🔔"
                                        "language"      -> "🌍"
                                        else            -> "ℹ️"
                                    },
                                    fontSize = 16.sp
                                )
                            }
                            Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
                            Text("›", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.rotate(chevronAngle))
                        }

                        AnimatedVisibility(
                            visible = expanded,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                when (key) {
                                    "account"       -> AccountSection(uiState, viewModel, onLogout)
                                    "appearance"    -> AppearanceSection(uiState, viewModel)
                                    "layout"        -> LayoutSection(uiState, viewModel)
                                    "chat"          -> ChatSection(uiState, viewModel)
                                    "notifications" -> NotificationsSection(uiState, viewModel)
                                    "language"      -> LanguageSection(uiState, viewModel)
                                    "about"         -> AboutSection()
                                }
                            }
                        }

                        if (index < sections.size - 1 && !expanded) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Section composables ──────────────────────────────────────────────────────

@Composable
private fun AccountSection(state: SettingsUiState, vm: SettingsViewModel, onLogout: () -> Unit) {
    Text("Logged in as", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Text(state.username, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(2.dp))
    Text("Server", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Text(state.serverUrl, style = MaterialTheme.typography.labelMedium)
    Spacer(Modifier.height(4.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(ZammyColors.StatusEscalated.copy(alpha = 0.1f))
            .border(1.dp, ZammyColors.StatusEscalated.copy(alpha = 0.28f), RoundedCornerShape(8.dp))
            .clickable { vm.logout(); onLogout() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("Sign Out", fontWeight = FontWeight.SemiBold, color = ZammyColors.StatusEscalated, fontSize = 13.sp)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AppearanceSection(state: SettingsUiState, vm: SettingsViewModel) {
    SettingLabel("Theme")
    SegmentedControl(
        options = listOf("LIGHT" to "Light", "DARK" to "Dark", "SYSTEM" to "System"),
        selected = state.display.themeMode,
        onSelect = { vm.setThemeMode(ThemeMode.valueOf(it)) }
    )

    Spacer(Modifier.height(4.dp))
    SettingLabel("Accent Color")
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        accentPresets.forEach { hex ->
            val col = parseHexColor(hex)
            val selected = state.display.accentColorHex.uppercase() == hex.uppercase()
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(col)
                    .then(if (selected) Modifier.border(2.dp, Color.White, RoundedCornerShape(7.dp)) else Modifier)
                    .clickable { vm.setAccentColor(hex) }
            )
        }
    }
}

@Composable
private fun LayoutSection(state: SettingsUiState, vm: SettingsViewModel) {
    SettingLabel("List Layout")
    SegmentedControl(
        options = listOf("cards" to "Cards", "rows" to "Rows", "compact" to "Compact"),
        selected = state.display.listLayout,
        onSelect = vm::setListLayout
    )
    Spacer(Modifier.height(4.dp))
    SettingLabel("Sort By")
    SegmentedControl(
        options = listOf("updated" to "Updated", "created" to "Created", "priority" to "Priority", "id" to "ID"),
        selected = state.display.sortBy,
        onSelect = vm::setSortBy
    )
    Spacer(Modifier.height(4.dp))
    SettingLabel("Density")
    SegmentedControl(
        options = listOf("compact" to "Compact", "regular" to "Regular", "comfy" to "Comfy"),
        selected = state.display.density,
        onSelect = vm::setDensity
    )
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    SettingToggleRow("Show Avatars",          state.display.showAvatars,         vm::setShowAvatars)
    SettingToggleRow("Show Tags",             state.display.showTags,            vm::setShowTags)
    SettingToggleRow("Show Priority",         state.display.showPriority,        vm::setShowPriority)
    SettingToggleRow("Show Ticket ID",        state.display.showTicketId,        vm::setShowTicketId)
    SettingToggleRow("Bold Unread",           state.display.boldUnread,          vm::setBoldUnread)
    SettingToggleRow("Highlight Escalated",   state.display.highlightEscalated,  vm::setHighlightEscalated)
}

@Composable
private fun ChatSection(state: SettingsUiState, vm: SettingsViewModel) {
    SettingLabel("Bubble Style")
    SegmentedControl(
        options = listOf("rounded" to "Pill", "chat" to "Chat", "square" to "Box"),
        selected = state.display.bubbleStyle,
        onSelect = vm::setBubbleStyle
    )
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    SettingToggleRow("Show Timestamps",     state.display.showTimestamps,     vm::setShowTimestamps)
    SettingToggleRow("Customer Avatars",    state.display.showCustomerAvatar, vm::setShowCustomerAvatar)
    SettingToggleRow("Internal Badge",      state.display.showInternalBadge,  vm::setShowInternalBadge)
}

@Composable
private fun NotificationsSection(state: SettingsUiState, vm: SettingsViewModel) {
    SettingToggleRow("Push Notifications", state.notificationIntervalMinutes > 0) {
        vm.onNotificationIntervalChange(if (it) 30 else 0)
        vm.saveServerSettings()
    }
    SettingToggleRow("Notification Badge", state.notificationIntervalMinutes > 0) { }
    Spacer(Modifier.height(4.dp))
    SettingLabel("Sync interval")
    SegmentedControl(
        options = listOf("15" to "15m", "30" to "30m", "60" to "60m"),
        selected = state.notificationIntervalMinutes.toString(),
        onSelect = { vm.onNotificationIntervalChange(it.toIntOrNull() ?: 30); vm.saveServerSettings() }
    )
}

@Composable
private fun LanguageSection(state: SettingsUiState, vm: SettingsViewModel) {
    SegmentedControl(
        options = listOf("system" to "System", "en" to "English 🇬🇧", "de" to "Deutsch 🇩🇪"),
        selected = state.display.language,
        onSelect = vm::setLanguage
    )
    Text(
        "Changing the language restarts the app.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun AboutSection() {
    Text("Zammy v1.1.0", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    Text("Zammad Mobile Agent", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(2.dp))
    Text("Open-source · MIT License", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
}

// ── Reusable controls ────────────────────────────────────────────────────────

@Composable
private fun SettingLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.03.sp
    )
}

@Composable
private fun SegmentedControl(
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.06f))
            .padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        options.forEach { (value, label) ->
            val active = selected == value
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (active) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onSelect(value) }
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SettingToggleRow(label: String, value: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Switch(
            checked = value,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MaterialTheme.colorScheme.primary)
        )
    }
}

// ── Color parsing ────────────────────────────────────────────────────────────

private fun parseHexColor(hex: String): Color {
    return try {
        val clean = hex.trimStart('#')
        Color(android.graphics.Color.parseColor("#$clean"))
    } catch (e: Exception) {
        Color(0xFF4F8EF7)
    }
}
