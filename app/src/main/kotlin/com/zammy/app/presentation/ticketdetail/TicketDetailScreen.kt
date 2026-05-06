package com.zammy.app.presentation.ticketdetail

import android.content.Intent
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zammy.app.R
import com.zammy.app.domain.model.Article
import com.zammy.app.domain.model.DisplaySettings
import com.zammy.app.domain.model.Ticket
import com.zammy.app.ui.components.Avatar
import com.zammy.app.ui.components.PriorityBadge
import com.zammy.app.ui.components.StatusBadge
import com.zammy.app.ui.components.TagChip
import com.zammy.app.ui.theme.ZammyColors
import com.zammy.app.util.MAX_ATTACHMENT_BYTES
import com.zammy.app.util.getFilenameFromUri
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TicketDetailScreen(
    ticketId: Int,
    onNavigateBack: () -> Unit,
    onEditTicket: (Int) -> Unit,
    viewModel: TicketDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val listState = rememberLazyListState()

    LaunchedEffect(ticketId) { viewModel.loadTicket(ticketId) }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.articles.size) {
        if (uiState.articles.isNotEmpty()) {
            listState.animateScrollToItem(uiState.articles.size - 1)
        }
    }

    val fileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        val files = uris.mapNotNull { uri ->
            runCatching {
                val filename = getFilenameFromUri(context, uri) ?: return@mapNotNull null
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: return@mapNotNull null
                if (bytes.size > MAX_ATTACHMENT_BYTES) return@mapNotNull null
                Pair(filename, bytes)
            }.getOrNull()
        }
        viewModel.onReplyAttachmentsChange(files)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    uiState.ticket?.let { ticket ->
                        Text(
                            text = "#${ticket.number}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    uiState.ticket?.let {
                        IconButton(onClick = { onEditTicket(ticketId) }) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit ticket",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@Scaffold
            }

            uiState.ticket?.let { ticket ->
                TicketHeaderCard(
                    ticket = ticket,
                    tags = uiState.tags,
                    availableTags = uiState.availableTags,
                    tagInput = uiState.tagInput,
                    pendingRemoveTag = uiState.pendingRemoveTag,
                    onTagInputChange = viewModel::onTagInputChange,
                    onAddTag = { viewModel.addTag(ticketId, it) },
                    onRemoveTag = { viewModel.removeTag(ticketId, it) },
                    onSetPendingRemoveTag = viewModel::setPendingRemoveTag
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }
                val displaySettings = uiState.display
                items(uiState.articles, key = { it.id }) { article ->
                    ArticleBubble(
                        article = article,
                        displaySettings = displaySettings,
                        onToggleAvatar = viewModel::toggleShowAvatars,
                        onCycleBubbleStyle = {
                            val styles = listOf("chat", "rounded", "square")
                            val idx = (styles.indexOf(displaySettings.bubbleStyle) + 1) % styles.size
                            viewModel.setBubbleStyle(styles[idx])
                        }
                    )
                }
                item { Spacer(modifier = Modifier.height(8.dp)) }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            ReplyBar(
                replyText = uiState.replyText,
                isInternal = uiState.isReplyInternal,
                isSubmitting = uiState.isSubmittingReply,
                attachmentCount = uiState.replyAttachments.size,
                onTextChange = viewModel::onReplyTextChange,
                onToggleInternal = viewModel::toggleReplyInternal,
                onAttach = { fileLauncher.launch(arrayOf("*/*")) },
                onSend = { viewModel.submitReply(ticketId) }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun TicketHeaderCard(
    ticket: Ticket,
    tags: List<String>,
    availableTags: List<String>,
    tagInput: String,
    pendingRemoveTag: String?,
    onTagInputChange: (String) -> Unit,
    onAddTag: (String) -> Unit,
    onRemoveTag: (String) -> Unit,
    onSetPendingRemoveTag: (String?) -> Unit
) {
    var showTagInput by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = ticket.title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusBadge(ticket.state)
            PriorityBadge(ticket.priority)
            Text(
                text = ticket.group,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            tags.forEach { tag ->
                val isPending = tag == pendingRemoveTag
                TagChip(
                    tag = tag,
                    pendingRemove = isPending,
                    onRemove = {
                        if (isPending) onRemoveTag(tag)
                        else onSetPendingRemoveTag(tag)
                    }
                )
            }
            if (showTagInput) {
                TagInputField(
                    value = tagInput,
                    suggestions = availableTags.filter {
                        it.contains(tagInput, ignoreCase = true) && !tags.contains(it)
                    },
                    onValueChange = onTagInputChange,
                    onAdd = { onAddTag(it); showTagInput = false },
                    onDismiss = { showTagInput = false }
                )
            } else {
                AddTagButton(onClick = { showTagInput = true })
            }
        }
    }
}

@Composable
private fun AddTagButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                Icons.Default.Add, contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(text = "Tag", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TagInputField(
    value: String,
    suggestions: List<String>,
    onValueChange: (String) -> Unit,
    onAdd: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded && suggestions.isNotEmpty(),
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                expanded = it.isNotEmpty()
            },
            modifier = Modifier
                .width(140.dp)
                .menuAnchor(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            textStyle = MaterialTheme.typography.bodySmall,
            trailingIcon = {
                IconButton(onClick = { onAdd(value) }, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                }
            }
        )
        ExposedDropdownMenu(
            expanded = expanded && suggestions.isNotEmpty(),
            onDismissRequest = { expanded = false; onDismiss() }
        ) {
            suggestions.take(5).forEach { suggestion ->
                DropdownMenuItem(
                    text = { Text(suggestion, style = MaterialTheme.typography.bodySmall) },
                    onClick = { onAdd(suggestion); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun ArticleBubble(
    article: Article,
    displaySettings: DisplaySettings,
    onToggleAvatar: () -> Unit,
    onCycleBubbleStyle: () -> Unit
) {
    val isAgent = article.sender.equals("Agent", ignoreCase = true)
    val isInternal = article.internal
    val darkTheme = isSystemInDarkTheme()

    val bubbleColor = when {
        isInternal -> if (darkTheme) ZammyColors.ArticleInternalBgDark else ZammyColors.ArticleInternalBg
        isAgent -> if (darkTheme) ZammyColors.ArticlePublicBgDark else ZammyColors.ArticlePublicBg
        else -> if (darkTheme) ZammyColors.ArticleCustomerBgDark else ZammyColors.ArticleCustomerBg
    }
    val accentColor = when {
        isInternal -> ZammyColors.StatusPending
        isAgent -> ZammyColors.Accent
        else -> Color.Transparent
    }

    val senderName = article.from?.substringBefore("<")?.trim() ?: article.sender
    val showAvatarForCustomer = displaySettings.showAvatars && displaySettings.showCustomerAvatar && !isInternal && !isAgent

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp),
        horizontalAlignment = if (isAgent) Alignment.End else Alignment.Start
    ) {
        // Header row: avatar + name + timestamp + settings
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showAvatarForCustomer) {
                Avatar(
                    name = senderName,
                    size = 20.dp,
                    modifier = Modifier.clickable(onClick = onToggleAvatar)
                )
            } else if (!isInternal && !isAgent) {
                Spacer(modifier = Modifier.size(20.dp))
            }

            Text(
                text = senderName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )

            if (displaySettings.showTimestamps) {
                Text(
                    text = formatArticleDate(article.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Settings buttons on the right
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onToggleAvatar,
                    modifier = Modifier.size(18.dp)
                ) {
                    Icon(
                        Icons.Default.Edit, // reuse as avatar toggle icon
                        contentDescription = "Avatar umschalten",
                        modifier = Modifier.size(10.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
                IconButton(
                    onClick = onCycleBubbleStyle,
                    modifier = Modifier.size(18.dp)
                ) {
                    Icon(
                        Icons.Default.Add, // reuse as bubble style toggle icon
                        contentDescription = "Bubble-Stil",
                        modifier = Modifier.size(10.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }

        // Internal badge
        if (!isInternal && displaySettings.showInternalBadge) {
            // no-op for non-internal, but keeps spacing consistent
        } else if (isInternal && displaySettings.showInternalBadge) {
            Box(
                modifier = Modifier
                    .padding(start = 4.dp, top = 2.dp)
                    .background(ZammyColors.StatusPending.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = stringResource(R.string.article_internal_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = ZammyColors.StatusPending
                )
            }
        }

        // Bubble card with configurable shape
        val bubbleShape = when (displaySettings.bubbleStyle) {
            "square" -> RoundedCornerShape(2.dp)
            "rounded" -> RoundedCornerShape(16.dp)
            else -> RoundedCornerShape(
                topStart = 16.dp, topEnd = 16.dp,
                bottomStart = if (isAgent) 16.dp else 4.dp,
                bottomEnd = if (isAgent) 4.dp else 16.dp
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(if (displaySettings.bubbleStyle == "chat" && !isInternal && !isAgent) 0.88f else 0.92f),
            shape = bubbleShape,
            colors = CardDefaults.cardColors(containerColor = bubbleColor),
            border = if (accentColor != Color.Transparent)
                BorderStroke(1.dp, accentColor.copy(alpha = 0.25f))
            else null
        ) {
            if (article.contentType.contains("html", ignoreCase = true)) {
                ArticleWebView(html = article.body)
            } else {
                Text(
                    text = article.body,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        if (article.attachments.isNotEmpty()) {
            Row(
                modifier = Modifier.padding(top = 4.dp, start = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.AttachFile, contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${article.attachments.size} Anhang/Anhänge",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ArticleWebView(html: String) {
    val context = LocalContext.current
    val bgColor = MaterialTheme.colorScheme.surface
    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = false
                setBackgroundColor(bgColor.toArgb())
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                        val scheme = request.url.scheme ?: return true
                        if (scheme != "http" && scheme != "https") return true
                        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, request.url)) }
                        return true
                    }
                }
            }
        },
        update = { webView ->
            val styledHtml = """
                <html><head><meta name='viewport' content='width=device-width,initial-scale=1'>
                <style>body{font-family:sans-serif;font-size:14px;padding:12px;margin:0;
                word-break:break-word;}</style></head><body>$html</body></html>
            """.trimIndent()
            webView.loadDataWithBaseURL(null, styledHtml, "text/html", "UTF-8", null)
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        onRelease = { webView -> webView.destroy() }
    )
}

@Composable
private fun ReplyBar(
    replyText: String,
    isInternal: Boolean,
    isSubmitting: Boolean,
    attachmentCount: Int,
    onTextChange: (String) -> Unit,
    onToggleInternal: () -> Unit,
    onAttach: () -> Unit,
    onSend: () -> Unit
) {
    Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 4.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                InternalToggle(isInternal = isInternal, onToggle = onToggleInternal)
                if (attachmentCount > 0) {
                    Text(
                        text = "$attachmentCount Datei(en) angehängt",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                OutlinedTextField(
                    value = replyText,
                    onValueChange = onTextChange,
                    placeholder = {
                        Text(
                            if (isInternal) "Interne Notiz…" else "Antwort schreiben…",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    modifier = Modifier.weight(1f),
                    maxLines = 6,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = if (isInternal)
                            ZammyColors.ArticleInternalBg.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = if (isInternal)
                            ZammyColors.ArticleInternalBg.copy(alpha = 0.3f)
                        else MaterialTheme.colorScheme.surface
                    )
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onAttach, modifier = Modifier.size(40.dp)) {
                        Icon(
                            Icons.Default.AttachFile, contentDescription = "Datei anhängen",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (isSubmitting) {
                        Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        }
                    } else {
                        IconButton(
                            onClick = onSend,
                            modifier = Modifier.size(40.dp),
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = if (replyText.isNotBlank())
                                    MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            enabled = replyText.isNotBlank()
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Senden",
                                tint = if (replyText.isNotBlank())
                                    MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InternalToggle(isInternal: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(2.dp)
    ) {
        ToggleChip(label = "Public", selected = !isInternal, color = ZammyColors.Accent) {
            if (isInternal) onToggle()
        }
        ToggleChip(label = "Intern", selected = isInternal, color = ZammyColors.StatusPending) {
            if (!isInternal) onToggle()
        }
    }
}

@Composable
private fun ToggleChip(label: String, selected: Boolean, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) color.copy(alpha = 0.15f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

private fun formatArticleDate(dateStr: String): String {
    val formats = listOf("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd'T'HH:mm:ss'Z'")
    for (fmt in formats) {
        runCatching {
            val sdf = SimpleDateFormat(fmt, Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val date = sdf.parse(dateStr) ?: return@runCatching
            return SimpleDateFormat("dd.MM. HH:mm", Locale.getDefault()).format(date)
        }
    }
    return dateStr
}
