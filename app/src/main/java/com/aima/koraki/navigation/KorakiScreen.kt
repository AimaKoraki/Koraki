package com.aima.koraki.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Defines every named destination in the Koraki nav graph.
 */
sealed class KorakiScreen(val route: String) {

    // ── Bottom-nav destinations ──────────────────────────────────────────────
    data object Notes : KorakiScreen("notes")
    data object Companions : KorakiScreen("companions")
    data object Profile : KorakiScreen("profile")

    // ── Modal / full-screen destinations ────────────────────────────────────
    data object Vault : KorakiScreen("vault")
    data object Login : KorakiScreen("login")

    /** Editor with an optional [noteId] and [isLocked]. -1 means create new note. */
    data object Editor : KorakiScreen("editor/{noteId}?isLocked={isLocked}") {
        fun route(noteId: Long = -1L, isLocked: Boolean = false): String = "editor/$noteId?isLocked=$isLocked"
    }

    /** Journal Editor with an optional [noteId]. -1 means create new journal entry. */
    data object JournalEditor : KorakiScreen("journal_editor/{noteId}?isJournal={isJournal}") {
        fun route(noteId: Long = -1L, isJournal: Boolean = true): String = "journal_editor/$noteId?isJournal=$isJournal"
    }
}

/** Metadata for bottom navigation bar items. */
data class BottomNavItem(
    val screen: KorakiScreen,
    val label: String,
    val icon: ImageVector,
)

val bottomNavItems = listOf(
    BottomNavItem(KorakiScreen.Notes, "Notes", Icons.Default.StickyNote2),
    BottomNavItem(KorakiScreen.Companions, "Companions", Icons.Default.People),
    BottomNavItem(KorakiScreen.Profile, "Profile", Icons.Default.Person),
)
