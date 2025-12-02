package com.example.healynk.ui.screens

import android.graphics.Paint
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healynk.models.FoodEntry
import com.example.healynk.viewmodel.UiState
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

private enum class AnalyticsRange(val label: String) {
    WEEK("Minggu"),
    MONTH("Bulan"),
    YEAR("Tahun")
}

private enum class MetricKey(
    val label: String,
    val color: Color,
    val selector: (AnalyticsDayPoint) -> Float
) {
    CALORIE_INTAKE("Kalori Masuk", IntakeLineColor, { it.calorieIntake }),
    CALORIE_BURNED("Kalori Terbakar", BurnLineColor, { it.calorieBurned }),
    FAT("Lemak", FatColor, { it.fat }),
    PROTEIN("Protein", ProteinColor, { it.protein }),
    CARBS("Karbo", CarbsColor, { it.carbs }),
    PACE("Pace", PaceColor, { it.pace }),
    DURATION("Durasi Aktivitas", DurationColor, { it.durationMinutes }),
    DISTANCE("Jarak Tempuh", DistanceColor, { it.distanceKm }),
    BMI("BMI", BmiTrendColor, { it.bmi }),
    BLOOD_PRESSURE("Tekanan Darah", BloodPressureColor, { it.bloodPressure }),
    GLUCOSE("Gula Darah", GlucoseColor, { it.glucose });
}

@Composable
fun AnalyticsScreen(uiState: UiState) {
    var selectedRange by remember { mutableStateOf(AnalyticsRange.WEEK) }
    val scrollState = rememberScrollState()
    val dailyPoints = remember(uiState.foods, uiState.activities, uiState.measurements) {
        buildAnalyticsDayPoints(uiState)
    }

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
            HealthTrendsCard(points = dailyPoints, range = selectedRange)
            Spacer(modifier = Modifier.height(16.dp))
            MetricComparisonCard(points = dailyPoints, range = selectedRange)
            Spacer(modifier = Modifier.height(16.dp))
            MealFrequencyCard(uiState = uiState)
            Spacer(modifier = Modifier.height(16.dp))
            BmiScoreCard(uiState = uiState)
            Spacer(modifier = Modifier.height(16.dp))
            BloodPressureCard(uiState = uiState)
            Spacer(modifier = Modifier.height(16.dp))
            GlucoseCard(uiState = uiState)
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
private fun HealthTrendsCard(points: List<AnalyticsDayPoint>, range: AnalyticsRange) {
    var selectedMetrics by remember { mutableStateOf(setOf(MetricKey.CALORIE_INTAKE, MetricKey.CALORIE_BURNED)) }

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
                text = "Kesehatan",
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
                    text = "Data belum tersedia",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF9A9A9A),
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            } else {
                MetricFilterDropdown(
                    selected = selectedMetrics,
                    onSelectionChange = { selectedMetrics = it }
                )
                Spacer(modifier = Modifier.height(12.dp))
                val metricsList = selectedMetrics.toList().sortedBy { it.ordinal }
                if (metricsList.isEmpty()) {
                    Text(
                        text = "Pilih minimal satu data untuk ditampilkan",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF9A9A9A),
                        modifier = Modifier.padding(vertical = 32.dp)
                    )
                } else {
                    MultiMetricLineChart(points = points, metrics = metricsList)
                    Spacer(modifier = Modifier.height(12.dp))
                    AnalyticsChartLabels(points = points)
                    Spacer(modifier = Modifier.height(16.dp))
                    MetricsLegend(metrics = metricsList)
                }
            }
        }
    }
}

