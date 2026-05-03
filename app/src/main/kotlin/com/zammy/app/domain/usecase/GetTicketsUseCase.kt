package com.zammy.app.domain.usecase

import com.zammy.app.domain.model.Ticket
import com.zammy.app.domain.repository.TicketRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTicketsUseCase @Inject constructor(
    private val ticketRepository: TicketRepository
) {
    operator fun invoke(state: String? = null): Flow<List<Ticket>> {
        return ticketRepository.getTickets(state)
    }

    suspend fun refresh(): Result<Unit> {
        return ticketRepository.refreshTickets()
    }

    suspend fun search(query: String): Result<List<Ticket>> {
        return ticketRepository.searchTickets(query)
    }
}
