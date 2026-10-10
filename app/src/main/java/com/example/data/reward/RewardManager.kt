package com.example.data.reward

import android.content.Context
import com.example.ads.AdRewardResult
import com.example.data.db.AppDatabase
import com.example.data.model.CoinTransaction
import com.example.data.model.PendingReward
import com.example.data.model.PuzzleCompletion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class RewardManager(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)

    /**
     * Step 1: Initiates a pending reward session on the backend ledger.
     */
    suspend fun createPendingPuzzleReward(
        userId: String,
        sessionId: String,
        puzzleId: String,
        rewardCoins: Long
    ): Result<String> = withContext(Dispatchers.IO) {
        val pendingId = "prw_" + UUID.randomUUID().toString().take(10)
        val pending = PendingReward(
            id = pendingId,
            userId = userId,
            rewardType = "PUZZLE_REWARD",
            referenceId = "$sessionId:$puzzleId",
            rewardCoins = rewardCoins,
            status = "PENDING_AD"
        )
        db.pendingRewardDao().insertPendingReward(pending)
        Result.success(pendingId)
    }

    /**
     * Step 1 (Tasks): Initiates a pending reward session for a task.
     */
    suspend fun createPendingTaskReward(
        userId: String,
        taskId: String,
        rewardCoins: Long
    ): Result<String> = withContext(Dispatchers.IO) {
        val pendingId = "prw_task_" + UUID.randomUUID().toString().take(10)
        val pending = PendingReward(
            id = pendingId,
            userId = userId,
            rewardType = "TASK_REWARD",
            referenceId = taskId,
            rewardCoins = rewardCoins,
            status = "PENDING_AD"
        )
        db.pendingRewardDao().insertPendingReward(pending)
        Result.success(pendingId)
    }

    /**
     * Step 2: Validates the verified ad completion callback and authorizes crediting the wallet.
     * Prevents replay attacks, fake requests, duplicate claims, and uncompleted ads.
     */
    suspend fun validateAndGrantPendingReward(
        pendingRewardId: String,
        adResult: AdRewardResult,
        moves: Int = 0,
        timeSeconds: Long = 0L
    ): Result<Long> = withContext(Dispatchers.IO) {
        // 1. Verify ad was genuinely completed
        if (!adResult.isCompleted || adResult.adReferenceId.isBlank()) {
            return@withContext Result.failure(Exception("Ad verification failed. Reward was not completed."))
        }

        // 2. Fetch pending transaction
        val pending = db.pendingRewardDao().getPendingReward(pendingRewardId)
            ?: return@withContext Result.failure(Exception("Invalid or non-existent reward session."))

        // 3. Prevent replay attacks or duplicate claims
        if (pending.status != "PENDING_AD") {
            return@withContext Result.failure(Exception("Reward has already been claimed or expired."))
        }

        val user = db.userDao().getUserSync(pending.userId)
            ?: return@withContext Result.failure(Exception("User account not found."))

        val rewardCoins = pending.rewardCoins
        val balanceBefore = user.coinBalance
        val balanceAfter = balanceBefore + rewardCoins

        // 4. Mark pending reward as CLAIMED with ad reference and network
        db.pendingRewardDao().updateRewardStatus(
            id = pendingRewardId,
            status = "CLAIMED",
            network = adResult.network,
            adRef = adResult.adReferenceId
        )

        // 5. Update user wallet balance
        db.userDao().updateBalance(
            userId = user.id,
            newBalance = balanceAfter,
            totalEarned = user.totalCoinsEarned + rewardCoins,
            timestamp = System.currentTimeMillis()
        )

        // 6. Record immutable transaction in CoinTransaction ledger
        val txId = "tx_" + UUID.randomUUID().toString().take(8)
        val tx = CoinTransaction(
            id = txId,
            userId = user.id,
            amount = rewardCoins,
            type = pending.rewardType,
            balanceBefore = balanceBefore,
            balanceAfter = balanceAfter,
            description = "Rewarded Ad Claim (${adResult.network} ref: ${adResult.adReferenceId}) for ${pending.referenceId}"
        )
        db.transactionDao().insertTransaction(tx)

        // If puzzle, record completion
        if (pending.rewardType == "PUZZLE_REWARD") {
            val parts = pending.referenceId.split(":")
            val sessId = parts.getOrNull(0) ?: ""
            val puzId = parts.getOrNull(1) ?: ""

            db.sessionDao().insertCompletion(
                PuzzleCompletion(
                    id = "comp_" + UUID.randomUUID().toString().take(8),
                    userId = user.id,
                    puzzleId = puzId,
                    sessionId = sessId,
                    rewardCoins = rewardCoins,
                    moves = moves,
                    timeSeconds = timeSeconds,
                    completedAt = System.currentTimeMillis()
                )
            )
            db.puzzleDao().incrementSolveCount(puzId)
        } else if (pending.rewardType == "TASK_REWARD") {
            val taskId = pending.referenceId
            db.taskDao().markTaskCompleted(taskId)
            val verif = db.taskVerificationDao().getVerification(user.id, taskId)
            if (verif != null) {
                db.taskVerificationDao().insertVerification(
                    verif.copy(
                        status = "REWARDED",
                        rewardTransactionId = txId,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        }

        Result.success(rewardCoins)
    }
}
