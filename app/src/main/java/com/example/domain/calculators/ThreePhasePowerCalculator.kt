package com.example.domain.calculators

import com.example.domain.formatting.NumberFormatter
import com.example.domain.models.CalculationResult
import com.example.domain.models.CalculationStep
import com.example.domain.models.CalculatorOutput
import com.example.domain.models.EngineOutcome
import kotlin.math.sqrt

object ThreePhasePowerCalculator {

    fun calculate(
        voltageV: Double, // Line-to-line (composee)
        currentA: Double, // Line current
        powerFactor: Double // cos phi in [0, 1]
    ): EngineOutcome {
        if (voltageV <= 0.0) {
            return EngineOutcome.Failure("La tension entre phases (U) doit être strictement positive.")
        }
        if (currentA <= 0.0) {
            return EngineOutcome.Failure("Le courant de ligne (I) doit être strictement positif.")
        }
        if (powerFactor < 0.0 || powerFactor > 1.0) {
            return EngineOutcome.Failure("Le facteur de puissance (cos φ) doit être compris entre 0.0 et 1.0.")
        }

        val sqrt3 = sqrt(3.0)
        // S in VA
        val sVa = sqrt3 * voltageV * currentA
        val sKva = sVa / 1000.0

        // P in W
        val pW = sVa * powerFactor
        val pKw = pW / 1000.0

        // sin(phi) = sqrt(1 - cos^2)
        val sinPhi = sqrt(1.0 - powerFactor * powerFactor)
        // Q in VAR
        val qVar = sVa * sinPhi
        val qKvar = qVar / 1000.0

        val steps = listOf(
            CalculationStep(
                title = "Calcul de la puissance apparente (S)",
                formula = "S = √3 · U · I",
                substitution = "S = √3 × $voltageV V × $currentA A",
                resultSnippet = "S = ${NumberFormatter.format(sKva)} kVA (${NumberFormatter.format(sVa)} VA)"
            ),
            CalculationStep(
                title = "Calcul de la puissance active (P)",
                formula = "P = S · cos φ = √3 · U · I · cos φ",
                substitution = "P = ${NumberFormatter.format(sVa)} VA × $powerFactor",
                resultSnippet = "P = ${NumberFormatter.format(pKw)} kW"
            ),
            CalculationStep(
                title = "Calcul de la puissance réactive (Q)",
                formula = "Q = S · sin φ = S · √(1 - cos² φ)",
                substitution = "Q = ${NumberFormatter.format(sVa)} VA × ${NumberFormatter.format(sinPhi)}",
                resultSnippet = "Q = ${NumberFormatter.format(qKvar)} kVAR"
            )
        )

        val outputs = listOf(
            CalculatorOutput("Puissance active (P)", NumberFormatter.format(pKw), "kW", isPrimary = true),
            CalculatorOutput("Puissance apparente (S)", NumberFormatter.format(sKva), "kVA", isPrimary = true),
            CalculatorOutput("Puissance réactive (Q)", NumberFormatter.format(qKvar), "kVAR", isPrimary = true),
            CalculatorOutput("Facteur de puissance (cos φ)", NumberFormatter.format(powerFactor), ""),
            CalculatorOutput("Facteur réactif (sin φ)", NumberFormatter.format(sinPhi), ""),
            CalculatorOutput("Courant absorbé", NumberFormatter.format(currentA), "A")
        )

        val warnings = mutableListOf<String>()
        if (powerFactor < 0.85) {
            warnings.add("Facteur de puissance faible (cos φ < 0.85) : Risque de pénalités pour consommation excessive d'énergie réactive.")
        }

        return EngineOutcome.Success(
            CalculationResult(
                outputs = outputs,
                steps = steps,
                assumptions = listOf(
                    "Réseau triphasé sinusoïdal parfaitement équilibré en tensions et en courants.",
                    "Charge symétrique sans harmonique de courant (THD négligé)."
                ),
                warnings = warnings,
                canonicalFormula = "P = √3·U·I·cosφ  |  Q = √3·U·I·sinφ  |  S = √3·U·I"
            )
        )
    }
}
