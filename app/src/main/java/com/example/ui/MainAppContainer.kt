package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Product
import com.example.domain.repository.SettingsRepository
import com.example.ui.components.PinLockDialog
import com.example.ui.navigation.MainTab
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.home.HomeViewModel
import com.example.ui.screens.inventory.InventoryScreen
import com.example.ui.screens.inventory.InventoryViewModel
import com.example.ui.screens.onboarding.OnboardingScreen
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun MainAppContainer(
    onToggleTheme: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val settingsRepository: SettingsRepository = koinInject()
    val homeViewModel: HomeViewModel = koinViewModel()
    val inventoryViewModel: InventoryViewModel = koinViewModel()
    val coroutineScope = rememberCoroutineScope()
    val colors = LocalTindaColors.current

    var isOnboardingDone by remember { mutableStateOf<Boolean?>(null) }
    var isAppLocked by remember { mutableStateOf(false) }
    var currentTab by remember { mutableStateOf(MainTab.HOME) }
    var tabBackStack by remember { mutableStateOf(listOf(MainTab.HOME)) }

    // Sub-navigation triggers
    var initialInventoryFilter by remember { mutableStateOf<String?>(null) }
    var selectedProductForQr by remember { mutableStateOf<Product?>(null) }
    var initialSalesSubTab by remember { mutableStateOf(com.example.ui.screens.sales.SalesSubTab.SALES) }

    // Load initial settings
    LaunchedEffect(Unit) {
        val done = settingsRepository.isOnboardingDone()
        val passcodeEnabled = settingsRepository.isPasscodeEnabled()
        isOnboardingDone = done
        isAppLocked = passcodeEnabled
    }

    // BackHandler to handle custom state stack
    BackHandler(enabled = currentTab != MainTab.HOME || selectedProductForQr != null) {
        if (selectedProductForQr != null) {
            selectedProductForQr = null
        } else if (tabBackStack.size > 1) {
            val updated = tabBackStack.dropLast(1)
            tabBackStack = updated
            currentTab = updated.last()
        } else {
            currentTab = MainTab.HOME
        }
    }

    fun navigateToTab(tab: MainTab) {
        if (tab != currentTab) {
            tabBackStack = tabBackStack + tab
            currentTab = tab
        }
    }

    if (isAppLocked) {
        PinLockDialog(
            correctPin = "1234",
            onSuccess = { isAppLocked = false },
            isBiometricAvailable = true,
            onBiometricClick = { isAppLocked = false }
        )
    }

    if (isOnboardingDone == false) {
        OnboardingScreen(
            onFinish = {
                coroutineScope.launch {
                    settingsRepository.setOnboardingDone(true)
                    isOnboardingDone = true
                }
            }
        )
    } else if (isOnboardingDone == true) {
        BoxWithConstraints(modifier = modifier.fillMaxSize()) {
            val isExpanded = maxWidth >= 600.dp

            Row(modifier = Modifier.fillMaxSize()) {
                // Adaptive Navigation Rail for Tablets / Wide screens
                if (isExpanded) {
                    NavigationRail(
                        containerColor = colors.cardCream,
                        contentColor = colors.ink,
                        header = {
                            Box(
                                modifier = Modifier
                                    .padding(vertical = 16.dp)
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(colors.forest),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "₱",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FrauncesFontFamily
                                )
                            }
                        }
                    ) {
                        MainTab.values().forEach { tab ->
                            NavigationRailItem(
                                selected = currentTab == tab,
                                onClick = { navigateToTab(tab) },
                                icon = {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.title
                                    )
                                },
                                label = { Text(tab.title) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    selectedTextColor = colors.forest,
                                    indicatorColor = colors.forest,
                                    unselectedIconColor = colors.muted,
                                    unselectedTextColor = colors.muted
                                )
                            )
                        }
                    }
                }

                // Main Screen Content
                Scaffold(
                    modifier = Modifier.weight(1f),
                    containerColor = colors.cream,
                    bottomBar = {
                        if (!isExpanded) {
                            TindaBottomNavBar(
                                currentTab = currentTab,
                                onTabSelected = { navigateToTab(it) }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = if (!isExpanded) innerPadding.calculateBottomPadding() else 0.dp)
                    ) {
                        when (currentTab) {
                            MainTab.HOME -> HomeScreen(
                                viewModel = homeViewModel,
                                onNavigateToInventory = { filter ->
                                    initialInventoryFilter = filter
                                    navigateToTab(MainTab.INVENTORY)
                                },
                                onNavigateToScan = { navigateToTab(MainTab.SCAN) },
                                onNavigateToSales = {
                                    initialSalesSubTab = com.example.ui.screens.sales.SalesSubTab.SALES
                                    navigateToTab(MainTab.SALES)
                                },
                                onNavigateToUtang = {
                                    initialSalesSubTab = com.example.ui.screens.sales.SalesSubTab.UTANG
                                    navigateToTab(MainTab.SALES)
                                },
                                onNavigateToMore = { navigateToTab(MainTab.MORE) },
                                onOpenAddProduct = {
                                    inventoryViewModel.openProductForm(null)
                                    navigateToTab(MainTab.INVENTORY)
                                },
                                onToggleTheme = onToggleTheme
                            )

                            MainTab.INVENTORY -> InventoryScreen(
                                viewModel = inventoryViewModel,
                                initialStatusFilter = initialInventoryFilter,
                                onNavigateToQrLabel = { product ->
                                    selectedProductForQr = product
                                    navigateToTab(MainTab.SCAN)
                                }
                            )

                            MainTab.SCAN -> {
                                val scanViewModel: com.example.ui.screens.scan.ScanViewModel = koinViewModel()
                                val cartManager: com.example.domain.model.CartManager = koinInject()
                                com.example.ui.screens.scan.ScanScreen(
                                    viewModel = scanViewModel,
                                    cartManager = cartManager,
                                    onNavigateToCheckout = {
                                        initialSalesSubTab = com.example.ui.screens.sales.SalesSubTab.SALES
                                        navigateToTab(MainTab.SALES)
                                    },
                                    onAddNewProductWithCode = { scannedCode ->
                                        inventoryViewModel.openProductFormWithQr(scannedCode)
                                        navigateToTab(MainTab.INVENTORY)
                                    },
                                    onAdjustStock = { product ->
                                        inventoryViewModel.openStockAdjustment(product)
                                        navigateToTab(MainTab.INVENTORY)
                                    }
                                )
                            }

                            MainTab.SALES -> {
                                val salesViewModel: com.example.ui.screens.sales.SalesViewModel = koinViewModel()
                                val utangViewModel: com.example.ui.screens.utang.UtangViewModel = koinViewModel()
                                com.example.ui.screens.sales.SalesAndUtangScreen(
                                    salesViewModel = salesViewModel,
                                    utangViewModel = utangViewModel,
                                    initialSubTab = initialSalesSubTab
                                )
                            }

                            MainTab.MORE -> {
                                val moreViewModel: com.example.ui.screens.more.MoreViewModel = koinViewModel()
                                com.example.ui.screens.more.MoreScreen(viewModel = moreViewModel)
                            }
                        }
                    }
                }
            }

            // QR Label Dialog for single product
            selectedProductForQr?.let { product ->
                com.example.ui.screens.scan.ProductQrLabelDialog(
                    product = product,
                    onDismiss = { selectedProductForQr = null }
                )
            }
        }
    }
}

