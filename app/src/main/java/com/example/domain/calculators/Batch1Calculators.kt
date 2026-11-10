package com.example.domain.calculators

import com.example.domain.formatting.NumberFormatter
import com.example.domain.models.CalculationResult
import com.example.domain.models.CalculationStep
import com.example.domain.models.CalculatorOutput
import com.example.domain.models.EngineOutcome
import com.example.domain.units.EngineeringUnit
import com.example.domain.units.UnitEngine
import com.example.domain.units.UnitRegistry
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sqrt

// ============================================================================
// TOOL 11 — CIRCULAR SHAFT TORSION
// ============================================================================
object ShaftTorsionCalculator {

    fun calculate(
        torqueValue: Double,
        torqueUnit: EngineeringUnit,
        lengthValue: Double,
        lengthUnit: EngineeringUnit,
        outerDiameterValue: Double,
        innerDiameterValue: Double,
        diameterUnit: EngineeringUnit,
        shearModulusGpa: Double
    ): EngineOutcome {
        if (torqueValue < 0.0) {
            return EngineOutcome.Failure("Le couple appliqué T doit être positif ou nul.")
        }
        if (lengthValue <= 0.0) {
            return EngineOutcome.Failure("La longueur de l'arbre L doit être strictement positive.")
        }
        if (outerDiameterValue <= 0.0) {
            return EngineOutcome.Failure("Le diamètre extérieur D doit être strictement positif.")
        }
        if (innerDiameterValue < 0.0) {
            return EngineOutcome.Failure("Le diamètre intérieur d ne peut pas être négatif.")
        }
        if (innerDiameterValue >= outerDiameterValue) {
            return EngineOutcome.Failure("Le diamètre extérieur D doit être strictement supérieur au diamètre intérieur d (D > d ≥ 0).")
        }
        if (shearModulusGpa <= 0.0) {
            return EngineOutcome.Failure("Le module de Coulomb (cisaillement) G doit être strictement positif.")
        }

        val tNm = UnitEngine.convert(torqueValue, torqueUnit, UnitRegistry.NM)
        val lM = UnitEngine.convert(lengthValue, lengthUnit, UnitRegistry.M)
        val dOuterM = UnitEngine.convert(outerDiameterValue, diameterUnit, UnitRegistry.M)
        val dInnerM = UnitEngine.convert(innerDiameterValue, diameterUnit, UnitRegistry.M)
        val dOuterMm = UnitEngine.convert(outerDiameterValue, diameterUnit, UnitRegistry.MM)
        val dInnerMm = UnitEngine.convert(innerDiameterValue, diameterUnit, UnitRegistry.MM)
        val gPa = shearModulusGpa * 1e9

        // J = pi * (D^4 - d^4) / 32
        val jM4 = PI * (dOuterM.pow(4) - dInnerM.pow(4)) / 32.0
        val jMm4 = PI * (dOuterMm.pow(4) - dInnerMm.pow(4)) / 32.0
        val jCm4 = jMm4 / 1e4

        // tau_max = T * (D / 2) / J
        val tauMaxPa = (tNm * (dOuterM / 2.0)) / jM4
        val tauMaxMpa = tauMaxPa / 1e6

        // theta = T * L / (G * J)
        val thetaRad = (tNm * lM) / (gPa * jM4)
        val thetaDeg = Math.toDegrees(thetaRad)

        val steps = listOf(
            CalculationStep(
                title = "1. Moment quadratique polaire (J)",
                formula = "J = π · (D⁴ - d⁴) / 32",
                substitution = "J = π · (${NumberFormatter.format(dOuterMm)}⁴ - ${NumberFormatter.format(dInnerMm)}⁴) / 32",
                resultSnippet = "J = ${NumberFormatter.format(jMm4)} mm⁴ (${NumberFormatter.format(jCm4)} cm⁴)"
            ),
            CalculationStep(
                title = "2. Contrainte tangentielle maximale (τ_max)",
                formula = "τ_max = [T · (D / 2)] / J",
                substitution = "τ_max = [${NumberFormatter.format(tNm)} N·m × (${NumberFormatter.format(dOuterM / 2.0)} m)] / ${NumberFormatter.format(jM4)} m⁴",
                resultSnippet = "τ_max = ${NumberFormatter.format(tauMaxMpa)} MPa"
            ),
            CalculationStep(
                title = "3. Angle de torsion unitaire et total (θ)",
                formula = "θ = (T · L) / (G · J)",
                substitution = "θ = (${NumberFormatter.format(tNm)} N·m × ${NumberFormatter.format(lM)} m) / (${NumberFormatter.format(gPa)} Pa × ${NumberFormatter.format(jM4)} m⁴)",
                resultSnippet = "θ = ${NumberFormatter.format(thetaRad, 5)} rad (${NumberFormatter.format(thetaDeg, 3)}°)"
            )
        )

        val outputs = listOf(
            CalculatorOutput("Contrainte de cisaillement max (τ_max)", NumberFormatter.format(tauMaxMpa), "MPa", isPrimary = true),
            CalculatorOutput("Angle de torsion (θ en degrés)", NumberFormatter.format(thetaDeg, 4), "°", isPrimary = true),
            CalculatorOutput("Angle de torsion (θ en radians)", NumberFormatter.format(thetaRad, 5), "rad"),
            CalculatorOutput("Moment quadratique polaire (J)", NumberFormatter.format(jCm4), "cm⁴"),
            CalculatorOutput("Moment polaire en mm⁴", NumberFormatter.format(jMm4), "mm⁴")
        )

        return EngineOutcome.Success(
            CalculationResult(
                outputs = outputs,
                steps = steps,
                assumptions = listOf(
                    "Arbre circulaire droit à section constante (plein ou tubulaire concentrique).",
                    "Matériau homogène, isotrope et élastique linéaire (Loi de Hooke en cisaillement τ = G·γ).",
                    "Les sections droites restent planes et perpendiculaires à l'axe après déformation (Hypothèse de Coulomb)."
                ),
                warnings = listOf(
                    "Les concentrations de contraintes (rainures de clavettes, épaulements, gorges) ne sont pas prises en compte.",
                    "Ce calcul ne vérifie pas la fatigue en torsion alternée ni le flambement torsional des tubes minces."
                ),
                canonicalFormula = "J = π(D⁴ - d⁴)/32  |  τ_max = T(D/2)/J  |  θ = TL/(GJ)"
            )
        )
    }
}

