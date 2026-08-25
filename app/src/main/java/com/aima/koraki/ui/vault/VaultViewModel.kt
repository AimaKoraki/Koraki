package com.aima.koraki.ui.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aima.koraki.data.local.entity.NoteEntity
import com.aima.koraki.data.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** UI state for the Vault (locked notes) screen. */
data class VaultUiState(
    val notes: List<NoteEntity> = emptyList(),
    val isLoading: Boolean = true,
)

/**
 * ViewModel for the stealth Vault screen.
 * Only loads notes where [NoteEntity.isLocked] = true.
 */
@HiltViewModel
class VaultViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
) : ViewModel() {

    val uiState: StateFlow<VaultUiState> = noteRepository.getLockedNotes()
        .map { notes -> VaultUiState(notes = notes, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = VaultUiState(),
        )

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch { noteRepository.deleteNote(note) }
    }

    /** Flips the [NoteEntity.isPinned] state and persists it. */
    fun togglePin(note: NoteEntity) {
        viewModelScope.launch { noteRepository.upsertNote(note.copy(isPinned = !note.isPinned)) }
    }
}
