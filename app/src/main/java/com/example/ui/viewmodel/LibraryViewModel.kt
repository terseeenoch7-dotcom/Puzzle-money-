package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.GeminiService
import com.example.data.audio.AudioRecorderHelper
import com.example.data.db.AppDatabase
import com.example.data.model.PuzzleImage
import com.example.data.repository.PuzzleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LibraryFilters(
    val selectedTab: String = "All", // "All", "New", "Popular", "Featured", "Favorites"
    val selectedCategory: String = "All",
    val selectedDifficulty: String = "All", // "All", "3x3", "4x4", "5x5"
    val searchQuery: String = ""
)

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = PuzzleRepository(db)
    private val audioRecorder = AudioRecorderHelper()

    val rawPuzzles: StateFlow<List<PuzzleImage>> = repository.getAllPuzzles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _filters = MutableStateFlow(LibraryFilters())
    val filters: StateFlow<LibraryFilters> = _filters

    private val _isRecordingVoice = MutableStateFlow(false)
    val isRecordingVoice: StateFlow<Boolean> = _isRecordingVoice

    private val _isTranscribing = MutableStateFlow(false)
    val isTranscribing: StateFlow<Boolean> = _isTranscribing

    val filteredPuzzles: StateFlow<List<PuzzleImage>> = combine(rawPuzzles, _filters) { puzzles, filter ->
        puzzles.filter { puzzle ->
            val matchTab = when (filter.selectedTab) {
                "New" -> puzzle.isNew
                "Popular" -> puzzle.isPopular
                "Featured" -> puzzle.isFeatured
                "Favorites" -> puzzle.isFavorite
                else -> true
            }

            val matchCategory = if (filter.selectedCategory == "All") true
            else puzzle.category.equals(filter.selectedCategory, ignoreCase = true)

            val matchDifficulty = when (filter.selectedDifficulty) {
                "3x3" -> puzzle.gridSize == 3
                "4x4" -> puzzle.gridSize == 4
                "5x5" -> puzzle.gridSize == 5
                else -> true
            }

            val matchQuery = if (filter.searchQuery.isBlank()) true
            else puzzle.title.contains(filter.searchQuery, ignoreCase = true) ||
                    puzzle.category.contains(filter.searchQuery, ignoreCase = true)

            matchTab && matchCategory && matchDifficulty && matchQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setTab(tab: String) {
        _filters.value = _filters.value.copy(selectedTab = tab)
    }

    fun setCategory(category: String) {
        _filters.value = _filters.value.copy(selectedCategory = category)
    }

    fun setDifficulty(difficulty: String) {
        _filters.value = _filters.value.copy(selectedDifficulty = difficulty)
    }

    fun setSearchQuery(query: String) {
        _filters.value = _filters.value.copy(searchQuery = query)
    }

    fun toggleFavorite(puzzle: PuzzleImage) {
        viewModelScope.launch {
            repository.toggleFavorite(puzzle.id, !puzzle.isFavorite)
        }
    }

    /**
     * Handles microphone toggle for voice search using gemini-3.5-transcribe
     */
    fun toggleVoiceSearch(hasRecordAudioPermission: Boolean) {
        if (!hasRecordAudioPermission) return
        if (!com.example.util.NetworkMonitor.getInstance(getApplication()).isCurrentlyOnline()) return

        if (_isRecordingVoice.value) {
            // Stop and transcribe
            viewModelScope.launch {
                _isRecordingVoice.value = false
                _isTranscribing.value = true
                val wavBase64 = audioRecorder.stopRecordingAndGetWavBase64()
                if (wavBase64 != null) {
                    val result = GeminiService.transcribeAudio(wavBase64)
                    result.onSuccess { transcribedText ->
                        // Clean transcription (remove periods and make clean search term)
                        val cleanQuery = transcribedText.replace(".", "").trim()
                        if (cleanQuery.isNotBlank()) {
                            _filters.value = _filters.value.copy(searchQuery = cleanQuery)
                        }
                    }
                }
                _isTranscribing.value = false
            }
        } else {
            // Start recording
            val started = audioRecorder.startRecording()
            if (started) {
                _isRecordingVoice.value = true
            }
        }
    }
}
