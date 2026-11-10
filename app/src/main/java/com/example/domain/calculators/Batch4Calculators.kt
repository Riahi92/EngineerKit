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
// TOOL 26 — NUMBER BASE CONVERTER
// ============================================================================
enum class NumberBase(val radix: Int, val displayName: String, val prefix: String) {
    BINARY(2, "Binaire (Base 2)", "0b"),
    OCTAL(8, "Octal (Base 8)", "0o"),
    DECIMAL(10, "Décimal (Base 10)", ""),
    HEXADECIMAL(16, "Hexadécimal (Base 16)", "0x")
}

object NumberBaseConverter {

    fun convert(rawInput: String, sourceBase: NumberBase): EngineOutcome {
        val cleaned = rawInput.trim()
            .removePrefix("0x").removePrefix("0X")
            .removePrefix("0b").removePrefix("0B")
            .removePrefix("0o").removePrefix("0O")
            .replace(" ", "")
            .replace("_", "")

        if (cleaned.isEmpty()) {
            return EngineOutcome.Failure("Veuillez saisir une valeur entière valide.")
        }

        val isNegative = cleaned.startsWith("-")
        val digitsOnly = if (cleaned.startsWith("-") || cleaned.startsWith("+")) cleaned.substring(1) else cleaned
        if (digitsOnly.isEmpty()) {
            return EngineOutcome.Failure("Aucun chiffre valide détecté.")
        }

        val validRegex = when (sourceBase) {
            NumberBase.BINARY -> Regex("^[01]+$")
            NumberBase.OCTAL -> Regex("^[0-7]+$")
            NumberBase.DECIMAL -> Regex("^[0-9]+$")
            NumberBase.HEXADECIMAL -> Regex("^[0-9a-fA-F]+$")
        }

        if (!validRegex.matches(digitsOnly)) {
            return EngineOutcome.Failure(
                "Caractères invalides pour la base ${sourceBase.radix} (${sourceBase.displayName})."
            )
        }

        val valueLong = try {
            val unsigned = digitsOnly.toLong(sourceBase.radix)
            if (isNegative) -unsigned else unsigned
        } catch (_: NumberFormatException) {
            return EngineOutcome.Failure("Dépassement de capacité entière 64 bits (Long.MAX_VALUE).")
        }

        val binStr = formatInRadix(valueLong, 2)
        val octStr = formatInRadix(valueLong, 8)
        val decStr = valueLong.toString(10)
        val hexStr = formatInRadix(valueLong, 16).uppercase()

        val steps = listOf(
            CalculationStep(
                title = "1. Décodage en entier signé 64 bits (Base 10)",
                formula = "N₁₀ = ∑ d_i · b^i",
                substitution = "Entrée ($cleaned)_${sourceBase.radix} → $decStr en base 10",
                resultSnippet = "N₁₀ = $decStr"
            ),
            CalculationStep(
                title = "2. Divisions euclidiennes successives vers les bases 2, 8 et 16",
                formula = "Conversion exacte sans flottant (64-bit integer safe)",
                substitution = "Bin: $binStr | Oct: $octStr | Hex: $hexStr",
                resultSnippet = "Précision entière garantie"
            )
        )

        val outputs = listOf(
            CalculatorOutput("Décimal (Base 10)", decStr, "", isPrimary = true),
            CalculatorOutput("Hexadécimal (Base 16)", "0x$hexStr", "", isPrimary = true),
            CalculatorOutput("Binaire (Base 2)", binStr, "", isPrimary = true),
            CalculatorOutput("Octal (Base 8)", "0o$octStr", "")
        )

        return EngineOutcome.Success(
            CalculationResult(
                outputs = outputs,
                steps = steps,
                assumptions = listOf(
                    "Arithmétique entière exacte 64 bits signée (Kotlin Long, aucune perte de précision liée aux nombres flottants IEEE-754).",
                    "Signe moins (-) conservé de manière explicite pour la lisibilité entre bases."
                ),
                canonicalFormula = "N = ∑ (d_k × Base^k)"
            )
        )
    }

    private fun formatInRadix(value: Long, radix: Int): String {
        return if (value < 0) {
            "-" + (-value).toString(radix)
        } else {
            value.toString(radix)
        }
    }
}

// ============================================================================
// TOOL 27 — BOOLEAN LOGIC
// ============================================================================
enum class LogicGate(val displayName: String, val algebraicExpr: String, val isUnary: Boolean = false) {
    AND("Porte ET (AND)", "Y = A · B"),
    OR("Porte OU (OR)", "Y = A + B"),
    NOT("Porte NON (NOT - sur A)", "Y = Ā", isUnary = true),
    XOR("Porte OU Exclusif (XOR)", "Y = A ⊕ B"),
    NAND("Porte NON-ET (NAND)", "Y = ¬(A · B)"),
    NOR("Porte NON-OU (NOR)", "Y = ¬(A + B)")
}

