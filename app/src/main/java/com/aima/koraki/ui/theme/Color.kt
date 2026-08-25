package com.aima.koraki.ui.theme

import androidx.compose.ui.graphics.Color

// =============================================================================
// UNIFIED RED DESIGN SYSTEM
// =============================================================================

// ── 1. Background & Surface ──────────────────────────────────────────────────
val Background = Color(0xFF080506)            // Main app background
val BackgroundElevated = Color(0xFF0E0709)    // Screens / sections
val Surface = Color(0xFF14090C)               // Cards
val SurfaceElevated = Color(0xFF1C0C10)       // Modals, sheets
val SurfaceHover = Color(0xFF251016)          // Hover / pressed
val SurfaceActive = Color(0xFF2D1118)         // Selected cards
val SurfaceCrimson = Color(0xFF350F18)        // Special red sections

// Backwards-compatible aliases
val AppBackground = Background
val DeepBackground = BackgroundElevated
val PrimarySurface = Surface
val ElevatedSurface = SurfaceElevated
val HoverSurface = SurfaceHover

// ── 2. Primary Red Scale ────────────────────────────────────────────────────
val Red950 = Color(0xFF24060C)                // Deepest red
val Red900 = Color(0xFF3A0710)                // Dark backgrounds
val Red800 = Color(0xFF520A16)                // Dark accents
val Red700 = Color(0xFF720F20)                // Oxblood
val Red600 = Color(0xFF94152A)                // Dark crimson
val Red500 = Color(0xFFB51E35)                // Primary brand
val Red400 = Color(0xFFD12642)                // Active elements
val Red300 = Color(0xFFE43A55)                // Highlights
val Red200 = Color(0xFFF05A70)                // Bright accent
val Red100 = Color(0xFFFF8A9A)                // Rare highlights

// ── 3. Special Romantic / Deep-Red Palette ───────────────────────────────────
val BlackRose = Color(0xFF170609)
val DeepWine = Color(0xFF2A0810)
val Burgundy = Color(0xFF480B17)
val BloodWine = Color(0xFF650D1B)
val CrimsonRose = Color(0xFF8F1429)
val VelvetRed = Color(0xFFA71932)
val TrueCrimson = Color(0xFFC5223D)
val RubyHeart = Color(0xFFE03450)
val SoftRose = Color(0xFFF06A7D)

// Brand aliases
val PrimaryCrimson = Red500                    // #B51E35
val BrightCrimson = Red400                     // #D12642
val DarkCrimson = Red600                       // #94152A
val CrimsonGlow = Red300                       // #E43A55

// ── 4. Text Palette ─────────────────────────────────────────────────────────
val TextPrimary = Color(0xFFFFF7F8)           // Titles / important text
val TextSecondary = Color(0xFFE4D7DA)         // Body text
val TextTertiary = Color(0xFFB8A5AA)          // Metadata
val TextMuted = Color(0xFF806C72)             // Placeholder / disabled
val TextOnRed = Color(0xFFFFFFFF)             // Text on crimson buttons
val TextOnDark = Color(0xFFF8EEF0)            // General dark UI

// Text aliases
val PrimaryText = TextPrimary
val SecondaryText = TextSecondary
val MutedText = TextMuted

// ── 5. Borders ──────────────────────────────────────────────────────────────
val BorderSubtle = Color(0xFF281317)          // Normal cards
val BorderDefault = Color(0xFF3A1A21)         // Cards / inputs
val BorderStrong = Color(0xFF54202B)          // Focused elements
val BorderCrimson = Color(0xFF8F1429)         // Selected / important
val BorderActive = Color(0xFFB51E35)          // Active state

// Border aliases
val Borders = BorderDefault
val StrongBorder = BorderStrong

// ── 6. Buttons ──────────────────────────────────────────────────────────────
val ButtonPrimaryBg = Red500                   // #B51E35
val ButtonPrimaryText = TextOnRed              // #FFFFFF
val ButtonPrimaryPressed = CrimsonRose         // #8F1429
val ButtonPrimaryHover = Red400                // #D12642

val ButtonSecondaryBg = DeepWine               // #2A0810
val ButtonSecondaryBorder = BorderStrong       // #54202B
val ButtonSecondaryText = TextSecondary        // #E4D7DA
val ButtonSecondaryPressed = Burgundy          // #480B17

val ButtonDestructiveBg = BloodWine            // #650D1B
val ButtonDestructiveText = TextOnRed          // #FFFFFF
val ButtonDestructivePressed = Burgundy        // #480B17

// ── 7. Journaling Vault ─────────────────────────────────────────────────────
val VaultBackground = Color(0xFF070405)        // Deepest intimate background
val VaultSurface = Color(0xFF110608)           // Vault card surface
val VaultCard = Color(0xFF19090D)              // Vault card container
val VaultElevated = Color(0xFF230C12)          // Elevated modal / dialogs
val VaultAccent = Red600                       // #94152A — FAB & pinned bookmarks
val VaultActive = Red400                       // #D12642 — security/lock indicators
val VaultText = TextPrimary                    // #FFF7F8 — primary vault text
val VaultSecondary = Color(0xFFD5C3C7)         // Secondary vault body text
val VaultMuted = TextMuted                     // #806C72 — metadata / muted
val VaultBorder = BorderSubtle                 // #281317 — vault card borders

// ── 8. Note Card Specific Colors ────────────────────────────────────────────
val NoteBackground = Surface                   // #14090C
val NoteHover = SurfaceHover                   // #251016
val NoteSelected = SurfaceElevated             // #1C0C10
val NoteBorder = BorderSubtle                  // #281317
val NoteTitleText = TextPrimary                // #FFF7F8
val NoteBodyText = TextSecondary               // #E4D7DA
val NoteTimestampText = TextMuted              // #806C72
val NoteTagBackground = DeepWine               // #2A0810
val NoteTagText = SoftRose                     // #F06A7D

// ── 9. Semantic Colors ──────────────────────────────────────────────────────
val SemanticSuccess = Color(0xFF55A878)
val SemanticWarning = Color(0xFFD39A45)
val SemanticError = Color(0xFFD12642)
val SemanticInfo = Color(0xFF718EAD)
val SemanticNeutral = TextMuted                // #806C72

// ── 10. Glow Effects & Gradients ────────────────────────────────────────────
val GlowGeneral = PrimaryCrimson.copy(alpha = 0.18f)
val GlowStrong = BrightCrimson.copy(alpha = 0.28f)
val GlowAmbient = PrimaryCrimson.copy(alpha = 0.08f)
val AmbientGlow = PrimaryCrimson.copy(alpha = 0.08f)  // Background subtle radial depth
val FabGlow = PrimaryCrimson.copy(alpha = 0.35f)

// Gradients
val CrimsonGradient = listOf(Red700, Red500)                 // #720F20 -> #B51E35
val BloodGradient = listOf(Red900, CrimsonRose)              // #3A0710 -> #8F1429
val RubyGradient = listOf(Red600, Red400)                    // #94152A -> #D12642
val DarkAtmosphericGradient = listOf(Background, BlackRose)  // #080506 -> #170609
val JournalEntryGradient = listOf(Color(0xFF1A080D), Color(0xFF2A0B12)) // #1A080D -> #2A0B12