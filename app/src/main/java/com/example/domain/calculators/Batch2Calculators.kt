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

// ============================================================================
// TOOL 16 — CONCRETE VOLUME
// ============================================================================
enum class ConcreteElementType(val displayName: String) {
    RECTANGULAR_SLAB("Dalle rectangulaire"),
    RECTANGULAR_BEAM("Poutre rectangulaire"),
    RECTANGULAR_COLUMN("Poteau rectangulaire"),
    CYLINDRICAL_FOUNDATION("Fondation / Pieu cylindrique")
}

object ConcreteVolumeCalculator {

    fun calculate(
        elementType: ConcreteElementType,
        dim1: Double, // Length or Diameter
        dim2: Double, // Width or Depth/Height for cylinder
        dim3: Double, // Thickness or Height for prism
        lengthUnit: EngineeringUnit,
        allowancePercent: Double = 5.0,
        elementCount: Int = 1
    ): EngineOutcome {
        if (elementCount <= 0) {
            return EngineOutcome.Failure("Le nombre d'ouvrages identiques doit être au moins égal à 1.")
        }
        if (allowancePercent < 0.0 || allowancePercent > 100.0) {
            return EngineOutcome.Failure("La majoration pour pertes (%) doit être comprise entre 0% et 100%.")
        }

        val d1M = UnitEngine.convert(dim1, lengthUnit, UnitRegistry.M)
        val d2M = UnitEngine.convert(dim2, lengthUnit, UnitRegistry.M)
        val d3M = UnitEngine.convert(dim3, lengthUnit, UnitRegistry.M)

        val singleVolumeM3: Double
        val formulaStr: String
        val substStr: String

        when (elementType) {
            ConcreteElementType.RECTANGULAR_SLAB,
            ConcreteElementType.RECTANGULAR_BEAM,
            ConcreteElementType.RECTANGULAR_COLUMN -> {
                if (dim1 <= 0.0 || dim2 <= 0.0 || dim3 <= 0.0) {
                    return EngineOutcome.Failure("Toutes les dimensions (Longueur, Largeur, Épaisseur/Hauteur) doivent être strictement positives.")
                }
                singleVolumeM3 = d1M * d2M * d3M
                formulaStr = "V_théo = N × (L × l × h)"
                substStr = "V_théo = $elementCount × (${NumberFormatter.format(d1M)} m × ${NumberFormatter.format(d2M)} m × ${NumberFormatter.format(d3M)} m)"
            }
            ConcreteElementType.CYLINDRICAL_FOUNDATION -> {
                if (dim1 <= 0.0 || dim2 <= 0.0) {
                    return EngineOutcome.Failure("Le diamètre et la profondeur/hauteur du cylindre doivent être strictement positifs.")
                }
                val rM = d1M / 2.0
                singleVolumeM3 = PI * rM.pow(2) * d2M
                formulaStr = "V_théo = N × [π · (D / 2)² · H]"
                substStr = "V_théo = $elementCount × [π × (${NumberFormatter.format(rM)} m)² × ${NumberFormatter.format(d2M)} m]"
            }
        }

        val theoreticalVolumeM3 = singleVolumeM3 * elementCount
        val orderingVolumeM3 = theoreticalVolumeM3 * (1.0 + allowancePercent / 100.0)
        val allowanceVolumeM3 = orderingVolumeM3 - theoreticalVolumeM3

        val steps = listOf(
            CalculationStep(
                title = "1. Volume géométrique théorique net (V_théo)",
                formula = formulaStr,
                substitution = substStr,
                resultSnippet = "V_théo = ${NumberFormatter.format(theoreticalVolumeM3, 3)} m³"
            ),
            CalculationStep(
                title = "2. Volume de commande avec marge de sécurité (${NumberFormatter.format(allowancePercent, 1)}%)",
                formula = "V_commande = V_théo × (1 + marge / 100)",
                substitution = "V_commande = ${NumberFormatter.format(theoreticalVolumeM3, 3)} m³ × (1 + ${allowancePercent / 100.0})",
                resultSnippet = "V_commande = ${NumberFormatter.format(orderingVolumeM3, 3)} m³"
            )
        )

        val outputs = listOf(
            CalculatorOutput("Volume théorique net (V_théo)", NumberFormatter.format(theoreticalVolumeM3, 3), "m³", isPrimary = true),
            CalculatorOutput("Volume de commande conseillé (+${NumberFormatter.format(allowancePercent, 1)}%)", NumberFormatter.format(orderingVolumeM3, 3), "m³", isPrimary = true),
            CalculatorOutput("Volume de perte / tolérance inclus", NumberFormatter.format(allowanceVolumeM3, 3), "m³"),
            CalculatorOutput("Volume en litres (théorique)", NumberFormatter.format(theoreticalVolumeM3 * 1000.0, 1), "L")
        )

        return EngineOutcome.Success(
            CalculationResult(
                outputs = outputs,
                steps = steps,
                assumptions = listOf(
                    "Volume géométrique coffré théorique sans déduction du volume des armatures acier.",
                    "Le volume de commande inclut la majoration définie par l'utilisateur pour couvrir les irrégularités de fouille, le compactage et les fonds de toupie."
                ),
                warnings = listOf(
                    "Distinguez toujours le volume théorique (métré géométrique net) de la quantité commandée en centrale à béton."
                ),
                canonicalFormula = "$formulaStr  |  V_cmd = V_théo · (1 + %_perte)"
            )
        )
    }
}

