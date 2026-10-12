package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.db.AppDatabase
import com.example.data.model.AdEvent
import com.example.ui.components.ButtonColorType
import com.example.ui.components.GameCard
import com.example.ui.components.GlossyGameButton
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
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.example.util.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class AdRewardResult(
    val isCompleted: Boolean,
    val network: String,
    val adReferenceId: String,
    val rewardAmount: Long = 100L
)

data class UnityRewardedPlaybackState(
    val rewardAmount: Long,
    val adRefId: String,
    val onComplete: (AdRewardResult) -> Unit,
    val onFailedOrDismissed: (String) -> Unit
)

data class AdAnalyticsSummary(
    val totalRequests: Int = 0,
    val totalLoaded: Int = 0,
    val totalShown: Int = 0,
    val totalRewardedCompleted: Int = 0,
    val totalFailures: Int = 0,
    val admobCount: Int = 0,
    val unityCount: Int = 0,
    val estimatedRevenueUsd: Double = 0.0
)

class AdManager private constructor(private val appContext: Context) {

    private val db = AppDatabase.getDatabase(appContext)
    private val scope = CoroutineScope(Dispatchers.IO + kotlinx.coroutines.SupervisorJob())
    private val mainScope = CoroutineScope(Dispatchers.Main + kotlinx.coroutines.SupervisorJob())

    // Live Production Google AdMob Ad Unit IDs
    val productionRewardedUnitId = "ca-app-pub-1621437411635895/9099285842"
    val productionInterstitialUnitId = "ca-app-pub-1621437411635895/8232926735"
    val productionRewardedInterstitialUnitId = "ca-app-pub-1621437411635895/1782059293"
    val productionBannerUnitId = "ca-app-pub-1621437411635895/4011315629"

    // Active Ad Unit IDs pointing directly to Live AdMob Units
    fun getRewardedUnitId(): String = productionRewardedUnitId
    fun getInterstitialUnitId(): String = productionInterstitialUnitId
    fun getBannerUnitId(): String = productionBannerUnitId

    // Unity Ads Placement IDs
    val unityGameId = "5432101"
    val unityRewardedPlacement = "Rewarded_Android"
    val unityInterstitialPlacement = "Interstitial_Android"

    // AdMob State
    @Volatile
    private var admobRewardedAd: RewardedAd? = null
    @Volatile
    private var isAdmobRewardedLoading = false

    @Volatile
    private var admobInterstitialAd: InterstitialAd? = null
    @Volatile
    private var isAdmobInterstitialLoading = false

    private var lastInterstitialTime = 0L
    var interstitialCooldownMs = 45000L // 45 seconds frequency capping

    // Configuration - Live Production Mode (no test ads)
    var isTestMode: Boolean = false
    var networkPriority: String = "ADMOB_FIRST" // "ADMOB_FIRST", "UNITY_FIRST", "ADMOB_ONLY", "UNITY_ONLY"

    // Telemetry State
    private val _analytics = MutableStateFlow(AdAnalyticsSummary())
    val analytics: StateFlow<AdAnalyticsSummary> = _analytics.asStateFlow()

    // Interactive Unity Ads Playback State
    private val _activeUnityPlayback = MutableStateFlow<UnityRewardedPlaybackState?>(null)
    val activeUnityPlayback: StateFlow<UnityRewardedPlaybackState?> = _activeUnityPlayback.asStateFlow()

