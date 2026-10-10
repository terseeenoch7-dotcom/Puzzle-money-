package com.example.data.repository

import com.example.data.db.AppDatabase
import com.example.data.model.CoinTransaction
import com.example.data.model.User
import com.example.data.model.Withdrawal
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class WalletRepository(private val db: AppDatabase) {

    fun getUser(userId: String): Flow<User?> = db.userDao().getUser(userId)

    fun getUserTransactions(userId: String): Flow<List<CoinTransaction>> =
        db.transactionDao().getUserTransactions(userId)

    fun getUserWithdrawals(userId: String): Flow<List<Withdrawal>> =
        db.withdrawalDao().getUserWithdrawals(userId)

    suspend fun getConversionRate(): Long {
        return db.settingDao().getSettingValue("coin_usd_conversion_rate")?.toLongOrNull() ?: 1000000L
    }

    suspend fun getMinWithdrawalCoins(): Long {
        return db.settingDao().getSettingValue("min_withdrawal_coins")?.toLongOrNull() ?: 10000000L
    }

    suspend fun requestWithdrawal(
        userId: String,
        coinsToWithdraw: Long,
        method: String,
        paymentDetails: String
    ): Result<Withdrawal> {
        val user = db.userDao().getUserSync(userId)
            ?: return Result.failure(Exception("User not found."))

        val minCoins = getMinWithdrawalCoins()
        if (coinsToWithdraw < minCoins) {
            return Result.failure(Exception("Minimum withdrawal is %,d coins.".format(minCoins)))
        }

        if (coinsToWithdraw > user.coinBalance) {
            return Result.failure(Exception("Insufficient balance. You have %,d coins.".format(user.coinBalance)))
        }

        val conversionRate = getConversionRate()
        val usdAmount = coinsToWithdraw.toDouble() / conversionRate.toDouble()

        val balanceBefore = user.coinBalance
        val balanceAfter = balanceBefore - coinsToWithdraw

        val withdrawalId = "wd_" + UUID.randomUUID().toString().take(8)
        val withdrawal = Withdrawal(
            id = withdrawalId,
            userId = userId,
            coinAmount = coinsToWithdraw,
            cashAmountUsd = usdAmount,
            method = method,
            paymentDetails = paymentDetails,
            status = "PENDING",
            createdAt = System.currentTimeMillis()
        )
        db.withdrawalDao().insertWithdrawal(withdrawal)

        // Deduct from balance
        db.userDao().updateWithdrawalBalance(
            userId = userId,
            newBalance = balanceAfter,
            totalWithdrawn = user.totalCoinsWithdrawn + coinsToWithdraw,
            timestamp = System.currentTimeMillis()
        )

        // Record transaction
        val transaction = CoinTransaction(
            id = "tx_" + UUID.randomUUID().toString().take(8),
            userId = userId,
            amount = -coinsToWithdraw,
            type = "WITHDRAWAL",
            balanceBefore = balanceBefore,
            balanceAfter = balanceAfter,
            description = "Withdrawal request of $${String.format("%.2f", usdAmount)} via $method"
        )
        db.transactionDao().insertTransaction(transaction)

        return Result.success(withdrawal)
    }
}
