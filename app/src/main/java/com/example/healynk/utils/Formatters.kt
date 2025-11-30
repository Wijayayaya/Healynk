package com.example.healynk.utils

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {
    private val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateTimeFormatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    private val percentFormatter = NumberFormat.getPercentInstance(Locale.getDefault()).apply {
        maximumFractionDigits = 0
    }

    fun formatDate(timestamp: Long): String = dateFormatter.format(Date(timestamp))
    fun formatTime(timestamp: Long): String = timeFormatter.format(Date(timestamp))
    fun formatDateTime(timestamp: Long): String = dateTimeFormatter.format(Date(timestamp))
    fun formatSteps(steps: Int): String = NumberFormat.getIntegerInstance().format(steps)
    fun formatPercentage(value: Float): String = percentFormatter.format(value)
    fun formatDistance(distanceKm: Double): String = String.format(Locale.getDefault(), "%.2f", distanceKm)
    fun formatPace(pace: Double): String = String.format(Locale.getDefault(), "%.2f", pace)
}
