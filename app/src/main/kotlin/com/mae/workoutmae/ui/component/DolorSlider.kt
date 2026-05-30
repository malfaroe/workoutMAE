package com.mae.workoutmae.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mae.workoutmae.ui.theme.dolorColor

@Composable
fun DolorSlider(
    valor: Int,
    onValorChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Dolor (0–10)",
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(
                text = valor.toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = dolorColor(valor),
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            for (i in 0..10) {
                val seleccionado = i == valor
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .background(if (seleccionado) dolorColor(i) else dolorColor(i).copy(alpha = 0.18f))
                        .border(
                            width = if (seleccionado) 2.dp else 0.dp,
                            color = if (seleccionado) dolorColor(i) else Color.Transparent,
                            shape = CircleShape,
                        )
                        .clickable { onValorChange(i) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = i.toString(),
                        fontSize = 11.sp,
                        fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal,
                        color = if (seleccionado) Color.White else dolorColor(i),
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Sin dolor", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Máximo", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