// ============================================================================
// TOOL 17 — DISTRIBUTED LOAD
// ============================================================================
object DistributedLoadCalculator {

    fun calculate(
        loadIntensityQ: Double, // e.g., kN/m or N/m
        isKilonewtonPerMeter: Boolean,
        loadedLengthValue: Double,
        lengthUnit: EngineeringUnit
    ): EngineOutcome {
        if (loadedLengthValue <= 0.0) {
            return EngineOutcome.Failure("La longueur chargée L doit être strictement positive.")
        }
        if (loadIntensityQ == 0.0) {
            return EngineOutcome.Failure("L'intensité de charge répartie q ne doit pas être nulle.")
        }

        val lengthM = UnitEngine.convert(loadedLengthValue, lengthUnit, UnitRegistry.M)
        val qNPerM = if (isKilonewtonPerMeter) loadIntensityQ * 1000.0 else loadIntensityQ
        val qKnPerM = qNPerM / 1000.0

        val resultantN = qNPerM * lengthM
        val resultantKn = resultantN / 1000.0
        val centerLocationM = lengthM / 2.0
        val centerInSelectedUnit = loadedLengthValue / 2.0

        val steps = listOf(
            CalculationStep(
                title = "1. Force résultante statiquement équivalente (F)",
                formula = "F = q × L",
                substitution = "F = ${NumberFormatter.format(qKnPerM)} kN/m × ${NumberFormatter.format(lengthM)} m",
                resultSnippet = "F = ${NumberFormatter.format(resultantKn)} kN (${NumberFormatter.format(resultantN)} N)"
            ),
            CalculationStep(
                title = "2. Point d'application de la résultante (x_c)",
                formula = "x_c = L / 2",
                substitution = "x_c = ${NumberFormatter.format(lengthM)} m / 2",
                resultSnippet = "x_c = ${NumberFormatter.format(centerLocationM)} m depuis l'origine du segment"
            )
        )

        val outputs = listOf(
            CalculatorOutput("Force résultante équivalente (F)", NumberFormatter.format(resultantKn), "kN", isPrimary = true),
            CalculatorOutput("Force résultante en Newtons", NumberFormatter.format(resultantN), "N"),
            CalculatorOutput("Position du centre de poussée (x_c)", NumberFormatter.format(centerLocationM), "m", isPrimary = true),
            CalculatorOutput("Position dans l'unité choisie", NumberFormatter.format(centerInSelectedUnit), lengthUnit.symbol)
        )

        return EngineOutcome.Success(
            CalculationResult(
                outputs = outputs,
                steps = steps,
                assumptions = listOf(
                    "Charge linéique uniformément répartie d'intensité constante q sur toute la longueur L.",
                    "Le point d'application de la résultante se situe au barycentre (milieu x = L/2) du segment chargé."
                ),
                warnings = listOf(
                    "Cette résultante sert au calcul des réactions d'appui globales ; elle ne remplace pas la charge répartie pour le calcul des moments fléchissants internes ni le dimensionnement complet de structure."
                ),
                canonicalFormula = "F = q · L  |  x_c = L / 2"
            )
        )
    }
}

// ============================================================================
// TOOL 18 — HYDRAULIC CYLINDER FORCE
// ============================================================================
object HydraulicCylinderForceCalculator {