// ============================================================================
// TOOL 12 — VOLTAGE DROP
// ============================================================================
enum class VoltageDropCircuitType(val displayName: String, val factorB: Double) {
    DC_TWO_WIRE("Continu DC (2 conducteurs)", 2.0),
    AC_SINGLE_PHASE("Monophasé AC (2 conducteurs)", 2.0),
    AC_THREE_PHASE("Triphasé équilibré AC (3 conducteurs)", sqrt(3.0))
}

object VoltageDropCalculator {

    fun calculate(
        circuitType: VoltageDropCircuitType,
        currentAmps: Double,
        lengthMeters: Double,
        areaMm2: Double,
        resistivityOhmMm2PerM: Double,
        nominalVoltageV: Double,
        powerFactor: Double = 1.0
    ): EngineOutcome {
        if (currentAmps <= 0.0) {
            return EngineOutcome.Failure("Le courant d'emploi I doit être strictement positif.")
        }
        if (lengthMeters <= 0.0) {
            return EngineOutcome.Failure("La longueur de câble aller simple L doit être strictement positive.")
        }
        if (areaMm2 <= 0.0) {
            return EngineOutcome.Failure("La section du conducteur S doit être strictement positive.")
        }
        if (resistivityOhmMm2PerM <= 0.0) {
            return EngineOutcome.Failure("La résistivité ρ doit être strictement positive.")
        }
        if (nominalVoltageV <= 0.0) {
            return EngineOutcome.Failure("La tension nominale Un est requise et doit être strictement positive.")
        }
        if (circuitType != VoltageDropCircuitType.DC_TWO_WIRE && (powerFactor <= 0.0 || powerFactor > 1.0)) {
            return EngineOutcome.Failure("Le facteur de puissance cos φ doit être compris dans ]0, 1].")
        }

        val effCosPhi = if (circuitType == VoltageDropCircuitType.DC_TWO_WIRE) 1.0 else powerFactor
        val b = circuitType.factorB
        val resistanceOneWay = (resistivityOhmMm2PerM * lengthMeters) / areaMm2
        val deltaU = b * resistanceOneWay * currentAmps * effCosPhi
        val deltaUPercent = (deltaU / nominalVoltageV) * 100.0
        val loadVoltage = (nominalVoltageV - deltaU).coerceAtLeast(0.0)

        val coeffSymbol = if (circuitType == VoltageDropCircuitType.AC_THREE_PHASE) "√3" else "2"
        val formulaStr = if (circuitType == VoltageDropCircuitType.DC_TWO_WIRE) {
            "ΔU = (2 · ρ · L · I) / S"
        } else {
            "ΔU = ($coeffSymbol · ρ · L · I · cos φ) / S"
        }

        val steps = listOf(
            CalculationStep(
                title = "1. Résistance linéique d'un conducteur (R)",
                formula = "R = (ρ · L) / S",
                substitution = "R = ($resistivityOhmMm2PerM Ω·mm²/m × $lengthMeters m) / $areaMm2 mm²",
                resultSnippet = "R = ${NumberFormatter.format(resistanceOneWay, 4)} Ω"
            ),
            CalculationStep(
                title = "2. Chute de tension en volts (ΔU)",
                formula = formulaStr,
                substitution = "ΔU = ${NumberFormatter.format(b, 4)} × ${NumberFormatter.format(resistanceOneWay, 4)} Ω × $currentAmps A × $effCosPhi",
                resultSnippet = "ΔU = ${NumberFormatter.format(deltaU, 3)} V"
            ),
            CalculationStep(
                title = "3. Chute de tension relative (ΔU%)",
                formula = "ΔU% = (ΔU / U_n) × 100",
                substitution = "ΔU% = (${NumberFormatter.format(deltaU, 3)} V / $nominalVoltageV V) × 100",
                resultSnippet = "ΔU% = ${NumberFormatter.format(deltaUPercent, 2)} %"
            )
        )

        val warnings = mutableListOf(
            "Modèle résistif simplifié : la réactance linéique inductive (X·sin φ), l'effet de peau, la température réelle d'âme et les harmoniques ne sont pas modélisés.",
            "Ce résultat est un pré-dimensionnement et ne remplace pas une vérification normative complète (NF C 15-100 / CEI 60364 : courant admissible Iz, pose, courts-circuits)."
        )
        if (deltaUPercent > 5.0) {
            warnings.add("Chute de tension élevée (> 5%) : Risque de sous-tension critique pour les récepteurs.")
        } else if (deltaUPercent > 3.0) {
            warnings.add("Chute de tension > 3% : Dépasse la limite usuelle recommandée pour les circuits d'éclairage.")
        }

        val outputs = listOf(
            CalculatorOutput("Chute de tension (ΔU)", NumberFormatter.format(deltaU, 3), "V", isPrimary = true),
            CalculatorOutput("Chute de tension relative", NumberFormatter.format(deltaUPercent, 2), "%", isPrimary = true),
            CalculatorOutput("Tension estimée au récepteur", NumberFormatter.format(loadVoltage, 2), "V"),
            CalculatorOutput("Résistance d'un conducteur", NumberFormatter.format(resistanceOneWay, 4), "Ω")
        )

        return EngineOutcome.Success(
            CalculationResult(
                outputs = outputs,
                steps = steps,
                assumptions = listOf(
                    "Modèle purement résistif (réactance inductive négligée, valide pour S ≤ 50 mm²).",
                    "Résistivité supposée constante à la température de référence choisie.",
                    "Charge équilibrée en mode triphasé."
                ),
                warnings = warnings,
                canonicalFormula = "$formulaStr  |  ΔU% = 100 · ΔU / U_n"
            )
        )
    }
}

