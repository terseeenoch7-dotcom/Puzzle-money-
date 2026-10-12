package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AdminSetting
import com.example.data.model.AuditLog
import com.example.data.model.CoinTransaction
import com.example.data.model.GameTask
import com.example.data.model.PuzzleCompletion
import com.example.data.model.PuzzleImage
import com.example.data.model.PuzzleSession
import com.example.data.model.User
import com.example.data.model.Withdrawal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        User::class,
        PuzzleImage::class,
        PuzzleSession::class,
        PuzzleCompletion::class,
        CoinTransaction::class,
        GameTask::class,
        Withdrawal::class,
        AdminSetting::class,
        AuditLog::class,
        com.example.data.model.PendingReward::class,
        com.example.data.model.TaskVerification::class,
        com.example.data.model.AdEvent::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun puzzleDao(): PuzzleDao
    abstract fun sessionDao(): SessionDao
    abstract fun transactionDao(): TransactionDao
    abstract fun taskDao(): TaskDao
    abstract fun withdrawalDao(): WithdrawalDao
    abstract fun settingDao(): SettingDao
    abstract fun auditDao(): AuditDao
    abstract fun pendingRewardDao(): PendingRewardDao
    abstract fun taskVerificationDao(): TaskVerificationDao
    abstract fun adEventDao(): AdEventDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val appContext = context.applicationContext
                val instance = Room.databaseBuilder(
                    appContext,
                    AppDatabase::class.java,
                    "puzzle_earn_database"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO + kotlinx.coroutines.SupervisorJob()).launch {
                                try {
                                    val database = getDatabase(appContext)
                                    seedDatabase(database)
                                } catch (e: Throwable) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun checkAndSeed(context: Context) {
            CoroutineScope(Dispatchers.IO + kotlinx.coroutines.SupervisorJob()).launch {
                try {
                    val database = getDatabase(context.applicationContext)
                    val existingUser = database.userDao().getUserSync("user_default_1")
                    if (existingUser == null) {
                        seedDatabase(database)
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        }

        private suspend fun seedDatabase(database: AppDatabase) {
            // Seed Default User
            val defaultUser = User(
                id = "user_default_1",
                googleId = "google_user_9921",
                username = "GoldenPuzzler",
                email = "emziyproverfiy@gmail.com",
                coinBalance = 25000L,
                totalCoinsEarned = 25000L,
                totalCoinsWithdrawn = 0L,
                role = "SUPER_ADMIN",
                status = "ACTIVE"
            )
            database.userDao().insertUser(defaultUser)

            // Seed Initial Coin Transaction
            database.transactionDao().insertTransaction(
                CoinTransaction(
                    id = "tx_welcome_bonus",
                    userId = "user_default_1",
                    amount = 25000L,
                    type = "DAILY_BONUS",
                    balanceBefore = 0L,
                    balanceAfter = 25000L,
                    description = "Welcome to Puzzle Earn Starter Bonus!"
                )
            )

            // Seed Admin Settings
            val initialSettings = listOf(
                AdminSetting(
                    key = "puzzle_reward_coins",
                    value = "100",
                    description = "Default coin reward awarded for solving a puzzle"
                ),
                AdminSetting(
                    key = "coin_usd_conversion_rate",
                    value = "1000000",
                    description = "Coins equivalent to 1 US Dollar (1,000,000 = $1)"
                ),
                AdminSetting(
                    key = "min_withdrawal_coins",
                    value = "10000000",
                    description = "Minimum coins required to submit a withdrawal (10,000,000 = $10)"
                ),
                AdminSetting(
                    key = "reward_policy",
                    value = "OPTION_A",
                    description = "OPTION_A: Reward every completion; OPTION_B: Reward only first time"
                ),
                AdminSetting(
                    key = "anti_cheat_min_seconds",
                    value = "4",
                    description = "Minimum valid puzzle solve duration in seconds"
                )
            )
            database.settingDao().insertSettings(initialSettings)

            // Seed Expandable Puzzle Library with high quality images
            val initialPuzzles = listOf(
                PuzzleImage(
                    id = "puzzle_supercar_1",
                    title = "Cyber Neon Supercar",
                    imageUrl = "puzzle_supercar",
                    category = "Cars",
                    difficulty = "Easy",
                    gridSize = 3,
                    isFeatured = true,
                    isPopular = true,
                    isNew = true,
                    playCount = 142,
                    solveCount = 98,
                    isFavorite = true
                ),
                PuzzleImage(
                    id = "puzzle_lion_1",
                    title = "Golden Safari King",
                    imageUrl = "puzzle_lion",
                    category = "Animals",
                    difficulty = "Medium",
                    gridSize = 4,
                    isFeatured = true,
                    isPopular = true,
                    isNew = true,
                    playCount = 210,
                    solveCount = 135,
                    isFavorite = false
                ),
                PuzzleImage(
                    id = "puzzle_cosmic_1",
                    title = "Star Explorer Astronaut",
                    imageUrl = "puzzle_cosmic",
                    category = "Space",
                    difficulty = "Easy",
                    gridSize = 3,
                    isFeatured = true,
                    isPopular = true,
                    isNew = false,
                    playCount = 385,
                    solveCount = 264,
                    isFavorite = true
                ),
                PuzzleImage(
                    id = "puzzle_icon_1",
                    title = "Magic Jewel Tiles",
                    imageUrl = "app_icon_puzzle",
                    category = "Fantasy",
                    difficulty = "Hard",
                    gridSize = 5,
                    isFeatured = false,
                    isPopular = true,
                    isNew = false,
                    playCount = 89,
                    solveCount = 42,
                    isFavorite = false
                ),
                PuzzleImage(
                    id = "puzzle_neon_drift_2",
                    title = "Midnight Turbo Racer",
                    imageUrl = "puzzle_supercar",
                    category = "Cars",
                    difficulty = "Medium",
                    gridSize = 4,
                    isFeatured = false,
                    isPopular = true,
                    isNew = true,
                    playCount = 64,
                    solveCount = 48
                ),
                PuzzleImage(
                    id = "puzzle_savanna_cub_2",
                    title = "Sunset Whispering Plains",
                    imageUrl = "puzzle_lion",
                    category = "Nature",
                    difficulty = "Hard",
                    gridSize = 5,
                    isFeatured = false,
                    isPopular = false,
                    isNew = true,
                    playCount = 51,
                    solveCount = 23
                ),
                PuzzleImage(
                    id = "puzzle_galaxy_voyage_2",
                    title = "Nebula Dreamer",
                    imageUrl = "puzzle_cosmic",
                    category = "Space",
                    difficulty = "Medium",
                    gridSize = 4,
                    isFeatured = true,
                    isPopular = false,
                    isNew = false,
                    playCount = 112,
                    solveCount = 76
                ),
                PuzzleImage(
                    id = "puzzle_crystal_crown_2",
                    title = "Enchanted Gem Kingdom",
                    imageUrl = "app_icon_puzzle",
                    category = "Cartoon",
                    difficulty = "Easy",
                    gridSize = 3,
                    isFeatured = false,
                    isPopular = false,
                    isNew = true,
                    playCount = 95,
                    solveCount = 70
                )
            )
            database.puzzleDao().insertPuzzles(initialPuzzles)

            // Seed Initial Reward Tasks
            val initialTasks = listOf(
                GameTask(
                    id = "task_1",
                    title = "Follow Our YouTube Channel",
                    description = "Watch official trailer and subscribe for weekly puzzle code drops!",
                    rewardCoins = 5000L,
                    iconType = "YOUTUBE",
                    targetActionUrl = "https://youtube.com"
                ),
                GameTask(
                    id = "task_2",
                    title = "Join VIP Telegram Community",
                    description = "Chat with puzzle solvers, share strategies and get secret coin promo codes.",
                    rewardCoins = 2500L,
                    iconType = "TELEGRAM",
                    targetActionUrl = "https://t.me"
                ),
                GameTask(
                    id = "task_3",
                    title = "Daily Puzzle Streak Day 1",
                    description = "Solve your first sliding puzzle today to activate your daily multiplier.",
                    rewardCoins = 1000L,
                    iconType = "STREAK",
                    targetActionUrl = ""
                ),
                GameTask(
                    id = "task_4",
                    title = "Share Puzzle Earn With Friends",
                    description = "Invite a fellow puzzle master to download and play.",
                    rewardCoins = 10000L,
                    iconType = "SHARE",
                    targetActionUrl = ""
                ),
                GameTask(
                    id = "task_5",
                    title = "Rate & Review Puzzle Earn",
                    description = "Give us feedback on the Google Play Store to support indie puzzle game dev.",
                    rewardCoins = 3000L,
                    iconType = "SURVEY",
                    targetActionUrl = ""
                )
            )
            database.taskDao().insertTasks(initialTasks)

            // Seed Sample Withdrawal record
            database.withdrawalDao().insertWithdrawal(
                Withdrawal(
                    id = "wd_sample_1",
                    userId = "user_default_1",
                    coinAmount = 10000000L,
                    cashAmountUsd = 10.0,
                    method = "PayPal",
                    paymentDetails = "emziyproverfiy@gmail.com",
                    status = "PAID",
                    processedAt = System.currentTimeMillis() - 86400000L,
                    adminNote = "Payout processed successfully via PayPal."
                )
            )
        }
    }
}
