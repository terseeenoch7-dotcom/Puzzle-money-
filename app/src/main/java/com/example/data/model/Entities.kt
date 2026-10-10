package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String = "user_default_1",
    val googleId: String = "google_1029384756",
    val username: String = "PuzzleMaster",
    val email: String = "player@puzzleearn.com",
    val avatarUrl: String = "",
    val coinBalance: Long = 1500L,
    val totalCoinsEarned: Long = 1500L,
    val totalCoinsWithdrawn: Long = 0L,
    val role: String = "USER", // "USER", "ADMIN", "SUPER_ADMIN"
    val status: String = "ACTIVE", // "ACTIVE", "SUSPENDED"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "puzzle_images")
data class PuzzleImage(
    @PrimaryKey val id: String,
    val title: String,
    val imageUrl: String, // Can be drawable resource name like "puzzle_supercar" or remote URL
    val category: String, // Animals, Cars, Space, Fantasy, Nature, Cities, etc.
    val difficulty: String = "Easy", // Easy (3x3), Medium (4x4), Hard (5x5)
    val gridSize: Int = 3, // 3, 4, 5
    val isFeatured: Boolean = false,
    val isPopular: Boolean = false,
    val isNew: Boolean = false,
    val playCount: Int = 0,
    val solveCount: Int = 0,
    val isFavorite: Boolean = false,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "puzzle_sessions")
data class PuzzleSession(
    @PrimaryKey val id: String,
    val userId: String,
    val puzzleId: String,
    val gridSize: Int,
    val initialBoardState: String, // Comma separated tile positions
    val currentBoardState: String,
    val moveCount: Int,
    val status: String, // "IN_PROGRESS", "SOLVED", "ABANDONED"
    val startedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

@Entity(tableName = "puzzle_completions")
data class PuzzleCompletion(
    @PrimaryKey val id: String,
    val userId: String,
    val puzzleId: String,
    val sessionId: String,
    val rewardCoins: Long,
    val moves: Int,
    val timeSeconds: Long,
    val completedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "coin_transactions")
data class CoinTransaction(
    @PrimaryKey val id: String,
    val userId: String,
    val amount: Long, // Positive for earnings, negative for withdrawals
    val type: String, // "PUZZLE_REWARD", "TASK_REWARD", "DAILY_BONUS", "ADMIN_ADJUSTMENT", "WITHDRAWAL"
    val balanceBefore: Long,
    val balanceAfter: Long,
    val description: String,
    val status: String = "COMPLETED", // "COMPLETED", "PENDING", "REJECTED"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "game_tasks")
data class GameTask(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val rewardCoins: Long,
    val iconType: String = "STAR", // "YOUTUBE", "TELEGRAM", "SHARE", "STREAK", "SURVEY"
    val targetActionUrl: String = "",
    val isCompleted: Boolean = false,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "withdrawals")
data class Withdrawal(
    @PrimaryKey val id: String,
    val userId: String,
    val coinAmount: Long,
    val cashAmountUsd: Double,
    val method: String, // "Bank Transfer", "PayPal", "Crypto USDT", "Amazon Gift Card"
    val paymentDetails: String, // Account number / email / wallet address
    val status: String = "PENDING", // "PENDING", "PROCESSING", "APPROVED", "PAID", "REJECTED"
    val createdAt: Long = System.currentTimeMillis(),
    val processedAt: Long? = null,
    val adminNote: String = ""
)

@Entity(tableName = "admin_settings")
data class AdminSetting(
    @PrimaryKey val key: String,
    val value: String,
    val description: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val adminId: String,
    val action: String,
    val target: String,
    val oldValue: String,
    val newValue: String,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "pending_rewards")
data class PendingReward(
    @PrimaryKey val id: String,
    val userId: String,
    val rewardType: String, // "PUZZLE_REWARD", "TASK_REWARD", "BONUS_REWARD"
    val referenceId: String, // puzzleId or taskId
    val rewardCoins: Long,
    val status: String = "PENDING_AD", // "PENDING_AD", "CLAIMED", "EXPIRED", "FAILED"
    val adNetwork: String = "",
    val adReferenceId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "task_verifications")
data class TaskVerification(
    @PrimaryKey val id: String,
    val userId: String,
    val taskId: String,
    val channelOrTargetId: String,
    val status: String = "NOT_STARTED", // "NOT_STARTED", "PENDING_VERIFICATION", "VERIFIED", "REWARD_PENDING", "REWARDED", "FAILED"
    val verifiedAt: Long? = null,
    val rewardTransactionId: String? = null,
    val verificationDetails: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "ad_events")
data class AdEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val network: String, // "AdMob", "Unity Ads"
    val format: String, // "REWARDED", "INTERSTITIAL", "BANNER"
    val eventType: String, // "REQUEST", "LOADED", "SHOWN", "COMPLETED", "FAILED"
    val rewardAmount: Long = 0L,
    val errorMessage: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
