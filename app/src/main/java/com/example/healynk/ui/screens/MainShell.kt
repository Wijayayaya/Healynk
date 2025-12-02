package com.example.healynk.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.healynk.utils.Constants
import com.example.healynk.viewmodel.UiState
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.healynk.models.Measurement
import com.example.healynk.models.ActivityEntry
import com.example.healynk.models.FoodEntry

@Composable
fun MainShell(
    uiState: UiState,
    onAddMeasurement: () -> Unit,
    onAddBodyStats: () -> Unit,
    onAddActivity: () -> Unit,
    onAddFood: () -> Unit,
    onSignOut: () -> Unit,
    onRemovePin: () -> Unit,
    onUpdateDisplayName: (String) -> Unit,
    onUpdatePhotoUrl: (String?) -> Unit,
    onUpdateEmail: (String) -> Unit,
    onUpdatePassword: (String) -> Unit,
    onDeleteMeasurement: (String) -> Unit,
    onDeleteActivity: (String) -> Unit,
    onDeleteFood: (String) -> Unit,
    onRestoreMeasurement: (Measurement) -> Unit,
    onRestoreActivity: (ActivityEntry) -> Unit,
    onRestoreFood: (FoodEntry) -> Unit,
    onUploadPhoto: (Uri) -> Unit,
    onUpdateBurnGoal: (Int) -> Unit
) {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                contentColor = Color(0xFF00897B)
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination?.route
                val items = listOf(
                    NavItem(Constants.ROUTE_HOME, "Beranda", Icons.Default.Home),
                    NavItem(Constants.ROUTE_HISTORY, "Riwayat", Icons.Default.History),
                    NavItem(Constants.ROUTE_ANALYTICS, "Analitik", Icons.Default.Analytics),
                    NavItem(Constants.ROUTE_SETTINGS, "Pengaturan", Icons.Default.Settings)
                )
                items.forEach { item ->
                    NavigationBarItem(
                        selected = currentDestination == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(Constants.ROUTE_HOME) {
                                    inclusive = false
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { 
                            Icon(
                                item.icon, 
                                contentDescription = item.label,
                                modifier = Modifier.padding(bottom = 2.dp)
                            ) 
                        },
                        label = { 
                            Text(
                                text = item.label,
                                fontWeight = if (currentDestination == item.route) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            ) 
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF00897B),
                            selectedTextColor = Color(0xFF00897B),
                            unselectedIconColor = Color(0xFF757575),
                            unselectedTextColor = Color(0xFF757575),
                            indicatorColor = Color(0xFF00897B).copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Constants.ROUTE_HOME,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Constants.ROUTE_HOME) {
                HomeScreen(
                    uiState = uiState,
                    onAddMeasurement = onAddMeasurement,
                    onAddBodyStats = onAddBodyStats,
                    onAddActivity = onAddActivity,
                    onAddFood = onAddFood
                )
            }
            composable(Constants.ROUTE_HISTORY) {
                HistoryScreen(
                    uiState = uiState,
                    onDeleteMeasurement = onDeleteMeasurement,
                    onDeleteActivity = onDeleteActivity,
                    onDeleteFood = onDeleteFood,
                    onRestoreMeasurement = onRestoreMeasurement,
                    onRestoreActivity = onRestoreActivity,
                    onRestoreFood = onRestoreFood
                )
            }
            composable(Constants.ROUTE_ANALYTICS) {
                AnalyticsScreen(uiState = uiState)
            }
            composable(Constants.ROUTE_SETTINGS) {
                SettingsScreen(
                    uiState = uiState,
                    onSignOut = onSignOut,
                    onRemovePin = onRemovePin,
                    onUpdateDisplayName = onUpdateDisplayName,
                    onUpdatePhotoUrl = onUpdatePhotoUrl,
                    onUpdateEmail = onUpdateEmail,
                    onUpdatePassword = onUpdatePassword,
                    onUploadPhoto = onUploadPhoto,
                    onUpdateBurnGoal = onUpdateBurnGoal
                )
            }
        }
    }
}

private data class NavItem(val route: String, val label: String, val icon: ImageVector)
