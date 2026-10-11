package com.example.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.SlidingPuzzleEngine
import com.example.ui.components.ButtonColorType
import com.example.ui.components.CelebrationPopup
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
import com.example.ui.viewmodel.GameViewModel
import com.example.ui.viewmodel.WalletViewModel

@Composable
fun PuzzleGameScreen(
    puzzleId: String,
    gameViewModel: GameViewModel,
    walletViewModel: WalletViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToLibrary: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNextPuzzle: (String) -> Unit,
    allPuzzleIds: List<String>,
    modifier: Modifier = Modifier
) {
    val uiState by gameViewModel.uiState.collectAsState()
    val user by walletViewModel.user.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? android.app.Activity

    BackHandler {
        onNavigateBack()
    }

    LaunchedEffect(puzzleId) {
        gameViewModel.loadPuzzle(puzzleId)
    }

    // Auto 1-Minute Cycle Ad Trigger: when hidden 1-minute timer expires, call ad and grant extra 1 minute
    LaunchedEffect(uiState.shouldShowCycleAd) {
        if (uiState.shouldShowCycleAd && activity != null) {
            gameViewModel.onCycleAdTriggered(activity)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GameBackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // Top Bar: Back, Puzzle Title, Coin Balance
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(GameSurfaceElevated)
                        .testTag("game_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextWhite
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = uiState.puzzle?.title ?: "Pocket Puzzle",
                        color = TextWhite,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1
                    )
                    Text(
                        text = "${uiState.puzzle?.gridSize ?: 3}x${uiState.puzzle?.gridSize ?: 3} • ${uiState.puzzle?.difficulty ?: "Easy"}",
                        color = GameCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                CoinBalanceBadge(
                    coins = user?.coinBalance ?: 0L,
                    onClick = {}
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Gameplay Stats Row: Moves, Timer, Target
            GameCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = GameSurfaceElevated,
                borderColor = GameCardBorder
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Moves
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "MOVES", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        Text(
                            text = "${uiState.puzzleState?.moveCount ?: 0}",
                            color = GameGoldLight,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    // Active Round Progress (Hidden 1-Minute timer kept under the hood)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = GameSurfaceElevated,
                            border = androidx.compose.foundation.BorderStroke(1.dp, GameCyan.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = GameCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ACTIVE ROUND",
                                    color = GameCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    // Reward badge
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = GameGoldDark.copy(alpha = 0.35f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GameGold)
                    ) {
                        Text(
                            text = "+100 🪙",
                            color = GameGoldLight,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Toolbar Controls: Preview Hint, Number Hint, Gemini AI Hint, Restart
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Preview Full Picture
                GameToolButton(
                    icon = if (uiState.isHintVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    label = "Preview",
                    isActive = uiState.isHintVisible,
                    onClick = { gameViewModel.toggleHintOverlay() },
                    testTag = "btn_preview_hint"
                )

                // Toggle Numbers
                GameToolButton(
                    icon = Icons.Default.FormatListNumbered,
                    label = if (uiState.showTileNumbers) "Hide #" else "Show #",
                    isActive = uiState.showTileNumbers,
                    onClick = { gameViewModel.toggleTileNumbers() },
                    testTag = "btn_toggle_numbers"
                )

                // Gemini AI Hint
                GameToolButton(
                    icon = Icons.Default.AutoAwesome,
                    label = "AI Tip",
                    isActive = uiState.aiHintText != null,
                    onClick = { gameViewModel.requestAiHint() },
                    testTag = "btn_gemini_ai_hint"
                )

                // Restart Puzzle (Shows ad upon restart)
                GameToolButton(
                    icon = Icons.Default.Refresh,
                    label = "Shuffle",
                    isActive = false,
                    onClick = { gameViewModel.restartPuzzleWithAd(activity) },
                    testTag = "btn_restart_puzzle"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Puzzle Board Area
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = GameGold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Shuffling puzzle tiles...",
                            color = TextSubtitle,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                val pState = uiState.puzzleState
                if (pState != null) {
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        val boardSize = minOf(maxWidth, maxHeight - 20.dp)

                        GamePuzzleBoard(
                            gridSize = pState.gridSize,
                            tiles = pState.tiles,
                            tileBitmaps = uiState.tileBitmaps,
                            showNumbers = uiState.showTileNumbers,
                            onTileClicked = { index -> gameViewModel.onTileClicked(index) },
                            modifier = Modifier
                                .size(boardSize)
                                .testTag("sliding_puzzle_board")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Full Image Preview Dialog / Overlay
        if (uiState.isHintVisible && uiState.fullBitmap != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .clickable { gameViewModel.toggleHintOverlay() },
                contentAlignment = Alignment.Center
            ) {
                GameCard(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .padding(16.dp),
                    backgroundColor = GameSurfaceElevated,
                    borderColor = GameCyan
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Original Target Image",
                                color = TextWhite,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                            IconButton(onClick = { gameViewModel.toggleHintOverlay() }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Image(
                            bitmap = uiState.fullBitmap!!.asImageBitmap(),
                            contentDescription = "Target Picture",
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Tap anywhere to resume sliding!",
                            color = TextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Gemini AI Smart Tactical Hint Popup
        if (uiState.aiHintText != null || uiState.isRequestingAiHint) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f))
                    .clickable { gameViewModel.dismissAiHint() },
                contentAlignment = Alignment.Center
            ) {
                GameCard(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .padding(16.dp),
                    backgroundColor = GameSurfaceElevated,
                    borderColor = GameMagenta
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = GameMagenta,
                            modifier = Modifier.size(36.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Gemini Puzzle Coach",
                            color = GameGoldLight,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (uiState.isRequestingAiHint) {
                            CircularProgressIndicator(color = GameMagenta, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Analyzing tile layout...", color = TextSubtitle, fontSize = 13.sp)
                        } else {
                            Text(
                                text = uiState.aiHintText ?: "",
                                color = TextWhite,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        GlossyGameButton(
                            text = "GOT IT!",
                            onClick = { gameViewModel.dismissAiHint() },
                            colorType = ButtonColorType.MAGENTA,
                            height = 42.dp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Solved Waiting Claim Modal: Rewarded Ad verification before coins are granted
        if (uiState.isSolvedWaitingClaim) {
            val context = androidx.compose.ui.platform.LocalContext.current
            val activity = context as? android.app.Activity

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
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
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = GameGold,
                                modifier = Modifier.size(38.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "PUZZLE SOLVED!",
                            color = GameGoldLight,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )

                        Text(
                            text = "Solved in ${uiState.puzzleState?.moveCount ?: 0} moves and ${uiState.elapsedSeconds}s!",
                            color = TextSubtitle,
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = GameSurfaceDark,
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, GameGold)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "REWARD: ",
                                    color = TextWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "+100 COINS 🪙",
                                    color = GameGoldLight,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Watch a quick sponsored video ad to verify completion and credit 100 coins to your wallet.",
                            color = TextMuted,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )

                        if (uiState.errorMessage != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = uiState.errorMessage ?: "",
                                color = Color(0xFFFF5252),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        GlossyGameButton(
                            text = if (uiState.isWatchingAd) "PREPARING AD..." else "WATCH AD & CLAIM 100 🪙",
                            onClick = {
                                if (activity != null) {
                                    gameViewModel.claimRewardWithAd(activity)
                                }
                            },
                            colorType = ButtonColorType.GREEN,
                            height = 50.dp,
                            enabled = !uiState.isWatchingAd,
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "btn_claim_puzzle_coins"
                        )
                    }
                }
            }
        }

        // Celebration Modal (Confetti + +100 Coins + Next / Library / Home)
        if (uiState.isCompleteCelebration) {
            CelebrationPopup(
                rewardCoins = uiState.rewardCoinsEarned,
                moves = uiState.puzzleState?.moveCount ?: 0,
                timeSeconds = uiState.elapsedSeconds,
                puzzleTitle = uiState.puzzle?.title ?: "Puzzle",
                onNextPuzzle = {
                    val currentId = uiState.puzzle?.id
                    val others = allPuzzleIds.filter { it != currentId }
                    val nextId = if (others.isNotEmpty()) others.random() else currentId ?: "puzzle_supercar_1"
                    onNextPuzzle(nextId)
                },
                onLibrary = onNavigateToLibrary,
                onHome = onNavigateToHome
            )
        }
    }
}

@Composable
fun GameToolButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isActive) GameCyan else GameSurfaceElevated,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isActive) GameCyan else GameCardBorder),
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) Color.Black else TextWhite,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = if (isActive) Color.Black else TextWhite,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun GamePuzzleBoard(
    gridSize: Int,
    tiles: List<Int>,
    tileBitmaps: Map<Int, Bitmap>,
    showNumbers: Boolean,
    onTileClicked: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.radialGradient(
                    listOf(GameSurfaceElevated, GameSurfaceDark)
                )
            )
            .border(3.dp, GameCardBorder, RoundedCornerShape(24.dp))
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (row in 0 until gridSize) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (col in 0 until gridSize) {
                        val boardIndex = row * gridSize + col
                        val tileId = tiles.getOrNull(boardIndex) ?: SlidingPuzzleEngine.BLANK_ID

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            if (tileId != SlidingPuzzleEngine.BLANK_ID) {
                                val bitmap = tileBitmaps[tileId]
                                PuzzleTileItem(
                                    tileId = tileId,
                                    bitmap = bitmap,
                                    showNumber = showNumbers,
                                    onClick = { onTileClicked(boardIndex) }
                                )
                            } else {
                                // Blank slot: Subtle recessed slot
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.Black.copy(alpha = 0.35f))
                                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PuzzleTileItem(
    tileId: Int,
    bitmap: Bitmap?,
    showNumber: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(14.dp))
            .background(GameSurfaceDark)
            .border(1.5.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("puzzle_tile_$tileId")
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Tile $tileId",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(GameMagenta),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${tileId + 1}",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
            }
        }

        // Subtle gloss shine on top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.35f), Color.Transparent)
                    )
                )
        )

        // Optional Tile Number Hint Overlay
        if (showNumber) {
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.65f),
                border = androidx.compose.foundation.BorderStroke(1.dp, GameGold),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(4.dp)
                    .size(22.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "${tileId + 1}",
                        color = GameGoldLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}