data class TruthTableRow(
    val a: Boolean,
    val b: Boolean?,
    val output: Boolean,
    val isCurrentSelection: Boolean
)

object BooleanLogicCalculator {

    fun evaluateGate(gate: LogicGate, a: Boolean, b: Boolean): Boolean {
        return when (gate) {
            LogicGate.AND -> a && b
            LogicGate.OR -> a || b
            LogicGate.NOT -> !a
            LogicGate.XOR -> a xor b
            LogicGate.NAND -> !(a && b)
            LogicGate.NOR -> !(a || b)
        }
    }

    fun generateTruthTable(gate: LogicGate, selectedA: Boolean, selectedB: Boolean): List<TruthTableRow> {
        return if (gate.isUnary) {
            listOf(false, true).map { aVal ->
                TruthTableRow(
                    a = aVal,
                    b = null,
                    output = evaluateGate(gate, aVal, false),
                    isCurrentSelection = (aVal == selectedA)
                )
            }
        } else {
            val pairs = listOf(
                false to false,
                false to true,
                true to false,
                true to true
            )
            pairs.map { (aVal, bVal) ->
                TruthTableRow(
                    a = aVal,
                    b = bVal,
                    output = evaluateGate(gate, aVal, bVal),
                    isCurrentSelection = (aVal == selectedA && bVal == selectedB)
                )
            }
        }
    }

    fun calculate(gate: LogicGate, a: Boolean, b: Boolean): EngineOutcome {
        val out = evaluateGate(gate, a, b)
        val aBit = if (a) 1 else 0
        val bBit = if (b) 1 else 0
        val outBit = if (out) 1 else 0

        val steps = listOf(
            CalculationStep(
                title = "Évaluation algébrique de Boole",
                formula = gate.algebraicExpr,
                substitution = if (gate.isUnary) "A = $aBit ($a)" else "A = $aBit ($a), B = $bBit ($b)",
                resultSnippet = "Sortie logique Y = $outBit (${if (out) "VRAI / HIGH" else "FAUX / LOW"})"
            )
        )

        val outputs = listOf(
            CalculatorOutput("État logique de sortie (Y)", "$outBit (${if (out) "TRUE / HIGH" else "FALSE / LOW"})", "", isPrimary = true),
            CalculatorOutput("Expression booléenne", gate.algebraicExpr, "", isPrimary = true),
            CalculatorOutput("Entrée A", "$aBit ($a)", ""),
            CalculatorOutput("Entrée B", if (gate.isUnary) "Ignorée (Opérateur unaire)" else "$bBit ($b)", "")
        )

        return EngineOutcome.Success(
            CalculationResult(
                outputs = outputs,
                steps = steps,
                assumptions = listOf(
                    "Logique binaire positive (1 = Niveau Haut / Vrai, 0 = Niveau Bas / Faux).",
                    "Comportement combinatoire instantané sans temps de propagation matériel."
                ),
                canonicalFormula = gate.algebraicExpr
            )
        )
    }
}

// ============================================================================
// TOOL 28 — ANALOG SIGNAL SCALING
// ============================================================================
object AnalogSignalScalingCalculator {

