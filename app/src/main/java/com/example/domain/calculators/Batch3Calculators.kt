package com.example.domain.calculators

import com.example.domain.formatting.NumberFormatter
import com.example.domain.models.CalculationResult
import com.example.domain.models.CalculationStep
import com.example.domain.models.CalculatorOutput
import com.example.domain.models.EngineOutcome
import com.example.domain.units.EngineeringUnit
import com.example.domain.units.UnitEngine
import com.example.domain.units.UnitRegistry

// ============================================================================
// TOOL 22 — TAKT TIME
// ============================================================================
object TaktTimeCalculator {

    fun calculate(
        availableTimeMinutes: Double,
        plannedBreaksMinutes: Double,
        customerDemandUnits: Double
    ): EngineOutcome {
        if (availableTimeMinutes <= 0.0) {
            return EngineOutcome.Failure("Le temps total d'ouverture doit être strictement positif.")
        }
        if (plannedBreaksMinutes < 0.0) {
            return EngineOutcome.Failure("Le temps de pauses planifiées ne peut pas être négatif.")
        }
        val netTimeMinutes = availableTimeMinutes - plannedBreaksMinutes
        if (netTimeMinutes <= 0.0) {
            return EngineOutcome.Failure("Le temps net disponible (Temps total - Pauses) doit être strictement positif.")
        }
        if (customerDemandUnits <= 0.0) {
            return EngineOutcome.Failure("La demande client doit être strictement supérieure à zéro.")
        }

        val netTimeSeconds = netTimeMinutes * 60.0
        val taktSecondsPerUnit = netTimeSeconds / customerDemandUnits
        val taktMinutesPerUnit = netTimeMinutes / customerDemandUnits
        val requiredHourlyRate = (customerDemandUnits / netTimeMinutes) * 60.0

        val steps = listOf(
            CalculationStep(
                title = "1. Temps net de production disponible (T_net)",
                formula = "T_net = T_ouverture - T_pauses",
                substitution = "T_net = $availableTimeMinutes min - $plannedBreaksMinutes min",
                resultSnippet = "T_net = ${NumberFormatter.format(netTimeMinutes, 2)} min (${NumberFormatter.format(netTimeSeconds, 0)} s)"
            ),
            CalculationStep(
                title = "2. Calcul du Takt Time (cadence client)",
                formula = "Takt = T_net / Demande_client",
                substitution = "Takt = ${NumberFormatter.format(netTimeSeconds, 0)} s / $customerDemandUnits unités",
                resultSnippet = "Takt = ${NumberFormatter.format(taktSecondsPerUnit, 2)} s / unité"
            ),
            CalculationStep(
                title = "3. Cadence horaire cible requise",
                formula = "Cadence = 3600 / Takt_s",
                substitution = "Cadence = 3600 / ${NumberFormatter.format(taktSecondsPerUnit, 2)}",
                resultSnippet = "Cadence = ${NumberFormatter.format(requiredHourlyRate, 2)} unités / heure"
            )
        )

        val outputs = listOf(
            CalculatorOutput("Takt Time (en secondes)", NumberFormatter.format(taktSecondsPerUnit, 2), "s / unité", isPrimary = true),
            CalculatorOutput("Takt Time (en minutes)", NumberFormatter.format(taktMinutesPerUnit, 3), "min / unité", isPrimary = true),
            CalculatorOutput("Cadence horaire requise", NumberFormatter.format(requiredHourlyRate, 2), "unités / h", isPrimary = true),
            CalculatorOutput("Temps net disponible", NumberFormatter.format(netTimeMinutes, 1), "min")
        )

        return EngineOutcome.Success(
            CalculationResult(
                outputs = outputs,
                steps = steps,
                assumptions = listOf(
                    "Demande client lissée sur la période de référence.",
                    "Les arrêts planifiés (pauses, maintenance préventive programmée, réunions) sont déduits du temps brut.",
                    "Le Takt Time est un indicateur de rythme de la demande et ne tient pas compte des pannes aléatoires ni des rebuts (voir TRS/OEE)."
                ),
                canonicalFormula = "Takt Time = (Temps disponible - Pauses) / Demande client"
            )
        )
    }
}

// ============================================================================
// TOOL 23 — PRODUCTION CAPACITY
// ============================================================================
object ProductionCapacityCalculator {

