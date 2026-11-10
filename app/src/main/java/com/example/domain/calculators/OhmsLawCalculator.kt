package com.example.domain.calculators

import com.example.domain.formatting.NumberFormatter
import com.example.domain.models.CalculationResult
import com.example.domain.models.CalculationStep
import com.example.domain.models.CalculatorOutput
import com.example.domain.models.EngineOutcome
import kotlin.math.sqrt

enum class OhmsLawPair(val displayName: String) {
    VOLTAGE_CURRENT("Tension (U) et Courant (I)"),
    VOLTAGE_RESISTANCE("Tension (U) et Résistance (R)"),
    VOLTAGE_POWER("Tension (U) et Puissance (P)"),
    CURRENT_RESISTANCE("Courant (I) et Résistance (R)"),
    CURRENT_POWER("Courant (I) et Puissance (P)"),
    RESISTANCE_POWER("Résistance (R) et Puissance (P)")
}

object OhmsLawCalculator {

    fun calculate(
        pair: OhmsLawPair,
        val1: Double,
        val2: Double
    ): EngineOutcome {
        if (val1 <= 0 || val2 <= 0) {
            return EngineOutcome.Failure("Les grandeurs électriques doivent être strictement positives.")
        }

        val u: Double
        val i: Double
        val r: Double
        val p: Double
        val steps = mutableListOf<CalculationStep>()

        when (pair) {
            OhmsLawPair.VOLTAGE_CURRENT -> {
                u = val1
                i = val2
                r = u / i
                p = u * i
                steps.add(CalculationStep("Calcul de la résistance (R)", "R = U / I", "R = $u V / $i A", "R = ${NumberFormatter.format(r)} Ω"))
                steps.add(CalculationStep("Calcul de la puissance dissipée (P)", "P = U · I", "P = $u V × $i A", "P = ${NumberFormatter.format(p)} W"))
            }
            OhmsLawPair.VOLTAGE_RESISTANCE -> {
                u = val1
                r = val2
                i = u / r
                p = (u * u) / r
                steps.add(CalculationStep("Calcul du courant absorbé (I)", "I = U / R", "I = $u V / $r Ω", "I = ${NumberFormatter.format(i)} A"))
                steps.add(CalculationStep("Calcul de la puissance dissipée (P)", "P = U² / R", "P = ($u V)² / $r Ω", "P = ${NumberFormatter.format(p)} W"))
            }
            OhmsLawPair.VOLTAGE_POWER -> {
                u = val1
                p = val2
                i = p / u
                r = (u * u) / p
                steps.add(CalculationStep("Calcul du courant (I)", "I = P / U", "I = $p W / $u V", "I = ${NumberFormatter.format(i)} A"))
                steps.add(CalculationStep("Calcul de la résistance équivalente (R)", "R = U² / P", "R = ($u V)² / $p W", "R = ${NumberFormatter.format(r)} Ω"))
            }
            OhmsLawPair.CURRENT_RESISTANCE -> {
                i = val1
                r = val2
                u = i * r
                p = r * i * i
                steps.add(CalculationStep("Calcul de la chute de tension (U)", "U = R · I", "U = $r Ω × $i A", "U = ${NumberFormatter.format(u)} V"))
                steps.add(CalculationStep("Calcul de l'effet Joule (P)", "P = R · I²", "P = $r Ω × ($i A)²", "P = ${NumberFormatter.format(p)} W"))
            }
            OhmsLawPair.CURRENT_POWER -> {
                i = val1
                p = val2
                u = p / i
                r = p / (i * i)
                steps.add(CalculationStep("Calcul de la tension (U)", "U = P / I", "U = $p W / $i A", "U = ${NumberFormatter.format(u)} V"))
                steps.add(CalculationStep("Calcul de la résistance (R)", "R = P / I²", "R = $p W / ($i A)²", "R = ${NumberFormatter.format(r)} Ω"))
            }
            OhmsLawPair.RESISTANCE_POWER -> {
                r = val1
                p = val2
                u = sqrt(p * r)
                i = sqrt(p / r)
                steps.add(CalculationStep("Calcul de la tension (U)", "U = √(P · R)", "U = √($p W × $r Ω)", "U = ${NumberFormatter.format(u)} V"))
                steps.add(CalculationStep("Calcul du courant (I)", "I = √(P / R)", "I = √($p W / $r Ω)", "I = ${NumberFormatter.format(i)} A"))
            }
        }

        val outputs = listOf(
            CalculatorOutput("Tension (U)", NumberFormatter.format(u), "V", isPrimary = true),
            CalculatorOutput("Intensité du courant (I)", NumberFormatter.format(i), "A", isPrimary = true),
            CalculatorOutput("Résistance (R)", NumberFormatter.format(r), "Ω", isPrimary = true),
            CalculatorOutput("Puissance dissipée (P)", NumberFormatter.format(p), "W", isPrimary = true)
        )

        return EngineOutcome.Success(
            CalculationResult(
                outputs = outputs,
                steps = steps,
                assumptions = listOf(
                    "Circuit purement résistif en régime continu (DC) ou alternatif monophasé (cos φ = 1).",
                    "Résistance constante indépendante de l'échauffement thermique (loi d'Ohm linéaire)."
                ),
                canonicalFormula = "U = R · I  |  P = U · I"
            )
        )
    }
}
