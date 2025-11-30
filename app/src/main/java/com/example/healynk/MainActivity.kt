package com.example.healynk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.healynk.services.AuthService
import com.example.healynk.services.FirebaseService
import com.example.healynk.services.PinService
import com.example.healynk.services.ProfilePhotoStorageService
import com.example.healynk.services.UserPreferences
import com.example.healynk.ui.screens.AddActivityScreen
import com.example.healynk.ui.screens.AddBodyStatsScreen
import com.example.healynk.ui.screens.AddFoodScreen
import com.example.healynk.ui.screens.AddMeasurementScreen
import com.example.healynk.ui.screens.AnalyticsScreen
import com.example.healynk.ui.screens.HistoryScreen
import com.example.healynk.ui.screens.HomeScreen
import com.example.healynk.ui.screens.LoginScreen
import com.example.healynk.ui.screens.MainShell
import com.example.healynk.ui.screens.PinAuthScreen
import com.example.healynk.ui.screens.PinSetupScreen
import com.example.healynk.ui.screens.RegisterScreen
import com.example.healynk.ui.screens.SettingsScreen
import com.example.healynk.ui.screens.SplashScreen
import com.example.healynk.ui.theme.HealynkTheme
import com.example.healynk.utils.Constants
import com.example.healynk.viewmodel.UiState
import com.example.healynk.viewmodel.UiViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: UiViewModel by viewModels {
        UiViewModel.factory(
            AuthService(),
            FirebaseService(),
            PinService(applicationContext),
            ProfilePhotoStorageService(applicationContext),
            UserPreferences(applicationContext)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HealynkAppRoot(viewModel)
        }
    }
}

@Composable
private fun HealynkAppRoot(viewModel: UiViewModel) {
    val navController = rememberNavController()
    HealynkTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            HealynkNavHost(navController = navController, viewModel = viewModel)
        }
    }
}

@Composable
fun HealynkNavHost(navController: NavHostController, viewModel: UiViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    NavHost(
        navController = navController,
        startDestination = Constants.ROUTE_SPLASH
    ) {
        composable(Constants.ROUTE_SPLASH) {
            SplashScreen(
                isAuthenticated = uiState.isAuthenticated,
                hasPin = uiState.hasPin,
                pinVerified = uiState.pinVerified,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(Constants.ROUTE_SPLASH) { inclusive = true }
                    }
                },
                onCheckState = viewModel::initSplash
            )
        }
        composable(Constants.ROUTE_LOGIN) {
            LoginScreen(
                onLogin = viewModel::login,
                onNavigateRegister = { navController.navigate(Constants.ROUTE_REGISTER) },
                errorMessage = uiState.error,
                isAuthenticated = uiState.isAuthenticated,
                hasPin = uiState.hasPin,
                onNavigateAfterLogin = { hasPin ->
                    if (hasPin) {
                        // User sudah punya PIN → minta PIN dulu
                        navController.navigate(Constants.ROUTE_PIN_AUTH) {
                            popUpTo(Constants.ROUTE_LOGIN) { inclusive = true }
                        }
                    } else {
                        // User belum punya PIN → langsung ke setup PIN
                        navController.navigate(Constants.ROUTE_PIN_SETUP) {
                            popUpTo(Constants.ROUTE_LOGIN) { inclusive = true }
                        }
                    }
                }
            )
        }
        composable(Constants.ROUTE_REGISTER) {
            RegisterScreen(
                onRegister = viewModel::register,
                onNavigateLogin = {
                    navController.navigate(Constants.ROUTE_LOGIN) {
                        popUpTo(Constants.ROUTE_REGISTER) { inclusive = true }
                    }
                },
                onRegistrationConsumed = viewModel::consumeRegistrationSuccess,
                errorMessage = uiState.error,
                registrationSuccess = uiState.registrationSuccess
            )
        }
        composable(Constants.ROUTE_PIN_SETUP) {
            PinSetupScreen(
                onSetPin = {
                    viewModel.setPin(it)
                    navController.navigate(Constants.ROUTE_MAIN) {
                        popUpTo(Constants.ROUTE_SPLASH) { inclusive = true }
                    }
                }
            )
        }
        composable(Constants.ROUTE_PIN_AUTH) {
            PinAuthScreen(
                errorMessage = uiState.error,
                onVerifyPin = {
                    viewModel.verifyPin(it)
                    if (viewModel.uiState.value.pinVerified) {
                        navController.navigate(Constants.ROUTE_MAIN) {
                            popUpTo(Constants.ROUTE_SPLASH) { inclusive = true }
                        }
                    }
                }
            )
        }
        composable(Constants.ROUTE_MAIN) {
            MainShell(
                uiState = uiState,
                onAddMeasurement = { navController.navigate(Constants.ROUTE_ADD_MEASUREMENT) },
                onAddBodyStats = { navController.navigate(Constants.ROUTE_ADD_BODY_STATS) },
                onAddActivity = { navController.navigate(Constants.ROUTE_ADD_ACTIVITY) },
                onAddFood = { navController.navigate(Constants.ROUTE_ADD_FOOD) },
                onSignOut = {
                    viewModel.signOut()
                    navController.navigate(Constants.ROUTE_LOGIN) {
                        popUpTo(Constants.ROUTE_SPLASH) { inclusive = true }
                    }
                },
                onRemovePin = viewModel::removePin,
                onUpdateDisplayName = viewModel::updateDisplayName,
                onUpdatePhotoUrl = viewModel::updatePhotoUrl,
                onUpdateEmail = viewModel::updateEmail,
                onUpdatePassword = viewModel::updatePassword,
                onDeleteMeasurement = viewModel::deleteMeasurement,
                onDeleteActivity = viewModel::deleteActivity,
                onDeleteFood = viewModel::deleteFood,
                onRestoreMeasurement = viewModel::addMeasurement,
                onRestoreActivity = viewModel::addActivity,
                onRestoreFood = viewModel::addFood,
                onUploadPhoto = viewModel::uploadProfilePhoto,
                onUpdateBurnGoal = viewModel::updateBurnGoal
            )
        }
        addFormDestinations(navController, viewModel)
    }
}

private fun NavGraphBuilder.addFormDestinations(navController: NavHostController, viewModel: UiViewModel) {
    composable(Constants.ROUTE_ADD_MEASUREMENT) {
        AddMeasurementScreen(
            onSave = {
                viewModel.addMeasurement(it)
                navController.popBackStack()
            },
            onCancel = { navController.popBackStack() }
        )
    }
    composable(Constants.ROUTE_ADD_ACTIVITY) {
        AddActivityScreen(
            onSave = {
                viewModel.addActivity(it)
                navController.popBackStack()
            },
            onCancel = { navController.popBackStack() }
        )
    }
    composable(Constants.ROUTE_ADD_FOOD) {
        AddFoodScreen(
            onSave = {
                viewModel.addFood(it)
                navController.popBackStack()
            },
            onCancel = { navController.popBackStack() }
        )
    }
    composable(Constants.ROUTE_ADD_BODY_STATS) {
        AddBodyStatsScreen(
            onSave = {
                viewModel.addBodyStats(it)
                navController.popBackStack()
            },
            onCancel = { navController.popBackStack() }
        )
    }
}