@Composable
private fun MetricComparisonCard(points: List<AnalyticsDayPoint>, range: AnalyticsRange) {
    var selectedMetrics by remember { mutableStateOf(setOf(MetricKey.CALORIE_INTAKE, MetricKey.CALORIE_BURNED)) }
    Card(
        colors = CardDefaults.cardColors(Color.White),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Perbandingan Data",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0D1F2D)
            )
            Text(
                text = "Ringkasan ${range.label}",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF9A9A9A)
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (points.isEmpty()) {
                Text(
                    text = "Belum ada data untuk dibandingkan",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF9A9A9A),
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            } else {
                MetricFilterDropdown(selected = selectedMetrics, onSelectionChange = { selectedMetrics = it })
                Spacer(modifier = Modifier.height(12.dp))
                val metricsList = selectedMetrics.toList().sortedBy { it.ordinal }
                when {
                    metricsList.isEmpty() -> Text(
                        text = "Pilih minimal satu data untuk ditampilkan",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF9A9A9A),
                        modifier = Modifier.padding(vertical = 24.dp)
                    )

                    !hasMetricsData(points, metricsList.toSet()) -> Text(
                        text = "Data untuk metrik yang dipilih belum tersedia",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF9A9A9A),
                        modifier = Modifier.padding(vertical = 24.dp)
                    )

                    else -> {
                        MetricComparisonBarChart(points = points, metrics = metricsList)
                        Spacer(modifier = Modifier.height(12.dp))
                        AnalyticsChartLabels(points = points)
                        Spacer(modifier = Modifier.height(16.dp))
                        MetricsLegend(metrics = metricsList)
                    }
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
private fun BloodPressureCard(uiState: UiState) {
    val latestSystolic = uiState.latestMeasurement?.systolic
    val latestDiastolic = uiState.latestMeasurement?.diastolic
    val (statusText, statusColor) = remember(latestSystolic, latestDiastolic) {
        bloodPressureStatus(latestSystolic, latestDiastolic)
    }
    val trend = remember(uiState.bloodPressureTrend) { uiState.bloodPressureTrend.takeLast(7) }

    Card(
        colors = CardDefaults.cardColors(Color.White),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Tekanan Darah",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0D1F2D)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Terakhir",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF9A9A9A)
                    )
                    Text(
                        text = formatBloodPressure(latestSystolic, latestDiastolic),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D1F2D)
                    )
                }
                StatusChip(text = statusText, color = statusColor)
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (trend.isEmpty()) {
                Text(
                    text = "Belum ada catatan tekanan darah",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF9A9A9A)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    trend.forEach { (label, values) ->
                        MeasurementTrendRowItem(
                            day = label.take(3).uppercase(),
                            value = formatBloodPressure(values.first, values.second)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GlucoseCard(uiState: UiState) {
    val latestGlucose = uiState.latestMeasurement?.glucoseMgDl
    val (statusText, statusColor) = remember(latestGlucose) { glucoseStatus(latestGlucose) }
    val trend = remember(uiState.glucoseTrend) { uiState.glucoseTrend.takeLast(7) }

    Card(
        colors = CardDefaults.cardColors(Color.White),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Gula Darah",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0D1F2D)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Terakhir",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF9A9A9A)
                    )
                    Text(
                        text = formatGlucose(latestGlucose),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D1F2D)
                    )
                }
                StatusChip(text = statusText, color = statusColor)
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (trend.isEmpty()) {
                Text(
                    text = "Belum ada catatan gula darah",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF9A9A9A)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    trend.forEach { (label, value) ->
                        MeasurementTrendRowItem(
                            day = label.take(3).uppercase(),
                            value = formatGlucose(value)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(text: String, color: Color) {
    val background = color.copy(alpha = 0.12f)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(background)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun MeasurementTrendRowItem(day: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = day,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF9A9A9A)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF0D1F2D)
        )
    }
}

@Composable
private fun MetricFilterDropdown(
    selected: Set<MetricKey>,
    onSelectionChange: (Set<MetricKey>) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val buttonLabel = if (selected.isEmpty()) "Pilih data" else "${selected.size} data dipilih"

    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(buttonLabel, modifier = Modifier.weight(1f, fill = false))
            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            MetricKey.values().forEach { metric ->
                val isChecked = selected.contains(metric)
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = isChecked, onCheckedChange = null)
                            Text(text = metric.label, modifier = Modifier.padding(start = 8.dp))
                        }
                    },
                    onClick = {
                        val updated = selected.toMutableSet().apply {
                            if (!add(metric)) remove(metric)
                        }
                        onSelectionChange(updated)
                    }
                )
            }
        }
    }
}

@Composable
private fun MultiMetricLineChart(points: List<AnalyticsDayPoint>, metrics: List<MetricKey>) {
    val labelTextSize = with(LocalDensity.current) { 11.sp.toPx() }
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        if (points.isEmpty() || metrics.isEmpty()) return@Canvas
        val values = metrics.flatMap { metric -> points.map { metric.selector(it) } }
        val maxValue = values.maxOrNull()?.takeIf { it > 0f } ?: 1f
        val horizontalGap = if (points.size <= 1) 0f else size.width / (points.size - 1)
        val chartHeight = size.height
        val textPaint = Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            textSize = labelTextSize
        }

        metrics.forEach { metric ->
            val path = Path().apply {
                points.forEachIndexed { index, point ->
                    val x = horizontalGap * index
                    val value = metric.selector(point)
                    val normalized = (value / maxValue).coerceIn(0f, 1f)
                    val y = chartHeight - normalized * chartHeight
                    if (index == 0) moveTo(x, y) else lineTo(x, y)
                }
            }

            drawPath(
                path = path,
                color = metric.color,
                style = Stroke(width = 5f, cap = StrokeCap.Round)
            )

            points.forEachIndexed { index, point ->
                val x = horizontalGap * index
                val value = metric.selector(point)
                val normalized = (value / maxValue).coerceIn(0f, 1f)
                val y = chartHeight - normalized * chartHeight
                drawCircle(color = metric.color, radius = 7f, center = Offset(x, y))
                val label = formatMetricValue(metric, value)
                val textY = (y - 12f).coerceAtLeast(labelTextSize)
                textPaint.color = metric.color.toArgb()
                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.drawText(label, x, textY, textPaint)
                }
            }
        }
    }
}

