package com.example.healynk.services

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthService(private val auth: FirebaseAuth = FirebaseAuth.getInstance()) {

    val currentUserId: String?
        get() = auth.currentUser?.uid

    val currentUserEmail: String?
        get() = auth.currentUser?.email

    val currentUserDisplayName: String?
        get() = auth.currentUser?.displayName

    val currentUserPhotoUrl: String?
        get() = auth.currentUser?.photoUrl?.toString()

    fun authState(): Flow<Boolean> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser != null)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun register(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email.trim(), password).await()
    }

    suspend fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email.trim(), password).await()
    }

    fun logout() {
        auth.signOut()
    }

    suspend fun updateDisplayName(displayName: String) {
        val user = requireUser()
        val request = userProfileChangeRequest { this.displayName = displayName }
        user.updateProfile(request).await()
    }

    suspend fun updatePhotoUrl(photoUrl: String?) {
        val user = requireUser()
        val request = userProfileChangeRequest {
            this.photoUri = photoUrl?.takeIf { it.isNotBlank() }?.let(Uri::parse)
        }
        user.updateProfile(request).await()
    }

    suspend fun updateEmail(newEmail: String) {
        requireUser().updateEmail(newEmail.trim()).await()
    }

    suspend fun updatePassword(newPassword: String) {
        requireUser().updatePassword(newPassword).await()
    }

    private fun requireUser() = auth.currentUser ?: throw IllegalStateException("User belum login")
}