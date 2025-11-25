package com.example.healynk.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.healynk.viewmodel.UiState

@Composable
fun AnalyticsScreen(uiState: UiState) {
    val latest = uiState.latestMeasurement

    // asumsi: bloodPressureTrend: List<Pair<String, Pair<Int?, Int?>>>
    val avgBpPairs = uiState.bloodPressureTrend.map { it.second }
    val avgSystolic = avgBpPairs.mapNotNull { it.first }.averageOrNull()
    val avgDiastolic = avgBpPairs.mapNotNull { it.second }.averageOrNull()

    val avgGlucose = uiState.glucoseTrend.mapNotNull { it.second }.averageOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Analytics", style = MaterialTheme.typography.headlineSmall)

        // Row 1: kalori masuk & aktivitas
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            AnalyticsSummaryCard(
                title = "Kalori masuk",
                value = "${uiState.dailyCalories} kcal",
                subtitle = "Target ${uiState.dailyCaloriesGoal}",
                modifier = Modifier.weight(1f)
            )
            AnalyticsSummaryCard(
                title = "Aktivitas",
                value = "${uiState.dailyActivityCalories} kcal",
                subtitle = "${uiState.summaryDuration} mnt",
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: tekanan darah & gula rata2
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val bpText = if (avgSystolic != null && avgDiastolic != null) {
                "${avgSystolic.toInt()}/${avgDiastolic.toInt()}"
            } else {
                "-"
            }

            AnalyticsSummaryCard(
                title = "Tekanan darah rata²",
                value = bpText,
                subtitle = "Data 7 hari",
                modifier = Modifier.weight(1f)
            )
            AnalyticsSummaryCard(
                title = "Gula rata²",
                value = avgGlucose?.toInt()?.toString() ?: "-",
                subtitle = "mg/dL",
                modifier = Modifier.weight(1f)
            )
        }

        // Pengukuran terakhir
        latest?.let {
            Card(colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Pengukuran terakhir", style = MaterialTheme.typography.titleMedium)
                    Text("Berat: ${it.weightKg ?: "-"} kg")
                    Text("Tekanan: ${it.systolic ?: "-"}/${it.diastolic ?: "-"} mmHg")
                    Text("Gula: ${it.glucoseMgDl ?: "-"} mg/dL")
                }
            }
        }

        // Tren mingguan
        TrendList(
            title = "Kalori makanan 7 hari",
            entries = uiState.weeklyFoodChart
        )
        TrendList(
            title = "Kalori aktivitas 7 hari",
            entries = uiState.weeklyActivityChart
        )
        TrendList(
            title = "Tren berat",
            entries = uiState.weightTrend.map { it.first to it.second.toInt() }
        )
    }
}

@Composable
private fun AnalyticsSummaryCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TrendList(title: String, entries: List<Pair<String, Int>>) {
    if (entries.isEmpty()) return

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Card {
            LazyColumn {
                items(entries) { (day, value) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(day)
                        Text("$value")
                    }
                    Divider()
                }
            }
        }
    }
}

private fun List<Int?>.averageOrNull(): Double? {
    val filtered = this.filterNotNull()
    return if (filtered.isNotEmpty()) filtered.average() else null
}
