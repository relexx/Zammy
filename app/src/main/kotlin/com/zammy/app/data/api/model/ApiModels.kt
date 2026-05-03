package com.zammy.app.data.api.model

import com.google.gson.annotations.SerializedName

data class TicketDto(
    @SerializedName("id") val id: Int,
    @SerializedName("number") val number: Int,
    @SerializedName("title") val title: String,
    @SerializedName("state") val state: String?,
    @SerializedName("state_id") val stateId: Int,
    @SerializedName("priority") val priority: String?,
    @SerializedName("priority_id") val priorityId: Int,
    @SerializedName("group") val group: String?,
    @SerializedName("group_id") val groupId: Int,
    @SerializedName("owner_id") val ownerId: Int?,
    @SerializedName("customer_id") val customerId: Int,
    @SerializedName("article_count") val articleCount: Int?,
    @SerializedName("note") val note: String?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)

data class ArticleDto(
    @SerializedName("id") val id: Int,
    @SerializedName("ticket_id") val ticketId: Int,
    @SerializedName("type") val type: String?,
    @SerializedName("sender") val sender: String?,
    @SerializedName("from") val from: String?,
    @SerializedName("to") val to: String?,
    @SerializedName("subject") val subject: String?,
    @SerializedName("body") val body: String,
    @SerializedName("content_type") val contentType: String?,
    @SerializedName("internal") val internal: Boolean,
    @SerializedName("created_by_id") val createdById: Int,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("attachments") val attachments: List<AttachmentDto>?
)

data class AttachmentDto(
    @SerializedName("id") val id: Int,
    @SerializedName("filename") val filename: String?,
    @SerializedName("size") val size: Long?,
    @SerializedName("preferences") val preferences: Map<String, Any>?
)

data class UserDto(
    @SerializedName("id") val id: Int,
    @SerializedName("login") val login: String,
    @SerializedName("firstname") val firstname: String?,
    @SerializedName("lastname") val lastname: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("active") val active: Boolean?,
    @SerializedName("role_ids") val roleIds: List<Int>?
)

data class GroupDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("active") val active: Boolean?
)

data class AttachmentRequest(
    @SerializedName("filename") val filename: String,
    @SerializedName("data") val data: String,
    @SerializedName("mime-type") val mimeType: String
)

data class ArticleRequest(
    @SerializedName("subject") val subject: String?,
    @SerializedName("body") val body: String,
    @SerializedName("type") val type: String = "web",
    @SerializedName("internal") val internal: Boolean = false,
    @SerializedName("content_type") val contentType: String = "text/plain",
    @SerializedName("attachments") val attachments: List<AttachmentRequest>? = null
)

data class CreateTicketRequest(
    @SerializedName("title") val title: String,
    @SerializedName("group_id") val groupId: Int,
    @SerializedName("priority_id") val priorityId: Int,
    @SerializedName("article") val article: ArticleRequest
)

data class UpdateTicketRequest(
    @SerializedName("state_id") val stateId: Int?,
    @SerializedName("priority_id") val priorityId: Int?,
    @SerializedName("owner_id") val ownerId: Int?
)

data class SearchResult(
    @SerializedName("ticket") val tickets: List<TicketDto>?,
    @SerializedName("query") val query: String?
)

data class TicketListResponse(
    @SerializedName("tickets") val ticketIds: List<Int>?,
    @SerializedName("tickets_count") val count: Int?
)
