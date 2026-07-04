package com.telen.namebattle.data.local

import android.content.res.AssetManager
import android.content.Context
import com.telen.namebattle.data.local.dao.FirstNameDao
import com.telen.namebattle.data.local.entity.MeaningUpdate
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.FileNotFoundException

class DatabaseSeederTest {

    private lateinit var context: Context
    private lateinit var assetManager: AssetManager
    private lateinit var dao: FirstNameDao
    private lateinit var prefs: AppPreferences
    private lateinit var seeder: DatabaseSeeder

    private val meaningsJson = """
        [
          {"name_raw": "Emma", "meaning": "Universal", "origin": "Germanic"},
          {"name_raw": "Liam", "meaning": "Strong-willed warrior", "origin": "Irish"},
          {"name_raw": "Noname", "meaning": "Without origin"}
        ]
    """.trimIndent()

    @Before
    fun setUp() {
        assetManager = mockk()
        context = mockk { every { assets } returns assetManager }
        dao = mockk(relaxed = true)
        prefs = mockk()
        seeder = DatabaseSeeder(context, dao, prefs)

        // Default: names already seeded so prenoms_insee.json is not opened
        coEvery { prefs.isDbSeeded() } returns true
        coEvery { dao.count() } returns 100
    }

    // ── meanings already seeded ───────────────────────────────────────────────

    @Test
    fun `seedIfNeeded skips meanings when already seeded`() = runTest {
        coEvery { prefs.isMeaningsSeeded() } returns true

        seeder.seedIfNeeded()

        coVerify(exactly = 0) { dao.updateMeaningsBatch(any()) }
        coVerify(exactly = 0) { prefs.markMeaningsSeeded() }
    }

    // ── meanings seeded from asset ────────────────────────────────────────────

    @Test
    fun `seedIfNeeded seeds meanings from asset when not yet seeded`() = runTest {
        coEvery { prefs.isMeaningsSeeded() } returns false
        every { assetManager.open("meanings.json") } returns meaningsJson.toByteArray().inputStream()

        seeder.seedIfNeeded()

        coVerify(exactly = 1) { dao.updateMeaningsBatch(any()) }
        coVerify(exactly = 1) { prefs.markMeaningsSeeded() }
    }

    @Test
    fun `seedIfNeeded passes correct entries to dao`() = runTest {
        coEvery { prefs.isMeaningsSeeded() } returns false
        every { assetManager.open("meanings.json") } returns meaningsJson.toByteArray().inputStream()

        val captured = mutableListOf<List<MeaningUpdate>>()
        coEvery { dao.updateMeaningsBatch(capture(captured)) } returns Unit

        seeder.seedIfNeeded()

        val entries = captured.flatten()
        assert(entries.any { it.nameRaw == "Emma" && it.meaning == "Universal" && it.origin == "Germanic" })
        assert(entries.any { it.nameRaw == "Liam" && it.meaning == "Strong-willed warrior" && it.origin == "Irish" })
        assert(entries.any { it.nameRaw == "Noname" && it.meaning == "Without origin" && it.origin == null })
    }

    // ── missing asset ─────────────────────────────────────────────────────────

    @Test
    fun `seedIfNeeded handles missing meanings asset without throwing`() = runTest {
        coEvery { prefs.isMeaningsSeeded() } returns false
        every { assetManager.open("meanings.json") } throws FileNotFoundException("meanings.json")

        seeder.seedIfNeeded() // must not throw

        coVerify(exactly = 0) { dao.updateMeaningsBatch(any()) }
        coVerify(exactly = 0) { prefs.markMeaningsSeeded() }
    }
}
