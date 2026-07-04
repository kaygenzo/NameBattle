package com.telen.namebattle.data.repository

import android.content.Context
import com.telen.namebattle.data.local.dao.FirstNameDao
import com.telen.namebattle.data.local.entity.FirstNameEntity
import com.telen.namebattle.data.remote.MeaningInfo
import com.telen.namebattle.data.remote.MeaningRemoteDataSource
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FirstNameRepositoryImplTest {

    private lateinit var dao: FirstNameDao
    private lateinit var meaningRemote: MeaningRemoteDataSource
    private lateinit var repository: FirstNameRepositoryImpl

    @Before
    fun setUp() {
        dao = mockk()
        val context = mockk<Context>(relaxed = true)
        meaningRemote = mockk(relaxed = true)
        repository = FirstNameRepositoryImpl(dao, meaningRemote)
    }

    // ── searchByFirstLetter ───────────────────────────────────────────────────

    @Test
    fun `searchByFirstLetter normalizes accented letter E acute to E`() = runTest {
        // given
        val letterSlot = slot<String>()
        every { dao.searchByFirstLetter(capture(letterSlot), any()) } returns flowOf(emptyList())

        // when
        repository.searchByFirstLetter('É', Gender.BOY).first()

        // then
        assertEquals("E", letterSlot.captured)
    }

    @Test
    fun `searchByFirstLetter maps entities to domain models`() = runTest {
        // given
        val entity = FirstNameEntity(
            id = 1L, name = "Emma", nameLower = "emma", firstLetter = "E",
            gender = "GIRL", births1900 = 100, births1980 = 50, births2000 = 20, births2010 = 10
        )
        every { dao.searchByFirstLetter("E", "GIRL") } returns flowOf(listOf(entity))

        // when
        val result = repository.searchByFirstLetter('E', Gender.GIRL).first()

        // then
        assertEquals(1, result.size)
        assertEquals("Emma", result[0].name)
        assertEquals(Gender.GIRL, result[0].gender)
    }

    // ── getTopNames ───────────────────────────────────────────────────────────

    @Test
    fun `getTopNames dispatches to getTopSince2010 when fromYear is 2010`() = runTest {
        // given
        every { dao.getTopSince2010("BOY") } returns flowOf(emptyList())

        // when
        repository.getTopNames(Gender.BOY, fromYear = 2010).first()

        // then
        verify(exactly = 1) { dao.getTopSince2010("BOY") }
    }

    @Test
    fun `getTopNames dispatches to getTopSince2000 when fromYear is 2000`() = runTest {
        // given
        every { dao.getTopSince2000("BOY") } returns flowOf(emptyList())

        // when
        repository.getTopNames(Gender.BOY, fromYear = 2000).first()

        // then
        verify(exactly = 1) { dao.getTopSince2000("BOY") }
    }

    @Test
    fun `getTopNames dispatches to getTopSince1980 when fromYear is 1980`() = runTest {
        // given
        every { dao.getTopSince1980("BOY") } returns flowOf(emptyList())

        // when
        repository.getTopNames(Gender.BOY, fromYear = 1980).first()

        // then
        verify(exactly = 1) { dao.getTopSince1980("BOY") }
    }

    @Test
    fun `getTopNames dispatches to getTopSince1900 when fromYear is 1900`() = runTest {
        // given
        every { dao.getTopSince1900("BOY") } returns flowOf(emptyList())

        // when
        repository.getTopNames(Gender.BOY, fromYear = 1900).first()

        // then
        verify(exactly = 1) { dao.getTopSince1900("BOY") }
    }

    @Test
    fun `getTopNames dispatches to getTopSince1900 when fromYear is before 1980`() = runTest {
        // given
        every { dao.getTopSince1900("GIRL") } returns flowOf(emptyList())

        // when
        repository.getTopNames(Gender.GIRL, fromYear = 1950).first()

        // then
        verify(exactly = 1) { dao.getTopSince1900("GIRL") }
    }

    // ── getNameDetail ─────────────────────────────────────────────────────────

    @Test
    fun `getNameDetail returns local when meaning already cached`() = runTest {
        // given
        val entity = FirstNameEntity(
            id = 1L, name = "Emma", nameLower = "emma", firstLetter = "E",
            gender = "GIRL", births1900 = 100, births1980 = 50, births2000 = 20, births2010 = 10,
            meaning = "Universal", hasMeaning = true
        )
        coEvery { dao.getById(1L) } returns entity

        // when
        val result = repository.getNameDetail(1L)

        // then
        assertNotNull(result)
        assertEquals("Universal", result?.meaning)
        coVerify(exactly = 0) { meaningRemote.meaningFor(any()) }
    }

    @Test
    fun `getNameDetail returns null when name not found`() = runTest {
        // given
        coEvery { dao.getById(99L) } returns null

        // when
        val result = repository.getNameDetail(99L)

        // then
        assertNull(result)
    }

    @Test
    fun `getNameDetail fetches remote meaning when local has none and updates dao`() = runTest {
        // given
        val entity = FirstNameEntity(
            id = 1L, name = "Emma", nameLower = "emma", firstLetter = "E",
            gender = "GIRL", births1900 = 100, births1980 = 50, births2000 = 20, births2010 = 10,
            meaning = null
        )
        coEvery { dao.getById(1L) } returns entity
        coEvery { meaningRemote.meaningFor("Emma") } returns MeaningInfo(meaning = "Universal", origin = "Latin")
        coEvery { dao.updateMeaning(1L, "Universal", "Latin") } returns Unit

        // when
        val result = repository.getNameDetail(1L)

        // then
        assertNotNull(result)
        assertEquals("Universal", result?.meaning)
        assertEquals("Latin", result?.origin)
        assertTrue(result?.hasMeaning == true)
        coVerify(exactly = 1) { dao.updateMeaning(1L, "Universal", "Latin") }
    }

    @Test
    fun `getNameDetail returns local without meaning when remote returns null`() = runTest {
        // given
        val entity = FirstNameEntity(
            id = 1L, name = "Emma", nameLower = "emma", firstLetter = "E",
            gender = "GIRL", births1900 = 100, births1980 = 50, births2000 = 20, births2010 = 10,
            meaning = null
        )
        coEvery { dao.getById(1L) } returns entity
        coEvery { meaningRemote.meaningFor(any()) } returns null

        // when
        val result = repository.getNameDetail(1L)

        // then
        assertNotNull(result)
        assertNull(result?.meaning)
        coVerify(exactly = 0) { dao.updateMeaning(any(), any(), any()) }
    }

    // ── getOrCreateCustom ─────────────────────────────────────────────────────

    @Test
    fun `getOrCreateCustom returns existing when name found in db`() = runTest {
        // given
        val entity = FirstNameEntity(
            id = 5L, name = "Lyla", nameLower = "lyla", firstLetter = "L",
            gender = "GIRL", births1900 = 0, births1980 = 0, births2000 = 0, births2010 = 0,
            isCustom = true
        )
        coEvery { dao.findByName("Lyla", "GIRL") } returns entity

        // when
        val result = repository.getOrCreateCustom("Lyla", Gender.GIRL)

        // then
        assertEquals(5L, result.id)
        assertEquals("Lyla", result.name)
        coVerify(exactly = 0) { dao.insert(any()) }
    }

    @Test
    fun `getOrCreateCustom creates new entry when not found, returns with new id`() = runTest {
        // given
        coEvery { dao.findByName("Zara", "GIRL") } returns null
        coEvery { dao.insert(any()) } returns 99L

        // when
        val result = repository.getOrCreateCustom("Zara", Gender.GIRL)

        // then
        assertEquals(99L, result.id)
        assertEquals("Zara", result.name)
        coVerify(exactly = 1) { dao.insert(any()) }
    }

    @Test
    fun `getOrCreateCustom falls back to refetch when insert returns 0`() = runTest {
        // given
        val existingEntity = FirstNameEntity(
            id = 7L, name = "Zara", nameLower = "zara", firstLetter = "Z",
            gender = "GIRL", births1900 = 0, births1980 = 0, births2000 = 0, births2010 = 0,
            isCustom = true
        )
        coEvery { dao.findByName("Zara", "GIRL") } returns null andThen existingEntity
        coEvery { dao.insert(any()) } returns 0L

        // when
        val result = repository.getOrCreateCustom("Zara", Gender.GIRL)

        // then
        assertEquals(7L, result.id)
        assertEquals("Zara", result.name)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `getOrCreateCustom throws when name is blank`() = runTest {
        // when
        repository.getOrCreateCustom("   ", Gender.GIRL)
    }

    // ── namesWithMeaning ─────────────────────────────────────────────────────

    @Test
    fun `namesWithMeaning delegates to meaningRemote`() = runTest {
        // given
        val names = setOf("EMMA", "LYLA", "ZARA")
        coEvery { meaningRemote.availableNames() } returns names

        // when
        val result = repository.namesWithMeaning()

        // then
        assertEquals(names, result)
    }

    // ── count ─────────────────────────────────────────────────────────────────

    @Test
    fun `count delegates to dao`() = runTest {
        // given
        coEvery { dao.count() } returns 42

        // when
        val result = repository.count()

        // then
        assertEquals(42, result)
    }
}
