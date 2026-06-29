package com.telen.namebattle.data.mapper

import com.telen.namebattle.data.local.entity.FirstNameEntity
import com.telen.namebattle.domain.model.FirstName
import com.telen.namebattle.domain.model.Gender

fun FirstNameEntity.toDomain() = FirstName(
    id = id,
    name = name,
    gender = when (gender) {
        "GIRL" -> Gender.GIRL
        else -> Gender.BOY
    },
    birthsSince1900 = births1900,
    birthsSince1980 = births1980,
    birthsSince2000 = births2000,
    birthsSince2010 = births2010,
    totalBirths = totalBirths,
    peakYear = peakYear,
    firstYear = firstYear,
    origin = origin,
    meaning = meaning,
    hasMeaning = hasMeaning
)
