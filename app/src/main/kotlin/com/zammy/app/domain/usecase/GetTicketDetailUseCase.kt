package com.zammy.app.domain.usecase

import com.zammy.app.domain.model.Article
import com.zammy.app.domain.model.Ticket
import com.zammy.app.domain.repository.TicketRepository
import javax.inject.Inject

class GetTicketDetailUseCase @Inject constructor(
    private val ticketRepository: TicketRepository
) {
    suspend fun getTicket(id: Int): Result<Ticket> {
        return ticketRepository.getTicket(id)
    }

    suspend fun getArticles(ticketId: Int): Result<List<Article>> {
        return ticketRepository.getArticles(ticketId)
    }

    suspend fun updateTicket(
        id: Int,
        state: String? = null,
        groupId: Int? = null,
        priorityId: Int? = null,
        ownerId: Int? = null,
        pendingTime: String? = null
    ): Result<Ticket> {
        return ticketRepository.updateTicket(id, state, groupId, priorityId, ownerId, pendingTime)
    }
}
