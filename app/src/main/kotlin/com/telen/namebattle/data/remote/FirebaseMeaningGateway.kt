package com.telen.namebattle.data.remote

import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await
import timber.log.Timber

private const val KEY_VERSION = "meanings_version"
private const val KEY_CHECKSUM = "meanings_checksum"
private const val KEY_STORAGE_PATH = "meanings_storage_path"
private const val FETCH_INTERVAL_SECONDS = 3600L
private const val MAX_DOWNLOAD_BYTES = 5 * 1024 * 1024L // 5 MB

class FirebaseMeaningConfigGateway : MeaningConfigGateway {

    init {
        Firebase.remoteConfig.setConfigSettingsAsync(
            remoteConfigSettings { minimumFetchIntervalInSeconds = FETCH_INTERVAL_SECONDS }
        )
    }

    override suspend fun fetchMeta(): MeaningMeta? = runCatching {
        val config = Firebase.remoteConfig
        config.fetchAndActivate().await()

        val version = config.getLong(KEY_VERSION).toInt()
        val checksum = config.getString(KEY_CHECKSUM)
        val path = config.getString(KEY_STORAGE_PATH)

        if (version == 0 || checksum.isEmpty() || path.isEmpty()) {
            Timber.d("MeaningConfigGateway: Remote Config not yet populated")
            return@runCatching null
        }
        MeaningMeta(version = version, checksum = checksum, storagePath = path)
    }.getOrElse { e ->
        Timber.e(e, "MeaningConfigGateway: failed to fetch remote config")
        null
    }
}

class FirebaseMeaningStorageGateway : MeaningStorageGateway {

    override suspend fun download(storagePath: String): ByteArray =
        Firebase.storage.reference.child(storagePath).getBytes(MAX_DOWNLOAD_BYTES).await()
}
