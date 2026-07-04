package com.telen.namebattle.data.remote

data class MeaningMeta(
    val version: Int,
    val checksum: String,
    val storagePath: String,
)

interface MeaningConfigGateway {
    /** Fetches the latest catalog metadata from Remote Config. Returns null on failure or if unconfigured. */
    suspend fun fetchMeta(): MeaningMeta?
}

interface MeaningStorageGateway {
    /** Downloads raw bytes from Firebase Storage at [storagePath]. Throws on failure. */
    suspend fun download(storagePath: String): ByteArray
}
