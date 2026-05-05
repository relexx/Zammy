package com.zammy.app.presentation.createticket

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zammy.app.domain.model.Group
import com.zammy.app.domain.model.Ticket
import com.zammy.app.domain.usecase.CreateTicketUseCase
import com.zammy.app.domain.usecase.GetGroupsUseCase
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
    val customerEmail: String = "",
    val groups: List<Group> = emptyList(),
    val selectedGroupId: Int? = null,
    val selectedPriorityId: Int = 2,
    val attachments: List<Pair<String, ByteArray>> = emptyList(),
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val createdTicket: Ticket? = null
)

@HiltViewModel
class CreateTicketViewModel @Inject constructor(
    private val createTicketUseCase: CreateTicketUseCase,
    private val getGroupsUseCase: GetGroupsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateTicketUiState())
    val uiState: StateFlow<CreateTicketUiState> = _uiState.asStateFlow()

    init {
        loadGroups()
    }

    private fun loadGroups() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getGroupsUseCase().fold(
                onSuccess = { groups ->
                    _uiState.update {
                        it.copy(
                            groups = groups,
                            selectedGroupId = groups.firstOrNull()?.id,
                            isLoading = false
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Unbekannter Fehler") }
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

    fun onCustomerEmailChange(email: String) {
        _uiState.update { it.copy(customerEmail = email) }
    }

    fun onGroupSelected(groupId: Int) {
        _uiState.update { it.copy(selectedGroupId = groupId) }
    }

    fun onPrioritySelected(priorityId: Int) {
        _uiState.update { it.copy(selectedPriorityId = priorityId) }
    }

    fun onAttachmentsSelected(files: List<Pair<String, ByteArray>>) {
        _uiState.update { it.copy(attachments = files) }
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
                priorityId = state.selectedPriorityId,
                attachments = state.attachments,
                customer = state.customerEmail.takeIf { it.isNotBlank() }
            ).fold(
                onSuccess = { ticket ->
                    _uiState.update { it.copy(isSubmitting = false, createdTicket = ticket) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isSubmitting = false, error = e.message ?: "Unbekannter Fehler") }
                }
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
