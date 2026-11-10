package com.example.feature.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.data.PreferencesRepository
import com.example.core.model.DecimalPrecision
import com.example.core.model.ThemeMode
import com.example.core.model.UnitSystem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ProfileUiState(
    val themeMode: ThemeMode = ThemeMode.DARK,
    val unitSystem: UnitSystem = UnitSystem.METRIC_SI,
    val decimalPrecision: DecimalPrecision = DecimalPrecision.FOUR,
    val appVersion: String = "1.0.0 (Phase 1)",
    val favoriteCount: Int = 0
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PreferencesRepository.getInstance(application)

    val uiState: StateFlow<ProfileUiState> = combine(
        repository.themeMode,
        repository.unitSystem,
        repository.decimalPrecision,
        repository.favoriteIds
    ) { theme, units, precision, favs ->
        ProfileUiState(
            themeMode = theme,
            unitSystem = units,
            decimalPrecision = precision,
            favoriteCount = favs.size
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProfileUiState()
    )

    fun setThemeMode(mode: ThemeMode) {
        repository.setThemeMode(mode)
    }

    fun setUnitSystem(system: UnitSystem) {
        repository.setUnitSystem(system)
    }

    fun setDecimalPrecision(precision: DecimalPrecision) {
        repository.setDecimalPrecision(precision)
    }

    fun clearRecents() {
        repository.clearRecents()
    }
}
