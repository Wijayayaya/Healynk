package com.example.healynk.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.healynk.R
import com.example.healynk.models.ActivityEntry
import com.example.healynk.models.FoodEntry
import com.example.healynk.models.TargetIds
import com.example.healynk.utils.Formatters
import com.example.healynk.viewmodel.UiState
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    uiState: UiState,
    onAddMeasurement: () -> Unit,
    onAddBodyStats: () -> Unit,
    onAddActivity: () -> Unit,
    onAddFood: () -> Unit,
    onUpdateTargetGoal: (String, Float) -> Unit
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
    val targetOptions = remember { TargetTemplates }
    val targetEntries = remember(uiState.targetGoals, targetOptions) {
        targetOptions.map { template ->
            TargetEntry(template, uiState.targetGoals[template.id])
        }
    }
    var showTargetDialog by remember { mutableStateOf(false) }
    var selectedTemplateId by remember { mutableStateOf(targetOptions.firstOrNull()?.id) }
    var targetValueInput by remember { mutableStateOf("") }
    fun openTargetDialog(entry: TargetEntry?) {
        if (targetOptions.isEmpty()) return
        if (entry == null) {
            val defaultTemplate = targetOptions.firstOrNull() ?: return
            selectedTemplateId = defaultTemplate.id
            val existing = targetEntries.firstOrNull { it.template.id == defaultTemplate.id }
            targetValueInput = existing?.value?.let { formatTargetInput(it, defaultTemplate.allowDecimal) } ?: ""
        } else {
            selectedTemplateId = entry.template.id
            targetValueInput = entry.value?.let { formatTargetInput(it, entry.template.allowDecimal) } ?: ""
        }
        showTargetDialog = true
    }
    val fabActions = listOf(
        FabMenuAction(
            title = "Kelola target",
            subtitle = "Atur target harianmu",
            icon = Icons.Filled.Flag,
            iconTint = TargetTimeColor,
            iconBackground = TargetTimeColor.copy(alpha = 0.15f)
        ) {
            fabMenuExpanded = false
            openTargetDialog(null)
        },
        FabMenuAction(
            title = "Tambah makanan",
            subtitle = "Catat konsumsi hari ini",
            icon = Icons.Filled.Restaurant
        ) {
            fabMenuExpanded = false
            onAddFood()
        },
        FabMenuAction(
            title = "Tambah aktivitas",
            subtitle = "Rekam olahraga terkini",
            icon = Icons.Filled.DirectionsRun
        ) {
            fabMenuExpanded = false
            onAddActivity()
        },
        FabMenuAction(
            title = "Input tekanan darah",
            subtitle = "Pantau tekanan darah dan gula",
            icon = Icons.Filled.Bloodtype
        ) {
            fabMenuExpanded = false
            onAddMeasurement()
        },
        FabMenuAction(
            title = "Input tinggi & berat",
            subtitle = "Perbarui data tubuh",
            icon = Icons.Filled.MonitorWeight
        ) {
            fabMenuExpanded = false
            onAddBodyStats()
        }
    )

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
            TargetOverviewCard(
                targets = targetEntries,
                onAddTarget = { openTargetDialog(null) },
                onEditTarget = { entry -> openTargetDialog(entry) }
            )
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
                onDismissRequest = { fabMenuExpanded = false },
                modifier = Modifier
                    .shadow(12.dp, RoundedCornerShape(20.dp))
                    .background(Color.White, RoundedCornerShape(20.dp))
            ) {
                fabActions.forEachIndexed { index, action ->
                    FabMenuItem(action)
                    if (index != fabActions.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = Color(0xFFE0E0E0)
                        )
                    }
                }
            }
            if (showTargetDialog && targetOptions.isNotEmpty()) {
                val selectedTemplate = targetOptions.firstOrNull { it.id == selectedTemplateId }
                TargetDialog(
                    templates = targetOptions,
                    selectedTemplateId = selectedTemplateId,
                    valueInput = targetValueInput,
                    onValueChange = { input ->
                        val allowDecimal = selectedTemplate?.allowDecimal == true
                        targetValueInput = sanitizeNumericInput(input, allowDecimal)
                    },
                    onTemplateSelected = { template ->
                        selectedTemplateId = template.id
                        val existing = targetEntries.firstOrNull { it.template.id == template.id }
                        targetValueInput = existing?.value?.let { formatTargetInput(it, template.allowDecimal) } ?: ""
                    },
                    onDismiss = { showTargetDialog = false },
                    onSave = { template, value ->
                        val normalized = if (template.allowDecimal) value else value.roundToInt().toFloat()
                        onUpdateTargetGoal(template.id, normalized)
                        showTargetDialog = false
                    }
                )
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
private fun TargetOverviewCard(
    targets: List<TargetEntry>,
    onAddTarget: () -> Unit,
    onEditTarget: (TargetEntry) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(TargetTimeColor.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Flag,
                            contentDescription = null,
                            tint = TargetTimeColor
                        )
                    }
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text(text = "Menu Target", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "Sesuaikan target makan dan aktivitas",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Button(onClick = onAddTarget) {
                    Text("Kelola")
                }
            }
            if (targets.isEmpty()) {
                Text(
                    text = "Tidak ada target tersedia.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    targets.forEach { target ->
                        TargetTile(entry = target) { onEditTarget(target) }
                    }
                }
            }
        }
    }
}

