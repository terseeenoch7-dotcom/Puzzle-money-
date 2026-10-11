package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ads.AdBannerComposable
import com.example.data.model.PuzzleImage
import com.example.ui.components.ButtonColorType
import com.example.ui.components.CoinBalanceBadge
import com.example.ui.components.GameCard
import com.example.ui.components.GlossyGameButton
import com.example.ui.components.VoiceSearchBar
import com.example.ui.theme.GameBackgroundDark
import com.example.ui.theme.GameCardBorder
import com.example.ui.theme.GameCyan
import com.example.ui.theme.GameGold
import com.example.ui.theme.GameGoldLight
import com.example.ui.theme.GameMagenta
import com.example.ui.theme.GameSurfaceDark
import com.example.ui.theme.GameSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtitle
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.LibraryViewModel
import com.example.ui.viewmodel.WalletViewModel

@Composable
fun PuzzleLibraryScreen(
    libraryViewModel: LibraryViewModel,
    walletViewModel: WalletViewModel,
    onNavigateToGame: (String) -> Unit,
    onNavigateToWallet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val user by walletViewModel.user.collectAsState()
    val filteredPuzzles by libraryViewModel.filteredPuzzles.collectAsState()
    val filters by libraryViewModel.filters.collectAsState()
    val isRecordingVoice by libraryViewModel.isRecordingVoice.collectAsState()
    val isTranscribing by libraryViewModel.isTranscribing.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        libraryViewModel.toggleVoiceSearch(isGranted)
    }

    val tabs = listOf("All", "New", "Popular", "Featured", "Favorites")
    val categories = listOf("All", "Animals", "Cars", "Space", "Fantasy", "Nature", "Cartoon")
    val difficulties = listOf("All", "3x3", "4x4", "5x5")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GameBackgroundDark)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header with Title & Coins
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Puzzle Library",
                    color = TextWhite,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "${filteredPuzzles.size} picture puzzles available",
                    color = TextSubtitle,
                    fontSize = 13.sp
                )
            }

            CoinBalanceBadge(
                coins = user?.coinBalance ?: 0L,
                onClick = onNavigateToWallet
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Voice Search Bar with gemini-3.5-transcribe support
        VoiceSearchBar(
            query = filters.searchQuery,
            onQueryChange = { libraryViewModel.setSearchQuery(it) },
            onVoiceClick = {
                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
                if (hasPermission) {
                    libraryViewModel.toggleVoiceSearch(true)
                } else {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            },
            isRecording = isRecordingVoice,
            isTranscribing = isTranscribing,
            placeholder = "Search picture titles or tap mic..."
        )

        Spacer(modifier = Modifier.height(10.dp))

        // TOP AD BANNER
        AdBannerComposable()

        Spacer(modifier = Modifier.height(12.dp))

        // Main Tabs: All, New, Popular, Featured, Favorites
        ScrollableTabRow(
            selectedTabIndex = tabs.indexOf(filters.selectedTab).coerceAtLeast(0),
            containerColor = Color.Transparent,
            contentColor = GameGold,
            edgePadding = 0.dp,
            indicator = { tabPositions ->
                val tabIdx = tabs.indexOf(filters.selectedTab).coerceAtLeast(0)
                if (tabIdx < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[tabIdx]),
                        color = GameGold
                    )
                }
            },
            divider = {}
        ) {
            tabs.forEach { tabName ->
                val selected = filters.selectedTab == tabName
                Tab(
                    selected = selected,
                    onClick = { libraryViewModel.setTab(tabName) },
                    text = {
                        Text(
                            text = tabName,
                            color = if (selected) GameGoldLight else TextSubtitle,
                            fontWeight = if (selected) FontWeight.Black else FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Difficulty Chips: All, 3x3 (Easy), 4x4 (Medium), 5x5 (Hard)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            difficulties.forEach { diff ->
                val selected = filters.selectedDifficulty == diff
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (selected) GameCyan else GameSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) GameCyan else GameCardBorder),
                    modifier = Modifier
                        .clickable { libraryViewModel.setDifficulty(diff) }
                        .testTag("filter_diff_$diff")
                ) {
                    Text(
                        text = if (diff == "All") "All Sizes" else diff,
                        color = if (selected) Color.Black else TextWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Category Filter Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                val selected = filters.selectedCategory == category
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (selected) GameMagenta else GameSurfaceDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) GameMagenta else GameCardBorder),
                    modifier = Modifier
                        .clickable { libraryViewModel.setCategory(category) }
                        .testTag("filter_cat_$category")
                ) {
                    Text(
                        text = category,
                        color = if (selected) Color.White else TextSubtitle,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // MIDDLE AD BANNER
        AdBannerComposable()

        Spacer(modifier = Modifier.height(12.dp))

        // Grid of Puzzles
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 96.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (filteredPuzzles.isEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Extension,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No puzzles found",
                                color = TextWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Try adjusting filters or voice searching another term",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } else {
                items(filteredPuzzles, key = { it.id }) { puzzle ->
                    LibraryPuzzleCard(
                        puzzle = puzzle,
                        onPlay = { onNavigateToGame(puzzle.id) },
                        onFavorite = { libraryViewModel.toggleFavorite(puzzle) }
                    )
                }

                // BOTTOM AD BANNER
                item(span = { GridItemSpan(2) }, key = "library_bottom_banner") {
                    Spacer(modifier = Modifier.height(8.dp))
                    AdBannerComposable()
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun LibraryPuzzleCard(
    puzzle: PuzzleImage,
    onPlay: () -> Unit,
    onFavorite: () -> Unit
) {
    val context = LocalContext.current
    val resId = context.resources.getIdentifier(puzzle.imageUrl, "drawable", context.packageName)

    GameCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("library_puzzle_${puzzle.id}"),
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
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                // Favorite button
                IconButton(
                    onClick = onFavorite,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.55f))
                ) {
                    Icon(
                        imageVector = if (puzzle.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (puzzle.isFavorite) GameMagenta else Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Grid size badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.75f),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                ) {
                    Text(
                        text = "${puzzle.gridSize}x${puzzle.gridSize} • ${puzzle.difficulty}",
                        color = GameGoldLight,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = puzzle.title,
                    color = TextWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = puzzle.category,
                        color = TextSubtitle,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "+100 🪙",
                        color = GameGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                GlossyGameButton(
                    text = "PLAY",
                    onClick = onPlay,
                    colorType = ButtonColorType.GOLD,
                    height = 36.dp,
                    testTag = "lib_play_btn_${puzzle.id}"
                )
            }
        }
    }
}
