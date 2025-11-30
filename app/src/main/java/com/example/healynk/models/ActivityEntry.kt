package com.example.healynk.models

data class ActivityEntry(
    val id: String = "",
    val userId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val type: String = "",
    val durationMinutes: Int? = null,
    val distanceKm: Double? = null,
    val pace: Double? = null,
    val caloriesBurned: Int? = null,
    val notes: String? = null
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "userId" to userId,
        "timestamp" to timestamp,
        "type" to type,
        "durationMinutes" to durationMinutes,
        "distanceKm" to distanceKm,
        "pace" to pace,
        "caloriesBurned" to caloriesBurned,
        "notes" to notes
    )

    companion object {
        fun fromMap(data: Map<String, Any?>): ActivityEntry = ActivityEntry(
            id = data["id"] as? String ?: "",
            userId = data["userId"] as? String ?: "",
            timestamp = (data["timestamp"] as? Number)?.toLong() ?: 0L,
            type = data["type"] as? String ?: "",
            durationMinutes = (data["durationMinutes"] as? Number)?.toInt(),
            distanceKm = (data["distanceKm"] as? Number)?.toDouble(),
            pace = (data["pace"] as? Number)?.toDouble(),
            caloriesBurned = (data["caloriesBurned"] as? Number)?.toInt(),
            notes = data["notes"] as? String
        )
    }
}

