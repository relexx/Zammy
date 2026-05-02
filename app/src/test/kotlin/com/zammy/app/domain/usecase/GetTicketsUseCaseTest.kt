package com.zammy.app.domain.usecase

import com.zammy.app.domain.model.Ticket
import com.zammy.app.domain.repository.TicketRepository
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

class GetTicketsUseCaseTest {

    private lateinit var ticketRepository: TicketRepository
    private lateinit var useCase: GetTicketsUseCase

    private val fakeTickets = listOf(
        Ticket(
            id = 1, number = 101, title = "Bug in login", state = "open",
            priority = "high", group = "Dev", ownerId = 3,
            customerId = 10, createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-02T00:00:00Z", articleCount = 1
        ),
        Ticket(
            id = 2, number = 102, title = "Feature request", state = "pending reminder",
            priority = "normal", group = "Product", ownerId = null,
            customerId = 11, createdAt = "2024-01-03T00:00:00Z",
            updatedAt = "2024-01-04T00:00:00Z", articleCount = 0
        )
    )

    @Before
    fun setup() {
        ticketRepository = mockk()
        useCase = GetTicketsUseCase(ticketRepository)
    }

    @Test
    fun `invoke returns flow from repository`() = runTest {
        every { ticketRepository.getTickets(null) } returns flowOf(fakeTickets)

        val result = useCase(null).first()

        assertEquals(2, result.size)
        assertEquals("Bug in login", result[0].title)
    }

    @Test
    fun `invoke with state filters correctly`() = runTest {
        val openTickets = fakeTickets.filter { it.state == "open" }
        every { ticketRepository.getTickets("open") } returns flowOf(openTickets)

        val result = useCase("open").first()

        assertEquals(1, result.size)
        assertEquals("open", result[0].state)
    }

    @Test
    fun `refresh calls repository`() = runTest {
        coEvery { ticketRepository.refreshTickets() } returns Result.success(Unit)

        val result = useCase.refresh()

        assertTrue(result.isSuccess)
        coVerify { ticketRepository.refreshTickets() }
    }

    @Test
    fun `refresh propagates failure`() = runTest {
        val error = RuntimeException("Server error")
        coEvery { ticketRepository.refreshTickets() } returns Result.failure(error)

        val result = useCase.refresh()

        assertTrue(result.isFailure)
        assertEquals("Server error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `search delegates to repository`() = runTest {
        coEvery { ticketRepository.searchTickets("bug") } returns Result.success(
            listOf(fakeTickets[0])
        )

        val result = useCase.search("bug")

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("Bug in login", result.getOrNull()?.first()?.title)
    }
}
