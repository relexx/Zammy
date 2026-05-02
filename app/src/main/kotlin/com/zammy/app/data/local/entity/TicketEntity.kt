package com.zammy.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tickets")
data class TicketEntity(
    @PrimaryKey val id: Int,
    val number: Int,
    val title: String,
    val state: String,
    val priority: String,
    val group: String,
    val ownerId: Int?,
    val customerId: Int,
    val articleCount: Int,
    val note: String?,
    val createdAt: String,
    val updatedAt: String,
    val syncedAt: Long = System.currentTimeMillis()
)
