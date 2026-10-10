package com.example.ui.viewmodel

import android.app.Activity
import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ads.AdManager
import com.example.data.ai.GeminiService
import com.example.data.db.AppDatabase
import com.example.data.model.PuzzleImage
import com.example.data.model.PuzzleSession
import com.example.data.repository.PuzzleRepository
import com.example.data.reward.RewardManager
import com.example.game.ImageTileSlicer
import com.example.game.PuzzleState
import com.example.game.SlidingPuzzleEngine
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GameUiState(
    val puzzle: PuzzleImage? = null,
    val puzzleState: PuzzleState? = null,
    val tileBitmaps: Map<Int, Bitmap> = emptyMap(),
    val fullBitmap: Bitmap? = null,
    val isLoading: Boolean = true,
    val isHintVisible: Boolean = false,
    val showTileNumbers: Boolean = false,
    val elapsedSeconds: Long = 0L,
    val aiHintText: String? = null,
    val isRequestingAiHint: Boolean = false,
    val isSolvedWaitingClaim: Boolean = false,
    val isCompleteCelebration: Boolean = false,
    val isWatchingAd: Boolean = false,
    val rewardCoinsEarned: Long = 0L,
    val pendingRewardId: String? = null,
    val errorMessage: String? = null
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = PuzzleRepository(db)
    private val adManager = AdManager.getInstance(application)
    private val rewardManager = RewardManager(application)

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var activeSession: PuzzleSession? = null

    private val currentUserId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: "user_default_1"

    fun loadPuzzle(puzzleId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val puzzle = repository.getPuzzleById(puzzleId)
            if (puzzle == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Puzzle not found."
                )
                return@launch
            }

            // Preload rewarded ad in background as user enters puzzle
            adManager.preloadRewardedAd()

            // Slice image
            val sliced = ImageTileSlicer.loadAndSliceImage(
                context = getApplication(),
                imageIdentifier = puzzle.imageUrl,
                gridSize = puzzle.gridSize
            )
            val fullImage = ImageTileSlicer.getFullBitmap(
                context = getApplication(),
                imageIdentifier = puzzle.imageUrl
            )

            // Create solvable shuffled state
            val initialState = SlidingPuzzleEngine.createShuffledPuzzle(puzzle.gridSize)

            // Create server-side session for anti-cheat validation
            val session = repository.startPuzzleSession(
                userId = currentUserId,
                puzzleId = puzzle.id,
                gridSize = puzzle.gridSize,
                initialTiles = initialState.tiles
            )
            activeSession = session

            _uiState.value = GameUiState(
                puzzle = puzzle,
                puzzleState = initialState,
                tileBitmaps = sliced,
                fullBitmap = fullImage,
                isLoading = false,
                elapsedSeconds = 0L
            )

            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val current = _uiState.value
                if (current.puzzleState?.isSolved != true) {
                    _uiState.value = current.copy(elapsedSeconds = current.elapsedSeconds + 1)
                } else {
                    break
                }
            }
        }
    }

    fun onTileClicked(index: Int) {
        val currentState = _uiState.value.puzzleState ?: return
        val newState = SlidingPuzzleEngine.moveTile(currentState, index) ?: return

        _uiState.value = _uiState.value.copy(puzzleState = newState)

        if (newState.isSolved) {
            timerJob?.cancel()
            handlePuzzleSolved(newState)
        }
    }

    private fun handlePuzzleSolved(solvedState: PuzzleState) {
        val session = activeSession ?: return
        val puzzle = _uiState.value.puzzle ?: return

        viewModelScope.launch {
            // Create pending reward on the backend ledger
            val pendingRes = rewardManager.createPendingPuzzleReward(
                userId = currentUserId,
                sessionId = session.id,
                puzzleId = puzzle.id,
                rewardCoins = 100L
            )

            val pendingId = pendingRes.getOrNull()
            _uiState.value = _uiState.value.copy(
                isSolvedWaitingClaim = true,
                pendingRewardId = pendingId
            )
        }
    }

    /**
     * User taps "WATCH AD & CLAIM 100 COINS":
     * Plays preloaded rewarded ad -> validates completion callback -> updates wallet.
     */
    fun claimRewardWithAd(activity: Activity) {
        val pendingId = _uiState.value.pendingRewardId
        val moves = _uiState.value.puzzleState?.moveCount ?: 0
        val duration = _uiState.value.elapsedSeconds

        if (pendingId == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "Reward session expired.")
            return
        }

        _uiState.value = _uiState.value.copy(isWatchingAd = true, errorMessage = null)

        adManager.showRewardedAd(
            activity = activity,
            rewardAmount = 100L,
            onComplete = { adResult ->
                viewModelScope.launch {
                    val validationRes = rewardManager.validateAndGrantPendingReward(
                        pendingRewardId = pendingId,
                        adResult = adResult,
                        moves = moves,
                        timeSeconds = duration
                    )

                    validationRes.onSuccess { coins ->
                        _uiState.value = _uiState.value.copy(
                            isWatchingAd = false,
                            isSolvedWaitingClaim = false,
                            isCompleteCelebration = true,
                            rewardCoinsEarned = coins
                        )
                    }.onFailure { err ->
                        _uiState.value = _uiState.value.copy(
                            isWatchingAd = false,
                            errorMessage = err.message ?: "Failed to validate reward."
                        )
                    }
                }
            },
            onFailedOrDismissed = { reason ->
                _uiState.value = _uiState.value.copy(
                    isWatchingAd = false,
                    errorMessage = reason
                )
            }
        )
    }

    fun onLeavePuzzle(activity: Activity?) {
        activity?.let { adManager.showInterstitialAdIfReady(it) }
    }

    fun toggleHintOverlay() {
        _uiState.value = _uiState.value.copy(isHintVisible = !_uiState.value.isHintVisible)
    }

    fun toggleTileNumbers() {
        _uiState.value = _uiState.value.copy(showTileNumbers = !_uiState.value.showTileNumbers)
    }

    fun restartPuzzle() {
        val puzzle = _uiState.value.puzzle ?: return
        loadPuzzle(puzzle.id)
    }

    fun requestAiHint() {
        val puzzle = _uiState.value.puzzle ?: return
        val pState = _uiState.value.puzzleState ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRequestingAiHint = true)
            val result = GeminiService.getPuzzleHint(
                puzzleTitle = puzzle.title,
                gridSize = puzzle.gridSize,
                moves = pState.moveCount,
                isEasyMode = puzzle.gridSize == 3
            )
            _uiState.value = _uiState.value.copy(
                isRequestingAiHint = false,
                aiHintText = result.getOrNull() ?: "Focus on completing the top rows first!"
            )
        }
    }

    fun dismissAiHint() {
        _uiState.value = _uiState.value.copy(aiHintText = null)
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
