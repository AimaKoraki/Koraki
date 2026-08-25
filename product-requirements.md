# Product Requirements Document (PRD)

## App Overview
**Koraki** is a minimalist, private-first personal note manager with a bold, dark crimson aesthetic, a stealth vault for confidential notes, and a dedicated space for tracking close personal connections ("Companions").

### Target Devices
The application is specifically optimized for modern flagship Android hardware:
- **Google Pixel 10 Pro**
- **Samsung Galaxy S24 FE**

## Unified Red Design System

### 1. Background & Surface
| Token                | Hex         | Usage                |
| -------------------- | ----------- | -------------------- |
| `background`         | **#080506** | Main app background  |
| `backgroundElevated` | **#0E0709** | Screens / sections   |
| `surface`            | **#14090C** | Cards                |
| `surfaceElevated`    | **#1C0C10** | Modals, sheets       |
| `surfaceHover`       | **#251016** | Hover / pressed      |
| `surfaceActive`      | **#2D1118** | Selected cards       |
| `surfaceCrimson`     | **#350F18** | Special red sections |

### 2. Primary Red Scale
| Token    | Hex         | Usage             |
| -------- | ----------- | ----------------- |
| `red950` | **#24060C** | Deepest red       |
| `red900` | **#3A0710** | Dark backgrounds  |
| `red800` | **#520A16** | Dark accents      |
| `red700` | **#720F20** | Oxblood           |
| `red600` | **#94152A** | Dark crimson      |
| `red500` | **#B51E35** | **Primary brand** |
| `red400` | **#D12642** | Active elements   |
| `red300` | **#E43A55** | Highlights        |
| `red200` | **#F05A70** | Bright accent     |
| `red100` | **#FF8A9A** | Rare highlights   |

### 3. Special Romantic / Deep-Red Palette
| Name         | Hex       |
| ------------ | --------- |
| Black Rose   | `#170609` |
| Deep Wine    | `#2A0810` |
| Burgundy     | `#480B17` |
| Blood Wine   | `#650D1B` |
| Crimson Rose | `#8F1429` |
| Velvet Red   | `#A71932` |
| True Crimson | `#C5223D` |
| Ruby Heart   | `#E03450` |
| Soft Rose    | `#F06A7D` |

### 4. Text Palette
| Token           | Hex         | Usage                   |
| --------------- | ----------- | ----------------------- |
| `textPrimary`   | **#FFF7F8** | Titles / important text |
| `textSecondary` | **#E4D7DA** | Body text               |
| `textTertiary`  | **#B8A5AA** | Metadata                |
| `textMuted`     | **#806C72** | Placeholder / disabled  |
| `textOnRed`     | **#FFFFFF** | Text on crimson buttons |
| `textOnDark`    | **#F8EEF0** | General dark UI         |

### 5. Borders
| Token           | Hex         | Usage                |
| --------------- | ----------- | -------------------- |
| `borderSubtle`  | **#281317** | Normal cards         |
| `borderDefault` | **#3A1A21** | Cards / inputs       |
| `borderStrong`  | **#54202B** | Focused elements     |
| `borderCrimson` | **#8F1429** | Selected / important |
| `borderActive`  | **#B51E35** | Active state         |

### 6. Buttons
- **Primary button:** Background `#B51E35`, Text `#FFFFFF`, Pressed `#8F1429`, Hover `#D12642`
- **Secondary button:** Background `#2A0810`, Border `#54202B`, Text `#E4D7DA`, Pressed `#480B17`
- **Destructive button:** Background `#650D1B`, Text `#FFFFFF`, Pressed `#480B17`

### 7. Journaling Vault
| Role             | Hex         | Usage                             |
| ---------------- | ----------- | --------------------------------- |
| Vault Background | **#070405** | Deepest intimate background       |
| Vault Surface    | **#110608** | Vault card surface                |
| Vault Card       | **#19090D** | Vault card container              |
| Vault Elevated   | **#230C12** | Elevated modal / dialogs          |
| Vault Accent     | **#94152A** | FAB & pinned bookmarks            |
| Vault Active     | **#D12642** | Security / lock indicators        |
| Vault Text       | **#FFF7F8** | Primary vault text                |
| Vault Secondary  | **#D5C3C7** | Secondary vault body text         |
| Vault Muted      | **#806C72** | Metadata / muted timestamps       |
| Vault Border     | **#281317** | Vault card borders                |

### 8. Semantic System
| Meaning | Color      | Hex       |
| ------- | ---------- | --------- |
| Success | Soft Green | `#55A878` |
| Warning | Amber Gold | `#D39A45` |
| Info    | Muted Blue | `#718EAD` |
| Error   | Crimson    | `#D12642` |

