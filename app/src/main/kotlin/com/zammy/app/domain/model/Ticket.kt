package com.zammy.app.domain.model

data class Ticket(
    val id: Int,
    val number: Int,
    val title: String,
    val state: String,
    val priority: String,
    val group: String,
    val ownerId: Int?,
    val customerId: Int,
    val customerName: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val articleCount: Int = 0,
    val note: String? = null
)
