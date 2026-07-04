package com.telen.namebattle.data.repository

import com.telen.namebattle.data.local.dao.SessionDao
import com.telen.namebattle.data.local.entity.ParentEntity
import com.telen.namebattle.data.local.entity.SessionEntity
import com.telen.namebattle.data.local.entity.ShortlistEntryEntity
import com.telen.namebattle.domain.model.Gender
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SessionRepositoryImplTest {

    private lateinit var dao: SessionDao
    private lateinit var repository: SessionRepositoryImpl

    @Before
    fun setUp() {
        dao = mockk()
        repository = SessionRepositoryImpl(dao)
    }

    // ── createSession ─────────────────────────────────────────────────────────

    @Test
    fun `createSession inserts session and parent1 only in solo mode`() = runTest {
        // given
        val sessionEntity = SessionEntity(id = 1L, gender = "BOY", createdAt = 0L)
        val parent1Entity = ParentEntity(id = 10L, sessionId = 1L, name = "Alice", passwordHash = "hash1", parentIndex = 0)
        coEvery { dao.insertSession(any()) } returns 1L
        coEvery { dao.insertParent(any()) } returns 10L
        coEvery { dao.getSessionById(1L) } returns sessionEntity
        coEvery { dao.getParentsBySession(1L) } returns listOf(parent1Entity)

        // when
        repository.createSession(Gender.BOY, "Alice", "hash1", null, null)

        // then
        coVerify(exactly = 1) { dao.insertParent(any()) }
    }

    @Test
    fun `createSession inserts session and both parents in duo mode`() = runTest {
        // given
        val sessionEntity = SessionEntity(id = 1L, gender = "GIRL", createdAt = 0L)
        val parent1Entity = ParentEntity(id = 10L, sessionId = 1L, name = "Alice", passwordHash = "hash1", parentIndex = 0)
        val parent2Entity = ParentEntity(id = 11L, sessionId = 1L, name = "Bob", passwordHash = "hash2", parentIndex = 1)
        coEvery { dao.insertSession(any()) } returns 1L
        coEvery { dao.insertParent(any()) } returns 10L andThen 11L
        coEvery { dao.getSessionById(1L) } returns sessionEntity
        coEvery { dao.getParentsBySession(1L) } returns listOf(parent1Entity, parent2Entity)

        // when
        repository.createSession(Gender.GIRL, "Alice", "hash1", "Bob", "hash2")

        // then
        coVerify(exactly = 2) { dao.insertParent(any()) }
    }

    @Test
    fun `createSession returns session with correct gender`() = runTest {
        // given
        val sessionEntity = SessionEntity(id = 1L, gender = "GIRL", createdAt = 1000L)
        val parent1Entity = ParentEntity(id = 10L, sessionId = 1L, name = "Alice", passwordHash = "hash1", parentIndex = 0)
        coEvery { dao.insertSession(any()) } returns 1L
        coEvery { dao.insertParent(any()) } returns 10L
        coEvery { dao.getSessionById(1L) } returns sessionEntity
        coEvery { dao.getParentsBySession(1L) } returns listOf(parent1Entity)

        // when
        val result = repository.createSession(Gender.GIRL, "Alice", "hash1", null, null)

        // then
        assertEquals(Gender.GIRL, result.gender)
    }

    // ── getAllSessions ────────────────────────────────────────────────────────

    @Test
    fun `getAllSessions skips sessions without parent1`() = runTest {
        // given
        val sessionWithoutParent1 = SessionEntity(id = 2L, gender = "BOY", createdAt = 0L)
        coEvery { dao.getAllSessions() } returns listOf(sessionWithoutParent1)
        coEvery { dao.getParentsBySession(2L) } returns emptyList()

        // when
        val result = repository.getAllSessions()

        // then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getAllSessions returns sessions with parent2`() = runTest {
        // given
        val sessionEntity = SessionEntity(id = 1L, gender = "BOY", createdAt = 0L)
        val parent1Entity = ParentEntity(id = 10L, sessionId = 1L, name = "Alice", passwordHash = "hash1", parentIndex = 0)
        val parent2Entity = ParentEntity(id = 11L, sessionId = 1L, name = "Bob", passwordHash = "hash2", parentIndex = 1)
        coEvery { dao.getAllSessions() } returns listOf(sessionEntity)
        coEvery { dao.getParentsBySession(1L) } returns listOf(parent1Entity, parent2Entity)

        // when
        val result = repository.getAllSessions()

        // then
        assertEquals(1, result.size)
        assertNotNull(result[0].parent2)
        assertEquals("Bob", result[0].parent2?.name)
    }

    // ── getSessionById ────────────────────────────────────────────────────────

    @Test
    fun `getSessionById returns null when not found`() = runTest {
        // given
        coEvery { dao.getSessionById(99L) } returns null

        // when
        val result = repository.getSessionById(99L)

        // then
        assertNull(result)
    }

    @Test
    fun `getSessionById returns null when parent1 missing`() = runTest {
        // given
        val sessionEntity = SessionEntity(id = 1L, gender = "BOY", createdAt = 0L)
        coEvery { dao.getSessionById(1L) } returns sessionEntity
        coEvery { dao.getParentsBySession(1L) } returns emptyList()

        // when
        val result = repository.getSessionById(1L)

        // then
        assertNull(result)
    }

    // ── deleteSession ─────────────────────────────────────────────────────────

    @Test
    fun `deleteSession delegates to dao`() = runTest {
        // given
        coEvery { dao.deleteSession(5L) } returns Unit

        // when
        repository.deleteSession(5L)

        // then
        coVerify(exactly = 1) { dao.deleteSession(5L) }
    }

    // ── removeNameFromShortlist ───────────────────────────────────────────────

    @Test
    fun `removeNameFromShortlist does NOT invalidate list when items remain`() = runTest {
        // given
        coEvery { dao.removeNameFromShortlist(1L, 2L) } returns Unit
        coEvery { dao.getShortlistIds(1L) } returns listOf(1L)
        coEvery { dao.invalidateList(any()) } returns Unit

        // when
        repository.removeNameFromShortlist(parentId = 1L, firstNameId = 2L)

        // then
        coVerify(exactly = 0) { dao.invalidateList(any()) }
    }

    @Test
    fun `removeNameFromShortlist invalidates list when empty after removal`() = runTest {
        // given
        coEvery { dao.removeNameFromShortlist(1L, 2L) } returns Unit
        coEvery { dao.getShortlistIds(1L) } returns emptyList()
        coEvery { dao.invalidateList(1L) } returns Unit

        // when
        repository.removeNameFromShortlist(parentId = 1L, firstNameId = 2L)

        // then
        coVerify(exactly = 1) { dao.invalidateList(1L) }
    }

    // ── markParentAuthenticated ───────────────────────────────────────────────

    @Test
    fun `markParentAuthenticated delegates to dao`() = runTest {
        // given
        coEvery { dao.markAuthenticated(7L) } returns Unit

        // when
        repository.markParentAuthenticated(7L)

        // then
        coVerify(exactly = 1) { dao.markAuthenticated(7L) }
    }

    // ── validateParentList ────────────────────────────────────────────────────

    @Test
    fun `validateParentList delegates to dao`() = runTest {
        // given
        coEvery { dao.validateList(7L) } returns Unit

        // when
        repository.validateParentList(7L)

        // then
        coVerify(exactly = 1) { dao.validateList(7L) }
    }

    // ── isNameInShortlist ─────────────────────────────────────────────────────

    @Test
    fun `isNameInShortlist returns true when count is greater than 0`() = runTest {
        // given
        coEvery { dao.isInList(1L, 2L) } returns 1

        // when
        val result = repository.isNameInShortlist(parentId = 1L, firstNameId = 2L)

        // then
        assertTrue(result)
    }

    @Test
    fun `isNameInShortlist returns false when count is 0`() = runTest {
        // given
        coEvery { dao.isInList(1L, 2L) } returns 0

        // when
        val result = repository.isNameInShortlist(parentId = 1L, firstNameId = 2L)

        // then
        assertFalse(result)
    }

    // ── getShortlistIdsFlow ───────────────────────────────────────────────────

    @Test
    fun `getShortlistIdsFlow delegates to dao`() = runTest {
        // given
        val expectedIds = listOf(1L, 2L, 3L)
        every { dao.getShortlistIdsFlow(1L) } returns flowOf(expectedIds)

        // when
        val result = repository.getShortlistIdsFlow(1L).first()

        // then
        assertEquals(expectedIds, result)
        verify(exactly = 1) { dao.getShortlistIdsFlow(1L) }
    }
}
