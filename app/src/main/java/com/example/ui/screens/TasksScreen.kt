package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.AdBannerComposable
import com.example.data.model.GameTask
import com.example.data.model.TaskVerification
import com.example.ui.components.ButtonColorType
import com.example.ui.components.CoinBalanceBadge
import com.example.ui.components.ConfettiEffect
import com.example.ui.components.GameCard
import com.example.ui.components.GlossyGameButton
import com.example.ui.theme.GameBackgroundDark
import com.example.ui.theme.GameCardBorder
import com.example.ui.theme.GameCyan
import com.example.ui.theme.GameGold
import com.example.ui.theme.GameGoldLight
import com.example.ui.theme.GameGreen
import com.example.ui.theme.GameMagenta
import com.example.ui.theme.GameSurfaceDark
import com.example.ui.theme.GameSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtitle
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.TaskViewModel
import com.example.ui.viewmodel.WalletViewModel

@Composable
fun TasksScreen(
    taskViewModel: TaskViewModel,
    walletViewModel: WalletViewModel,
    onNavigateToWallet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val tasks by taskViewModel.tasks.collectAsState()
    val verifications by taskViewModel.verifications.collectAsState()
    val isVerifying by taskViewModel.isVerifying.collectAsState()
    val isWatchingAd by taskViewModel.isWatchingAd.collectAsState()
    val celebrationReward by taskViewModel.celebrationReward.collectAsState()
    val errorMessage by taskViewModel.errorMessage.collectAsState()
    val user by walletViewModel.user.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(GameBackgroundDark)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Reward Tasks",
                            color = TextWhite,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Complete verified missions for bonus coins",
                            color = TextSubtitle,
                            fontSize = 13.sp
                        )
                    }

                    CoinBalanceBadge(
                        coins = user?.coinBalance ?: 0L,
                        onClick = onNavigateToWallet
                    )
                }
            }

            // TOP AD BANNER
            item(key = "tasks_top_banner") {
                AdBannerComposable()
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Error Banner (e.g. ad closed early)
            if (errorMessage != null) {
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0x33FF5252),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp)
                            .clickable { taskViewModel.dismissError() }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Notice",
                                    color = Color(0xFFFF5252),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = errorMessage ?: "",
                                    color = TextWhite,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // Streak Daily Challenge Banner
            item {
                GameCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = GameSurfaceElevated,
                    borderColor = GameGold
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(GameGold.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = GameGold,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "Daily Challenge Active",
                                color = TextWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Solve puzzles & complete tasks to maximize coin earnings!",
                                color = GameGoldLight,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // MIDDLE AD BANNER
            item(key = "tasks_middle_banner") {
                AdBannerComposable()
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Tasks Section Title
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Available Missions (${tasks.size})",
                        color = TextWhite,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = GameSurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, GameCardBorder)
                    ) {
                        Text(
                            text = "Ads Monetized",
                            color = GameCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Task Cards List
            items(tasks, key = { it.id }) { task ->
                val verif = verifications[task.id]

                if (task.iconType == "YOUTUBE") {
                    YouTubeTaskItemCard(
                        task = task,
                        verification = verif,
                        isVerifying = isVerifying == task.id,
                        isWatchingAd = isWatchingAd,
                        onOpenChannel = { taskViewModel.startTaskAction(context, task) },
                        onVerify = { taskViewModel.verifyYouTubeSubscription(task) },
                        onClaim = {
                            if (activity != null) {
                                taskViewModel.claimTaskRewardWithAd(activity, task)
                            }
                        }
                    )
                } else {
                    GenericTaskItemCard(
                        task = task,
                        verification = verif,
                        isWatchingAd = isWatchingAd,
                        onStartTask = { taskViewModel.startTaskAction(context, task) },
                        onClaim = {
                            if (activity != null) {
                                taskViewModel.claimTaskRewardWithAd(activity, task)
                            }
                        }
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Ad Banner at bottom of task list with stable item key
            item(key = "tasks_ad_banner_key") {
                Spacer(modifier = Modifier.height(10.dp))
                AdBannerComposable()
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Celebratory Reward Popup Dialog
        if (celebrationReward != null) {
            val reward = celebrationReward!!
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                ConfettiEffect()

                AnimatedVisibility(
                    visible = true,
                    enter = scaleIn() + fadeIn()
                ) {
                    GameCard(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .padding(16.dp),
                        backgroundColor = GameSurfaceElevated,
                        borderColor = GameGold
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(GameGold.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Celebration,
                                    contentDescription = null,
                                    tint = GameGold,
                                    modifier = Modifier.size(40.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "REWARD GRANTED!",
                                color = GameGoldLight,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black
                            )

                            Text(
                                text = reward.taskTitle,
                                color = TextSubtitle,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = GameSurfaceDark,
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, GameGold)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "+${String.format("%,d", reward.rewardCoins)} 🪙",
                                        color = GameGoldLight,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Verified via Rewarded Ad and credited to your wallet ledger.",
                                color = TextMuted,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            GlossyGameButton(
                                text = "AWESOME! COLLECT",
                                onClick = { taskViewModel.dismissCelebration() },
                                colorType = ButtonColorType.GREEN,
                                height = 48.dp,
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "btn_collect_task_reward"
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dedicated High-Trust YouTube Verification Card.
 * Adheres strictly to the user requirement:
 * 1. Open channel -> 2. Verify subscription -> 3. Rewarded Ad -> 4. Coins granted.
 */
@Composable
fun YouTubeTaskItemCard(
    task: GameTask,
    verification: TaskVerification?,
    isVerifying: Boolean,
    isWatchingAd: Boolean,
    onOpenChannel: () -> Unit,
    onVerify: () -> Unit,
    onClaim: () -> Unit
) {
    val isCompleted = task.isCompleted || verification?.status == "REWARDED"
    val status = verification?.status ?: "NOT_STARTED"

    GameCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("task_item_${task.id}"),
        backgroundColor = GameSurfaceElevated,
        borderColor = if (isCompleted) GameGreen else Color(0xFFFF0000).copy(alpha = 0.7f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Red YouTube badge + Title + Coin Reward
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFF0000).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Subscriptions,
                        contentDescription = "YouTube",
                        tint = Color(0xFFFF0000),
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = task.title,
                            color = TextWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Text(
                        text = task.description,
                        color = TextSubtitle,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = GameSurfaceDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, GameGold.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = "+${String.format("%,d", task.rewardCoins)} 🪙",
                        color = GameGoldLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Step Progress State
            when {
                isCompleted -> {
                    // Fully Claimed State
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = GameGreen.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GameGreen),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = GameGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CLAIMED ✓ (+${String.format("%,d", task.rewardCoins)} COINS)",
                                color = GameGreen,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                status == "VERIFIED" -> {
                    // STEP 3: Verification confirmed! User can now watch Rewarded Ad to claim.
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = GameGreen.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GameGreen),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = GameGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Subscription verified! Watch sponsored ad to claim coins.",
                                    color = GameGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        GlossyGameButton(
                            text = if (isWatchingAd) "PREPARING AD..." else "WATCH AD & CLAIM 5,000 🪙",
                            onClick = onClaim,
                            colorType = ButtonColorType.GREEN,
                            height = 44.dp,
                            enabled = !isWatchingAd,
                            modifier = Modifier.fillMaxWidth(),
                            icon = Icons.Default.Star,
                            testTag = "btn_claim_youtube_task"
                        )
                    }
                }

                status == "PENDING_VERIFICATION" -> {
                    // STEP 2: Channel was opened; User subscribes and taps to verify
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Channel opened! Once subscribed, verify below:",
                            color = GameCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = GameSurfaceDark,
                                border = androidx.compose.foundation.BorderStroke(1.dp, GameCardBorder),
                                modifier = Modifier
                                    .clickable { onOpenChannel() }
                                    .padding(vertical = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.OpenInNew,
                                        contentDescription = null,
                                        tint = TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Re-Open",
                                        color = TextWhite,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            GlossyGameButton(
                                text = if (isVerifying) "VERIFYING..." else "VERIFY SUBSCRIPTION",
                                onClick = onVerify,
                                colorType = ButtonColorType.CYAN,
                                height = 42.dp,
                                enabled = !isVerifying,
                                modifier = Modifier.weight(1f),
                                testTag = "btn_verify_youtube_task"
                            )
                        }
                    }
                }

                else -> {
                    // STEP 1: Not started yet. User opens channel to subscribe.
                    GlossyGameButton(
                        text = "SUBSCRIBE CHANNEL",
                        onClick = onOpenChannel,
                        colorType = ButtonColorType.MAGENTA,
                        height = 42.dp,
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Default.Subscriptions,
                        testTag = "btn_subscribe_youtube_task"
                    )
                }
            }
        }
    }
}

/**
 * Standard Task Item Card for Telegram, Streak, Share, and Review tasks.
 */
@Composable
fun GenericTaskItemCard(
    task: GameTask,
    verification: TaskVerification?,
    isWatchingAd: Boolean,
    onStartTask: () -> Unit,
    onClaim: () -> Unit
) {
    val isCompleted = task.isCompleted || verification?.status == "REWARDED"
    val isVerified = verification?.status == "VERIFIED"

    val (icon, iconColor) = when (task.iconType) {
        "TELEGRAM" -> Pair(Icons.Default.Send, Color(0xFF29B6F6))
        "SHARE" -> Pair(Icons.Default.Share, GameGreen)
        "STREAK" -> Pair(Icons.Default.ElectricBolt, GameGold)
        "SURVEY" -> Pair(Icons.Default.RateReview, GameMagenta)
        else -> Pair(Icons.Default.Star, GameGold)
    }

    GameCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("task_item_${task.id}"),
        backgroundColor = GameSurfaceElevated,
        borderColor = if (isCompleted) GameGreen else GameCardBorder
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        color = TextWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = task.description,
                        color = TextSubtitle,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = GameSurfaceDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, GameGold.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "+${String.format("%,d", task.rewardCoins)} COINS",
                        color = GameGoldLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                when {
                    isCompleted -> {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = GameGreen.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GameGreen)
                        ) {
                            Text(
                                text = "CLAIMED ✓",
                                color = GameGreen,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }

                    isVerified -> {
                        GlossyGameButton(
                            text = if (isWatchingAd) "PREPARING..." else "CLAIM (WATCH AD)",
                            onClick = onClaim,
                            colorType = ButtonColorType.GREEN,
                            height = 38.dp,
                            enabled = !isWatchingAd,
                            testTag = "claim_task_button_${task.id}"
                        )
                    }

                    else -> {
                        GlossyGameButton(
                            text = "START TASK",
                            onClick = onStartTask,
                            colorType = ButtonColorType.CYAN,
                            height = 38.dp,
                            testTag = "start_task_button_${task.id}"
                        )
                    }
                }
            }
        }
    }
}
