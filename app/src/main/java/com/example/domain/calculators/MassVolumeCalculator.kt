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
import kotlin.math.pow

enum class SolidGeometry(val displayName: String) {
    RECTANGULAR_PRISM("Prisme rectangulaire (Bloc)"),
    SOLID_CYLINDER("Cylindre plein"),
    HOLLOW_CYLINDER("Tube cylindrique creux"),
    SPHERE("Sphère pleine"),
    PLATE("Tôle / Plaque plane")
}

data class MaterialPreset(val name: String, val densityKgM3: Double)

object MaterialPresets {
    val list = listOf(
        MaterialPreset("Acier de construction (S235/S355)", 7850.0),
        MaterialPreset("Acier inoxydable (304/316)", 7900.0),
        MaterialPreset("Aluminium (6060/7075)", 2700.0),
        MaterialPreset("Cuivre pur", 8960.0),
        MaterialPreset("Laiton", 8500.0),
        MaterialPreset("Titane", 4500.0),
        MaterialPreset("Fonte ductile", 7200.0),
        MaterialPreset("Béton armé standard", 2400.0),
        MaterialPreset("Eau pure (20°C)", 1000.0),
        MaterialPreset("Bois (Chêne sec)", 750.0),
        MaterialPreset("Plastique (POM / Acétal)", 1410.0)
    )
}

object MassVolumeCalculator {

    fun calculatePrism(
        length: Double,
        width: Double,
        height: Double,
        dimUnit: EngineeringUnit,
        densityKgM3: Double
    ): EngineOutcome {
        if (length <= 0 || width <= 0 || height <= 0) {
            return EngineOutcome.Failure("Les dimensions du prisme doivent être strictement positives.")
        }
        if (densityKgM3 <= 0) {
            return EngineOutcome.Failure("La masse volumique doit être strictement positive.")
        }

        val lM = UnitEngine.convert(length, dimUnit, UnitRegistry.M)
        val wM = UnitEngine.convert(width, dimUnit, UnitRegistry.M)
        val hM = UnitEngine.convert(height, dimUnit, UnitRegistry.M)

        val volumeM3 = lM * wM * hM
        val massKg = volumeM3 * densityKgM3

        return buildResult(
            formula = "V = L · l · h  |  m = ρ · V",
            stepFormula = "$lM m × $wM m × $hM m",
            volumeM3 = volumeM3,
            massKg = massKg,
            densityKgM3 = densityKgM3
        )
    }

    fun calculateSolidCylinder(
        diameter: Double,
        height: Double,
        dimUnit: EngineeringUnit,
        densityKgM3: Double
    ): EngineOutcome {
        if (diameter <= 0 || height <= 0) {
            return EngineOutcome.Failure("Le diamètre et la hauteur doivent être strictement positifs.")
        }
        if (densityKgM3 <= 0) {
            return EngineOutcome.Failure("La masse volumique doit être strictement positive.")
        }

        val dM = UnitEngine.convert(diameter, dimUnit, UnitRegistry.M)
        val hM = UnitEngine.convert(height, dimUnit, UnitRegistry.M)
        val rM = dM / 2.0

        val volumeM3 = PI * rM.pow(2) * hM
        val massKg = volumeM3 * densityKgM3

        return buildResult(
            formula = "V = π · (D/2)² · h  |  m = ρ · V",
            stepFormula = "π × ($rM m)² × $hM m",
            volumeM3 = volumeM3,
            massKg = massKg,
            densityKgM3 = densityKgM3
        )
    }

    fun calculateHollowCylinder(
        outerDiameter: Double,
        innerDiameter: Double,
        height: Double,
        dimUnit: EngineeringUnit,
        densityKgM3: Double
    ): EngineOutcome {
        if (outerDiameter <= 0 || innerDiameter <= 0 || height <= 0) {
            return EngineOutcome.Failure("Toutes les dimensions doivent être strictement positives.")
        }
        if (innerDiameter >= outerDiameter) {
            return EngineOutcome.Failure("Le diamètre intérieur doit être strictement inférieur au diamètre extérieur.")
        }
        if (densityKgM3 <= 0) {
            return EngineOutcome.Failure("La masse volumique doit être strictement positive.")
        }

        val odM = UnitEngine.convert(outerDiameter, dimUnit, UnitRegistry.M)
        val idM = UnitEngine.convert(innerDiameter, dimUnit, UnitRegistry.M)
        val hM = UnitEngine.convert(height, dimUnit, UnitRegistry.M)

        val rOuter = odM / 2.0
        val rInner = idM / 2.0

        val volumeM3 = PI * (rOuter.pow(2) - rInner.pow(2)) * hM
        val massKg = volumeM3 * densityKgM3

        return buildResult(
            formula = "V = π · (R_ext² - R_int²) · h  |  m = ρ · V",
            stepFormula = "π × [($rOuter m)² - ($rInner m)²] × $hM m",
            volumeM3 = volumeM3,
            massKg = massKg,
            densityKgM3 = densityKgM3
        )
    }