@Composable
private fun TargetTile(entry: TargetEntry, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = entry.template.accent.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, entry.template.accent.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = entry.template.icon,
                    contentDescription = null,
                    tint = entry.template.accent
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.template.title, fontWeight = FontWeight.SemiBold)
                Text(
                    entry.template.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
                val hasValue = entry.value != null
            Column(horizontalAlignment = Alignment.End) {
                Text(
                        text = entry.displayValue(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                        color = if (hasValue) entry.template.accent else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                        text = if (hasValue) "Ketuk untuk ubah" else "Ketuk untuk atur",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TargetDialog(
    templates: List<TargetTemplate>,
    selectedTemplateId: String?,
    valueInput: String,
    onValueChange: (String) -> Unit,
    onTemplateSelected: (TargetTemplate) -> Unit,
    onDismiss: () -> Unit,
    onSave: (TargetTemplate, Float) -> Unit
) {
    val selectedTemplate = templates.firstOrNull { it.id == selectedTemplateId } ?: templates.firstOrNull()
    val numericValue = valueInput.toFloatOrNull()
    val isValid = selectedTemplate != null && numericValue != null && numericValue > 0f
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Atur Target Harian") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Pilih jenis target", fontWeight = FontWeight.SemiBold)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    templates.forEach { template ->
                        TargetOptionRow(
                            template = template,
                            selected = template.id == selectedTemplate?.id,
                            onClick = { onTemplateSelected(template) }
                        )
                    }
                }
                selectedTemplate?.let { template ->
                    OutlinedTextField(
                        value = valueInput,
                        onValueChange = onValueChange,
                        label = { Text("Nilai (${template.unit})") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = if (template.allowDecimal) KeyboardType.Decimal else KeyboardType.Number
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = template.accent,
                            focusedLabelColor = template.accent
                        )
                    )
                    Text(
                        text = template.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = { selectedTemplate?.let { onSave(it, numericValue!!) } }, enabled = isValid) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
private fun TargetOptionRow(template: TargetTemplate, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) template.accent else Color(0xFFE0E0E0)
        ),
        color = if (selected) template.accent.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(template.accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = template.icon, contentDescription = null, tint = template.accent)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(template.title, fontWeight = FontWeight.SemiBold)
                Text(
                    template.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (selected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = template.accent
                )
            }
        }
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
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(FabColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Restaurant,
                            contentDescription = null,
                            tint = FabColor
                        )
                    }
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text(text = "Makanan Terbaru", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "Pantau asupan terakhir kamu",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                TextButton(onClick = onAddFood) { Text("Tambah") }
            }
            if (foods.isEmpty()) {
                Text("Belum ada makanan yang dicatat hari ini.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    foods.forEach { food ->
                        MealRow(food)
                    }
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ActivityAccentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DirectionsRun,
                            contentDescription = null,
                            tint = ActivityAccentColor
                        )
                    }
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text(text = "Aktivitas Terbaru", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "Lihat pergerakan teranyar",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                TextButton(onClick = onAddActivity) { Text("Tambah") }
            }
            if (activities.isEmpty()) {
                Text("Belum ada aktivitas hari ini.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    activities.forEach { activity ->
                        ActivityRow(activity)
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityRow(activity: ActivityEntry) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = ActivityAccentColor.copy(alpha = 0.08f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(ActivityAccentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (activity.type.ifBlank { "Aktivitas" }).take(1).uppercase(),
                    fontWeight = FontWeight.Bold,
                    color = ActivityAccentColor
                )
            }
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
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                metrics.forEach { metric ->
                    MetricChip(
                        text = metric,
                        background = Color.White.copy(alpha = 0.9f),
                        textColor = ActivityAccentColor
                    )
                }
            }
        }
    }
}

