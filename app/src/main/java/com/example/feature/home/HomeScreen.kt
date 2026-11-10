package com.example.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.core.designsystem.component.CategoryIcon
import com.example.core.designsystem.component.EmptyState
import com.example.core.designsystem.component.EngineeringCategoryCard
import com.example.core.designsystem.component.EngineeringSearchBar
import com.example.core.designsystem.component.EngineeringToolCard
import com.example.core.designsystem.component.SectionHeader
import com.example.core.model.CategoryId
import com.example.core.model.EngineeringTool
import com.example.ui.theme.PrimaryAccent
import com.example.ui.theme.SecondaryAccent

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onCategoryClick: (CategoryId) -> Unit,
    onToolClick: (EngineeringTool) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToTools: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header with branding & Settings shortcut
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_engineerkit_logo),
                        contentDescription = "EngineerKit Logo",
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "EngineerKit",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = PrimaryAccent.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "v1.0",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = PrimaryAccent,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Boîte à outils d'ingénierie multitechnique",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Développée par Marwen Riahi",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = PrimaryAccent
                        )
                    }
                }

                IconButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .testTag("home_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Paramètres",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 2. Prominent Search Shortcut
        item {
            EngineeringSearchBar(
                query = "",
                onQueryChange = {},
                enabled = false,
                onClickWhenDisabled = onNavigateToSearch
            )
        }

        // 3. Quick Access Section
        item(key = "section_quick_access", contentType = "section_quick_access") {
            Column {
                SectionHeader(
                    title = "Accès rapide",
                    subtitle = "Outils d'ingénierie fondamentaux"
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(
                        items = uiState.quickAccessTools,
                        key = { it.id },
                        contentType = { "quick_access_card" }
                    ) { tool ->
                        QuickAccessCard(
                            tool = tool,
                            onClick = {
                                viewModel.onToolOpened(tool.id)
                                onToolClick(tool)
                            }
                        )
                    }
                }
            }
        }

        // 4. Engineering Categories Header
        item(key = "section_categories_header", contentType = "section_header") {
            SectionHeader(
                title = "Disciplines de l'ingénierie",
                subtitle = "12 catégories spécialisées",
                actionLabel = "Tout voir",
                onActionClick = onNavigateToTools
            )
        }

        // 5. Engineering Categories Grid (Paired rows for stable adaptive layout)
        val categoryPairs = uiState.categories.chunked(2)
        items(
            items = categoryPairs,
            key = { pair -> "cat_pair_${pair[0].id.name}" },
            contentType = { "category_pair_row" }
        ) { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                EngineeringCategoryCard(
                    category = pair[0],
                    onClick = { onCategoryClick(pair[0].id) },
                    modifier = Modifier.weight(1f)
                )

                if (pair.size > 1) {
                    EngineeringCategoryCard(
                        category = pair[1],
                        onClick = { onCategoryClick(pair[1].id) },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        // 6. Favorites Section
        item(key = "section_favorites_header", contentType = "section_header") {
            SectionHeader(
                title = "Outils favoris",
                subtitle = "Vos raccourcis personnalisés"
            )
        }

        if (uiState.favoriteTools.isEmpty()) {
            item(key = "favorites_empty", contentType = "empty_state") {
                EmptyState(
                    icon = Icons.Default.BookmarkBorder,
                    title = "Aucun favori enregistré",
                    description = "Appuyez sur l'icône signet d'un outil pour l'épingler sur votre tableau de bord."
                )
            }
        } else {
            items(
                items = uiState.favoriteTools,
                key = { "home_fav_${it.id}" },
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

        // 7. Recent Tools Section
        item(key = "section_recents_header", contentType = "section_header") {
            SectionHeader(
                title = "Récemment consultés",
                subtitle = "Historique de vos dernières fiches"
            )
        }

        if (uiState.recentTools.isEmpty()) {
            item(key = "recents_empty", contentType = "empty_state") {
                EmptyState(
                    icon = Icons.Default.History,
                    title = "Aucun outil récent",
                    description = "Les fiches techniques que vous consultez apparaîtront ici automatiquement."
                )
            }
        } else {
            items(
                items = uiState.recentTools.take(3),
                key = { "home_rec_${it.id}" },
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

        // 8. Discover EngineerKit Section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("discover_engineerkit_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, PrimaryAccent.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(PrimaryAccent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = PrimaryAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Découvrir EngineerKit",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "EngineerKit est conçu pour fonctionner 100% hors-ligne. Chaque outil intègre les modèles théoriques certifiés ISO / Eurocodes / NF, sans dépendance réseau.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Développée par Marwen Riahi • Kotlin & Compose",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = PrimaryAccent
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = PrimaryAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickAccessCard(
    tool: EngineeringTool,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(190.dp)
            .clickable(onClick = onClick)
            .testTag("quick_access_${tool.id}"),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryIcon(
                    categoryId = tool.categoryId,
                    tint = SecondaryAccent,
                    size = 32.dp,
                    iconSize = 18.dp
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = PrimaryAccent.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = if (tool.isAvailable) "Calculateur" else "Aperçu",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = PrimaryAccent,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = tool.name,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = tool.formulaPreview,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
