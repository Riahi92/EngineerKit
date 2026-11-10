package com.example.feature.calculator

import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.domain.calculators.DilutionUnknown
import com.example.domain.calculators.MolarConcentrationCalculator
import com.example.domain.calculators.ProductionCapacityCalculator
import com.example.domain.calculators.SoluteInputMode
import com.example.domain.calculators.SolutionDilutionCalculator
import com.example.domain.calculators.TaktTimeCalculator
import com.example.domain.models.CalculationResult
import com.example.domain.models.EngineOutcome
import com.example.domain.units.UnitDimension
import com.example.domain.units.UnitRegistry
import com.example.feature.calculator.common.EngineeringInputField
import com.example.feature.calculator.common.ParamWithConvertibleUnit
import com.example.feature.calculator.common.ResultCard
import com.example.ui.theme.PrimaryAccent

// ============================================================================
// TOOL 22 SCREEN — TAKT TIME
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaktTimeScreen(
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }

    var availableMinutesText by remember { mutableStateOf("480") }
    var breaksMinutesText by remember { mutableStateOf("45") }
    var demandUnitsText by remember { mutableStateOf("870") }

    var calculationResult by remember { mutableStateOf<CalculationResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun calculate() {
        val avail = availableMinutesText.replace(',', '.').toDoubleOrNull()
        val breaks = breaksMinutesText.replace(',', '.').toDoubleOrNull()
        val demand = demandUnitsText.replace(',', '.').toDoubleOrNull()

        if (avail == null || breaks == null || demand == null) {
            errorMessage = "Veuillez saisir des valeurs numériques valides."
            return
        }

        when (val outcome = TaktTimeCalculator.calculate(avail, breaks, demand)) {
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

    Column(modifier = modifier.fillMaxSize().testTag("takt_time_screen")) {
        TopAppBar(
            title = { Text("Calculateur de Takt Time", fontWeight = FontWeight.Bold) },
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
                Text(
                    text = "GÉNIE INDUSTRIEL • LEAN MANUFACTURING",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryAccent
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Détermine le rythme d'écoulement requis d'une ligne de production pour synchroniser exactement la fabrication sur la demande client.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                EngineeringInputField(
                    label = "Temps total d'ouverture par poste (T_ouverture)",
                    valueText = availableMinutesText,
                    onValueChange = { availableMinutesText = it; calculate() },
                    unit = "min",
                    helperText = "Ex: 480 min pour un poste de 8 heures"
                )
            }

            item {
                EngineeringInputField(
                    label = "Pauses & Arrêts planifiés (T_pauses)",
                    valueText = breaksMinutesText,
                    onValueChange = { breaksMinutesText = it; calculate() },
                    unit = "min",
                    helperText = "Pauses repas, briefings 5S, maintenance programmée"
                )
            }

            item {
                EngineeringInputField(
                    label = "Demande client sur la période",
                    valueText = demandUnitsText,
                    onValueChange = { demandUnitsText = it; calculate() },
                    unit = "unités",
                    errorMessage = errorMessage
                )
            }

            item {
                ActionButtonsRow(
                    onReset = {
                        availableMinutesText = "480"
                        breaksMinutesText = "45"
                        demandUnitsText = "870"
                        calculate()
                    },
                    onCalculate = { calculate() }
                )
            }

            calculationResult?.let { res ->
                item { ResultCard(result = res) }
            }
        }
    }
}

// ============================================================================
// TOOL 23 SCREEN — PRODUCTION CAPACITY
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductionCapacityScreen(
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }

    var operatingHoursText by remember { mutableStateOf("8.0") }
    var cycleTimeSecText by remember { mutableStateOf("18.0") }
    var machinesCountText by remember { mutableStateOf("2") }
    var availabilityPctText by remember { mutableStateOf("90.0") }
    var performancePctText by remember { mutableStateOf("95.0") }

    var calculationResult by remember { mutableStateOf<CalculationResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun calculate() {
        val hours = operatingHoursText.replace(',', '.').toDoubleOrNull()
        val cycle = cycleTimeSecText.replace(',', '.').toDoubleOrNull()
        val machines = machinesCountText.trim().toIntOrNull()
        val avail = availabilityPctText.replace(',', '.').toDoubleOrNull() ?: 100.0
        val perf = performancePctText.replace(',', '.').toDoubleOrNull() ?: 100.0

        if (hours == null || cycle == null || machines == null) {
            errorMessage = "Veuillez saisir des paramètres numériques valides."
            return
        }

        when (val outcome = ProductionCapacityCalculator.calculate(hours, cycle, machines, avail, perf)) {
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

    Column(modifier = modifier.fillMaxSize().testTag("production_capacity_screen")) {
        TopAppBar(
            title = { Text("Capacité de production", fontWeight = FontWeight.Bold) },
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
                Text(
                    text = "GÉNIE INDUSTRIEL • DIMENSIONNEMENT DE LIGNE",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryAccent
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Calcule la capacité de production théorique et réelle pondérée d'un îlot de fabrication à postes multiples en parallèle.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                EngineeringInputField(
                    label = "Temps d'ouverture opérationnel (T)",
                    valueText = operatingHoursText,
                    onValueChange = { operatingHoursText = it; calculate() },
                    unit = "heures"
                )
            }

            item {
                EngineeringInputField(
                    label = "Temps de cycle unitaire (t_cycle)",
                    valueText = cycleTimeSecText,
                    onValueChange = { cycleTimeSecText = it; calculate() },
                    unit = "s / unité"
                )
            }

            item {
                EngineeringInputField(
                    label = "Nombre de postes / machines en parallèle (N)",
                    valueText = machinesCountText,
                    onValueChange = { machinesCountText = it; calculate() },
                    unit = "machines",
                    keyboardType = KeyboardType.Number
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    EngineeringInputField(
                        label = "Disponibilité (A)",
                        valueText = availabilityPctText,
                        onValueChange = { availabilityPctText = it; calculate() },
                        unit = "%",
                        modifier = Modifier.weight(1f)
                    )
                    EngineeringInputField(
                        label = "Performance (P)",
                        valueText = performancePctText,
                        onValueChange = { performancePctText = it; calculate() },
                        unit = "%",
                        modifier = Modifier.weight(1f)
                    )
                }
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            item {
                ActionButtonsRow(
                    onReset = {
                        operatingHoursText = "8.0"
                        cycleTimeSecText = "18.0"
                        machinesCountText = "2"
                        availabilityPctText = "90.0"
                        performancePctText = "95.0"
                        calculate()
                    },
                    onCalculate = { calculate() }
                )
            }

            calculationResult?.let { res ->
                item { ResultCard(result = res) }
            }
        }
    }
}

// ============================================================================
// TOOL 24 SCREEN — MOLAR CONCENTRATION
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MolarConcentrationScreen(
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }

    var inputMode by remember { mutableStateOf(SoluteInputMode.FROM_MASS_AND_MOLAR_MASS) }
    var soluteAmountText by remember { mutableStateOf("11.688") }
    var massUnit by remember { mutableStateOf(UnitRegistry.G) }
    var molarMassText by remember { mutableStateOf("58.44") } // NaCl default
    var volumeText by remember { mutableStateOf("500.0") }
    var volumeUnit by remember { mutableStateOf(UnitRegistry.ML) }

    var calculationResult by remember { mutableStateOf<CalculationResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun calculate() {
        val amount = soluteAmountText.replace(',', '.').toDoubleOrNull()
        val molarMass = molarMassText.replace(',', '.').toDoubleOrNull() ?: 58.44
        val vol = volumeText.replace(',', '.').toDoubleOrNull()

        if (amount == null || vol == null) {
            errorMessage = "Veuillez saisir des valeurs numériques valides."
            return
        }

        when (val outcome = MolarConcentrationCalculator.calculate(inputMode, amount, massUnit, molarMass, vol, volumeUnit)) {
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

    Column(modifier = modifier.fillMaxSize().testTag("molar_concentration_screen")) {
        TopAppBar(
            title = { Text("Concentration molaire (Molarité)", fontWeight = FontWeight.Bold) },
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
                Text(
                    text = "GÉNIE CHIMIQUE • PRÉPARATION DE SOLUTIONS",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryAccent
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Calcule la molarité C (mol/L) et la concentration massique d'une solution à partir de la masse pesée ou du nombre de moles de soluté.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                Text(
                    text = "Mode de saisie du soluté",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    SoluteInputMode.entries.forEach { mode ->
                        FilterChip(
                            selected = inputMode == mode,
                            onClick = { inputMode = mode; calculate() },
                            label = { Text(mode.displayName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryAccent.copy(alpha = 0.2f),
                                selectedLabelColor = PrimaryAccent
                            )
                        )
                    }
                }
            }

            item {
                if (inputMode == SoluteInputMode.FROM_MASS_AND_MOLAR_MASS) {
                    ParamWithConvertibleUnit(
                        label = "Masse de soluté pesée (m)",
                        valueText = soluteAmountText,
                        onValueChange = { soluteAmountText = it; calculate() },
                        currentUnit = massUnit,
                        availableUnits = UnitRegistry.byDimension(UnitDimension.MASS),
                        onUnitChanged = { massUnit = it; calculate() }
                    )
                } else {
                    EngineeringInputField(
                        label = "Quantité de matière de soluté (n)",
                        valueText = soluteAmountText,
                        onValueChange = { soluteAmountText = it; calculate() },
                        unit = "mol"
                    )
                }
            }

            item {
                EngineeringInputField(
                    label = "Masse molaire moléculaire (M)",
                    valueText = molarMassText,
                    onValueChange = { molarMassText = it; calculate() },
                    unit = "g/mol",
                    helperText = "Exemples : NaCl = 58.44, NaOH = 40.00, H₂SO₄ = 98.08, Glucose = 180.16"
                )
            }

            item {
                ParamWithConvertibleUnit(
                    label = "Volume final de la solution (V)",
                    valueText = volumeText,
                    onValueChange = { volumeText = it; calculate() },
                    currentUnit = volumeUnit,
                    availableUnits = UnitRegistry.byDimension(UnitDimension.VOLUME),
                    onUnitChanged = { volumeUnit = it; calculate() }
                )
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            item {
                ActionButtonsRow(
                    onReset = {
                        inputMode = SoluteInputMode.FROM_MASS_AND_MOLAR_MASS
                        soluteAmountText = "11.688"
                        massUnit = UnitRegistry.G
                        molarMassText = "58.44"
                        volumeText = "500.0"
                        volumeUnit = UnitRegistry.ML
                        calculate()
                    },
                    onCalculate = { calculate() }
                )
            }

            calculationResult?.let { res ->
                item { ResultCard(result = res) }
            }
        }
    }
}

// ============================================================================
// TOOL 25 SCREEN — SOLUTION DILUTION (C1 * V1 = C2 * V2)
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolutionDilutionScreen(
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }

    var unknownTarget by remember { mutableStateOf(DilutionUnknown.SOLVE_V1) }
    var c1Text by remember { mutableStateOf("2.0") }
    var v1Text by remember { mutableStateOf("25.0") }
    var c2Text by remember { mutableStateOf("0.2") }
    var v2Text by remember { mutableStateOf("250.0") }
    var concUnit by remember { mutableStateOf("mol/L") }
    var volUnit by remember { mutableStateOf("mL") }

    var calculationResult by remember { mutableStateOf<CalculationResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun calculate() {
        val c1 = c1Text.replace(',', '.').toDoubleOrNull() ?: 0.0
        val v1 = v1Text.replace(',', '.').toDoubleOrNull() ?: 0.0
        val c2 = c2Text.replace(',', '.').toDoubleOrNull() ?: 0.0
        val v2 = v2Text.replace(',', '.').toDoubleOrNull() ?: 0.0

        when (val outcome = SolutionDilutionCalculator.calculate(unknownTarget, c1, v1, c2, v2, concUnit, volUnit)) {
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

    Column(modifier = modifier.fillMaxSize().testTag("solution_dilution_screen")) {
        TopAppBar(
            title = { Text("Dilution de solution (C₁V₁ = C₂V₂)", fontWeight = FontWeight.Bold) },
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
                Text(
                    text = "GÉNIE CHIMIQUE • CONSERVATION DE MATIÈRE",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryAccent
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Résout n'importe quelle inconnue de l'équation de dilution C₁·V₁ = C₂·V₂ et indique le volume de solvant à compléter.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                Text(
                    text = "Grandeur inconnue à déterminer",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    DilutionUnknown.entries.forEach { target ->
                        FilterChip(
                            selected = unknownTarget == target,
                            onClick = { unknownTarget = target; calculate() },
                            label = { Text(target.displayName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryAccent.copy(alpha = 0.2f),
                                selectedLabelColor = PrimaryAccent
                            )
                        )
                    }
                }
            }

            if (unknownTarget != DilutionUnknown.SOLVE_C1) {
                item {
                    EngineeringInputField(
                        label = "Concentration mère initiale (C₁)",
                        valueText = c1Text,
                        onValueChange = { c1Text = it; calculate() },
                        unit = concUnit
                    )
                }
            }

            if (unknownTarget != DilutionUnknown.SOLVE_V1) {
                item {
                    EngineeringInputField(
                        label = "Volume initial prélevé (V₁)",
                        valueText = v1Text,
                        onValueChange = { v1Text = it; calculate() },
                        unit = volUnit
                    )
                }
            }

            if (unknownTarget != DilutionUnknown.SOLVE_C2) {
                item {
                    EngineeringInputField(
                        label = "Concentration finale souhaitée (C₂)",
                        valueText = c2Text,
                        onValueChange = { c2Text = it; calculate() },
                        unit = concUnit
                    )
                }
            }

            if (unknownTarget != DilutionUnknown.SOLVE_V2) {
                item {
                    EngineeringInputField(
                        label = "Volume final de la fiole (V₂)",
                        valueText = v2Text,
                        onValueChange = { v2Text = it; calculate() },
                        unit = volUnit,
                        errorMessage = errorMessage
                    )
                }
            }

            item {
                ActionButtonsRow(
                    onReset = {
                        unknownTarget = DilutionUnknown.SOLVE_V1
                        c1Text = "2.0"
                        v1Text = "25.0"
                        c2Text = "0.2"
                        v2Text = "250.0"
                        calculate()
                    },
                    onCalculate = { calculate() }
                )
            }

            calculationResult?.let { res ->
                item { ResultCard(result = res) }
            }
        }
    }
}