@Composable
private fun MealRow(food: FoodEntry) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MealAccentColor.copy(alpha = 0.11f)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = food.name.ifBlank { "Tanpa nama" }.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = FabColor
                    )
                }
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
                Text(
                    text = "${food.calories} kkal",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = FabColor
                )
            }
            val macros = buildList {
                food.protein?.let { add("${it}g protein") }
                food.carbs?.let { add("${it}g karbohidrat") }
                food.fat?.let { add("${it}g lemak") }
            }
            if (macros.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    macros.forEach { item ->
                        MetricChip(text = item)
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricChip(
    text: String,
    background: Color = Color.White.copy(alpha = 0.9f),
    textColor: Color = FabColor
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = textColor)
    }
}

@Composable
private fun FabMenuItem(action: FabMenuAction) {
    DropdownMenuItem(
        text = {
            Column {
                Text(
                    text = action.title,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = action.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        leadingIcon = {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(action.iconBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = action.icon,
                    contentDescription = null,
                    tint = action.iconTint
                )
            }
        },
        onClick = action.onClick,
        colors = MenuDefaults.itemColors(
            textColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

private data class FabMenuAction(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconTint: Color = FabColor,
    val iconBackground: Color = FabColor.copy(alpha = 0.12f),
    val onClick: () -> Unit
)

private data class MacroRowData(
    val label: String,
    val current: Float,
    val target: Float,
    val unit: String,
    val color: Color
) {
    val progress: Float get() = if (target <= 0f) 0f else current / target
}

private data class TargetEntry(
    val template: TargetTemplate,
    val value: Float?
) {
    fun displayValue(): String = value?.let {
        "${formatTargetValue(it, template.allowDecimal)} ${template.unit}"
    } ?: "Belum diatur"
}

private data class TargetTemplate(
    val id: String,
    val title: String,
    val subtitle: String,
    val unit: String,
    val icon: ImageVector,
    val accent: Color,
    val allowDecimal: Boolean = false
)

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

private fun formatTargetValue(value: Float, allowDecimal: Boolean): String =
    if (allowDecimal) {
        String.format(Locale.getDefault(), "%.1f", value).trimEnd('0').trimEnd('.')
    } else {
        value.roundToInt().toString()
    }

private fun formatTargetInput(value: Float, allowDecimal: Boolean): String =
    formatTargetValue(value, allowDecimal)

private fun sanitizeNumericInput(input: String, allowDecimal: Boolean): String {
    if (input.isBlank()) return ""
    val builder = StringBuilder()
    var hasDecimal = false
    input.forEach { char ->
        when {
            char.isDigit() -> builder.append(char)
            allowDecimal && char == '.' && !hasDecimal -> {
                if (builder.isEmpty()) builder.append('0')
                builder.append('.')
                hasDecimal = true
            }
        }
    }
    return builder.toString()
}

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
private val HomeGradientBottom = Color(0xFF00897B)
private val GoalGradientStart = Color(0xFF0F9B8E)
private val GoalGradientEnd = Color(0xFF2BC0A4)
private val FabColor = Color(0xFF00897B)
private val MacroPurple = Color(0xFF5C6BC0)
private val MacroGold = Color(0xFFFFB74D)
private val MacroGreen = Color(0xFF26A69A)
private val MealAccentColor = Color(0xFF26A69A)
private val ActivityAccentColor = Color(0xFF5C6BC0)
private val TargetTimeColor = Color(0xFF7E57C2)
private val TargetDistanceColor = Color(0xFF42A5F5)

private val TargetTemplates = listOf(
    TargetTemplate(
        id = TargetIds.CalorieIn,
        title = "Kalori Masuk",
        subtitle = "Batas konsumsi harian",
        unit = "kkal",
        icon = Icons.Filled.Restaurant,
        accent = MealAccentColor
    ),
    TargetTemplate(
        id = TargetIds.CalorieOut,
        title = "Kalori Terbakar",
        subtitle = "Target olahraga harian",
        unit = "kkal",
        icon = Icons.Filled.LocalFireDepartment,
        accent = ActivityAccentColor
    ),
    TargetTemplate(
        id = TargetIds.Duration,
        title = "Durasi Aktivitas",
        subtitle = "Menit aktif setiap hari",
        unit = "menit",
        icon = Icons.Filled.Schedule,
        accent = TargetTimeColor
    ),
    TargetTemplate(
        id = TargetIds.Distance,
        title = "Jarak Tempuh",
        subtitle = "Kilometer per hari",
        unit = "km",
        icon = Icons.Filled.Flag,
        accent = TargetDistanceColor,
        allowDecimal = true
    )
)
