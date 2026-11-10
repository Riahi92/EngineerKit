package com.example.core.data

import com.example.core.model.CategoryId
import com.example.core.model.EngineeringCategory
import com.example.core.model.EngineeringTool
import com.example.core.model.PlannedInput
import com.example.core.model.PlannedOutput
import com.example.ui.theme.*

object ToolCatalog {

    val categories: List<EngineeringCategory> = listOf(
        EngineeringCategory(
            id = CategoryId.MECANIQUE,
            name = "Génie mécanique",
            shortDescription = "RDM, contraintes axiales, torsion d'arbre, flexion de poutre et masse/volume",
            iconName = "settings",
            accentColor = CatMecanique,
            toolCount = 4,
            longDescription = "Calculs fondamentaux pour la résistance des matériaux, le dimensionnement d'arbres en torsion, la flexion de poutres et l'analyse des contraintes."
        ),
        EngineeringCategory(
            id = CategoryId.ELECTRIQUE,
            name = "Génie électrique",
            shortDescription = "Loi d'Ohm, chute de tension, puissance triphasée et résistances équivalentes",
            iconName = "bolt",
            accentColor = CatElectrique,
            toolCount = 4,
            longDescription = "Outils de calcul pour installations basse et haute tension, dimensionnement de câbles, puissance active/réactive et groupements de résistances."
        ),
        EngineeringCategory(
            id = CategoryId.ELECTRONIQUE,
            name = "Électronique",
            shortDescription = "Pont diviseur de tension, constante de temps RC et réponse transitoire",
            iconName = "memory",
            accentColor = CatElectronique,
            toolCount = 2,
            longDescription = "Formulaires pour circuits analogiques, ponts diviseurs, charge/décharge de condensateurs et filtres RC du premier ordre."
        ),
        EngineeringCategory(
            id = CategoryId.INDUSTRIEL,
            name = "Génie industriel",
            shortDescription = "TRS (OEE), Takt time Lean et capacité de production multi-machines",
            iconName = "precision_manufacturing",
            accentColor = CatIndustriel,
            toolCount = 3,
            longDescription = "Optimisation de la production industrielle, cadencement Lean Manufacturing, TRS/OEE et dimensionnement de capacité de lignes."
        ),
        EngineeringCategory(
            id = CategoryId.CIVIL,
            name = "Génie civil",
            shortDescription = "Cubature de béton (dalles, poutres, poteaux, pieux) et charges réparties",
            iconName = "foundation",
            accentColor = CatCivil,
            toolCount = 2,
            longDescription = "Calculs dimensionnels pour ouvrages de génie civil, cubature de béton avec majoration de pertes et réduction des charges linéiques."
        ),
        EngineeringCategory(
            id = CategoryId.HYDRAULIQUE,
            name = "Hydraulique",
            shortDescription = "Effort de vérin hydraulique (poussée/traction) et débit/vitesse d'actionneur",
            iconName = "water_drop",
            accentColor = CatHydraulique,
            toolCount = 2,
            longDescription = "Transmission de puissance hydrostatique, dimensionnement de vérins simple et double effet, vitesses de tige et débits d'huile."
        ),
        EngineeringCategory(
            id = CategoryId.THERMIQUE,
            name = "Thermique et énergétique",
            shortDescription = "Conduction thermique de Fourier et dilatation thermique linéaire",
            iconName = "thermostat",
            accentColor = CatThermique,
            toolCount = 2,
            longDescription = "Transferts thermiques conductifs à travers parois planes, résistance thermique et allongement thermo-élastique des matériaux."
        ),
        EngineeringCategory(
            id = CategoryId.CHIMIQUE,
            name = "Génie chimique",
            shortDescription = "Concentration molaire (molarité) et dilution de solutions C₁V₁ = C₂V₂",
            iconName = "science",
            accentColor = CatChimique,
            toolCount = 2,
            longDescription = "Stœchiométrie, préparation de solutions titrées, molarité, concentration massique et facteurs de dilution."
        ),
        EngineeringCategory(
            id = CategoryId.INFORMATIQUE,
            name = "Informatique",
            shortDescription = "Convertisseur de bases (2/8/10/16) 64 bits et portes logiques booléennes",
            iconName = "terminal",
            accentColor = CatInformatique,
            toolCount = 2,
            longDescription = "Outils pour l'ingénierie logicielle et systèmes embarqués : conversion entière 64 bits Binaire/Octal/Décimal/Hexa et algèbre de Boole."
        ),
        EngineeringCategory(
            id = CategoryId.AUTOMATISME,
            name = "Automatisme",
            shortDescription = "Mise à l'échelle de signaux analogiques et convertisseur boucle 4–20 mA",
            iconName = "tune",
            accentColor = CatAutomatisme,
            toolCount = 2,
            longDescription = "Contrôle-commande industriel, instrumentation API/PLC, mise à l'échelle linéaire universelle et diagnostic de boucles de courant 4–20 mA."
        ),
        EngineeringCategory(
            id = CategoryId.ENVIRONNEMENT,
            name = "Environnement",
            shortDescription = "Consommation d'énergie électrique et empreinte carbone CO₂e",
            iconName = "eco",
            accentColor = CatEnvironnement,
            toolCount = 1,
            longDescription = "Évaluation d'impact environnemental, bilan énergétique électrique (kWh, MWh, MJ) et estimation des émissions de gaz à effet de serre."
        ),
        EngineeringCategory(
            id = CategoryId.MATHEMATIQUES,
            name = "Mathématiques",
            shortDescription = "Calculatrice scientifique, convertisseur d'unités, 2nd degré et statistiques",
            iconName = "calculate",
            accentColor = CatMathematiques,
            toolCount = 4,
            longDescription = "Outils mathématiques et dimensionnels appliqués aux sciences de l'ingénieur : calcul scientifique, ISO 80000, algèbre et statistiques."
        )
    )

