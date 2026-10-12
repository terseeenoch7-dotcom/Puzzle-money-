package com.example.data.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

sealed class AuthState {
    data object Idle : AuthState()
    data object Loading : AuthState()
    data class Authenticated(val user: FirebaseUser) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthManager(private val context: Context) {

    private val auth: FirebaseAuth = run {
        if (com.google.firebase.FirebaseApp.getApps(context).isEmpty()) {
            com.google.firebase.FirebaseApp.initializeApp(context)
        }
        FirebaseAuth.getInstance()
    }
    private val credentialManager: CredentialManager = CredentialManager.create(context)

    private val _authState = MutableStateFlow<AuthState>(
        auth.currentUser?.let { AuthState.Authenticated(it) } ?: AuthState.Idle
    )
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        _currentUser.value = user
        if (user != null) {
            _authState.value = AuthState.Authenticated(user)
        } else {
            _authState.value = AuthState.Idle
        }
    }

    init {
        auth.addAuthStateListener(authStateListener)
    }

    private fun getServerClientId(): String {
        return try {
            val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
            if (resId != 0) {
                context.getString(resId)
            } else {
                "1004173276169-lau2sk38rj95mmpuing126ao1sva1r6m.apps.googleusercontent.com"
            }
        } catch (e: Exception) {
            "1004173276169-lau2sk38rj95mmpuing126ao1sva1r6m.apps.googleusercontent.com"
        }
    }

    /**
     * Interactive Google Sign-In using Credential Manager and GetSignInWithGoogleOption.
     * Option exclusivity: exactly one credential option in the request.
     */
    suspend fun signInWithGoogle(activity: Activity): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        _authState.value = AuthState.Loading
        try {
            val serverClientId = getServerClientId()

            val googleSignInOption = GetSignInWithGoogleOption.Builder(serverClientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleSignInOption)
                .build()

            val response = credentialManager.getCredential(
                request = request,
                context = activity
            )

            val credential = response.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(authCredential).await()
                val user = authResult.user ?: throw Exception("Firebase user is null after sign in.")

                _authState.value = AuthState.Authenticated(user)
                Result.success(user)
            } else {
                val err = "Received unsupported credential type: ${credential.type}"
                _authState.value = AuthState.Error(err)
                Result.failure(Exception(err))
            }
        } catch (e: GetCredentialCancellationException) {
            // Diagnostic warning: Sign-in was dismissed/cancelled by user
            Log.w(TAG, "Interactive Google Sign-In was cancelled by user: ${e.message}")
            _authState.value = AuthState.Idle
            Result.failure(e)
        } catch (e: GetCredentialException) {
            Log.e(TAG, "Credential Manager error during Google Sign-In", e)
            val msg = e.localizedMessage ?: "Google Sign-In failed"
            _authState.value = AuthState.Error(msg)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during Google Sign-In", e)
            val msg = e.localizedMessage ?: "Authentication failed"
            _authState.value = AuthState.Error(msg)
            Result.failure(e)
        }
    }

    /**
     * Silent Google Sign-In check for returning users using GetGoogleIdOption.
     */
    suspend fun attemptSilentSignIn(activity: Activity): Result<FirebaseUser>? = withContext(Dispatchers.IO) {
        if (auth.currentUser != null) {
            return@withContext Result.success(auth.currentUser!!)
        }

        try {
            val serverClientId = getServerClientId()
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(true)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(true)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val response = credentialManager.getCredential(
                request = request,
                context = activity
            )

            val credential = response.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(authCredential).await()
                authResult.user?.let { Result.success(it) }
            } else null
        } catch (e: Exception) {
            // Silent sign-in may not find credentials; ignore silently
            null
        }
    }

    fun signOut() {
        auth.signOut()
        _authState.value = AuthState.Idle
    }

    companion object {
        private const val TAG = "AuthManager"
    }
}
