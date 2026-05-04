package com.zammy.app.presentation.ticketdetail

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zammy.app.R
import com.zammy.app.domain.model.Article
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketDetailScreen(
    ticketId: Int,
    onNavigateBack: () -> Unit,
    viewModel: TicketDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(ticketId) {
        viewModel.loadTicket(ticketId)
    }

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

    val attachmentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        val files = uris.mapNotNull { uri ->
            try {
                val filename = getFilenameFromUri(context, uri)
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (filename != null && bytes != null) Pair(filename, bytes) else null
            } catch (e: Exception) {
                null
            }
        }
        viewModel.onReplyAttachmentsChange(files)
    }

    if (uiState.showStatusDialog) {
        val statuses = listOf(
            "open" to stringResource(R.string.status_open),
            "pending reminder" to stringResource(R.string.status_pending_reminder),
            "pending close" to stringResource(R.string.status_pending_close),
            "closed" to stringResource(R.string.status_closed)
        )
        var selectedStatus by remember { mutableStateOf(uiState.ticket?.state?.lowercase() ?: "open") }
        var pendingDateMs by remember { mutableLongStateOf(-1L) }
        var pendingHour by remember { mutableIntStateOf(8) }
        var pendingMinute by remember { mutableIntStateOf(0) }
        var showDatePicker by remember { mutableStateOf(false) }

        val isPending = selectedStatus in listOf("pending reminder", "pending close")

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        pendingDateMs = datePickerState.selectedDateMillis ?: -1L
                        showDatePicker = false
                    }) { Text(stringResource(R.string.action_done)) }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text(stringResource(R.string.action_cancel))
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        AlertDialog(
            onDismissRequest = { viewModel.toggleStatusDialog(false) },
            title = { Text(stringResource(R.string.ticket_detail_change_status)) },
            text = {
                Column {
                    statuses.forEach { (apiName, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedStatus = apiName }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedStatus == apiName,
                                onClick = { selectedStatus = apiName }
                            )
                            Text(label, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                    if (isPending) {
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.ticket_detail_pending_time),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedButton(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (pendingDateMs > 0) formatUtcDateMs(pendingDateMs)
                                else stringResource(R.string.ticket_detail_select_date)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = "%02d".format(pendingHour),
                                onValueChange = { v ->
                                    v.toIntOrNull()?.takeIf { it in 0..23 }?.let { pendingHour = it }
                                },
                                label = { Text("HH") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = "%02d".format(pendingMinute),
                                onValueChange = { v ->
                                    v.toIntOrNull()?.takeIf { it in 0..59 }?.let { pendingMinute = it }
                                },
                                label = { Text("MM") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val pendingTime = if (isPending && pendingDateMs > 0) {
                            buildPendingTimeIso(pendingDateMs, pendingHour, pendingMinute)
                        } else null
                        viewModel.updateStatus(ticketId, selectedStatus, pendingTime)
                    },
                    enabled = !isPending || pendingDateMs > 0
                ) {
                    Text(stringResource(R.string.action_done))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.toggleStatusDialog(false) }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    if (uiState.showGroupDialog && uiState.groups.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleGroupDialog(false) },
            title = { Text(stringResource(R.string.ticket_detail_change_group)) },
            text = {
                Column {
                    uiState.groups.forEach { group ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.updateGroup(ticketId, group.id) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = uiState.ticket?.group == group.name,
                                onClick = { viewModel.updateGroup(ticketId, group.id) }
                            )
                            Text(group.name, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { viewModel.toggleGroupDialog(false) }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    if (uiState.showCustomerDialog) {
        var customerInput by remember { mutableStateOf(uiState.ticket?.customerName ?: "") }
        AlertDialog(
            onDismissRequest = { viewModel.toggleCustomerDialog(false) },
            title = { Text(stringResource(R.string.ticket_detail_change_customer)) },
            text = {
                OutlinedTextField(
                    value = customerInput,
                    onValueChange = { customerInput = it },
                    label = { Text(stringResource(R.string.create_ticket_customer)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.updateCustomer(ticketId, customerInput) },
                    enabled = customerInput.isNotBlank()
                ) {
                    Text(stringResource(R.string.action_done))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.toggleCustomerDialog(false) }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    if (uiState.showPriorityDialog) {
        val priorities = listOf(
            1 to stringResource(R.string.priority_low),
            2 to stringResource(R.string.priority_normal),
            3 to stringResource(R.string.priority_high)
        )
        AlertDialog(
            onDismissRequest = { viewModel.togglePriorityDialog(false) },
            title = { Text(stringResource(R.string.ticket_detail_change_priority)) },
            text = {
                Column {
                    priorities.forEach { (id, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.updatePriority(ticketId, id) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = uiState.ticket?.priority?.lowercase() == label.lowercase(),
                                onClick = { viewModel.updatePriority(ticketId, id) }
                            )
                            Text(label, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { viewModel.togglePriorityDialog(false) }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    if (uiState.showReplyDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleReplyDialog(false) },
            title = { Text(stringResource(R.string.add_comment_title)) },
            text = {
                Column {
                    OutlinedTextField(
                        value = uiState.replyText,
                        onValueChange = viewModel::onReplyTextChange,
                        placeholder = { Text(stringResource(R.string.add_comment_body)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        maxLines = 6
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { attachmentLauncher.launch(arrayOf("*/*")) }) {
                            Icon(
                                Icons.Default.AttachFile,
                                contentDescription = stringResource(R.string.add_comment_attach_file)
                            )
                        }
                        if (uiState.replyAttachments.isNotEmpty()) {
                            Text(
                                text = stringResource(
                                    R.string.attachment_files_selected,
                                    uiState.replyAttachments.size
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.submitReply(ticketId) },
                    enabled = !uiState.isSubmittingReply && uiState.replyText.isNotBlank()
                ) {
                    if (uiState.isSubmittingReply) {
                        CircularProgressIndicator()
                    } else {
                        Text(stringResource(R.string.add_comment_submit))
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.toggleReplyDialog(false) }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.ticket?.let {
                            stringResource(R.string.ticket_detail_title, it.number)
                        } ?: stringResource(R.string.loading)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.toggleReplyDialog(true) }) {
                Icon(Icons.Default.Add, contentDescription = "Add reply")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.ticket != null -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    item {
                        TicketInfoCard(
                            uiState = uiState,
                            onChangeStatus = { viewModel.toggleStatusDialog(true) },
                            onChangePriority = { viewModel.togglePriorityDialog(true) },
                            onChangeGroup = { viewModel.toggleGroupDialog(true) },
                            onChangeCustomer = { viewModel.toggleCustomerDialog(true) }
                        )
                        HorizontalDivider()
                        Text(
                            text = "Timeline",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    items(uiState.articles, key = { it.id }) { article ->
                        ArticleItem(article = article)
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }

            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.ticket_detail_error))
                }
            }
        }
    }
}

@Composable
fun TicketInfoCard(
    uiState: TicketDetailUiState,
    onChangeStatus: () -> Unit,
    onChangePriority: () -> Unit,
    onChangeGroup: () -> Unit,
    onChangeCustomer: () -> Unit
) {
    val ticket = uiState.ticket ?: return
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = ticket.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            EditableInfoRow(
                label = stringResource(R.string.ticket_detail_status),
                value = ticket.state,
                isUpdating = uiState.isUpdating,
                onEdit = onChangeStatus
            )
            EditableInfoRow(
                label = stringResource(R.string.ticket_detail_priority),
                value = ticket.priority,
                isUpdating = uiState.isUpdating,
                onEdit = onChangePriority
            )
            EditableInfoRow(
                label = stringResource(R.string.ticket_detail_group),
                value = ticket.group,
                isUpdating = uiState.isUpdating,
                onEdit = onChangeGroup
            )
            EditableInfoRow(
                label = stringResource(R.string.ticket_detail_customer),
                value = ticket.customerName ?: "-",
                isUpdating = uiState.isUpdating,
                onEdit = onChangeCustomer
            )
            InfoRow(label = stringResource(R.string.ticket_detail_created), value = ticket.createdAt)
            InfoRow(label = stringResource(R.string.ticket_detail_updated), value = ticket.updatedAt)
        }
    }
}

@Composable
fun EditableInfoRow(
    label: String,
    value: String,
    isUpdating: Boolean,
    onEdit: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            IconButton(onClick = onEdit, enabled = !isUpdating) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit $label",
                    modifier = Modifier.padding(start = 4.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ArticleItem(article: Article) {
    val isInternal = article.internal
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isInternal)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = article.sender,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                if (isInternal) {
                    Text(
                        text = "Internal",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            if (article.contentType.startsWith("text/html", ignoreCase = true)) {
                ArticleBodyWebView(html = article.body)
            } else {
                Text(
                    text = article.body,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = article.createdAt,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ArticleBodyWebView(html: String, modifier: Modifier = Modifier) {
    val textColorHex = "#%06X".format(MaterialTheme.colorScheme.onSurface.toArgb() and 0xFFFFFF)
    val linkColorHex = "#%06X".format(MaterialTheme.colorScheme.primary.toArgb() and 0xFFFFFF)
    val styledHtml = remember(html, textColorHex, linkColorHex) {
        """<html><head>
<meta name="viewport" content="width=device-width,initial-scale=1">
<style>
body{margin:0;padding:0;font-family:sans-serif;font-size:14px;line-height:1.5;
     word-wrap:break-word;overflow-wrap:break-word;color:$textColorHex;background:transparent}
img{max-width:100%;height:auto}
table{max-width:100%;word-break:break-word}
pre{white-space:pre-wrap}
a{color:$linkColorHex}
</style></head><body>$html</body></html>"""
    }
    var heightDp by remember { mutableStateOf(100.dp) }

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.apply {
                    javaScriptEnabled = false
                    allowFileAccess = false
                    allowContentAccess = false
                    setSupportZoom(false)
                    loadWithOverviewMode = false
                    useWideViewPort = false
                }
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String) {
                        view.post {
                            val h = view.contentHeight
                            if (h > 0) heightDp = h.dp
                        }
                    }
                    override fun shouldOverrideUrlLoading(
                        view: WebView,
                        request: WebResourceRequest
                    ): Boolean {
                        runCatching {
                            context.startActivity(Intent(Intent.ACTION_VIEW, request.url))
                        }
                        return true
                    }
                }
                loadDataWithBaseURL(null, styledHtml, "text/html", "UTF-8", null)
            }
        },
        modifier = modifier.fillMaxWidth().height(heightDp)
    )
}

private fun getFilenameFromUri(context: android.content.Context, uri: Uri): String? =
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        cursor.moveToFirst()
        if (nameIndex >= 0) cursor.getString(nameIndex) else null
    }

private fun formatUtcDateMs(ms: Long): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        .apply { timeZone = TimeZone.getTimeZone("UTC") }
        .format(Date(ms))

private fun buildPendingTimeIso(dateMidnightUtcMs: Long, hour: Int, minute: Int): String {
    val totalMs = dateMidnightUtcMs + (hour * 3600 + minute * 60) * 1000L
    return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        .apply { timeZone = TimeZone.getTimeZone("UTC") }
        .format(Date(totalMs))
}
