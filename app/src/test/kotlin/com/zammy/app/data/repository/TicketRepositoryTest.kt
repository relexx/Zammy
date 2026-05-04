package com.zammy.app.data.repository

import com.zammy.app.data.api.ZammadApiService
import com.zammy.app.data.api.model.TicketDto
import com.zammy.app.data.local.dao.TicketDao
import com.zammy.app.data.local.entity.TicketEntity
import com.zammy.app.domain.repository.SettingsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TicketRepositoryTest {

    private lateinit var api: ZammadApiService
    private lateinit var ticketDao: TicketDao
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var repository: TicketRepositoryImpl

    @Before
    fun setup() {
        api = mockk()
        ticketDao = mockk(relaxed = true)
        settingsRepository = mockk<SettingsRepository>().also {
            every { it.getUsername() } returns "test@example.com"
        }
        repository = TicketRepositoryImpl(api, ticketDao, settingsRepository)
    }

    @Test
    fun `getTickets returns flow from dao`() = runTest {
        val entities = listOf(
            TicketEntity(
                id = 1, number = 1001, title = "Test ticket", state = "open",
                priority = "normal", group = "Support", ownerId = null,
                customerId = 5, articleCount = 2, note = null,
                createdAt = "2024-01-01T00:00:00Z", updatedAt = "2024-01-02T00:00:00Z"
            )
        )
        coEvery { ticketDao.getAllTickets() } returns flowOf(entities)

        val tickets = repository.getTickets(null).first()

        assertEquals(1, tickets.size)
        assertEquals("Test ticket", tickets[0].title)
        assertEquals("open", tickets[0].state)
    }

    @Test
    fun `refreshTickets calls api and inserts to dao`() = runTest {
        val dtos = listOf(
            TicketDto(
                id = 1, number = 1001, title = "Test ticket", state = "open",
                stateId = 2, priority = "normal", priorityId = 2, group = "Support",
                groupId = 1, ownerId = null, customerId = 5, articleCount = 2,
                note = null, createdAt = "2024-01-01T00:00:00Z", updatedAt = "2024-01-02T00:00:00Z"
            )
        )
        coEvery { api.getTickets(any(), any(), any(), any()) } returns dtos

        val result = repository.refreshTickets()

        assertTrue(result.isSuccess)
        coVerify { ticketDao.insertTickets(any()) }
    }

    @Test
    fun `refreshTickets returns failure when api throws`() = runTest {
        coEvery { api.getTickets(any(), any(), any(), any()) } throws RuntimeException("Network error")

        val result = repository.refreshTickets()

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getTicket fetches from api and caches to dao`() = runTest {
        val dto = TicketDto(
            id = 42, number = 42, title = "Detail ticket", state = "open",
            stateId = 2, priority = "high", priorityId = 3, group = "Dev",
            groupId = 2, ownerId = 7, customerId = 5, articleCount = 3,
            note = "some note", createdAt = "2024-01-01T00:00:00Z", updatedAt = "2024-01-03T00:00:00Z"
        )
        coEvery { api.getTicket(42, any()) } returns dto

        val result = repository.getTicket(42)

        assertTrue(result.isSuccess)
        val ticket = result.getOrNull()!!
        assertEquals(42, ticket.id)
        assertEquals("Detail ticket", ticket.title)
        coVerify { ticketDao.insertTicket(any()) }
    }
}