@Composable
private fun MetricsLegend(metrics: List<MetricKey>) {
    if (metrics.isEmpty()) return
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        metrics.chunked(2).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                rowItems.forEach { metric ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(metric.color)
                        )
                        Text(
                            text = metric.label,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF606060),
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun hasMetricsData(points: List<AnalyticsDayPoint>, metrics: Set<MetricKey>): Boolean =
    metrics.any { metric -> points.any { point -> metric.selector(point) > 0f } }

private fun formatMetricValue(metric: MetricKey, value: Float): String = when (metric) {
    MetricKey.CALORIE_INTAKE,
    MetricKey.CALORIE_BURNED,
    MetricKey.FAT,
    MetricKey.PROTEIN,
    MetricKey.CARBS,
    MetricKey.DURATION,
    MetricKey.BLOOD_PRESSURE,
    MetricKey.GLUCOSE -> value.roundToInt().toString()

    MetricKey.PACE,
    MetricKey.DISTANCE,
    MetricKey.BMI -> String.format(Locale.getDefault(), "%.1f", value)
}

@Composable
private fun MetricComparisonBarChart(points: List<AnalyticsDayPoint>, metrics: List<MetricKey>) {
    val labelTextSize = with(LocalDensity.current) { 10.sp.toPx() }
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        if (points.isEmpty() || metrics.isEmpty()) return@Canvas
        val maxValue = metrics.maxOf { metric ->
            points.maxOf { point -> metric.selector(point) }
        }.takeIf { it > 0f } ?: 1f
        val groupWidth = size.width / points.size
        val innerSpacing = groupWidth * 0.2f
        val barsArea = groupWidth - innerSpacing
        val barWidth = barsArea / metrics.size

        val textPaint = Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            textSize = labelTextSize
        }

        points.forEachIndexed { index, point ->
            val groupStart = groupWidth * index

            metrics.forEachIndexed { metricIndex, metric ->
                val value = metric.selector(point)
                val barHeight = (value / maxValue) * size.height
                val top = size.height - barHeight
                val barStart = groupStart + (innerSpacing / 2f) + (metricIndex * barWidth)

                val width = (barWidth - 6f).coerceAtLeast(2f)
                drawRoundRect(
                    color = metric.color,
                    topLeft = Offset(barStart, top),
                    size = Size(width, barHeight),
                    cornerRadius = CornerRadius(12f, 12f)
                )

                val label = formatMetricValue(metric, value)
                val centerX = barStart + width / 2f
                val textY = (top - 6f).coerceAtLeast(labelTextSize)
                textPaint.color = metric.color.toArgb()
                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.drawText(label, centerX, textY, textPaint)
                }
            }
        }
    }
}

