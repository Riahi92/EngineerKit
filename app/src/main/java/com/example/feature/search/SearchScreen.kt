package com.example.feature.search

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.data.ToolCatalog
import com.example.core.designsystem.component.EmptyState
import com.example.core.designsystem.component.EngineeringSearchBar
import com.example.core.designsystem.component.EngineeringToolCard
import com.example.core.designsystem.component.SectionHeader
import com.example.core.model.EngineeringTool
import com.example.ui.theme.PrimaryAccent

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onToolClick: (EngineeringTool) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val popularTags = listOf("Ohm", "Reynolds", "von Mises", "Béton", "Fourier", "Matrice", "IP / CIDR")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("search_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Search bar
        item {
            EngineeringSearchBar(
                query = uiState.query,
                onQueryChange = { viewModel.onQueryChanged(it) }
            )
        }

        // Popular search tags suggestion chips
        item {
            Column {
                Text(
                    text = "Suggestions fréquentes",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    popularTags.forEach { tag ->
                        AssistChip(
                            onClick = { viewModel.onQueryChanged(tag) },
                            label = { Text(tag) },
                            modifier = Modifier.testTag("tag_suggestion_$tag")
                        )
                    }
                }
            }
        }

        // Discipline Category Filters
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = uiState.selectedCategoryFilter == null,
                    onClick = { viewModel.onCategoryFilterChanged(null) },
                    label = { Text("Toutes disciplines") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryAccent.copy(alpha = 0.2f),
                        selectedLabelColor = PrimaryAccent
                    )
                )

                ToolCatalog.categories.forEach { category ->
                    val isSelected = uiState.selectedCategoryFilter == category.id
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (isSelected) viewModel.onCategoryFilterChanged(null)
                            else viewModel.onCategoryFilterChanged(category.id)
                        },
                        label = { Text(category.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = category.accentColor.copy(alpha = 0.2f),
                            selectedLabelColor = category.accentColor
                        )
                    )
                }
            }
        }

        // Results Header
        item {
            SectionHeader(
                title = if (uiState.query.isBlank()) "Tous les outils disponibles" else "Résultats de recherche",
                subtitle = "${uiState.results.size} correspondances trouvées"
            )
        }

        // Results or Empty State
        if (uiState.results.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Default.SearchOff,
                    title = "Aucun résultat trouvé",
                    description = "Aucun outil ne correspond à votre recherche. Vérifiez l'orthographe ou essayez un terme générique.",
                    actionLabel = "Effacer la recherche",
                    onActionClick = {
                        viewModel.onQueryChanged("")
                        viewModel.onCategoryFilterChanged(null)
                    }
                )
            }
        } else {
            items(
                items = uiState.results,
                key = { it.id },
                contentType = { "tool_card" }
            ) { tool ->
                EngineeringToolCard(
                    tool = tool,
                    isFavorite = uiState.favoriteIds.contains(tool.id),
                    onFavoriteToggle = { viewModel.toggleFavorite(tool.id) },
                    onClick = {
                        viewModel.onToolOpened(tool.id)
                        onToolClick(tool)
                    }
                )
            }
        }
    }
}
