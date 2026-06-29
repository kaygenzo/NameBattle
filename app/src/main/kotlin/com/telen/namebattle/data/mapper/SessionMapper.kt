package com.telen.namebattle.data.mapper

import com.telen.namebattle.data.local.entity.ParentEntity
import com.telen.namebattle.data.local.entity.SessionEntity
import com.telen.namebattle.domain.model.Gender
import com.telen.namebattle.domain.model.Parent

fun SessionEntity.gender(): Gender = when (gender) {
    "BOY" -> Gender.BOY
    else -> Gender.GIRL
}

fun Gender.toDb(): String = when (this) {
    Gender.BOY -> "BOY"
    Gender.GIRL -> "GIRL"
}

fun ParentEntity.toDomain() = Parent(
    id = id,
    sessionId = sessionId,
    name = name,
    passwordHash = passwordHash,
    isAuthenticated = isAuthenticated,
    listValidated = listValidated,
    parentIndex = parentIndex
)
