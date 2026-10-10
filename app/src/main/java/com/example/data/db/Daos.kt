package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AdminSetting
import com.example.data.model.AuditLog
import com.example.data.model.CoinTransaction
import com.example.data.model.GameTask
import com.example.data.model.PuzzleCompletion
import com.example.data.model.PuzzleImage
import com.example.data.model.PuzzleSession
import com.example.data.model.User
import com.example.data.model.Withdrawal
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun getUser(userId: String): Flow<User?>

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserSync(userId: String): User?

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Query("UPDATE users SET coinBalance = :newBalance, totalCoinsEarned = :totalEarned, updatedAt = :timestamp WHERE id = :userId")
    suspend fun updateBalance(userId: String, newBalance: Long, totalEarned: Long, timestamp: Long)

    @Query("UPDATE users SET coinBalance = :newBalance, totalCoinsWithdrawn = :totalWithdrawn, updatedAt = :timestamp WHERE id = :userId")
    suspend fun updateWithdrawalBalance(userId: String, newBalance: Long, totalWithdrawn: Long, timestamp: Long)
}

@Dao
interface PuzzleDao {
    @Query("SELECT * FROM puzzle_images WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getAllPuzzles(): Flow<List<PuzzleImage>>

    @Query("SELECT * FROM puzzle_images WHERE id = :id LIMIT 1")
    suspend fun getPuzzleById(id: String): PuzzleImage?

    @Query("SELECT * FROM puzzle_images WHERE isFeatured = 1 AND isActive = 1")
    fun getFeaturedPuzzles(): Flow<List<PuzzleImage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPuzzle(puzzle: PuzzleImage)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPuzzles(puzzles: List<PuzzleImage>)

    @Update
    suspend fun updatePuzzle(puzzle: PuzzleImage)

    @Query("DELETE FROM puzzle_images WHERE id = :id")
    suspend fun deletePuzzle(id: String)

    @Query("UPDATE puzzle_images SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: String, isFavorite: Boolean)

    @Query("UPDATE puzzle_images SET playCount = playCount + 1 WHERE id = :id")
    suspend fun incrementPlayCount(id: String)

    @Query("UPDATE puzzle_images SET solveCount = solveCount + 1 WHERE id = :id")
    suspend fun incrementSolveCount(id: String)
}

@Dao
interface SessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: PuzzleSession)

    @Query("SELECT * FROM puzzle_sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getSession(sessionId: String): PuzzleSession?

    @Update
    suspend fun updateSession(session: PuzzleSession)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(completion: PuzzleCompletion)

    @Query("SELECT * FROM puzzle_completions WHERE userId = :userId ORDER BY completedAt DESC")
    fun getUserCompletions(userId: String): Flow<List<PuzzleCompletion>>

    @Query("SELECT COUNT(*) FROM puzzle_completions WHERE userId = :userId AND puzzleId = :puzzleId")
    suspend fun getCompletionCountForPuzzle(userId: String, puzzleId: String): Int

    @Query("SELECT COUNT(*) FROM puzzle_completions WHERE userId = :userId")
    fun getTotalCompletedCount(userId: String): Flow<Int>
}

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: CoinTransaction)

    @Query("SELECT * FROM coin_transactions WHERE userId = :userId ORDER BY createdAt DESC")
    fun getUserTransactions(userId: String): Flow<List<CoinTransaction>>

    @Query("SELECT * FROM coin_transactions ORDER BY createdAt DESC")
    fun getAllTransactions(): Flow<List<CoinTransaction>>
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM game_tasks WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<GameTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: GameTask)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<GameTask>)

    @Update
    suspend fun updateTask(task: GameTask)

    @Query("UPDATE game_tasks SET isCompleted = 1 WHERE id = :taskId")
    suspend fun markTaskCompleted(taskId: String)

    @Query("DELETE FROM game_tasks WHERE id = :taskId")
    suspend fun deleteTask(taskId: String)
}

@Dao
interface WithdrawalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWithdrawal(withdrawal: Withdrawal)

    @Query("SELECT * FROM withdrawals WHERE userId = :userId ORDER BY createdAt DESC")
    fun getUserWithdrawals(userId: String): Flow<List<Withdrawal>>

    @Query("SELECT * FROM withdrawals ORDER BY createdAt DESC")
    fun getAllWithdrawals(): Flow<List<Withdrawal>>

    @Query("UPDATE withdrawals SET status = :status, processedAt = :timestamp, adminNote = :note WHERE id = :id")
    suspend fun updateWithdrawalStatus(id: String, status: String, timestamp: Long, note: String)
}

@Dao
interface SettingDao {
    @Query("SELECT * FROM admin_settings")
    fun getAllSettings(): Flow<List<AdminSetting>>

    @Query("SELECT value FROM admin_settings WHERE `key` = :key LIMIT 1")
    suspend fun getSettingValue(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(setting: AdminSetting)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: List<AdminSetting>)
}

@Dao
interface AuditDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudit(audit: AuditLog)

    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogs(): Flow<List<AuditLog>>
}

@Dao
interface PendingRewardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPendingReward(reward: com.example.data.model.PendingReward)

    @Query("SELECT * FROM pending_rewards WHERE id = :id LIMIT 1")
    suspend fun getPendingReward(id: String): com.example.data.model.PendingReward?

    @Query("UPDATE pending_rewards SET status = :status, adNetwork = :network, adReferenceId = :adRef WHERE id = :id")
    suspend fun updateRewardStatus(id: String, status: String, network: String, adRef: String)

    @Query("SELECT * FROM pending_rewards WHERE userId = :userId ORDER BY createdAt DESC")
    fun getUserPendingRewards(userId: String): Flow<List<com.example.data.model.PendingReward>>
}

@Dao
interface TaskVerificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerification(verification: com.example.data.model.TaskVerification)

    @Query("SELECT * FROM task_verifications WHERE userId = :userId AND taskId = :taskId LIMIT 1")
    suspend fun getVerification(userId: String, taskId: String): com.example.data.model.TaskVerification?

    @Query("SELECT * FROM task_verifications WHERE userId = :userId")
    fun getUserVerifications(userId: String): Flow<List<com.example.data.model.TaskVerification>>

    @Query("SELECT * FROM task_verifications ORDER BY updatedAt DESC")
    fun getAllVerifications(): Flow<List<com.example.data.model.TaskVerification>>
}

@Dao
interface AdEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdEvent(event: com.example.data.model.AdEvent)

    @Query("SELECT * FROM ad_events ORDER BY timestamp DESC LIMIT 200")
    fun getRecentAdEvents(): Flow<List<com.example.data.model.AdEvent>>

    @Query("SELECT COUNT(*) FROM ad_events WHERE eventType = 'COMPLETED' AND format = 'REWARDED'")
    fun getRewardedCompletedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM ad_events WHERE eventType = 'SHOWN'")
    fun getTotalAdImpressions(): Flow<Int>
}
