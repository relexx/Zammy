package com.zammy.app.presentation.ticketdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val replyText: String = "",
    val isSubmittingReply: Boolean = false,
    val showReplyDialog: Boolean = false
)

@HiltViewModel
class TicketDetailViewModel @Inject constructor(
    private val getTicketDetailUseCase: GetTicketDetailUseCase,
    private val addCommentUseCase: AddCommentUseCase
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
        }
    }

    fun updateTicket(
        ticketId: Int,
        stateId: Int? = null,
        priorityId: Int? = null,
        ownerId: Int? = null
    ) {
        viewModelScope.launch {
            getTicketDetailUseCase.updateTicket(ticketId, stateId, priorityId, ownerId).fold(
                onSuccess = { ticket ->
                    _uiState.update {
                        it.copy(
                            ticket = ticket,
                            successMessage = "Ticket updated successfully"
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
            )
        }
    }

    fun onReplyTextChange(text: String) {
        _uiState.update { it.copy(replyText = text) }
    }

    fun toggleReplyDialog(show: Boolean) {
        _uiState.update { it.copy(showReplyDialog = show) }
    }

    fun submitReply(ticketId: Int) {
        val replyText = _uiState.value.replyText
        if (replyText.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingReply = true) }
            addCommentUseCase(ticketId, replyText).fold(
                onSuccess = { article ->
                    _uiState.update { state ->
                        state.copy(
                            articles = state.articles + article,
                            replyText = "",
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
