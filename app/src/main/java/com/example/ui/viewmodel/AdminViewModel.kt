package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.GeminiService
import com.example.data.db.AppDatabase
import com.example.data.model.AdminSetting
import com.example.data.model.AuditLog
import com.example.data.model.CoinTransaction
import com.example.data.model.PuzzleImage
import com.example.data.model.User
import com.example.data.model.Withdrawal
import com.example.data.repository.AdminRepository
import com.example.data.repository.PuzzleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class AdminDashboardStats(
    val totalUsers: Int = 0,
    val totalPuzzles: Int = 0,
    val totalPlays: Int = 0,
    val totalSolves: Int = 0,
    val totalCoinsDistributed: Long = 0L,
    val pendingWithdrawalsCount: Int = 0,
    val totalPendingUsd: Double = 0.0
)

data class AdminUiFeedback(
    val message: String? = null,
    val isError: Boolean = false
)

class AdminViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val adminRepo = AdminRepository(db)
    private val puzzleRepo = PuzzleRepository(db)
    private val adManager = com.example.ads.AdManager.getInstance(application)
    val currentAdminId = "SUPER_ADMIN"

    val adAnalytics: StateFlow<com.example.ads.AdAnalyticsSummary> = adManager.analytics
    private val _networkPriority = MutableStateFlow(adManager.networkPriority)
    val networkPriority: StateFlow<String> = _networkPriority.asStateFlow()
    private val _isAdTestMode = MutableStateFlow(adManager.isTestMode)
    val isAdTestMode: StateFlow<Boolean> = _isAdTestMode.asStateFlow()

    val users: StateFlow<List<User>> = adminRepo.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val puzzles: StateFlow<List<PuzzleImage>> = puzzleRepo.getAllPuzzles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val withdrawals: StateFlow<List<Withdrawal>> = adminRepo.getAllWithdrawals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: StateFlow<List<AdminSetting>> = adminRepo.getAllSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs: StateFlow<List<AuditLog>> = adminRepo.getAllAuditLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<CoinTransaction>> = adminRepo.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasks: StateFlow<List<com.example.data.model.GameTask>> = adminRepo.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _feedback = MutableStateFlow(AdminUiFeedback())
    val feedback: StateFlow<AdminUiFeedback> = _feedback.asStateFlow()

    private val _isGeneratingAi = MutableStateFlow(false)
    val isGeneratingAi: StateFlow<Boolean> = _isGeneratingAi

    val dashboardStats: StateFlow<AdminDashboardStats> = combine(
        users, puzzles, withdrawals, transactions
    ) { uList, pList, wList, tList ->
        val plays = pList.sumOf { it.playCount }
        val solves = pList.sumOf { it.solveCount }
        val distributed = tList.filter { it.amount > 0 }.sumOf { it.amount }
        val pending = wList.filter { it.status == "PENDING" }
        AdminDashboardStats(
            totalUsers = uList.size,
            totalPuzzles = pList.size,
            totalPlays = plays,
            totalSolves = solves,
            totalCoinsDistributed = distributed,
            pendingWithdrawalsCount = pending.size,
            totalPendingUsd = pending.sumOf { it.cashAmountUsd }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AdminDashboardStats())

    fun updateSetting(key: String, value: String, reason: String) {
        viewModelScope.launch {
            val result = adminRepo.updateSetting(currentAdminId, key, value, reason)
            result.onSuccess {
                _feedback.value = AdminUiFeedback(message = "Setting '$key' updated to '$value'!")
            }
        }
    }

    fun adjustUserCoins(userId: String, delta: Long, reason: String) {
        viewModelScope.launch {
            val result = adminRepo.adjustUserCoins(currentAdminId, userId, delta, reason)
            result.onSuccess { newBal ->
                _feedback.value = AdminUiFeedback(message = "Adjusted user balance. New balance: %,d coins.".format(newBal))
            }.onFailure { e ->
                _feedback.value = AdminUiFeedback(message = e.message, isError = true)
            }
        }
    }

    fun processWithdrawal(withdrawalId: String, newStatus: String, note: String) {
        viewModelScope.launch {
            adminRepo.processWithdrawal(currentAdminId, withdrawalId, newStatus, note)
            _feedback.value = AdminUiFeedback(message = "Withdrawal marked as $newStatus")
        }
    }

    fun addNewPuzzle(
        title: String,
        category: String,
        difficulty: String,
        imageName: String,
        isFeatured: Boolean
    ) {
        val gridSize = when (difficulty) {
            "Easy" -> 3
            "Medium" -> 4
            else -> 5
        }
        val puzzle = PuzzleImage(
            id = "puzzle_admin_" + UUID.randomUUID().toString().take(6),
            title = title,
            imageUrl = imageName,
            category = category,
            difficulty = difficulty,
            gridSize = gridSize,
            isFeatured = isFeatured,
            isNew = true,
            isPopular = false,
            playCount = 0,
            solveCount = 0
        )
        viewModelScope.launch {
            adminRepo.addPuzzle(currentAdminId, puzzle)
            _feedback.value = AdminUiFeedback(message = "Added new puzzle: '$title' ($difficulty)")
        }
    }

    fun updatePuzzle(puzzle: PuzzleImage) {
        viewModelScope.launch {
            adminRepo.updatePuzzle(currentAdminId, puzzle)
            _feedback.value = AdminUiFeedback(message = "Updated puzzle: '${puzzle.title}'")
        }
    }

    fun addOrUpdateTask(task: com.example.data.model.GameTask, isNew: Boolean = false) {
        viewModelScope.launch {
            if (isNew) {
                adminRepo.addTask(currentAdminId, task)
                _feedback.value = AdminUiFeedback(message = "Created new task: '${task.title}'")
            } else {
                adminRepo.updateTask(currentAdminId, task)
                _feedback.value = AdminUiFeedback(message = "Updated task: '${task.title}'")
            }
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            adminRepo.deleteTask(currentAdminId, taskId)
            _feedback.value = AdminUiFeedback(message = "Task removed.")
        }
    }

    fun updateUserRoleOrStatus(user: User, newRole: String, newStatus: String) {
        viewModelScope.launch {
            val updated = user.copy(role = newRole, status = newStatus, updatedAt = System.currentTimeMillis())
            adminRepo.updateUser(currentAdminId, updated)
            _feedback.value = AdminUiFeedback(message = "Updated ${user.username}: Role=$newRole, Status=$newStatus")
        }
    }

    fun deletePuzzle(puzzleId: String) {
        viewModelScope.launch {
            adminRepo.deletePuzzle(currentAdminId, puzzleId)
            _feedback.value = AdminUiFeedback(message = "Puzzle removed.")
        }
    }

    fun generateAiPuzzleConcept(theme: String, onGenerated: (String, String) -> Unit) {
        viewModelScope.launch {
            _isGeneratingAi.value = true
            val res = GeminiService.generatePuzzleIdea(theme)
            _isGeneratingAi.value = false
            res.onSuccess { (title, desc) ->
                onGenerated(title, desc)
            }
        }
    }

    fun setAdNetworkPriority(priority: String) {
        adManager.networkPriority = priority
        _networkPriority.value = priority
        _feedback.value = AdminUiFeedback(message = "Ad Network priority changed to: $priority")
    }

    fun toggleAdTestMode(enabled: Boolean) {
        adManager.isTestMode = enabled
        _isAdTestMode.value = enabled
        _feedback.value = AdminUiFeedback(message = "Ad Test Mode set to: $enabled")
    }

    fun testRewardedAd(activity: android.app.Activity) {
        adManager.showRewardedAd(
            activity = activity,
            rewardAmount = 100L,
            onComplete = { result ->
                _feedback.value = AdminUiFeedback(message = "Test Rewarded Ad Completed on ${result.network}! Ref: ${result.adReferenceId}")
            },
            onFailedOrDismissed = { reason ->
                _feedback.value = AdminUiFeedback(message = reason, isError = true)
            }
        )
    }

    fun dismissFeedback() {
        _feedback.value = AdminUiFeedback()
    }
}
