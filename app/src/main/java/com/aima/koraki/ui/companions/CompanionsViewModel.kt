package com.aima.koraki.ui.companions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aima.koraki.data.local.entity.CompanionEntity
import com.aima.koraki.data.model.VirtualCompanion
import com.aima.koraki.data.preferences.VaultPreferences
import com.aima.koraki.data.repository.CompanionRepository
import com.aima.koraki.data.repository.ShimejiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.aima.koraki.util.LevelManager

/** UI state for the Companions (Shimeji picker) screen. */
data class CompanionsUiState(
    val companions: List<VirtualCompanion> = emptyList(),
    val activeCompanionName: String? = null,
    val userLevel: Int = 1,
    val isLoading: Boolean = true,
)

@HiltViewModel
class CompanionsViewModel @Inject constructor(
    private val shimejiRepository: ShimejiRepository,
    private val companionRepository: CompanionRepository,
    private val vaultPreferences: VaultPreferences,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompanionsUiState())
    val uiState: StateFlow<CompanionsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Restore persisted active companion name from DataStore
            val savedName = vaultPreferences.getActiveShimeji().first()

            // Load companion roster from assets
            val baseCompanions = shimejiRepository.loadVirtualCompanions()

            // Combine with Room companions & XP to track affectionLevel & unlocks reactively
            combine(
                companionRepository.getAllCompanions(),
                vaultPreferences.getActiveShimeji(),
                vaultPreferences.getTotalXp,
            ) { roomEntities, activeName, totalXp ->
                val userLevel = LevelManager.getLevel(totalXp)
                val affectionMap = roomEntities.associate { it.name to it.affectionLevel }
                val updatedCompanions = baseCompanions.map { companion ->
                    companion.copy(
                        unlockStatus = userLevel >= companion.requiredLevel,
                        affectionLevel = affectionMap[companion.name] ?: 0,
                    )
                }
                CompanionsUiState(
                    companions = updatedCompanions,
                    activeCompanionName = activeName ?: savedName,
                    userLevel = userLevel,
                    isLoading = false,
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    /** Returns the currently active [VirtualCompanion], or null if none is set. */
    val activeCompanion: VirtualCompanion?
        get() = _uiState.value.let { state ->
            state.companions.firstOrNull { it.name == state.activeCompanionName && it.unlockStatus }
        }

    /** Activate [name] as the walking companion, or pass null to dismiss it. */
    fun setActive(name: String?) {
        if (name != null) {
            val target = _uiState.value.companions.firstOrNull { it.name == name }
            if (target == null || !target.unlockStatus) return
        }
        _uiState.update { it.copy(activeCompanionName = name) }
        viewModelScope.launch {
            vaultPreferences.setActiveShimeji(name)
        }
    }

    /** Increments affection in Room & awards petting XP when the companion sprite is tapped. */
    fun onSpriteTapped(name: String) {
        viewModelScope.launch {
            vaultPreferences.recordPettingTap()

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
