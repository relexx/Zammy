package com.zammy.app.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zammy.app.domain.repository.SettingsRepository
import com.zammy.app.domain.repository.TicketRepository
import com.zammy.app.util.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class TicketSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val ticketRepository: TicketRepository,
    private val settingsRepository: SettingsRepository,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        if (!settingsRepository.isLoggedIn()) {
            return Result.success()
        }

        return try {
            val lastSyncTime = settingsRepository.getLastSyncTimestamp()

            // Get current tickets before refresh
            val ticketsBefore = ticketRepository.getTickets()
                .first()
                .associateBy { it.id }

            // Refresh from server
            ticketRepository.refreshTickets().getOrThrow()

            // Get updated tickets
            val ticketsAfter = ticketRepository.getTickets()
                .first()

            // Notify about new tickets
            ticketsAfter.forEach { ticket ->
                val existing = ticketsBefore[ticket.id]
                if (existing == null && lastSyncTime > 0) {
                    notificationHelper.showNewTicketNotification(ticket.id, ticket.title)
                } else if (existing != null && existing.updatedAt != ticket.updatedAt) {
                    notificationHelper.showTicketUpdateNotification(ticket.id)
                }
            }

            settingsRepository.setLastSyncTimestamp(System.currentTimeMillis())
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "ticket_sync_worker"
    }
}
