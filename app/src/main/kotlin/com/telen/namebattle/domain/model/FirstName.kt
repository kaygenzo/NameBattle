package com.telen.namebattle.domain.model

data class FirstName(
    val id: Long = 0,
    val name: String,
    val gender: Gender,
    val birthsSince1900: Int,
    val birthsSince1980: Int,
    val birthsSince2000: Int,
    val birthsSince2010: Int,
    val totalBirths: Int = 0,
    val peakYear: Int = 0,
    val firstYear: Int = 0,
    val origin: String? = null,
    val meaning: String? = null,
    val hasMeaning: Boolean = false
)