    fun calculate(
        pressureValue: Double,
        pressureUnit: EngineeringUnit,
        pistonDiameterValue: Double,
        rodDiameterValue: Double,
        diameterUnit: EngineeringUnit,
        efficiencyFactor: Double = 1.0
    ): EngineOutcome {
        if (pressureValue <= 0.0) {
            return EngineOutcome.Failure("La pression hydraulique P doit être strictement positive.")
        }
        if (pistonDiameterValue <= 0.0) {
            return EngineOutcome.Failure("Le diamètre d'alésage du piston D doit être strictement positif.")
        }
        if (rodDiameterValue < 0.0) {
            return EngineOutcome.Failure("Le diamètre de tige d ne peut pas être négatif.")
        }
        if (rodDiameterValue >= pistonDiameterValue) {
            return EngineOutcome.Failure("Le diamètre de tige d doit être strictement inférieur au diamètre du piston D.")
        }
        if (efficiencyFactor <= 0.0 || efficiencyFactor > 1.0) {
            return EngineOutcome.Failure("Le rendement hydromécanique η doit être compris dans ]0.0, 1.0].")
        }

        val pPa = UnitEngine.convert(pressureValue, pressureUnit, UnitRegistry.PA)
        val dPistonM = UnitEngine.convert(pistonDiameterValue, diameterUnit, UnitRegistry.M)
        val dRodM = UnitEngine.convert(rodDiameterValue, diameterUnit, UnitRegistry.M)

        val areaExtM2 = PI * dPistonM.pow(2) / 4.0
        val areaRetM2 = PI * (dPistonM.pow(2) - dRodM.pow(2)) / 4.0

        val fExtTheoN = pPa * areaExtM2
        val fRetTheoN = pPa * areaRetM2

        val fExtEffN = fExtTheoN * efficiencyFactor
        val fRetEffN = fRetTheoN * efficiencyFactor

        val steps = listOf(
            CalculationStep(
                title = "1. Sections utiles en poussée (A_ext) et tirage (A_ret)",
                formula = "A_ext = π·D²/4  |  A_ret = π·(D² - d²)/4",
                substitution = "A_ext = ${NumberFormatter.format(areaExtM2 * 1e4, 2)} cm²  |  A_ret = ${NumberFormatter.format(areaRetM2 * 1e4, 2)} cm²",
                resultSnippet = "Sections calculées"
            ),
            CalculationStep(
                title = "2. Forces théoriques sans frottement (η = 100%)",
                formula = "F_ext,th = P · A_ext  |  F_ret,th = P · A_ret",
                substitution = "P = ${NumberFormatter.format(pPa / 1e5, 2)} bar",
                resultSnippet = "F_ext,th = ${NumberFormatter.format(fExtTheoN / 1000.0, 2)} kN  |  F_ret,th = ${NumberFormatter.format(fRetTheoN / 1000.0, 2)} kN"
            ),
            CalculationStep(
                title = "3. Forces utiles corrigées du rendement (η = ${NumberFormatter.format(efficiencyFactor * 100.0, 1)}%)",
                formula = "F_utile = η · F_théorique",
                substitution = "F_ext,u = $efficiencyFactor × ${NumberFormatter.format(fExtTheoN / 1000.0, 2)} kN",
                resultSnippet = "F_ext,u = ${NumberFormatter.format(fExtEffN / 1000.0, 2)} kN  |  F_ret,u = ${NumberFormatter.format(fRetEffN / 1000.0, 2)} kN"
            )
        )

        val outputs = listOf(
            CalculatorOutput("Force théorique en poussée (F_ext,th)", NumberFormatter.format(fExtTheoN / 1000.0, 3), "kN", isPrimary = true),
            CalculatorOutput("Force théorique en tirage (F_ret,th)", NumberFormatter.format(fRetTheoN / 1000.0, 3), "kN", isPrimary = true),
            CalculatorOutput("Force utile en poussée (avec η)", NumberFormatter.format(fExtEffN / 1000.0, 3), "kN", isPrimary = true),
            CalculatorOutput("Force utile en tirage (avec η)", NumberFormatter.format(fRetEffN / 1000.0, 3), "kN", isPrimary = true),
            CalculatorOutput("Surface pleine (poussée)", NumberFormatter.format(areaExtM2 * 1e4, 2), "cm²"),
            CalculatorOutput("Surface annulaire (tirage)", NumberFormatter.format(areaRetM2 * 1e4, 2), "cm²")
        )

        return EngineOutcome.Success(
            CalculationResult(
                outputs = outputs,
                steps = steps,
                assumptions = listOf(
                    "Vérin hydraulique double effet à tige simple.",
                    "Contre-pression sur la chambre opposée supposée nulle (retour direct au réservoir).",
                    "Distinction explicite entre effort hydrostatique théorique (P·A) et effort effectif incluant le rendement mécanique des joints."
                ),
                warnings = listOf(
                    "En compression (sortie de tige longue), vérifiez toujours la charge critique de flambement d'Euler de la tige."
                ),
                canonicalFormula = "F_ext = P · πD²/4  |  F_ret = P · π(D² - d²)/4"
            )
        )
    }
}

