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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.healynk.models.FoodEntry
import com.example.healynk.ui.components.NumberTextField
import com.example.healynk.ui.components.TextAreaField

@Composable
fun AddFoodScreen(
    onSave: (FoodEntry) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Catat makanan", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nama makanan") },
            modifier = Modifier.fillMaxWidth()
        )
        NumberTextField(value = calories, onValueChange = { calories = it }, label = "Kalori (kcal)")
        RowWithTwoFields(
            left = {
                NumberTextField(
                    value = carbs,
                    onValueChange = { carbs = it },
                    label = "Karbo (g)",
                    modifier = Modifier.fillMaxWidth()
                )
            },
            right = {
                NumberTextField(
                    value = protein,
                    onValueChange = { protein = it },
                    label = "Protein (g)",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        )
        NumberTextField(value = fat, onValueChange = { fat = it }, label = "Lemak (g)")
        TextAreaField(value = notes, onValueChange = { notes = it }, label = "Catatan")
        Spacer(modifier = Modifier.height(8.dp))
        RowButtons(onCancel = onCancel) {
            onSave(
                FoodEntry(
                    name = name,
                    calories = calories.toIntOrNull() ?: 0,
                    carbs = carbs.toIntOrNull(),
                    protein = protein.toIntOrNull(),
                    fat = fat.toIntOrNull(),
                    notes = notes.ifBlank { null }
                )
            )
        }
    }
}

@Composable
private fun RowWithTwoFields(left: @Composable () -> Unit, right: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) { left() }
        Column(modifier = Modifier.weight(1f)) { right() }
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
