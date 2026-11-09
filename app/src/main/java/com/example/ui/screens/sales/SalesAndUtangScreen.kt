package com.example.ui.screens.sales

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.screens.utang.UtangScreen
import com.example.ui.screens.utang.UtangViewModel
import com.example.ui.theme.*

enum class SalesSubTab {
    SALES,
    UTANG
}

@Composable
fun SalesAndUtangScreen(
    salesViewModel: SalesViewModel,
    utangViewModel: UtangViewModel,
    initialSubTab: SalesSubTab = SalesSubTab.SALES,
    modifier: Modifier = Modifier
) {
    var activeSubTab by remember { mutableStateOf(initialSubTab) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Cream)
    ) {
        // Top Segmented Bar to switch between Sales and Utang
        Surface(
            color = CardCream,
            shadowElevation = 1.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Cream)
                        .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TabTogglePill(
                        title = "🛒 Sales & Cart",
                        isSelected = activeSubTab == SalesSubTab.SALES,
                        onClick = { activeSubTab = SalesSubTab.SALES },
                        modifier = Modifier.weight(1f),
                        testTag = "subtab_sales"
                    )

                    TabTogglePill(
                        title = "📝 Utang Ledger",
                        isSelected = activeSubTab == SalesSubTab.UTANG,
                        onClick = { activeSubTab = SalesSubTab.UTANG },
                        modifier = Modifier.weight(1f),
                        testTag = "subtab_utang"
                    )
                }
            }
        }

        // Animated Content
        Box(modifier = Modifier.weight(1f)) {
            when (activeSubTab) {
                SalesSubTab.SALES -> SalesScreen(viewModel = salesViewModel)
                SalesSubTab.UTANG -> UtangScreen(viewModel = utangViewModel)
            }
        }
    }
}

@Composable
private fun TabTogglePill(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) Forest else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = TindaTypography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) CreamLight else Muted
        )
    }
}
