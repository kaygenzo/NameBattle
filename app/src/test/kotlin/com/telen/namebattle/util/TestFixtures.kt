package com.telen.namebattle.util

import com.telen.namebattle.domain.model.BattleRound
import com.telen.namebattle.domain.model.BattleState
import com.telen.namebattle.domain.model.DuelState
import com.telen.namebattle.domain.model.FirstName
import com.telen.namebattle.domain.model.Gender
import com.telen.namebattle.domain.model.Parent
import com.telen.namebattle.domain.model.Session

fun buildParent(
    id: Long = 1L,
    sessionId: Long = 1L,
    name: String = "Alice",
    passwordHash: String = "abc123".sha256(),
    isAuthenticated: Boolean = false,
    listValidated: Boolean = false,
    parentIndex: Int = 0,
) = Parent(
    id = id,
    sessionId = sessionId,
    name = name,
    passwordHash = passwordHash,
    isAuthenticated = isAuthenticated,
    listValidated = listValidated,
    parentIndex = parentIndex,
)

fun buildSession(
    id: Long = 1L,
    gender: Gender = Gender.BOY,
    parent1: Parent = buildParent(id = 1L, parentIndex = 0),
    parent2: Parent? = null,
    createdAt: Long = 0L,
) = Session(
    id = id,
    gender = gender,
    parent1 = parent1,
    parent2 = parent2,
    createdAt = createdAt,
)

fun buildFirstName(
    id: Long = 1L,
    name: String = "Emma",
    gender: Gender = Gender.GIRL,
    birthsSince1900: Int = 100,
    birthsSince1980: Int = 50,
    birthsSince2000: Int = 20,
    birthsSince2010: Int = 10,
    totalBirths: Int = 100,
    peakYear: Int = 2005,
    firstYear: Int = 1920,
    origin: String? = null,
    meaning: String? = null,
    hasMeaning: Boolean = false,
) = FirstName(
    id = id,
    name = name,
    gender = gender,
    birthsSince1900 = birthsSince1900,
    birthsSince1980 = birthsSince1980,
    birthsSince2000 = birthsSince2000,
    birthsSince2010 = birthsSince2010,
    totalBirths = totalBirths,
    peakYear = peakYear,
    firstYear = firstYear,
    origin = origin,
    meaning = meaning,
    hasMeaning = hasMeaning,
)

fun buildBattleState(
    sessionId: Long = 1L,
    initialCount: Int = 4,
    targetFinalists: Int = 4,
    rounds: List<BattleRound> = listOf(
        BattleRound(
            roundNumber = 1,
            duels = listOf(
                DuelState(firstName1Id = 1L, firstName2Id = 2L),
                DuelState(firstName1Id = 3L, firstName2Id = 4L),
            ),
        )
    ),
    currentRoundIndex: Int = 0,
    currentDuelIndex: Int = 0,
    finalists: List<Long> = emptyList(),
    isComplete: Boolean = false,
) = BattleState(
    sessionId = sessionId,
    initialCount = initialCount,
    targetFinalists = targetFinalists,
    rounds = rounds,
    currentRoundIndex = currentRoundIndex,
    currentDuelIndex = currentDuelIndex,
    finalists = finalists,
    isComplete = isComplete,
)