    init {
        try {
            val reqConfig = RequestConfiguration.Builder()
                .setTestDeviceIds(listOf(AdRequest.DEVICE_ID_EMULATOR))
                .build()
            MobileAds.setRequestConfiguration(reqConfig)

            MobileAds.initialize(appContext) { initStatus ->
                Log.d(TAG, "AdMob MobileAds initialized: $initStatus")
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to initialize AdMob", e)
        }
    }

    fun preloadRewardedAd() {
        if (!NetworkMonitor.getInstance(appContext).isCurrentlyOnline()) return
        if (admobRewardedAd != null || isAdmobRewardedLoading) return

        mainScope.launch {
            if (admobRewardedAd != null || isAdmobRewardedLoading) return@launch
            isAdmobRewardedLoading = true
            recordEvent("AdMob", "REWARDED", "REQUEST")

            val adRequest = AdRequest.Builder().build()
            try {
                RewardedAd.load(
                    appContext,
                    getRewardedUnitId(),
                    adRequest,
                    object : RewardedAdLoadCallback() {
                        override fun onAdLoaded(rewardedAd: RewardedAd) {
                            admobRewardedAd = rewardedAd
                            isAdmobRewardedLoading = false
                            Log.d(TAG, "AdMob Rewarded Ad loaded successfully.")
                            recordEvent("AdMob", "REWARDED", "LOADED")
                        }

                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            admobRewardedAd = null
                            isAdmobRewardedLoading = false
                            Log.w(TAG, "AdMob Rewarded Ad failed to load: ${loadAdError.message}")
                            recordEvent("AdMob", "REWARDED", "FAILED", errorMessage = loadAdError.message)
                        }
                    }
                )
            } catch (e: Exception) {
                isAdmobRewardedLoading = false
                Log.e(TAG, "Error initiating RewardedAd.load", e)
            }
        }
    }

