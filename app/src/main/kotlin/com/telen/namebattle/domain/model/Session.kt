package com.telen.namebattle.domain.model

data class Session(
    val id: Long = 0,
    val gender: Gender,
    val parent1: Parent,
    val parent2: Parent?,
    val createdAt: Long = System.currentTimeMillis()
)
