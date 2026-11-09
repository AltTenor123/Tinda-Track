package com.example.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.TindaDatabaseHelper
import com.example.data.seeder.SampleDataSeeder
import com.example.domain.model.*
import com.example.domain.repository.ProductRepository
import com.example.domain.repository.SalesRepository
import com.example.domain.repository.UtangRepository
import com.example.domain.usecase.ExpiryAlertItem
import com.example.domain.usecase.ExpiryAlertUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class HomeUiState(
    val isLoading: Boolean = true,
    val todaySalesCentavos: Long = 0L,
    val todayProfitCentavos: Long = 0L,
    val todayTransactionsCount: Int = 0,
    val stockRetailValueCentavos: Long = 0L,
    val stockCostValueCentavos: Long = 0L,
    val totalProductsCount: Int = 0,
    val lowStockCount: Int = 0,
    val outOfStockCount: Int = 0,
    val expiringSoonCount: Int = 0,
    val openUtangTotalCentavos: Long = 0L,
    val openUtangCount: Int = 0,
    val recentSales: List<Sale> = emptyList(),
    val recentMovements: List<StockMovement> = emptyList(),
    val expiringAlerts: List<ExpiryAlertItem> = emptyList(),
    val productMap: Map<Long, Product> = emptyMap()
)

class HomeViewModel(
    private val productRepository: ProductRepository,
    private val salesRepository: SalesRepository,
    private val utangRepository: UtangRepository,
    private val expiryAlertUseCase: ExpiryAlertUseCase,
    private val dbHelper: TindaDatabaseHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val dateOnlyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Ensure sample data exists on first boot
            SampleDataSeeder.seedIfEmpty(dbHelper)

            combine(
                productRepository.getAllProductsFlow(),
                salesRepository.getAllSalesFlow(),
                utangRepository.getAllUtangFlow()
            ) { products, sales, utangs ->
                val todayStr = dateOnlyFormat.format(Date())
                val todaySales = sales.filter { it.createdAt.startsWith(todayStr) }

                val todaySalesTotal = todaySales.sumOf { it.total }
                val todayProfitTotal = todaySales.sumOf { it.estimatedProfit }
                val stockRetailTotal = products.sumOf { it.totalStockValueRetail }
                val stockCostTotal = products.sumOf { it.totalStockValueCost }

                val lowCount = products.count { it.status == StockStatus.LOW_STOCK }
                val outCount = products.count { it.status == StockStatus.OUT_OF_STOCK }
                val expiringItems = expiryAlertUseCase.getExpiryAlerts()

                val openUtangTotal = utangs.filter { it.status != UtangStatus.Paid }.sumOf { it.balance }
                val openUtangCount = utangs.count { it.status != UtangStatus.Paid }

                val movements = productRepository.getStockMovements(limit = 6)
                val prodMap = products.associateBy { it.id }

                HomeUiState(
                    isLoading = false,
                    todaySalesCentavos = todaySalesTotal,
                    todayProfitCentavos = todayProfitTotal,
                    todayTransactionsCount = todaySales.size,
                    stockRetailValueCentavos = stockRetailTotal,
                    stockCostValueCentavos = stockCostTotal,
                    totalProductsCount = products.size,
                    lowStockCount = lowCount,
                    outOfStockCount = outCount,
                    expiringSoonCount = expiringItems.size,
                    openUtangTotalCentavos = openUtangTotal,
                    openUtangCount = openUtangCount,
                    recentSales = sales.take(5),
                    recentMovements = movements,
                    expiringAlerts = expiringItems.take(5),
                    productMap = prodMap
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun restoreSampleData(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            SampleDataSeeder.restoreSampleData(dbHelper)
            loadData()
            onComplete()
        }
    }
}