// ============================================================================
// TOOL 13 — SERIES AND PARALLEL RESISTANCE
// ============================================================================
enum class ResistanceConnectionMode(val displayName: String) {
    SERIES("Association en Série"),
    PARALLEL("Association en Parallèle")
}

object EquivalentResistanceCalculator {

    fun parseResistances(rawInput: String): List<Double>? {
        val tokens = rawInput.trim().split(Regex("[,;\\s]+")).filter { it.isNotBlank() }
        if (tokens.isEmpty()) return emptyList()
        val parsed = mutableListOf<Double>()
        for (token in tokens) {
            val v = token.replace(',', '.').toDoubleOrNull() ?: return null
            parsed.add(v)
        }
        return parsed
    }

    fun calculate(mode: ResistanceConnectionMode, resistances: List<Double>): EngineOutcome {
        if (resistances.isEmpty()) {
            return EngineOutcome.Failure("Veuillez saisir au moins une valeur de résistance.")
        }
        if (resistances.any { it < 0.0 }) {
            return EngineOutcome.Failure("Les valeurs de résistance ne peuvent pas être négatives (R_i ≥ 0).")
        }

        val n = resistances.size
        val steps = mutableListOf<CalculationStep>()
        val warnings = mutableListOf<String>()

        val req: Double
        val canonical: String

        when (mode) {
            ResistanceConnectionMode.SERIES -> {
                req = resistances.sum()
                canonical = "R_eq = ∑ R_i"
                steps.add(
                    CalculationStep(
                        title = "Somme des résistances en série",
                        formula = "R_eq = R₁ + R₂ + ... + R_n",
                        substitution = resistances.joinToString(" + ") { "${NumberFormatter.format(it)} Ω" },
                        resultSnippet = "R_eq = ${NumberFormatter.format(req)} Ω"
                    )
                )
            }
            ResistanceConnectionMode.PARALLEL -> {
                canonical = "1 / R_eq = ∑ (1 / R_i)"
                val hasZeroBranch = resistances.any { it == 0.0 }
                if (hasZeroBranch) {
                    req = 0.0
                    warnings.add("Branche à 0 Ω détectée (court-circuit franc) : la résistance équivalente parallèle est nulle (0 Ω).")
                    steps.add(
                        CalculationStep(
                            title = "Détection d'une branche en court-circuit (R_i = 0 Ω)",
                            formula = "lim (R_i → 0) R_eq = 0 Ω",
                            substitution = "Au moins une résistance vaut 0 Ω dans le groupement parallèle.",
                            resultSnippet = "R_eq = 0 Ω"
                        )
                    )
                } else {
                    val conductanceSum = resistances.sumOf { 1.0 / it }
                    req = 1.0 / conductanceSum
                    steps.add(
                        CalculationStep(
                            title = "1. Somme des conductances (G_eq)",
                            formula = "G_eq = ∑ (1 / R_i)",
                            substitution = resistances.joinToString(" + ") { "1/${NumberFormatter.format(it)}" },
                            resultSnippet = "G_eq = ${NumberFormatter.format(conductanceSum, 6)} S"
                        )
                    )
                    steps.add(
                        CalculationStep(
                            title = "2. Inversion pour obtenir R_eq",
                            formula = "R_eq = 1 / G_eq",
                            substitution = "R_eq = 1 / ${NumberFormatter.format(conductanceSum, 6)}",
                            resultSnippet = "R_eq = ${NumberFormatter.format(req)} Ω"
                        )
                    )
                }
            }
        }

        val conductance = if (req > 0.0) NumberFormatter.format(1.0 / req, 6) else "∞ (Court-circuit)"
        val outputs = listOf(
            CalculatorOutput("Résistance équivalente (R_eq)", NumberFormatter.format(req), "Ω", isPrimary = true),
            CalculatorOutput("En kilohms", NumberFormatter.format(req / 1000.0, 5), "kΩ"),
            CalculatorOutput("Conductance totale (G_eq)", conductance, "S"),
            CalculatorOutput("Nombre de dipôles", n.toString(), "résistances")
        )

        return EngineOutcome.Success(
            CalculationResult(
                outputs = outputs,
                steps = steps,
                assumptions = listOf(
                    "Dipôles passifs purement résistifs linéaires et indépendants de la fréquence.",
                    "Résistance des fils de liaison supposée nulle."
                ),
                warnings = warnings,
                canonicalFormula = canonical
            )
        )
    }
}