@Composable
private fun AnalyticsChartLabels(points: List<AnalyticsDayPoint>) {
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

private data class AnalyticsDayPoint(
    val label: String,
    val calorieIntake: Float,
    val calorieBurned: Float,
    val fat: Float,
    val protein: Float,
    val carbs: Float,
    val pace: Float,
    val durationMinutes: Float,
    val distanceKm: Float,
    val bmi: Float,
    val bloodPressure: Float,
    val glucose: Float
)

private fun buildAnalyticsDayPoints(uiState: UiState): List<AnalyticsDayPoint> {
    if (uiState.foods.isEmpty() && uiState.activities.isEmpty() && uiState.measurements.isEmpty()) {
        return emptyList()
    }
    val zone = analyticsZoneId
    val today = LocalDate.now(zone)
    val days = (0 until DAYS_IN_WEEK).map { offset -> today.minusDays((DAYS_IN_WEEK - 1 - offset).toLong()) }

    val foodsByDay = uiState.foods.groupBy { Instant.ofEpochMilli(it.timestamp).atZone(zone).toLocalDate() }
    val activitiesByDay = uiState.activities.groupBy { Instant.ofEpochMilli(it.timestamp).atZone(zone).toLocalDate() }
    val measurementsByDay = uiState.measurements.groupBy { Instant.ofEpochMilli(it.timestamp).atZone(zone).toLocalDate() }

    return days.map { day ->
        val foods = foodsByDay[day].orEmpty()
        val activities = activitiesByDay[day].orEmpty()
        val measurements = measurementsByDay[day].orEmpty().sortedBy { it.timestamp }

        val label = day.format(analyticsDayFormatter)
        val intake = foods.sumOf { it.calories }.toFloat()
        val fat = foods.sumOf { it.fat ?: 0 }.toFloat()
        val protein = foods.sumOf { it.protein ?: 0 }.toFloat()
        val carbs = foods.sumOf { it.carbs ?: 0 }.toFloat()

        val burned = activities.sumOf { it.caloriesBurned ?: 0 }.toFloat()
        val duration = activities.sumOf { it.durationMinutes ?: 0 }.toFloat()
        val distance = activities.sumOf { it.distanceKm ?: 0.0 }.toFloat()
        val paceValues = activities.mapNotNull { it.pace?.toFloat() }
        val pace = if (paceValues.isEmpty()) 0f else paceValues.average().toFloat()

        val bmi = measurements.lastOrNull { it.heightCm != null && it.weightKg != null }
            ?.let { calculateBmiValue(it.heightCm, it.weightKg)?.toFloat() }
            ?: 0f

        val systolic = measurements.lastOrNull { it.systolic != null }?.systolic?.toFloat() ?: 0f
        val glucose = measurements.lastOrNull { it.glucoseMgDl != null }?.glucoseMgDl?.toFloat() ?: 0f

        AnalyticsDayPoint(
            label = label,
            calorieIntake = intake,
            calorieBurned = burned,
            fat = fat,
            protein = protein,
            carbs = carbs,
            pace = pace,
            durationMinutes = duration,
            distanceKm = distance,
            bmi = bmi,
            bloodPressure = systolic,
            glucose = glucose
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

private fun bloodPressureStatus(systolic: Int?, diastolic: Int?): Pair<String, Color> {
    if (systolic == null || diastolic == null) return "Belum ada data" to StatusNeutral
    return when {
        systolic < 90 || diastolic < 60 -> "Tekanan rendah" to StatusLow
        systolic < 120 && diastolic < 80 -> "Normal" to StatusNormal
        systolic < 140 && diastolic < 90 -> "Waspada" to StatusElevated
        else -> "Tinggi" to StatusHigh
    }
}

private fun glucoseStatus(glucose: Int?): Pair<String, Color> {
    if (glucose == null) return "Belum ada data" to StatusNeutral
    return when {
        glucose < 70 -> "Hipoglikemia" to StatusLow
        glucose <= 140 -> "Normal" to StatusNormal
        glucose <= 180 -> "Waspada" to StatusElevated
        else -> "Tinggi" to StatusHigh
    }
}

private fun formatBloodPressure(systolic: Int?, diastolic: Int?): String =
    if (systolic != null && diastolic != null) "$systolic/$diastolic mmHg" else "--"

private fun formatGlucose(glucose: Int?): String =
    glucose?.let { "$it mg/dL" } ?: "--"

private const val DAYS_IN_WEEK = 7
private const val BMI_MAX_REFERENCE = 40.0
private val analyticsZoneId: ZoneId = ZoneId.systemDefault()
private val analyticsDayFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE", Locale("id", "ID"))
private val AnalyticsGradientTop = Color(0xFF00897B)
private val AnalyticsGradientBottom = Color(0xFF00897B)
private val IntakeLineColor = Color(0xFFFF7043)
private val BurnLineColor = Color(0xFF26A69A)
private val FatColor = Color(0xFFFFB74D)
private val ProteinColor = Color(0xFFBA68C8)
private val CarbsColor = Color(0xFF4FC3F7)
private val PaceColor = Color(0xFF4DB6AC)
private val DurationColor = Color(0xFFFF8A80)
private val DistanceColor = Color(0xFF9575CD)
private val BmiTrendColor = Color(0xFF26C6DA)
private val BloodPressureColor = Color(0xFFD32F2F)
private val GlucoseColor = Color(0xFF8D6E63)
private val TabSelected = Color(0xFF0F9B8E)
private val TabUnselectedText = Color(0xFF5F6F69)
private val BmiArcColor = Color(0xFF26A69A)
private val StatusNeutral = Color(0xFF9A9A9A)
private val StatusLow = Color(0xFF42A5F5)
private val StatusNormal = Color(0xFF26A69A)
private val StatusElevated = Color(0xFFFFB300)
private val StatusHigh = Color(0xFFE53935)
