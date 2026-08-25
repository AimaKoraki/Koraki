package com.aima.koraki.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aima.koraki.data.local.entity.CompanionEntity
import com.aima.koraki.data.local.entity.NoteEntity
import com.aima.koraki.data.preferences.VaultPreferences
import com.aima.koraki.data.repository.CompanionRepository
import com.aima.koraki.data.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** UI state for the Notes screen. */
data class NotesUiState(
    val notes: List<NoteEntity> = emptyList(),
    val journalNotes: List<NoteEntity> = emptyList(),
    val query: String = "",
    val isLoading: Boolean = true,
    val isJournalUnlocked: Boolean = false,
    val username: String = "User",
    val isFingerprintEnabled: Boolean = false,
    val anniversaryDate: Long? = null,
)

/** One-time side-effects emitted from [NotesViewModel]. */
sealed interface NotesEvent {
    /** Navigate silently to the vault (for backwards compatibility if needed, or if we keep vault separate) */
    data object OpenVault : NotesEvent
    /** Trigger a haptic pulse when journal is unlocked */
    data object TriggerHaptic : NotesEvent
}

/**
 * ViewModel for the public Notes screen and inline Journal View.
 */
@HiltViewModel
class NotesViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val companionRepository: CompanionRepository,
    private val vaultPreferences: VaultPreferences,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    private val _isJournalUnlocked = MutableStateFlow(false)
    val isJournalUnlocked = _isJournalUnlocked.asStateFlow()

    private val _events = MutableSharedFlow<NotesEvent>()
    val events = _events.asSharedFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _notes: StateFlow<List<NoteEntity>> = _query
        .flatMapLatest { q ->
            if (q.isBlank()) {
                noteRepository.getPublicNotes()
            } else {
                noteRepository.searchPublicNotes(q)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _journalNotes: StateFlow<List<NoteEntity>> = noteRepository.getJournalNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val uiState: StateFlow<NotesUiState> = combine(
        _notes,
        _journalNotes,
        _query,
        _isJournalUnlocked,
        vaultPreferences.getUsername,
        vaultPreferences.isFingerprintEnabled,
        vaultPreferences.getAnniversaryDate,
    ) { args ->
        val rawNotes = args[0] as List<NoteEntity>
        val isFingerprintEnabled = args[5] as Boolean
        val displayedNotes = if (isFingerprintEnabled) rawNotes else rawNotes.filter { !it.isLocked }
        
        NotesUiState(
            notes = displayedNotes,
            journalNotes = args[1] as List<NoteEntity>,
            query = args[2] as String,
            isJournalUnlocked = args[3] as Boolean,
            username = args[4] as String,
            isFingerprintEnabled = isFingerprintEnabled,
            anniversaryDate = args[6] as Long?,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = NotesUiState(),
    )

    private val vaultCode: StateFlow<String> = vaultPreferences.getVaultCode
        .stateIn(viewModelScope, SharingStarted.Eagerly, VaultPreferences.DEFAULT_VAULT_CODE)

    /**
     * Called on every keystroke in the search bar.
     * Checks for the vault passcode before updating the public query.
     */
    fun onQueryChange(newQuery: String) {
        val currentPasscode = vaultCode.value
        val trimmed = newQuery.trim()
        if (trimmed == currentPasscode) {
            _query.update { "" }
            viewModelScope.launch {
                _events.emit(NotesEvent.TriggerHaptic)
                _events.emit(NotesEvent.OpenVault)
            }
        } else if (trimmed == "#journal") {
            // Stealth journal trigger — clear query first, unlock journal, emit haptic
            _query.update { "" }
            _isJournalUnlocked.update { true }
            viewModelScope.launch { _events.emit(NotesEvent.TriggerHaptic) }
        } else {
            _query.update { newQuery }
        }
    }

    fun lockJournal() {
        _isJournalUnlocked.update { false }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch { noteRepository.deleteNote(note) }
    }

    fun onSpriteTapped(name: String) {
        viewModelScope.launch {
            val existing = companionRepository.getCompanionByName(name)
            if (existing != null) {
                companionRepository.incrementAffectionByName(name)
            } else {
                val newEntity = CompanionEntity(
                    name = name,
                    roleOrRelation = "Virtual Companion",
                    avatarColorHex = "#B51E35",
                    bio = "Your loyal companion",
                    createdAt = System.currentTimeMillis(),
                    affectionLevel = 1,
                )
                companionRepository.upsertCompanion(newEntity)
            }
        }
    }
}
