package com.telen.namebattle.data.repository

import com.telen.namebattle.data.local.dao.FirstNameDao
import com.telen.namebattle.data.local.entity.FirstNameEntity
import com.telen.namebattle.data.mapper.toDomain
import com.telen.namebattle.domain.model.FirstName
import com.telen.namebattle.domain.model.Gender
import com.telen.namebattle.domain.repository.FirstNameRepository
import com.telen.namebattle.util.toBaseAscii
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FirstNameRepositoryImpl(
    private val dao: FirstNameDao,
) : FirstNameRepository {

    override fun searchByFirstLetter(letter: Char, gender: Gender): Flow<List<FirstName>> =
        dao.searchByFirstLetter(letter.toBaseAscii().toString(), gender.toDb())
            .map { list -> list.map { it.toDomain() } }

    override fun searchFreeText(query: String, gender: Gender): Flow<List<FirstName>> =
        dao.searchFreeText(query, gender.toDb())
            .map { list -> list.map { it.toDomain() } }

    override fun getTopNames(gender: Gender, fromYear: Int): Flow<List<FirstName>> {
        val db = gender.toDb()
        return when {
            fromYear >= 2010 -> dao.getTopSince2010(db)
            fromYear >= 2000 -> dao.getTopSince2000(db)
            fromYear >= 1980 -> dao.getTopSince1980(db)
            else -> dao.getTopSince1900(db)
        }.map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getById(id: Long): FirstName? = dao.getById(id)?.toDomain()

    override suspend fun getByIds(ids: List<Long>): List<FirstName> =
        dao.getByIds(ids).map { it.toDomain() }

    override suspend fun getNameDetail(id: Long): FirstName? = dao.getById(id)?.toDomain()

    override suspend fun namesWithMeaning(): Set<String> =
        dao.getNamesWithMeaning().toSet()

    override suspend fun getOrCreateCustom(name: String, gender: Gender): FirstName {
        val trimmed = name.trim()
        require(trimmed.isNotEmpty()) { "Custom name cannot be blank" }
        dao.findByName(trimmed, gender.toDb())?.let { return it.toDomain() }
        val entity = FirstNameEntity(
            name = trimmed,
            nameLower = trimmed.lowercase(),
            firstLetter = trimmed.first().toBaseAscii().toString(),
            gender = gender.toDb(),
            births1900 = 0, births1980 = 0, births2000 = 0, births2010 = 0,
            isCustom = true,
        )
        val id = dao.insert(entity)
        return when {
            id > 0 -> entity.copy(id = id).toDomain()
            else -> dao.findByName(trimmed, gender.toDb())?.toDomain() ?: entity.toDomain()
        }
    }

    override suspend fun updateMeaning(firstNameId: Long, meaning: String) =
        dao.updateMeaning(firstNameId, meaning, null)

    override suspend fun count(): Int = dao.count()

    private fun Gender.toDb() = when (this) {
        Gender.BOY -> "BOY"
        Gender.GIRL -> "GIRL"
    }
}