    fun calculateSphere(
        diameter: Double,
        dimUnit: EngineeringUnit,
        densityKgM3: Double
    ): EngineOutcome {
        if (diameter <= 0) {
            return EngineOutcome.Failure("Le diamètre de la sphère doit être strictement positif.")
        }
        if (densityKgM3 <= 0) {
            return EngineOutcome.Failure("La masse volumique doit être strictement positive.")
        }

        val dM = UnitEngine.convert(diameter, dimUnit, UnitRegistry.M)
        val rM = dM / 2.0
        val volumeM3 = (4.0 / 3.0) * PI * rM.pow(3)
        val massKg = volumeM3 * densityKgM3

        return buildResult(
            formula = "V = (4/3) · π · R³  |  m = ρ · V",
            stepFormula = "(4/3) × π × ($rM m)³",
            volumeM3 = volumeM3,
            massKg = massKg,
            densityKgM3 = densityKgM3
        )
    }

    fun calculatePlate(
        length: Double,
        width: Double,
        thicknessMm: Double,
        dimUnit: EngineeringUnit,
        densityKgM3: Double
    ): EngineOutcome {
        if (length <= 0 || width <= 0 || thicknessMm <= 0) {
            return EngineOutcome.Failure("Les dimensions et l'épaisseur de la tôle doivent être strictement positives.")
        }
        if (densityKgM3 <= 0) {
            return EngineOutcome.Failure("La masse volumique doit être strictement positive.")
        }

        val lM = UnitEngine.convert(length, dimUnit, UnitRegistry.M)
        val wM = UnitEngine.convert(width, dimUnit, UnitRegistry.M)
        val tM = thicknessMm / 1000.0 // mm to m

        val volumeM3 = lM * wM * tM
        val massKg = volumeM3 * densityKgM3

        return buildResult(
            formula = "V = L · l · e  |  m = ρ · V",
            stepFormula = "$lM m × $wM m × $tM m",
            volumeM3 = volumeM3,
            massKg = massKg,
            densityKgM3 = densityKgM3
        )
    }

    private fun buildResult(
        formula: String,
        stepFormula: String,
        volumeM3: Double,
        massKg: Double,
        densityKgM3: Double
    ): EngineOutcome {
        val volumeLiters = volumeM3 * 1000.0
        val volumeCm3 = volumeM3 * 1e6
        val massG = massKg * 1000.0
        val massTonne = massKg / 1000.0

        val steps = listOf(
            CalculationStep(
                title = "Calcul du volume géométrique",
                formula = formula.split("|")[0].trim(),
                substitution = stepFormula,
                resultSnippet = "V = ${NumberFormatter.format(volumeM3)} m³ (${NumberFormatter.format(volumeLiters)} L)"
            ),
            CalculationStep(
                title = "Calcul de la masse totale",
                formula = "m = ρ · V",
                substitution = "m = $densityKgM3 kg/m³ × ${NumberFormatter.format(volumeM3)} m³",
                resultSnippet = "m = ${NumberFormatter.format(massKg)} kg"
            )
        )

        val outputs = listOf(
            CalculatorOutput("Masse totale", NumberFormatter.format(massKg), "kg", isPrimary = true),
            CalculatorOutput("Masse en tonnes", NumberFormatter.format(massTonne), "t"),
            CalculatorOutput("Masse en grammes", NumberFormatter.format(massG), "g"),
            CalculatorOutput("Volume en m³", NumberFormatter.format(volumeM3), "m³", isPrimary = true),
            CalculatorOutput("Volume en litres", NumberFormatter.format(volumeLiters), "L"),
            CalculatorOutput("Volume en cm³", NumberFormatter.format(volumeCm3), "cm³")
        )

        return EngineOutcome.Success(
            CalculationResult(
                outputs = outputs,
                steps = steps,
                assumptions = listOf(
                    "Géométrie géométrique parfaite sans bavures ni tolérances d'usinage.",
                    "Matériau compact, homogène et dépourvu de porosités internes."
                ),
                canonicalFormula = formula
            )
        )
    }
}
