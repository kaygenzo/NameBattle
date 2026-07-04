package com.telen.namebattle.data.remote

import com.telen.namebattle.data.local.AppPreferences
import com.telen.namebattle.data.local.dao.FirstNameDao
import com.telen.namebattle.data.local.entity.MeaningUpdate
import com.telen.namebattle.util.sha256Hex
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class MeaningRemoteDataSourceTest {

    private lateinit var prefs: AppPreferences
    private lateinit var configGateway: MeaningConfigGateway
    private lateinit var storageGateway: MeaningStorageGateway
    private lateinit var dao: FirstNameDao
    private lateinit var source: MeaningRemoteDataSource

    private val validJson = """
        [
          {"name_raw": "Emma", "meaning": "Universal", "origin": "Germanic"},
          {"name_raw": "Liam", "meaning": "Strong-willed warrior", "origin": "Irish"},
          {"name_raw": "Noname", "meaning": "Without origin"}
        ]
    """.trimIndent()

    @Before
    fun setUp() {
        prefs = mockk()
        configGateway = mockk()
        storageGateway = mockk()
        dao = mockk(relaxed = true)

        source = MeaningRemoteDataSource(
            prefs = prefs,
            configGateway = configGateway,
            storageGateway = storageGateway,
            dao = dao,
        )
    }

    // ── no remote config ──────────────────────────────────────────────────────

    @Test
    fun `sync does nothing when fetchMeta returns null`() = runTest {
        coEvery { configGateway.fetchMeta() } returns null

        source.sync()

        coVerify(exactly = 0) { storageGateway.download(any()) }
        coVerify(exactly = 0) { dao.updateMeaningsBatch(any()) }
    }

    // ── version already up-to-date ────────────────────────────────────────────

    @Test
    fun `sync does nothing when version and checksum already match`() = runTest {
        val bytes = validJson.toByteArray()
        val meta = MeaningMeta(version = 3, checksum = bytes.sha256Hex(), storagePath = "meanings/v3.json")

        coEvery { configGateway.fetchMeta() } returns meta
        coEvery { prefs.getMeaningsVersion() } returns 3
        coEvery { prefs.getMeaningsChecksum() } returns bytes.sha256Hex()

        source.sync()

        coVerify(exactly = 0) { storageGateway.download(any()) }
        coVerify(exactly = 0) { dao.updateMeaningsBatch(any()) }
    }

    // ── download and apply ────────────────────────────────────────────────────

    @Test
    fun `sync downloads and applies catalog when version differs`() = runTest {
        val bytes = validJson.toByteArray()
        val meta = MeaningMeta(version = 4, checksum = bytes.sha256Hex(), storagePath = "meanings/v4.json")

        coEvery { configGateway.fetchMeta() } returns meta
        coEvery { prefs.getMeaningsVersion() } returns 3
        coEvery { prefs.getMeaningsChecksum() } returns "old_checksum"
        coEvery { storageGateway.download("meanings/v4.json") } returns bytes

        source.sync()

        coVerify(exactly = 1) { storageGateway.download("meanings/v4.json") }
        coVerify(exactly = 1) { dao.updateMeaningsBatch(any()) }
    }

    @Test
    fun `sync passes correct entries to dao`() = runTest {
        val bytes = validJson.toByteArray()
        val meta = MeaningMeta(version = 2, checksum = bytes.sha256Hex(), storagePath = "meanings/v2.json")

        coEvery { configGateway.fetchMeta() } returns meta
        coEvery { prefs.getMeaningsVersion() } returns 0
        coEvery { prefs.getMeaningsChecksum() } returns ""
        coEvery { storageGateway.download(any()) } returns bytes

        val captured = mutableListOf<List<MeaningUpdate>>()
        coEvery { dao.updateMeaningsBatch(capture(captured)) } returns Unit

        source.sync()

        val entries = captured.single()
        assert(entries.any { it.nameRaw == "Emma" && it.meaning == "Universal" && it.origin == "Germanic" })
        assert(entries.any { it.nameRaw == "Liam" && it.meaning == "Strong-willed warrior" && it.origin == "Irish" })
        assert(entries.any { it.nameRaw == "Noname" && it.meaning == "Without origin" && it.origin == null })
    }

    @Test
    fun `sync saves meta to prefs after successful apply`() = runTest {
        val bytes = validJson.toByteArray()
        val checksum = bytes.sha256Hex()
        val meta = MeaningMeta(version = 5, checksum = checksum, storagePath = "meanings/v5.json")

        coEvery { configGateway.fetchMeta() } returns meta
        coEvery { prefs.getMeaningsVersion() } returns 0
        coEvery { prefs.getMeaningsChecksum() } returns ""
        coEvery { storageGateway.download(any()) } returns bytes

        source.sync()

        coVerify(exactly = 1) { prefs.saveMeaningsMeta(version = 5, checksum = checksum) }
    }

    // ── checksum mismatch ─────────────────────────────────────────────────────

    @Test
    fun `sync does not update DB when checksum does not match`() = runTest {
        val bytes = validJson.toByteArray()
        val meta = MeaningMeta(version = 5, checksum = "wrong_checksum", storagePath = "meanings/v5.json")

        coEvery { configGateway.fetchMeta() } returns meta
        coEvery { prefs.getMeaningsVersion() } returns 0
        coEvery { prefs.getMeaningsChecksum() } returns ""
        coEvery { storageGateway.download(any()) } returns bytes

        source.sync()

        coVerify(exactly = 0) { dao.updateMeaningsBatch(any()) }
        coVerify(exactly = 0) { prefs.saveMeaningsMeta(any(), any()) }
    }

    // ── error resilience ──────────────────────────────────────────────────────

    @Test
    fun `sync does not throw when download throws`() = runTest {
        val meta = MeaningMeta(version = 1, checksum = "abc", storagePath = "meanings/v1.json")

        coEvery { configGateway.fetchMeta() } returns meta
        coEvery { prefs.getMeaningsVersion() } returns 0
        coEvery { prefs.getMeaningsChecksum() } returns ""
        coEvery { storageGateway.download(any()) } throws RuntimeException("network error")

        source.sync() // must not throw

        coVerify(exactly = 0) { dao.updateMeaningsBatch(any()) }
    }
}
