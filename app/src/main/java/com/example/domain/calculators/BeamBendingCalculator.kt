package com.example.domain.calculators

import com.example.domain.formatting.NumberFormatter
import com.example.domain.models.CalculationResult
import com.example.domain.models.CalculationStep
import com.example.domain.models.CalculatorOutput
import com.example.domain.models.EngineOutcome
import kotlin.math.abs

enum class BeamLoadType(val displayName: String) {
    CENTRAL_POINT_LOAD("Charge ponctuelle centrée (F)"),
    UNIFORM_DISTRIBUTED_LOAD("Charge répartie uniforme (q)")
}

object BeamBendingCalculator {

    fun calculate(
        loadType: BeamLoadType,
        spanLengthM: Double,
        loadMagnitude: Double, // F in kN or q in kN/m
        youngModulusGpa: Double,
        inertiaCm4: Double,
        sectionModulusCm3: Double? = null
    ): EngineOutcome {
        if (spanLengthM <= 0.0) {
            return EngineOutcome.Failure("La portée de la poutre 'L' doit être strictement positive.")
        }
        if (youngModulusGpa <= 0.0) {
            return EngineOutcome.Failure("Le module de Young 'E' doit être strictement positif.")
        }
        if (inertiaCm4 <= 0.0) {
            return EngineOutcome.Failure("Le moment quadratique 'I' doit être strictement positif.")
        }

        // Convert to SI base units:
        // L in m
        // E: GPa = 10^9 N/m²
        val ePa = youngModulusGpa * 1e9
        // I: 1 cm^4 = 10^-8 m^4
        val iM4 = inertiaCm4 * 1e-8
        // Wel: 1 cm^3 = 10^-6 m^3
        val welM3 = (sectionModulusCm3 ?: 0.0) * 1e-6

        val mMaxNm: Double
        val deflM: Double
        val formulaMoment: String
        val formulaDefl: String
        val substMoment: String
        val substDefl: String

        when (loadType) {
            BeamLoadType.CENTRAL_POINT_LOAD -> {
                // F in N
                val forceN = loadMagnitude * 1000.0
                mMaxNm = (forceN * spanLengthM) / 4.0
                deflM = (forceN * spanLengthM * spanLengthM * spanLengthM) / (48.0 * ePa * iM4)

                formulaMoment = "M_max = (F · L) / 4"
                formulaDefl = "δ_max = (F · L³) / (48 · E · I)"
                substMoment = "M_max = (${forceN} N × ${spanLengthM} m) / 4"
                substDefl = "δ_max = (${forceN} N × (${spanLengthM} m)³) / [48 × ${NumberFormatter.format(ePa)} Pa × ${NumberFormatter.format(iM4)} m⁴]"
            }
            BeamLoadType.UNIFORM_DISTRIBUTED_LOAD -> {
                // q in N/m
                val qNm = loadMagnitude * 1000.0
                mMaxNm = (qNm * spanLengthM * spanLengthM) / 8.0
                deflM = (5.0 * qNm * spanLengthM * spanLengthM * spanLengthM * spanLengthM) / (384.0 * ePa * iM4)

                formulaMoment = "M_max = (q · L²) / 8"
                formulaDefl = "δ_max = (5 · q · L⁴) / (384 · E · I)"
                substMoment = "M_max = (${qNm} N/m × (${spanLengthM} m)²) / 8"
                substDefl = "δ_max = [5 × ${qNm} N/m × (${spanLengthM} m)⁴] / [384 × ${NumberFormatter.format(ePa)} Pa × ${NumberFormatter.format(iM4)} m⁴]"
            }
        }

        val mMaxKnm = mMaxNm / 1000.0
        val deflMm = deflM * 1000.0
        val deflRatio = if (deflMm > 0) spanLengthM * 1000.0 / deflMm else Double.POSITIVE_INFINITY

        val steps = mutableListOf(
            CalculationStep(
                title = "Moment fléchissant maximal à mi-travée",
                formula = formulaMoment,
                substitution = substMoment,
                resultSnippet = "M_max = ${NumberFormatter.format(mMaxKnm)} kN·m (${NumberFormatter.format(mMaxNm)} N·m)"
            ),
            CalculationStep(
                title = "Flèche élastique maximale au centre",
                formula = formulaDefl,
                substitution = substDefl,
                resultSnippet = "δ_max = ${NumberFormatter.format(deflMm)} mm"
            )
        )

        val outputs = mutableListOf(
            CalculatorOutput("Moment fléchissant max (M_max)", NumberFormatter.format(mMaxKnm), "kN·m", isPrimary = true),
            CalculatorOutput("Flèche maximale (δ_max)", NumberFormatter.format(deflMm), "mm", isPrimary = true),
            CalculatorOutput("Rapport flèche / portée", "L / ${NumberFormatter.format(deflRatio, 0)}", "")
        )

        if (welM3 > 0.0) {
            val stressPa = mMaxNm / welM3
            val stressMpa = stressPa / 1e6
            steps.add(
                CalculationStep(
                    title = "Contrainte normale maximale de flexion",
                    formula = "σ_max = M_max / W_el",
                    substitution = "σ_max = ${NumberFormatter.format(mMaxNm)} N·m / ${NumberFormatter.format(welM3)} m³",
                    resultSnippet = "σ_max = ${NumberFormatter.format(stressMpa)} MPa"
                )
            )
            outputs.add(
                CalculatorOutput("Contrainte de flexion (σ_max)", NumberFormatter.format(stressMpa), "MPa", isPrimary = true)
            )
        }

        val warnings = listOf(
            "Hypothèse de Navier-Bernoulli : sections planes restent planes, déformations transversales de cisaillement négligées.",
            "Avertissement de sécurité : Calcul élastique préliminaire. Ne remplace pas une note de calcul certifiée selon l'Eurocode 3 (acier) ou Eurocode 2 (béton)."
        )

        return EngineOutcome.Success(
            CalculationResult(
                outputs = outputs,
                steps = steps,
                assumptions = listOf(
                    "Poutre droite reposant librement sur deux appuis simples aux extrémités.",
                    "Matériau élastique linéaire isotrope (Petites déformations et petits déplacements).",
                    "Section transversale constante et symétrique par rapport au plan de flexion."
                ),
                warnings = warnings,
                canonicalFormula = formulaMoment + "  |  " + formulaDefl
            )
        )
    }
}