    fun calculate(
        inputSignalX: Double,
        inputMin: Double,
        inputMax: Double,
        outputMin: Double,
        outputMax: Double,
        inputUnit: String = "V",
        outputUnit: String = "unité"
    ): EngineOutcome {
        val deltaX = inputMax - inputMin
        if (kotlin.math.abs(deltaX) < 1e-12) {
            return EngineOutcome.Failure("La plage d'entrée est nulle (X_max = X_min). Division par zéro impossible.")
        }

        val deltaY = outputMax - outputMin
        val normalizedRatio = (inputSignalX - inputMin) / deltaX
        val scaledY = outputMin + normalizedRatio * deltaY
        val percentOfSpan = normalizedRatio * 100.0
        val slopeGain = deltaY / deltaX
        val offset = outputMin - slopeGain * inputMin

        val warnings = mutableListOf<String>()
        if (normalizedRatio < 0.0 || normalizedRatio > 1.0) {
            warnings.add(
                "Signal d'entrée hors de l'étendue nominale [$inputMin .. $inputMax] (${NumberFormatter.format(percentOfSpan, 1)}%). La valeur est extrapolée linéairement sans écrêtage."
            )
        }
        if (outputMax < outputMin || inputMax < inputMin) {
            warnings.add("Échelle inversée détectée (pente négative ou bornes inversées) : prise en compte algébrique active.")
        }

        val steps = listOf(
            CalculationStep(
                title = "1. Position relative normalisée sur l'échelle d'entrée",
                formula = "r = (X - X_min) / (X_max - X_min)",
                substitution = "r = ($inputSignalX - $inputMin) / ($inputMax - $inputMin)",
                resultSnippet = "r = ${NumberFormatter.format(normalizedRatio, 5)} (${NumberFormatter.format(percentOfSpan, 2)}%)"
            ),
            CalculationStep(
                title = "2. Interpolation linéaire vers la grandeur physique (Y)",
                formula = "Y = Y_min + r × (Y_max - Y_min)",
                substitution = "Y = $outputMin + ${NumberFormatter.format(normalizedRatio, 5)} × ($outputMax - $outputMin)",
                resultSnippet = "Y = ${NumberFormatter.format(scaledY, 4)} $outputUnit"
            )
        )

        val outputs = listOf(
            CalculatorOutput("Valeur mise à l'échelle (Y)", NumberFormatter.format(scaledY, 4), outputUnit, isPrimary = true),
            CalculatorOutput("Pourcentage d'étendue", NumberFormatter.format(percentOfSpan, 2), "%", isPrimary = true),
            CalculatorOutput("Gain linéaire (pente a)", NumberFormatter.format(slopeGain, 5), "$outputUnit/$inputUnit"),
            CalculatorOutput("Décalage à l'origine (b)", NumberFormatter.format(offset, 4), outputUnit)
        )

        return EngineOutcome.Success(
            CalculationResult(
                outputs = outputs,
                steps = steps,
                assumptions = listOf(
                    "Loi de transfert strictement linéaire entre le capteur/convertisseur et la grandeur ingénieur.",
                    "Supporte les échelles directes et inversées (ex: vanne Normalement Ouverte 20–4 mA)."
                ),
                warnings = warnings,
                canonicalFormula = "Y = Y_min + (X - X_min) · (Y_max - Y_min) / (X_max - X_min)"
            )
        )
    }
}

// ============================================================================
// TOOL 29 — 4–20 mA SIGNAL CONVERTER
// ============================================================================
object CurrentLoop4To20mACalculator {

    fun calculate(
        currentMa: Double,
        engineeringMin: Double,
        engineeringMax: Double,
        engineeringUnit: String = "bar"
    ): EngineOutcome {
        val spanPercentage = ((currentMa - 4.0) / 16.0) * 100.0
        val engDelta = engineeringMax - engineeringMin
        val engValue = engineeringMin + ((currentMa - 4.0) / 16.0) * engDelta

        val warnings = mutableListOf<String>()
        when {
            currentMa < 0.0 -> {
                warnings.add("Courant négatif ($currentMa mA) : Polarité inversée ou erreur de mesure en boucle 4–20 mA.")
            }
            currentMa < 3.8 -> {
                warnings.add("Signal inférieur à la plage nominale 4–20 mA ($currentMa mA < 4 mA) : Susceptible d'indiquer une rupture de boucle (fil coupé) ou un défaut capteur selon la configuration API/NAMUR NE43, sans présumer d'un code défaut propriétaire.")
            }
            currentMa < 4.0 -> {
                warnings.add("Signal légèrement sous le zéro vivant 4 mA ($currentMa mA) : Sous-échelle (under-range).")
            }
            currentMa > 20.5 -> {
                warnings.add("Signal supérieur à la plage nominale 4–20 mA ($currentMa mA > 20 mA) : Dépassement d'échelle (over-range) ou saturation du transmetteur.")
            }
            currentMa > 20.0 -> {
                warnings.add("Signal légèrement au-dessus de 20 mA ($currentMa mA) : Marge haute de mesure.")
            }
        }

        val steps = listOf(
            CalculationStep(
                title = "1. Fraction de la plage utile de courant (16 mA d'étendue)",
                formula = "Pourcentage = [(I - 4) / 16] × 100",
                substitution = "Pourcentage = [($currentMa mA - 4 mA) / 16 mA] × 100",
                resultSnippet = "Signal = ${NumberFormatter.format(spanPercentage, 2)} %"
            ),
            CalculationStep(
                title = "2. Grandeur physique mesurée (Y)",
                formula = "Y = Y_min + [(I - 4) / 16] × (Y_max - Y_min)",
                substitution = "Y = $engineeringMin + [(${NumberFormatter.format(currentMa - 4.0, 3)}) / 16] × ($engineeringMax - $engineeringMin)",
                resultSnippet = "Y = ${NumberFormatter.format(engValue, 4)} $engineeringUnit"
            )
        )

        val statusLabel = if (currentMa in 4.0..20.0) "Nominal (4–20 mA)" else "Hors plage nominale"

        val outputs = listOf(
            CalculatorOutput("Mesure physique (Y)", NumberFormatter.format(engValue, 4), engineeringUnit, isPrimary = true),
            CalculatorOutput("Pourcentage d'échelle", NumberFormatter.format(spanPercentage, 2), "%", isPrimary = true),
            CalculatorOutput("Statut du signal", statusLabel, ""),
            CalculatorOutput("Sensibilité de boucle", NumberFormatter.format(engDelta / 16.0, 4), "$engineeringUnit/mA")
        )

        return EngineOutcome.Success(
            CalculationResult(
                outputs = outputs,
                steps = steps,
                assumptions = listOf(
                    "Boucle de courant analogique industrielle standard 4–20 mA à 'zéro vivant' (4 mA = 0%, 20 mA = 100%).",
                    "Échelle linéaire entre 4 mA et 20 mA (étendue utile ΔI = 16 mA)."
                ),
                warnings = warnings,
                canonicalFormula = "Y = Y_min + [(I - 4) / 16] × (Y_max - Y_min)"
            )
        )
    }
}

