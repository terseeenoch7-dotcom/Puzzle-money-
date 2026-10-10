package com.example.data.repository

import com.example.data.db.AppDatabase
import com.example.data.model.CoinTransaction
import com.example.data.model.PuzzleCompletion
import com.example.data.model.PuzzleImage
import com.example.data.model.PuzzleSession
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class PuzzleRepository(private val db: AppDatabase) {

    fun getAllPuzzles(): Flow<List<PuzzleImage>> = db.puzzleDao().getAllPuzzles()

    fun getFeaturedPuzzles(): Flow<List<PuzzleImage>> = db.puzzleDao().getFeaturedPuzzles()

    suspend fun getPuzzleById(id: String): PuzzleImage? = db.puzzleDao().getPuzzleById(id)

    suspend fun toggleFavorite(id: String, isFavorite: Boolean) {
        db.puzzleDao().setFavorite(id, isFavorite)
    }

    suspend fun incrementPlayCount(id: String) {
        db.puzzleDao().incrementPlayCount(id)
    }

    suspend fun startPuzzleSession(
        userId: String,
        puzzleId: String,
        gridSize: Int,
        initialTiles: List<Int>
    ): PuzzleSession {
        val session = PuzzleSession(
            id = "sess_" + UUID.randomUUID().toString().take(8),
            userId = userId,
            puzzleId = puzzleId,
            gridSize = gridSize,
            initialBoardState = initialTiles.joinToString(","),
            currentBoardState = initialTiles.joinToString(","),
            moveCount = 0,
            status = "IN_PROGRESS",
            startedAt = System.currentTimeMillis()
        )
        db.sessionDao().insertSession(session)
        db.puzzleDao().incrementPlayCount(puzzleId)
        return session
    }

    /**
     * Anti-Cheat Server/Backend Validation & Reward Awarding (PRD Sections 9 & 10)
     */
    suspend fun validateAndCompletePuzzle(
        userId: String,
        sessionId: String,
        puzzleId: String,
        moves: Int,
        timeSeconds: Long
    ): Result<Long> {
        val session = db.sessionDao().getSession(sessionId)
            ?: return Result.failure(Exception("Session not found or expired."))

        if (session.userId != userId) {
            return Result.failure(Exception("Security error: Session does not belong to active user."))
        }

        if (session.status == "SOLVED") {
            return Result.failure(Exception("Session has already been validated."))
        }

        // Anti-cheat verification: Minimum moves and duration check
        val minSecondsSetting = db.settingDao().getSettingValue("anti_cheat_min_seconds")?.toLongOrNull() ?: 3L
        if (timeSeconds < minSecondsSetting && moves < 2) {
            return Result.failure(Exception("Validation failed: Impossible puzzle solve velocity."))
        }

        // Check configurable reward policy
        val rewardPolicy = db.settingDao().getSettingValue("reward_policy") ?: "OPTION_A"
        val priorCompletions = db.sessionDao().getCompletionCountForPuzzle(userId, puzzleId)

        val defaultRewardCoins = db.settingDao().getSettingValue("puzzle_reward_coins")?.toLongOrNull() ?: 100L
        val rewardToAward = if (rewardPolicy == "OPTION_B" && priorCompletions > 0) {
            0L // Only first time completion rewarded
        } else {
            defaultRewardCoins
        }

        val user = db.userDao().getUserSync(userId)
            ?: return Result.failure(Exception("User not found."))

        val balanceBefore = user.coinBalance
        val balanceAfter = balanceBefore + rewardToAward

        // 1. Mark session as SOLVED
        db.sessionDao().updateSession(
            session.copy(
                status = "SOLVED",
                moveCount = moves,
                completedAt = System.currentTimeMillis()
            )
        )

        // 2. Record completion
        val completion = PuzzleCompletion(
            id = "comp_" + UUID.randomUUID().toString().take(8),
            userId = userId,
            puzzleId = puzzleId,
            sessionId = sessionId,
            rewardCoins = rewardToAward,
            moves = moves,
            timeSeconds = timeSeconds,
            completedAt = System.currentTimeMillis()
        )
        db.sessionDao().insertCompletion(completion)

        // 3. Update User Balance & Total Coins Earned
        db.userDao().updateBalance(
            userId = userId,
            newBalance = balanceAfter,
            totalEarned = user.totalCoinsEarned + rewardToAward,
            timestamp = System.currentTimeMillis()
        )

        // 4. Record transaction ledger
        if (rewardToAward > 0) {
            val transaction = CoinTransaction(
                id = "tx_" + UUID.randomUUID().toString().take(8),
                userId = userId,
                amount = rewardToAward,
                type = "PUZZLE_REWARD",
                balanceBefore = balanceBefore,
                balanceAfter = balanceAfter,
                description = "Solved puzzle: $puzzleId (Moves: $moves, Time: ${timeSeconds}s)"
            )
            db.transactionDao().insertTransaction(transaction)
        }

        // 5. Update puzzle statistics
        db.puzzleDao().incrementSolveCount(puzzleId)

        return Result.success(rewardToAward)
    }

    fun getUserCompletions(userId: String): Flow<List<PuzzleCompletion>> =
        db.sessionDao().getUserCompletions(userId)

    fun getTotalCompletedCount(userId: String): Flow<Int> =
        db.sessionDao().getTotalCompletedCount(userId)
}
