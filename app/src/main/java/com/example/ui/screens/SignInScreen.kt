package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.auth.AuthState
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
import com.example.ui.viewmodel.AuthViewModel

@Composable
fun SignInScreen(
    authViewModel: AuthViewModel,
    onContinueAsGuest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(Unit) {
        activity?.let { authViewModel.attemptSilentSignIn(it) }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GameBackgroundDark)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Icon Graphic
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.radialGradient(listOf(GameGoldLight, GameGold, GameGoldDark))
                    )
                    .border(3.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_icon_puzzle),
                    contentDescription = "Puzzle Earn App Icon",
                    modifier = Modifier
                        .size(92.dp)
                        .clip(RoundedCornerShape(22.dp))
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Puzzle Earn",
                color = TextWhite,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )

            Text(
                text = "Play Puzzles, Earn Real Coins!",
                color = GameGoldLight,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Game Highlights
            GameCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = GameSurfaceElevated,
                borderColor = GameCardBorder
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    FeatureBullet(
                        icon = Icons.Default.Extension,
                        title = "Classic Sliding Picture Puzzles",
                        subtitle = "Solve 3x3, 4x4 & 5x5 picture tiles"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    FeatureBullet(
                        icon = Icons.Default.Stars,
                        title = "100 Coins Per Solved Puzzle",
                        subtitle = "Instant validated coin rewards"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    FeatureBullet(
                        icon = Icons.Default.AccountBalanceWallet,
                        title = "Cash Payouts via PayPal & Bank",
                        subtitle = "1,000,000 Coins = $1.00 USD"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Error Message
            if (authState is AuthState.Error) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GameMagenta.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GameMagenta),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        text = (authState as AuthState.Error).message,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Google Sign-In Button
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(
                    color = GameGold,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Signing in with Google...",
                    color = TextSubtitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                GoogleSignInButton(
                    onClick = {
                        activity?.let { authViewModel.signInWithGoogle(it) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "google_sign_in_button"
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Or play in Offline / Guest Mode",
                    color = TextMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { onContinueAsGuest() }
                        .padding(8.dp)
                        .testTag("guest_mode_button")
                )
            }
        }
    }
}

@Composable
fun GoogleSignInButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "google_sign_in_btn"
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFDDDDDD)),
        shadowElevation = 4.dp,
        modifier = modifier
            .height(54.dp)
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Google 'G' colorful circular emblem
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4285F4)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "G",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Sign in with Google",
                color = Color(0xFF1F1F1F),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun FeatureBullet(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(GameCyan.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GameCyan,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = title,
                color = TextWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = subtitle,
                color = TextSubtitle,
                fontSize = 11.sp
            )
        }
    }
}
