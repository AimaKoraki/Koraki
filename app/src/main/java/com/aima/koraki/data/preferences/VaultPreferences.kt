package com.aima.koraki.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Extension property to obtain the DataStore instance on a [Context]. */
private val Context.vaultDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "vault_preferences",
)

/**
 * Wraps Jetpack DataStore to persist vault passcode, user prefs, XP progression, and active companion.
 *
 * Default passcode: `#koraki`
 * The stored passcode is never logged or surfaced in public UI.
 */
@Singleton
class VaultPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private val KEY_VAULT_CODE = stringPreferencesKey("vault_code")
        private val KEY_VAULT_HINT = stringPreferencesKey("vault_hint")
        private val KEY_USERNAME = stringPreferencesKey("username")
        private val KEY_ACTIVE_SHIMEJI = stringPreferencesKey("active_shimeji_name")
        private val KEY_FINGERPRINT_ENABLED = booleanPreferencesKey("fingerprint_enabled")
        private val KEY_ANNIVERSARY_DATE = longPreferencesKey("anniversary_date")
        // App-level authentication
        private val KEY_APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
        private val KEY_APP_PIN_HASH = stringPreferencesKey("app_pin_hash")
        // XP & Gamification Leveling
        private val KEY_TOTAL_XP = intPreferencesKey("total_xp")
        private val KEY_LAST_PET_EPOCH_DAY = longPreferencesKey("last_pet_epoch_day")
        private val KEY_PET_COUNT_TODAY = intPreferencesKey("pet_count_today")
        private val KEY_LAST_JOURNAL_EPOCH_DAY = longPreferencesKey("last_journal_epoch_day")
        private val KEY_STREAK_COUNT = intPreferencesKey("streak_count")
        const val DEFAULT_VAULT_CODE = "#koraki"
    }

    /** A [Flow] that emits the current vault passcode. Falls back to [DEFAULT_VAULT_CODE]. */
    val getVaultCode: Flow<String> = context.vaultDataStore.data.map { prefs ->
        prefs[KEY_VAULT_CODE] ?: DEFAULT_VAULT_CODE
    }

    val getVaultHint: Flow<String> = context.vaultDataStore.data.map { prefs ->
        prefs[KEY_VAULT_HINT] ?: ""
    }

    val getUsername: Flow<String> = context.vaultDataStore.data.map { prefs ->
        prefs[KEY_USERNAME] ?: "User"
    }

    /** A [Flow] emitting the name of the active Shimeji companion, or null if none is set. */
    fun getActiveShimeji(): Flow<String?> = context.vaultDataStore.data.map { prefs ->
        prefs[KEY_ACTIVE_SHIMEJI]
    }

    val isFingerprintEnabled: Flow<Boolean> = context.vaultDataStore.data.map { prefs ->
        prefs[KEY_FINGERPRINT_ENABLED] ?: false
    }

    /** A [Flow] emitting the anniversary date timestamp (ms), or null if not set. */
    val getAnniversaryDate: Flow<Long?> = context.vaultDataStore.data.map { prefs ->
        prefs[KEY_ANNIVERSARY_DATE]
    }

    /** Persists the anniversary start date timestamp (in milliseconds). Pass null to clear. */
    suspend fun setAnniversaryDate(timestamp: Long?) {
        context.vaultDataStore.edit { prefs ->
            if (timestamp == null) prefs.remove(KEY_ANNIVERSARY_DATE)
            else prefs[KEY_ANNIVERSARY_DATE] = timestamp
        }
    }

    /** Persists a new vault passcode. */
    suspend fun setVaultCode(code: String) {
        context.vaultDataStore.edit { prefs ->
            prefs[KEY_VAULT_CODE] = code
        }
    }

    suspend fun setVaultHint(hint: String) {
        context.vaultDataStore.edit { prefs ->
            prefs[KEY_VAULT_HINT] = hint
        }
    }

    suspend fun setUsername(username: String) {
        context.vaultDataStore.edit { prefs ->
            prefs[KEY_USERNAME] = username
        }
    }

    /** Persists the active Shimeji companion name. Pass null to clear (deactivate). */
    suspend fun setActiveShimeji(name: String?) {
        context.vaultDataStore.edit { prefs ->
            if (name == null) prefs.remove(KEY_ACTIVE_SHIMEJI)
            else prefs[KEY_ACTIVE_SHIMEJI] = name
        }
    }

    suspend fun setFingerprintEnabled(enabled: Boolean) {
        context.vaultDataStore.edit { prefs ->
            prefs[KEY_FINGERPRINT_ENABLED] = enabled
        }
    }

    // ── App-level lock ────────────────────────────────────────────────────────

    val isAppLockEnabled: Flow<Boolean> = context.vaultDataStore.data.map { prefs ->
        prefs[KEY_APP_LOCK_ENABLED] ?: false
    }

    /** Raw stored hash (empty string if no PIN is set). */
    val getAppPinHash: Flow<String> = context.vaultDataStore.data.map { prefs ->
        prefs[KEY_APP_PIN_HASH] ?: ""
    }

    suspend fun setAppLockEnabled(enabled: Boolean) {
        context.vaultDataStore.edit { prefs ->
            prefs[KEY_APP_LOCK_ENABLED] = enabled
        }
    }

    /**
     * Hashes [pin] with SHA-256 and stores the result.
     * The raw PIN is never persisted.
     */
    suspend fun setAppPin(pin: String) {
        val hash = sha256(pin)
        context.vaultDataStore.edit { prefs ->
            prefs[KEY_APP_PIN_HASH] = hash
        }
    }

    /** Clears the stored PIN hash, effectively removing the PIN. */
    suspend fun clearAppPin() {
        context.vaultDataStore.edit { prefs ->
            prefs[KEY_APP_PIN_HASH] = ""
        }
    }

    /** Returns true if [input] matches the stored PIN hash. */
    fun verifyPin(input: String, storedHash: String): Boolean =
        storedHash.isNotEmpty() && sha256(input) == storedHash

    private fun sha256(input: String): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    // ── Leveling & XP Mechanics ────────────────────────────────────────────────

    val getTotalXp: Flow<Int> = context.vaultDataStore.data.map { prefs ->
        prefs[KEY_TOTAL_XP] ?: 0
    }

    val getStreakCount: Flow<Int> = context.vaultDataStore.data.map { prefs ->
        prefs[KEY_STREAK_COUNT] ?: 0
    }

    /** Adds [amount] to total XP and returns the updated total XP. */
    suspend fun addXp(amount: Int): Int {
        var newTotal = 0
        context.vaultDataStore.edit { prefs ->
            val current = prefs[KEY_TOTAL_XP] ?: 0
            newTotal = current + amount
            prefs[KEY_TOTAL_XP] = newTotal
        }
        return newTotal
    }

    /**
     * Records a petting tap on a companion.
     * Resets daily tap counter at midnight (`LocalDate.now().toEpochDay()`).
     * Cap: Max 10 taps / 50 XP per day (+5 XP per tap).
     * Returns XP awarded (5 if under cap, 0 if cap reached).
     */
    suspend fun recordPettingTap(): Int {
        val todayEpoch = java.time.LocalDate.now(java.time.ZoneId.systemDefault()).toEpochDay()
        var xpAwarded = 0

        context.vaultDataStore.edit { prefs ->
            val lastDay = prefs[KEY_LAST_PET_EPOCH_DAY] ?: -1L
            var petCount = prefs[KEY_PET_COUNT_TODAY] ?: 0

            if (lastDay != todayEpoch) {
                prefs[KEY_LAST_PET_EPOCH_DAY] = todayEpoch
                petCount = 0
            }

            if (petCount < 10) {
                petCount++
                prefs[KEY_PET_COUNT_TODAY] = petCount
                xpAwarded = 5
                val currentXp = prefs[KEY_TOTAL_XP] ?: 0
                prefs[KEY_TOTAL_XP] = currentXp + 5
            }
        }
        return xpAwarded
    }

    /**
     * Evaluates daily journaling streak upon saving a new entry.
     * Awards +30 XP once per calendar day.
     * Returns true if streak bonus (+30 XP) was awarded today.
     */
    suspend fun checkAndRecordDailyStreak(): Boolean {
        val todayEpoch = java.time.LocalDate.now(java.time.ZoneId.systemDefault()).toEpochDay()
        var streakAwarded = false

        context.vaultDataStore.edit { prefs ->
            val lastJournalDay = prefs[KEY_LAST_JOURNAL_EPOCH_DAY] ?: -1L
            var streak = prefs[KEY_STREAK_COUNT] ?: 0

            if (lastJournalDay == todayEpoch) {
                // Already journaled today — streak already awarded today
                streakAwarded = false
            } else {
                if (lastJournalDay == todayEpoch - 1) {
                    streak++
                } else {
                    streak = 1
                }
                prefs[KEY_LAST_JOURNAL_EPOCH_DAY] = todayEpoch
                prefs[KEY_STREAK_COUNT] = streak
                streakAwarded = true

                val currentXp = prefs[KEY_TOTAL_XP] ?: 0
                prefs[KEY_TOTAL_XP] = currentXp + 30
            }
        }
        return streakAwarded
    }
}

