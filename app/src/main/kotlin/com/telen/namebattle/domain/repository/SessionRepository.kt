package com.telen.namebattle.domain.repository

import com.telen.namebattle.domain.model.Gender
import com.telen.namebattle.domain.model.Parent
import com.telen.namebattle.domain.model.Session
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    suspend fun createSession(
        gender: Gender,
        parent1Name: String, parent1PasswordHash: String,
        parent2Name: String?, parent2PasswordHash: String?
    ): Session

    suspend fun getAllSessions(): List<Session>
    suspend fun getSessionById(sessionId: Long): Session?
    suspend fun deleteSession(sessionId: Long)

    suspend fun getParentBySession(sessionId: Long, parentIndex: Int): Parent?
    suspend fun markParentAuthenticated(parentId: Long)
    suspend fun validateParentList(parentId: Long)

    suspend fun addNameToShortlist(parentId: Long, firstNameId: Long)
    suspend fun removeNameFromShortlist(parentId: Long, firstNameId: Long)
    suspend fun getShortlistIds(parentId: Long): List<Long>
    fun getShortlistIdsFlow(parentId: Long): Flow<List<Long>>
    suspend fun isNameInShortlist(parentId: Long, firstNameId: Long): Boolean
}
