package com.example.data.repository

import android.content.ContentValues
import com.example.data.db.TindaDatabaseHelper
import com.example.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class SettingsRepositoryImpl(
    private val dbHelper: TindaDatabaseHelper
) : SettingsRepository {

    override fun getSettingFlow(key: String, defaultValue: String): Flow<String> = flow {
        emit(getSetting(key, defaultValue))
        dbHelper.databaseUpdates.collect {
            emit(getSetting(key, defaultValue))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun getSetting(key: String, defaultValue: String): String = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT value FROM settings WHERE key = ?", arrayOf(key))
        cursor.use {
            if (it.moveToNext()) it.getString(0) else defaultValue
        }
    }

    override suspend fun setSetting(key: String, value: String) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("key", key)
            put("value", value)
        }
        db.insertWithOnConflict(
            "settings",
            null,
            cv,
            android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
        )
        dbHelper.notifyUpdated()
    }

    override suspend fun isOnboardingDone(): Boolean {
        return getSetting("onboarding_done", "false").toBoolean()
    }

    override suspend fun setOnboardingDone(done: Boolean) {
        setSetting("onboarding_done", done.toString())
    }

    override suspend fun isPasscodeEnabled(): Boolean {
        return getSetting("passcode_enabled", "false").toBoolean()
    }

    override suspend fun setPasscodeEnabled(enabled: Boolean) {
        setSetting("passcode_enabled", enabled.toString())
    }

    override suspend fun getPasscode(): String? {
        val code = getSetting("passcode_value", "")
        return if (code.isBlank()) null else code
    }

    override suspend fun setPasscode(code: String?) {
        setSetting("passcode_value", code ?: "")
    }

    override suspend fun getTheme(): String {
        return getSetting("app_theme", "system")
    }

    override suspend fun setTheme(theme: String) {
        setSetting("app_theme", theme)
    }
}
