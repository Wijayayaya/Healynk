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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.healynk.R
import com.example.healynk.models.ActivityEntry
import com.example.healynk.models.FoodEntry
import com.example.healynk.utils.Formatters
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
    val scrollState = rememberScrollState()
    val greeting = remember { greetingMessage() }
    val todaysFoods = remember(uiState.foods) { uiState.foods.filter { it.isToday() }.sortedByDescending { it.timestamp }.take(3) }
    val todaysActivities = remember(uiState.activities) { uiState.activities.filter { it.isToday() }.sortedByDescending { it.timestamp }.take(3) }
    val macroRows = remember(todaysFoods) { macroOverview(todaysFoods) }
    val remainingCalories = (uiState.dailyCaloriesGoal - uiState.dailyCalories).coerceAtLeast(0)
    val consumed = uiState.dailyCalories
    val burned = uiState.dailyActivityCalories
    val goal = uiState.dailyCaloriesGoal
    var fabMenuExpanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(HomeGradientTop, HomeGradientBottom)
                    )
                )
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            GreetingHeader(
                greeting = greeting,
                name = uiState.displayName.ifBlank { uiState.userEmail.substringBefore('@', missingDelimiterValue = "Pengguna") },
                photoUrl = uiState.photoUrl
            )
            Spacer(modifier = Modifier.height(16.dp))
            DailyGoalCard(
                remaining = remainingCalories,
                goal = goal,
                consumed = consumed,
                burned = burned
            )
            Spacer(modifier = Modifier.height(16.dp))
            MacrosCard(macroRows)
            Spacer(modifier = Modifier.height(16.dp))
            RecentMealsCard(
                foods = todaysFoods,
                onAddFood = onAddFood
            )
            Spacer(modifier = Modifier.height(16.dp))
            RecentActivitiesCard(
                activities = todaysActivities,
                onAddActivity = onAddActivity
            )
            Spacer(modifier = Modifier.height(80.dp))
        }

        Box(modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(20.dp)
        ) {
            FloatingActionButton(
                containerColor = FabColor,
                onClick = { fabMenuExpanded = !fabMenuExpanded }
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah data", tint = Color.White)
            }
            DropdownMenu(
                expanded = fabMenuExpanded,
                onDismissRequest = { fabMenuExpanded = false }
            ) {
                FabMenuItem("Tambah makanan") {
                    fabMenuExpanded = false
                    onAddFood()
                }
                FabMenuItem("Tambah aktivitas") {
                    fabMenuExpanded = false
                    onAddActivity()
                }
                FabMenuItem("Input tekanan darah") {
                    fabMenuExpanded = false
                    onAddMeasurement()
                }
                FabMenuItem("Input tinggi & berat") {
                    fabMenuExpanded = false
                    onAddBodyStats()
                }
            }
        }
    }
}

@Composable
private fun GreetingHeader(greeting: String, name: String, photoUrl: String?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.2f)) {
                AsyncImage(
                    model = photoUrl ?: R.drawable.logo_healynk,
                    contentDescription = "Foto profil",
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                )
            }
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(text = greeting, color = Color.White.copy(alpha = 0.9f))
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        IconButton(onClick = { /* reserved for notifications */ }) {
            Icon(imageVector = Icons.Default.Notifications, contentDescription = "Notifikasi", tint = Color.White)
        }
    }
}

@Composable
private fun DailyGoalCard(remaining: Int, goal: Int, consumed: Int, burned: Int) {
    val progress = if (goal <= 0) 0f else consumed.toFloat() / goal.toFloat()
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(GoalGradientStart, GoalGradientEnd)
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Target Kalori Harian", color = Color.White.copy(alpha = 0.85f))
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$remaining",
                            style = MaterialTheme.typography.headlineMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "tersisa", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GoalStat(value = goal, label = "Target", modifier = Modifier.weight(1f))
                        GoalStatDivider()
                        GoalStat(value = consumed, label = "Dikonsumsi", modifier = Modifier.weight(1f))
                        GoalStatDivider()
                        GoalStat(value = burned, label = "Dibakar", modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                GoalProgressRing(progress = progress)
            }
        }
    }
}

