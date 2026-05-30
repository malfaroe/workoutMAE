package com.mae.workoutmae.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun StarRating(
    valor: Int,
    onValorChange: (Int) -> Unit,
    max: Int = 5,
    label: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Row {
            for (i in 1..max) {
                Icon(
                    imageVector = if (i <= valor) Icons.Filled.Star else Icons.Outlined.StarOutline,
                    contentDescription = null,
                    tint = if (i <= valor) Color(0xFFFFC107) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { onValorChange(i) }
                        .padding(2.dp),
                )
            }
        }
    }
}
