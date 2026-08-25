# Task Plan

**Instructions for AI Agent:** Update this file by changing `[ ]` to `[x]` as you complete each task. Read this file before making major decisions.

## Phase 1: Project Scaffolding & Setup
- [x] Initialize Android project with empty Compose activity.
- [x] Add dependencies to `build.gradle.kts` (Compose BOM, Navigation, Room, Hilt, Coroutines).
- [x] Set up Hilt `Application` class and update `AndroidManifest.xml`.

## Phase 2: Theme & UI Foundation
- [x] Implement `Color.kt` and `Theme.kt` using the Dark Red and Raven Black palette.
- [x] Setup Material 3 `MaterialTheme` wrapper.
- [x] Create base composable skeletons for `HomeScreen` and `NoteEditorScreen`.
- [x] Implement basic Compose Navigation (`NavHost`) between the two screens.

## Phase 3: Data Layer & Persistence
- [x] Create `NoteEntity` Room entity (updated with NoteType).
- [x] Create `CompanionEntity` Room entity.
- [x] Create `NoteDao` interface with all required queries (public/locked separation enforced).
- [x] Create `CompanionDao` interface.
- [x] Create `KorakiDatabase` Room database class.
- [x] Create `VaultPreferences` DataStore wrapper.
- [x] Create `NoteRepository`.
- [x] Create `CompanionRepository`.
- [x] Create Hilt `AppModule` providing all singletons.

## Phase 4: Business Logic (ViewModels)
- [x] `NotesViewModel` — handle search filtering, passcode detection, and state transitions (Standard vs Journal).
- [x] `VaultViewModel` — locked notes stream.
- [x] `EditorViewModel` — load by id, auto-save, delete.
- [x] `CompanionsViewModel` — companion CRUD.
- [x] `ProfileViewModel` — stats aggregation + vault passcode read/write.

## Phase 5: Navigation Refactor
- [x] Define `KorakiScreen` sealed class with all routes + bottom-nav metadata.
- [x] Rebuild `KorakiNavHost` with bottom-nav shell + modal routes.

## Phase 6: UI Components & Screens
- [x] `NoteCard` composable (Staggered Grid).
- [x] `JournalEntryCard` composable (Timeline aesthetic).
- [x] `KorakiBottomBar` composable.
- [x] `NotesScreen` (Notes Header, Search Bar Passcode Interceptor, View Switcher).
- [x] `Journaling UI` (Timeline view, stealth exit/lock button).
- [x] `VaultScreen` (locked notes grid, no visible entry point).
- [x] `CompanionsScreen` (list + add/edit bottom sheet).
- [x] `ProfileScreen` (stats + passcode editor).
- [x] `NoteEditorScreen` (wired to EditorViewModel, lock toggle, companion tag, imePadding).
- [x] `JournalEditorScreen` (editorial view, wired to EditorViewModel).

## Phase 7: Theme Polish
- [x] Update `Color.kt` to exact PRD hex tokens.
- [x] Update `Theme.kt` color scheme mapping.
- [x] Add Journal specific styling tokens (Serif fonts, Timeline dividers).

## Phase 8: Notes Screen Visual Refinement
- [x] Header title reduced to 36–40px (38sp) with subtitle ("N notes · Updated MMM d").
- [x] Search bar restyled (64px height, `#151012` container, 1px `#352126` border, `#B9A6AA` icon, `#77686C` placeholder).
- [x] Note Cards restyled (`#151012` bg, `#352126` border, `#F5EDEF` title, `#B9A6AA` body, 20px padding, 16px grid gap).
- [x] Note Metadata moved to bottom-left row ("Aug 25 · 2 min read" at 12sp).
- [x] Floating Action Button respecified (`#B51E35` bg, `#F5EDEF` icon, subtle crimson shadow/glow).
- [x] Bottom Navigation enhanced (15% height reduction, subdued crimson pill indicator, `#D72D48` active icon/label, `#77686C`/`#B9A6AA` inactive state).
- [x] Ambient Crimson Radial Glow added behind note grid (`rgba(181, 30, 53, 0.04)`).
- [x] Typography Hierarchy updated (`Type.kt`).

## Phase 9: Media Management & Voice Notes
- [x] In-app audio recorder using `MediaRecorder` with AAC encoding (`filesDir/audio/`).
- [x] Audio player using `MediaPlayer` (`playAudio()`).
- [x] Gallery image picker using Android Photo Picker API with internal sandboxed storage copying (`filesDir/images/`).
- [x] Room schema & converter updates to persist `images` and `audio` path lists (`Converters.kt`).

## Phase 10: App Lock, Biometrics & Data Export
- [x] Master App Lock Gate (`LoginScreen.kt`, `LoginViewModel.kt`) with SHA-256 PIN hashing.
- [x] Biometric / Device Credential support (`BiometricHelper.kt`).
- [x] Full local JSON Backup & Restore via Storage Access Framework (`BackupRepository.kt`, `BackupData.kt`).
- [x] Virtual Companion Shimeji engine overlay (`ShimejiRepository.kt`, `ShimejiOverlay.kt`).