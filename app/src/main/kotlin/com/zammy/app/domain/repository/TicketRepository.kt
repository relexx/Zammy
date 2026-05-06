package com.zammy.app.domain.repository

import com.zammy.app.domain.model.Article
import com.zammy.app.domain.model.Group
import com.zammy.app.domain.model.Ticket
import kotlinx.coroutines.flow.Flow

interface TicketRepository {
    fun getTickets(state: String? = null): Flow<List<Ticket>>
    suspend fun refreshTickets(): Result<Unit>
    suspend fun getTicket(id: Int): Result<Ticket>
    suspend fun getArticles(ticketId: Int): Result<List<Article>>
    suspend fun createTicket(
        title: String,
        body: String,
        groupId: Int,
        priorityId: Int,
        attachments: List<Pair<String, ByteArray>> = emptyList(),
        customer: String? = null
    ): Result<Ticket>
    suspend fun updateTicket(
        id: Int,
        title: String? = null,
        state: String? = null,
        groupId: Int? = null,
        priorityId: Int? = null,
        ownerId: Int? = null,
        pendingTime: String? = null,
        customer: String? = null
    ): Result<Ticket>
    suspend fun addArticle(
        ticketId: Int,
        body: String,
        internal: Boolean = false,
        attachments: List<Pair<String, ByteArray>> = emptyList()
    ): Result<Article>
    suspend fun searchTickets(query: String): Result<List<Ticket>>
    suspend fun getGroups(): Result<List<Group>>
    suspend fun getTags(ticketId: Int): Result<List<String>>
    suspend fun getTagList(): Result<List<String>>
    suspend fun addTag(ticketId: Int, tag: String): Result<Unit>
    suspend fun removeTag(ticketId: Int, tag: String): Result<Unit>
}
