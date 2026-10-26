package com.bankiyemonitor.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Dashboard : Screen(
        route = "dashboard",
        title = "Dashboard",
        selectedIcon = Icons.Filled.Dashboard,
        unselectedIcon = Icons.Outlined.Dashboard
    )
    
    data object Transactions : Screen(
        route = "transactions",
        title = "Transactions",
        selectedIcon = Icons.Filled.Receipt,
        unselectedIcon = Icons.Outlined.Receipt
    )
    
    data object Security : Screen(
        route = "security",
        title = "Security",
        selectedIcon = Icons.Filled.Shield,
        unselectedIcon = Icons.Outlined.Shield
    )
    
    data object Settings : Screen(
        route = "settings",
        title = "Settings",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )

    data object SmsDetail : Screen(
        route = "sms_detail/{smsId}",
        title = "SMS Detail",
        selectedIcon = Icons.Filled.Email,
        unselectedIcon = Icons.Outlined.Email
    ) {
        fun createRoute(smsId: Long) = "sms_detail/$smsId"
    }

    data object Summary : Screen(
        route = "summary/{periodType}",
        title = "Summary",
        selectedIcon = Icons.Filled.Summarize,
        unselectedIcon = Icons.Outlined.Summarize
    ) {
        fun createRoute(periodType: String) = "summary/$periodType"
    }
}

val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Transactions,
    Screen.Security,
    Screen.Settings
)
