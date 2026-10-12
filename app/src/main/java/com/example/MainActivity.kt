package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.auth.AdminAuthHelper
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OfflineScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.PuzzleGameScreen
import com.example.ui.screens.PuzzleLibraryScreen
import com.example.ui.screens.SignInScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.screens.WalletScreen
import com.example.util.NetworkMonitor
import com.example.ui.theme.GameBackgroundDark
import com.example.ui.theme.GameCardBorder
import com.example.ui.theme.GameCyan
import com.example.ui.theme.GameGold
import com.example.ui.theme.GameGoldLight
import com.example.ui.theme.GameSurfaceElevated
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtitle
import com.example.ui.viewmodel.AdminViewModel
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.GameViewModel
import com.example.ui.viewmodel.LibraryViewModel
import com.example.ui.viewmodel.TaskViewModel
import com.example.ui.viewmodel.WalletViewModel

sealed class Screen {
    data object SignIn : Screen()
    data object Home : Screen()
    data object Library : Screen()
    data class Game(val puzzleId: String) : Screen()
    data object Tasks : Screen()
    data object Wallet : Screen()
    data object Profile : Screen()
    data object Admin : Screen()
}

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val walletViewModel: WalletViewModel by viewModels()
    private val libraryViewModel: LibraryViewModel by viewModels()
    private val gameViewModel: GameViewModel by viewModels()
    private val taskViewModel: TaskViewModel by viewModels()
    private val adminViewModel: AdminViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            if (com.google.firebase.FirebaseApp.getApps(this).isEmpty()) {
                com.google.firebase.FirebaseApp.initializeApp(this)
            }
        } catch (e: Throwable) {
            android.util.Log.e("MainActivity", "FirebaseApp initialization handled", e)
        }
        com.example.data.db.AppDatabase.checkAndSeed(this)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val firebaseUser by authViewModel.currentUser.collectAsState()
                var currentScreen by remember { mutableStateOf<Screen>(if (firebaseUser != null) Screen.Home else Screen.SignIn) }

                val networkMonitor = remember { NetworkMonitor.getInstance(this@MainActivity) }
                val isOnline by networkMonitor.isOnline.collectAsState()

                if (!isOnline) {
                    // Strict offline blocker: App does not run offline
                    OfflineScreen(
                        onRetry = { networkMonitor.isCurrentlyOnline() }
                    )
                } else {
                    // Advance to Home when Google user authentication is resolved
                    androidx.compose.runtime.LaunchedEffect(firebaseUser) {
                        if (firebaseUser != null && currentScreen is Screen.SignIn) {
                            currentScreen = Screen.Home
                        }
                    }

                    val allPuzzles by libraryViewModel.rawPuzzles.collectAsState()
                    val allPuzzleIds = remember(allPuzzles) { allPuzzles.map { it.id } }

                    val showBottomNav = currentScreen !is Screen.Game && currentScreen !is Screen.Admin && currentScreen !is Screen.SignIn

                    Scaffold(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(GameBackgroundDark)
                            .statusBarsPadding(),
                        bottomBar = {
                            if (showBottomNav) {
                                GameBottomNavBar(
                                    currentScreen = currentScreen,
                                    onScreenSelected = { screen -> currentScreen = screen }
                                )
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = if (showBottomNav) 68.dp else 0.dp)
                        ) {
                            when (val screen = currentScreen) {
                                is Screen.SignIn -> {
                                    SignInScreen(
                                        authViewModel = authViewModel,
                                        onContinueAsGuest = { currentScreen = Screen.Home }
                                    )
                                }
                                is Screen.Home -> {
                                    HomeScreen(
                                        walletViewModel = walletViewModel,
                                        libraryViewModel = libraryViewModel,
                                        onNavigateToGame = { puzzleId -> currentScreen = Screen.Game(puzzleId) },
                                        onNavigateToLibrary = { category ->
                                            if (category != null) {
                                                libraryViewModel.setCategory(category)
                                            }
                                            currentScreen = Screen.Library
                                        },
                                        onNavigateToTasks = { currentScreen = Screen.Tasks },
                                        onNavigateToWallet = { currentScreen = Screen.Wallet },
                                        onNavigateToAdmin = { currentScreen = Screen.Admin }
                                    )
                                }
                                is Screen.Library -> {
                                    BackHandler { currentScreen = Screen.Home }
                                    PuzzleLibraryScreen(
                                        libraryViewModel = libraryViewModel,
                                        walletViewModel = walletViewModel,
                                        onNavigateToGame = { puzzleId -> currentScreen = Screen.Game(puzzleId) },
                                        onNavigateToWallet = { currentScreen = Screen.Wallet }
                                    )
                                }
                                is Screen.Game -> {
                                    PuzzleGameScreen(
                                        puzzleId = screen.puzzleId,
                                        gameViewModel = gameViewModel,
                                        walletViewModel = walletViewModel,
                                        onNavigateBack = { currentScreen = Screen.Library },
                                        onNavigateToLibrary = { currentScreen = Screen.Library },
                                        onNavigateToHome = { currentScreen = Screen.Home },
                                        onNextPuzzle = { nextId -> currentScreen = Screen.Game(nextId) },
                                        allPuzzleIds = allPuzzleIds
                                    )
                                }
                                is Screen.Tasks -> {
                                    BackHandler { currentScreen = Screen.Home }
                                    TasksScreen(
                                        taskViewModel = taskViewModel,
                                        walletViewModel = walletViewModel,
                                        onNavigateToWallet = { currentScreen = Screen.Wallet }
                                    )
                                }
                                is Screen.Wallet -> {
                                    BackHandler { currentScreen = Screen.Home }
                                    WalletScreen(walletViewModel = walletViewModel)
                                }
                                is Screen.Profile -> {
                                    BackHandler { currentScreen = Screen.Home }
                                    ProfileScreen(
                                        walletViewModel = walletViewModel,
                                        authViewModel = authViewModel,
                                        onNavigateToWallet = { currentScreen = Screen.Wallet },
                                        onNavigateToAdmin = { currentScreen = Screen.Admin },
                                        onNavigateToLibrary = { currentScreen = Screen.Library },
                                        onSignOut = { currentScreen = Screen.SignIn }
                                    )
                                }
                                is Screen.Admin -> {
                                    val currentUser by walletViewModel.user.collectAsState()
                                    val isUserAdmin = AdminAuthHelper.isUserAdmin(currentUser?.role, currentUser?.email)

                                    if (!isUserAdmin) {
                                        androidx.compose.runtime.LaunchedEffect(Unit) {
                                            currentScreen = Screen.Home
                                        }
                                    } else {
                                        AdminScreen(
                                            adminViewModel = adminViewModel,
                                            onNavigateBack = { currentScreen = Screen.Home }
                                        )
                                    }
                                }
                            }
                        }

                        // Global Unity Ads Interactive Player Overlay
                        com.example.ads.UnityRewardedAdPlayerOverlay(
                            adManager = com.example.ads.AdManager.getInstance(this@MainActivity)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GameBottomNavBar(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = GameSurfaceElevated,
        border = androidx.compose.foundation.BorderStroke(1.dp, GameCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GameNavItem(
                icon = Icons.Default.Home,
                label = "HOME",
                isSelected = currentScreen is Screen.Home,
                onClick = { onScreenSelected(Screen.Home) },
                testTag = "nav_home"
            )

            GameNavItem(
                icon = Icons.Default.Extension,
                label = "PLAY",
                isSelected = currentScreen is Screen.Library,
                onClick = { onScreenSelected(Screen.Library) },
                testTag = "nav_play"
            )

            GameNavItem(
                icon = Icons.Default.Assignment,
                label = "TASKS",
                isSelected = currentScreen is Screen.Tasks,
                onClick = { onScreenSelected(Screen.Tasks) },
                testTag = "nav_tasks"
            )

            GameNavItem(
                icon = Icons.Default.AccountBalanceWallet,
                label = "WALLET",
                isSelected = currentScreen is Screen.Wallet,
                onClick = { onScreenSelected(Screen.Wallet) },
                testTag = "nav_wallet"
            )

            GameNavItem(
                icon = Icons.Default.Person,
                label = "PROFILE",
                isSelected = currentScreen is Screen.Profile,
                onClick = { onScreenSelected(Screen.Profile) },
                testTag = "nav_profile"
            )
        }
    }
}

@Composable
fun GameNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag(testTag)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(if (isSelected) GameGold.copy(alpha = 0.2f) else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) GameGoldLight else TextMuted,
                modifier = Modifier.size(22.dp)
            )
        }

        Text(
            text = label,
            color = if (isSelected) GameGoldLight else TextMuted,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
        )
    }
}
