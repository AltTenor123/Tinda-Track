package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import com.example.di.appModules
import com.example.domain.repository.SettingsRepository
import com.example.ui.MainAppContainer
import com.example.ui.theme.TindaTrackTheme
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.compose.koinInject
import org.koin.core.context.GlobalContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Koin DI safely
        if (GlobalContext.getOrNull() == null) {
            org.koin.core.context.startKoin {
                androidContext(this@MainActivity.applicationContext)
                modules(appModules)
            }
        }

        setContent {
            val settingsRepo: SettingsRepository = koinInject()
            val themeSetting by settingsRepo.getSettingFlow("app_theme", "SYSTEM").collectAsState(initial = "SYSTEM")
            val systemIsDark = isSystemInDarkTheme()
            val isDark = when (themeSetting.uppercase()) {
                "DARK" -> true
                "LIGHT" -> false
                else -> systemIsDark
            }
            val coroutineScope = rememberCoroutineScope()

            TindaTrackTheme(darkTheme = isDark) {
                MainAppContainer(
                    onToggleTheme = {
                        coroutineScope.launch {
                            val nextTheme = if (isDark) "LIGHT" else "DARK"
                            settingsRepo.setTheme(nextTheme)
                        }
                    }
                )
            }
        }
    }
}
