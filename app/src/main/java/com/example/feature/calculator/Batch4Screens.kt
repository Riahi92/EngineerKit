package com.example.feature.calculator

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.domain.calculators.AnalogSignalScalingCalculator
import com.example.domain.calculators.BooleanLogicCalculator
import com.example.domain.calculators.CurrentLoop4To20mACalculator
import com.example.domain.calculators.EnergyCo2Calculator
import com.example.domain.calculators.LogicGate
import com.example.domain.calculators.NumberBase
import com.example.domain.calculators.NumberBaseConverter
import com.example.domain.models.CalculationResult
import com.example.domain.models.EngineOutcome
import com.example.domain.units.UnitDimension
import com.example.domain.units.UnitRegistry
import com.example.feature.calculator.common.EngineeringInputField
import com.example.feature.calculator.common.ParamWithConvertibleUnit
import com.example.feature.calculator.common.ResultCard
import com.example.ui.theme.PrimaryAccent
import com.example.ui.theme.SecondaryAccent

// ============================================================================
// TOOL 26 SCREEN — NUMBER BASE CONVERTER
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumberBaseConverterScreen(
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }

    var sourceBase by remember { mutableStateOf(NumberBase.DECIMAL) }
    var rawInputText by remember { mutableStateOf("255") }

    var calculationResult by remember { mutableStateOf<CalculationResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun calculate() {
        when (val outcome = NumberBaseConverter.convert(rawInputText, sourceBase)) {
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

    Column(modifier = modifier.fillMaxSize().testTag("number_base_converter_screen")) {
        TopAppBar(
            title = { Text("Convertisseur de bases (2/8/10/16)", fontWeight = FontWeight.Bold) },
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
                    text = "INFORMATIQUE & SYSTÈMES EMBARQUÉS",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryAccent
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Conversion entière exacte 64 bits (sans perte de précision flottante) entre Binaire, Octal, Décimal et Hexadécimal.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                Text(
                    text = "Base numérique d'entrée",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    NumberBase.entries.forEach { base ->
                        FilterChip(
                            selected = sourceBase == base,
                            onClick = {
                                sourceBase = base
                                rawInputText = when (base) {
                                    NumberBase.BINARY -> "11111111"
                                    NumberBase.OCTAL -> "377"
                                    NumberBase.DECIMAL -> "255"
                                    NumberBase.HEXADECIMAL -> "FF"
                                }
                                calculate()
                            },
                            label = { Text(base.displayName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryAccent.copy(alpha = 0.2f),
                                selectedLabelColor = PrimaryAccent
                            )
                        )
                    }
                }
            }

            item {
                EngineeringInputField(
                    label = "Valeur entière à convertir (${sourceBase.displayName})",
                    valueText = rawInputText,
                    onValueChange = { rawInputText = it; calculate() },
                    unit = "Base ${sourceBase.radix}",
                    keyboardType = if (sourceBase == NumberBase.HEXADECIMAL) KeyboardType.Ascii else KeyboardType.Number,
                    errorMessage = errorMessage,
                    helperText = "Espaces et préfixes (0x, 0b, 0o) ignorés automatiquement."
                )
            }

            item {
                ActionButtonsRow(
                    onReset = {
                        sourceBase = NumberBase.DECIMAL
                        rawInputText = "255"
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
// TOOL 27 SCREEN — BOOLEAN LOGIC & TRUTH TABLE
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BooleanLogicScreen(
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }

    var selectedGate by remember { mutableStateOf(LogicGate.AND) }
    var inputA by remember { mutableStateOf(true) }
    var inputB by remember { mutableStateOf(false) }

    val outcome = remember(selectedGate, inputA, inputB) {
        BooleanLogicCalculator.calculate(selectedGate, inputA, inputB)
    }
    val truthTable = remember(selectedGate, inputA, inputB) {
        BooleanLogicCalculator.generateTruthTable(selectedGate, inputA, inputB)
    }

    Column(modifier = modifier.fillMaxSize().testTag("boolean_logic_screen")) {
        TopAppBar(
            title = { Text("Portes logiques & Table de vérité", fontWeight = FontWeight.Bold) },
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
                    text = "INFORMATIQUE & LOGIQUE COMBINATOIRE",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryAccent
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Simulateur d'opérateurs booléens (AND, OR, NOT, XOR, NAND, NOR) avec génération dynamique de la table de vérité.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                Text(
                    text = "Sélection de la porte logique",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    LogicGate.entries.forEach { gate ->
                        FilterChip(
                            selected = selectedGate == gate,
                            onClick = { selectedGate = gate },
                            label = { Text("${gate.displayName}  [${gate.algebraicExpr}]") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryAccent.copy(alpha = 0.2f),
                                selectedLabelColor = PrimaryAccent
                            )
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Entrée logique A : ${if (inputA) "1 (HIGH / VRAI)" else "0 (LOW / FAUX)"}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Switch(
                                checked = inputA,
                                onCheckedChange = { inputA = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = PrimaryAccent)
                            )
                        }

                        if (!selectedGate.isUnary) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Entrée logique B : ${if (inputB) "1 (HIGH / VRAI)" else "0 (LOW / FAUX)"}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Switch(
                                    checked = inputB,
                                    onCheckedChange = { inputB = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = PrimaryAccent)
                                )
                            }
                        }
                    }
                }
            }

            // Visual Truth Table Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, SecondaryAccent.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Table de vérité — ${selectedGate.displayName}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = SecondaryAccent
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(vertical = 6.dp, horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("A", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            if (!selectedGate.isUnary) {
                                Text("B", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            }
                            Text("Sortie Y", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        truthTable.forEach { row ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (row.isCurrentSelection) PrimaryAccent.copy(alpha = 0.18f)
                                        else MaterialTheme.colorScheme.surface
                                    )
                                    .padding(vertical = 6.dp, horizontal = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (row.a) "1" else "0",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = if (row.isCurrentSelection) FontWeight.Bold else FontWeight.Normal
                                )
                                if (!selectedGate.isUnary) {
                                    Text(
                                        text = if (row.b == true) "1" else "0",
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = if (row.isCurrentSelection) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                                Text(
                                    text = if (row.output) "1 (TRUE)" else "0 (FALSE)",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = if (row.output) PrimaryAccent else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            if (outcome is EngineOutcome.Success) {
                item { ResultCard(result = outcome.result) }
            }
        }
    }
}

// ============================================================================
// TOOL 28 SCREEN — ANALOG SIGNAL SCALING
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalogSignalScalingScreen(
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }

    var inputSignalXText by remember { mutableStateOf("6.5") }
    var inputMinText by remember { mutableStateOf("0.0") }
    var inputMaxText by remember { mutableStateOf("10.0") }
    var outputMinText by remember { mutableStateOf("-20.0") }
    var outputMaxText by remember { mutableStateOf("120.0") }
    var inputUnitText by remember { mutableStateOf("V") }
    var outputUnitText by remember { mutableStateOf("°C") }

    var calculationResult by remember { mutableStateOf<CalculationResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun calculate() {
        val x = inputSignalXText.replace(',', '.').toDoubleOrNull()
        val xMin = inputMinText.replace(',', '.').toDoubleOrNull()
        val xMax = inputMaxText.replace(',', '.').toDoubleOrNull()
        val yMin = outputMinText.replace(',', '.').toDoubleOrNull()
        val yMax = outputMaxText.replace(',', '.').toDoubleOrNull()

        if (x == null || xMin == null || xMax == null || yMin == null || yMax == null) {
            errorMessage = "Veuillez saisir des bornes numériques valides."
            return
        }

        when (val outcome = AnalogSignalScalingCalculator.calculate(x, xMin, xMax, yMin, yMax, inputUnitText, outputUnitText)) {
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

    Column(modifier = modifier.fillMaxSize().testTag("analog_signal_scaling_screen")) {
        TopAppBar(
            title = { Text("Mise à l'échelle analogique", fontWeight = FontWeight.Bold) },
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
                    text = "AUTOMATISME & INSTRUMENTATION PLC/API",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryAccent
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Interpolation linéaire universelle entre un signal brut capteur/CAN (ex: 0–10 V, 0–27648 pts) et la grandeur d'ingénierie.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                EngineeringInputField(
                    label = "Signal d'entrée mesuré (X)",
                    valueText = inputSignalXText,
                    onValueChange = { inputSignalXText = it; calculate() },
                    unit = inputUnitText
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    EngineeringInputField(
                        label = "Entrée min (X_min)",
                        valueText = inputMinText,
                        onValueChange = { inputMinText = it; calculate() },
                        unit = inputUnitText,
                        modifier = Modifier.weight(1f)
                    )
                    EngineeringInputField(
                        label = "Entrée max (X_max)",
                        valueText = inputMaxText,
                        onValueChange = { inputMaxText = it; calculate() },
                        unit = inputUnitText,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    EngineeringInputField(
                        label = "Sortie min (Y_min)",
                        valueText = outputMinText,
                        onValueChange = { outputMinText = it; calculate() },
                        unit = outputUnitText,
                        modifier = Modifier.weight(1f)
                    )
                    EngineeringInputField(
                        label = "Sortie max (Y_max)",
                        valueText = outputMaxText,
                        onValueChange = { outputMaxText = it; calculate() },
                        unit = outputUnitText,
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
                        inputSignalXText = "6.5"
                        inputMinText = "0.0"
                        inputMaxText = "10.0"
                        outputMinText = "-20.0"
                        outputMaxText = "120.0"
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
// TOOL 29 SCREEN — 4–20 mA CURRENT LOOP CONVERTER
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrentLoop4To20mAScreen(
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }

    var currentMaText by remember { mutableStateOf("12.0") }
    var engMinText by remember { mutableStateOf("0.0") }
    var engMaxText by remember { mutableStateOf("16.0") }
    var engUnitText by remember { mutableStateOf("bar") }

    var calculationResult by remember { mutableStateOf<CalculationResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun calculate() {
        val iMa = currentMaText.replace(',', '.').toDoubleOrNull()
        val yMin = engMinText.replace(',', '.').toDoubleOrNull()
        val yMax = engMaxText.replace(',', '.').toDoubleOrNull()

        if (iMa == null || yMin == null || yMax == null) {
            errorMessage = "Veuillez saisir des valeurs numériques valides."
            return
        }

        when (val outcome = CurrentLoop4To20mACalculator.calculate(iMa, yMin, yMax, engUnitText.ifBlank { "unité" })) {
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

    Column(modifier = modifier.fillMaxSize().testTag("current_loop_4_20_ma_screen")) {
        TopAppBar(
            title = { Text("Boucle de courant 4–20 mA", fontWeight = FontWeight.Bold) },
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
                    text = "AUTOMATISME & INSTRUMENTATION INDUSTRIELLE",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryAccent
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Convertit un courant de boucle 4–20 mA en pourcentage d'échelle et grandeur physique avec diagnostic hors plage.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                EngineeringInputField(
                    label = "Courant de boucle mesuré (I)",
                    valueText = currentMaText,
                    onValueChange = { currentMaText = it; calculate() },
                    unit = "mA",
                    helperText = "4.0 mA = 0% | 12.0 mA = 50% | 20.0 mA = 100%"
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    EngineeringInputField(
                        label = "Échelle basse (4 mA)",
                        valueText = engMinText,
                        onValueChange = { engMinText = it; calculate() },
                        unit = engUnitText,
                        modifier = Modifier.weight(1f)
                    )
                    EngineeringInputField(
                        label = "Échelle haute (20 mA)",
                        valueText = engMaxText,
                        onValueChange = { engMaxText = it; calculate() },
                        unit = engUnitText,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                EngineeringInputField(
                    label = "Unité physique du capteur",
                    valueText = engUnitText,
                    onValueChange = { engUnitText = it; calculate() },
                    keyboardType = KeyboardType.Text,
                    errorMessage = errorMessage,
                    helperText = "Ex: bar, °C, m³/h, %, mm"
                )
            }

            item {
                ActionButtonsRow(
                    onReset = {
                        currentMaText = "12.0"
                        engMinText = "0.0"
                        engMaxText = "16.0"
                        engUnitText = "bar"
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
// TOOL 30 SCREEN — ENERGY AND CO2e
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnergyCo2Screen(
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }

    var powerText by remember { mutableStateOf("45.0") }
    var powerUnit by remember { mutableStateOf(UnitRegistry.KW) }
    var timeText by remember { mutableStateOf("2000.0") }
    var timeUnit by remember { mutableStateOf(UnitRegistry.HOUR) }
    var emissionFactorText by remember { mutableStateOf("0.052") } // Ex: mix bas carbone / modifiable

    var calculationResult by remember { mutableStateOf<CalculationResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun calculate() {
        val p = powerText.replace(',', '.').toDoubleOrNull()
        val t = timeText.replace(',', '.').toDoubleOrNull()
        val ef = emissionFactorText.replace(',', '.').toDoubleOrNull()

        if (p == null || t == null || ef == null) {
            errorMessage = "Veuillez saisir des valeurs numériques valides."
            return
        }

        when (val outcome = EnergyCo2Calculator.calculate(p, powerUnit, t, timeUnit, ef)) {
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

    Column(modifier = modifier.fillMaxSize().testTag("energy_co2_screen")) {
        TopAppBar(
            title = { Text("Énergie & Émissions CO₂e", fontWeight = FontWeight.Bold) },
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
                    text = "ENVIRONNEMENT & EFFICACITÉ ÉNERGÉTIQUE",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryAccent
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Estime la consommation d'énergie électrique (kWh, MWh, MJ) et l'empreinte carbone associée selon le facteur d'émission renseigné.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                ParamWithConvertibleUnit(
                    label = "Puissance électrique moyenne (P)",
                    valueText = powerText,
                    onValueChange = { powerText = it; calculate() },
                    currentUnit = powerUnit,
                    availableUnits = UnitRegistry.byDimension(UnitDimension.POWER),
                    onUnitChanged = { powerUnit = it; calculate() }
                )
            }

            item {
                ParamWithConvertibleUnit(
                    label = "Durée d'exploitation (t)",
                    valueText = timeText,
                    onValueChange = { timeText = it; calculate() },
                    currentUnit = timeUnit,
                    availableUnits = UnitRegistry.byDimension(UnitDimension.TIME),
                    onUnitChanged = { timeUnit = it; calculate() }
                )
            }

            item {
                EngineeringInputField(
                    label = "Facteur d'émission réseau (FE)",
                    valueText = emissionFactorText,
                    onValueChange = { emissionFactorText = it; calculate() },
                    unit = "kg CO₂e/kWh",
                    errorMessage = errorMessage,
                    helperText = "Ordres de grandeur : France ~0.052 | UE moyenne ~0.25 | Mix mondial ~0.45 kg CO₂e/kWh"
                )
            }

            item {
                ActionButtonsRow(
                    onReset = {
                        powerText = "45.0"
                        powerUnit = UnitRegistry.KW
                        timeText = "2000.0"
                        timeUnit = UnitRegistry.HOUR
                        emissionFactorText = "0.052"
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
