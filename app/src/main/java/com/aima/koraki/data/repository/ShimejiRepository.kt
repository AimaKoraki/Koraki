package com.aima.koraki.data.repository

import android.content.Context
import android.util.Log
import com.aima.koraki.data.model.CompanionListResponse
import com.aima.koraki.data.model.VirtualCompanion
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads the master list of virtual companions from the bundled asset
 * `assets/companions/companions_list.json`.
 */
@Singleton
class ShimejiRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    suspend fun loadVirtualCompanions(): List<VirtualCompanion> = withContext(Dispatchers.IO) {
        runCatching {
            context.assets.open("companions/companions_list.json").use { stream ->
                Gson().fromJson(stream.bufferedReader(), CompanionListResponse::class.java).companions
            }
        }.getOrElse { e ->
            Log.e("ShimejiRepository", "Failed to load companions_list.json", e)
            emptyList()
        }
    }
}
