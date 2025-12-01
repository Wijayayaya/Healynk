package com.example.healynk.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healynk.viewmodel.UiState

@Composable
fun AnalyticsScreen(uiState: UiState) {
    val latest = uiState.latestMeasurement

    // asumsi: bloodPressureTrend: List<Pair<String, Pair<Int?, Int?>>>
    val avgBpPairs = uiState.bloodPressureTrend.map { it.second }
    val avgSystolic = avgBpPairs.mapNotNull { it.first }.averageOrNull()
    val avgDiastolic = avgBpPairs.mapNotNull { it.second }.averageOrNull()

    val avgGlucose = uiState.glucoseTrend.mapNotNull { it.second }.averageOrNull()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF00897B),
                        Color(0xFF26A69A)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Header
            Text(
                text = "Analisis Kesehatan",
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 28.sp),
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

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

            Spacer(modifier = Modifier.height(12.dp))

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

            Spacer(modifier = Modifier.height(20.dp))

            // Calorie Trends Chart
            CalorieTrendsCard(uiState = uiState)

            Spacer(modifier = Modifier.height(16.dp))

            // Meal Frequency Statistics
            MealFrequencyCard(uiState = uiState)

            Spacer(modifier = Modifier.height(16.dp))

            // Health Score Card
            HealthScoreCard(uiState = uiState)

            Spacer(modifier = Modifier.height(16.dp))

            // Pengukuran terakhir
            latest?.let {
                Card(
                    colors = CardDefaults.cardColors(Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Pengukuran terakhir",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00897B)
                        )
                        Text(
                            text = "Berat: ${it.weightKg ?: "-"} kg",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF555555)
                        )
                        Text(
                            text = "Tekanan: ${it.systolic ?: "-"}/${it.diastolic ?: "-"} mmHg",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF555555)
                        )
                        Text(
                            text = "Gula: ${it.glucoseMgDl ?: "-"} mg/dL",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF555555)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Tren berat
            TrendList(
                title = "Tren berat",
                entries = uiState.weightTrend.map { it.first to it.second.toInt() }
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
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
        colors = CardDefaults.cardColors(Color.White.copy(alpha = 0.95f)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF666666),
                fontSize = 13.sp
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                ),
                color = Color(0xFF2C2C2C)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF999999),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun TrendList(title: String, entries: List<Pair<String, Int>>) {
    if (entries.isEmpty()) return

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Card(
            colors = CardDefaults.cardColors(Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                entries.forEachIndexed { index, (day, value) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = day,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF555555),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "$value",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF2C2C2C),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (index < entries.size - 1) {
                        Divider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = Color(0xFFE0E0E0),
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalorieTrendsCard(uiState: UiState) {
    Card(
        colors = CardDefaults.cardColors(Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Grafik Asupan Kalori",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C2C2C)
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // Simple visualization with bars
            val weekData = uiState.weeklyFoodChart.takeLast(7)
            if (weekData.isNotEmpty()) {
                val maxCalories = weekData.maxOfOrNull { it.second } ?: 1
                weekData.forEach { (day, calories) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = day.take(3),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF666666),
                            modifier = Modifier.weight(0.15f)
                        )
                        Box(
                            modifier = Modifier
                                .weight(0.7f)
                                .height(24.dp)
                                .background(Color(0xFFF0F0F0), RoundedCornerShape(4.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(calories.toFloat() / maxCalories.toFloat())
                                    .height(24.dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0xFF00897B),
                                                Color(0xFF26A69A)
                                            )
                                        ),
                                        RoundedCornerShape(4.dp)
                                    )
                            )
                        }
                        Text(
                            text = "$calories",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2C2C2C),
                            modifier = Modifier.weight(0.15f)
                        )
                    }
                }
            } else {
                Text(
                    text = "Belum ada data",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF999999)
                )
            }
        }
    }
}

