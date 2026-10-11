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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.AdBannerComposable
import com.example.data.auth.AdminAuthHelper
import com.example.ui.components.ButtonColorType
import com.example.ui.components.CoinBalanceBadge
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

@Composable
fun ProfileScreen(
    walletViewModel: WalletViewModel,
    authViewModel: com.example.ui.viewmodel.AuthViewModel,
    onNavigateToWallet: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onNavigateToLibrary: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val user by walletViewModel.user.collectAsState()
    val firebaseUser by authViewModel.currentUser.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? android.app.Activity
    var is2FaEnabled by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
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
                Text(
                    text = "Player Profile",
                    color = TextWhite,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )

                CoinBalanceBadge(
                    coins = user?.coinBalance ?: 0L,
                    onClick = onNavigateToWallet
                )
            }
        }

        // Profile Overview Card
        item {
            GameCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = GameSurfaceElevated,
                borderColor = GameGold
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(GameMagenta, GameGold))
                            )
                            .border(3.dp, GameGoldLight, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(46.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val displayName = firebaseUser?.displayName?.ifBlank { null } ?: user?.username ?: "Puzzle Master"
                    val displayEmail = firebaseUser?.email ?: user?.email ?: "player@puzzleearn.com"

                    Text(
                        text = displayName,
                        color = TextWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )

                    Text(
                        text = displayEmail,
                        color = TextSubtitle,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (firebaseUser != null) GameGreen.copy(alpha = 0.2f) else GameCyan.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (firebaseUser != null) GameGreen else GameCyan)
                    ) {
                        Text(
                            text = if (firebaseUser != null) "GOOGLE AUTHENTICATED ✓" else "GUEST / LOCAL PROFILE",
                            color = if (firebaseUser != null) GameGreen else GameCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Player Stats Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ProfileStatItem(title = "Balance", value = String.format("%,d", user?.coinBalance ?: 0L))
                        ProfileStatItem(title = "Earned", value = String.format("%,d", user?.totalCoinsEarned ?: 0L))
                        ProfileStatItem(title = "Payouts", value = String.format("%,d", user?.totalCoinsWithdrawn ?: 0L))
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }

        // TOP AD BANNER
        item(key = "profile_top_banner") {
            AdBannerComposable()
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Admin Access Launcher Card (strictly visible ONLY to crowned administrator accounts)
        if (AdminAuthHelper.isUserAdmin(user?.role, user?.email)) {
            item(key = "profile_admin_card") {
                GameCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = GameSurfaceElevated,
                    borderColor = GameCyan,
                    onClick = onNavigateToAdmin
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(GameCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = GameCyan,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Admin Control Room",
                                color = TextWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Manage puzzles, reward policy, users & payouts",
                                color = TextSubtitle,
                                fontSize = 12.sp
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = GameCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
            }
        }

        // Account & Security Options
        item {
            Text(
                text = "Security & Settings",
                color = TextWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(10.dp))

            GameCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = GameSurfaceElevated,
                borderColor = GameCardBorder
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Google 2FA Authenticator Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = GameGold)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "Google Authenticator 2FA", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(text = "Extra protection for payouts", color = TextMuted, fontSize = 11.sp)
                            }
                        }

                        Switch(
                            checked = is2FaEnabled,
                            onCheckedChange = { is2FaEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = GameGold,
                                checkedTrackColor = GameGoldDark
                            )
                        )
                    }

                    HorizontalDivider(color = GameCardBorder.copy(alpha = 0.5f))

                    // My Favorites
                    ProfileMenuRow(
                        icon = Icons.Default.Star,
                        title = "Favorite Puzzles",
                        subtitle = "Jump back into saved pictures",
                        onClick = onNavigateToLibrary
                    )

                    HorizontalDivider(color = GameCardBorder.copy(alpha = 0.5f))

                    // Help & FAQ
                    ProfileMenuRow(
                        icon = Icons.Default.HelpOutline,
                        title = "How Rewards Work",
                        subtitle = "1,000,000 Coins = $1.00 USD conversion rules",
                        onClick = onNavigateToWallet
                    )
                }
            }

            if (firebaseUser != null) {
                GlossyGameButton(
                    text = "SIGN OUT OF GOOGLE",
                    onClick = {
                        authViewModel.signOut()
                        onSignOut()
                    },
                    colorType = ButtonColorType.PURPLE,
                    icon = Icons.Default.Logout,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "profile_logout_button"
                )
            } else {
                com.example.ui.screens.GoogleSignInButton(
                    onClick = {
                        activity?.let { authViewModel.signInWithGoogle(it) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "profile_google_sign_in_button"
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // BOTTOM AD BANNER
        item(key = "profile_bottom_banner") {
            AdBannerComposable()
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ProfileStatItem(title: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(GameSurfaceDark)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(text = title, color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text(text = value, color = GameGoldLight, fontSize = 15.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
fun ProfileMenuRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = GameCyan)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(text = subtitle, color = TextSubtitle, fontSize = 11.sp)
        }
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
    }
}
