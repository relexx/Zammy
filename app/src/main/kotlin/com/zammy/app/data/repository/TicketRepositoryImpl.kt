package com.zammy.app.data.repository

import android.util.Base64
import com.zammy.app.data.api.ZammadApiService
import com.zammy.app.data.api.model.ArticleCreateRequest
import com.zammy.app.data.api.model.ArticleRequest
import com.zammy.app.data.api.model.AttachmentRequest
import com.zammy.app.data.api.model.CreateTicketRequest
import com.zammy.app.data.api.model.UpdateTicketRequest
import com.zammy.app.data.local.dao.TicketDao
import com.zammy.app.data.local.entity.TicketEntity
import com.zammy.app.domain.model.Article
import com.zammy.app.domain.model.Attachment
import com.zammy.app.domain.model.Group
import com.zammy.app.domain.model.Ticket
import com.zammy.app.domain.repository.SettingsRepository
import com.zammy.app.domain.repository.TicketRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONObject
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TicketRepositoryImpl @Inject constructor(
    private val api: ZammadApiService,
    private val ticketDao: TicketDao,
    private val settingsRepository: SettingsRepository
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
        val perPage = 100
        val allEntities = mutableListOf<TicketEntity>()
        var page = 1
        while (true) {
            val batch = api.getTickets(page = page, perPage = perPage)
            allEntities += batch.map { it.toEntity() }
            if (batch.size < perPage) break
            page++
        }
        ticketDao.deleteAllTickets()
        ticketDao.insertTickets(allEntities)
    }

    override suspend fun getTicket(id: Int): Result<Ticket> = runCatching {
        val dto = api.getTicket(id)
        dto.toEntity().also { entity ->
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
        attachments: List<Pair<String, ByteArray>>,
        customer: String?
    ): Result<Ticket> = runCatching {
        val encodedAttachments = attachments.map { (filename, data) ->
            AttachmentRequest(
                filename = filename,
                data = Base64.encodeToString(data, Base64.NO_WRAP),
                mimeType = guessMimeType(filename)
            )
        }.takeIf { it.isNotEmpty() }

        val request = CreateTicketRequest(
            title = title,
            groupId = groupId,
            priorityId = priorityId,
            customer = customer?.takeIf { it.isNotBlank() }
                ?: settingsRepository.getUsername().takeIf { it.isNotBlank() },
            article = ArticleRequest(
                subject = title,
                body = body,
                attachments = encodedAttachments
            )
        )
        try {
            api.createTicket(request).toEntity().also { entity ->
                ticketDao.insertTicket(entity)
            }.toDomain()
        } catch (e: HttpException) {
            throw RuntimeException(e.zammadError(), e)
        }
    }

    override suspend fun updateTicket(
        id: Int,
        state: String?,
        groupId: Int?,
        priorityId: Int?,
        ownerId: Int?,
        pendingTime: String?,
        customer: String?
    ): Result<Ticket> = runCatching {
        val request = UpdateTicketRequest(
            state = state,
            groupId = groupId,
            priorityId = priorityId,
            ownerId = ownerId,
            pendingTime = pendingTime,
            customer = customer
        )
        try {
            api.updateTicket(id, request)
            // Re-fetch with expand=true to get correct state/group/priority strings
            api.getTicket(id, expand = true).toEntity().also { entity ->
                ticketDao.insertTicket(entity)
            }.toDomain()
        } catch (e: HttpException) {
            throw RuntimeException(e.zammadError(), e)
        }
    }

    override suspend fun addArticle(
        ticketId: Int,
        body: String,
        internal: Boolean,
        attachments: List<Pair<String, ByteArray>>
    ): Result<Article> = runCatching {
        val encodedAttachments = attachments.map { (filename, data) ->
            AttachmentRequest(
                filename = filename,
                data = Base64.encodeToString(data, Base64.NO_WRAP),
                mimeType = guessMimeType(filename)
            )
        }.takeIf { it.isNotEmpty() }
        val request = ArticleCreateRequest(
            ticketId = ticketId,
            body = body,
            internal = internal,
            attachments = encodedAttachments
        )
        try {
            val dto = api.createArticle(request)
            Article(
                id = dto.id,
                ticketId = dto.ticketId,
                type = dto.type ?: "web",
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
        } catch (e: HttpException) {
            throw RuntimeException(e.zammadError(), e)
        }
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
                customerName = dto.customer,
                createdAt = dto.createdAt,
                updatedAt = dto.updatedAt,
                articleCount = dto.articleCount ?: 0,
                note = dto.note
            )
        }
    }

    override suspend fun getGroups(): Result<List<Group>> = runCatching {
        api.getGroups()
            .filter { it.active != false }
            .map { Group(id = it.id, name = it.name) }
    }

    override suspend fun getTags(ticketId: Int): Result<List<String>> = runCatching {
        api.getTags(oId = ticketId).tags
    }

    override suspend fun getTagList(): Result<List<String>> = runCatching {
        api.getTagList().map { it.name }
    }

    override suspend fun addTag(ticketId: Int, tag: String): Result<Unit> = runCatching {
        api.addTag(com.zammy.app.data.api.model.TagRequest(oId = ticketId, item = tag))
    }

    override suspend fun removeTag(ticketId: Int, tag: String): Result<Unit> = runCatching {
        api.removeTag(com.zammy.app.data.api.model.TagRequest(oId = ticketId, item = tag))
    }

    private fun guessMimeType(filename: String): String = when {
        filename.endsWith(".pdf", ignoreCase = true) -> "application/pdf"
        filename.endsWith(".png", ignoreCase = true) -> "image/png"
        filename.endsWith(".jpg", ignoreCase = true) || filename.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
        filename.endsWith(".txt", ignoreCase = true) -> "text/plain"
        else -> "application/octet-stream"
    }

    private fun com.zammy.app.data.api.model.TicketDto.toEntity() = TicketEntity(
        id = id,
        number = number,
        title = title,
        state = state ?: "unknown",
        priority = priority ?: "normal",
        group = group ?: "unknown",
        ownerId = ownerId,
        customerId = customerId,
        customerName = customer,
        articleCount = articleCount ?: 0,
        note = note,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun TicketEntity.toDomain() = Ticket(
        id = id,
        number = number,
        title = title,
        state = state,
        priority = priority,
        group = group,
        ownerId = ownerId,
        customerId = customerId,
        customerName = customerName,
        createdAt = createdAt,
        updatedAt = updatedAt,
        articleCount = articleCount,
        note = note
    )
}

private fun HttpException.zammadError(): String {
    val body = response()?.errorBody()?.string()
    if (!body.isNullOrBlank()) {
        runCatching {
            val json = JSONObject(body)
            return json.optString("error").takeIf { it.isNotBlank() }
                ?: json.optString("error_human").takeIf { it.isNotBlank() }
                ?: body
        }
    }
    return "HTTP ${code()} ${message()}"
}
