package com.example.ui.screens

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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.AdBannerComposable
import com.example.data.model.CoinTransaction
import com.example.data.model.Withdrawal
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
import com.example.ui.viewmodel.WalletViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WalletScreen(
    walletViewModel: WalletViewModel,
    modifier: Modifier = Modifier
) {
    val user by walletViewModel.user.collectAsState()
    val transactions by walletViewModel.transactions.collectAsState()
    val withdrawals by walletViewModel.withdrawals.collectAsState()
    val uiState by walletViewModel.uiState.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Withdraw, 1: History, 2: Withdrawals

    val availableCoins = user?.coinBalance ?: 0L
    val conversionRate = uiState.conversionRate.coerceAtLeast(1L)
    val availableUsd = availableCoins.toDouble() / conversionRate.toDouble()
    val requestedCoins = uiState.withdrawalCoinsInput.toLongOrNull() ?: 0L
    val requestedUsd = requestedCoins.toDouble() / conversionRate.toDouble()

    val methods = listOf("PayPal", "Bank Transfer", "Crypto USDT", "Amazon Gift")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(GameBackgroundDark)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // Header
        item {
            Text(
                text = "Coin Wallet",
                color = TextWhite,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "1,000,000 Coins = $1.00 USD • Real Cash Payouts",
                color = GameGoldLight,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Virtual Wallet Hero Card
        item {
            GameCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = GameSurfaceElevated,
                borderColor = GameGold
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AVAILABLE BALANCE",
                            color = TextSubtitle,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = GameGold,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = String.format("%,d", availableCoins),
                            color = GameGoldLight,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "COINS",
                            color = GameGold,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Text(
                        text = "≈ $${String.format(Locale.US, "%.2f", availableUsd)} USD",
                        color = GameCyan,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stats row: Earned vs Withdrawn
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(GameSurfaceDark)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Total Earned", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "${String.format("%,d", user?.totalCoinsEarned ?: 0L)} 🪙",
                                color = TextWhite,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Total Withdrawn", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "${String.format("%,d", user?.totalCoinsWithdrawn ?: 0L)} 🪙",
                                color = GameGreen,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // TOP AD BANNER
        item(key = "wallet_top_banner") {
            AdBannerComposable()
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Sub Tabs: Request Payout, Ledger History, My Requests
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = GameGold,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = GameGold
                    )
                },
                divider = {}
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "Withdraw",
                            color = if (selectedTab == 0) GameGoldLight else TextSubtitle,
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "Ledger",
                            color = if (selectedTab == 1) GameGoldLight else TextSubtitle,
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Text(
                            text = "Requests",
                            color = if (selectedTab == 2) GameGoldLight else TextSubtitle,
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // MIDDLE AD BANNER
        item(key = "wallet_middle_banner") {
            AdBannerComposable()
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Tab Content
        if (selectedTab == 0) {
            // Withdrawal Request Form
            item {
                GameCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = GameSurfaceElevated,
                    borderColor = GameCardBorder
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Text(
                            text = "Request Payout",
                            color = TextWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Minimum withdrawal is %,d coins ($10.00 USD)".format(uiState.minWithdrawalCoins),
                            color = TextSubtitle,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Method selection
                        Text(
                            text = "Select Payout Method",
                            color = TextWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            methods.forEach { method ->
                                val isSelected = uiState.selectedMethod == method
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) GameCyan else GameSurfaceDark,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) GameCyan else GameCardBorder),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { walletViewModel.setMethod(method) }
                                        .testTag("method_${method.replace(" ", "_")}")
                                ) {
                                    Text(
                                        text = method,
                                        color = if (isSelected) Color.Black else TextWhite,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 10.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Coins to withdraw input
                        Text(
                            text = "Coins to Withdraw",
                            color = TextWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = uiState.withdrawalCoinsInput,
                            onValueChange = { walletViewModel.setWithdrawalCoinsInput(it) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_withdrawal_coins"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GameGold,
                                unfocusedBorderColor = GameCardBorder,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            ),
                            supportingText = {
                                Text(
                                    text = "Payout amount: ≈ $${String.format(Locale.US, "%.2f", requestedUsd)} USD",
                                    color = GameGoldLight
                                )
                            }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Account details input
                        Text(
                            text = "Account / Address Details",
                            color = TextWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = uiState.paymentDetailsInput,
                            onValueChange = { walletViewModel.setPaymentDetailsInput(it) },
                            placeholder = {
                                Text(
                                    text = when (uiState.selectedMethod) {
                                        "PayPal" -> "Enter PayPal email address"
                                        "Crypto USDT" -> "Enter USDT (TRC20 / ERC20) address"
                                        "Amazon Gift" -> "Enter delivery email address"
                                        else -> "Enter IBAN / Bank Account Details"
                                    },
                                    color = TextMuted,
                                    fontSize = 13.sp
                                )
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_payment_details"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GameCyan,
                                unfocusedBorderColor = GameCardBorder,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Feedback Messages
                        if (uiState.errorMessage != null) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = GameMagenta.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, GameMagenta),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Default.Error, contentDescription = null, tint = GameMagenta)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = uiState.errorMessage ?: "", color = Color.White, fontSize = 12.sp)
                                }
                            }
                        }

                        if (uiState.successMessage != null) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = GameGreen.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, GameGreen),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = GameGreen)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = uiState.successMessage ?: "", color = Color.White, fontSize = 12.sp)
                                }
                            }
                        }

                        GlossyGameButton(
                            text = if (uiState.isSubmitting) "PROCESSING..." else "SUBMIT WITHDRAWAL",
                            onClick = { walletViewModel.submitWithdrawal() },
                            colorType = ButtonColorType.GOLD,
                            enabled = !uiState.isSubmitting,
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "submit_withdrawal_button"
                        )
                    }
                }
            }
        } else if (selectedTab == 1) {
            // Coin Transaction Ledger
            item {
                Text(
                    text = "Transaction Ledger (${transactions.size})",
                    color = TextWhite,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (transactions.isEmpty()) {
                item {
                    Text(text = "No coin transactions yet.", color = TextMuted, fontSize = 13.sp)
                }
            } else {
                items(transactions, key = { it.id }) { tx ->
                    TransactionItemCard(tx = tx)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        } else {
            // Payout Requests List
            item {
                Text(
                    text = "Withdrawal Requests (${withdrawals.size})",
                    color = TextWhite,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (withdrawals.isEmpty()) {
                item {
                    Text(text = "No withdrawal requests submitted yet.", color = TextMuted, fontSize = 13.sp)
                }
            } else {
                items(withdrawals, key = { it.id }) { wd ->
                    WithdrawalItemCard(wd = wd)
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }

        // BOTTOM AD BANNER
        item(key = "wallet_bottom_banner") {
            Spacer(modifier = Modifier.height(10.dp))
            AdBannerComposable()
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun TransactionItemCard(tx: CoinTransaction) {
    val isPositive = tx.amount > 0
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }

    GameCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = GameSurfaceElevated,
        borderColor = GameCardBorder
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.description,
                    color = TextWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${dateFormat.format(Date(tx.createdAt))} • Type: ${tx.type}",
                    color = TextMuted,
                    fontSize = 11.sp
                )
                Text(
                    text = "Balance: %,d → %,d".format(tx.balanceBefore, tx.balanceAfter),
                    color = TextSubtitle,
                    fontSize = 11.sp
                )
            }

            Text(
                text = (if (isPositive) "+" else "") + String.format("%,d 🪙", tx.amount),
                color = if (isPositive) GameGoldLight else GameMagenta,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun WithdrawalItemCard(wd: Withdrawal) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }
    val (statusColor, statusBg) = when (wd.status) {
        "PAID" -> Pair(GameGreen, GameGreen.copy(alpha = 0.2f))
        "APPROVED" -> Pair(GameCyan, GameCyan.copy(alpha = 0.2f))
        "REJECTED" -> Pair(GameMagenta, GameMagenta.copy(alpha = 0.2f))
        else -> Pair(GameGold, GameGold.copy(alpha = 0.2f))
    }

    GameCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = GameSurfaceElevated,
        borderColor = GameCardBorder
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "$${String.format(Locale.US, "%.2f", wd.cashAmountUsd)} USD",
                        color = TextWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "${String.format("%,d", wd.coinAmount)} coins via ${wd.method}",
                        color = GameGoldLight,
                        fontSize = 12.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, statusColor)
                ) {
                    Text(
                        text = wd.status,
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Target: ${wd.paymentDetails}",
                color = TextSubtitle,
                fontSize = 12.sp
            )

            Text(
                text = "Submitted: ${dateFormat.format(Date(wd.createdAt))}",
                color = TextMuted,
                fontSize = 11.sp
            )

            if (wd.adminNote.isNotBlank()) {
                Text(
                    text = "Note: ${wd.adminNote}",
                    color = GameCyan,
                    fontSize = 11.sp
                )
            }
        }
    }
}
