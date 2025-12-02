package com.example.healynk.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healynk.models.FoodEntry
import com.example.healynk.viewmodel.UiState
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.max

private enum class AnalyticsRange(val label: String) {
    WEEK("Minggu"),
    MONTH("Bulan"),
    YEAR("Tahun")
}

@Composable
fun AnalyticsScreen(uiState: UiState) {
    var selectedRange by remember { mutableStateOf(AnalyticsRange.WEEK) }
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(AnalyticsGradientTop, AnalyticsGradientBottom)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Analitik",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            AnalyticsRangeSelector(selectedRange = selectedRange) { selectedRange = it }
            Spacer(modifier = Modifier.height(20.dp))
            CalorieTrendsCard(uiState = uiState, range = selectedRange)
            Spacer(modifier = Modifier.height(16.dp))
            MealFrequencyCard(uiState = uiState)
            Spacer(modifier = Modifier.height(16.dp))
            BmiScoreCard(uiState = uiState)
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AnalyticsRangeSelector(
    selectedRange: AnalyticsRange,
    onRangeSelected: (AnalyticsRange) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            AnalyticsRange.values().forEach { range ->
                val isSelected = range == selectedRange
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (isSelected) TabSelected else Color.Transparent)
                        .clickable { onRangeSelected(range) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = range.label,
                        color = if (isSelected) Color.White else TabUnselectedText,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun CalorieTrendsCard(uiState: UiState, range: AnalyticsRange) {
    val points = remember(uiState.weeklyFoodChart, uiState.weeklyActivityChart) {
        buildCalorieTrendPoints(uiState)
    }

    Card(
        colors = CardDefaults.cardColors(Color.White),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Tren Kalori",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0D1F2D)
            )
            Text(
                text = "Ringkasan ${range.label}",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF9A9A9A)
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (points.isEmpty()) {
                Text(
                    text = "Data kalori belum tersedia",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF9A9A9A),
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            } else {
                DualCalorieLineChart(points = points)
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    LegendItem(color = IntakeLineColor, label = "Kalori Masuk")
                    LegendItem(color = BurnLineColor, label = "Kalori Terbakar")
                }
            }
        }
    }
}

@Composable
private fun MealFrequencyCard(uiState: UiState) {
    val recentMeals = remember(uiState.foods) { foodsWithinLastWeek(uiState.foods) }
    val groupedMeals = remember(recentMeals) { recentMeals.groupBy { it.mealSlot() } }

    Card(
        colors = CardDefaults.cardColors(Color.White),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Frekuensi Makan",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0D1F2D)
            )
            Spacer(modifier = Modifier.height(16.dp))

            MealFrequencyItem(
                icon = "🌅",
                iconBg = Color(0xFFFFF3E0),
                mealName = "Sarapan",
                mealsCount = groupedMeals[MealSlot.BREAKFAST]?.size ?: 0,
                avgCalories = groupedMeals[MealSlot.BREAKFAST]?.averageCalories() ?: 0
            )
            Spacer(modifier = Modifier.height(12.dp))
            MealFrequencyItem(
                icon = "☀️",
                iconBg = Color(0xFFFFEBEE),
                mealName = "Makan Siang",
                mealsCount = groupedMeals[MealSlot.LUNCH]?.size ?: 0,
                avgCalories = groupedMeals[MealSlot.LUNCH]?.averageCalories() ?: 0
            )
            Spacer(modifier = Modifier.height(12.dp))
            MealFrequencyItem(
                icon = "🌙",
                iconBg = Color(0xFFE8EAF6),
                mealName = "Makan Malam",
                mealsCount = groupedMeals[MealSlot.DINNER]?.size ?: 0,
                avgCalories = groupedMeals[MealSlot.DINNER]?.averageCalories() ?: 0
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
                    color = Color(0xFF0D1F2D)
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
                text = "$avgCalories kkal",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0D1F2D)
            )
            Text(
                text = "rata-rata kkal",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF999999)
            )
        }
    }
}

@Composable
private fun BmiScoreCard(uiState: UiState) {
    val latestMeasurement = uiState.latestMeasurement
    val height = latestMeasurement?.heightCm
    val weight = latestMeasurement?.weightKg
    val bmiValue = uiState.latestBmi ?: calculateBmiValue(height, weight)
    val bmiText = bmiValue?.let { String.format("%.1f", it) } ?: "--"
    val status = bmiValue?.let { bmiClassification(it) } ?: "Tambahkan tinggi & berat"

    Card(
        colors = CardDefaults.cardColors(Color.White),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Skor BMI",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0D1F2D)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = status,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF26A69A)
                )
                Spacer(modifier = Modifier.height(24.dp))
                BmiMetricRow(label = "Tinggi Badan", value = height?.let { "${String.format("%.0f", it)} cm" } ?: "--")
                Spacer(modifier = Modifier.height(12.dp))
                BmiMetricRow(label = "Berat Badan", value = weight?.let { "${String.format("%.1f", it)} kg" } ?: "--")
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(start = 16.dp)
            ) {
                Canvas(modifier = Modifier.size(130.dp)) {
                    val strokeWidth = 14.dp.toPx()
                    drawArc(
                        color = Color(0xFFEAEAEA),
                        startAngle = 150f,
                        sweepAngle = 240f,
                        useCenter = false,
                        style = Stroke(strokeWidth, cap = StrokeCap.Round)
                    )
                    val sweep = ((bmiValue ?: 0.0) / BMI_MAX_REFERENCE).coerceIn(0.0, 1.0).toFloat() * 240f
                    drawArc(
                        color = BmiArcColor,
                        startAngle = 150f,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = Stroke(strokeWidth, cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = bmiText,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D1F2D)
                    )
                    Text(
                        text = "BMI",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF7A7A7A)
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF606060),
            modifier = Modifier.padding(start = 6.dp)
        )
    }
}

