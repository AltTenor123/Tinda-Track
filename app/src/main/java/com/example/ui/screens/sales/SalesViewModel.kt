package com.example.ui.screens.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.CartItem
import com.example.domain.model.CartManager
import com.example.domain.model.CurrencyHelper
import com.example.domain.model.Sale
import com.example.domain.repository.SalesRepository
import com.example.domain.repository.SalesSummary
import com.example.domain.usecase.CheckoutResult
import com.example.domain.usecase.CheckoutUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SalesUiState(
    val isLoading: Boolean = true,
    val allSales: List<Sale> = emptyList(),
    val filteredSales: List<Sale> = emptyList(),
    val groupedSales: Map<String, List<Sale>> = emptyMap(),
    val todaySummary: SalesSummary = SalesSummary(0L, 0L, 0),
    val totalSummary: SalesSummary = SalesSummary(0L, 0L, 0),
    val searchQuery: String = "",
    val activeReceiptSale: Sale? = null,
    val isCartOpen: Boolean = false,
    val checkoutError: String? = null,
    val isCheckingOut: Boolean = false,
    val snackbarMessage: String? = null
)

class SalesViewModel(
    private val salesRepository: SalesRepository,
    private val checkoutUseCase: CheckoutUseCase,
    val cartManager: CartManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SalesUiState())
    val uiState: StateFlow<SalesUiState> = _uiState.asStateFlow()

    init {
        loadSalesData()
    }

    private fun loadSalesData() {
        viewModelScope.launch {
            salesRepository.getAllSalesFlow().collect { sales ->
                val todaySummary = salesRepository.getTodaySalesSummary()
                val totalSummary = salesRepository.getTotalSalesSummary()
                val grouped = groupSalesByDay(sales)

                _uiState.update { current ->
                    val filtered = applyFilter(sales, current.searchQuery)
                    current.copy(
                        isLoading = false,
                        allSales = sales,
                        filteredSales = filtered,
                        groupedSales = groupSalesByDay(filtered),
                        todaySummary = todaySummary,
                        totalSummary = totalSummary
                    )
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { current ->
            val filtered = applyFilter(current.allSales, query)
            current.copy(
                searchQuery = query,
                filteredSales = filtered,
                groupedSales = groupSalesByDay(filtered)
            )
        }
    }

    private fun applyFilter(sales: List<Sale>, query: String): List<Sale> {
        val q = query.trim().lowercase()
        if (q.isBlank()) return sales
        return sales.filter { sale ->
            sale.receiptNo.lowercase().contains(q) ||
                    sale.cashier.lowercase().contains(q) ||
                    sale.items.any { it.productName.lowercase().contains(q) }
        }
    }

    private fun groupSalesByDay(sales: List<Sale>): Map<String, List<Sale>> {
        val now = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        return sales.groupBy { sale ->
            val datePart = sale.createdAt.take(10)
            when (datePart) {
                now -> "Today"
                else -> datePart
            }
        }
    }

    fun openCart() {
        _uiState.update { it.copy(isCartOpen = true, checkoutError = null) }
    }

    fun closeCart() {
        _uiState.update { it.copy(isCartOpen = false, checkoutError = null) }
    }

    fun performCheckout(cashReceivedCentavos: Long) {
        val items = cartManager.items.value
        if (items.isEmpty()) {
            _uiState.update { it.copy(checkoutError = "Cart is empty") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingOut = true, checkoutError = null) }
            val result = checkoutUseCase(
                items = items,
                cashReceivedCentavos = cashReceivedCentavos,
                cashier = "Nanay Rosa"
            )

            when (result) {
                is CheckoutResult.Success -> {
                    cartManager.clearCart()
                    _uiState.update {
                        it.copy(
                            isCheckingOut = false,
                            isCartOpen = false,
                            activeReceiptSale = result.sale,
                            snackbarMessage = "Sale completed! Receipt: ${result.sale.receiptNo}"
                        )
                    }
                }
                is CheckoutResult.InsufficientPayment -> {
                    val req = CurrencyHelper.formatCentavos(result.requiredCentavos)
                    val rec = CurrencyHelper.formatCentavos(result.receivedCentavos)
                    _uiState.update {
                        it.copy(
                            isCheckingOut = false,
                            checkoutError = "Insufficient cash: Received $rec, Total is $req"
                        )
                    }
                }
                is CheckoutResult.InsufficientStock -> {
                    _uiState.update {
                        it.copy(
                            isCheckingOut = false,
                            checkoutError = "Insufficient stock for '${result.productName}'! Only ${result.available} pcs available (requested ${result.requested})."
                        )
                    }
                }
                is CheckoutResult.EmptyCart -> {
                    _uiState.update { it.copy(isCheckingOut = false, checkoutError = "Cart is empty") }
                }
                is CheckoutResult.Error -> {
                    _uiState.update { it.copy(isCheckingOut = false, checkoutError = result.message) }
                }
            }
        }
    }

    fun viewReceipt(sale: Sale) {
        _uiState.update { it.copy(activeReceiptSale = sale) }
    }

    fun dismissReceipt() {
        _uiState.update { it.copy(activeReceiptSale = null) }
    }

    fun dismissSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
