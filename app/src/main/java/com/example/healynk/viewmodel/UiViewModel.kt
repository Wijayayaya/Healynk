package com.example.healynk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.healynk.models.ActivityEntry
import com.example.healynk.models.FoodEntry
import com.example.healynk.models.Measurement
import com.example.healynk.services.AuthService
import com.example.healynk.services.FirebaseService
import com.example.healynk.services.PinResult
import com.example.healynk.services.PinService
import com.example.healynk.utils.Constants
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

class UiViewModel(
    private val authService: AuthService,
    private val firebaseService: FirebaseService,
    private val pinService: PinService
) : ViewModel() {

    private val zoneId: ZoneId = ZoneId.systemDefault()
    private val dayFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM", Locale.getDefault())
    private var dataJob: Job? = null

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        observeData()
    }

    fun initSplash() {
        val userId = authService.currentUserId
        val email = authService.currentUserEmail.orEmpty()
        _uiState.value = _uiState.value.copy(
            isAuthenticated = userId != null,
            hasPin = userId?.let { pinService.hasPin(it) } ?: false,
            userEmail = email
        )
    }

    fun register(email: String, password: String) = viewModelScope.launch {
        runCatching { authService.register(email, password) }
            .onSuccess {
                _uiState.value = _uiState.value.copy(isAuthenticated = true, error = null)
            }
            .onFailure { _uiState.value = _uiState.value.copy(error = it.message) }
    }

    fun login(email: String, password: String) = viewModelScope.launch {
        runCatching { authService.login(email, password) }
            .onSuccess {
                _uiState.value = _uiState.value.copy(isAuthenticated = true, error = null)
            }
            .onFailure { _uiState.value = _uiState.value.copy(error = it.message) }
    }

    fun signOut() {
        authService.logout()
        _uiState.value = UiState()
    }

    fun refreshPinStatus() {
        authService.currentUserId?.let { uid ->
            _uiState.update { it.copy(hasPin = pinService.hasPin(uid)) }
        }
    }

    fun removePin() {
        authService.currentUserId?.let { uid ->
            pinService.removePin(uid)
            _uiState.update { it.copy(hasPin = false, pinVerified = false) }
        }
    }

    fun setPin(pin: String) {
        val userId = authService.currentUserId ?: return
        pinService.savePin(userId, pin)
        _uiState.value = _uiState.value.copy(hasPin = true, pinVerified = true)
    }

    fun verifyPin(pin: String) {
        val userId = authService.currentUserId ?: return
        when (val result = pinService.verifyPin(userId, pin)) {
            PinResult.Success -> _uiState.value = _uiState.value.copy(pinVerified = true, error = null)
            is PinResult.Failure -> _uiState.value = _uiState.value.copy(error = "PIN salah, sisa ${result.attemptsRemaining}")
            is PinResult.Locked -> _uiState.value = _uiState.value.copy(error = "Terkunci hingga ${result.unlockTimeMillis}")
            PinResult.NoPin -> _uiState.value = _uiState.value.copy(hasPin = false)
        }
    }

    fun addMeasurement(measurement: Measurement) = viewModelScope.launch {
        authService.currentUserId?.let { uid ->
            val payload = (if (measurement.id.isBlank()) {
                measurement.copy(id = UUID.randomUUID().toString())
            } else measurement).copy(userId = uid)
            firebaseService.addMeasurement(uid, payload)
        }
    }

    fun addActivity(activity: ActivityEntry) = viewModelScope.launch {
        authService.currentUserId?.let { uid ->
            val payload = (if (activity.id.isBlank()) {
                activity.copy(id = UUID.randomUUID().toString())
            } else activity).copy(userId = uid)
            firebaseService.addActivity(uid, payload)
        }
    }

    fun addFood(foodEntry: FoodEntry) = viewModelScope.launch {
        authService.currentUserId?.let { uid ->
            val payload = (if (foodEntry.id.isBlank()) {
                foodEntry.copy(id = UUID.randomUUID().toString())
            } else foodEntry).copy(userId = uid)
            firebaseService.addFood(uid, payload)
        }
    }

    fun deleteMeasurement(id: String) = viewModelScope.launch {
        authService.currentUserId?.let { firebaseService.deleteMeasurement(it, id) }
    }

    fun deleteActivity(id: String) = viewModelScope.launch {
        authService.currentUserId?.let { firebaseService.deleteActivity(it, id) }
    }

    fun deleteFood(id: String) = viewModelScope.launch {
        authService.currentUserId?.let { firebaseService.deleteFood(it, id) }
    }

    private fun observeData() {
        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            authService.authState().collectLatest { isLoggedIn ->
                if (!isLoggedIn) {
                    _uiState.value = UiState()
                    return@collectLatest
                }
                val userId = authService.currentUserId ?: return@collectLatest
                combine(
                    firebaseService.measurementsFlow(userId),
                    firebaseService.activitiesFlow(userId),
                    firebaseService.foodsFlow(userId)
                ) { measurements, activities, foods ->
                    val todayFoods = foods.filter { it.isToday() }
                    val todayActivities = activities.filter { it.isToday() }
                    val groupedMeasurements = measurements.groupByDay()
                    val groupedFoods = foods.groupFoodDayCalories()
                    val groupedActivities = activities.groupActivityDayCalories()
                    UiState(
                        isAuthenticated = true,
                        hasPin = pinService.hasPin(userId),
                        pinVerified = _uiState.value.pinVerified,
                        measurements = measurements,
                        activities = activities,
                        foods = foods,
                        dailyCalories = todayFoods.sumOf { it.calories },
                        dailyActivityCalories = todayActivities.sumOf { it.caloriesBurned ?: 0 },
                        dailyCaloriesGoal = Constants.DAILY_CALORIE_GOAL,
                        latestMeasurement = measurements.maxByOrNull { it.timestamp },
                        weightTrend = groupedMeasurements.weightTrend(),
                        bloodPressureTrend = groupedMeasurements.bloodPressureTrend(),
                        glucoseTrend = groupedMeasurements.glucoseTrend(),
                        weeklyFoodChart = groupedFoods,
                        weeklyActivityChart = groupedActivities,
                        summarySteps = todayActivities.sumOf { it.steps ?: 0 },
                        summaryDuration = todayActivities.sumOf { it.durationMinutes ?: 0 },
                        error = null,
                        userEmail = authService.currentUserEmail.orEmpty()
                    )
                }.collectLatest { newState ->
                    _uiState.value = newState
                }
            }
        }
    }

    private fun Measurement.isToday(): Boolean = timestamp >= startOfDay() && timestamp <= endOfDay()
    private fun ActivityEntry.isToday(): Boolean = timestamp >= startOfDay() && timestamp <= endOfDay()
    private fun FoodEntry.isToday(): Boolean = timestamp >= startOfDay() && timestamp <= endOfDay()

    private fun startOfDay(): Long {
        val now = System.currentTimeMillis()
        return now - (now % (24 * 60 * 60 * 1000))
    }

    private fun endOfDay(): Long = startOfDay() + (24 * 60 * 60 * 1000)

    private fun List<Measurement>.groupByDay(): Map<String, List<Measurement>> =
        groupBy { measurement ->
            Instant.ofEpochMilli(measurement.timestamp).atZone(zoneId).toLocalDate().format(dayFormatter)
        }

    private fun Map<String, List<Measurement>>.weightTrend(): List<Pair<String, Double>> =
        entries.map { (day, entries) ->
            day to (entries.lastOrNull { it.weightKg != null }?.weightKg ?: 0.0)
        }.takeLast(7)

    private fun Map<String, List<Measurement>>.bloodPressureTrend(): List<Pair<String, Pair<Int?, Int?>>> =
        entries.map { (day, entries) ->
            day to (entries.lastOrNull()?.let { it.systolic to it.diastolic } ?: (null to null))
        }.takeLast(7)

    private fun Map<String, List<Measurement>>.glucoseTrend(): List<Pair<String, Int?>> =
        entries.map { (day, entries) ->
            day to entries.lastOrNull { it.glucoseMgDl != null }?.glucoseMgDl
        }.takeLast(7)

    private fun List<FoodEntry>.groupFoodDayCalories(): List<Pair<String, Int>> =
        groupBy { Instant.ofEpochMilli(it.timestamp).atZone(zoneId).toLocalDate().format(dayFormatter) }
            .map { (day, entries) -> day to entries.sumOf { entry -> entry.calories } }
            .sortedBy { LocalDate.parse(it.first, dayFormatter) }
            .takeLast(7)

    private fun List<ActivityEntry>.groupActivityDayCalories(): List<Pair<String, Int>> =
        groupBy { Instant.ofEpochMilli(it.timestamp).atZone(zoneId).toLocalDate().format(dayFormatter) }
            .map { (day, entries) -> day to entries.sumOf { entry -> entry.caloriesBurned ?: 0 } }
            .sortedBy { LocalDate.parse(it.first, dayFormatter) }
            .takeLast(7)

    companion object {
        fun factory(
            authService: AuthService,
            firebaseService: FirebaseService,
            pinService: PinService
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                UiViewModel(authService, firebaseService, pinService)
            }
        }
    }
}

data class UiState(
    val isAuthenticated: Boolean = false,
    val hasPin: Boolean = false,
    val pinVerified: Boolean = false,
    val measurements: List<Measurement> = emptyList(),
    val activities: List<ActivityEntry> = emptyList(),
    val foods: List<FoodEntry> = emptyList(),
    val dailyCalories: Int = 0,
    val dailyActivityCalories: Int = 0,
    val dailyCaloriesGoal: Int = Constants.DAILY_CALORIE_GOAL,
    val latestMeasurement: Measurement? = null,
    val weightTrend: List<Pair<String, Double>> = emptyList(),
    val bloodPressureTrend: List<Pair<String, Pair<Int?, Int?>>> = emptyList(),
    val glucoseTrend: List<Pair<String, Int?>> = emptyList(),
    val weeklyFoodChart: List<Pair<String, Int>> = emptyList(),
    val weeklyActivityChart: List<Pair<String, Int>> = emptyList(),
    val summarySteps: Int = 0,
    val summaryDuration: Int = 0,
    val error: String? = null,
    val userEmail: String = ""
)
