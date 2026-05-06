package com.zammy.app.presentation.tickets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zammy.app.R
import com.zammy.app.domain.model.DisplaySettings
import com.zammy.app.domain.model.Ticket
import com.zammy.app.ui.components.Avatar
import com.zammy.app.ui.components.PriorityDot
import com.zammy.app.ui.components.StatusBadge
import com.zammy.app.ui.components.statusColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketsScreen(
    onTicketClick: (Int) -> Unit,
    onCreateTicket: () -> Unit,
    onSettings: () -> Unit,
    viewModel: TicketsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Refresh display settings when screen resumes (user may have changed settings)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshDisplaySettings()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.tickets_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Aktualisieren")
                    }
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Einstellungen")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateTicket,
                containerColor = MaterialTheme.colorScheme.primary,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Ticket erstellen",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            // Search field
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                placeholder = { Text(stringResource(R.string.tickets_search_hint)) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Löschen", modifier = Modifier.size(18.dp))
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            if (uiState.searchResults != null) {
                TicketList(
                    tickets = uiState.searchResults.orEmpty(),
                    display = uiState.display,
                    isRefreshing = false,
                    onRefresh = {},
                    onTicketClick = onTicketClick
                )
            } else {
                val tabs = listOf(
                    stringResource(R.string.tickets_tab_open),
                    stringResource(R.string.tickets_tab_pending),
                    stringResource(R.string.tickets_tab_closed)
                )
                ScrollableTabRow(
                    selectedTabIndex = uiState.selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    edgePadding = 16.dp
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = uiState.selectedTab == index,
                            onClick = { viewModel.onTabSelected(index) },
                            text = {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (uiState.selectedTab == index)
                                        FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }

                val tickets = when (uiState.selectedTab) {
                    0 -> uiState.openTickets
                    1 -> uiState.pendingTickets
                    2 -> uiState.closedTickets
                    else -> emptyList()
                }

                TicketList(
                    tickets = tickets,
                    display = uiState.display,
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = viewModel::refresh,
                    onTicketClick = onTicketClick
                )
            }
        }
    }
}

