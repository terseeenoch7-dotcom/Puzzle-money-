package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GameCardBorder
import com.example.ui.theme.GameCyan
import com.example.ui.theme.GameCyanDark
import com.example.ui.theme.GameGold
import com.example.ui.theme.GameGoldDark
import com.example.ui.theme.GameGoldLight
import com.example.ui.theme.GameGreen
import com.example.ui.theme.GameGreenDark
import com.example.ui.theme.GameMagenta
import com.example.ui.theme.GameMagentaDark
import com.example.ui.theme.GameSurfaceDark
import com.example.ui.theme.GameSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtitle
import com.example.ui.theme.TextWhite
import kotlin.random.Random

@Composable
fun GlossyGameButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colorType: ButtonColorType = ButtonColorType.GOLD,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    height: Dp = 54.dp,
    testTag: String = "game_button"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val (topColor, bottomColor, shadowColor) = when (colorType) {
        ButtonColorType.GOLD -> Triple(GameGold, GameGoldDark, Color(0xFF9E6400))
        ButtonColorType.CYAN -> Triple(GameCyan, GameCyanDark, Color(0xFF006064))
        ButtonColorType.MAGENTA -> Triple(GameMagenta, GameMagentaDark, Color(0xFF880E4F))
        ButtonColorType.GREEN -> Triple(GameGreen, GameGreenDark, Color(0xFF00600F))
        ButtonColorType.PURPLE -> Triple(Color(0xFF9D46FF), Color(0xFF6200EA), Color(0xFF3700B3))
    }

    val offsetY = if (isPressed) 4.dp else 0.dp
    val shadowDepth = if (isPressed) 0.dp else 4.dp

    Box(
        modifier = modifier
            .testTag(testTag)
            .height(height)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
    ) {
        // Bottom 3D shadow layer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = shadowDepth)
                .background(shadowColor, RoundedCornerShape(16.dp))
        )

        // Main glossy button body
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = offsetY)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(topColor, bottomColor)
                    )
                )
                .border(1.5.dp, Color.White.copy(alpha = 0.45f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(22.dp)
                            .shadow(2.dp, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    letterSpacing = 0.5.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

enum class ButtonColorType {
    GOLD, CYAN, MAGENTA, GREEN, PURPLE
}

@Composable
fun CoinBalanceBadge(
    coins: Long,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = GameSurfaceElevated,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, GameGold.copy(alpha = 0.6f)),
        modifier = modifier
            .testTag("coin_balance_badge")
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            // Shiny 3D coin icon circle
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(GameGoldLight, GameGold, GameGoldDark)
                        )
                    )
                    .border(1.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$",
                    color = Color(0xFF6B4500),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = String.format("%,d", coins),
                color = GameGoldLight,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun GameCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = GameSurfaceDark,
    borderColor: Color = GameCardBorder,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor),
        modifier = modifier.then(
            if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
        )
    ) {
        content()
    }
}

@Composable
fun VoiceSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onVoiceClick: () -> Unit,
    isRecording: Boolean,
    isTranscribing: Boolean,
    placeholder: String = "Search puzzle pictures...",
    modifier: Modifier = Modifier
) {
    GameCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = GameSurfaceDark,
        borderColor = if (isRecording) GameMagenta else GameCardBorder
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = TextSubtitle
            )

            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = {
                    Text(
                        text = if (isRecording) "Listening... speak now" else placeholder,
                        color = if (isRecording) GameMagenta else TextMuted,
                        fontSize = 14.sp
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("search_text_input"),
                singleLine = true
            )

            if (isTranscribing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = GameCyan,
                    strokeWidth = 2.5.dp
                )
            } else {
                IconButton(
                    onClick = onVoiceClick,
                    modifier = Modifier
                        .testTag("voice_search_mic_button")
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isRecording) GameMagenta else GameSurfaceElevated)
                ) {
                    Icon(
                        imageVector = if (isRecording) Icons.Default.Mic else Icons.Default.MicNone,
                        contentDescription = "Voice Search",
                        tint = if (isRecording) Color.White else GameGold
                    )
                }
            }
        }
    }
}

@Composable
fun CelebrationPopup(
    rewardCoins: Long,
    moves: Int,
    timeSeconds: Long,
    puzzleTitle: String,
    onNextPuzzle: () -> Unit,
    onLibrary: () -> Unit,
    onHome: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.82f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        // Confetti burst animation
        ConfettiEffect()

        AnimatedVisibility(
            visible = true,
            enter = scaleIn(tween(400)) + fadeIn()
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
                    Icon(
                        imageVector = Icons.Default.Celebration,
                        contentDescription = null,
                        tint = GameGold,
                        modifier = Modifier.size(56.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "PUZZLE COMPLETE!",
                        color = GameGoldLight,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = puzzleTitle,
                        color = TextSubtitle,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Reward banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(GameGoldDark, GameGold, GameGoldDark)
                                )
                            )
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "+$rewardCoins COINS",
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stats row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatBadge(title = "Moves", value = "$moves")
                        StatBadge(title = "Time", value = "${timeSeconds}s")
                        StatBadge(title = "Rating", value = "★★★")
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action buttons matching PRD: NEXT PUZZLE, PUZZLE LIBRARY, HOME
                    GlossyGameButton(
                        text = "NEXT PUZZLE",
                        onClick = onNextPuzzle,
                        colorType = ButtonColorType.GOLD,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "celebration_next_puzzle_button"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    GlossyGameButton(
                        text = "PUZZLE LIBRARY",
                        onClick = onLibrary,
                        colorType = ButtonColorType.CYAN,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "celebration_library_button"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    GlossyGameButton(
                        text = "HOME",
                        onClick = onHome,
                        colorType = ButtonColorType.PURPLE,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "celebration_home_button"
                    )
                }
            }
        }
    }
}

@Composable
fun StatBadge(title: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(GameSurfaceDark)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(text = title, color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(text = value, color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
fun ConfettiEffect() {
    val particles = remember {
        List(30) {
            ConfettiParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat() * -0.5f,
                color = listOf(GameGold, GameCyan, GameMagenta, GameGreen, Color.White).random(),
                size = Random.nextFloat() * 12f + 6f
            )
        }
    }

    val transition = rememberInfiniteTransition(label = "confetti")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confetti_progress"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { p ->
            val curY = (p.y + progress * 1.5f) % 1.2f
            drawCircle(
                color = p.color,
                radius = p.size,
                center = Offset(p.x * size.width, curY * size.height)
            )
        }
    }
}

data class ConfettiParticle(
    val x: Float,
    val y: Float,
    val color: Color,
    val size: Float
)