// ============================================================================
// TOOL 30 — ENERGY AND CO2e
// ============================================================================
object EnergyCo2Calculator {

    fun calculate(
        powerValue: Double,
        powerUnit: EngineeringUnit,
        operatingTimeValue: Double,
        timeUnit: EngineeringUnit,
        emissionFactorKgCo2PerKwh: Double
    ): EngineOutcome {
        if (powerValue < 0.0) {
            return EngineOutcome.Failure("La puissance appelée P doit être positive ou nulle.")
        }
        if (operatingTimeValue < 0.0) {
            return EngineOutcome.Failure("La durée de fonctionnement t doit être positive ou nulle.")
        }
        if (emissionFactorKgCo2PerKwh < 0.0) {
            return EngineOutcome.Failure("Le facteur d'émission (kg CO₂e/kWh) doit être positif ou nul.")
        }

        val powerKw = UnitEngine.convert(powerValue, powerUnit, UnitRegistry.KW)
        val timeHours = UnitEngine.convert(operatingTimeValue, timeUnit, UnitRegistry.HOUR)

        val energyKwh = powerKw * timeHours
        val energyMwh = energyKwh / 1000.0
        val energyMj = energyKwh * 3.6

        val emissionsKgCo2e = energyKwh * emissionFactorKgCo2PerKwh
        val emissionsTonnesCo2e = emissionsKgCo2e / 1000.0

        val steps = listOf(
            CalculationStep(
                title = "1. Énergie électrique consommée (E)",
                formula = "E [kWh] = P [kW] × t [h]",
                substitution = "E = ${NumberFormatter.format(powerKw, 4)} kW × ${NumberFormatter.format(timeHours, 3)} h",
                resultSnippet = "E = ${NumberFormatter.format(energyKwh, 3)} kWh (${NumberFormatter.format(energyMj, 2)} MJ)"
            ),
            CalculationStep(
                title = "2. Émissions estimées de gaz à effet de serre (CO₂e)",
                formula = "Émissions [kg CO₂e] = E [kWh] × Facteur_émission [kg CO₂e/kWh]",
                substitution = "Émissions = ${NumberFormatter.format(energyKwh, 3)} kWh × $emissionFactorKgCo2PerKwh kg CO₂e/kWh",
                resultSnippet = "Émissions = ${NumberFormatter.format(emissionsKgCo2e, 3)} kg CO₂e"
            )
        )

        val outputs = listOf(
            CalculatorOutput("Consommation d'énergie (E)", NumberFormatter.format(energyKwh, 3), "kWh", isPrimary = true),
            CalculatorOutput("Émissions estimées (CO₂e)", NumberFormatter.format(emissionsKgCo2e, 3), "kg CO₂e", isPrimary = true),
            CalculatorOutput("Émissions en tonnes CO₂e", NumberFormatter.format(emissionsTonnesCo2e, 5), "t CO₂e"),
            CalculatorOutput("Énergie en Mégajoules", NumberFormatter.format(energyMj, 2), "MJ"),
            CalculatorOutput("Énergie en MWh", NumberFormatter.format(energyMwh, 5), "MWh")
        )

        return EngineOutcome.Success(
            CalculationResult(
                outputs = outputs,
                steps = steps,
                assumptions = listOf(
                    "Puissance active moyenne constante sur la durée d'exploitation.",
                    "Les émissions sont des estimations directes basées exclusivement sur le facteur d'émission renseigné par l'utilisateur."
                ),
                warnings = listOf(
                    "Le facteur d'émission dépend fortement du mix énergétique local, de la période horaire et du périmètre (Scope 2 location-based vs market-based). Vérifiez toujours la source officielle applicable à votre projet."
                ),
                canonicalFormula = "E = P · t  |  CO₂e = E · FE"
            )
        )
    }
}
