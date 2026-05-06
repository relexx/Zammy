package com.zammy.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.zammy.app.presentation.createticket.CreateTicketScreen
import com.zammy.app.presentation.editticket.EditTicketScreen
import com.zammy.app.presentation.login.LoginScreen
import com.zammy.app.presentation.settings.SettingsScreen
import com.zammy.app.presentation.ticketdetail.TicketDetailScreen
import com.zammy.app.presentation.tickets.TicketsScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Tickets : Screen("tickets")
    object TicketDetail : Screen("ticket_detail/{ticketId}") {
        fun createRoute(ticketId: Int) = "ticket_detail/$ticketId"
    }
    object EditTicket : Screen("edit_ticket/{ticketId}") {
        fun createRoute(ticketId: Int) = "edit_ticket/$ticketId"
    }
    object CreateTicket : Screen("create_ticket")
    object Settings : Screen("settings")
}

@Composable
fun AppNavigation(initialTicketId: Int? = null) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Tickets.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                    if (initialTicketId != null) {
                        navController.navigate(Screen.TicketDetail.createRoute(initialTicketId))
                    }
                }
            )
        }

        composable(Screen.Tickets.route) {
            LaunchedEffect(initialTicketId) {
                if (initialTicketId != null) {
                    navController.navigate(Screen.TicketDetail.createRoute(initialTicketId))
                }
            }
            TicketsScreen(
                onTicketClick = { ticketId ->
                    navController.navigate(Screen.TicketDetail.createRoute(ticketId))
                },
                onCreateTicket = {
                    navController.navigate(Screen.CreateTicket.route)
                },
                onSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(
            route = Screen.TicketDetail.route,
            arguments = listOf(navArgument("ticketId") { type = NavType.IntType })
        ) { backStackEntry ->
            val ticketId = backStackEntry.arguments?.getInt("ticketId") ?: return@composable
            TicketDetailScreen(
                ticketId = ticketId,
                onNavigateBack = { navController.popBackStack() },
                onEditTicket = { id ->
                    navController.navigate(Screen.EditTicket.createRoute(id))
                }
            )
        }

        composable(
            route = Screen.EditTicket.route,
            arguments = listOf(navArgument("ticketId") { type = NavType.IntType })
        ) { backStackEntry ->
            val ticketId = backStackEntry.arguments?.getInt("ticketId") ?: return@composable
            EditTicketScreen(
                ticketId = ticketId,
                onNavigateBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable(Screen.CreateTicket.route) {
            CreateTicketScreen(
                onNavigateBack = { navController.popBackStack() },
                onTicketCreated = { ticketId ->
                    navController.popBackStack()
                    navController.navigate(Screen.TicketDetail.createRoute(ticketId))
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