@Composable
private fun MealFrequencyCard(uiState: UiState) {
    Card(
        colors = CardDefaults.cardColors(Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Frekuensi Makan",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C2C2C)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Calculate meal frequency from food entries
            val totalMeals = uiState.weeklyFoodChart.size
            val avgCaloriesPerMeal = if (totalMeals > 0) {
                uiState.weeklyFoodChart.sumOf { it.second } / totalMeals
            } else 0

            MealFrequencyItem(
                icon = "🌅",
                iconBg = Color(0xFFFFF3E0),
                mealName = "Sarapan",
                mealsCount = (totalMeals * 0.3).toInt().coerceAtLeast(0),
                avgCalories = (avgCaloriesPerMeal * 0.8).toInt()
            )
            Spacer(modifier = Modifier.height(12.dp))
            MealFrequencyItem(
                icon = "☀️",
                iconBg = Color(0xFFFFEBEE),
                mealName = "Makan Siang",
                mealsCount = (totalMeals * 0.35).toInt().coerceAtLeast(0),
                avgCalories = (avgCaloriesPerMeal * 1.2).toInt()
            )
            Spacer(modifier = Modifier.height(12.dp))
            MealFrequencyItem(
                icon = "🌙",
                iconBg = Color(0xFFE8EAF6),
                mealName = "Makan Malam",
                mealsCount = (totalMeals * 0.35).toInt().coerceAtLeast(0),
                avgCalories = avgCaloriesPerMeal
            )
        }
    }
}

@Composable
private fun MealFrequencyItem(
    icon: String,
    iconBg: Color,
    mealName: String,
    mealsCount: Int,
    avgCalories: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .padding(end = 12.dp)
                    .background(iconBg, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text(text = icon, fontSize = 24.sp)
            }
            Column {
                Text(
                    text = mealName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2C2C2C)
                )
                Text(
                    text = "$mealsCount kali minggu ini",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF999999)
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$avgCalories",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C2C2C)
            )
            Text(
                text = "rata-rata",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF999999)
            )
        }
    }
}

@Composable
private fun HealthScoreCard(uiState: UiState) {
    // Calculate health score based on various metrics
    val hasRecentMeasurement = uiState.latestMeasurement != null
    val calorieGoalProgress = if (uiState.dailyCaloriesGoal > 0) {
        (uiState.dailyCalories.toFloat() / uiState.dailyCaloriesGoal * 100).toInt().coerceIn(0, 100)
    } else 0
    val activityScore = if (uiState.dailyActivityCalories > 200) 30 else (uiState.dailyActivityCalories / 200.0 * 30).toInt()
    val healthScore = (calorieGoalProgress * 0.4 + activityScore + (if (hasRecentMeasurement) 20 else 0)).toInt().coerceIn(0, 100)

    Card(
        colors = CardDefaults.cardColors(Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Skor Kesehatan",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C2C2C)
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                HealthScoreItem(
                    label = "vs Minggu Lalu",
                    value = "+5",
                    valueColor = Color(0xFF00897B)
                )
                Spacer(modifier = Modifier.height(16.dp))
                HealthScoreItem(
                    label = "Konsistensi",
                    value = "${uiState.weightTrend.size} hari",
                    valueColor = Color(0xFF2C2C2C)
                )
                Spacer(modifier = Modifier.height(16.dp))
                HealthScoreItem(
                    label = "Status",
                    value = when {
                        healthScore >= 80 -> "Sangat Baik"
                        healthScore >= 60 -> "Baik"
                        healthScore >= 40 -> "Cukup"
                        else -> "Perlu Ditingkatkan"
                    },
                    valueColor = Color(0xFF00897B)
                )
            }
            
            // Circular progress
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(start = 16.dp)
            ) {
                Canvas(
                    modifier = Modifier.size(120.dp)
                ) {
                    val strokeWidth = 12.dp.toPx()
                    drawCircle(
                        color = Color(0xFFF0F0F0),
                        style = Stroke(strokeWidth)
                    )
                    drawArc(
                        color = Color(0xFF00897B),
                        startAngle = -90f,
                        sweepAngle = (healthScore / 100f) * 360f,
                        useCenter = false,
                        style = Stroke(
                            strokeWidth,
                            cap = StrokeCap.Round
                        )
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "❤️",
                        fontSize = 28.sp
                    )
                    Text(
                        text = "$healthScore",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2C2C2C)
                    )
                }
            }
        }
    }
}

@Composable
private fun HealthScoreItem(
    label: String,
    value: String,
    valueColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF666666)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

private fun List<Int?>.averageOrNull(): Double? {
    val filtered = this.filterNotNull()
    return if (filtered.isNotEmpty()) filtered.average() else null
}
