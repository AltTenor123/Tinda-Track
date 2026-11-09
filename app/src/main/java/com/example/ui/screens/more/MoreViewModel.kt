package com.example.ui.screens.more

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.seeder.SampleDataSeeder
import com.example.domain.model.Product
import com.example.domain.model.Sale
import com.example.domain.model.Utang
import com.example.domain.repository.ProductRepository
import com.example.domain.repository.SalesRepository
import com.example.domain.repository.SettingsRepository
import com.example.domain.repository.UtangRepository
import com.example.domain.usecase.ExpiryAlertItem
import com.example.domain.usecase.ExpiryAlertUseCase
import com.example.domain.usecase.ReorderAlertUseCase
import com.example.domain.usecase.SupplierReorderGroup
import com.example.platform.CsvManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class MoreSubTab {
    REORDER,
    EXPIRY,
    DATA_BACKUP,
    SETTINGS,
    ABOUT
}

data class MoreUiState(
    val isLoading: Boolean = true,
    val activeSubTab: MoreSubTab = MoreSubTab.REORDER,
    val reorderGroups: List<SupplierReorderGroup> = emptyList(),
    val expiryAlerts: List<ExpiryAlertItem> = emptyList(),
    val totalLowStockProducts: Int = 0,
    val totalExpiringProducts: Int = 0,
    val themeSetting: String = "SYSTEM",
    val isPasscodeEnabled: Boolean = false,
    val isBiometricEnabled: Boolean = true,
    val snackbarMessage: String? = null
)

class MoreViewModel(
    private val productRepository: ProductRepository,
    private val salesRepository: SalesRepository,
    private val utangRepository: UtangRepository,
    private val settingsRepository: SettingsRepository,
    private val reorderAlertUseCase: ReorderAlertUseCase,
    private val expiryAlertUseCase: ExpiryAlertUseCase,
    private val dbHelper: com.example.data.db.TindaDatabaseHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(MoreUiState())
    val uiState: StateFlow<MoreUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun setSubTab(tab: MoreSubTab) {
        _uiState.update { it.copy(activeSubTab = tab) }
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val reorders = reorderAlertUseCase.getReorderAlerts()
            val expiries = expiryAlertUseCase.getExpiryAlerts()
            val theme = settingsRepository.getTheme()
            val passcode = settingsRepository.isPasscodeEnabled()

            _uiState.update {
                it.copy(
                    isLoading = false,
                    reorderGroups = reorders,
                    expiryAlerts = expiries,
                    totalLowStockProducts = reorders.sumOf { g -> g.items.size },
                    totalExpiringProducts = expiries.size,
                    themeSetting = theme,
                    isPasscodeEnabled = passcode
                )
            }
        }
    }

    suspend fun getFormattedReorderText(): String {
        return reorderAlertUseCase.formatReorderText()
    }

    fun togglePasscode(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setPasscodeEnabled(enabled)
            _uiState.update {
                it.copy(
                    isPasscodeEnabled = enabled,
                    snackbarMessage = if (enabled) "App PIN lock enabled (Default: 1234)" else "App PIN lock disabled"
                )
            }
        }
    }

    fun setTheme(theme: String) {
        viewModelScope.launch {
            settingsRepository.setTheme(theme)
            _uiState.update { it.copy(themeSetting = theme, snackbarMessage = "Theme updated to $theme") }
        }
    }

    fun exportProducts(context: Context) {
        viewModelScope.launch {
            val products = productRepository.getAllProducts()
            val csv = CsvManager.exportProductsToCsv(products)
            CsvManager.shareCsvFile(context, "tinda_track_products.csv", csv, "Tinda Track Products Backup")
        }
    }

    fun exportSales(context: Context) {
        viewModelScope.launch {
            val sales = salesRepository.getAllSales()
            val csv = CsvManager.exportSalesToCsv(sales)
            CsvManager.shareCsvFile(context, "tinda_track_sales.csv", csv, "Tinda Track Sales Backup")
        }
    }

    fun exportUtang(context: Context) {
        viewModelScope.launch {
            val utang = utangRepository.getAllUtang()
            val csv = CsvManager.exportUtangToCsv(utang)
            CsvManager.shareCsvFile(context, "tinda_track_utang.csv", csv, "Tinda Track Utang Ledger Backup")
        }
    }

    fun importProductsCsv(csvContent: String) {
        viewModelScope.launch {
            val parsed = CsvManager.parseProductsCsv(csvContent)
            if (parsed.isEmpty()) {
                _uiState.update { it.copy(snackbarMessage = "No valid products found in CSV") }
                return@launch
            }
            for (p in parsed) {
                productRepository.insertProduct(p)
            }
            loadData()
            _uiState.update { it.copy(snackbarMessage = "Imported ${parsed.size} products successfully!") }
        }
    }

    fun restoreSampleData(context: Context) {
        viewModelScope.launch {
            SampleDataSeeder.restoreSampleData(dbHelper)
            loadData()
            _uiState.update { it.copy(snackbarMessage = "Restored 35 items, 50 sales, and 5 utang records!") }
        }
    }

    fun dismissSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
