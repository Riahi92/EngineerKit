package com.example.feature.calculator

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.SwapVert
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.formatting.NumberFormatter
import com.example.domain.units.EngineeringUnit
import com.example.domain.units.UnitDimension
import com.example.domain.units.UnitEngine
import com.example.domain.units.UnitRegistry
import com.example.ui.theme.PrimaryAccent
import com.example.ui.theme.SecondaryAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConverterScreen(
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDimension by remember { mutableStateOf(UnitDimension.PRESSURE) }
    var availableUnits by remember(selectedDimension) {
        mutableStateOf(UnitRegistry.getUnitsForDimension(selectedDimension))
    }

    var fromUnit by remember(selectedDimension) {
        mutableStateOf(availableUnits.firstOrNull() ?: UnitRegistry.BAR)
    }
    var toUnit by remember(selectedDimension) {
        mutableStateOf(availableUnits.getOrNull(2) ?: availableUnits.lastOrNull() ?: UnitRegistry.MPA)
    }

    var inputValueText by remember { mutableStateOf("1") }
    val clipboardManager = LocalClipboardManager.current

    val numericInput = inputValueText.replace(',', '.').toDoubleOrNull()
    val convertedValue = if (numericInput != null) {
        try {
            UnitEngine.convert(numericInput, fromUnit, toUnit)
        } catch (_: Exception) {
            Double.NaN
        }
    } else Double.NaN

    fun swapUnits() {
        val temp = fromUnit
        fromUnit = toUnit
        toUnit = temp
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("unit_converter_screen")
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Convertisseur d'unités",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
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
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Category Chips Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    UnitDimension.entries.forEach { dim ->
                        val isSelected = dim == selectedDimension
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedDimension = dim
                                val units = UnitRegistry.getUnitsForDimension(dim)
                                availableUnits = units
                                fromUnit = units.firstOrNull() ?: UnitRegistry.M
                                toUnit = units.getOrNull(1) ?: units.firstOrNull() ?: UnitRegistry.M
                            },
                            label = { Text(dim.displayName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryAccent.copy(alpha = 0.2f),
                                selectedLabelColor = PrimaryAccent
                            )
                        )
                    }
                }
            }

            // Source Input Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Valeur d'origine",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = inputValueText,
                                onValueChange = { inputValueText = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("converter_input_field"),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            UnitPicker(
                                selectedUnit = fromUnit,
                                availableUnits = availableUnits,
                                onUnitSelected = { fromUnit = it },
                                modifier = Modifier.width(130.dp)
                            )
                        }
                    }
                }
            }

            // Swap Button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Surface(
                        modifier = Modifier
                            .clickable(onClick = ::swapUnits)
                            .testTag("converter_swap_button"),
                        shape = RoundedCornerShape(12.dp),
                        color = SecondaryAccent.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, SecondaryAccent.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapVert,
                                contentDescription = "Inverser",
                                tint = SecondaryAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Inverser les unités",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = SecondaryAccent
                            )
                        }
                    }
                }
            }

            // Target Result Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, PrimaryAccent.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Valeur convertie",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            UnitPicker(
                                selectedUnit = toUnit,
                                availableUnits = availableUnits,
                                onUnitSelected = { toUnit = it },
                                modifier = Modifier.width(130.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (convertedValue.isNaN()) "Entrée invalide"
                                else "${NumberFormatter.format(convertedValue, 6)} ${toUnit.symbol}",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp
                                ),
                                color = if (convertedValue.isNaN()) MaterialTheme.colorScheme.error else PrimaryAccent,
                                modifier = Modifier.weight(1f)
                            )

                            if (!convertedValue.isNaN()) {
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(
                                            AnnotatedString("${NumberFormatter.format(convertedValue, 6)} ${toUnit.symbol}")
                                        )
                                    },
                                    modifier = Modifier.testTag("copy_converted_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copier",
                                        tint = PrimaryAccent
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Dimension Summary & Equivalences
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Grandeur physique : ${selectedDimension.displayName}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Conversion dimensionnelle exacte selon la norme ISO 80000. Facteur direct : 1 ${fromUnit.symbol} = ${NumberFormatter.format(UnitEngine.convert(1.0, fromUnit, toUnit), 6)} ${toUnit.symbol}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitPicker(
    selectedUnit: EngineeringUnit,
    availableUnits: List<EngineeringUnit>,
    onUnitSelected: (EngineeringUnit) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedUnit.symbol,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            }
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            availableUnits.forEach { unit ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(
                                text = "${unit.name} (${unit.symbol})",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    },
                    onClick = {
                        onUnitSelected(unit)
                        expanded = false
                    }
                )
            }
        }
    }
}
