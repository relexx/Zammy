package com.zammy.app.domain.usecase

import com.zammy.app.domain.model.Article
import com.zammy.app.domain.repository.TicketRepository
import javax.inject.Inject

class AddCommentUseCase @Inject constructor(
    private val ticketRepository: TicketRepository
) {
    suspend operator fun invoke(
        ticketId: Int,
        body: String,
        internal: Boolean = false,
        attachments: List<Pair<String, ByteArray>> = emptyList()
    ): Result<Article> {
        if (body.isBlank()) {
            return Result.failure(IllegalArgumentException("Comment body cannot be empty"))
        }
        return ticketRepository.addArticle(ticketId, body, internal, attachments)
    }
}
