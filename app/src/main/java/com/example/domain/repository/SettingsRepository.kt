package com.example.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getSettingFlow(key: String, defaultValue: String): Flow<String>
    suspend fun getSetting(key: String, defaultValue: String): String
    suspend fun setSetting(key: String, value: String)
    suspend fun isOnboardingDone(): Boolean
    suspend fun setOnboardingDone(done: Boolean)
    suspend fun isPasscodeEnabled(): Boolean
    suspend fun setPasscodeEnabled(enabled: Boolean)
    suspend fun getPasscode(): String?
    suspend fun setPasscode(code: String?)
    suspend fun getTheme(): String
    suspend fun setTheme(theme: String)
}
