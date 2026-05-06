package com.zammy.app.presentation.editticket

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zammy.app.domain.model.Group
import com.zammy.app.domain.model.User
import com.zammy.app.domain.usecase.GetAgentsUseCase
import com.zammy.app.domain.usecase.GetGroupsUseCase
import com.zammy.app.domain.usecase.GetTicketDetailUseCase
import com.zammy.app.ui.components.priorityIdFromString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditTicketUiState(
    val ticketId: Int = 0,
    val title: String = "",
    val selectedState: String = "open",
    val pendingTime: String = "",
    val groups: List<Group> = emptyList(),
    val selectedGroupId: Int? = null,
    val selectedPriorityId: Int = 2,
    val agents: List<User> = emptyList(),
    val selectedOwnerId: Int? = null,
    val tags: List<String> = emptyList(),
    val availableTags: List<String> = emptyList(),
    val tagInput: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val isDone: Boolean = false
)

@HiltViewModel
class EditTicketViewModel @Inject constructor(
    private val getTicketDetailUseCase: GetTicketDetailUseCase,
    private val getGroupsUseCase: GetGroupsUseCase,
    private val getAgentsUseCase: GetAgentsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditTicketUiState())
    val uiState: StateFlow<EditTicketUiState> = _uiState.asStateFlow()

    fun loadTicket(ticketId: Int) {
        _uiState.update { it.copy(ticketId = ticketId, isLoading = true) }
        viewModelScope.launch {
            val ticketResult = getTicketDetailUseCase.getTicket(ticketId)
            ticketResult.onSuccess { ticket ->
                _uiState.update { state ->
                    state.copy(
                        title = ticket.title,
                        selectedState = ticket.state,
                        selectedPriorityId = priorityIdFromString(ticket.priority),
                        selectedOwnerId = ticket.ownerId,
                        isLoading = false
                    )
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Fehler beim Laden") }
            }

            val cachedTicket = ticketResult.getOrNull()
            getGroupsUseCase().onSuccess { groups ->
                val groupId = groups.find { it.name == cachedTicket?.group }?.id ?: groups.firstOrNull()?.id
                _uiState.update { it.copy(groups = groups, selectedGroupId = groupId) }
            }

            getAgentsUseCase().onSuccess { agents ->
                _uiState.update { it.copy(agents = agents) }
            }

            getTicketDetailUseCase.getTags(ticketId).onSuccess { tags ->
                _uiState.update { it.copy(tags = tags) }
            }

            getTicketDetailUseCase.getTagList().onSuccess { available ->
                _uiState.update { it.copy(availableTags = available) }
            }
        }
    }

    fun onTitleChange(title: String) = _uiState.update { it.copy(title = title) }

    fun onStateSelected(state: String) = _uiState.update { it.copy(selectedState = state) }

    fun onPendingTimeChange(time: String) = _uiState.update { it.copy(pendingTime = time) }

    fun onGroupSelected(groupId: Int) = _uiState.update { it.copy(selectedGroupId = groupId) }

    fun onPrioritySelected(priorityId: Int) = _uiState.update { it.copy(selectedPriorityId = priorityId) }

    fun onOwnerSelected(ownerId: Int?) = _uiState.update { it.copy(selectedOwnerId = ownerId) }

    fun onTagInputChange(input: String) = _uiState.update { it.copy(tagInput = input) }

    fun addTag(tag: String) {
        val state = _uiState.value
        if (tag.isBlank() || state.tags.contains(tag)) return
        viewModelScope.launch {
            getTicketDetailUseCase.addTag(state.ticketId, tag).onSuccess {
                _uiState.update { it.copy(tags = it.tags + tag, tagInput = "") }
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message ?: "Fehler beim Hinzufügen des Tags") }
            }
        }
    }

    fun removeTag(tag: String) {
        val state = _uiState.value
        viewModelScope.launch {
            getTicketDetailUseCase.removeTag(state.ticketId, tag).onSuccess {
                _uiState.update { it.copy(tags = it.tags - tag) }
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message ?: "Fehler beim Entfernen des Tags") }
            }
        }
    }

    fun saveChanges() {
        val state = _uiState.value
        if (state.title.isBlank()) {
            _uiState.update { it.copy(error = "Betreff darf nicht leer sein") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val pendingTime = if (state.selectedState.contains("pending", ignoreCase = true)) {
                state.pendingTime.takeIf { it.isNotBlank() }
            } else null

            getTicketDetailUseCase.updateTicket(
                id = state.ticketId,
                title = state.title,
                state = state.selectedState,
                groupId = state.selectedGroupId,
                priorityId = state.selectedPriorityId,
                ownerId = state.selectedOwnerId,
                pendingTime = pendingTime
            ).fold(
                onSuccess = { _uiState.update { it.copy(isSaving = false, isDone = true) } },
                onFailure = { e ->
                    _uiState.update { it.copy(isSaving = false, error = e.message ?: "Speichern fehlgeschlagen") }
                }
            )
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }

    fun clearDone() = _uiState.update { it.copy(isDone = false) }
}
