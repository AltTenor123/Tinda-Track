package com.example.ui.screens.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.CartManager
import com.example.domain.model.Product
import com.example.domain.model.StockStatus
import com.example.domain.repository.ProductRepository
import com.example.domain.usecase.QrSequenceUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ScanMode {
    LOOKUP,
    SELL
}

data class ScanUiState(
    val mode: ScanMode = ScanMode.LOOKUP,
    val manualInputText: String = "",
    val activeProduct: Product? = null,
    val isProductSheetOpen: Boolean = false,
    val unknownCodeScanned: String? = null,
    val isUnknownCodeSheetOpen: Boolean = false,
    val snackbarMessage: String? = null,
    val lastScannedCode: String? = null
)

class ScanViewModel(
    private val productRepository: ProductRepository,
    private val cartManager: CartManager,
    private val qrSequenceUseCase: QrSequenceUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    fun setMode(mode: ScanMode) {
        _uiState.update { it.copy(mode = mode) }
    }

    fun onManualInputChanged(text: String) {
        _uiState.update { it.copy(manualInputText = text) }
    }

    fun onCodeDetected(rawCode: String) {
        val sanitized = qrSequenceUseCase.sanitizeScannedCode(rawCode)
        if (sanitized.isBlank()) return

        viewModelScope.launch {
            val product = productRepository.getProductByQr(sanitized)
            if (product != null) {
                if (_uiState.value.mode == ScanMode.SELL) {
                    if (product.quantity <= 0) {
                        showSnackbar("Cannot add '${product.name}'. Out of stock!")
                    } else {
                        val added = cartManager.addToCart(product, 1)
                        if (added) {
                            showSnackbar("Added 1x ${product.name} to cart (₱${product.sellingPrice / 100.0})")
                        } else {
                            showSnackbar("Cannot add more. Reached stock limit (${product.quantity} pcs)!")
                        }
                    }
                } else {
                    // LOOKUP Mode
                    _uiState.update {
                        it.copy(
                            activeProduct = product,
                            isProductSheetOpen = true,
                            lastScannedCode = sanitized
                        )
                    }
                }
            } else {
                // Unknown Code
                _uiState.update {
                    it.copy(
                        unknownCodeScanned = sanitized,
                        isUnknownCodeSheetOpen = true,
                        lastScannedCode = sanitized
                    )
                }
            }
        }
    }

    fun submitManualEntry() {
        val code = _uiState.value.manualInputText.trim()
        if (code.isNotBlank()) {
            onCodeDetected(code)
            _uiState.update { it.copy(manualInputText = "") }
        }
    }

    fun addActiveProductToCart(quantity: Int = 1) {
        val product = _uiState.value.activeProduct ?: return
        if (product.quantity <= 0) {
            showSnackbar("Item is out of stock!")
            return
        }
        val added = cartManager.addToCart(product, quantity)
        if (added) {
            showSnackbar("Added $quantity x ${product.name} to cart")
            closeProductSheet()
        } else {
            showSnackbar("Cannot exceed available stock (${product.quantity} pcs)!")
        }
    }

    fun closeProductSheet() {
        _uiState.update { it.copy(activeProduct = null, isProductSheetOpen = false) }
    }

    fun closeUnknownCodeSheet() {
        _uiState.update { it.copy(unknownCodeScanned = null, isUnknownCodeSheetOpen = false) }
    }

    fun showSnackbar(message: String) {
        _uiState.update { it.copy(snackbarMessage = message) }
    }

    fun dismissSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
