package com.example.ui.screens.utang

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.CurrencyHelper
import com.example.domain.model.Utang
import com.example.domain.model.UtangPayment
import com.example.domain.model.UtangStatus
import com.example.domain.repository.UtangRepository
import com.example.domain.usecase.UtangPaymentResult
import com.example.domain.usecase.UtangPaymentUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class UtangUiState(
    val isLoading: Boolean = true,
    val allUtang: List<Utang> = emptyList(),
    val filteredUtang: List<Utang> = emptyList(),
    val openCreditTotalCentavos: Long = 0L,
    val selectedStatusFilter: UtangStatus? = null,
    val searchQuery: String = "",
    val activeUtangForPayment: Utang? = null,
    val isAddUtangOpen: Boolean = false,
    val paymentHistoryForActive: List<UtangPayment> = emptyList(),
    val paymentErrorMessage: String? = null,
    val snackbarMessage: String? = null
)

class UtangViewModel(
    private val utangRepository: UtangRepository,
    private val paymentUseCase: UtangPaymentUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(UtangUiState())
    val uiState: StateFlow<UtangUiState> = _uiState.asStateFlow()

    init {
        loadUtangData()
    }

    private fun loadUtangData() {
        viewModelScope.launch {
            utangRepository.getAllUtangFlow().collect { list ->
                val openTotal = utangRepository.getOpenUtangTotal()
                _uiState.update { current ->
                    val filtered = applyFilters(
                        list = list,
                        query = current.searchQuery,
                        status = current.selectedStatusFilter
                    )
                    current.copy(
                        isLoading = false,
                        allUtang = list,
                        filteredUtang = filtered,
                        openCreditTotalCentavos = openTotal
                    )
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { current ->
            val filtered = applyFilters(current.allUtang, query, current.selectedStatusFilter)
            current.copy(searchQuery = query, filteredUtang = filtered)
        }
    }

    fun onStatusFilterChanged(status: UtangStatus?) {
        _uiState.update { current ->
            val filtered = applyFilters(current.allUtang, current.searchQuery, status)
            current.copy(selectedStatusFilter = status, filteredUtang = filtered)
        }
    }

    private fun applyFilters(
        list: List<Utang>,
        query: String,
        status: UtangStatus?
    ): List<Utang> {
        val q = query.trim().lowercase()
        return list.filter { item ->
            val matchesQuery = q.isEmpty() ||
                    item.customerName.lowercase().contains(q) ||
                    item.itemsNote.lowercase().contains(q)
            val matchesStatus = status == null || item.status == status
            matchesQuery && matchesStatus
        }
    }

    fun openAddUtang() {
        _uiState.update { it.copy(isAddUtangOpen = true) }
    }

    fun closeAddUtang() {
        _uiState.update { it.copy(isAddUtangOpen = false) }
    }

    fun createUtang(customerName: String, itemsNote: String, amountCentavos: Long) {
        if (customerName.isBlank() || amountCentavos <= 0) {
            _uiState.update { it.copy(snackbarMessage = "Please enter customer name and valid amount") }
            return
        }

        viewModelScope.launch {
            utangRepository.createUtang(
                customerName = customerName.trim(),
                itemsNote = itemsNote.trim(),
                amount = amountCentavos
            )
            _uiState.update {
                it.copy(
                    isAddUtangOpen = false,
                    snackbarMessage = "Recorded utang for ${customerName.trim()} (${CurrencyHelper.formatCentavos(amountCentavos)})"
                )
            }
        }
    }

    fun openRecordPayment(utang: Utang) {
        viewModelScope.launch {
            val payments = utangRepository.getPaymentsForUtang(utang.id)
            _uiState.update {
                it.copy(
                    activeUtangForPayment = utang,
                    paymentHistoryForActive = payments,
                    paymentErrorMessage = null
                )
            }
        }
    }

    fun closeRecordPayment() {
        _uiState.update {
            it.copy(
                activeUtangForPayment = null,
                paymentHistoryForActive = emptyList(),
                paymentErrorMessage = null
            )
        }
    }

    fun submitPayment(amountCentavos: Long) {
        val active = _uiState.value.activeUtangForPayment ?: return

        viewModelScope.launch {
            val result = paymentUseCase(active.id, amountCentavos)
            when (result) {
                is UtangPaymentResult.InvalidAmount -> {
                    _uiState.update { it.copy(paymentErrorMessage = "Please enter a valid payment amount") }
                }
                is UtangPaymentResult.AlreadyFullyPaid -> {
                    _uiState.update { it.copy(paymentErrorMessage = "This account is already fully paid!") }
                }
                is UtangPaymentResult.OverpaymentRejected -> {
                    val rem = CurrencyHelper.formatCentavos(result.maxAllowedCentavos)
                    val entered = CurrencyHelper.formatCentavos(result.attemptedCentavos)
                    _uiState.update {
                        it.copy(
                            paymentErrorMessage = "Cannot overpay! Entered $entered exceeds remaining balance of $rem"
                        )
                    }
                }
                is UtangPaymentResult.UtangNotFound -> {
                    _uiState.update { it.copy(paymentErrorMessage = "Utang record not found") }
                }
                is UtangPaymentResult.Error -> {
                    _uiState.update { it.copy(paymentErrorMessage = result.message) }
                }
                is UtangPaymentResult.Success -> {
                    _uiState.update {
                        it.copy(
                            activeUtangForPayment = null,
                            paymentErrorMessage = null,
                            snackbarMessage = "Recorded payment of ${CurrencyHelper.formatCentavos(amountCentavos)} from ${result.updatedUtang.customerName}!"
                        )
                    }
                }
            }
        }
    }

    fun deleteUtang(id: Long) {
        viewModelScope.launch {
            utangRepository.deleteUtang(id)
            _uiState.update { it.copy(snackbarMessage = "Utang record deleted") }
        }
    }

    fun dismissSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
