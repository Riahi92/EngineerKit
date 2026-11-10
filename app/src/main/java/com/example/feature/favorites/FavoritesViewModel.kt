package com.example.feature.favorites

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.data.PreferencesRepository
import com.example.core.data.ToolCatalog
import com.example.core.model.EngineeringTool
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class FavoritesUiState(
    val favoriteTools: List<EngineeringTool> = emptyList(),
    val favoriteIds: Set<String> = emptySet()
)

class FavoritesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PreferencesRepository.getInstance(application)

    val uiState: StateFlow<FavoritesUiState> = repository.favoriteIds
        .map { favIds ->
            val favs = ToolCatalog.tools.filter { favIds.contains(it.id) }
            FavoritesUiState(
                favoriteTools = favs,
                favoriteIds = favIds
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FavoritesUiState()
        )

    fun toggleFavorite(toolId: String) {
        repository.toggleFavorite(toolId)
    }

    fun onToolOpened(toolId: String) {
        repository.addRecentTool(toolId)
    }
}
