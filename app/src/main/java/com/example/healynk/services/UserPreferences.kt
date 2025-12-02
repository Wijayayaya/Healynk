package com.example.healynk.services

import android.content.Context
import java.util.Locale

class UserPreferences(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getBurnGoal(default: Int): Int = prefs.getInt(KEY_BURN_GOAL, default)

    fun setBurnGoal(value: Int) {
        prefs.edit().putInt(KEY_BURN_GOAL, value).apply()
    }

    fun getTargetGoals(userId: String, defaults: Map<String, Float> = emptyMap()): Map<String, Float> {
        val raw = prefs.getString(targetKey(userId), null)
        val parsed = raw
            ?.split(ENTRY_DELIMITER)
            ?.mapNotNull { entry ->
                val parts = entry.split(VALUE_DELIMITER)
                if (parts.size != 2) return@mapNotNull null
                val id = parts[0]
                val value = parts[1].toFloatOrNull()
                if (value != null) id to value else null
            }
            ?.toMap()
            ?.toMutableMap() ?: mutableMapOf()
        defaults.forEach { (key, value) ->
            parsed.putIfAbsent(key, value)
        }
        return parsed
    }

    fun updateTargetGoal(userId: String, targetId: String, value: Float) {
        val goals = getTargetGoals(userId).toMutableMap()
        goals[targetId] = value
        saveTargetGoals(userId, goals)
    }

    private fun saveTargetGoals(userId: String, goals: Map<String, Float>) {
        val serialized = goals.entries.joinToString(ENTRY_DELIMITER) { (id, value) ->
            val normalized = String.format(Locale.US, "%.2f", value)
            "$id$VALUE_DELIMITER$normalized"
        }
        prefs.edit().putString(targetKey(userId), serialized).apply()
    }

    private fun targetKey(userId: String) = "$TARGET_PREFIX$userId"

    companion object {
        private const val PREFS_NAME = "healynk_user_prefs"
        private const val KEY_BURN_GOAL = "burn_goal"
        private const val TARGET_PREFIX = "target_goals_"
        private const val ENTRY_DELIMITER = "|"
        private const val VALUE_DELIMITER = ":"
    }
}

