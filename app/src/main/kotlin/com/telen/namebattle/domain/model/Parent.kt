package com.telen.namebattle.domain.model

data class Parent(
    val id: Long = 0,
    val sessionId: Long = 0,
    val name: String,
    val passwordHash: String,
    val isAuthenticated: Boolean = false,
    val listValidated: Boolean = false,
    val parentIndex: Int = 0
)
