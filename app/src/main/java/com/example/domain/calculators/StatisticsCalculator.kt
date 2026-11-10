package com.example.domain.calculators

import com.example.domain.formatting.NumberFormatter
import com.example.domain.models.CalculationResult
import com.example.domain.models.CalculationStep
import com.example.domain.models.CalculatorOutput
import com.example.domain.models.EngineOutcome
import kotlin.math.sqrt

object StatisticsCalculator {

    fun parseDataset(rawInput: String): List<Double> {
        val delimiters = Regex("[,;\\s]+")
        return rawInput.trim()
            .split(delimiters)
            .filter { it.isNotBlank() }
            .mapNotNull { it.replace(',', '.').toDoubleOrNull() }
    }

    fun calculate(values: List<Double>): EngineOutcome {
        if (values.isEmpty()) {
            return EngineOutcome.Failure("Le jeu de données est vide. Saisissez au moins un nombre valide.")
        }

        val n = values.size
        val sorted = values.sorted()
        val sum = values.sum()
        val mean = sum / n

        val median = if (n % 2 == 1) {
            sorted[n / 2]
        } else {
            (sorted[n / 2 - 1] + sorted[n / 2]) / 2.0
        }

        val min = sorted.first()
        val max = sorted.last()
        val range = max - min

        // Sum of squared deviations
        val ss = values.sumOf { (it - mean) * (it - mean) }
        val popVariance = ss / n
        val popStdDev = sqrt(popVariance)

        val sampleVariance = if (n > 1) ss / (n - 1) else Double.NaN
        val sampleStdDev = if (n > 1) sqrt(sampleVariance) else Double.NaN

        val steps = mutableListOf<CalculationStep>()
        steps.add(
            CalculationStep(
                title = "Moyenne arithmétique",
                formula = "μ = (∑ x_i) / N",
                substitution = "μ = $sum / $n",
                resultSnippet = "μ = ${NumberFormatter.format(mean)}"
            )
        )
        steps.add(
            CalculationStep(
                title = "Somme des écarts au carré",
                formula = "SS = ∑ (x_i - μ)²",
                substitution = "SS = ${NumberFormatter.format(ss)}",
                resultSnippet = "Variance pop = SS / N = ${NumberFormatter.format(popVariance)}"
            )
        )

        val outputs = mutableListOf(
            CalculatorOutput("Effectif (N)", n.toString(), "valeurs"),
            CalculatorOutput("Moyenne (μ / x̄)", NumberFormatter.format(mean), "", isPrimary = true),
            CalculatorOutput("Médiane", NumberFormatter.format(median), "", isPrimary = true),
            CalculatorOutput("Étendue (Max - Min)", NumberFormatter.format(range), ""),
            CalculatorOutput("Minimum", NumberFormatter.format(min), ""),
            CalculatorOutput("Maximum", NumberFormatter.format(max), ""),
            CalculatorOutput("Somme totale", NumberFormatter.format(sum), ""),
            CalculatorOutput("Écart-type population (σ)", NumberFormatter.format(popStdDev), "", isPrimary = true),
            CalculatorOutput("Variance population (σ²)", NumberFormatter.format(popVariance), ""),
            CalculatorOutput(
                "Écart-type échantillon (s)",
                if (n > 1) NumberFormatter.format(sampleStdDev) else "N/A (N < 2)",
                ""
            ),
            CalculatorOutput(
                "Variance échantillon (s²)",
                if (n > 1) NumberFormatter.format(sampleVariance) else "N/A (N < 2)",
                ""
            )
        )

        val warnings = mutableListOf<String>()
        if (n < 2) {
            warnings.add("L'échantillon comporte un seul élément : la variance d'échantillon (degré de liberté N-1) est indéfinie.")
        }

        return EngineOutcome.Success(
            CalculationResult(
                outputs = outputs,
                steps = steps,
                assumptions = listOf(
                    "Distribution échantillonnée de variables quantitatives continues ou discrètes.",
                    "La variance d'échantillon utilise la correction de Bessel (division par N-1)."
                ),
                warnings = warnings,
                canonicalFormula = "x̄ = (∑x)/N  |  s = √[ ∑(x - x̄)² / (N-1) ]"
            )
        )
    }
}
