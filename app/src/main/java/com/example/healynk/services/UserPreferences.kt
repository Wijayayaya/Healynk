package com.example.healynk.services

import android.content.Context

class UserPreferences(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getBurnGoal(default: Int): Int = prefs.getInt(KEY_BURN_GOAL, default)

    fun setBurnGoal(value: Int) {
        prefs.edit().putInt(KEY_BURN_GOAL, value).apply()
    }

    companion object {
        private const val PREFS_NAME = "healynk_user_prefs"
        private const val KEY_BURN_GOAL = "burn_goal"
    }
}

