package com.zammy.app.presentation.createticket

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zammy.app.data.api.ZammadApiService
import com.zammy.app.data.api.model.GroupDto
import com.zammy.app.domain.model.Ticket
import com.zammy.app.domain.usecase.CreateTicketUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateTicketUiState(
    val title: String = "",
    val body: String = "",
    val groups: List<GroupDto> = emptyList(),
    val selectedGroupId: Int? = null,
    val selectedPriorityId: Int = 2, // Default: normal
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val createdTicket: Ticket? = null
)

@HiltViewModel
class CreateTicketViewModel @Inject constructor(
    private val createTicketUseCase: CreateTicketUseCase,
    private val api: ZammadApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateTicketUiState())
    val uiState: StateFlow<CreateTicketUiState> = _uiState.asStateFlow()

    init {
        loadGroups()
    }

    private fun loadGroups() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching { api.getGroups() }.fold(
                onSuccess = { groups ->
                    _uiState.update {
                        it.copy(
                            groups = groups.filter { g -> g.active != false },
                            selectedGroupId = groups.firstOrNull()?.id,
                            isLoading = false
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }

    fun onTitleChange(title: String) {
        _uiState.update { it.copy(title = title, error = null) }
    }

    fun onBodyChange(body: String) {
        _uiState.update { it.copy(body = body, error = null) }
    }

    fun onGroupSelected(groupId: Int) {
        _uiState.update { it.copy(selectedGroupId = groupId) }
    }

    fun onPrioritySelected(priorityId: Int) {
        _uiState.update { it.copy(selectedPriorityId = priorityId) }
    }

    fun submit() {
        val state = _uiState.value
        if (state.title.isBlank() || state.body.isBlank()) {
            _uiState.update { it.copy(error = "Title and description are required") }
            return
        }
        val groupId = state.selectedGroupId ?: run {
            _uiState.update { it.copy(error = "Please select a group") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            createTicketUseCase(
                title = state.title,
                body = state.body,
                groupId = groupId,
                priorityId = state.selectedPriorityId
            ).fold(
                onSuccess = { ticket ->
                    _uiState.update { it.copy(isSubmitting = false, createdTicket = ticket) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isSubmitting = false, error = e.message) }
                }
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
