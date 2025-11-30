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
import androidx.compose.ui.unit.dp
import com.example.healynk.models.ActivityEntry
import com.example.healynk.utils.Formatters

@Composable
fun ActivityItem(
    activity: ActivityEntry,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    deleteIcon: ImageVector? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${activity.type} · ${Formatters.formatDateTime(activity.timestamp)}",
                    style = MaterialTheme.typography.titleMedium
                )
                if (onDelete != null && deleteIcon != null) {
                    IconButton(onClick = onDelete) {
                        Icon(imageVector = deleteIcon, contentDescription = "Delete activity")
                    }
                }
            }
            activity.durationMinutes?.let { Text("Durasi: $it menit") }
            activity.distanceKm?.let {
                Text("Jarak: ${Formatters.formatDistance(it)} km")
            }
            activity.pace?.let {
                Text("Pace: ${Formatters.formatPace(it)} mnt/km")
            }
            activity.caloriesBurned?.let { Text("Kalori: $it kcal") }
            activity.notes?.takeIf { it.isNotBlank() }?.let { Text(it) }
        }
    }
}
