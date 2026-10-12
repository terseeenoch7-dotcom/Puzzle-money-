package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.AdBannerComposable
import com.example.data.auth.AdminAuthHelper
import com.example.data.model.PuzzleImage
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
import com.example.ui.theme.GameGreenDark
import com.example.ui.theme.GameMagenta
import com.example.ui.theme.GameSurfaceDark
import com.example.ui.theme.GameSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtitle
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.LibraryViewModel
import com.example.ui.viewmodel.WalletViewModel

@Composable
fun HomeScreen(
    walletViewModel: WalletViewModel,
    libraryViewModel: LibraryViewModel,
    onNavigateToGame: (String) -> Unit,
    onNavigateToLibrary: (String?) -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToWallet: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val user by walletViewModel.user.collectAsState()
    val allPuzzles by libraryViewModel.rawPuzzles.collectAsState()
    val featuredPuzzles = allPuzzles.filter { it.isFeatured }
    val heroPuzzle = featuredPuzzles.firstOrNull() ?: allPuzzles.firstOrNull()

    val categories = listOf("Animals", "Cars", "Space", "Fantasy", "Nature", "Cartoon")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(GameBackgroundDark)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // Top Bar: Player profile & Coin balance
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(GameMagenta, GameGold)))
                            .border(2.dp, GameGoldLight, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SportsEsports,
                            contentDescription = "Avatar",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "Hello, ${user?.username ?: "Player"}!",
                            color = TextWhite,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Solve puzzles & earn cash",
                            color = TextSubtitle,
                            fontSize = 12.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    CoinBalanceBadge(
                        coins = user?.coinBalance ?: 0L,
                        onClick = onNavigateToWallet
                    )

                    // Admin Panel button visible ONLY to crowned administrator accounts
                    if (AdminAuthHelper.isUserAdmin(user?.role, user?.email)) {
                        Spacer(modifier = Modifier.width(6.dp))

                        IconButton(
                            onClick = onNavigateToAdmin,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(GameSurfaceElevated)
                                .testTag("home_admin_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "Admin Panel",
                                tint = GameCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // TOP AD BANNER
        item(key = "home_top_banner") {
            AdBannerComposable()
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Featured Hero Puzzle Card
        if (heroPuzzle != null) {
            item {
                FeaturedHeroCard(
                    puzzle = heroPuzzle,
                    onPlay = { onNavigateToGame(heroPuzzle.id) }
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Daily Task & Reward Teaser Banner
        item {
            GameCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = GameSurfaceElevated,
                borderColor = GameGreen,
                onClick = onNavigateToTasks
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(GameGreen.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TaskAlt,
                            contentDescription = null,
                            tint = GameGreen,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Earn Up To 10,000 Coins!",
                            color = TextWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Complete quick daily tasks and community goals",
                            color = TextSubtitle,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = GameGreenDark,
                        modifier = Modifier.clickable { onNavigateToTasks() }
                    ) {
                        Text(
                            text = "GO",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Categories section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Puzzle Categories",
                    color = TextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = "See All",
                    color = GameCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToLibrary(null) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(categories) { category ->
                    CategoryPill(
                        title = category,
                        onClick = { onNavigateToLibrary(category) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))
        }

        // Popular Puzzles
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Popular Puzzles",
                    color = TextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "View Library",
                    color = GameGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToLibrary(null) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(allPuzzles.take(6)) { puzzle ->
                    PuzzleThumbnailCard(
                        puzzle = puzzle,
                        onPlay = { onNavigateToGame(puzzle.id) },
                        onFavorite = { libraryViewModel.toggleFavorite(puzzle) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))
        }

        // New Puzzles
        item {
            Text(
                text = "New Pictures",
                color = TextWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(allPuzzles.filter { it.isNew }) { puzzle ->
                    PuzzleThumbnailCard(
                        puzzle = puzzle,
                        onPlay = { onNavigateToGame(puzzle.id) },
                        onFavorite = { libraryViewModel.toggleFavorite(puzzle) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun FeaturedHeroCard(
    puzzle: PuzzleImage,
    onPlay: () -> Unit
) {
    val context = LocalContext.current
    val resId = context.resources.getIdentifier(puzzle.imageUrl, "drawable", context.packageName)

    GameCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = GameSurfaceElevated,
        borderColor = GameGold
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                if (resId != 0) {
                    Image(
                        painter = painterResource(id = resId),
                        contentDescription = puzzle.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.radialGradient(listOf(GameMagenta, GameBackgroundDark))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Extension,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(60.dp)
                        )
                    }
                }

                // Featured badge
                Surface(
                    shape = RoundedCornerShape(bottomEnd = 12.dp),
                    color = GameGoldDark,
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "FEATURED PUZZLE",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // Grid size tag
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.6f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "${puzzle.gridSize}x${puzzle.gridSize} (${puzzle.difficulty})",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = puzzle.title,
                            color = TextWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Category: ${puzzle.category} • Reward: +100 Coins",
                            color = GameGoldLight,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                GlossyGameButton(
                    text = "PLAY SLIDING PUZZLE",
                    onClick = onPlay,
                    colorType = ButtonColorType.GOLD,
                    icon = Icons.Default.PlayArrow,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "hero_play_button"
                )
            }
        }
    }
}

@Composable
fun CategoryPill(
    title: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = GameSurfaceElevated,
        border = androidx.compose.foundation.BorderStroke(1.dp, GameCardBorder),
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag("category_pill_$title")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Extension,
                contentDescription = null,
                tint = GameCyan,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                color = TextWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PuzzleThumbnailCard(
    puzzle: PuzzleImage,
    onPlay: () -> Unit,
    onFavorite: () -> Unit
) {
    val context = LocalContext.current
    val resId = context.resources.getIdentifier(puzzle.imageUrl, "drawable", context.packageName)

    GameCard(
        modifier = Modifier
            .width(160.dp)
            .testTag("puzzle_card_${puzzle.id}"),
        backgroundColor = GameSurfaceElevated,
        borderColor = GameCardBorder,
        onClick = onPlay
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                if (resId != 0) {
                    Image(
                        painter = painterResource(id = resId),
                        contentDescription = puzzle.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(GameSurfaceDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Extension,
                            contentDescription = null,
                            tint = GameCyan,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                // Favorite icon button
                IconButton(
                    onClick = onFavorite,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = if (puzzle.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (puzzle.isFavorite) GameMagenta else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Grid badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.7f),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                ) {
                    Text(
                        text = "${puzzle.gridSize}x${puzzle.gridSize}",
                        color = GameGoldLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = puzzle.title,
                    color = TextWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )

                Text(
                    text = puzzle.category,
                    color = TextSubtitle,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                GlossyGameButton(
                    text = "PLAY",
                    onClick = onPlay,
                    colorType = ButtonColorType.GOLD,
                    height = 36.dp,
                    testTag = "play_button_${puzzle.id}"
                )
            }
        }
    }
}
