package com.zammy.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zammy.app.ui.theme.ZammyColors

// ─── Avatar ───────────────────────────────────────────────────────────────────

private val avatarPalette = listOf(
    Color(0xFF4F8EF7), Color(0xFF22C55E), Color(0xFFF59E0B),
    Color(0xFFEF4444), Color(0xFFA78BFA), Color(0xFF06B6D4), Color(0xFFF97316)
)

@Composable
fun Avatar(
    name: String,
    size: Dp = 34.dp,
    color: Color? = null,
    modifier: Modifier = Modifier
) {
    val bg = color ?: run {
        val hash = name.fold(0) { acc, c -> acc + c.code }
        avatarPalette[hash.and(0x7FFFFFFF) % avatarPalette.size]
    }
    val initials = name.trim().split(" ")
        .filter { it.isNotEmpty() }
        .take(2)
        .joinToString("") { it[0].uppercaseChar().toString() }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = Color.White,
            fontSize = (size.value * 0.36f).sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.02).sp
        )
    }
}

// ─── StatusBadge ─────────────────────────────────────────────────────────────

@Composable
fun StatusBadge(status: String, modifier: Modifier = Modifier, small: Boolean = false) {
    val color = statusColor(status)
    Row(
        modifier = modifier
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
            .padding(horizontal = if (small) 6.dp else 8.dp, vertical = if (small) 1.dp else 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = formatStatus(status).uppercase(),
            color = color,
            fontSize = if (small) 10.sp else 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.03.sp
        )
    }
}

// ─── PriorityBadge ───────────────────────────────────────────────────────────

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

// ─── PriorityDot (square, matching design) ───────────────────────────────────

@Composable
fun PriorityDot(priority: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(8.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(priorityColor(priority))
    )
}

// ─── TagChip ─────────────────────────────────────────────────────────────────

@Composable
fun TagChip(
    tag: String,
    onRemove: (() -> Unit)? = null,
    pendingRemove: Boolean = false,
    modifier: Modifier = Modifier
) {
    val bg = when {
        pendingRemove -> ZammyColors.StatusEscalated.copy(alpha = 0.13f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = when {
        pendingRemove -> ZammyColors.StatusEscalated
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val borderColor = when {
        pendingRemove -> ZammyColors.StatusEscalated.copy(alpha = 0.4f)
        else -> Color.Transparent
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(5.dp))
            .background(bg)
            .border(1.dp, borderColor, RoundedCornerShape(5.dp))
            .then(if (onRemove != null) Modifier.clickable { onRemove() } else Modifier)
            .padding(horizontal = 7.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = tag,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = textColor,
            letterSpacing = 0.02.sp
        )
        if (onRemove != null) {
            Spacer(Modifier.width(3.dp))
            Text(
                text = if (pendingRemove) "✕" else "×",
                fontSize = 10.sp,
                color = textColor,
            )
        }
    }
}

// ─── Pure helpers ─────────────────────────────────────────────────────────────

fun statusColor(status: String): Color = when (status.lowercase().trim()) {
    "new"              -> ZammyColors.StatusNew
    "open"             -> ZammyColors.StatusOpen
    "pending reminder",
    "pending close",
    "pending"          -> ZammyColors.StatusPending
    "closed"           -> ZammyColors.StatusClosed
    "merged"           -> ZammyColors.StatusMerged
    "escalated"        -> ZammyColors.StatusEscalated
    else               -> ZammyColors.StatusEscalated
}

fun priorityColor(priority: String): Color = when {
    priority.contains("low",    ignoreCase = true) -> ZammyColors.PriorityLow
    priority.contains("high",   ignoreCase = true) -> ZammyColors.PriorityHigh
    priority.contains("urgent", ignoreCase = true) -> ZammyColors.PriorityUrgent
    priority.contains("medium", ignoreCase = true) -> ZammyColors.PriorityHigh
    else                                            -> ZammyColors.PriorityNormal
}

fun formatStatus(status: String): String = when (status.lowercase().trim()) {
    "new"              -> "New"
    "open"             -> "Open"
    "pending reminder" -> "Pending"
    "pending close"    -> "Pending Close"
    "closed"           -> "Closed"
    "merged"           -> "Merged"
    "escalated"        -> "Escalated"
    else               -> status.replaceFirstChar { it.uppercase() }
}

fun formatPriority(priority: String): String = when {
    priority.contains("low",    ignoreCase = true) -> "Low"
    priority.contains("high",   ignoreCase = true) -> "High"
    priority.contains("urgent", ignoreCase = true) -> "Urgent"
    priority.contains("medium", ignoreCase = true) -> "Medium"
    else                                            -> "Normal"
}

fun priorityIdFromString(priority: String): Int = when {
    priority.contains("low",    ignoreCase = true) -> 1
    priority.contains("high",   ignoreCase = true) -> 3
    priority.contains("urgent", ignoreCase = true) -> 4
    else                                            -> 2
}

fun priorityFromId(id: Int): String = when (id) {
    1    -> "low"
    3    -> "high"
    4    -> "urgent"
    else -> "normal"
}