// ============================================================================
// TOOL 19 — HYDRAULIC CYLINDER FLOW AND SPEED
// ============================================================================
enum class HydraulicSolveTarget(val displayName: String) {
    SOLVE_VELOCITY_FROM_FLOW("Calculer la vitesse v à partir du débit Q"),
    SOLVE_FLOW_FROM_VELOCITY("Calculer le débit requis Q à partir de la vitesse v")
}

object HydraulicCylinderFlowCalculator {

    fun calculate(
        solveTarget: HydraulicSolveTarget,
        pistonDiameterMm: Double,
        rodDiameterMm: Double,
        knownValue: Double // Flow in L/min OR Velocity in m/s
    ): EngineOutcome {
        if (pistonDiameterMm <= 0.0) {
            return EngineOutcome.Failure("Le diamètre du piston D doit être strictement positif.")
        }
        if (rodDiameterMm < 0.0 || rodDiameterMm >= pistonDiameterMm) {
            return EngineOutcome.Failure("Le diamètre de tige d doit vérifier 0 ≤ d < D.")
        }
        if (knownValue <= 0.0) {
            return EngineOutcome.Failure("La grandeur d'entrée (débit ou vitesse) doit être strictement positive.")
        }

        val dPistonM = pistonDiameterMm / 1000.0
        val dRodM = rodDiameterMm / 1000.0

        val areaExtM2 = PI * dPistonM.pow(2) / 4.0
        val areaRetM2 = PI * (dPistonM.pow(2) - dRodM.pow(2)) / 4.0

        val steps = mutableListOf<CalculationStep>()
        val outputs: List<CalculatorOutput>

        steps.add(
            CalculationStep(
                title = "1. Aires des chambres du vérin",
                formula = "A_ext = π·D²/4  |  A_ret = π·(D² - d²)/4",
                substitution = "A_ext = ${NumberFormatter.format(areaExtM2 * 1e4, 2)} cm²  |  A_ret = ${NumberFormatter.format(areaRetM2 * 1e4, 2)} cm²",
                resultSnippet = "Sections déterminées"
            )
        )

        when (solveTarget) {
            HydraulicSolveTarget.SOLVE_VELOCITY_FROM_FLOW -> {
                // knownValue is Q in L/min -> convert to m³/s: 1 L/min = 1e-3 / 60 m³/s
                val qM3PerS = (knownValue * 1e-3) / 60.0
                val vExtMps = qM3PerS / areaExtM2
                val vRetMps = qM3PerS / areaRetM2

                steps.add(
                    CalculationStep(
                        title = "2. Vitesses de déplacement (v = Q / A)",
                        formula = "v_ext = Q / A_ext  |  v_ret = Q / A_ret",
                        substitution = "Q = $knownValue L/min = ${NumberFormatter.format(qM3PerS, 6)} m³/s",
                        resultSnippet = "v_ext = ${NumberFormatter.format(vExtMps, 3)} m/s  |  v_ret = ${NumberFormatter.format(vRetMps, 3)} m/s"
                    )
                )

                outputs = listOf(
                    CalculatorOutput("Vitesse en sortie (extension v_ext)", NumberFormatter.format(vExtMps, 4), "m/s", isPrimary = true),
                    CalculatorOutput("Vitesse en rentrée (rétraction v_ret)", NumberFormatter.format(vRetMps, 4), "m/s", isPrimary = true),
                    CalculatorOutput("Vitesse sortie en mm/s", NumberFormatter.format(vExtMps * 1000.0, 1), "mm/s"),
                    CalculatorOutput("Vitesse rentrée en mm/s", NumberFormatter.format(vRetMps * 1000.0, 1), "mm/s")
                )
            }
            HydraulicSolveTarget.SOLVE_FLOW_FROM_VELOCITY -> {
                // knownValue is v in m/s -> Q = A * v in m³/s -> L/min = m³/s * 60,000
                val qExtM3s = areaExtM2 * knownValue
                val qRetM3s = areaRetM2 * knownValue
                val qExtLmin = qExtM3s * 60000.0
                val qRetLmin = qRetM3s * 60000.0

                steps.add(
                    CalculationStep(
                        title = "2. Débits volumiques requis (Q = A × v)",
                        formula = "Q_ext = A_ext · v  |  Q_ret = A_ret · v",
                        substitution = "v = $knownValue m/s",
                        resultSnippet = "Q_ext = ${NumberFormatter.format(qExtLmin, 2)} L/min  |  Q_ret = ${NumberFormatter.format(qRetLmin, 2)} L/min"
                    )
                )

                outputs = listOf(
                    CalculatorOutput("Débit requis en sortie (Q_ext)", NumberFormatter.format(qExtLmin, 2), "L/min", isPrimary = true),
                    CalculatorOutput("Débit requis en rentrée (Q_ret)", NumberFormatter.format(qRetLmin, 2), "L/min", isPrimary = true),
                    CalculatorOutput("Débit sortie en m³/h", NumberFormatter.format(qExtM3s * 3600.0, 3), "m³/h"),
                    CalculatorOutput("Débit rentrée en m³/h", NumberFormatter.format(qRetM3s * 3600.0, 3), "m³/h")
                )
            }
        }

        return EngineOutcome.Success(
            CalculationResult(
                outputs = outputs,
                steps = steps,
                assumptions = listOf(
                    "Fluide hydraulique supposé incompressible.",
                    "Absence de fuites internes entre chambres (rendement volumétrique = 100%).",
                    "Écoulement permanent sans montage différentiel régénératif."
                ),
                canonicalFormula = "Q = A · v  |  v = Q / A"
            )
        )
    }
}

