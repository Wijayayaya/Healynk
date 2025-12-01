package com.example.healynk.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                .padding(20.dp)
        ) {
            // Header
            Text(
                text = "Riwayat",
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 28.sp),
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tab Row dengan styling modern
            TabRow(
                selectedTabIndex = tabIndex,
                modifier = Modifier.fillMaxWidth(),
                containerColor = Color.White.copy(alpha = 0.2f),
                contentColor = Color.White,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[tabIndex]),
                        color = Color.White,
                        height = 3.dp
                    )
                },
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = tabIndex == index,
                        onClick = { tabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (tabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 15.sp
                            )
                        },
                        selectedContentColor = Color.White,
                        unselectedContentColor = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Content
            when (tabIndex) {
                0 -> MeasurementHistory(uiState) { measurement ->
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
                1 -> ActivityHistory(uiState) { activity ->
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
                else -> FoodHistory(uiState) { food ->
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
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun MeasurementHistory(
    uiState: UiState,
    onRequestDelete: (Measurement) -> Unit
) {
    if (uiState.measurements.isEmpty()) {
        EmptyStateMessage("Belum ada pengukuran")
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp)
        ) {
            items(uiState.measurements.reversed(), key = { it.id }) { measurement ->
                MeasurementItem(measurement = measurement, onDelete = { onRequestDelete(measurement) })
            }
        }
    }
}

@Composable
private fun ActivityHistory(
    uiState: UiState,
    onRequestDelete: (ActivityEntry) -> Unit
) {
    if (uiState.activities.isEmpty()) {
        EmptyStateMessage("Belum ada aktivitas")
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp)
        ) {
            items(uiState.activities.reversed(), key = { it.id }) { activity ->
                ActivityItem(activity = activity, onDelete = { onRequestDelete(activity) })
            }
        }
    }
}

@Composable
private fun FoodHistory(
    uiState: UiState,
    onRequestDelete: (FoodEntry) -> Unit
) {
    if (uiState.foods.isEmpty()) {
        EmptyStateMessage("Belum ada makanan")
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp)
        ) {
            items(uiState.foods.reversed(), key = { it.id }) { food ->
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
            color = Color.White.copy(alpha = 0.8f),
            fontWeight = FontWeight.Medium
        )
    }
}
