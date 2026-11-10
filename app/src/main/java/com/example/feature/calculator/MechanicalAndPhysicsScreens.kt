package com.example.feature.calculator

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import com.example.domain.calculators.AxialStressCalculator
import com.example.domain.calculators.BeamBendingCalculator
import com.example.domain.calculators.BeamLoadType
import com.example.domain.calculators.MassVolumeCalculator
import com.example.domain.calculators.MaterialPresets
import com.example.domain.calculators.SolidGeometry
import com.example.domain.models.CalculationResult
import com.example.domain.models.EngineOutcome
import com.example.domain.units.UnitDimension
import com.example.domain.units.UnitRegistry
import com.example.feature.calculator.common.EngineeringInputField
import com.example.feature.calculator.common.ParamWithConvertibleUnit
import com.example.feature.calculator.common.ResultCard
import com.example.ui.theme.PrimaryAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AxialStressScreen(
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var forceText by remember { mutableStateOf("150") }
    var forceUnit by remember { mutableStateOf(UnitRegistry.KN) }
    var areaText by remember { mutableStateOf("6000") }
    var areaUnit by remember { mutableStateOf(UnitRegistry.MM2) }
    var yieldStrengthText by remember { mutableStateOf("235") }

    var calculationResult by remember { mutableStateOf<CalculationResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun calculate() {
        val f = forceText.replace(',', '.').toDoubleOrNull()
        val a = areaText.replace(',', '.').toDoubleOrNull()
        val re = yieldStrengthText.replace(',', '.').toDoubleOrNull()

        if (f == null || a == null) {
            errorMessage = "Veuillez renseigner des nombres valides pour la force et la section."
            return
        }

        when (val outcome = AxialStressCalculator.calculate(f, forceUnit, a, areaUnit, re)) {
            is EngineOutcome.Success -> {
                calculationResult = outcome.result
                errorMessage = null
            }
            is EngineOutcome.Failure -> {
                errorMessage = outcome.error
            }
        }
    }

    // Auto-calculate on initial launch
    remember { calculate(); true }

    Column(modifier = modifier.fillMaxSize().testTag("axial_stress_screen")) {
        TopAppBar(
            title = { Text("Contrainte axiale (RDM)", fontWeight = FontWeight.Bold) },
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
                            text = "Calcul de la contrainte normale de traction ou compression",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Formule canonique : σ = F / A. Convertit automatiquement entre unités et évalue le coefficient de sécurité vis-à-vis de la limite élastique Re.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                ParamWithConvertibleUnit(
                    label = "Effort normal axial (F)",
                    valueText = forceText,
                    onValueChange = { forceText = it },
                    currentUnit = forceUnit,
                    availableUnits = UnitRegistry.getUnitsForDimension(UnitDimension.FORCE),
                    onUnitChanged = { forceUnit = it }
                )
            }

            item {
                ParamWithConvertibleUnit(
                    label = "Section droite de la pièce (A)",
                    valueText = areaText,
                    onValueChange = { areaText = it },
                    currentUnit = areaUnit,
                    availableUnits = UnitRegistry.getUnitsForDimension(UnitDimension.AREA),
                    onUnitChanged = { areaUnit = it }
                )
            }

            item {
                EngineeringInputField(
                    label = "Limite élastique du matériau Re (Optionnel)",
                    valueText = yieldStrengthText,
                    onValueChange = { yieldStrengthText = it },
                    unit = "MPa",
                    placeholder = "ex: 235 pour acier S235",
                    helperText = "Permet de vérifier le ratio Re / |σ|"
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            forceText = ""
                            areaText = ""
                            yieldStrengthText = ""
                            calculationResult = null
                            errorMessage = null
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Effacer")
                    }

                    Button(
                        onClick = ::calculate,
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
fun MassVolumeScreen(
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedGeometry by remember { mutableStateOf(SolidGeometry.RECTANGULAR_PRISM) }
    var dimUnit by remember { mutableStateOf(UnitRegistry.MM) }
    var densityKgM3Text by remember { mutableStateOf("7850") }
    var selectedPresetName by remember { mutableStateOf("Acier de construction (S235/S355)") }

    // Prism inputs
    var lengthText by remember { mutableStateOf("500") }
    var widthText by remember { mutableStateOf("200") }
    var heightText by remember { mutableStateOf("50") }

    // Cylinder inputs
    var diamText by remember { mutableStateOf("100") }
    var innerDiamText by remember { mutableStateOf("80") }

    // Plate thickness
    var plateThicknessMm by remember { mutableStateOf("10") }

    var calculationResult by remember { mutableStateOf<CalculationResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isPresetDropdownExpanded by remember { mutableStateOf(false) }

    fun calculate() {
        val density = densityKgM3Text.replace(',', '.').toDoubleOrNull() ?: 7850.0

        val outcome = when (selectedGeometry) {
            SolidGeometry.RECTANGULAR_PRISM -> {
                val l = lengthText.replace(',', '.').toDoubleOrNull() ?: 0.0
                val w = widthText.replace(',', '.').toDoubleOrNull() ?: 0.0
                val h = heightText.replace(',', '.').toDoubleOrNull() ?: 0.0
                MassVolumeCalculator.calculatePrism(l, w, h, dimUnit, density)
            }
            SolidGeometry.SOLID_CYLINDER -> {
                val d = diamText.replace(',', '.').toDoubleOrNull() ?: 0.0
                val h = heightText.replace(',', '.').toDoubleOrNull() ?: 0.0
                MassVolumeCalculator.calculateSolidCylinder(d, h, dimUnit, density)
            }
            SolidGeometry.HOLLOW_CYLINDER -> {
                val od = diamText.replace(',', '.').toDoubleOrNull() ?: 0.0
                val id = innerDiamText.replace(',', '.').toDoubleOrNull() ?: 0.0
                val h = heightText.replace(',', '.').toDoubleOrNull() ?: 0.0
                MassVolumeCalculator.calculateHollowCylinder(od, id, h, dimUnit, density)
            }
            SolidGeometry.SPHERE -> {
                val d = diamText.replace(',', '.').toDoubleOrNull() ?: 0.0
                MassVolumeCalculator.calculateSphere(d, dimUnit, density)
            }
            SolidGeometry.PLATE -> {
                val l = lengthText.replace(',', '.').toDoubleOrNull() ?: 0.0
                val w = widthText.replace(',', '.').toDoubleOrNull() ?: 0.0
                val t = plateThicknessMm.replace(',', '.').toDoubleOrNull() ?: 0.0
                MassVolumeCalculator.calculatePlate(l, w, t, dimUnit, density)
            }
        }

        when (outcome) {
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

    Column(modifier = modifier.fillMaxSize().testTag("mass_volume_screen")) {
        TopAppBar(
            title = { Text("Masse & Volume", fontWeight = FontWeight.Bold) },
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
            // Geometry Selector
            item {
                Text("Forme géométrique", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SolidGeometry.entries.take(3).forEach { geom ->
                        FilterChip(
                            selected = selectedGeometry == geom,
                            onClick = { selectedGeometry = geom; calculate() },
                            label = { Text(geom.name.substringBefore("_").lowercase().capitalize()) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryAccent.copy(alpha = 0.2f),
                                selectedLabelColor = PrimaryAccent
                            )
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SolidGeometry.entries.drop(3).forEach { geom ->
                        FilterChip(
                            selected = selectedGeometry == geom,
                            onClick = { selectedGeometry = geom; calculate() },
                            label = { Text(geom.name.substringBefore("_").lowercase().capitalize()) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryAccent.copy(alpha = 0.2f),
                                selectedLabelColor = PrimaryAccent
                            )
                        )
                    }
                }
            }

            // Material Preset Selector
            item {
                Text("Masse volumique du matériau", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(4.dp))
                ExposedDropdownMenuBox(
                    expanded = isPresetDropdownExpanded,
                    onExpandedChange = { isPresetDropdownExpanded = it }
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
                            Text(selectedPresetName, style = MaterialTheme.typography.bodyMedium)
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = isPresetDropdownExpanded)
                        }
                    }

                    ExposedDropdownMenu(
                        expanded = isPresetDropdownExpanded,
                        onDismissRequest = { isPresetDropdownExpanded = false }
                    ) {
                        MaterialPresets.list.forEach { preset ->
                            DropdownMenuItem(
                                text = { Text("${preset.name} (${preset.densityKgM3.toInt()} kg/m³)") },
                                onClick = {
                                    selectedPresetName = preset.name
                                    densityKgM3Text = preset.densityKgM3.toInt().toString()
                                    isPresetDropdownExpanded = false
                                    calculate()
                                }
                            )
                        }
                    }
                }
            }

            item {
                EngineeringInputField(
                    label = "Masse volumique personnalisée (ρ)",
                    valueText = densityKgM3Text,
                    onValueChange = { densityKgM3Text = it; selectedPresetName = "Personnalisé" },
                    unit = "kg/m³"
                )
            }

            // Dimensions according to selected geometry
            when (selectedGeometry) {
                SolidGeometry.RECTANGULAR_PRISM -> {
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            EngineeringInputField("Longueur (L)", lengthText, { lengthText = it }, modifier = Modifier.weight(1f), unit = dimUnit.symbol)
                            EngineeringInputField("Largeur (w)", widthText, { widthText = it }, modifier = Modifier.weight(1f), unit = dimUnit.symbol)
                        }
                    }
                    item {
                        EngineeringInputField("Hauteur (h)", heightText, { heightText = it }, unit = dimUnit.symbol)
                    }
                }
                SolidGeometry.SOLID_CYLINDER -> {
                    item {
                        EngineeringInputField("Diamètre extérieur (D)", diamText, { diamText = it }, unit = dimUnit.symbol)
                    }
                    item {
                        EngineeringInputField("Hauteur / Longueur (h)", heightText, { heightText = it }, unit = dimUnit.symbol)
                    }
                }
                SolidGeometry.HOLLOW_CYLINDER -> {
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            EngineeringInputField("Diam. Extérieur (D_ext)", diamText, { diamText = it }, modifier = Modifier.weight(1f), unit = dimUnit.symbol)
                            EngineeringInputField("Diam. Intérieur (D_int)", innerDiamText, { innerDiamText = it }, modifier = Modifier.weight(1f), unit = dimUnit.symbol)
                        }
                    }
                    item {
                        EngineeringInputField("Longueur / Hauteur (h)", heightText, { heightText = it }, unit = dimUnit.symbol)
                    }
                }
                SolidGeometry.SPHERE -> {
                    item {
                        EngineeringInputField("Diamètre (D)", diamText, { diamText = it }, unit = dimUnit.symbol)
                    }
                }
                SolidGeometry.PLATE -> {
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            EngineeringInputField("Longueur (L)", lengthText, { lengthText = it }, modifier = Modifier.weight(1f), unit = dimUnit.symbol)
                            EngineeringInputField("Largeur (w)", widthText, { widthText = it }, modifier = Modifier.weight(1f), unit = dimUnit.symbol)
                        }
                    }
                    item {
                        EngineeringInputField("Épaisseur de tôle (e)", plateThicknessMm, { plateThicknessMm = it }, unit = "mm")
                    }
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
                    Text("Calculer le volume et la masse", fontWeight = FontWeight.Bold)
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
fun BeamScreen(
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var loadType by remember { mutableStateOf(BeamLoadType.CENTRAL_POINT_LOAD) }
    var spanText by remember { mutableStateOf("6.0") } // m
    var loadText by remember { mutableStateOf("25.0") } // kN or kN/m
    var youngModulusText by remember { mutableStateOf("210") } // GPa (Acier)
    var inertiaText by remember { mutableStateOf("8690") } // cm4 (ex: IPE 300)
    var sectionModulusText by remember { mutableStateOf("557") } // cm3

    var calculationResult by remember { mutableStateOf<CalculationResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun calculate() {
        val span = spanText.replace(',', '.').toDoubleOrNull()
        val load = loadText.replace(',', '.').toDoubleOrNull()
        val eGpa = youngModulusText.replace(',', '.').toDoubleOrNull()
        val iCm4 = inertiaText.replace(',', '.').toDoubleOrNull()
        val welCm3 = sectionModulusText.replace(',', '.').toDoubleOrNull()

        if (span == null || load == null || eGpa == null || iCm4 == null) {
            errorMessage = "Tous les champs principaux doivent contenir des nombres valides."
            return
        }

        when (val outcome = BeamBendingCalculator.calculate(loadType, span, load, eGpa, iCm4, welCm3)) {
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

    Column(modifier = modifier.fillMaxSize().testTag("beam_screen")) {
        TopAppBar(
            title = { Text("Poutre sur deux appuis", fontWeight = FontWeight.Bold) },
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
                Text("Cas de chargement", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = loadType == BeamLoadType.CENTRAL_POINT_LOAD,
                        onClick = { loadType = BeamLoadType.CENTRAL_POINT_LOAD; calculate() },
                        label = { Text("Charge ponctuelle centrée (F)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryAccent.copy(alpha = 0.2f),
                            selectedLabelColor = PrimaryAccent
                        )
                    )
                    FilterChip(
                        selected = loadType == BeamLoadType.UNIFORM_DISTRIBUTED_LOAD,
                        onClick = { loadType = BeamLoadType.UNIFORM_DISTRIBUTED_LOAD; calculate() },
                        label = { Text("Charge répartie (q)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryAccent.copy(alpha = 0.2f),
                            selectedLabelColor = PrimaryAccent
                        )
                    )
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EngineeringInputField("Portée entre appuis (L)", spanText, { spanText = it }, modifier = Modifier.weight(1f), unit = "m")
                    EngineeringInputField(
                        label = if (loadType == BeamLoadType.CENTRAL_POINT_LOAD) "Force ponctuelle (F)" else "Charge linéique (q)",
                        valueText = loadText,
                        onValueChange = { loadText = it },
                        modifier = Modifier.weight(1f),
                        unit = if (loadType == BeamLoadType.CENTRAL_POINT_LOAD) "kN" else "kN/m"
                    )
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EngineeringInputField("Module de Young (E)", youngModulusText, { youngModulusText = it }, modifier = Modifier.weight(1f), unit = "GPa", helperText = "Acier: 210, Alu: 70")
                    EngineeringInputField("Inertie quadratique (I_z)", inertiaText, { inertiaText = it }, modifier = Modifier.weight(1f), unit = "cm⁴")
                }
            }

            item {
                EngineeringInputField(
                    label = "Module de flexion élastique W_el (Optionnel)",
                    valueText = sectionModulusText,
                    onValueChange = { sectionModulusText = it },
                    unit = "cm³",
                    helperText = "Calcule la contrainte normale σ_max = M_max / W_el"
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
                    Text("Calculer moment fléchissant & flèche", fontWeight = FontWeight.Bold)
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
