package com.example.feature.calculator

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.calculators.EquivalentResistanceCalculator
import com.example.domain.calculators.RcTimeConstantCalculator
import com.example.domain.calculators.ResistanceConnectionMode
import com.example.domain.calculators.ShaftTorsionCalculator
import com.example.domain.calculators.VoltageDividerCalculator
import com.example.domain.calculators.VoltageDropCalculator
import com.example.domain.calculators.VoltageDropCircuitType
import com.example.domain.models.CalculationResult
import com.example.domain.models.EngineOutcome
import com.example.domain.units.UnitDimension
import com.example.domain.units.UnitRegistry
import com.example.feature.calculator.common.EngineeringInputField
import com.example.feature.calculator.common.ParamWithConvertibleUnit
import com.example.feature.calculator.common.ResultCard
import com.example.ui.theme.PrimaryAccent
import com.example.ui.theme.SecondaryAccent
import kotlin.math.exp

// ============================================================================
// TOOL 11 SCREEN — CIRCULAR SHAFT TORSION
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShaftTorsionScreen(
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var torqueText by remember { mutableStateOf("500") }
    var torqueUnit by remember { mutableStateOf(UnitRegistry.NM) }
    var lengthText by remember { mutableStateOf("1.2") }
    var lengthUnit by remember { mutableStateOf(UnitRegistry.M) }
    var outerDiamText by remember { mutableStateOf("50") }
    var innerDiamText by remember { mutableStateOf("0") }
    var diamUnit by remember { mutableStateOf(UnitRegistry.MM) }
    var shearModulusGpaText by remember { mutableStateOf("81") } // Acier ~81 GPa

    var calculationResult by remember { mutableStateOf<CalculationResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun calculate() {
        val t = torqueText.replace(',', '.').toDoubleOrNull()
        val l = lengthText.replace(',', '.').toDoubleOrNull()
        val dOut = outerDiamText.replace(',', '.').toDoubleOrNull()
        val dIn = innerDiamText.replace(',', '.').toDoubleOrNull() ?: 0.0
        val g = shearModulusGpaText.replace(',', '.').toDoubleOrNull()

        if (t == null || l == null || dOut == null || g == null) {
            errorMessage = "Veuillez renseigner des valeurs numériques valides pour T, L, D et G."
            return
        }

        when (val outcome = ShaftTorsionCalculator.calculate(t, torqueUnit, l, lengthUnit, dOut, dIn, diamUnit, g)) {
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

    Column(modifier = modifier.fillMaxSize().testTag("shaft_torsion_screen")) {
        TopAppBar(
            title = { Text("Torsion d'arbre circulaire", fontWeight = FontWeight.Bold) },
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Cisaillement torsional τ_max et angle de torsion θ",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Arbre plein (d = 0) ou arbre creux (D > d > 0). Calcule le moment polaire J = π(D⁴ - d⁴)/32, la contrainte tangentielle maximale et la déformation angulaire.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                ParamWithConvertibleUnit(
                    label = "Couple de torsion appliqué (T)",
                    valueText = torqueText,
                    onValueChange = { torqueText = it },
                    currentUnit = torqueUnit,
                    availableUnits = UnitRegistry.getUnitsForDimension(UnitDimension.TORQUE),
                    onUnitChanged = { torqueUnit = it }
                )
            }

            item {
                ParamWithConvertibleUnit(
                    label = "Longueur de l'arbre sollicité (L)",
                    valueText = lengthText,
                    onValueChange = { lengthText = it },
                    currentUnit = lengthUnit,
                    availableUnits = UnitRegistry.getUnitsForDimension(UnitDimension.LENGTH),
                    onUnitChanged = { lengthUnit = it }
                )
            }

            item {
                ParamWithConvertibleUnit(
                    label = "Diamètre extérieur (D)",
                    valueText = outerDiamText,
                    onValueChange = { outerDiamText = it },
                    currentUnit = diamUnit,
                    availableUnits = UnitRegistry.getUnitsForDimension(UnitDimension.LENGTH),
                    onUnitChanged = { diamUnit = it }
                )
            }

            item {
                EngineeringInputField(
                    label = "Diamètre intérieur (d — 0 pour arbre plein)",
                    valueText = innerDiamText,
                    onValueChange = { innerDiamText = it },
                    unit = diamUnit.symbol,
                    helperText = "Doit vérifier 0 ≤ d < D"
                )
            }

            item {
                EngineeringInputField(
                    label = "Module de cisaillement de Coulomb (G)",
                    valueText = shearModulusGpaText,
                    onValueChange = { shearModulusGpaText = it },
                    unit = "GPa",
                    helperText = "Acier: ~81 GPa | Aluminium: ~26 GPa | Cuivre: ~45 GPa"
                )
            }

            item {
                ActionButtonsRow(
                    onReset = {
                        torqueText = ""
                        lengthText = ""
                        outerDiamText = ""
                        innerDiamText = "0"
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
// TOOL 12 SCREEN — VOLTAGE DROP
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoltageDropScreen(
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var circuitType by remember { mutableStateOf(VoltageDropCircuitType.AC_SINGLE_PHASE) }
    var currentText by remember { mutableStateOf("32") }
    var lengthText by remember { mutableStateOf("45") }
    var areaText by remember { mutableStateOf("6") }
    var resistivityText by remember { mutableStateOf("0.018") } // Cuivre 0.018, Alu 0.029
    var nominalVoltageText by remember { mutableStateOf("230") }
    var cosPhiText by remember { mutableStateOf("0.85") }

    var calculationResult by remember { mutableStateOf<CalculationResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun calculate() {
        val i = currentText.replace(',', '.').toDoubleOrNull()
        val l = lengthText.replace(',', '.').toDoubleOrNull()
        val s = areaText.replace(',', '.').toDoubleOrNull()
        val rho = resistivityText.replace(',', '.').toDoubleOrNull()
        val un = nominalVoltageText.replace(',', '.').toDoubleOrNull()
        val cosPhi = cosPhiText.replace(',', '.').toDoubleOrNull() ?: 1.0

        if (i == null || l == null || s == null || rho == null || un == null) {
            errorMessage = "Veuillez renseigner des valeurs valides pour I, L, S, ρ et Un."
            return
        }

        when (val outcome = VoltageDropCalculator.calculate(circuitType, i, l, s, rho, un, cosPhi)) {
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

    Column(modifier = modifier.fillMaxSize().testTag("voltage_drop_screen")) {
        TopAppBar(
            title = { Text("Chute de tension en ligne", fontWeight = FontWeight.Bold) },
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
                Text("Type de réseau électrique", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    VoltageDropCircuitType.entries.forEach { type ->
                        FilterChip(
                            selected = circuitType == type,
                            onClick = {
                                circuitType = type
                                if (type == VoltageDropCircuitType.AC_THREE_PHASE && nominalVoltageText == "230") {
                                    nominalVoltageText = "400"
                                } else if (type == VoltageDropCircuitType.AC_SINGLE_PHASE && nominalVoltageText == "400") {
                                    nominalVoltageText = "230"
                                }
                                calculate()
                            },
                            label = { Text(type.displayName) },
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
                    EngineeringInputField("Courant d'emploi (I)", currentText, { currentText = it }, modifier = Modifier.weight(1f), unit = "A")
                    EngineeringInputField("Longueur aller (L)", lengthText, { lengthText = it }, modifier = Modifier.weight(1f), unit = "m")
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EngineeringInputField("Section câble (S)", areaText, { areaText = it }, modifier = Modifier.weight(1f), unit = "mm²")
                    EngineeringInputField("Tension nominale (Un)", nominalVoltageText, { nominalVoltageText = it }, modifier = Modifier.weight(1f), unit = "V")
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = resistivityText == "0.018",
                        onClick = { resistivityText = "0.018"; calculate() },
                        label = { Text("Cuivre (0.018)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryAccent.copy(alpha = 0.2f),
                            selectedLabelColor = PrimaryAccent
                        )
                    )
                    FilterChip(
                        selected = resistivityText == "0.029",
                        onClick = { resistivityText = "0.029"; calculate() },
                        label = { Text("Aluminium (0.029)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryAccent.copy(alpha = 0.2f),
                            selectedLabelColor = PrimaryAccent
                        )
                    )
                }
                EngineeringInputField(
                    label = "Résistivité du conducteur (ρ)",
                    valueText = resistivityText,
                    onValueChange = { resistivityText = it },
                    unit = "Ω·mm²/m"
                )
            }

            if (circuitType != VoltageDropCircuitType.DC_TWO_WIRE) {
                item {
                    EngineeringInputField(
                        label = "Facteur de puissance (cos φ)",
                        valueText = cosPhiText,
                        onValueChange = { cosPhiText = it },
                        unit = "-",
                        helperText = "Compris entre 0.1 et 1.0"
                    )
                }
            }

            item {
                ActionButtonsRow(
                    onReset = {
                        currentText = ""
                        lengthText = ""
                        areaText = ""
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
// TOOL 13 SCREEN — SERIES AND PARALLEL RESISTANCE
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquivalentResistanceScreen(
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var mode by remember { mutableStateOf(ResistanceConnectionMode.PARALLEL) }
    var rawResistancesText by remember { mutableStateOf("100, 220, 470") }

    var calculationResult by remember { mutableStateOf<CalculationResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun calculate() {
        val list = EquivalentResistanceCalculator.parseResistances(rawResistancesText)
        if (list == null) {
            errorMessage = "Format invalide : séparez les valeurs par des virgules ou espaces (ex: 100, 220, 470)."
            return
        }
        when (val outcome = EquivalentResistanceCalculator.calculate(mode, list)) {
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

    Column(modifier = modifier.fillMaxSize().testTag("equivalent_resistance_screen")) {
        TopAppBar(
            title = { Text("Résistances Série / Parallèle", fontWeight = FontWeight.Bold) },
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
                    ResistanceConnectionMode.entries.forEach { m ->
                        FilterChip(
                            selected = mode == m,
                            onClick = { mode = m; calculate() },
                            label = { Text(m.displayName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryAccent.copy(alpha = 0.2f),
                                selectedLabelColor = PrimaryAccent
                            )
                        )
                    }
                }
            }

            item {
                Column {
                    Text(
                        text = "Valeurs des résistances R₁, R₂, ... R_n (en Ω)",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = rawResistancesText,
                        onValueChange = { rawResistancesText = it },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        shape = RoundedCornerShape(12.dp),
                        placeholder = { Text("ex: 100, 220, 470, 1000") }
                    )
                }
            }

            item {
                ActionButtonsRow(
                    onReset = {
                        rawResistancesText = ""
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
// TOOL 14 SCREEN — VOLTAGE DIVIDER
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoltageDividerScreen(
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var vinText by remember { mutableStateOf("12.0") }
    var r1Text by remember { mutableStateOf("10.0") }
    var r2Text by remember { mutableStateOf("4.7") }
    var unitSymbol by remember { mutableStateOf("kΩ") }

    var calculationResult by remember { mutableStateOf<CalculationResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun calculate() {
        val vin = vinText.replace(',', '.').toDoubleOrNull()
        val r1 = r1Text.replace(',', '.').toDoubleOrNull()
        val r2 = r2Text.replace(',', '.').toDoubleOrNull()

        if (vin == null || r1 == null || r2 == null) {
            errorMessage = "Veuillez saisir des valeurs valides pour V_in, R1 et R2."
            return
        }

        when (val outcome = VoltageDividerCalculator.calculate(vin, r1, r2, unitSymbol)) {
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

    Column(modifier = modifier.fillMaxSize().testTag("voltage_divider_screen")) {
        TopAppBar(
            title = { Text("Pont diviseur de tension", fontWeight = FontWeight.Bold) },
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
                    label = "Tension d'entrée (V_in)",
                    valueText = vinText,
                    onValueChange = { vinText = it },
                    unit = "V"
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Ω", "kΩ", "MΩ").forEach { u ->
                        FilterChip(
                            selected = unitSymbol == u,
                            onClick = { unitSymbol = u; calculate() },
                            label = { Text("Résistances en $u") },
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
                    EngineeringInputField("Résistance haute (R₁)", r1Text, { r1Text = it }, modifier = Modifier.weight(1f), unit = unitSymbol)
                    EngineeringInputField("Résistance de sortie (R₂)", r2Text, { r2Text = it }, modifier = Modifier.weight(1f), unit = unitSymbol)
                }
            }

            item {
                ActionButtonsRow(
                    onReset = {
                        vinText = ""
                        r1Text = ""
                        r2Text = ""
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
// TOOL 15 SCREEN — RC TIME CONSTANT (WITH CHARGING CURVE CANVAS)
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RcTimeConstantScreen(
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var resistanceKOhmText by remember { mutableStateOf("10") } // kOhm
    var capacitanceUfText by remember { mutableStateOf("100") } // uF
    var supplyVoltageText by remember { mutableStateOf("5.0") } // V
    var evalTimeSecText by remember { mutableStateOf("1.0") } // s

    var calculationResult by remember { mutableStateOf<CalculationResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun calculate() {
        val rKohm = resistanceKOhmText.replace(',', '.').toDoubleOrNull()
        val cUf = capacitanceUfText.replace(',', '.').toDoubleOrNull()
        val vs = supplyVoltageText.replace(',', '.').toDoubleOrNull() ?: 5.0
        val tEval = evalTimeSecText.replace(',', '.').toDoubleOrNull()

        if (rKohm == null || cUf == null) {
            errorMessage = "Veuillez saisir des valeurs valides pour R et C."
            return
        }

        val rOhms = rKohm * 1000.0
        val cFarads = cUf * 1e-6

        when (val outcome = RcTimeConstantCalculator.calculate(rOhms, cFarads, vs, tEval)) {
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

    Column(modifier = modifier.fillMaxSize().testTag("rc_time_constant_screen")) {
        TopAppBar(
            title = { Text("Constante de temps RC", fontWeight = FontWeight.Bold) },
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
                    EngineeringInputField("Résistance (R)", resistanceKOhmText, { resistanceKOhmText = it }, modifier = Modifier.weight(1f), unit = "kΩ")
                    EngineeringInputField("Capacité (C)", capacitanceUfText, { capacitanceUfText = it }, modifier = Modifier.weight(1f), unit = "μF")
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EngineeringInputField("Tension source (V_s)", supplyVoltageText, { supplyVoltageText = it }, modifier = Modifier.weight(1f), unit = "V")
                    EngineeringInputField("Instant t évalué", evalTimeSecText, { evalTimeSecText = it }, modifier = Modifier.weight(1f), unit = "s")
                }
            }

            item {
                ActionButtonsRow(
                    onReset = {
                        resistanceKOhmText = ""
                        capacitanceUfText = ""
                        calculationResult = null
                        errorMessage = null
                    },
                    onCalculate = ::calculate
                )
            }

            if (errorMessage != null) {
                item { Text(text = errorMessage ?: "", color = MaterialTheme.colorScheme.error) }
            }

            // Visual RC Charging Curve Card
            if (calculationResult != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Courbe de charge exponentielle V(t) sur [0 .. 5τ]",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                            ) {
                                val w = size.width
                                val h = size.height

                                // Draw asymptote V_s and tau grid lines
                                for (k in 1..5) {
                                    val x = (k / 5f) * w
                                    drawLine(
                                        color = gridColor,
                                        start = Offset(x, 0f),
                                        end = Offset(x, h),
                                        strokeWidth = 1.dp.toPx()
                                    )
                                }
                                drawLine(
                                    color = SecondaryAccent.copy(alpha = 0.5f),
                                    start = Offset(0f, h * (1f - 0.632f)),
                                    end = Offset(w, h * (1f - 0.632f)),
                                    strokeWidth = 1.dp.toPx()
                                )

                                // Draw exponential curve V(t)/Vs = 1 - exp(-5 * u)
                                val path = Path()
                                val steps = 80
                                for (i in 0..steps) {
                                    val u = i.toFloat() / steps.toFloat() // 0..1 maps to 0..5*tau
                                    val yNorm = (1.0 - exp(-5.0 * u)).toFloat()
                                    val px = u * w
                                    val py = h - (yNorm * (h * 0.9f) + h * 0.05f)
                                    if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                                }
                                drawPath(
                                    path = path,
                                    color = PrimaryAccent,
                                    style = Stroke(width = 3.dp.toPx())
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Repères : 1τ (63.2%), 3τ (95.0%), 5τ (99.3% du palier asymptotique V_s).",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            calculationResult?.let { res ->
                item { ResultCard(result = res) }
            }
        }
    }
}

@Composable
fun ActionButtonsRow(
    onReset: () -> Unit,
    onCalculate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedButton(
            onClick = onReset,
            modifier = Modifier.weight(1f).testTag("reset_button"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Réinitialiser")
        }

        Button(
            onClick = onCalculate,
            modifier = Modifier.weight(1.5f).testTag("calculate_button"),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Calculer", fontWeight = FontWeight.Bold)
        }
    }
}
