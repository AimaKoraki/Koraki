# Shimeji (Virtual Companion) Feature Reference

This document provides a comprehensive guide and code reference for recreating the Shimeji (walking virtual companion) feature in a Jetpack Compose Android application.

## 1. Models

These data classes represent the companion data and the animation configuration required to render them.

### `Companion.kt`
Defines the structure of the companion data, usually loaded from a JSON file.

```kotlin
package com.aima.habitual.model

import com.google.gson.annotations.SerializedName

/**
 * A virtual companion that can be loaded from JSON and shown in the app.
 */
data class VirtualCompanion(
    @SerializedName("name") val name: String,
    @SerializedName("species") val species: String,
    @SerializedName("requiredLevel") val requiredLevel: Int = 1,
    @SerializedName("spriteAsset") val spriteAsset: String,
    val unlockStatus: Boolean = false,
    @SerializedName("birthYear") val birthYear: Int? = null,
    @SerializedName("favFoods") val favoriteFoods: List<String>? = null
)

data class CompanionListResponse(
    @SerializedName("companions") val companions: List<VirtualCompanion>
)
```

### `CompanionMetadata.kt`
Defines the animation timings and frame sequences for a specific companion. This allows different companions to have different behaviors (e.g., walking speeds, idle frequencies).

```kotlin
package com.aima.habitual.model

import com.google.gson.annotations.SerializedName

/**
 * Animation and behavior settings for a companion sprite.
 */
data class CompanionMetadata(
    @SerializedName("id") val id: String = "",
    @SerializedName("walkFrames") val walkFrames: List<String> = listOf("walk_0.png", "walk_1.png"),
    @SerializedName("turnFrames") val turnFrames: List<String> = emptyList(),
    @SerializedName("idleFrames") val idleFrames: List<String> = listOf("idle_0.png"),
    @SerializedName("walkFrameDurationMs") val walkFrameDurationMs: Long = 200L,
    @SerializedName("idleFrameDurationMs") val idleFrameDurationMs: Long = 150L,
    @SerializedName("idleChanceOnEdge") val idleChanceOnEdge: Float = 0.45f,
    @SerializedName("idleMinMs") val idleMinMs: Long = 800L,
    @SerializedName("idleMaxMs") val idleMaxMs: Long = 3500L,
    @SerializedName("walkHoldOnLastFrame") val walkHoldOnLastFrame: Boolean = false
)
```

## 2. Data Layer

### `LocalCompanionRepository.kt`
Reads the master list of companions from a bundled JSON asset (`assets/companions/companions_list.json`).

```kotlin
package com.aima.habitual.data

import android.content.Context
import android.util.Log
import com.aima.habitual.model.CompanionListResponse
import com.aima.habitual.model.VirtualCompanion
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalCompanionRepository(private val context: Context) {
    suspend fun loadCompanions(): List<VirtualCompanion> = withContext(Dispatchers.IO) {
        try {
            context.assets.open("companions/companions_list.json").use { stream ->
                val text = stream.bufferedReader().readText()
                Gson().fromJson(text, CompanionListResponse::class.java).companions
            }
        } catch (e: Exception) {
            Log.e("LocalCompanionRepository", "Failed to load companions_list.json", e)
            emptyList()
        }
    }
}
```

## 3. ViewModel

### `CompanionViewModel.kt`
Manages which companions the user has unlocked based on their level, and tracks which companion is currently active (persisting the choice in `SharedPreferences`).

