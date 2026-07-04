package com.telen.namebattle.data.repository

import com.telen.namebattle.data.local.dao.FirstNameDao
import com.telen.namebattle.data.local.entity.FirstNameEntity
import com.telen.namebattle.domain.model.Gender
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class FirstNameRepositoryImplTest {

    private lateinit var dao: FirstNameDao
    private lateinit var repository: FirstNameRepositoryImpl

    @Before
    fun setUp() {
        dao = mockk()
        repository = FirstNameRepositoryImpl(dao)
    }

    // ── searchByFirstLetter ───────────────────────────────────────────────────

    @Test
    fun `searchByFirstLetter normalizes accented letter E acute to E`() = runTest {
        val letterSlot = slot<String>()
        every { dao.searchByFirstLetter(capture(letterSlot), any()) } returns flowOf(emptyList())

        repository.searchByFirstLetter('É', Gender.BOY).first()

        assertEquals("E", letterSlot.captured)
    }

    @Test
    fun `searchByFirstLetter maps entities to domain models`() = runTest {
        val entity = FirstNameEntity(
            id = 1L, name = "Emma", nameLower = "emma", firstLetter = "E",
            gender = "GIRL", births1900 = 100, births1980 = 50, births2000 = 20, births2010 = 10
        )
        every { dao.searchByFirstLetter("E", "GIRL") } returns flowOf(listOf(entity))

        val result = repository.searchByFirstLetter('E', Gender.GIRL).first()

        assertEquals(1, result.size)
        assertEquals("Emma", result[0].name)
        assertEquals(Gender.GIRL, result[0].gender)
    }

    // ── getTopNames ───────────────────────────────────────────────────────────

    @Test
    fun `getTopNames dispatches to getTopSince2010 when fromYear is 2010`() = runTest {
        every { dao.getTopSince2010("BOY") } returns flowOf(emptyList())

        repository.getTopNames(Gender.BOY, fromYear = 2010).first()

        verify(exactly = 1) { dao.getTopSince2010("BOY") }
    }

    @Test
    fun `getTopNames dispatches to getTopSince2000 when fromYear is 2000`() = runTest {
        every { dao.getTopSince2000("BOY") } returns flowOf(emptyList())

        repository.getTopNames(Gender.BOY, fromYear = 2000).first()

        verify(exactly = 1) { dao.getTopSince2000("BOY") }
    }

    @Test
    fun `getTopNames dispatches to getTopSince1980 when fromYear is 1980`() = runTest {
        every { dao.getTopSince1980("BOY") } returns flowOf(emptyList())

        repository.getTopNames(Gender.BOY, fromYear = 1980).first()

        verify(exactly = 1) { dao.getTopSince1980("BOY") }
    }

    @Test
    fun `getTopNames dispatches to getTopSince1900 when fromYear is 1900`() = runTest {
        every { dao.getTopSince1900("BOY") } returns flowOf(emptyList())

        repository.getTopNames(Gender.BOY, fromYear = 1900).first()

        verify(exactly = 1) { dao.getTopSince1900("BOY") }
    }

    @Test
    fun `getTopNames dispatches to getTopSince1900 when fromYear is before 1980`() = runTest {
        every { dao.getTopSince1900("GIRL") } returns flowOf(emptyList())

        repository.getTopNames(Gender.GIRL, fromYear = 1950).first()

        verify(exactly = 1) { dao.getTopSince1900("GIRL") }
    }

    // ── getNameDetail ─────────────────────────────────────────────────────────

    @Test
    fun `getNameDetail returns null when name not found`() = runTest {
        coEvery { dao.getById(99L) } returns null

        assertNull(repository.getNameDetail(99L))
    }

    @Test
    fun `getNameDetail returns domain model from DB with meaning when present`() = runTest {
        val entity = FirstNameEntity(
            id = 1L, name = "Emma", nameLower = "emma", firstLetter = "E",
            gender = "GIRL", births1900 = 100, births1980 = 50, births2000 = 20, births2010 = 10,
            meaning = "Universal", origin = "Germanic", hasMeaning = true
        )
        coEvery { dao.getById(1L) } returns entity

        val result = repository.getNameDetail(1L)

        assertNotNull(result)
        assertEquals("Universal", result?.meaning)
        assertEquals("Germanic", result?.origin)
    }

    @Test
    fun `getNameDetail returns domain model from DB without meaning when absent`() = runTest {
        val entity = FirstNameEntity(
            id = 1L, name = "Emma", nameLower = "emma", firstLetter = "E",
            gender = "GIRL", births1900 = 100, births1980 = 50, births2000 = 20, births2010 = 10,
            meaning = null
        )
        coEvery { dao.getById(1L) } returns entity

        val result = repository.getNameDetail(1L)

        assertNotNull(result)
        assertNull(result?.meaning)
        coVerify(exactly = 0) { dao.updateMeaning(any(), any(), any()) }
    }

    // ── namesWithMeaning ──────────────────────────────────────────────────────

    @Test
    fun `namesWithMeaning returns uppercased names from DB`() = runTest {
        coEvery { dao.getNamesWithMeaning() } returns listOf("EMMA", "LYLA", "ZARA")

        val result = repository.namesWithMeaning()

        assertEquals(setOf("EMMA", "LYLA", "ZARA"), result)
    }

    // ── getOrCreateCustom ─────────────────────────────────────────────────────

    @Test
    fun `getOrCreateCustom returns existing when name found in db`() = runTest {
        val entity = FirstNameEntity(
            id = 5L, name = "Lyla", nameLower = "lyla", firstLetter = "L",
            gender = "GIRL", births1900 = 0, births1980 = 0, births2000 = 0, births2010 = 0,
            isCustom = true
        )
        coEvery { dao.findByName("Lyla", "GIRL") } returns entity

        val result = repository.getOrCreateCustom("Lyla", Gender.GIRL)

        assertEquals(5L, result.id)
        assertEquals("Lyla", result.name)
        coVerify(exactly = 0) { dao.insert(any()) }
    }

    @Test
    fun `getOrCreateCustom creates new entry when not found, returns with new id`() = runTest {
        coEvery { dao.findByName("Zara", "GIRL") } returns null
        coEvery { dao.insert(any()) } returns 99L

        val result = repository.getOrCreateCustom("Zara", Gender.GIRL)

        assertEquals(99L, result.id)
        assertEquals("Zara", result.name)
        coVerify(exactly = 1) { dao.insert(any()) }
    }

    @Test
    fun `getOrCreateCustom falls back to refetch when insert returns 0`() = runTest {
        val existingEntity = FirstNameEntity(
            id = 7L, name = "Zara", nameLower = "zara", firstLetter = "Z",
            gender = "GIRL", births1900 = 0, births1980 = 0, births2000 = 0, births2010 = 0,
            isCustom = true
        )
        coEvery { dao.findByName("Zara", "GIRL") } returns null andThen existingEntity
        coEvery { dao.insert(any()) } returns 0L

        val result = repository.getOrCreateCustom("Zara", Gender.GIRL)

        assertEquals(7L, result.id)
        assertEquals("Zara", result.name)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `getOrCreateCustom throws when name is blank`() = runTest {
        repository.getOrCreateCustom("   ", Gender.GIRL)
    }

    // ── count ─────────────────────────────────────────────────────────────────

    @Test
    fun `count delegates to dao`() = runTest {
        coEvery { dao.count() } returns 42

        assertEquals(42, repository.count())
    }
}
