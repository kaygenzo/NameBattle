package com.telen.namebattle.data.local

import android.content.res.AssetManager
import android.content.Context
import com.telen.namebattle.data.local.dao.FirstNameDao
import com.telen.namebattle.data.local.entity.MeaningUpdate
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.FileNotFoundException

class DatabaseSeederTest {

    private lateinit var context: Context
    private lateinit var assetManager: AssetManager
    private lateinit var dao: FirstNameDao
    private lateinit var prefs: AppPreferences
    private lateinit var seeder: DatabaseSeeder

    private val namesJson = """
        [
            {
                "name":"Emma",
                "gender":"F",
                "total":100,
                "yearly_counts":{
                    "2000":50,
                    "2010":20
                },
                "peak_year":2010,
                "first_year":2000
            },
            {
                "name":"Liam",
                "gender":"M",
                "total":80,
                "yearly_counts":{
                    "2005":40
                },
                "peak_year":2005,
                "first_year":2003
            },
            {
                "name":"Alex",
                "gender":"X",
                "total":50,
                "yearly_counts":{},
                "peak_year":2000,
                "first_year":1995
            }
        ]
    """.trimIndent()

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

    // ── seedNames ─────────────────────────────────────────────────────────────

    @Test
    fun `seedIfNeeded seeds names when db not seeded`() = runTest {
        coEvery { prefs.isDbSeeded() } returns false
        coEvery { dao.count() } returns 0
        coEvery { prefs.isMeaningsSeeded() } returns true
        every {
            assetManager.open("prenoms_insee.json")
        } returns namesJson.toByteArray().inputStream()

        seeder.seedIfNeeded()

        coVerify(atLeast = 1) { dao.insertAll(any()) }
        coVerify(exactly = 1) { prefs.markDbSeeded() }
    }

    @Test
    fun `seedIfNeeded inserts one entity for female name and one for male`() = runTest {
        coEvery { prefs.isDbSeeded() } returns false
        coEvery { dao.count() } returns 0
        coEvery { prefs.isMeaningsSeeded() } returns true
        every {
            assetManager.open("prenoms_insee.json")
        } returns namesJson.toByteArray().inputStream()

        val captured = mutableListOf<List<com.telen.namebattle.data.local.entity.FirstNameEntity>>()
        coEvery { dao.insertAll(capture(captured)) } returns Unit

        seeder.seedIfNeeded()

        val all = captured.flatten()
        assertTrue(all.any { it.name == "Emma" && it.gender == "GIRL" })
        assertTrue(all.any { it.name == "Liam" && it.gender == "BOY" })
        assertTrue(all.any { it.name == "Alex" && it.gender == "BOY" })
        assertTrue(all.any { it.name == "Alex" && it.gender == "GIRL" })
    }

    @Test
    fun `seedIfNeeded computes birth counts correctly`() = runTest {
        coEvery { prefs.isDbSeeded() } returns false
        coEvery { dao.count() } returns 0
        coEvery { prefs.isMeaningsSeeded() } returns true
        every {
            assetManager.open("prenoms_insee.json")
        } returns namesJson.toByteArray().inputStream()

        val captured = slot<List<com.telen.namebattle.data.local.entity.FirstNameEntity>>()
        coEvery { dao.insertAll(capture(captured)) } returns Unit

        seeder.seedIfNeeded()

        val emma = captured.captured.first { it.name == "Emma" }
        // yearly_counts = {"2000":50,"2010":20} → sumFrom(1900) = 70, sumFrom(2000) = 70,
        // sumFrom(2010) = 20
        assertEquals(70, emma.births1900)
        assertEquals(70, emma.births2000)
        assertEquals(20, emma.births2010)
        assertEquals(100, emma.totalBirths)
    }

    @Test
    fun `seedIfNeeded handles missing names asset gracefully`() = runTest {
        coEvery { prefs.isDbSeeded() } returns false
        coEvery { dao.count() } returns 0
        coEvery { prefs.isMeaningsSeeded() } returns true
        every {
            assetManager.open("prenoms_insee.json")
        } throws FileNotFoundException("prenoms_insee.json")

        seeder.seedIfNeeded() // must not throw

        coVerify(exactly = 0) { dao.insertAll(any()) }
        coVerify(exactly = 0) { prefs.markDbSeeded() }
    }

    @Test
    fun `seedIfNeeded skips names seeding when already seeded and count is positive`() = runTest {
        coEvery { prefs.isDbSeeded() } returns true
        coEvery { dao.count() } returns 100
        coEvery { prefs.isMeaningsSeeded() } returns true

        seeder.seedIfNeeded()

        coVerify(exactly = 0) { dao.insertAll(any()) }
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
