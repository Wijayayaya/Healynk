package com.example.healynk.services

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.healynk.utils.Constants
import java.security.MessageDigest
import kotlin.math.max

class PinService(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun hasPin(userId: String): Boolean = sharedPreferences.contains(pinKey(userId))

    fun savePin(userId: String, pin: String) {
        sharedPreferences.edit()
            .putString(pinKey(userId), hash(pin))
            .remove(attemptKey(userId))
            .remove(lockKey(userId))
            .apply()
    }

    fun removePin(userId: String) {
        sharedPreferences.edit()
            .remove(pinKey(userId))
            .remove(attemptKey(userId))
            .remove(lockKey(userId))
            .apply()
    }

    fun verifyPin(userId: String, pin: String): PinResult {
        val lockUntil = sharedPreferences.getLong(lockKey(userId), 0L)
        if (lockUntil > System.currentTimeMillis()) {
            return PinResult.Locked(lockUntil)
        }
        val storedHash = sharedPreferences.getString(pinKey(userId), null) ?: return PinResult.NoPin
        val isMatch = storedHash == hash(pin)
        return if (isMatch) {
            sharedPreferences.edit()
                .remove(attemptKey(userId))
                .remove(lockKey(userId))
                .apply()
            PinResult.Success
        } else {
            val attempts = sharedPreferences.getInt(attemptKey(userId), 0) + 1
            val editor = sharedPreferences.edit().putInt(attemptKey(userId), attempts)
            if (attempts >= MAX_ATTEMPTS) {
                val newLockUntil = System.currentTimeMillis() + LOCK_DURATION_MS
                editor.putLong(lockKey(userId), newLockUntil)
                editor.putInt(attemptKey(userId), 0)
                editor.apply()
                PinResult.Locked(newLockUntil)
            } else {
                editor.apply()
                PinResult.Failure(max(0, MAX_ATTEMPTS - attempts))
            }
        }
    }

    private fun hash(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
        return digest.joinToString(separator = "") { byte -> "%02x".format(byte) }
    }

    private fun pinKey(userId: String) = "${Constants.PIN_KEY_PREFIX}$userId"
    private fun attemptKey(userId: String) = "${Constants.PIN_KEY_PREFIX}ATTEMPTS_$userId"
    private fun lockKey(userId: String) = "${Constants.PIN_KEY_PREFIX}LOCK_$userId"

    companion object {
        private const val PREFS_NAME = "healynk_secure_prefs"
        private const val MAX_ATTEMPTS = 5
        private const val LOCK_DURATION_MS = 60_000L
    }
}

sealed class PinResult {
    object Success : PinResult()
    data class Failure(val attemptsRemaining: Int) : PinResult()
    data class Locked(val unlockTimeMillis: Long) : PinResult()
    object NoPin : PinResult()
}
