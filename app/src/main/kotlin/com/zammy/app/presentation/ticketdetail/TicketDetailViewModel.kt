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
    val replyAttachments: List<Pair<String, ByteArray>> = emptyList(),
    val isSubmittingReply: Boolean = false,
    val isReplyInternal: Boolean = false,
    val tags: List<String> = emptyList(),
    val availableTags: List<String> = emptyList(),
    val tagInput: String = ""
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
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Unbekannter Fehler") }
                }
            )
            getTicketDetailUseCase.getArticles(ticketId).fold(
                onSuccess = { articles ->
                    _uiState.update { it.copy(articles = articles) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(error = e.message ?: "Unbekannter Fehler") }
                }
            )
            loadTags(ticketId)
        }
    }

    fun reloadTicket(ticketId: Int) {
        viewModelScope.launch {
            getTicketDetailUseCase.getTicket(ticketId).onSuccess { ticket ->
                _uiState.update { it.copy(ticket = ticket) }
            }
        }
    }

    private fun loadTags(ticketId: Int) {
        viewModelScope.launch {
            getTicketDetailUseCase.getTags(ticketId).onSuccess { tags ->
                _uiState.update { it.copy(tags = tags) }
            }
            getTicketDetailUseCase.getTagList().onSuccess { available ->
                _uiState.update { it.copy(availableTags = available) }
            }
        }
    }

    fun onReplyTextChange(text: String) = _uiState.update { it.copy(replyText = text) }

    fun toggleReplyInternal() = _uiState.update { it.copy(isReplyInternal = !it.isReplyInternal) }

    fun onReplyAttachmentsChange(attachments: List<Pair<String, ByteArray>>) =
        _uiState.update { it.copy(replyAttachments = attachments) }

    fun onTagInputChange(input: String) = _uiState.update { it.copy(tagInput = input) }

    fun addTag(ticketId: Int, tag: String) {
        if (tag.isBlank()) return
        viewModelScope.launch {
            getTicketDetailUseCase.addTag(ticketId, tag).fold(
                onSuccess = { _uiState.update { it.copy(tags = it.tags + tag, tagInput = "") } },
                onFailure = { e -> _uiState.update { it.copy(error = e.message ?: "Unbekannter Fehler") } }
            )
        }
    }

    fun removeTag(ticketId: Int, tag: String) {
        viewModelScope.launch {
            getTicketDetailUseCase.removeTag(ticketId, tag).fold(
                onSuccess = { _uiState.update { it.copy(tags = it.tags - tag) } },
                onFailure = { e -> _uiState.update { it.copy(error = e.message ?: "Unbekannter Fehler") } }
            )
        }
    }

    fun submitReply(ticketId: Int) {
        val state = _uiState.value
        if (state.replyText.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingReply = true) }
            addCommentUseCase(
                ticketId = ticketId,
                body = state.replyText,
                internal = state.isReplyInternal,
                attachments = state.replyAttachments
            ).fold(
                onSuccess = { article ->
                    _uiState.update { s ->
                        s.copy(
                            articles = s.articles + article,
                            replyText = "",
                            replyAttachments = emptyList(),
                            isSubmittingReply = false,
                            isReplyInternal = false,
                            successMessage = "Antwort gesendet"
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isSubmittingReply = false, error = e.message ?: "Unbekannter Fehler") }
                }
            )
        }
    }

    fun clearMessages() = _uiState.update { it.copy(error = null, successMessage = null) }
}
