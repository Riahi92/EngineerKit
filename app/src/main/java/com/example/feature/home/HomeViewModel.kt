package com.example.feature.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.data.PreferencesRepository
import com.example.core.data.ToolCatalog
import com.example.core.model.EngineeringCategory
import com.example.core.model.EngineeringTool
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val categories: List<EngineeringCategory> = emptyList(),
    val quickAccessTools: List<EngineeringTool> = emptyList(),
    val favoriteTools: List<EngineeringTool> = emptyList(),
    val recentTools: List<EngineeringTool> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val selectedToolForSpec: EngineeringTool? = null
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PreferencesRepository.getInstance(application)

    val uiState: StateFlow<HomeUiState> = combine(
        repository.favoriteIds,
        repository.recentToolIds
    ) { favIds, recIds ->
        val quickAccess = listOfNotNull(
            ToolCatalog.getToolById("tool_sci_calc"),
            ToolCatalog.getToolById("tool_unit_converter"),
            ToolCatalog.getToolById("tool_meca_stress"),
            ToolCatalog.getToolById("tool_elec_ohm")
        )

        val favorites = ToolCatalog.tools.filter { favIds.contains(it.id) }
        val recents = recIds.mapNotNull { ToolCatalog.getToolById(it) }

        HomeUiState(
            categories = ToolCatalog.categories,
            quickAccessTools = quickAccess,
            favoriteTools = favorites,
            recentTools = recents,
            favoriteIds = favIds
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(
            categories = ToolCatalog.categories,
            quickAccessTools = listOfNotNull(
                ToolCatalog.getToolById("tool_sci_calc"),
                ToolCatalog.getToolById("tool_unit_converter"),
                ToolCatalog.getToolById("tool_meca_stress"),
                ToolCatalog.getToolById("tool_elec_ohm")
            )
        )
    )

    fun toggleFavorite(toolId: String) {
        repository.toggleFavorite(toolId)
    }

    fun onToolOpened(toolId: String) {
        repository.addRecentTool(toolId)
    }
}
