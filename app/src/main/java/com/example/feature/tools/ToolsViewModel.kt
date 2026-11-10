package com.example.feature.tools

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.data.PreferencesRepository
import com.example.core.data.ToolCatalog
import com.example.core.model.CategoryId
import com.example.core.model.EngineeringCategory
import com.example.core.model.EngineeringTool
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ToolsUiState(
    val categories: List<EngineeringCategory> = emptyList(),
    val selectedCategoryId: CategoryId? = null,
    val displayedTools: List<EngineeringTool> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val totalToolCount: Int = 0
)

class ToolsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PreferencesRepository.getInstance(application)
    private val selectedCategoryId = MutableStateFlow<CategoryId?>(null)

    val uiState: StateFlow<ToolsUiState> = combine(
        selectedCategoryId,
        repository.favoriteIds
    ) { selectedCat, favIds ->
        val tools = if (selectedCat == null) {
            ToolCatalog.tools
        } else {
            ToolCatalog.getToolsByCategory(selectedCat)
        }

        ToolsUiState(
            categories = ToolCatalog.categories,
            selectedCategoryId = selectedCat,
            displayedTools = tools,
            favoriteIds = favIds,
            totalToolCount = ToolCatalog.tools.size
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ToolsUiState(
            categories = ToolCatalog.categories,
            displayedTools = ToolCatalog.tools,
            totalToolCount = ToolCatalog.tools.size
        )
    )

    fun selectCategory(categoryId: CategoryId?) {
        selectedCategoryId.value = categoryId
    }

    fun toggleFavorite(toolId: String) {
        repository.toggleFavorite(toolId)
    }

    fun onToolOpened(toolId: String) {
        repository.addRecentTool(toolId)
    }
}
