package com.example.ui.viewmodel

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ads.AdManager
import com.example.data.db.AppDatabase
import com.example.data.model.GameTask
import com.example.data.model.TaskVerification
import com.example.data.repository.TaskRepository
import com.example.data.reward.RewardManager
import com.example.data.task.TaskVerificationManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TaskClaimSuccess(
    val taskTitle: String,
    val rewardCoins: Long
)

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = TaskRepository(db)
    private val verificationManager = TaskVerificationManager(application)
    private val rewardManager = RewardManager(application)
    private val adManager = AdManager.getInstance(application)

    val currentUserId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: "user_default_1"

    val tasks: StateFlow<List<GameTask>> = repository.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val verifications: StateFlow<Map<String, TaskVerification>> = db.taskVerificationDao()
        .getUserVerifications(currentUserId)
        .map { list -> list.associateBy { it.taskId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private val _isVerifying = MutableStateFlow<String?>(null) // taskId being verified
    val isVerifying: StateFlow<String?> = _isVerifying.asStateFlow()

    private val _isWatchingAd = MutableStateFlow(false)
    val isWatchingAd: StateFlow<Boolean> = _isWatchingAd.asStateFlow()

    private val _celebrationReward = MutableStateFlow<TaskClaimSuccess?>(null)
    val celebrationReward: StateFlow<TaskClaimSuccess?> = _celebrationReward.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        // Preload rewarded ad in background for quick response when user claims
        adManager.preloadRewardedAd()
    }

    /**
     * Step 1: User initiates task action (e.g. opens YouTube channel or social link).
     */
    fun startTaskAction(context: Context, task: GameTask) {
        viewModelScope.launch {
            _errorMessage.value = null
            if (task.iconType == "YOUTUBE") {
                val channelUrl = if (task.targetActionUrl.isNotBlank()) {
                    task.targetActionUrl
                } else {
                    "https://www.youtube.com/@tersee_digitalhub"
                }
                verificationManager.openAndInitiateYouTubeSubscription(
                    userId = currentUserId,
                    taskId = task.id,
                    channelUrl = channelUrl,
                    channelId = "@tersee_digitalhub"
                )
            } else if (task.iconType == "SHARE") {
                try {
                    val sendIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, "Play Puzzle Earn and earn real rewards! Check it out: https://play.google.com/store/apps/details?id=com.aistudio.puzzleearn")
                        type = "text/plain"
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    val shareIntent = Intent.createChooser(sendIntent, "Share Puzzle Earn").apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(shareIntent)
                    verificationManager.verifyGenericTask(currentUserId, task.id, "Shared app link")
                } catch (e: Exception) {
                    Log.e("TaskViewModel", "Failed to launch share sheet", e)
                }
            } else if (task.targetActionUrl.isNotBlank()) {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(task.targetActionUrl)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    verificationManager.verifyGenericTask(currentUserId, task.id, "Opened ${task.title}")
                } catch (e: Exception) {
                    Log.e("TaskViewModel", "Failed to launch action URL", e)
                }
            } else {
                // Streak or in-app task
                verificationManager.verifyGenericTask(currentUserId, task.id, "In-app achievement verified")
            }
        }
    }

    /**
     * Step 2: Verifies YouTube subscription requirement.
     */
    fun verifyYouTubeSubscription(task: GameTask) {
        viewModelScope.launch {
            _isVerifying.value = task.id
            _errorMessage.value = null
            val result = verificationManager.verifyYouTubeSubscription(
                userId = currentUserId,
                taskId = task.id,
                channelId = "@tersee_digitalhub"
            )
            _isVerifying.value = null
            result.onSuccess {
                Log.d("TaskViewModel", "YouTube subscription verified successfully for task ${task.id}")
            }.onFailure { err ->
                _errorMessage.value = err.message ?: "Could not verify YouTube subscription. Please ensure you subscribed!"
            }
        }
    }

    /**
     * Step 3: Verified task claiming via Rewarded Ad.
     * Flow:
     * 1. Check requirement verified.
     * 2. Create pending reward transaction on backend (status = PENDING_AD).
     * 3. Show Rewarded Ad (AdMob / Unity Ads).
     * 4. Wait for full ad completion callback.
     * 5. Validate transaction & credit wallet.
     * 6. Trigger celebratory animation!
     */
    fun claimTaskRewardWithAd(activity: Activity, task: GameTask) {
        viewModelScope.launch {
            _errorMessage.value = null

            // 1. Check verification
            val currentStatus = verificationManager.getVerificationStatus(currentUserId, task.id)
            if (currentStatus != "VERIFIED" && !task.isCompleted) {
                // For YouTube, verify first
                if (task.iconType == "YOUTUBE") {
                    _errorMessage.value = "Please complete and verify your YouTube subscription before claiming."
                    return@launch
                } else {
                    // Auto-verify generic tasks if user pressed claim
                    verificationManager.verifyGenericTask(currentUserId, task.id, "Action completed")
                }
            }

            // 2. Create pending reward transaction on backend
            val pendingResult = rewardManager.createPendingTaskReward(
                userId = currentUserId,
                taskId = task.id,
                rewardCoins = task.rewardCoins
            )

            val pendingId = pendingResult.getOrNull()
            if (pendingId == null) {
                _errorMessage.value = "Could not initialize reward session. Please try again."
                return@launch
            }

            _isWatchingAd.value = true

            // 3. Show Rewarded Ad via Centralized Ad Manager
            adManager.showRewardedAd(
                activity = activity,
                rewardAmount = task.rewardCoins,
                onComplete = { adResult ->
                    viewModelScope.launch {
                        // 4. Validate and grant reward on backend ledger
                        val grantResult = rewardManager.validateAndGrantPendingReward(
                            pendingRewardId = pendingId,
                            adResult = adResult
                        )

                        grantResult.onSuccess { coinsGranted ->
                            verificationManager.markTaskRewarded(
                                userId = currentUserId,
                                taskId = task.id,
                                rewardTxId = pendingId
                            )
                            _isWatchingAd.value = false
                            _celebrationReward.value = TaskClaimSuccess(
                                taskTitle = task.title,
                                rewardCoins = coinsGranted
                            )
                        }.onFailure { err ->
                            _isWatchingAd.value = false
                            _errorMessage.value = err.message ?: "Failed to validate reward."
                        }
                    }
                },
                onFailedOrDismissed = { reason ->
                    _isWatchingAd.value = false
                    _errorMessage.value = reason
                }
            )
        }
    }

    fun dismissCelebration() {
        _celebrationReward.value = null
    }

    fun dismissError() {
        _errorMessage.value = null
    }
}
