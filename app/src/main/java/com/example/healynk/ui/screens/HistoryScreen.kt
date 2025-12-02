package com.example.healynk.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healynk.models.ActivityEntry
import com.example.healynk.models.FoodEntry
import com.example.healynk.models.Measurement
import com.example.healynk.ui.components.ActivityItem
import com.example.healynk.ui.components.FoodItem
import com.example.healynk.ui.components.MeasurementItem
import com.example.healynk.viewmodel.UiState
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    uiState: UiState,
    onDeleteMeasurement: (String) -> Unit,
    onDeleteActivity: (String) -> Unit,
    onDeleteFood: (String) -> Unit,
    onRestoreMeasurement: (Measurement) -> Unit,
    onRestoreActivity: (ActivityEntry) -> Unit,
    onRestoreFood: (FoodEntry) -> Unit
) {
    var tabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Pengukuran", "Aktivitas", "Makanan")
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    val zoneId = remember { ZoneId.systemDefault() }
    val locale = remember { Locale("id", "ID") }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEEE, d MMM yyyy", locale) }
    val formattedDate = selectedDate?.format(dateFormatter) ?: "Menampilkan semua tanggal"

    val filteredMeasurements = remember(uiState.measurements, selectedDate) {
        filterEntriesByDate(uiState.measurements, selectedDate, zoneId) { it.timestamp }
    }
    val filteredActivities = remember(uiState.activities, selectedDate) {
        filterEntriesByDate(uiState.activities, selectedDate, zoneId) { it.timestamp }
    }
    val filteredFoods = remember(uiState.foods, selectedDate) {
        filterEntriesByDate(uiState.foods, selectedDate, zoneId) { it.timestamp }
    }

    Surface(color = Color.Transparent) {
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
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Riwayat",
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 28.sp),
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text(
                    text = "Lihat kembali catatan kesehatanmu berdasarkan tanggal.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                FilterCard(
                    formattedDate = formattedDate,
                    hasSelection = selectedDate != null,
                    onChooseDate = { showDatePicker = true },
                    onClearDate = { selectedDate = null }
                )

                Spacer(modifier = Modifier.height(16.dp))

                HistoryTabs(
                    tabs = tabs,
                    tabIndex = tabIndex,
                    onTabSelected = { tabIndex = it }
                )

                Spacer(modifier = Modifier.height(12.dp))

                when (tabIndex) {
                    0 -> MeasurementHistory(filteredMeasurements) { measurement ->
                        scope.launch {
                            onDeleteMeasurement(measurement.id)
                            val result = snackbarHostState.showSnackbar(
                                message = "Pengukuran dihapus",
                                actionLabel = "BATAL",
                                duration = SnackbarDuration.Short,
                                withDismissAction = true
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                onRestoreMeasurement(measurement)
                            }
                        }
                    }
                    1 -> ActivityHistory(filteredActivities) { activity ->
                        scope.launch {
                            onDeleteActivity(activity.id)
                            val result = snackbarHostState.showSnackbar(
                                message = "Aktivitas dihapus",
                                actionLabel = "BATAL",
                                duration = SnackbarDuration.Short
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                onRestoreActivity(activity)
                            }
                        }
                    }
                    else -> FoodHistory(filteredFoods) { food ->
                        scope.launch {
                            onDeleteFood(food.id)
                            val result = snackbarHostState.showSnackbar(
                                message = "Makanan dihapus",
                                actionLabel = "BATAL",
                                duration = SnackbarDuration.Short
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                onRestoreFood(food)
                            }
                        }
                    }
                }
            }
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            )
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate?.atStartOfDay(zoneId)?.toInstant()?.toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    selectedDate = millis?.let { Instant.ofEpochMilli(it).atZone(zoneId).toLocalDate() }
                    showDatePicker = false
                }) {
                    Text("Pilih")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Batal")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = false
            )
        }
    }
}

@Composable
private fun FilterCard(
    formattedDate: String,
    hasSelection: Boolean,
    onChooseDate: () -> Unit,
    onClearDate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)),
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = "Filter tanggal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(text = formattedDate, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(onClick = onChooseDate) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pilih tanggal")
                }
                if (hasSelection) {
                    TextButton(onClick = onClearDate) {
                        Icon(Icons.Default.Close, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset")
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryTabs(
    tabs: List<String>,
    tabIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            tabs.forEachIndexed { index, title ->
                val selected = tabIndex == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(if (selected) HistoryTabSelected else Color.Transparent)
                        .clickable { onTabSelected(index) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = if (selected) Color.White else HistoryTabUnselectedText,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun MeasurementHistory(
    measurements: List<Measurement>,
    onRequestDelete: (Measurement) -> Unit
) {
    if (measurements.isEmpty()) {
        EmptyStateMessage("Belum ada pengukuran")
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp)
        ) {
            items(measurements.sortedByDescending { it.timestamp }, key = { it.id }) { measurement ->
                MeasurementItem(measurement = measurement, onDelete = { onRequestDelete(measurement) })
            }
        }
    }
}

@Composable
private fun ActivityHistory(
    activities: List<ActivityEntry>,
    onRequestDelete: (ActivityEntry) -> Unit
) {
    if (activities.isEmpty()) {
        EmptyStateMessage("Belum ada aktivitas")
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp)
        ) {
            items(activities.sortedByDescending { it.timestamp }, key = { it.id }) { activity ->
                ActivityItem(activity = activity, onDelete = { onRequestDelete(activity) })
            }
        }
    }
}

@Composable
private fun FoodHistory(
    foods: List<FoodEntry>,
    onRequestDelete: (FoodEntry) -> Unit
) {
    if (foods.isEmpty()) {
        EmptyStateMessage("Belum ada makanan")
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp)
        ) {
            items(foods.sortedByDescending { it.timestamp }, key = { it.id }) { food ->
                FoodItem(food = food, onDelete = { onRequestDelete(food) })
            }
        }
    }
}

@Composable
private fun EmptyStateMessage(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun <T> filterEntriesByDate(
    items: List<T>,
    selectedDate: LocalDate?,
    zoneId: ZoneId,
    timestampProvider: (T) -> Long
): List<T> {
    if (selectedDate == null) return items
    return items.filter { item ->
        val instant = Instant.ofEpochMilli(timestampProvider(item))
        instant.atZone(zoneId).toLocalDate() == selectedDate
    }
}

private val AnalyticsGradientTop = Color(0xFF00897B)
private val AnalyticsGradientBottom = Color(0xFF00897B)
private val HistoryTabSelected = Color(0xFF00897B)
private val HistoryTabUnselectedText = Color(0xFF4F5B67)
