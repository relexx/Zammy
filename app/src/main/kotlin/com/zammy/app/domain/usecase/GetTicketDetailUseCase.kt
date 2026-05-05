package com.zammy.app.domain.usecase

import com.zammy.app.domain.model.Article
import com.zammy.app.domain.model.Group
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

    suspend fun getGroups(): Result<List<Group>> = ticketRepository.getGroups()

    suspend fun getTags(ticketId: Int) = ticketRepository.getTags(ticketId)
    suspend fun getTagList() = ticketRepository.getTagList()
    suspend fun addTag(ticketId: Int, tag: String) = ticketRepository.addTag(ticketId, tag)
    suspend fun removeTag(ticketId: Int, tag: String) = ticketRepository.removeTag(ticketId, tag)

    suspend fun updateTicket(
        id: Int,
        state: String? = null,
        groupId: Int? = null,
        priorityId: Int? = null,
        ownerId: Int? = null,
        pendingTime: String? = null,
        customer: String? = null
    ): Result<Ticket> {
        return ticketRepository.updateTicket(id, state, groupId, priorityId, ownerId, pendingTime, customer)
    }
}
