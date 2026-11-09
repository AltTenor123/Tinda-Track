package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class MainTab(
    val title: String,
    val icon: ImageVector,
    val testTag: String
) {
    HOME("Home", Icons.Default.Dashboard, "tab_home"),
    INVENTORY("Inventory", Icons.Default.Inventory2, "tab_inventory"),
    SCAN("Scan", Icons.Default.QrCodeScanner, "tab_scan"),
    SALES("Sales & Utang", Icons.Default.PointOfSale, "tab_sales"),
    MORE("More", Icons.Default.Menu, "tab_more")
}

sealed class Screen {
    object Onboarding : Screen()
    object Main : Screen()
}
