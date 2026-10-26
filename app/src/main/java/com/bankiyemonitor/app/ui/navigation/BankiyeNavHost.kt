package com.bankiyemonitor.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.bankiyemonitor.app.ui.screens.dashboard.DashboardScreen
import com.bankiyemonitor.app.ui.screens.transactions.TransactionsScreen
import com.bankiyemonitor.app.ui.screens.security.SecurityScreen
import com.bankiyemonitor.app.ui.screens.settings.SettingsScreen
import com.bankiyemonitor.app.ui.screens.detail.SmsDetailScreen
import com.bankiyemonitor.app.ui.screens.summary.SummaryScreen

@Composable
fun BankiyeNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToTransactions = { navController.navigate(Screen.Transactions.route) },
                onNavigateToSecurity = { navController.navigate(Screen.Security.route) },
                onNavigateToSummary = { periodType ->
                    navController.navigate(Screen.Summary.createRoute(periodType))
                },
                onNavigateToSmsDetail = { smsId ->
                    navController.navigate(Screen.SmsDetail.createRoute(smsId))
                }
            )
        }
        
        composable(Screen.Transactions.route) {
            TransactionsScreen(
                onNavigateToSmsDetail = { smsId ->
                    navController.navigate(Screen.SmsDetail.createRoute(smsId))
                }
            )
        }
        
        composable(Screen.Security.route) {
            SecurityScreen(
                onNavigateToSmsDetail = { smsId ->
                    navController.navigate(Screen.SmsDetail.createRoute(smsId))
                }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen()
        }

        composable(
            route = Screen.SmsDetail.route,
            arguments = listOf(navArgument("smsId") { type = NavType.LongType })
        ) { backStackEntry ->
            val smsId = backStackEntry.arguments?.getLong("smsId") ?: return@composable
            SmsDetailScreen(
                smsId = smsId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Summary.route,
            arguments = listOf(navArgument("periodType") { type = NavType.StringType })
        ) { backStackEntry ->
            val periodType = backStackEntry.arguments?.getString("periodType") ?: "WEEKLY"
            SummaryScreen(
                periodType = periodType,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