    fun calculate(
        operatingTimeHours: Double,
        cycleTimeSecondsPerUnit: Double,
        parallelMachines: Int = 1,
        availabilityPercent: Double = 100.0,
        performancePercent: Double = 100.0
    ): EngineOutcome {
        if (operatingTimeHours <= 0.0) {
            return EngineOutcome.Failure("Le temps d'ouverture opérationnel doit être strictement positif.")
        }
        if (cycleTimeSecondsPerUnit <= 0.0) {
            return EngineOutcome.Failure("Le temps de cycle unitaire doit être strictement positif.")
        }
        if (parallelMachines <= 0) {
            return EngineOutcome.Failure("Le nombre de postes/machines en parallèle doit être au moins égal à 1.")
        }
        if (availabilityPercent <= 0.0 || availabilityPercent > 100.0) {
            return EngineOutcome.Failure("Le facteur de disponibilité doit être compris dans ]0%, 100%].")
        }
        if (performancePercent <= 0.0 || performancePercent > 150.0) {
            return EngineOutcome.Failure("Le facteur de performance doit être compris dans ]0%, 150%].")
        }

        val operatingTimeSeconds = operatingTimeHours * 3600.0
        val singleMachineTheoCapacity = operatingTimeSeconds / cycleTimeSecondsPerUnit
        val totalTheoreticalCapacity = singleMachineTheoCapacity * parallelMachines

        val availabilityFactor = availabilityPercent / 100.0
        val performanceFactor = performancePercent / 100.0
        val adjustedCapacity = totalTheoreticalCapacity * availabilityFactor * performanceFactor
        val hourlyAdjustedRate = adjustedCapacity / operatingTimeHours

        val steps = listOf(
            CalculationStep(
                title = "1. Capacité théorique nominale (100%)",
                formula = "C_théo = N_machines × (T_opérationnel / t_cycle)",
                substitution = "C_théo = $parallelMachines × (${NumberFormatter.format(operatingTimeSeconds, 0)} s / $cycleTimeSecondsPerUnit s)",
                resultSnippet = "C_théo = ${NumberFormatter.format(totalTheoreticalCapacity, 1)} unités"
            ),
            CalculationStep(
                title = "2. Capacité réelle estimée (pondérée)",
                formula = "C_ajustée = C_théo × Disponibilité × Performance",
                substitution = "C_ajustée = ${NumberFormatter.format(totalTheoreticalCapacity, 1)} × $availabilityFactor × $performanceFactor",
                resultSnippet = "C_ajustée = ${NumberFormatter.format(adjustedCapacity, 1)} unités"
            )
        )

        val outputs = listOf(
            CalculatorOutput("Capacité théorique maximale", NumberFormatter.format(totalTheoreticalCapacity, 1), "unités", isPrimary = true),
            CalculatorOutput("Capacité estimée ajustée", NumberFormatter.format(adjustedCapacity, 1), "unités", isPrimary = true),
            CalculatorOutput("Débit horaire ajusté", NumberFormatter.format(hourlyAdjustedRate, 2), "unités / h"),
            CalculatorOutput("Capacité par machine (théorique)", NumberFormatter.format(singleMachineTheoCapacity, 1), "unités")
        )

        return EngineOutcome.Success(
            CalculationResult(
                outputs = outputs,
                steps = steps,
                assumptions = listOf(
                    "Postes ou machines en parallèle indépendants, identiques et alimentés sans famine ni blocage amont/aval.",
                    "Temps de cycle supposé déterministe et constant.",
                    "Les pertes de qualité (rebuts) ne sont pas déduites de la capacité brute."
                ),
                canonicalFormula = "C_théo = N · (T / t_c)  |  C_adj = C_théo · A · P"
            )
        )
    }
}

// ============================================================================
// TOOL 24 — MOLAR CONCENTRATION
// ============================================================================
enum class SoluteInputMode(val displayName: String) {
    FROM_MASS_AND_MOLAR_MASS("À partir de la masse (m) et masse molaire (M)"),
    FROM_MOLES_DIRECTLY("À partir de la quantité de matière (n en mol)")
}

object MolarConcentrationCalculator {