// ── Ticket list with pull-to-refresh ─────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TicketList(
    tickets: List<Ticket>,
    display: DisplaySettings,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onTicketClick: (Int) -> Unit
) {
    val pullState = rememberPullToRefreshState()

    LaunchedEffect(pullState.isRefreshing) {
        if (pullState.isRefreshing) onRefresh()
    }
    LaunchedEffect(isRefreshing) {
        if (!isRefreshing) pullState.endRefresh()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(pullState.nestedScrollConnection)
    ) {
        if (tickets.isEmpty() && !isRefreshing) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.tickets_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                item { Spacer(Modifier.height(4.dp)) }
                items(tickets, key = { it.id }) { ticket ->
                    when (display.listLayout) {
                        "rows"    -> TicketRowItem(ticket, display, onClick = { onTicketClick(ticket.id) })
                        "compact" -> TicketCompactItem(ticket, display, onClick = { onTicketClick(ticket.id) })
                        else      -> TicketCardItem(ticket, display, onClick = { onTicketClick(ticket.id) })
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }

        PullToRefreshContainer(
            state = pullState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

// ── Cards layout ─────────────────────────────────────────────────────────────

@Composable
private fun TicketCardItem(
    ticket: Ticket,
    display: DisplaySettings,
    onClick: () -> Unit
) {
    val isEscalated = ticket.state.equals("escalated", ignoreCase = true)
    val escalateHighlight = display.highlightEscalated && isEscalated

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp)
            .then(
                if (escalateHighlight)
                    Modifier.border(1.dp, com.zammy.app.ui.theme.ZammyColors.StatusEscalated.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                else Modifier
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (escalateHighlight)
                com.zammy.app.ui.theme.ZammyColors.StatusEscalated.copy(alpha = 0.04f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Avatar or priority dot
            if (display.showAvatars) {
                Avatar(
                    name = ticket.customerName ?: ticket.group,
                    size = 36.dp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            } else {
                PriorityDot(
                    priority = ticket.priority,
                    modifier = Modifier
                        .padding(top = 7.dp)
                        .size(8.dp)
                )
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Top row: ticket id + status + time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (display.showTicketId) {
                            Text(
                                text = "#${ticket.number}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        StatusBadge(ticket.state, small = true)
                    }
                    Text(
                        text = formatTicketTime(ticket.updatedAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }

                // Title
                Text(
                    text = ticket.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = if (display.boldUnread) FontWeight.SemiBold else FontWeight.Normal
                )

                // Subtitle: group · customer
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (display.showPriority) {
                        PriorityDot(priority = ticket.priority, modifier = Modifier.size(7.dp))
                    }
                    Text(
                        text = ticket.group,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                    ticket.customerName?.let { name ->
                        Text("·", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

// ── Rows layout ───────────────────────────────────────────────────────────────

@Composable
private fun TicketRowItem(
    ticket: Ticket,
    display: DisplaySettings,
    onClick: () -> Unit
) {
    val statusCol = statusColor(ticket.state)
    val isEscalated = ticket.state.equals("escalated", ignoreCase = true)
    val bgColor = if (display.highlightEscalated && isEscalated)
        com.zammy.app.ui.theme.ZammyColors.StatusEscalated.copy(alpha = 0.04f)
    else MaterialTheme.colorScheme.surface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(vertical = 1.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left accent bar
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(56.dp)
                .background(statusCol)
        )

        Row(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (display.showAvatars) {
                Avatar(
                    name = ticket.customerName ?: ticket.group,
                    size = 32.dp
                )
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = ticket.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = if (display.boldUnread) FontWeight.SemiBold else FontWeight.Normal,
                    fontSize = 13.sp
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (display.showTicketId) {
                        Text(
                            text = "#${ticket.number}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 10.sp
                        )
                        Text("·", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(
                        text = ticket.group,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                StatusBadge(ticket.state, small = true)
                Text(
                    text = formatTicketTime(ticket.updatedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.5.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}

// ── Compact layout ────────────────────────────────────────────────────────────

@Composable
private fun TicketCompactItem(
    ticket: Ticket,
    display: DisplaySettings,
    onClick: () -> Unit
) {
    val isEscalated = ticket.state.equals("escalated", ignoreCase = true)
    val bgColor = if (display.highlightEscalated && isEscalated)
        com.zammy.app.ui.theme.ZammyColors.StatusEscalated.copy(alpha = 0.05f)
    else MaterialTheme.colorScheme.background

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (display.showPriority) {
            PriorityDot(priority = ticket.priority, modifier = Modifier.size(7.dp))
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                text = ticket.title,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = if (display.boldUnread) FontWeight.SemiBold else FontWeight.Normal,
                fontSize = 12.sp
            )
            if (display.showTicketId) {
                Text(
                    text = "#${ticket.number} · ${ticket.group}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            }
        }

        StatusBadge(ticket.state, small = true)
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp)
            .height(0.5.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}

// ── Time formatter ────────────────────────────────────────────────────────────

private fun formatTicketTime(dateStr: String): String {
    val formats = listOf("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd'T'HH:mm:ss'Z'")
    for (fmt in formats) {
        runCatching {
            val sdf = SimpleDateFormat(fmt, Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val date = sdf.parse(dateStr) ?: return@runCatching
            val now = Date()
            val diffMs = now.time - date.time
            val diffMin = diffMs / 60_000
            return when {
                diffMin < 1    -> "jetzt"
                diffMin < 60   -> "${diffMin}m"
                diffMin < 1440 -> "${diffMin / 60}h"
                diffMin < 10080 -> "${diffMin / 1440}d"
                else -> SimpleDateFormat("dd.MM.", Locale.getDefault()).format(date)
            }
        }
    }
    return ""
}