### 9. Glow Effects & Gradients
| Name | Formula | Usage |
|---|---|---|
| Ambient Glow | `rgba(181, 30, 53, 0.08)` | Ambient radial background lighting behind notes |
| General Glow | `rgba(181, 30, 53, 0.18)` | Active note, secondary highlights, bottom nav pill |
| Strong Glow  | `rgba(209, 38, 66, 0.28)` | Primary CTA, Selected navigation, Vault unlock |

- **Crimson gradient:** `#720F20` → `#B51E35`
- **Blood gradient:** `#3A0710` → `#8F1429`
- **Ruby gradient:** `#94152A` → `#D12642`
- **Dark atmospheric background:** `#080506` → `#170609`
- **Special journal entry subtle gradient:** `#1A080D` → `#2A0B12`
- **App background radial gradient:** Center `rgba(181, 30, 53, 0.08)`, Edges `#080506`

### Note Types
Notes can be of standard type (`NOTE`) or stealth journal type (`JOURNAL`).

## Core Screens

### 1. Notes Screen
- **Top Header:** Display the user's name/tagline (e.g., "Janadhi's Koraki") with a subtle raven motif.
- **Search Bar:** Placed directly below the username header. As the user types, filter active notes in real time.
- **Passcode Interceptor:** If the input matches the stored passcode (default: `#journal` or `#koraki`), trigger a haptic pulse, clear the search text, and set the screen state `isJournalUnlocked = true`.
- **View Switcher:**
  - `isJournalUnlocked == false`: Render the **Standard Notes View** (Google Keep style).
  - `isJournalUnlocked == true`: Transition smoothly with an animated `AnimatedContent` into the **Journal View**, displaying a stealth exit/lock button in the header to return to standard notes.

### 2. Standard Notes View (Google Keep Style)
- `LazyVerticalStaggeredGrid` (2-column staggered) displaying **unlocked standard** notes (`isLocked = false`, `type = NOTE`).
- Dark charcoal surface (`#1E1A1B`) with subtle crimson borders (`#3E181A`).
- Bold title, truncated body preview, and formatted relative timestamp.
- Red FAB to create new note.

### 3. Hidden Journaling Page (Editorial/Book Vibe)
- **Aesthetic:** Move away from standard grid tiles to an intimate, personal journal aesthetic.
- **Layout:** Single-column vertical timeline (`LazyColumn`) with date-anchored dividers (e.g., *"Monday, August 24"*).
- **Card UI (`JournalEntryCard.kt`):** Full-width entries styled like pages in a dark crimson leather-bound book, featuring subtle line dividers, serif typography (`FontFamily.Serif`), mood/tag badges, and time stamps.
  - **Media Integration:** Beautifully renders attached images (using Coil) and playable voice note modules natively in the timeline.
- **Quick Entry Header:** A dedicated journal prompt banner at the top (e.g., *"What's on your mind tonight?"*).
- **FAB / Quick Add:** Specialized Journal Entry creator.
- Displays notes where `type = JOURNAL`.

### 4. Companions Screen
- Directory of virtual sprite companions (Shimeji).
- Displays a 2-column grid of available sprite companions.
- Each card shows a preview of the sprite, name, species, and unlock status.
- Tap a companion to activate/deactivate.
- **Active State:** The active companion renders as a walking animation (`ShimejiOverlay`) pinned directly above the bottom navigation bar across all main screens. The overlay hides automatically when navigating into the editor or vault.

### 5. Profile Screen
- Stats overview: total public notes, vault/journal notes count, companions count.
- Section to view and change the Vault passcode string.
- **Local JSON Backup & Restore:** 
  - An "Export" button leveraging the native Storage Access Framework to dump the entire Room Database into a human-readable `koraki_backup.json` file on external storage (like Downloads or Google Drive).
  - An "Import" button to restore and merge notes back from a JSON backup file seamlessly without data loss.

### 6. Editor Screens (`NoteEditorScreen` & `JournalEditorScreen`)
- Full Create, Read, Update, and Delete operations for both standard notes and journal entries.
- Clean, distraction-free plain-text editor.
- **Media Tools:** Ability to attach gallery images (Photo Picker) and record in-app voice notes (Microphone). Copies media into the app's secure internal storage (`filesDir/images` and `filesDir/audio`).
- Auto-save on back navigation.
- `imePadding()` so keyboard never obscures text.
- Include a delete action with a confirmation dialog.