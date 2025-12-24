package com.kmhinfratech.safetytaskmanager.data.repo

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth
) {
    val currentUserId: String? get() = auth.currentUser?.uid
    fun isLoggedIn(): Boolean = auth.currentUser != null

    suspend fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    fun logout() = auth.signOut()
}
