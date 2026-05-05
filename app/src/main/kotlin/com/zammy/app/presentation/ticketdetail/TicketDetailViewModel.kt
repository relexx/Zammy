package com.zammy.app.presentation.ticketdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zammy.app.data.api.ZammadApiService
import com.zammy.app.data.api.model.GroupDto
import com.zammy.app.domain.model.Article
import com.zammy.app.domain.model.Ticket
import com.zammy.app.domain.usecase.AddCommentUseCase
import com.zammy.app.domain.usecase.GetTicketDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TicketDetailUiState(
    val ticket: Ticket? = null,
    val articles: List<Article> = emptyList(),
    val groups: List<GroupDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val replyText: String = "",
    val replyAttachments: List<Pair<String, ByteArray>> = emptyList(),
    val isSubmittingReply: Boolean = false,
    val showReplyDialog: Boolean = false,
    val showStatusDialog: Boolean = false,
    val showPriorityDialog: Boolean = false,
    val showGroupDialog: Boolean = false,
    val showCustomerDialog: Boolean = false,
    val showTagDialog: Boolean = false,
    val tags: List<String> = emptyList(),
    val availableTags: List<String> = emptyList(),
    val isUpdating: Boolean = false
)

@HiltViewModel
class TicketDetailViewModel @Inject constructor(
    private val getTicketDetailUseCase: GetTicketDetailUseCase,
    private val addCommentUseCase: AddCommentUseCase,
    private val api: ZammadApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(TicketDetailUiState())
    val uiState: StateFlow<TicketDetailUiState> = _uiState.asStateFlow()

    fun loadTicket(ticketId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getTicketDetailUseCase.getTicket(ticketId).fold(
                onSuccess = { ticket ->
                    _uiState.update { it.copy(ticket = ticket, isLoading = false) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
            getTicketDetailUseCase.getArticles(ticketId).fold(
                onSuccess = { articles ->
                    _uiState.update { it.copy(articles = articles) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
            )
            loadGroups()
            loadTags(ticketId)
        }
    }

    private fun loadTags(ticketId: Int) {
        viewModelScope.launch {
            getTicketDetailUseCase.getTags(ticketId).onSuccess { tags ->
                _uiState.update { it.copy(tags = tags) }
            }
        }
        viewModelScope.launch {
            getTicketDetailUseCase.getTagList().onSuccess { available ->
                _uiState.update { it.copy(availableTags = available) }
            }
        }
    }

    private fun loadGroups() {
        viewModelScope.launch {
            runCatching { api.getGroups() }.onSuccess { groups ->
                _uiState.update { it.copy(groups = groups.filter { g -> g.active != false }) }
            }
        }
    }

    fun updateStatus(ticketId: Int, state: String, pendingTime: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true, showStatusDialog = false) }
            getTicketDetailUseCase.updateTicket(ticketId, state = state, pendingTime = pendingTime).fold(
                onSuccess = { ticket ->
                    _uiState.update {
                        it.copy(ticket = ticket, isUpdating = false,
                            successMessage = "Ticket updated successfully")
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isUpdating = false, error = e.message) }
                }
            )
        }
    }

    fun updatePriority(ticketId: Int, priorityId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true, showPriorityDialog = false) }
            getTicketDetailUseCase.updateTicket(ticketId, priorityId = priorityId).fold(
                onSuccess = { ticket ->
                    _uiState.update {
                        it.copy(ticket = ticket, isUpdating = false,
                            successMessage = "Ticket updated successfully")
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isUpdating = false, error = e.message) }
                }
            )
        }
    }

    fun updateGroup(ticketId: Int, groupId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true, showGroupDialog = false) }
            getTicketDetailUseCase.updateTicket(ticketId, groupId = groupId).fold(
                onSuccess = { ticket ->
                    _uiState.update {
                        it.copy(ticket = ticket, isUpdating = false,
                            successMessage = "Ticket updated successfully")
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isUpdating = false, error = e.message) }
                }
            )
        }
    }

    fun toggleStatusDialog(show: Boolean) {
        _uiState.update { it.copy(showStatusDialog = show) }
    }

    fun togglePriorityDialog(show: Boolean) {
        _uiState.update { it.copy(showPriorityDialog = show) }
    }

    fun toggleGroupDialog(show: Boolean) {
        _uiState.update { it.copy(showGroupDialog = show) }
    }

    fun toggleCustomerDialog(show: Boolean) {
        _uiState.update { it.copy(showCustomerDialog = show) }
    }

    fun toggleTagDialog(show: Boolean) {
        _uiState.update { it.copy(showTagDialog = show) }
    }

    fun addTag(ticketId: Int, tag: String) {
        viewModelScope.launch {
            getTicketDetailUseCase.addTag(ticketId, tag).fold(
                onSuccess = {
                    _uiState.update { it.copy(tags = it.tags + tag, showTagDialog = false) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(error = e.message, showTagDialog = false) }
                }
            )
        }
    }

    fun removeTag(ticketId: Int, tag: String) {
        viewModelScope.launch {
            getTicketDetailUseCase.removeTag(ticketId, tag).fold(
                onSuccess = {
                    _uiState.update { it.copy(tags = it.tags - tag) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
            )
        }
    }

    fun updateCustomer(ticketId: Int, customer: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true, showCustomerDialog = false) }
            getTicketDetailUseCase.updateTicket(ticketId, customer = customer).fold(
                onSuccess = { ticket ->
                    _uiState.update {
                        it.copy(ticket = ticket, isUpdating = false,
                            successMessage = "Ticket updated successfully")
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isUpdating = false, error = e.message) }
                }
            )
        }
    }

    fun onReplyTextChange(text: String) {
        _uiState.update { it.copy(replyText = text) }
    }

    fun onReplyAttachmentsChange(attachments: List<Pair<String, ByteArray>>) {
        _uiState.update { it.copy(replyAttachments = attachments) }
    }

    fun toggleReplyDialog(show: Boolean) {
        _uiState.update { it.copy(showReplyDialog = show, replyAttachments = emptyList()) }
    }

    fun submitReply(ticketId: Int) {
        val state = _uiState.value
        if (state.replyText.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingReply = true) }
            addCommentUseCase(ticketId, state.replyText, attachments = state.replyAttachments).fold(
                onSuccess = { article ->
                    _uiState.update { s ->
                        s.copy(
                            articles = s.articles + article,
                            replyText = "",
                            replyAttachments = emptyList(),
                            isSubmittingReply = false,
                            showReplyDialog = false,
                            successMessage = "Reply sent successfully"
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(isSubmittingReply = false, error = e.message)
                    }
                }
            )
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}
