package com.zammy.app.presentation.editticket

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zammy.app.R
import com.zammy.app.ui.components.TagChip
import com.zammy.app.ui.components.statusColor
import com.zammy.app.ui.components.formatStatus

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditTicketScreen(
    ticketId: Int,
    onNavigateBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: EditTicketViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(ticketId) { viewModel.loadTicket(ticketId) }

    LaunchedEffect(uiState.isDone) {
        if (uiState.isDone) onSaved()
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
                title = { Text(stringResource(R.string.edit_ticket_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Button(
                            onClick = viewModel::saveChanges,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(stringResource(R.string.edit_ticket_save))
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Subject
            SectionLabel(stringResource(R.string.edit_ticket_subject))
            OutlinedTextField(
                value = uiState.title,
                onValueChange = viewModel::onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Status
            SectionLabel(stringResource(R.string.edit_ticket_status))
            val statuses = listOf("new", "open", "pending reminder", "pending close", "closed")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                statuses.forEach { state ->
                    StatusButton(
                        label = formatStatus(state),
                        color = statusColor(state),
                        selected = uiState.selectedState == state,
                        onClick = { viewModel.onStateSelected(state) }
                    )
                }
            }

            // Pending time — only for pending states
            if (uiState.selectedState.contains("pending", ignoreCase = true)) {
                SectionLabel(stringResource(R.string.edit_ticket_pending_time))
                OutlinedTextField(
                    value = uiState.pendingTime,
                    onValueChange = viewModel::onPendingTimeChange,
                    label = { Text("YYYY-MM-DDThh:mm:ss.000Z") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Priority
            SectionLabel(stringResource(R.string.edit_ticket_priority))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(1 to "Low", 2 to "Normal", 3 to "High", 4 to "Urgent").forEach { (id, label) ->
                    val selected = uiState.selectedPriorityId == id
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { viewModel.onPrioritySelected(id) }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selected) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Group
            if (uiState.groups.isNotEmpty()) {
                SectionLabel(stringResource(R.string.edit_ticket_group))
                var groupExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = groupExpanded,
                    onExpandedChange = { groupExpanded = it }
                ) {
                    OutlinedTextField(
                        value = uiState.groups.find { it.id == uiState.selectedGroupId }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = groupExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = groupExpanded,
                        onDismissRequest = { groupExpanded = false }
                    ) {
                        uiState.groups.forEach { group ->
                            DropdownMenuItem(
                                text = { Text(group.name) },
                                onClick = {
                                    viewModel.onGroupSelected(group.id)
                                    groupExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Owner
            if (uiState.agents.isNotEmpty()) {
                SectionLabel(stringResource(R.string.edit_ticket_owner))
                var ownerExpanded by remember { mutableStateOf(false) }
                val ownerName = uiState.agents.find { it.id == uiState.selectedOwnerId }
                    ?.let { "${it.firstname} ${it.lastname}".trim() }
                    ?: stringResource(R.string.edit_ticket_no_owner)
                ExposedDropdownMenuBox(
                    expanded = ownerExpanded,
                    onExpandedChange = { ownerExpanded = it }
                ) {
                    OutlinedTextField(
                        value = ownerName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = ownerExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = ownerExpanded,
                        onDismissRequest = { ownerExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.edit_ticket_no_owner)) },
                            onClick = {
                                viewModel.onOwnerSelected(null)
                                ownerExpanded = false
                            }
                        )
                        uiState.agents.forEach { agent ->
                            DropdownMenuItem(
                                text = { Text("${agent.firstname} ${agent.lastname}".trim()) },
                                onClick = {
                                    viewModel.onOwnerSelected(agent.id)
                                    ownerExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Tags
            SectionLabel(stringResource(R.string.edit_ticket_tags))
            if (uiState.tags.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.tags.forEach { tag ->
                        TagChip(tag = tag, onRemove = { viewModel.removeTag(tag) })
                    }
                }
            }

            // Tag input
            var tagExpanded by remember { mutableStateOf(false) }
            val filteredTags = uiState.availableTags.filter {
                it.contains(uiState.tagInput, ignoreCase = true) && !uiState.tags.contains(it)
            }
            ExposedDropdownMenuBox(
                expanded = tagExpanded && filteredTags.isNotEmpty(),
                onExpandedChange = { tagExpanded = it }
            ) {
                OutlinedTextField(
                    value = uiState.tagInput,
                    onValueChange = {
                        viewModel.onTagInputChange(it)
                        tagExpanded = it.isNotEmpty()
                    },
                    label = { Text(stringResource(R.string.edit_ticket_add_tag)) },
                    trailingIcon = {
                        IconButton(onClick = { viewModel.addTag(uiState.tagInput) }) {
                            Icon(Icons.Default.Add, contentDescription = "Add tag")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = tagExpanded && filteredTags.isNotEmpty(),
                    onDismissRequest = { tagExpanded = false }
                ) {
                    filteredTags.take(8).forEach { suggestion ->
                        DropdownMenuItem(
                            text = { Text(suggestion) },
                            onClick = {
                                viewModel.addTag(suggestion)
                                tagExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun StatusButton(
    label: String,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) color else color.copy(alpha = 0.12f))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) Color.White else color,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
