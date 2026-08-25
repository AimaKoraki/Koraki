# AGENTS.md

## Project Identity
- **Project Name:** Koraki (κόρακι)
- **Platform:** Native Android (Min SDK 34, Target SDK 36)
- **Hardware Targets:** Google Pixel 10 Pro, Samsung Galaxy S24 FE
- **UI Toolkit:** Jetpack Compose (Material 3)
- **Architecture:** Single Activity, MVVM + Clean Architecture with Unidirectional Data Flow (UDF)
- **Dependency Injection:** Dagger Hilt
- **Local Persistence:** Room Database + Jetpack DataStore Preferences

## AI Agent Rules & Engineering Constraints
- **100% Jetpack Compose:** Do not create XML layouts or ViewBinding.
- **Edge-to-Edge:** Implement native Android 14 window insets (`WindowInsets.systemBars`, `imePadding`) across all screens.
- **State Management:** ViewModels must expose immutable `StateFlow<UiState>`. Composable screens must accept state and emit lambda events.
- **Vault Security:** Locked notes (`isLocked = true`) MUST NEVER be returned by standard public queries or exposed in public search results.
- **Secret Vault Trigger:** The vault must open stealthily when the user types the stored passcode (default: `#koraki`) into the main notes search bar. The query must auto-clear upon navigation.
- **Strict Theming:** UI elements must exclusively use the curated dark-crimson palette and Semantic/Glow system defined in `Color.kt` and `product-requirements.md`. NEVER invent or use hardcoded hex colors outside this exact palette. Glow effects (`GlowStrong`, `GlowGeneral`) must be used sparingly (e.g., Primary CTAs, active states) as opposed to solid red fills.

## Package Structure
```
com.aima.koraki
├── data/
│   ├── local/
│   │   ├── dao/         (NoteDao, CompanionDao)
│   │   ├── entity/      (NoteEntity, CompanionEntity)
│   │   ├── Converters.kt
│   │   └── KorakiDatabase.kt
│   ├── model/           (VirtualCompanion, CompanionMetadata)
│   ├── preferences/     (VaultPreferences)
│   └── repository/      (NoteRepository, CompanionRepository, ShimejiRepository, BackupRepository)
├── di/                  (AppModule)
├── navigation/          (KorakiScreen, Navigation)
├── ui/
│   ├── companions/      (CompanionsScreen, CompanionsViewModel)
│   ├── components/      (NoteCard, JournalEntryCard, KorakiBottomBar, ShimejiOverlay)
│   ├── editor/          (NoteEditorScreen, JournalEditorScreen, EditorViewModel)
│   ├── login/           (LoginScreen, LoginViewModel)
│   ├── notes/           (NotesScreen, NotesViewModel)
│   ├── profile/         (ProfileScreen, ProfileViewModel)
│   ├── theme/           (Color, Theme, Type)
│   └── vault/           (VaultScreen, VaultViewModel)
└── util/                (BiometricHelper, MediaManager)
```

## Command Execution
- Always run `./gradlew build` after adding new dependencies to verify project health.
- Run `./gradlew spotlessApply` (or standard ktlint formatting) before finalizing any UI file.