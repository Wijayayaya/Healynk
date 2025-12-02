package com.example.healynk.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.healynk.models.FoodEntry
import com.example.healynk.utils.Formatters

@Composable
fun FoodItem(
    food: FoodEntry,
    onDelete: (() -> Unit)? = null,
    deleteIcon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = food.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                if (onDelete != null && deleteIcon != null) {
                    IconButton(onClick = onDelete) {
                        Icon(imageVector = deleteIcon, contentDescription = "Hapus catatan makanan")
                    }
                }
            }
            Text(text = "${food.calories} kkal · ${Formatters.formatDateTime(food.timestamp)}")
            food.carbs?.let { Text("Karbohidrat: $it g") }
            food.protein?.let { Text("Protein: $it g") }
            food.fat?.let { Text("Lemak: $it g") }
            food.notes?.takeIf { it.isNotBlank() }?.let { Text(it) }
        }
    }
}

