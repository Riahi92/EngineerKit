package com.example.domain.calculators

import com.example.domain.formatting.NumberFormatter
import com.example.domain.models.CalculationResult
import com.example.domain.models.CalculationStep
import com.example.domain.models.CalculatorOutput
import com.example.domain.models.EngineOutcome

object OeeTrsCalculator {

    fun calculate(
        plannedTimeHours: Double,
        operatingTimeHours: Double,
        idealCycleTimeSeconds: Double,
        totalQuantity: Double,
        goodQuantity: Double
    ): EngineOutcome {
        if (plannedTimeHours <= 0.0) {
            return EngineOutcome.Failure("Le temps d'ouverture planifié doit être strictement positif.")
        }
        if (operatingTimeHours < 0.0) {
            return EngineOutcome.Failure("Le temps de fonctionnement ne peut pas être négatif.")
        }
        if (operatingTimeHours > plannedTimeHours) {
            return EngineOutcome.Failure("Le temps de fonctionnement ($operatingTimeHours h) ne peut pas excéder le temps planifié ($plannedTimeHours h).")
        }
        if (idealCycleTimeSeconds <= 0.0) {
            return EngineOutcome.Failure("Le temps de cycle idéal unitaire doit être strictement positif.")
        }
        if (totalQuantity < 0.0 || goodQuantity < 0.0) {
            return EngineOutcome.Failure("Les quantités produites ne peuvent pas être négatives.")
        }
        if (goodQuantity > totalQuantity) {
            return EngineOutcome.Failure("La quantité de pièces bonnes ($goodQuantity) ne peut pas excéder la quantité totale ($totalQuantity).")
        }

        val warnings = mutableListOf<String>()

        // 1. Availability
        val availability = if (plannedTimeHours > 0) operatingTimeHours / plannedTimeHours else 0.0
        val availabilityPercent = availability * 100.0

        // 2. Performance
        // Operating time in seconds
        val operatingTimeSeconds = operatingTimeHours * 3600.0
        val performance = if (operatingTimeSeconds > 0) {
            (idealCycleTimeSeconds * totalQuantity) / operatingTimeSeconds
        } else 0.0
        val performancePercent = performance * 100.0

        if (performancePercent > 100.0) {
            warnings.add("Performance anormale (> 100%) : Le temps de cycle réel a été plus rapide que le cycle idéal de référence (possible sous-estimation du temps théorique).")
        }

        // 3. Quality
        val quality = if (totalQuantity > 0) goodQuantity / totalQuantity else 1.0
        val qualityPercent = quality * 100.0

        // 4. Global OEE / TRS
        val oee = availability * performance * quality
        val oeePercent = oee * 100.0

        val steps = listOf(
            CalculationStep(
                title = "1. Taux de Disponibilité opérationnelle (A)",
                formula = "Disponibilité = Temps réel de fonctionnement / Temps planifié",
                substitution = "A = $operatingTimeHours h / $plannedTimeHours h",
                resultSnippet = "A = ${NumberFormatter.format(availabilityPercent, 2)} %"
            ),
            CalculationStep(
                title = "2. Taux de Performance de cadence (P)",
                formula = "Performance = (Temps de cycle idéal × Pièces totales) / Temps de fonctionnement",
                substitution = "P = ($idealCycleTimeSeconds s × $totalQuantity pcs) / ($operatingTimeHours × 3600 s)",
                resultSnippet = "P = ${NumberFormatter.format(performancePercent, 2)} %"
            ),
            CalculationStep(
                title = "3. Taux de Qualité de fabrication (Q)",
                formula = "Qualité = Pièces conformes bonnes / Pièces totales",
                substitution = "Q = $goodQuantity pcs / $totalQuantity pcs",
                resultSnippet = "Q = ${NumberFormatter.format(qualityPercent, 2)} %"
            ),
            CalculationStep(
                title = "4. Taux de Rendement Synthétique global (TRS / OEE)",
                formula = "TRS = Disponibilité × Performance × Qualité",
                substitution = "TRS = ${NumberFormatter.format(availability, 4)} × ${NumberFormatter.format(performance, 4)} × ${NumberFormatter.format(quality, 4)}",
                resultSnippet = "TRS = ${NumberFormatter.format(oeePercent, 2)} %"
            )
        )

        val outputs = listOf(
            CalculatorOutput("TRS / OEE Global", NumberFormatter.format(oeePercent, 2), "%", isPrimary = true),
            CalculatorOutput("Taux de Disponibilité", NumberFormatter.format(availabilityPercent, 2), "%", isPrimary = true),
            CalculatorOutput("Taux de Performance", NumberFormatter.format(performancePercent, 2), "%", isPrimary = true),
            CalculatorOutput("Taux de Qualité", NumberFormatter.format(qualityPercent, 2), "%", isPrimary = true),
            CalculatorOutput("Rebuts / Non-conformes", (totalQuantity - goodQuantity).toInt().toString(), "pièces"),
            CalculatorOutput("Temps d'arrêt estimé", NumberFormatter.format(plannedTimeHours - operatingTimeHours, 2), "heures")
        )

        return EngineOutcome.Success(
            CalculationResult(
                outputs = outputs,
                steps = steps,
                assumptions = listOf(
                    "Standard industriel NF E60-182 / TPM (Total Productive Maintenance).",
                    "Temps d'arrêt non planifiés (pannes, réglages, changements de séries) imputés sur la disponibilité."
                ),
                warnings = warnings,
                canonicalFormula = "OEE = Disponibilité × Performance × Qualité"
            )
        )
    }
}