// ============================================================================
// TOOL 14 — VOLTAGE DIVIDER
// ============================================================================
object VoltageDividerCalculator {

    fun calculate(
        vin: Double,
        r1: Double,
        r2: Double,
        resistanceUnitSymbol: String = "Ω"
    ): EngineOutcome {
        if (r1 < 0.0 || r2 < 0.0) {
            return EngineOutcome.Failure("Les résistances R1 et R2 doivent être positives ou nulles.")
        }
        val denom = r1 + r2
        if (denom <= 0.0) {
            return EngineOutcome.Failure("La somme (R1 + R2) doit être strictement positive (court-circuit interdit).")
        }

        val ratio = r2 / denom
        val vout = vin * ratio

        val unitFactor = when (resistanceUnitSymbol) {
            "kΩ" -> 1000.0
            "MΩ" -> 1e6
            else -> 1.0
        }
        val totalROhms = denom * unitFactor
        val currentMa = (kotlin.math.abs(vin) / totalROhms) * 1000.0

        val steps = listOf(
            CalculationStep(
                title = "1. Rapport de division résistif (k)",
                formula = "k = R₂ / (R₁ + R₂)",
                substitution = "k = $r2 / ($r1 + $r2) = $r2 / ${NumberFormatter.format(denom)}",
                resultSnippet = "k = ${NumberFormatter.format(ratio, 5)}"
            ),
            CalculationStep(
                title = "2. Tension de sortie à vide (V_out)",
                formula = "V_out = V_in × k",
                substitution = "V_out = $vin V × ${NumberFormatter.format(ratio, 5)}",
                resultSnippet = "V_out = ${NumberFormatter.format(vout, 4)} V"
            )
        )

        val outputs = listOf(
            CalculatorOutput("Tension de sortie à vide (V_out)", NumberFormatter.format(vout, 4), "V", isPrimary = true),
            CalculatorOutput("Rapport de transfert (V_out / V_in)", NumberFormatter.format(ratio, 4), "", isPrimary = true),
            CalculatorOutput("Courant de pont à vide", NumberFormatter.format(currentMa, 4), "mA"),
            CalculatorOutput("Impédance de sortie Thévenin", NumberFormatter.format((r1 * r2) / denom, 4), resistanceUnitSymbol)
        )

        return EngineOutcome.Success(
            CalculationResult(
                outputs = outputs,
                steps = steps,
                assumptions = listOf(
                    "Pont diviseur de tension non chargé (courant de sortie I_out = 0 A, impédance de charge infinie).",
                    "Régime continu ou basse fréquence sur résistances idéales."
                ),
                warnings = listOf(
                    "Si une charge R_L est connectée en sortie, elle se place en parallèle avec R₂ et abaisse la tension réelle V_out."
                ),
                canonicalFormula = "V_out = V_in × R₂ / (R₁ + R₂)"
            )
        )
    }
}

