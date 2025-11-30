package com.example.healynk.services

import android.content.Context
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class ProfilePhotoStorageService(
    private val context: Context,
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {
    private fun userPhotoPath(userId: String) = "users/$userId/profile.jpg"

    suspend fun uploadProfilePhoto(userId: String, photoUri: Uri): String {
        val bytes = context.contentResolver.openInputStream(photoUri)?.use { input ->
            input.readBytes()
        } ?: throw IllegalArgumentException("Gagal membaca foto profil")

        val reference = storage.reference.child(userPhotoPath(userId))
        reference.putBytes(bytes).await()
        return reference.downloadUrl.await().toString()
    }

    suspend fun deleteProfilePhoto(userId: String) {
        runCatching {
            storage.reference.child(userPhotoPath(userId)).delete().await()
        }
    }
}

