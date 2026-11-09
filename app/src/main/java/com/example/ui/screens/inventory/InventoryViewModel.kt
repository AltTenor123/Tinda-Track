package com.example.ui.screens.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Product
import com.example.domain.model.StockMovementType
import com.example.domain.model.StockStatus
import com.example.domain.repository.ProductRepository
import com.example.domain.usecase.QrSequenceUseCase
import com.example.domain.usecase.StockMovementResult
import com.example.domain.usecase.StockMovementUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class InventoryUiState(
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val selectedStatus: StockStatus? = null,
    val selectedCategory: String? = null,
    val selectedOwner: String? = null,
    val allProducts: List<Product> = emptyList(),
    val filteredProducts: List<Product> = emptyList(),
    val availableCategories: List<String> = emptyList(),
    val availableOwners: List<String> = emptyList(),
    val totalCount: Int = 0,
    val inStockCount: Int = 0,
    val lowStockCount: Int = 0,
    val outOfStockCount: Int = 0,
    val activeProductForAdjustment: Product? = null,
    val activeProductForEdit: Product? = null,
    val isFormOpen: Boolean = false,
    val snackbarMessage: String? = null
)

class InventoryViewModel(
    private val productRepository: ProductRepository,
    private val stockMovementUseCase: StockMovementUseCase,
    private val qrSequenceUseCase: QrSequenceUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState: StateFlow<InventoryUiState> = _uiState.asStateFlow()

    init {
        loadInventory()
    }

    private fun loadInventory() {
        viewModelScope.launch {
            productRepository.getAllProductsFlow().collect { products ->
                val categories = productRepository.getAllCategories()
                val owners = productRepository.getAllOwners()

                _uiState.update { current ->
                    val filtered = applyFilters(
                        products = products,
                        query = current.searchQuery,
                        status = current.selectedStatus,
                        category = current.selectedCategory,
                        owner = current.selectedOwner
                    )
                    current.copy(
                        isLoading = false,
                        allProducts = products,
                        filteredProducts = filtered,
                        availableCategories = categories,
                        availableOwners = owners,
                        totalCount = products.size,
                        inStockCount = products.count { it.status == StockStatus.IN_STOCK },
                        lowStockCount = products.count { it.status == StockStatus.LOW_STOCK },
                        outOfStockCount = products.count { it.status == StockStatus.OUT_OF_STOCK }
                    )
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { current ->
            current.copy(
                searchQuery = query,
                filteredProducts = applyFilters(
                    products = current.allProducts,
                    query = query,
                    status = current.selectedStatus,
                    category = current.selectedCategory,
                    owner = current.selectedOwner
                )
            )
        }
    }

    fun onStatusFilterChanged(status: StockStatus?) {
        _uiState.update { current ->
            current.copy(
                selectedStatus = status,
                filteredProducts = applyFilters(
                    products = current.allProducts,
                    query = current.searchQuery,
                    status = status,
                    category = current.selectedCategory,
                    owner = current.selectedOwner
                )
            )
        }
    }

    fun onCategoryFilterChanged(category: String?) {
        _uiState.update { current ->
            current.copy(
                selectedCategory = category,
                filteredProducts = applyFilters(
                    products = current.allProducts,
                    query = current.searchQuery,
                    status = current.selectedStatus,
                    category = category,
                    owner = current.selectedOwner
                )
            )
        }
    }

    fun onOwnerFilterChanged(owner: String?) {
        _uiState.update { current ->
            current.copy(
                selectedOwner = owner,
                filteredProducts = applyFilters(
                    products = current.allProducts,
                    query = current.searchQuery,
                    status = current.selectedStatus,
                    category = current.selectedCategory,
                    owner = owner
                )
            )
        }
    }

    private fun applyFilters(
        products: List<Product>,
        query: String,
        status: StockStatus?,
        category: String?,
        owner: String?
    ): List<Product> {
        val q = query.trim().lowercase()
        return products.filter { p ->
            val matchesQuery = q.isEmpty() ||
                    p.name.lowercase().contains(q) ||
                    p.sku.lowercase().contains(q) ||
                    p.qrCode.lowercase().contains(q) ||
                    p.category.lowercase().contains(q) ||
                    p.owner.lowercase().contains(q) ||
                    p.supplier.lowercase().contains(q)

            val matchesStatus = status == null || p.status == status
            val matchesCategory = category == null || p.category.equals(category, ignoreCase = true)
            val matchesOwner = owner == null || p.owner.equals(owner, ignoreCase = true)

            matchesQuery && matchesStatus && matchesCategory && matchesOwner
        }
    }

    fun openProductForm(product: Product? = null) {
        _uiState.update {
            it.copy(
                activeProductForEdit = product,
                isFormOpen = true
            )
        }
    }

    fun openProductFormWithQr(scannedCode: String) {
        val now = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US).format(java.util.Date())
        val dummyProduct = Product(
            id = 0,
            name = "",
            owner = "Nanay Rosa",
            category = "Snacks",
            supplier = "",
            sku = "SKU-${System.currentTimeMillis() % 100000}",
            qrCode = scannedCode,
            costPrice = 0L,
            sellingPrice = 0L,
            quantity = 1,
            reorderLevel = 5,
            expiryDate = null,
            photoPath = null,
            createdAt = now,
            updatedAt = now
        )
        _uiState.update {
            it.copy(
                activeProductForEdit = dummyProduct,
                isFormOpen = true
            )
        }
    }

    fun closeProductForm() {
        _uiState.update {
            it.copy(
                activeProductForEdit = null,
                isFormOpen = false
            )
        }
    }

    fun openStockAdjustment(product: Product) {
        _uiState.update { it.copy(activeProductForAdjustment = product) }
    }

    fun closeStockAdjustment() {
        _uiState.update { it.copy(activeProductForAdjustment = null) }
    }

    fun saveProduct(
        id: Long = 0,
        name: String,
        owner: String,
        category: String,
        supplier: String,
        sku: String,
        qrCode: String,
        costPrice: Long,
        sellingPrice: Long,
        quantity: Int,
        reorderLevel: Int,
        expiryDate: String?,
        emoji: String?
    ) {
        viewModelScope.launch {
            val finalSku = sku.ifBlank { "SKU-${System.currentTimeMillis() % 100000}" }
            val finalQr = qrCode.ifBlank { qrSequenceUseCase.generateNextCode() }

            val product = Product(
                id = id,
                name = name.trim(),
                owner = owner.trim(),
                category = category.trim(),
                supplier = supplier.trim(),
                sku = finalSku.trim(),
                qrCode = finalQr.trim(),
                costPrice = costPrice,
                sellingPrice = sellingPrice,
                quantity = quantity,
                reorderLevel = reorderLevel,
                expiryDate = expiryDate?.trim()?.ifBlank { null },
                photoPath = emoji ?: "📦",
                createdAt = "",
                updatedAt = ""
            )

            if (id == 0L) {
                productRepository.insertProduct(product)
                showSnackbar("Added '${product.name}' to inventory")
            } else {
                productRepository.updateProduct(product)
                showSnackbar("Updated '${product.name}'")
            }
            closeProductForm()
        }
    }

    fun deleteProduct(productId: Long, productName: String) {
        viewModelScope.launch {
            val success = productRepository.deleteProduct(productId)
            if (success) {
                showSnackbar("Deleted '$productName' from active inventory")
            }
        }
    }

    fun applyStockChange(
        productId: Long,
        type: StockMovementType,
        deltaOrExact: Int,
        reason: String
    ) {
        viewModelScope.launch {
            val result = when (type) {
                StockMovementType.IN -> stockMovementUseCase.stockIn(productId, deltaOrExact, reason)
                StockMovementType.OUT -> stockMovementUseCase.stockOut(productId, deltaOrExact, reason)
                StockMovementType.ADJUST -> stockMovementUseCase.adjustStock(productId, deltaOrExact, reason)
                StockMovementType.SALE -> stockMovementUseCase.stockOut(productId, deltaOrExact, reason)
            }

            when (result) {
                is StockMovementResult.Success -> {
                    showSnackbar("Stock updated successfully (${result.newQuantity} available)")
                    closeStockAdjustment()
                }
                is StockMovementResult.InsufficientStock -> {
                    showSnackbar("Cannot deduct $deltaOrExact. Only ${result.available} items in stock!")
                }
                is StockMovementResult.NegativeAdjustment -> {
                    showSnackbar("Stock quantity cannot be negative!")
                }
                is StockMovementResult.ReasonRequired -> {
                    showSnackbar("Reason is required for inventory audit logs!")
                }
                is StockMovementResult.InvalidDelta -> {
                    showSnackbar("Quantity must be greater than zero!")
                }
                else -> {
                    showSnackbar("Failed to update stock.")
                }
            }
        }
    }

    fun showSnackbar(message: String) {
        _uiState.update { it.copy(snackbarMessage = message) }
    }

    fun dismissSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
