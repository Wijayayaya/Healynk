package com.example.healynk.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FoodBank
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.healynk.R
import com.example.healynk.ui.components.CalorieRing
import com.example.healynk.ui.components.QuickActionButton
import com.example.healynk.ui.components.SummaryCardSmall
import com.example.healynk.utils.Constants
import com.example.healynk.viewmodel.UiState
import java.util.Locale

@Composable
fun HomeScreen(
    uiState: UiState,
    onAddMeasurement: () -> Unit,
    onAddBodyStats: () -> Unit,
    onAddActivity: () -> Unit,
    onAddFood: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        HeaderSection(uiState)
        Spacer(modifier = Modifier.height(12.dp))
        CalorieCard(uiState)
        Spacer(modifier = Modifier.height(12.dp))
        SummaryRow(uiState)
        Spacer(modifier = Modifier.height(12.dp))
        QuickActionsRow(onAddMeasurement, onAddBodyStats, onAddActivity, onAddFood)
    }
}
@Composable
private fun HeaderSection(uiState: UiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.ic_healynk_logo),
                contentDescription = Constants.APP_NAME,
                modifier = Modifier.height(40.dp)
            )
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(text = Constants.APP_NAME, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = "Halo, ${uiState.userEmail.substringBefore('@', missingDelimiterValue = "User")}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CalorieCard(uiState: UiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Kalori Harian yang Terbakar", style = MaterialTheme.typography.titleMedium)
            CalorieRing(
                consumed = uiState.dailyActivityCalories,
                target = uiState.dailyCaloriesGoal,
                modifier = Modifier.padding(top = 16.dp),
                label = "Kalori Terbakar"
            )
        }
    }
}

@Composable
private fun SummaryRow(uiState: UiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryCardSmall(
            modifier = Modifier.weight(1f),
            title = "Aktivitas fisik",
            subtitle = "Jarak: ${"%.1f".format(uiState.summaryDistanceKm)} km\nDurasi: ${uiState.summaryDuration} mnt",
            icon = Icons.Default.FitnessCenter
        )
        val bpText = uiState.latestMeasurement?.let { latest ->
            val bp = if (latest.systolic != null && latest.diastolic != null) "BP ${latest.systolic}/${latest.diastolic} mmHg" else null
            val glucose = latest.glucoseMgDl?.let { "Gula $it mg/dL" }
            val bmiValue = uiState.latestBmi
            val bmiText = bmiValue?.let { String.format(Locale.getDefault(), "BMI %.1f", it) }
            val bmiStatus = bmiValue?.let {
                when {
                    it < 18.5 -> "Berat badan rendah"
                    it < 25 -> "Berat badan ideal"
                    it < 30 -> "Berat badan berlebih"
                    else -> "Obesitas"
                }
            }
            listOfNotNull(bp, glucose, bmiText, bmiStatus).ifEmpty { listOf("Belum ada data") }.joinToString(" · ")
        } ?: "Belum ada data"
        SummaryCardSmall(
            modifier = Modifier.weight(1f),
            title = "Tekanan darah & gula",
            subtitle = bpText,
            icon = Icons.Default.HealthAndSafety
        )
    }
}

@Composable
private fun QuickActionsRow(
    onAddMeasurement: () -> Unit,
    onAddBodyStats: () -> Unit,
    onAddActivity: () -> Unit,
    onAddFood: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Quick actions", style = MaterialTheme.typography.titleMedium)
            Text(text = "Lihat semua", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(
                listOf(
                    Triple("Tinggi & berat", Icons.Default.MonitorWeight, onAddBodyStats),
                    Triple("Tekanan darah", Icons.Default.HealthAndSafety, onAddMeasurement),
                    Triple("Aktivitas", Icons.Default.FitnessCenter, onAddActivity),
                    Triple("Makanan", Icons.Default.FoodBank, onAddFood)
                )
            ) { (label, icon, action) ->
                QuickActionButton(label = label, icon = icon, onClick = action)
            }
        }
    }
}
