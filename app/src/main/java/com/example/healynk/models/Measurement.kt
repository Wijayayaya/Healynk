package com.example.healynk.models

data class Measurement(
    val id: String = "",
    val userId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val weightKg: Double? = null,
    val heightCm: Double? = null,
    val systolic: Int? = null,
    val diastolic: Int? = null,
    val glucoseMgDl: Int? = null,
    val notes: String? = null
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "userId" to userId,
        "timestamp" to timestamp,
        "weightKg" to weightKg,
        "heightCm" to heightCm,
        "systolic" to systolic,
        "diastolic" to diastolic,
        "glucoseMgDl" to glucoseMgDl,
        "notes" to notes
    )

    companion object {
        fun fromMap(data: Map<String, Any?>): Measurement = Measurement(
            id = data["id"] as? String ?: "",
            userId = data["userId"] as? String ?: "",
            timestamp = (data["timestamp"] as? Number)?.toLong() ?: 0L,
            weightKg = (data["weightKg"] as? Number)?.toDouble(),
            heightCm = (data["heightCm"] as? Number)?.toDouble(),
            systolic = (data["systolic"] as? Number)?.toInt(),
            diastolic = (data["diastolic"] as? Number)?.toInt(),
            glucoseMgDl = (data["glucoseMgDl"] as? Number)?.toInt(),
            notes = data["notes"] as? String
        )
    }
}

