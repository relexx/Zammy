package com.zammy.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.zammy.app.data.local.entity.TicketEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TicketDao {

    @Query("SELECT * FROM tickets ORDER BY updatedAt DESC")
    fun getAllTickets(): Flow<List<TicketEntity>>

    @Query("SELECT * FROM tickets WHERE state = :state ORDER BY updatedAt DESC")
    fun getTicketsByState(state: String): Flow<List<TicketEntity>>

    @Query("SELECT * FROM tickets WHERE state IN (:states) ORDER BY updatedAt DESC")
    fun getTicketsByStates(states: List<String>): Flow<List<TicketEntity>>

    @Query("SELECT * FROM tickets WHERE id = :id")
    suspend fun getTicketById(id: Int): TicketEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTickets(tickets: List<TicketEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: TicketEntity)

    @Update
    suspend fun updateTicket(ticket: TicketEntity)

    @Query("DELETE FROM tickets")
    suspend fun deleteAllTickets()

    @Query("SELECT * FROM tickets WHERE title LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun searchTickets(query: String): Flow<List<TicketEntity>>
}
