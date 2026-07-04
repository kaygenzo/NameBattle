package com.telen.namebattle.data.remote

import com.telen.namebattle.data.local.AppPreferences
import com.telen.namebattle.data.local.dao.FirstNameDao
import com.telen.namebattle.data.local.entity.MeaningUpdate
import com.telen.namebattle.util.sha256Hex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import timber.log.Timber

@Serializable
private data class MeaningEntry(
    @SerialName("name_raw") val nameRaw: String,
    val meaning: String,
    val origin: String? = null,
)

private val jsonParser = Json { ignoreUnknownKeys = true }

class MeaningRemoteDataSource(
    private val prefs: AppPreferences,
    private val configGateway: MeaningConfigGateway,
    private val storageGateway: MeaningStorageGateway,
    private val dao: FirstNameDao,
) {
    /** Fetches remote config + downloads and applies catalog to DB if a new version is available. */
    suspend fun sync() {
        runCatching { doSync() }.onFailure { Timber.e(it, "Meanings: sync failed") }
    }

    private suspend fun doSync() {
        val meta = configGateway.fetchMeta() ?: return

        val storedVersion = prefs.getMeaningsVersion()
        val storedChecksum = prefs.getMeaningsChecksum()

        if (meta.version == storedVersion && meta.checksum == storedChecksum) {
            Timber.d("Meanings: catalog up-to-date (v${meta.version}), nothing to do")
            return
        }

        Timber.d("Meanings: downloading catalog v${meta.version} from ${meta.storagePath}")
        val bytes = storageGateway.download(meta.storagePath)

        val actualChecksum = bytes.sha256Hex()
        if (actualChecksum != meta.checksum) {
            Timber.w("Meanings: checksum mismatch — expected ${meta.checksum}, got $actualChecksum")
            return
        }

        val updates = withContext(Dispatchers.Default) {
            parseEntries(bytes).map { MeaningUpdate(it.nameRaw, it.meaning, it.origin) }
        }
        dao.updateMeaningsBatch(updates)
        prefs.saveMeaningsMeta(version = meta.version, checksum = meta.checksum)
        Timber.d("Meanings: catalog v${meta.version} applied (${updates.size} entries)")
    }

    private fun parseEntries(bytes: ByteArray): List<MeaningEntry> =
        jsonParser.decodeFromString(bytes.decodeToString())
}
