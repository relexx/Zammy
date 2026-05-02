package com.zammy.app.data.repository

import com.zammy.app.data.api.ZammadApiService
import com.zammy.app.data.api.model.ArticleRequest
import com.zammy.app.data.api.model.CreateTicketRequest
import com.zammy.app.data.api.model.UpdateTicketRequest
import com.zammy.app.data.local.dao.TicketDao
import com.zammy.app.data.local.entity.TicketEntity
import com.zammy.app.domain.model.Article
import com.zammy.app.domain.model.Attachment
import com.zammy.app.domain.model.Ticket
import com.zammy.app.domain.repository.TicketRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TicketRepositoryImpl @Inject constructor(
    private val api: ZammadApiService,
    private val ticketDao: TicketDao
) : TicketRepository {

    override fun getTickets(state: String?): Flow<List<Ticket>> {
        return if (state != null) {
            val states = when (state.lowercase()) {
                "open" -> listOf("new", "open")
                "pending" -> listOf("pending reminder", "pending close")
                "closed" -> listOf("closed")
                else -> listOf(state)
            }
            ticketDao.getTicketsByStates(states).map { entities ->
                entities.map { it.toDomain() }
            }
        } else {
            ticketDao.getAllTickets().map { entities ->
                entities.map { it.toDomain() }
            }
        }
    }

    override suspend fun refreshTickets(): Result<Unit> = runCatching {
        val tickets = api.getTickets(perPage = 100)
        val entities = tickets.map { dto ->
            TicketEntity(
                id = dto.id,
                number = dto.number,
                title = dto.title,
                state = dto.state ?: "unknown",
                priority = dto.priority ?: "normal",
                group = dto.group ?: "unknown",
                ownerId = dto.ownerId,
                customerId = dto.customerId,
                articleCount = dto.articleCount ?: 0,
                note = dto.note,
                createdAt = dto.createdAt,
                updatedAt = dto.updatedAt
            )
        }
        ticketDao.insertTickets(entities)
    }

    override suspend fun getTicket(id: Int): Result<Ticket> = runCatching {
        val dto = api.getTicket(id)
        TicketEntity(
            id = dto.id,
            number = dto.number,
            title = dto.title,
            state = dto.state ?: "unknown",
            priority = dto.priority ?: "normal",
            group = dto.group ?: "unknown",
            ownerId = dto.ownerId,
            customerId = dto.customerId,
            articleCount = dto.articleCount ?: 0,
            note = dto.note,
            createdAt = dto.createdAt,
            updatedAt = dto.updatedAt
        ).also { entity ->
            ticketDao.insertTicket(entity)
        }.toDomain()
    }

    override suspend fun getArticles(ticketId: Int): Result<List<Article>> = runCatching {
        api.getArticles(ticketId).map { dto ->
            Article(
                id = dto.id,
                ticketId = dto.ticketId,
                type = dto.type ?: "note",
                sender = dto.sender ?: "Customer",
                from = dto.from,
                to = dto.to,
                subject = dto.subject,
                body = dto.body,
                contentType = dto.contentType ?: "text/plain",
                internal = dto.internal,
                createdById = dto.createdById,
                createdAt = dto.createdAt,
                updatedAt = dto.updatedAt,
                attachments = dto.attachments?.map { att ->
                    Attachment(
                        id = att.id,
                        filename = att.filename ?: "attachment",
                        size = att.size ?: 0L,
                        preferences = att.preferences ?: emptyMap()
                    )
                } ?: emptyList()
            )
        }
    }

    override suspend fun createTicket(
        title: String,
        body: String,
        groupId: Int,
        priorityId: Int,
        attachments: List<Pair<String, ByteArray>>
    ): Result<Ticket> = runCatching {
        val request = CreateTicketRequest(
            title = title,
            groupId = groupId,
            priorityId = priorityId,
            article = ArticleRequest(subject = title, body = body)
        )
        val dto = api.createTicket(request)
        TicketEntity(
            id = dto.id,
            number = dto.number,
            title = dto.title,
            state = dto.state ?: "new",
            priority = dto.priority ?: "normal",
            group = dto.group ?: "unknown",
            ownerId = dto.ownerId,
            customerId = dto.customerId,
            articleCount = dto.articleCount ?: 1,
            note = dto.note,
            createdAt = dto.createdAt,
            updatedAt = dto.updatedAt
        ).also { entity ->
            ticketDao.insertTicket(entity)
        }.toDomain()
    }

    override suspend fun updateTicket(
        id: Int,
        stateId: Int?,
        priorityId: Int?,
        ownerId: Int?
    ): Result<Ticket> = runCatching {
        val request = UpdateTicketRequest(
            stateId = stateId,
            priorityId = priorityId,
            ownerId = ownerId
        )
        val dto = api.updateTicket(id, request)
        TicketEntity(
            id = dto.id,
            number = dto.number,
            title = dto.title,
            state = dto.state ?: "unknown",
            priority = dto.priority ?: "normal",
            group = dto.group ?: "unknown",
            ownerId = dto.ownerId,
            customerId = dto.customerId,
            articleCount = dto.articleCount ?: 0,
            note = dto.note,
            createdAt = dto.createdAt,
            updatedAt = dto.updatedAt
        ).also { entity ->
            ticketDao.insertTicket(entity)
        }.toDomain()
    }

    override suspend fun addArticle(
        ticketId: Int,
        body: String,
        internal: Boolean,
        attachments: List<Pair<String, ByteArray>>
    ): Result<Article> = runCatching {
        val requestBody: Map<String, Any> = mapOf(
            "ticket_id" to ticketId,
            "body" to body,
            "type" to "note",
            "internal" to internal,
            "content_type" to "text/plain"
        )
        val dto = api.createArticle(requestBody)
        Article(
            id = dto.id,
            ticketId = dto.ticketId,
            type = dto.type ?: "note",
            sender = dto.sender ?: "Agent",
            from = dto.from,
            to = dto.to,
            subject = dto.subject,
            body = dto.body,
            contentType = dto.contentType ?: "text/plain",
            internal = dto.internal,
            createdById = dto.createdById,
            createdAt = dto.createdAt,
            updatedAt = dto.updatedAt
        )
    }

    override suspend fun searchTickets(query: String): Result<List<Ticket>> = runCatching {
        api.searchTickets(query).map { dto ->
            Ticket(
                id = dto.id,
                number = dto.number,
                title = dto.title,
                state = dto.state ?: "unknown",
                priority = dto.priority ?: "normal",
                group = dto.group ?: "unknown",
                ownerId = dto.ownerId,
                customerId = dto.customerId,
                createdAt = dto.createdAt,
                updatedAt = dto.updatedAt,
                articleCount = dto.articleCount ?: 0,
                note = dto.note
            )
        }
    }

    private fun TicketEntity.toDomain() = Ticket(
        id = id,
        number = number,
        title = title,
        state = state,
        priority = priority,
        group = group,
        ownerId = ownerId,
        customerId = customerId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        articleCount = articleCount,
        note = note
    )
}
