package com.example.feature.calculator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.calculators.ConcreteElementType
import com.example.domain.calculators.ConcreteVolumeCalculator
import com.example.domain.calculators.DistributedLoadCalculator
import com.example.domain.calculators.HeatConductionCalculator
import com.example.domain.calculators.HydraulicCylinderFlowCalculator
import com.example.domain.calculators.HydraulicCylinderForceCalculator
import com.example.domain.calculators.HydraulicSolveTarget
import com.example.domain.calculators.ThermalExpansionCalculator
import com.example.domain.models.CalculationResult
import com.example.domain.models.EngineOutcome
import com.example.domain.units.UnitDimension
import com.example.domain.units.UnitRegistry
import com.example.feature.calculator.common.EngineeringInputField
import com.example.feature.calculator.common.ParamWithConvertibleUnit
import com.example.feature.calculator.common.ResultCard
import com.example.ui.theme.PrimaryAccent

// ============================================================================
// TOOL 16 SCREEN — CONCRETE VOLUME
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConcreteVolumeScreen(
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var elementType by remember { mutableStateOf(ConcreteElementType.RECTANGULAR_SLAB) }
    var lengthUnit by remember { mutableStateOf(UnitRegistry.M) }
    var dim1Text by remember { mutableStateOf("8.0") } // L or D
    var dim2Text by remember { mutableStateOf("5.0") } // l or H
    var dim3Text by remember { mutableStateOf("0.20") } // e or h
    var allowanceText by remember { mutableStateOf("5.0") }
    var countText by remember { mutableStateOf("1") }

    var calculationResult by remember { mutableStateOf<CalculationResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun calculate() {
        val d1 = dim1Text.replace(',', '.').toDoubleOrNull()
        val d2 = dim2Text.replace(',', '.').toDoubleOrNull()
        val d3 = dim3Text.replace(',', '.').toDoubleOrNull() ?: 1.0
        val allow = allowanceText.replace(',', '.').toDoubleOrNull() ?: 0.0
        val count = countText.trim().toIntOrNull() ?: 1

        if (d1 == null || d2 == null) {
            errorMessage = "Veuillez saisir des dimensions numériques valides."
            return
        }

        when (val outcome = ConcreteVolumeCalculator.calculate(elementType, d1, d2, d3, lengthUnit, allow, count)) {
            is EngineOutcome.Success -> {
                calculationResult = outcome.result
                errorMessage = null
            }
            is EngineOutcome.Failure -> {
                errorMessage = outcome.error
            }
        }
    }

    remember { calculate(); true }

    Column(modifier = modifier.fillMaxSize().testTag("concrete_volume_screen")) {
        TopAppBar(
            title = { Text("Cubature & Volume de béton", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                }
            },
            actions = {
                IconButton(onClick = onFavoriteToggle) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Favori",
                        tint = if (isFavorite) PrimaryAccent else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text("Type d'ouvrage en béton", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ConcreteElementType.entries.forEach { type ->
                        FilterChip(
                            selected = elementType == type,
                            onClick = { elementType = type; calculate() },
                            label = { Text(type.displayName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryAccent.copy(alpha = 0.2f),
                                selectedLabelColor = PrimaryAccent
                            )
                        )
                    }
                }
            }

            if (elementType == ConcreteElementType.CYLINDRICAL_FOUNDATION) {
                item {
                    ParamWithConvertibleUnit(
                        label = "Diamètre de la fondation / pieu (D)",
                        valueText = dim1Text,
                        onValueChange = { dim1Text = it },
                        currentUnit = lengthUnit,
                        availableUnits = UnitRegistry.getUnitsForDimension(UnitDimension.LENGTH),
                        onUnitChanged = { lengthUnit = it }
                    )
                }
                item {
                    EngineeringInputField(
                        label = "Profondeur / Hauteur (H)",
                        valueText = dim2Text,
                        onValueChange = { dim2Text = it },
                        unit = lengthUnit.symbol
                    )
                }
            } else {
                item {
                    ParamWithConvertibleUnit(
                        label = "Longueur (L)",
                        valueText = dim1Text,
                        onValueChange = { dim1Text = it },
                        currentUnit = lengthUnit,
                        availableUnits = UnitRegistry.getUnitsForDimension(UnitDimension.LENGTH),
                        onUnitChanged = { lengthUnit = it }
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        EngineeringInputField("Largeur (l)", dim2Text, { dim2Text = it }, modifier = Modifier.weight(1f), unit = lengthUnit.symbol)
                        EngineeringInputField("Épaisseur / Hauteur (h)", dim3Text, { dim3Text = it }, modifier = Modifier.weight(1f), unit = lengthUnit.symbol)
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EngineeringInputField("Marge de perte (%)", allowanceText, { allowanceText = it }, modifier = Modifier.weight(1f), unit = "%", helperText = "Usager: 5% à 10%")
                    EngineeringInputField("Nombre d'éléments", countText, { countText = it }, modifier = Modifier.weight(1f), unit = "unités")
                }
            }

            item {
                ActionButtonsRow(
                    onReset = {
                        dim1Text = ""
                        dim2Text = ""
                        dim3Text = ""
                        calculationResult = null
                        errorMessage = null
                    },
                    onCalculate = ::calculate
                )
            }

            if (errorMessage != null) {
                item { Text(text = errorMessage ?: "", color = MaterialTheme.colorScheme.error) }
            }

            calculationResult?.let { res ->
                item { ResultCard(result = res) }
            }
        }
    }
}

// ============================================================================
// TOOL 17 SCREEN — DISTRIBUTED LOAD
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DistributedLoadScreen(
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var loadIntensityText by remember { mutableStateOf("12.5") }
    var isKnPerM by remember { mutableStateOf(true) }
    var lengthText by remember { mutableStateOf("6.0") }
    var lengthUnit by remember { mutableStateOf(UnitRegistry.M) }

    var calculationResult by remember { mutableStateOf<CalculationResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun calculate() {
        val q = loadIntensityText.replace(',', '.').toDoubleOrNull()
        val l = lengthText.replace(',', '.').toDoubleOrNull()

        if (q == null || l == null) {
            errorMessage = "Veuillez saisir des valeurs valides pour l'intensité q et la longueur L."
            return
        }

        when (val outcome = DistributedLoadCalculator.calculate(q, isKnPerM, l, lengthUnit)) {
            is EngineOutcome.Success -> {
                calculationResult = outcome.result
                errorMessage = null
            }
            is EngineOutcome.Failure -> {
                errorMessage = outcome.error
            }
        }
    }

    remember { calculate(); true }

    Column(modifier = modifier.fillMaxSize().testTag("distributed_load_screen")) {
        TopAppBar(
            title = { Text("Charge répartie uniforme", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                }
            },
            actions = {
                IconButton(onClick = onFavoriteToggle) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Favori",
                        tint = if (isFavorite) PrimaryAccent else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = isKnPerM,
                        onClick = { isKnPerM = true; calculate() },
                        label = { Text("Charge q en kN/m") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryAccent.copy(alpha = 0.2f),
                            selectedLabelColor = PrimaryAccent
                        )
                    )
                    FilterChip(
                        selected = !isKnPerM,
                        onClick = { isKnPerM = false; calculate() },
                        label = { Text("Charge q en N/m") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryAccent.copy(alpha = 0.2f),
                            selectedLabelColor = PrimaryAccent
                        )
                    )
                }
            }

            item {
                EngineeringInputField(
                    label = "Intensité de la charge linéique (q)",
                    valueText = loadIntensityText,
                    onValueChange = { loadIntensityText = it },
                    unit = if (isKnPerM) "kN/m" else "N/m"
                )
            }

            item {
                ParamWithConvertibleUnit(
                    label = "Longueur du segment chargé (L)",
                    valueText = lengthText,
                    onValueChange = { lengthText = it },
                    currentUnit = lengthUnit,
                    availableUnits = UnitRegistry.getUnitsForDimension(UnitDimension.LENGTH),
                    onUnitChanged = { lengthUnit = it }
                )
            }

            item {
                ActionButtonsRow(
                    onReset = {
                        loadIntensityText = ""
                        lengthText = ""
                        calculationResult = null
                        errorMessage = null
                    },
                    onCalculate = ::calculate
                )
            }

            if (errorMessage != null) {
                item { Text(text = errorMessage ?: "", color = MaterialTheme.colorScheme.error) }
            }

            calculationResult?.let { res ->
                item { ResultCard(result = res) }
            }
        }
    }
}

// ============================================================================
// TOOL 18 SCREEN — HYDRAULIC CYLINDER FORCE
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HydraulicCylinderForceScreen(
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pressureText by remember { mutableStateOf("180") }
    var pressureUnit by remember { mutableStateOf(UnitRegistry.BAR) }
    var pistonDiamText by remember { mutableStateOf("80") }
    var rodDiamText by remember { mutableStateOf("45") }
    var diamUnit by remember { mutableStateOf(UnitRegistry.MM) }
    var efficiencyText by remember { mutableStateOf("0.92") }

    var calculationResult by remember { mutableStateOf<CalculationResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun calculate() {
        val p = pressureText.replace(',', '.').toDoubleOrNull()
        val d = pistonDiamText.replace(',', '.').toDoubleOrNull()
        val rod = rodDiamText.replace(',', '.').toDoubleOrNull() ?: 0.0
        val eta = efficiencyText.replace(',', '.').toDoubleOrNull() ?: 1.0

        if (p == null || d == null) {
            errorMessage = "Veuillez renseigner la pression P et le diamètre de piston D."
            return
        }

        when (val outcome = HydraulicCylinderForceCalculator.calculate(p, pressureUnit, d, rod, diamUnit, eta)) {
            is EngineOutcome.Success -> {
                calculationResult = outcome.result
                errorMessage = null
            }
            is EngineOutcome.Failure -> {
                errorMessage = outcome.error
            }
        }
    }

    remember { calculate(); true }

    Column(modifier = modifier.fillMaxSize().testTag("hydraulic_cylinder_force_screen")) {
        TopAppBar(
            title = { Text("Force de vérin hydraulique", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                }
            },
            actions = {
                IconButton(onClick = onFavoriteToggle) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Favori",
                        tint = if (isFavorite) PrimaryAccent else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                ParamWithConvertibleUnit(
                    label = "Pression de service hydraulique (P)",
                    valueText = pressureText,
                    onValueChange = { pressureText = it },
                    currentUnit = pressureUnit,
                    availableUnits = UnitRegistry.getUnitsForDimension(UnitDimension.PRESSURE),
                    onUnitChanged = { pressureUnit = it }
                )
            }

            item {
                ParamWithConvertibleUnit(
                    label = "Diamètre d'alésage du piston (D)",
                    valueText = pistonDiamText,
                    onValueChange = { pistonDiamText = it },
                    currentUnit = diamUnit,
                    availableUnits = UnitRegistry.getUnitsForDimension(UnitDimension.LENGTH),
                    onUnitChanged = { diamUnit = it }
                )
            }

            item {
                EngineeringInputField(
                    label = "Diamètre de la tige (d)",
                    valueText = rodDiamText,
                    onValueChange = { rodDiamText = it },
                    unit = diamUnit.symbol,
                    helperText = "Doit vérifier 0 ≤ d < D"
                )
            }

            item {
                EngineeringInputField(
                    label = "Facteur de rendement hydromécanique (η)",
                    valueText = efficiencyText,
                    onValueChange = { efficiencyText = it },
                    unit = "0.0 à 1.0",
                    helperText = "1.0 = Force théorique pure | 0.90–0.95 = Rendement usuel avec joints"
                )
            }

            item {
                ActionButtonsRow(
                    onReset = {
                        pressureText = ""
                        pistonDiamText = ""
                        rodDiamText = ""
                        calculationResult = null
                        errorMessage = null
                    },
                    onCalculate = ::calculate
                )
            }

            if (errorMessage != null) {
                item { Text(text = errorMessage ?: "", color = MaterialTheme.colorScheme.error) }
            }

            calculationResult?.let { res ->
                item { ResultCard(result = res) }
            }
        }
    }
}

// ============================================================================
// TOOL 19 SCREEN — HYDRAULIC CYLINDER FLOW AND SPEED
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HydraulicCylinderFlowScreen(
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var solveTarget by remember { mutableStateOf(HydraulicSolveTarget.SOLVE_VELOCITY_FROM_FLOW) }
    var pistonDiamMmText by remember { mutableStateOf("80") }
    var rodDiamMmText by remember { mutableStateOf("45") }
    var knownValText by remember { mutableStateOf("35") } // 35 L/min or 0.15 m/s

    var calculationResult by remember { mutableStateOf<CalculationResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun calculate() {
        val d = pistonDiamMmText.replace(',', '.').toDoubleOrNull()
        val rod = rodDiamMmText.replace(',', '.').toDoubleOrNull() ?: 0.0
        val kVal = knownValText.replace(',', '.').toDoubleOrNull()

        if (d == null || kVal == null) {
            errorMessage = "Veuillez renseigner le diamètre du piston et la grandeur connue."
            return
        }

        when (val outcome = HydraulicCylinderFlowCalculator.calculate(solveTarget, d, rod, kVal)) {
            is EngineOutcome.Success -> {
                calculationResult = outcome.result
                errorMessage = null
            }
            is EngineOutcome.Failure -> {
                errorMessage = outcome.error
            }
        }
    }

    remember { calculate(); true }

    Column(modifier = modifier.fillMaxSize().testTag("hydraulic_cylinder_flow_screen")) {
        TopAppBar(
            title = { Text("Débit & Vitesse de vérin", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                }
            },
            actions = {
                IconButton(onClick = onFavoriteToggle) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Favori",
                        tint = if (isFavorite) PrimaryAccent else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    HydraulicSolveTarget.entries.forEach { target ->
                        FilterChip(
                            selected = solveTarget == target,
                            onClick = {
                                solveTarget = target
                                knownValText = if (target == HydraulicSolveTarget.SOLVE_VELOCITY_FROM_FLOW) "35" else "0.15"
                                calculate()
                            },
                            label = { Text(target.displayName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryAccent.copy(alpha = 0.2f),
                                selectedLabelColor = PrimaryAccent
                            )
                        )
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EngineeringInputField("Diamètre piston (D)", pistonDiamMmText, { pistonDiamMmText = it }, modifier = Modifier.weight(1f), unit = "mm")
                    EngineeringInputField("Diamètre tige (d)", rodDiamMmText, { rodDiamMmText = it }, modifier = Modifier.weight(1f), unit = "mm")
                }
            }

            item {
                EngineeringInputField(
                    label = if (solveTarget == HydraulicSolveTarget.SOLVE_VELOCITY_FROM_FLOW) "Débit d'alimentation pompe (Q)" else "Vitesse souhaitée du vérin (v)",
                    valueText = knownValText,
                    onValueChange = { knownValText = it },
                    unit = if (solveTarget == HydraulicSolveTarget.SOLVE_VELOCITY_FROM_FLOW) "L/min" else "m/s"
                )
            }

            item {
                ActionButtonsRow(
                    onReset = {
                        pistonDiamMmText = ""
                        rodDiamMmText = ""
                        knownValText = ""
                        calculationResult = null
                        errorMessage = null
                    },
                    onCalculate = ::calculate
                )
            }

            if (errorMessage != null) {
                item { Text(text = errorMessage ?: "", color = MaterialTheme.colorScheme.error) }
            }

            calculationResult?.let { res ->
                item { ResultCard(result = res) }
            }
        }
    }
}

// ============================================================================
// TOOL 20 SCREEN — PLANE WALL HEAT CONDUCTION
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeatConductionScreen(
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var kText by remember { mutableStateOf("0.04") } // Laine minérale 0.04, Béton 1.7, Acier 50
    var areaText by remember { mutableStateOf("15.0") }
    var areaUnit by remember { mutableStateOf(UnitRegistry.M2) }
    var thicknessText by remember { mutableStateOf("100") }
    var thicknessUnit by remember { mutableStateOf(UnitRegistry.MM) }
    var deltaTText by remember { mutableStateOf("20.0") }

    var calculationResult by remember { mutableStateOf<CalculationResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun calculate() {
        val k = kText.replace(',', '.').toDoubleOrNull()
        val a = areaText.replace(',', '.').toDoubleOrNull()
        val l = thicknessText.replace(',', '.').toDoubleOrNull()
        val dt = deltaTText.replace(',', '.').toDoubleOrNull()

        if (k == null || a == null || l == null || dt == null) {
            errorMessage = "Veuillez renseigner tous les paramètres thermiques (k, A, L, ΔT)."
            return
        }

        when (val outcome = HeatConductionCalculator.calculate(k, a, areaUnit, l, thicknessUnit, dt)) {
            is EngineOutcome.Success -> {
                calculationResult = outcome.result
                errorMessage = null
            }
            is EngineOutcome.Failure -> {
                errorMessage = outcome.error
            }
        }
    }

    remember { calculate(); true }

    Column(modifier = modifier.fillMaxSize().testTag("heat_conduction_screen")) {
        TopAppBar(
            title = { Text("Conduction thermique plane", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                }
            },
            actions = {
                IconButton(onClick = onFavoriteToggle) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Favori",
                        tint = if (isFavorite) PrimaryAccent else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                EngineeringInputField(
                    label = "Conductivité thermique du matériau (k / λ)",
                    valueText = kText,
                    onValueChange = { kText = it },
                    unit = "W/(m·K)",
                    helperText = "Isolant: 0.035 | Béton: 1.75 | Acier: 50 | Cuivre: 390"
                )
            }

            item {
                ParamWithConvertibleUnit(
                    label = "Surface de la paroi (A)",
                    valueText = areaText,
                    onValueChange = { areaText = it },
                    currentUnit = areaUnit,
                    availableUnits = UnitRegistry.getUnitsForDimension(UnitDimension.AREA),
                    onUnitChanged = { areaUnit = it }
                )
            }

            item {
                ParamWithConvertibleUnit(
                    label = "Épaisseur de la paroi (L)",
                    valueText = thicknessText,
                    onValueChange = { thicknessText = it },
                    currentUnit = thicknessUnit,
                    availableUnits = UnitRegistry.getUnitsForDimension(UnitDimension.LENGTH),
                    onUnitChanged = { thicknessUnit = it }
                )
            }

            item {
                EngineeringInputField(
                    label = "Écart de température entre faces (ΔT)",
                    valueText = deltaTText,
                    onValueChange = { deltaTText = it },
                    unit = "K ou °C"
                )
            }

            item {
                ActionButtonsRow(
                    onReset = {
                        kText = ""
                        areaText = ""
                        thicknessText = ""
                        deltaTText = ""
                        calculationResult = null
                        errorMessage = null
                    },
                    onCalculate = ::calculate
                )
            }

            if (errorMessage != null) {
                item { Text(text = errorMessage ?: "", color = MaterialTheme.colorScheme.error) }
            }

            calculationResult?.let { res ->
                item { ResultCard(result = res) }
            }
        }
    }
}

// ============================================================================
// TOOL 21 SCREEN — THERMAL EXPANSION
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThermalExpansionScreen(
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var l0Text by remember { mutableStateOf("10.0") }
    var lengthUnit by remember { mutableStateOf(UnitRegistry.M) }
    var alphaMicronsText by remember { mutableStateOf("12.0") } // Acier: 12, Alu: 23, Cuivre: 16.5
    var deltaTText by remember { mutableStateOf("45.0") }

    var calculationResult by remember { mutableStateOf<CalculationResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun calculate() {
        val l0 = l0Text.replace(',', '.').toDoubleOrNull()
        val alpha = alphaMicronsText.replace(',', '.').toDoubleOrNull()
        val dt = deltaTText.replace(',', '.').toDoubleOrNull()

        if (l0 == null || alpha == null || dt == null) {
            errorMessage = "Veuillez renseigner L₀, α et ΔT."
            return
        }

        when (val outcome = ThermalExpansionCalculator.calculate(l0, lengthUnit, alpha, dt)) {
            is EngineOutcome.Success -> {
                calculationResult = outcome.result
                errorMessage = null
            }
            is EngineOutcome.Failure -> {
                errorMessage = outcome.error
            }
        }
    }

    remember { calculate(); true }

    Column(modifier = modifier.fillMaxSize().testTag("thermal_expansion_screen")) {
        TopAppBar(
            title = { Text("Dilatation thermique linéaire", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                }
            },
            actions = {
                IconButton(onClick = onFavoriteToggle) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Favori",
                        tint = if (isFavorite) PrimaryAccent else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                ParamWithConvertibleUnit(
                    label = "Longueur initiale à froid (L₀)",
                    valueText = l0Text,
                    onValueChange = { l0Text = it },
                    currentUnit = lengthUnit,
                    availableUnits = UnitRegistry.getUnitsForDimension(UnitDimension.LENGTH),
                    onUnitChanged = { lengthUnit = it }
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Acier (12)" to "12.0", "Alu (23)" to "23.0", "Cuivre (16.5)" to "16.5").forEach { (label, valStr) ->
                        FilterChip(
                            selected = alphaMicronsText == valStr,
                            onClick = { alphaMicronsText = valStr; calculate() },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryAccent.copy(alpha = 0.2f),
                                selectedLabelColor = PrimaryAccent
                            )
                        )
                    }
                }
                EngineeringInputField(
                    label = "Coefficient de dilatation linéaire (α)",
                    valueText = alphaMicronsText,
                    onValueChange = { alphaMicronsText = it },
                    unit = "×10⁻⁶ K⁻¹",
                    helperText = "En μm/(m·K) : Acier=12, Inox=16, Alu=23, PVC=80"
                )
            }

            item {
                EngineeringInputField(
                    label = "Variation de température (ΔT = T_f - T_0)",
                    valueText = deltaTText,
                    onValueChange = { deltaTText = it },
                    unit = "°C ou K",
                    helperText = "Positif (+) pour chauffage, négatif (-) pour refroidissement"
                )
            }

            item {
                ActionButtonsRow(
                    onReset = {
                        l0Text = ""
                        deltaTText = ""
                        calculationResult = null
                        errorMessage = null
                    },
                    onCalculate = ::calculate
                )
            }

            if (errorMessage != null) {
                item { Text(text = errorMessage ?: "", color = MaterialTheme.colorScheme.error) }
            }

            calculationResult?.let { res ->
                item { ResultCard(result = res) }
            }
        }
    }
}