@Composable
private fun DualCalorieLineChart(points: List<CalorieTrendPoint>) {
    Column {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            if (points.isEmpty()) return@Canvas
            val maxValue = points.maxOf { max(it.intake, it.burned) }.takeIf { it > 0f } ?: 1f
            val horizontalGap = if (points.size == 1) 0f else size.width / (points.size - 1)
            val chartHeight = size.height

            fun Path.plotLine(selector: (CalorieTrendPoint) -> Float) {
                points.forEachIndexed { index, point ->
                    val x = horizontalGap * index
                    val value = selector(point)
                    val y = chartHeight - (value / maxValue) * chartHeight
                    if (index == 0) moveTo(x, y) else lineTo(x, y)
                }
            }

            val intakePath = Path().apply { plotLine { it.intake } }
            val burnPath = Path().apply { plotLine { it.burned } }

            drawPath(
                path = intakePath,
                color = IntakeLineColor,
                style = Stroke(width = 6f, cap = StrokeCap.Round)
            )
            drawPath(
                path = burnPath,
                color = BurnLineColor,
                style = Stroke(width = 6f, cap = StrokeCap.Round)
            )

            points.forEachIndexed { index, point ->
                val x = horizontalGap * index
                val intakeY = chartHeight - (point.intake / maxValue) * chartHeight
                val burnY = chartHeight - (point.burned / maxValue) * chartHeight
                drawCircle(color = IntakeLineColor, radius = 8f, center = androidx.compose.ui.geometry.Offset(x, intakeY))
                drawCircle(color = BurnLineColor, radius = 8f, center = androidx.compose.ui.geometry.Offset(x, burnY))
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            points.forEach {
                Text(
                    text = it.label.take(3).uppercase(),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF9A9A9A),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun BmiMetricRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF7A7A7A)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0D1F2D)
        )
    }
}

private data class CalorieTrendPoint(
    val label: String,
    val intake: Float,
    val burned: Float
)

private fun buildCalorieTrendPoints(uiState: UiState): List<CalorieTrendPoint> {
    val intakeData = uiState.weeklyFoodChart.takeLast(7)
    val burnData = uiState.weeklyActivityChart.takeLast(7)
    val orderedKeys = mutableListOf<String>()
    intakeData.forEach { if (!orderedKeys.contains(it.first)) orderedKeys.add(it.first) }
    burnData.forEach { if (!orderedKeys.contains(it.first)) orderedKeys.add(it.first) }
    if (orderedKeys.isEmpty()) return emptyList()
    val intakeMap = intakeData.toMap()
    val burnMap = burnData.toMap()
    return orderedKeys.map { day ->
        CalorieTrendPoint(
            label = day,
            intake = intakeMap[day]?.toFloat() ?: 0f,
            burned = burnMap[day]?.toFloat() ?: 0f
        )
    }
}

private enum class MealSlot { BREAKFAST, LUNCH, DINNER }

private fun FoodEntry.mealSlot(): MealSlot {
    val hour = Instant.ofEpochMilli(timestamp).atZone(analyticsZoneId).hour
    return when {
        hour < 11 -> MealSlot.BREAKFAST
        hour < 17 -> MealSlot.LUNCH
        else -> MealSlot.DINNER
    }
}

private fun foodsWithinLastWeek(foods: List<FoodEntry>): List<FoodEntry> {
    val threshold = LocalDate.now(analyticsZoneId)
        .minusDays((DAYS_IN_WEEK - 1).toLong())
        .atStartOfDay(analyticsZoneId)
        .toInstant()
        .toEpochMilli()
    return foods.filter { it.timestamp >= threshold }
}

private fun List<FoodEntry>.averageCalories(): Int {
    if (isEmpty()) return 0
    return (sumOf { it.calories } / size.toFloat()).toInt()
}

private fun calculateBmiValue(heightCm: Double?, weightKg: Double?): Double? {
    if (heightCm == null || weightKg == null || heightCm == 0.0) return null
    val heightM = heightCm / 100.0
    return weightKg / (heightM * heightM)
}

private fun bmiClassification(value: Double): String = when {
    value < 18.5 -> "Kurus"
    value < 25 -> "Normal"
    value < 30 -> "Berat berlebih"
    else -> "Obesitas"
}

private const val DAYS_IN_WEEK = 7
private const val BMI_MAX_REFERENCE = 40.0
private val analyticsZoneId: ZoneId = ZoneId.systemDefault()
private val AnalyticsGradientTop = Color(0xFF00897B)
private val AnalyticsGradientBottom = Color(0xFF26A69A)
private val IntakeLineColor = Color(0xFFFF7043)
private val BurnLineColor = Color(0xFF26A69A)
private val TabBackground = Color.White
private val TabSelected = Color(0xFF0F9B8E)
private val TabUnselectedText = Color(0xFF5F6F69)
private val BmiArcColor = Color(0xFF26A69A)
