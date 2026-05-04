package com.zammy.app.domain.usecase

import com.zammy.app.domain.model.Ticket
import com.zammy.app.domain.repository.TicketRepository
import javax.inject.Inject

class CreateTicketUseCase @Inject constructor(
    private val ticketRepository: TicketRepository
) {
    suspend operator fun invoke(
        title: String,
        body: String,
        groupId: Int,
        priorityId: Int,
        attachments: List<Pair<String, ByteArray>> = emptyList(),
        customer: String? = null
    ): Result<Ticket> {
        if (title.isBlank() || body.isBlank()) {
            return Result.failure(IllegalArgumentException("Title and body are required"))
        }
        return ticketRepository.createTicket(title, body, groupId, priorityId, attachments, customer)
    }
}
