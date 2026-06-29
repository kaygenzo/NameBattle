package com.telen.namebattle.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.telen.namebattle.data.local.entity.ParentEntity
import com.telen.namebattle.data.local.entity.SessionEntity
import com.telen.namebattle.data.local.entity.ShortlistEntryEntity

@Dao
interface SessionDao {

    // ── Sessions ──────────────────────────────────────────────────
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity): Long

    @Query("SELECT * FROM sessions ORDER BY created_at DESC")
    suspend fun getAllSessions(): List<SessionEntity>

    @Query("SELECT * FROM sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionById(id: Long): SessionEntity?

    @Query("DELETE FROM sessions WHERE id = :id")
    suspend fun deleteSession(id: Long)

    @Query("UPDATE sessions SET battle_state_json = :json WHERE id = :sessionId")
    suspend fun updateBattleState(sessionId: Long, json: String?)

    // ── Parents ───────────────────────────────────────────────────
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParent(parent: ParentEntity): Long

    @Query("SELECT * FROM parents WHERE session_id = :sessionId AND parent_index = :index LIMIT 1")
    suspend fun getParentByIndex(sessionId: Long, index: Int): ParentEntity?

    @Query("SELECT * FROM parents WHERE session_id = :sessionId ORDER BY parent_index ASC")
    suspend fun getParentsBySession(sessionId: Long): List<ParentEntity>

    @Query("UPDATE parents SET is_authenticated = 1 WHERE id = :parentId")
    suspend fun markAuthenticated(parentId: Long)

    @Query("UPDATE parents SET list_validated = 1 WHERE id = :parentId")
    suspend fun validateList(parentId: Long)

    @Query("UPDATE parents SET list_validated = 0 WHERE id = :parentId")
    suspend fun invalidateList(parentId: Long)

    // ── FirstName list entries ────────────────────────────────────────
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addNameToShortlist(entry: ShortlistEntryEntity)

    @Query("DELETE FROM shortlist_entries WHERE parent_id = :parentId AND first_name_id = :firstNameId")
    suspend fun removeNameFromShortlist(parentId: Long, firstNameId: Long)

    @Query("SELECT first_name_id FROM shortlist_entries WHERE parent_id = :parentId")
    suspend fun getShortlistIds(parentId: Long): List<Long>

    @Query("SELECT first_name_id FROM shortlist_entries WHERE parent_id = :parentId")
    fun getShortlistIdsFlow(parentId: Long): kotlinx.coroutines.flow.Flow<List<Long>>

    @Query("SELECT COUNT(*) FROM shortlist_entries WHERE parent_id = :parentId AND first_name_id = :firstNameId")
    suspend fun isInList(parentId: Long, firstNameId: Long): Int
}
