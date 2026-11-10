package com.example.feature.tools

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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.designsystem.component.EngineeringCategoryCard
import com.example.core.designsystem.component.EngineeringSearchBar
import com.example.core.designsystem.component.EngineeringToolCard
import com.example.core.designsystem.component.SectionHeader
import com.example.core.model.CategoryId
import com.example.core.model.EngineeringTool
import com.example.ui.theme.PrimaryAccent

@Composable
fun ToolsScreen(
    viewModel: ToolsViewModel,
    onCategoryClick: (CategoryId) -> Unit,
    onToolClick: (EngineeringTool) -> Unit,
    onNavigateToSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("tools_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Search shortcut
        item {
            EngineeringSearchBar(
                query = "",
                onQueryChange = {},
                enabled = false,
                onClickWhenDisabled = onNavigateToSearch
            )
        }

        // Horizontal Category Filter Chips
        item {
            Column {
                Text(
                    text = "Filtrer par discipline",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.selectedCategoryId == null,
                        onClick = { viewModel.selectCategory(null) },
                        label = { Text("Toutes (${uiState.totalToolCount})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryAccent.copy(alpha = 0.2f),
                            selectedLabelColor = PrimaryAccent
                        ),
                        modifier = Modifier.testTag("filter_chip_all")
                    )

                    uiState.categories.forEach { category ->
                        val isSelected = uiState.selectedCategoryId == category.id
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (isSelected) viewModel.selectCategory(null)
                                else viewModel.selectCategory(category.id)
                            },
                            label = { Text(category.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = category.accentColor.copy(alpha = 0.2f),
                                selectedLabelColor = category.accentColor
                            ),
                            modifier = Modifier.testTag("filter_chip_${category.id.name.lowercase()}")
                        )
                    }
                }
            }
        }

        // Category Detail Shortcut / Summary when filtered
        item {
            if (uiState.selectedCategoryId == null) {
                SectionHeader(
                    title = "Catalogue des outils",
                    subtitle = "${uiState.displayedTools.size} outils scientifiques et techniques"
                )
            } else {
                val currentCategory = uiState.categories.find { it.id == uiState.selectedCategoryId }
                currentCategory?.let { cat ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        SectionHeader(
                            title = cat.name,
                            subtitle = cat.shortDescription,
                            actionLabel = "Fiche discipline",
                            onActionClick = { onCategoryClick(cat.id) }
                        )
                    }
                }
            }
        }

        // List of tools for current category / filter
        items(
            items = uiState.displayedTools,
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
