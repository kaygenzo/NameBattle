package com.telen.namebattle.domain.repository

import com.telen.namebattle.domain.model.FirstName
import com.telen.namebattle.domain.model.Gender
import kotlinx.coroutines.flow.Flow

interface FirstNameRepository {
    fun searchByFirstLetter(letter: Char, gender: Gender): Flow<List<FirstName>>
    fun searchFreeText(query: String, gender: Gender): Flow<List<FirstName>>
    fun getTopNames(gender: Gender, fromYear: Int): Flow<List<FirstName>>
    suspend fun getById(id: Long): FirstName?
    suspend fun getByIds(ids: List<Long>): List<FirstName>

    /**
     * Like [getById] but lazily resolves meaning + origin from the remote catalog
     * (and caches them locally) when they are not yet present.
     */
    suspend fun getNameDetail(id: Long): FirstName?

    /** Raw (uppercase) names that have a meaning available, to flag them in lists. */
    suspend fun namesWithMeaning(): Set<String>

    /**
     * Returns the existing row matching this spelling+gender, or creates a custom
     * (user-typed) one. Lets parents add foreign/original spellings absent from INSEE.
     */
    suspend fun getOrCreateCustom(name: String, gender: Gender): FirstName

    suspend fun updateMeaning(firstNameId: Long, meaning: String)
    suspend fun count(): Int
}
