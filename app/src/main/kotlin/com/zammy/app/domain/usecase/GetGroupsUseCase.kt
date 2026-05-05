package com.zammy.app.domain.usecase

import com.zammy.app.domain.model.Group
import com.zammy.app.domain.repository.TicketRepository
import javax.inject.Inject

class GetGroupsUseCase @Inject constructor(
    private val ticketRepository: TicketRepository
) {
    suspend operator fun invoke(): Result<List<Group>> = ticketRepository.getGroups()
}
