package com.example.healynk.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.healynk.models.Measurement
import com.example.healynk.utils.Formatters
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete

@Composable
fun MeasurementItem(
    measurement: Measurement,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    deleteIcon: ImageVector = Icons.Default.Delete
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = Formatters.formatDateTime(measurement.timestamp), style = MaterialTheme.typography.labelLarge)
                if (onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(imageVector = deleteIcon, contentDescription = "Hapus pengukuran")
                    }
                }
            }
            measurement.weightKg?.let {
                Text(text = "Berat: $it kg", fontWeight = FontWeight.SemiBold)
            }
            measurement.heightCm?.let {
                Text(text = "Tinggi: $it cm")
            }
            if (measurement.systolic != null && measurement.diastolic != null) {
                Text(text = "Tekanan darah: ${measurement.systolic}/${measurement.diastolic} mmHg")
            }
            measurement.glucoseMgDl?.let {
                Text(text = "Gula darah: $it mg/dL")
            }
            measurement.notes?.takeIf { it.isNotBlank() }?.let {
                Text(text = it, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
