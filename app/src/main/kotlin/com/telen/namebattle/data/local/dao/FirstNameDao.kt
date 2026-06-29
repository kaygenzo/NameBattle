package com.telen.namebattle.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.telen.namebattle.data.local.entity.FirstNameEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FirstNameDao {

    @Query(
        """
        SELECT * FROM first_names
        WHERE first_letter = UPPER(:letter)
          AND gender = :gender
          AND is_custom = 0
        ORDER BY name ASC
    """
    )
    fun searchByFirstLetter(letter: String, gender: String): Flow<List<FirstNameEntity>>

    @Query(
        """
        SELECT * FROM first_names
        WHERE name_lower LIKE '%' || LOWER(:query) || '%'
          AND gender = :gender
          AND is_custom = 0
        ORDER BY name ASC
        LIMIT 60
    """
    )
    fun searchFreeText(query: String, gender: String): Flow<List<FirstNameEntity>>

    @Query(
        """
        SELECT * FROM first_names
        WHERE gender = :gender AND is_custom = 0
        ORDER BY births_1900 DESC
        LIMIT 100
    """
    )
    fun getTopSince1900(gender: String): Flow<List<FirstNameEntity>>

    @Query(
        """
        SELECT * FROM first_names
        WHERE gender = :gender AND is_custom = 0
        ORDER BY births_1980 DESC
        LIMIT 100
    """
    )
    fun getTopSince1980(gender: String): Flow<List<FirstNameEntity>>

    @Query(
        """
        SELECT * FROM first_names
        WHERE gender = :gender AND is_custom = 0
        ORDER BY births_2000 DESC
        LIMIT 100
    """
    )
    fun getTopSince2000(gender: String): Flow<List<FirstNameEntity>>

    @Query(
        """
        SELECT * FROM first_names
        WHERE gender = :gender AND is_custom = 0
        ORDER BY births_2010 DESC
        LIMIT 100
    """
    )
    fun getTopSince2010(gender: String): Flow<List<FirstNameEntity>>

    @Query("SELECT * FROM first_names WHERE id = :id")
    suspend fun getById(id: Long): FirstNameEntity?

    @Query("SELECT * FROM first_names WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<Long>): List<FirstNameEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(names: List<FirstNameEntity>)

    @Query(
        """
        SELECT * FROM first_names
        WHERE name_lower = LOWER(:name) AND gender = :gender
        LIMIT 1
    """
    )
    suspend fun findByName(name: String, gender: String): FirstNameEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(name: FirstNameEntity): Long

    @Query("SELECT COUNT(*) FROM first_names")
    suspend fun count(): Int

    @Query("UPDATE first_names SET meaning = :meaning, origin = :origin, has_meaning = 1 WHERE id = :id")
    suspend fun updateMeaning(id: Long, meaning: String, origin: String?)
}
