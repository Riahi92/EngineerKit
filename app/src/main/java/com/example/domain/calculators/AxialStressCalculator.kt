package com.example.domain.calculators

import com.example.domain.formatting.NumberFormatter
import com.example.domain.models.CalculationResult
import com.example.domain.models.CalculationStep
import com.example.domain.models.CalculatorOutput
import com.example.domain.models.EngineOutcome
import com.example.domain.units.EngineeringUnit
import com.example.domain.units.UnitEngine
import com.example.domain.units.UnitRegistry
import kotlin.math.abs

object AxialStressCalculator {

    fun calculate(
        forceValue: Double,
        forceUnit: EngineeringUnit,
        areaValue: Double,
        areaUnit: EngineeringUnit,
        yieldStrengthMpa: Double? = null
    ): EngineOutcome {
        if (areaValue <= 0.0) {
            return EngineOutcome.Failure("La section droite 'A' doit être strictement supérieure à zéro.", "area")
        }

        // Convert Force to N, Area to m² and mm²
        val forceN = UnitEngine.convert(forceValue, forceUnit, UnitRegistry.N)
        val areaM2 = UnitEngine.convert(areaValue, areaUnit, UnitRegistry.M2)
        val areaMm2 = UnitEngine.convert(areaValue, areaUnit, UnitRegistry.MM2)

        val stressPa = forceN / areaM2
        val stressMpa = stressPa / 1e6
        val stressNmm2 = forceN / areaMm2 // 1 MPa == 1 N/mm²

        val nature = when {
            forceN > 0 -> "Traction (+)"
            forceN < 0 -> "Compression (-)"
            else -> "Effort nul"
        }

        val steps = mutableListOf<CalculationStep>()
        steps.add(
            CalculationStep(
                title = "Conversion des unités au système SI",
                formula = "F [N] = F_saisie · Facteur  |  A [mm²] = A_saisie · Facteur",
                substitution = "F = ${NumberFormatter.format(forceN)} N  |  A = ${NumberFormatter.format(areaMm2)} mm²",
                resultSnippet = "Entrées converties"
            )
        )
        steps.add(
            CalculationStep(
                title = "Calcul de la contrainte normale axiale",
                formula = "σ = F / A",
                substitution = "σ = ${NumberFormatter.format(forceN)} N / ${NumberFormatter.format(areaMm2)} mm²",
                resultSnippet = "σ = ${NumberFormatter.format(stressMpa)} MPa"
            )
        )

        val outputs = mutableListOf(
            CalculatorOutput("Contrainte axiale (σ)", NumberFormatter.format(stressMpa), "MPa", isPrimary = true),
            CalculatorOutput("Contrainte en N/mm²", NumberFormatter.format(stressNmm2), "N/mm²"),
            CalculatorOutput("Régime d'effort", nature, "")
        )

        val warnings = mutableListOf<String>()
        warnings.add("Hypothèse de répartition uniforme : valable uniquement en dehors des zones d'application des charges (Principe de Barré de Saint-Venant).")
        warnings.add("Avertissement normatif : Le ratio limite élastique / contrainte ne constitue pas une vérification complète certifiée (flambement pour compression, concentrations de contraintes et fatigue non inclus).")

        if (yieldStrengthMpa != null && yieldStrengthMpa > 0.0) {
            val absStress = abs(stressMpa)
            if (absStress > 1e-9) {
                val ratio = yieldStrengthMpa / absStress
                steps.add(
                    CalculationStep(
                        title = "Ratio limite élastique / contrainte",
                        formula = "s = R_e / |σ|",
                        substitution = "s = $yieldStrengthMpa MPa / ${NumberFormatter.format(absStress)} MPa",
                        resultSnippet = "s = ${NumberFormatter.format(ratio)}"
                    )
                )
                outputs.add(
                    CalculatorOutput(
                        label = "Ratio limite élastique / |σ|",
                        valueFormatted = NumberFormatter.format(ratio),
                        unit = "",
                        isPrimary = true,
                        description = if (ratio >= 1.0) "Domaine élastique admissible (s ≥ 1)" else "Dépassement de la limite élastique (Plasticité)"
                    )
                )
            }
        }

        return EngineOutcome.Success(
            CalculationResult(
                outputs = outputs,
                steps = steps,
                assumptions = listOf(
                    "Poutre droite prismatique à section constante.",
                    "Matériau homogène, isotrope et à comportement élastique linéaire Hookéen.",
                    "L'axe de l'effort passe exactement par le centre de gravité de la section."
                ),
                warnings = warnings,
                canonicalFormula = "σ = F / A"
            )
        )
    }
}
