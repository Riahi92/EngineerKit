package com.example.feature.category

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.data.ToolCatalog
import com.example.core.designsystem.component.CategoryIcon
import com.example.core.designsystem.component.EmptyState
import com.example.core.designsystem.component.EngineeringSearchBar
import com.example.core.designsystem.component.EngineeringToolCard
import com.example.core.designsystem.component.SectionHeader
import com.example.core.model.CategoryId
import com.example.core.model.EngineeringTool
import com.example.ui.theme.PrimaryAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailScreen(
    categoryId: CategoryId,
    favoriteIds: Set<String>,
    onFavoriteToggle: (String) -> Unit,
    onToolClick: (EngineeringTool) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val category = ToolCatalog.getCategoryById(categoryId)
    val categoryTools = remember(categoryId) { ToolCatalog.getToolsByCategory(categoryId) }
    var inCategoryQuery by remember { mutableStateOf("") }

    val filteredTools = remember(inCategoryQuery, categoryTools) {
        if (inCategoryQuery.isBlank()) categoryTools
        else categoryTools.filter {
            it.name.contains(inCategoryQuery, ignoreCase = true) ||
                    it.shortDescription.contains(inCategoryQuery, ignoreCase = true) ||
                    it.formulaPreview.contains(inCategoryQuery, ignoreCase = true)
        }
    }

    if (category == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Catégorie introuvable")
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("category_detail_screen_${category.id.name.lowercase()}")
    ) {
        // Custom TopBar for Category Detail
        TopAppBar(
            title = {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("category_detail_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Retour",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Category Hero Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, category.accentColor.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CategoryIcon(
                                categoryId = category.id,
                                tint = category.accentColor,
                                size = 48.dp,
                                iconSize = 26.dp
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = category.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${category.toolCount} outils prévus dans cette branche",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = category.accentColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = category.longDescription,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            // In-category search bar
            item {
                EngineeringSearchBar(
                    query = inCategoryQuery,
                    onQueryChange = { inCategoryQuery = it },
                    placeholderText = "Filtrer dans ${category.name}..."
                )
            }

            // Tools Section Header
            item {
                SectionHeader(
                    title = "Outils de la discipline",
                    subtitle = "${filteredTools.size} affichés"
                )
            }

            if (filteredTools.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.Search,
                        title = "Aucun outil trouvé",
                        description = "Aucun outil de ${category.name} ne correspond à '$inCategoryQuery'."
                    )
                }
            } else {
                items(
                    items = filteredTools,
                    key = { it.id },
                    contentType = { "tool_card" }
                ) { tool ->
                    EngineeringToolCard(
                        tool = tool,
                        isFavorite = favoriteIds.contains(tool.id),
                        onFavoriteToggle = { onFavoriteToggle(tool.id) },
                        onClick = { onToolClick(tool) }
                    )
                }
            }
        }
    }
}
