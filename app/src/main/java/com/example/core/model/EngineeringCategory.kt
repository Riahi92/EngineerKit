package com.example.core.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

enum class CategoryId {
    MECANIQUE,
    ELECTRIQUE,
    ELECTRONIQUE,
    INDUSTRIEL,
    CIVIL,
    HYDRAULIQUE,
    THERMIQUE,
    CHIMIQUE,
    INFORMATIQUE,
    AUTOMATISME,
    ENVIRONNEMENT,
    MATHEMATIQUES
}

@Immutable
data class EngineeringCategory(
    val id: CategoryId,
    val name: String,
    val shortDescription: String,
    val iconName: String,
    val accentColor: Color,
    val toolCount: Int,
    val longDescription: String
)
