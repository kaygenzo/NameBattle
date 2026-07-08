package com.telen.namebattle.data.repository

import com.telen.namebattle.data.local.dao.SessionDao
import com.telen.namebattle.data.local.entity.ParentEntity
import com.telen.namebattle.data.local.entity.SessionEntity
import com.telen.namebattle.data.local.entity.ShortlistEntryEntity
import com.telen.namebattle.data.mapper.gender
import com.telen.namebattle.data.mapper.toDb
import com.telen.namebattle.data.mapper.toDomain
import com.telen.namebattle.domain.model.Gender
import com.telen.namebattle.domain.model.Parent
import com.telen.namebattle.domain.model.Session
import com.telen.namebattle.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow

class SessionRepositoryImpl(private val dao: SessionDao) : SessionRepository {

    override suspend fun createSession(
        gender: Gender,
        parent1Name: String, parent1PasswordHash: String,
        parent2Name: String?, parent2PasswordHash: String?
    ): Session {
        val sessionId = dao.insertSession(
            SessionEntity(gender = gender.toDb(), createdAt = System.currentTimeMillis())
        )
        dao.insertParent(
            ParentEntity(
                sessionId = sessionId,
                name = parent1Name,
                passwordHash = parent1PasswordHash,
                parentIndex = 0
            )
        )
        if (parent2Name != null && parent2PasswordHash != null) {
            dao.insertParent(
                ParentEntity(
                    sessionId = sessionId,
                    name = parent2Name,
                    passwordHash = parent2PasswordHash,
                    parentIndex = 1
                )
            )
        }

        val session = dao.getSessionById(sessionId)!!
        val parents = dao.getParentsBySession(sessionId)
        val p1 = parents.first { it.parentIndex == 0 }.toDomain()
        val p2 = parents.firstOrNull { it.parentIndex == 1 }?.toDomain()
        return Session(
            id = session.id,
            gender = session.gender(),
            parent1 = p1,
            parent2 = p2,
            createdAt = session.createdAt
        )
    }

    override suspend fun getAllSessions(): List<Session> {
        return dao.getAllSessions().mapNotNull { entity ->
            val parents = dao.getParentsBySession(entity.id)
            val p1 = parents.firstOrNull { it.parentIndex == 0 }?.toDomain()
                ?: return@mapNotNull null
            val p2 = parents.firstOrNull { it.parentIndex == 1 }?.toDomain()
            Session(
                id = entity.id,
                gender = entity.gender(),
                parent1 = p1,
                parent2 = p2,
                createdAt = entity.createdAt,
            )
        }
    }

    override suspend fun getSessionById(sessionId: Long): Session? {
        val entity = dao.getSessionById(sessionId) ?: return null
        val parents = dao.getParentsBySession(entity.id)
        val p1 = parents.firstOrNull { it.parentIndex == 0 }?.toDomain() ?: return null
        val p2 = parents.firstOrNull { it.parentIndex == 1 }?.toDomain()
        return Session(
            id = entity.id,
            gender = entity.gender(),
            parent1 = p1,
            parent2 = p2,
            createdAt = entity.createdAt,
        )
    }

    override suspend fun deleteSession(sessionId: Long) = dao.deleteSession(sessionId)

    override suspend fun getParentBySession(sessionId: Long, parentIndex: Int): Parent? =
        dao.getParentByIndex(sessionId, parentIndex)?.toDomain()

    override suspend fun markParentAuthenticated(parentId: Long) = dao.markAuthenticated(parentId)

    override suspend fun validateParentList(parentId: Long) = dao.validateList(parentId)

    override suspend fun addNameToShortlist(parentId: Long, firstNameId: Long) =
        dao.addNameToShortlist(ShortlistEntryEntity(parentId, firstNameId))

    override suspend fun removeNameFromShortlist(parentId: Long, firstNameId: Long) {
        dao.removeNameFromShortlist(parentId, firstNameId)
        if (dao.getShortlistIds(parentId).isEmpty()) dao.invalidateList(parentId)
    }

    override suspend fun getShortlistIds(parentId: Long): List<Long> =
        dao.getShortlistIds(parentId)

    override fun getShortlistIdsFlow(parentId: Long): Flow<List<Long>> =
        dao.getShortlistIdsFlow(parentId)

    override suspend fun isNameInShortlist(parentId: Long, firstNameId: Long): Boolean =
        dao.isInList(parentId, firstNameId) > 0
}
