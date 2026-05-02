package com.zammy.app.presentation.tickets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zammy.app.domain.model.Ticket
import com.zammy.app.domain.usecase.GetTicketsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TicketsUiState(
    val openTickets: List<Ticket> = emptyList(),
    val pendingTickets: List<Ticket> = emptyList(),
    val closedTickets: List<Ticket> = emptyList(),
    val searchResults: List<Ticket>? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val selectedTab: Int = 0
)

@HiltViewModel
class TicketsViewModel @Inject constructor(
    private val getTicketsUseCase: GetTicketsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TicketsUiState())
    val uiState: StateFlow<TicketsUiState> = _uiState.asStateFlow()

    init {
        observeTickets()
        refresh()
    }

    private fun observeTickets() {
        viewModelScope.launch {
            getTicketsUseCase("open")
                .catch { e -> _uiState.update { it.copy(error = e.message) } }
                .collect { tickets ->
                    _uiState.update { it.copy(openTickets = tickets) }
                }
        }
        viewModelScope.launch {
            getTicketsUseCase("pending")
                .catch { e -> _uiState.update { it.copy(error = e.message) } }
                .collect { tickets ->
                    _uiState.update { it.copy(pendingTickets = tickets) }
                }
        }
        viewModelScope.launch {
            getTicketsUseCase("closed")
                .catch { e -> _uiState.update { it.copy(error = e.message) } }
                .collect { tickets ->
                    _uiState.update { it.copy(closedTickets = tickets) }
                }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }
            getTicketsUseCase.refresh().fold(
                onSuccess = { _uiState.update { it.copy(isRefreshing = false) } },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(isRefreshing = false, error = e.message)
                    }
                }
            )
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        if (query.isBlank()) {
            _uiState.update { it.copy(searchResults = null) }
            return
        }
        viewModelScope.launch {
            getTicketsUseCase.search(query).fold(
                onSuccess = { results ->
                    _uiState.update { it.copy(searchResults = results) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
            )
        }
    }

    fun onTabSelected(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
