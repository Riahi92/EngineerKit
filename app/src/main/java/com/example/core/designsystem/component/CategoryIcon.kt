package com.example.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.core.model.CategoryId

@Composable
fun CategoryIcon(
    categoryId: CategoryId,
    tint: Color,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    iconSize: Dp = 22.dp,
    showContainer: Boolean = true
) {
    val imageVector = when (categoryId) {
        CategoryId.MECANIQUE -> Icons.Default.Build
        CategoryId.ELECTRIQUE -> Icons.Default.Bolt
        CategoryId.ELECTRONIQUE -> Icons.Default.Memory
        CategoryId.INDUSTRIEL -> Icons.Default.PrecisionManufacturing
        CategoryId.CIVIL -> Icons.Default.Apartment
        CategoryId.HYDRAULIQUE -> Icons.Default.WaterDrop
        CategoryId.THERMIQUE -> Icons.Default.Thermostat
        CategoryId.CHIMIQUE -> Icons.Default.Science
        CategoryId.INFORMATIQUE -> Icons.Default.Code
        CategoryId.AUTOMATISME -> Icons.Default.Tune
        CategoryId.ENVIRONNEMENT -> Icons.Default.Eco
        CategoryId.MATHEMATIQUES -> Icons.Default.Calculate
    }

    if (showContainer) {
        Box(
            modifier = modifier
                .size(size)
                .clip(RoundedCornerShape(10.dp))
                .background(tint.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(iconSize)
            )
        }
    } else {
        Icon(
            imageVector = imageVector,
            contentDescription = null,
            tint = tint,
            modifier = modifier.size(iconSize)
        )
    }
}
