# Technical Specification

## SDK & Build
- `minSdk = 34` | `targetSdk = 36` | `compileSdk 36`
- **Target Hardware:** Google Pixel 10 Pro, Samsung Galaxy S24 FE
- Language: Kotlin 2.0.21
- Build system: AGP 8.13.2, Gradle Kotlin DSL
- Annotation processing: **KSP (Kotlin Symbol Processing)**

## Dependencies (Version Catalog: `gradle/libs.versions.toml`)

| Dependency | Version |
|---|---|
| Compose BOM | `2024.09.00` |
| Navigation Compose | `2.9.0` |
| Material Icons Extended | (from Compose BOM) |
| Hilt Android | `2.52` |
| Hilt Navigation Compose | `1.2.0` |
| Room Runtime + KTX | `2.7.0` |
| Room Compiler (KSP) | `2.7.0` |
| KSP Plugin | `2.0.21-1.0.27` |
| DataStore Preferences | `1.1.1` |
| Coroutines Android | `1.9.0` |
| Gson | `2.10.1` |
| Coil Compose | `2.6.0` |

## Database Entities & Schemas

### `KorakiBackup` (JSON DTO)
```kotlin
data class KorakiBackup(
    val exportDateMs: Long,
    val notes: List<NoteEntity>,
    val companions: List<CompanionEntity>
)
```

### `NoteType`
```kotlin
enum class NoteType {
    NOTE, JOURNAL
}
```

### `NoteEntity`
```kotlin
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val timestamp: Long,
    val isLocked: Boolean = false,
    val isPinned: Boolean = false,
    val companionId: Long? = null,
    val type: NoteType = NoteType.NOTE,
    val images: List<String> = emptyList(),
    val audio: List<String> = emptyList()
)
```

### `CompanionEntity`
```kotlin
@Entity(tableName = "companions")
data class CompanionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val roleOrRelation: String,
    val avatarColorHex: String,
    val bio: String,
    val createdAt: Long,
)
```

## Data Access Objects (DAOs)

### `NoteDao`
| Method | Return | Description |
|---|---|---|
| `getPublicNotes()` | `Flow<List<NoteEntity>>` | WHERE `isLocked = 0` AND `type = 'NOTE'` ORDER BY `timestamp` DESC |
| `getJournalNotes()` | `Flow<List<NoteEntity>>` | WHERE `type = 'JOURNAL'` AND `isLocked = 0` ORDER BY `timestamp` DESC |
| `getLockedNotes()` | `Flow<List<NoteEntity>>` | WHERE `isLocked = 1` ORDER BY `timestamp` DESC |
| `searchPublicNotes(query)` | `Flow<List<NoteEntity>>` | title/content LIKE `%query%` WHERE `isLocked = 0` AND `type = 'NOTE'` |
| `getNotesForCompanion(id)` | `Flow<List<NoteEntity>>` | WHERE `companionId = :id` AND `isLocked = 0` |
| `getPublicNoteCount()` | `Flow<Int>` | COUNT WHERE `type = 'NOTE'` for Profile stats |
| `getLockedNoteCount()` | `Flow<Int>` | COUNT WHERE `isLocked = 1 OR type = 'JOURNAL'` |
| `upsertNote(note)` | `suspend Unit` | `@Upsert` |
| `deleteNote(note)` | `suspend Unit` | `@Delete` |
| `getNoteById(id)` | `suspend NoteEntity?` | single lookup for editor |
| `getAllNotesSnapshot()` | `suspend List<NoteEntity>` | snapshot for JSON backup |

### `CompanionDao`
| Method | Return | Description |
|---|---|---|
| `getAllCompanions()` | `Flow<List<CompanionEntity>>` | ORDER BY `name` ASC |
| `getCompanionCount()` | `Flow<Int>` | COUNT of all companions for Profile stats |
| `getCompanionById(id)` | `suspend CompanionEntity?` | single lookup |
| `upsertCompanion(c)` | `suspend Unit` | `@Upsert` |
| `deleteCompanion(c)` | `suspend Unit` | `@Delete` |
| `getAllCompanionsSnapshot()` | `suspend List<CompanionEntity>` | snapshot for JSON backup |

## DataStore Vault Preferences
- Class: `VaultPreferences`
- Keys: 
  - `KEY_VAULT_CODE: Preferences.Key<String>` — default value `"#koraki"`
  - `KEY_VAULT_HINT: Preferences.Key<String>` — optional password hint
  - `KEY_USERNAME: Preferences.Key<String>` — username display
  - `KEY_ACTIVE_SHIMEJI: Preferences.Key<String>` — persists the currently active virtual companion
  - `KEY_FINGERPRINT_ENABLED: Preferences.Key<Boolean>` — fingerprint unlock toggle
  - `KEY_APP_LOCK_ENABLED: Preferences.Key<Boolean>` — master app pin lock toggle
  - `KEY_APP_PIN_HASH: Preferences.Key<String>` — SHA-256 hashed PIN
- Exposes: `getVaultCode`, `getVaultHint`, `getUsername`, `getActiveShimeji()`, `isFingerprintEnabled`, `isAppLockEnabled`, `getAppPinHash`

## DI (Hilt)
- `AppModule` (`@InstallIn(SingletonComponent::class)`)
  - Provides `KorakiDatabase` (version 4 with `@TypeConverters(Converters::class)`)
  - Provides `NoteDao`, `CompanionDao`
  - Injects `NoteRepository`, `CompanionRepository`, `BackupRepository`, `ShimejiRepository`, `VaultPreferences`, `MediaManager`

## Navigation Structure
```
NavHost (startDestination = "login" if appLockEnabled else "notes")
├── composable("login")                        → LoginScreen
├── composable("notes")                        → NotesScreen
├── composable("companions")                   → CompanionsScreen
├── composable("profile")                      → ProfileScreen
├── composable("vault")                        → VaultScreen
├── composable("editor/{noteId}?isLocked=...") → NoteEditorScreen
└── composable("journal_editor/{noteId}?...")  → JournalEditorScreen
```

## UDF State Pattern (per ViewModel)
```kotlin
data class XxxUiState(
    val data: List<...> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

val uiState: StateFlow<XxxUiState> = ...
```