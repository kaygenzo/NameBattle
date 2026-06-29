package com.telen.namebattle.data.local

import android.content.Context
import com.telen.namebattle.data.local.dao.FirstNameDao
import com.telen.namebattle.data.local.entity.FirstNameEntity
import com.telen.namebattle.util.toBaseAscii
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeToSequence
import timber.log.Timber

/**
 * Seeds the names table from assets/prenoms_insee.json on first launch.
 */
class DatabaseSeeder(
    private val context: Context,
    private val firstNameDao: FirstNameDao,
    private val prefs: AppPreferences
) {
    private val jsonFormat = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val mutex = Mutex()

    @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
    suspend fun seedIfNeeded() = mutex.withLock {
        withContext(Dispatchers.IO) {
            if (prefs.isDbSeeded() && firstNameDao.count() > 0) return@withContext
            try {
                context.assets.open("prenoms_insee.json").use { inputStream ->
                    var count = 0
                    jsonFormat.decodeToSequence<JsonFirstName>(inputStream)
                        .chunked(500)
                        .forEach { batch ->
                            val entities = batch.flatMap { dto ->
                                val genders = when (dto.gender.uppercase()) {
                                    "F" -> listOf("GIRL")
                                    "M" -> listOf("BOY")
                                    else -> listOf("BOY", "GIRL")
                                }

                                fun sumFrom(fromYear: Int): Int {
                                    if (dto.yearlyCounts.isEmpty()) return dto.total
                                    return dto.yearlyCounts
                                        .filterKeys { (it.toIntOrNull() ?: 0) >= fromYear }
                                        .values
                                        .sum()
                                }

                                genders.map { gender ->
                                    FirstNameEntity(
                                        name = dto.name,
                                        nameLower = dto.name.lowercase(),
                                        firstLetter = if (dto.name.isNotEmpty()) {
                                            dto.name.first().toBaseAscii().toString()
                                        } else {
                                            ""
                                        },
                                        gender = gender,
                                        births1900 = sumFrom(1900),
                                        births1980 = sumFrom(1980),
                                        births2000 = sumFrom(2000),
                                        births2010 = sumFrom(2010),
                                        totalBirths = dto.total,
                                        peakYear = dto.peakYear,
                                        firstYear = dto.firstYear
                                    )
                                }
                            }
                            firstNameDao.insertAll(entities)
                            count += entities.size
                        }
                    prefs.markDbSeeded()
                    Timber.i("DB seeded with $count prénoms")
                }
            } catch (e: Exception) {
                Timber.e(e, "Seeding failed")
            }
        }
    }
}

@Serializable
private data class JsonFirstName(
    val name: String,
    val gender: String,
    val total: Int = 0,
    @SerialName("yearly_counts") val yearlyCounts: Map<String, Int> = emptyMap(),
    @SerialName("peak_year") val peakYear: Int = 0,
    @SerialName("first_year") val firstYear: Int = 0
)
