package com.telen.namebattle.data.remote

import android.content.Context
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber

/** Meaning + etymology for a first name, as served by the remote catalog. */
data class MeaningInfo(val meaning: String, val origin: String?)

/**
 * Source of first-name meanings + origins.
 */
class MeaningRemoteDataSource(private val context: Context) {

    private val mutex = Mutex()

    @Volatile
    private var catalog: Map<String, MeaningInfo>? = null

    /** Returns meaning + origin for a raw (uppercase) name, or null if unknown. */
    suspend fun meaningFor(nameRaw: String): MeaningInfo? = loadCatalog()[nameRaw.uppercase()]

    /** Set of raw (uppercase) names for which a meaning exists — used to flag rows in lists. */
    suspend fun availableNames(): Set<String> = loadCatalog().keys

    private suspend fun loadCatalog(): Map<String, MeaningInfo> {
        catalog?.let { return it }
        return mutex.withLock {
            catalog ?: buildCatalog().also { catalog = it }
        }
    }

    private fun buildCatalog(): Map<String, MeaningInfo> = runCatching {
        // TODO(firebase): replace this asset read with a Firebase Storage download.
        emptyMap<String, MeaningInfo>()
    }.getOrElse {
        Timber.d("MeaningRemoteDataSource: no catalog available (Firebase not configured); meanings empty")
        emptyMap()
    }
}
