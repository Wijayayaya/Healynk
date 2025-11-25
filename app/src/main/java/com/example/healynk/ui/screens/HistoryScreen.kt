package com.example.healynk.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.healynk.models.ActivityEntry
import com.example.healynk.models.FoodEntry
import com.example.healynk.models.Measurement
import com.example.healynk.ui.components.ActivityItem
import com.example.healynk.ui.components.FoodItem
import com.example.healynk.ui.components.MeasurementItem
import com.example.healynk.viewmodel.UiState
import kotlinx.coroutines.launch

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
    val tabs = listOf("Measurement", "Activity", "Food")
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(text = "History", style = MaterialTheme.typography.headlineSmall)
            TabRow(
                selectedTabIndex = tabIndex,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(selected = tabIndex == index, onClick = { tabIndex = index }, text = { Text(title) })
                }
            }
            when (tabIndex) {
                0 -> MeasurementHistory(uiState) { measurement ->
                    scope.launch {
                        onDeleteMeasurement(measurement.id)
                        val result = snackbarHostState.showSnackbar(
                            message = "Pengukuran dihapus",
                            actionLabel = "UNDO",
                            duration = SnackbarDuration.Short,
                            withDismissAction = true
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            onRestoreMeasurement(measurement)
                        }
                    }
                }
                1 -> ActivityHistory(uiState) { activity ->
                    scope.launch {
                        onDeleteActivity(activity.id)
                        val result = snackbarHostState.showSnackbar(
                            message = "Aktivitas dihapus",
                            actionLabel = "UNDO",
                            duration = SnackbarDuration.Short
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            onRestoreActivity(activity)
                        }
                    }
                }
                else -> FoodHistory(uiState) { food ->
                    scope.launch {
                        onDeleteFood(food.id)
                        val result = snackbarHostState.showSnackbar(
                            message = "Makanan dihapus",
                            actionLabel = "UNDO",
                            duration = SnackbarDuration.Short
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            onRestoreFood(food)
                        }
                    }
                }
            }
        }
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun MeasurementHistory(
    uiState: UiState,
    onRequestDelete: (Measurement) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
        items(uiState.measurements, key = { it.id }) { measurement ->
            MeasurementItem(measurement = measurement, onDelete = { onRequestDelete(measurement) })
        }
    }
}

@Composable
private fun ActivityHistory(
    uiState: UiState,
    onRequestDelete: (ActivityEntry) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
        items(uiState.activities, key = { it.id }) { activity ->
            ActivityItem(activity = activity, onDelete = { onRequestDelete(activity) })
        }
    }
}

@Composable
private fun FoodHistory(
    uiState: UiState,
    onRequestDelete: (FoodEntry) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
        items(uiState.foods, key = { it.id }) { food ->
            FoodItem(food = food, onDelete = { onRequestDelete(food) })
        }
    }
}