// ============================================================================
// TOOL 20 — PLANE WALL HEAT CONDUCTION
// ============================================================================
object HeatConductionCalculator {

    fun calculate(
        thermalConductivityWmk: Double, // k in W/(m·K)
        areaValue: Double,
        areaUnit: EngineeringUnit,
        thicknessValue: Double,
        thicknessUnit: EngineeringUnit,
        deltaTemperatureK: Double // ΔT in K or °C
    ): EngineOutcome {
        if (thermalConductivityWmk <= 0.0) {
            return EngineOutcome.Failure("La conductivité thermique k doit être strictement positive.")
        }
        if (areaValue <= 0.0) {
            return EngineOutcome.Failure("La surface d'échange A doit être strictement positive.")
        }
        if (thicknessValue <= 0.0) {
            return EngineOutcome.Failure("L'épaisseur de paroi L doit être strictement positive.")
        }

        val areaM2 = UnitEngine.convert(areaValue, areaUnit, UnitRegistry.M2)
        val thicknessM = UnitEngine.convert(thicknessValue, thicknessUnit, UnitRegistry.M)

        val qDotWatts = (thermalConductivityWmk * areaM2 * deltaTemperatureK) / thicknessM
        val qFluxDensityWm2 = qDotWatts / areaM2
        val thermalResistanceKw = thicknessM / (thermalConductivityWmk * areaM2)

        val steps = listOf(
            CalculationStep(
                title = "1. Résistance thermique de conduction (R_th)",
                formula = "R_th = L / (k · A)",
                substitution = "R_th = ${NumberFormatter.format(thicknessM)} m / ($thermalConductivityWmk W/(m·K) × ${NumberFormatter.format(areaM2)} m²)",
                resultSnippet = "R_th = ${NumberFormatter.format(thermalResistanceKw, 5)} K/W"
            ),
            CalculationStep(
                title = "2. Flux thermique transmis (Loi de Fourier)",
                formula = "Q̇ = (k · A · ΔT) / L",
                substitution = "Q̇ = ($thermalConductivityWmk × ${NumberFormatter.format(areaM2)} × $deltaTemperatureK) / ${NumberFormatter.format(thicknessM)}",
                resultSnippet = "Q̇ = ${NumberFormatter.format(qDotWatts, 2)} W (${NumberFormatter.format(qDotWatts / 1000.0, 3)} kW)"
            )
        )

        val outputs = listOf(
            CalculatorOutput("Puissance thermique transférée (Q̇)", NumberFormatter.format(qDotWatts, 2), "W", isPrimary = true),
            CalculatorOutput("Puissance en kilowatts", NumberFormatter.format(qDotWatts / 1000.0, 4), "kW", isPrimary = true),
            CalculatorOutput("Densité de flux thermique (φ)", NumberFormatter.format(qFluxDensityWm2, 2), "W/m²"),
            CalculatorOutput("Résistance thermique conductive (R_th)", NumberFormatter.format(thermalResistanceKw, 5), "K/W")
        )

        return EngineOutcome.Success(
            CalculationResult(
                outputs = outputs,
                steps = steps,
                assumptions = listOf(
                    "Régime thermique permanent (stationnaire).",
                    "Transfert de chaleur unidimensionnel (1D) normal à la paroi plane.",
                    "Conductivité thermique k homogène et constante sur la plage de température.",
                    "Aucune résistance thermique de contact ni résistance superficielle de convection ou rayonnement incluse."
                ),
                canonicalFormula = "Q̇ = (k · A · ΔT) / L"
            )
        )
    }
}

