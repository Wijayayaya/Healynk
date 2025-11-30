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
import com.example.healynk.models.Measurement
import com.example.healynk.ui.components.NumberTextField
import com.example.healynk.ui.components.TextAreaField

@Composable
fun AddBodyStatsScreen(
    onSave: (Measurement) -> Unit,
    onCancel: () -> Unit
) {
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Catat tinggi & berat", style = MaterialTheme.typography.titleLarge)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NumberTextField(
                value = weight,
                onValueChange = { weight = it },
                label = "Berat (kg)",
                allowDecimal = true,
                modifier = Modifier.weight(1f)
            )
            NumberTextField(
                value = height,
                onValueChange = { height = it },
                label = "Tinggi (cm)",
                allowDecimal = true,
                modifier = Modifier.weight(1f)
            )
        }
        TextAreaField(value = notes, onValueChange = { notes = it }, label = "Catatan")
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text("Batal") }
            Button(
                onClick = {
                    onSave(
                        Measurement(
                            weightKg = weight.toDoubleOrNull(),
                            heightCm = height.toDoubleOrNull(),
                            notes = notes.ifBlank { null }
                        )
                    )
                },
                modifier = Modifier.weight(1f)
            ) { Text("Simpan") }
        }
    }
}

