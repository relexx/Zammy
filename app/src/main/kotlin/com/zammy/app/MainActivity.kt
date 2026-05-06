package com.zammy.app

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zammy.app.navigation.AppNavigation
import com.zammy.app.presentation.settings.SettingsViewModel
import com.zammy.app.ui.theme.ThemeMode
import com.zammy.app.ui.theme.ZammyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* permission result handled silently */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        val initialTicketId = intent.getIntExtra("ticket_id", -1).takeIf { it > 0 }

        setContent {
            val settingsVm: SettingsViewModel = hiltViewModel()
            val uiState by settingsVm.uiState.collectAsStateWithLifecycle()
            val themeMode = when (uiState.display.themeMode) {
                "LIGHT" -> ThemeMode.LIGHT
                "DARK"  -> ThemeMode.DARK
                else    -> ThemeMode.SYSTEM
            }
            ZammyTheme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(initialTicketId = initialTicketId)
                }
            }
        }
    }
}