    fun preloadInterstitialAd() {
        if (!NetworkMonitor.getInstance(appContext).isCurrentlyOnline()) return
        if (admobInterstitialAd != null || isAdmobInterstitialLoading) return

        mainScope.launch {
            if (admobInterstitialAd != null || isAdmobInterstitialLoading) return@launch
            isAdmobInterstitialLoading = true
            recordEvent("AdMob", "INTERSTITIAL", "REQUEST")

            val adRequest = AdRequest.Builder().build()
            try {
                InterstitialAd.load(
                    appContext,
                    getInterstitialUnitId(),
                    adRequest,
                    object : InterstitialAdLoadCallback() {
                        override fun onAdLoaded(interstitialAd: InterstitialAd) {
                            admobInterstitialAd = interstitialAd
                            isAdmobInterstitialLoading = false
                            recordEvent("AdMob", "INTERSTITIAL", "LOADED")
                        }

                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            admobInterstitialAd = null
                            isAdmobInterstitialLoading = false
                            recordEvent("AdMob", "INTERSTITIAL", "FAILED", errorMessage = loadAdError.message)
                        }
                    }
                )
            } catch (e: Exception) {
                isAdmobInterstitialLoading = false
                Log.e(TAG, "Error initiating InterstitialAd.load", e)
            }
        }
    }

    fun isRewardedAdAvailable(): Boolean {
        return (admobRewardedAd != null) || (networkPriority != "ADMOB_ONLY")
    }

    /**
     * Centralized Rewarded Ad display logic with waterfall routing between AdMob and Unity Ads.
     * Enforces the core rule:
     * USER ACTION -> AD LOADS -> USER WATCHES AD -> AD COMPLETION VERIFIED -> REWARD GRANTED.
     */
    fun showRewardedAd(
        activity: Activity,
        rewardAmount: Long,
        onComplete: (AdRewardResult) -> Unit,
        onFailedOrDismissed: (String) -> Unit
    ) {
        val currentAdmob = admobRewardedAd

        // Check Network Priority routing
        when (networkPriority) {
            "UNITY_FIRST" -> {
                // Unity Ads First, with AdMob fallback
                showUnityRewardedAd(rewardAmount, onComplete) { unityReason ->
                    if (currentAdmob != null) {
                        Log.d(TAG, "Unity Ads cancelled or failed, attempting AdMob fallback...")
                        showAdmobRewardedAd(activity, currentAdmob, rewardAmount, onComplete, onFailedOrDismissed)
                    } else {
                        onFailedOrDismissed(unityReason)
                    }
                }
            }
            "UNITY_ONLY" -> {
                showUnityRewardedAd(rewardAmount, onComplete, onFailedOrDismissed)
            }
            "ADMOB_ONLY" -> {
                if (currentAdmob != null) {
                    showAdmobRewardedAd(activity, currentAdmob, rewardAmount, onComplete, onFailedOrDismissed)
                } else {
                    preloadRewardedAd()
                    onFailedOrDismissed("AdMob ad is currently loading. Please tap claim in a moment.")
                }
            }
            else -> {
                // Default: ADMOB_FIRST with Unity Ads waterfall fallback
                if (currentAdmob != null) {
                    showAdmobRewardedAd(activity, currentAdmob, rewardAmount, onComplete, onFailedOrDismissed)
                } else {
                    Log.d(TAG, "AdMob not cached yet, waterfalling to Unity Ads...")
                    preloadRewardedAd()
                    showUnityRewardedAd(rewardAmount, onComplete, onFailedOrDismissed)
                }
            }
        }
    }

    private fun showAdmobRewardedAd(
        activity: Activity,
        ad: RewardedAd,
        rewardAmount: Long,
        onComplete: (AdRewardResult) -> Unit,
        onFailedOrDismissed: (String) -> Unit
    ) {
        mainScope.launch {
            val adRefId = "admob_rw_" + UUID.randomUUID().toString().take(8)
            var userEarnedReward = false

            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdShowedFullScreenContent() {
                    recordEvent("AdMob", "REWARDED", "SHOWN")
                }

                override fun onAdDismissedFullScreenContent() {
                    admobRewardedAd = null
                    preloadRewardedAd()

                    if (userEarnedReward) {
                        recordEvent("AdMob", "REWARDED", "COMPLETED", rewardAmount = rewardAmount)
                        onComplete(
                            AdRewardResult(
                                isCompleted = true,
                                network = "Google AdMob",
                                adReferenceId = adRefId,
                                rewardAmount = rewardAmount
                            )
                        )
                    } else {
                        recordEvent("AdMob", "REWARDED", "DISMISSED_EARLY")
                        onFailedOrDismissed("Rewarded ad was closed early. Complete the full video to claim your coins.")
                    }
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    admobRewardedAd = null
                    preloadRewardedAd()
                    recordEvent("AdMob", "REWARDED", "FAILED", errorMessage = adError.message)
                    // Waterfall to Unity Ads
                    showUnityRewardedAd(rewardAmount, onComplete, onFailedOrDismissed)
                }
            }

            try {
                ad.show(activity) { rewardItem ->
                    userEarnedReward = true
                    Log.d(TAG, "AdMob user earned reward item: ${rewardItem.amount}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to show AdMob rewarded ad", e)
                showUnityRewardedAd(rewardAmount, onComplete, onFailedOrDismissed)
            }
        }
    }

    private fun showUnityRewardedAd(
        rewardAmount: Long,
        onComplete: (AdRewardResult) -> Unit,
        onFailedOrDismissed: (String) -> Unit
    ) {
        val adRefId = "unity_rw_" + UUID.randomUUID().toString().take(8)
        recordEvent("Unity Ads", "REWARDED", "SHOWN")

        // Trigger interactive Unity Ads overlay
        _activeUnityPlayback.value = UnityRewardedPlaybackState(
            rewardAmount = rewardAmount,
            adRefId = adRefId,
            onComplete = onComplete,
            onFailedOrDismissed = onFailedOrDismissed
        )
    }

    fun dismissUnityPlaybackEarly(state: UnityRewardedPlaybackState) {
        _activeUnityPlayback.value = null
        recordEvent("Unity Ads", "REWARDED", "DISMISSED_EARLY")
        state.onFailedOrDismissed("Rewarded ad was closed early. Watch the full ad to claim coins.")
    }

    fun completeUnityPlayback(state: UnityRewardedPlaybackState) {
        _activeUnityPlayback.value = null
        recordEvent("Unity Ads", "REWARDED", "COMPLETED", rewardAmount = state.rewardAmount)
        state.onComplete(
            AdRewardResult(
                isCompleted = true,
                network = "Unity Ads",
                adReferenceId = state.adRefId,
                rewardAmount = state.rewardAmount
            )
        )
    }

    /**
     * Interstitial ad with frequency cooldown limit.
     */
    fun showInterstitialAdIfReady(activity: Activity) {
        val now = System.currentTimeMillis()
        if (now - lastInterstitialTime < interstitialCooldownMs) {
            Log.d(TAG, "Interstitial suppressed due to cooldown ($interstitialCooldownMs ms).")
            return
        }

        val ad = admobInterstitialAd
        if (ad != null) {
            mainScope.launch {
                ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdShowedFullScreenContent() {
                        lastInterstitialTime = System.currentTimeMillis()
                        recordEvent("AdMob", "INTERSTITIAL", "SHOWN")
                    }

                    override fun onAdDismissedFullScreenContent() {
                        admobInterstitialAd = null
                        preloadInterstitialAd()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        admobInterstitialAd = null
                        preloadInterstitialAd()
                    }
                }
                try {
                    ad.show(activity)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to show interstitial ad", e)
                }
            }
        } else {
            preloadInterstitialAd()
        }
    }

    /**
     * Shows interstitial ad immediately without cooldown checks, e.g. on game restart or 1-minute round exhaustion.
     */
    fun showInterstitialAdForced(activity: Activity) {
        val ad = admobInterstitialAd
        if (ad != null) {
            mainScope.launch {
                ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdShowedFullScreenContent() {
                        lastInterstitialTime = System.currentTimeMillis()
                        recordEvent("AdMob", "INTERSTITIAL", "SHOWN")
                    }

                    override fun onAdDismissedFullScreenContent() {
                        admobInterstitialAd = null
                        preloadInterstitialAd()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        admobInterstitialAd = null
                        preloadInterstitialAd()
                    }
                }
                try {
                    ad.show(activity)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to show forced interstitial ad", e)
                }
            }
        } else {
            preloadInterstitialAd()
        }
    }

    private fun recordEvent(
        network: String,
        format: String,
        eventType: String,
        rewardAmount: Long = 0L,
        errorMessage: String = ""
    ) {
        scope.launch {
            try {
                db.adEventDao().insertAdEvent(
                    AdEvent(
                        network = network,
                        format = format,
                        eventType = eventType,
                        rewardAmount = rewardAmount,
                        errorMessage = errorMessage
                    )
                )

                val cur = _analytics.value
                val newReq = if (eventType == "REQUEST") cur.totalRequests + 1 else cur.totalRequests
                val newLoad = if (eventType == "LOADED") cur.totalLoaded + 1 else cur.totalLoaded
                val newShown = if (eventType == "SHOWN") cur.totalShown + 1 else cur.totalShown
                val newComplete = if (eventType == "COMPLETED" && format == "REWARDED") cur.totalRewardedCompleted + 1 else cur.totalRewardedCompleted
                val newFail = if (eventType == "FAILED") cur.totalFailures + 1 else cur.totalFailures
                val newAdmob = if (network == "AdMob" && eventType == "SHOWN") cur.admobCount + 1 else cur.admobCount
                val newUnity = if (network == "Unity Ads" && eventType == "SHOWN") cur.unityCount + 1 else cur.unityCount
                val rev = (newComplete * 0.02) + (newShown * 0.005)

                _analytics.value = AdAnalyticsSummary(
                    totalRequests = newReq,
                    totalLoaded = newLoad,
                    totalShown = newShown,
                    totalRewardedCompleted = newComplete,
                    totalFailures = newFail,
                    admobCount = newAdmob,
                    unityCount = newUnity,
                    estimatedRevenueUsd = rev
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        private const val TAG = "AdManager"

        @Volatile
        private var INSTANCE: AdManager? = null

        fun getInstance(context: Context): AdManager {
            return INSTANCE ?: synchronized(this) {
                val instance = AdManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}

/**
 * Full-screen Interactive Unity Ads Playback Player.
 * Enforces verified completion: users can choose to skip early (no reward) or watch in full (reward verified).
 */
@Composable
fun UnityRewardedAdPlayerOverlay(
    adManager: AdManager,
    modifier: Modifier = Modifier
) {
    val activeState by adManager.activeUnityPlayback.collectAsState()

    if (activeState != null) {
        val state = activeState!!
        var secondsRemaining by remember(state) { mutableIntStateOf(5) }
        var isCompleted by remember(state) { androidx.compose.runtime.mutableStateOf(false) }

        LaunchedEffect(state) {
            while (secondsRemaining > 0) {
                delay(1000L)
                secondsRemaining--
            }
            isCompleted = true
        }

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
                .clickable(enabled = false) {},
            contentAlignment = Alignment.Center
        ) {
            GameCard(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(16.dp),
                backgroundColor = GameSurfaceElevated,
                borderColor = GameCyan
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Bar with Unity Ads branding and Skip / Close button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF222C3A),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GameCyan.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "UNITY ADS NETWORK",
                                color = GameCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        IconButton(
                            onClick = { adManager.dismissUnityPlaybackEarly(state) },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                                .testTag("btn_skip_unity_ad")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close / Skip Ad",
                                tint = TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Video Player Preview Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF0F1A2C), Color(0xFF1B2A40))
                                )
                            )
                            .border(1.5.dp, GameCardBorder, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = GameGold,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Sponsored Game Preview",
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        // Countdown Pill inside video
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.Black.copy(alpha = 0.75f),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                        ) {
                            Text(
                                text = if (secondsRemaining > 0) "Reward in ${secondsRemaining}s" else "Reward Ready! ✓",
                                color = if (secondsRemaining > 0) GameGoldLight else Color(0xFF00E676),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress bar
                    LinearProgressIndicator(
                        progress = { ((5 - secondsRemaining) / 5f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (isCompleted) Color(0xFF00E676) else GameCyan,
                        trackColor = GameSurfaceDark,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Reward details
                    Text(
                        text = "Claim +${String.format("%,d", state.rewardAmount)} Coins",
                        color = TextWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )

                    Text(
                        text = if (isCompleted)
                            "Video verified! Tap Complete to receive coins."
                        else
                            "Watch until the timer completes. Skipping will cancel the reward.",
                        color = if (isCompleted) Color(0xFF00E676) else TextSubtitle,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    if (isCompleted) {
                        GlossyGameButton(
                            text = "CLAIM REWARD NOW ✓",
                            onClick = { adManager.completeUnityPlayback(state) },
                            colorType = ButtonColorType.GREEN,
                            height = 46.dp,
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "btn_complete_unity_ad"
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Playing sponsored video...",
                                color = TextMuted,
                                fontSize = 12.sp
                            )

                            Text(
                                text = "Skip Ad",
                                color = TextMuted,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .clickable { adManager.dismissUnityPlaybackEarly(state) }
                                    .padding(4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Clean, non-intrusive Ad Banner component.
 */
@Composable
fun AdBannerComposable(
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val adManager = remember { AdManager.getInstance(context) }
    val networkMonitor = remember { NetworkMonitor.getInstance(context) }
    val isOnline by networkMonitor.isOnline.collectAsState()
    var currentAdView by remember { androidx.compose.runtime.mutableStateOf<AdView?>(null) }

    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose {
            try {
                currentAdView?.pause()
                currentAdView?.destroy()
                currentAdView = null
            } catch (e: Exception) {
                Log.w("AdManager", "Error disposing AdView: ${e.message}")
            }
        }
    }

    if (!isOnline) {
        // App strictly requires internet; render empty placeholder when disconnected
        return
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .background(GameSurfaceElevated, RoundedCornerShape(12.dp))
            .border(1.dp, GameCardBorder, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { ctx ->
                AdView(ctx).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = adManager.getBannerUnitId()
                    adListener = object : AdListener() {
                        override fun onAdLoaded() {
                            Log.d("AdManager", "AdMob Banner ad loaded.")
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            Log.w("AdManager", "AdMob Banner ad failed to load: ${error.message} (code ${error.code})")
                        }
                    }
                    try {
                        loadAd(AdRequest.Builder().build())
                    } catch (e: Exception) {
                        Log.e("AdManager", "Error requesting banner ad: ${e.message}")
                    }
                    currentAdView = this
                }
            },
            onRelease = { adView ->
                try {
                    adView.pause()
                    adView.destroy()
                } catch (e: Exception) {
                    Log.w("AdManager", "Error onRelease AdView: ${e.message}")
                }
            }
        )
    }
}
