package com.example.healynk.viewmodel

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.healynk.models.ActivityEntry
import com.example.healynk.models.FoodEntry
import com.example.healynk.models.TargetIds
import com.example.healynk.models.Measurement
import com.example.healynk.services.AuthService
import com.example.healynk.services.FirebaseService
import com.example.healynk.services.PinResult
import com.example.healynk.services.PinService
import com.example.healynk.services.ProfilePhotoStorageService
import com.example.healynk.services.UserPreferences
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
    private val pinService: PinService,
    private val profilePhotoStorageService: ProfilePhotoStorageService,
    private val userPreferences: UserPreferences
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
            userEmail = email,
            displayName = authService.currentUserDisplayName.orEmpty(),
            photoUrl = authService.currentUserPhotoUrl
        )
    }

    fun register(email: String, password: String) = viewModelScope.launch {
        runCatching { authService.register(email, password) }
            .onSuccess {
                authService.logout()
                _uiState.update { state ->
                    state.copy(
                        isAuthenticated = false,
                        hasPin = false,
                        pinVerified = false,
                        registrationSuccess = true,
                        error = null
                    )
                }
            }
            .onFailure {
                _uiState.update { state -> state.copy(error = it.message, registrationSuccess = false) }
            }
    }

    fun login(email: String, password: String) = viewModelScope.launch {
        runCatching { authService.login(email, password) }
            .onSuccess {
                _uiState.value = _uiState.value.copy(isAuthenticated = true, error = null)
            }
            .onFailure { _uiState.value = _uiState.value.copy(error = it.message) }
    }

    fun signOut() {
        val userId = authService.currentUserId
        userId?.let(pinService::removePin)
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

    fun consumeRegistrationSuccess() {
        _uiState.update { it.copy(registrationSuccess = false) }
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun observeData() {
        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            authService.authState().collectLatest { isLoggedIn ->
                if (!isLoggedIn) {
                    _uiState.update { state ->
                        state.copy(
                            isAuthenticated = false,
                            hasPin = false,
                            pinVerified = false,
                            measurements = emptyList(),
                            activities = emptyList(),
                            foods = emptyList(),
                            dailyCalories = 0,
                            dailyActivityCalories = 0,
                            latestMeasurement = null,
                            latestBmi = null,
                            weightTrend = emptyList(),
                            bloodPressureTrend = emptyList(),
                            glucoseTrend = emptyList(),
                            weeklyFoodChart = emptyList(),
                            weeklyActivityChart = emptyList(),
                            summaryDistanceKm = 0.0,
                            summaryDuration = 0,
                            userEmail = "",
                            dailyCaloriesGoal = userPreferences.getBurnGoal(Constants.DAILY_CALORIE_GOAL),
                            targetGoals = emptyMap()
                        )
                    }
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
                    val dailyGoal = userPreferences.getBurnGoal(Constants.DAILY_CALORIE_GOAL)
                    val defaultTargets = mapOf(
                        TargetIds.CalorieIn to dailyGoal.toFloat(),
                        TargetIds.CalorieOut to (todayActivities.sumOf { it.caloriesBurned ?: 0 }.takeIf { it > 0 }?.toFloat() ?: 400f)
                    )
                    val groupedMeasurements = measurements.groupByDay()
                    val groupedFoods = foods.groupFoodDayCalories()
                    val groupedActivities = activities.groupActivityDayCalories()
                    val latestBodyStat = measurements
                        .filter { it.weightKg != null && it.heightCm != null }
                        .maxByOrNull { it.timestamp }
                    val bmi = calculateBmi(latestBodyStat?.weightKg, latestBodyStat?.heightCm)
                    UiState(
                        isAuthenticated = true,
                        hasPin = pinService.hasPin(userId),
                        pinVerified = _uiState.value.pinVerified,
                        measurements = measurements,
                        activities = activities,
                        foods = foods,
                        dailyCalories = todayFoods.sumOf { it.calories },
                        dailyActivityCalories = todayActivities.sumOf { it.caloriesBurned ?: 0 },
                        dailyCaloriesGoal = dailyGoal,
                        latestMeasurement = measurements.maxByOrNull { it.timestamp },
                        latestBmi = bmi,
                        weightTrend = groupedMeasurements.weightTrend(),
                        bloodPressureTrend = groupedMeasurements.bloodPressureTrend(),
                        glucoseTrend = groupedMeasurements.glucoseTrend(),
                        weeklyFoodChart = groupedFoods,
                        weeklyActivityChart = groupedActivities,
                        summaryDistanceKm = todayActivities.sumOf { it.distanceKm ?: 0.0 },
                        summaryDuration = todayActivities.sumOf { it.durationMinutes ?: 0 },
                        error = null,
                        userEmail = authService.currentUserEmail.orEmpty(),
                        displayName = authService.currentUserDisplayName.orEmpty(),
                        photoUrl = authService.currentUserPhotoUrl,
                        targetGoals = userPreferences.getTargetGoals(userId, defaultTargets)
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
        groupBy { Instant.ofEpochMilli(it.timestamp).atZone(zoneId).toLocalDate() }
            .map { (date, entries) -> date to entries.sumOf { entry -> entry.calories } }
            .sortedBy { it.first }
            .takeLast(7)
            .map { (date, calories) -> date.format(dayFormatter) to calories }

    private fun List<ActivityEntry>.groupActivityDayCalories(): List<Pair<String, Int>> =
        groupBy { Instant.ofEpochMilli(it.timestamp).atZone(zoneId).toLocalDate() }
            .map { (date, entries) -> date to entries.sumOf { entry -> entry.caloriesBurned ?: 0 } }
            .sortedBy { it.first }
            .takeLast(7)
            .map { (date, calories) -> date.format(dayFormatter) to calories }

    private fun calculateBmi(weightKg: Double?, heightCm: Double?): Double? {
        if (weightKg == null || heightCm == null || heightCm == 0.0) return null
        val heightM = heightCm / 100.0
        if (heightM <= 0) return null
        return weightKg / (heightM * heightM)
    }

    companion object {
        fun factory(
            authService: AuthService,
            firebaseService: FirebaseService,
            pinService: PinService,
            profilePhotoStorageService: ProfilePhotoStorageService,
            userPreferences: UserPreferences
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                UiViewModel(authService, firebaseService, pinService, profilePhotoStorageService, userPreferences)
            }
        }
    }

    fun updateDisplayName(name: String) = viewModelScope.launch {
        runCatching { authService.updateDisplayName(name.trim()) }
            .onSuccess {
                _uiState.update { it.copy(displayName = name.trim(), error = null) }
            }
            .onFailure { throwable -> _uiState.update { state -> state.copy(error = throwable.message ?: "Gagal update nama") } }
    }

    fun updatePhotoUrl(photoUrl: String?) = viewModelScope.launch {
        runCatching { authService.updatePhotoUrl(photoUrl) }
            .onSuccess { _uiState.update { it.copy(photoUrl = photoUrl, error = null) } }
            .onFailure { throwable -> _uiState.update { state -> state.copy(error = throwable.message ?: "Gagal update foto") } }
    }

    fun updateEmail(newEmail: String) = viewModelScope.launch {
        runCatching { authService.updateEmail(newEmail.trim()) }
            .onSuccess { _uiState.update { it.copy(userEmail = newEmail.trim(), error = null) } }
            .onFailure { throwable -> _uiState.update { state -> state.copy(error = throwable.message ?: "Gagal update email") } }
    }

    fun updatePassword(newPassword: String) = viewModelScope.launch {
        runCatching { authService.updatePassword(newPassword) }
            .onSuccess { _uiState.update { it.copy(error = null) } }
            .onFailure { throwable -> _uiState.update { state -> state.copy(error = throwable.message ?: "Gagal update password") } }
    }

    fun addBodyStats(measurement: Measurement) = addMeasurement(measurement)

    fun uploadProfilePhoto(photoUri: Uri) = viewModelScope.launch {
        val userId = authService.currentUserId ?: return@launch
        runCatching {
            val downloadUrl = profilePhotoStorageService.uploadProfilePhoto(userId, photoUri)
            authService.updatePhotoUrl(downloadUrl)
            downloadUrl
        }.onSuccess { url ->
            _uiState.update { it.copy(photoUrl = url, error = null) }
        }.onFailure { throwable ->
            _uiState.update { it.copy(error = throwable.message ?: "Gagal upload foto") }
        }
    }

    fun updateBurnGoal(goal: Int) {
        userPreferences.setBurnGoal(goal)
        _uiState.update { it.copy(dailyCaloriesGoal = goal) }
    }

    fun updateTargetGoal(targetId: String, value: Float) {
        val userId = authService.currentUserId ?: return
        userPreferences.updateTargetGoal(userId, targetId, value)
        _uiState.update { state ->
            val updatedGoals = state.targetGoals.toMutableMap()
            updatedGoals[targetId] = value
            state.copy(targetGoals = updatedGoals)
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
    val latestBmi: Double? = null,
    val weightTrend: List<Pair<String, Double>> = emptyList(),
    val bloodPressureTrend: List<Pair<String, Pair<Int?, Int?>>> = emptyList(),
    val glucoseTrend: List<Pair<String, Int?>> = emptyList(),
    val weeklyFoodChart: List<Pair<String, Int>> = emptyList(),
    val weeklyActivityChart: List<Pair<String, Int>> = emptyList(),
    val summaryDistanceKm: Double = 0.0,
    val summaryDuration: Int = 0,
    val error: String? = null,
    val userEmail: String = "",
    val displayName: String = "",
    val photoUrl: String? = null,
    val registrationSuccess: Boolean = false,
    val targetGoals: Map<String, Float> = emptyMap()
)
