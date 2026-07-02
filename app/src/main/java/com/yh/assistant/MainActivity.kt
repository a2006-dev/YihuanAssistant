package com.yh.assistant
import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.yh.assistant.ui.login.LoginScreen
import com.yh.assistant.ui.main.MainScreen
import com.yh.assistant.ui.disclaimer.DisclaimerScreen
import com.yh.assistant.ui.settings.SettingsScreen
import com.yh.assistant.util.PreferenceUtil
import com.yh.assistant.data.api.RetrofitClient
import com.yh.assistant.data.repository.AuthRepository
import kotlinx.coroutines.*

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF8B5CF6), secondary = Color(0xFFF59E0B), tertiary = Color(0xFFEC4899),
    background = Color(0xFF0A0A1A), surface = Color(0xFF13132B), surfaceVariant = Color(0xFF1C1C3A),
    onPrimary = Color.White, onBackground = Color(0xFFF1F1F9),
    onSurface = Color(0xFFF1F1F9), onSurfaceVariant = Color(0xFF94A3B8),
    error = Color(0xFFEF4444), outline = Color(0xFF2D2D4A)
)
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF7C3AED), secondary = Color(0xFFF59E0B), tertiary = Color(0xFFEC4899),
    background = Color(0xFFFFF8F0), surface = Color(0xFFFFFFFF), surfaceVariant = Color(0xFFF1F5F9),
    onPrimary = Color.White, onBackground = Color(0xFF1E293B),
    onSurface = Color(0xFF1E293B), onSurfaceVariant = Color(0xFF64748B),
    error = Color(0xFFEF4444), outline = Color(0xFFE2E8F0)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.parseColor("#0A0A1A")
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        AuthRepository.restoreSession()

        setContent {
            val systemDark = isSystemInDarkTheme()
            val userMode = PreferenceUtil.isDarkMode()
            val isDark = when (userMode) { 1 -> true; 2 -> false; else -> systemDark }
            val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

            MaterialTheme(colorScheme = colorScheme) {
                LaunchedEffect(colorScheme) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        val isLight = colorScheme.background.luminance() > 0.5f
                        val flags = if (isLight) View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR else 0
                        window.decorView.systemUiVisibility = flags
                    }
                }

                val hasAccounts = PreferenceUtil.hasAccounts()
                val disclaimerAccepted = PreferenceUtil.isDisclaimerAccepted()
                var isLoggedIn by remember { mutableStateOf(hasAccounts && RetrofitClient.accessToken.isNotEmpty()) }
                var showSettings by remember { mutableStateOf(false) }
                var showDisclaimer by remember { mutableStateOf(!disclaimerAccepted) }

                val page = when {
                    showDisclaimer -> 0
                    showSettings -> 1
                    !isLoggedIn -> 2
                    else -> 3
                }

                when (page) {
                    0 -> DisclaimerScreen(onAccepted = { PreferenceUtil.setDisclaimerAccepted(true); showDisclaimer = false })
                    1 -> SettingsScreen(onBack = { showSettings = false })
                    2 -> LoginScreen(onLoggedIn = { isLoggedIn = true })
                    3 -> MainScreen(
                        onLogout = { AuthRepository.logout(); isLoggedIn = false },
                        onSettings = { showSettings = true }
                    )
                }
            }
        }
    }
}