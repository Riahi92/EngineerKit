package com.example.feature.calculator

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.domain.calculators.OeeTrsCalculator
import com.example.domain.calculators.OhmsLawCalculator
import com.example.domain.calculators.OhmsLawPair
import com.example.domain.calculators.QuadraticEquationSolver
import com.example.domain.calculators.StatisticsCalculator
import com.example.domain.calculators.ThreePhasePowerCalculator
import com.example.domain.models.CalculationResult
import com.example.domain.models.EngineOutcome
import com.example.feature.calculator.common.EngineeringInputField
import com.example.feature.calculator.common.ResultCard
import com.example.ui.theme.PrimaryAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuadraticScreen(
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var aText by remember { mutableStateOf("1") }
    var bText by remember { mutableStateOf("-5") }
    var cText by remember { mutableStateOf("6") }

    var calculationResult by remember { mutableStateOf<CalculationResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun calculate() {
        val a = aText.replace(',', '.').toDoubleOrNull()
        val b = bText.replace(',', '.').toDoubleOrNull()
        val c = cText.replace(',', '.').toDoubleOrNull()

        if (a == null || b == null || c == null) {
            errorMessage = "Tous les coefficients (a, b, c) doivent être des nombres réels valides."
            return
        }

        when (val outcome = QuadraticEquationSolver.solve(a, b, c)) {
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

    Column(modifier = modifier.fillMaxSize().testTag("quadratic_screen")) {
        TopAppBar(
            title = { Text("Équation du 2nd degré", fontWeight = FontWeight.Bold) },
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
                            text = "Résolution analytique : a·x² + b·x + c = 0",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Calcule le discriminant Δ = b² - 4ac, détermine les racines réelles ou complexes conjuguées, et gère la dégénérescence linéaire lorsque a = 0.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EngineeringInputField("Coefficient a", aText, { aText = it }, modifier = Modifier.weight(1f), placeholder = "1.0")
                    EngineeringInputField("Coefficient b", bText, { bText = it }, modifier = Modifier.weight(1f), placeholder = "0.0")
                    EngineeringInputField("Coefficient c", cText, { cText = it }, modifier = Modifier.weight(1f), placeholder = "0.0")
                }
            }

            item {
                Button(
                    onClick = ::calculate,
                    modifier = Modifier.fillMaxWidth().testTag("calculate_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Calculer les racines", fontWeight = FontWeight.Bold)
                }
            }

            if (errorMessage != null) {
                item {
                    Text(text = errorMessage ?: "", color = MaterialTheme.colorScheme.error)
                }
            }

            calculationResult?.let { res ->
                item {
                    ResultCard(result = res)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var rawDataText by remember { mutableStateOf("12, 15.5, 14, 18.2, 19, 13.5, 16, 17.5") }
    var calculationResult by remember { mutableStateOf<CalculationResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun calculate() {
        val dataset = StatisticsCalculator.parseDataset(rawDataText)
        when (val outcome = StatisticsCalculator.calculate(dataset)) {
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

    Column(modifier = modifier.fillMaxSize().testTag("statistics_screen")) {
        TopAppBar(
            title = { Text("Statistiques descriptives", fontWeight = FontWeight.Bold) },
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
                            text = "Analyse statistique d'échantillons et populations",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Séparez vos valeurs par des virgules, points-virgules, espaces ou retours à la ligne. Calcule moyenne, médiane, variances et écarts-types.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Column {
                    Text("Jeu de données numériques (x_i)", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = rawDataText,
                        onValueChange = { rawDataText = it },
                        modifier = Modifier.fillMaxWidth().height(110.dp),
                        shape = RoundedCornerShape(12.dp),
                        placeholder = { Text("ex: 10, 12.5, 14, 18, 22.3") }
                    )
                }
            }

            item {
                Button(
                    onClick = ::calculate,
                    modifier = Modifier.fillMaxWidth().testTag("calculate_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Analyser le jeu de données", fontWeight = FontWeight.Bold)
                }
            }

            if (errorMessage != null) {
                item {
                    Text(text = errorMessage ?: "", color = MaterialTheme.colorScheme.error)
                }
            }

            calculationResult?.let { res ->
                item {
                    ResultCard(result = res)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OhmsLawScreen(
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPair by remember { mutableStateOf(OhmsLawPair.VOLTAGE_CURRENT) }
    var val1Text by remember { mutableStateOf("230") }
    var val2Text by remember { mutableStateOf("10") }

    var calculationResult by remember { mutableStateOf<CalculationResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isPairDropdownExpanded by remember { mutableStateOf(false) }

    fun calculate() {
        val v1 = val1Text.replace(',', '.').toDoubleOrNull()
        val v2 = val2Text.replace(',', '.').toDoubleOrNull()

        if (v1 == null || v2 == null) {
            errorMessage = "Veuillez renseigner des nombres positifs valides."
            return
        }

        when (val outcome = OhmsLawCalculator.calculate(selectedPair, v1, v2)) {
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

    Column(modifier = modifier.fillMaxSize().testTag("ohms_law_screen")) {
        TopAppBar(
            title = { Text("Loi d'Ohm & Puissance", fontWeight = FontWeight.Bold) },
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
                Text("Variables connues en entrée", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(4.dp))
                ExposedDropdownMenuBox(
                    expanded = isPairDropdownExpanded,
                    onExpandedChange = { isPairDropdownExpanded = it }
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(selectedPair.displayName, style = MaterialTheme.typography.bodyMedium)
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = isPairDropdownExpanded)
                        }
                    }

                    ExposedDropdownMenu(
                        expanded = isPairDropdownExpanded,
                        onDismissRequest = { isPairDropdownExpanded = false }
                    ) {
                        OhmsLawPair.entries.forEach { pair ->
                            DropdownMenuItem(
                                text = { Text(pair.displayName) },
                                onClick = {
                                    selectedPair = pair
                                    isPairDropdownExpanded = false
                                    calculate()
                                }
                            )
                        }
                    }
                }
            }

            // Labels for inputs based on chosen pair
            val (label1, unit1, label2, unit2) = when (selectedPair) {
                OhmsLawPair.VOLTAGE_CURRENT -> Quadruple("Tension (U)", "V", "Intensité du courant (I)", "A")
                OhmsLawPair.VOLTAGE_RESISTANCE -> Quadruple("Tension (U)", "V", "Résistance (R)", "Ω")
                OhmsLawPair.VOLTAGE_POWER -> Quadruple("Tension (U)", "V", "Puissance dissipée (P)", "W")
                OhmsLawPair.CURRENT_RESISTANCE -> Quadruple("Intensité du courant (I)", "A", "Résistance (R)", "Ω")
                OhmsLawPair.CURRENT_POWER -> Quadruple("Intensité du courant (I)", "A", "Puissance dissipée (P)", "W")
                OhmsLawPair.RESISTANCE_POWER -> Quadruple("Résistance (R)", "Ω", "Puissance dissipée (P)", "W")
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EngineeringInputField(label1, val1Text, { val1Text = it }, modifier = Modifier.weight(1f), unit = unit1)
                    EngineeringInputField(label2, val2Text, { val2Text = it }, modifier = Modifier.weight(1f), unit = unit2)
                }
            }

            item {
                Button(
                    onClick = ::calculate,
                    modifier = Modifier.fillMaxWidth().testTag("calculate_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Calculer l'état électrique complet", fontWeight = FontWeight.Bold)
                }
            }

            if (errorMessage != null) {
                item {
                    Text(text = errorMessage ?: "", color = MaterialTheme.colorScheme.error)
                }
            }

            calculationResult?.let { res ->
                item {
                    ResultCard(result = res)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreePhaseScreen(
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var voltageText by remember { mutableStateOf("400") } // V
    var currentText by remember { mutableStateOf("32") } // A
    var cosPhiText by remember { mutableStateOf("0.85") }

    var calculationResult by remember { mutableStateOf<CalculationResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun calculate() {
        val u = voltageText.replace(',', '.').toDoubleOrNull()
        val i = currentText.replace(',', '.').toDoubleOrNull()
        val cosPhi = cosPhiText.replace(',', '.').toDoubleOrNull()

        if (u == null || i == null || cosPhi == null) {
            errorMessage = "Veuillez renseigner des valeurs valides pour U, I et cos φ."
            return
        }

        when (val outcome = ThreePhasePowerCalculator.calculate(u, i, cosPhi)) {
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

    Column(modifier = modifier.fillMaxSize().testTag("three_phase_screen")) {
        TopAppBar(
            title = { Text("Puissance triphasée", fontWeight = FontWeight.Bold) },
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
                    EngineeringInputField("Tension entre phases (U)", voltageText, { voltageText = it }, modifier = Modifier.weight(1f), unit = "V", helperText = "Standard: 400V")
                    EngineeringInputField("Courant de ligne (I)", currentText, { currentText = it }, modifier = Modifier.weight(1f), unit = "A")
                }
            }

            item {
                EngineeringInputField(
                    label = "Facteur de puissance (cos φ)",
                    valueText = cosPhiText,
                    onValueChange = { cosPhiText = it },
                    unit = "-",
                    placeholder = "0.0 à 1.0",
                    helperText = "Moteurs asynchrones typiques: 0.80 à 0.88"
                )
            }

            item {
                Button(
                    onClick = ::calculate,
                    modifier = Modifier.fillMaxWidth().testTag("calculate_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Calculer P, Q, S", fontWeight = FontWeight.Bold)
                }
            }

            if (errorMessage != null) {
                item {
                    Text(text = errorMessage ?: "", color = MaterialTheme.colorScheme.error)
                }
            }

            calculationResult?.let { res ->
                item {
                    ResultCard(result = res)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OeeScreen(
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var plannedHoursText by remember { mutableStateOf("8.0") }
    var operatingHoursText by remember { mutableStateOf("7.2") }
    var idealCycleSecText by remember { mutableStateOf("15.0") }
    var totalQtyText by remember { mutableStateOf("1500") }
    var goodQtyText by remember { mutableStateOf("1470") }

    var calculationResult by remember { mutableStateOf<CalculationResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun calculate() {
        val planned = plannedHoursText.replace(',', '.').toDoubleOrNull()
        val op = operatingHoursText.replace(',', '.').toDoubleOrNull()
        val cycle = idealCycleSecText.replace(',', '.').toDoubleOrNull()
        val tot = totalQtyText.replace(',', '.').toDoubleOrNull()
        val good = goodQtyText.replace(',', '.').toDoubleOrNull()

        if (planned == null || op == null || cycle == null || tot == null || good == null) {
            errorMessage = "Tous les champs de production doivent être renseignés."
            return
        }

        when (val outcome = OeeTrsCalculator.calculate(planned, op, cycle, tot, good)) {
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

    Column(modifier = modifier.fillMaxSize().testTag("oee_screen")) {
        TopAppBar(
            title = { Text("TRS / OEE Industriel", fontWeight = FontWeight.Bold) },
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
                    EngineeringInputField("Temps planifié", plannedHoursText, { plannedHoursText = it }, modifier = Modifier.weight(1f), unit = "h")
                    EngineeringInputField("Temps réel de marche", operatingHoursText, { operatingHoursText = it }, modifier = Modifier.weight(1f), unit = "h")
                }
            }

            item {
                EngineeringInputField(
                    label = "Temps de cycle idéal théorique unitaire",
                    valueText = idealCycleSecText,
                    onValueChange = { idealCycleSecText = it },
                    unit = "s / pièce",
                    helperText = "Cadence nominale de la ligne de fabrication"
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EngineeringInputField("Quantité totale produite", totalQtyText, { totalQtyText = it }, modifier = Modifier.weight(1f), unit = "pcs")
                    EngineeringInputField("Quantité conforme (Bonnes)", goodQtyText, { goodQtyText = it }, modifier = Modifier.weight(1f), unit = "pcs")
                }
            }

            item {
                Button(
                    onClick = ::calculate,
                    modifier = Modifier.fillMaxWidth().testTag("calculate_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Calculer le TRS / OEE", fontWeight = FontWeight.Bold)
                }
            }

            if (errorMessage != null) {
                item {
                    Text(text = errorMessage ?: "", color = MaterialTheme.colorScheme.error)
                }
            }

            calculationResult?.let { res ->
                item {
                    ResultCard(result = res)
                }
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