    fun calculate(
        inputMode: SoluteInputMode,
        soluteAmountValue: Double, // mass in selected massUnit OR moles in mol
        massUnit: EngineeringUnit = UnitRegistry.G,
        molarMassGPerMol: Double = 58.44,
        solutionVolumeValue: Double,
        volumeUnit: EngineeringUnit = UnitRegistry.L
    ): EngineOutcome {
        if (soluteAmountValue < 0.0) {
            return EngineOutcome.Failure("La quantité ou masse de soluté doit être positive ou nulle.")
        }
        if (solutionVolumeValue <= 0.0) {
            return EngineOutcome.Failure("Le volume de solution V doit être strictement positif.")
        }

        val volumeLiters = UnitEngine.convert(solutionVolumeValue, volumeUnit, UnitRegistry.L)
        val steps = mutableListOf<CalculationStep>()
        val moles: Double
        val massGrams: Double?

        when (inputMode) {
            SoluteInputMode.FROM_MASS_AND_MOLAR_MASS -> {
                if (molarMassGPerMol <= 0.0) {
                    return EngineOutcome.Failure("La masse molaire M doit être strictement positive (g/mol).")
                }
                massGrams = UnitEngine.convert(soluteAmountValue, massUnit, UnitRegistry.G)
                moles = massGrams / molarMassGPerMol
                steps.add(
                    CalculationStep(
                        title = "1. Quantité de matière de soluté (n)",
                        formula = "n = m / M",
                        substitution = "n = ${NumberFormatter.format(massGrams, 4)} g / $molarMassGPerMol g/mol",
                        resultSnippet = "n = ${NumberFormatter.format(moles, 5)} mol"
                    )
                )
            }
            SoluteInputMode.FROM_MOLES_DIRECTLY -> {
                moles = soluteAmountValue
                massGrams = if (molarMassGPerMol > 0.0) moles * molarMassGPerMol else null
                steps.add(
                    CalculationStep(
                        title = "1. Quantité de matière saisie (n)",
                        formula = "n directe",
                        substitution = "n = ${NumberFormatter.format(moles, 5)} mol",
                        resultSnippet = "V = ${NumberFormatter.format(volumeLiters, 4)} L"
                    )
                )
            }
        }

        val molarityMolPerL = moles / volumeLiters
        val millimolar = molarityMolPerL * 1000.0

        steps.add(
            CalculationStep(
                title = "2. Concentration molaire volumique (C)",
                formula = "C = n / V",
                substitution = "C = ${NumberFormatter.format(moles, 5)} mol / ${NumberFormatter.format(volumeLiters, 4)} L",
                resultSnippet = "C = ${NumberFormatter.format(molarityMolPerL, 5)} mol/L"
            )
        )

        val outputs = mutableListOf(
            CalculatorOutput("Concentration molaire (C)", NumberFormatter.format(molarityMolPerL, 5), "mol/L (M)", isPrimary = true),
            CalculatorOutput("Concentration en mmol/L", NumberFormatter.format(millimolar, 3), "mmol/L", isPrimary = true),
            CalculatorOutput("Quantité de matière (n)", NumberFormatter.format(moles, 5), "mol")
        )

        if (massGrams != null) {
            val massConcGPerL = massGrams / volumeLiters
            outputs.add(CalculatorOutput("Concentration massique (C_m)", NumberFormatter.format(massConcGPerL, 4), "g/L"))
        }

        return EngineOutcome.Success(
            CalculationResult(
                outputs = outputs,
                steps = steps,
                assumptions = listOf(
                    "Dissolution complète et homogène du soluté dans le volume final V de solution.",
                    "Température constante (le volume d'une solution liquide varie légèrement avec la température)."
                ),
                canonicalFormula = "C = n / V  |  n = m / M"
            )
        )
    }
}

// ============================================================================
// TOOL 25 — SOLUTION DILUTION (C1 * V1 = C2 * V2)
// ============================================================================
enum class DilutionUnknown(val displayName: String) {
    SOLVE_V1("Volume initial à prélever (V₁)"),
    SOLVE_C2("Concentration finale obtenue (C₂)"),
    SOLVE_V2("Volume final de solution (V₂)"),
    SOLVE_C1("Concentration mère initiale (C₁)")
}

object SolutionDilutionCalculator {

