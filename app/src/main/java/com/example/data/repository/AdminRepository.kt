package com.example.data.repository

import com.example.data.db.AppDatabase
import com.example.data.model.AdminSetting
import com.example.data.model.AuditLog
import com.example.data.model.CoinTransaction
import com.example.data.model.GameTask
import com.example.data.model.PuzzleImage
import com.example.data.model.User
import com.example.data.model.Withdrawal
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class AdminRepository(private val db: AppDatabase) {

    fun getAllUsers(): Flow<List<User>> = db.userDao().getAllUsers()

    fun getAllWithdrawals(): Flow<List<Withdrawal>> = db.withdrawalDao().getAllWithdrawals()

    fun getAllAuditLogs(): Flow<List<AuditLog>> = db.auditDao().getAllAuditLogs()

    fun getAllSettings(): Flow<List<AdminSetting>> = db.settingDao().getAllSettings()

    fun getAllTransactions(): Flow<List<CoinTransaction>> = db.transactionDao().getAllTransactions()

    suspend fun updateSetting(
        adminId: String,
        key: String,
        newValue: String,
        reason: String
    ): Result<Unit> {
        val oldValue = db.settingDao().getSettingValue(key) ?: ""
        db.settingDao().insertSetting(
            AdminSetting(key = key, value = newValue, description = "Configured by Admin $adminId")
        )
        db.auditDao().insertAudit(
            AuditLog(
                adminId = adminId,
                action = "UPDATE_SETTING",
                target = key,
                oldValue = oldValue,
                newValue = newValue,
                reason = reason
            )
        )
        return Result.success(Unit)
    }

    suspend fun adjustUserCoins(
        adminId: String,
        userId: String,
        coinDelta: Long,
        reason: String
    ): Result<Long> {
        val user = db.userDao().getUserSync(userId)
            ?: return Result.failure(Exception("User not found"))

        val balanceBefore = user.coinBalance
        val balanceAfter = (balanceBefore + coinDelta).coerceAtLeast(0L)

        db.userDao().updateBalance(
            userId = userId,
            newBalance = balanceAfter,
            totalEarned = if (coinDelta > 0) user.totalCoinsEarned + coinDelta else user.totalCoinsEarned,
            timestamp = System.currentTimeMillis()
        )

        val tx = CoinTransaction(
            id = "tx_admin_" + UUID.randomUUID().toString().take(8),
            userId = userId,
            amount = coinDelta,
            type = "ADMIN_ADJUSTMENT",
            balanceBefore = balanceBefore,
            balanceAfter = balanceAfter,
            description = "Admin ($adminId) adjustment: $reason"
        )
        db.transactionDao().insertTransaction(tx)

        db.auditDao().insertAudit(
            AuditLog(
                adminId = adminId,
                action = "ADJUST_COINS",
                target = userId,
                oldValue = balanceBefore.toString(),
                newValue = balanceAfter.toString(),
                reason = reason
            )
        )

        return Result.success(balanceAfter)
    }

    suspend fun processWithdrawal(
        adminId: String,
        withdrawalId: String,
        status: String, // "APPROVED", "PAID", "REJECTED"
        note: String
    ): Result<Unit> {
        db.withdrawalDao().updateWithdrawalStatus(
            id = withdrawalId,
            status = status,
            timestamp = System.currentTimeMillis(),
            note = note
        )
        db.auditDao().insertAudit(
            AuditLog(
                adminId = adminId,
                action = "PROCESS_WITHDRAWAL",
                target = withdrawalId,
                oldValue = "PENDING",
                newValue = status,
                reason = note
            )
        )
        return Result.success(Unit)
    }

    suspend fun addPuzzle(adminId: String, puzzle: PuzzleImage): Result<Unit> {
        db.puzzleDao().insertPuzzle(puzzle)
        db.auditDao().insertAudit(
            AuditLog(
                adminId = adminId,
                action = "ADD_PUZZLE",
                target = puzzle.id,
                oldValue = "",
                newValue = puzzle.title,
                reason = "Admin added puzzle: ${puzzle.title} (${puzzle.difficulty})"
            )
        )
        return Result.success(Unit)
    }

    suspend fun deletePuzzle(adminId: String, puzzleId: String): Result<Unit> {
        db.puzzleDao().deletePuzzle(puzzleId)
        db.auditDao().insertAudit(
            AuditLog(
                adminId = adminId,
                action = "DELETE_PUZZLE",
                target = puzzleId,
                oldValue = puzzleId,
                newValue = "DELETED",
                reason = "Admin deleted puzzle"
            )
        )
        return Result.success(Unit)
    }

    suspend fun addTask(adminId: String, task: GameTask): Result<Unit> {
        db.taskDao().insertTask(task)
        db.auditDao().insertAudit(
            AuditLog(
                adminId = adminId,
                action = "ADD_TASK",
                target = task.id,
                oldValue = "",
                newValue = task.title,
                reason = "Admin created task"
            )
        )
        return Result.success(Unit)
    }
}
