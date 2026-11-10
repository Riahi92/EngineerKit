package com.example.feature.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.data.PreferencesRepository
import com.example.core.data.ToolCatalog
import com.example.core.model.CategoryId
import com.example.core.model.EngineeringTool
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class SearchUiState(
    val query: String = "",
    val selectedCategoryFilter: CategoryId? = null,
    val results: List<EngineeringTool> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val totalAvailableCount: Int = 0
)

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PreferencesRepository.getInstance(application)
    private val query = MutableStateFlow("")
    private val categoryFilter = MutableStateFlow<CategoryId?>(null)

    val uiState: StateFlow<SearchUiState> = combine(
        query,
        categoryFilter,
        repository.favoriteIds
    ) { q, cat, favIds ->
        val filtered = ToolCatalog.tools.filter { tool ->
            val matchCat = cat == null || tool.categoryId == cat
            val category = ToolCatalog.getCategoryById(tool.categoryId)
            val matchQuery = if (q.isBlank()) true else {
                tool.name.contains(q, ignoreCase = true) ||
                        tool.shortDescription.contains(q, ignoreCase = true) ||
                        tool.formulaPreview.contains(q, ignoreCase = true) ||
                        tool.tags.any { it.contains(q, ignoreCase = true) } ||
                        (category?.name?.contains(q, ignoreCase = true) == true)
            }
            matchCat && matchQuery
        }

        SearchUiState(
            query = q,
            selectedCategoryFilter = cat,
            results = filtered,
            favoriteIds = favIds,
            totalAvailableCount = ToolCatalog.tools.size
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SearchUiState(
            results = ToolCatalog.tools,
            totalAvailableCount = ToolCatalog.tools.size
        )
    )

    fun onQueryChanged(newQuery: String) {
        query.value = newQuery
    }

    fun onCategoryFilterChanged(categoryId: CategoryId?) {
        categoryFilter.value = categoryId
    }

    fun toggleFavorite(toolId: String) {
        repository.toggleFavorite(toolId)
    }

    fun onToolOpened(toolId: String) {
        repository.addRecentTool(toolId)
    }
}