    fun calculate(
        unknown: DilutionUnknown,
        c1: Double,
        v1: Double,
        c2: Double,
        v2: Double,
        concentrationUnit: String = "mol/L",
        volumeUnit: String = "mL"
    ): EngineOutcome {
        val calcC1: Double
        val calcV1: Double
        val calcC2: Double
        val calcV2: Double
        val stepFormula: String
        val stepSubst: String

        when (unknown) {
            DilutionUnknown.SOLVE_V1 -> {
                if (c1 <= 0.0 || c2 <= 0.0 || v2 <= 0.0) {
                    return EngineOutcome.Failure("C₁, C₂ et V₂ doivent être strictement positifs.")
                }
                calcC1 = c1
                calcC2 = c2
                calcV2 = v2
                calcV1 = (c2 * v2) / c1
                stepFormula = "V₁ = (C₂ × V₂) / C₁"
                stepSubst = "V₁ = ($c2 × $v2) / $c1 = ${NumberFormatter.format(calcV1, 4)} $volumeUnit"
            }
            DilutionUnknown.SOLVE_C2 -> {
                if (c1 <= 0.0 || v1 <= 0.0 || v2 <= 0.0) {
                    return EngineOutcome.Failure("C₁, V₁ et V₂ doivent être strictement positifs.")
                }
                calcC1 = c1
                calcV1 = v1
                calcV2 = v2
                calcC2 = (c1 * v1) / v2
                stepFormula = "C₂ = (C₁ × V₁) / V₂"
                stepSubst = "C₂ = ($c1 × $v1) / $v2 = ${NumberFormatter.format(calcC2, 5)} $concentrationUnit"
            }
            DilutionUnknown.SOLVE_V2 -> {
                if (c1 <= 0.0 || v1 <= 0.0 || c2 <= 0.0) {
                    return EngineOutcome.Failure("C₁, V₁ et C₂ doivent être strictement positifs.")
                }
                calcC1 = c1
                calcV1 = v1
                calcC2 = c2
                calcV2 = (c1 * v1) / c2
                stepFormula = "V₂ = (C₁ × V₁) / C₂"
                stepSubst = "V₂ = ($c1 × $v1) / $c2 = ${NumberFormatter.format(calcV2, 4)} $volumeUnit"
            }
            DilutionUnknown.SOLVE_C1 -> {
                if (v1 <= 0.0 || c2 <= 0.0 || v2 <= 0.0) {
                    return EngineOutcome.Failure("V₁, C₂ et V₂ doivent être strictement positifs.")
                }
                calcV1 = v1
                calcC2 = c2
                calcV2 = v2
                calcC1 = (c2 * v2) / v1
                stepFormula = "C₁ = (C₂ × V₂) / V₁"
                stepSubst = "C₁ = ($c2 × $v2) / $v1 = ${NumberFormatter.format(calcC1, 5)} $concentrationUnit"
            }
        }

        val warnings = mutableListOf<String>()
        if (calcC2 > calcC1 || calcV1 > calcV2) {
            warnings.add("Attention : C₂ > C₁ (ou V₁ > V₂) correspond à une concentration par évaporation et non à une dilution par ajout de solvant.")
        }

        val solventToAdd = (calcV2 - calcV1).coerceAtLeast(0.0)
        val dilutionFactor = if (calcC2 > 0.0) calcC1 / calcC2 else 0.0

        val steps = listOf(
            CalculationStep(
                title = "1. Conservation de la quantité de matière de soluté",
                formula = "n₁ = n₂  ⇒  C₁ · V₁ = C₂ · V₂",
                substitution = stepFormula,
                resultSnippet = stepSubst
            ),
            CalculationStep(
                title = "2. Volume de solvant pur à ajouter (V_solvant)",
                formula = "V_solvant ≈ V₂ - V₁",
                substitution = "V_solvant = ${NumberFormatter.format(calcV2, 3)} - ${NumberFormatter.format(calcV1, 3)}",
                resultSnippet = "V_solvant = ${NumberFormatter.format(solventToAdd, 3)} $volumeUnit"
            )
        )

        val outputs = listOf(
            CalculatorOutput("Concentration initiale (C₁)", NumberFormatter.format(calcC1, 5), concentrationUnit, isPrimary = unknown == DilutionUnknown.SOLVE_C1),
            CalculatorOutput("Volume à prélever (V₁)", NumberFormatter.format(calcV1, 4), volumeUnit, isPrimary = unknown == DilutionUnknown.SOLVE_V1),
            CalculatorOutput("Concentration finale (C₂)", NumberFormatter.format(calcC2, 5), concentrationUnit, isPrimary = unknown == DilutionUnknown.SOLVE_C2),
            CalculatorOutput("Volume final (V₂)", NumberFormatter.format(calcV2, 4), volumeUnit, isPrimary = unknown == DilutionUnknown.SOLVE_V2),
            CalculatorOutput("Solvant à ajouter (V₂ - V₁)", NumberFormatter.format(solventToAdd, 4), volumeUnit),
            CalculatorOutput("Facteur de dilution (C₁ / C₂)", NumberFormatter.format(dilutionFactor, 2), "×")
        )

        return EngineOutcome.Success(
            CalculationResult(
                outputs = outputs,
                steps = steps,
                assumptions = listOf(
                    "Conservation stricte de la quantité de soluté lors de la dilution (aucune réaction chimique ni précipitation).",
                    "Additivité idéale des volumes (approximation valide pour les solutions diluées ; en pratique, compléter au trait de jauge d'une fiole jaugée V₂)."
                ),
                warnings = warnings,
                canonicalFormula = "C₁ · V₁ = C₂ · V₂"
            )
        )
    }
}