// ============================================================================
// TOOL 21 — THERMAL EXPANSION
// ============================================================================
object ThermalExpansionCalculator {

    fun calculate(
        initialLengthValue: Double,
        lengthUnit: EngineeringUnit,
        alphaPerKelvinMicrons: Double, // α in 10^-6 / K (μm/(m·K))
        deltaTemperatureK: Double // ΔT = T_final - T_initial (in °C or K)
    ): EngineOutcome {
        if (initialLengthValue <= 0.0) {
            return EngineOutcome.Failure("La longueur initiale L₀ doit être strictement positive.")
        }
        if (alphaPerKelvinMicrons <= 0.0) {
            return EngineOutcome.Failure("Le coefficient de dilatation linéaire α doit être strictement positif.")
        }

        val alpha = alphaPerKelvinMicrons * 1e-6
        val l0Meters = UnitEngine.convert(initialLengthValue, lengthUnit, UnitRegistry.M)
        val deltaLMeters = alpha * l0Meters * deltaTemperatureK
        val finalLMeters = l0Meters + deltaLMeters

        val deltaLMm = deltaLMeters * 1000.0
        val finalLSelectedUnit = UnitEngine.convert(finalLMeters, UnitRegistry.M, lengthUnit)

        val regime = when {
            deltaTemperatureK > 0.0 -> "Dilatation thermique (+)"
            deltaTemperatureK < 0.0 -> "Contraction thermique (-)"
            else -> "Équilibre isotherme (0)"
        }

        val steps = listOf(
            CalculationStep(
                title = "1. Variation dimensionnelle linéaire (ΔL)",
                formula = "ΔL = α · L₀ · ΔT",
                substitution = "ΔL = (${alphaPerKelvinMicrons}×10⁻⁶ K⁻¹) × ${NumberFormatter.format(l0Meters)} m × $deltaTemperatureK K",
                resultSnippet = "ΔL = ${NumberFormatter.format(deltaLMm, 4)} mm"
            ),
            CalculationStep(
                title = "2. Longueur finale libre (L_f)",
                formula = "L_f = L₀ + ΔL",
                substitution = "L_f = ${NumberFormatter.format(l0Meters)} m + (${NumberFormatter.format(deltaLMeters, 6)} m)",
                resultSnippet = "L_f = ${NumberFormatter.format(finalLSelectedUnit, 4)} ${lengthUnit.symbol}"
            )
        )

        val outputs = listOf(
            CalculatorOutput("Variation de longueur (ΔL)", NumberFormatter.format(deltaLMm, 4), "mm", isPrimary = true),
            CalculatorOutput("Longueur finale (L_f)", NumberFormatter.format(finalLSelectedUnit, 4), lengthUnit.symbol, isPrimary = true),
            CalculatorOutput("Longueur finale en mètres", NumberFormatter.format(finalLMeters, 5), "m"),
            CalculatorOutput("Phénomène physique", regime, "")
        )

        return EngineOutcome.Success(
            CalculationResult(
                outputs = outputs,
                steps = steps,
                assumptions = listOf(
                    "Dilatation thermique libre sans bridage mécanique externe.",
                    "Coefficient de dilatation linéaire α supposé constant sur l'intervalle ΔT.",
                    "Matériau continu sans changement de phase cristalline sur la plage de température."
                ),
                warnings = listOf(
                    "Si la pièce est bridée à ses extrémités, l'allongement empêché génère une contrainte thermique σ_th = -E · α · ΔT pouvant causer du flambement ou la rupture."
                ),
                canonicalFormula = "ΔL = α · L₀ · ΔT  |  L_f = L₀ + ΔL"
            )
        )
    }
}
