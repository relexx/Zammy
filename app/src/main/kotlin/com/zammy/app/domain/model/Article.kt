package com.zammy.app.domain.model

data class Article(
    val id: Int,
    val ticketId: Int,
    val type: String,
    val sender: String,
    val from: String?,
    val to: String?,
    val subject: String?,
    val body: String,
    val contentType: String,
    val internal: Boolean,
    val createdById: Int,
    val createdAt: String,
    val updatedAt: String,
    val attachments: List<Attachment> = emptyList()
)

data class Attachment(
    val id: Int,
    val filename: String,
    val size: Long,
    val preferences: Map<String, Any> = emptyMap()
)
