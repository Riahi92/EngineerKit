package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.data.PreferencesRepository
import com.example.ui.theme.EngineerKitTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val repository = remember { PreferencesRepository.getInstance(applicationContext) }
            val themeMode by repository.themeMode.collectAsStateWithLifecycle()

            EngineerKitTheme(themeMode = themeMode) {
                EngineerKitApp()
            }
        }
    }
}
