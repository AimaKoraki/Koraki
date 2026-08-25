package com.aima.koraki.ui.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aima.koraki.data.local.entity.CompanionEntity
import com.aima.koraki.data.local.entity.NoteEntity
import com.aima.koraki.data.local.entity.NoteType
import com.aima.koraki.data.repository.CompanionRepository
import com.aima.koraki.data.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.aima.koraki.data.preferences.VaultPreferences
import com.aima.koraki.util.LevelManager
import kotlinx.coroutines.flow.first

/** One-shot event triggered when gaining enough XP to cross a Level boundary. */
data class LevelUpEvent(
    val newLevel: Int,
    val newTitle: String,
    val unlockDescription: String,
)

/** UI state for the Note Editor screen. */
data class EditorUiState(
    val title: String = "",
    val content: String = "",
    val isLocked: Boolean = false,
    val isPinned: Boolean = false,
    val companionId: Long? = null,
    val noteType: NoteType = NoteType.NOTE,
    val images: List<String> = emptyList(),
    val audio: List<String> = emptyList(),
    val mood: String? = null,
    val xpAwarded: Boolean = false,
    val isRecordingAudio: Boolean = false,
    val isPlayingAudio: Boolean = false,
    val playingAudioPath: String? = null,
    val isLoading: Boolean = true,
    val isSaved: Boolean = false,
    val levelUpEvent: LevelUpEvent? = null,
)

/**
 * ViewModel for the Note Editor screen.
 *
 * - Loads an existing note by [noteId] from [SavedStateHandle] (id = -1 → new note).
 * - Auto-saves via [saveNote] which is triggered before back navigation.
 * - Exposes [companions] so the UI can render a companion tag picker.
 */
