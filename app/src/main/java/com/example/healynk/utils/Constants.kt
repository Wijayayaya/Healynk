package com.example.healynk.utils

object Constants {
    const val APP_NAME = "Healynk"
    const val USERS_COLLECTION = "users"
    const val MEASUREMENTS_COLLECTION = "measurements"
    const val ACTIVITIES_COLLECTION = "activities"
    const val FOODS_COLLECTION = "foods"
    const val DAILY_CALORIE_GOAL = 2000
    const val PIN_KEY_PREFIX = "KEY_PIN_HASH_"
    val ACTIVITY_TYPES = listOf("Walking", "Running", "Cycling", "Swimming", "Gym", "Other")

    // Navigation routes
    const val ROUTE_SPLASH = "splash"
    const val ROUTE_LOGIN = "login"
    const val ROUTE_REGISTER = "register"
    const val ROUTE_PIN_SETUP = "pin_setup"
    const val ROUTE_PIN_AUTH = "pin_auth"
    const val ROUTE_MAIN = "main"
    const val ROUTE_HOME = "home"
    const val ROUTE_HISTORY = "history"
    const val ROUTE_ANALYTICS = "analytics"
    const val ROUTE_SETTINGS = "settings"
    const val ROUTE_ADD_MEASUREMENT = "add_measurement"
    const val ROUTE_ADD_ACTIVITY = "add_activity"
    const val ROUTE_ADD_FOOD = "add_food"
}