    val tools: List<EngineeringTool> = listOf(
        // =====================================================================
        // MATHÉMATIQUES (Tools 1–4)
        // =====================================================================
        EngineeringTool(
            id = "tool_sci_calc",
            name = "Calculatrice scientifique",
            shortDescription = "Calculateur scientifique haute précision avec trigonométrie, puissances, racines, logarithmes et constantes.",
            categoryId = CategoryId.MATHEMATIQUES,
            isAvailable = true,
            releasePhase = "Phase 2 : Calculateur certifié",
            formulaPreview = "f(x) = sin(θ) + ln(x) · e^(-kt)",
            formulaExplanation = "Analyseur syntaxique récursif descendant évaluant les expressions mathématiques complexes sans interpréteur eval.",
            plannedInputs = listOf(
                PlannedInput("Expression mathématique", "expr", "Texte", "Formule algébrique ou trigonométrique"),
                PlannedInput("Mode angulaire", "deg/rad", "Degré", "Sélection d'unité pour les angles trigonométriques")
            ),
            plannedOutputs = listOf(
                PlannedOutput("Résultat numérique", "R", "Réel"),
                PlannedOutput("Forme scientifique", "R_sci", "Notation ingénieur")
            ),
            tags = listOf("calculatrice", "scientifique", "trigonométrie", "rapide", "maths")
        ),
        EngineeringTool(
            id = "tool_unit_converter",
            name = "Convertisseur d'unités",
            shortDescription = "Conversion instantanée et bidirectionnelle pour 12 grandeurs dimensionnelles (Pression, Force, Énergie, etc.).",
            categoryId = CategoryId.MATHEMATIQUES,
            isAvailable = true,
            releasePhase = "Phase 2 : Calculateur certifié",
            formulaPreview = "V_cible = (V_source · k_source) / k_cible",
            formulaExplanation = "Moteur de conversion dimensionnelle conforme ISO 80000 avec gestion affine exacte des échelles de températures.",
            plannedInputs = listOf(
                PlannedInput("Valeur source", "val", "Unité dép.", "Grandeur physique à convertir"),
                PlannedInput("Unité d'origine", "u1", "-", "Unité actuelle (ex: bar, PSI, Pascal)"),
                PlannedInput("Unité cible", "u2", "-", "Unité de destination voulue")
            ),
            plannedOutputs = listOf(
                PlannedOutput("Valeur convertie", "val_out", "Unité cible"),
                PlannedOutput("Facteur de passage", "k", "Ratio direct")
            ),
            tags = listOf("unités", "conversion", "bar", "psi", "newton", "joule", "kelvin", "si")
        ),
        EngineeringTool(
            id = "tool_math_quadratic",
            name = "Équation du 2nd degré",
            shortDescription = "Résolution analytique a·x² + b·x + c = 0 avec discriminant Δ, racines réelles, doubles ou complexes.",
            categoryId = CategoryId.MATHEMATIQUES,
            isAvailable = true,
            releasePhase = "Phase 2 : Calculateur certifié",
            formulaPreview = "Δ = b² - 4ac  |  x₁,₂ = (-b ± √Δ) / 2a",
            formulaExplanation = "Résolution complète de Viète avec détection automatique de la dégénérescence linéaire (a = 0) et étapes détaillées.",
            plannedInputs = listOf(
                PlannedInput("Coefficient quadratique", "a", "-", "Facteur de x²"),
                PlannedInput("Coefficient linéaire", "b", "-", "Facteur de x"),
                PlannedInput("Constante indépendante", "c", "-", "Terme constant")
            ),
            plannedOutputs = listOf(
                PlannedOutput("Discriminant", "Δ", "-"),
                PlannedOutput("Première racine", "x₁", "Réel ou Complexe"),
                PlannedOutput("Deuxième racine", "x₂", "Réel ou Complexe")
            ),
            tags = listOf("maths", "second degré", "discriminant", "racines", "complexe", "polynome")
        ),
        EngineeringTool(
            id = "tool_math_statistics",
            name = "Statistiques descriptives",
            shortDescription = "Moyenne, médiane, étendue, variance de population/échantillon et écarts-types pour séries numériques.",
            categoryId = CategoryId.MATHEMATIQUES,
            isAvailable = true,
            releasePhase = "Phase 2 : Calculateur certifié",
            formulaPreview = "x̄ = (∑x)/N  |  s = √[ ∑(x - x̄)² / (N-1) ]",
            formulaExplanation = "Analyse statistique univariée avec correction de Bessel pour les variances et écarts-types d'échantillons.",
            plannedInputs = listOf(
                PlannedInput("Série de nombres", "x_i", "Liste", "Valeurs séparées par virgules, espaces ou retours à la ligne")
            ),
            plannedOutputs = listOf(
                PlannedOutput("Moyenne arithmétique", "x̄", "-"),
                PlannedOutput("Médiane", "Med", "-"),
                PlannedOutput("Écart-type échantillon", "s", "-"),
                PlannedOutput("Étendue", "Max - Min", "-")
            ),
            tags = listOf("maths", "statistiques", "moyenne", "médiane", "variance", "écart-type")
        ),

        // =====================================================================
        // GÉNIE MÉCANIQUE (Tools 5, 6, 7, 11)
        // =====================================================================
        EngineeringTool(
            id = "tool_meca_stress",
            name = "Calcul mécanique & Contrainte axiale",
            shortDescription = "Contrainte normale σ = F / A en traction ou compression, vérification vis-à-vis de la limite élastique Re.",
            categoryId = CategoryId.MECANIQUE,
            isAvailable = true,
            releasePhase = "Phase 2 : Calculateur certifié",
            formulaPreview = "σ = F / A  |  s = R_e / |σ|",
            formulaExplanation = "Résistance des matériaux pour éléments prismatiques sous sollicitation axiale pure avec conversions d'unités intégrées.",
            plannedInputs = listOf(
                PlannedInput("Effort normal axial", "F", "N, kN, lbf", "Force axiale appliquée"),
                PlannedInput("Section droite", "A", "mm², cm², m²", "Aire transversale porteuse"),
                PlannedInput("Limite élastique", "Re", "MPa", "Optionnel : propriété mécanique du matériau")
            ),
            plannedOutputs = listOf(
                PlannedOutput("Contrainte normale", "σ", "MPa"),
                PlannedOutput("Contrainte en N/mm²", "σ", "N/mm²"),
                PlannedOutput("Coefficient de sécurité", "s", "-")
            ),
            tags = listOf("mecanique", "rdm", "contrainte", "traction", "compression", "sécurité")
        ),
        EngineeringTool(
            id = "tool_meca_mass_volume",
            name = "Masse & Volume géométrique",
            shortDescription = "Calcul de volume et masse pour prismes, cylindres, tubes creux, sphères et tôles avec sélection de matériaux.",
            categoryId = CategoryId.MECANIQUE,
            isAvailable = true,
            releasePhase = "Phase 2 : Calculateur certifié",
            formulaPreview = "V = f(L, l, h, D)  |  m = ρ · V",
            formulaExplanation = "Intégration géométrique exacte couplée à une bibliothèque de masses volumiques certifiées pour métaux, polymères et bétons.",
            plannedInputs = listOf(
                PlannedInput("Dimensions géométriques", "L, D, h", "mm, cm, m", "Cotes caractéristiques"),
                PlannedInput("Masse volumique", "ρ", "kg/m³", "Préréglage matière ou saisie manuelle")
            ),
            plannedOutputs = listOf(
                PlannedOutput("Masse totale", "m", "kg"),
                PlannedOutput("Volume en m³", "V", "m³"),
                PlannedOutput("Volume en litres", "V", "L")
            ),
            tags = listOf("mecanique", "volume", "masse", "densité", "poids", "acier", "aluminium")
        ),
        EngineeringTool(
            id = "tool_meca_beam",
            name = "Poutre sur deux appuis (Flèche & Flexion)",
            shortDescription = "Moment fléchissant maximal, flèche élastique au centre et contrainte de flexion pour charges ponctuelle et répartie.",
            categoryId = CategoryId.MECANIQUE,
            isAvailable = true,
            releasePhase = "Phase 2 : Calculateur certifié",
            formulaPreview = "M_max = (F·L)/4 ou (q·L²)/8  |  δ_max",
            formulaExplanation = "Théorie des poutres d'Euler-Bernoulli en régime élastique linéaire avec estimation du ratio flèche/portée L/δ.",
            plannedInputs = listOf(
                PlannedInput("Portée entre appuis", "L", "m", "Longueur de la travée"),
                PlannedInput("Charge appliquée", "F ou q", "kN ou kN/m", "Intensité de la charge"),
                PlannedInput("Module d'Young", "E", "GPa", "Rigidité du matériau"),
                PlannedInput("Moment d'inertie", "I_z", "cm⁴", "Moment quadratique de section")
            ),
            plannedOutputs = listOf(
                PlannedOutput("Moment fléchissant max", "M_max", "kN·m"),
                PlannedOutput("Flèche maximale", "δ_max", "mm"),
                PlannedOutput("Contrainte de flexion", "σ_max", "MPa")
            ),
            tags = listOf("mecanique", "poutre", "flèche", "flexion", "moment", "inertie")
        ),
        EngineeringTool(
            id = "tool_meca_torsion",
            name = "Torsion d'arbre circulaire",
            shortDescription = "Contrainte tangentielle maximale τ_max, moment quadratique polaire J et angle de torsion θ pour arbre plein ou creux.",
            categoryId = CategoryId.MECANIQUE,
            isAvailable = true,
            releasePhase = "Phase 3 : Calculateur certifié",
            formulaPreview = "J = π(D⁴ - d⁴)/32  |  τ_max = T(D/2)/J  |  θ = TL/(GJ)",
            formulaExplanation = "Calcul de résistance en torsion élastique linéaire pour arbres de transmission pleins ou tubulaires.",
            plannedInputs = listOf(
                PlannedInput("Couple de torsion", "T", "N·m, kN·m", "Moment de torsion appliqué"),
                PlannedInput("Longueur d'arbre", "L", "mm, m", "Portée soumise à la torsion"),
                PlannedInput("Diamètre extérieur / intérieur", "D, d", "mm", "d = 0 pour arbre plein"),
                PlannedInput("Module de Coulomb", "G", "GPa", "Module de cisaillement")
            ),
            plannedOutputs = listOf(
                PlannedOutput("Contrainte de cisaillement max", "τ_max", "MPa"),
                PlannedOutput("Angle de torsion", "θ", "deg et rad"),
                PlannedOutput("Moment polaire", "J", "mm⁴")
            ),
            tags = listOf("mecanique", "torsion", "arbre", "cisaillement", "couple", "rdm")
        ),

        // =====================================================================
        // GÉNIE ÉLECTRIQUE (Tools 8, 9, 12, 13)
        // =====================================================================
        EngineeringTool(
            id = "tool_elec_ohm",
            name = "Loi d'Ohm & Puissance",
            shortDescription = "Résolution multivariable complète entre Tension U, Courant I, Résistance R et Puissance dissipée P.",
            categoryId = CategoryId.ELECTRIQUE,
            isAvailable = true,
            releasePhase = "Phase 2 : Calculateur certifié",
            formulaPreview = "U = R · I  |  P = U · I = R · I² = U² / R",
            formulaExplanation = "Résout automatiquement les deux inconnues restantes à partir de deux grandeurs électriques quelconques saisies.",
            plannedInputs = listOf(
                PlannedInput("Première grandeur connue", "U, I, R ou P", "V, A, Ω, W", "Valeur 1"),
                PlannedInput("Deuxième grandeur connue", "U, I, R ou P", "V, A, Ω, W", "Valeur 2")
            ),
            plannedOutputs = listOf(
                PlannedOutput("Tension", "U", "V"),
                PlannedOutput("Courant", "I", "A"),
                PlannedOutput("Résistance", "R", "Ω"),
                PlannedOutput("Puissance", "P", "W")
            ),
            tags = listOf("electrique", "ohm", "puissance", "tension", "courant", "résistance", "joule")
        ),
        EngineeringTool(
            id = "tool_elec_threephase",
            name = "Puissance triphasée équilibrée",
            shortDescription = "Puissance active P (kW), apparente S (kVA), réactive Q (kVAR) et courant pour réseaux triphasés équilibrés.",
            categoryId = CategoryId.ELECTRIQUE,
            isAvailable = true,
            releasePhase = "Phase 2 : Calculateur certifié",
            formulaPreview = "P = √3·U·I·cosφ  |  Q = √3·U·I·sinφ  |  S = √3·U·I",
            formulaExplanation = "Formules de Boucherot pour circuits équilibrés sinusoïdaux avec détection d'énergie réactive excessive.",
            plannedInputs = listOf(
                PlannedInput("Tension composée", "U", "V", "Tension entre phases (ex: 400V)"),
                PlannedInput("Courant de ligne", "I", "A", "Intensité absorbée"),
                PlannedInput("Facteur de puissance", "cos φ", "-", "Compris entre 0 et 1")
            ),
            plannedOutputs = listOf(
                PlannedOutput("Puissance active", "P", "kW"),
                PlannedOutput("Puissance apparente", "S", "kVA"),
                PlannedOutput("Puissance réactive", "Q", "kVAR")
            ),
            tags = listOf("electrique", "triphasé", "cos phi", "puissance active", "kvar", "kva", "moteur")
        ),
        EngineeringTool(
            id = "tool_elec_cable",
            name = "Chute de tension en ligne",
            shortDescription = "Calcul de la chute de tension ΔU (V et %) et tension en bout de ligne pour circuits DC, monophasé et triphasé.",
            categoryId = CategoryId.ELECTRIQUE,
            isAvailable = true,
            releasePhase = "Phase 3 : Calculateur certifié",
            formulaPreview = "ΔU = k · ρ · L · I · cosφ / S  |  ΔU% = 100 · ΔU / U_n",
            formulaExplanation = "Modèle résistif des liaisons électriques cuivre/aluminium avec alerte sur les seuils usuels (3% éclairage, 5% force).",
            plannedInputs = listOf(
                PlannedInput("Type de réseau", "DC / 1~ / 3~", "-", "Continu, monophasé ou triphasé"),
                PlannedInput("Courant & Tension", "I, U_n", "A, V", "Paramètres nominaux"),
                PlannedInput("Longueur & Section", "L, S", "m, mm²", "Géométrie du câble")
            ),
            plannedOutputs = listOf(
                PlannedOutput("Chute de tension", "ΔU", "V"),
                PlannedOutput("Chute relative", "ΔU%", "%"),
                PlannedOutput("Tension récepteur", "U_load", "V")
            ),
            tags = listOf("electrique", "câble", "section", "chute de tension", "cuivre", "aluminium")
        ),
        EngineeringTool(
            id = "tool_elec_resistors_eq",
            name = "Résistances équivalentes (Série / Parallèle)",
            shortDescription = "Calcul de la résistance équivalente R_eq et conductance G_eq pour un réseau de N résistances en série ou parallèle.",
            categoryId = CategoryId.ELECTRIQUE,
            isAvailable = true,
            releasePhase = "Phase 3 : Calculateur certifié",
            formulaPreview = "R_série = ∑ R_i  |  1 / R_par = ∑ (1 / R_i)",
            formulaExplanation = "Association de dipôles résistifs passifs avec gestion exacte des branches court-circuitées.",
            plannedInputs = listOf(
                PlannedInput("Topologie", "Série / Parallèle", "-", "Mode de connexion"),
                PlannedInput("Liste des résistances", "R_1 .. R_n", "Ω, kΩ, MΩ", "Valeurs séparées par virgules")
            ),
            plannedOutputs = listOf(
                PlannedOutput("Résistance équivalente", "R_eq", "Ω / kΩ"),
                PlannedOutput("Conductance totale", "G_eq", "S")
            ),
            tags = listOf("electrique", "résistance", "série", "parallèle", "équivalente")
        ),

        // =====================================================================
        // ÉLECTRONIQUE (Tools 14, 15)
        // =====================================================================
        EngineeringTool(
            id = "tool_electron_divider",
            name = "Pont diviseur de tension",
            shortDescription = "Tension de sortie à vide V_out, ratio d'atténuation, courant de branche et puissances dissipées dans R₁ et R₂.",
            categoryId = CategoryId.ELECTRONIQUE,
            isAvailable = true,
            releasePhase = "Phase 3 : Calculateur certifié",
            formulaPreview = "V_out = V_in · R₂ / (R₁ + R₂)",
            formulaExplanation = "Analyse complète d'un diviseur résistif avec calcul d'impédance de Thévenin équivalente (R₁ // R₂).",
            plannedInputs = listOf(
                PlannedInput("Tension d'entrée", "V_in", "V", "Source continue ou efficace"),
                PlannedInput("Résistances du pont", "R₁, R₂", "Ω, kΩ, MΩ", "Résistances supérieure et de pied")
            ),
            plannedOutputs = listOf(
                PlannedOutput("Tension de sortie", "V_out", "V"),
                PlannedOutput("Ratio de transfert", "k", "%"),
                PlannedOutput("Impédance Thévenin", "R_th", "Ω")
            ),
            tags = listOf("electronique", "pont diviseur", "tension", "thévenin", "atténuation")
        ),
        EngineeringTool(
            id = "tool_elec_filter_rc",
            name = "Constante de temps RC & Filtre",
            shortDescription = "Constante τ = R·C, temps à 63.2% (1τ), 95% (3τ), 99.3% (5τ), fréquence de coupure f_c et tension instantanée V(t).",
            categoryId = CategoryId.ELECTRONIQUE,
            isAvailable = true,
            releasePhase = "Phase 3 : Calculateur certifié",
            formulaPreview = "τ = R · C  |  V(t) = V_s · (1 - e^(-t/RC))",
            formulaExplanation = "Réponse indicielle d'un circuit RC du premier ordre avec tracé de la courbe exponentielle de charge.",
            plannedInputs = listOf(
                PlannedInput("Résistance", "R", "Ω, kΩ, MΩ", "Résistance série"),
                PlannedInput("Capacité", "C", "pF, nF, µF, mF", "Condensateur"),
                PlannedInput("Tension source & instant t", "V_s, t", "V, s/ms", "Pour évaluation transitoire")
            ),
            plannedOutputs = listOf(
                PlannedOutput("Constante de temps", "τ", "s / ms"),
                PlannedOutput("Temps de réponse 5τ", "5τ", "s / ms"),
                PlannedOutput("Fréquence de coupure", "f_c", "Hz")
            ),
            tags = listOf("electronique", "rc", "constante de temps", "condensateur", "filtre", "tau")
        ),

        // =====================================================================
        // GÉNIE CIVIL (Tools 16, 17)
        // =====================================================================
        EngineeringTool(
            id = "tool_civil_concrete",
            name = "Cubature & Volume de béton",
            shortDescription = "Calcul du volume théorique et du volume à commander (avec pertes %) pour dalles, poutres, poteaux et fondations cylindriques.",
            categoryId = CategoryId.CIVIL,
            isAvailable = true,
            releasePhase = "Phase 3 : Calculateur certifié",
            formulaPreview = "V_cmd = N · V_théo · (1 + Pertes%/100)",
            formulaExplanation = "Métré d'éléments de structure en béton armé avec estimation de la masse fraîche et du nombre de toupies.",
            plannedInputs = listOf(
                PlannedInput("Type d'ouvrage", "Dalle / Poutre / Pieu", "-", "Forme géométrique"),
                PlannedInput("Cotes & Quantité", "L, l, e ou D, H", "m, cm, mm", "Dimensions de coffrage"),
                PlannedInput("Majoration pertes", "Pertes", "%", "Marge chantier usuelle 3% à 10%")
            ),
            plannedOutputs = listOf(
                PlannedOutput("Volume théorique", "V_théo", "m³"),
                PlannedOutput("Volume à commander", "V_cmd", "m³"),
                PlannedOutput("Masse estimée", "m", "tonnes")
            ),
            tags = listOf("civil", "béton", "cubature", "dalle", "poteau", "fondation", "chantier")
        ),
        EngineeringTool(
            id = "tool_civil_distributed_load",
            name = "Réduction de charge répartie uniforme",
            shortDescription = "Convertit une charge linéique uniforme q (kN/m, N/m) sur une travée L en résultante ponctuelle F et point d'application.",
            categoryId = CategoryId.CIVIL,
            isAvailable = true,
            releasePhase = "Phase 3 : Calculateur certifié",
            formulaPreview = "F_eq = q · L  |  x_G = L / 2",
            formulaExplanation = "Torseur statique équivalent d'une charge uniformément répartie avec réactions d'appuis symétriques.",
            plannedInputs = listOf(
                PlannedInput("Intensité linéique", "q", "kN/m, N/m", "Charge par unité de longueur"),
                PlannedInput("Longueur chargée", "L", "m, cm, mm", "Étendue de la charge")
            ),
            plannedOutputs = listOf(
                PlannedOutput("Force résultante", "F_eq", "kN et N"),
                PlannedOutput("Centre d'application", "x_G", "m")
            ),
            tags = listOf("civil", "charge répartie", "résultante", "descente de charges", "statique")
        ),

        // =====================================================================
        // HYDRAULIQUE (Tools 18, 19)
        // =====================================================================
        EngineeringTool(
            id = "tool_hydro_cylinder_force",
            name = "Effort de vérin hydraulique",
            shortDescription = "Forces de poussée (sortie) et de traction (rentrée de tige) théoriques et utiles avec rendement mécanique η.",
            categoryId = CategoryId.HYDRAULIQUE,
            isAvailable = true,
            releasePhase = "Phase 3 : Calculateur certifié",
            formulaPreview = "F_poussée = P · πD²/4 · η  |  F_traction = P · π(D²-d²) / 4 · η",
            formulaExplanation = "Calcul hydrostatique complet pour vérin double effet tenant compte de la section annulaire côté tige.",
            plannedInputs = listOf(
                PlannedInput("Pression de service", "P", "bar, MPa, PSI", "Pression hydraulique"),
                PlannedInput("Alésage piston & Tige", "D, d", "mm", "Diamètres du vérin"),
                PlannedInput("Rendement hydromécanique", "η", "%", "Pertes par frottement des joints")
            ),
            plannedOutputs = listOf(
                PlannedOutput("Force de poussée utile", "F_ext", "kN et daN"),
                PlannedOutput("Force de traction utile", "F_ret", "kN et daN")
            ),
            tags = listOf("hydraulique", "vérin", "poussée", "traction", "pression", "bar")
        ),
        EngineeringTool(
            id = "tool_hydro_cylinder_flow",
            name = "Débit & Vitesse de vérin hydraulique",
            shortDescription = "Calcule la vitesse de déplacement d'un vérin à partir du débit Q (L/min) ou le débit requis pour une vitesse cible.",
            categoryId = CategoryId.HYDRAULIQUE,
            isAvailable = true,
            releasePhase = "Phase 3 : Calculateur certifié",
            formulaPreview = "v = Q / A  |  Q = A · v",
            formulaExplanation = "Analyse cinématique sortie/rentrée de tige pour le dimensionnement de groupes hydrauliques et distributeurs.",
            plannedInputs = listOf(
                PlannedInput("Alésage & Tige", "D, d", "mm", "Diamètres du vérin"),
                PlannedInput("Débit ou Vitesse", "Q ou v", "L/min ou mm/s", "Selon le mode de calcul choisi")
            ),
            plannedOutputs = listOf(
                PlannedOutput("Vitesse sortie / rentrée", "v_ext, v_ret", "mm/s et m/s"),
                PlannedOutput("Débit pompe requis", "Q", "L/min")
            ),
            tags = listOf("hydraulique", "débit", "vitesse", "vérin", "pompe", "l/min")
        ),

        // =====================================================================
        // THERMIQUE ET ÉNERGÉTIQUE (Tools 20, 21)
        // =====================================================================
        EngineeringTool(
            id = "tool_therm_conduction",
            name = "Conduction thermique de Fourier",
            shortDescription = "Flux thermique conductif Q̇ (W, kW), densité de flux q (W/m²) et résistance thermique R_th à travers une paroi plane.",
            categoryId = CategoryId.THERMIQUE,
            isAvailable = true,
            releasePhase = "Phase 3 : Calculateur certifié",
            formulaPreview = "Q̇ = k · A · (T₁ - T₂) / L  |  R_th = L / (k · A)",
            formulaExplanation = "Loi de Fourier 1D stationnaire avec gestion complète des unités de température (°C, K, °F).",
            plannedInputs = listOf(
                PlannedInput("Conductivité thermique", "k (λ)", "W/(m·K)", "Propriété du matériau"),
                PlannedInput("Surface & Épaisseur", "A, L", "m², mm/cm/m", "Géométrie de la paroi"),
                PlannedInput("Températures des faces", "T₁, T₂", "°C, K, °F", "Faces chaude et froide")
            ),
            plannedOutputs = listOf(
                PlannedOutput("Flux thermique", "Q̇", "W et kW"),
                PlannedOutput("Densité de flux", "q", "W/m²"),
                PlannedOutput("Résistance thermique", "R_th", "K/W")
            ),
            tags = listOf("thermique", "fourier", "conduction", "isolation", "flux", "paroi")
        ),
        EngineeringTool(
            id = "tool_therm_expansion",
            name = "Dilatation thermique linéaire",
            shortDescription = "Variation dimensionnelle ΔL, longueur finale L_f et déformation thermique ε_th d'une pièce soumise à un écart ΔT.",
            categoryId = CategoryId.THERMIQUE,
            isAvailable = true,
            releasePhase = "Phase 3 : Calculateur certifié",
            formulaPreview = "ΔL = α · L₀ · (T_f - T₀)  |  L_f = L₀ + ΔL",
            formulaExplanation = "Dilatation ou contraction thermo-élastique libre avec préréglages de coefficients α (Acier, Alu, Cuivre, Inox, Béton).",
            plannedInputs = listOf(
                PlannedInput("Longueur initiale", "L₀", "m, mm", "Cote à la température initiale"),
                PlannedInput("Coefficient de dilatation", "α", "10⁻⁶ /K", "Dilatation linéique du matériau"),
                PlannedInput("Températures initiale & finale", "T₀, T_f", "°C, K, °F", "Plage thermique")
            ),
            plannedOutputs = listOf(
                PlannedOutput("Allongement / retrait", "ΔL", "mm"),
                PlannedOutput("Longueur finale", "L_f", "m et mm"),
                PlannedOutput("Déformation", "ε_th", "µm/m")
            ),
            tags = listOf("thermique", "dilatation", "allongement", "température", "acier", "tuyauterie")
        ),

        // =====================================================================
        // GÉNIE INDUSTRIEL (Tools 10, 22, 23)
        // =====================================================================
        EngineeringTool(
            id = "tool_indus_oee",
            name = "TRS / OEE Industriel",
            shortDescription = "Calcul du Taux de Rendement Synthétique (Disponibilité × Performance × Qualité) selon la norme NF E60-182.",
            categoryId = CategoryId.INDUSTRIEL,
            isAvailable = true,
            releasePhase = "Phase 2 : Calculateur certifié",
            formulaPreview = "TRS = Disponibilité × Performance × Qualité",
            formulaExplanation = "Indicateur d'efficacité des équipements de production (TPM) avec alertes sur dépassement de cadence théorique.",
            plannedInputs = listOf(
                PlannedInput("Temps planifié", "T_p", "heures", "Temps d'ouverture programmé"),
                PlannedInput("Temps de marche réel", "T_f", "heures", "Temps réel sans arrêts"),
                PlannedInput("Temps de cycle idéal", "t_cycle", "secondes", "Cadence théorique de référence"),
                PlannedInput("Production totale", "N_tot", "pièces", "Quantité totale produite"),
                PlannedInput("Production conforme", "N_bon", "pièces", "Pièces bonnes sans rebuts")
            ),
            plannedOutputs = listOf(
                PlannedOutput("TRS global", "OEE", "%"),
                PlannedOutput("Disponibilité", "A", "%"),
                PlannedOutput("Performance", "P", "%"),
                PlannedOutput("Qualité", "Q", "%")
            ),
            tags = listOf("industriel", "trs", "oee", "productivité", "lean", "disponibilité", "qualité")
        ),
        EngineeringTool(
            id = "tool_indus_takt",
            name = "Calculateur de Takt Time",
            shortDescription = "Détermine le Takt Time (s/unité et min/unité) et la cadence horaire cible à partir du temps net disponible et de la demande client.",
            categoryId = CategoryId.INDUSTRIEL,
            isAvailable = true,
            releasePhase = "Phase 3 : Calculateur certifié",
            formulaPreview = "Takt = (T_ouverture - T_pauses) / Demande_client",
            formulaExplanation = "Synchronisation Lean Manufacturing du rythme de production sur le besoin client réel.",
            plannedInputs = listOf(
                PlannedInput("Temps d'ouverture", "T_ouv", "min", "Durée totale du poste"),
                PlannedInput("Pauses planifiées", "T_pauses", "min", "Arrêts programmés"),
                PlannedInput("Demande client", "D", "unités", "Volume à livrer sur la période")
            ),
            plannedOutputs = listOf(
                PlannedOutput("Takt Time", "Takt", "s/unité et min/unité"),
                PlannedOutput("Cadence cible", "Cad", "unités/h")
            ),
            tags = listOf("industriel", "takt time", "lean", "cadence", "production", "flux")
        ),
        EngineeringTool(
            id = "tool_indus_capacity",
            name = "Capacité de production",
            shortDescription = "Calcule la capacité théorique maximale et la capacité réelle ajustée (Disponibilité × Performance) pour N machines en parallèle.",
            categoryId = CategoryId.INDUSTRIEL,
            isAvailable = true,
            releasePhase = "Phase 3 : Calculateur certifié",
            formulaPreview = "C_théo = N · (T / t_cycle)  |  C_adj = C_théo · A · P",
            formulaExplanation = "Dimensionnement capacitaire d'atelier multi-postes avec prise en compte des pertes d'exploitation.",
            plannedInputs = listOf(
                PlannedInput("Temps d'ouverture & Cycle", "T, t_c", "h, s", "Paramètres temporels"),
                PlannedInput("Postes en parallèle", "N", "machines", "Nombre d'équipements identiques"),
                PlannedInput("Disponibilité & Performance", "A, P", "%", "Facteurs de pondération")
            ),
            plannedOutputs = listOf(
                PlannedOutput("Capacité théorique", "C_théo", "unités"),
                PlannedOutput("Capacité ajustée", "C_adj", "unités"),
                PlannedOutput("Débit horaire", "Q_h", "unités/h")
            ),
            tags = listOf("industriel", "capacité", "production", "machines", "cadence", "atelier")
        ),

        // =====================================================================
        // GÉNIE CHIMIQUE (Tools 24, 25)
        // =====================================================================
        EngineeringTool(
            id = "tool_chem_molarity",
            name = "Concentration molaire (Molarité)",
            shortDescription = "Calcule la concentration molaire C (mol/L, mmol/L), la quantité de matière n et la concentration massique (g/L).",
            categoryId = CategoryId.CHIMIQUE,
            isAvailable = true,
            releasePhase = "Phase 3 : Calculateur certifié",
            formulaPreview = "n = m / M  |  C = n / V",
            formulaExplanation = "Préparation de solutions chimiques à partir de la masse pesée et de la masse molaire ou directement en moles.",
            plannedInputs = listOf(
                PlannedInput("Masse ou Moles de soluté", "m ou n", "g/mg/kg ou mol", "Quantité de soluté"),
                PlannedInput("Masse molaire", "M", "g/mol", "Masse moléculaire"),
                PlannedInput("Volume de solution", "V", "mL, L, m³", "Volume final jaugé")
            ),
            plannedOutputs = listOf(
                PlannedOutput("Molarité", "C", "mol/L (M)"),
                PlannedOutput("Concentration massique", "C_m", "g/L"),
                PlannedOutput("Quantité de matière", "n", "mol")
            ),
            tags = listOf("chimique", "molarité", "concentration", "mole", "solution", "chimie")
        ),
        EngineeringTool(
            id = "tool_chem_dilution",
            name = "Dilution de solution (C₁V₁ = C₂V₂)",
            shortDescription = "Résout n'importe quelle variable de l'équation de dilution C₁·V₁ = C₂·V₂ et calcule le volume de solvant à ajouter.",
            categoryId = CategoryId.CHIMIQUE,
            isAvailable = true,
            releasePhase = "Phase 3 : Calculateur certifié",
            formulaPreview = "C₁ · V₁ = C₂ · V₂  |  V_solvant = V₂ - V₁",
            formulaExplanation = "Bilan de conservation de matière du soluté avec alerte si C₂ > C₁ (évaporation/concentration).",
            plannedInputs = listOf(
                PlannedInput("Inconnue à résoudre", "V₁, C₂, V₂ ou C₁", "-", "Variable cible"),
                PlannedInput("Paramètres connus", "C, V", "mol/L, mL", "3 grandeurs connues sur 4")
            ),
            plannedOutputs = listOf(
                PlannedOutput("Grandeur calculée", "V₁ / C₂ / V₂ / C₁", "Unité choisie"),
                PlannedOutput("Volume de solvant à ajouter", "V_eau", "mL / L"),
                PlannedOutput("Facteur de dilution", "F", "×")
            ),
            tags = listOf("chimique", "dilution", "solution", "c1v1", "laboratoire", "chimie")
        ),

        // =====================================================================
        // INFORMATIQUE (Tools 26, 27)
        // =====================================================================
        EngineeringTool(
            id = "tool_cs_base_converter",
            name = "Convertisseur de bases (2/8/10/16)",
            shortDescription = "Conversion entière exacte 64 bits entre Binaire (Base 2), Octal (Base 8), Décimal (Base 10) et Hexadécimal (Base 16).",
            categoryId = CategoryId.INFORMATIQUE,
            isAvailable = true,
            releasePhase = "Phase 3 : Calculateur certifié",
            formulaPreview = "N = ∑ d_k · b^k  (Arithmétique entière 64 bits)",
            formulaExplanation = "Décodage et transcodage entier signé 64 bits sans erreur d'arrondi flottant avec validation stricte des chiffres par base.",
            plannedInputs = listOf(
                PlannedInput("Base d'origine", "2, 8, 10 ou 16", "-", "Système de numération source"),
                PlannedInput("Mot numérique", "N", "Texte", "Valeur entière à convertir")
            ),
            plannedOutputs = listOf(
                PlannedOutput("Décimal", "Base 10", "-"),
                PlannedOutput("Hexadécimal", "Base 16", "0x..."),
                PlannedOutput("Binaire", "Base 2", "0b..."),
                PlannedOutput("Octal", "Base 8", "0o...")
            ),
            tags = listOf("informatique", "binaire", "hexadécimal", "octal", "décimal", "base")
        ),
        EngineeringTool(
            id = "tool_cs_boolean_logic",
            name = "Portes logiques & Table de vérité",
            shortDescription = "Évaluation d'opérateurs booléens (AND, OR, NOT, XOR, NAND, NOR) avec table de vérité complète interactive.",
            categoryId = CategoryId.INFORMATIQUE,
            isAvailable = true,
            releasePhase = "Phase 3 : Calculateur certifié",
            formulaPreview = "AND: A·B | OR: A+B | XOR: A⊕B | NAND | NOR | NOT",
            formulaExplanation = "Logique combinatoire binaire avec mise en évidence de la ligne active dans la table de vérité.",
            plannedInputs = listOf(
                PlannedInput("Opérateur logique", "Porte", "-", "AND, OR, NOT, XOR, NAND, NOR"),
                PlannedInput("États d'entrée", "A, B", "0 / 1", "Niveaux logiques d'entrée")
            ),
            plannedOutputs = listOf(
                PlannedOutput("Sortie logique", "Y", "0 (FALSE) / 1 (TRUE)"),
                PlannedOutput("Table de vérité", "Table", "Toutes les combinaisons")
            ),
            tags = listOf("informatique", "logique", "booléen", "and", "xor", "nand", "table de vérité")
        ),

        // =====================================================================
        // AUTOMATISME (Tools 28, 29)
        // =====================================================================
        EngineeringTool(
            id = "tool_auto_analog_scaling",
            name = "Mise à l'échelle de signal analogique",
            shortDescription = "Interpolation linéaire universelle Y = Y_min + (X - X_min)(Y_max - Y_min)/(X_max - X_min) pour entrées API/PLC.",
            categoryId = CategoryId.AUTOMATISME,
            isAvailable = true,
            releasePhase = "Phase 3 : Calculateur certifié",
            formulaPreview = "Y = Y_min + [(X - X_min) / (X_max - X_min)] · (Y_max - Y_min)",
            formulaExplanation = "Conversion entre signal brut (0–10 V, 0–27648 pts CAN, etc.) et grandeur physique avec support des échelles inversées.",
            plannedInputs = listOf(
                PlannedInput("Signal d'entrée", "X", "V, mA, pts", "Valeur brute mesurée"),
                PlannedInput("Plage d'entrée", "X_min .. X_max", "-", "Bornes du signal d'entrée"),
                PlannedInput("Plage physique", "Y_min .. Y_max", "Unité ing.", "Échelle du capteur")
            ),
            plannedOutputs = listOf(
                PlannedOutput("Grandeur physique", "Y", "Unité ing."),
                PlannedOutput("Pourcentage d'étendue", "Span%", "%"),
                PlannedOutput("Gain & Offset", "a, b", "Y = a·X + b")
            ),
            tags = listOf("automatisme", "scaling", "mise à l'échelle", "analogique", "plc", "api", "0-10v")
        ),
        EngineeringTool(
            id = "tool_auto_sensor_scale",
            name = "Boucle de courant 4–20 mA",
            shortDescription = "Conversion dédiée 4–20 mA vers grandeur physique et % d'échelle avec détection de sous-échelle (< 4 mA) et dépassement (> 20 mA).",
            categoryId = CategoryId.AUTOMATISME,
            isAvailable = true,
            releasePhase = "Phase 3 : Calculateur certifié",
            formulaPreview = "Y = Y_min + [(I - 4) / 16] · (Y_max - Y_min)",
            formulaExplanation = "Instrumentation industrielle à zéro vivant 4 mA permettant de distinguer un signal 0% (4 mA) d'une coupure de boucle (0 mA).",
            plannedInputs = listOf(
                PlannedInput("Courant mesuré", "I", "mA", "Courant de boucle"),
                PlannedInput("Échelle physique", "Y_min, Y_max", "bar, °C, m³/h", "Valeurs à 4 mA et 20 mA")
            ),
            plannedOutputs = listOf(
                PlannedOutput("Mesure physique", "Y", "Unité"),
                PlannedOutput("Pourcentage de plage", "Span", "%"),
                PlannedOutput("Diagnostic signal", "Statut", "Nominal / Hors plage")
            ),
            tags = listOf("automatisme", "capteur", "4-20ma", "boucle de courant", "transmetteur", "instrumentation")
        ),

        // =====================================================================
        // ENVIRONNEMENT (Tool 30)
        // =====================================================================
        EngineeringTool(
            id = "tool_env_co2",
            name = "Énergie & Émissions CO₂e",
            shortDescription = "Calcule l'énergie électrique consommée E = P·t (kWh, MWh, MJ) et les émissions de gaz à effet de serre (kg CO₂e, t CO₂e).",
            categoryId = CategoryId.ENVIRONNEMENT,
            isAvailable = true,
            releasePhase = "Phase 3 : Calculateur certifié",
            formulaPreview = "E [kWh] = P [kW] · t [h]  |  CO₂e = E · Facteur_émission",
            formulaExplanation = "Bilan énergétique et carbone basé sur un facteur d'émission paramétrable selon le mix électrique national ou local.",
            plannedInputs = listOf(
                PlannedInput("Puissance appelée", "P", "W, kW, MW, ch", "Puissance moyenne de l'équipement"),
                PlannedInput("Durée d'exploitation", "t", "h, min, j", "Temps de fonctionnement"),
                PlannedInput("Facteur d'émission", "FE", "kg CO₂e/kWh", "Intensité carbone du réseau")
            ),
            plannedOutputs = listOf(
                PlannedOutput("Énergie consommée", "E", "kWh, MWh, MJ"),
                PlannedOutput("Émissions carbone", "CO₂e", "kg CO₂e et t CO₂e")
            ),
            tags = listOf("environnement", "carbone", "co2", "énergie", "kwh", "bilan carbone")
        )
    )

    private val categoryByIdMap: Map<CategoryId, EngineeringCategory> =
        categories.associateBy { it.id }

    private val toolsByCategoryMap: Map<CategoryId, List<EngineeringTool>> =
        tools.groupBy { it.categoryId }

    private val toolByIdMap: Map<String, EngineeringTool> =
        tools.associateBy { it.id }

    fun getCategoryById(id: CategoryId): EngineeringCategory? =
        categoryByIdMap[id]

    fun getToolsByCategory(categoryId: CategoryId): List<EngineeringTool> =
        toolsByCategoryMap[categoryId] ?: emptyList()

    fun getToolById(id: String): EngineeringTool? =
        toolByIdMap[id]
}
