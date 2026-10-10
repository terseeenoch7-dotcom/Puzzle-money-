package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ButtonColorType
import com.example.ui.components.GameCard
import com.example.ui.components.GlossyGameButton
import com.example.ui.theme.GameBackgroundDark
import com.example.ui.theme.GameCardBorder
import com.example.ui.theme.GameCyan
import com.example.ui.theme.GameGold
import com.example.ui.theme.GameGoldDark
import com.example.ui.theme.GameGoldLight
import com.example.ui.theme.GameGreen
import com.example.ui.theme.GameMagenta
import com.example.ui.theme.GameSurfaceDark
import com.example.ui.theme.GameSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtitle
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.AdminViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminScreen(
    adminViewModel: AdminViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stats by adminViewModel.dashboardStats.collectAsState()
    val users by adminViewModel.users.collectAsState()
    val puzzles by adminViewModel.puzzles.collectAsState()
    val withdrawals by adminViewModel.withdrawals.collectAsState()
    val settings by adminViewModel.settings.collectAsState()
    val auditLogs by adminViewModel.auditLogs.collectAsState()
    val feedback by adminViewModel.feedback.collectAsState()
    val isGeneratingAi by adminViewModel.isGeneratingAi.collectAsState()

    val adAnalytics by adminViewModel.adAnalytics.collectAsState()
    val networkPriority by adminViewModel.networkPriority.collectAsState()
    val isAdTestMode by adminViewModel.isAdTestMode.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? android.app.Activity

    var activeTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Overview", "Ads Monetization", "Puzzles", "Rewards", "Withdrawals", "Users", "Audit Logs")

    // Add Puzzle Form State
    var newTitle by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("Animals") }
    var newDifficulty by remember { mutableStateOf("Easy") }
    var newImageName by remember { mutableStateOf("puzzle_lion") }
    var aiConceptKeyword by remember { mutableStateOf("Cyber Dragon") }

    // Coin Adjustment State
    var adjustUserId by remember { mutableStateOf("user_default_1") }
    var adjustDelta by remember { mutableStateOf("1000") }
    var adjustReason by remember { mutableStateOf("Tournament Reward") }

    BackHandler {
        onNavigateBack()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GameBackgroundDark)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        // Admin Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(GameSurfaceElevated)
                    .testTag("admin_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextWhite
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "Admin Control Room",
                    color = TextWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Role: SUPER_ADMIN • Live Economy Management",
                    color = GameCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Feedback Banner
        if (feedback.message != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (feedback.isError) GameMagenta.copy(alpha = 0.2f) else GameGreen.copy(alpha = 0.2f),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (feedback.isError) GameMagenta else GameGreen),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .clickable { adminViewModel.dismissFeedback() }
            ) {
                Text(
                    text = feedback.message ?: "",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // Admin Section Tabs
        ScrollableTabRow(
            selectedTabIndex = activeTab,
            containerColor = Color.Transparent,
            contentColor = GameGold,
            edgePadding = 0.dp,
            indicator = { tabPositions ->
                if (activeTab < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                        color = GameGold
                    )
                }
            },
            divider = {}
        ) {
            tabTitles.forEachIndexed { idx, title ->
                Tab(
                    selected = activeTab == idx,
                    onClick = { activeTab = idx },
                    text = {
                        Text(
                            text = title,
                            color = if (activeTab == idx) GameGoldLight else TextSubtitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Main Tab Content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            when (activeTab) {
                0 -> {
                    // TAB 0: Dashboard Overview Stats
                    item {
                        Text(text = "Platform Telemetry", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            AdminMetricCard(title = "Total Users", value = "${stats.totalUsers}", modifier = Modifier.weight(1f))
                            AdminMetricCard(title = "Total Puzzles", value = "${stats.totalPuzzles}", modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            AdminMetricCard(title = "Puzzles Played", value = "${stats.totalPlays}", modifier = Modifier.weight(1f))
                            AdminMetricCard(title = "Puzzles Solved", value = "${stats.totalSolves}", modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            AdminMetricCard(title = "Coins Distributed", value = String.format("%,d", stats.totalCoinsDistributed), modifier = Modifier.weight(1f))
                            AdminMetricCard(title = "Pending Payouts", value = "${stats.pendingWithdrawalsCount} ($${String.format(Locale.US, "%.1f", stats.totalPendingUsd)})", modifier = Modifier.weight(1f))
                        }
                    }
                }

                1 -> {
                    // TAB 1: Ad Monetization & Network Telemetry
                    item {
                        Text(text = "Ad Networks Monetization", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Black)
                        Text(text = "Hybrid Google AdMob + Unity Ads waterfall orchestration", color = TextSubtitle, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    item {
                        GameCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = GameSurfaceElevated,
                            borderColor = GameGold
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = "Network Priority Strategy", color = TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Black)
                                Text(text = "Active Priority: $networkPriority", color = GameGoldLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("ADMOB_FIRST", "UNITY_FIRST", "ADMOB_ONLY", "UNITY_ONLY").forEach { p ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (networkPriority == p) GameGold else GameSurfaceDark,
                                            modifier = Modifier.clickable { adminViewModel.setAdNetworkPriority(p) }
                                        ) {
                                            Text(
                                                text = p.replace("_", " "),
                                                color = if (networkPriority == p) Color.Black else TextWhite,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = "Environment Mode", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text(text = if (isAdTestMode) "Test Mode (Safe Sandbox)" else "Production Live Ads", color = if (isAdTestMode) GameCyan else GameGreen, fontSize = 11.sp)
                                    }

                                    GlossyGameButton(
                                        text = if (isAdTestMode) "TEST MODE: ON" else "TEST MODE: OFF",
                                        onClick = { adminViewModel.toggleAdTestMode(!isAdTestMode) },
                                        colorType = if (isAdTestMode) ButtonColorType.CYAN else ButtonColorType.GREEN,
                                        height = 36.dp
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    item {
                        Text(text = "Live Ad Analytics Telemetry", color = TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            AdminMetricCard(title = "Total Ad Requests", value = "${adAnalytics.totalRequests}", modifier = Modifier.weight(1f))
                            AdminMetricCard(title = "Ads Loaded", value = "${adAnalytics.totalLoaded}", modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            AdminMetricCard(title = "Impressions Shown", value = "${adAnalytics.totalShown}", modifier = Modifier.weight(1f))
                            AdminMetricCard(title = "Rewarded Solves", value = "${adAnalytics.totalRewardedCompleted}", modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            AdminMetricCard(title = "AdMob Impressions", value = "${adAnalytics.admobCount}", modifier = Modifier.weight(1f))
                            AdminMetricCard(title = "Unity Impressions", value = "${adAnalytics.unityCount}", modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    item {
                        AdminMetricCard(
                            title = "Estimated Ad Revenue (USD)",
                            value = "$${String.format(Locale.US, "%.3f", adAnalytics.estimatedRevenueUsd)}",
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    item {
                        GameCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = GameSurfaceElevated,
                            borderColor = GameCyan
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = "Quick Ad Diagnostics", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Black)
                                Text(text = "Simulate rewarded video ad playback on the active network", color = TextSubtitle, fontSize = 12.sp)

                                Spacer(modifier = Modifier.height(10.dp))

                                GlossyGameButton(
                                    text = "TEST PLAY REWARDED AD (100 🪙)",
                                    onClick = {
                                        if (activity != null) {
                                            adminViewModel.testRewardedAd(activity)
                                        }
                                    },
                                    colorType = ButtonColorType.GOLD,
                                    height = 42.dp,
                                    modifier = Modifier.fillMaxWidth(),
                                    icon = Icons.Default.PlayArrow
                                )
                            }
                        }
                    }
                }

                2 -> {
                    // TAB 2: Puzzle Management
                    item {
                        GameCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = GameSurfaceElevated,
                            borderColor = GameCyan
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = "Add New Picture Puzzle", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Black)
                                Text(text = "Unlimited picture expansion without app updates", color = TextSubtitle, fontSize = 12.sp)

                                Spacer(modifier = Modifier.height(12.dp))

                                // AI Concept Generator
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    OutlinedTextField(
                                        value = aiConceptKeyword,
                                        onValueChange = { aiConceptKeyword = it },
                                        placeholder = { Text("AI theme prompt", color = TextMuted, fontSize = 12.sp) },
                                        modifier = Modifier.weight(1f),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = GameMagenta,
                                            unfocusedBorderColor = GameCardBorder,
                                            focusedTextColor = TextWhite,
                                            unfocusedTextColor = TextWhite
                                        ),
                                        singleLine = true
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    GlossyGameButton(
                                        text = if (isGeneratingAi) "..." else "AI IDEA",
                                        onClick = {
                                            adminViewModel.generateAiPuzzleConcept(aiConceptKeyword) { genTitle, genCat ->
                                                newTitle = genTitle
                                            }
                                        },
                                        colorType = ButtonColorType.MAGENTA,
                                        height = 48.dp,
                                        enabled = !isGeneratingAi,
                                        icon = Icons.Default.AutoAwesome
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = newTitle,
                                    onValueChange = { newTitle = it },
                                    label = { Text("Puzzle Title", color = TextSubtitle) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = GameGold,
                                        unfocusedBorderColor = GameCardBorder,
                                        focusedTextColor = TextWhite,
                                        unfocusedTextColor = TextWhite
                                    ),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Category selector
                                Text(text = "Category: $newCategory", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf("Animals", "Cars", "Space", "Nature", "Fantasy").forEach { cat ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (newCategory == cat) GameGold else GameSurfaceDark,
                                            modifier = Modifier.clickable { newCategory = cat }
                                        ) {
                                            Text(
                                                text = cat,
                                                color = if (newCategory == cat) Color.Black else TextWhite,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Grid Size / Difficulty selector
                                Text(text = "Grid Size / Difficulty: $newDifficulty", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("Easy", "Medium", "Hard").forEach { diff ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (newDifficulty == diff) GameCyan else GameSurfaceDark,
                                            modifier = Modifier.clickable { newDifficulty = diff }
                                        ) {
                                            Text(
                                                text = diff,
                                                color = if (newDifficulty == diff) Color.Black else TextWhite,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Image Asset Selection
                                Text(text = "Image Artwork: $newImageName", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf("puzzle_lion", "puzzle_supercar", "puzzle_cosmic", "app_icon_puzzle").forEach { img ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (newImageName == img) GameGreen else GameSurfaceDark,
                                            modifier = Modifier.clickable { newImageName = img }
                                        ) {
                                            Text(
                                                text = img.replace("puzzle_", ""),
                                                color = if (newImageName == img) Color.Black else TextWhite,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                GlossyGameButton(
                                    text = "PUBLISH NEW PUZZLE",
                                    onClick = {
                                        if (newTitle.isNotBlank()) {
                                            adminViewModel.addNewPuzzle(
                                                title = newTitle,
                                                category = newCategory,
                                                difficulty = newDifficulty,
                                                imageName = newImageName,
                                                isFeatured = true
                                            )
                                            newTitle = ""
                                        }
                                    },
                                    colorType = ButtonColorType.GOLD,
                                    modifier = Modifier.fillMaxWidth(),
                                    testTag = "admin_publish_puzzle_button"
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(text = "Existing Puzzles in Database (${puzzles.size})", color = TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    items(puzzles, key = { it.id }) { p ->
                        GameCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            backgroundColor = GameSurfaceElevated,
                            borderColor = GameCardBorder
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = p.title, color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "${p.category} • ${p.gridSize}x${p.gridSize} (${p.difficulty}) • Plays: ${p.playCount} • Solves: ${p.solveCount}",
                                        color = TextSubtitle,
                                        fontSize = 11.sp
                                    )
                                }

                                IconButton(onClick = { adminViewModel.deletePuzzle(p.id) }) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = GameMagenta)
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // TAB 3: Financial & Reward Settings (Audit logged!)
                    item {
                        Text(text = "Platform Reward & Financial Settings", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Black)
                        Text(text = "Modifications are audited and take effect instantly", color = TextSubtitle, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    items(settings, key = { it.key }) { setting ->
                        AdminSettingEditorCard(
                            setting = setting,
                            onSave = { newVal, reason ->
                                adminViewModel.updateSetting(setting.key, newVal, reason)
                            }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

                4 -> {
                    // TAB 4: Withdrawal Payout Processing
                    item {
                        Text(text = "User Withdrawal Requests (${withdrawals.size})", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    items(withdrawals, key = { it.id }) { wd ->
                        GameCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            backgroundColor = GameSurfaceElevated,
                            borderColor = GameCardBorder
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "$${String.format(Locale.US, "%.2f", wd.cashAmountUsd)} (${String.format("%,d", wd.coinAmount)} coins)",
                                        color = GameGoldLight,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(text = wd.status, color = GameCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                Text(text = "User: ${wd.userId} • Method: ${wd.method}", color = TextSubtitle, fontSize = 12.sp)
                                Text(text = "Target: ${wd.paymentDetails}", color = TextWhite, fontSize = 12.sp)

                                if (wd.status == "PENDING") {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        GlossyGameButton(
                                            text = "APPROVE & PAY",
                                            onClick = { adminViewModel.processWithdrawal(wd.id, "PAID", "Processed by Super Admin") },
                                            colorType = ButtonColorType.GREEN,
                                            height = 36.dp,
                                            modifier = Modifier.weight(1f)
                                        )

                                        GlossyGameButton(
                                            text = "REJECT",
                                            onClick = { adminViewModel.processWithdrawal(wd.id, "REJECTED", "Rejected by Admin") },
                                            colorType = ButtonColorType.MAGENTA,
                                            height = 36.dp,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                5 -> {
                    // TAB 5: User Management & Coin Adjustment
                    item {
                        GameCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = GameSurfaceElevated,
                            borderColor = GameGold
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = "Manual Coin Adjustment", color = TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Black)
                                Text(text = "Credit or debit user coins (creates audit log)", color = TextSubtitle, fontSize = 12.sp)

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = adjustUserId,
                                    onValueChange = { adjustUserId = it },
                                    label = { Text("User ID", color = TextSubtitle) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = adjustDelta,
                                    onValueChange = { adjustDelta = it },
                                    label = { Text("Coin Amount (+ or -)", color = TextSubtitle) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = adjustReason,
                                    onValueChange = { adjustReason = it },
                                    label = { Text("Reason", color = TextSubtitle) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                GlossyGameButton(
                                    text = "APPLY COIN ADJUSTMENT",
                                    onClick = {
                                        val delta = adjustDelta.toLongOrNull() ?: 0L
                                        adminViewModel.adjustUserCoins(adjustUserId, delta, adjustReason)
                                    },
                                    colorType = ButtonColorType.GOLD,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Registered Users (${users.size})", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    items(users, key = { it.id }) { u ->
                        GameCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            backgroundColor = GameSurfaceElevated,
                            borderColor = GameCardBorder
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = u.username, color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text(text = "${u.email} • ID: ${u.id}", color = TextMuted, fontSize = 11.sp)
                                    Text(text = "Balance: ${String.format("%,d", u.coinBalance)} 🪙 • Role: ${u.role}", color = GameGoldLight, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                6 -> {
                    // TAB 6: Immutable Audit Logs
                    item {
                        Text(text = "Security Audit Logs (${auditLogs.size})", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    items(auditLogs) { log ->
                        val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }
                        GameCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            backgroundColor = GameSurfaceElevated,
                            borderColor = GameCardBorder
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = log.action, color = GameCyan, fontSize = 13.sp, fontWeight = FontWeight.Black)
                                    Text(text = dateFormat.format(Date(log.timestamp)), color = TextMuted, fontSize = 11.sp)
                                }
                                Text(text = "Target: ${log.target} • Admin: ${log.adminId}", color = TextWhite, fontSize = 12.sp)
                                Text(text = "Change: '${log.oldValue}' → '${log.newValue}'", color = GameGoldLight, fontSize = 12.sp)
                                Text(text = "Reason: ${log.reason}", color = TextSubtitle, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminMetricCard(title: String, value: String, modifier: Modifier = Modifier) {
    GameCard(
        modifier = modifier,
        backgroundColor = GameSurfaceElevated,
        borderColor = GameCardBorder
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, color = TextSubtitle, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, color = GameGoldLight, fontSize = 18.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun AdminSettingEditorCard(
    setting: com.example.data.model.AdminSetting,
    onSave: (String, String) -> Unit
) {
    var editValue by remember(setting.value) { mutableStateOf(setting.value) }
    var reason by remember { mutableStateOf("Admin update") }

    GameCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = GameSurfaceElevated,
        borderColor = GameCardBorder
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = setting.key, color = GameCyan, fontSize = 13.sp, fontWeight = FontWeight.Black)
            Text(text = setting.description, color = TextSubtitle, fontSize = 11.sp)

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = editValue,
                    onValueChange = { editValue = it },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = GameGold,
                        unfocusedBorderColor = GameCardBorder
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                GlossyGameButton(
                    text = "SAVE",
                    onClick = { onSave(editValue, reason) },
                    colorType = ButtonColorType.GOLD,
                    height = 48.dp
                )
            }
        }
    }
}