@Composable
private fun GoalStat(value: Int, label: String, modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(text = label, color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodySmall)
        Text(text = "$value", color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun GoalProgressRing(progress: Float) {
    val clamped = progress.coerceIn(0f, 1f)
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 10.dp.toPx()
            drawArc(
                color = Color.White.copy(alpha = 0.25f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            drawArc(
                color = Color.White,
                startAngle = -90f,
                sweepAngle = clamped * 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        Text(
            text = "${(clamped * 100).toInt()}%",
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun GoalStatDivider() {
    Box(
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .height(32.dp)
            .width(1.dp)
            .background(Color.White.copy(alpha = 0.3f))
    )
}

@Composable
private fun MacrosCard(rows: List<MacroRowData>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(text = "Makro Hari Ini", style = MaterialTheme.typography.titleMedium)
            rows.forEach { row ->
                MacroRow(row)
            }
        }
    }
}

@Composable
private fun MacroRow(row: MacroRowData) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(row.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(row.color)
                )
            }
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(row.label, fontWeight = FontWeight.SemiBold)
                val currentText = formatMacroValue(row.current, row.unit)
                val targetText = formatMacroValue(row.target, row.unit)
                Text("$currentText${row.unit} / $targetText${row.unit}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.weight(1f))
                Text(text = "${(row.progress * 100).toInt()}%", style = MaterialTheme.typography.labelMedium)
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = row.progress.coerceIn(0f, 1f),
            color = row.color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun RecentMealsCard(
    foods: List<FoodEntry>,
    onAddFood: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Makanan Terbaru", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = onAddFood) { Text("Tambah") }
            }
            if (foods.isEmpty()) {
                Text("Belum ada makanan yang dicatat hari ini.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                foods.forEach { food ->
                    MealRow(food)
                }
            }
        }
    }
}

@Composable
private fun RecentActivitiesCard(
    activities: List<ActivityEntry>,
    onAddActivity: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Aktivitas Terbaru", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = onAddActivity) { Text("Tambah") }
            }
            if (activities.isEmpty()) {
                Text("Belum ada aktivitas hari ini.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                activities.forEach { activity ->
                    ActivityRow(activity)
                }
            }
        }
    }
}

@Composable
private fun ActivityRow(activity: ActivityEntry) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(activity.type.ifBlank { "Aktivitas" }, fontWeight = FontWeight.SemiBold)
            val subtitle = buildString {
                append(Formatters.formatTime(activity.timestamp))
                activity.durationMinutes?.let {
                    append(" • ")
                    append("${it} menit")
                }
            }
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        val metrics = buildList {
            activity.caloriesBurned?.let { add("${it} kkal") }
            activity.distanceKm?.let { add(String.format(Locale.getDefault(), "%.1f km", it)) }
        }
        Column(horizontalAlignment = Alignment.End) {
            metrics.forEach { metric ->
                Text(metric, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun MealRow(food: FoodEntry) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(food.name.ifBlank { "Tanpa nama" }, fontWeight = FontWeight.SemiBold)
                val subtitle = buildString {
                    append(Formatters.formatTime(food.timestamp))
                    food.notes?.takeIf { it.isNotBlank() }?.let {
                        append(" • ")
                        append(it)
                    }
                }
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            val macros = buildList {
                add("${food.calories} kkal")
                food.protein?.let { add("${it}g protein") }
                food.carbs?.let { add("${it}g karbohidrat") }
                food.fat?.let { add("${it}g lemak") }
            }
            Column(horizontalAlignment = Alignment.End) {
                macros.forEachIndexed { index, item ->
                    val spacer = if (index == macros.lastIndex) 0.dp else 2.dp
                    Text(item, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(bottom = spacer))
                }
            }
        }
    }
}

@Composable
private fun FabMenuItem(label: String, onClick: () -> Unit) {
    DropdownMenuItem(text = { Text(label) }, onClick = onClick)
}

private data class MacroRowData(
    val label: String,
    val current: Float,
    val target: Float,
    val unit: String,
    val color: Color
) {
    val progress: Float get() = if (target <= 0f) 0f else current / target
}

private fun macroOverview(foods: List<FoodEntry>): List<MacroRowData> {
    val protein = foods.sumOf { it.protein ?: 0 }
    val carbs = foods.sumOf { it.carbs ?: 0 }
    val fat = foods.sumOf { it.fat ?: 0 }
    return listOf(
        MacroRowData("Protein", protein.toFloat(), 120f, "g", color = MacroPurple),
        MacroRowData("Karbohidrat", carbs.toFloat(), 200f, "g", color = MacroGold),
        MacroRowData("Lemak", fat.toFloat(), 65f, "g", color = MacroGreen)
    )
}

private fun formatMacroValue(value: Float, unit: String): String =
    if (unit == "L") String.format(Locale.getDefault(), "%.1f", value) else value.toInt().toString()

private fun FoodEntry.isToday(): Boolean = isTodayTimestamp(timestamp)

private fun ActivityEntry.isToday(): Boolean = isTodayTimestamp(timestamp)

private fun isTodayTimestamp(timestamp: Long): Boolean {
    val now = System.currentTimeMillis()
    val start = now - (now % DAY_IN_MILLIS)
    val end = start + DAY_IN_MILLIS
    return timestamp in start..end
}

private fun greetingMessage(): String {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Selamat pagi"
        hour < 18 -> "Selamat siang"
        else -> "Selamat malam"
    }
}

private const val DAY_IN_MILLIS = 24 * 60 * 60 * 1000L
private val HomeGradientTop = Color(0xFF00897B)
private val HomeGradientBottom = Color(0xFF26A69A)
private val GoalGradientStart = Color(0xFF0F9B8E)
private val GoalGradientEnd = Color(0xFF2BC0A4)
private val FabColor = Color(0xFF00897B)
private val MacroPurple = Color(0xFF5C6BC0)
private val MacroGold = Color(0xFFFFB74D)
private val MacroGreen = Color(0xFF26A69A)