```kotlin
package com.aima.habitual.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aima.habitual.data.LocalCompanionRepository
import com.aima.habitual.model.VirtualCompanion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant

class CompanionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LocalCompanionRepository(application)
    private val prefs = application.getSharedPreferences("habitual_prefs", Context.MODE_PRIVATE)

    private val _companions = MutableStateFlow<List<VirtualCompanion>>(emptyList())
    val companions: StateFlow<List<VirtualCompanion>> = _companions.asStateFlow()

    private val _activeCompanionName = MutableStateFlow(prefs.getString(KEY_ACTIVE, null))
    val activeCompanionName: StateFlow<String?> = _activeCompanionName.asStateFlow()

    private var lastUserLevel: Int = 0

    init {
        viewModelScope.launch {
            val raw = repository.loadCompanions()
            _companions.value = raw.map { it.copy(unlockStatus = lastUserLevel >= it.requiredLevel) }
        }
    }

    fun onUserLevelChanged(userLevel: Int) {
        if (userLevel == lastUserLevel && _companions.value.isNotEmpty()) return
        lastUserLevel = userLevel
        _companions.update { current ->
            current.map { c ->
                val unlocked = userLevel >= c.requiredLevel
                if (unlocked && prefs.getString(unlockKey(c.name), null) == null) {
                    prefs.edit().putString(unlockKey(c.name), Instant.now().toString()).apply()
                }
                c.copy(unlockStatus = unlocked)
            }
        }
        val active = _activeCompanionName.value
        if (active != null && _companions.value.firstOrNull { it.name == active }?.unlockStatus != true) {
            setActive(null)
        }
    }

    fun setActive(name: String?) {
        if (name != null) {
            val target = _companions.value.firstOrNull { it.name == name }
            if (target == null || !target.unlockStatus) return
        }
        prefs.edit().apply {
            if (name == null) remove(KEY_ACTIVE) else putString(KEY_ACTIVE, name)
        }.apply()
        _activeCompanionName.value = name
    }

    private fun unlockKey(name: String) = "companion_unlocked_$name"

    companion object {
        private const val KEY_ACTIVE = "active_companion_name"
    }
}
```

## 4. UI Components

### `ShimejiOverlay.kt`
The core component responsible for the animation and movement logic of the companion. It sits on top of other content, moving back and forth.

