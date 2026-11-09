package com.example.di

import com.example.data.db.TindaDatabaseHelper
import com.example.data.repository.ProductRepositoryImpl
import com.example.data.repository.SalesRepositoryImpl
import com.example.data.repository.SettingsRepositoryImpl
import com.example.data.repository.UtangRepositoryImpl
import com.example.domain.repository.ProductRepository
import com.example.domain.repository.SalesRepository
import com.example.domain.repository.SettingsRepository
import com.example.domain.repository.UtangRepository
import com.example.domain.usecase.*
import com.example.ui.screens.home.HomeViewModel
import com.example.ui.screens.inventory.InventoryViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val databaseModule = module {
    single { TindaDatabaseHelper.getInstance(androidContext()) }
}

val repositoryModule = module {
    single<ProductRepository> { ProductRepositoryImpl(get()) }
    single<SalesRepository> { SalesRepositoryImpl(get()) }
    single<UtangRepository> { UtangRepositoryImpl(get()) }
    single<SettingsRepository> { SettingsRepositoryImpl(get()) }
}

val useCaseModule = module {
    factory { CheckoutUseCase(get(), get()) }
    factory { StockMovementUseCase(get()) }
    factory { UtangPaymentUseCase(get()) }
    factory { ReorderAlertUseCase(get()) }
    factory { ExpiryAlertUseCase(get()) }
    factory { QrSequenceUseCase(get()) }
}

val appManagerModule = module {
    single { com.example.domain.model.CartManager() }
}

val viewModelModule = module {
    viewModel { HomeViewModel(get(), get(), get(), get(), get()) }
    viewModel { InventoryViewModel(get(), get(), get()) }
    viewModel { com.example.ui.screens.scan.ScanViewModel(get(), get(), get()) }
    viewModel { com.example.ui.screens.sales.SalesViewModel(get(), get(), get()) }
    viewModel { com.example.ui.screens.utang.UtangViewModel(get(), get()) }
    viewModel { com.example.ui.screens.more.MoreViewModel(get(), get(), get(), get(), get(), get(), get()) }
}

val appModules = listOf(
    databaseModule,
    repositoryModule,
    useCaseModule,
    appManagerModule,
    viewModelModule
)
