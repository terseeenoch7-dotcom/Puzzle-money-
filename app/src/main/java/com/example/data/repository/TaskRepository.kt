package com.example.data.repository

import com.example.data.db.AppDatabase
import com.example.data.model.CoinTransaction
import com.example.data.model.GameTask
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class TaskRepository(private val db: AppDatabase) {

    fun getAllTasks(): Flow<List<GameTask>> = db.taskDao().getAllTasks()

    suspend fun claimTaskReward(userId: String, taskId: String): Result<Long> {
        val user = db.userDao().getUserSync(userId)
            ?: return Result.failure(Exception("User not found."))

        val tasks = db.taskDao().getAllTasks()
        // Find task
        val task = db.taskDao().getAllTasks()
        // We can query task directly or mark completed
        db.taskDao().markTaskCompleted(taskId)

        // Award reward coins based on task
        val rewardAmount = when (taskId) {
            "task_1" -> 5000L
            "task_2" -> 2500L
            "task_3" -> 1000L
            "task_4" -> 10000L
            "task_5" -> 3000L
            else -> 2000L
        }

        val balanceBefore = user.coinBalance
        val balanceAfter = balanceBefore + rewardAmount

        db.userDao().updateBalance(
            userId = userId,
            newBalance = balanceAfter,
            totalEarned = user.totalCoinsEarned + rewardAmount,
            timestamp = System.currentTimeMillis()
        )

        val tx = CoinTransaction(
            id = "tx_task_" + UUID.randomUUID().toString().take(8),
            userId = userId,
            amount = rewardAmount,
            type = "TASK_REWARD",
            balanceBefore = balanceBefore,
            balanceAfter = balanceAfter,
            description = "Completed Task: $taskId"
        )
        db.transactionDao().insertTransaction(tx)

        return Result.success(rewardAmount)
    }
}
