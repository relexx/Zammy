package com.zammy.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zammy.app.ui.theme.ZammyColors

@Composable
fun StatusBadge(status: String, modifier: Modifier = Modifier) {
    val color = statusColor(status)
    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = formatStatus(status),
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun PriorityBadge(priority: String, modifier: Modifier = Modifier) {
    val color = priorityColor(priority)
    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = formatPriority(priority),
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun PriorityDot(priority: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(8.dp)
            .background(priorityColor(priority), CircleShape)
    )
}

@Composable
fun TagChip(
    tag: String,
    onRemove: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (onRemove != null) {
        InputChip(
            selected = false,
            onClick = { onRemove() },
            label = { Text(tag, fontSize = 12.sp) },
            trailingIcon = {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove $tag",
                    modifier = Modifier.size(14.dp)
                )
            },
            modifier = modifier,
            colors = InputChipDefaults.inputChipColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                labelColor = MaterialTheme.colorScheme.onSurface
            ),
            border = InputChipDefaults.inputChipBorder(
                enabled = true,
                selected = false,
                borderColor = MaterialTheme.colorScheme.outline
            )
        )
    } else {
        Box(
            modifier = modifier
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 10.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = tag,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

fun statusColor(status: String): Color = when (status.lowercase().trim()) {
    "new", "open" -> ZammyColors.StatusOpen
    "pending reminder", "pending close" -> ZammyColors.StatusPending
    "closed" -> ZammyColors.StatusClosed
    "merged" -> ZammyColors.StatusMerged
    else -> ZammyColors.StatusEscalated
}

fun priorityColor(priority: String): Color = when {
    priority.contains("low", ignoreCase = true) -> ZammyColors.PriorityLow
    priority.contains("high", ignoreCase = true) -> ZammyColors.PriorityHigh
    priority.contains("urgent", ignoreCase = true) -> ZammyColors.PriorityUrgent
    else -> ZammyColors.PriorityNormal
}

fun formatStatus(status: String): String = when (status.lowercase().trim()) {
    "new" -> "New"
    "open" -> "Open"
    "pending reminder" -> "Pending"
    "pending close" -> "Pending Close"
    "closed" -> "Closed"
    "merged" -> "Merged"
    else -> status.replaceFirstChar { it.uppercase() }
}

fun formatPriority(priority: String): String = when {
    priority.contains("low", ignoreCase = true) -> "Low"
    priority.contains("high", ignoreCase = true) -> "High"
    priority.contains("urgent", ignoreCase = true) -> "Urgent"
    else -> "Normal"
}

fun priorityIdFromString(priority: String): Int = when {
    priority.contains("low", ignoreCase = true) -> 1
    priority.contains("high", ignoreCase = true) -> 3
    priority.contains("urgent", ignoreCase = true) -> 4
    else -> 2
}