// ============================================================================
// TOOL 15 — RC TIME CONSTANT
// ============================================================================
object RcTimeConstantCalculator {

    fun calculate(
        resistanceOhms: Double,
        capacitanceFarads: Double,
        supplyVoltageV: Double = 5.0,
        evaluationTimeSeconds: Double? = null
    ): EngineOutcome {
        if (resistanceOhms <= 0.0) {
            return EngineOutcome.Failure("La résistance R doit être strictement positive.")
        }
        if (capacitanceFarads <= 0.0) {
            return EngineOutcome.Failure("La capacité C doit être strictement positive.")
        }
        if (evaluationTimeSeconds != null && evaluationTimeSeconds < 0.0) {
            return EngineOutcome.Failure("L'instant d'évaluation t doit être positif ou nul.")
        }

        val tauSeconds = resistanceOhms * capacitanceFarads
        val tauMs = tauSeconds * 1000.0
        val t632Ms = tauMs * 1.0
        val t950Ms = tauMs * 3.0
        val t993Ms = tauMs * 5.0
        val cutoffHz = 1.0 / (2.0 * PI * tauSeconds)

        val tEval = evaluationTimeSeconds ?: tauSeconds
        val vAtT = supplyVoltageV * (1.0 - exp(-tEval / tauSeconds))

        val steps = listOf(
            CalculationStep(
                title = "1. Constante de temps caractéristique (τ)",
                formula = "τ = R × C",
                substitution = "τ = ${NumberFormatter.format(resistanceOhms)} Ω × ${NumberFormatter.format(capacitanceFarads)} F",
                resultSnippet = "τ = ${NumberFormatter.format(tauSeconds, 6)} s (${NumberFormatter.format(tauMs, 4)} ms)"
            ),
            CalculationStep(
                title = "2. Temps de charge caractéristiques",
                formula = "t_63.2% = 1τ  |  t_95.0% = 3τ  |  t_99.3% = 5τ",
                substitution = "1τ = ${NumberFormatter.format(t632Ms, 3)} ms, 3τ = ${NumberFormatter.format(t950Ms, 3)} ms, 5τ = ${NumberFormatter.format(t993Ms, 3)} ms",
                resultSnippet = "Régime permanent atteint à ~5τ"
            ),
            CalculationStep(
                title = "3. Tension instantanée de charge V(t)",
                formula = "V(t) = V_s × (1 - e^(-t / τ))",
                substitution = "V(${NumberFormatter.format(tEval)} s) = $supplyVoltageV V × (1 - e^(-${NumberFormatter.format(tEval)} / ${NumberFormatter.format(tauSeconds)}))",
                resultSnippet = "V(t) = ${NumberFormatter.format(vAtT, 4)} V"
            )
        )

        val outputs = listOf(
            CalculatorOutput("Constante de temps (τ = RC)", NumberFormatter.format(tauMs, 4), "ms", isPrimary = true),
            CalculatorOutput("Constante de temps en secondes", NumberFormatter.format(tauSeconds, 6), "s"),
            CalculatorOutput("Temps à 63.2% de charge (1τ)", NumberFormatter.format(t632Ms, 4), "ms", isPrimary = true),
            CalculatorOutput("Temps à 95.0% de charge (3τ)", NumberFormatter.format(t950Ms, 4), "ms"),
            CalculatorOutput("Temps à 99.3% de charge (5τ)", NumberFormatter.format(t993Ms, 4), "ms", isPrimary = true),
            CalculatorOutput("Tension V(t) à t = ${NumberFormatter.format(tEval)} s", NumberFormatter.format(vAtT, 4), "V"),
            CalculatorOutput("Fréquence de coupure (-3 dB)", NumberFormatter.format(cutoffHz, 3), "Hz")
        )

        return EngineOutcome.Success(
            CalculationResult(
                outputs = outputs,
                steps = steps,
                assumptions = listOf(
                    "Condensateur initialement totalement déchargé : V(0) = 0 V.",
                    "Échelon de tension parfait V_s appliqué à t = 0 s.",
                    "Composants R et C idéaux sans courant de fuite ni résistance série équivalente (ESR = 0)."
                ),
                canonicalFormula = "τ = R · C  |  V(t) = V_s · (1 - e^(-t/RC))"
            )
        )
    }
}
