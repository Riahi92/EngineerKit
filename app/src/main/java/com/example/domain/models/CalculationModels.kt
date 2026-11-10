package com.example.domain.models

import androidx.compose.runtime.Immutable

@Immutable
data class CalculationStep(
    val title: String,
    val formula: String,
    val substitution: String,
    val resultSnippet: String
)

@Immutable
data class CalculatorOutput(
    val label: String,
    val valueFormatted: String,
    val unit: String,
    val isPrimary: Boolean = false,
    val description: String? = null
)

@Immutable
data class CalculationResult(
    val outputs: List<CalculatorOutput>,
    val steps: List<CalculationStep> = emptyList(),
    val assumptions: List<String> = emptyList(),
    val warnings: List<String> = emptyList(),
    val canonicalFormula: String = ""
)

sealed interface EngineOutcome {
    data class Success(val result: CalculationResult) : EngineOutcome
    data class Failure(val error: String, val invalidFieldId: String? = null) : EngineOutcome
}
