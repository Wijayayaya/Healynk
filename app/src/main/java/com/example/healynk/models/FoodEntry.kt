package com.example.healynk.models

data class FoodEntry(
    val id: String = "",
    val userId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val name: String = "",
    val calories: Int = 0,
    val carbs: Int? = null,
    val protein: Int? = null,
    val fat: Int? = null,
    val notes: String? = null
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "userId" to userId,
        "timestamp" to timestamp,
        "name" to name,
        "calories" to calories,
        "carbs" to carbs,
        "protein" to protein,
        "fat" to fat,
        "notes" to notes
    )

    companion object {
        fun fromMap(data: Map<String, Any?>): FoodEntry = FoodEntry(
            id = data["id"] as? String ?: "",
            userId = data["userId"] as? String ?: "",
            timestamp = (data["timestamp"] as? Number)?.toLong() ?: 0L,
            name = data["name"] as? String ?: "",
            calories = (data["calories"] as? Number)?.toInt() ?: 0,
            carbs = (data["carbs"] as? Number)?.toInt(),
            protein = (data["protein"] as? Number)?.toInt(),
            fat = (data["fat"] as? Number)?.toInt(),
            notes = data["notes"] as? String
        )
    }
}

