package com.example.feature.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.designsystem.component.EmptyState
import com.example.core.designsystem.component.EngineeringToolCard
import com.example.core.designsystem.component.SectionHeader
import com.example.core.model.EngineeringTool

@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel,
    onToolClick: (EngineeringTool) -> Unit,
    onExploreTools: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("favorites_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SectionHeader(
                title = "Outils favoris",
                subtitle = "${uiState.favoriteTools.size} outil(s) épinglé(s)"
            )
        }

        if (uiState.favoriteTools.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Default.BookmarkBorder,
                    title = "Aucun favori enregistré",
                    description = "Explorez les disciplines d'ingénierie et cliquez sur le signet d'un outil pour l'ajouter à vos favoris.",
                    actionLabel = "Explorer le catalogue",
                    onActionClick = onExploreTools
                )
            }
        } else {
            items(
                items = uiState.favoriteTools,
                key = { it.id },
                contentType = { "tool_card" }
            ) { tool ->
                EngineeringToolCard(
                    tool = tool,
                    isFavorite = true,
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
