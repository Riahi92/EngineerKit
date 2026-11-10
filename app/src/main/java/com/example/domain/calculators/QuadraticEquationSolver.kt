package com.example.domain.calculators

import com.example.domain.formatting.NumberFormatter
import com.example.domain.models.CalculationResult
import com.example.domain.models.CalculationStep
import com.example.domain.models.CalculatorOutput
import com.example.domain.models.EngineOutcome
import kotlin.math.abs
import kotlin.math.sqrt

object QuadraticEquationSolver {

    fun solve(a: Double, b: Double, c: Double): EngineOutcome {
        val steps = mutableListOf<CalculationStep>()
        val outputs = mutableListOf<CalculatorOutput>()
        val warnings = mutableListOf<String>()

        if (a == 0.0) {
            // Degenerate linear equation
            if (b == 0.0) {
                if (c == 0.0) {
                    return EngineOutcome.Failure("Équation indéterminée (0 = 0) : infinité de solutions.")
                } else {
                    return EngineOutcome.Failure("Équation impossible ($c = 0) : aucune solution.")
                }
            }

            val x = -c / b
            steps.add(
                CalculationStep(
                    title = "Dégénérescence linéaire (a = 0)",
                    formula = "b · x + c = 0  =>  x = -c / b",
                    substitution = "x = -($c) / $b",
                    resultSnippet = "x = ${NumberFormatter.format(x)}"
                )
            )
            warnings.add("Le coefficient quadratique 'a' est nul : l'équation se réduit au 1er degré.")
            outputs.add(
                CalculatorOutput(
                    label = "Racine unique (linéaire)",
                    valueFormatted = NumberFormatter.format(x),
                    unit = "",
                    isPrimary = true
                )
            )

            return EngineOutcome.Success(
                CalculationResult(
                    outputs = outputs,
                    steps = steps,
                    warnings = warnings,
                    canonicalFormula = "b·x + c = 0"
                )
            )
        }

        // Discriminant calculation
        val delta = b * b - 4 * a * c
        steps.add(
            CalculationStep(
                title = "Calcul du discriminant (Δ)",
                formula = "Δ = b² - 4·a·c",
                substitution = "Δ = ($b)² - 4·($a)·($c)",
                resultSnippet = "Δ = ${NumberFormatter.format(delta)}"
            )
        )

        outputs.add(
            CalculatorOutput(
                label = "Discriminant (Δ)",
                valueFormatted = NumberFormatter.format(delta),
                unit = "",
                description = if (delta > 0) "Δ > 0 (Deux racines réelles)" else if (delta == 0.0) "Δ = 0 (Racine double)" else "Δ < 0 (Deux racines complexes)"
            )
        )

        when {
            delta > 0.0 -> {
                val sqrtDelta = sqrt(delta)
                val x1 = (-b - sqrtDelta) / (2 * a)
                val x2 = (-b + sqrtDelta) / (2 * a)

                steps.add(
                    CalculationStep(
                        title = "Racines réelles distinctes",
                        formula = "x₁,₂ = (-b ± √Δ) / (2·a)",
                        substitution = "x₁ = (-($b) - $sqrtDelta) / (2·$a)\nx₂ = (-($b) + $sqrtDelta) / (2·$a)",
                        resultSnippet = "x₁ = ${NumberFormatter.format(x1)}, x₂ = ${NumberFormatter.format(x2)}"
                    )
                )

                outputs.add(CalculatorOutput("Première racine x₁", NumberFormatter.format(x1), "", isPrimary = true))
                outputs.add(CalculatorOutput("Deuxième racine x₂", NumberFormatter.format(x2), "", isPrimary = true))
            }
            delta == 0.0 -> {
                val x0 = -b / (2 * a)
                steps.add(
                    CalculationStep(
                        title = "Racine réelle double",
                        formula = "x₀ = -b / (2·a)",
                        substitution = "x₀ = -($b) / (2·$a)",
                        resultSnippet = "x₀ = ${NumberFormatter.format(x0)}"
                    )
                )
                outputs.add(CalculatorOutput("Racine double x₀", NumberFormatter.format(x0), "", isPrimary = true))
            }
            else -> {
                val realPart = -b / (2 * a)
                val imagPart = sqrt(-delta) / (2 * abs(a))

                val x1Str = "${NumberFormatter.format(realPart)} - ${NumberFormatter.format(imagPart)} i"
                val x2Str = "${NumberFormatter.format(realPart)} + ${NumberFormatter.format(imagPart)} i"

                steps.add(
                    CalculationStep(
                        title = "Racines complexes conjuguées",
                        formula = "x₁,₂ = -b/(2a) ± i·√(−Δ)/(2|a|)",
                        substitution = "Partie réelle = -($b) / (2·$a) = ${NumberFormatter.format(realPart)}\nPartie imaginaire = √(${NumberFormatter.format(-delta)}) / (2·|${a}|) = ${NumberFormatter.format(imagPart)}",
                        resultSnippet = "x₁ = $x1Str, x₂ = $x2Str"
                    )
                )

                outputs.add(CalculatorOutput("Racine complexe z₁", x1Str, "", isPrimary = true))
                outputs.add(CalculatorOutput("Racine complexe z₂", x2Str, "", isPrimary = true))
            }
        }

        return EngineOutcome.Success(
            CalculationResult(
                outputs = outputs,
                steps = steps,
                assumptions = listOf(
                    "Équation polynomiale à coefficients réels.",
                    "Formule analytique de Viète et Cardan."
                ),
                warnings = warnings,
                canonicalFormula = "a·x² + b·x + c = 0"
            )
        )
    }
}
