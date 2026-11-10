package com.example.core.model

import androidx.compose.runtime.Immutable

@Immutable
data class PlannedInput(
    val label: String,
    val symbol: String,
    val defaultUnit: String,
    val description: String
)

@Immutable
data class PlannedOutput(
    val label: String,
    val symbol: String,
    val defaultUnit: String
)

@Immutable
data class EngineeringTool(
    val id: String,
    val name: String,
    val shortDescription: String,
    val categoryId: CategoryId,
    val isAvailable: Boolean = false,
    val releasePhase: String = "Phase 2 : Calculateur certifié",
    val formulaPreview: String,
    val formulaExplanation: String,
    val plannedInputs: List<PlannedInput>,
    val plannedOutputs: List<PlannedOutput>,
    val tags: List<String> = emptyList(),
    val isFavorite: Boolean = false
)