@Composable
fun TindaBottomNavBar(
    currentTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalTindaColors.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .border(
                1.dp,
                colors.cardBorder,
                RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = colors.cardCream
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 6.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tab 1: Home
            BottomTabItem(
                tab = MainTab.HOME,
                isSelected = currentTab == MainTab.HOME,
                onClick = { onTabSelected(MainTab.HOME) }
            )

            // Tab 2: Inventory
            BottomTabItem(
                tab = MainTab.INVENTORY,
                isSelected = currentTab == MainTab.INVENTORY,
                onClick = { onTabSelected(MainTab.INVENTORY) }
            )

            // Tab 3: Raised Center Scan Button
            Box(
                modifier = Modifier
                    .offset(y = (-14).dp)
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(colors.forest)
                    .border(3.dp, colors.cream, CircleShape)
                    .clickable { onTabSelected(MainTab.SCAN) }
                    .testTag("raised_scan_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = "Scan Barcode / QR",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Tab 4: Sales & Utang
            BottomTabItem(
                tab = MainTab.SALES,
                isSelected = currentTab == MainTab.SALES,
                onClick = { onTabSelected(MainTab.SALES) }
            )

            // Tab 5: More
            BottomTabItem(
                tab = MainTab.MORE,
                isSelected = currentTab == MainTab.MORE,
                onClick = { onTabSelected(MainTab.MORE) }
            )
        }
    }
}

@Composable
fun BottomTabItem(
    tab: MainTab,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalTindaColors.current
    val itemColor = if (isSelected) colors.forest else colors.muted

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag(tab.testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(
            imageVector = tab.icon,
            contentDescription = tab.title,
            tint = itemColor,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = tab.title,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = itemColor,
            fontFamily = DmSansFontFamily
        )
    }
}
