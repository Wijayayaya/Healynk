package com.example.healynk.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            HeaderSection(uiState)
            Spacer(modifier = Modifier.height(20.dp))
            CalorieCard(uiState)
            Spacer(modifier = Modifier.height(16.dp))
            SummaryRow(uiState)
            Spacer(modifier = Modifier.height(16.dp))
            QuickActionsRow(onAddMeasurement, onAddBodyStats, onAddActivity, onAddFood)
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
@Composable
private fun HeaderSection(uiState: UiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_healynk),
            contentDescription = Constants.APP_NAME,
            modifier = Modifier.size(48.dp)
        )
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(
                text = Constants.APP_NAME,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 22.sp,
                    letterSpacing = 0.5.sp
                ),
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = "Halo, ${uiState.userEmail.substringBefore('@', missingDelimiterValue = "admin")}",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                fontWeight = FontWeight.Normal,
                color = Color.White.copy(alpha = 0.95f)
            )
        }
    }
}

@Composable
private fun CalorieCard(uiState: UiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Kalori Harian yang Terbakar",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF00897B)
            )
            CalorieRing(
                consumed = uiState.dailyActivityCalories,
                target = uiState.dailyCaloriesGoal,
                modifier = Modifier.padding(top = 16.dp),
                label = "Kalori Terbakar",
                ringColor = Color(0xFF00897B)
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
            icon = Icons.Default.FitnessCenter,
            backgroundColor = Color.White,
            titleColor = Color(0xFF00897B),
            iconTint = Color(0xFF00897B)
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
            icon = Icons.Default.HealthAndSafety,
            backgroundColor = Color.White,
            titleColor = Color(0xFF00897B),
            iconTint = Color(0xFF00897B)
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Quick actions",
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Lihat semua",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.95f)
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            items(
                listOf(
                    Triple("Tinggi & berat", Icons.Default.MonitorWeight, onAddBodyStats),
                    Triple("Tekanan darah", Icons.Default.HealthAndSafety, onAddMeasurement),
                    Triple("Aktivitas", Icons.Default.FitnessCenter, onAddActivity),
                    Triple("Makanan", Icons.Default.FoodBank, onAddFood)
                )
            ) { (label, icon, action) ->
                QuickActionButton(
                    label = label,
                    icon = icon,
                    onClick = action,
                    buttonColor = Color.White,
                    iconTint = Color(0xFF00897B),
                    textColor = Color.White
                )
            }
        }
    }
}
