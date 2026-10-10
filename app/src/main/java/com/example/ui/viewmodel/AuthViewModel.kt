package com.example.ui.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.AuthManager
import com.example.data.auth.AuthState
import com.example.data.db.AppDatabase
import com.example.data.model.User
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authManager = AuthManager(application)
    private val db = AppDatabase.getDatabase(application)
    private val userDao = db.userDao()

    val authState: StateFlow<AuthState> = authManager.authState
    val currentUser: StateFlow<FirebaseUser?> = authManager.currentUser

    fun signInWithGoogle(activity: Activity) {
        viewModelScope.launch {
            val result = authManager.signInWithGoogle(activity)
            result.onSuccess { firebaseUser ->
                syncUserWithDatabase(firebaseUser)
            }
        }
    }

    fun attemptSilentSignIn(activity: Activity) {
        viewModelScope.launch {
            val result = authManager.attemptSilentSignIn(activity)
            result?.onSuccess { firebaseUser ->
                syncUserWithDatabase(firebaseUser)
            }
        }
    }

    fun signOut() {
        authManager.signOut()
    }

    private suspend fun syncUserWithDatabase(firebaseUser: FirebaseUser) {
        val existing = userDao.getUserSync(firebaseUser.uid)
        val displayName = firebaseUser.displayName?.ifBlank { "PuzzleMaster" } ?: "PuzzleMaster"
        val email = firebaseUser.email ?: "player@puzzleearn.com"
        val photoUrl = firebaseUser.photoUrl?.toString() ?: ""

        if (existing == null) {
            // New user registered via Google
            val newUser = User(
                id = firebaseUser.uid,
                googleId = firebaseUser.uid,
                username = displayName,
                email = email,
                avatarUrl = photoUrl,
                coinBalance = 5000L, // Registration bonus
                totalCoinsEarned = 5000L,
                totalCoinsWithdrawn = 0L,
                role = if (email == "emziyproverfiy@gmail.com") "SUPER_ADMIN" else "USER",
                status = "ACTIVE"
            )
            userDao.insertUser(newUser)
        } else {
            // Update existing user profile info
            userDao.insertUser(
                existing.copy(
                    username = displayName,
                    email = email,
                    avatarUrl = photoUrl,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }
}