```kotlin
package com.aima.habitual.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.aima.habitual.model.CompanionMetadata
import com.aima.habitual.model.VirtualCompanion
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.random.Random

@Composable
fun ShimejiOverlay(
    activeCompanion: VirtualCompanion?,
    modifier: Modifier = Modifier
) {
    val spriteSize = 48.dp
    val bandHeight = 56.dp
    val walkSpeedDpPerSec = 60f
    val tickMs = 16L

    if (activeCompanion == null) return

    val context = LocalContext.current
    val density = LocalDensity.current
    val spriteSizePx = with(density) { spriteSize.toPx() }
    val walkSpeedPxPerMs = with(density) { walkSpeedDpPerSec.dp.toPx() } / 1000f

    var containerWidthPx by remember { mutableFloatStateOf(0f) }
    var xPx by remember { mutableFloatStateOf(0f) }
    var facingRight by remember { mutableStateOf(false) }
    var currentFramePath by remember {
        mutableStateOf("companions/${activeCompanion.spriteAsset}/walk_0.png")
    }

    val bitmap: ImageBitmap? = remember(currentFramePath) {
        runCatching {
            context.assets.open(currentFramePath).use {
                BitmapFactory.decodeStream(it).asImageBitmap()
            }
        }.getOrNull()
    }

    LaunchedEffect(activeCompanion.spriteAsset) {
        val meta: CompanionMetadata = withContext(Dispatchers.IO) {
            runCatching {
                context.assets.open("companions/${activeCompanion.spriteAsset}/metadata.json")
                    .use { Gson().fromJson(it.bufferedReader(), CompanionMetadata::class.java) }
            }.getOrElse { CompanionMetadata() }
        }

        val base = "companions/${activeCompanion.spriteAsset}"
        fun walkPath(frame: String) = "$base/$frame"

        suspend fun playTurnFrames() {
            for (frame in meta.turnFrames) {
                currentFramePath = walkPath(frame)
                delay(meta.walkFrameDurationMs)
            }
        }

        suspend fun playIdleLoop() {
            val idleDuration = Random.nextLong(meta.idleMinMs, meta.idleMaxMs)
            val idleEnd = System.currentTimeMillis() + idleDuration
            var idx = 0
            while (System.currentTimeMillis() < idleEnd) {
                currentFramePath = walkPath(meta.idleFrames[idx % meta.idleFrames.size])
                delay(meta.idleFrameDurationMs)
                idx++
            }
        }

        currentFramePath = walkPath(meta.walkFrames[0])
        var walkFrameIdx = 0
        var walkFrameTimer = 0L

        while (true) {
            delay(tickMs)
            val travel = (containerWidthPx - spriteSizePx).coerceAtLeast(0f)
            if (travel == 0f) continue

            val step = walkSpeedPxPerMs * tickMs
            xPx = (xPx + (if (facingRight) step else -step)).coerceIn(0f, travel)

            walkFrameTimer += tickMs
            if (walkFrameTimer >= meta.walkFrameDurationMs) {
                walkFrameTimer = 0L
                walkFrameIdx = if (meta.walkHoldOnLastFrame) {
                    (walkFrameIdx + 1).coerceAtMost(meta.walkFrames.size - 1)
                } else {
                    (walkFrameIdx + 1) % meta.walkFrames.size
                }
                currentFramePath = walkPath(meta.walkFrames[walkFrameIdx])
            }

            if (xPx <= 0f || xPx >= travel) {
                if (meta.turnFrames.isNotEmpty()) playTurnFrames()
                facingRight = !facingRight
                if (meta.idleFrames.isNotEmpty() && Random.nextFloat() < meta.idleChanceOnEdge) {
                    playIdleLoop()
                }
                walkFrameIdx = 0
                walkFrameTimer = 0L
                currentFramePath = walkPath(meta.walkFrames[0])
            }
        }
    }

    if (bitmap == null) return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(bandHeight)
            .onSizeChanged { containerWidthPx = it.width.toFloat() }
    ) {
        Image(
            bitmap = bitmap,
            contentDescription = null,
            modifier = Modifier
                .size(spriteSize)
                .offset { IntOffset(xPx.toInt(), 0) }
                .graphicsLayer {
                    scaleX = if (facingRight) -1f else 1f
                }
        )
    }
}
```

### Main Scaffold Integration
To display the Shimeji across the app, overlay it at the bottom of your root `Scaffold` content block, directly above the bottom navigation bar.

```kotlin
// In your MainScreen.kt or root container:
val companionViewModel: CompanionViewModel = viewModel()
val activeName by companionViewModel.activeCompanionName.collectAsState()
val companions by companionViewModel.companions.collectAsState()
val activeCompanion = companions.firstOrNull { it.name == activeName && it.unlockStatus }

Scaffold(
    bottomBar = { BottomNavigationBar() }
) { innerPadding ->
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        // Your main NavHost or screen content goes here
        
        // Render the ShimejiOverlay pinned to the bottom of the screen
        ShimejiOverlay(
            activeCompanion = activeCompanion,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
```

## 5. Companion Selection Screens

These screens are used to browse and select the active companion.

### `CompanionsScreen.kt` (Snippet)
Displays a grid of all companions. Locked companions are rendered in grayscale using `ColorFilter`.

```kotlin
// ... inside a LazyVerticalGrid item:
val lockedAlpha = 0.6f
val grayscaleFilter = remember {
    ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
}

Image(
    painter = painter,
    contentDescription = "${companion.name} sprite",
    colorFilter = if (!companion.unlockStatus) grayscaleFilter else null,
    modifier = Modifier
        .size(96.dp)
        .alpha(if (companion.unlockStatus) 1f else lockedAlpha)
)
```

### `CompanionDetailScreen.kt` (Snippet)
Allows the user to select the companion as active via a Switch component.

```kotlin
// ... inside the detail screen:
val isActive = companion.name == activeName

Switch(
    checked = isActive,
    onCheckedChange = { checked ->
        companionViewModel.setActive(if (checked) companion.name else null)
    }
)
```
