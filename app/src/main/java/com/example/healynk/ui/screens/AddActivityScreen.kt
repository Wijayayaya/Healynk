package com.example.healynk.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.healynk.models.ActivityEntry
import com.example.healynk.ui.components.ActivityTypeDropdown
import com.example.healynk.ui.components.NumberTextField
import com.example.healynk.ui.components.TextAreaField
import com.example.healynk.utils.Constants

@Composable
fun AddActivityScreen(
    onSave: (ActivityEntry) -> Unit,
    onCancel: () -> Unit
) {
    var type by remember { mutableStateOf(Constants.ACTIVITY_TYPES.first()) }
    var duration by remember { mutableStateOf("") }
    var steps by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Catat aktivitas", style = MaterialTheme.typography.titleLarge)
        ActivityTypeDropdown(
            label = "Jenis aktivitas",
            items = Constants.ACTIVITY_TYPES,
            selected = type,
            onTypeSelected = { type = it },
            modifier = Modifier.fillMaxWidth()
        )
        NumberTextField(value = duration, onValueChange = { duration = it }, label = "Durasi (menit)", modifier = Modifier.fillMaxWidth())
        NumberTextField(value = steps, onValueChange = { steps = it }, label = "Langkah", modifier = Modifier.fillMaxWidth())
        NumberTextField(value = calories, onValueChange = { calories = it }, label = "Kalori terbakar", modifier = Modifier.fillMaxWidth())
        TextAreaField(value = notes, onValueChange = { notes = it }, label = "Catatan")
        Spacer(modifier = Modifier.height(8.dp))
        RowButtons(
            onCancel = onCancel,
            onSave = {
                onSave(
                    ActivityEntry(
                        type = type,
                        durationMinutes = duration.toIntOrNull(),
                        steps = steps.toIntOrNull(),
                        caloriesBurned = calories.toIntOrNull(),
                        notes = notes.ifBlank { null }
                    )
                )
            }
        )
    }
}

@Composable
private fun RowButtons(onCancel: () -> Unit, onSave: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text("Batal") }
        Button(onClick = onSave, modifier = Modifier.weight(1f)) { Text("Simpan") }
    }
}
