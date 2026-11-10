package com.example.core.data

import android.content.Context
import android.content.SharedPreferences
import com.example.core.model.DecimalPrecision
import com.example.core.model.ThemeMode
import com.example.core.model.UnitSystem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(readThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _unitSystem = MutableStateFlow(readUnitSystem())
    val unitSystem: StateFlow<UnitSystem> = _unitSystem.asStateFlow()

    private val _decimalPrecision = MutableStateFlow(readDecimalPrecision())
    val decimalPrecision: StateFlow<DecimalPrecision> = _decimalPrecision.asStateFlow()

    private val _favoriteIds = MutableStateFlow(readFavorites())
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

    private val _recentToolIds = MutableStateFlow(readRecents())
    val recentToolIds: StateFlow<List<String>> = _recentToolIds.asStateFlow()

    private fun readThemeMode(): ThemeMode {
        val name = prefs.getString(KEY_THEME_MODE, ThemeMode.DARK.name)
        return try {
            ThemeMode.valueOf(name ?: ThemeMode.DARK.name)
        } catch (_: Exception) {
            ThemeMode.DARK
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _themeMode.value = mode
    }

    private fun readUnitSystem(): UnitSystem {
        val name = prefs.getString(KEY_UNIT_SYSTEM, UnitSystem.METRIC_SI.name)
        return try {
            UnitSystem.valueOf(name ?: UnitSystem.METRIC_SI.name)
        } catch (_: Exception) {
            UnitSystem.METRIC_SI
        }
    }

    fun setUnitSystem(system: UnitSystem) {
        prefs.edit().putString(KEY_UNIT_SYSTEM, system.name).apply()
        _unitSystem.value = system
    }

    private fun readDecimalPrecision(): DecimalPrecision {
        val name = prefs.getString(KEY_DECIMAL_PRECISION, DecimalPrecision.FOUR.name)
        return try {
            DecimalPrecision.valueOf(name ?: DecimalPrecision.FOUR.name)
        } catch (_: Exception) {
            DecimalPrecision.FOUR
        }
    }

    fun setDecimalPrecision(precision: DecimalPrecision) {
        prefs.edit().putString(KEY_DECIMAL_PRECISION, precision.name).apply()
        _decimalPrecision.value = precision
    }

    private fun readFavorites(): Set<String> {
        val raw = prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
        return raw.toSet()
    }

    fun toggleFavorite(toolId: String) {
        val current = _favoriteIds.value.toMutableSet()
        if (current.contains(toolId)) {
            current.remove(toolId)
        } else {
            current.add(toolId)
        }
        prefs.edit().putStringSet(KEY_FAVORITES, current).apply()
        _favoriteIds.value = current
    }

    fun isFavorite(toolId: String): Boolean =
        _favoriteIds.value.contains(toolId)

    private fun readRecents(): List<String> {
        val raw = prefs.getString(KEY_RECENTS, "") ?: ""
        if (raw.isBlank()) return emptyList()
        return raw.split(",").filter { it.isNotBlank() }
    }

    fun addRecentTool(toolId: String) {
        val list = _recentToolIds.value.toMutableList()
        list.remove(toolId)
        list.add(0, toolId)
        val trimmed = list.take(10)
        prefs.edit().putString(KEY_RECENTS, trimmed.joinToString(",")).apply()
        _recentToolIds.value = trimmed
    }

    fun clearRecents() {
        prefs.edit().remove(KEY_RECENTS).apply()
        _recentToolIds.value = emptyList()
    }

    companion object {
        private const val PREFS_NAME = "engineerkit_user_prefs"
        private const val KEY_THEME_MODE = "pref_theme_mode"
        private const val KEY_UNIT_SYSTEM = "pref_unit_system"
        private const val KEY_DECIMAL_PRECISION = "pref_decimal_precision"
        private const val KEY_FAVORITES = "pref_favorites_set"
        private const val KEY_RECENTS = "pref_recents_csv"

        @Volatile
        private var INSTANCE: PreferencesRepository? = null

        fun getInstance(context: Context): PreferencesRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PreferencesRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
