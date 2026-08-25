package com.aima.koraki.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aima.koraki.ui.companions.CompanionsScreen
import com.aima.koraki.ui.companions.CompanionsViewModel
import com.aima.koraki.ui.components.KorakiBottomBar
import com.aima.koraki.ui.components.ShimejiOverlay
import com.aima.koraki.ui.editor.JournalEditorScreen
import com.aima.koraki.ui.editor.NoteEditorScreen
import com.aima.koraki.ui.login.LoginScreen
import com.aima.koraki.ui.login.LoginViewModel
import com.aima.koraki.ui.notes.NotesScreen
import com.aima.koraki.ui.profile.ProfileScreen
import com.aima.koraki.ui.vault.VaultScreen

import androidx.compose.foundation.background
import com.aima.koraki.ui.theme.DeepBackground

@Composable
fun KorakiNavHost(
    loginVm: LoginViewModel = hiltViewModel(),
) {
    val loginState by loginVm.uiState.collectAsStateWithLifecycle()

    if (loginState.isLoading) {
        // Safe fallback if Compose renders before splash screen finishes
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DeepBackground),
        )
        return
    }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Determine start destination based on app lock setting
    val startDestination = if (loginState.isAppLockEnabled) {
        KorakiScreen.Login.route
    } else {
        KorakiScreen.Notes.route
    }

    // Show bottom bar only on top-level destinations
    val showBottomBar = currentRoute in listOf(
        KorakiScreen.Notes.route,
        KorakiScreen.Companions.route,
        KorakiScreen.Profile.route,
    )

    // Hoist the Shimeji ViewModel at nav-host scope so the overlay persists across all tabs
    val shimejiVm: CompanionsViewModel = hiltViewModel()
    val shimejiState by shimejiVm.uiState.collectAsStateWithLifecycle()
    val activeCompanion = shimejiState.companions
        .firstOrNull { it.name == shimejiState.activeCompanionName && it.unlockStatus }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                KorakiBottomBar(
                    items = bottomNavItems,
                    currentRoute = currentRoute,
                    onItemClick = { item ->
                        navController.navigate(item.screen.route) {
                            popUpTo(KorakiScreen.Notes.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            NavHost(
                navController = navController,
                startDestination = startDestination,
            ) {
                // ── Login Gate ───────────────────────────────────────────────
                composable(KorakiScreen.Login.route) {
                    LoginScreen(
                        onAuthSuccess = {
                            navController.navigate(KorakiScreen.Notes.route) {
                                popUpTo(KorakiScreen.Login.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        viewModel = loginVm,
                    )
                }

                // ── Main destinations ────────────────────────────────────────
                composable(KorakiScreen.Notes.route) {
                    NotesScreen(
                        onNoteClick = { noteId ->
                            navController.navigate(KorakiScreen.Editor.route(noteId))
                        },
                        onNewNote = {
                            navController.navigate(KorakiScreen.Editor.route())
                        },
                        onJournalEntryClick = { noteId ->
                            navController.navigate(KorakiScreen.JournalEditor.route(noteId = noteId, isJournal = true))
                        },
                        onNewJournalEntry = {
                            navController.navigate(KorakiScreen.JournalEditor.route(noteId = -1L, isJournal = true))
                        },
                        onOpenVault = {
                            navController.navigate(KorakiScreen.Vault.route)
                        },
                    )
                }

                composable(KorakiScreen.Companions.route) {
                    CompanionsScreen(
                        onNoteClick = { noteId ->
                            navController.navigate(KorakiScreen.Editor.route(noteId))
                        },
                        viewModel = shimejiVm,
                    )
                }

                composable(KorakiScreen.Profile.route) {
                    ProfileScreen()
                }

                composable(KorakiScreen.Vault.route) {
                    VaultScreen(
                        onNoteClick = { noteId ->
                            navController.navigate(KorakiScreen.Editor.route(noteId))
                        },
                        onNewLockedNote = {
                            navController.navigate(KorakiScreen.Editor.route(isLocked = true))
                        },
                        onBack = { navController.popBackStack() },
                    )
                }

                composable(
                    route = KorakiScreen.Editor.route,
                    arguments = listOf(
                        navArgument("noteId") {
                            type = NavType.LongType
                            defaultValue = -1L
                        },
                        navArgument("isLocked") {
                            type = NavType.BoolType
                            defaultValue = false
                        },
                    ),
                ) { backStackEntry ->
                    val noteId = backStackEntry.arguments?.getLong("noteId") ?: -1L
                    NoteEditorScreen(
                        noteId = noteId.toInt(),
                        onNavigateBack = { navController.popBackStack() },
                    )
                }

                composable(
                    route = KorakiScreen.JournalEditor.route,
                    arguments = listOf(
                        navArgument("noteId") {
                            type = NavType.LongType
                            defaultValue = -1L
                        },
                        navArgument("isJournal") {
                            type = NavType.BoolType
                            defaultValue = true
                        },
                    ),
                ) { backStackEntry ->
                    val noteId = backStackEntry.arguments?.getLong("noteId") ?: -1L
                    JournalEditorScreen(
                        noteId = noteId.toInt(),
                        onNavigateBack = { navController.popBackStack() },
                    )
                }
            }

            // Shimeji overlay: pinned above the bottom nav, visible on all main tabs
            if (showBottomBar) {
                ShimejiOverlay(
                    activeCompanion = activeCompanion,
                    modifier = Modifier.align(Alignment.BottomCenter),
                    onSpriteTapped = { name -> shimejiVm.onSpriteTapped(name) },
                )
            }
        }
    }
}


