package com.zammy.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.zammy.app.data.local.dao.TicketDao
import com.zammy.app.data.local.entity.TicketEntity

@Database(
    entities = [TicketEntity::class],
    version = 2,
    exportSchema = false
)
abstract class ZammyDatabase : RoomDatabase() {
    abstract fun ticketDao(): TicketDao

    companion object {
        const val DATABASE_NAME = "zammy_db"
    }
}