@HiltViewModel
class EditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val noteRepository: NoteRepository,
    private val companionRepository: CompanionRepository,
    private val vaultPreferences: VaultPreferences,
    private val mediaManager: com.aima.koraki.util.MediaManager,
) : ViewModel() {

    private val noteId: Long = savedStateHandle.get<Long>("noteId") ?: -1L
    private val isJournal: Boolean = savedStateHandle.get<Boolean>("isJournal") ?: false
    private val initIsLocked: Boolean = savedStateHandle.get<Boolean>("isLocked") ?: false

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    val companions: StateFlow<List<CompanionEntity>> = companionRepository.getAllCompanions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        if (noteId > 0) {
            viewModelScope.launch {
                val note = noteRepository.getNoteById(noteId)
                if (note != null) {
                    _uiState.update {
                        it.copy(
                            title = note.title,
                            content = note.content,
                            isLocked = note.isLocked,
                            isPinned = note.isPinned,
                            companionId = note.companionId,
                            noteType = note.type,
                            images = note.images,
                            audio = note.audio,
                            mood = note.mood,
                            xpAwarded = note.xpAwarded,
                            isLoading = false,
                        )
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            noteType = if (isJournal) NoteType.JOURNAL else NoteType.NOTE,
                            isLocked = initIsLocked,
                            isLoading = false 
                        ) 
                    }
                }
            }
        } else {
            _uiState.update { 
                it.copy(
                    noteType = if (isJournal) NoteType.JOURNAL else NoteType.NOTE,
                    isLocked = initIsLocked,
                    isLoading = false 
                ) 
            }
        }
    }

    fun onTitleChange(title: String) = _uiState.update { it.copy(title = title) }
    fun onContentChange(content: String) = _uiState.update { it.copy(content = content) }
    fun onLockToggle(isLocked: Boolean) = _uiState.update { it.copy(isLocked = isLocked) }
    fun onPinToggle(isPinned: Boolean) = _uiState.update { it.copy(isPinned = isPinned) }
    fun onCompanionSelect(companionId: Long?) = _uiState.update { it.copy(companionId = companionId) }
    fun onMoodChange(mood: String?) = _uiState.update { it.copy(mood = mood) }
    fun onLevelUpDismissed() = _uiState.update { it.copy(levelUpEvent = null) }

    fun addImage(uri: android.net.Uri) {
        viewModelScope.launch {
            val internalPath = mediaManager.copyImageToInternalStorage(uri)
            if (internalPath != null) {
                _uiState.update { it.copy(images = it.images + internalPath) }
            }
        }
    }

    fun deleteImage(path: String) {
        _uiState.update { it.copy(images = it.images - path) }
    }

    fun toggleAudioRecording() {
        if (_uiState.value.isRecordingAudio) {
            val audioPath = mediaManager.stopRecording()
            if (audioPath != null) {
                _uiState.update { it.copy(audio = it.audio + audioPath, isRecordingAudio = false) }
            } else {
                _uiState.update { it.copy(isRecordingAudio = false) }
            }
        } else {
            mediaManager.startRecording()
            _uiState.update { it.copy(isRecordingAudio = true) }
        }
    }

    fun toggleAudioPlayback(path: String) {
        val currentPlaying = _uiState.value.playingAudioPath
        if (currentPlaying == path && _uiState.value.isPlayingAudio) {
            mediaManager.stopAudio()
            _uiState.update { it.copy(isPlayingAudio = false, playingAudioPath = null) }
        } else {
            mediaManager.playAudio(path)
            _uiState.update { it.copy(isPlayingAudio = true, playingAudioPath = path) }
        }
    }

    fun deleteAudio(path: String) {
        if (_uiState.value.playingAudioPath == path) {
            mediaManager.stopAudio()
            _uiState.update { it.copy(isPlayingAudio = false, playingAudioPath = null, audio = it.audio - path) }
        } else {
            _uiState.update { it.copy(audio = it.audio - path) }
        }
    }

    /**
     * Upserts the current editor state as a [NoteEntity].
     * Awards XP strictly ONCE upon initial creation.
     */
    fun saveNote() {
        val state = _uiState.value
        if (state.title.isBlank() && state.content.isBlank() && state.images.isEmpty() && state.audio.isEmpty()) return
        viewModelScope.launch {
            kotlinx.coroutines.withContext(kotlinx.coroutines.NonCancellable) {
                var finalAudioList = state.audio
                if (state.isRecordingAudio) {
                    val audioPath = mediaManager.stopRecording()
                    if (audioPath != null) {
                        finalAudioList = finalAudioList + audioPath
                    }
                }

                var isNewXpAwarded = state.xpAwarded
                var levelUpEvt: LevelUpEvent? = null

                if (!isNewXpAwarded && noteId <= 0) {
                    var xpGain = 50 // +50 XP base for creation
                    if (state.images.isNotEmpty() || finalAudioList.isNotEmpty()) xpGain += 20
                    if (!state.mood.isNull_Blank()) xpGain += 15

                    // Evaluate daily journaling streak bonus (+30 XP once per day)
                    vaultPreferences.checkAndRecordDailyStreak()

                    val oldTotalXp = vaultPreferences.getTotalXp.first()
                    val newTotalXp = vaultPreferences.addXp(xpGain)

                    if (LevelManager.isLevelUp(oldTotalXp, newTotalXp)) {
                        val milestone = LevelManager.getMilestone(newTotalXp)
                        levelUpEvt = LevelUpEvent(
                            newLevel = milestone.level,
                            newTitle = milestone.title,
                            unlockDescription = milestone.unlockDescription,
                        )
                    }

                    isNewXpAwarded = true
                }

                val note = NoteEntity(
                    id = if (noteId > 0) noteId else 0L,
                    title = state.title.trim(),
                    content = state.content.trim(),
                    timestamp = System.currentTimeMillis(),
                    isLocked = state.isLocked,
                    isPinned = state.isPinned,
                    companionId = state.companionId,
                    type = state.noteType,
                    images = state.images,
                    audio = finalAudioList,
                    mood = state.mood,
                    xpAwarded = isNewXpAwarded,
                )
                noteRepository.upsertNote(note)

                _uiState.update {
                    it.copy(
                        isSaved = true,
                        xpAwarded = isNewXpAwarded,
                        levelUpEvent = levelUpEvt,
                    )
                }
            }
        }
    }

    private fun String?.isNull_Blank(): Boolean = this.isNullOrBlank()

    fun deleteNote() {
        if (noteId <= 0) return
        viewModelScope.launch {
            kotlinx.coroutines.withContext(kotlinx.coroutines.NonCancellable) {
                val note = noteRepository.getNoteById(noteId) ?: return@withContext
                noteRepository.deleteNote(note)
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        mediaManager.stopAudio()
        if (_uiState.value.isRecordingAudio) {
            mediaManager.stopRecording()
        }
    }
}
